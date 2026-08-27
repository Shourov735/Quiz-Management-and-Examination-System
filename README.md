# Quiz Application

A mini Java software project planned to demonstrate the practical application of object-oriented design patterns in a console-based quiz system.

The project will focus on building a small but well-structured quiz application while demonstrating how common design patterns can be applied to solve specific software design problems.

## Authors

- Md. Shourov - 1609
- Md. Rokib Ikbal - 1605

## Project Overview

The planned application will allow users to take quizzes containing different types of questions, answer questions within a configured time limit, receive scores according to a selected scoring strategy, and view their progress during the quiz.

The project will intentionally remain small in scope and will focus primarily on demonstrating clean object-oriented design and the practical integration of five design patterns.

> **Project Status:** Planning / Not Yet Implemented

## Planned Features

The application is planned to support the following features:

- Multiple question types:
  - Multiple Choice Questions (MCQ)
  - True/False Questions
  - Fill-in-the-Blank Questions
- Configurable quiz settings:
  - Time limit
  - Difficulty level
  - Category
  - Question shuffling
  - Negative marking
- Multiple scoring strategies:
  - Standard fixed-point scoring
  - Timed scoring with speed bonuses
- Live score and progress tracking while taking a quiz
- Centralized management of the active quiz session
- Final score and quiz summary after submission

These features represent the **planned functionality** and are not currently implemented.

## Planned Design Patterns

The project will demonstrate the following five design patterns.

### 1. Factory Method

The Factory Method pattern will be used to create different types of question objects without exposing their instantiation logic to the quiz system.

Planned components:

- `Question` — abstract base class
- `MCQQuestion` — concrete question type
- `TrueFalseQuestion` — concrete question type
- `FillInBlankQuestion` — concrete question type
- `QuestionFactory` — factory abstraction
- `MCQFactory` — factory for MCQ questions
- `TrueFalseFactory` — factory for True/False questions
- `FillInBlankFactory` — factory for Fill-in-the-Blank questions

**Purpose:**

This design will allow new question types to be introduced with minimal changes to the existing quiz logic.

### 2. Builder

The Builder pattern will be used to construct a `Quiz` object with several optional configuration parameters.

Planned components:

- `Quiz` — product
- `QuizBuilder` — builds a `Quiz` step by step

Possible configuration options will include:

- Time limit
- Difficulty
- Category
- Question shuffling
- Negative marking
- Scoring strategy

**Purpose:**

The Builder pattern will keep quiz construction readable and avoid constructors containing a large number of optional parameters.

### 3. Strategy

The Strategy pattern will be used to make scoring algorithms interchangeable.

Planned components:

- `ScoringStrategy` — strategy interface
- `StandardScoring` — fixed points for correct answers
- `TimedScoring` — additional points based on answer speed

**Purpose:**

The scoring behavior will be changeable without modifying the core quiz logic.

### 4. Observer

The Observer pattern will be used to notify components when an important event occurs during a quiz, such as a question being answered.

Planned components:

- `QuizSubject` — observable quiz interface
- `QuizObserver` — observer interface
- `ScoreBoard` — observes score-related changes
- `ProgressTracker` — observes quiz progress

**Purpose:**

This design will decouple the quiz engine from components that need to react to quiz events.

### 5. Singleton

The Singleton pattern will be considered for managing the currently active quiz session.

Planned component:

- `QuizSessionManager` — manages access to the active quiz session

**Purpose:**

The design will provide a single controlled access point for the application's active session.

> **Note:** The final implementation will verify whether Singleton is actually necessary. The pattern should only be retained if it provides a meaningful design benefit rather than being included merely to demonstrate a pattern.

## Planned Project Structure

The project is expected to follow a structure similar to the following:

```text
src/
├── question/
│   ├── Question.java
│   ├── MCQQuestion.java
│   ├── TrueFalseQuestion.java
│   ├── FillInBlankQuestion.java
│   ├── QuestionFactory.java
│   ├── MCQFactory.java
│   ├── TrueFalseFactory.java
│   └── FillInBlankFactory.java
│
├── quiz/
│   ├── Quiz.java
│   └── QuizBuilder.java
│
├── scoring/
│   ├── ScoringStrategy.java
│   ├── StandardScoring.java
│   └── TimedScoring.java
│
├── observer/
│   ├── QuizSubject.java
│   ├── QuizObserver.java
│   ├── ScoreBoard.java
│   └── ProgressTracker.java
│
├── session/
│   └── QuizSessionManager.java
│
└── Main.java