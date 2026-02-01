package com.delacruz.trivia.kafka.consumer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.AnswerSubmittedEvent;
import com.delacruz.trivia.websocket.GameWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumes answer submitted events from Kafka.
 * Note: The actual score calculation and database updates are done in ScoreServiceImpl
 * when the answer is initially submitted via REST API. This consumer is for additional
 * real-time notifications like showing other players that someone answered.
 * 
 * Note: This consumer is in game-service (not kafka-service) because it needs
 * access to GameWebSocketHandler for broadcasting to clients.
 */
@Service
public class AnswerEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AnswerEventConsumer.class);

    @Autowired
    private GameWebSocketHandler webSocketHandler;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_ANSWER_SUBMITTED,
            containerFactory = "answerKafkaListenerContainerFactory"
    )
    public void consumeAnswerSubmitted(AnswerSubmittedEvent event) {
        logger.info("Received answer submitted event: eventId={}, roomCode={}, playerId={}",
                event.getEventId(), event.getRoomCode(), event.getPlayerId());

        try {
            // Build notification payload for the room
            // This lets the host (spectator mode) know that a player has answered
            Map<String, Object> answerNotification = buildAnswerNotificationPayload(event);
            
            // Broadcast to room that a player has answered (without revealing the answer)
            // This is useful for the host spectator view to show answer progress
            webSocketHandler.broadcastGameEvent(event.getRoomCode(), answerNotification);
            
            logger.debug("Broadcasted answer notification for player {} in room {}", 
                    event.getPlayerNickname(), event.getRoomCode());

        } catch (Exception e) {
            logger.error("Failed to broadcast answer notification to WebSocket: eventId={}, roomCode={}, error={}",
                    event.getEventId(), event.getRoomCode(), e.getMessage(), e);
        }
    }

    /**
     * Builds a notification payload indicating a player has answered.
     * Does NOT include the actual answer to prevent cheating.
     */
    private Map<String, Object> buildAnswerNotificationPayload(AnswerSubmittedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "PLAYER_ANSWERED");
        payload.put("eventId", event.getEventId());
        payload.put("roomCode", event.getRoomCode());
        payload.put("playerId", event.getPlayerId());
        payload.put("playerNickname", event.getPlayerNickname());
        payload.put("questionId", event.getQuestionId());
        payload.put("questionIndex", event.getQuestionIndex());
        // Intentionally NOT including selectedAnswerIndex to prevent cheating
        payload.put("answerTimeMs", event.getAnswerTimeMs());
        payload.put("serverTimestamp", event.getServerTimestamp());
        return payload;
    }
}
