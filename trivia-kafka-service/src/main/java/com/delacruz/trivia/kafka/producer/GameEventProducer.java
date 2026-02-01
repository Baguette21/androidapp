package com.delacruz.trivia.kafka.producer;

import com.delacruz.trivia.kafka.KafkaTopicConfig;
import com.delacruz.trivia.kafka.event.GameStateEvent;
import com.delacruz.trivia.kafka.event.ScoreUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(GameEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishGameEvent(String roomCode, GameStateEvent.GameEventType eventType,
                                  Long questionId, int questionIndex, int totalQuestions,
                                  int timerSeconds, Object payload) {
        GameStateEvent event = GameStateEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .roomCode(roomCode)
                .eventType(eventType)
                .questionId(questionId)
                .questionIndex(questionIndex)
                .totalQuestions(totalQuestions)
                .questionStartTime(System.currentTimeMillis())
                .timerSeconds(timerSeconds)
                .payload(payload)
                .serverTimestamp(System.currentTimeMillis())
                .build();

        logger.info("Publishing game event: roomCode={}, eventType={}", roomCode, eventType);
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_GAME_EVENTS, roomCode, event);
    }

    public void publishScoreUpdated(String roomCode, Long playerId, String playerNickname,
                                     Long questionId, boolean isCorrect, int correctAnswerIndex,
                                     int pointsEarned, int newTotalScore,
                                     int previousStreak, int newStreak, int currentRank) {
        ScoreUpdatedEvent event = ScoreUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .roomCode(roomCode)
                .playerId(playerId)
                .playerNickname(playerNickname)
                .questionId(questionId)
                .isCorrect(isCorrect)
                .correctAnswerIndex(correctAnswerIndex)
                .pointsEarned(pointsEarned)
                .newTotalScore(newTotalScore)
                .previousStreak(previousStreak)
                .newStreak(newStreak)
                .currentRank(currentRank)
                .serverTimestamp(System.currentTimeMillis())
                .build();

        logger.info("Publishing score updated: roomCode={}, playerId={}, points={}", roomCode, playerId, pointsEarned);
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_SCORE_UPDATED, roomCode, event);
    }
}
