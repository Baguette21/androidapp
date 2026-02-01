package com.delacruz.trivia.kafka.consumer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.ScoreUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder consumer in kafka-service module.
 * The actual WebSocket bridging consumer is in trivia-game-service.
 * This file is kept for reference but should be disabled to avoid duplicate consumption.
 * 
 * To disable: Remove or comment out the @KafkaListener annotation
 */
// @Service - Disabled: actual consumer is in trivia-game-service
public class ScoreEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ScoreEventConsumer.class);

    // Disabled - actual consumer is in trivia-game-service
    // @KafkaListener(
    //         topics = KafkaTopicConfig.TOPIC_SCORE_UPDATED,
    //         containerFactory = "scoreKafkaListenerContainerFactory"
    // )
    public void consumeScoreUpdated(ScoreUpdatedEvent event) {
        logger.debug("Received score updated event (kafka-service): eventId={}, roomCode={}, playerId={}, points={}",
                event.getEventId(), event.getRoomCode(), event.getPlayerId(), event.getPointsEarned());
        // WebSocket handling is done in trivia-game-service consumer
    }
}
