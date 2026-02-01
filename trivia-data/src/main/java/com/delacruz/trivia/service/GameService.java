package com.delacruz.trivia.service;

import com.delacruz.trivia.model.GameState;
import com.delacruz.trivia.model.LeaderboardEntry;

import java.util.List;

public interface GameService {
    GameState getGameState(String roomCode);
    void advanceToNextQuestion(String roomCode);
    void endGame(String roomCode);
    List<LeaderboardEntry> getLeaderboard(String roomCode);
}
