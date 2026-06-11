package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lesson implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String link;
    private int id;
    public static int count = 0;

    public Lesson(String name, String link) {
        this.name = name;
        this.link = link;
        id = count++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLink(String link) {
        this.link = link;
    }
    public ReplyKeyboardMarkup getKeyboard(){
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow("صوتي", "فيديو"));
        rows.add(new KeyboardRow("العودة للسلسلة"));

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }
}
