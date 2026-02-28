package com.dbeast.cricket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dbeast.cricket.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {
}