package org.example.abilities;

import org.example.AdminUser;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RemoveSeries implements AbilityExtension {
    private Map<Long, AdminUser> adminAction;
    private final DBContext db;
    private final AbilityBot bot;

    public RemoveSeries(AbilityBot bot) {
        this.bot = bot;
        db = bot.getDb();
    }

    public Ability removeSeries() {
        return Ability.builder()
                .name("remove")
                .info("إزاله سلسلة")
                .privacy(Privacy.ADMIN)
                .locality(Locality.ALL)
                .action(ctx -> {
                    adminAction = TelegramBot.adminAction;
                    adminAction.put(ctx.chatId(), new AdminUser(ctx.chatId(), AdminUser.userActions.REMOVE_SERIES));
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للحذف")
                                    .chatId(ctx.chatId())
                                    .replyMarkup(MainKeyboard.getAllSeries())
                                    .build()
                    );
                })
                .build();
    }

    public Reply getRemovedSeries() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isCommandUsed = update -> adminAction.containsKey(update.getMessage().getChatId());
        Predicate<Update> isUserWantDelete = update -> adminAction.get(update.getMessage().getChatId()).checkAction(AdminUser.userActions.REMOVE_SERIES);

        return Reply.of((bot, update) -> {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            AdminUser currentUser = adminAction.get(chatId);
            String seriesName = adminAction.get(chatId).getSeries();

            if (currentUser.getSeries().isEmpty() ) { //|| db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(adminAction.get(chatId).getSeries())
                if (message.equals("العودة")) {
                    adminAction.remove(chatId);
                    bot.getSilent().send("تم الإلغاء", chatId);
                    return;
                }
                if (!db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(message)) {
                    bot.getSilent().send("تأكد من اسم السلسلة", chatId);
                    return;
                }

                if (message.equals("لا") && db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(currentUser.getSeries())) {

                    currentUser.setSeries("");
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .text("يرجى اختيار سلسلة للحذف")
                                    .chatId(chatId)
                                    .replyMarkup(MainKeyboard.getAllSeries())
                                    .build()
                    );
                    return;
                }
//                confirm deleting
                currentUser.setSeries(message);
                adminAction.put(chatId, currentUser);

                List<KeyboardRow> rows = new ArrayList<>();
                rows.add(new KeyboardRow("نعم", "لا"));
                rows.add(new KeyboardRow("العودة"));
                ReplyKeyboardMarkup confirm = ReplyKeyboardMarkup.builder()
                        .keyboard(rows)
                        .resizeKeyboard(true)
                        .build();

                bot.getSilent().send("هل انت متأكد من حذف السلسلة ", chatId);
                Series name = (Series) db.getMap(TelegramBot.dataBases.SERIES.name()).get(message);
                bot.getSilent().execute(
                        SendMessage.builder()
                                .text(name.getName())
                                .chatId(chatId)
                                .replyMarkup(confirm)
                                .build()
                );
                return;
            }

            db.getMap(TelegramBot.dataBases.SERIES.name()).remove(adminAction.get(chatId).getSeries());
            adminAction.remove(chatId);
            db.commit();

            bot.getSilent().execute(
                    SendMessage.builder()
                            .text("تم الحفظ")
                            .chatId(chatId)
                            .replyMarkup(MainKeyboard.getMainKeyboard())
                            .build()
            );
        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantDelete);
    }


}
