package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.dto.PlayerInput;
import com.ssau.mozaikagame.entity.Player;
import com.ssau.mozaikagame.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService playerService;

  // Query для получения списка игроков
  @QueryMapping
  public List<Player> players() {
    return playerService.findAll();
  }

  // Query для получения игрока по id
  @QueryMapping
  public Player player(@Argument Long id) {
    return playerService.findById(id);
  }

  // Mutation для создания/обновления игрока (mapping происходит на уровне сервиса)
  @MutationMapping
  public Player newPlayer(@Argument PlayerInput player) {
    return playerService.createOrUpdatePlayer(player);
  }

  @QueryMapping
  public Long authenticatePlayer(@Argument PlayerInput player) {
    return playerService.authenticate(player.getUsername(), player.getPassword());
  }
}
