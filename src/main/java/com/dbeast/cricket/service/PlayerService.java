package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.PlayerLoginRequest;
import com.dbeast.cricket.dto.PlayerLoginResponse;
import com.dbeast.cricket.dto.PlayerProfileResponse;
import com.dbeast.cricket.dto.PlayerRegistrationRequest;
import com.dbeast.cricket.dto.PlayerResponse;
import com.dbeast.cricket.dto.SendOtpRequest;
import com.dbeast.cricket.dto.TeamMembershipResponse;
import com.dbeast.cricket.dto.UpdatePlayerProfileRequest;
import com.dbeast.cricket.dto.WalletResponse;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.UserRole;
import com.dbeast.cricket.entity.Wallet;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import com.dbeast.cricket.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final JwtUtil jwtUtil;

    public PlayerService(
            PlayerRepository playerRepository,
            PlayerTeamRepository playerTeamRepository,
            JwtUtil jwtUtil
    ) {
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public PlayerResponse registerPlayer(PlayerRegistrationRequest request) {
        String normalizedName = request.getName().trim();
        String normalizedMobile = request.getMobile().trim();

        playerRepository.findByMobile(normalizedMobile)
                .ifPresent(player -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Player already registered");
                });

        Player player = new Player();
        player.setName(normalizedName);
        player.setMobile(normalizedMobile);
        player.setUserRole(UserRole.PLAYER);

        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        player.setWallet(wallet);

        Player saved = playerRepository.save(player);

        return new PlayerResponse(saved.getId(), saved.getName(), saved.getMobile());
    }

    public void sendOtp(SendOtpRequest request) {
        Player player = findPlayerByMobile(request.getMobile().trim());

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        player.setOtp(otp);
        playerRepository.save(player);

        System.out.println("OTP for mobile " + request.getMobile() + " is: " + otp);
    }

    public PlayerLoginResponse login(PlayerLoginRequest request) {
        Player player = findPlayerByMobile(request.getMobile().trim());

        if (!request.getOtp().equals(player.getOtp())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        String token = jwtUtil.generateToken(player.getMobile());
        player.setOtp(null);
        playerRepository.save(player);

        return new PlayerLoginResponse(
                player.getId(),
                player.getName(),
                player.getMobile(),
                player.getUserRole(),
                token
        );
    }

    public PlayerProfileResponse getCurrentPlayerProfile(String mobile) {
        return mapProfile(findPlayerByMobile(mobile));
    }

    @Transactional
    public PlayerProfileResponse updateCurrentPlayerProfile(String mobile, UpdatePlayerProfileRequest request) {
        Player player = findPlayerByMobile(mobile);

        if (request.getName() != null) {
            player.setName(request.getName().trim());
        }
        if (request.getEmail() != null) {
            player.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getDateOfBirth() != null) {
            player.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            player.setAddress(request.getAddress().trim());
        }
        if (request.getProfileImageUrl() != null) {
            player.setProfileImageUrl(request.getProfileImageUrl().trim());
        }
        if (request.getPlayerRole() != null) {
            player.setPlayerRole(request.getPlayerRole());
        }

        return mapProfile(playerRepository.save(player));
    }

    private Player findPlayerByMobile(String mobile) {
        return playerRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private PlayerProfileResponse mapProfile(Player player) {
        WalletResponse walletResponse = player.getWallet() == null
                ? null
                : new WalletResponse(player.getWallet().getId(), player.getWallet().getBalance());

        return new PlayerProfileResponse(
                player.getId(),
                player.getName(),
                player.getMobile(),
                player.getEmail(),
                player.getDateOfBirth(),
                player.getAddress(),
                player.getProfileImageUrl(),
                player.getUserRole(),
                player.getPlayerRole(),
                walletResponse,
                playerTeamRepository.findByPlayer(player).stream()
                        .map(this::mapTeamMembership)
                        .toList()
        );
    }

    private TeamMembershipResponse mapTeamMembership(PlayerTeam playerTeam) {
        return new TeamMembershipResponse(
                playerTeam.getTeam().getId(),
                playerTeam.getTeam().getTeamName(),
                playerTeam.getRole()
        );
    }
}
