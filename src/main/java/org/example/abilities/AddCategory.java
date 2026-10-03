package org.example.abilities;


import org.example.*;
import org.example.TelegramBot.dataBases;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Map;
import java.util.function.Predicate;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

// 1. Define the extension class to manage specific functionality
public class AddCategory implements AbilityExtension {
    private final DBContext db;
    private final AbilityBot bot;
    private Map<Long, AdminUser> adminAction;

    public AddCategory(AbilityBot bot) {
        this.bot = bot;
        this.db = bot.getDb();
    }

    // 2. Define an Ability inside the extension
    public Ability addCategory() {
//                    System.out.println("add category 1");
        return Ability.builder()
                .name("add_category")
                .info("صنف")
                .privacy(ADMIN)
                .locality(ALL)
                .action(ctx -> {
//                    System.out.println("add category");
                    adminAction = TelegramBot.adminAction;
                    adminAction.put(ctx.chatId(), new AdminUser(ctx.chatId(), AdminUser.userActions.ADD_CATEGORY));
                    db.commit();
                    KeyboardRow row = new KeyboardRow("إلغاء");
                    ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                            .keyboardRow(row)
                            .resizeKeyboard(true)
                            .build();
                    bot.getSilent().execute(
                            SendMessage.builder()
                                    .chatId(ctx.chatId())
                                    .text("اكتب اسم المجموعة \n للإلغاء اضغط إلغاء")
                                    .replyMarkup(keyboard)
                                    .build()
                    );
                })
                .build();
    }

    public Reply getSeriesFromUser() {
        Predicate<Update> hasMessage = (update) -> update.hasMessage();
        Predicate<Update> isMessageHasText = (update) -> update.getMessage().hasText();
        Predicate<Update> isNotCommand = (update) -> !(update.getMessage().getText().startsWith("/"));
        Predicate<Update> isCommandUsed = update -> adminAction.containsKey(update.getMessage().getChatId());
        Predicate<Update> isUserWantAdd = update -> adminAction.get(update.getMessage().getChatId()).checkAction(AdminUser.userActions.ADD_CATEGORY);

        return Reply.of((bot, update) -> {
            try {

                long chatId = update.getMessage().getChatId();
                AdminUser currentUser = adminAction.get(chatId);
                String message = update.getMessage().getText();
//                System.out.println(currentUser.elementStack);

                if (message.equals("إلغاء")) {
                    adminAction.remove(chatId);
                    bot.getSilent().send("تم الإلغاء", chatId);
                    return;
                }
                if (message.equals("الصفحة التالية") && currentUser.page <= MainKeyboard.numberOfPages) {
//                    System.out.println("الصفحة التالية");
                    currentUser.page++;
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                    try {
                        bot.getTelegramClient().execute(SendMessage.builder()
                                .chatId(chatId)
                                .replyMarkup(currentUser.getKeyboardMarkup())
                                .text(String.format("""
                                        الصفحة %d  من  %d
                                        
                                        يمكنك ان تنتقل إلى الصفحة بكتابة رقم الصفحة
                                        """, currentUser.page, MainKeyboard.numberOfPages))
                                .build());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    return;
                } else if (message.equals("الصفحة السابقة") && currentUser.page > 0) {
                    currentUser.page--;
                    currentUser.setKeyboardMarkup(MainKeyboard.getAllSeries(currentUser.page));
                    try {
                        bot.getTelegramClient().execute(SendMessage.builder()
                                .chatId(chatId)
                                .replyMarkup(currentUser.getKeyboardMarkup())
                                .text(String.format("""
                                        الصفحة %d  من  %d
                                        
                                        يمكنك ان تنتقل إلى الصفحة بكتابة رقم الصفحة
                                        """, currentUser.page, MainKeyboard.numberOfPages))
                                .build());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adminAction.put(chatId, currentUser);
                    db.commit();
                    return;
                }

                if (message.equals("تم")) {
                    db.getMap(dataBases.SERIES.name()).put(currentUser.elementStack.peek().getName(), currentUser.elementStack.peek());
                    db.commit();
                    bot.getTelegramClient().execute(
                            SendMessage.builder()
                                    .chatId(currentUser.getChatId())
                                    .text("تم الحفظ")
                                    .replyMarkup(MainKeyboard.getMainKeyboard())
                                    .build()
                    );
                    adminAction.remove(chatId);
                } else if (TelegramBot.series.containsKey(message)) {
                    ((Category) currentUser.elementStack.peek()).addSeries((Series) TelegramBot.series.get(message));
                    adminAction.put(chatId, currentUser);
                    TelegramBot.series.remove(message);
                    db.commit();
                    bot.getTelegramClient().execute(
                            SendMessage.builder()
                                    .chatId(currentUser.getChatId())
                                    .text("اختر السلاسل التي تريد إضافتها إلى المجموعة اكتب تم للحفظ")
                                    .replyMarkup(MainKeyboard.getAllSeries(currentUser.page))
                                    .build()
                    );
                } else {
                    if (currentUser.elementStack.empty()) {
                        currentUser.elementStack.push(new Category(message));
                        adminAction.put(chatId, currentUser);
                        db.commit();
                    }
                    bot.getTelegramClient().execute(
                            SendMessage.builder()
                                    .chatId(currentUser.getChatId())
                                    .text("اختر السلاسل التي تريد إضافتها إلى المجموعة اكتب تم للحفظ")
                                    .replyMarkup(MainKeyboard.getAllSeries(currentUser.page))
                                    .build()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, hasMessage, isMessageHasText, isNotCommand, isCommandUsed, isUserWantAdd);
    }
}

