package org.example;

import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.abilities.LinkLocator;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.InetSocketAddress;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
//        final String token = "8696997537:AAGA0VA0_sPc1YSBB2l_7Uw5AQBihO-PuS0";

        HttpServer server = HttpServer.create(new InetSocketAddress(7860), 0);
        server.createContext("/", exchange -> {
            String response = "Bot is Running Successfully!";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("Web server started on port 7860");


        Dotenv Token = Dotenv.load();
        String token = Token.get("TELEGRAM_BOT_TOKEN").trim();
        try(TelegramBotsLongPollingApplication botApp = new TelegramBotsLongPollingApplication()){
            TelegramClient client = new OkHttpTelegramClient(token);
            botApp.registerBot(token,new TelegramBot(client, "Sh_Ihab_bot"));
            System.out.println("success");
//            LinkLocator.seriesGetter("https://anasalafy.com/ar/category/%D8%AC%D8%B3%D8%B1-%D8%A7%D9%84%D8%AA%D8%B9%D8%A8?sortBy=date-asc");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
