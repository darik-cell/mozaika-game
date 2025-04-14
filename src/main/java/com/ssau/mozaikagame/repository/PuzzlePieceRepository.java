package com.ssau.mozaikagame.repository;

import com.ssau.mozaikagame.entity.PuzzlePiece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuzzlePieceRepository extends JpaRepository<PuzzlePiece, Long> {
  List<PuzzlePiece> findByPuzzleId(Long puzzleId);
}
