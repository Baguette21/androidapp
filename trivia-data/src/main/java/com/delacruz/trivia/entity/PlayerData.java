package com.delacruz.trivia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "players", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "nickname"})
})
public class PlayerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomData room;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "is_host")
    private Boolean isHost = false;

    @Column(name = "is_proxy_host")
    private Boolean isProxyHost = false;

    @Column(name = "join_order", nullable = false)
    private Integer joinOrder;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "is_connected")
    private Boolean isConnected = true;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
    }
}
