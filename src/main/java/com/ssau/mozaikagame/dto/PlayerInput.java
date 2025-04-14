package com.ssau.mozaikagame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInput {
  private Long id;        // если id передан, выполняется обновление; иначе – создание
  private String username;  // будем использовать как email
  private String password;
}
