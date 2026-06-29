package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Category extends BotElement {
    @Serial
    private static final long serialVersionUID = 3L;
    private final Map<String, Series> seriesMap = new HashMap<>();
    public int page = 1;
    public int numberOfPages;

    public Category(String name) {
        super.setName(name);
    }
    public Map<String, Series> getMap(){
        return seriesMap;
    }
    public void addSeries(Series series){
        seriesMap.put(series.getName(), series);
    }
    public Series getSeries(String name){
        return seriesMap.getOrDefault(name, new Series("لا يوجد"));
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard() {
        numberOfPages = seriesMap.size() / 20 + 1;
        List<KeyboardRow> rows = new ArrayList<>();
        List<Series> temp = new ArrayList<>(seriesMap.values());
        int rowNumber = 0;
        rows.add(new KeyboardRow());
        for (int i = (page - 1) * 20; i < (Math.min(page * 20, temp.size())); i++) {
            if (i % 2 == 0 && i > 0) {
                rowNumber++;
                rows.add(new KeyboardRow());
            }
            rows.get(rowNumber).add(temp.get(i).getName());
        }
        if (page >= 1 && page < temp.size() / 20 + 1) {
            rows.add(new KeyboardRow("الصفحة التالية"));
        }
        if (page > 1) {
            rows.add(new KeyboardRow("الصفحة السابقة"));
        }
        rows.add(new KeyboardRow("العودة للسلاسل"));

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }
}
