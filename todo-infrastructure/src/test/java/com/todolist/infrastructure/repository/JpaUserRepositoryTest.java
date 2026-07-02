package com.todolist.infrastructure.repository;

import com.todolist.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JpaUserRepositoryTest {

    @Test
    void shouldKeepUserIdWhenLoadingByIdAndUsername() {
        JpaUserRepository repository = new JpaUserRepository();
        User user = repository.save(new User("user-" + System.nanoTime(), "password-hash"));

        User loadedById = repository.findById(user.getId()).orElseThrow();
        User loadedByUsername = repository.findByUsername(user.getUsername()).orElseThrow();

        assertEquals(user.getId(), loadedById.getId());
        assertEquals(user.getId(), loadedByUsername.getId());
        assertEquals(user.getUsername(), loadedByUsername.getUsername());
    }
}
