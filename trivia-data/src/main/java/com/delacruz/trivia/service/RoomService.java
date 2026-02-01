package com.delacruz.trivia.service;

import com.delacruz.trivia.model.Room;
import com.delacruz.trivia.model.Player;

public interface RoomService {
    Room createRoom(Long categoryId, Boolean isThemeBased, Integer timerSeconds, Integer maxPlayers);
    Room getRoomByCode(String roomCode);
    Player joinRoom(String roomCode, String nickname);
    void leaveRoom(String roomCode, Long playerId);
    Room startGame(String roomCode, Long playerId);
    void updateHostPlayer(String roomCode, Long newHostPlayerId);
}
