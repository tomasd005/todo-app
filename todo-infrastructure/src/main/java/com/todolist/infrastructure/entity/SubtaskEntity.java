package com.todolist.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subtasks")
public class SubtaskEntity {
    @Id
    @Column(length = 36)
    private String id;

    private String title;
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    public SubtaskEntity() {
    }

    public SubtaskEntity(String id, String title, boolean completed, TaskEntity task) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.task = task;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }
}
