package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.abilities.*;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class TelegramBot extends AbilityBot {
    public static Map<String, BotElement> series;
    public static Map<Long, AdminUser> adminAction;

    protected TelegramBot(TelegramClient telegramClient, String botUsername) {
//        super(telegramClient, botUsername);
        super(telegramClient, botUsername,
                MapDBContext.offlineInstance(
                        System.getenv("HOME") != null
                                ? "/home/site/wwwroot/" + botUsername
                                : botUsername
                )
        );
        this.onRegister();
        adminAction = db.getMap(dataBases.ADMIN_ACTIONS.name());
        silent.send("start", 1784824244L);
        series = db.getMap(dataBases.SERIES.name());
//        backupDataToJson();
//        restoreDataFromJson();
//        db.getMap(dataBases.NORMAL_USERS.name()).remove(creatorId());
//        System.out.println(series.values());

//        series.forEach((s, ser) -> {
//            for (Lesson lesson :ser.getLessons().getLessonsMap().values()){
//                if (getBig(lesson.getVoiceLink()) > 20) {
//                    silent.send(s, creatorId());
//                    break;
//                }
//            }
//        });
//        db.clear();
//        series.clear();
//        db.getMap(dataBases.ADMIN_ACTIONS.name()).clear();
        db.commit();
//        db.getMap(TelegramBot.dataBases.NORMAL_USERS.name()).clear();
    }

    public AbilityExtension MainMenu() {
        return new MainMenu();
    }

    public AbilityExtension addSeries() {
        return new AddSeries(this);
    }
    public AbilityExtension addCategory() {
        return new AddCategory(this);
    }

    public AbilityExtension welcomeMessage() {
        return new Welcome();
    }

    public AbilityExtension removeSeries() {
        return new RemoveElemet(this);
    }
    public AbilityExtension reset(){
        return new ResetUsers(this);
    }

//        public AbilityExtension editSeries() {
//        return new EditSeries(this);
//    }
    public AbilityExtension linkLocator() {
        return new LinkLocator(this);
    }

    @Override
    public long creatorId() {
        return 1784824244L;
    }

    @Override
    public void consume(Update update) {
        super.consume(update);
    }

    double getBig(String fileUrl) {
        double[] mb = new double[1];
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS) // تتبع التحويلات (Redirects)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody()) // طلب HEAD
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() == 200) {
                // استخراج الحجم من الـ Headers
                response.headers().firstValue("Content-Length").ifPresentOrElse(size -> {
                    long bytes = Long.parseLong(size);
                    mb[0] = bytes / (1024.0 * 1024.0);
//                    System.out.printf("size: %.2f MB%n", mb[0]);
                }, () -> {
//                    System.out.println("server didnt return the size (Content-Length not found).");
                });
            } else {
//                System.out.println("url not working. error: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mb[0];
    }

    public enum dataBases {
        SERIES,
        ADMIN_ACTIONS,
        NORMAL_USERS,
        EDIT_USER_STATE
    }
    public void backupDataToJson() {
        try {
            // 1. هات الداتا من الداتا بيز القديمة
            Map<String, Series> seriesMap = db.getMap(dataBases.SERIES.name());

            // 2. حول الداتا إلى JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(seriesMap);

            // 3. احفظ الـ JSON في ملف نصي
            Files.writeString( Paths.get("series_backup.json"), json);

            System.out.println("✅ تم حفظ البيانات القديمة بنجاح في ملف series_backup.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void restoreDataFromJson() {
        try {
            // 1. اقرأ ملف الـ JSON
            String json = Files.readString(Paths.get("series_backup.json"));

            // 2. حول الـ JSON إلى Map يحتوي على الكلاسات بشكلها الجديد
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Series>>() {}.getType();
            Map<String, Series> restoredData = gson.fromJson(json, mapType);

            // 3. هات الـ Map الخاص بالداتا بيز الجديدة (سيكون فارغاً)
            Map<String, Series> newSeriesMap = db.getMap(dataBases.SERIES.name());

            // 4. ضع الداتا المُسترجعة داخل الداتا بيز
            newSeriesMap.putAll(restoredData);

            // 5. احفظ التغييرات في الداتا بيز (مهم جداً في AbilityBot/MapDB)
            db.commit();

            System.out.println("✅ تم استرجاع البيانات للهيكلة الجديدة بنجاح!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
