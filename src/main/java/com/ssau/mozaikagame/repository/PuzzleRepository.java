package com.ssau.mozaikagame.repository;

import com.ssau.mozaikagame.entity.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {}
