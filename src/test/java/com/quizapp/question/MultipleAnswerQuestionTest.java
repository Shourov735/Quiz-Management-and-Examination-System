package com.quizapp.question;

import com.quizapp.model.QuestionOption;
import com.quizapp.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MultipleAnswerQuestion}.
 */
public class MultipleAnswerQuestionTest {

    private MultipleAnswerQuestion question;

    @BeforeEach
    void setUp() {
        question = new MultipleAnswerQuestion();
        question.setQuestionText("Select all Gang of Four Creational Patterns:");
        question.setMarks(3.0);

        QuestionOption o1 = new QuestionOption(1, 0, "Singleton", true, 1);
        QuestionOption o2 = new QuestionOption(2, 0, "Factory Method", true, 2);
        QuestionOption o3 = new QuestionOption(3, 0, "Observer", false, 3);
        QuestionOption o4 = new QuestionOption(4, 0, "Strategy", false, 4);

        question.setOptions(List.of(o1, o2, o3, o4));
    }

    @Test
    void testQuestionTypeIsMultipleAnswer() {
        assertEquals(QuestionType.MULTIPLE_ANSWER, question.getQuestionType());
    }

    @Test
    void testValidateAllCorrectOptionsSelected() {
        assertTrue(question.validateAnswer("Singleton,Factory Method"));
        assertTrue(question.validateAnswer("Factory Method, Singleton"));
        assertTrue(question.validateAnswer("singleton , factory method"));
    }

    @Test
    void testValidatePartialSelectionFails() {
        assertFalse(question.validateAnswer("Singleton"));
        assertFalse(question.validateAnswer("Factory Method"));
    }

    @Test
    void testValidateExtraIncorrectOptionFails() {
        assertFalse(question.validateAnswer("Singleton, Factory Method, Observer"));
    }

    @Test
    void testValidateEmptyAnswerFails() {
        assertFalse(question.validateAnswer(""));
        assertFalse(question.validateAnswer(null));
    }
}
