package com.delacruz.trivia.model;

import lombok.Data;
import java.util.List;

@Data
public class Question {
    private Long id;
    private Long roomId;
    private Long categoryId;
    private String questionText;
    private Integer questionOrder;
    private Integer correctAnswerIndex;
    private Integer timerSeconds;
    private List<Answer> answers;
}
