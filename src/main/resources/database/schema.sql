-- ============================================================
-- Quiz Management System - Database Schema
-- SQLite with foreign key enforcement
-- ============================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------
-- Table: users
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL,
    email      TEXT    UNIQUE NOT NULL,
    password   TEXT    NOT NULL,
    role       TEXT    NOT NULL CHECK (role IN ('TEACHER', 'STUDENT')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- Table: categories
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT UNIQUE NOT NULL,
    description TEXT
);

-- -------------------------------------------------------
-- Table: quizzes
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS quizzes (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    title               TEXT    NOT NULL,
    description         TEXT,
    category_id         INTEGER REFERENCES categories (id) ON DELETE SET NULL,
    difficulty          TEXT    CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    time_limit_minutes  INTEGER,
    max_attempts        INTEGER DEFAULT 0,
    scoring_strategy    TEXT    DEFAULT 'STANDARD',
    status              TEXT    DEFAULT 'DRAFT'
                                CHECK (status IN ('DRAFT', 'PUBLISHED', 'ACTIVE', 'COMPLETED', 'ARCHIVED')),
    created_by          INTEGER REFERENCES users (id) ON DELETE SET NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    shuffle_questions   INTEGER DEFAULT 0
);

-- -------------------------------------------------------
-- Table: questions
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS questions (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    question_text TEXT    NOT NULL,
    question_type TEXT    NOT NULL
                          CHECK (question_type IN ('MCQ', 'TRUE_FALSE', 'FILL_BLANK', 'MULTIPLE_ANSWER')),
    marks         REAL    NOT NULL DEFAULT 1.0,
    difficulty    TEXT    CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    category_id   INTEGER REFERENCES categories (id) ON DELETE SET NULL,
    created_by    INTEGER REFERENCES users (id) ON DELETE SET NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- Table: question_options
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS question_options (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id  INTEGER NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    option_text  TEXT    NOT NULL,
    is_correct   INTEGER DEFAULT 0,
    option_order INTEGER
);

-- -------------------------------------------------------
-- Table: quiz_questions
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS quiz_questions (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id        INTEGER NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    question_id    INTEGER NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    question_order INTEGER NOT NULL,
    UNIQUE (quiz_id, question_id)
);

-- -------------------------------------------------------
-- Table: quiz_attempts
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id     INTEGER NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    student_id  INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    start_time  DATETIME,
    end_time    DATETIME,
    status      TEXT    DEFAULT 'IN_PROGRESS'
                        CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT')),
    score       REAL    DEFAULT 0,
    total_marks REAL    DEFAULT 0
);

-- -------------------------------------------------------
-- Table: answers
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS answers (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    attempt_id    INTEGER NOT NULL REFERENCES quiz_attempts (id) ON DELETE CASCADE,
    question_id   INTEGER NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    answer_value  TEXT,
    is_correct    INTEGER DEFAULT 0,
    marks_awarded REAL    DEFAULT 0
);

-- -------------------------------------------------------
-- Indexes
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_quizzes_status       ON quizzes (status);
CREATE INDEX IF NOT EXISTS idx_quizzes_category     ON quizzes (category_id);
CREATE INDEX IF NOT EXISTS idx_quizzes_created_by   ON quizzes (created_by);

CREATE INDEX IF NOT EXISTS idx_questions_type       ON questions (question_type);
CREATE INDEX IF NOT EXISTS idx_questions_category   ON questions (category_id);
CREATE INDEX IF NOT EXISTS idx_questions_created_by ON questions (created_by);

CREATE INDEX IF NOT EXISTS idx_options_question     ON question_options (question_id);

CREATE INDEX IF NOT EXISTS idx_qq_quiz              ON quiz_questions (quiz_id);
CREATE INDEX IF NOT EXISTS idx_qq_question          ON quiz_questions (question_id);

CREATE INDEX IF NOT EXISTS idx_attempts_quiz        ON quiz_attempts (quiz_id);
CREATE INDEX IF NOT EXISTS idx_attempts_student     ON quiz_attempts (student_id);
CREATE INDEX IF NOT EXISTS idx_attempts_status      ON quiz_attempts (status);

CREATE INDEX IF NOT EXISTS idx_answers_attempt      ON answers (attempt_id);
CREATE INDEX IF NOT EXISTS idx_answers_question     ON answers (question_id);
