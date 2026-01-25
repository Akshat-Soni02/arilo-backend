package com.project_x.project_x_backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.entity.LlmMetric;
import com.project_x.project_x_backend.repository.LlmMetricRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LlmMetricDAO {
    @Autowired
    private LlmMetricRepository llmMetricRepository;

    public List<LlmMetric> getAllLlmMetrics() {
        return llmMetricRepository.findAll();
    }

    public LlmMetric getLlmMetricById(UUID id) {
        return llmMetricRepository.findById(id).orElse(null);
    }
}
