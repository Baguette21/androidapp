package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.model.Question;

public interface TransformQuestionService {
    Question transform(QuestionData questionData);
    QuestionData transform(Question question);
}
