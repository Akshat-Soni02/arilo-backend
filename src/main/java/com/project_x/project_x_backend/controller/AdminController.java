package com.project_x.project_x_backend.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project_x.project_x_backend.dao.JobDAO;
import com.project_x.project_x_backend.dao.LlmMetricDAO;
import com.project_x.project_x_backend.dao.PlanDAO;
import com.project_x.project_x_backend.dao.SubscriptionDAO;
import com.project_x.project_x_backend.dao.UserDAO;
import com.project_x.project_x_backend.dto.UserDetailDTO;
import com.project_x.project_x_backend.dto.LLM.LlmMetricDetailDTO;
import com.project_x.project_x_backend.dto.PlanDTO.PlanDetailDTO;
import com.project_x.project_x_backend.dto.SubscriptionDTO.SubscriptionDetailDTO;
import com.project_x.project_x_backend.dto.jobDTO.JobDetailRes;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.LlmMetric;
import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.User;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    // Functionalities for admin
    // 1. Get all jobs {id, userId, noteId, status, error, createdAt, updatedAt}
    // 2. Get all llm metrics {id, userId, jobId, pipelineStageId, llmCall,
    // inputTokens, outputTokens, promptTokens, totalInputTokens, thoughtTokens,
    // confidenceScore, elapsedTime, model, createdAt, updatedAt}
    // 3. Get all plans {id, name, price, currency, durationDays, isActive,
    // createdAt}
    // 4. Get all subscriptions {id, userId, planId, status, startDate, endDate,
    // autoRenew, cancelledAt, createdAt, updatedAt}
    // 5. Get all users {id, email, googleId, name, createdAt, updatedAt,
    // deletedAt}

    @Autowired
    private JobDAO jobDAO;

    @Autowired
    private LlmMetricDAO llmMetricDAO;

    @Autowired
    private PlanDAO planDAO;

    @Autowired
    private SubscriptionDAO subscriptionDAO;

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/jobs")
    public List<JobDetailRes> getAllJobs() {
        try {
            List<JobDetailRes> jobDetailRes = new ArrayList<>();

            for (Job job : jobDAO.getAllJobs()) {
                JobDetailRes jobDetail = new JobDetailRes();
                jobDetail.setId(job.getId());
                jobDetail.setUserId(job.getUserId());
                jobDetail.setNoteId(job.getNoteId());
                jobDetail.setStatus(job.getStatus());
                jobDetail.setError(job.getErrorMessage());
                jobDetail.setCreatedAt(job.getCreatedAt());
                jobDetail.setUpdatedAt(job.getUpdatedAt());
                jobDetailRes.add(jobDetail);
            }
            return jobDetailRes;
        } catch (Exception e) {
            log.error("Failed to get all jobs: {}", e.getMessage(), e);
            return null;
        }
    }

    @GetMapping("/llm-metrics")
    public List<LlmMetricDetailDTO> getAllLlmMetrics() {
        try {
            List<LlmMetricDetailDTO> llmMetricRes = new ArrayList<>();

            for (LlmMetric llmMetric : llmMetricDAO.getAllLlmMetrics()) {
                LlmMetricDetailDTO llmMetricDetail = new LlmMetricDetailDTO();
                llmMetricDetail.setId(llmMetric.getId());
                llmMetricDetail.setUserId(llmMetric.getUser().getId());
                llmMetricDetail.setJobId(llmMetric.getJob().getId());
                llmMetricDetail.setPipelineStageId(llmMetric.getPipelineStage().getId());
                llmMetricDetail.setLlmCall(llmMetric.getLlmCall());
                llmMetricDetail.setInputTokens(llmMetric.getInputTokens());
                llmMetricDetail.setOutputTokens(llmMetric.getOutputTokens());
                llmMetricDetail.setPromptTokens(llmMetric.getPromptTokens());
                llmMetricDetail.setTotalInputTokens(llmMetric.getTotalInputTokens());
                llmMetricDetail.setThoughtTokens(llmMetric.getThoughtTokens());
                llmMetricDetail.setConfidenceScore(llmMetric.getConfidenceScore());
                llmMetricDetail.setElapsedTime(llmMetric.getElapsedTime());
                llmMetricDetail.setModel(llmMetric.getModel());
                llmMetricDetail.setCreatedAt(llmMetric.getCreatedAt());
                llmMetricDetail.setUpdatedAt(llmMetric.getUpdatedAt());
                llmMetricRes.add(llmMetricDetail);
            }
            return llmMetricRes;
        } catch (Exception e) {
            log.error("Failed to get all llm metrics: {}", e.getMessage(), e);
            return null;
        }
    }

    @GetMapping("/plans")
    public List<PlanDetailDTO> getAllPlans() {
        try {
            List<PlanDetailDTO> planDetailRes = new ArrayList<>();

            for (Plan plan : planDAO.getAllPlans()) {
                PlanDetailDTO planDetail = new PlanDetailDTO();
                planDetail.setId(plan.getId());
                planDetail.setName(plan.getName());
                planDetail.setPrice(plan.getPrice());
                planDetail.setCurrency(plan.getCurrency());
                planDetail.setDurationDays(plan.getDurationDays());
                planDetail.setIsActive(plan.getIsActive());
                planDetail.setCreatedAt(plan.getCreatedAt());
                planDetailRes.add(planDetail);
            }
            return planDetailRes;
        } catch (Exception e) {
            log.error("Failed to get all plans: {}", e.getMessage(), e);
            return null;
        }
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionDetailDTO> getAllSubscriptions() {
        try {
            List<SubscriptionDetailDTO> subscriptionDetailRes = new ArrayList<>();

            for (Subscription subscription : subscriptionDAO.getAllSubscriptions()) {
                SubscriptionDetailDTO subscriptionDetail = new SubscriptionDetailDTO();
                subscriptionDetail.setId(subscription.getId());
                subscriptionDetail.setUserId(subscription.getUser().getId());
                subscriptionDetail.setPlanId(subscription.getPlan().getId());
                subscriptionDetail.setStatus(subscription.getStatus());
                subscriptionDetail.setStartDate(subscription.getStartDate());
                subscriptionDetail.setEndDate(subscription.getEndDate());
                subscriptionDetail.setAutoRenew(subscription.getAutoRenew());
                subscriptionDetail.setCancelledAt(subscription.getCancelledAt());
                subscriptionDetail.setCreatedAt(subscription.getCreatedAt());
                subscriptionDetail.setUpdatedAt(subscription.getUpdatedAt());
                subscriptionDetailRes.add(subscriptionDetail);
            }
            return subscriptionDetailRes;
        } catch (Exception e) {
            log.error("Failed to get all subscriptions: {}", e.getMessage(), e);
            return null;
        }
    }

    @GetMapping("/users")
    public List<UserDetailDTO> getAllUsers() {
        try {
            List<UserDetailDTO> userDetailRes = new ArrayList<>();

            for (User user : userDAO.getAllUsers()) {
                UserDetailDTO userDetail = new UserDetailDTO();
                userDetail.setId(user.getId());
                userDetail.setEmail(user.getEmail());
                userDetail.setGoogleId(user.getGoogleId());
                userDetail.setName(user.getName());
                userDetail.setCreatedAt(user.getCreatedAt());
                userDetail.setUpdatedAt(user.getUpdatedAt());
                userDetail.setDeletedAt(user.getDeletedAt());
                userDetailRes.add(userDetail);
            }
            return userDetailRes;
        } catch (Exception e) {
            log.error("Failed to get all users: {}", e.getMessage(), e);
            return null;
        }
    }
}
