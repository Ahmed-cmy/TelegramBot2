package org.example;

import java.io.Serial;
import java.util.Stack;


public class AdminUser extends TelegramUser {
    @Serial
    private static final long serialVersionUID = 1L;
    public Stack<Enum<AdminStates>> states = new Stack<>();
    private Enum<userActions> action;
    private Enum<AdminStates> actionType;
//    private String series = "";
//    private String lesson = "";
//    public int page;
//    private long chatId;

    public AdminUser(long chatId, Enum<userActions> action) {
        this.chatId = chatId;
        this.action = action;
    }

    public Stack<Enum<AdminStates>> getStates() {
        return states;
    }

    public void setStates(Stack<Enum<AdminStates>> states) {
        this.states = states;
    }

    public Enum<AdminStates> getActionType() {
        return actionType;
    }

    public void setActionType(Enum<AdminStates> actionType) {
        this.actionType = actionType;
    }

    public boolean checkAction(Enum<userActions> action) {
        return this.action.equals(action);
    }

    public boolean checkEditAction(Enum<AdminStates> action) {
        return this.actionType.equals(action);
    }

    public Enum<userActions> getAction() {
        return action;
    }

    public void setAction(Enum<userActions> action) {
        this.action = action;
    }

//    public String getSeries() {
//        return series;
//    }

//    public void setSeries(String series) {
//        this.series = series;
//    }

//    public String getLesson() {
//        return lesson;
//    }

//    public void setLesson(Lesson lesson) {
//        this.lesson = lesson;
//    }

//    public long getChatId() {
//        return chatId;
//    }

//    public void setChatId(long chatId) {
//        this.chatId = chatId;
//    }

    public enum AdminStates {
        BASE,
        SERIES_SELECT,
        EDIT_TYPE,
        CHECK_EDIT_TYPE,
        ADD_LESSONS,
        EDIT_LESSON,
        SELECT_LESSON,
        ADD_FROM_INTERNET_ARCHIVE,
        ADD_FROM_CHANNEL
    }

    public static enum userActions {
        ADD_SERIES,
        ADD_CATEGORY,
        REMOVE_SERIES,
        EDIT
    }

}
