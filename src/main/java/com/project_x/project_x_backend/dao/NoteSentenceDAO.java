package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.dto.NoteSentenceDTO.CreateNoteSentence;
import com.project_x.project_x_backend.entity.NoteSentence;
import com.project_x.project_x_backend.repository.NoteSentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NoteSentenceDAO {

    private final NoteSentenceRepository noteSentenceRepository;

    @Transactional
    public void createNoteSentences(UUID noteId, UUID userId, List<CreateNoteSentence> sentences) {
        List<NoteSentence> noteSentences = sentences.stream()
                .map(dto -> {
                    com.pgvector.PGvector embeddingVector = null;
                    if (dto.getEmbedding() != null) {
                        float[] embeddingArray = new float[dto.getEmbedding().size()];
                        for (int i = 0; i < dto.getEmbedding().size(); i++) {
                            embeddingArray[i] = dto.getEmbedding().get(i);
                        }
                        embeddingVector = new com.pgvector.PGvector(embeddingArray);
                    }

                    return NoteSentence.builder()
                            .noteId(noteId)
                            .userId(userId)
                            .sentenceIndex(dto.getSentenceIndex())
                            .sentenceText(dto.getSentenceText())
                            .importanceScore(dto.getImportanceScore())
                            .embedding(embeddingVector)
                            .build();
                })
                .collect(Collectors.toList());

        noteSentenceRepository.saveAll(noteSentences);
    }
}
