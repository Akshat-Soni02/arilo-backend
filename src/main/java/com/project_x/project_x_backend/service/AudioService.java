package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.entity.AudioStore;
import com.project_x.project_x_backend.repository.AudioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_x.project_x_backend.dao.JobDAO;
import com.project_x.project_x_backend.dto.jobDTO.CreateJob;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.PipelineStage;
import com.project_x.project_x_backend.dao.PipelineStageDAO;
import com.project_x.project_x_backend.enums.PipelineName;
import com.project_x.project_x_backend.dto.pipelineDTO.CreatePipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AudioService {

    private static final Logger logger = LoggerFactory.getLogger(AudioService.class);

    @Autowired
    private AudioRepository audioRepository;

    @Autowired
    private GcsStorageService gcsStorageService;

    @Autowired
    private PipelineStageDAO pipelineStageDAO;

    @Autowired
    private JobDAO jobDAO;

    public AudioStore uploadAudio(UUID userId, byte[] audioBytes, String contentType) throws IOException {
        String normalizedContentType = normalizeContentType(contentType);
        validateAudioData(audioBytes, normalizedContentType);

        AudioStore audioStore = new AudioStore(userId, null, audioBytes.length, AudioStore.Status.PROCESSING);
        audioStore = audioRepository.save(audioStore);

        try {
            String gcsUrl = gcsStorageService.uploadAudio(audioBytes, audioStore.getId().toString(),
                    normalizedContentType);
            audioStore.setStorageUrl(gcsUrl);
            audioStore.setStatus(AudioStore.Status.UPLOADED);
            return audioRepository.save(audioStore);
        } catch (Exception e) {
            audioStore.setStatus(AudioStore.Status.FAILED);
            audioRepository.save(audioStore);
            throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
        }
    }

    public void startEngineJob(UUID userId, UUID noteId, UUID audioId, String gcsUrl, String location, String timestamp,
            String inputType) {
        // create job
        // create pipeline stage row for each stage
        // add job to queue

        try {
            Job job = jobDAO.createJob(new CreateJob(audioId, userId));
            PipelineStage stage1 = pipelineStageDAO
                    .createPipelineStage(new CreatePipeline(job.getId(), PipelineName.STT));
            PipelineStage stage2 = pipelineStageDAO
                    .createPipelineStage(new CreatePipeline(job.getId(), PipelineName.SMART));

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> pubsubPayload = new HashMap<>();
            pubsubPayload.put("job_id", job.getId());
            pubsubPayload.put("note_id", noteId);
            pubsubPayload.put("user_id", userId);
            pubsubPayload.put("location", location);
            pubsubPayload.put("timestamp", timestamp);
            pubsubPayload.put("input_type", "audio/wav");

            String jsonPayload = mapper.writeValueAsString(pubsubPayload);
            // enqueue job here
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }

        switch (contentType.toLowerCase()) {
            case "audio/wave":
                return "audio/wav";
            default:
                return contentType;
        }
    }

    private void validateAudioData(byte[] audioBytes, String contentType) throws IOException {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IOException("Audio data is empty");
        }

        if (contentType == null || !isValidAudioType(contentType)) {
            throw new IllegalArgumentException("Unsupported audio format: " + contentType);
        }

        if (audioBytes.length > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Audio data size cannot exceed 50MB");
        }

        if (!isValidAudioHeader(audioBytes, contentType)) {
            throw new IllegalArgumentException("Invalid audio data format");
        }
    }

    private boolean isValidAudioHeader(byte[] audioBytes, String contentType) {
        if (audioBytes.length < 4) {
            return false;
        }

        switch (contentType) {
            case "audio/mpeg":
                return (audioBytes[0] == 'I' && audioBytes[1] == 'D' && audioBytes[2] == '3') ||
                        (audioBytes[0] == (byte) 0xFF && (audioBytes[1] & 0xE0) == 0xE0);

            case "audio/wav":
            case "audio/x-wav":
                return audioBytes[0] == 'R' && audioBytes[1] == 'I' &&
                        audioBytes[2] == 'F' && audioBytes[3] == 'F';

            case "audio/flac":
                return audioBytes[0] == 'f' && audioBytes[1] == 'L' &&
                        audioBytes[2] == 'a' && audioBytes[3] == 'C';

            case "audio/mp4":
            case "audio/x-m4a":
                return audioBytes.length > 8 &&
                        audioBytes[4] == 'f' && audioBytes[5] == 't' &&
                        audioBytes[6] == 'y' && audioBytes[7] == 'p';

            default:
                return true;
        }
    }

    private boolean isValidAudioType(String contentType) {
        return contentType.equals("audio/mpeg") ||
                contentType.equals("audio/wav") ||
                contentType.equals("audio/x-wav") ||
                contentType.equals("audio/mp4") ||
                contentType.equals("audio/x-m4a") ||
                contentType.equals("audio/flac");
    }
}
