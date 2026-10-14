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
 * Tests for Deck.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class DeckTest extends student.TestCase {

    private Deck deck;
    private Flashcard math;
    private Flashcard capital;

    /**
     * Creates an empty deck and two cards before each test.
     */
    public void setUp() {
        deck = new Deck();
        math = new Flashcard("2+2", "4");
        capital = new Flashcard("Capital of VA?", "Richmond");
    }


    /**
     * Tests that a new deck is empty.
     */
    public void testConstructor() {
        assertEquals(0, deck.size());
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.getCards().length);
    }


    /**
     * Tests addCard keeps insertion order and rejects null.
     */
    public void testAddCard() {
        deck.addCard(math);
        deck.addCard(capital);
        assertEquals(2, deck.size());
        assertFalse(deck.isEmpty());
        assertEquals(math, deck.getCards()[0]);
        assertEquals(capital, deck.getCards()[1]);

        Exception thrown = null;
        try {
            deck.addCard(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(2, deck.size());
    }


    /**
     * Tests containsCard for present, absent, empty, and null.
     */
    public void testContainsCard() {
        assertFalse(deck.containsCard(math));
        deck.addCard(math);
        assertTrue(deck.containsCard(new Flashcard("2+2", "4")));
        assertFalse(deck.containsCard(capital));
        assertFalse(deck.containsCard(null));
    }


    /**
     * Tests findByQuestion for a match (any case and spaces), no match, blank,
     * and null.
     */
    public void testFindByQuestion() {
        deck.addCard(math);
        deck.addCard(capital);
        assertEquals(capital, deck.findByQuestion("  capital of va?  "));
        assertNull(deck.findByQuestion("3+3"));
        assertNull(deck.findByQuestion(""));
        assertNull(deck.findByQuestion(null));
    }


    /**
     * Tests removeByQuestion for a match, not there, empty, blank, and null.
     */
    public void testRemoveByQuestion() {
        assertFalse(deck.removeByQuestion("2+2"));
        deck.addCard(math);
        deck.addCard(capital);
        assertTrue(deck.removeByQuestion(" 2+2 "));
        assertEquals(1, deck.size());
        assertFalse(deck.containsCard(math));
        assertFalse(deck.removeByQuestion("3+3"));
        assertFalse(deck.removeByQuestion("   "));
        assertFalse(deck.removeByQuestion(null));
        assertEquals(1, deck.size());
    }


    /**
     * Tests getCards returns a copy in insertion order.
     */
    public void testGetCards() {
        deck.addCard(math);
        deck.addCard(capital);
        Flashcard[] cards = deck.getCards();
        assertEquals(2, cards.length);
        cards[0] = null;
        assertEquals(math, deck.getCards()[0]);
    }


    /**
     * Tests copyCards keeps order and leaves the deck untouched, and that an
     * empty deck copies to an empty chain.
     */
    public void testCopyCards() {
        assertTrue(deck.copyCards().isEmpty());
        deck.addCard(math);
        deck.addCard(capital);
        LinkedChain<Flashcard> copy = deck.copyCards();
        assertEquals(2, copy.size());
        assertEquals(math, copy.get(0));
        assertEquals(capital, copy.get(1));
        copy.remove();
        assertEquals(2, deck.size());
    }


    /**
     * Tests that deleting the only card makes the deck empty again.
     */
    public void testIsEmptyAfterDelete() {
        deck.addCard(math);
        assertFalse(deck.isEmpty());
        deck.removeByQuestion("2+2");
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
    }


    /**
     * Tests removeAt removes the card at a position, so one of two
     * duplicate cards can be deleted.
     */
    public void testRemoveAt() {
        deck.addCard(math);
        deck.addCard(capital);
        deck.addCard(new Flashcard("2+2", "4"));
        assertEquals(capital, deck.removeAt(1));
        assertEquals(2, deck.size());
        assertEquals(math, deck.removeAt(1));
        assertEquals(1, deck.size());
        assertEquals(math, deck.getCards()[0]);
    }


    /**
     * Tests removeAt with positions outside the deck, including on an empty
     * deck: an exception and no change.
     */
    public void testRemoveAtBadInput() {
        int[] bad = { 0, -1 };
        for (int i = 0; i < bad.length; i++) {
            Exception thrown = null;
            try {
                deck.removeAt(bad[i]);
            }
            catch (Exception exception) {
                thrown = exception;
            }
            assertTrue(thrown instanceof IndexOutOfBoundsException);
        }
        deck.addCard(math);
        Exception thrown = null;
        try {
            deck.removeAt(1);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IndexOutOfBoundsException);
        assertEquals(1, deck.size());
    }
}
