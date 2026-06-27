package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.Serial;
import java.io.Serializable;

public class TelegramUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public int page = 1;
    ReplyKeyboardMarkup keyboardMarkup;
    Series series;
    Lesson lesson;
    long chatId;

    public ReplyKeyboardMarkup getKeyboardMarkup() {
        return keyboardMarkup;
    }

    public void setKeyboardMarkup(ReplyKeyboardMarkup keyboardMarkup) {
        this.keyboardMarkup = keyboardMarkup;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
