package com.project_x.project_x_backend.dao;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class UserDAO {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}
