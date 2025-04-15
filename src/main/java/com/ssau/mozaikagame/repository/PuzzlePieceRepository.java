package com.ssau.mozaikagame.repository;

import com.ssau.mozaikagame.entity.PuzzlePiece;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PuzzlePieceRepository extends JpaRepository<PuzzlePiece, Long> {
  List<PuzzlePiece> findByPuzzleId(Long puzzleId);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(value = "update PuzzlePiece p set p.isPlacedCorrectly = false")
  void prepare();
}
