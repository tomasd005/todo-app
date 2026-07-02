package com.todolist.infrastructure.repository;

import com.todolist.domain.model.Priority;
import com.todolist.domain.model.Task;
import com.todolist.domain.model.TaskStatus;
import com.todolist.domain.repository.TaskRepository;
import com.todolist.infrastructure.entity.SubtaskEntity;
import com.todolist.infrastructure.entity.TaskEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaTaskRepository implements TaskRepository {
    private final EntityManagerFactory emf;

    public JpaTaskRepository() {
        this.emf = Persistence.createEntityManagerFactory("todoPU");
    }

    private EntityManager entityManager() {
        return emf.createEntityManager();
    }

    @Override
    public Task save(Task task) {
        EntityManager em = entityManager();
        try {
            em.getTransaction().begin();
            TaskEntity entity = toEntity(task);
            em.merge(entity);
            em.getTransaction().commit();
            return task;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        EntityManager em = entityManager();
        try {
            TaskEntity entity = em.find(TaskEntity.class, id);
            return Optional.ofNullable(entity).map(this::toModel);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Task> findAll() {
        EntityManager em = entityManager();
        try {
            List<TaskEntity> entities = em.createQuery("SELECT t FROM TaskEntity t", TaskEntity.class).getResultList();
            return entities.stream().map(this::toModel).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(String id) {
        EntityManager em = entityManager();
        try {
            em.getTransaction().begin();
            TaskEntity entity = em.find(TaskEntity.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private TaskEntity toEntity(Task task) {
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setPriority(task.getPriority().name());
        entity.setStatus(task.getStatus().name());
        entity.setDueDate(task.getDueDate());
        entity.setProjectId(task.getProjectId());
        entity.setTags(task.getTags());
        entity.getSubtasks().clear();
        for (var subtask : task.getSubtasks()) {
            SubtaskEntity subtaskEntity = new SubtaskEntity(subtask.getId(), subtask.getTitle(), subtask.isCompleted(), entity);
            entity.getSubtasks().add(subtaskEntity);
        }
        return entity;
    }

    private Task toModel(TaskEntity entity) {
        Task task = new Task(entity.getTitle(), entity.getDescription(), Priority.valueOf(entity.getPriority()), entity.getProjectId());
        task.setStatus(TaskStatus.valueOf(entity.getStatus()));
        task.setDueDate(entity.getDueDate());
        for (String tag : entity.getTags()) {
            task.addTag(tag);
        }
        for (SubtaskEntity subtaskEntity : entity.getSubtasks()) {
            task.addSubtask(new com.todolist.domain.model.Subtask(subtaskEntity.getTitle()));
        }
        return task;
    }
}
