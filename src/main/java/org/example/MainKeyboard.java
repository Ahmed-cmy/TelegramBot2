package org.example;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class MainKeyboard {

    public static int numberOfElementsPerPage = 20;
    public static int numberOfPages = TelegramBot.series.size() / numberOfElementsPerPage + 1;

    public static ReplyKeyboardMarkup getMainKeyboard() {
        KeyboardRow row0 = new KeyboardRow("جميع السلاسل");

        return ReplyKeyboardMarkup.builder()
                .keyboardRow(row0)
                .resizeKeyboard(true)
                .build();
    }

    public static ReplyKeyboardMarkup getAllSeries(int page) {
        System.out.println(page);
        List<KeyboardRow> rows = new ArrayList<>();
        List<Series> temp = new ArrayList<>(TelegramBot.series.values());
            System.out.println("page: " + page);
//        System.out.println(d);
        rows.add(new KeyboardRow());
        int rowNum = 0;
        for (int i = (page - 1) * 20; i < (Math.min(page * 20, TelegramBot.series.size())); i++) {
//            System.out.println("i " + i);
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
        rows.add(new KeyboardRow());
        if (page > 1) {
//            rows.add(new KeyboardRow("الصفحة السابقة"));
            rows.getLast().add(("الصفحة السابقة"));
        }
        if (page >= 1 && page < numberOfPages) {
//            rows.add(new KeyboardRow("الصفحة التالية"));
            rows.getLast().add(("الصفحة التالية"));
        }
        rows.add(new KeyboardRow("العودة"));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

}
