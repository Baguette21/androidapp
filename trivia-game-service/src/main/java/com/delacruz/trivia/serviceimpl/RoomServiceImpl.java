package com.delacruz.trivia.serviceimpl;

import com.delacruz.trivia.entity.CategoryData;
import com.delacruz.trivia.entity.PlayerData;
import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.kafka.event.GameStateEvent;
import com.delacruz.trivia.kafka.producer.GameEventProducer;
import com.delacruz.trivia.model.Player;
import com.delacruz.trivia.model.Room;
import com.delacruz.trivia.repository.CategoryRepository;
import com.delacruz.trivia.repository.PlayerRepository;
import com.delacruz.trivia.repository.RoomRepository;
import com.delacruz.trivia.service.RoomService;
import com.delacruz.trivia.transform.TransformPlayerService;
import com.delacruz.trivia.transform.TransformRoomService;
import com.delacruz.trivia.websocket.GameWebSocketHandler;
import com.delacruz.trivia.scheduler.GameTimerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);
    private static final String ROOM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransformRoomService transformRoomService;

    @Autowired
    private TransformPlayerService transformPlayerService;

    @Autowired
    private GameEventProducer gameEventProducer;

    @Autowired
    private GameWebSocketHandler webSocketHandler;

    @Autowired
    @Lazy
    private GameTimerScheduler gameTimerScheduler;

    @Override
    @Transactional
    public Room createRoom(Long categoryId, Boolean isThemeBased, Integer timerSeconds, Integer maxPlayers) {
        RoomData roomData = new RoomData();
        roomData.setRoomCode(generateUniqueRoomCode());
        roomData.setIsThemeBased(isThemeBased != null ? isThemeBased : false);
        roomData.setQuestionTimerSeconds(timerSeconds != null ? timerSeconds : 15);
        roomData.setMaxPlayers(maxPlayers != null ? maxPlayers : 100);
        roomData.setStatus(RoomData.RoomStatus.LOBBY);

        if (categoryId != null) {
            CategoryData category = categoryRepository.findById(categoryId).orElse(null);
            roomData.setCategory(category);
        }

        roomData = roomRepository.save(roomData);
        logger.info("Created room: {}", roomData.getRoomCode());

        return transformRoomService.transform(roomData);
    }

    @Override
    public Room getRoomByCode(String roomCode) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode).orElse(null);
        return transformRoomService.transform(roomData);
    }

    @Override
    @Transactional
    public Player joinRoom(String roomCode, String nickname) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (roomData.getStatus() != RoomData.RoomStatus.LOBBY) {
            throw new IllegalArgumentException("Cannot join room that has already started");
        }

        int playerCount = playerRepository.countByRoomId(roomData.getId());
        if (playerCount >= roomData.getMaxPlayers()) {
            throw new IllegalArgumentException("Room is full");
        }

        if (playerRepository.existsByRoomIdAndNickname(roomData.getId(), nickname)) {
            throw new IllegalArgumentException("Nickname already taken in this room");
        }

        boolean isHost = playerCount == 0;

        PlayerData playerData = new PlayerData();
        playerData.setRoom(roomData);
        playerData.setNickname(nickname);
        playerData.setIsHost(isHost);
        playerData.setJoinOrder(playerCount + 1);
        playerData = playerRepository.save(playerData);

        if (isHost) {
            roomData.setHostPlayerId(playerData.getId());
            roomRepository.save(roomData);
        }

        // Must reload player to ensure relationships are set for transformation
        playerData = playerRepository.findById(playerData.getId()).orElseThrow();

        Player player = transformPlayerService.transform(playerData);

        // Broadcast player joined event (Kafka)
        Map<String, Object> joinPayload = Map.of("player", player, "totalPlayers", playerCount + 1);
        gameEventProducer.publishGameEvent(
                roomCode,
                GameStateEvent.GameEventType.PLAYER_JOINED,
                null, 0, 0, 0,
                joinPayload
        );

        // Direct WebSocket broadcast (bypass Kafka for testing/reliability)
        // CRITICAL FIX: Structure must match Android PlayerEventDto exactly
        // PlayerEventDto expects "player" at top level, not inside "payload"
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("eventId", UUID.randomUUID().toString());
        wsPayload.put("eventType", "PLAYER_JOINED");
        wsPayload.put("roomCode", roomCode);
        wsPayload.put("player", player); // Top level player object
        wsPayload.put("serverTimestamp", System.currentTimeMillis());
        
        webSocketHandler.broadcastPlayerEvent(roomCode, wsPayload);

        return player;
    }

    @Override
    @Transactional
    public void leaveRoom(String roomCode, Long playerId) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        PlayerData playerData = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        boolean wasHost = playerData.getIsHost();
        String nickname = playerData.getNickname();

        playerRepository.delete(playerData);

        // If host left, promote next player
        if (wasHost) {
            PlayerData newHost = playerRepository.findFirstByRoomIdAndIsHostFalseOrderByJoinOrderAsc(roomData.getId())
                    .orElse(null);
            if (newHost != null) {
                newHost.setIsProxyHost(true);
                playerRepository.save(newHost);
                roomData.setHostPlayerId(newHost.getId());
                roomRepository.save(roomData);

                Map<String, Object> hostChangePayload = Map.of("newHostId", newHost.getId(), "newHostNickname", newHost.getNickname());
                gameEventProducer.publishGameEvent(
                        roomCode,
                        GameStateEvent.GameEventType.HOST_CHANGED,
                        null, 0, 0, 0,
                        hostChangePayload
                );

                Map<String, Object> wsPayload = new HashMap<>();
                wsPayload.put("eventType", "HOST_CHANGED");
                wsPayload.put("roomCode", roomCode);
                wsPayload.put("payload", hostChangePayload);
                webSocketHandler.broadcastPlayerEvent(roomCode, wsPayload);
            }
        }

        int remainingPlayers = playerRepository.countByRoomId(roomData.getId());
        Map<String, Object> leavePayload = Map.of("playerId", playerId, "nickname", nickname, "totalPlayers", remainingPlayers);
        gameEventProducer.publishGameEvent(
                roomCode,
                GameStateEvent.GameEventType.PLAYER_LEFT,
                null, 0, 0, 0,
                leavePayload
        );

        // Direct WebSocket broadcast for LEAVE
        Map<String, Object> wsLeavePayload = new HashMap<>();
        wsLeavePayload.put("eventId", UUID.randomUUID().toString());
        wsLeavePayload.put("eventType", "PLAYER_LEFT");
        wsLeavePayload.put("roomCode", roomCode);
        wsLeavePayload.put("serverTimestamp", System.currentTimeMillis());

        // Construct a partial player map because Android expects a "player" object
        Map<String, Object> partialPlayer = new HashMap<>();
        partialPlayer.put("id", playerId);
        partialPlayer.put("nickname", nickname);
        partialPlayer.put("isHost", wasHost); // Retain original status for info
        partialPlayer.put("isProxyHost", false);
        partialPlayer.put("totalScore", 0);
        partialPlayer.put("currentStreak", 0);
        partialPlayer.put("isConnected", false);
        
        wsLeavePayload.put("player", partialPlayer);
        
        webSocketHandler.broadcastPlayerEvent(roomCode, wsLeavePayload);
    }

    @Override
    @Transactional
    public Room startGame(String roomCode, Long playerId) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!roomData.getHostPlayerId().equals(playerId)) {
            PlayerData player = playerRepository.findById(playerId).orElse(null);
            if (player == null || !player.getIsProxyHost()) {
                throw new IllegalArgumentException("Only the host can start the game");
            }
        }

        roomData.setStatus(RoomData.RoomStatus.IN_PROGRESS);
        roomData.setStartedAt(LocalDateTime.now());
        roomData.setCurrentQuestionIndex(0);
        roomData = roomRepository.save(roomData);

        gameEventProducer.publishGameEvent(
                roomCode,
                GameStateEvent.GameEventType.GAME_STARTING,
                null, 0, 0, roomData.getQuestionTimerSeconds(),
                null
        );

        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("eventId", UUID.randomUUID().toString());
        wsPayload.put("eventType", "GAME_STARTING");
        wsPayload.put("roomCode", roomCode);
        wsPayload.put("timerSeconds", roomData.getQuestionTimerSeconds());
        wsPayload.put("serverTimestamp", System.currentTimeMillis());
        // Android GameStateDto might optionally check payload, but timerSeconds is top level there too? 
        // Let's check GameStateDto again. It has top level timerSeconds.
        
        webSocketHandler.broadcastGameEvent(roomCode, wsPayload);

        // Start the game timer scheduler to manage question timing
        try {
            gameTimerScheduler.startGame(roomCode);
        } catch (Exception e) {
            logger.error("Failed to start game timer for room {}: {}", roomCode, e.getMessage(), e);
            throw new IllegalStateException("Failed to start game: " + e.getMessage());
        }

        return transformRoomService.transform(roomData);
    }

    @Override
    @Transactional
    public void updateHostPlayer(String roomCode, Long newHostPlayerId) {
        RoomData roomData = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        roomData.setHostPlayerId(newHostPlayerId);
        roomRepository.save(roomData);
    }

    private String generateUniqueRoomCode() {
        String code;
        do {
            code = generateRoomCode();
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }

    private String generateRoomCode() {
        StringBuilder sb = new StringBuilder(ROOM_CODE_LENGTH);
        for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
            sb.append(ROOM_CODE_CHARS.charAt(random.nextInt(ROOM_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
