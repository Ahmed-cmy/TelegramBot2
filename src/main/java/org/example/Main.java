package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args){
        final String token = "8696997537:AAGA0VA0_sPc1YSBB2l_7Uw5AQBihO-PuS0";
        try(TelegramBotsLongPollingApplication botApp = new TelegramBotsLongPollingApplication()){
            TelegramClient client = new OkHttpTelegramClient(token);
            botApp.registerBot(token,new TelegramBot(client, "Sh_Ihab_bot"));
            System.out.println("success");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
