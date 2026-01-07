package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.repository.NoteRepository;
import com.project_x.project_x_backend.utility.DefaultOutputProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_x.project_x_backend.dao.ExtractedTagDAO;
import com.project_x.project_x_backend.dao.ExtractedTaskDAO;
import com.project_x.project_x_backend.dao.JobDAO;
import com.project_x.project_x_backend.dto.ExtractedTagDTO.CreateTag;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.CreateTask;
import com.project_x.project_x_backend.dto.SmartNoteDTO.CreateSmartNote;
import com.project_x.project_x_backend.dto.SttDTO.CreateStt;
import com.project_x.project_x_backend.dto.jobDTO.CreateJob;
import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackReq;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.PipelineStage;
import com.project_x.project_x_backend.dao.PipelineStageDAO;
import com.project_x.project_x_backend.dao.SmartNoteDAO;
import com.project_x.project_x_backend.dao.SttDAO;
import com.project_x.project_x_backend.dao.TagDAO;
import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.enums.NoteStatus;
import com.project_x.project_x_backend.enums.PipelineName;
import com.project_x.project_x_backend.enums.PipelineStageStatus;
import com.project_x.project_x_backend.enums.TaskStatus;
import com.project_x.project_x_backend.dto.pipelineDTO.CreatePipeline;
import com.project_x.project_x_backend.dao.AnxietyScoreDAO;
import com.project_x.project_x_backend.dto.AnxietyScoreDTO.CreateAnxietyScore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AudioService {

    private static final Logger logger = LoggerFactory.getLogger(AudioService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private GcsStorageService gcsStorageService;

    @Autowired
    private PipelineStageDAO pipelineStageDAO;

    @Autowired
    private DefaultOutputProvider defaultOutputProvider;

    @Autowired
    private SttDAO sttDAO;

    @Autowired
    private SmartNoteDAO smartNoteDAO;

    @Autowired
    private TagDAO tagDAO;

    @Autowired
    private ExtractedTaskDAO extractedTaskDAO;

    @Autowired
    private ExtractedTagDAO extractedTagDAO;

    @Autowired
    private AnxietyScoreDAO anxietyScoreDAO;

    @Autowired
    private JobDAO jobDAO;

    @Autowired
    private PubSubService pubSubService;

    public Note uploadAudio(UUID userId, byte[] audioBytes, String contentType) throws IOException {
        String normalizedContentType = normalizeContentType(contentType);
        validateAudioData(audioBytes, normalizedContentType);

        // Default values for noteType and textContent as they are not provided in the
        // current upload flow
        Note note = new Note(userId, null, audioBytes.length, NoteStatus.PROCESSING, "AUDIO", "");
        note = noteRepository.save(note);

        try {
            String gcsUrl = gcsStorageService.uploadAudio(audioBytes, note.getId().toString(),
                    normalizedContentType);
            note.setStorageUrl(gcsUrl);
            note.setStatus(NoteStatus.UPLOADED);
            Note savedNote = noteRepository.save(note);
            startEngineJob(userId, note.getId(), gcsUrl, "", "", "audio/wav");
            return savedNote;
        } catch (Exception e) {
            note.setStatus(NoteStatus.FAILED);
            noteRepository.save(note);
            throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
        }
    }

    public void startEngineJob(UUID userId, UUID noteId, String gcsUrl, String location, String timestamp,
            String inputType) {
        // create job
        // create pipeline stage row for each stage
        // add job to queue

        try {
            Job job = jobDAO.createJob(new CreateJob(userId, noteId));
            pipelineStageDAO.createPipelineStage(new CreatePipeline(job.getId(), PipelineName.STT));
            pipelineStageDAO.createPipelineStage(new CreatePipeline(job.getId(), PipelineName.SMART));

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> pubsubPayload = new HashMap<>();
            pubsubPayload.put("job_id", job.getId());
            pubsubPayload.put("note_id", noteId);
            pubsubPayload.put("user_id", userId);
            pubsubPayload.put("location", location);
            pubsubPayload.put("timestamp", timestamp);
            pubsubPayload.put("input_type", "audio/wav");

            String jsonPayload = mapper.writeValueAsString(pubsubPayload);
            pubSubService.publishMessage(jsonPayload);
        } catch (Exception e) {
            logger.error("Failed to start engine job for note {}: {}", noteId, e.getMessage());
        }
    }

    public boolean handleEngineCallback(EngineCallbackReq engineCallbackReq) {
        if (engineCallbackReq.getStatus().equals(PipelineStageStatus.FAILED)) {
            checkAndMarkJobFailed(engineCallbackReq);
        } else if (engineCallbackReq.getStatus().equals(PipelineStageStatus.COMPLETED)) {
            checkAndMarkJobCompleted(engineCallbackReq);
        }

        updateUserStt(engineCallbackReq.getJobId(), engineCallbackReq.getUserId(), engineCallbackReq.getStatus(),
                engineCallbackReq.getOutput());
        updateUserNote(engineCallbackReq.getJobId(), engineCallbackReq.getUserId(), engineCallbackReq.getStatus(),
                engineCallbackReq.getOutput());
        return engineCallbackReq.getStatus().equals(PipelineStageStatus.COMPLETED);
    }

    public void checkAndMarkJobFailed(EngineCallbackReq engineCallbackReq) {
        List<PipelineStage> pipelineStages = pipelineStageDAO.getPipelineStagesByJobId(engineCallbackReq.getJobId());

        boolean allStagesFailed = true;
        for (PipelineStage stage : pipelineStages) {
            if (!stage.getStatus().equals(PipelineStageStatus.FAILED)) {
                allStagesFailed = false;
                break;
            }
        }

        if (allStagesFailed) {
            jobDAO.updateJobStatus(engineCallbackReq.getJobId(), JobStatus.FAILED);
        }
    }

    public void checkAndMarkJobCompleted(EngineCallbackReq engineCallbackReq) {
        List<PipelineStage> pipelineStages = pipelineStageDAO.getPipelineStagesByJobId(engineCallbackReq.getJobId());

        boolean allStagesCompleted = true;
        for (PipelineStage stage : pipelineStages) {
            if (!stage.getStatus().equals(PipelineStageStatus.FAILED)) {
                allStagesCompleted = false;
                break;
            }
        }

        if (allStagesCompleted) {
            jobDAO.updateJobStatus(engineCallbackReq.getJobId(), JobStatus.COMPLETED);
        }
    }

    // @TODO add tags to user tags if tag_count >= 3
    public void updateUserStt(UUID jobId, UUID userId, PipelineStageStatus status, JsonNode sttResult) {
        if (status.equals(PipelineStageStatus.FAILED)) {
            sttResult = defaultOutputProvider.getFallbackStt();
        }

        // save stt
        sttDAO.createStt(
                new CreateStt(jobId, userId, sttResult.get("language").asText(), sttResult.get("stt").asText()));

        // save extracted tags
        JsonNode tagsNode = sttResult.get("tags");
        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode tagNode : tagsNode) {
                extractedTagDAO.addExtractedTag(new CreateTag(jobId, userId, tagNode.asText(), 1));
            }
        }

        // save extracted tasks
        JsonNode tasksNode = sttResult.get("tasks");
        if (tasksNode != null && tasksNode.isArray()) {
            for (JsonNode taskNode : tasksNode) {
                extractedTaskDAO
                        .createExtractedTask(new CreateTask(jobId, userId, taskNode.asText(), TaskStatus.IN_PROGRESS));
            }
        }

        // save anxiety score
        if (sttResult.has("anxiety_score")) {
            anxietyScoreDAO
                    .createAnxietyScore(new CreateAnxietyScore(jobId, userId, sttResult.get("anxiety_score").asInt()));
        }
    }

    public void updateUserNote(UUID jobId, UUID userId, PipelineStageStatus status, JsonNode noteResult) {
        if (status.equals(PipelineStageStatus.FAILED)) {
            noteResult = defaultOutputProvider.getFallbackSmart();
        }

        smartNoteDAO.createSmartNote(new CreateSmartNote(jobId, userId, noteResult.get("note").asText(), noteResult));
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
