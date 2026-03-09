package com.dbeast.cricket.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.dbeast.cricket.dto.*;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.security.JwtUtil;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final JwtUtil jwtUtil;

    public PlayerService(PlayerRepository playerRepository, JwtUtil jwtUtil) {
        this.playerRepository = playerRepository;
        this.jwtUtil = jwtUtil;
    }

    // Register Player
    public PlayerResponse registerPlayer(PlayerRegistrationRequest request) {

        playerRepository.findByMobile(request.getMobile())
                .ifPresent(p -> {
                    throw new RuntimeException("Player already registered");
                });

        Player player = new Player();
        player.setName(request.getName());
        player.setMobile(request.getMobile());

        Player saved = playerRepository.save(player);

        return new PlayerResponse(
                saved.getId(),
                saved.getName(),
                saved.getMobile()
        );
    }

    // Send OTP
    public void sendOtp(SendOtpRequest request) {

        Player player = playerRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        player.setOtp(otp);

        playerRepository.save(player);

        System.out.println("OTP for mobile " + request.getMobile() + " is: " + otp);
    }

    // Login with OTP
    public PlayerLoginResponse login(PlayerLoginRequest request) {

        Player player = playerRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (!request.getOtp().equals(player.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        String token = jwtUtil.generateToken(player.getMobile());

        return new PlayerLoginResponse(
                player.getId(),
                player.getName(),
                player.getMobile(),
                token
        );
    }
}