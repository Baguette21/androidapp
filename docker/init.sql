-- ============================================================================
-- ECTrivia Database Schema
-- ============================================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ectrivia_db;
USE ectrivia_db;

-- Categories (premade question themes)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rooms (game sessions)
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_code VARCHAR(6) NOT NULL UNIQUE,
    host_player_id BIGINT NULL,
    category_id BIGINT NULL,
    status ENUM('LOBBY', 'IN_PROGRESS', 'FINISHED', 'CANCELLED') DEFAULT 'LOBBY',
    is_theme_based BOOLEAN DEFAULT FALSE,
    question_timer_seconds INT DEFAULT 15,
    max_players INT DEFAULT 100,
    current_question_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Players (anonymous, per-room)
CREATE TABLE IF NOT EXISTS players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    is_host BOOLEAN DEFAULT FALSE,
    is_proxy_host BOOLEAN DEFAULT FALSE,
    join_order INT NOT NULL,
    total_score INT DEFAULT 0,
    current_streak INT DEFAULT 0,
    is_connected BOOLEAN DEFAULT TRUE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE KEY unique_nickname_per_room (room_id, nickname)
);

-- Add host foreign key after players table exists
ALTER TABLE rooms ADD FOREIGN KEY (host_player_id) REFERENCES players(id);

-- Questions (per-room custom OR from category)
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NULL,
    category_id BIGINT NULL,
    question_text VARCHAR(500) NOT NULL,
    question_order INT NOT NULL,
    correct_answer_index INT NOT NULL,
    timer_seconds INT DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Answer choices (4 per question)
CREATE TABLE IF NOT EXISTS answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    answer_text VARCHAR(255) NOT NULL,
    answer_index INT NOT NULL,
    
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Player answers (for scoring and analytics)
CREATE TABLE IF NOT EXISTS player_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_answer_index INT NULL,
    is_correct BOOLEAN NULL,
    answer_time_ms INT NULL,
    points_earned INT DEFAULT 0,
    streak_at_time INT DEFAULT 0,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY one_answer_per_question (player_id, question_id)
);

-- ============================================================================
-- Indexes for Performance
-- ============================================================================

CREATE INDEX idx_rooms_code ON rooms(room_code);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_players_room ON players(room_id);
CREATE INDEX idx_players_room_order ON players(room_id, join_order);
CREATE INDEX idx_questions_room ON questions(room_id);
CREATE INDEX idx_questions_category ON questions(category_id);
CREATE INDEX idx_questions_order ON questions(room_id, question_order);
CREATE INDEX idx_answers_question ON answers(question_id);
CREATE INDEX idx_player_answers_player ON player_answers(player_id);
CREATE INDEX idx_player_answers_question ON player_answers(question_id);

-- ============================================================================
-- Seed Data: Categories
-- ============================================================================

INSERT INTO categories (name, description) VALUES
('Pop Culture', 'Movies, TV shows, celebrities, and entertainment'),
('Science and Nature', 'Physics, chemistry, biology, and the natural world'),
('Sports', 'Athletic competitions, teams, and famous athletes'),
('Video Games', 'Gaming history, characters, and franchises'),
('History', 'World events, historical figures, and civilizations'),
('Geography', 'Countries, capitals, landmarks, and world facts');

-- ============================================================================
-- Seed Data: Sample Questions for Pop Culture
-- ============================================================================

INSERT INTO questions (category_id, question_text, question_order, correct_answer_index, timer_seconds) VALUES
(1, 'Which movie won the Academy Award for Best Picture in 2020?', 1, 1, 15),
(1, 'Who played Iron Man in the Marvel Cinematic Universe?', 2, 2, 15),
(1, 'Which TV series features a chemistry teacher turned drug lord?', 3, 0, 15),
(1, 'What is the name of the fictional continent in Game of Thrones?', 4, 3, 15),
(1, 'Which artist released the album "Thriller"?', 5, 1, 15);

-- Pop Culture Answers
INSERT INTO answers (question_id, answer_text, answer_index) VALUES
(1, '1917', 0), (1, 'Parasite', 1), (1, 'Joker', 2), (1, 'Once Upon a Time in Hollywood', 3),
(2, 'Chris Evans', 0), (2, 'Chris Hemsworth', 1), (2, 'Robert Downey Jr.', 2), (2, 'Mark Ruffalo', 3),
(3, 'Breaking Bad', 0), (3, 'Better Call Saul', 1), (3, 'Ozark', 2), (3, 'Narcos', 3),
(4, 'Middle-earth', 0), (4, 'Narnia', 1), (4, 'Azeroth', 2), (4, 'Westeros', 3),
(5, 'Prince', 0), (5, 'Michael Jackson', 1), (5, 'Whitney Houston', 2), (5, 'Madonna', 3);

-- ============================================================================
-- Seed Data: Sample Questions for Science and Nature
-- ============================================================================

INSERT INTO questions (category_id, question_text, question_order, correct_answer_index, timer_seconds) VALUES
(2, 'What is the chemical symbol for gold?', 1, 2, 15),
(2, 'How many planets are in our solar system?', 2, 1, 15),
(2, 'What is the largest organ in the human body?', 3, 0, 15),
(2, 'What gas do plants absorb from the atmosphere?', 4, 2, 15),
(2, 'What is the speed of light in vacuum (approximately)?', 5, 3, 15);

-- Science and Nature Answers
INSERT INTO answers (question_id, answer_text, answer_index) VALUES
(6, 'Ag', 0), (6, 'Go', 1), (6, 'Au', 2), (6, 'Gd', 3),
(7, '7', 0), (7, '8', 1), (7, '9', 2), (7, '10', 3),
(8, 'Skin', 0), (8, 'Liver', 1), (8, 'Heart', 2), (8, 'Brain', 3),
(9, 'Oxygen', 0), (9, 'Nitrogen', 1), (9, 'Carbon Dioxide', 2), (9, 'Hydrogen', 3),
(10, '300 km/s', 0), (10, '3,000 km/s', 1), (10, '30,000 km/s', 2), (10, '300,000 km/s', 3);

-- ============================================================================
-- Seed Data: Sample Questions for Sports
-- ============================================================================

INSERT INTO questions (category_id, question_text, question_order, correct_answer_index, timer_seconds) VALUES
(3, 'Which country has won the most FIFA World Cup titles?', 1, 0, 15),
(3, 'In which sport would you perform a slam dunk?', 2, 1, 15),
(3, 'How many players are on a standard soccer team on the field?', 3, 2, 15),
(3, 'Which athlete has won the most Olympic gold medals?', 4, 3, 15),
(3, 'What is the diameter of a basketball hoop in inches?', 5, 1, 15);

-- Sports Answers
INSERT INTO answers (question_id, answer_text, answer_index) VALUES
(11, 'Brazil', 0), (11, 'Germany', 1), (11, 'Italy', 2), (11, 'Argentina', 3),
(12, 'Volleyball', 0), (12, 'Basketball', 1), (12, 'Tennis', 2), (12, 'Handball', 3),
(13, '9', 0), (13, '10', 1), (13, '11', 2), (13, '12', 3),
(14, 'Usain Bolt', 0), (14, 'Carl Lewis', 1), (14, 'Mark Spitz', 2), (14, 'Michael Phelps', 3),
(15, '16 inches', 0), (15, '18 inches', 1), (15, '20 inches', 2), (15, '22 inches', 3);

-- ============================================================================
-- Seed Data: Sample Questions for Video Games
-- ============================================================================

INSERT INTO questions (category_id, question_text, question_order, correct_answer_index, timer_seconds) VALUES
(4, 'What is the name of the main character in The Legend of Zelda?', 1, 1, 15),
(4, 'Which company created Mario?', 2, 0, 15),
(4, 'In Minecraft, what material do you need to mine diamonds?', 3, 2, 15),
(4, 'What year was the original PlayStation released in Japan?', 4, 3, 15),
(4, 'Which game features the quote "The cake is a lie"?', 5, 1, 15);

-- Video Games Answers
INSERT INTO answers (question_id, answer_text, answer_index) VALUES
(16, 'Zelda', 0), (16, 'Link', 1), (16, 'Ganondorf', 2), (16, 'Epona', 3),
(17, 'Nintendo', 0), (17, 'Sega', 1), (17, 'Sony', 2), (17, 'Atari', 3),
(18, 'Gold Pickaxe', 0), (18, 'Stone Pickaxe', 1), (18, 'Iron Pickaxe', 2), (18, 'Wood Pickaxe', 3),
(19, '1991', 0), (19, '1992', 1), (19, '1993', 2), (19, '1994', 3),
(20, 'Half-Life 2', 0), (20, 'Portal', 1), (20, 'Bioshock', 2), (20, 'Team Fortress 2', 3);

-- ============================================================================
-- Seed Data: Sample Questions for History
-- ============================================================================

INSERT INTO questions (category_id, question_text, question_order, correct_answer_index, timer_seconds) VALUES
(5, 'In which year did World War II end?', 1, 2, 15),
(5, 'Who was the first President of the United States?', 2, 0, 15),
(5, 'Which ancient wonder was located in Alexandria, Egypt?', 3, 1, 15),
(5, 'The Berlin Wall fell in which year?', 4, 3, 15),
(5, 'Who wrote the Declaration of Independence?', 5, 2, 15);

-- History Answers
INSERT INTO answers (question_id, answer_text, answer_index) VALUES
(21, '1943', 0), (21, '1944', 1), (21, '1945', 2), (21, '1946', 3),
(22, 'George Washington', 0), (22, 'Thomas Jefferson', 1), (22, 'Abraham Lincoln', 2), (22, 'John Adams', 3),
(23, 'Hanging Gardens', 0), (23, 'Lighthouse of Alexandria', 1), (23, 'Colossus of Rhodes', 2), (23, 'Temple of Artemis', 3),
(24, '1987', 0), (24, '1988', 1), (24, '1990', 2), (24, '1989', 3),
(25, 'Benjamin Franklin', 0), (25, 'John Adams', 1), (25, 'Thomas Jefferson', 2), (25, 'James Madison', 3);

-- ============================================================================
-- Seed Data: Sample Questions for Geography
-- ============================================================================

INSERT INTO questions (category_id, question_text, question_order, correct_answer_index, timer_seconds) VALUES
(6, 'What is the capital of Australia?', 1, 2, 15),
(6, 'Which is the largest ocean on Earth?', 2, 0, 15),
(6, 'Mount Everest is located in which mountain range?', 3, 1, 15),
(6, 'What is the smallest country in the world?', 4, 3, 15),
(6, 'The Amazon River is primarily located in which country?', 5, 0, 15);

-- Geography Answers
INSERT INTO answers (question_id, answer_text, answer_index) VALUES
(26, 'Sydney', 0), (26, 'Melbourne', 1), (26, 'Canberra', 2), (26, 'Perth', 3),
(27, 'Pacific Ocean', 0), (27, 'Atlantic Ocean', 1), (27, 'Indian Ocean', 2), (27, 'Arctic Ocean', 3),
(28, 'Alps', 0), (28, 'Himalayas', 1), (28, 'Andes', 2), (28, 'Rocky Mountains', 3),
(29, 'Monaco', 0), (29, 'San Marino', 1), (29, 'Liechtenstein', 2), (29, 'Vatican City', 3),
(30, 'Brazil', 0), (30, 'Peru', 1), (30, 'Colombia', 2), (30, 'Venezuela', 3);
