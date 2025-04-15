package com.ssau.mozaikagame.service;

import com.ssau.mozaikagame.dto.CoordinatesInput;
import com.ssau.mozaikagame.entity.Coordinates;
import com.ssau.mozaikagame.entity.PuzzlePiece;
import com.ssau.mozaikagame.repository.PuzzlePieceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PuzzleService {

  private final PuzzlePieceRepository puzzlePieceRepository;
  // Допустимый порог смещения (например, в единицах сетки)
  private static final double POSITION_TOLERANCE = 0.5;

  /**
   * Обновляет положение кусочка пазла.
   * Если новое положение (newPosition) близко к правильному (correctPosition) с учётом tolerance,
   * то кусок фиксируется (isPlacedCorrectly = true).
   *
   * @param pieceId     идентификатор кусочка пазла
   * @param newPosition новые координаты (в сеточных единицах, например, (0,0), (1,2) и т.д.)
   * @return обновленный объект PuzzlePiece
   */
  public PuzzlePiece updatePiecePosition(Long pieceId, CoordinatesInput newPosition) {
    PuzzlePiece piece = puzzlePieceRepository.findById(pieceId)
            .orElseThrow(() -> new RuntimeException("Puzzle piece not found with id: " + pieceId));
    // Проверяем разницу координат по каждой оси
    if (Objects.equals(newPosition.getX(), piece.getCorrectPosition().getX()) && Objects.equals(newPosition.getY(), piece.getCorrectPosition().getY())) {
      // Если координаты близки, фиксируем кусок, устанавливая текущее положение равным правильному
      piece.setCurrentPosition(new Coordinates(piece.getCorrectPosition().getX(), piece.getCorrectPosition().getY()));
      piece.setPlacedCorrectly(true);
    } else {
      // Иначе просто обновляем его положение
      piece.setCurrentPosition(new Coordinates(newPosition.getX(), newPosition.getY()));
      piece.setPlacedCorrectly(false);
    }
    return puzzlePieceRepository.save(piece);
  }
}
