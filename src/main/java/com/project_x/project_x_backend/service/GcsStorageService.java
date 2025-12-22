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

    @Value("${spring.gcs.credentials.path:#{null}}")
    private String credentialsPath;

    private Storage storage;

    @PostConstruct
    public void initializeStorage() throws IOException {
        logger.info("=== GCS Storage Initialization Started ===");
        logger.info("Project ID: '{}'", projectId);
        logger.info("Bucket Name: '{}'", bucketName);
        logger.info("Credentials Path: '{}'", credentialsPath);
        logger.info("Credentials Path is null: {}", credentialsPath == null);
        logger.info("Credentials Path is empty: {}", credentialsPath != null ? credentialsPath.trim().isEmpty() : "null");
        
        GoogleCredentials credentials;

        if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
            File credFile = new File(credentialsPath);
            logger.info("Using credentials file: {}", credFile.getAbsolutePath());
            logger.info("Credentials file exists: {}", credFile.exists());

            if (!credFile.exists()) {
                logger.error("GCS credentials file not found at: {}", credentialsPath);
                throw new IOException("GCS credentials file not found at: " + credentialsPath);
            }

            try (FileInputStream fis = new FileInputStream(credFile)) {
                credentials = GoogleCredentials.fromStream(fis);
                logger.info("GCS Storage initialized with provided credentials.");
            } catch (Exception e) {
                logger.error("Failed to load credentials from file: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            logger.info("Credentials path is null or empty - Using Application Default Credentials");
            try {
                credentials = GoogleCredentials.getApplicationDefault();
                logger.info("Application Default Credentials loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load Application Default Credentials: {}", e.getMessage(), e);
                throw e;
            }
        }

        try {
            this.storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();
            logger.info("=== GCS Storage Client Initialized Successfully ===");
            
            // Test connection by listing buckets (requires minimal permissions)
            testConnection();
            
        } catch (Exception e) {
            logger.error("Failed to initialize GCS Storage client: {}", e.getMessage(), e);
            throw new IOException("Failed to initialize GCS Storage client", e);
        }
    }

    private void testConnection() {
        try {
            logger.info("Testing GCS connection...");
            
            // Try to check if bucket exists
            Bucket bucket = storage.get(bucketName);
            if (bucket != null) {
                logger.info("Successfully connected to GCS bucket: {}", bucketName);
                logger.info("Bucket location: {}", bucket.getLocation());
                logger.info("Bucket storage class: {}", bucket.getStorageClass());
            } else {
                logger.error("Bucket '{}' does not exist or is not accessible", bucketName);
            }
            
        } catch (Exception e) {
            logger.error("GCS connection test failed: {}", e.getMessage(), e);
        }
    }

    public String uploadAudio(byte[] audioData, String audioId, String contentType) throws IOException {
        logger.info("=== Starting GCS Audio Upload ===");
        logger.info("Audio ID: {}", audioId);
        logger.info("Content Type: {}", contentType);
        logger.info("Audio data size: {} bytes", audioData != null ? audioData.length : 0);
        logger.info("Target bucket: {}", bucketName);
        
        if (audioData == null || audioData.length == 0) {
            logger.error("Audio data is null or empty");
            throw new IllegalArgumentException("Audio data cannot be null or empty");
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getExtensionFromContentType(contentType);
            String fileName = String.format("audio/%s_%s.%s", audioId, timestamp, extension);
            
            logger.info("Generated filename: {}", fileName);
            logger.info("File extension: {}", extension);

            BlobId blobId = BlobId.of(bucketName, fileName);
            logger.info("Created BlobId - Bucket: {}, Name: {}", blobId.getBucket(), blobId.getName());
            
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setCacheControl("public, max-age=86400")
                    .build();
            logger.info("Created BlobInfo with content type: {}", blobInfo.getContentType());

            logger.info("Attempting to upload to GCS...");
            Blob blob = storage.create(blobInfo, audioData);
            
            if (blob != null) {
                logger.info("=== GCS Upload Successful ===");
                logger.info("Blob name: {}", blob.getName());
                logger.info("Blob size: {} bytes", blob.getSize());
                logger.info("Blob generation: {}", blob.getGeneration());
                logger.info("Blob media link: {}", blob.getMediaLink());
                
                String gcsUrl = String.format("gs://%s/%s", bucketName, fileName);
                logger.info("Generated GCS URL: {}", gcsUrl);
                return gcsUrl;
            } else {
                logger.error("Upload failed - blob is null");
                throw new IOException("Upload failed - no blob returned");
            }

        } catch (Exception e) {
            logger.error("=== GCS Upload Failed ===");
            logger.error("Error type: {}", e.getClass().getSimpleName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Stack trace: ", e);
            
            // Check if it's an authentication issue
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                logger.error("Authentication failed - check service account credentials");
            } else if (e.getMessage() != null && e.getMessage().contains("403")) {
                logger.error("Permission denied - check service account permissions");
            } else if (e.getMessage() != null && e.getMessage().contains("redirect")) {
                logger.error("Detected redirect - likely authentication configuration issue");
            }
            
            throw new IOException("Failed to upload audio to GCS: " + e.getMessage(), e);
        }
    }

    public String getSignedUrl(String gcsUrl, int durationMinutes) throws IOException {
        logger.info("=== Generating Signed URL ===");
        logger.info("GCS URL: {}", gcsUrl);
        logger.info("Duration: {} minutes", durationMinutes);
        
        try {
            String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
                logger.error("Invalid GCS URL format: {}", gcsUrl);
                throw new IllegalArgumentException("Invalid GCS URL format: " + gcsUrl);
            }

            String bucketName = parts[0];
            String blobName = parts[1];
            
            logger.info("Parsed - Bucket: {}, Blob: {}", bucketName, blobName);

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName).build();

            logger.info("Generating signed URL...");
            java.net.URL signedUrl = storage.signUrl(
                    blobInfo,
                    durationMinutes,
                    java.util.concurrent.TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature());

            logger.info("Signed URL generated successfully: {}", signedUrl.toString());
            return signedUrl.toString();

        } catch (Exception e) {
            logger.error("Failed to generate signed URL: {}", e.getMessage(), e);
            throw new IOException("Failed to generate signed URL: " + e.getMessage(), e);
        }
    }

    public boolean deleteAudio(String gcsUrl) {
        try {
            String[] parts = gcsUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
                return false;
            }

            String bucketName = parts[0];
            String blobName = parts[1];

            BlobId blobId = BlobId.of(bucketName, blobName);
            return storage.delete(blobId);

        } catch (Exception e) {
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
                return "bin";
        }
    }
}