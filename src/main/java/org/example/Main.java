package org.example;

import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.InetSocketAddress;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);

        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token == null || token.isEmpty()){
            Dotenv Token = Dotenv.load();
        token = Token.get("TELEGRAM_BOT_TOKEN");
        }
        if (token != null){
            token = token.trim();
        }
        try (TelegramBotsLongPollingApplication botApp = new TelegramBotsLongPollingApplication()) {
            TelegramClient client = new OkHttpTelegramClient(token);
            botApp.registerBot(token, new TelegramBot(client, "Sh_Ihab_bot"));
            System.out.println("success");
            Thread.currentThread().join();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
