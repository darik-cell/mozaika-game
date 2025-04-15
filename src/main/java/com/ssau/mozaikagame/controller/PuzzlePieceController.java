package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.dto.CoordinatesInput;
import com.ssau.mozaikagame.entity.PuzzlePiece;
import com.ssau.mozaikagame.service.PuzzleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PuzzlePieceController {

  private final PuzzleService puzzleService;

  /**
   * GraphQL мутация для обновления положения кусочка пазла.
   * Принимает идентификатор кусочка и новый объект с координатами.
   */
  @MutationMapping
  public PuzzlePiece updatePiecePosition(@Argument Long pieceId, @Argument CoordinatesInput newPosition) {
    return puzzleService.updatePiecePosition(pieceId, newPosition);
  }
}
