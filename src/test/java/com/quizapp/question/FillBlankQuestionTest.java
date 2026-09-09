package com.quizapp.question;

import com.quizapp.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FillBlankQuestion}.
 */
public class FillBlankQuestionTest {

    private FillBlankQuestion question;

    @BeforeEach
    void setUp() {
        question = new FillBlankQuestion();
        question.setQuestionText("Complete: The 'O' in SOLID stands for the ________ Principle.");
        question.setCorrectAnswer("Open-Closed");
        question.setMarks(2.0);
    }

    @Test
    void testQuestionTypeIsFillBlank() {
        assertEquals(QuestionType.FILL_BLANK, question.getQuestionType());
    }

    @Test
    void testExactMatch() {
        assertTrue(question.validateAnswer("Open-Closed"));
    }

    @Test
    void testCaseInsensitiveAndTrimmedMatch() {
        assertTrue(question.validateAnswer("open-closed"));
        assertTrue(question.validateAnswer(" OPEN-CLOSED "));
    }

    @Test
    void testWrongAnswer() {
        assertFalse(question.validateAnswer("Single Responsibility"));
        assertFalse(question.validateAnswer(""));
        assertFalse(question.validateAnswer(null));
    }

    @Test
    void testGetCorrectAnswerDisplay() {
        assertEquals("Open-Closed", question.getCorrectAnswerDisplay());
    }
}
