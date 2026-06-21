package org.example.abilities;

import org.example.MainKeyboard;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

public class Welcome implements AbilityExtension {
    public Reply WelcomeMessage() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isStart = (update) -> update.getMessage().getText().equalsIgnoreCase("/start");
        return Reply.of((bot, update) -> {
            bot.getSilent().execute(
                    SendMessage.builder()
                            .chatId(update.getMessage().getChatId())
                            .text("""
                                    مرحبا في بوت الشيخ إيهاب الرسمي
                                     ملاحظه البوت لا يزال تحت التطوير
                                     للدعم @AhmedAh15
                                    """)
                            .replyMarkup(MainKeyboard.getMainKeyboard())
                            .build()
            );
        }, hasMessage, isMessageHasText, isStart);
    }
}
