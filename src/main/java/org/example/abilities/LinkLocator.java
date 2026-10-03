package org.example.abilities;

import org.example.Lesson;
import org.example.Series;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkLocator implements AbilityExtension {
    private static AbilityBot bot;

    public LinkLocator(AbilityBot bot) {
        LinkLocator.bot = bot;
    }

    public static Series seriesGetter(String currentURL, long chatId) throws Exception {
        Series series = new Series();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(currentURL.replace("details", "metadata")))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());

            if (jsonResponse.has("metadata")) {
                String seriesTitle = jsonResponse.getJSONObject("metadata").optString("title", "بدون عنوان");
                Pattern pattern = Pattern.compile("الشيخ.\\s*إيهاب الشريف");
                Matcher m = pattern.matcher(seriesTitle);
                seriesTitle = m.replaceAll("").trim();
                series.setName(seriesTitle);
                bot.getSilent().send("اسم السلسلة : " + seriesTitle, chatId);
            }

            if (jsonResponse.has("files")) {
                JSONArray files = jsonResponse.getJSONArray("files");
                for (int i = 0; i < files.length(); i++) {
                    JSONObject file = files.getJSONObject(i);
                    String fileName = file.getString("name");

                    // فلترة للملفات الصوتية (أو mp4 للفيديو)
                    if (fileName.endsWith(".mp3")) {

                        // 💡 استخراج العنوان (لو مش موجود هناخد اسم الملف نفسه)
                        String lessonTitle = file.has("title") ? file.getString("title") : fileName.replace(".mp3", "");
                        Pattern pattern = Pattern.compile("الشيخ\\s*.\\s*إيهاب الشريف");
                        Matcher m = pattern.matcher(lessonTitle);
                        lessonTitle = m.replaceAll("").trim();
                        // تكوين الرابط المباشر
                        String encodedFileName = fileName.replace(" ", "%20");
                        String directLink = currentURL.replace("details", "download") + "/" + encodedFileName;

                        // إضافة الدرس للقائمة
                        series.addLesson(new Lesson(lessonTitle, directLink));
                    }
                }
            }
//            System.out.println("after" + series);
            return series;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
