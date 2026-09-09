# Design Decisions & Design Patterns Rationale

This document details the software design decisions and architectural patterns applied in the **Quiz Management & Examination System**, adhering to the course guidelines.

---

## 1. Strategy Pattern — Scoring Mechanisms

### Problem
Different examinations require distinct scoring rules:
- Traditional examinations award flat marks for correct answers and zero for wrong answers.
- Competitive or stringent examinations enforce negative marking penalties to deter random guessing.
- Rapid-fire assessments provide speed bonuses for finishing ahead of schedule.

If all scoring variations were embedded in a single evaluation method or class using nested `if-else` or `switch` statements, the evaluation engine would violate the **Open/Closed Principle (OCP)** and become brittle as new scoring formulas are added.

### Motivation
Evaluation algorithms vary independently of the quiz attempt workflow. Decoupling the evaluation algorithm from the orchestration service makes scoring logic isolated, unit-testable, and pluggable.

### Selected Pattern
**Strategy Pattern**:
- **Strategy Interface**: `ScoringStrategy` defining `calculateScore(List<Answer> answers, double totalMarks, int timeLimitSeconds, int timeSpentSeconds)`.
- **Concrete Strategies**:
  - `StandardScoring`: $\sum \text{marksAwarded}$ for correct answers.
  - `NegativeMarkingScoring`: Deducts a configurable penalty fraction of the average marks per question for each wrong answer ($\text{score} \ge 0$).
  - `TimeBasedScoring`: Evaluates standard marks and applies speed bonuses ($1.10\times$ for $<50\%$ time, $1.05\times$ for $<75\%$ time, capped at total possible marks).
- **Strategy Factory**: `ScoringStrategyFactory` resolves the appropriate strategy dynamically by name.

### Alternatives Considered
1. **Conditional Statements (`if-else` in AttemptService)**: Simple initially, but leads to high cyclomatic complexity, tight coupling, and modification of existing business code whenever a grading policy changes.
2. **Inheritance hierarchy on `Quiz` (e.g., `NegativeMarkingQuiz`)**: Leads to class explosion if combined with other quiz dimensions (e.g., Timed vs Untimed, Shuffled vs Ordered).

### Trade-offs
Introduces small additional classes and an interface. The trade-off is negligible compared to the architectural clarity gained.

### Future Benefits
Introducing future scoring models (e.g., `PartialCreditScoring` for multiple answers, `DifficultyWeightedScoring`, or `PercentileCurveScoring`) requires only adding a new `ScoringStrategy` implementation without altering `AttemptService` or the examination workflow.

---

## 2. State Pattern — Quiz Lifecycle

### Problem
A quiz transitions through distinct phases throughout its lifecycle:
```text
DRAFT  →  PUBLISHED  →  ACTIVE  →  COMPLETED  →  ARCHIVED
```
The allowed operations depend fundamentally on the current state:
- Questions can only be added, removed, or edited when a quiz is in `DRAFT`.
- A quiz cannot be attempted by students unless it is `PUBLISHED` or `ACTIVE`.
- An `ARCHIVED` quiz cannot accept edits or new attempts.

Relying on scattered `if (status == QuizStatus.DRAFT)` checks across controllers and services leads to duplicate validation logic and dangerous illegal state transitions.

### Motivation
Encapsulating state-dependent behavior and valid transitions into distinct state objects ensures that invalid operations are rejected consistently at the domain level.

### Selected Pattern
**State Pattern**:
- **State Interface**: `QuizState` defining lifecycle transitions (`publish`, `unpublish`, `archive`, `activate`, `complete`) and query methods (`canBeAttempted()`, `canBeEdited()`, `canAddQuestions()`).
- **Concrete States**:
  - `DraftState`: Allows editing and adding questions; transitions to `PublishedState` or `ArchivedState`.
  - `PublishedState`: Disallows content editing to preserve exam integrity; permits student attempts; transitions to `DraftState` (unpublish) or `ActiveState` or `ArchivedState`.
  - `ActiveState`: Represents currently ongoing exams; transitions to `CompletedState`.
  - `CompletedState`: Archived or finalized; transitions to `ArchivedState`.
  - `ArchivedState`: Terminal state; throws `IllegalStateException` on modification attempts.
- **Context**: `QuizStateManager` maintains the current state and synchronizes `Quiz#status`.

### Alternatives Considered
1. **Enum flags and switch blocks**: Scattering switch statements in every service method. Prone to missed edge cases when new states are introduced.

### Trade-offs
Requires creating state classes. However, it completely eliminates fragile status checking in the business layer and guarantees invalid transitions fail fast.

### Future Benefits
Adding administrative review states (e.g., `PENDING_MODERATION`, `SUSPENDED_INVESTIGATION`) only requires defining new state classes with their allowed transitions.

---

## 3. Factory Method Pattern — Polymorphic Question Types

### Problem
The application supports multiple distinct question types:
- Multiple Choice (MCQ) — exactly one correct option.
- True/False — binary truth value.
- Fill in the Blank — text matching with case/whitespace trimming.
- Multiple Answer — set comparison of all correct selections.

Each question type has different data attributes, validation algorithms, and UI presentation representations. Hardcoding `new MCQQuestion()` or `new TrueFalseQuestion()` throughout repository and service layers creates tight coupling to concrete types.

### Motivation
Abstracting question creation allows database persistence and user interface layers to instantiate questions polymorphically based on the `question_type` column without knowing implementation specifics.

### Selected Pattern
**Factory Method Pattern**:
- **Abstract Product**: `Question` declaring `validateAnswer(String answer)` and `getCorrectAnswerDisplay()`.
- **Concrete Products**: `MCQQuestion`, `TrueFalseQuestion`, `FillBlankQuestion`, `MultipleAnswerQuestion`.
- **Creator / Factory**: `QuestionFactory` with factory methods:
  - `createQuestion(QuestionType type)`
  - `createFromResultSet(ResultSet rs)` (polymorphic database row mapper)

### Alternatives Considered
1. **Single generic `Question` class with type flags and null fields**: A monolithic class with nullable columns (`optA, optB, isTrue, textAnswer`) creates antipatterns and violates SRP.
2. **Direct constructor calls**: Violates the Dependency Inversion Principle and causes code duplication when deserializing from SQLite.

### Trade-offs
Subclassing requires maintaining separate classes, but each class is small, focused, and cohesive.

### Future Benefits
Adding new question types like `CodeSnippetQuestion`, `OrderingQuestion`, or `MatchingPairsQuestion` requires only extending `Question` and adding an entry to `QuestionFactory`.

---

## 4. Observer Pattern — Quiz Session Events

### Problem
During an active examination session, multiple decoupled concerns must react to examination events:
- When an answer is submitted, the `ProgressTracker` updates answered counts and accuracy.
- When the timer expires, the session listener automatically finalizes the attempt.
- When a quiz attempt completes, reporting and leaderboard listeners can trigger calculations without tightly coupling the quiz execution engine to the UI or reporting subsystem.

### Motivation
Tightly coupling `AttemptService` to GUI progress bars, timer listeners, and analytics would violate the **Single Responsibility Principle (SRP)**.

### Selected Pattern
**Observer Pattern**:
- **Subject / Publisher**: `QuizEventPublisher` providing a thread-safe publish-subscribe event bus (`subscribe`, `unsubscribe`, `publish`) guarded by `ReentrantReadWriteLock`.
- **Events**: `QuizEvent` enum (`ATTEMPT_STARTED`, `ANSWER_SUBMITTED`, `SCORE_UPDATED`, `QUIZ_COMPLETED`, `TIME_EXPIRED`).
- **Observer Interface**: `QuizEventListener` with `onEvent(QuizEvent event, Object data)`.
- **Concrete Observers**:
  - `ProgressTracker`: Tracks completed question count, running score, and percentage.
  - `TimerListener`: Monitors session expiration.

### Alternatives Considered
1. **Direct callback references / hardcoded method calls**: Calling GUI updates directly from services causes thread synchronization issues and leaks presentation concerns into the application layer.

### Trade-offs
Event publishers require careful handling of listener registration, which is handled cleanly in session lifecycle management.

### Future Benefits
New listeners can easily be attached in the future, such as an audit logger (`AuditLogListener`), auto-save notification, or remote proctoring alert monitor.

---

## 5. Singleton Pattern — SQLite Connection Manager (Justified)

### Problem
SQLite is an embedded, file-based database. Creating multiple uncoordinated write connections to the same `.db` file in a single JVM causes `SQLITE_BUSY` locking errors and degrades performance.

### Motivation
A single shared connection manager coordinates access, enables `PRAGMA foreign_keys = ON`, runs migration/seeding on startup, and provides connection pooling.

### Selected Implementation
- `DatabaseConnection` with thread-safe double-checked locking (`volatile instance`).
- Enforces private constructor and single JDBC lifecycle.
- Course guideline compliance: Singleton is restricted exclusively to this shared physical resource manager and avoided for all business services and repositories.

---

## 6. Repository Pattern — Persistence Abstraction

### Problem
Directly writing raw SQL queries inside JavaFX controllers or business services couples the application tightly to SQLite and makes unit testing impossible without an active database.

### Selected Pattern
**Repository Pattern**:
- Interfaces: `UserRepository`, `QuizRepository`, `QuestionRepository`, `QuizAttemptRepository`, `AnswerRepository`, `CategoryRepository`.
- Implementations: `SQLiteUserRepository`, `SQLiteQuizRepository`, `SQLiteQuestionRepository`, etc.
- Inversion of Control: Services take repository interfaces via constructor injection. In unit tests, Mockito mocks are passed seamlessly.

---

## Summary of SOLID Principles Applied

1. **Single Responsibility Principle (SRP)**: Each class has a single, well-defined purpose (e.g. `AuthenticationService` handles user credentials; `StandardScoring` computes standard scores; `SQLiteQuizRepository` handles SQL mapping).
2. **Open/Closed Principle (OCP)**: New scoring algorithms and question types are added by creating new classes, not editing existing evaluation loops.
3. **Liskov Substitution Principle (LSP)**: All `Question` subclasses (`MCQQuestion`, `TrueFalseQuestion`, etc.) can be substituted wherever `Question` is expected without unexpected side effects.
4. **Interface Segregation Principle (ISP)**: Focused interfaces (`ScoringStrategy`, `QuizEventListener`, `QuizState`) prevent clients from depending on methods they do not use.
5. **Dependency Inversion Principle (DIP)**: High-level modules (`QuizService`, `AttemptService`) depend on abstractions (`QuizRepository`, `ScoringStrategy`, `QuizEventListener`), not low-level SQLite drivers.
