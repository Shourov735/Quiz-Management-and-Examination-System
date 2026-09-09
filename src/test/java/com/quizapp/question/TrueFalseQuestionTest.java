package com.quizapp.question;

import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TrueFalseQuestion}.
 */
public class TrueFalseQuestionTest {

    private TrueFalseQuestion question;

    @BeforeEach
    void setUp() {
        question = new TrueFalseQuestion();
        question.setQuestionText("Java is purely an interpreted language.");
        question.setMarks(1.0);

        QuestionOption o1 = new QuestionOption(1, 0, "True", false, 1);
        QuestionOption o2 = new QuestionOption(2, 0, "False", true, 2);
        question.setOptions(List.of(o1, o2));
    }

    @Test
    void testQuestionTypeIsTrueFalse() {
        assertEquals(QuestionType.TRUE_FALSE, question.getQuestionType());
    }

    @Test
    void testValidateCorrectAnswer() {
        assertTrue(question.validateAnswer("False"));
        assertTrue(question.validateAnswer("false"));
        assertTrue(question.validateAnswer("FALSE"));
        assertTrue(question.validateAnswer(" false "));
    }

    @Test
    void testValidateWrongAnswer() {
        assertFalse(question.validateAnswer("True"));
        assertFalse(question.validateAnswer("true"));
    }

    @Test
    void testValidateInvalidAnswer() {
        assertFalse(question.validateAnswer("Maybe"));
        assertFalse(question.validateAnswer(""));
        assertFalse(question.validateAnswer(null));
    }

    @Test
    void testGetCorrectAnswerDisplay() {
        assertEquals("False", question.getCorrectAnswerDisplay());
    }

    @Test
    void testIsCorrectValueTrue() {
        assertFalse(question.isCorrectValueTrue());
    }
}
