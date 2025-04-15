### java\com\ssau\mozaikagame\controller\ImageController.java
```java
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
}
```

### java\com\ssau\mozaikagame\controller\PageController.java
```java
package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.dto.GameSessionDTO;
import com.ssau.mozaikagame.entity.Puzzle;
import com.ssau.mozaikagame.entity.PuzzlePiece;
import com.ssau.mozaikagame.repository.PuzzlePieceRepository;
import com.ssau.mozaikagame.repository.PuzzleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

  private final PuzzleRepository puzzleRepository;
  private final PuzzlePieceRepository puzzlePieceRepository;

  @GetMapping("/login")
  public String loginPage() {
    return "login"; // будет отображена /WEB-INF/views/login.jsp
  }

  @GetMapping("/register")
  public String registrationPage() {
    return "registration"; // отображается /WEB-INF/views/registration.jsp
  }

  @GetMapping("/home")
  public String homePage(Model model) {
    List<Puzzle> puzzles = puzzleRepository.findAll();
    model.addAttribute("puzzles", puzzles);
    return "home"; // страница /WEB-INF/views/home.jsp
  }

  @GetMapping("/logout")
  public String logoutPage(HttpServletRequest request) {
    request.getSession().invalidate();
    return "redirect:/login";
  }

  @GetMapping("/puzzleDetail")
  public String puzzleDetailPage(@RequestParam("pid") Long puzzleId, Model model) {
    puzzlePieceRepository.prepare();
    Puzzle puzzle = puzzleRepository.findById(puzzleId)
            .orElseThrow(() -> new RuntimeException("Puzzle not found with id: " + puzzleId));
    List<PuzzlePiece> pieces = puzzlePieceRepository.findByPuzzleId(puzzleId);
    GameSessionDTO gameSession = new GameSessionDTO();
    gameSession.setPuzzle(puzzle);
    gameSession.setPieces(pieces);
    model.addAttribute("gameSession", gameSession);
    return "game"; // будет отображаться /WEB-INF/views/game.jsp
  }

}
```

### java\com\ssau\mozaikagame\controller\PlayerController.java
```java
package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.dto.PlayerInput;
import com.ssau.mozaikagame.entity.Player;
import com.ssau.mozaikagame.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService playerService;

  // Query для получения списка игроков
  @QueryMapping
  public List<Player> players() {
    return playerService.findAll();
  }

  // Query для получения игрока по id
  @QueryMapping
  public Player player(@Argument Long id) {
    return playerService.findById(id);
  }

  // Mutation для создания/обновления игрока (mapping происходит на уровне сервиса)
  @MutationMapping
  public Player newPlayer(@Argument PlayerInput player) {
    return playerService.createOrUpdatePlayer(player);
  }

  @QueryMapping
  public Long authenticatePlayer(@Argument PlayerInput player, HttpServletRequest request) {
    Long playerId = playerService.authenticate(player.getUsername(), player.getPassword());
    request.getSession().setAttribute("playerId", playerId);
    return playerId;
  }
}
```

### java\com\ssau\mozaikagame\controller\PuzzleController.java
```java
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
```

### java\com\ssau\mozaikagame\controller\PuzzlePieceController.java
```java
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
```

### java\com\ssau\mozaikagame\dto\CoordinatesInput.java
```java
package com.ssau.mozaikagame.dto;

import lombok.Data;

@Data
public class CoordinatesInput {
  private Integer x;
  private Integer y;
}
```

### java\com\ssau\mozaikagame\dto\GameSessionDTO.java
```java
package com.ssau.mozaikagame.dto;

import com.ssau.mozaikagame.entity.Puzzle;
import com.ssau.mozaikagame.entity.PuzzlePiece;
import lombok.Data;

import java.util.List;

@Data
public class GameSessionDTO {
  private Puzzle puzzle;
  private List<PuzzlePiece> pieces;
}
```

### java\com\ssau\mozaikagame\dto\PlayerInput.java
```java
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
```

### java\com\ssau\mozaikagame\dto\PuzzlePieceImageDTO.java
```java
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
```

### java\com\ssau\mozaikagame\entity\Coordinates.java
```java
package com.ssau.mozaikagame.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
  private Integer x;
  private Integer y;
}
```

### java\com\ssau\mozaikagame\entity\Difficulty.java
```java
package com.ssau.mozaikagame.entity;

public enum Difficulty {
  EASY,
  MEDIUM,
  HARD
}
```

### java\com\ssau\mozaikagame\entity\GameSession.java
```java
package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
public class GameSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private Integer duration;  // В секундах

  private String status;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;

  private Integer remainingTime;  // Оставшееся время в секундах

  // Ссылка на игрока (многие сессии могут принадлежать одному игроку)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  @ToString.Exclude
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "puzzles_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Puzzle puzzle;
}
```

### java\com\ssau\mozaikagame\entity\Player.java
```java
package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
public class Player {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String password; // Хранится в виде BCrypt‑хэша

  // Один игрок может иметь несколько игровых сессий
  @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<GameSession> gameSessions = new ArrayList<>();

  // Хелпер‑метод для добавления игровой сессии и установки обратной ссылки
  public void addGameSession(GameSession session) {
    this.gameSessions.add(session);
    session.setPlayer(this);
  }

  // Хелпер‑метод для удаления игровой сессии
  public void removeGameSession(GameSession session) {
    this.gameSessions.remove(session);
    session.setPlayer(null);
  }
}
```

### java\com\ssau\mozaikagame\entity\Puzzle.java
```java
package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puzzles")
@Data
@NoArgsConstructor
public class Puzzle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String imageUrl;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;

  // Одна сессия содержит много паззловых элементов
  @OneToMany(mappedBy = "puzzle", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<PuzzlePiece> pieces = new ArrayList<>();

  public void addPuzzlePiece(PuzzlePiece piece) {
    this.pieces.add(piece);
    piece.setPuzzle(this);
  }

  public void removePuzzlePiece(PuzzlePiece piece) {
    this.pieces.remove(piece);
    piece.setPuzzle(null);
  }
}
```

### java\com\ssau\mozaikagame\entity\PuzzlePiece.java
```java
package com.ssau.mozaikagame.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "puzzle_pieces")
@Data
@NoArgsConstructor
public class PuzzlePiece {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int pieceNumber;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "x", column = @Column(name = "correct_x")),
          @AttributeOverride(name = "y", column = @Column(name = "correct_y"))
  })
  private Coordinates correctPosition;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "x", column = @Column(name = "current_x")),
          @AttributeOverride(name = "y", column = @Column(name = "current_y"))
  })
  private Coordinates currentPosition;

  private boolean isPlacedCorrectly;

  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "puzzles_id")
  @ToString.Exclude
  private Puzzle puzzle;
}
```

### java\com\ssau\mozaikagame\filter\SessionFilter.java
```java
package com.ssau.mozaikagame.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class SessionFilter implements Filter {

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String uri = request.getRequestURI();
    // Определяем эндпоинты, которые не требуют проверки сессии
    if (uri.endsWith("/login") || uri.endsWith("/register")
            || uri.contains("/graphql") && isAuthenticationQuery(request)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    // Если атрибут playerId отсутствует, перенаправляем на страницу логина
    if (request.getSession().getAttribute("playerId") == null) {
      log.info("Пользователь не авторизован при доступе к: {}", uri);
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  /**
   * Этот метод можно доработать для более точного определения GraphQL-запросов,
   * требующих авторизации (например, если в теле запроса содержится операция,
   * отличная от аутентификации).
   * В упрощённом варианте здесь можно вернуть true, чтобы не блокировать запросы по /graphql.
   */
  private boolean isAuthenticationQuery(HttpServletRequest request) {
    // Для упрощения на данный момент пропускаем все запросы к /graphql
    return true;
  }
}
```

### java\com\ssau\mozaikagame\mapper\PlayerMapper.java
```java
package com.ssau.mozaikagame.mapper;

import com.ssau.mozaikagame.dto.PlayerInput;
import com.ssau.mozaikagame.entity.Player;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
  Player toEntity(PlayerInput dto);
  PlayerInput toDto(Player entity);
}
```

### java\com\ssau\mozaikagame\MozaikaGameApplication.java
```java
package com.ssau.mozaikagame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class MozaikaGameApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(MozaikaGameApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(MozaikaGameApplication.class, args);
  }

}
```

### java\com\ssau\mozaikagame\repository\PlayerRepository.java
```java
package com.ssau.mozaikagame.repository;

import com.ssau.mozaikagame.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
  Optional<Player> findByUsername(String username);
}
```

### java\com\ssau\mozaikagame\repository\PuzzlePieceRepository.java
```java
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
```

### java\com\ssau\mozaikagame\repository\PuzzleRepository.java
```java
package com.ssau.mozaikagame.repository;

import com.ssau.mozaikagame.entity.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {}
```

### java\com\ssau\mozaikagame\service\PlayerService.java
```java
package com.ssau.mozaikagame.service;

import com.ssau.mozaikagame.dto.PlayerInput;
import com.ssau.mozaikagame.entity.Player;
import com.ssau.mozaikagame.mapper.PlayerMapper;
import com.ssau.mozaikagame.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final PlayerMapper playerMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  /**
   * Создает или обновляет игрока на основании DTO.
   * Если в DTO присутствует id – производится обновление, иначе создается новый игрок.
   *
   * @param playerInput DTO с данными игрока
   * @return сохраненный объект Player
   */
  public Player createOrUpdatePlayer(PlayerInput playerInput) {
    Player player = playerMapper.toEntity(playerInput);
    if (player.getId() != null) {
      Player existing = playerRepository.findById(player.getId())
              .orElseThrow(() -> new RuntimeException("Player not found with id: " + player.getId()));
      existing.setUsername(player.getUsername());
      // Если пароль изменился, то хэшируем новый пароль
      if (player.getPassword() != null && !player.getPassword().isBlank()
              && !passwordEncoder.matches(player.getPassword(), existing.getPassword())) {
        existing.setPassword(passwordEncoder.encode(player.getPassword()));
      }
      return playerRepository.save(existing);
    } else {
      // Хэшируем пароль при создании нового игрока
      player.setPassword(passwordEncoder.encode(player.getPassword()));
      return playerRepository.save(player);
    }
  }

  /**
   * Аутентификация игрока.
   *
   * @param email    email (username) игрока
   * @param password пароль
   * @return id игрока, если учетные данные корректны
   */
  public Long authenticate(String email, String password) {
    Optional<Player> playerOpt = playerRepository.findByUsername(email);
    if (playerOpt.isPresent()) {
      Player player = playerOpt.get();
      if (passwordEncoder.matches(password, player.getPassword())) {
        return player.getId();
      }
    }
    throw new RuntimeException("Invalid credentials");
  }

  public List<Player> findAll() {
    return playerRepository.findAll();
  }

  public Player findById(Long id) {
    return playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
  }
}
```

### java\com\ssau\mozaikagame\service\PuzzleService.java
```java
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
```

### resources\application.yaml
```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
  datasource:
#    url: jdbc:h2:file:./data/db;MODE=PostgreSQL;AUTO_SERVER=TRUE
    url: jdbc:h2:mem:MODE=PostgreSQL
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
#      ddl-auto: validate
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    defer-datasource-initialization: true
  graphql:
    graphiql:
      enabled: true
  h2:
    console:
      enabled: true
      path: /h2-console
  mvc:
    view:
      prefix: /WEB-INF/views/
      suffix: .jsp
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql

server:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```

### resources\data.sql
```sql
-- 1. Заполнение таблицы puzzles
INSERT INTO puzzles (id, name, image_url, difficulty)
VALUES (1, 'Avatar', '/puzzles/avatar/avatar.png', 'EASY'),
       (2, 'Avatar', '/puzzles/avatar/avatar.png', 'MEDIUM'),
       (3, 'Avatar', '/puzzles/avatar/avatar.png', 'HARD'),
       (4, 'Ocean', '/puzzles/ocean/ocean.png', 'EASY'),
       (5, 'Ocean', '/puzzles/ocean/ocean.png', 'MEDIUM'),
       (6, 'Ocean', '/puzzles/ocean/ocean.png', 'HARD');

-----------------------------------------------------------------------
-- 2. Заполнение таблицы puzzle_pieces для пазла "Avatar" (EASY: 4×4)
-----------------------------------------------------------------------
INSERT INTO puzzle_pieces (piece_number, correct_x, correct_y, current_x, current_y, is_placed_correctly, image_url,
                           puzzles_id)
SELECT (r1.X * 4 + r2.X)                                          AS piece_number,
       r1.X                                                       AS correct_x,
       r2.X                                                       AS correct_y,
       r1.X                                                       AS current_x,
       r2.X                                                       AS current_y,
       FALSE                                                      AS is_placed_correctly,
       '/puzzles/avatar/big/avatar-' || r1.X || ',' || r2.X || '.png' AS image_url,
       1                                                          AS puzzles_id
FROM SYSTEM_RANGE(0, 3) r1,
     SYSTEM_RANGE(0, 3) r2;

------------------------------------------------------------------------
-- 3. Заполнение таблицы puzzle_pieces для пазла "Avatar" (MEDIUM: 8×8)
------------------------------------------------------------------------
INSERT INTO puzzle_pieces (piece_number, correct_x, correct_y, current_x, current_y, is_placed_correctly, image_url,
                           puzzles_id)
SELECT (r1.X * 8 + r2.X) AS piece_number,
       r1.X              AS correct_x,
       r2.X              AS correct_y,
       r1.X              AS current_x,
       r2.X              AS current_y,
       FALSE,
       '/puzzles/avatar/medium/avatar-' || r1.X || ',' || r2.X || '.png',
       2
FROM SYSTEM_RANGE(0, 7) r1,
     SYSTEM_RANGE(0, 7) r2;

------------------------------------------------------------------------
-- 4. Заполнение таблицы puzzle_pieces для пазла "Avatar" (HARD: 12×12)
------------------------------------------------------------------------
INSERT INTO puzzle_pieces (piece_number, correct_x, correct_y, current_x, current_y, is_placed_correctly, image_url,
                           puzzles_id)
SELECT (r1.X * 12 + r2.X) AS piece_number,
       r1.X               AS correct_x,
       r2.X               AS correct_y,
       r1.X               AS current_x,
       r2.X               AS current_y,
       FALSE,
       '/puzzles/avatar/small/avatar-' || r1.X || ',' || r2.X || '.png',
       3
FROM SYSTEM_RANGE(0, 11) r1,
     SYSTEM_RANGE(0, 11) r2;

-----------------------------------------------------------------------
-- 5. Заполнение таблицы puzzle_pieces для пазла "Ocean" (EASY: 4×4)
-----------------------------------------------------------------------
INSERT INTO puzzle_pieces (piece_number, correct_x, correct_y, current_x, current_y, is_placed_correctly, image_url,
                           puzzles_id)
SELECT (r1.X * 4 + r2.X) AS piece_number,
       r1.X,
       r2.X,
       r1.X,
       r2.X,
       FALSE,
       '/puzzles/ocean/big/ocean-' || r1.X || ',' || r2.X || '.png',
       4
FROM SYSTEM_RANGE(0, 3) r1,
     SYSTEM_RANGE(0, 3) r2;

------------------------------------------------------------------------
-- 6. Заполнение таблицы puzzle_pieces для пазла "Ocean" (MEDIUM: 8×8)
------------------------------------------------------------------------
INSERT INTO puzzle_pieces (piece_number, correct_x, correct_y, current_x, current_y, is_placed_correctly, image_url,
                           puzzles_id)
SELECT (r1.X * 8 + r2.X) AS piece_number,
       r1.X,
       r2.X,
       r1.X,
       r2.X,
       FALSE,
       '/puzzles/ocean/medium/ocean-' || r1.X || ',' || r2.X || '.png',
       5
FROM SYSTEM_RANGE(0, 7) r1,
     SYSTEM_RANGE(0, 7) r2;

------------------------------------------------------------------------
-- 7. Заполнение таблицы puzzle_pieces для пазла "Ocean" (HARD: 12×12)
------------------------------------------------------------------------
INSERT INTO puzzle_pieces (piece_number, correct_x, correct_y, current_x, current_y, is_placed_correctly, image_url,
                           puzzles_id)
SELECT (r1.X * 12 + r2.X) AS piece_number,
       r1.X,
       r2.X,
       r1.X,
       r2.X,
       FALSE,
       '/puzzles/ocean/small/ocean-' || r1.X || ',' || r2.X || '.png',
       6
FROM SYSTEM_RANGE(0, 11) r1,
     SYSTEM_RANGE(0, 11) r2;
```

### resources\graphql\player.graphqls
```
type Query {
    players: [Player]
    player(id: ID!): Player
    authenticatePlayer(player: PlayerInput!): Int
}

type Mutation {
    newPlayer(player: PlayerInput!): Player
}

type Player {
    id: ID!
    username: String!
    password: String!  # Пароль хранится в виде хэша (BCrypt)
    gameSessions: [GameSession]
}

input PlayerInput {
    id: ID
    username: String!
    password: String!
}
```

### resources\graphql\puzzle.graphqls
```
extend type Query {
    puzzles: [Puzzle]
    puzzle(id: ID!): Puzzle
}

type Puzzle {
    id: ID!
    imageUrl: String!
    difficulty: Difficulty!
    name: String
    pieces: [PuzzlePiece!]!
}
```

### resources\graphql\puzzlePiece.graphqls
```
extend type Query {
    puzzlePieces(gameSessionId: ID!): [PuzzlePiece]
}

extend type Mutation {
    updatePiecePosition(pieceId: ID!, newPosition: CoordinatesInput!): PuzzlePiece
}

type PuzzlePiece {
    id: ID!
    pieceNumber: Int!
    correctPosition: Coordinates!
    currentPosition: Coordinates!
    isPlacedCorrectly: Boolean!
    puzzle: Puzzle
}

type Coordinates {
    x: Int!
    y: Int!
}

input CoordinatesInput {
    x: Int!
    y: Int!
}
```

### resources\puzzles\avatar\avatar.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-0,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-0,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-0,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-0,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-1,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-1,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-1,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-1,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-2,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-2,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-2,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-2,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-3,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-3,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-3,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\big\avatar-3,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-0,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-1,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-2,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-3,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-4,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-5,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-6,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\medium\avatar-7,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-0,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-1,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-10,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-11,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-2,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-3,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-4,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-5,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-6,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-7,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-8,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\avatar\small\avatar-9,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-0,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-0,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-0,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-0,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-1,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-1,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-1,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-1,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-2,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-2,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-2,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-2,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-3,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-3,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-3,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\big\ocean-3,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-0,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-1,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-2,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-3,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-4,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-5,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-6,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\medium\ocean-7,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\ocean.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-0,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-1,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-10,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-11,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-2,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-3,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-4,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-5,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-6,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-7,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-8,9.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,0.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,1.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,10.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,11.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,2.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,3.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,4.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,5.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,6.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,7.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,8.png
```
Ошибка чтения файла: Input length = 1
```

### resources\puzzles\ocean\small\ocean-9,9.png
```
Ошибка чтения файла: Input length = 1
```

### webapp\WEB-INF\views\game.jsp
```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Игровой режим – Собери пазл</title>

    <!-- Задаём размер поля в зависимости от сложности -->
    <c:choose>
        <c:when test="${gameSession.puzzle.difficulty == 'EASY'}">
            <c:set var="gridSize" value="4"/>
        </c:when>
        <c:when test="${gameSession.puzzle.difficulty == 'MEDIUM'}">
            <c:set var="gridSize" value="8"/>
        </c:when>
        <c:otherwise>
            <c:set var="gridSize" value="12"/>
        </c:otherwise>
    </c:choose>

    <!-- Задаём лимит времени (в секундах) в зависимости от сложности -->
    <c:choose>
        <c:when test="${gameSession.puzzle.difficulty == 'EASY'}">
            <c:set var="timeLimit" value="480"/>
        </c:when>
        <c:when test="${gameSession.puzzle.difficulty == 'MEDIUM'}">
            <c:set var="timeLimit" value="900"/>
        </c:when>
        <c:otherwise>
            <c:set var="timeLimit" value="1200"/>
        </c:otherwise>
    </c:choose>

    <!-- Подсчёт правильно размещённых фрагментов -->
    <c:set var="placedCount" value="0"/>
    <c:forEach var="p" items="${gameSession.pieces}">
        <c:if test="${p.placedCorrectly}">
            <c:set var="placedCount" value="${placedCount + 1}"/>
        </c:if>
    </c:forEach>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        h1 {
            text-align: center;
        }
        .container {
            display: flex;
            gap: 40px;
            justify-content: center;
        }
        .board {
            width: 500px;
            height: 500px;
            border: 2px solid #666;
            display: grid;
            grid-template-columns: repeat(${gridSize}, 1fr);
            grid-template-rows: repeat(${gridSize}, 1fr);
        }
        #targetBoard {
            border-color: #ccc;
        }
        #sourceBoard {
            border-color: #666;
        }
        .cell {
            border: 1px dashed #aaa;
            position: relative;
            overflow: hidden;
        }
        .piece {
            width: 100%;
            height: 100%;
            object-fit: cover;
            cursor: grab;
        }
    </style>
</head>
<body>

<h1>Собери пазл: ${gameSession.puzzle.name}</h1>
<p style="text-align: center;">Сложность: ${gameSession.puzzle.difficulty}</p>
<!-- Добавляем отображение таймера -->
<p id="timer" style="text-align: center;">Оставшееся время: <span id="timeDisplay"></span></p>

<div class="container">
    <div id="targetBoard" class="board">
        <c:forEach var="x" begin="0" end="${gridSize - 1}">
            <c:forEach var="y" begin="0" end="${gridSize - 1}">
                <div class="cell" id="target-${y}-${x}">
                    <c:forEach var="p" items="${gameSession.pieces}">
                        <c:if test="${p.placedCorrectly and p.correctPosition.x == x and p.correctPosition.y == y}">
                            <img id="piece-${p.id}" src="${pageContext.request.contextPath}/images/piece/${p.id}" class="piece" draggable="false"/>
                        </c:if>
                    </c:forEach>
                </div>
            </c:forEach>
        </c:forEach>
    </div>

    <div id="sourceBoard" class="board">
        <c:forEach var="x" begin="0" end="${gridSize - 1}">
            <c:forEach var="y" begin="0" end="${gridSize - 1}">
                <div class="cell" id="source-${x}-${y}">
                    <c:forEach var="p" items="${gameSession.pieces}">
                        <c:if test="${!p.placedCorrectly and p.currentPosition.x == x and p.currentPosition.y == y}">
                            <img id="piece-${p.id}" src="${pageContext.request.contextPath}/images/piece/${p.id}" class="piece" draggable="true"/>
                        </c:if>
                    </c:forEach>
                </div>
            </c:forEach>
        </c:forEach>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const ctx = '${pageContext.request.contextPath}';
        const gridSize = Number('${gridSize}');
        const totalPieces = gridSize * gridSize;
        let placedCount = Number('${placedCount}');
        let draggedId = null;

        // Таймер: получаем лимит времени из JSP (в секундах)
        let timeRemaining = Number('${timeLimit}');
        const timerDisplay = document.getElementById('timeDisplay');

        // Функция обновления таймера
        function updateTimer() {
            const minutes = Math.floor(timeRemaining / 60);
            const seconds = timeRemaining % 60;
            timerDisplay.textContent = minutes.toString().padStart(2, '0') + ':' + seconds.toString().padStart(2, '0');
            if (timeRemaining <= 0) {
                clearInterval(timerInterval);
                alert('Время вышло');
                redirectHome('timeout');
            }
            timeRemaining--;
        }
        // Первоначальное обновление и запуск отсчёта каждую секунду
        updateTimer();
        const timerInterval = setInterval(updateTimer, 1000);

        // Обработка перетаскивания фрагментов пазла
        document.querySelectorAll('.piece').forEach(img => {
            img.addEventListener('dragstart', ev => {
                draggedId = ev.target.id.replace('piece-', '');
                ev.dataTransfer.setData('text/plain', draggedId);
            });
        });

        document.querySelectorAll('#targetBoard .cell').forEach(cell => {
            cell.addEventListener('dragover', e => e.preventDefault());
            cell.addEventListener('drop', e => {
                e.preventDefault();
                if (!draggedId) return;
                const [ , x, y ] = cell.id.split('-');
                updatePosition(draggedId, x, y)
                    .then(ok => {
                        if (ok) {
                            const img = document.getElementById('piece-' + draggedId);
                            img.draggable = false;
                            cell.appendChild(img);
                            placedCount++;
                            if (placedCount === totalPieces) {
                                redirectHome('success');
                            }
                        } else {
                            alert('Неверная ячейка. Попробуйте снова.');
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        alert('Ошибка сервера. Попробуйте ещё раз.');
                    });
            });
        });

        // Функция обновления позиции фрагмента через GraphQL
        function updatePosition(id, x, y) {
            const query = `
        mutation UpdatePos($id: ID!, $x: Int!, $y: Int!){
          updatePiecePosition(pieceId:$id, newPosition:{x:$x, y:$y}){
            isPlacedCorrectly
          }
        }`;
            const variables = {
                id: parseInt(id, 10),
                x: parseInt(x, 10),
                y: parseInt(y, 10)
            };
            return fetch(ctx + '/graphql', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query, variables })
            })
                .then(r => r.json())
                .then(res => {
                    if (res.errors) { throw res.errors[0]; }
                    return res.data.updatePiecePosition.isPlacedCorrectly;
                });
        }

        // Функция редиректа на страницу home с сообщением
        function redirectHome(msg) {
            window.location.href = ctx + '/home?msg=' + msg;
        }
    });
</script>
</body>
</html>
```

### webapp\WEB-INF\views\home.jsp
```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Галерея пазлов</title>
    <style>
        /* Базовая стилизация галереи */
        .gallery {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
        }
        .card {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 10px;
            width: 200px;
            box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.1);
            transition: transform 0.2s;
            text-align: center;
        }
        .card:hover {
            transform: scale(1.03);
        }
        .card img {
            width: 100%;
            height: auto;
            border-bottom: 1px solid #ddd;
            margin-bottom: 10px;
        }
        .card .card-title {
            font-weight: bold;
            margin: 5px 0;
        }
        .card .card-difficulty {
            color: #888;
            margin: 5px 0;
        }
    </style>
</head>
<body>
<h1>Галерея пазлов</h1>
<c:choose>
    <c:when test="${param.msg == 'success'}">
        <div style="padding:10px;border:1px solid #4caf50;background:#e8f5e9;color:#2e7d32;margin-bottom:15px;">
            ✅ Поздравляем! Пазл полностью собран.
        </div>
    </c:when>
    <c:when test="${param.msg == 'timeout'}">
        <div style="padding:10px;border:1px solid #ff9800;background:#fff3e0;color:#e65100;margin-bottom:15px;">
            ⏰ Время вышло. Попробуйте снова!
        </div>
    </c:when>
</c:choose>
<!-- Контейнер для карточек пазлов -->
<div class="gallery">
    <!-- Перебираем коллекцию пазлов, переданную в модель под именем puzzles -->
    <c:forEach var="puzzle" items="${puzzles}">
        <div class="card">
            <!-- Картинка пазла получаем через REST API: /images/full/{puzzle.id} -->
            <a href="${pageContext.request.contextPath}/puzzleDetail?pid=${puzzle.id}">
                <img src="${pageContext.request.contextPath}/images/full/${puzzle.id}" alt="${puzzle.name}">
            </a>
            <!-- Название пазла -->
            <div class="card-title">${puzzle.name}</div>
            <!-- Уровень сложности -->
            <div class="card-difficulty">Сложность: ${puzzle.difficulty}</div>
        </div>
    </c:forEach>
</div>

<!-- Ссылка для выхода (или навигация) -->
<p><a href="${pageContext.request.contextPath}/logout">Выйти</a></p>
</body>
</html>
```

### webapp\WEB-INF\views\login.jsp
```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Логин</title>
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            document.getElementById("loginForm").addEventListener("submit", function (e) {
                e.preventDefault();
                var username = document.getElementById("username").value;
                var password = document.getElementById("password").value;
                // Формируем GraphQL запрос для аутентификации пользователя
                var query = `
            query {
              authenticatePlayer(player: {username: "${username}", password: "${password}"})
            }
          `;
                fetch("${pageContext.request.contextPath}/graphql", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({query: query})
                })
                    .then(function (response) {
                        return response.json();
                    })
                    .then(function (data) {
                        console.log("Результат аутентификации", data);
                        if (data.data && data.data.authenticatePlayer) {
                            // Сохраняем идентификатор пользователя в sessionStorage
                            var playerId = data.data.authenticatePlayer;
                            sessionStorage.setItem("playerId", playerId);
                            // Перенаправляем на главную страницу
                            window.location.href = "${pageContext.request.contextPath}/home";
                        } else {
                            alert("Неверный email или пароль.");
                        }
                    })
                    .catch(function (error) {
                        console.error("Ошибка аутентификации", error);
                        alert("Ошибка аутентификации. Повторите попытку.");
                    });
            });
        });
    </script>
</head>
<body>
<h2>Логин</h2>
<form id="loginForm">
    <label for="username">Email:</label>
    <input type="email" id="username" name="username" required/><br/><br/>

    <label for="password">Пароль:</label>
    <input type="password" id="password" name="password" required/><br/><br/>

    <input type="submit" value="Войти"/>
</form>
<br/>
<p>Еще не зарегистрированы? <a href="${pageContext.request.contextPath}/register">Регистрация</a></p>
</body>
</html>
```

### webapp\WEB-INF\views\registration.jsp
```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Регистрация</title>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            document.getElementById("regForm").addEventListener("submit", function(e) {
                e.preventDefault();
                var username = document.getElementById("username").value;
                var password = document.getElementById("password").value;
                // Формируем GraphQL мутацию для создания нового пользователя
                var query = `
            mutation {
              newPlayer(player: {username: "${username}", password: "${password}"}) {
                id
                username
              }
            }
          `;
                fetch("${pageContext.request.contextPath}/graphql", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ query: query })
                })
                    .then(function(response) { return response.json(); })
                    .then(function(data) {
                        console.log("Регистрация успешно выполнена", data);
                        // Перенаправляем на страницу логина после регистрации
                        window.location.href = "${pageContext.request.contextPath}/login";
                    })
                    .catch(function(error) {
                        console.error("Ошибка регистрации", error);
                        alert("Ошибка регистрации. Повторите попытку.");
                    });
            });
        });
    </script>
</head>
<body>
<h2>Регистрация нового пользователя</h2>
<form id="regForm">
    <label for="username">Email:</label>
    <input type="email" id="username" name="username" required/><br/><br/>

    <label for="password">Пароль:</label>
    <input type="password" id="password" name="password" required/><br/><br/>

    <input type="submit" value="Зарегистрироваться"/>
</form>
<br/>
<p>Уже зарегистрированы? <a href="${pageContext.request.contextPath}/login">Войти</a></p>
</body>
</html>
```

