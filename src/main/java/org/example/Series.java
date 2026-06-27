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
    private final SeriesMap lessons = new SeriesMap();
    public int page = 1;
    public int numberOfPages;
    String name;
    int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Series(String name) {
        this.name = name;
        Lesson.count = 0;
        id = numberOfSeries++;
    }

    public Series() {
        Lesson.count = 0;
        id = numberOfSeries++;
    }


    public Lesson getLesson(int i) {
        return lessons.getLesson(i);
    }

    public Lesson getLesson(String name) {
        return lessons.getLesson(name);
    }

    public SeriesMap getLessons() {
        return lessons;
    }

    public void setLessons(Map<Integer, Lesson> lessons) {
        this.lessons.setLessons(lessons);
    }

    public void addLesson(Lesson lesson) {
        numberOfPages = getLessons().size() / 20 + 1;
        getLessons().addLesson(lesson);
    }

    public void addLessonIfAbsent(Lesson lesson) {
        numberOfPages = getLessons().size() / 20 + 1;
        if (!getLessons().getLessonsMap().containsKey(lesson.getName())) {
            getLessons().addLesson(lesson);
        }
    }

    public boolean containsId(int i) {
        return lessons.containsId(i);
    }

    public ReplyKeyboardMarkup getKeyboard() {
        List<KeyboardRow> rows = new ArrayList<>();
        int rowNumber = 0;
        rows.add(new KeyboardRow());
        for (int i = (page - 1) * 20; i < (Math.min(page * 20, getLessons().size())); i++) {
            if (i % 2 == 0 && i > 0) {
                rowNumber++;
                rows.add(new KeyboardRow());
            }
            rows.get(rowNumber).add(lessons.getLesson(i).getName());
        }
        if (page >= 1 && page < getLessons().size() / 20 + 1) {
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

    @Override
    public String toString() {
        StringBuilder print = new StringBuilder();
        for (int i = 0; i < getLessons().size(); i++) {
            print.append(i).append(" - ").append(getLessons().getLesson(i).getName()).append(", ");
        }
        return print.toString();
    }
}
