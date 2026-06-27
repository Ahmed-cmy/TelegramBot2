package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.Serial;
import java.io.Serializable;

public abstract class BotElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
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

    public abstract ReplyKeyboardMarkup getKeyboard();
}
