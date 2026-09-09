package com.quizapp.service;

import com.quizapp.model.*;
import com.quizapp.question.MCQQuestion;
import com.quizapp.repository.CategoryRepository;
import com.quizapp.repository.QuestionRepository;
import com.quizapp.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link QuizService} business logic and validation rules.
 */
@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock private QuizRepository quizRepo;
    @Mock private QuestionRepository questionRepo;
    @Mock private CategoryRepository categoryRepo;

    @InjectMocks private QuizService quizService;

    private Quiz sampleQuiz;

    @BeforeEach
    void setUp() {
        sampleQuiz = new Quiz();
        sampleQuiz.setId(10);
        sampleQuiz.setTitle("Java Fundamentals");
        sampleQuiz.setStatus(QuizStatus.DRAFT);
    }

    @Test
    void testCreateQuizValid() {
        when(quizRepo.save(any(Quiz.class))).thenReturn(10);

        Quiz created = quizService.createQuiz(
                "Java Fundamentals",
                "Basic Java test",
                1,
                Difficulty.EASY,
                30,
                2,
                "STANDARD",
                1
        );

        assertNotNull(created);
        assertEquals("Java Fundamentals", created.getTitle());
        assertEquals(QuizStatus.DRAFT, created.getStatus());
        verify(quizRepo, times(1)).save(any(Quiz.class));
    }

    @Test
    void testCreateQuizBlankTitleThrows() {
        assertThrows(IllegalArgumentException.class, () -> quizService.createQuiz(
                "",
                "Description",
                1,
                Difficulty.EASY,
                30,
                1,
                "STANDARD",
                1
        ));
    }

    @Test
    void testPublishQuizWithQuestionsSuccess() {
        when(quizRepo.findById(10)).thenReturn(Optional.of(sampleQuiz));

        MCQQuestion q = new MCQQuestion();
        q.setId(1);
        q.setMarks(2.0);
        when(questionRepo.findByQuizId(10)).thenReturn(List.of(q));
        when(quizRepo.update(any(Quiz.class))).thenReturn(true);

        Quiz published = quizService.publishQuiz(10);
        assertEquals(QuizStatus.PUBLISHED, published.getStatus());
        verify(quizRepo).update(published);
    }

    @Test
    void testPublishQuizWithoutQuestionsThrows() {
        when(quizRepo.findById(10)).thenReturn(Optional.of(sampleQuiz));
        when(questionRepo.findByQuizId(10)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> quizService.publishQuiz(10));
    }

    @Test
    void testDeleteDraftQuiz() {
        when(quizRepo.findById(10)).thenReturn(Optional.of(sampleQuiz));
        when(quizRepo.delete(10)).thenReturn(true);

        boolean deleted = quizService.deleteQuiz(10);
        assertTrue(deleted);
        verify(quizRepo).delete(10);
    }

    @Test
    void testDeletePublishedQuizThrows() {
        sampleQuiz.setStatus(QuizStatus.PUBLISHED);
        when(quizRepo.findById(10)).thenReturn(Optional.of(sampleQuiz));

        assertThrows(IllegalStateException.class, () -> quizService.deleteQuiz(10));
    }
}
