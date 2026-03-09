package com.dbeast.cricket.service;

import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    public Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    public Optional<Player> getPlayerByMobile(String mobile) {
        return playerRepository.findByMobile(mobile);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
}