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

/**
 * Tests for MissTracker (stretch goal: hardest cards).
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class MissTrackerTest extends student.TestCase {

    private MissTracker tracker;
    private Flashcard math;
    private Flashcard capital;

    /**
     * Creates an empty tracker and two cards before each test.
     */
    public void setUp() {
        tracker = new MissTracker();
        math = new Flashcard("2+2", "4");
        capital = new Flashcard("Capital of VA?", "Richmond");
    }


    /**
     * Tests the empty state.
     */
    public void testConstructor() {
        assertEquals(0, tracker.getTotalMisses());
        assertEquals(0, tracker.missCount(math));
    }


    /**
     * Tests recordMiss counts equal cards together (any case) and rejects
     * null.
     */
    public void testRecordMiss() {
        tracker.recordMiss(math);
        tracker.recordMiss(new Flashcard("2+2", "4"));
        tracker.recordMiss(capital);
        assertEquals(2, tracker.missCount(math));
        assertEquals(1, tracker.missCount(new Flashcard("CAPITAL OF VA?",
            "richmond")));
        assertEquals(3, tracker.getTotalMisses());

        Exception thrown = null;
        try {
            tracker.recordMiss(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(3, tracker.getTotalMisses());
    }


    /**
     * Tests missCount for a card never missed and for null.
     */
    public void testMissCountNotThere() {
        tracker.recordMiss(math);
        assertEquals(0, tracker.missCount(capital));
        assertEquals(0, tracker.missCount(null));
    }


    /**
     * Tests buildHardDeck: only cards missed HARD_THRESHOLD or more times are
     * included, and the user's deck is not changed.
     */
    public void testBuildHardDeck() {
        Deck deck = new Deck();
        deck.addCard(math);
        deck.addCard(capital);
        for (int i = 0; i < MissTracker.HARD_THRESHOLD; i++) {
            tracker.recordMiss(math);
        }
        for (int i = 0; i < MissTracker.HARD_THRESHOLD - 1; i++) {
            tracker.recordMiss(capital);
        }
        Deck hard = tracker.buildHardDeck(deck);
        assertEquals(1, hard.size());
        assertTrue(hard.containsCard(math));
        assertFalse(hard.containsCard(capital));
        assertEquals(2, deck.size());
    }


    /**
     * Tests buildHardDeck with an empty deck, no misses, and a null deck.
     */
    public void testBuildHardDeckBadInput() {
        assertTrue(tracker.buildHardDeck(new Deck()).isEmpty());
        Deck deck = new Deck();
        deck.addCard(math);
        assertTrue(tracker.buildHardDeck(deck).isEmpty());

        Exception thrown = null;
        try {
            tracker.buildHardDeck(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }


    /**
     * Tests that cards whose question and answer join to the same text are
     * still counted separately.
     */
    public void testMissCountSimilarCards() {
        Flashcard first = new Flashcard("a\nb", "c");
        Flashcard second = new Flashcard("a", "b\nc");
        tracker.recordMiss(first);
        assertEquals(1, tracker.missCount(first));
        assertEquals(0, tracker.missCount(second));
    }
}
