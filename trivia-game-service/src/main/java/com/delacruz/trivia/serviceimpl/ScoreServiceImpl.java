package com.delacruz.trivia.serviceimpl;

import com.delacruz.trivia.entity.PlayerAnswerData;
import com.delacruz.trivia.entity.PlayerData;
import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.kafka.processor.ScoreCalculator;
import com.delacruz.trivia.kafka.producer.AnswerEventProducer;
import com.delacruz.trivia.kafka.producer.GameEventProducer;
import com.delacruz.trivia.model.PlayerAnswer;
import com.delacruz.trivia.repository.PlayerAnswerRepository;
import com.delacruz.trivia.repository.PlayerRepository;
import com.delacruz.trivia.repository.QuestionRepository;
import com.delacruz.trivia.repository.RoomRepository;
import com.delacruz.trivia.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreServiceImpl implements ScoreService {

    private static final Logger logger = LoggerFactory.getLogger(ScoreServiceImpl.class);

    @Autowired
    private PlayerAnswerRepository playerAnswerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ScoreCalculator scoreCalculator;

    @Autowired
    private AnswerEventProducer answerEventProducer;

    @Autowired
    private GameEventProducer gameEventProducer;

    @Override
    @Transactional
    public PlayerAnswer submitAnswer(String roomCode, Long playerId, Long questionId, 
                                      Integer selectedAnswerIndex, Integer answerTimeMs) {
        // Check if already answered
        if (playerAnswerRepository.existsByPlayerIdAndQuestionId(playerId, questionId)) {
            throw new IllegalArgumentException("Already answered this question");
        }

        PlayerData player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        QuestionData question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        RoomData room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Check if answer is correct
        boolean isCorrect = selectedAnswerIndex.equals(question.getCorrectAnswerIndex());

        // Calculate score
        ScoreCalculator.ScoreResult scoreResult = scoreCalculator.calculate(
                isCorrect, answerTimeMs, question.getTimerSeconds(), player.getCurrentStreak());

        // Save player answer
        PlayerAnswerData answerData = new PlayerAnswerData();
        answerData.setPlayer(player);
        answerData.setQuestion(question);
        answerData.setSelectedAnswerIndex(selectedAnswerIndex);
        answerData.setIsCorrect(isCorrect);
        answerData.setAnswerTimeMs(answerTimeMs);
        answerData.setPointsEarned(scoreResult.getPointsEarned());
        answerData.setStreakAtTime(scoreResult.getNewStreak());
        answerData = playerAnswerRepository.save(answerData);

        // Update player score and streak
        int previousStreak = player.getCurrentStreak();
        player.setTotalScore(player.getTotalScore() + scoreResult.getPointsEarned());
        player.setCurrentStreak(isCorrect ? scoreResult.getNewStreak() : 0);
        player = playerRepository.save(player);

        // Publish events
        answerEventProducer.publishAnswerSubmitted(
                roomCode, room.getId(), playerId, player.getNickname(),
                questionId, room.getCurrentQuestionIndex(), selectedAnswerIndex, answerTimeMs);

        gameEventProducer.publishScoreUpdated(
                roomCode, playerId, player.getNickname(), questionId,
                isCorrect, question.getCorrectAnswerIndex(),
                scoreResult.getPointsEarned(), player.getTotalScore(),
                previousStreak, player.getCurrentStreak(), 0);

        // Create response
        PlayerAnswer result = new PlayerAnswer();
        result.setId(answerData.getId());
        result.setPlayerId(playerId);
        result.setQuestionId(questionId);
        result.setSelectedAnswerIndex(selectedAnswerIndex);
        result.setIsCorrect(isCorrect);
        result.setAnswerTimeMs(answerTimeMs);
        result.setPointsEarned(scoreResult.getPointsEarned());
        result.setStreakAtTime(player.getCurrentStreak());

        logger.info("Answer submitted: player={}, correct={}, points={}", 
                player.getNickname(), isCorrect, scoreResult.getPointsEarned());

        return result;
    }

    @Override
    public int calculateScore(boolean isCorrect, int answerTimeMs, int timerSeconds, int currentStreak) {
        ScoreCalculator.ScoreResult result = scoreCalculator.calculate(
                isCorrect, answerTimeMs, timerSeconds, currentStreak);
        return result.getPointsEarned();
    }
}
