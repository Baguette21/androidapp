package com.delacruz.trivia.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Room {
    private Long id;
    private String roomCode;
    private Long hostPlayerId;
    private Category category;
    private String status;
    private Boolean isThemeBased;
    private Integer questionTimerSeconds;
    private Integer maxPlayers;
    private Integer currentQuestionIndex;
    private Integer totalQuestions;
    private Integer playerCount;
    private List<Player> players;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
