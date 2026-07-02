package com.todolist.infrastructure.repository;

import com.todolist.domain.model.Project;
import com.todolist.domain.repository.ProjectRepository;
import com.todolist.infrastructure.entity.ProjectEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaProjectRepository implements ProjectRepository {
    private final EntityManagerFactory emf;

    public JpaProjectRepository() {
        this.emf = Persistence.createEntityManagerFactory("todoPU");
    }

    private EntityManager entityManager() {
        return emf.createEntityManager();
    }

    @Override
    public Project save(Project project) {
        EntityManager em = entityManager();
        try {
            em.getTransaction().begin();
            ProjectEntity entity = toEntity(project);
            em.merge(entity);
            em.getTransaction().commit();
            return project;
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
    public Optional<Project> findById(String id) {
        EntityManager em = entityManager();
        try {
            ProjectEntity entity = em.find(ProjectEntity.class, id);
            return Optional.ofNullable(entity).map(this::toModel);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Project> findAll() {
        EntityManager em = entityManager();
        try {
            List<ProjectEntity> entities = em.createQuery("SELECT p FROM ProjectEntity p", ProjectEntity.class).getResultList();
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
            ProjectEntity entity = em.find(ProjectEntity.class, id);
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

    private ProjectEntity toEntity(Project project) {
        return new ProjectEntity(project.getId(), project.getName(), project.getDescription());
    }

    private Project toModel(ProjectEntity entity) {
        return new Project(entity.getId(), entity.getName(), entity.getDescription());
    }
}
