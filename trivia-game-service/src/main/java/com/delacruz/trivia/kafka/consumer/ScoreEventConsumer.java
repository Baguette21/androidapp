package com.delacruz.trivia.kafka.consumer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.ScoreUpdatedEvent;
import com.delacruz.trivia.websocket.GameWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumes score update events from Kafka and broadcasts them to WebSocket clients.
 * Sends personal score updates to individual players and leaderboard updates to the room.
 * 
 * Note: This consumer is in game-service (not kafka-service) because it needs
 * access to GameWebSocketHandler for broadcasting to clients.
 */
@Service
public class ScoreEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ScoreEventConsumer.class);

    @Autowired
    private GameWebSocketHandler webSocketHandler;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_SCORE_UPDATED,
            containerFactory = "scoreKafkaListenerContainerFactory"
    )
    public void consumeScoreUpdated(ScoreUpdatedEvent event) {
        logger.info("Received score updated event: eventId={}, roomCode={}, playerId={}, points={}",
                event.getEventId(), event.getRoomCode(), event.getPlayerId(), event.getPointsEarned());

        try {
            // Build personal score update for the player
            Map<String, Object> personalScorePayload = buildPersonalScorePayload(event);
            
            // Send personal score update to the specific player
            webSocketHandler.sendToPlayer(
                    event.getPlayerId().toString(), 
                    "score", 
                    personalScorePayload
            );
            
            logger.debug("Sent personal score update to player {}: {} points", 
                    event.getPlayerId(), event.getPointsEarned());

            // Build leaderboard update for the room
            Map<String, Object> leaderboardUpdatePayload = buildLeaderboardUpdatePayload(event);
            
            // Broadcast leaderboard update to the room
            webSocketHandler.broadcastLeaderboard(event.getRoomCode(), leaderboardUpdatePayload);
            
            logger.debug("Broadcasted leaderboard update to room {}", event.getRoomCode());

        } catch (Exception e) {
            logger.error("Failed to broadcast score update to WebSocket: eventId={}, roomCode={}, error={}",
                    event.getEventId(), event.getRoomCode(), e.getMessage(), e);
        }
    }

    /**
     * Builds the personal score payload to send to the individual player.
     */
    private Map<String, Object> buildPersonalScorePayload(ScoreUpdatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "SCORE_UPDATE");
        payload.put("eventId", event.getEventId());
        payload.put("playerId", event.getPlayerId());
        payload.put("playerNickname", event.getPlayerNickname());
        payload.put("questionId", event.getQuestionId());
        payload.put("isCorrect", event.isCorrect());
        payload.put("correctAnswerIndex", event.getCorrectAnswerIndex());
        payload.put("pointsEarned", event.getPointsEarned());
        payload.put("newTotalScore", event.getNewTotalScore());
        payload.put("previousStreak", event.getPreviousStreak());
        payload.put("newStreak", event.getNewStreak());
        payload.put("currentRank", event.getCurrentRank());
        payload.put("serverTimestamp", event.getServerTimestamp());
        return payload;
    }

    /**
     * Builds the leaderboard update payload to broadcast to the room.
     * This is a lightweight update showing just the score change.
     */
    private Map<String, Object> buildLeaderboardUpdatePayload(ScoreUpdatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "LEADERBOARD_UPDATE");
        payload.put("roomCode", event.getRoomCode());
        payload.put("playerId", event.getPlayerId());
        payload.put("playerNickname", event.getPlayerNickname());
        payload.put("newTotalScore", event.getNewTotalScore());
        payload.put("newStreak", event.getNewStreak());
        payload.put("currentRank", event.getCurrentRank());
        payload.put("pointsEarned", event.getPointsEarned());
        payload.put("isCorrect", event.isCorrect());
        payload.put("serverTimestamp", event.getServerTimestamp());
        return payload;
    }
}
