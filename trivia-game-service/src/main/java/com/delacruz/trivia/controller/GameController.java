package com.delacruz.trivia.controller;

import com.delacruz.trivia.entity.PlayerData;
import com.delacruz.trivia.model.GameState;
import com.delacruz.trivia.model.LeaderboardEntry;
import com.delacruz.trivia.model.PlayerAnswer;
import com.delacruz.trivia.repository.PlayerRepository;
import com.delacruz.trivia.service.GameService;
import com.delacruz.trivia.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms/{roomCode}")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable String roomCode, @RequestBody Map<String, Object> request) {
        try {
            Long playerId = Long.valueOf(request.get("playerId").toString());
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Integer selectedAnswerIndex = (Integer) request.get("selectedAnswerIndex");
            Integer answerTimeMs = (Integer) request.get("answerTimeMs");

            PlayerAnswer result = scoreService.submitAnswer(
                    roomCode.toUpperCase(), playerId, questionId, selectedAnswerIndex, answerTimeMs);

            // Fetch updated player to get actual total score
            PlayerData player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));

            logger.info("Answer submitted: roomCode={}, playerId={}, correct={}", 
                    roomCode, playerId, result.getIsCorrect());

            return ResponseEntity.ok(Map.of(
                    "isCorrect", result.getIsCorrect(),
                    "correctAnswerIndex", result.getSelectedAnswerIndex(),
                    "pointsEarned", result.getPointsEarned(),
                    "newTotalScore", player.getTotalScore(),
                    "newStreak", result.getStreakAtTime()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ANSWER_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to submit answer: {}", roomCode, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable String roomCode) {
        try {
            List<LeaderboardEntry> leaderboard = gameService.getLeaderboard(roomCode.toUpperCase());
            GameState gameState = gameService.getGameState(roomCode.toUpperCase());

            return ResponseEntity.ok(Map.of(
                    "roomCode", roomCode.toUpperCase(),
                    "questionIndex", gameState.getCurrentQuestionIndex(),
                    "totalQuestions", gameState.getTotalQuestions(),
                    "leaderboard", leaderboard
            ));
        } catch (Exception e) {
            logger.error("Failed to get leaderboard: {}", roomCode, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/results")
    public ResponseEntity<?> getFinalResults(@PathVariable String roomCode) {
        try {
            GameState gameState = gameService.getGameState(roomCode.toUpperCase());
            List<LeaderboardEntry> leaderboard = gameService.getLeaderboard(roomCode.toUpperCase());

            // Get top 3 for podium
            List<LeaderboardEntry> podium = leaderboard.size() > 3 ? 
                    leaderboard.subList(0, 3) : leaderboard;

            return ResponseEntity.ok(Map.of(
                    "roomCode", roomCode.toUpperCase(),
                    "status", gameState.getStatus(),
                    "totalQuestions", gameState.getTotalQuestions(),
                    "podium", podium,
                    "allPlayers", leaderboard
            ));
        } catch (Exception e) {
            logger.error("Failed to get results: {}", roomCode, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
