package org.example.abilities;


import org.example.*;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.function.Predicate;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

import org.example.TelegramBot.dataBases;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

// 1. Define the extension class to manage specific functionality
public class AddSeries implements AbilityExtension {
    private final DBContext db;
    private final AbilityBot bot;
    private Map<Long, AdminUser> adminAction;

    public AddSeries(AbilityBot bot) {
        this.bot = bot;
        this.db = bot.getDb();
    }

    // 2. Define an Ability inside the extension
    public Ability addSeries() {
        return Ability.builder()
                .name("add")
                .info("إضافة سلسلة")
                .privacy(ADMIN)
                .locality(ALL)
                .action(ctx -> {
                    adminAction = TelegramBot.adminAction;
                    adminAction.put(ctx.chatId(), new AdminUser(ctx.chatId(), AdminUser.userActions.ADD_SERIES));
                    db.commit();
                    KeyboardRow row = new KeyboardRow("إلغاء");
                    ReplyKeyboardMarkup addKeyboard = ReplyKeyboardMarkup.builder()
                            .keyboardRow(row)
                            .resizeKeyboard(true)
                            .build();
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text("اكتب لينك السلسلة للإلغاء اضغط إلغاء")
                                    .replyMarkup(addKeyboard)
                                    .build()
                    );
                })
                .build();
    }

    public Reply getSeriesFromUser() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isCommandUsed = update -> adminAction.containsKey(update.getMessage().getChatId());
        Predicate<Update> isUserWantAdd = update -> adminAction.get(update.getMessage().getChatId()).checkAction(AdminUser.userActions.ADD_SERIES);

        return Reply.of((bot, update) -> {
            long chatId = update.getMessage().getChatId();
            AdminUser currentUser = adminAction.get(chatId);
            String message = update.getMessage().getText();
            String seriesName = adminAction.get(chatId).getSeries();

            if (message.equals("إلغاء")) {
                adminAction.remove(chatId);
                bot.getSilent().send("تم الإلغاء", chatId);
                return;
            }
//            System.out.println(seriesName);
//            if (adminAction.get(chatId).getSeries().isEmpty()) {
//                currentUser.setSeries(message);
//                adminAction.put(chatId, currentUser);
//                db.commit();
//                bot.getSilent().send("ارسل السلسلة", update.getMessage().getChatId());
//                return;
//            }
            bot.getSilent().send("جاري التحميل", chatId);
            Series series = null;
            try {
                series = LinkLocator.seriesGetter(message, chatId);
//                System.out.println(series);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                bot.getSilent().send("تم",chatId);
                if (series != null) {

                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("تم")
                                    .chatId(chatId)
                                    .replyMarkup(MainKeyboard.getMainKeyboard())
                                    .build()
                    );
                    db.getMap(dataBases.SERIES.name()).put(series.getName(), series);
                    adminAction.remove(chatId);
                    db.commit();
//                    System.out.println(series.getLesson(series.getLesson(0).getName()).getKeyboard());
                    System.out.println(db.getMap(dataBases.SERIES.name()).get(series.getName()));
                } else {
                    bot.getSilent().send("فشل حاول مره أخرى", chatId);
                }
            }
        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantAdd);
    }
}

