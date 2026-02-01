package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.RoomData;
import com.delacruz.trivia.model.Room;

public interface TransformRoomService {
    Room transform(RoomData roomData);
    RoomData transform(Room room);
}
