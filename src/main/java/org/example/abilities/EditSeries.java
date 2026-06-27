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
    private Map<String, Series> series;

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

                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للتعديل")
                                    .chatId(ctx.chatId())
                                    .replyMarkup(MainKeyboard.getAllSeries(currentUser.page))
                                    .build()
                    );
                })
                .build();
    }

    public Reply getEditedSeries() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isCommandUsed = update -> adminAction.containsKey(update.getMessage().getChatId());
        Predicate<Update> isUserWantEdit = update -> adminAction.get(update.getMessage().getChatId()).checkAction(AdminUser.userActions.EDIT);

        return Reply.of((bot, update) -> {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            AdminUser currentUser = adminAction.get(chatId);
            if (message.equals("العودة")) {
                currentUser.states.pop();
                adminAction.put(chatId, currentUser);
                db.commit();
//                return;
            }
            if (message.equals("الخروج من وضع التعديل")) {
                currentUser.states.clear();
                bot.getSilent().execute(
                        SendMessage.builder()
                                .chatId(chatId)
                                .text("تم الإلغاء")
                                .replyMarkup(MainKeyboard.getMainKeyboard())
                                .build()
                );
                return;
            }
            switch (currentUser.states.peek()) {
                case AdminUser.AdminStates.BASE:
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للتعديل")
                                    .chatId(chatId)
                                    .replyMarkup(MainKeyboard.getAllSeries(currentUser.page))
                                    .build()
                    );
                    currentUser.states.push(AdminUser.AdminStates.SERIES_SELECT);
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    break;
                case AdminUser.AdminStates.SERIES_SELECT:
                    if (currentUser.getSeries() == null) {

                        if (!series.containsKey(message)) {
                            bot.getSilent().send("تحقق من اسم السلسلة", chatId);
                            break;
                        }
                        currentUser.setSeries(TelegramBot.series.get(message));
                        currentUser.states.push(AdminUser.AdminStates.EDIT_TYPE);
                        adminAction.put(chatId, currentUser);
                        db.commit();


                        List<KeyboardRow> rows = new ArrayList<>();
                        rows.add(new KeyboardRow("إضافة درس/دروس", "حذف درس", "تعديل درس"));
                        rows.add(new KeyboardRow("العودة"));

                        bot.getSilent().execute(
                                SendMessage.builder()
                                        .text("اختر")
                                        .chatId(chatId)
                                        .replyMarkup(ReplyKeyboardMarkup.builder()
                                                .keyboard(rows)
                                                .resizeKeyboard(true)
                                                .build())
                                        .build()
                        );
                    }
                    break;
                case AdminUser.AdminStates.EDIT_TYPE:
                    System.out.println("out");
                    switch (message) {
                        case "إضافة درس/دروس":
                            System.out.println("in");
                            bot.getSilent().execute(
                                    SendMessage.builder()
                                            .text("اكتب اسم الدرس ثم , ثم link")
                                            .replyMarkup(ReplyKeyboardMarkup.builder()
                                                    .keyboardRow(new KeyboardRow("العودة"))
                                                    .resizeKeyboard(true)
                                                    .build())
                                            .build()
                            );
                            currentUser.states.push(AdminUser.AdminStates.ADD_LESSONS);
                            adminAction.put(chatId, currentUser);
                            db.commit();
                            break;

                        case "حذف درس":
                            break;

                        case "تعديل درس":
                            System.out.println("in edit");
                            try {
                                bot.getSilent().execute(
                                        SendMessage.builder()
                                                .text("اختر درس للتعديل")
                                                .chatId(chatId)
                                                .replyMarkup(series.get(currentUser.getSeries()).getKeyboard())
                                                .build()
                                );
                                currentUser.states.push(AdminUser.AdminStates.SELECT_LESSON);
                                adminAction.put(chatId, currentUser);
                                db.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        default:
                            bot.getSilent().send("اختيار خاطئ", chatId);
                    }
                case AdminUser.AdminStates.SELECT_LESSON:
                    System.out.println("message" + message);
                    currentUser.setLesson(currentUser.getSeries().getLesson(message));
                    currentUser.states.push(AdminUser.AdminStates.EDIT_LESSON);
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("السطر ألأول للعنوان و الثاني للرابط" + "\n" +
                                            currentUser.getLesson().getName() + "\n" +
                                            currentUser.getLesson().getVoiceLink())
                                    .chatId(chatId)
                                    .build()
                    );
                    break;
                case AdminUser.AdminStates.EDIT_LESSON:
                    System.out.println(currentUser.getLesson());
                    try {

                        Scanner scanner = new Scanner(message);
                        String[] lessonData = message.split("\\n");
                        if (scanner.hasNextLine() && lessonData.length != 2) {
                            currentUser.getLesson().setName(lessonData[0]);
                            currentUser.getLesson().setVoiceLink(lessonData[1]);
                            db.commit();
                            bot.getSilent().send("تم", chatId);
                            return;
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:

            }
            System.out.println("s: " + currentUser.states.toString());


        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantEdit);
    }

}
