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
                    currentUser.states.push(AdminUser.editStates.EDIT_TYPE);
                    adminAction.put(ctx.chatId(), currentUser);
                    db.commit();

                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للتعديل")
                                    .chatId(ctx.chatId())
                                    .replyMarkup(MainKeyboard.getAllSeries())
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
            System.out.println(currentUser.states.toString());
//            System.out.println(currentUser.states.search(AdminUser.editStates.EDIT_TYPE));

            if (currentUser.states.search(AdminUser.editStates.SERIES_SELECT) == 1) {
                bot.getSilent().execute(
                        SendMessage.builder()
                                .text("يرجى اختيار سلسلة للتعديل")
                                .chatId(chatId)
                                .replyMarkup(MainKeyboard.getAllSeries())
                                .build()
                );
                currentUser.states.push(AdminUser.editStates.EDIT_TYPE);
                adminAction.put(chatId, currentUser);
                db.commit();
                return;
            }
            if (currentUser.states.search(AdminUser.editStates.EDIT_TYPE) == 1) {
                if (!series.containsKey(message)) {
                    bot.getSilent().send("تحقق من اسم السلسلة", chatId);
                    return;
                }
                currentUser.setSeries(message);
                adminAction.put(chatId, currentUser);
                currentUser.states.push(AdminUser.editStates.CHECK_EDIT_TYPE);
                db.commit();
                System.out.println("working");

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

                return;
            }
            if (currentUser.states.search(AdminUser.editStates.EDIT_TYPE) != -1) {
                switch (message) {
                    case "إضافة درس/دروس":
                        bot.getSilent().execute(
                                SendMessage.builder()
                                        .text("اكتب اسم الدرس ثم , ثم link")
                                        .replyMarkup(ReplyKeyboardMarkup.builder()
                                                .keyboardRow(new KeyboardRow("العودة"))
                                                .resizeKeyboard(true)
                                                .build())
                                        .build()
                        );
                        currentUser.states.push(AdminUser.editStates.ADD_LESSONS);
                        adminAction.put(chatId, currentUser);
                        db.commit();
                        break;

                    case "حذف درس":
                        break;

                    case "تعديل درس":
                        break;
                    default:
                        bot.getSilent().send("اختيار خاطئ", chatId);
                }
            }


        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantEdit);
    }

}
