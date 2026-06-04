package org.example;


import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class MainKeyboard {
    public static ReplyKeyboardMarkup getMainKeyboard(){
        KeyboardRow row0 = new KeyboardRow("جميع السلاسل");

        return ReplyKeyboardMarkup.builder()
                .keyboardRow(row0)
                .resizeKeyboard(true)
                .build();
    }
    public static ReplyKeyboardMarkup getAllSeries() {
        List<KeyboardRow> rows = new ArrayList<>();
        List<Series> temp = new ArrayList<>(TelegramBot.series.values());
        for (int i = 0; i < TelegramBot.series.size(); i++) {
            rows.add(new KeyboardRow(temp.get(i).getName()));
        }
        rows.add(new KeyboardRow("العودة"));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

}
