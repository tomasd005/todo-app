package com.todolist.application.service;

import com.todolist.domain.model.Project;
import com.todolist.domain.repository.ProjectRepository;

import java.util.List;

public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(String name, String description) {
        Project project = new Project(name, description);
        return projectRepository.save(project);
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }
}
