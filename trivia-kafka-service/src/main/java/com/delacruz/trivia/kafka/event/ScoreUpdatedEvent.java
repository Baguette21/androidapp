package com.delacruz.trivia.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreUpdatedEvent {
    private String eventId;
    private String roomCode;
    private Long playerId;
    private String playerNickname;
    private Long questionId;
    private boolean isCorrect;
    private int correctAnswerIndex;
    private int pointsEarned;
    private int newTotalScore;
    private int previousStreak;
    private int newStreak;
    private int currentRank;
    private long serverTimestamp;
}
