package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.model.Room;
import com.delacruz.trivia.repository.PlayerRepository;
import com.delacruz.trivia.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class TransformRoomServiceImpl implements TransformRoomService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TransformCategoryService transformCategoryService;

    @Autowired
    private TransformPlayerService transformPlayerService;

    @Override
    public Room transform(RoomData roomData) {
        if (roomData == null) return null;

        Room room = new Room();
        room.setId(roomData.getId());
        room.setRoomCode(roomData.getRoomCode());
        room.setHostPlayerId(roomData.getHostPlayerId());
        room.setStatus(roomData.getStatus().name());
        room.setIsThemeBased(roomData.getIsThemeBased());
        room.setQuestionTimerSeconds(roomData.getQuestionTimerSeconds());
        room.setMaxPlayers(roomData.getMaxPlayers());
        room.setCurrentQuestionIndex(roomData.getCurrentQuestionIndex());
        room.setCreatedAt(roomData.getCreatedAt());
        room.setStartedAt(roomData.getStartedAt());
        room.setFinishedAt(roomData.getFinishedAt());

        if (roomData.getCategory() != null) {
            room.setCategory(transformCategoryService.transform(roomData.getCategory()));
        }

        room.setPlayerCount(playerRepository.countByRoomId(roomData.getId()));
        
        if (roomData.getIsThemeBased() && roomData.getCategory() != null) {
            room.setTotalQuestions(questionRepository.countByCategoryId(roomData.getCategory().getId()));
        } else {
            room.setTotalQuestions(questionRepository.countByRoomId(roomData.getId()));
        }

        room.setPlayers(playerRepository.findByRoomIdOrderByJoinOrderAsc(roomData.getId())
                .stream()
                .map(transformPlayerService::transform)
                .collect(Collectors.toList()));

        return room;
    }

    @Override
    public RoomData transform(Room room) {
        if (room == null) return null;

        RoomData roomData = new RoomData();
        roomData.setId(room.getId());
        roomData.setRoomCode(room.getRoomCode());
        roomData.setHostPlayerId(room.getHostPlayerId());
        if (room.getStatus() != null) {
            roomData.setStatus(RoomData.RoomStatus.valueOf(room.getStatus()));
        }
        roomData.setIsThemeBased(room.getIsThemeBased());
        roomData.setQuestionTimerSeconds(room.getQuestionTimerSeconds());
        roomData.setMaxPlayers(room.getMaxPlayers());
        roomData.setCurrentQuestionIndex(room.getCurrentQuestionIndex());

        return roomData;
    }
}
