package org.example;


import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class MainKeyboard {
    public static ReplyKeyboardMarkup getMainKeyboard() {
        KeyboardRow row0 = new KeyboardRow("جميع السلاسل");

        return ReplyKeyboardMarkup.builder()
                .keyboardRow(row0)
                .resizeKeyboard(true)
                .build();
    }

    public static ReplyKeyboardMarkup getAllSeries() {
        List<KeyboardRow> rows = new ArrayList<>();
        List<Series> temp = new ArrayList<>(TelegramBot.series.values());
        rows.add(new KeyboardRow());
        int rowNum = 0;
        for (int i = 0; i < TelegramBot.series.size(); i++) {
            rows.get(rowNum).add(temp.get(i).getName());
            if (i % 3 == 0 && i > 0) {
                rows.add(new KeyboardRow());
                rowNum++;
            }
            if ((i) % 2 == 0) {
                rows.add(new KeyboardRow());
                rowNum++;
            }
        }
        rows.add(new KeyboardRow("العودة"));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

}
