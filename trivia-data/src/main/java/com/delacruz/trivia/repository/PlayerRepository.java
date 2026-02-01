package com.delacruz.trivia.repository;

import com.delacruz.trivia.entity.PlayerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerData, Long> {
    List<PlayerData> findByRoomIdOrderByJoinOrderAsc(Long roomId);
    Optional<PlayerData> findByRoomIdAndNickname(Long roomId, String nickname);
    Optional<PlayerData> findFirstByRoomIdAndIsHostFalseOrderByJoinOrderAsc(Long roomId);
    int countByRoomId(Long roomId);
    boolean existsByRoomIdAndNickname(Long roomId, String nickname);
}
