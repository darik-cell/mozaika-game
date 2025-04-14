package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.dto.PuzzlePieceImageDTO;
import com.ssau.mozaikagame.entity.Puzzle;
import com.ssau.mozaikagame.entity.PuzzlePiece;
import com.ssau.mozaikagame.repository.PuzzlePieceRepository;
import com.ssau.mozaikagame.repository.PuzzleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

        import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

  private final ResourceLoader resourceLoader;
  private final PuzzleRepository puzzleRepository;
  private final PuzzlePieceRepository puzzlePieceRepository;

  /**
   * Возвращает полное изображение пазла по puzzleId.
   */
  @GetMapping("/full/{puzzleId}")
  public ResponseEntity<Resource> getFullPuzzleImage(@PathVariable Long puzzleId) {
    Puzzle puzzle = puzzleRepository.findById(puzzleId)
            .orElseThrow(() -> new RuntimeException("Puzzle not found with id: " + puzzleId));
    String imagePath = puzzle.getImageUrl(); // например, "/puzzles/avatar/avatar.png"
    Resource resource = resourceLoader.getResource("classpath:" + imagePath);
    if (!resource.exists()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.IMAGE_PNG)
            .body(resource);
  }

  /**
   * Возвращает изображение одного кусочка пазла по pieceId.
   */
  @GetMapping("/piece/{pieceId}")
  public ResponseEntity<Resource> getPuzzlePieceImage(@PathVariable Long pieceId) {
    PuzzlePiece piece = puzzlePieceRepository.findById(pieceId)
            .orElseThrow(() -> new RuntimeException("Puzzle piece not found with id: " + pieceId));
    String imagePath = piece.getImageUrl(); // например, "/puzzles/avatar/avatar-0,0.png"
    Resource resource = resourceLoader.getResource("classpath:" + imagePath);
    if (!resource.exists()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.IMAGE_PNG)
            .body(resource);
  }

  /**
   * Возвращает коллекцию изображений (в виде Base64 строк) для всех фрагментов пазла с указанным puzzleId.
   */
  @GetMapping("/pieces/{puzzleId}")
  public ResponseEntity<List<PuzzlePieceImageDTO>> getPuzzlePiecesImagesByPuzzle(@PathVariable Long puzzleId) {
    List<PuzzlePiece> pieces = puzzlePieceRepository.findByPuzzleId(puzzleId);
    if (pieces.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    List<PuzzlePieceImageDTO> pieceImages = pieces.stream().map(piece -> {
      String imagePath = piece.getImageUrl(); // путь, например, "/puzzles/avatar/avatar-0,0.png"
      Resource resource = resourceLoader.getResource("classpath:" + imagePath);
      if (!resource.exists()) {
        return new PuzzlePieceImageDTO(piece.getId(), "not-found", "");
      }
      try (InputStream is = resource.getInputStream()) {
        byte[] imageBytes = StreamUtils.copyToByteArray(is);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return new PuzzlePieceImageDTO(piece.getId(), resource.getFilename(), base64Image);
      } catch (IOException ex) {
        throw new RuntimeException("Error reading image for piece id: " + piece.getId(), ex);
      }
    }).collect(Collectors.toList());

    return ResponseEntity.ok(pieceImages);
  }
}
