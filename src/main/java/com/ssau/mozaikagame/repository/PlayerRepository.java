package com.ssau.mozaikagame.repository;

import com.ssau.mozaikagame.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
  Optional<Player> findByUsername(String username);
}
