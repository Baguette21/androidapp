package com.delacruz.trivia.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEvent {
    private String eventId;
    private String roomCode;
    private Long playerId;
    private String playerNickname;
    private PlayerEventType eventType;
    private int totalPlayers;
    private long serverTimestamp;

    public enum PlayerEventType {
        JOINED,
        LEFT,
        DISCONNECTED,
        RECONNECTED
    }
}
