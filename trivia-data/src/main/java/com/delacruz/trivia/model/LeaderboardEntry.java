package com.delacruz.trivia.model;

import lombok.Data;

@Data
public class LeaderboardEntry {
    private Integer rank;
    private Long playerId;
    private String nickname;
    private Integer totalScore;
    private Integer currentStreak;
}
