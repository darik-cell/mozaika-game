package com.ssau.mozaikagame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PuzzlePieceImageDTO {
  private Long pieceId;
  private String fileName;
  private String base64Image;
}
