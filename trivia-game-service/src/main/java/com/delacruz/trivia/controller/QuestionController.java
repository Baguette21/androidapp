package com.delacruz.trivia.controller;

import com.delacruz.trivia.model.Question;
import com.delacruz.trivia.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms/{roomCode}/questions")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<?> getQuestions(@PathVariable String roomCode) {
        try {
            List<Question> questions = questionService.getQuestionsForRoom(roomCode.toUpperCase());
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Failed to get questions: {}", roomCode, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addQuestions(@PathVariable String roomCode, 
                                           @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) request.get("questions");
            
            int addedCount = 0;
            for (Map<String, Object> q : questionsData) {
                String questionText = (String) q.get("questionText");
                @SuppressWarnings("unchecked")
                List<String> answers = (List<String>) q.get("answers");
                Integer correctIndex = (Integer) q.get("correctAnswerIndex");
                Integer timerSeconds = (Integer) q.getOrDefault("timerSeconds", 15);

                questionService.addQuestion(roomCode.toUpperCase(), questionText, answers, correctIndex, timerSeconds);
                addedCount++;
            }

            int totalQuestions = questionService.getQuestionsForRoom(roomCode.toUpperCase()).size();

            logger.info("Added {} questions to room {}", addedCount, roomCode);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("addedCount", addedCount, "totalQuestions", totalQuestions));
        } catch (Exception e) {
            logger.error("Failed to add questions: {}", roomCode, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable String roomCode,
                                             @PathVariable Long questionId,
                                             @RequestBody Map<String, Object> request) {
        try {
            String questionText = (String) request.get("questionText");
            @SuppressWarnings("unchecked")
            List<String> answers = (List<String>) request.get("answers");
            Integer correctIndex = (Integer) request.get("correctAnswerIndex");
            Integer timerSeconds = (Integer) request.getOrDefault("timerSeconds", 15);

            Question updated = questionService.updateQuestion(
                    roomCode.toUpperCase(), questionId, questionText, answers, correctIndex, timerSeconds);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Failed to update question: {}", questionId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable String roomCode, @PathVariable Long questionId) {
        try {
            questionService.deleteQuestion(roomCode.toUpperCase(), questionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete question: {}", questionId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
