package com.todolist.application.service;

import com.todolist.domain.model.Priority;
import com.todolist.domain.model.Task;
import com.todolist.domain.model.TaskStatus;
import com.todolist.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskServiceTest {

    @Test
    void shouldCreateTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository);

        Task task = service.createTask("Estudar Java", "Revisar arquitetura", Priority.HIGH, "project-1");

        assertNotNull(task.getId());
        assertEquals("Estudar Java", task.getTitle());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void shouldUpdateTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository);

        Task task = service.createTask("Tarefa", "Descrição", Priority.LOW, "project-1");
        Task updated = service.updateTask(task.getId(), "Tarefa atualizada", "Nova descrição", Priority.MEDIUM, TaskStatus.IN_PROGRESS, LocalDate.now());

        assertEquals("Tarefa atualizada", updated.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        assertEquals(LocalDate.now(), updated.getDueDate());
    }

    private static class InMemoryTaskRepository implements TaskRepository {
        private final List<Task> tasks = new ArrayList<>();

        @Override
        public Task save(Task task) {
            tasks.removeIf(existing -> existing.getId().equals(task.getId()));
            tasks.add(task);
            return task;
        }

        @Override
        public Optional<Task> findById(String id) {
            return tasks.stream().filter(task -> task.getId().equals(id)).findFirst();
        }

        @Override
        public List<Task> findAll() {
            return new ArrayList<>(tasks);
        }

        @Override
        public void deleteById(String id) {
            tasks.removeIf(task -> task.getId().equals(id));
        }
    }
}
