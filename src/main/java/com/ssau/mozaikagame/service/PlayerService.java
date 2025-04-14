package com.ssau.mozaikagame.service;

import com.ssau.mozaikagame.dto.PlayerInput;
import com.ssau.mozaikagame.entity.Player;
import com.ssau.mozaikagame.mapper.PlayerMapper;
import com.ssau.mozaikagame.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final PlayerMapper playerMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  /**
   * Создает или обновляет игрока на основании DTO.
   * Если в DTO присутствует id – производится обновление, иначе создается новый игрок.
   *
   * @param playerInput DTO с данными игрока
   * @return сохраненный объект Player
   */
  public Player createOrUpdatePlayer(PlayerInput playerInput) {
    Player player = playerMapper.toEntity(playerInput);
    if (player.getId() != null) {
      Player existing = playerRepository.findById(player.getId())
              .orElseThrow(() -> new RuntimeException("Player not found with id: " + player.getId()));
      existing.setUsername(player.getUsername());
      // Если пароль изменился, то хэшируем новый пароль
      if (player.getPassword() != null && !player.getPassword().isBlank()
              && !passwordEncoder.matches(player.getPassword(), existing.getPassword())) {
        existing.setPassword(passwordEncoder.encode(player.getPassword()));
      }
      return playerRepository.save(existing);
    } else {
      // Хэшируем пароль при создании нового игрока
      player.setPassword(passwordEncoder.encode(player.getPassword()));
      return playerRepository.save(player);
    }
  }

  /**
   * Аутентификация игрока.
   *
   * @param email    email (username) игрока
   * @param password пароль
   * @return id игрока, если учетные данные корректны
   */
  public Long authenticate(String email, String password) {
    Optional<Player> playerOpt = playerRepository.findByUsername(email);
    if (playerOpt.isPresent()) {
      Player player = playerOpt.get();
      if (passwordEncoder.matches(password, player.getPassword())) {
        return player.getId();
      }
    }
    throw new RuntimeException("Invalid credentials");
  }

  public List<Player> findAll() {
    return playerRepository.findAll();
  }

  public Player findById(Long id) {
    return playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
  }
}
