package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.Stack;


public class AdminUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public Stack<Enum<editStates>> states = new Stack<>();
    private Enum<userActions> action;
    private Enum<editStates> editAction;
    private String series = "";
    private String lesson = "";
    private long chatId;

    public AdminUser(long chatId, Enum<userActions> action) {
        this.chatId = chatId;
        this.action = action;
    }

    public Stack<Enum<editStates>> getStates() {
        return states;
    }

    public void setStates(Stack<Enum<editStates>> states) {
        this.states = states;
    }

    public Enum<editStates> getEditAction() {
        return editAction;
    }

    public void setEditAction(Enum<editStates> editAction) {
        this.editAction = editAction;
    }

    public boolean checkAction(Enum<userActions> action) {
        return this.action.equals(action);
    }

    public boolean checkEditAction(Enum<editStates> action) {
        return this.editAction.equals(action);
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

    public enum editStates {
        BASE,
        SERIES_SELECT,
        EDIT_TYPE,
        CHECK_EDIT_TYPE,
        ADD_LESSONS,
        EDIT_LESSON,
        SELECT_LESSON
    }

    public static enum userActions {
        ADD_SERIES,
        REMOVE_SERIES,
        EDIT
    }

}
