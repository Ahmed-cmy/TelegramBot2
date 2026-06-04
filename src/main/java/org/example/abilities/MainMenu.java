package org.example.abilities;

import org.example.Lesson;
import org.example.MainKeyboard;
import org.example.NormalUser;
import org.example.TelegramBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.function.Predicate;

import static org.example.TelegramBot.adminAction;
import static org.example.TelegramBot.series;

public class MainMenu implements AbilityExtension {
    private DBContext db;
    Map<Long, NormalUser> NormalUsersMap;


    public Reply getSeriesFromUser() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isNotAdminCommand = update -> !adminAction.containsKey(update.getMessage().getChatId());

        return Reply.of((bot, update) -> {
            db = bot.getDb();
            NormalUsersMap = db.getMap(TelegramBot.dataBases.NORMAL_USERS.name());

            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            NormalUsersMap.putIfAbsent(chatId, new NormalUser(chatId));
            db.commit();

            NormalUser currentUser = NormalUsersMap.get(chatId);

            switch (message) {
                case "جميع السلاسل":
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries());
                    NormalUsersMap.put(chatId, currentUser);
                    db.commit();
                    break;
                case "العودة":
                    currentUser.setSeries(null);
                    currentUser.setLesson(null);
                    currentUser.setKeyboardMarkup(MainKeyboard.getMainKeyboard());
                    NormalUsersMap.put(chatId, currentUser);
                    System.out.println("cc");
                    db.commit();
                    break;
                case "العودة للسلسلة":
                    currentUser.setLesson(null);
                    currentUser.setKeyboardMarkup(currentUser.getSeries().getKeyboard());
                    NormalUsersMap.put(chatId, currentUser);
                    db.commit();
                    break;
                case "العودة للسلاسل":
                    currentUser.setSeries(null);
                    currentUser.setLesson(null);
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries());
                    NormalUsersMap.put(chatId, currentUser);
                    db.commit();
                    break;
                default:
                    if (series.containsKey(message)) {
                        currentUser.setSeries(series.get(message));
                        currentUser.setKeyboardMarkup(series.get(message).getKeyboard());
                        NormalUsersMap.put(chatId, currentUser);
                        db.commit();
                        break;
                    }
                    if (currentUser.getSeries() != null && currentUser.getLesson() == null) {
                        currentUser.setLesson(currentUser.getSeries().getLesson(message));
                        currentUser.setKeyboardMarkup(currentUser.getSeries().getLesson(message).getKeyboard());
                        NormalUsersMap.put(chatId, currentUser);
                        db.commit();
                        break;
                    }
                    if (!currentUser.getLesson().getName().isEmpty()) {
                        Lesson lesson = currentUser.getSeries().getLesson(currentUser.getLesson().getName());
                        if (message.equals("صوتي")) {
                            bot.getSilent().sendMd("[" + lesson.getName() + "](" + lesson.getLink() + ")", chatId);
                            break;
                        }
                        bot.getSilent().send("نأسف غير متوفر", chatId);
                        return;
                    }

            }
            String messageText = currentUser.getLesson() != null ?
                    currentUser.getLesson().getName() : currentUser.getSeries() != null ?
                    currentUser.getSeries().getName() : "اختر السلسلة";
            bot.getSilent().execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(messageText)
                            .replyMarkup(currentUser.getKeyboardMarkup())
                            .build()
            );

        }, hasMessage, isMessageHasText, isNotCommand, isNotAdminCommand);
    }
}
