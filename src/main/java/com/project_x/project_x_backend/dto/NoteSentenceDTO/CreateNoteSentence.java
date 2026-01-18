package com.project_x.project_x_backend.dto.NoteSentenceDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteSentence {
    private Integer sentenceIndex;
    private String sentenceText;
    private Float importanceScore;
    private List<Float> embedding;
}
