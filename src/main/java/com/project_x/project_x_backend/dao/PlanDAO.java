package com.project_x.project_x_backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.repository.PlanRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PlanDAO {
    @Autowired
    private PlanRepository planRepository;

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Plan getPlanById(UUID id) {
        return planRepository.findById(id).orElse(null);
    }
}
