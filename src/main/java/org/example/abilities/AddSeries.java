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
                    adminAction.put(ctx.chatId(),new AdminUser(ctx.chatId(), AdminUser.userActions.ADD_SERIES));
                    db.commit();
                    KeyboardRow row = new KeyboardRow("إلغاء");
                    ReplyKeyboardMarkup addKeyboard =ReplyKeyboardMarkup.builder()
                            .keyboardRow(row)
                            .resizeKeyboard(true)
                            .build();
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text("اكتب اسم السلسلة للإلغاء اضغط إلغاء")
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

                if(message.equals("إلغاء")) {
                    adminAction.remove(chatId);
                    bot.getSilent().send("تم الإلغاء", chatId);
                    return;
                }
            System.out.println(seriesName);
            if (adminAction.get(chatId).getSeries().isEmpty()) {
                currentUser.setSeries(message);
                adminAction.put(chatId, currentUser);
                db.commit();
                bot.getSilent().send("ارسل السلسلة",update.getMessage().getChatId());
                return;
            }
            Scanner scanner = new Scanner(message);
            Series series = new Series(seriesName);
            Lesson.count = 0;
            while (scanner.hasNextLine()) {
                String[] lesson = scanner.nextLine().split(",");
                if(lesson.length<2) {
                    bot.getSilent().send("الصيغة غير صالحة", chatId);
                    return;
                }
                if(series.getLessons().containsKey(lesson[0])){
                    bot.getSilent().send("يوجد درس مكرر يرجى التحقق",chatId);
                    return;
                }
                series.getLessons().addLesson(new Lesson(lesson[0],lesson[1]));
            }

            bot.getSilent().execute(
                    SendMessage.builder()
                            .text("تم")
                            .chatId(chatId)
                            .replyMarkup(MainKeyboard.getMainKeyboard())
                            .build()
            );
            db.getMap(dataBases.SERIES.name()).put(seriesName,series);
            adminAction.remove(chatId);
            db.commit();
        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantAdd);
    }
}

