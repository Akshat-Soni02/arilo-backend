package com.project_x.project_x_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project_x.project_x_backend.service.TaskService;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // @GetMapping("")
    // public List<Task> getAllTasks() {
    // return taskService.getAllTasks();
    // }

    // @PostMapping("/id")
    // public Task updateTask(@PathVariable UUID id) {
    // return taskService.updateTask(id);
    // }
}
