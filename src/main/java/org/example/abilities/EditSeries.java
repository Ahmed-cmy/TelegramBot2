package org.example.abilities;

import org.example.*;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.function.Predicate;

public class EditSeries implements AbilityExtension {
    private final DBContext db;
    private final AbilityBot bot;
    private Map<Long, AdminUser> adminAction;
    private Map<String, BotElement> series;

    public EditSeries(AbilityBot bot) {
        this.bot = bot;
        this.db = bot.getDb();
    }

    public Ability editSeries() {
        return Ability.builder()
                .name("edit")
                .info("تعديل سلسلة")
                .privacy(Privacy.ADMIN)
                .locality(Locality.ALL)
                .action(ctx -> {
                    adminAction = TelegramBot.adminAction;
                    series = TelegramBot.series;

                    AdminUser currentUser = new AdminUser(ctx.chatId(), AdminUser.userActions.EDIT);
                    currentUser.states.push(AdminUser.AdminStates.BASE);
                    currentUser.states.push(AdminUser.AdminStates.SERIES_SELECT);
                    adminAction.put(ctx.chatId(), currentUser);
                    db.commit();

                    sendSelectSeriesMenu(ctx.chatId(), currentUser);
                })
                .build();
    }

    public Reply getEditedSeries() {
        Predicate<Update> hasMessage = update -> update.hasMessage();
        Predicate<Update> isMessageHasText = update -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = update -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isCommandUsed = update -> adminAction.containsKey(update.getMessage().getChatId());
        Predicate<Update> isUserWantEdit = update -> adminAction.get(update.getMessage().getChatId()).checkAction(AdminUser.userActions.EDIT);

        return Reply.of((bot, update) -> {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            AdminUser currentUser = adminAction.get(chatId);

            // 1. التعامل مع الأوامر العامة أولاً (خروج / عودة)
            if (message.equals("الخروج من وضع التعديل")) {
                handleExit(chatId, currentUser);
                return;
            }
            if (message.equals("العودة")) {
                handleBack(chatId, currentUser);
                return;
            }

            // 2. توجيه المدخلات حسب الحالة الحالية (State Routing)
            Object currentState = currentUser.states.peek();

            if (currentState.equals(AdminUser.AdminStates.BASE)) {
                sendSelectSeriesMenu(chatId, currentUser);

            } else if (currentState.equals(AdminUser.AdminStates.SERIES_SELECT)) {
                handleSeriesSelection(chatId, message, currentUser);

            } else if (currentState.equals(AdminUser.AdminStates.EDIT_TYPE)) {
                handleEditTypeSelection(chatId, message, currentUser);

            } else if (currentState.equals(AdminUser.AdminStates.SELECT_LESSON)) {
                handleLessonSelection(chatId, message, currentUser);

            } else if (currentState.equals(AdminUser.AdminStates.EDIT_LESSON)) {
                handleLessonEditing(chatId, message, currentUser);

            } else if (currentState.equals(AdminUser.AdminStates.ADD_LESSONS)) {
                // يمكنك إضافة منطق إضافة الدروس هنا مستقبلاً
                bot.getSilent().send("جاري العمل على هذه الخاصية...", chatId);
            } else {
                bot.getSilent().send("حالة غير معروفة، يرجى الخروج وإعادة المحاولة.", chatId);
            }

        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantEdit);
    }

    // ==========================================
    // دوال معالجة الحالات (State Handlers)
    // ==========================================

    private void handleSeriesSelection(long chatId, String message, AdminUser currentUser) {
        if (!series.containsKey(message)) {
            bot.getSilent().send("السلسلة غير موجودة، تحقق من اسم السلسلة.", chatId);
            return;
        }

        currentUser.elementStack.push(TelegramBot.series.get(message));
        currentUser.states.push(AdminUser.AdminStates.EDIT_TYPE);
        adminAction.put(chatId, currentUser);
        db.commit();

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow("إضافة درس/دروس", "حذف درس", "تعديل درس"));
        rows.add(new KeyboardRow("العودة", "الخروج من وضع التعديل"));

        bot.getSilent().execute(
                SendMessage.builder()
                        .text("تم اختيار السلسلة بنجاح. ماذا تريد أن تفعل؟")
                        .chatId(chatId)
                        .replyMarkup(ReplyKeyboardMarkup.builder()
                                .keyboard(rows)
                                .resizeKeyboard(true)
                                .build())
                        .build()
        );
    }

    private void handleEditTypeSelection(long chatId, String message, AdminUser currentUser) {
        switch (message) {
            case "إضافة درس/دروس":
                currentUser.states.push(AdminUser.AdminStates.ADD_LESSONS);
                db.commit();
                bot.getSilent().execute(
                        SendMessage.builder()
                                .text("اكتب اسم الدرس، ثم سطر جديد، ثم الرابط (Link)")
                                .chatId(chatId)
                                .replyMarkup(ReplyKeyboardMarkup.builder()
                                        .keyboardRow(new KeyboardRow("العودة"))
                                        .resizeKeyboard(true)
                                        .build())
                                .build()
                );
                break;

            case "حذف درس":
                bot.getSilent().send("خاصية الحذف غير مفعلة بعد.", chatId);
                break;

            case "تعديل درس":
                currentUser.states.push(AdminUser.AdminStates.SELECT_LESSON);
                adminAction.put(chatId, currentUser);
                db.commit();
                bot.getSilent().execute(
                        SendMessage.builder()
                                .text("اختر درساً للتعديل:")
                                .chatId(chatId)
                                .replyMarkup(currentUser.elementStack.peek().getKeyboard())
                                .build()
                );
                break;

            default:
                bot.getSilent().send("اختيار خاطئ، يرجى اختيار أحد الأزرار المتاحة.", chatId);
        }
    }

    private void handleLessonSelection(long chatId, String message, AdminUser currentUser) {
        if (currentUser.elementStack.peek() instanceof Series currentSeries) {

            // يجب التحقق مما إذا كان الدرس موجوداً لتفادي NullPointerException
            BotElement lesson = currentSeries.getLesson(message);
            if (lesson == null) {
                bot.getSilent().send("الدرس غير موجود، تأكد من الاختيار الصحيح.", chatId);
                return;
            }

            currentUser.elementStack.push(lesson);
            currentUser.states.push(AdminUser.AdminStates.EDIT_LESSON);
            db.commit();

            String lessonInfo = "السطر الأول للعنوان والثاني للرابط\n\n" +
                    "البيانات الحالية:\n" +
                    currentUser.elementStack.peek().getName() + "\n" +
                    ((Lesson) currentUser.elementStack.peek()).getVoiceLink();

            bot.getSilent().execute(
                    SendMessage.builder()
                            .text(lessonInfo)
                            .chatId(chatId)
                            .build()
            );
        }
    }

    private void handleLessonEditing(long chatId, String message, AdminUser currentUser) {
        try {
            String[] lessonData = message.split("\\n");

            // إصلاح الخطأ المنطقي هنا: يجب أن يكون الطول 2 (وليس لا يساوي 2)
            if (lessonData.length >= 2) {
                currentUser.getLesson().setName(lessonData[0].trim());
                currentUser.getLesson().setVoiceLink(lessonData[1].trim());

                // يمكنك إضافة العودة التلقائية خطوة للخلف بعد التعديل الناجح
                currentUser.states.pop();
                currentUser.elementStack.pop();

                db.commit();
                bot.getSilent().send("✅ تم تعديل الدرس بنجاح.", chatId);

                // إعادة عرض قائمة الدروس لتعديل درس آخر إذا رغب
                handleEditTypeSelection(chatId, "تعديل درس", currentUser);
            } else {
                bot.getSilent().send("❌ صيغة خاطئة! تأكد من كتابة الاسم ثم (Enter) ثم الرابط.", chatId);
            }
        } catch (Exception e) {
            bot.getSilent().send("حدث خطأ أثناء التعديل.", chatId);
            e.printStackTrace();
        }
    }

    // ==========================================
    // دوال الملاحة (Navigation Handlers)
    // ==========================================

    private void handleExit(long chatId, AdminUser currentUser) {
        currentUser.states.clear();
        currentUser.elementStack.clear();
        db.commit();
        bot.getSilent().execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("تم الإلغاء والخروج من وضع التعديل.")
                        .replyMarkup(MainKeyboard.getMainKeyboard())
                        .build()
        );
    }

    private void handleBack(long chatId, AdminUser currentUser) {
        if (!currentUser.states.isEmpty()) {
            currentUser.states.pop(); // العودة للحالة السابقة
        }
        // إذا كان المكدس يحتوي على عناصر تعتمد على الحالة، يجب إزالتها أيضاً
        if (!currentUser.elementStack.isEmpty() && currentUser.states.size() <= currentUser.elementStack.size()) {
            currentUser.elementStack.pop();
        }

        db.commit();

        // إعادة عرض القائمة بناءً على الحالة التي رجعنا إليها
        if (currentUser.states.isEmpty()) {
            handleExit(chatId, currentUser);
        } else {
            Object previousState = currentUser.states.peek();
            if (previousState.equals(AdminUser.AdminStates.SERIES_SELECT) || previousState.equals(AdminUser.AdminStates.BASE)) {
                sendSelectSeriesMenu(chatId, currentUser);
            } else if (previousState.equals(AdminUser.AdminStates.EDIT_TYPE)) {
                handleSeriesSelection(chatId, currentUser.elementStack.peek().getName(), currentUser); // محاكاة إعادة إرسال القائمة
            }
        }
    }

    private void sendSelectSeriesMenu(long chatId, AdminUser currentUser) {
        bot.getSilent().execute(
                SendMessage.builder()
                        .text("يرجى اختيار سلسلة للتعديل:")
                        .chatId(chatId)
                        .replyMarkup(MainKeyboard.getAllSeries(currentUser.page))
                        .build()
        );
    }
}