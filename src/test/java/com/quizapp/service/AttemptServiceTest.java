package com.quizapp.service;

import com.quizapp.model.*;
import com.quizapp.observer.QuizEventPublisher;
import com.quizapp.question.MCQQuestion;
import com.quizapp.repository.AnswerRepository;
import com.quizapp.repository.QuestionRepository;
import com.quizapp.repository.QuizAttemptRepository;
import com.quizapp.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AttemptService} rules (attempt limits, scoring, timeout).
 */
@ExtendWith(MockitoExtension.class)
public class AttemptServiceTest {

    @Mock private QuizAttemptRepository attemptRepo;
    @Mock private QuizRepository quizRepo;
    @Mock private QuestionRepository questionRepo;
    @Mock private AnswerRepository answerRepo;
    @Spy  private QuizEventPublisher eventPublisher = new QuizEventPublisher();

    @InjectMocks private AttemptService attemptService;

    private Quiz sampleQuiz;

    @BeforeEach
    void setUp() {
        sampleQuiz = new Quiz();
        sampleQuiz.setId(5);
        sampleQuiz.setTitle("OOP Quiz");
        sampleQuiz.setStatus(QuizStatus.PUBLISHED);
        sampleQuiz.setMaxAttempts(2);
        sampleQuiz.setScoringStrategy("STANDARD");
        sampleQuiz.setTotalMarks(10.0);
    }

    @Test
    void testStartAttemptAllowed() {
        when(quizRepo.findById(5)).thenReturn(Optional.of(sampleQuiz));
        when(attemptRepo.countCompletedAttempts(1, 5)).thenReturn(0);
        when(attemptRepo.save(any(QuizAttempt.class))).thenReturn(100);

        QuizAttempt attempt = attemptService.startAttempt(1, 5);
        assertNotNull(attempt);
        assertEquals(100, attempt.getId());
        assertEquals(AttemptStatus.IN_PROGRESS, attempt.getStatus());
        verify(attemptRepo).save(any(QuizAttempt.class));
    }

    @Test
    void testStartAttemptExceedsMaxAttemptsThrows() {
        when(quizRepo.findById(5)).thenReturn(Optional.of(sampleQuiz));
        when(attemptRepo.countCompletedAttempts(1, 5)).thenReturn(2);

        assertThrows(IllegalStateException.class, () -> attemptService.startAttempt(1, 5));
    }

    @Test
    void testStartAttemptOnDraftQuizThrows() {
        sampleQuiz.setStatus(QuizStatus.DRAFT);
        when(quizRepo.findById(5)).thenReturn(Optional.of(sampleQuiz));

        assertThrows(IllegalStateException.class, () -> attemptService.startAttempt(1, 5));
    }

    @Test
    void testSubmitAttemptCalculatesScore() {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(100);
        attempt.setQuizId(5);
        attempt.setStudentId(1);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartTime(LocalDateTime.now().minusMinutes(5));

        when(attemptRepo.findById(100)).thenReturn(Optional.of(attempt));
        when(quizRepo.findById(5)).thenReturn(Optional.of(sampleQuiz));

        MCQQuestion q = new MCQQuestion();
        q.setId(10);
        q.setMarks(5.0);
        QuestionOption opt = new QuestionOption(1, 10, "Inheritance", true, 1);
        q.setOptions(List.of(opt));
        when(questionRepo.findByQuizId(5)).thenReturn(List.of(q));

        Answer ans = new Answer(100, 10, "Inheritance");
        when(answerRepo.findByAttemptId(100)).thenReturn(List.of(ans));

        QuizAttempt finished = attemptService.submitAttempt(100);
        assertEquals(AttemptStatus.SUBMITTED, finished.getStatus());
        assertEquals(5.0, finished.getScore(), 0.001);
        verify(attemptRepo).update(attempt);
    }

    @Test
    void testTimeoutAttemptFinalisesWithTimeoutStatus() {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(101);
        attempt.setQuizId(5);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartTime(LocalDateTime.now().minusMinutes(35));

        when(attemptRepo.findById(101)).thenReturn(Optional.of(attempt));
        when(quizRepo.findById(5)).thenReturn(Optional.of(sampleQuiz));
        when(questionRepo.findByQuizId(5)).thenReturn(List.of());
        when(answerRepo.findByAttemptId(101)).thenReturn(List.of());

        QuizAttempt finished = attemptService.timeoutAttempt(101);
        assertEquals(AttemptStatus.TIMED_OUT, finished.getStatus());
        verify(attemptRepo).update(attempt);
    }
}
