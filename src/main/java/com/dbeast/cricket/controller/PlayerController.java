package com.dbeast.cricket.controller;

import com.dbeast.cricket.dto.PlayerLoginRequest;
import com.dbeast.cricket.dto.PlayerLoginResponse;
import com.dbeast.cricket.dto.PlayerProfileResponse;
import com.dbeast.cricket.dto.PlayerRegistrationRequest;
import com.dbeast.cricket.dto.PlayerResponse;
import com.dbeast.cricket.dto.SendOtpRequest;
import com.dbeast.cricket.dto.UpdatePlayerProfileRequest;
import com.dbeast.cricket.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<PlayerResponse> register(@Valid @RequestBody PlayerRegistrationRequest request) {
        return ResponseEntity.ok(playerService.registerPlayer(request));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        playerService.sendOtp(request);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<PlayerLoginResponse> login(@Valid @RequestBody PlayerLoginRequest request) {
        return ResponseEntity.ok(playerService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<PlayerProfileResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(playerService.getCurrentPlayerProfile(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<PlayerProfileResponse> updateMyProfile(
            Principal principal,
            @Valid @RequestBody UpdatePlayerProfileRequest request
    ) {
        return ResponseEntity.ok(playerService.updateCurrentPlayerProfile(principal.getName(), request));
    }
}
