package org.example.abilities;

import org.example.*;
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

public class RemoveElemet implements AbilityExtension {
    private final DBContext db;
    private final AbilityBot bot;
    private Map<Long, AdminUser> adminAction;

    public RemoveElemet(AbilityBot bot) {
        this.bot = bot;
        db = bot.getDb();
    }

    public Ability removeElement() {
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
                                    .replyMarkup(MainKeyboard.getAllSeries(1))
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
//            String seriesName = adminAction.get(chatId).getSeries();

            if (currentUser.elementStack.empty()) { //|| db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(adminAction.get(chatId).getSeries())
                if (message.equals("العودة")) {
                    adminAction.remove(chatId);
                    bot.getSilent().send("تم الإلغاء", chatId);
                    return;
                }

                if (message.equals("الصفحة التالية")) {
                    currentUser.page++;
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    return;
                } else if (message.equals("الصفحة السابقة")) {
                    currentUser.page--;
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    return;
                }

                if (!db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(message)) {
                    bot.getSilent().send("تأكد من اسم السلسلة", chatId);
                    return;
                }

//                confirm deleting
                currentUser.elementStack.push(TelegramBot.series.get(message));
                adminAction.put(chatId, currentUser);
                db.commit();

                List<KeyboardRow> rows = new ArrayList<>();
                rows.add(new KeyboardRow("نعم", "لا"));
                rows.add(new KeyboardRow("العودة"));
                ReplyKeyboardMarkup confirm = ReplyKeyboardMarkup.builder()
                        .keyboard(rows)
                        .resizeKeyboard(true)
                        .build();

                bot.getSilent().send("هل انت متأكد من حذف السلسلة ", chatId);
                String name = currentUser.elementStack.peek().getName();
                bot.getSilent().execute(
                        SendMessage.builder()
                                .text(name)
                                .chatId(chatId)
                                .replyMarkup(confirm)
                                .build()
                );
                return;
            }

            if (message.equals("لا") && db.getMap(TelegramBot.dataBases.SERIES.name()).containsKey(currentUser.elementStack.peek())) {

                currentUser.elementStack.clear();
                adminAction.put(chatId, currentUser);
                db.commit();
                bot.getSilent().execute(
                        SendMessage.builder()
                                .text("يرجى اختيار سلسلة للحذف")
                                .chatId(chatId)
                                .replyMarkup(MainKeyboard.getAllSeries(currentUser.page))
                                .build()
                );
                return;
            }
            if (currentUser.elementStack.peek() instanceof Category category){
                List<Series> seriesList = new ArrayList<>(category.getMap().values());
                for (int i = 0; i < category.getMap().size(); i++) {
                    TelegramBot.series.put(seriesList.get(i).getName(),seriesList.get(i));
                }
            }
            db.getMap(TelegramBot.dataBases.SERIES.name()).remove(currentUser.elementStack.peek().getName());

            bot.getSilent().execute(
                    SendMessage.builder()
                            .text("تم الحفظ")
                            .chatId(chatId)
                            .replyMarkup(MainKeyboard.getMainKeyboard())
                            .build()
            );
            adminAction.remove(chatId);
            db.commit();
        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantDelete);
    }


}
