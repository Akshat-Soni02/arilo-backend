package com.project_x.project_x_backend.dao;

import org.springframework.stereotype.Component;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class UserDAO {

    @Autowired
    private UserRepository userRepository;
}
