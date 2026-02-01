package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.PlayerData;
import com.delacruz.trivia.model.Player;

public interface TransformPlayerService {
    Player transform(PlayerData playerData);
    PlayerData transform(Player player);
}
