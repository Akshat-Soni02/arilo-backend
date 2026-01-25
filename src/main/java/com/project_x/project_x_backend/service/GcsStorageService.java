package com.project_x.project_x_backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class GcsStorageService {

    @Value("${spring.gcs.bucket.name:your-audio-bucket}")
    private String bucketName;

    @Value("${spring.gcs.project.id:your-project-id}")
    private String projectId;

    private Storage storage;

    @PostConstruct
    public void initializeStorage() {
        // Automatically uses:
        // - GOOGLE_APPLICATION_CREDENTIALS locally
        // - Cloud Run service account in prod
        log.info("Initializing GCS Storage for project: {}", projectId);
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            this.storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();
            log.info("GCS Storage initialized successfully for bucket: {}", bucketName);
        } catch (IOException e) {
            log.error("Failed to initialize GCS storage: {}", e.getMessage(), e);
        }
    }

    public String uploadAudio(byte[] audioData, String noteId, String contentType) throws IOException {
        log.info("Uploading audio for note ID: {}, content type: {}", noteId, contentType);
        if (audioData == null || audioData.length == 0) {
            log.warn("Upload failed: audio data is null or empty for note ID: {}", noteId);
            throw new IllegalArgumentException("Audio data cannot be null or empty");
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getExtensionFromContentType(contentType);
            String fileName = String.format("audio/%s_%s.%s", noteId, timestamp, extension);

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setCacheControl("public, max-age=86400")
                    .build();

            log.debug("Creating blob: {} in bucket: {}", fileName, bucketName);
            Blob blob = storage.create(blobInfo, audioData);
            if (blob == null) {
                log.error("GCS upload returned null blob for note ID: {}", noteId);
                throw new IOException("Upload failed - no blob returned");
            }

            String gcsUrl = String.format("gs://%s/%s", bucketName, fileName);
            log.info("Successfully uploaded audio to: {}", gcsUrl);
            return gcsUrl;

        } catch (Exception e) {
            log.error("Failed to upload audio to GCS for note {}: {}", noteId, e.getMessage(), e);
            throw new IOException("Failed to upload audio to GCS: " + e.getMessage(), e);
        }
    }

    public String getSignedUrl(String gcsUrl, int durationMinutes) throws IOException {
        log.debug("Generating signed URL for: {} with duration: {} mins", gcsUrl, durationMinutes);
        try {
            String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
                log.warn("Invalid GCS URL provided for signing: {}", gcsUrl);
                throw new IllegalArgumentException("Invalid GCS URL format: " + gcsUrl);
            }

            String bucketName = parts[0];
            String blobName = parts[1];
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName).build();

            java.net.URL signedUrl = storage.signUrl(
                    blobInfo,
                    durationMinutes,
                    java.util.concurrent.TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature());

            log.debug("Successfully generated signed URL for: {}", gcsUrl);
            return signedUrl.toString();

        } catch (Exception e) {
            log.error("Failed to generate signed URL for {}: {}", gcsUrl, e.getMessage(), e);
            throw new IOException("Failed to generate signed URL: " + e.getMessage(), e);
        }
    }

    public boolean deleteAudio(String gcsUrl) {
        log.info("Deleting audio from GCS: {}", gcsUrl);
        try {
            String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
                log.warn("Invalid GCS URL for deletion: {}", gcsUrl);
                return false;
            }

            BlobId blobId = BlobId.of(parts[0], parts[1]);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                log.info("Successfully deleted GCS object: {}", gcsUrl);
            } else {
                log.warn("GCS object not found for deletion: {}", gcsUrl);
            }
            return deleted;

        } catch (Exception e) {
            log.error("Failed to delete audio from GCS {}: {}", gcsUrl, e.getMessage(), e);
            return false;
        }
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null)
            return "bin";
        switch (contentType.toLowerCase()) {
            case "audio/mpeg":
                return "mp3";
            case "audio/wav":
            case "audio/x-wav":
                return "wav";
            case "audio/mp4":
            case "audio/x-m4a":
                return "m4a";
            case "audio/flac":
                return "flac";
            default:
                log.warn("Unknown content type '{}', using 'bin' extension", contentType);
                return "bin";
        }
    }
}