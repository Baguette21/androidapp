package com.delacruz.trivia.kafka.consumer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.AnswerSubmittedEvent;
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
public class AnswerEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AnswerEventConsumer.class);

    // Disabled - actual consumer is in trivia-game-service
    // @KafkaListener(
    //         topics = KafkaTopicConfig.TOPIC_ANSWER_SUBMITTED,
    //         containerFactory = "answerKafkaListenerContainerFactory"
    // )
    public void consumeAnswerSubmitted(AnswerSubmittedEvent event) {
        logger.debug("Received answer submitted event (kafka-service): eventId={}, roomCode={}, playerId={}",
                event.getEventId(), event.getRoomCode(), event.getPlayerId());
        // WebSocket handling is done in trivia-game-service consumer
    }
}
