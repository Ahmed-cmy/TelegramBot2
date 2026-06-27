package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.Serial;
import java.io.Serializable;

public class NormalUser extends TelegramUser {
    @Serial
    private static final long serialVersionUID = 1L;


    public NormalUser(long chatId) {
        this.chatId = chatId;
    }


}
