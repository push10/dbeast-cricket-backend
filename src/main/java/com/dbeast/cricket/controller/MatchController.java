package com.dbeast.cricket.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.service.MatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService service;

    @PostMapping
    public Match create(@RequestBody Match match) {
        return service.createMatch(match);
    }

    @GetMapping
    public List<Match> getAll() {
        return service.getAllMatches();
    }
}