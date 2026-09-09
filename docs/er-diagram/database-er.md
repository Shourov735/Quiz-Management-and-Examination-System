# Database Entity-Relationship (ER) Diagram

## Overview

The Quiz Management & Examination System uses **SQLite** as its persistent relational database. The schema is normalized, enforces referential integrity through foreign keys, and contains 8 distinct entities.

---

## ER Diagram (Mermaid)

```mermaid
erDiagram
    USERS ||--o{ QUIZZES : "creates (createdBy)"
    USERS ||--o{ QUESTIONS : "creates (createdBy)"
    USERS ||--o{ QUIZ_ATTEMPTS : "takes (studentId)"
    CATEGORIES ||--o{ QUIZZES : "categorizes (categoryId)"
    CATEGORIES ||--o{ QUESTIONS : "categorizes (categoryId)"
    QUIZZES ||--o{ QUIZ_QUESTIONS : "contains (quizId)"
    QUESTIONS ||--o{ QUIZ_QUESTIONS : "included in (questionId)"
    QUESTIONS ||--o{ QUESTION_OPTIONS : "has (questionId)"
    QUIZZES ||--o{ QUIZ_ATTEMPTS : "attempted in (quizId)"
    QUIZ_ATTEMPTS ||--o{ ANSWERS : "contains (attemptId)"
    QUESTIONS ||--o{ ANSWERS : "answered for (questionId)"

    USERS {
        int id PK
        string name "NOT NULL"
        string email "NOT NULL, UNIQUE"
        string password "NOT NULL"
        string role "CHECK ('TEACHER','STUDENT')"
        datetime created_at
    }

    CATEGORIES {
        int id PK
        string name "NOT NULL, UNIQUE"
        string description
    }

    QUIZZES {
        int id PK
        string title "NOT NULL"
        string description
        int category_id FK
        string difficulty "CHECK ('EASY','MEDIUM','HARD')"
        int time_limit_minutes
        int max_attempts "0 = unlimited"
        string scoring_strategy "DEFAULT 'STANDARD'"
        string status "CHECK ('DRAFT','PUBLISHED','ACTIVE','COMPLETED','ARCHIVED')"
        int created_by FK
        datetime created_at
        datetime updated_at
        int shuffle_questions "0 or 1"
    }

    QUESTIONS {
        int id PK
        string question_text "NOT NULL"
        string question_type "CHECK ('MCQ','TRUE_FALSE','FILL_BLANK','MULTIPLE_ANSWER')"
        double marks "NOT NULL DEFAULT 1.0"
        string difficulty
        int category_id FK
        int created_by FK
        datetime created_at
    }

    QUESTION_OPTIONS {
        int id PK
        int question_id FK
        string option_text "NOT NULL"
        int is_correct "0 or 1"
        int option_order
    }

    QUIZ_QUESTIONS {
        int id PK
        int quiz_id FK
        int question_id FK
        int question_order "NOT NULL"
    }

    QUIZ_ATTEMPTS {
        int id PK
        int quiz_id FK
        int student_id FK
        datetime start_time
        datetime end_time
        string status "CHECK ('IN_PROGRESS','SUBMITTED','TIMED_OUT')"
        double score "DEFAULT 0.0"
        double total_marks "DEFAULT 0.0"
    }

    ANSWERS {
        int id PK
        int attempt_id FK
        int question_id FK
        string answer_value
        int is_correct "0 or 1"
        double marks_awarded
    }
```

---

## Table Relationships & Cardinality

| Relationship | Cardinality | Foreign Key Constraint | Purpose |
|---|---|---|---|
| `users` → `quizzes` | 1 : N | `quizzes.created_by → users.id` | Identifies which teacher authored the examination |
| `users` → `quiz_attempts` | 1 : N | `quiz_attempts.student_id → users.id` | Identifies student taking the examination |
| `categories` → `quizzes` | 1 : N | `quizzes.category_id → categories.id` | Subjects/domains (e.g. Programming, Database) |
| `categories` → `questions` | 1 : N | `questions.category_id → categories.id` | Categorizes standalone questions in question bank |
| `quizzes` × `questions` | M : N | `quiz_questions (quiz_id, question_id)` | Allows question reuse across quizzes with custom order |
| `questions` → `question_options` | 1 : N | `question_options.question_id → questions.id` | Stores choices for MCQ, True/False, Multiple Answer |
| `quizzes` → `quiz_attempts` | 1 : N | `quiz_attempts.quiz_id → quizzes.id` | Tracks individual examination sessions per quiz |
| `quiz_attempts` → `answers` | 1 : N | `answers.attempt_id → quiz_attempts.id` | Stores student responses per attempt |
| `questions` → `answers` | 1 : N | `answers.question_id → questions.id` | Links student response back to question definition |

---

## Key Constraints & Indexes

1. **Foreign Keys**: `PRAGMA foreign_keys = ON` is enabled on every connection session.
2. **Unique Constraints**:
   - `users.email` (prevents duplicate accounts)
   - `categories.name` (unique taxonomy)
   - `quiz_questions(quiz_id, question_id)` (prevents adding same question twice to a quiz)
3. **Check Constraints**:
   - `users.role IN ('TEACHER', 'STUDENT')`
   - `quizzes.status IN ('DRAFT', 'PUBLISHED', 'ACTIVE', 'COMPLETED', 'ARCHIVED')`
   - `quizzes.difficulty IN ('EASY', 'MEDIUM', 'HARD')`
   - `questions.question_type IN ('MCQ', 'TRUE_FALSE', 'FILL_BLANK', 'MULTIPLE_ANSWER')`
   - `quiz_attempts.status IN ('IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT')`
4. **Performance Indexes**:
   - `idx_quizzes_status` on `quizzes(status)`
   - `idx_quizzes_category` on `quizzes(category_id)`
   - `idx_questions_type` on `questions(question_type)`
   - `idx_questions_category` on `questions(category_id)`
   - `idx_attempts_student` on `quiz_attempts(student_id)`
   - `idx_attempts_quiz` on `quiz_attempts(quiz_id)`
   - `idx_answers_attempt` on `answers(attempt_id)`
