package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.entity.AudioStore;
import com.project_x.project_x_backend.repository.AudioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AudioService {

    @Autowired
    private AudioRepository audioRepository;

    @Autowired
    private GcsStorageService gcsStorageService;

    public AudioStore uploadAudio(UUID userId, byte[] audioBytes, String contentType)
            throws IOException {

        validateAudioData(audioBytes, contentType);

        AudioStore audioStore = new AudioStore(
                userId,
                null,
                audioBytes.length,
                AudioStore.ProcessingStatus.PENDING);

        audioStore = audioRepository.save(audioStore);

        try {
            String gcsUrl = gcsStorageService.uploadAudio(
                    audioBytes,
                    audioStore.getId().toString(),
                    contentType);
            audioStore.setGcsAudioUrl(gcsUrl);
            audioStore.setProcessingStatus(AudioStore.ProcessingStatus.PROCESSING);

            return audioRepository.save(audioStore);
        } catch (Exception e) {

            audioStore.setProcessingStatus(AudioStore.ProcessingStatus.FAILED);
            audioRepository.save(audioStore);
            throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
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
        if (audioBytes.length < 4)
            return false;

        switch (contentType) {
            case "audio/mpeg":
                // MP3 header check (ID3 tag or frame sync)
                return (audioBytes[0] == 'I' && audioBytes[1] == 'D' && audioBytes[2] == '3') ||
                        (audioBytes[0] == (byte) 0xFF && (audioBytes[1] & 0xE0) == 0xE0);

            case "audio/wav":
            case "audio/x-wav":
                // WAV header check
                return audioBytes[0] == 'R' && audioBytes[1] == 'I' &&
                        audioBytes[2] == 'F' && audioBytes[3] == 'F';

            case "audio/flac":
                // FLAC header check
                return audioBytes[0] == 'f' && audioBytes[1] == 'L' &&
                        audioBytes[2] == 'a' && audioBytes[3] == 'C';

            case "audio/mp4":
            case "audio/x-m4a":
                // M4A/MP4 might have various headers, basic check for ftyp box
                return audioBytes.length > 8 &&
                        audioBytes[4] == 'f' && audioBytes[5] == 't' &&
                        audioBytes[6] == 'y' && audioBytes[7] == 'p';

            default:
                return true; // Allow other formats for now
        }
    }

    private boolean isValidAudioType(String contentType) {
        return contentType.equals("audio/mpeg") || // mp3
                contentType.equals("audio/wav") || // wav
                contentType.equals("audio/x-wav") || // wav
                contentType.equals("audio/mp4") || // m4a
                contentType.equals("audio/x-m4a") || // m4a
                contentType.equals("audio/flac"); // flac
    }

}
