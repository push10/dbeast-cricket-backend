package com.dbeast.cricket.repository;

import com.dbeast.cricket.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByMobile(String mobile);

}