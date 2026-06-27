package org.example.abilities;

import org.example.NormalUser;
import org.example.TelegramBot;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
// 1. Fetch the exact target map
                    Map<Object, Object> targetMap = bot.getDb().getMap(TelegramBot.dataBases.NORMAL_USERS.name());

// 2. Duplicate the keys into a separate set to avoid ConcurrentModificationException
                    Set<Object> keysToRemove = new HashSet<>(targetMap.keySet());

// 3. Delete only these specific keys from the database context
                    for (Object key : keysToRemove) {
                        targetMap.remove(key);
                    }

// 4. Safely commit only the updated mappings
                    bot.getDb().commit();

                    bot.getSilent().send("rested", bot.creatorId());
                })
                .build();
    }
}
