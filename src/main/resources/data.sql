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
