package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.PlayerData;
import com.delacruz.trivia.model.Player;
import org.springframework.stereotype.Service;

@Service
public class TransformPlayerServiceImpl implements TransformPlayerService {

    @Override
    public Player transform(PlayerData playerData) {
        if (playerData == null) return null;

        Player player = new Player();
        player.setId(playerData.getId());
        player.setRoomId(playerData.getRoom().getId());
        player.setRoomCode(playerData.getRoom().getRoomCode());
        player.setNickname(playerData.getNickname());
        player.setIsHost(playerData.getIsHost());
        player.setIsProxyHost(playerData.getIsProxyHost());
        player.setJoinOrder(playerData.getJoinOrder());
        player.setTotalScore(playerData.getTotalScore());
        player.setCurrentStreak(playerData.getCurrentStreak());
        player.setIsConnected(playerData.getIsConnected());
        player.setJoinedAt(playerData.getJoinedAt());

        return player;
    }

    @Override
    public PlayerData transform(Player player) {
        if (player == null) return null;

        PlayerData playerData = new PlayerData();
        playerData.setId(player.getId());
        playerData.setNickname(player.getNickname());
        playerData.setIsHost(player.getIsHost());
        playerData.setIsProxyHost(player.getIsProxyHost());
        playerData.setJoinOrder(player.getJoinOrder());
        playerData.setTotalScore(player.getTotalScore());
        playerData.setCurrentStreak(player.getCurrentStreak());
        playerData.setIsConnected(player.getIsConnected());

        return playerData;
    }
}
