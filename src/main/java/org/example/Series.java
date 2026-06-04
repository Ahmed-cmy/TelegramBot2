package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Series implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public static int numberOfSeries = 0;
    private final int id;
    private String name;
    //    private Map<Integer, Lesson> lessons;
    private SeriesMap lessons = new SeriesMap();
//    private List<KeyboardRow> rows;

    public Series(String name) {
        this.name = name;
        id = numberOfSeries++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public Lesson getLesson(int i){
        return lessons.getLesson(i);
    }
    public Lesson getLesson(String name){
        return lessons.getLesson(name);
    }

    public SeriesMap getLessons() {
        return lessons;
    }

    public void setLessons(Map<String, Lesson> lessons) {
        this.lessons.setLessons(lessons);
    }

    public void addLesson(Lesson lesson) {
        getLessons().addLesson(lesson);
    }

    public ReplyKeyboardMarkup getKeyboard() {
        List<KeyboardRow> rows = new ArrayList<>();
        int rowNumber = 0;
        rows.add(new KeyboardRow());
        for (int i = 0; i < getLessons().size(); i++) {
            if (lessons.size() == 0) {
                break;
            }
            if (i % 2 == 0 && i > 0) {
                rowNumber++;
                rows.add(new KeyboardRow());
            }
            rows.get(rowNumber).add(lessons.getLesson(i).getName());
        }
        rows.add(new KeyboardRow("العودة للسلاسل"));

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

    @Override
    public String toString() {
        StringBuilder print = new StringBuilder();
        for (int i = 0; i < getLessons().size(); i++) {
            print.append(getLessons().getLesson(i).getName()).append(", ");
        }
        return print.toString();
    }
}
