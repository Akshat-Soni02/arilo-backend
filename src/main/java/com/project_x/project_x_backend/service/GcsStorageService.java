package com.project_x.project_x_backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

@Service
public class GcsStorageService {

    private static final Logger logger = LoggerFactory.getLogger(GcsStorageService.class);

    @Value("${spring.gcs.bucket.name:your-audio-bucket}")
    private String bucketName;

    @Value("${spring.gcs.project.id:your-project-id}")
    private String projectId;

    private Storage storage;

    @PostConstruct
    public void initializeStorage() throws IOException {

        // Automatically uses:
        // - GOOGLE_APPLICATION_CREDENTIALS locally
        // - Cloud Run service account in prod
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();

        logger.info("GCS Storage initialized successfully");
    }

    public String uploadAudio(byte[] audioData, String noteId, String contentType) throws IOException {
        if (audioData == null || audioData.length == 0) {
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

            Blob blob = storage.create(blobInfo, audioData);
            if (blob == null) {
                throw new IOException("Upload failed - no blob returned");
            }

            return String.format("gs://%s/%s", bucketName, fileName);

        } catch (Exception e) {
            logger.error("Failed to upload audio to GCS: {}", e.getMessage());
            throw new IOException("Failed to upload audio to GCS: " + e.getMessage(), e);
        }
    }

    public String getSignedUrl(String gcsUrl, int durationMinutes) throws IOException {
        try {
            String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
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

            return signedUrl.toString();

        } catch (Exception e) {
            logger.error("Failed to generate signed URL: {}", e.getMessage());
            throw new IOException("Failed to generate signed URL: " + e.getMessage(), e);
        }
    }

    public boolean deleteAudio(String gcsUrl) {
        try {
            String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
                return false;
            }

            BlobId blobId = BlobId.of(parts[0], parts[1]);
            return storage.delete(blobId);

        } catch (Exception e) {
            logger.error("Failed to delete audio: {}", e.getMessage());
            return false;
        }
    }

    private String getExtensionFromContentType(String contentType) {
        switch (contentType) {
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
                logger.warn("Unknown content type '{}', using 'bin' extension", contentType);
                return "bin";
        }
    }
}