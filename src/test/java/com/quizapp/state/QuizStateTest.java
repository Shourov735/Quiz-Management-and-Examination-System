package com.quizapp.state;

import com.quizapp.model.Quiz;
import com.quizapp.model.QuizStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Quiz State transitions using the State Pattern.
 */
public class QuizStateTest {

    private Quiz quiz;
    private QuizStateManager manager;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();
        quiz.setId(1);
        quiz.setTitle("Design Patterns Quiz");
        quiz.setStatus(QuizStatus.DRAFT);
        manager = new QuizStateManager(quiz);
    }

    @Test
    void testInitialDraftState() {
        assertInstanceOf(DraftState.class, manager.getState());
        assertTrue(manager.getState().canBeEdited());
        assertTrue(manager.getState().canAddQuestions());
        assertFalse(manager.getState().canBeAttempted());
    }

    @Test
    void testPublishFromDraft() {
        manager.publish();
        assertEquals(QuizStatus.PUBLISHED, quiz.getStatus());
        assertInstanceOf(PublishedState.class, manager.getState());
        assertTrue(manager.getState().canBeAttempted());
        assertFalse(manager.getState().canBeEdited());
    }

    @Test
    void testUnpublishFromPublished() {
        manager.publish();
        manager.unpublish();
        assertEquals(QuizStatus.DRAFT, quiz.getStatus());
        assertInstanceOf(DraftState.class, manager.getState());
        assertTrue(manager.getState().canBeEdited());
    }

    @Test
    void testArchiveFromPublished() {
        manager.publish();
        manager.archive();
        assertEquals(QuizStatus.ARCHIVED, quiz.getStatus());
        assertInstanceOf(ArchivedState.class, manager.getState());
        assertFalse(manager.getState().canBeAttempted());
        assertFalse(manager.getState().canBeEdited());
    }

    @Test
    void testCannotPublishFromArchived() {
        quiz.setStatus(QuizStatus.ARCHIVED);
        QuizStateManager archivedManager = new QuizStateManager(quiz);
        assertThrows(IllegalStateException.class, archivedManager::publish);
    }

    @Test
    void testCannotUnpublishFromArchived() {
        quiz.setStatus(QuizStatus.ARCHIVED);
        QuizStateManager archivedManager = new QuizStateManager(quiz);
        assertThrows(IllegalStateException.class, archivedManager::unpublish);
    }

    @Test
    void testActivateAndCompleteLifecycle() {
        manager.publish();
        manager.activate();
        assertEquals(QuizStatus.ACTIVE, quiz.getStatus());
        assertInstanceOf(ActiveState.class, manager.getState());

        manager.complete();
        assertEquals(QuizStatus.COMPLETED, quiz.getStatus());
        assertInstanceOf(CompletedState.class, manager.getState());
    }
}
