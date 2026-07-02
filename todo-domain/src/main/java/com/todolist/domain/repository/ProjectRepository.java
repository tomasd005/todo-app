package com.todolist.domain.repository;

import com.todolist.domain.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(String id);
    List<Project> findAll();
    void deleteById(String id);
}
