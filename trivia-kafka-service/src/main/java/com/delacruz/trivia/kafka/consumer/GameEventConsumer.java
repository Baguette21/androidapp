package com.delacruz.trivia.kafka.consumer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.GameStateEvent;
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
public class GameEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GameEventConsumer.class);

    // Disabled - actual consumer is in trivia-game-service
    // @KafkaListener(
    //         topics = KafkaTopicConfig.TOPIC_GAME_EVENTS,
    //         containerFactory = "gameEventKafkaListenerContainerFactory"
    // )
    public void consumeGameEvent(GameStateEvent event) {
        logger.debug("Received game event (kafka-service): eventId={}, roomCode={}, eventType={}",
                event.getEventId(), event.getRoomCode(), event.getEventType());
        // WebSocket handling is done in trivia-game-service consumer
    }
}
