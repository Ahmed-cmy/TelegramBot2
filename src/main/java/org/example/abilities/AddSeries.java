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
                    List<KeyboardRow> rows = new ArrayList<>();
                    rows.add(new KeyboardRow("Internet archive"));
                    rows.add(new KeyboardRow("Channel"));
                    rows.add(new KeyboardRow("إلغاء"));
                    ReplyKeyboardMarkup addKeyboard = ReplyKeyboardMarkup.builder()
                            .keyboard(rows)
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
            KeyboardRow row = new KeyboardRow("إلغاء");
            ReplyKeyboardMarkup addKeyboard = ReplyKeyboardMarkup.builder()
                    .keyboardRow(row)
                    .resizeKeyboard(true)
                    .build();
            switch (message) {
                case "Internet archive":
                    currentUser.states.push(AdminUser.AdminStates.ADD_FROM_INTERNET_ARCHIVE);
                    adminAction.put(chatId, currentUser);
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .chatId(chatId)
                                    .text("اكتب لينك السلسلة للإلغاء اضغط إلغاء")
                                    .replyMarkup(addKeyboard)
                                    .build()
                    );
                    db.commit();
                    return;
                case "Channel":
                    currentUser.states.push(AdminUser.AdminStates.ADD_FROM_CHANNEL);
                    adminAction.put(chatId, currentUser);
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .chatId(chatId)
                                    .text("اكتب لينك السلسلة للإلغاء اضغط إلغاء")
                                    .replyMarkup(addKeyboard)
                                    .build()
                    );
                    db.commit();
                    return;
                default:
                    if (currentUser.states.peek() == null) {
                        bot.getSilent().send("invalid", chatId);
                    }
            }
            if (currentUser.states.peek() == AdminUser.AdminStates.ADD_FROM_INTERNET_ARCHIVE) {
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
//                    System.out.println(db.getMap(dataBases.SERIES.name()).get(series.getName()));
                    } else {
                        bot.getSilent().send("فشل حاول مره أخرى", chatId);
                    }
                }
            } else {
                try {

                    Scanner scanner = new Scanner(message);
                    String name = scanner.nextLine();
                    String firstink = scanner.nextLine();
                    int len = scanner.nextInt();
                    String linkPattern = firstink.substring(0, firstink.lastIndexOf("/") + 1);
                    int firstId = Integer.parseInt(firstink.substring(firstink.lastIndexOf("/") + 1));
                    int currentId = firstId;

                    System.out.println(
                            "name:  " + name +
                                    "\nfirstLink:     " + firstink +
                                    "\nlen:   " + len +
                                    "\npattern:   " + linkPattern+
                                    "\nfirstId:   " + firstId
                    );

                    Series series = new Series(name);
                    for (int i = firstId; i < len+firstId; i++) {
                        series.addLesson(new Lesson("الحلقة: " + (i - firstId + 1), linkPattern + currentId));
//                        bot.getSilent().send(linkPattern + currentId, chatId);
                        currentId++;
//                        System.out.println(series.getLesson(i-firstId));
                    }
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
                    bot.getSilent().send("done", chatId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantAdd);
    }
}

