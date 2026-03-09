package com.dbeast.cricket.controller;

import com.dbeast.cricket.dto.*;
import com.dbeast.cricket.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // Register player
    @PostMapping("/register")
    public ResponseEntity<PlayerResponse> register(@Valid @RequestBody PlayerRegistrationRequest request) {
        return ResponseEntity.ok(playerService.registerPlayer(request));
    }

    // Generate OTP
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody SendOtpRequest request) {
        playerService.sendOtp(request);
        return ResponseEntity.ok("OTP sent successfully");
    }

    // Verify OTP + Login
    @PostMapping("/login")
    public ResponseEntity<PlayerLoginResponse> login(@Valid @RequestBody PlayerLoginRequest request) {
        return ResponseEntity.ok(playerService.login(request));
    }
}