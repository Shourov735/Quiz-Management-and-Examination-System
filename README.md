# Quiz Management & Examination System

A desktop-based quiz and examination management system developed in Java as a **Design Patterns Lab Final Project**.

The system will provide separate functionality for **Teachers** and **Students**. Teachers will be able to create and manage quizzes and questions, configure examination rules, publish quizzes, and analyze student performance. Students will be able to discover available quizzes, attempt examinations, submit answers, and review their results and attempt history.

The project is intended not only to produce a working application, but also to demonstrate the ability to:

- Analyze a software problem
- Identify meaningful design challenges
- Model a domain
- Apply appropriate object-oriented design principles
- Select design patterns based on actual problems
- Build a maintainable and extensible architecture
- Design and use a relational database
- Implement meaningful workflows and business rules
- Test important application logic

---

# Table of Contents

- [1. Project Information](#1-project-information)
- [2. Project Overview](#2-project-overview)
- [3. Problem Statement](#3-problem-statement)
- [4. Proposed Solution](#4-proposed-solution)
- [5. Objectives](#5-objectives)
- [6. Target Users](#6-target-users)
- [7. Functional Requirements](#7-functional-requirements)
- [8. Core Entities](#8-core-entities)
- [9. Business Rules](#9-business-rules)
- [10. Major Workflows](#10-major-workflows)
- [11. Search and Reporting](#11-search-and-reporting)
- [12. Database Design](#12-database-design)
- [13. Database Seeder](#13-database-seeder)
- [14. Application Architecture](#14-application-architecture)
- [15. JavaFX Application Screens](#15-javafx-application-screens)
- [16. Planned Design Patterns](#16-planned-design-patterns)
- [17. SOLID Principles](#17-solid-principles)
- [18. Persistence and Repository Layer](#18-persistence-and-repository-layer)
- [19. Validation and Error Handling](#19-validation-and-error-handling)
- [20. Testing Strategy](#20-testing-strategy)
- [21. Maven Configuration](#21-maven-configuration)
- [22. Project Structure](#22-project-structure)
- [23. UML and Documentation](#23-uml-and-documentation)
- [24. Future Extensibility](#24-future-extensibility)
- [25. Project Scope Compliance](#25-project-scope-compliance)
- [26. Technologies](#26-technologies)
- [27. Design Philosophy](#27-design-philosophy)
- [License](#license)

---

# 1. Project Information

| Item | Information |
|---|---|
| Project Title | Quiz Management & Examination System |
| Project Type | Design Patterns Final Project |
| Application Type | Desktop Application |
| Primary Language | Java (JDK 17) |
| UI Framework | JavaFX |
| Build / Dependency Management | Maven |
| Database | SQLite |
| Database Access | JDBC |
| Testing | JUnit 5 |
| Version Control | Git |
| Repository | GitHub |
| Team Size | 2 |

## Authors

- **Md. Shourov** — 1609
- **Md. Rokib Ikbal** — 1605

---

# 2. Project Overview

The **Quiz Management & Examination System** will be a desktop application that supports the complete lifecycle of quizzes and examinations.

The system will provide two primary roles:

### Teacher

Teachers will prepare and manage examinations by:

- Creating quizzes
- Managing questions
- Configuring quiz rules
- Adding questions to quizzes
- Editing and deleting questions
- Publishing quizzes
- Managing quiz availability
- Reviewing student attempts
- Analyzing examination results

### Student

Students will use the system to:

- Log in
- View available quizzes
- Search and filter quizzes
- View quiz details
- Start examinations
- Answer questions
- Submit examinations
- Handle time-limited attempts
- View scores
- View previous attempts
- Review performance information

The application will use **SQLite as persistent storage**, meaning important data will remain available after the application is closed and started again.

---

# 3. Problem Statement

A basic quiz application can be implemented using a small number of classes and direct database operations. However, as the requirements grow, several software design problems appear:

### Different Question Types
The system will support:
- Multiple Choice Questions
- True/False Questions
- Fill-in-the-Blank Questions
- Multiple Answer Questions

Each question type has different data representations, validation algorithms, and answer matching rules.

### Different Scoring Rules
Different quizzes will use different scoring mechanisms, such as:
- Standard scoring
- Negative marking
- Time-based scoring

Embedding all scoring logic in a single class produces tight coupling and violates the Open/Closed Principle.

### Quiz Lifecycle
A quiz moves through different states:
```text
DRAFT  →  PUBLISHED  →  ACTIVE  →  COMPLETED  →  ARCHIVED
```
The valid operations on a quiz depend strictly on its current state (e.g. students cannot attempt drafts; questions cannot be added to active examinations).

### Quiz Events
Multiple components need to react to events during an attempt (answer submission, timer expiry, quiz completion). Tightly coupling every component directly to the quiz execution engine makes the system brittle.

### Persistence Separation
The application must communicate with SQLite while keeping database access separate from business logic and JavaFX presentation code.

The project will use appropriate abstractions and design patterns to manage these sources of complexity.

---

# 4. Proposed Solution

The proposed system will follow a layered design in which:

* **JavaFX** handles presentation and user interaction.
* **Services** coordinate application and business workflows.
* **Domain objects** represent core business concepts.
* **Design patterns** manage variable behavior, object creation, lifecycle states, and events.
* **Repositories** abstract persistence operations.
* **JDBC** handles SQLite communication.
* **SQLite** stores persistent application data.

The system emphasizes **separation of responsibilities**, **low coupling**, **high cohesion**, and **future extensibility**.

---

# 5. Objectives

## 5.1 Software Engineering Objectives
* Apply object-oriented design and SOLID principles.
* Cleanly separate presentation, business logic, domain logic, and persistence.
* Reduce coupling between components and keep classes focused.
* Build understandable, maintainable, and extensible code.

## 5.2 Design Pattern Objectives
* Identify real software design challenges in the examination domain.
* Select appropriate design patterns based on actual problems.
* Implement patterns correctly without forcing unnecessary patterns.
* Document reasons for pattern selection, alternatives considered, and future benefits.

## 5.3 Database Objectives
* Use SQLite as a persistent relational database.
* Maintain relationships, primary keys, foreign keys, and constraints.
* Seed initial data for demonstration and testing.
* Preserve all quiz, user, and attempt data across application restarts.

---

# 6. Target Users

## 6.1 Teacher
Teachers will prepare and manage examinations and evaluate student performance:
* Authentication & login
* Create, view, search, edit, delete, publish, unpublish, and archive quizzes
* Create, view, search, edit, and delete questions across categories
* Add and remove questions to/from quizzes
* Configure exam rules (time limit, max attempts, scoring strategy, shuffling)
* Review student attempts and inspect aggregate quiz analytics

## 6.2 Student
Students will participate in examinations:
* Authentication & registration
* Discover, search, and filter available published quizzes
* Attempt examinations with question navigation and countdown timers
* Submit answers manually or automatically upon time expiration
* Review scores, percentage performance, and question-by-question solutions
* Review attempt history and overall performance metrics

---

# 7. Functional Requirements

## 7.1 Authentication
* Basic credential validation identifying whether the user is a Teacher or Student.
* Feature access and dashboards restricted according to user role.

## 7.2 Quiz Management
* Create and configure quizzes with title, description, category, difficulty, time limit, max attempts, and scoring strategy.
* Full lifecycle control: Draft, Publish, Unpublish, Archive, and Delete.

## 7.3 Question Management
* Central question bank supporting:
  1. Multiple Choice Question (MCQ)
  2. True/False Question
  3. Fill-in-the-Blank Question
  4. Multiple Answer Question
* Questions contain text, marks, difficulty, category, options, and expected answers.

## 7.4 Quiz Configuration
* Configurable examination parameters: time limit (minutes), max attempts allowed per student (0 = unlimited), question shuffling, and scoring algorithm.

## 7.5 Quiz Attempt & Examination
* Eligibility validation (quiz status and student attempt limits).
* Examination session management with live timing.
* Dynamic answer input based on question type.
* Answer validation and automated score computation.
* Solution review and attempt recording.

---

# 8. Core Entities

```text
User
Teacher
Student
Quiz
Question
QuestionOption
QuizQuestion
QuizAttempt
Answer
Category
```

### 8.1 User
Base entity representing application accounts with attributes: `id`, `name`, `email`, `password`, `role` (`TEACHER`, `STUDENT`), and `createdAt`.

### 8.2 Quiz
Represents an examination with attributes: `id`, `title`, `description`, `categoryId`, `difficulty`, `timeLimitMinutes`, `maxAttempts`, `scoringStrategy`, `status`, `createdBy`, `shuffleQuestions`, and `totalMarks`.

### 8.3 Question
Abstract base entity representing a question with attributes: `id`, `questionText`, `questionType`, `marks`, `difficulty`, `categoryId`, `createdBy`, and associated `options`.

### 8.4 QuestionOption
Selectable option for questions: `id`, `questionId`, `optionText`, `isCorrect`, and `optionOrder`.

### 8.5 QuizQuestion
Many-to-many junction relating quizzes to questions, maintaining quiz-specific ordering: `id`, `quizId`, `questionId`, and `questionOrder`.

### 8.6 QuizAttempt
Represents a student's examination attempt: `id`, `quizId`, `studentId`, `startTime`, `endTime`, `status` (`IN_PROGRESS`, `SUBMITTED`, `TIMED_OUT`), `score`, and `totalMarks`.

### 8.7 Answer
Represents a student's answer to a question within an attempt: `id`, `attemptId`, `questionId`, `answerValue`, `isCorrect`, and `marksAwarded`.

### 8.8 Category
Represents subject classification: `id`, `name`, and `description` (e.g. Programming, Database, Operating Systems).

---

# 9. Business Rules

The system will enforce essential business rules:

1. **Quiz Publication Rules**: A quiz must have a non-empty title and contain at least one valid question before it can be published.
2. **Quiz Availability Rules**: Only quizzes in `PUBLISHED` or `ACTIVE` states can be attempted by students. `DRAFT` and `ARCHIVED` quizzes reject attempt requests.
3. **Attempt Limit Rules**: If a quiz specifies a maximum attempt limit ($>0$), students who have reached the limit are blocked from starting new attempts.
4. **Time Limit Rules**: When an attempt reaches the time limit, the session is terminated as `TIMED_OUT` and evaluated automatically.
5. **Answer Validation Rules**: Validation is polymorphically evaluated per question type:
   - MCQ: Matches correct option text or ID.
   - True/False: Binary case-insensitive matching.
   - Fill-in-the-Blank: Trimmed, case-insensitive string comparison.
   - Multiple Answer: Exact set match of all correct options with no missing or extra selections.
6. **Attempt Immutability Rules**: Once an attempt is `SUBMITTED` or `TIMED_OUT`, answers and scores cannot be modified.

---

# 10. Major Workflows

## 10.1 Teacher Quiz Creation Workflow

```text
Teacher Login
      ↓
Teacher Dashboard
      ↓
Create Quiz (Title, Category, Difficulty, Rules)
      ↓
Select / Add Questions from Question Bank
      ↓
Validate Quiz Constraints (Questions count >= 1)
      ↓
Save as Draft
      ↓
Publish Quiz
```

## 10.2 Student Examination Workflow

```text
Student Login
      ↓
Student Dashboard (Browse Available Quizzes)
      ↓
Select Examination
      ↓
Validate Eligibility (Status == PUBLISHED, Attempts < MaxAttempts)
      ↓
Start Attempt & Launch Timer
      ↓
Navigate & Answer Questions
      ↓
Submit Voluntarily OR Auto-Submit on Timeout
      ↓
Evaluate Answers & Apply Scoring Strategy
      ↓
Persist Results & Display Solutions Breakdown
```

## 10.3 Quiz Lifecycle Transition Workflow

```text
Draft
  │ (publish)
  ▼
Published
  │ (activate)      │ (unpublish)
  ▼                 ▼
Active            Draft
  │ (complete)
  ▼
Completed ──(archive)──▶ Archived
```

---

# 11. Search and Reporting

The application will include meaningful search and analytical operations:

## 11.1 Quiz Search & Filtering
* Filter by title keyword (`LIKE`), subject category, difficulty level, and publication status.

## 11.2 Question Bank Search
* Filter questions by text keyword, question type (MCQ, True/False, Fill Blank, Multiple Answer), category, and difficulty.

## 11.3 Student Performance Reporting
* Calculate student examination history, total attempts, completed count, average score, and accuracy percentage.

## 11.4 Quiz Performance Analytics
* Calculate aggregate examination metrics for teachers: total attempts count, average score, highest score, lowest score, and full score leaderboard.

---

# 12. Database Design

SQLite is used as an integral persistent database. Data remains persistent across restarts.

## 12.1 Planned Tables
* `users`
* `categories`
* `quizzes`
* `questions`
* `question_options`
* `quiz_questions`
* `quiz_attempts`
* `answers`

## 12.2 Relationships
* `users` (1) — (N) `quizzes` (created_by)
* `users` (1) — (N) `quiz_attempts` (student_id)
* `categories` (1) — (N) `quizzes` (category_id)
* `categories` (1) — (N) `questions` (category_id)
* `quizzes` (M) — (N) `questions` (via `quiz_questions`)
* `questions` (1) — (N) `question_options` (question_id)
* `quizzes` (1) — (N) `quiz_attempts` (quiz_id)
* `quiz_attempts` (1) — (N) `answers` (attempt_id)
* `questions` (1) — (N) `answers` (question_id)

## 12.3 Database Constraints
* Primary keys on all tables.
* Foreign keys with referential integrity (`PRAGMA foreign_keys = ON`).
* Unique constraints on `users.email`, `categories.name`, and composite `quiz_questions(quiz_id, question_id)`.
* `CHECK` constraints on enums (roles, statuses, difficulties, question types).
* Indexes on frequently searched foreign keys and statuses.

---

# 13. Database Seeder

The application will include an automatic seeder:
1. Executes `schema.sql` to ensure all tables, constraints, and indexes exist.
2. Checks if the database is unseeded.
3. Executes `seed.sql` to populate sample accounts (1 teacher, 3 students), 5 subject categories, 15+ multi-type questions, and sample quizzes.

---

# 14. Application Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                 JavaFX Presentation Layer                   │
│        Views & Controllers (Login, Teacher, Student, etc.)  │
├─────────────────────────────────────────────────────────────┤
│                     Service Layer                           │
│  (AuthenticationService, QuizService, AttemptService, etc.) │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                            │
│      Entities, Models, Enums & Design Pattern Objects       │
├─────────────────────────────────────────────────────────────┤
│                   Repository Layer                          │
│  Interfaces & SQLite Implementations (PreparedStatement)    │
├─────────────────────────────────────────────────────────────┤
│                 Persistent Database (SQLite)                │
└─────────────────────────────────────────────────────────────┘
```

---

# 15. JavaFX Application Screens

The system will feature 8 major screens:

1. **Login Screen**: Authentication and role-based redirect, with quick-fill demo buttons and student registration.
2. **Teacher Dashboard**: Metric overview cards (total quizzes, published count, question bank items, attempts) and recent quizzes table.
3. **Quiz Management**: Multi-filter quiz table, quiz CRUD dialog, question assignment, and state transitions (Publish/Unpublish/Archive).
4. **Question Bank**: Searchable question repository with dynamic creation forms for all 4 question types.
5. **Student Dashboard**: Published examinations table, attempt limit verification, and past attempts history.
6. **Quiz Attempt Screen**: Live examination interface with countdown timer, question quick-navigator, and dynamic answer forms.
7. **Result Screen**: Visual score card, time spent, breakdown metrics, and question-by-question solution review.
8. **Reports & Analytics Screen**: Quiz leaderboards and student performance history.

---

# 16. Planned Design Patterns

Patterns will be introduced to solve specific architectural and extensibility challenges.

## 16.1 Strategy Pattern — Scoring
* **Problem**: Different examinations require distinct scoring rules (standard flat scoring, negative marking to penalize guessing, or speed bonuses for rapid completion). Hardcoding scoring inside the evaluation service violates the Open/Closed Principle.
* **Proposed Design**:
  - `ScoringStrategy` interface with `calculateScore(...)`.
  - `StandardScoring`: Flat sum of marks.
  - `NegativeMarkingScoring`: Deducts a penalty fraction for wrong answers.
  - `TimeBasedScoring`: Awards speed bonuses for finishing ahead of time.
  - `ScoringStrategyFactory`: Resolves strategies dynamically by name.
* **Future Benefit**: New scoring formulas can be added without modifying the evaluation service.

## 16.2 State Pattern — Quiz Lifecycle
* **Problem**: A quiz behaves differently depending on whether it is `DRAFT`, `PUBLISHED`, `ACTIVE`, `COMPLETED`, or `ARCHIVED`. Scattered `if-else` status checks lead to duplicated logic and invalid transitions.
* **Proposed Design**:
  - `QuizState` interface defining allowed transitions and permissions (`canBeAttempted()`, `canBeEdited()`, `canAddQuestions()`).
  - Concrete state classes: `DraftState`, `PublishedState`, `ActiveState`, `CompletedState`, `ArchivedState`.
  - `QuizStateManager`: Context managing state changes.
* **Future Benefit**: New lifecycle states can be introduced with zero changes to existing state classes.

## 16.3 Factory Method — Question Creation
* **Problem**: Multiple question types have distinct data attributes, validation algorithms, and UI controls. Direct instantiation throughout repositories creates tight coupling.
* **Proposed Design**:
  - Abstract class `Question` with abstract `validateAnswer(...)` and `getCorrectAnswerDisplay()`.
  - Subclasses: `MCQQuestion`, `TrueFalseQuestion`, `FillBlankQuestion`, `MultipleAnswerQuestion`.
  - `QuestionFactory` to instantiate question subclasses and map database result sets polymorphically.
* **Future Benefit**: Adding new question types requires only extending `Question` and adding a case in the factory.

## 16.4 Observer Pattern — Quiz Events
* **Problem**: During an examination attempt, multiple components need to react to events (tracking progress, updating running score, handling timeouts). Tightly coupling the service to GUI listeners violates Single Responsibility.
* **Proposed Design**:
  - `QuizEventPublisher`: Thread-safe publish-subscribe event bus.
  - `QuizEventListener`: Listener interface with `onEvent(QuizEvent, Object)`.
  - Concrete listeners: `ProgressTracker`, `TimerListener`.
* **Future Benefit**: New event consumers (audit logger, proctoring alert monitors) can be attached seamlessly.

## 16.5 Singleton Pattern (Justified)
* **Problem**: SQLite is an embedded file-based database. Multiple uncoordinated connections cause database lock errors (`SQLITE_BUSY`).
* **Proposed Design**: `DatabaseConnection` singleton using double-checked locking to manage the shared JDBC connection and execute migration scripts. Avoided across all business services.

## 16.6 Repository Pattern
* Decouples data access from business logic using repository interfaces and SQLite implementations.

---

# 17. SOLID Principles

* **Single Responsibility Principle (SRP)**: Each class is focused on one concern (e.g., `AuthenticationService` handles auth; `StandardScoring` computes standard marks).
* **Open/Closed Principle (OCP)**: The system is open for extension (new scoring strategies, question types, states) without modifying existing evaluation loops.
* **Liskov Substitution Principle (LSP)**: Concrete `Question` subclasses can be substituted wherever `Question` is expected without unexpected behavior.
* **Interface Segregation Principle (ISP)**: Focused interfaces (`ScoringStrategy`, `QuizEventListener`, `QuizState`).
* **Dependency Inversion Principle (DIP)**: Services depend on repository interfaces and abstractions, not concrete database classes.

---

# 18. Persistence and Repository Layer

```text
QuizService  ──▶  QuizRepository (Interface)
                         │
                         ▼
               SQLiteQuizRepository (Implementation)
                         │
                         ▼
                       JDBC
                         │
                         ▼
                      SQLite
```

Repository abstractions:
* `UserRepository`
* `QuizRepository`
* `QuestionRepository`
* `QuizAttemptRepository`
* `AnswerRepository`
* `CategoryRepository`

---

# 19. Validation and Error Handling

* **Form Validation**: Non-empty required fields, numeric constraints (positive marks, non-negative time limits).
* **Business Validation**: Cannot publish quiz with 0 questions; cannot attempt quiz beyond max attempts; cannot edit published or archived quiz.
* **Database Safety**: Checked `SQLException`s wrapped into clear runtime exceptions; try-with-resources used on statements and result sets to prevent leaks.
* **UI Feedback**: Non-blocking `AlertHelper` dialogs providing clear error, warning, and confirmation messages.

---

# 20. Testing Strategy

The project will include comprehensive automated tests using **JUnit 5** and **Mockito**:

## 20.1 Unit Testing Areas
* **Scoring Strategies**: Standard scoring, negative marking penalties, speed bonuses, edge cases (zero answers, empty quizzes).
* **Question Validation**: Case insensitivity, whitespace trimming, partial selection rejection in multiple-answer questions, true/false matching.
* **Quiz State Machine**: Valid and invalid state transitions, permission enforcement.
* **Service Layer**: Mocked repository tests for attempt creation, eligibility checking, timeout handling, and user registration.

---

# 21. Maven Configuration

Maven manages dependencies, compilation, testing, and packaging:
* Java version: **Java 17**
* **JavaFX Controls & FXML** (21.0.2)
* **SQLite JDBC Driver** (3.45.1.0)
* **JUnit Jupiter 5** & **Mockito**
* **Maven Shade Plugin** for packaging an executable JAR

---

# 22. Project Structure

```text
quiz-management-system/
├── pom.xml
├── README.md
├── .gitignore
│
├── docs/
│   ├── uml/
│   │   └── class-diagram.md
│   ├── er-diagram/
│   │   └── database-er.md
│   └── design-decisions/
│       └── design-patterns.md
│
├── src/
│   ├── main/
│   │   ├── java/com/quizapp/
│   │   │   ├── Main.java
│   │   │   ├── controller/      # JavaFX UI Controllers
│   │   │   ├── database/        # DatabaseConnection singleton
│   │   │   ├── factory/         # QuestionFactory
│   │   │   ├── model/           # Entities and enums
│   │   │   ├── observer/        # Event publisher & listeners
│   │   │   ├── question/        # Question polymorphic subclasses
│   │   │   ├── repository/      # Repository interfaces & SQLite impls
│   │   │   ├── scoring/         # ScoringStrategy implementations
│   │   │   ├── service/         # Business services
│   │   │   ├── state/           # QuizState implementations & manager
│   │   │   └── util/            # SessionContext & AlertHelper
│   │   │
│   │   └── resources/
│   │       ├── css/style.css
│   │       ├── database/        # schema.sql & seed.sql
│   │       └── fxml/            # 8 JavaFX FXML screen layouts
│   │
│   └── test/java/com/quizapp/   # Comprehensive JUnit 5 test suites
```

---

# 23. UML and Documentation

Comprehensive technical documentation has been generated for academic submission:
* **Technical Documentation PDF**: [`Quiz_Management_System_Technical_Documentation.pdf`](Quiz_Management_System_Technical_Documentation.pdf) (or [`docs/technical-documentation.pdf`](docs/technical-documentation.pdf))
* **Technical Documentation HTML**: [`docs/technical-documentation.html`](docs/technical-documentation.html)
* **UML Class Diagrams**: Core domain relationships and design pattern participants (Strategy, State, Factory Method, Observer) in [`docs/uml/class-diagram.md`](docs/uml/class-diagram.md).
* **Database ER Diagram**: Entity-relationship diagram detailing entities, attributes, primary keys, foreign keys, and cardinalities in [`docs/er-diagram/database-er.md`](docs/er-diagram/database-er.md).
* **Design Decision Records**: Written rationale for each pattern covering Problem, Motivation, Solution, Alternatives, Trade-offs, and Future Benefits in [`docs/design-decisions/design-patterns.md`](docs/design-decisions/design-patterns.md).

---

# 24. Future Extensibility

* **New Question Types**: Adding types such as `OrderingQuestion`, `MatchingQuestion`, or `CodeSnippetQuestion` by subclassing `Question`.
* **New Scoring Algorithms**: Adding algorithms such as `PartialCreditScoring` or `DifficultyWeightedScoring` via `ScoringStrategy`.
* **New Quiz States**: Introducing states such as `PENDING_REVIEW` or `SUSPENDED` via `QuizState`.
* **Alternative Persistence**: Swapping SQLite with another database (e.g. PostgreSQL or MySQL) by providing new repository implementations without altering business services.

---

# 25. Project Scope Compliance

| Guideline Requirement | Project Implementation |
|---|---|
| Team of two | Md. Shourov (1609) & Md. Rokib Ikbal (1605) |
| Java desktop application | JavaFX 21 desktop application |
| Maven dependency management | Fully configured `pom.xml` |
| SQLite persistent database | SQLite via JDBC with schema and seeder |
| Meaningful database entities (4+) | 8 entities (`users`, `categories`, `quizzes`, `questions`, `question_options`, `quiz_questions`, `quiz_attempts`, `answers`) |
| CRUD functionality (3+ entities) | Full CRUD for Quizzes, Questions, Users, Categories |
| Multi-step workflows (2+) | Quiz creation/publication workflow & student exam/evaluation workflow |
| Search & reporting operations (2+) | Multi-criteria quiz/question search & exam analytics/leaderboards |
| 4–8 major screens | 8 JavaFX screens (Login, Teacher Dashboard, Quiz Management, Question Bank, Student Dashboard, Quiz Attempt, Results, Reports) |
| Design patterns | Strategy, State, Factory Method, Observer, Singleton, Repository |
| Testing | Automated unit tests with JUnit 5 |
| Error handling & validation | Handled at domain, service, and UI levels |

---

# 26. Technologies

* **Java (JDK 17)**: Primary programming language.
* **JavaFX 21**: Desktop graphical user interface framework.
* **Maven**: Build and dependency management.
* **SQLite & JDBC**: Persistent database and connectivity.
* **JUnit 5 & Mockito**: Automated testing and mocking.
* **Git & GitHub**: Version control and collaboration.

---

# 27. Design Philosophy

1. **Design Patterns as Tools**: Patterns solve concrete architectural problems, not included merely to inflate numbers.
2. **Separation of Concerns**: UI code does not contain business logic or raw SQL queries; business logic does not know about JavaFX.
3. **Database as a Core Component**: SQLite persistence and relational integrity are designed as central parts of the application.
4. **Design for Change**: Architecture ensures that question types, scoring strategies, and states can be extended easily in the future.

---

# License

This project is developed for academic purposes as part of the **Design Patterns Lab Final Project**.