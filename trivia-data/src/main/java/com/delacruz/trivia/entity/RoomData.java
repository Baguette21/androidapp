package com.delacruz.trivia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rooms")
public class RoomData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true, length = 6)
    private String roomCode;

    @Column(name = "host_player_id")
    private Long hostPlayerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private CategoryData category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.LOBBY;

    @Column(name = "is_theme_based")
    private Boolean isThemeBased = false;

    @Column(name = "question_timer_seconds")
    private Integer questionTimerSeconds = 15;

    @Column(name = "max_players")
    private Integer maxPlayers = 100;

    @Column(name = "current_question_index")
    private Integer currentQuestionIndex = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum RoomStatus {
        LOBBY, IN_PROGRESS, FINISHED, CANCELLED
    }
}
