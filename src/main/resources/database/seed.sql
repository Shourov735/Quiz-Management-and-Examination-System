-- ============================================================
-- Quiz Management System - Seed Data (Bangladeshi Academic Context)
-- ============================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------
-- Users
-- -------------------------------------------------------
INSERT OR IGNORE INTO users (id, name, email, password, role) VALUES
    (1, 'Prof. Dr. Kazi Sakib', 'teacher@quiz.com',  'teacher123', 'TEACHER'),
    (2, 'Md. Shourov',           'student1@quiz.com', 'student123', 'STUDENT'),
    (3, 'Rokib Hasan',           'student2@quiz.com', 'student123', 'STUDENT'),
    (4, 'Tanvir Ahmed',          'student3@quiz.com', 'student123', 'STUDENT'),
    (5, 'Sadia Islam',           'student4@quiz.com', 'student123', 'STUDENT'),
    (6, 'Naimur Rahman',         'student5@quiz.com', 'student123', 'STUDENT'),
    (7, 'Md. Shourov',           'shourov@quiz.com',  'shourov123', 'STUDENT'),
    (8, 'Rokib Hasan',           'rokib@quiz.com',    'rokib123',   'STUDENT');

-- -------------------------------------------------------
-- Categories
-- -------------------------------------------------------
INSERT OR IGNORE INTO categories (id, name, description) VALUES
    (1, 'Programming',                           'Core Java programming concepts, OOP, and data structures'),
    (2, 'Database Systems',                      'Relational databases, SQLite, normalization, and SQL optimization'),
    (3, 'Operating Systems',                     'Process synchronization, scheduling algorithms, and memory management'),
    (4, 'Software Engineering & Design Patterns', 'GoF design patterns, SOLID principles, and software architecture'),
    (5, 'Information Systems & Networking',       'Networking fundamentals, distributed systems, and web architectures');

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

-- Software Engineering & Design Patterns MCQ
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (14, 'Which design pattern ensures only one instance of a class exists across the application runtime?',
         'MCQ', 1.0, 'MEDIUM', 4, 1);

-- Software Engineering FILL_BLANK
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (15, 'The ______ principle states that a class should have only one reason to change.',
         'FILL_BLANK', 2.0, 'HARD', 4, 1);

-- Bangladeshi Context Questions
INSERT OR IGNORE INTO questions (id, question_text, question_type, marks, difficulty, category_id, created_by) VALUES
    (16, 'In the University of Dhaka IIT Online Portal, multiple payment gateways (bKash, Nagad, Rocket) are supported interchangeably. Which GoF design pattern is best suited for selecting the gateway at runtime?',
         'MCQ', 2.0, 'MEDIUM', 4, 1),
    (17, 'In a national digital verification service, the Singleton pattern is suitable to coordinate shared SQLite connection access to avoid database locks.',
         'TRUE_FALSE', 1.0, 'EASY', 4, 1),
    (18, 'In the Dhaka Metro Rail ticketing database, which ACID property guarantees that completed passenger fare deductions persist even during power interruptions?',
         'MCQ', 1.0, 'MEDIUM', 2, 1),
    (19, 'Suppose the Bangladesh Meteorological Department implements a real-time storm warning system that pushes notifications to all subscribed coastal monitoring stations. Which design pattern applies?',
         'MCQ', 2.0, 'MEDIUM', 4, 1),
    (20, 'In Java, the ______ keyword is used by a subclass to inherit attributes and methods from its superclass.',
         'FILL_BLANK', 1.0, 'EASY', 1, 1);

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

-- Q6: FILL_BLANK - final keyword
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (17, 6, 'final', 1, 1);

-- Q7: SQL clause after aggregation
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (18, 7, 'WHERE',    0, 1),
    (19, 7, 'HAVING',   1, 2),
    (20, 7, 'GROUP BY', 0, 3),
    (21, 7, 'ORDER BY', 0, 4);

-- Q8: Normal form eliminating transitive dependencies
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (22, 8, '1NF',  0, 1),
    (23, 8, '2NF',  0, 2),
    (24, 8, '3NF',  1, 3),
    (25, 8, 'BCNF', 0, 4);

-- Q9: Primary key can be NULL (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (26, 9, 'True',  0, 1),
    (27, 9, 'False', 1, 2);

-- Q10: Valid SQL JOIN types (MULTIPLE_ANSWER)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (28, 10, 'INNER JOIN',      1, 1),
    (29, 10, 'LEFT JOIN',       1, 2),
    (30, 10, 'DIAGONAL JOIN',   0, 3),
    (31, 10, 'CROSS JOIN',      1, 4),
    (32, 10, 'FULL OUTER JOIN', 1, 5);

-- Q11: Shortest average waiting time scheduling
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (33, 11, 'FCFS',  0, 1),
    (34, 11, 'SJF',   1, 2),
    (35, 11, 'RR',    0, 3),
    (36, 11, 'LIFO',  0, 4);

-- Q12: What is a deadlock?
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (37, 12, 'A process waiting indefinitely for resources held by others', 1, 1),
    (38, 12, 'A memory leak in a program',                                  0, 2),
    (39, 12, 'A CPU scheduling algorithm',                                  0, 3),
    (40, 12, 'An infinite loop in code',                                    0, 4);

-- Q13: Semaphores and critical section (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (41, 13, 'True',  1, 1),
    (42, 13, 'False', 0, 2);

-- Q14: Singleton design pattern
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (43, 14, 'Factory',   0, 1),
    (44, 14, 'Singleton', 1, 2),
    (45, 14, 'Observer',  0, 3),
    (46, 14, 'Decorator', 0, 4);

-- Q15: Single Responsibility Principle (FILL_BLANK)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (47, 15, 'Single Responsibility', 1, 1);

-- Q16: University payment gateways (bKash/Nagad/Rocket)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (48, 16, 'Strategy Pattern',  1, 1),
    (49, 16, 'Observer Pattern',  0, 2),
    (50, 16, 'Decorator Pattern', 0, 3),
    (51, 16, 'Singleton Pattern', 0, 4);

-- Q17: Singleton for connection pool (True/False)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (52, 17, 'True',  1, 1),
    (53, 17, 'False', 0, 2);

-- Q18: Dhaka Metro Rail ACID durability
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (54, 18, 'Durability',  1, 1),
    (55, 18, 'Atomicity',   0, 2),
    (56, 18, 'Isolation',   0, 3),
    (57, 18, 'Consistency', 0, 4);

-- Q19: Bangladesh Met Department storm warning (Observer)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (58, 19, 'Observer Pattern', 1, 1),
    (59, 19, 'Factory Method',   0, 2),
    (60, 19, 'Adapter Pattern',  0, 3),
    (61, 19, 'Facade Pattern',   0, 4);

-- Q20: Java inheritance keyword (extends)
INSERT OR IGNORE INTO question_options (id, question_id, option_text, is_correct, option_order) VALUES
    (62, 20, 'extends', 1, 1);

-- -------------------------------------------------------
-- Quizzes
-- -------------------------------------------------------
INSERT OR IGNORE INTO quizzes
    (id, title, description, category_id, difficulty, time_limit_minutes, max_attempts, scoring_strategy, status, created_by, shuffle_questions)
VALUES
    (1, 'Java Basics Quiz',
        'A beginner-level quiz covering core Java fundamentals and OOP principles.',
        1, 'EASY', 30, 3, 'STANDARD', 'DRAFT', 1, 0),

    (2, 'Database Management Systems Quiz',
        'Comprehensive assessment covering SQL, relational schemas, indexing, and ACID properties.',
        2, 'MEDIUM', 45, 3, 'STANDARD', 'PUBLISHED', 1, 1),

    (3, 'Operating Systems Deep Dive',
        'Advanced topics in OS including CPU scheduling, deadlock resolution, and synchronization.',
        3, 'HARD', 60, 1, 'STANDARD', 'ARCHIVED', 1, 0),

    (4, 'Design Patterns & Software Architecture',
        'Practical evaluation of GoF design patterns (Strategy, State, Observer, Factory) in Bangladeshi enterprise systems.',
        4, 'MEDIUM', 40, 2, 'STANDARD', 'PUBLISHED', 1, 1);

-- -------------------------------------------------------
-- Quiz Questions (link questions to quizzes)
-- -------------------------------------------------------

-- Quiz 1 (Java Basics) - questions 1, 2, 3, 4, 5, 20
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (1, 1,  1),
    (1, 2,  2),
    (1, 3,  3),
    (1, 4,  4),
    (1, 5,  5),
    (1, 20, 6);

-- Quiz 2 (Database Management Systems) - questions 7, 8, 9, 10, 18
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (2, 7,  1),
    (2, 8,  2),
    (2, 9,  3),
    (2, 10, 4),
    (2, 18, 5);

-- Quiz 3 (OS Deep Dive) - questions 11, 12, 13
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (3, 11, 1),
    (3, 12, 2),
    (3, 13, 3);

-- Quiz 4 (Design Patterns & Architecture) - questions 6, 14, 15, 16, 17, 19
INSERT OR IGNORE INTO quiz_questions (quiz_id, question_id, question_order) VALUES
    (4, 6,  1),
    (4, 14, 2),
    (4, 15, 3),
    (4, 16, 4),
    (4, 17, 5),
    (4, 19, 6);

-- -------------------------------------------------------
-- Quiz Attempts (Seed student participation)
-- -------------------------------------------------------
INSERT OR IGNORE INTO quiz_attempts (id, quiz_id, student_id, start_time, end_time, status, score, total_marks) VALUES
    (1, 2, 2, '2026-09-08 10:00:00', '2026-09-08 10:22:15', 'SUBMITTED', 7.0, 7.0),
    (2, 2, 3, '2026-09-08 10:05:00', '2026-09-08 10:31:40', 'SUBMITTED', 6.0, 7.0),
    (3, 2, 4, '2026-09-08 10:10:00', '2026-09-08 10:38:20', 'SUBMITTED', 5.0, 7.0),
    (4, 4, 2, '2026-09-08 14:00:00', '2026-09-08 14:24:50', 'SUBMITTED', 9.5, 9.5),
    (5, 4, 3, '2026-09-08 14:05:00', '2026-09-08 14:32:10', 'SUBMITTED', 8.5, 9.5),
    (6, 4, 5, '2026-09-08 14:15:00', '2026-09-08 14:41:05', 'SUBMITTED', 7.5, 9.5);

-- -------------------------------------------------------
-- Answers (Seed evaluated student submissions)
-- -------------------------------------------------------
-- Attempt 1: Md. Shourov on Quiz 2 (Perfect score: 7.0/7.0)
INSERT OR IGNORE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) VALUES
    (1, 7,  'HAVING',                                      1, 1.0),
    (1, 8,  '3NF',                                         1, 2.0),
    (1, 9,  'False',                                       1, 1.0),
    (1, 10, 'INNER JOIN,LEFT JOIN,CROSS JOIN,FULL OUTER JOIN', 1, 2.0),
    (1, 18, 'Durability',                                  1, 1.0);

-- Attempt 2: Rokib Hasan on Quiz 2 (Score: 6.0/7.0)
INSERT OR IGNORE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) VALUES
    (2, 7,  'HAVING',                                      1, 1.0),
    (2, 8,  '3NF',                                         1, 2.0),
    (2, 9,  'True',                                        0, 0.0),
    (2, 10, 'INNER JOIN,LEFT JOIN,CROSS JOIN,FULL OUTER JOIN', 1, 2.0),
    (2, 18, 'Durability',                                  1, 1.0);

-- Attempt 3: Tanvir Ahmed on Quiz 2 (Score: 5.0/7.0)
INSERT OR IGNORE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) VALUES
    (3, 7,  'WHERE',                                       0, 0.0),
    (3, 8,  '3NF',                                         1, 2.0),
    (3, 9,  'False',                                       1, 1.0),
    (3, 10, 'INNER JOIN,LEFT JOIN,CROSS JOIN,FULL OUTER JOIN', 1, 2.0),
    (3, 18, 'Atomicity',                                   0, 0.0);

-- Attempt 4: Md. Shourov on Quiz 4 (Perfect score: 9.5/9.5)
INSERT OR IGNORE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) VALUES
    (4, 6,  'final',                 1, 1.5),
    (4, 14, 'Singleton',             1, 1.0),
    (4, 15, 'Single Responsibility', 1, 2.0),
    (4, 16, 'Strategy Pattern',      1, 2.0),
    (4, 17, 'True',                  1, 1.0),
    (4, 19, 'Observer Pattern',      1, 2.0);

-- Attempt 5: Rokib Hasan on Quiz 4 (Score: 8.5/9.5)
INSERT OR IGNORE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) VALUES
    (5, 6,  'final',                 1, 1.5),
    (5, 14, 'Singleton',             1, 1.0),
    (5, 15, 'Open Closed',           0, 0.0),
    (5, 16, 'Strategy Pattern',      1, 2.0),
    (5, 17, 'True',                  1, 1.0),
    (5, 19, 'Observer Pattern',      1, 2.0);

-- Attempt 6: Sadia Islam on Quiz 4 (Score: 7.5/9.5)
INSERT OR IGNORE INTO answers (attempt_id, question_id, answer_value, is_correct, marks_awarded) VALUES
    (6, 6,  'static',                0, 0.0),
    (6, 14, 'Singleton',             1, 1.0),
    (6, 15, 'Single Responsibility', 1, 2.0),
    (6, 16, 'Strategy Pattern',      1, 2.0),
    (6, 17, 'False',                 0, 0.0),
    (6, 19, 'Observer Pattern',      1, 2.0);
