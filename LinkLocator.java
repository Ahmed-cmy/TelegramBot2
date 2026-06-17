package org.example.abilities;

import org.example.AdminUser;
import org.example.Lesson;
import org.example.Series;
import org.example.TelegramBot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkLocator implements AbilityExtension {
    private static AbilityBot bot;
//    private DBContext db;

    public LinkLocator(AbilityBot bot) {
        this.bot = bot;
//        db = bot.getDb();
    }

    public static Series seriesGetter(String seriesPage, long chatId) throws Exception {
        String currentURL = seriesPage;
        Series series = new Series();
//        Series series = new Series();
        List<String> seriesLessonsPages = new ArrayList<>();
//        System.out.println("currentURL" + currentURL);
        try {
            while (currentURL != null && !currentURL.isEmpty()) {
                Document doc = Jsoup.connect(currentURL).get();

                Elements lessonsSet = doc.select("div.col-grid article.item a.global-link");
                Elements title = doc.select("section.part div.head a h3");
                series.setName(title.text());
//                System.out.println(series.getName());
                for (Element lesson : lessonsSet) {
                    seriesLessonsPages.add(lesson.attr("abs:href"));
                }
                Element nextPage = doc.selectFirst("a.page-link[rel=\"next\"]");
                if (nextPage != null) {
                    currentURL = nextPage.attr("abs:href");
                } else {
//                    System.out.println("completed");
                    currentURL = null;
                }
            }
            bot.getSilent().send("اسم السلسلة : " + series.getName(), chatId);
//            if (bot.getDb().getMap(TelegramBot.dataBases.SERIES.name()).containsKey(series.getName())){
//                return ;
//            }

            try (ExecutorService executor = Executors.newFixedThreadPool(seriesLessonsPages.size())) {

                Lesson.count = 0;
                for (int j = 0; j < seriesLessonsPages.size(); j++) {
                    int finalJ = j;
                    executor.submit(() -> {
                        try {
                            // جلب صفحة الدرس مع تحديد User-Agent و Timeout لتفادي التعليق
                            Document lessonDoc = Jsoup.connect(seriesLessonsPages.get(finalJ))
                                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                    .timeout(100000)
                                    .get();

                            Elements downloadLinks = lessonDoc.select("a.btn.downlod");

                            // التحقق من وجود العنصر الثاني لتفادي IndexOutOfBoundsException
                            Element link = downloadLinks.getLast();
                            String finalDownloadUrl = link.attr("abs:href");
                            String title = lessonDoc.title().replace(" - موقع أنا السلفي", "").replace(series.getName(),"").replace("الشيخ إيهاب الشريف", "").trim();
                            if (title.length() < 5){
                                title += "الحلقة";
                            }
//                            Matcher m = Pattern.compile("\\d+-").matcher(title);
//                            title = m.replaceAll("").trim();
                            Lesson lesson = new Lesson(title, finalDownloadUrl);
                            lesson.setId(finalJ);
                            series.addLesson(lesson);
                            bot.getSilent().send("تم " + Lesson.count + " / " + seriesLessonsPages.size(), chatId);


                        } catch (Exception e) {
                            System.err.println("err " + seriesLessonsPages.get(finalJ) + " reson " + e.getMessage());
                            bot.getSilent().send("تعذر الوصول الى الدرس" + seriesLessonsPages.get(finalJ), chatId);
                        }
                    });
                }
                executor.shutdown();
            }
//            System.out.println("after" + series);
            return series;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
