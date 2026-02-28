package com.dbeast.cricket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dbeast.cricket.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}