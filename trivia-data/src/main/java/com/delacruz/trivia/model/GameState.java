package com.delacruz.trivia.model;

import lombok.Data;
import java.util.List;

@Data
public class GameState {
    private String roomCode;
    private String status;
    private Integer currentQuestionIndex;
    private Integer totalQuestions;
    private Question currentQuestion;
    private Integer timerSeconds;
    private Long questionStartTime;
    private List<LeaderboardEntry> leaderboard;
}
