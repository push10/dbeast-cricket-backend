package com.dbeast.cricket.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository repository;

    @PostMapping
    public Player create(@RequestBody Player player) {
        return repository.save(player);
    }

    @GetMapping
    public List<Player> getAll() {
        return repository.findAll();
    }
}