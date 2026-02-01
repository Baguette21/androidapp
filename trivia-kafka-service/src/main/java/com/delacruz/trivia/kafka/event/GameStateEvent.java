package com.delacruz.trivia.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateEvent {
    private String eventId;
    private String roomCode;
    private GameEventType eventType;
    private Long questionId;
    private int questionIndex;
    private int totalQuestions;
    private long questionStartTime;
    private int timerSeconds;
    private Object payload;
    private long serverTimestamp;

    public enum GameEventType {
        PLAYER_JOINED,
        PLAYER_LEFT,
        GAME_STARTING,
        QUESTION_START,
        QUESTION_END,
        GAME_FINISHED,
        HOST_CHANGED
    }
}
