package org.example.abilities;

import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

public class MainMenu implements AbilityExtension {
    private AbilityBot bot;
    private DBContext db;
    public MainMenu(AbilityBot bot){
        this.bot = bot;
    }

}
