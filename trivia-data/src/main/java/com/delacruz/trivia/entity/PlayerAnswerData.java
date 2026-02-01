package com.delacruz.trivia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "player_answers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "question_id"})
})
public class PlayerAnswerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerData player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionData question;

    @Column(name = "selected_answer_index")
    private Integer selectedAnswerIndex;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "answer_time_ms")
    private Integer answerTimeMs;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @Column(name = "streak_at_time")
    private Integer streakAtTime = 0;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
