package com.delacruz.trivia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "answers")
public class AnswerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionData question;

    @Column(name = "answer_text", nullable = false, length = 255)
    private String answerText;

    @Column(name = "answer_index", nullable = false)
    private Integer answerIndex;
}
