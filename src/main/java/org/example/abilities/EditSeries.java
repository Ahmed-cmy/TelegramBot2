package org.example.abilities;

import org.example.Lesson;
import org.example.MainKeyboard;
import org.example.Series;
import org.example.TelegramBot;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.function.Predicate;

public class EditSeries implements AbilityExtension {
    public static Map<Long, Map<String, String>> userState;
    private final DBContext db;
    private final AbilityBot bot;
    private final String editing = "editing";
    private final Map<String, Series> series;
    private Map<String, String> state = new HashMap<>();

    public EditSeries(AbilityBot bot) {
        this.bot = bot;
        db = bot.getDb();
        userState = db.getMap(TelegramBot.dataBases.EDIT_USER_STATE.name());
        series = db.getMap(TelegramBot.dataBases.SERIES.name());
    }

    public Ability editSeries() {
        return Ability.builder()
                .name("edit")
                .info("تعديل سلسلة")
                .privacy(Privacy.ADMIN)
                .locality(Locality.ALL)
                .action(ctx -> {
                    state.put("STATE", editing);
                    userState.put(ctx.chatId(), state);
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للتعديل")
                                    .chatId(ctx.chatId())
                                    .replyMarkup(getAllSeries())
                                    .build()
                    );
                })
                .build();
    }

    public Reply getEditedSeries() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isCommandUsed = update -> userState.containsKey(update.getMessage().getChatId());

        return Reply.of((bot, update) -> {
            String message = update.getMessage().getText();
//            String seriesName = adminAction.get(update.getMessage().getChatId());
            long chatId = update.getMessage().getChatId();
            String currentState = userState.get(chatId).getOrDefault("STATE", "");

            if (currentState.equals(editing) || db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(userState.get(chatId).get("SELECTED"))) {
                System.out.println("1");
                if (message.equals("العودة")) {
                    userState.remove(chatId);
                    bot.getSilent().send("تم الإلغاء", chatId);
                    return;
                }
                if (currentState.equals(editing) && !db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(message)) {
                    bot.getSilent().send("تأكد من اسم السلسلة", chatId);
                    return;
                }

                if (message.equals("اختيار سلسلة أخرى") && db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(userState.get(chatId).get("SELECTED"))) {
                    state.put("STATE", editing);
                    userState.put(chatId, state);
                    db.commit();
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للحذف")
                                    .chatId(chatId)
                                    .replyMarkup(getAllSeries())
                                    .build()
                    );
                    return;
                }
//                confirm deleting

                if (userState.get(chatId).get("STATE").equals(editing)) {
                    List<KeyboardRow> rows = new ArrayList<>();
                    rows.add(new KeyboardRow("إضافة درس/دروس", "حذف درس", "تعديل درس"));
                    rows.add(new KeyboardRow("اختيار سلسلة أخرى"));
                    rows.add(new KeyboardRow("العودة"));

                    ReplyKeyboardMarkup confirm = ReplyKeyboardMarkup.builder()
                            .keyboard(rows)
                            .resizeKeyboard(true)
                            .build();

                    bot.getSilent().send("اختر التعديل", update.getMessage().getChatId());
                    Series name = (Series) db.getMap(TelegramBot.dataBases.SERIES.name()).get(message);
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("السلسلة :" + name.getName())
                                    .chatId(chatId)
                                    .replyMarkup(confirm)
                                    .build()
                    );
                    state.put("SELECTED", message);
                    state.put("STATE", "");
                    userState.put(chatId, state);
//                System.out.println(adminAction.get(chatId).get("STATE"));
                    return;
                }
            }
            System.out.println("gg");
            switch (message) {
                case "إضافة درس/دروس":
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("اكتب اسم الدرس ثم , ثم اللينك")
                                    .chatId(chatId)
                                    .replyMarkup(getSeriesLessonsAsKeyboard(series.get(userState.get(chatId).get("SELECTED"))))
                                    .build()
                    );
                    addNewLessons(chatId, message);
                    break;
                case "حذف درس":
                    break;
                case "تعديل درس":
                    break;
                default:
                    bot.getSilent().send("اختيار خاطئ", chatId);
            }
//            db.getMap(TelegramBot.dataBases.SERIES.name()).remove(adminAction.get(chatId));
//            adminAction.remove(chatId);
//            TelegramBot.admin = false;
//            db.commit();

//            bot.getSilent().execute(
//                    SendMessage.builder()
//                            .text("تم الحفظ")
//                            .chatId(chatId)
//                            .replyMarkup(MainKeyboard.getMainKeyboard())
//                            .build()
//            );
//            commandUsed = !commandUsed;
        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed);
    }

    public ReplyKeyboardMarkup getAllSeries() {
        List<KeyboardRow> rows = new ArrayList<>();
        List<Series> temp = new ArrayList<>(series.values());
        for (int i = 0; i < series.size(); i++) {
            rows.add(new KeyboardRow(temp.get(i).getName()));
        }
        rows.add(new KeyboardRow("العودة"));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup getSeriesLessonsAsKeyboard(Series selectedSeries) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < selectedSeries.getLessons().size(); i++) {
            int rowsCount = 0;
            rows.add(new KeyboardRow());
//            System.out.println(lesson.getId());
            if (i % 2 == 0 && i != 0) {
                rowsCount++;
                rows.add(new KeyboardRow());
            }
            rows.get(rowsCount).add(selectedSeries.getLessons().getLesson(i).getName());
        }
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

    public void addNewLessons(long chatId, String message) {
        Scanner scanner = new Scanner(message);
        Series selectedSeries = series.getOrDefault(userState.get(chatId).get("SELECTED"), new Series(""));
        if (selectedSeries.getName().isEmpty()) {
            bot.getSilent().send("حدث خطأ", chatId);
            return;
        }
//            System.out.println(seriesName);
        Lesson.count = selectedSeries.getLessons().size() - 1;
        while (scanner.hasNextLine()) {
            String[] lessonData = scanner.nextLine().split(",");
            if (lessonData.length < 2) {
                bot.getSilent().send("Invalid", chatId);
                return;
            }
            if (selectedSeries.getLessons().containsKey(lessonData[0])) {
                bot.getSilent().send("يوجد درس مكرر يرجى التحقق", chatId);
                return;
            }
            selectedSeries.getLessons().addLesson(new Lesson(lessonData[0], lessonData[1]));
        }
        System.out.println("lesson added");
        userState.remove(chatId);
//            TelegramBot.admin = false;
//        db.getMap(TelegramBot.dataBases.SERIES.name()).put(seriesName,series);
        db.commit();

        bot.getSilent().execute(
                SendMessage.builder()
                        .text("تم الحفظ")
                        .chatId(chatId)
                        .replyMarkup(MainKeyboard.getMainKeyboard())
                        .build()
        );
    }
}
