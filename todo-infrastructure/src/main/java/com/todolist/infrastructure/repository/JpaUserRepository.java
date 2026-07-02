package com.todolist.infrastructure.repository;

import com.todolist.domain.model.User;
import com.todolist.domain.repository.UserRepository;
import com.todolist.infrastructure.entity.UserEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaUserRepository implements UserRepository {
    private final EntityManagerFactory emf;

    public JpaUserRepository() {
        this.emf = Persistence.createEntityManagerFactory("todoPU");
    }

    private EntityManager entityManager() {
        return emf.createEntityManager();
    }

    @Override
    public User save(User user) {
        EntityManager em = entityManager();
        try {
            em.getTransaction().begin();
            UserEntity entity = toEntity(user);
            em.merge(entity);
            em.getTransaction().commit();
            return user;
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
    public Optional<User> findById(String id) {
        EntityManager em = entityManager();
        try {
            UserEntity entity = em.find(UserEntity.class, id);
            return Optional.ofNullable(entity).map(this::toModel);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        EntityManager em = entityManager();
        try {
            UserEntity entity = em.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.ofNullable(entity).map(this::toModel);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAll() {
        EntityManager em = entityManager();
        try {
            List<UserEntity> entities = em.createQuery("SELECT u FROM UserEntity u", UserEntity.class).getResultList();
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
            UserEntity entity = em.find(UserEntity.class, id);
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

    private UserEntity toEntity(User user) {
        return new UserEntity(user.getId(), user.getUsername(), user.getPasswordHash());
    }

    private User toModel(UserEntity entity) {
        User user = new User(entity.getUsername(), entity.getPasswordHash());
        return user;
    }
}
