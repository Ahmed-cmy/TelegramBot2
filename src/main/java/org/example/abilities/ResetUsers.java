package org.example.abilities;

import org.example.TelegramBot;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;


public class ResetUsers implements AbilityExtension {

    AbilityBot bot;

    public ResetUsers(AbilityBot bot) {
        this.bot = bot;
    }

    public Ability removeSeries() {
        return Ability.builder()
                .name("reset")
                .privacy(Privacy.CREATOR)
                .locality(Locality.ALL)
                .action(ctx -> {
                    bot.getDb().getMap(TelegramBot.dataBases.NORMAL_USERS.name());
                    bot.getDb().commit();

                    bot.getSilent().send("rested", bot.creatorId());
                })
                .build();
    }
}
