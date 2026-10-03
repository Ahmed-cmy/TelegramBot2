package org.example.abilities;

import org.example.MainKeyboard;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.function.Predicate;

public class AutoAdd implements AbilityExtension {
    public Reply channelMessageListener() {
        Predicate<Update> isChannelPost = Update::hasChannelPost;

        return Reply.of((bot, update) -> {

            // Extract the message safely
            Message channelMessage = update.getChannelPost();

            // 1. Get necessary data
            Long chatId = channelMessage.getChatId();
            Integer messageId = channelMessage.getMessageId();
            String chatUsername = channelMessage.getChat().getUserName();

            // 2. Construct the message link
            String messageLink;

            if (chatUsername != null && !chatUsername.isEmpty()) {
                // It's a PUBLIC channel
                messageLink = "https://t.me/" + chatUsername + "/" + messageId;
            } else {
                // It's a PRIVATE channel
                // Remove the "-100" prefix from the Chat ID for the URL
                String strippedChatId = String.valueOf(chatId).replace("-100", "");
                messageLink = "https://t.me/c/" + strippedChatId + "/" + messageId;
            }

            // 3. Print or use the link
            System.out.println("New message! Link: " + messageLink);

            if (channelMessage.hasText()) {
                System.out.println("Text: " + channelMessage.getText());
            }

        }, isChannelPost);
    }
}
