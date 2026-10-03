package org.example.abilities;

import org.example.*;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.function.Predicate;

import static org.example.TelegramBot.adminAction;
import static org.example.TelegramBot.series;

public class MainMenu implements AbilityExtension {
    Map<Long, NormalUser> NormalUsersMap;
    private DBContext db;

    public Reply getSeriesFromUser() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> {
            if (update.getMessage().getText().startsWith("/")) {
                NormalUsersMap.remove(update.getMessage().getChatId());
                db.commit();
            }
            return !(update.getMessage().getText().startsWith("/"));
        };
        Predicate<Update> isNotAdminCommand = update -> !adminAction.containsKey(update.getMessage().getChatId());

        return Reply.of((bot, update) -> {
            try{

            db = bot.getDb();
            NormalUsersMap = db.getMap(TelegramBot.dataBases.NORMAL_USERS.name());
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            NormalUsersMap.putIfAbsent(chatId, new NormalUser(chatId));
            db.commit();

            NormalUser currentUser = NormalUsersMap.get(chatId);
//            System.out.println(currentUser.page);

            switch (message) {
                case "جميع السلاسل":
                    try {
                        currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                        NormalUsersMap.put(chatId, currentUser);
                        db.commit();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                case "العودة":
//                    currentUser.setSeries(null);
//                    currentUser.setLesson(null);
//                    System.out.println(currentUser.getKeyboardMarkup());
                    currentUser.elementStack.clear();
                    currentUser.page = 1;
                    currentUser.setKeyboardMarkup(MainKeyboard.getMainKeyboard());
                    NormalUsersMap.put(chatId, currentUser);
                    db.commit();
                    break;
                case "العودة للسلسلة":
                    if (currentUser.elementStack.empty()){
                        currentUser.setKeyboardMarkup(MainKeyboard.getMainKeyboard());
                        NormalUsersMap.put(chatId, currentUser);
                        db.commit();
                        break;
                    }
//                    currentUser.setLesson(null);
                    currentUser.elementStack.pop();
                    currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                    NormalUsersMap.put(chatId, currentUser);
                    db.commit();
                    break;
                case "العودة للسلاسل":
                    currentUser.elementStack.clear();
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                    NormalUsersMap.put(chatId, currentUser);
                    db.commit();
                    break;
                default:
//                    try {
//
//                        if (message.matches("(?U)\\d+")) {
//                            int jump = Integer.parseInt(message);
//                            if (jump > MainKeyboard.numberOfPages || jump < 1) {
//                                break;
//                            }
//                            currentUser.page = jump;
//                            NormalUsersMap.put(chatId, currentUser);
//                            db.commit();
//                            break;
//                        }
//
//                        if (series.containsKey(message) && series.get(message) instanceof Series) {
//                            currentUser.setSeries((Series) series.get(message));
//                            currentUser.setKeyboardMarkup(series.get(message).getKeyboard());
//                            NormalUsersMap.put(chatId, currentUser);
//                            db.commit();
//                            break;
//                        }
//                        if (currentUser.getSeries() != null && currentUser.getLesson() == null) {
//                            if (message.equals("الصفحة التالية") && currentUser.getSeries().page < currentUser.getSeries().getLessons().size()) {
//                                currentUser.getSeries().page++;
//                                currentUser.setKeyboardMarkup(currentUser.getSeries().getKeyboard());
//                                NormalUsersMap.put(chatId, currentUser);
//                                db.commit();
//                                break;
//                            }
//                            if (message.equals("الصفحة السابقة") && currentUser.getSeries().page > 0) {
//                                currentUser.getSeries().page--;
//                                currentUser.setKeyboardMarkup(currentUser.getSeries().getKeyboard());
//                                NormalUsersMap.put(chatId, currentUser);
//                                db.commit();
//                                break;
//                            }
//                            if (message.matches("(?U)\\d")) {
//                                int jump = Integer.parseInt(message);
//                                if (jump > currentUser.getSeries().numberOfPages || jump < 1) {
//                                    break;
//                                }
//                                currentUser.getSeries().page = jump;
//                                currentUser.setKeyboardMarkup(currentUser.getSeries().getKeyboard());
//                                NormalUsersMap.put(chatId, currentUser);
//                                db.commit();
//                                break;
//                            }
//                            try {
//                                if (currentUser.getSeries().getLesson(message) != null) {
//                                    currentUser.setLesson(currentUser.getSeries().getLesson(message));
//                                    currentUser.setKeyboardMarkup(currentUser.getSeries().getLesson(message).getKeyboard());
//                                    NormalUsersMap.put(chatId, currentUser);
//                                    db.commit();
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            break;
//                        }
//
//                        if (message.equals("الصفحة التالية") && currentUser.page <= MainKeyboard.numberOfPages) {
//                            System.out.println("الصفحة التالية");
//                            currentUser.page++;
//                            currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
//                            NormalUsersMap.put(chatId, currentUser);
//                            db.commit();
//                            break;
//                        } else if (message.equals("الصفحة السابقة") && currentUser.page > 0) {
//                            currentUser.page--;
//                            currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
//                            NormalUsersMap.put(chatId, currentUser);
//                            db.commit();
//                            break;
//                        }
//
//                        if (currentUser.getLesson() != null && currentUser.getSeries().getLessons().containsKey(message)) {
//                            try {
//
//                                Lesson lesson = currentUser.getSeries().getLesson(currentUser.getLesson().getName());
//                                if (message.equals("صوتي")) {
//                                    try {
//                                        bot.getSilent().sendMd("[" + lesson.getName() + "](" + lesson.getVoiceLink() + ")", chatId);
//                                    } catch (Exception e) {
//                                        bot.getSilent().send("حدث خطأ", chatId);
//                                    }
//                                    break;
//                                }
//                                bot.getSilent().send("نأسف غير متوفر", chatId);
//                                return;
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    try {

                        if (message.matches("^(?U)\\d+$")) {
                            int jump = Integer.parseInt(message);
                            if (jump > MainKeyboard.numberOfPages || jump < 1) {
                                break;
                            }
                            currentUser.page = jump;
                            currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                            NormalUsersMap.put(chatId, currentUser);
                            db.commit();
                            break;
                        }
                        if (currentUser.elementStack.empty() && series.containsKey(message)){
                            currentUser.elementStack.push(series.get(message));
                            currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                            NormalUsersMap.put(chatId, currentUser);
                            db.commit();
                            break;
                        }
                        if (!currentUser.elementStack.empty()){
                            if (currentUser.elementStack.peek() instanceof Series currentSeries){
                            if (message.equals("الصفحة التالية") && currentSeries.page < currentSeries.numberOfPages) {
                                currentSeries.page++;
                                currentUser.setKeyboardMarkup(currentSeries.getKeyboard());
                                NormalUsersMap.put(chatId, currentUser);
                                db.commit();
                                break;
                            }
                            if (message.equals("الصفحة السابقة") && currentSeries.page > 0) {
                                currentSeries.page--;
                                currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                                NormalUsersMap.put(chatId, currentUser);
                                db.commit();
                                break;
                            }
                            if (message.matches("^(?U)\\d+$")) {
                                int jump = Integer.parseInt(message);
                                if (jump > currentSeries.numberOfPages || jump < 1) {
                                    break;
                                }
                                currentSeries.page = jump;
                                currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                                NormalUsersMap.put(chatId, currentUser);
                                db.commit();
                                break;
                            }
                            if (currentSeries.getLessons().containsKey(message)){
//                                System.out.println("in the series");
                                currentUser.elementStack.push(currentSeries.getLesson(message));
//                                System.out.println(currentUser.state.peek().getClass());
                                currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                                NormalUsersMap.put(chatId, currentUser);
                                db.commit();
                                break;
                            }
                            break;
                            }
                            if (currentUser.elementStack.peek() instanceof Category currentCategory){

                                if (message.equals("الصفحة التالية") && currentCategory.page < currentCategory.numberOfPages) {
                                    currentCategory.page++;
                                    currentUser.setKeyboardMarkup(currentCategory.getKeyboard());
                                    NormalUsersMap.put(chatId, currentUser);
                                    db.commit();
                                    break;
                                }
                                if (message.equals("الصفحة السابقة") && currentCategory.page > 0) {
                                    currentCategory.page--;
                                    currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                                    NormalUsersMap.put(chatId, currentUser);
                                    db.commit();
                                    break;
                                }
                                if (message.matches("^(?U)\\d+$")) {
                                    int jump = Integer.parseInt(message);
                                    if (jump > currentCategory.numberOfPages || jump < 1) {
                                        break;
                                    }
                                    currentCategory.page = jump;
                                    currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                                    NormalUsersMap.put(chatId, currentUser);
                                    db.commit();
                                    break;
                                }
                                if (currentCategory.getMap().containsKey(message)){
//                                    System.out.println("in the series");
                                    currentUser.elementStack.push(currentCategory.getSeries(message));
//                                System.out.println(currentUser.state.peek().getClass());
                                    currentUser.setKeyboardMarkup(currentUser.elementStack.peek().getKeyboard());
                                    NormalUsersMap.put(chatId, currentUser);
                                    db.commit();
                                    break;
                                }
                                break;
                            }
                        }
                        if (!currentUser.elementStack.empty() && currentUser.elementStack.peek() instanceof Lesson lesson){
                            if (message.equals("صوتي")) {
                                    try {
                                        bot.getSilent().sendMd("[" + lesson.getName() + "](" + lesson.getVoiceLink() + ")", chatId);
                                    } catch (Exception e) {
                                        bot.getSilent().send("حدث خطأ", chatId);
                                    }
                                    break;
                                }
                                bot.getSilent().send("نأسف غير متوفر", chatId);
                                return;
                        }
                        if (message.equals("الصفحة التالية") && currentUser.page <= MainKeyboard.numberOfPages) {
//                            System.out.println("الصفحة التالية");
                            currentUser.page++;
                            currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                            NormalUsersMap.put(chatId, currentUser);
                            db.commit();
                            break;
                        } else if (message.equals("الصفحة السابقة") && currentUser.page > 0) {
                            currentUser.page--;
                            currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                            NormalUsersMap.put(chatId, currentUser);
                            db.commit();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
//            String messageText = currentUser.getLesson() != null ?
//                    currentUser.getLesson().getName() : currentUser.getSeries() != null ?
//                                                        currentUser.getSeries().getName() +
//                                                        "\nالصفحة: " + currentUser.getSeries().page +
//                                                        " من " + currentUser.getSeries().maxPages +
//                                                        "\n يمكنك ان تنتقل إلى الصفحة بكتابة رقم الصفحة" :
//                                                        "اختر السلسلة";
//            System.out.println(currentUser.state.peek().getClass());
//            System.out.println(currentUser.getKeyboardMarkup());
            bot.getSilent().execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(generateMessageText(currentUser))
                            .replyMarkup(currentUser.getKeyboardMarkup())
                            .build()
            );
            } catch (Exception e) {
                e.printStackTrace();
            }


        }, hasMessage, isMessageHasText, isNotCommand, isNotAdminCommand);
    }
    private String generateMessageText(NormalUser currentUser) {
        // الشرط الأول
        if (currentUser.elementStack.empty() && currentUser.getKeyboardMarkup().equals(MainKeyboard.getMainKeyboard())) {
            return "الصفحة الرئيسية";
        }

        // الشرط الثاني
        if (currentUser.elementStack.empty() && !currentUser.getKeyboardMarkup().equals(MainKeyboard.getMainKeyboard())){
            return String.format("""
                    الصفحة %d  من  %d
                    
                    يمكنك ان تنتقل إلى الصفحة بكتابة رقم الصفحة
                    """, currentUser.page, MainKeyboard.numberOfPages);
        }
        if (currentUser.elementStack.peek() instanceof Series currentSeries) {
            return currentUser.elementStack.peek().getName() +
                    "\nالصفحة: " + currentSeries.page +
                    " من " + currentSeries.numberOfPages +
                    "\n يمكنك ان تنتقل إلى الصفحة بكتابة رقم الصفحة";
        }

        // الحالة الافتراضية (الـ else)
            return currentUser.elementStack.peek().getName();
    }

    //    public void sendLessonAudio(NormalUser currentUser, BaseAbilityBot bot) {
//            String audioUrl = currentUser.getLesson().getVoiceLink();
//            long chatId = currentUser.getChatId();
//            String lessonTitle = currentUser.getLesson().getName();
//        try {
//            // 1. فتح اتصال بالرابط من خلال الجافا
//            URL url = new URL(audioUrl);
//            URLConnection connection = url.openConnection();
//
//            // محاكاة متصفح عشان موقع الأرشيف ميرفضش الاتصال
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
//
//            // 2. تحويل الرابط إلى InputStream
//            InputStream inputStream = connection.getInputStream();
//
//            // 3. استخراج اسم الملف من الرابط (مهم جداً عشان تليجرام يعرف إنه mp3)
//            // مثلا هيستخرج "001.mp3" من الرابط
//            String fileName = audioUrl.substring(audioUrl.lastIndexOf("/") + 1);
//
//            // 4. إنشاء InputFile باستخدام الـ InputStream والاسم
//            InputFile inputFile = new InputFile(inputStream, fileName);
//
//            // 5. إعداد رسالة SendAudio
//            SendAudio sendAudio = SendAudio.builder()
//                    .chatId(chatId)
//                    .audio(inputFile)
//                    .title(lessonTitle)
//                    .performer("الشيخ إيهاب الشريف")
//                    .build();// العنوان تحت المقطع الصوتي
//
//            // 6. إرسال المقطع
//            bot.getTelegramClient().execute(sendAudio);
//
//            // إغلاق الـ Stream بعد الإرسال
//            inputStream.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            // لو حصل مشكلة، ممكن تبعت الرابط كنص عادي كحل بديل
//            bot.getSilent().send("الدرس: " + lessonTitle + "\nالرابط: " + audioUrl, chatId);
//        }
//    }
//    public String getFinalDirectUrl(String originalUrl) {
//        try {
//            URL url = new URL(originalUrl);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            // نمنع الجافا من تتبع التحويل تلقائياً عشان نمسكه إحنا
//            connection.setInstanceFollowRedirects(false);
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
//
//            // جلب كود حالة الرد
//            int status = connection.getResponseCode();
//
//            // لو الرد فيه تحويل (301, 302, 303)
//            if (status == HttpURLConnection.HTTP_MOVED_TEMP
//                    || status == HttpURLConnection.HTTP_MOVED_PERM
//                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
//
//                // نجيب الرابط الجديد من الهيدر
//                String redirectUrl = connection.getHeaderField("Location");
//
//                // لو الرابط ناقص بروتوكول نكملة
//                if (redirectUrl.startsWith("/")) {
//                    redirectUrl = "https://archive.org" + redirectUrl;
//                } else if (redirectUrl.startsWith("http://")) {
//                    // تليجرام بيفضل https
//                    redirectUrl = redirectUrl.replace("http://", "https://");
//                }
//                System.out.println(redirectUrl);
//                return redirectUrl; // ده الرابط المباشر النهائي لسيرفر الملف!
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return originalUrl; // لو مفيش تحويل، نرجع الرابط الأصلي
//    }
}
