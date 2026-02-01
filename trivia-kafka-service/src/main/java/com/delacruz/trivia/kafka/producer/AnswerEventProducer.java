package com.delacruz.trivia.kafka.producer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.AnswerSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AnswerEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(AnswerEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAnswerSubmitted(String roomCode, Long roomId, Long playerId, String playerNickname,
                                        Long questionId, int questionIndex, int selectedAnswerIndex, long answerTimeMs) {
        AnswerSubmittedEvent event = AnswerSubmittedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .roomCode(roomCode)
                .roomId(roomId)
                .playerId(playerId)
                .playerNickname(playerNickname)
                .questionId(questionId)
                .questionIndex(questionIndex)
                .selectedAnswerIndex(selectedAnswerIndex)
                .answerTimeMs(answerTimeMs)
                .serverTimestamp(System.currentTimeMillis())
                .build();

        logger.info("Publishing answer submitted event: roomCode={}, playerId={}", roomCode, playerId);
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_ANSWER_SUBMITTED, roomCode, event);
    }
}
