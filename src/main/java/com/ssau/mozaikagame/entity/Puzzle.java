package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puzzles")
@Data
@NoArgsConstructor
public class Puzzle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String imageUrl;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;

  // Одна сессия содержит много паззловых элементов
  @OneToMany(mappedBy = "puzzle", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<PuzzlePiece> pieces = new ArrayList<>();

  public void addPuzzlePiece(PuzzlePiece piece) {
    this.pieces.add(piece);
    piece.setPuzzle(this);
  }

  public void removePuzzlePiece(PuzzlePiece piece) {
    this.pieces.remove(piece);
    piece.setPuzzle(null);
  }
}
