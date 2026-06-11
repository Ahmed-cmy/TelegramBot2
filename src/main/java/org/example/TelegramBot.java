package org.example;

import org.example.abilities.*;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

public class TelegramBot extends AbilityBot {
    public static Map<String, Series> series;
    public static Map<Long, AdminUser> adminAction;
    public enum dataBases {
        SERIES,
        ADMIN_ACTIONS,
        NORMAL_USERS,
        EDIT_USER_STATE
    }

    protected TelegramBot(TelegramClient telegramClient, String botUsername) {
        super(telegramClient, botUsername);
        this.onRegister();
        adminAction = db.getMap(dataBases.ADMIN_ACTIONS.name());
        silent.send("start", 1784824244L);
        series = db.getMap(dataBases.SERIES.name());
        db.commit();
    }
    public AbilityExtension MainMenu(){
        return new MainMenu();
    }

    public AbilityExtension addSeries() {
        return new AddSeries(this);
    }

    public AbilityExtension removeSeries() {
        return new RemoveSeries(this);
    }
    public AbilityExtension editSeries() {
        return new EditSeries(this);
    }
    public AbilityExtension linkLocator(){
        return new LinkLocator(this);
    }

    @Override
    public long creatorId() {
        return 1784824244L;
    }

    @Override
    public void consume(Update update) {
        super.consume(update);
    }
}
