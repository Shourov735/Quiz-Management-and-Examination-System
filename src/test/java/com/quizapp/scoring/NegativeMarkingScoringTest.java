package com.quizapp.scoring;

import com.quizapp.model.Answer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NegativeMarkingScoring}.
 */
public class NegativeMarkingScoringTest {

    @Test
    void testAllCorrectNoPenalty() {
        NegativeMarkingScoring scoring = new NegativeMarkingScoring(0.25);
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(5.0);

        Answer a2 = new Answer(1, 2, "B");
        a2.setCorrect(true);
        a2.setMarksAwarded(5.0);

        answers.add(a1);
        answers.add(a2);

        double score = scoring.calculateScore(answers, 10.0, 600, 300);
        assertEquals(10.0, score, 0.001);
    }

    @Test
    void testPenaltyAppliedForWrongAnswer() {
        // 2 questions, total 10 marks => avgMarks = 5.0
        // 1 correct (5 marks), 1 wrong (penalty = 1 * 0.25 * 5.0 = 1.25)
        // expected score = 5.0 - 1.25 = 3.75
        NegativeMarkingScoring scoring = new NegativeMarkingScoring(0.25);
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(5.0);

        Answer a2 = new Answer(1, 2, "Wrong");
        a2.setCorrect(false);
        a2.setMarksAwarded(0.0);

        answers.add(a1);
        answers.add(a2);

        double score = scoring.calculateScore(answers, 10.0, 600, 300);
        assertEquals(3.75, score, 0.001);
    }

    @Test
    void testScoreFlooredAtZero() {
        // 2 questions, all wrong -> penalty would make score negative, but clamped to 0.0
        NegativeMarkingScoring scoring = new NegativeMarkingScoring(0.5);
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "Wrong1");
        a1.setCorrect(false);
        Answer a2 = new Answer(1, 2, "Wrong2");
        a2.setCorrect(false);

        answers.add(a1);
        answers.add(a2);

        double score = scoring.calculateScore(answers, 10.0, 600, 300);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void testCustomPenaltyFractionValidation() {
        assertThrows(IllegalArgumentException.class, () -> new NegativeMarkingScoring(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new NegativeMarkingScoring(1.5));
        NegativeMarkingScoring valid = new NegativeMarkingScoring(0.33);
        assertEquals(0.33, valid.getPenaltyFraction(), 0.001);
    }
}
