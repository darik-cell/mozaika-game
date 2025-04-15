package com.ssau.mozaikagame.dto;

import com.ssau.mozaikagame.entity.Puzzle;
import com.ssau.mozaikagame.entity.PuzzlePiece;
import lombok.Data;

import java.util.List;

@Data
public class GameSessionDTO {
  private Puzzle puzzle;
  private List<PuzzlePiece> pieces;
}
