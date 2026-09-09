package com.quizapp.scoring;

import com.quizapp.model.Answer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimeBasedScoring}.
 */
public class TimeBasedScoringTest {

    @Test
    void testFastBonusWhenFinishingUnderHalfTime() {
        // Base score = 10, timeSpent = 200s, timeLimit = 600s (ratio = 0.33 < 0.5)
        // 1.1x bonus => 11.0, but totalMarks = 15.0 so capped at min(11, 15) = 11.0
        TimeBasedScoring scoring = new TimeBasedScoring();
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(10.0);
        answers.add(a1);

        double score = scoring.calculateScore(answers, 15.0, 600, 200);
        assertEquals(11.0, score, 0.001);
    }

    @Test
    void testMediumBonusWhenFinishingBetweenHalfAndThreeQuarterTime() {
        // Base score = 10, timeSpent = 360s, timeLimit = 600s (ratio = 0.60, between 0.5 and 0.75)
        // 1.05x bonus => 10.5
        TimeBasedScoring scoring = new TimeBasedScoring();
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(10.0);
        answers.add(a1);

        double score = scoring.calculateScore(answers, 15.0, 600, 360);
        assertEquals(10.5, score, 0.001);
    }

    @Test
    void testNoBonusWhenSlow() {
        // ratio = 500 / 600 = 0.83 > 0.75 => no bonus
        TimeBasedScoring scoring = new TimeBasedScoring();
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(10.0);
        answers.add(a1);

        double score = scoring.calculateScore(answers, 15.0, 600, 500);
        assertEquals(10.0, score, 0.001);
    }

    @Test
    void testBonusCappedAtTotalMarks() {
        // Base score = 10.0, totalMarks = 10.0, ratio < 0.5 => 1.1x = 11.0, capped at 10.0
        TimeBasedScoring scoring = new TimeBasedScoring();
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(10.0);
        answers.add(a1);

        double score = scoring.calculateScore(answers, 10.0, 600, 200);
        assertEquals(10.0, score, 0.001);
    }

    @Test
    void testUntimedQuizHasNoBonus() {
        TimeBasedScoring scoring = new TimeBasedScoring();
        List<Answer> answers = new ArrayList<>();

        Answer a1 = new Answer(1, 1, "A");
        a1.setCorrect(true);
        a1.setMarksAwarded(10.0);
        answers.add(a1);

        double score = scoring.calculateScore(answers, 15.0, 0, 100);
        assertEquals(10.0, score, 0.001);
    }
}
