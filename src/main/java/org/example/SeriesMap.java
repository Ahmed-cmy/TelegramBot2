package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesMap implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Map<String, Lesson> lessonsByName = new HashMap<>();
    private Map<Integer, Lesson> lessonsById = new HashMap<>();

    public Map<String, Lesson> getLessonsMap(){
        return lessonsByName;
    }
    public boolean containsKey(String name){
        return lessonsByName.containsKey(name);
    }
    public Lesson getLesson(int i) {
        if(lessonsById.isEmpty()){
            return null;
        }
        return lessonsById.get(i);
    }
    public Lesson getLesson(String name){
        if(lessonsByName.isEmpty()){
            return null;
        }
        return lessonsByName.get(name);
    }
    public void setLessons(Map<String, Lesson> lessons) {
        this.lessonsByName = lessons;
        List<Lesson> values = new ArrayList<>(lessons.values());
        for (Lesson lesson:values){
            this.lessonsById.put(lesson.getId(),lesson);
        }
    }

    public void addLesson(Lesson lesson){
        if (lesson!=null){
            lessonsByName.put(lesson.getName(),lesson);
//            lesson.setId();
            lessonsById.put(lesson.getId(), lesson);
        }
    }
    public void removeLesson(String name){
        lessonsByName.remove(getLesson(name).getName());
        lessonsById.remove(getLesson(name).getId());
    }
    public void removeLesson(int id){
        lessonsByName.remove(getLesson(id).getName());
        lessonsById.remove(getLesson(id).getId());
    }
    public void removeLesson(Lesson lesson){
        lessonsByName.remove(lesson.getName());
        lessonsById.remove(lesson.getId());
    }
    public int size(){
        return lessonsByName.size();
    }

// Access value at index 0

}
