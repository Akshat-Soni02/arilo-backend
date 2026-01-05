package com.project_x.project_x_backend.dao;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.AnxietyScoreDTO.CreateAnxietyScore;
import com.project_x.project_x_backend.entity.AnxietyScore;
import com.project_x.project_x_backend.repository.AnxietyScoreRepository;
import com.project_x.project_x_backend.repository.JobRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class AnxietyScoreDAO {

    @Autowired
    private AnxietyScoreRepository anxietyScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    public void createAnxietyScore(CreateAnxietyScore createAnxietyScore) {
        AnxietyScore anxietyScore = new AnxietyScore();
        anxietyScore.setUser(userRepository.getReferenceById(createAnxietyScore.getUserId()));
        anxietyScore.setJob(jobRepository.getReferenceById(createAnxietyScore.getJobId()));
        anxietyScore.setAnxietyScore(createAnxietyScore.getAnxietyScore());
        anxietyScore.setCreatedAt(Instant.now());
        anxietyScoreRepository.save(anxietyScore);
    }
}
