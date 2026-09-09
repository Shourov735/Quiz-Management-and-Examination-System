package com.quizapp.scoring;

import com.quizapp.model.Answer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StandardScoring}.
 */
public class StandardScoringTest {

    private StandardScoring scoring;

    @BeforeEach
    void setUp() {
        scoring = new StandardScoring();
    }

    @Test
    void testPerfectScore() {
        List<Answer> answers = new ArrayList<>();
        Answer a1 = new Answer(1, 101, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(5.0);

        Answer a2 = new Answer(1, 102, "True");
        a2.setCorrect(true);
        a2.setMarksAwarded(5.0);

        answers.add(a1);
        answers.add(a2);

        double score = scoring.calculateScore(answers, 10.0, 600, 300);
        assertEquals(10.0, score, 0.001);
    }

    @Test
    void testZeroScore() {
        List<Answer> answers = new ArrayList<>();
        Answer a1 = new Answer(1, 101, "Wrong");
        a1.setCorrect(false);
        a1.setMarksAwarded(0.0);

        answers.add(a1);

        double score = scoring.calculateScore(answers, 5.0, 600, 300);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void testPartialScore() {
        List<Answer> answers = new ArrayList<>();
        Answer a1 = new Answer(1, 101, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(4.0);

        Answer a2 = new Answer(1, 102, "B");
        a2.setCorrect(false);
        a2.setMarksAwarded(0.0);

        answers.add(a1);
        answers.add(a2);

        double score = scoring.calculateScore(answers, 10.0, 600, 300);
        assertEquals(4.0, score, 0.001);
    }

    @Test
    void testEmptyAnswers() {
        double score = scoring.calculateScore(Collections.emptyList(), 10.0, 600, 300);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void testNullAnswers() {
        double score = scoring.calculateScore(null, 10.0, 600, 300);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void testStrategyName() {
        assertEquals("STANDARD", scoring.getStrategyName());
        assertNotNull(scoring.getDescription());
    }
}
