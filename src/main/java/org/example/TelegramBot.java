package org.example;

import org.example.abilities.AddSeries;
import org.example.abilities.MainMenu;
import org.example.abilities.RemoveSeries;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

public class TelegramBot extends AbilityBot {
    public static boolean admin;
    public static Map<String, Series> series;
    public static Map<Long, AdminUser> adminAction;
    public enum dataBases {
        SERIES,
        ADMIN_ACTIONS,
        NORMAL_USERS,
        EDIT_USER_STATE
    }

//    Map<Long, String> userSeriesSelect = db.getMap(dataBases.USER_SERIES_SELECT.name());
//    Map<Long, String> userLessonSelect = db.getMap(dataBases.USER_LESSON_SELECT.name());
    protected TelegramBot(TelegramClient telegramClient, String botUsername) {
        super(telegramClient, botUsername);
        this.onRegister();
        adminAction = db.getMap(dataBases.ADMIN_ACTIONS.name());
        silent.send("start", 1784824244L);
        series = db.getMap(dataBases.SERIES.name());
        db.commit();
    }
    public AbilityExtension MainMenu(){
        return new MainMenu(this);
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
