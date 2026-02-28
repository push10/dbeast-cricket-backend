package com.dbeast.cricket.controller;
 

import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.service.MatchService;

import jakarta.validation.Valid;
  
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
            @Valid @RequestBody MatchRequest request) {

        return ResponseEntity.ok(matchService.createMatch(request));
    }
}