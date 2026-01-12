package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.AnxietyScoreDTO.CreateAnxietyScore;
import com.project_x.project_x_backend.entity.AnxietyScore;
import com.project_x.project_x_backend.repository.NoteRepository;
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

    @Autowired
    private NoteRepository noteRepository;

    public void createAnxietyScore(CreateAnxietyScore createAnxietyScore) {
        AnxietyScore anxietyScore = new AnxietyScore();
        anxietyScore.setUser(userRepository.getReferenceById(createAnxietyScore.getUserId()));
        com.project_x.project_x_backend.entity.Job job = jobRepository.getReferenceById(createAnxietyScore.getJobId());
        anxietyScore.setJob(job);
        anxietyScore.setNote(noteRepository.getReferenceById(job.getNoteId()));
        anxietyScore.setAnxietyScore(createAnxietyScore.getAnxietyScore());
        anxietyScore.setCreatedAt(Instant.now());
        anxietyScoreRepository.save(anxietyScore);
    }

    public void deleteExtractedAnxietyScores(UUID noteId) {
        List<AnxietyScore> anxietyScores = anxietyScoreRepository.findAllByNoteId(noteId);
        for (AnxietyScore anxietyScore : anxietyScores) {
            anxietyScoreRepository.delete(anxietyScore);
        }
    }
}
