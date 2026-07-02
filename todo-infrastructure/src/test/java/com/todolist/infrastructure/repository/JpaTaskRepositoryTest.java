package com.todolist.infrastructure.repository;

import com.todolist.domain.model.Priority;
import com.todolist.domain.model.Subtask;
import com.todolist.domain.model.Task;
import com.todolist.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTaskRepositoryTest {

    @Test
    void shouldKeepTaskIdWhenLoadingAndUpdating() {
        JpaTaskRepository repository = new JpaTaskRepository();
        String title = "Persisted task " + System.nanoTime();
        Task task = repository.save(new Task(title, "Original", Priority.MEDIUM, "project-1"));

        Task loaded = repository.findById(task.getId()).orElseThrow();
        loaded.setTitle("Updated " + title);
        loaded.setStatus(TaskStatus.IN_PROGRESS);
        repository.save(loaded);

        Task updated = repository.findById(task.getId()).orElseThrow();
        List<Task> matchingTasks = repository.findAll().stream()
                .filter(existing -> existing.getTitle().contains(title))
                .toList();

        assertEquals(task.getId(), loaded.getId());
        assertEquals(task.getId(), updated.getId());
        assertEquals("Updated " + title, updated.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        assertEquals(1, matchingTasks.size());
    }

    @Test
    void shouldKeepSubtaskIdAndCompletionWhenLoading() {
        JpaTaskRepository repository = new JpaTaskRepository();
        Task task = new Task("Task with subtask " + System.nanoTime(), "Original", Priority.HIGH, "project-1");
        Subtask subtask = new Subtask("First step");
        subtask.setCompleted(true);
        task.addSubtask(subtask);
        repository.save(task);

        Task loaded = repository.findById(task.getId()).orElseThrow();

        assertEquals(1, loaded.getSubtasks().size());
        assertTrue(loaded.getSubtasks().get(0).isCompleted());
        assertEquals(subtask.getId(), loaded.getSubtasks().get(0).getId());
    }
}
