package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.repository.NoteRepository;
import com.project_x.project_x_backend.utility.DefaultOutputProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_x.project_x_backend.dao.ExtractedTagDAO;
import com.project_x.project_x_backend.dao.ExtractedTaskDAO;
import com.project_x.project_x_backend.dao.JobDAO;
import com.project_x.project_x_backend.dao.NoteDAO;
import com.project_x.project_x_backend.dao.NoteSentenceDAO;
import com.project_x.project_x_backend.dto.ExtractedTagDTO.CreateTag;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.CreateTask;
import com.project_x.project_x_backend.dto.NoteDTO.NoteFilter;
import com.project_x.project_x_backend.dto.NoteDTO.NoteRes;
import com.project_x.project_x_backend.dto.NoteDTO.NoteUploadResponse;
import com.project_x.project_x_backend.dto.NotebackDTO.CreateNoteback;
import com.project_x.project_x_backend.dto.SttDTO.CreateStt;
import com.project_x.project_x_backend.dto.jobDTO.CreateJob;
import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackReq;
import com.project_x.project_x_backend.dto.jobDTO.JobPollingRes;
import com.project_x.project_x_backend.entity.ExtractedTag;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.PipelineStage;
import com.project_x.project_x_backend.dao.*;
import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.enums.NoteSortField;
import com.project_x.project_x_backend.enums.NoteStatus;
import com.project_x.project_x_backend.enums.NoteType;
import com.project_x.project_x_backend.enums.PipelineName;
import com.project_x.project_x_backend.enums.PipelineStageStatus;
import com.project_x.project_x_backend.enums.PlanTypes;
import com.project_x.project_x_backend.enums.SortOrder;
import com.project_x.project_x_backend.enums.TaskStatus;
import com.project_x.project_x_backend.dto.pipelineDTO.CreatePipeline;
import com.project_x.project_x_backend.dao.AnxietyScoreDAO;
import com.project_x.project_x_backend.dto.AnxietyScoreDTO.CreateAnxietyScore;
import com.project_x.project_x_backend.dto.NoteSentenceDTO.CreateNoteSentence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.LimitExceededException;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.project_x.project_x_backend.entity.NoteTag;
import com.project_x.project_x_backend.entity.Stt;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.Tag;

@Service
@Transactional
@Slf4j
public class NoteService {

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
    private NotebackDAO notebackDAO;

    @Autowired
    private ExtractedTaskDAO extractedTaskDAO;

    @Autowired
    private TagDAO tagDAO;

    @Autowired
    private ExtractedTagDAO extractedTagDAO;

    @Autowired
    private AnxietyScoreDAO anxietyScoreDAO;

    @Autowired
    private JobDAO jobDAO;

    @Autowired
    private NoteSentenceDAO noteSentenceDAO;

    @Autowired
    private SubscriptionDAO subscriptionDAO;

    @Autowired
    private PubSubService pubSubService;

    @Autowired
    private NoteDAO noteDAO;

    @Autowired
    private UsageCycleDAO usageCycleDAO;

    @Autowired
    private UserDailyUsageDAO userDailyUsageDAO;

    // TODO: handle subscription auto-renewal and by default auto-renew for free
    // plan
    // TODO: handle subscription end
    @Transactional
    public NoteUploadResponse uploadNote(UUID userId, byte[] audioBytes, String contentType, boolean isMock)
            throws LimitExceededException, IOException {
        log.info("Starting note upload process for user {}", userId);

        // consume daily usage
        userDailyUsageDAO.consumeDailyUsage(userId);
        // consume cycle usage
        usageCycleDAO.consumeCycleUsage(userId);

        Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(userId);
        if (!subscription.isPresent()) {
            log.warn("Upload blocked: user {} does not have an active subscription", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have an active subscription");
        }

        PlanTypes plan = subscription.get().getPlan().getName();
        List<PipelineName> allowedPipelines = new ArrayList<>();
        if (plan.equals(PlanTypes.FREE)) {
            allowedPipelines.add(PipelineName.STT);

            // TODO: remove this after testing
            // allowedPipelines.add(PipelineName.SMART);
        } else if (plan.equals(PlanTypes.PRO_MONTHLY)) {
            allowedPipelines.add(PipelineName.STT);
            allowedPipelines.add(PipelineName.SMART);
        }

        String normalizedContentType = "";
        if (!isMock) {
            normalizedContentType = normalizeContentType(contentType);
            validateAudioData(audioBytes, normalizedContentType);
        }

        Note note = new Note(userId, null, audioBytes.length, NoteStatus.PROCESSING, NoteType.AUDIO, "");
        note = noteRepository.save(note);

        try {
            String gcsUrl = "";
            if (isMock) {
                gcsUrl = "MockUrl";
            } else {
                gcsUrl = gcsStorageService.uploadAudio(audioBytes, note.getId().toString(),
                        normalizedContentType);
                note.setStorageUrl(gcsUrl);
            }
            note.setStatus(NoteStatus.UPLOADED);
            noteRepository.save(note);

            List<String> existingTagsForEngine = extractedTagDAO.getUniqueExtractedTagsWithNoCanonicalTag(userId)
                    .stream()
                    .map(ExtractedTag::getTag)
                    .collect(Collectors.toList());

            List<Tag> userTags = tagDAO.getAllUserTags(userId);
            existingTagsForEngine.addAll(userTags.stream().map(Tag::getName).collect(Collectors.toList()));

            String existingTagsString = String.join(",", existingTagsForEngine);

            // TODO: update input type and other missing fields
            Job job = startEngineJob(userId, note.getId(), gcsUrl, existingTagsString, "", "", "audio/wav",
                    allowedPipelines, plan);
            return new NoteUploadResponse(note.getId(), note.getNoteType(), job.getId(), job.getStatus());
        } catch (Exception e) {
            log.error("Failed to upload audio for user {}: {}", userId, e.getMessage(), e);
            note.setStatus(NoteStatus.FAILED);
            noteRepository.save(note);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload audio: " + e.getMessage(), e);
        }
    }

    // filter = { tagId = tag id, q = substring present in the stt, createdAfter =
    // only get notes created after this timestamp inclusive, createdBefore = only
    // get notes created before this timestamp inclusive, sort = sort by what (for
    // now only supports createdAt), order = sorting order (asc | desc)}
    public List<NoteRes> getNotes(UUID userId, NoteFilter filter) {
        log.info("Fetching notes for user {} with filter: {}", userId, filter);
        try {
            NoteFilter normFilter = normalize(filter);
            Specification<Note> spec = (root, query, cb) -> {
                query.distinct(true);
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("userId"), userId));
                predicates.add(cb.isNull(root.get("deletedAt")));

                if (normFilter.getTagId() != null) {
                    Join<Note, NoteTag> tagsJoin = root.join("noteTags");
                    predicates.add(cb.equal(tagsJoin.get("tag").get("id"), normFilter.getTagId()));
                }

                if (normFilter.getQ() != null) {
                    Join<Note, Stt> sttJoin = root.join("stt", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(sttJoin.get("stt")), "%" + normFilter.getQ().toLowerCase() + "%"));
                }

                if (normFilter.getCreatedAfter() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), normFilter.getCreatedAfter()));
                }

                if (normFilter.getCreatedBefore() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), normFilter.getCreatedBefore()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Sort.Direction direction = normFilter.getOrder() == SortOrder.ASC ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            // TODO: add other sort fields
            String sortField = normFilter.getSort() == NoteSortField.CREATED_AT ? "createdAt" : "createdAt";

            List<Note> notes = noteRepository.findAll(spec, Sort.by(direction, sortField));
            log.debug("Found {} notes for user {}", notes.size(), userId);
            return notes.stream().map(this::mapToNoteRes).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get notes for user {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to get notes: " + e.getMessage(), e);
        }
    }

    public NoteRes mapToNoteRes(Note note) {
        log.debug("Mapping note {} to response DTO", note.getId());
        NoteRes res = new NoteRes();
        res.setNoteId(note.getId());
        res.setNoteType(note.getNoteType());

        Job job = jobDAO.getJobByNote(note);
        if (job != null) {
            res.setJobId(job.getId());
            res.setStatus(job.getStatus());
        }
        res.setCreatedAt(note.getCreatedAt());
        res.setStt(note.getStt() != null ? note.getStt().getStt() : null);
        res.setNoteback(note.getNoteback() != null ? note.getNoteback().getNoteContent() : null);
        return res;
    }

    public NoteFilter normalize(NoteFilter filter) {
        log.info("Normalizing note filter: {}", filter);
        if (filter.getCreatedAfter() != null && filter.getCreatedBefore() != null
                && filter.getCreatedAfter().isAfter(filter.getCreatedBefore())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "createdAfter cannot be after createdBefore");
        }

        if (filter.getQ() != null && !filter.getQ().isBlank()) {
            filter.setQ(filter.getQ().trim());
        } else {
            filter.setQ(null);
        }

        // if(filter.getCreatedAfter() != null) {
        // filter.setCreatedAfter(filter.getCreatedAfter().toInstant());
        // }
        // if(filter.getCreatedBefore() != null) {
        // filter.setCreatedBefore(filter.getCreatedBefore().toInstant());
        // }
        if (filter.getSort() == null) {
            filter.setSort(NoteSortField.CREATED_AT);
        }
        if (filter.getOrder() == null) {
            filter.setOrder(SortOrder.DESC);
        }
        return filter;
    }

    public JobPollingRes getPollingStatus(UUID userId, UUID jobId) {
        log.info("Getting polling status for job {} for user {}", jobId, userId);
        Job job = jobDAO.getUserJobById(jobId, userId);
        if (job == null) {
            log.warn("Job {} not found for user {}", jobId, userId);
            throw new RuntimeException("Job not found");
        }
        JobPollingRes res = new JobPollingRes();
        res.setStatus(job.getStatus());
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.PROCESSING) {
            sttDAO.getSttByJob(job).ifPresent(stt -> res.setStt(stt.getStt()));
            notebackDAO.getNotebackByJob(job).ifPresent(nb -> res.setNoteback(nb.getNoteContent()));
        }
        if (job.getStatus() == JobStatus.FAILED) {
            res.setErrorMessage(job.getErrorMessage());
        }
        return res;
    }

    public void deleteNote(UUID userId, UUID noteId) {
        log.info("Deleting note {} for user {}", noteId, userId);
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found"));
        if (!note.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete note {} which does not belong to them", userId, noteId);
            throw new RuntimeException("User is not authorized to delete this note");
        }

        noteDAO.deleteNote(userId, noteId);
        log.info("Note {} deleted successfully for user {}", noteId, userId);
    }

    public Job startEngineJob(UUID userId, UUID noteId, String gcsUrl, String existingTags, String location,
            String timestamp,
            String inputType, List<PipelineName> allowedPipelines, PlanTypes planType) {
        log.info("Starting engine job for note {} for user {}", noteId, userId);
        // create job
        // create pipeline stage row for each stage
        // add job to queue

        try {
            Job job = jobDAO.createJob(new CreateJob(userId, noteId));

            for (PipelineName pipelineName : allowedPipelines) {
                pipelineStageDAO.createPipelineStage(new CreatePipeline(job.getId(), pipelineName));
            }

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> pubsubPayload = new HashMap<>();
            pubsubPayload.put("job_id", job.getId());
            pubsubPayload.put("note_id", noteId);
            pubsubPayload.put("user_id", userId);
            pubsubPayload.put("location", location);
            pubsubPayload.put("timestamp", timestamp);
            pubsubPayload.put("input_type", "audio/wav");
            pubsubPayload.put("gcs_audio_url", gcsUrl);
            pubsubPayload.put("existing_tags", existingTags);
            pubsubPayload.put("allowed_pipelines", allowedPipelines);
            pubsubPayload.put("plan_type", planType);

            String jsonPayload = mapper.writeValueAsString(pubsubPayload);
            log.info("JSON payload: {}", jsonPayload);
            pubSubService.publishMessage(jsonPayload);
            log.info("Engine job {} started and message published for note {}", job.getId(), noteId);
            return job;
        } catch (Exception e) {
            log.error("Failed to start engine job for note {}: {}", noteId, e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public boolean handleEngineCallback(EngineCallbackReq engineCallbackReq) {
        if (engineCallbackReq.getStatus().equals(PipelineStageStatus.FAILED)) {
            checkAndMarkJobFailed(engineCallbackReq);
        } else if (engineCallbackReq.getStatus().equals(PipelineStageStatus.COMPLETED)) {
            checkAndMarkJobCompleted(engineCallbackReq);
        }

        log.info(
                "Job id: {}, note id: {}, user id: {}, input type: {}, Pipeline stage: {}, status: {}",
                engineCallbackReq.getJobId(), engineCallbackReq.getNoteId(), engineCallbackReq.getUserId(),
                engineCallbackReq.getInputType(), engineCallbackReq.getPipelineStage(), engineCallbackReq.getStatus());

        if (engineCallbackReq.getPipelineStage().equals(PipelineName.STT)) {
            updateUserStt(engineCallbackReq.getJobId(), engineCallbackReq.getUserId(), engineCallbackReq.getNoteId(),
                    engineCallbackReq.getStatus(), engineCallbackReq.getOutput());
        } else if (engineCallbackReq.getPipelineStage().equals(PipelineName.SMART)) {
            updateUserNote(engineCallbackReq.getJobId(), engineCallbackReq.getUserId(), engineCallbackReq.getNoteId(),
                    engineCallbackReq.getStatus(), engineCallbackReq.getOutput());
        }
        return engineCallbackReq.getStatus().equals(PipelineStageStatus.COMPLETED);
    }

    // if all stages failed or executed but last one failed then mark job as failed
    public void checkAndMarkJobFailed(EngineCallbackReq engineCallbackReq) {
        log.info("Checking and marking job {} as failed if all stages failed", engineCallbackReq.getJobId());
        List<PipelineStage> pipelineStages = pipelineStageDAO.getPipelineStagesByJobId(engineCallbackReq.getJobId());
        log.debug("Pipeline stages for job {}: {}", engineCallbackReq.getJobId(), pipelineStages);
        log.debug("Current stage status from callback: {}", engineCallbackReq.getStatus());

        boolean allStagesFailed = true;
        boolean allStagesExecuted = true;

        for (PipelineStage stage : pipelineStages) {
            if (!stage.getStatus().equals(PipelineStageStatus.FAILED)) {
                allStagesFailed = false;
            }
            if (stage.getStatus().equals(PipelineStageStatus.IN_PROGRESS)
                    || stage.getStatus().equals(PipelineStageStatus.PENDING)) {
                allStagesExecuted = false;
            }
        }

        if (allStagesFailed || allStagesExecuted) {
            jobDAO.updateJobStatus(engineCallbackReq.getJobId(), JobStatus.FAILED);
            userDailyUsageDAO.reduceDailyUsage(engineCallbackReq.getUserId());
            usageCycleDAO.reduceCycleUsage(engineCallbackReq.getUserId());
            log.warn("Job {} marked as FAILED because all its stages failed.", engineCallbackReq.getJobId());
        }
    }

    public void checkAndMarkJobCompleted(EngineCallbackReq engineCallbackReq) {
        log.info("Checking and marking job {} as completed if all stages completed", engineCallbackReq.getJobId());
        List<PipelineStage> pipelineStages = pipelineStageDAO.getPipelineStagesByJobId(engineCallbackReq.getJobId());
        log.debug("Current stage status from callback: {}", engineCallbackReq.getStatus());

        boolean allStagesCompleted = true;
        boolean allStagesExecuted = true;

        for (PipelineStage stage : pipelineStages) {
            if (!stage.getStatus().equals(PipelineStageStatus.COMPLETED)) {
                allStagesCompleted = false;
            }

            if (stage.getStatus().equals(PipelineStageStatus.IN_PROGRESS)
                    || stage.getStatus().equals(PipelineStageStatus.PENDING)) {
                allStagesExecuted = false;
            }
        }

        if (allStagesCompleted) {
            log.info("All stages completed for job: {}", engineCallbackReq.getJobId());
            jobDAO.updateJobStatus(engineCallbackReq.getJobId(), JobStatus.COMPLETED);
        } else if (allStagesExecuted) {
            log.info("All stages executed for job: {}, but not all completed", engineCallbackReq.getJobId());
            userDailyUsageDAO.reduceDailyUsage(engineCallbackReq.getUserId());
            usageCycleDAO.reduceCycleUsage(engineCallbackReq.getUserId());
            jobDAO.updateJobStatus(engineCallbackReq.getJobId(), JobStatus.FAILED);
        }
    }

    public void updateUserStt(UUID jobId, UUID userId, UUID noteId, PipelineStageStatus status, JsonNode sttResult) {
        if (status.equals(PipelineStageStatus.FAILED)) {
            sttResult = defaultOutputProvider.getFallbackStt();
        }

        // save stt
        sttDAO.createStt(
                new CreateStt(jobId, userId, sttResult.get("stt_response").get("language").asText(),
                        sttResult.get("stt_response").get("stt").asText()));

        // save extracted tags
        JsonNode tagsNode = sttResult.get("stt_response").get("tags");
        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode tagNode : tagsNode) {
                extractedTagDAO.addExtractedTag(new CreateTag(jobId, userId, tagNode.asText()));
            }
        }

        // save extracted tasks
        JsonNode tasksNode = sttResult.get("stt_response").get("tasks");
        if (tasksNode != null && tasksNode.isArray()) {
            for (JsonNode taskNode : tasksNode) {
                extractedTaskDAO
                        .createExtractedTask(new CreateTask(jobId, userId, taskNode.asText(), TaskStatus.IN_PROGRESS));
            }
        }

        // save anxiety score
        if (sttResult.get("stt_response").has("anxiety_score")) {
            anxietyScoreDAO
                    .createAnxietyScore(new CreateAnxietyScore(jobId, userId,
                            sttResult.get("stt_response").get("anxiety_score").asInt()));
        }

        // save note sentences
        if (sttResult.has("sentences_with_embeddings")) {
            addNoteSentences(noteId, userId, sttResult.get("sentences_with_embeddings"));
        }
    }

    public void updateUserNote(UUID jobId, UUID userId, UUID noteId, PipelineStageStatus status, JsonNode noteResult) {
        if (status.equals(PipelineStageStatus.FAILED)) {
            noteResult = defaultOutputProvider.getFallbackNoteback();
        }

        notebackDAO.createNoteback(new CreateNoteback(jobId, userId,
                noteResult.get("noteback_response").get("noteback").asText(), noteResult));

        // save note sentences
        if (noteResult.has("sentences_with_embeddings")) {
            addNoteSentences(noteId, userId, noteResult.get("sentences_with_embeddings"));
        }
    }

    public void addNoteSentences(UUID noteId, UUID userId, JsonNode sentencesNode) {
        if (sentencesNode != null && sentencesNode.isArray()) {
            List<CreateNoteSentence> sentencesList = new ArrayList<>();
            for (JsonNode sentenceNode : sentencesNode) {
                // Parse embedding array
                List<Float> embeddingList = new ArrayList<>();
                JsonNode embeddingNode = sentenceNode.get("embedding");
                if (embeddingNode != null && embeddingNode.isArray()) {
                    for (JsonNode val : embeddingNode) {
                        embeddingList.add((float) val.asDouble());
                    }
                }

                sentencesList.add(new CreateNoteSentence(
                        sentenceNode.get("sentence_index").asInt(),
                        sentenceNode.get("sentence_text").asText(),
                        (float) sentenceNode.get("importance_score").asDouble(),
                        embeddingList));
            }

            log.info("Saving {} sentences for note id: {}", sentencesList.size(), noteId);

            // Batch save
            if (!sentencesList.isEmpty()) {
                noteSentenceDAO.createNoteSentences(noteId, userId, sentencesList);
            }
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
            case "audio/m4a":
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
                contentType.equals("audio/m4a") ||
                contentType.equals("audio/flac");
    }
}
