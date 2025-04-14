package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puzzles")
@Data
@NoArgsConstructor
public class Puzzle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String imageUrl;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;
}
