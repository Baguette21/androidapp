package com.delacruz.trivia.serviceimpl;

import com.delacruz.trivia.entity.AnswerData;
import com.delacruz.trivia.entity.CategoryData;
import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.model.Question;
import com.delacruz.trivia.repository.AnswerRepository;
import com.delacruz.trivia.repository.CategoryRepository;
import com.delacruz.trivia.repository.QuestionRepository;
import com.delacruz.trivia.repository.RoomRepository;
import com.delacruz.trivia.service.QuestionService;
import com.delacruz.trivia.transform.TransformQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransformQuestionService transformQuestionService;

    @Override
    public List<Question> getQuestionsForRoom(String roomCode) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return questionRepository.findByRoomIdOrderByQuestionOrderAsc(roomData.getId())
                .stream()
                .map(transformQuestionService::transform)
                .collect(Collectors.toList());
    }

    @Override
    public List<Question> getQuestionsForCategory(Long categoryId, Integer limit) {
        List<QuestionData> questions = questionRepository.findByCategoryIdOrderByQuestionOrderAsc(categoryId);
        
        if (limit != null && limit < questions.size()) {
            questions = questions.subList(0, limit);
        }

        return questions.stream()
                .map(transformQuestionService::transform)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Question addQuestion(String roomCode, String questionText, List<String> answers, 
                                 Integer correctIndex, Integer timerSeconds) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        int questionOrder = questionRepository.countByRoomId(roomData.getId()) + 1;

        QuestionData questionData = new QuestionData();
        questionData.setRoom(roomData);
        questionData.setQuestionText(questionText);
        questionData.setQuestionOrder(questionOrder);
        questionData.setCorrectAnswerIndex(correctIndex);
        questionData.setTimerSeconds(timerSeconds != null ? timerSeconds : 15);
        questionData = questionRepository.save(questionData);

        // Save answers
        for (int i = 0; i < answers.size(); i++) {
            AnswerData answerData = new AnswerData();
            answerData.setQuestion(questionData);
            answerData.setAnswerText(answers.get(i));
            answerData.setAnswerIndex(i);
            answerRepository.save(answerData);
        }

        logger.info("Added question to room {}: {}", roomCode, questionText);
        return transformQuestionService.transform(questionData);
    }

    @Override
    @Transactional
    public Question updateQuestion(String roomCode, Long questionId, String questionText, 
                                    List<String> answers, Integer correctIndex, Integer timerSeconds) {
        QuestionData questionData = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        questionData.setQuestionText(questionText);
        questionData.setCorrectAnswerIndex(correctIndex);
        questionData.setTimerSeconds(timerSeconds);
        questionData = questionRepository.save(questionData);

        // Update answers
        answerRepository.deleteByQuestionId(questionId);
        for (int i = 0; i < answers.size(); i++) {
            AnswerData answerData = new AnswerData();
            answerData.setQuestion(questionData);
            answerData.setAnswerText(answers.get(i));
            answerData.setAnswerIndex(i);
            answerRepository.save(answerData);
        }

        return transformQuestionService.transform(questionData);
    }

    @Override
    @Transactional
    public void deleteQuestion(String roomCode, Long questionId) {
        answerRepository.deleteByQuestionId(questionId);
        questionRepository.deleteById(questionId);
        logger.info("Deleted question {} from room {}", questionId, roomCode);
    }

    @Override
    @Transactional
    public void copyQuestionsFromCategory(String roomCode, Long categoryId, Integer limit) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        CategoryData category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        List<QuestionData> categoryQuestions = questionRepository.findByCategoryIdOrderByQuestionOrderAsc(categoryId);
        
        if (limit != null && limit < categoryQuestions.size()) {
            categoryQuestions = categoryQuestions.subList(0, limit);
        }

        int order = 1;
        for (QuestionData source : categoryQuestions) {
            QuestionData newQuestion = new QuestionData();
            newQuestion.setRoom(roomData);
            newQuestion.setQuestionText(source.getQuestionText());
            newQuestion.setQuestionOrder(order++);
            newQuestion.setCorrectAnswerIndex(source.getCorrectAnswerIndex());
            newQuestion.setTimerSeconds(source.getTimerSeconds());
            newQuestion = questionRepository.save(newQuestion);

            // Copy answers
            List<AnswerData> sourceAnswers = answerRepository.findByQuestionIdOrderByAnswerIndexAsc(source.getId());
            for (AnswerData sourceAnswer : sourceAnswers) {
                AnswerData newAnswer = new AnswerData();
                newAnswer.setQuestion(newQuestion);
                newAnswer.setAnswerText(sourceAnswer.getAnswerText());
                newAnswer.setAnswerIndex(sourceAnswer.getAnswerIndex());
                answerRepository.save(newAnswer);
            }
        }

        logger.info("Copied {} questions from category {} to room {}", categoryQuestions.size(), categoryId, roomCode);
    }
}
