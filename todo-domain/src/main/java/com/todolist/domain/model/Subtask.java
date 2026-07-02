package com.todolist.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Subtask {
    private final String id;
    private String title;
    private boolean completed;

    public Subtask(String title) {
        this(UUID.randomUUID().toString(), title, false);
    }

    public Subtask(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(id, subtask.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Subtask clone() {
        Subtask clone = new Subtask(title);
        clone.completed = completed;
        return clone;
    }
}
