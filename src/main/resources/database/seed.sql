-- ============================================================
-- Quiz Management System - Seed Data
-- ============================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------
-- Users
-- -------------------------------------------------------
INSERT OR IGNORE INTO users (id, name, email, password, role) VALUES
    (1, 'Dr. Smith', 'teacher@quiz.com',  'teacher123', 'TEACHER'),
    (2, 'Alice',     'student1@quiz.com', 'student123', 'STUDENT'),
    (3, 'Bob',       'student2@quiz.com', 'student123', 'STUDENT'),
    (4, 'Carol',     'student3@quiz.com', 'student123', 'STUDENT');

-- -------------------------------------------------------
-- Categories
-- -------------------------------------------------------
INSERT OR IGNORE INTO categories (id, name, description) VALUES
    (1, 'Programming',           'Core programming concepts and languages'),
    (2, 'Database',              'Relational and non-relational database systems'),
    (3, 'Operating Systems',     'Process management, memory, and OS fundamentals'),
    (4, 'Software Engineering',  'SDLC, design patterns, and best practices'),
    (5, 'Mathematics',           'Discrete math, calculus, and algorithms');

-- -------------------------------------------------------
-- Questions
-- -------------------------------------------------------

-- Programming MCQ
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (1,  'Which keyword is used to define a class in Java?',
         'MCQ', 1.0, 'EASY', 1, 1),
    (2,  'What is the time complexity of binary search?',
         'MCQ', 2.0, 'MEDIUM', 1, 1),
    (3,  'Which of the following is NOT an object-oriented programming language?',
         'MCQ', 1.0, 'EASY', 1, 1);

-- Programming TRUE_FALSE
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (4,  'Java supports multiple inheritance through classes.',
         'TRUE_FALSE', 1.0, 'EASY', 1, 1),
    (5,  'A constructor can have a return type.',
         'TRUE_FALSE', 1.0, 'EASY', 1, 1);

-- Programming FILL_BLANK
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (6,  'The ______ keyword in Java is used to prevent method overriding.',
         'FILL_BLANK', 1.5, 'MEDIUM', 1, 1);

-- Database MCQ
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (7,  'Which SQL clause is used to filter rows after aggregation?',
         'MCQ', 1.0, 'MEDIUM', 2, 1),
    (8,  'Which normal form eliminates transitive dependencies?',
         'MCQ', 2.0, 'HARD', 2, 1);

-- Database TRUE_FALSE
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (9,  'A primary key can contain NULL values.',
         'TRUE_FALSE', 1.0, 'EASY', 2, 1);

-- Database MULTIPLE_ANSWER
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (10, 'Which of the following are valid SQL JOIN types?',
         'MULTIPLE_ANSWER', 2.0, 'MEDIUM', 2, 1);

-- OS MCQ
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (11, 'Which scheduling algorithm gives the shortest average waiting time?',
         'MCQ', 2.0, 'MEDIUM', 3, 1),
    (12, 'What is a deadlock?',
         'MCQ', 1.0, 'EASY', 3, 1);

-- OS TRUE_FALSE
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (13, 'Semaphores can be used to solve the critical section problem.',
         'TRUE_FALSE', 1.0, 'MEDIUM', 3, 1);

-- Software Engineering MCQ
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (14, 'Which design pattern ensures only one instance of a class exists?',
         'MCQ', 1.0, 'MEDIUM', 4, 1);

-- Software Engineering FILL_BLANK
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (15, 'The ______ principle states that a class should have only one reason to change.',
         'FILL_BLANK', 2.0, 'HARD', 4, 1);

-- -------------------------------------------------------
-- Question Options
-- -------------------------------------------------------

-- Q1: Which keyword is used to define a class in Java?
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (1,  1, 'class',     1, 1),
    (2,  1, 'struct',    0, 2),
    (3,  1, 'define',    0, 3),
    (4,  1, 'object',    0, 4);

-- Q2: Time complexity of binary search
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (5,  2, 'O(n)',      0, 1),
    (6,  2, 'O(log n)',  1, 2),
    (7,  2, 'O(n^2)',    0, 3),
    (8,  2, 'O(1)',      0, 4);

-- Q3: NOT an OOP language
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (9,  3, 'Java',   0, 1),
    (10, 3, 'Python', 0, 2),
    (11, 3, 'C',      1, 3),
    (12, 3, 'C++',    0, 4);

-- Q4: Java multiple inheritance through classes (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (13, 4, 'True',  0, 1),
    (14, 4, 'False', 1, 2);

-- Q5: Constructor has a return type (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (15, 5, 'True',  0, 1),
    (16, 5, 'False', 1, 2);

-- Q6: FILL_BLANK - final keyword (no options needed; answer stored in answer_value)

-- Q7: SQL clause after aggregation
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (17, 7, 'WHERE',    0, 1),
    (18, 7, 'HAVING',   1, 2),
    (19, 7, 'GROUP BY', 0, 3),
    (20, 7, 'ORDER BY', 0, 4);

-- Q8: Normal form eliminating transitive dependencies
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (21, 8, '1NF', 0, 1),
    (22, 8, '2NF', 0, 2),
    (23, 8, '3NF', 1, 3),
    (24, 8, 'BCNF', 0, 4);

-- Q9: Primary key can be NULL (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (25, 9, 'True',  0, 1),
    (26, 9, 'False', 1, 2);

-- Q10: Valid SQL JOIN types (MULTIPLE_ANSWER)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (27, 10, 'INNER JOIN', 1, 1),
    (28, 10, 'LEFT JOIN',  1, 2),
    (29, 10, 'DIAGONAL JOIN', 0, 3),
    (30, 10, 'CROSS JOIN', 1, 4),
    (31, 10, 'FULL OUTER JOIN', 1, 5);

-- Q11: Shortest average waiting time scheduling
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (32, 11, 'FCFS',  0, 1),
    (33, 11, 'SJF',   1, 2),
    (34, 11, 'RR',    0, 3),
    (35, 11, 'LIFO',  0, 4);

-- Q12: What is a deadlock?
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (36, 12, 'A process waiting indefinitely for resources held by others', 1, 1),
    (37, 12, 'A memory leak in a program',                                  0, 2),
    (38, 12, 'A CPU scheduling algorithm',                                  0, 3),
    (39, 12, 'An infinite loop in code',                                    0, 4);

-- Q13: Semaphores and critical section (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (40, 13, 'True',  1, 1),
    (41, 13, 'False', 0, 2);

-- Q14: Singleton design pattern
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (42, 14, 'Factory',   0, 1),
    (43, 14, 'Singleton', 1, 2),
    (44, 14, 'Observer',  0, 3),
    (45, 14, 'Decorator', 0, 4);

-- Q15: FILL_BLANK - Single Responsibility Principle (no options)

-- -------------------------------------------------------
-- Quizzes
-- -------------------------------------------------------
INSERT OR IGNORE INTO quizzes
    (id, title, description, category_id, difficulty, time_limit_minutes, max_attempts, scoring_strategy, status, created_by, shuffle_questions)
VALUES
    (1, 'Java Basics Quiz',
        'A beginner-level quiz covering core Java fundamentals.',
        1, 'EASY', 30, 3, 'STANDARD', 'DRAFT', 1, 0),

    (2, 'Database Fundamentals',
        'Test your knowledge of SQL and relational database concepts.',
        2, 'MEDIUM', 45, 2, 'STANDARD', 'PUBLISHED', 1, 1),

    (3, 'Operating Systems Deep Dive',
        'Advanced topics in OS including scheduling and synchronisation.',
        3, 'HARD', 60, 1, 'STANDARD', 'ARCHIVED', 1, 0);

-- -------------------------------------------------------
-- Quiz Questions (link questions to quizzes)
-- -------------------------------------------------------

-- Quiz 2 (Database Fundamentals) - questions 7, 8, 9, 10
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (2, 7,  1),
    (2, 8,  2),
    (2, 9,  3),
    (2, 10, 4);

-- Quiz 1 (Java Basics) - questions 1, 2, 3, 4, 5
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (1, 1, 1),
    (1, 2, 2),
    (1, 3, 3),
    (1, 4, 4),
    (1, 5, 5);

-- Quiz 3 (OS Deep Dive) - questions 11, 12, 13
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (3, 11, 1),
    (3, 12, 2),
    (3, 13, 3);
