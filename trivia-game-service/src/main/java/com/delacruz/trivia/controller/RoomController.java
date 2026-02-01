package com.delacruz.trivia.controller;

import com.delacruz.trivia.model.Player;
import com.delacruz.trivia.model.Room;
import com.delacruz.trivia.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> request) {
        try {
            Long categoryId = request.get("categoryId") != null ? 
                    Long.valueOf(request.get("categoryId").toString()) : null;
            Boolean isThemeBased = (Boolean) request.getOrDefault("isThemeBased", false);
            Integer timerSeconds = (Integer) request.getOrDefault("questionTimerSeconds", 15);
            Integer maxPlayers = (Integer) request.getOrDefault("maxPlayers", 100);

            Room room = roomService.createRoom(categoryId, isThemeBased, timerSeconds, maxPlayers);
            logger.info("Room created: {}", room.getRoomCode());
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (Exception e) {
            logger.error("Failed to create room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getRoom(@PathVariable String roomCode) {
        try {
            Room room = roomService.getRoomByCode(roomCode.toUpperCase());
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ROOM_NOT_FOUND", "message", "Room not found"));
            }
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            logger.error("Failed to get room: {}", roomCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomCode, @RequestBody Map<String, String> request) {
        try {
            String nickname = request.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "INVALID_NICKNAME", "message", "Nickname is required"));
            }

            Player player = roomService.joinRoom(roomCode.toUpperCase(), nickname.trim());
            logger.info("Player joined: {} -> {}", nickname, roomCode);
            return ResponseEntity.ok(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "JOIN_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to join room: {}", roomCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{roomCode}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomCode, @RequestParam Long playerId) {
        try {
            roomService.leaveRoom(roomCode.toUpperCase(), playerId);
            logger.info("Player left room: playerId={}, roomCode={}", playerId, roomCode);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to leave room: {}", roomCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<?> startGame(@PathVariable String roomCode, @RequestParam Long playerId) {
        try {
            Room room = roomService.startGame(roomCode.toUpperCase(), playerId);
            logger.info("Game started: {}", roomCode);
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "NOT_HOST", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to start game: {}", roomCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
