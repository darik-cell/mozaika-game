package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
public class GameSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private Integer duration;  // В секундах

  private String status;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;

  private Integer remainingTime;  // Оставшееся время в секундах

  // Ссылка на игрока (многие сессии могут принадлежать одному игроку)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  @ToString.Exclude
  private Player player;

  // Одна сессия содержит много паззловых элементов
  @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<PuzzlePiece> pieces = new ArrayList<>();

  // Хелпер-методы для управления паззловыми элементами
  public void addPuzzlePiece(PuzzlePiece piece) {
    this.pieces.add(piece);
    piece.setGameSession(this);
  }

  public void removePuzzlePiece(PuzzlePiece piece) {
    this.pieces.remove(piece);
    piece.setGameSession(null);
  }
}
