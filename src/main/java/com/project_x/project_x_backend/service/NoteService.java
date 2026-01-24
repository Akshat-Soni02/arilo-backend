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
import com.project_x.project_x_backend.dao.PipelineStageDAO;
import com.project_x.project_x_backend.dao.NotebackDAO;
import com.project_x.project_x_backend.dao.SttDAO;
import com.project_x.project_x_backend.dao.SubscriptionDAO;
import com.project_x.project_x_backend.dao.TagDAO;
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

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

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

    public NoteUploadResponse uploadNote(UUID userId, byte[] audioBytes, String contentType, boolean isMock)
            throws IOException {

        Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(userId);
        if (!subscription.isPresent()) {
            throw new BadRequestException("User does not have an active subscription");
        }

        PlanTypes plan = subscription.get().getPlan().getName();
        List<PipelineName> allowedPipelines = new ArrayList<>();
        if (plan.equals(PlanTypes.FREE)) {
            allowedPipelines.add(PipelineName.STT);

            // TODO: remove this after testing
            allowedPipelines.add(PipelineName.SMART);
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

            Job job = startEngineJob(userId, note.getId(), gcsUrl, existingTagsString, "", "", "audio/wav",
                    allowedPipelines);
            return new NoteUploadResponse(note.getId(), note.getNoteType(), job.getId(), job.getStatus());
        } catch (Exception e) {
            note.setStatus(NoteStatus.FAILED);
            noteRepository.save(note);
            throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
        }
    }

    public List<NoteRes> getNotes(UUID userId, NoteFilter filter) {
        // filter = { tagId = tag id, q = substring present in the stt, createdAfter =
        // only get notes created after this timestamp inclusive, createdBefore = only
        // get notes created before this timestamp inclusive, sort = sort by what (for
        // now only supports createdAt), order = sorting order (asc | desc)}
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

            String sortField = normFilter.getSort().equals(NoteSortField.CREATED_AT) ? "createdAt" : "createdAt";
            Sort.Direction direction = normFilter.getOrder() == SortOrder.ASC ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            List<Note> notes = noteRepository.findAll(spec, Sort.by(direction, sortField));

            return notes.stream().map(this::mapToNoteRes).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get notes: " + e.getMessage(), e);
        }

    }

    public NoteRes mapToNoteRes(Note note) {
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

    public NoteFilter normalize(NoteFilter filter) throws Exception {
        if (filter.getCreatedAfter() != null && filter.getCreatedBefore() != null
                && filter.getCreatedAfter().isAfter(filter.getCreatedBefore())) {
            throw new BadRequestException("createdAfter cannot be after createdBefore");
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
        Job job = jobDAO.getUserJobById(jobId, userId);
        if (job == null) {
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
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("User is not authorized to delete this note");
        }

        noteDAO.deleteNote(userId, noteId);
    }

    public Job startEngineJob(UUID userId, UUID noteId, String gcsUrl, String existingTags, String location,
            String timestamp,
            String inputType, List<PipelineName> allowedPipelines) {
        // create job
        // create pipeline stage row for each stage
        // add job to queue

        try {
            Job job = jobDAO.createJob(new CreateJob(userId, noteId));

            for (PipelineName pipelineName : allowedPipelines) {
                pipelineStageDAO.createPipelineStage(new CreatePipeline(job.getId(), pipelineName));
            }

            ObjectMapper mapper = new ObjectMapper();

            // TODO: pass tags
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

            String jsonPayload = mapper.writeValueAsString(pubsubPayload);
            logger.info("JSON payload: {}", jsonPayload);
            pubSubService.publishMessage(jsonPayload);
            return job;
        } catch (Exception e) {
            logger.error("Failed to start engine job for note {}: {}", noteId, e.getMessage());
            return null;
        }
    }

    // TODO: handle current note sentences saving, which will come with smart
    // callback
    public boolean handleEngineCallback(EngineCallbackReq engineCallbackReq) {
        logger.info(engineCallbackReq.toString());
        if (engineCallbackReq.getStatus().equals(PipelineStageStatus.FAILED)) {
            checkAndMarkJobFailed(engineCallbackReq);
        } else if (engineCallbackReq.getStatus().equals(PipelineStageStatus.COMPLETED)) {
            checkAndMarkJobCompleted(engineCallbackReq);
        }

        logger.info(
                "Job id: {}, note id: {}, user id: {}, input type: {}, Pipeline stage: {}, status: {}",
                engineCallbackReq.getJobId(), engineCallbackReq.getNoteId(), engineCallbackReq.getUserId(),
                engineCallbackReq.getInputType(), engineCallbackReq.getPipelineStage(), engineCallbackReq.getStatus());

        if (engineCallbackReq.getPipelineStage().equals(PipelineName.STT)) {
            updateUserStt(engineCallbackReq.getJobId(), engineCallbackReq.getUserId(), engineCallbackReq.getStatus(),
                    engineCallbackReq.getOutput());
        } else if (engineCallbackReq.getPipelineStage().equals(PipelineName.SMART)) {
            updateUserNote(engineCallbackReq.getJobId(), engineCallbackReq.getUserId(), engineCallbackReq.getNoteId(),
                    engineCallbackReq.getStatus(),
                    engineCallbackReq.getOutput());
        }
        return engineCallbackReq.getStatus().equals(PipelineStageStatus.COMPLETED);
    }

    public void checkAndMarkJobFailed(EngineCallbackReq engineCallbackReq) {
        List<PipelineStage> pipelineStages = pipelineStageDAO.getPipelineStagesByJobId(engineCallbackReq.getJobId());
        System.out.println("Pipeline stages: " + pipelineStages.toString());
        System.out.println("Pipeline stage status: " + engineCallbackReq.getStatus());

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
        System.out.println("Pipeline stage status: " + engineCallbackReq.getStatus());

        boolean allStagesCompleted = true;
        for (PipelineStage stage : pipelineStages) {
            if (!stage.getStatus().equals(PipelineStageStatus.COMPLETED)) {
                allStagesCompleted = false;
                break;
            }
        }

        if (allStagesCompleted) {
            System.out.println("All stages completed for job: " + engineCallbackReq.getJobId());
            jobDAO.updateJobStatus(engineCallbackReq.getJobId(), JobStatus.COMPLETED);
        }
    }

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
                extractedTagDAO.addExtractedTag(new CreateTag(jobId, userId, tagNode.asText()));
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

    public void updateUserNote(UUID jobId, UUID userId, UUID note_id, PipelineStageStatus status, JsonNode noteResult) {
        if (status.equals(PipelineStageStatus.FAILED)) {
            noteResult = defaultOutputProvider.getFallbackNoteback();
        }

        notebackDAO.createNoteback(new CreateNoteback(jobId, userId,
                noteResult.get("noteback_response").get("noteback").asText(), noteResult));

        // save note sentences
        JsonNode sentencesNode = noteResult.get("sentences_with_embeddings");
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

            // Batch save
            if (!sentencesList.isEmpty()) {
                noteSentenceDAO.createNoteSentences(note_id, userId, sentencesList);
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
