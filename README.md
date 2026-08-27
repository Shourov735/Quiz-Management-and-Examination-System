# Quiz Application

A mini Java software project developed to demonstrate the practical application of object-oriented design patterns in a console-based quiz system.

## Authors

- Md. Shourov-1609
- Md. Rokib Ikbal - 1605

## Project Overview

This project implements a quiz application that allows users to take a quiz consisting of multiple question types, receive a score based on a configurable scoring strategy, and view live progress as the quiz is attempted. The project is intentionally scoped as a mini-project, focusing on clean integration of five design patterns rather than breadth of features.

## Features

- Support for multiple question types: Multiple Choice, True/False, and Fill in the Blank
- Configurable quiz setup: time limit, difficulty level, category, question shuffling, and negative marking
- Pluggable scoring strategies: standard fixed-point scoring and timed scoring with speed bonuses
- Live scoreboard and progress tracking as questions are answered
- Centralized session management for the active quiz

## Design Patterns Used

### 1. Factory Method
Used to create question objects without exposing instantiation logic to the client.

- `Question` — abstract base class
- `MCQQuestion`, `TrueFalseQuestion`, `FillInBlankQuestion` — concrete question types
- `QuestionFactory` — abstract factory
- `MCQFactory`, `TrueFalseFactory`, `FillInBlankFactory` — concrete factories

**Why:** New question types can be added without modifying existing quiz logic.

### 2. Builder
Used to construct a `Quiz` object with several optional configuration parameters.

- `Quiz` — the product
- `QuizBuilder` — constructs a `Quiz` step by step

**Why:** Avoids a constructor with a large number of optional parameters and keeps quiz assembly readable.

### 3. Strategy
Used to make scoring rules interchangeable.

- `ScoringStrategy` — interface
- `StandardScoring` — fixed points per correct answer
- `TimedScoring` — bonus points based on answer speed

**Why:** Scoring behavior can be changed at runtime without altering the quiz engine.

### 4. Observer
Used to update dependent components whenever a question is answered.

- `QuizSubject` — interface for the observable quiz
- `QuizObserver` — interface for observers
- `ScoreBoard`, `ProgressTracker` — concrete observers

**Why:** Decouples the quiz engine from the components that react to its state changes.

### 5. Singleton
Used to manage the single active quiz session.

- `QuizSessionManager` — holds and provides access to the current session state

**Why:** Ensures only one active session exists and provides a single, controlled access point to it.

## Project Structure

```
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
├── quiz/
│   ├── Quiz.java
│   └── QuizBuilder.java
├── scoring/
│   ├── ScoringStrategy.java
│   ├── StandardScoring.java
│   └── TimedScoring.java
├── observer/
│   ├── QuizSubject.java
│   ├── QuizObserver.java
│   ├── ScoreBoard.java
│   └── ProgressTracker.java
├── session/
│   └── QuizSessionManager.java
└── Main.java
```

## How It Works

1. `QuizSessionManager` initializes a single active session.
2. `QuestionFactory` implementations create the required question objects.
3. `QuizBuilder` assembles a `Quiz` object with the selected questions and configuration.
4. A `ScoringStrategy` is assigned to the quiz to determine how answers are scored.
5. As the user answers questions, `Quiz` notifies its registered observers (`ScoreBoard`, `ProgressTracker`), which update accordingly.
6. At the end of the quiz, the final score and summary are displayed.

## How to Run

```bash
javac -d out src/**/*.java
java -cp out Main
```

## Requirements

- JDK 17 or later

## License

This project was developed for academic purposes as part of a Software Engineering course lab assignment.
