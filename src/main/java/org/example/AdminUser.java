package org.example;

import java.io.Serializable;

public class AdminUser implements Serializable {
    public static enum userActions{
        ADD_SERIES,
        REMOVE_SERIES;
        enum EDIT_SERIES{
            ADD_LESSON,
            REMOVE_LESSON,
            EDIT_LESSON
        }
    }
    private Enum<userActions> action;
    private String series = "";
    private String lesson = "";
    private long chatId;
    public AdminUser(long chatId, Enum<userActions> action){
        this.chatId = chatId;
        this.action = action;
//        System.out.println(TelegramBot.adminAction.keySet());
    }
    public boolean checkAction (Enum<userActions> action){
        return this.action.equals(action);
    }
    public Enum<userActions> getAction() {
        return action;
    }

    public void setAction(Enum<userActions> action) {
        this.action = action;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getLesson() {
        return lesson;
    }

    public void setLesson(String lesson) {
        this.lesson = lesson;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
