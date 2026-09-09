package com.quizapp.question;

import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MCQQuestion}.
 */
public class MCQQuestionTest {

    private MCQQuestion question;

    @BeforeEach
    void setUp() {
        question = new MCQQuestion();
        question.setQuestionText("What is the capital of France?");
        question.setMarks(2.0);

        QuestionOption o1 = new QuestionOption(1, 0, "London", false, 1);
        QuestionOption o2 = new QuestionOption(2, 0, "Paris", true, 2);
        QuestionOption o3 = new QuestionOption(3, 0, "Berlin", false, 3);
        QuestionOption o4 = new QuestionOption(4, 0, "Madrid", false, 4);

        question.setOptions(List.of(o1, o2, o3, o4));
    }

    @Test
    void testQuestionTypeIsMCQ() {
        assertEquals(QuestionType.MCQ, question.getQuestionType());
    }

    @Test
    void testValidateCorrectAnswerByText() {
        assertTrue(question.validateAnswer("Paris"));
        assertTrue(question.validateAnswer("paris"));
        assertTrue(question.validateAnswer(" PARIS "));
    }

    @Test
    void testValidateCorrectAnswerById() {
        assertTrue(question.validateAnswer("2"));
    }

    @Test
    void testValidateWrongAnswer() {
        assertFalse(question.validateAnswer("London"));
        assertFalse(question.validateAnswer("1"));
        assertFalse(question.validateAnswer("Rome"));
    }

    @Test
    void testValidateEmptyAnswer() {
        assertFalse(question.validateAnswer(""));
        assertFalse(question.validateAnswer(null));
        assertFalse(question.validateAnswer("   "));
    }

    @Test
    void testGetCorrectAnswerDisplay() {
        assertEquals("Paris", question.getCorrectAnswerDisplay());
    }
}
