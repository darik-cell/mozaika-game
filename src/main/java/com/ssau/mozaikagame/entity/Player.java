package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
public class Player {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String password; // Хранится в виде BCrypt‑хэша

  // Один игрок может иметь несколько игровых сессий
  @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<GameSession> gameSessions = new ArrayList<>();

  // Хелпер‑метод для добавления игровой сессии и установки обратной ссылки
  public void addGameSession(GameSession session) {
    this.gameSessions.add(session);
    session.setPlayer(this);
  }

  // Хелпер‑метод для удаления игровой сессии
  public void removeGameSession(GameSession session) {
    this.gameSessions.remove(session);
    session.setPlayer(null);
  }
}
