package com.delacruz.trivia.scheduler;

import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.kafka.event.GameStateEvent;
import com.delacruz.trivia.kafka.producer.GameEventProducer;
import com.delacruz.trivia.model.Question;
import com.delacruz.trivia.repository.QuestionRepository;
import com.delacruz.trivia.repository.RoomRepository;
import com.delacruz.trivia.service.GameService;
import com.delacruz.trivia.transform.TransformQuestionService;
import com.delacruz.trivia.websocket.GameWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Manages game timers for automatic question advancement.
 * When a game starts or advances to a new question, a timer is scheduled.
 * When the timer expires, the question ends and the next one starts (or game finishes).
 */
@Service
public class GameTimerScheduler {

    private static final Logger logger = LoggerFactory.getLogger(GameTimerScheduler.class);
    
    // Time to wait between questions (showing results) in seconds
    private static final int INTER_QUESTION_DELAY_SECONDS = 5;
    
    // Map of roomCode -> scheduled timer task
    private final ConcurrentHashMap<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private com.delacruz.trivia.repository.AnswerRepository answerRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameEventProducer gameEventProducer;

    @Autowired
    private GameWebSocketHandler webSocketHandler;

    @Autowired
    private TransformQuestionService transformQuestionService;

    /**
     * Start the game and schedule the first question timer.
     */
    public void startGame(String roomCode) {
        logger.info("Starting game timer for room: {}", roomCode);
        
        RoomData room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));
        
        List<QuestionData> questions;
        if (room.getIsThemeBased() && room.getCategory() != null) {
            logger.info("Fetching theme questions for category ID: {}", room.getCategory().getId());
            questions = questionRepository.findByCategoryIdOrderByQuestionOrderAsc(room.getCategory().getId());
        } else {
            logger.info("Fetching custom questions for room ID: {}", room.getId());
            questions = questionRepository.findByRoomIdOrderByQuestionOrderAsc(room.getId());
        }
        
        if (questions.isEmpty()) {
            logger.warn("No questions found for room: {}", roomCode);
            // Throw exception to notify controller/client
            throw new IllegalStateException("No questions found for this game. Please check theme or custom questions.");
        }
        
        // Start with the first question
        startQuestion(roomCode, 0);
    }

    /**
     * Start a specific question and schedule its timer.
     */
    public void startQuestion(String roomCode, int questionIndex) {
        RoomData room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));
        
        List<QuestionData> questions;
        if (room.getIsThemeBased() && room.getCategory() != null) {
            questions = questionRepository.findByCategoryIdOrderByQuestionOrderAsc(room.getCategory().getId());
        } else {
            questions = questionRepository.findByRoomIdOrderByQuestionOrderAsc(room.getId());
        }
        
        if (questionIndex >= questions.size()) {
            logger.info("No more questions, ending game for room: {}", roomCode);
            endGame(roomCode);
            return;
        }
        
        QuestionData currentQuestion = questions.get(questionIndex);
        int totalQuestions = questions.size();
        int timerSeconds = currentQuestion.getTimerSeconds() != null 
                ? currentQuestion.getTimerSeconds() 
                : room.getQuestionTimerSeconds();
        
        // Transform question for clients (without correct answer)
        Question questionForClients = transformQuestionService.transform(currentQuestion);
        questionForClients.setCorrectAnswerIndex(null); // Hide correct answer from clients
        
        // Publish QUESTION_START event via Kafka
        gameEventProducer.publishGameEvent(
                roomCode,
                GameStateEvent.GameEventType.QUESTION_START,
                currentQuestion.getId(),
                questionIndex,
                totalQuestions,
                timerSeconds,
                Map.of("question", questionForClients)
        );
        
        // Also broadcast directly via WebSocket for immediate delivery
        webSocketHandler.broadcastGameEvent(roomCode, Map.of(
                "eventType", "QUESTION_START",
                "questionId", currentQuestion.getId(),
                "questionIndex", questionIndex,
                "totalQuestions", totalQuestions,
                "timerSeconds", timerSeconds,
                "questionStartTime", System.currentTimeMillis(),
                "question", questionForClients
        ));
        
        logger.info("Question {} started in room {} (timer: {}s)", questionIndex + 1, roomCode, timerSeconds);
        
        // Cancel any existing timer for this room
        cancelTimer(roomCode);
        
        // Schedule question end
        ScheduledFuture<?> timerTask = scheduler.schedule(
                () -> endQuestion(roomCode, questionIndex),
                timerSeconds,
                TimeUnit.SECONDS
        );
        
        activeTimers.put(roomCode, timerTask);
    }

    /**
     * End the current question and show results.
     */
    private void endQuestion(String roomCode, int questionIndex) {
        logger.info("Question {} ended in room {}", questionIndex + 1, roomCode);
        
        RoomData room = roomRepository.findByRoomCode(roomCode).orElse(null);
        if (room == null || room.getStatus() != RoomData.RoomStatus.IN_PROGRESS) {
            logger.warn("Room {} not found or not in progress, skipping question end", roomCode);
            return;
        }
        
        List<QuestionData> questions;
        if (room.getIsThemeBased() && room.getCategory() != null) {
            questions = questionRepository.findByCategoryIdOrderByQuestionOrderAsc(room.getCategory().getId());
        } else {
            questions = questionRepository.findByRoomIdOrderByQuestionOrderAsc(room.getId());
        }
        
        if (questionIndex >= questions.size()) {
            endGame(roomCode);
            return;
        }
        
        QuestionData currentQuestion = questions.get(questionIndex);
        int totalQuestions = questions.size();
        
        // Get leaderboard for results
        var leaderboard = gameService.getLeaderboard(roomCode);
        
        String correctAnswerText = getCorrectAnswerText(currentQuestion);
        // Publish QUESTION_END event
        Question questionForClients = transformQuestionService.transform(currentQuestion);
        questionForClients.setCorrectAnswerIndex(null);
        gameEventProducer.publishGameEvent(
                roomCode,
                GameStateEvent.GameEventType.QUESTION_END,
                currentQuestion.getId(),
                questionIndex,
                totalQuestions,
                0,
                Map.of(
                        "correctAnswerIndex", currentQuestion.getCorrectAnswerIndex(),
                        "correctAnswerText", correctAnswerText,
                        "question", questionForClients,
                        "leaderboard", leaderboard.size() > 5 ? leaderboard.subList(0, 5) : leaderboard
                )
        );
        
        // Broadcast directly via WebSocket
        webSocketHandler.broadcastGameEvent(roomCode, Map.of(
                "eventType", "QUESTION_END",
                "questionId", currentQuestion.getId(),
                "questionIndex", questionIndex,
                "totalQuestions", totalQuestions,
                "correctAnswerIndex", currentQuestion.getCorrectAnswerIndex(),
                "correctAnswerText", correctAnswerText,
                "question", questionForClients,
                "leaderboard", leaderboard.size() > 5 ? leaderboard.subList(0, 5) : leaderboard
        ));
        
        // Broadcast full leaderboard update
        webSocketHandler.broadcastLeaderboard(roomCode, Map.of(
                "roomCode", roomCode,
                "questionIndex", questionIndex,
                "totalQuestions", totalQuestions,
                "leaderboard", leaderboard
        ));
        
        // Check if there are more questions
        int nextQuestionIndex = questionIndex + 1;
        if (nextQuestionIndex >= totalQuestions) {
            // Schedule game end after showing results
            scheduler.schedule(
                    () -> endGame(roomCode),
                    INTER_QUESTION_DELAY_SECONDS,
                    TimeUnit.SECONDS
            );
        } else {
            // Advance to next question in database
            gameService.advanceToNextQuestion(roomCode);
            
            // Schedule next question after showing results
            scheduler.schedule(
                    () -> startQuestion(roomCode, nextQuestionIndex),
                    INTER_QUESTION_DELAY_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    /**
     * End the game and publish final results.
     */
    private void endGame(String roomCode) {
        logger.info("Ending game for room: {}", roomCode);
        
        // Cancel any active timer
        cancelTimer(roomCode);
        
        // End game in database
        gameService.endGame(roomCode);
        
        // Get final leaderboard
        var leaderboard = gameService.getLeaderboard(roomCode);
        var podium = leaderboard.size() > 3 ? leaderboard.subList(0, 3) : leaderboard;
        
        // Publish GAME_FINISHED event
        gameEventProducer.publishGameEvent(
                roomCode,
                GameStateEvent.GameEventType.GAME_FINISHED,
                null,
                0,
                0,
                0,
                Map.of(
                        "podium", podium,
                        "allPlayers", leaderboard
                )
        );
        
        // Broadcast directly via WebSocket
        webSocketHandler.broadcastGameEvent(roomCode, Map.of(
                "eventType", "GAME_FINISHED",
                "roomCode", roomCode,
                "podium", podium,
                "allPlayers", leaderboard
        ));
        
        logger.info("Game finished for room: {}, winner: {}", roomCode, 
                podium.isEmpty() ? "none" : podium.get(0).getNickname());
    }

    /**
     * Cancel an active timer for a room.
     */
    public void cancelTimer(String roomCode) {
        ScheduledFuture<?> timer = activeTimers.remove(roomCode);
        if (timer != null && !timer.isDone()) {
            timer.cancel(false);
            logger.debug("Cancelled timer for room: {}", roomCode);
        }
    }

    /**
     * Cancel a game entirely (e.g., if all players leave).
     */
    public void cancelGame(String roomCode) {
        cancelTimer(roomCode);
        logger.info("Game cancelled for room: {}", roomCode);
    }

    /**
     * Check if a room has an active timer.
     */
    public boolean hasActiveTimer(String roomCode) {
        ScheduledFuture<?> timer = activeTimers.get(roomCode);
        return timer != null && !timer.isDone();
    }

    private String getCorrectAnswerText(QuestionData question) {
        Integer correctIndex = question.getCorrectAnswerIndex();
        if (correctIndex == null) {
            return "";
        }
        return answerRepository.findByQuestionIdOrderByAnswerIndexAsc(question.getId()).stream()
                .filter(answer -> correctIndex.equals(answer.getAnswerIndex()))
                .findFirst()
                .map(answer -> answer.getAnswerText())
                .orElse("");
    }

    /**
     * Get the number of active game timers.
     */
    public int getActiveTimerCount() {
        return (int) activeTimers.values().stream()
                .filter(t -> !t.isDone())
                .count();
    }
}
