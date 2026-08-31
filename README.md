# Quiz Management & Examination System

A mini Java software project designed to demonstrate the practical application of **Object-Oriented Programming, SOLID principles, and Design Patterns** through a console-based quiz and examination system.

The system will allow teachers to create and manage quizzes, students to attempt published quizzes, and the system to evaluate attempts and maintain examination results.

The project will focus on applying design patterns to real software design problems rather than using patterns only for demonstration purposes.

> **Project Status:** Planning / Not Yet Implemented

---

## Authors

* **Md. Shourov** - 1609
* **Md. Rokib Ikbal** - 1605

---

# 1. Project Overview

The **Quiz Management & Examination System** will be a Java-based console application for creating, managing, and taking quizzes.

The system will support two primary user roles:

* **Student**
* **Teacher**

Teachers will be able to create quizzes, add questions, configure quiz settings, publish quizzes, and review student results.

Students will be able to view available quizzes, attempt quizzes, answer questions within a time limit, submit their attempts, and view their results and attempt history.

The system will also demonstrate different scoring strategies, quiz lifecycle management, question creation, event notification, and persistence using a relational database.

---

# 2. Project Objectives

The main objectives of the project will be to:

* Apply Object-Oriented Programming principles in a real software system.
* Demonstrate the practical use of SOLID principles.
* Apply appropriate Design Patterns to solve specific design problems.
* Develop a maintainable and modular Java application.
* Implement role-based functionality for students and teachers.
* Manage different types of examination questions.
* Implement configurable scoring mechanisms.
* Track quiz attempts and examination results.
* Persist application data using SQLite.
* Practice database interaction using JDBC.
* Write unit tests for important business logic.
* Demonstrate clean separation between the user interface, business logic, and data access layers.

---

# 3. User Roles

## 3.1 Student

A student will be able to:

* Register an account.
* Log in to the system.
* View available quizzes.
* View quiz information.
* Start a quiz.
* Answer questions.
* Navigate through questions.
* Submit a quiz.
* Receive the final score.
* View correct and incorrect answers where permitted.
* View previous quiz attempts.
* View best scores.

## 3.2 Teacher

A teacher will be able to:

* Log in to the system.
* Create quizzes.
* Configure quiz settings.
* Add questions to quizzes.
* Edit questions.
* Remove questions.
* Select question types.
* Set question marks.
* Set quiz duration.
* Set difficulty and category.
* Configure scoring rules.
* Publish quizzes.
* Unpublish or archive quizzes.
* View student results.

---

# 4. Planned Features

## 4.1 User Management

The system will provide basic authentication and role management.

Planned functionality:

* Student registration
* Login
* Logout
* Role identification
* Student and teacher access control

---

## 4.2 Question Management

The system will support multiple question types.

### Planned Question Types

* Multiple Choice Question (MCQ)
* True/False Question
* Fill-in-the-Blank Question
* Multiple Answer Question

Each question may contain information such as:

* Question text
* Marks
* Difficulty
* Category
* Correct answer
* Available options where applicable

---

## 4.3 Quiz Management

Teachers will be able to create and configure quizzes.

Planned quiz configuration:

* Quiz title
* Description
* Category
* Difficulty
* Time limit
* Number of questions
* Question shuffling
* Negative marking
* Scoring strategy

---

## 4.4 Quiz Lifecycle

A quiz will have a defined lifecycle.

The planned states are:

```text
DRAFT
  ↓
PUBLISHED
  ↓
ACTIVE
  ↓
COMPLETED
  ↓
ARCHIVED
```

The system will enforce rules depending on the current state.

For example:

* A draft quiz cannot normally be attempted.
* A published quiz can become available to students.
* An archived quiz will no longer be available for new attempts.
* Quiz editing will be restricted according to its state.

The exact transition rules will be finalized during implementation.

---

# 5. Quiz Attempt Management

Every time a student takes a quiz, the system will create a separate **Quiz Attempt**.

A quiz attempt will contain information such as:

* Student
* Quiz
* Start time
* End time
* Attempt status
* Submitted answers
* Score
* Maximum possible score

A student may have multiple attempts where the quiz configuration permits it.

Example:

```text
Student
   │
   ├── Attempt 1 → 6/10
   ├── Attempt 2 → 8/10
   └── Attempt 3 → 9/10
```

The system will be able to maintain the student's attempt history and determine the best score where applicable.

---

# 6. Scoring System

The application will support interchangeable scoring strategies.

## Planned Scoring Strategies

### Standard Scoring

* Correct answer → positive marks
* Incorrect answer → no marks

### Negative Marking

* Correct answer → positive marks
* Incorrect answer → negative marks

### Time-Based Scoring

* Correct answer → base marks
* Faster answers → additional bonus where applicable

The scoring rules will be implemented independently from the quiz engine.

---

# 7. Planned Design Patterns

The project will demonstrate the following design patterns.

The patterns will be selected based on actual design requirements rather than being added artificially.

---

## 7.1 Factory Method

### Problem

The system will need to create different types of questions.

Creating every question directly inside the quiz or application logic would tightly couple those components to concrete question classes.

### Planned Solution

The Factory Method pattern will be used to create question objects.

Possible structure:

```text
Question
├── MCQQuestion
├── TrueFalseQuestion
├── FillInBlankQuestion
└── MultipleAnswerQuestion

QuestionFactory
├── MCQFactory
├── TrueFalseFactory
├── FillInBlankFactory
└── MultipleAnswerFactory
```

### Purpose

The quiz system will be able to request a question without depending directly on the construction details of every concrete question type.

This will make it easier to introduce new question types later.

---

## 7.2 Builder

### Problem

A quiz may have many optional configuration parameters.

For example:

```text
Quiz
├── title
├── description
├── category
├── difficulty
├── time limit
├── shuffle questions
├── negative marking
└── scoring strategy
```

Passing all of these values through a large constructor would make quiz creation difficult to read and maintain.

### Planned Solution

The Builder pattern will be used to construct `Quiz` objects step by step.

Example concept:

```text
QuizBuilder
    ↓
setTitle()
    ↓
setCategory()
    ↓
setDifficulty()
    ↓
setTimeLimit()
    ↓
enableShuffle()
    ↓
setScoringStrategy()
    ↓
build()
    ↓
Quiz
```

### Purpose

The Builder pattern will make quiz construction readable while allowing optional configuration.

---

## 7.3 Strategy

### Problem

Different quizzes may require different scoring rules.

The quiz engine should not contain separate conditional logic for every possible scoring algorithm.

### Planned Solution

The Strategy pattern will encapsulate scoring algorithms.

```text
ScoringStrategy
       │
       ├── StandardScoring
       ├── NegativeMarkingScoring
       └── TimeBasedScoring
```

### Purpose

The scoring strategy will be replaceable without changing the main quiz logic.

This will demonstrate the **Open/Closed Principle** and separation of responsibilities.

---

## 7.4 State

### Problem

The behavior of a quiz will depend on its current lifecycle state.

For example, a draft quiz should not behave in the same way as a published or archived quiz.

### Planned Solution

The State pattern will represent the different quiz states.

```text
QuizState
   │
   ├── DraftState
   ├── PublishedState
   ├── ActiveState
   ├── CompletedState
   └── ArchivedState
```

### Purpose

The State pattern will allow quiz behavior to change according to its current state without creating a large collection of conditional statements inside the `Quiz` class.

---

## 7.5 Observer

### Problem

Several components may need to react when important events occur during a quiz.

Examples include:

* A question is answered.
* The score changes.
* A quiz is completed.
* Time expires.

The quiz engine should not need to know the internal details of every component interested in these events.

### Planned Solution

The Observer pattern will be used for quiz events.

```text
Quiz
 │
 ├── ScoreBoard
 ├── ProgressTracker
 ├── ResultGenerator
 └── NotificationService
```

### Purpose

Observers will be notified when relevant quiz events occur.

This will reduce coupling between the quiz engine and components that react to quiz events.

---

# 8. Planned Use of SOLID Principles

The project will attempt to apply SOLID principles throughout the design.

## Single Responsibility Principle

Classes will have focused responsibilities.

For example:

```text
Quiz
ScoringStrategy
Question
QuizAttempt
QuizRepository
```

will have different responsibilities instead of placing all logic inside one large class.

## Open/Closed Principle

The system should be extendable without modifying existing stable code.

Examples include:

* Adding a new question type.
* Adding a new scoring strategy.
* Adding a new quiz state.

## Liskov Substitution Principle

Concrete question types and other subclasses should be usable through their abstractions without breaking expected behavior.

## Interface Segregation Principle

Interfaces will remain focused instead of forcing classes to implement unrelated methods.

## Dependency Inversion Principle

Higher-level business logic will depend on abstractions rather than concrete database or implementation classes where appropriate.

---

# 9. Planned Database

The project will use **SQLite** for persistent data storage.

Database access will be implemented using **JDBC**.

## Planned Tables

The exact schema will be finalized during implementation, but the database is expected to contain tables similar to:

```text
users
quizzes
questions
quiz_questions
quiz_attempts
answers
```

### users

Will store student and teacher information.

### quizzes

Will store quiz configuration and lifecycle information.

### questions

Will store question data.

### quiz_questions

Will represent the relationship between quizzes and questions.

### quiz_attempts

Will store individual student attempts.

### answers

Will store answers submitted during quiz attempts.

---

# 10. Planned Architecture

The application will aim to separate different responsibilities into layers.

```text
┌─────────────────────────────┐
│       Console Interface     │
├─────────────────────────────┤
│       Service Layer         │
├─────────────────────────────┤
│        Domain Model         │
├─────────────────────────────┤
│      Repository Layer       │
├─────────────────────────────┤
│       SQLite Database       │
└─────────────────────────────┘
```

### Console Interface

Responsible for:

* Displaying menus
* Reading user input
* Displaying results

### Service Layer

Responsible for:

* Authentication
* Quiz management
* Quiz attempts
* Scoring
* Business rules

### Domain Model

Responsible for representing:

* Users
* Quizzes
* Questions
* Attempts
* Answers

### Repository Layer

Responsible for:

* Saving data
* Retrieving data
* Updating data
* Deleting data

### Database

SQLite will provide persistent storage.

---

# 11. Planned Project Structure

The final structure will be determined after requirements and domain modeling are completed.

A possible structure is:

```text
src/
├── model/
│   ├── User.java
│   ├── Student.java
│   ├── Teacher.java
│   ├── Quiz.java
│   ├── QuizAttempt.java
│   └── Answer.java
│
├── question/
│   ├── Question.java
│   ├── MCQQuestion.java
│   ├── TrueFalseQuestion.java
│   ├── FillInBlankQuestion.java
│   └── MultipleAnswerQuestion.java
│
├── factory/
│   ├── QuestionFactory.java
│   ├── MCQFactory.java
│   ├── TrueFalseFactory.java
│   ├── FillInBlankFactory.java
│   └── MultipleAnswerFactory.java
│
├── builder/
│   └── QuizBuilder.java
│
├── scoring/
│   ├── ScoringStrategy.java
│   ├── StandardScoring.java
│   ├── NegativeMarkingScoring.java
│   └── TimeBasedScoring.java
│
├── state/
│   ├── QuizState.java
│   ├── DraftState.java
│   ├── PublishedState.java
│   ├── ActiveState.java
│   ├── CompletedState.java
│   └── ArchivedState.java
│
├── observer/
│   ├── QuizObserver.java
│   ├── ScoreBoard.java
│   ├── ProgressTracker.java
│   └── ResultGenerator.java
│
├── repository/
│   ├── UserRepository.java
│   ├── QuizRepository.java
│   ├── QuestionRepository.java
│   └── QuizAttemptRepository.java
│
├── service/
│   ├── AuthenticationService.java
│   ├── QuizService.java
│   ├── QuestionService.java
│   └── QuizAttemptService.java
│
├── database/
│   └── DatabaseConnection.java
│
└── Main.java
```

> This is a proposed structure, not the current implementation.

---

# 12. Planned Application Workflow

## Teacher Workflow

```text
Login
  ↓
Teacher Dashboard
  ↓
Create Quiz
  ↓
Configure Quiz
  ↓
Add Questions
  ↓
Save Quiz
  ↓
Publish Quiz
  ↓
View Student Results
```

## Student Workflow

```text
Login
  ↓
Student Dashboard
  ↓
View Available Quizzes
  ↓
Select Quiz
  ↓
Start Attempt
  ↓
Answer Questions
  ↓
Submit / Time Expires
  ↓
Calculate Score
  ↓
Display Result
  ↓
Save Attempt
```

---

# 13. Planned Quiz Attempt Flow

A typical quiz attempt will follow this process:

1. A student selects a published quiz.
2. The system will verify that the student is allowed to attempt it.
3. A new `QuizAttempt` will be created.
4. The quiz timer will start if a time limit is configured.
5. Questions will be presented to the student.
6. The student will submit answers.
7. The system will evaluate each answer.
8. The selected `ScoringStrategy` will calculate the score.
9. Relevant quiz events will notify registered observers.
10. The attempt will be marked as completed.
11. The result will be stored in the database.
12. The final score and summary will be displayed.

---

# 14. Planned Validation and Business Rules

The system will implement validation rules such as:

* Required fields cannot be empty.
* A quiz must contain at least one question before publication.
* Questions must have valid marks.
* MCQ questions must contain valid options.
* Correct answers must correspond to available options.
* Students cannot attempt unavailable quizzes.
* A completed attempt cannot normally be modified.
* Quiz state transitions must follow valid rules.
* Invalid user input will be handled appropriately.
* Database operations will handle failures safely.

Additional rules may be introduced during implementation.

---

# 15. Planned Testing

The project will include tests for important components and business logic.

Testing will include:

* Question validation
* Question creation
* Quiz construction
* Scoring strategies
* Quiz state transitions
* Answer evaluation
* Score calculation
* Quiz attempt handling
* Repository operations where appropriate

Particular attention will be given to edge cases such as:

* Empty quizzes
* Invalid answers
* Negative marking
* Time expiration
* Duplicate attempts
* Invalid state transitions

---

# 16. Implementation Plan

The project will be developed incrementally.

## Phase 1 — Requirements & Domain Modeling

* Define system requirements.
* Identify actors and use cases.
* Identify domain entities.
* Define business rules.
* Create initial class/domain model.

## Phase 2 — Core Domain Model

* Implement users.
* Implement questions.
* Implement quizzes.
* Implement quiz attempts.
* Implement answers.

## Phase 3 — Factory Method

* Implement question abstractions.
* Implement concrete question types.
* Implement question factories.

## Phase 4 — Builder

* Implement `QuizBuilder`.
* Add configurable quiz construction.
* Validate quiz configuration.

## Phase 5 — Strategy

* Implement scoring abstraction.
* Implement standard scoring.
* Implement negative marking.
* Implement time-based scoring.

## Phase 6 — State

* Implement quiz lifecycle states.
* Implement valid state transitions.
* Enforce state-dependent behavior.

## Phase 7 — Observer

* Implement quiz event handling.
* Implement score tracking.
* Implement progress tracking.
* Implement result-related observers.

## Phase 8 — Database & Repository

* Create SQLite database.
* Create database schema.
* Implement JDBC connection.
* Implement repositories.
* Connect repositories with services.

## Phase 9 — Application Services

* Implement authentication.
* Implement quiz management.
* Implement question management.
* Implement quiz attempts.
* Implement result processing.

## Phase 10 — Console Interface

* Implement menus.
* Implement teacher workflow.
* Implement student workflow.
* Handle user input and validation.

## Phase 11 — Testing & Refactoring

* Add unit tests.
* Test edge cases.
* Review SOLID compliance.
* Refactor code smells.
* Verify design pattern implementations.

---

# 17. Planned Design Goals

The project will prioritize:

* High cohesion
* Low coupling
* Separation of concerns
* Extensibility
* Maintainability
* Reusability
* Testability
* Clear object responsibilities

The primary goal will not be to create the largest possible quiz application.

Instead, the goal will be to create a **small but properly engineered software system** in which the design decisions can be clearly explained and justified.

---

# 18. Requirements

The planned project will require:

* **JDK 17 or later**
* **SQLite**
* **JDBC**
* A Java-compatible IDE or text editor

---

# 19. Planned Technologies

| Technology | Purpose                 |
| ---------- | ----------------------- |
| Java       | Application development |
| SQLite     | Persistent data storage |
| JDBC       | Database connectivity   |
| JUnit      | Unit testing            |
| Git        | Version control         |

---

# 20. Current Project Status

| Component           | Status          |
| ------------------- | --------------- |
| Project idea        | Planned         |
| Requirements        | In progress     |
| Domain model        | Planned         |
| Architecture        | Planned         |
| Database design     | Planned         |
| Factory Method      | Planned         |
| Builder             | Planned         |
| Strategy            | Planned         |
| State               | Planned         |
| Observer            | Planned         |
| User management     | Not implemented |
| Quiz management     | Not implemented |
| Question management | Not implemented |
| Quiz attempts       | Not implemented |
| Scoring             | Not implemented |
| Database            | Not implemented |
| Console interface   | Not implemented |
| Testing             | Not implemented |
| Final application   | Not implemented |

---

# 21. Future Improvements

Depending on the available project time, the following features may be considered as future improvements:

* Question bank management
* Random question selection
* Quiz difficulty-based question selection
* Multiple quiz attempts with configurable limits
* Leaderboard
* Detailed performance statistics
* Import/export of questions
* More question types
* Improved console interface
* Additional scoring strategies

These features will only be considered if they can be implemented without unnecessarily increasing project complexity.

---

# License

This project will be developed for academic purposes as part of a Software Engineering course lab assignment.

```
```
