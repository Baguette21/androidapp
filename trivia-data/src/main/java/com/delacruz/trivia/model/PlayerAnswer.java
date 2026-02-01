package com.delacruz.trivia.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlayerAnswer {
    private Long id;
    private Long playerId;
    private Long questionId;
    private Integer selectedAnswerIndex;
    private Boolean isCorrect;
    private Integer answerTimeMs;
    private Integer pointsEarned;
    private Integer streakAtTime;
    private LocalDateTime submittedAt;
}
