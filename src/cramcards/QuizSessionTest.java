// Project 2 fall 2026
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those who
// do.
// -- Aditya Banerjee (adityab7)

// LLM Statement:
//
// During the preparation of this assignment, I, Aditya Banerjee, used Claude
// (Anthropic) to write this project's code and JUnit tests from our
// Deliverable 1 scope and Deliverable 2 specification. After using this
// tool, I reviewed and edited the content as needed to ensure its accuracy
// and take full responsibility for the content in relation to grading. I
// understand that I am responsible for being able to complete this work
// without the use of assistance.

package cramcards;

import java.util.Random;

/**
 * Tests for QuizSession. A seeded Random makes draws repeat between runs;
 * the tests answer by looking at the drawn card, so they don't depend on
 * the draw order.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class QuizSessionTest extends student.TestCase {

    /** Seed used for every quiz in these tests. */
    private static final int SEED = 42;

    private Deck deck;
    private QuizSession session;

    /**
     * Builds a three-card deck and a session on it before each test.
     */
    public void setUp() {
        deck = new Deck();
        deck.addCard(new Flashcard("2+2", "4"));
        deck.addCard(new Flashcard("3+3", "6"));
        deck.addCard(new Flashcard("Capital of VA?", "Richmond"));
        session = new QuizSession(deck, new Random(SEED));
    }


    /**
     * Tests the starting state and that the deck is not changed.
     */
    public void testConstructor() {
        assertTrue(session.hasNextCard());
        assertEquals(3, session.getDeckSize());
        assertEquals(0, session.getCorrectFirstTry());
        assertEquals(0, session.getMissedCount());
        assertEquals(0, session.getScorePercent());
        assertEquals(3, deck.size());
    }


    /**
     * Tests that an empty deck, a null deck, and a null Random are rejected.
     */
    public void testConstructorBadInput() {
        Exception empty = null;
        try {
            new QuizSession(new Deck(), new Random(SEED));
        }
        catch (Exception exception) {
            empty = exception;
        }
        assertTrue(empty instanceof IllegalArgumentException);
        assertEquals(QuizSession.EMPTY_DECK_MESSAGE, empty.getMessage());

        Exception nullDeck = null;
        try {
            new QuizSession(null, new Random(SEED));
        }
        catch (Exception exception) {
            nullDeck = exception;
        }
        assertTrue(nullDeck instanceof IllegalArgumentException);

        Exception nullRandom = null;
        try {
            new QuizSession(deck, null);
        }
        catch (Exception exception) {
            nullRandom = exception;
        }
        assertTrue(nullRandom instanceof IllegalArgumentException);
    }


    /**
     * Tests that answering every card right draws each card once, then the
     * pile is empty and drawing again throws.
     */
    public void testNextCardAllRight() {
        int draws = 0;
        while (session.hasNextCard()) {
            Flashcard card = session.nextCard();
            assertTrue(session.submitAnswer(card, card.getAnswer()));
            draws++;
        }
        assertEquals(3, draws);
        assertEquals(100, session.getScorePercent());
        assertEquals(3, deck.size());

        Exception thrown = null;
        try {
            session.nextCard();
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalStateException);
        assertEquals(QuizSession.NO_CARDS_LEFT_MESSAGE, thrown.getMessage());
    }


    /**
     * Tests that a wrong answer puts a retry copy back and that the retry
     * doesn't earn a point: miss one card once, answer everything else
     * right, so the score is 2 of 3 = 66.
     */
    public void testSubmitAnswerWrongThenRetry() {
        boolean missedOnce = false;
        int draws = 0;
        while (session.hasNextCard()) {
            Flashcard card = session.nextCard();
            draws++;
            if (!missedOnce) {
                assertFalse(session.submitAnswer(card, "wrong"));
                missedOnce = true;
            }
            else {
                assertTrue(session.submitAnswer(card, card.getAnswer()));
            }
        }
        assertEquals(4, draws);
        assertEquals(3, session.getDeckSize());
        assertEquals(2, session.getCorrectFirstTry());
        assertEquals(1, session.getMissedCount());
        assertEquals(66, session.getScorePercent());
    }


    /**
     * Tests the spec's worst case: every card is missed twice (its first
     * showing and its first retry) and then answered right, so the score is
     * 0 and never negative.
     */
    public void testScoreNeverBelowZero() {
        MissTracker counts = new MissTracker();
        while (session.hasNextCard()) {
            Flashcard card = session.nextCard();
            if (counts.missCount(card) < 2) {
                counts.recordMiss(card);
                assertFalse(session.submitAnswer(card, "wrong"));
            }
            else {
                assertTrue(session.submitAnswer(card, card.getAnswer()));
            }
        }
        assertEquals(0, session.getCorrectFirstTry());
        assertEquals(0, session.getScorePercent());
        assertEquals(6, session.getMissedCount());
    }


    /**
     * Tests that a duplicate card in the deck still earns its own first-try
     * point after its twin was missed (the retry mark, not equals, decides).
     */
    public void testDuplicateCardsScoreSeparately() {
        Deck twins = new Deck();
        twins.addCard(new Flashcard("2+2", "4"));
        twins.addCard(new Flashcard("2+2", "4"));
        QuizSession twinQuiz = new QuizSession(twins, new Random(SEED));
        Flashcard first = twinQuiz.nextCard();
        assertFalse(twinQuiz.submitAnswer(first, "5"));
        while (twinQuiz.hasNextCard()) {
            Flashcard card = twinQuiz.nextCard();
            twinQuiz.submitAnswer(card, card.getAnswer());
        }
        assertEquals(1, twinQuiz.getCorrectFirstTry());
        assertEquals(50, twinQuiz.getScorePercent());
    }


    /**
     * Tests submitAnswer bad input: a null response is wrong, a null card is
     * rejected.
     */
    public void testSubmitAnswerBadInput() {
        Flashcard card = session.nextCard();
        assertFalse(session.submitAnswer(card, null));
        assertEquals(1, session.getMissedCount());

        Exception thrown = null;
        try {
            session.submitAnswer(null, "4");
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }
}
