package com.dbeast.cricket.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.security.PlayerPrincipal;

@Service
public class PlayerUserDetailsService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    public PlayerUserDetailsService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {

        Player player = playerRepository.findByMobile(mobile)
                .orElseThrow(() -> new UsernameNotFoundException("Player not found"));

        return new PlayerPrincipal(player);
    }
}