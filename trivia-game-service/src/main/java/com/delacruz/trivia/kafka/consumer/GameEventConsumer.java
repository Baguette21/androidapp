package com.delacruz.trivia.kafka.consumer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.GameStateEvent;
import com.delacruz.trivia.websocket.GameWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumes game state events from Kafka and broadcasts them to WebSocket clients.
 * This bridges the Kafka event stream to real-time WebSocket updates.
 * 
 * Note: This consumer is in game-service (not kafka-service) because it needs
 * access to GameWebSocketHandler for broadcasting to clients.
 */
@Service
public class GameEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GameEventConsumer.class);

    @Autowired
    private GameWebSocketHandler webSocketHandler;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_GAME_EVENTS,
            containerFactory = "gameEventKafkaListenerContainerFactory"
    )
    public void consumeGameEvent(GameStateEvent event) {
        logger.info("Received game event: eventId={}, roomCode={}, eventType={}",
                event.getEventId(), event.getRoomCode(), event.getEventType());

        try {
            // Build the WebSocket message payload
            Map<String, Object> wsPayload = buildWebSocketPayload(event);

            // Determine which destination to broadcast to based on event type
            switch (event.getEventType()) {
                case PLAYER_JOINED:
                case PLAYER_LEFT:
                case HOST_CHANGED:
                    // Player events go to the /players topic
                    webSocketHandler.broadcastPlayerEvent(event.getRoomCode(), wsPayload);
                    break;

                case GAME_STARTING:
                case QUESTION_START:
                case QUESTION_END:
                case GAME_FINISHED:
                    // Game flow events go to the /game topic
                    webSocketHandler.broadcastGameEvent(event.getRoomCode(), wsPayload);
                    break;

                default:
                    // Unknown event type, broadcast to game topic as fallback
                    logger.warn("Unknown event type: {}, broadcasting to game topic", event.getEventType());
                    webSocketHandler.broadcastGameEvent(event.getRoomCode(), wsPayload);
            }

            logger.debug("Broadcasted {} event to room {}", event.getEventType(), event.getRoomCode());

        } catch (Exception e) {
            logger.error("Failed to broadcast game event to WebSocket: eventId={}, roomCode={}, error={}",
                    event.getEventId(), event.getRoomCode(), e.getMessage(), e);
        }
    }

    /**
     * Builds the WebSocket payload from the Kafka event.
     */
    private Map<String, Object> buildWebSocketPayload(GameStateEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", event.getEventId());
        payload.put("eventType", event.getEventType().name());
        payload.put("roomCode", event.getRoomCode());
        payload.put("serverTimestamp", event.getServerTimestamp());

        // Add question-related fields if present
        if (event.getQuestionId() != null) {
            payload.put("questionId", event.getQuestionId());
        }
        if (event.getQuestionIndex() > 0 || event.getEventType() == GameStateEvent.GameEventType.QUESTION_START) {
            payload.put("questionIndex", event.getQuestionIndex());
        }
        if (event.getTotalQuestions() > 0) {
            payload.put("totalQuestions", event.getTotalQuestions());
        }
        if (event.getTimerSeconds() > 0) {
            payload.put("timerSeconds", event.getTimerSeconds());
        }
        if (event.getQuestionStartTime() > 0) {
            payload.put("questionStartTime", event.getQuestionStartTime());
        }

        // Add the event-specific payload
        if (event.getPayload() != null) {
            payload.put("payload", event.getPayload());
        }

        return payload;
    }
}
