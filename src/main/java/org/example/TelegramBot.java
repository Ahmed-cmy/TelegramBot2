package org.example;

import org.example.abilities.AddSeries;
import org.example.abilities.RemoveSeries;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

public class TelegramBot extends AbilityBot {
    public static boolean admin;
    public static Map<String, Series> series;
    public static Map<Long, AdminUser> adminAction;
    Map<Long, ReplyKeyboardMarkup> userKeyboard = db.getMap(dataBases.USER_KEYBOARD.name());
    Map<Long, String> userSeriesSelect = db.getMap(dataBases.USER_SERIES_SELECT.name());
    Map<Long, String> userLessonSelect = db.getMap(dataBases.USER_LESSON_SELECT.name());
    private TelegramClient client;
    protected TelegramBot(TelegramClient telegramClient, String botUsername) {
        super(telegramClient, botUsername);
        this.onRegister();
        adminAction = db.getMap(dataBases.ADD_USER_STATE.name());
        client = telegramClient;
        silent.send("start", 1784824244L);
        series = db.getMap(dataBases.SERIES.name());
        db.commit();
    }

    public AbilityExtension addSeries() {
        return new AddSeries(this);
    }

    public AbilityExtension removeSeries() {
        return new RemoveSeries(this);
    }

    @Override
    public long creatorId() {
        return 1784824244L;
    }

    @Override
    public void consume(Update update) {
        super.consume(update);

        if (update.hasMessage() && update.getMessage().hasText() && !update.getMessage().getText().startsWith("/")) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            System.out.println(message + "lll");
            if (adminAction.containsKey(chatId)) {
                return;
            }

            if (!userKeyboard.containsKey(chatId)) {
                userKeyboard.put(chatId, MainKeyboard.getMainKeyboard());
            }

            switch (message) {
                case "جميع السلاسل":
                    db.getMap(dataBases.USER_KEYBOARD.name()).put(chatId, MainKeyboard.getAllSeries());
                    db.commit();
                    break;
                case "العودة":
                    userSeriesSelect.remove(chatId);
                    userLessonSelect.remove(chatId);
                    db.getMap(dataBases.USER_KEYBOARD.name()).put(chatId, MainKeyboard.getMainKeyboard());
                    db.commit();
                    break;
                case "العودة للسلسلة":
                    userLessonSelect.remove(chatId);
                    db.getMap(dataBases.USER_KEYBOARD.name()).put(chatId, series.get(userSeriesSelect.get(chatId)).getKeyboard());
                    db.commit();
                    break;
                case "العودة للسلاسل":
                    userSeriesSelect.remove(chatId);
                    userLessonSelect.remove(chatId);
                    db.getMap(dataBases.USER_KEYBOARD.name()).put(chatId, MainKeyboard.getAllSeries());
                    db.commit();
                    break;
                default:
                    if (series.containsKey(message)) {
                        userSeriesSelect.put(chatId, message);
                        db.getMap(dataBases.USER_KEYBOARD.name()).put(chatId, series.get(message).getKeyboard());
                        db.commit();
                        break;
                    }
                    if (userSeriesSelect.containsKey(chatId) && !userLessonSelect.containsKey(chatId)) {
                        userLessonSelect.put(chatId, message);
                        db.getMap(dataBases.USER_KEYBOARD.name()).put(chatId, series.get(userSeriesSelect.get(chatId)).getLessons().getLesson(message).getKeyboard());
                        db.commit();
                        break;
                    }
                    if (userLessonSelect.containsKey(chatId)) {
                        Lesson lesson = series.get(userSeriesSelect.get(chatId)).getLessons().getLesson(userLessonSelect.get(chatId));
                        if (message.equals("صوتي")) {
                            silent.sendMd("[" + lesson.getName() + "](" + lesson.getLink() + ")", chatId);
                            break;
                        }
                        silent.send("نأسف غير متوفر", chatId);
                        return;
//                        userLessonSelect.remove(chatId);
                    }

            }
            String messageText = userLessonSelect.containsKey(chatId) ?
                    userLessonSelect.get(chatId) :
                    userSeriesSelect.getOrDefault(chatId, "اختر سلسة");
            silent.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(messageText)
                            .replyMarkup(userKeyboard.get(chatId))
                            .build()
            );
//            userKeyboard.put(update.getMessage().getChatId(),MainKeyboard.getMainKeyboard());
        }
    }

    public enum dataBases {
        SERIES,
        ADD_USER_STATE,
        REMOVE_USER_STATE,
        USER_KEYBOARD,
        USER_SERIES_SELECT,
        USER_LESSON_SELECT,
        EDIT_USER_STATE
    }
//    public ReplyKeyboardMarkup getAllSeries(){
//        List<KeyboardRow> rows = new ArrayList<>();
//        List<Series> temp = new ArrayList<>(series.values());
//        for (int i = 0; i < series.size(); i++) {
//            rows.add(new KeyboardRow(temp.get(i).getName()));
//        }
//        rows.add(new KeyboardRow("العودة"));
//        return ReplyKeyboardMarkup.builder()
//                .keyboard(rows)
//                .resizeKeyboard(true)
//                .build();
//    }
}
