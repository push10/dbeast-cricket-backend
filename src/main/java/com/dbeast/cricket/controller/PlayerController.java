package com.dbeast.cricket.controller;

import com.dbeast.cricket.dto.*;
import com.dbeast.cricket.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor  // Lombok will generate constructor for final fields
public class PlayerController {

    private final PlayerRepository repository;  // Spring injects this

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