package com.dbeast.cricket.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dbeast.cricket.entity.Match;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
}