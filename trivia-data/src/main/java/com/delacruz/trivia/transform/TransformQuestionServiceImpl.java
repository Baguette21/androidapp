package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.AnswerData;
import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.model.Answer;
import com.delacruz.trivia.model.Question;
import com.delacruz.trivia.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransformQuestionServiceImpl implements TransformQuestionService {

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public Question transform(QuestionData questionData) {
        if (questionData == null) return null;

        Question question = new Question();
        question.setId(questionData.getId());
        question.setQuestionText(questionData.getQuestionText());
        question.setQuestionOrder(questionData.getQuestionOrder());
        question.setCorrectAnswerIndex(questionData.getCorrectAnswerIndex());
        question.setTimerSeconds(questionData.getTimerSeconds());

        if (questionData.getRoom() != null) {
            question.setRoomId(questionData.getRoom().getId());
        }
        if (questionData.getCategory() != null) {
            question.setCategoryId(questionData.getCategory().getId());
        }

        List<AnswerData> answers = answerRepository.findByQuestionIdOrderByAnswerIndexAsc(questionData.getId());
        question.setAnswers(answers.stream().map(this::transformAnswer).collect(Collectors.toList()));

        return question;
    }

    @Override
    public QuestionData transform(Question question) {
        if (question == null) return null;

        QuestionData questionData = new QuestionData();
        questionData.setId(question.getId());
        questionData.setQuestionText(question.getQuestionText());
        questionData.setQuestionOrder(question.getQuestionOrder());
        questionData.setCorrectAnswerIndex(question.getCorrectAnswerIndex());
        questionData.setTimerSeconds(question.getTimerSeconds());

        return questionData;
    }

    private Answer transformAnswer(AnswerData answerData) {
        Answer answer = new Answer();
        answer.setId(answerData.getId());
        answer.setQuestionId(answerData.getQuestion().getId());
        answer.setAnswerText(answerData.getAnswerText());
        answer.setAnswerIndex(answerData.getAnswerIndex());
        return answer;
    }
}
