package com.delacruz.trivia.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmittedEvent {
    private String eventId;
    private String roomCode;
    private Long roomId;
    private Long playerId;
    private String playerNickname;
    private Long questionId;
    private int questionIndex;
    private int selectedAnswerIndex;
    private long answerTimeMs;
    private long serverTimestamp;
}
