package com.dbeast.cricket.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchStatus;
import com.dbeast.cricket.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository repository;

    public Match createMatch(Match match) {
        match.setStatus(MatchStatus.SCHEDULED);
        return repository.save(match);
    }

    public List<Match> getAllMatches() {
        return repository.findAll();
    }
}