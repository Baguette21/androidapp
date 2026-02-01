package com.delacruz.trivia.repository;

import com.delacruz.trivia.entity.RoomData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomData, Long> {
    Optional<RoomData> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);
}
