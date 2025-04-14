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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "puzzles_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Puzzle puzzle;
}
