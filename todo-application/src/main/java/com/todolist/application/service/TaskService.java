package com.todolist.application.service;

import com.todolist.domain.model.Priority;
import com.todolist.domain.model.Task;
import com.todolist.domain.model.TaskStatus;
import com.todolist.domain.repository.TaskRepository;

import java.time.LocalDate;
import java.util.List;

public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(String title, String description, Priority priority, String projectId) {
        Task task = new Task(title, description, priority, projectId);
        return taskRepository.save(task);
    }

    public Task updateTask(String id, String title, String description, Priority priority, TaskStatus status, LocalDate dueDate) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        task.setDueDate(dueDate);
        return taskRepository.save(task);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> listTasks() {
        return taskRepository.findAll();
    }

    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }
}
