# UML Class Diagrams & Architecture

## System Architecture

The application adopts a 5-layer enterprise desktop architecture:

```
┌─────────────────────────────────────────────────────────┐
│              JavaFX Presentation Layer                  │
│       Controllers (Login, Teacher, Student, etc.)       │
├─────────────────────────────────────────────────────────┤
│                  Service Layer                          │
│  (Authentication, Quiz, Question, Attempt, Reporting)   │
├─────────────────────────────────────────────────────────┤
│                   Domain Layer                          │
│     Entities, Models, Enums, Design Pattern Objects     │
├─────────────────────────────────────────────────────────┤
│                Repository Layer                         │
│  Interfaces & SQLite Implementations (JDBC Statements)   │
├─────────────────────────────────────────────────────────┤
│                SQLite Persistent DB                     │
└─────────────────────────────────────────────────────────┘
```

---

## 1. Core Domain Model & Repositories (Mermaid)

```mermaid
classDiagram
    class User {
        -int id
        -String name
        -String email
        -String password
        -UserRole role
        -LocalDateTime createdAt
        +getId() int
        +getName() String
        +getEmail() String
        +getRole() UserRole
    }

    class Quiz {
        -int id
        -String title
        -String description
        -int categoryId
        -Difficulty difficulty
        -int timeLimitMinutes
        -int maxAttempts
        -String scoringStrategy
        -QuizStatus status
        -int createdBy
        -boolean shuffleQuestions
        -double totalMarks
        +canBeAttempted() boolean
        +getTimeLimitSeconds() int
    }

    class Question {
        <<abstract>>
        -int id
        -String questionText
        #QuestionType questionType
        -double marks
        -Difficulty difficulty
        -int categoryId
        -List~QuestionOption~ options
        +validateAnswer(String answer)* boolean
        +getCorrectAnswerDisplay()* String
    }

    class QuizAttempt {
        -int id
        -int quizId
        -int studentId
        -LocalDateTime startTime
        -LocalDateTime endTime
        -AttemptStatus status
        -double score
        -double totalMarks
        +getPercentageScore() double
    }

    class Answer {
        -int id
        -int attemptId
        -int questionId
        -String answerValue
        -boolean isCorrect
        -double marksAwarded
    }

    User "1" --> "*" Quiz : creates
    User "1" --> "*" QuizAttempt : attempts
    Quiz "1" --> "*" Question : associates via QuizQuestion
    Quiz "1" --> "*" QuizAttempt : tracks
    QuizAttempt "1" --> "*" Answer : contains
    Question "1" --> "*" Answer : relates to
```

---

## 2. Design Pattern Participants

### 2.1 Strategy Pattern (Scoring)

```mermaid
classDiagram
    class ScoringStrategy {
        <<interface>>
        +calculateScore(answers, totalMarks, timeLimit, timeSpent)* double
        +getStrategyName()* String
        +getDescription()* String
    }

    class StandardScoring {
        +calculateScore(answers, totalMarks, timeLimit, timeSpent) double
    }

    class NegativeMarkingScoring {
        -double penaltyFraction
        +calculateScore(answers, totalMarks, timeLimit, timeSpent) double
    }

    class TimeBasedScoring {
        -StandardScoring standardScoring
        +calculateScore(answers, totalMarks, timeLimit, timeSpent) double
    }

    class ScoringStrategyFactory {
        <<factory>>
        +getStrategy(String name)$ ScoringStrategy
    }

    ScoringStrategy <|.. StandardScoring
    ScoringStrategy <|.. NegativeMarkingScoring
    ScoringStrategy <|.. TimeBasedScoring
    ScoringStrategyFactory ..> ScoringStrategy : creates
```

### 2.2 Factory Method Pattern (Question Hierarchy)

```mermaid
classDiagram
    class Question {
        <<abstract>>
        +validateAnswer(String answer)* boolean
        +getCorrectAnswerDisplay()* String
    }

    class MCQQuestion {
        +validateAnswer(String answer) boolean
        +getCorrectAnswerDisplay() String
    }

    class TrueFalseQuestion {
        +validateAnswer(String answer) boolean
        +getCorrectAnswerDisplay() String
    }

    class FillBlankQuestion {
        -String correctAnswer
        +validateAnswer(String answer) boolean
        +getCorrectAnswerDisplay() String
    }

    class MultipleAnswerQuestion {
        +validateAnswer(String answer) boolean
        +getCorrectAnswerDisplay() String
    }

    class QuestionFactory {
        <<factory>>
        +createQuestion(QuestionType type)$ Question
        +createFromResultSet(ResultSet rs)$ Question
    }

    Question <|-- MCQQuestion
    Question <|-- TrueFalseQuestion
    Question <|-- FillBlankQuestion
    Question <|-- MultipleAnswerQuestion
    QuestionFactory ..> Question : instantiates
```

### 2.3 State Pattern (Quiz Lifecycle)

```mermaid
classDiagram
    class QuizState {
        <<interface>>
        +publish(Quiz quiz)* void
        +unpublish(Quiz quiz)* void
        +archive(Quiz quiz)* void
        +activate(Quiz quiz)* void
        +complete(Quiz quiz)* void
        +canBeAttempted()* boolean
        +canBeEdited()* boolean
        +canAddQuestions()* boolean
    }

    class DraftState {
        +publish(Quiz quiz) void
        +archive(Quiz quiz) void
    }

    class PublishedState {
        +unpublish(Quiz quiz) void
        +activate(Quiz quiz) void
        +archive(Quiz quiz) void
    }

    class ActiveState {
        +complete(Quiz quiz) void
    }

    class CompletedState {
        +archive(Quiz quiz) void
    }

    class ArchivedState {
        +getStateName() String
    }

    class QuizStateManager {
        -Quiz quiz
        -QuizState currentState
        +publish() void
        +unpublish() void
        +archive() void
        +activate() void
        +complete() void
    }

    QuizState <|.. DraftState
    QuizState <|.. PublishedState
    QuizState <|.. ActiveState
    QuizState <|.. CompletedState
    QuizState <|.. ArchivedState
    QuizStateManager --> QuizState : delegates to
    QuizStateManager --> Quiz : modifies status
```

### 2.4 Observer Pattern (Quiz Event Pub-Sub)

```mermaid
classDiagram
    class QuizEventListener {
        <<interface>>
        +onEvent(QuizEvent event, Object data)* void
    }

    class QuizEventPublisher {
        -Map~QuizEvent, List~QuizEventListener~~ listenerMap
        -ReadWriteLock lock
        +subscribe(QuizEvent event, QuizEventListener listener) void
        +unsubscribe(QuizEvent event, QuizEventListener listener) void
        +publish(QuizEvent event, Object data) void
    }

    class ProgressTracker {
        -int totalQuestions
        -int answeredCount
        -double currentScore
        +onEvent(QuizEvent event, Object data) void
        +getProgressPercent() double
    }

    class TimerListener {
        -boolean expired
        +onEvent(QuizEvent event, Object data) void
        +isExpired() boolean
    }

    QuizEventListener <|.. ProgressTracker
    QuizEventListener <|.. TimerListener
    QuizEventPublisher o-- QuizEventListener : notifies
```
