package com.delacruz.trivia.serviceimpl;

import com.delacruz.trivia.entity.PlayerData;
import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.model.GameState;
import com.delacruz.trivia.model.LeaderboardEntry;
import com.delacruz.trivia.model.Question;
import com.delacruz.trivia.repository.PlayerRepository;
import com.delacruz.trivia.repository.QuestionRepository;
import com.delacruz.trivia.repository.RoomRepository;
import com.delacruz.trivia.service.GameService;
import com.delacruz.trivia.transform.TransformQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TransformQuestionService transformQuestionService;

    @Override
    public GameState getGameState(String roomCode) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        List<QuestionData> questions = getQuestionsForRoom(roomData);

        GameState gameState = new GameState();
        gameState.setRoomCode(roomCode);
        gameState.setStatus(roomData.getStatus().name());
        gameState.setCurrentQuestionIndex(roomData.getCurrentQuestionIndex());
        gameState.setTotalQuestions(questions.size());
        gameState.setTimerSeconds(roomData.getQuestionTimerSeconds());
        gameState.setLeaderboard(getLeaderboard(roomCode));

        // Get current question if game is in progress
        if (roomData.getStatus() == RoomData.RoomStatus.IN_PROGRESS) {
            if (roomData.getCurrentQuestionIndex() < questions.size()) {
                Question currentQuestion = transformQuestionService.transform(
                        questions.get(roomData.getCurrentQuestionIndex()));
                // Remove correct answer index for players
                currentQuestion.setCorrectAnswerIndex(null);
                gameState.setCurrentQuestion(currentQuestion);
            }
        }

        return gameState;
    }

    @Override
    @Transactional
    public void advanceToNextQuestion(String roomCode) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        int totalQuestions = getQuestionsForRoom(roomData).size();
        int nextIndex = roomData.getCurrentQuestionIndex() + 1;

        if (nextIndex >= totalQuestions) {
            endGame(roomCode);
        } else {
            roomData.setCurrentQuestionIndex(nextIndex);
            roomRepository.save(roomData);
            logger.info("Advanced to question {} in room {}", nextIndex, roomCode);
        }
    }

    private List<QuestionData> getQuestionsForRoom(RoomData roomData) {
        if (Boolean.TRUE.equals(roomData.getIsThemeBased()) && roomData.getCategory() != null) {
            return questionRepository.findByCategoryIdOrderByQuestionOrderAsc(roomData.getCategory().getId());
        }
        return questionRepository.findByRoomIdOrderByQuestionOrderAsc(roomData.getId());
    }

    @Override
    @Transactional
    public void endGame(String roomCode) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        roomData.setStatus(RoomData.RoomStatus.FINISHED);
        roomData.setFinishedAt(LocalDateTime.now());
        roomRepository.save(roomData);

        logger.info("Game ended in room {}", roomCode);
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(String roomCode) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        List<PlayerData> players = playerRepository.findByRoomIdOrderByJoinOrderAsc(roomData.getId());

        // Sort by score descending
        players.sort(Comparator.comparing(PlayerData::getTotalScore).reversed());

        AtomicInteger rank = new AtomicInteger(1);
        return players.stream()
                .map(p -> {
                    LeaderboardEntry entry = new LeaderboardEntry();
                    entry.setRank(rank.getAndIncrement());
                    entry.setPlayerId(p.getId());
                    entry.setNickname(p.getNickname());
                    entry.setTotalScore(p.getTotalScore());
                    entry.setCurrentStreak(p.getCurrentStreak());
                    return entry;
                })
                .collect(Collectors.toList());
    }
}
