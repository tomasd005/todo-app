package com.todolist.infrastructure.repository;

import com.todolist.domain.model.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JpaProjectRepositoryTest {

    @Test
    void shouldKeepProjectIdWhenLoading() {
        JpaProjectRepository repository = new JpaProjectRepository();
        Project project = repository.save(new Project("Project " + System.nanoTime(), "Description"));

        Project loaded = repository.findById(project.getId()).orElseThrow();

        assertEquals(project.getId(), loaded.getId());
        assertEquals(project.getName(), loaded.getName());
        assertEquals(project.getDescription(), loaded.getDescription());
    }
}
