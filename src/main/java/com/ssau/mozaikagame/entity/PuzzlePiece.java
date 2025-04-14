package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "puzzle_pieces")
@Data
@NoArgsConstructor
public class PuzzlePiece {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int pieceNumber;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "x", column = @Column(name = "correct_x")),
          @AttributeOverride(name = "y", column = @Column(name = "correct_y"))
  })
  private Coordinates correctPosition;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "x", column = @Column(name = "current_x")),
          @AttributeOverride(name = "y", column = @Column(name = "current_y"))
  })
  private Coordinates currentPosition;

  private boolean isPlacedCorrectly;

  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "puzzles_id")
  @ToString.Exclude
  private Puzzle puzzle;
}
