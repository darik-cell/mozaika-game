package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.entity.Puzzle;
import com.ssau.mozaikagame.repository.PuzzleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PuzzleController {
  private final PuzzleRepository puzzleRepository;

  @QueryMapping
  public List<Puzzle> puzzles() {
    return puzzleRepository.findAll();
  }
}
