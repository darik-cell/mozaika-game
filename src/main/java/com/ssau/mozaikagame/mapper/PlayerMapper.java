package com.ssau.mozaikagame.mapper;

import com.ssau.mozaikagame.dto.PlayerInput;
import com.ssau.mozaikagame.entity.Player;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
  Player toEntity(PlayerInput dto);
  PlayerInput toDto(Player entity);
}
