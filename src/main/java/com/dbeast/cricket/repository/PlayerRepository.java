package com.dbeast.cricket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dbeast.cricket.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
     Optional<Player> findByMobile(String mobile);
}