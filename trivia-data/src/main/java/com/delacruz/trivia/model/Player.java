package com.delacruz.trivia.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Player {
    private Long id;
    private Long roomId;
    private String roomCode;
    private String nickname;
    private Boolean isHost;
    private Boolean isProxyHost;
    private Integer joinOrder;
    private Integer totalScore;
    private Integer currentStreak;
    private Boolean isConnected;
    private LocalDateTime joinedAt;
}
