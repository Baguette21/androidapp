package com.delacruz.trivia.repository;

import com.delacruz.trivia.entity.PlayerAnswerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswerData, Long> {
    Optional<PlayerAnswerData> findByPlayerIdAndQuestionId(Long playerId, Long questionId);
    List<PlayerAnswerData> findByQuestionId(Long questionId);
    List<PlayerAnswerData> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndQuestionId(Long playerId, Long questionId);
}
