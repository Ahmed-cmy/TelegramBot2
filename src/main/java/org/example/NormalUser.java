package org.example;

import java.io.Serial;

public class NormalUser extends TelegramUser {
    @Serial
    private static final long serialVersionUID = 1L;

    public NormalUser(long chatId) {
        this.chatId = chatId;
    }


}
