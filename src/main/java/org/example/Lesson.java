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
    public static int count = 0;
    private String name;
    private String voiceLink;
    private int id;

    public Lesson(String name, String voiceLink) {
        this.name = name;
        this.voiceLink = voiceLink;
        id = count++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVoiceLink() {
        return voiceLink;
    }

    public void setVoiceLink(String voiceLink) {
        this.voiceLink = voiceLink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ReplyKeyboardMarkup getKeyboard() {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow("صوتي", "فيديو"));
        rows.add(new KeyboardRow("العودة للسلسلة"));

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }
}
