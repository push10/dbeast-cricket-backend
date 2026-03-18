package com.dbeast.cricket.repository;

import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, Long> {

    List<PlayerTeam> findByPlayer(Player player);

    List<PlayerTeam> findByTeam(Team team);

    boolean existsByPlayerAndTeam(Player player, Team team);

    boolean existsByPlayerAndTeamAndRole(Player player, Team team, TeamMemberRole role);
}
