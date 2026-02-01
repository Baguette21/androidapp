package com.delacruz.trivia.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastToRoom(String roomCode, String destination, Object payload) {
        messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/" + destination, payload);
    }

    public void sendToPlayer(String playerId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(playerId, "/queue/" + destination, payload);
    }

    public void broadcastGameEvent(String roomCode, Object event) {
        broadcastToRoom(roomCode, "game", event);
    }

    public void broadcastPlayerEvent(String roomCode, Object event) {
        broadcastToRoom(roomCode, "players", event);
    }

    public void broadcastLeaderboard(String roomCode, Object leaderboard) {
        broadcastToRoom(roomCode, "leaderboard", leaderboard);
    }
}
