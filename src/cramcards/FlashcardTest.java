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
 * Tests for Flashcard.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class FlashcardTest extends student.TestCase {

    private Flashcard card;

    /**
     * Creates the card (2+2, 4) before each test.
     */
    public void setUp() {
        card = new Flashcard("2+2", "4");
    }


    /**
     * Makes a String of the given length.
     *
     * @param length
     *            how many characters
     * @return a String of x characters
     */
    private String repeat(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append('x');
        }
        return builder.toString();
    }


    /**
     * Tries to build a card and returns what was thrown.
     *
     * @param question
     *            the question
     * @param answer
     *            the answer
     * @return the exception, or null if none was thrown
     */
    private Exception build(String question, String answer) {
        Exception thrown = null;
        try {
            new Flashcard(question, answer);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        return thrown;
    }


    /**
     * Tests that the constructor trims both parts.
     */
    public void testConstructorTrims() {
        Flashcard padded = new Flashcard("  2+2  ", " 4 ");
        assertEquals("2+2", padded.getQuestion());
        assertEquals("4", padded.getAnswer());
        assertFalse(padded.isRetry());
    }


    /**
     * Tests that blank and null parts are rejected.
     */
    public void testConstructorRejectsBlank() {
        Exception empty = build("", "4");
        assertTrue(empty instanceof IllegalArgumentException);
        assertEquals(Flashcard.BLANK_MESSAGE, empty.getMessage());
        assertTrue(build("   ", "4") instanceof IllegalArgumentException);
        assertTrue(build(null, "4") instanceof IllegalArgumentException);
        assertTrue(build("2+2", null) instanceof IllegalArgumentException);
        assertTrue(build("2+2", "  ") instanceof IllegalArgumentException);
        assertTrue(build("\u00A0\u00A0",
            "4") instanceof IllegalArgumentException);
        assertTrue(build("2+2", "\u3000") instanceof IllegalArgumentException);
    }


    /**
     * Tests isBlank for text, null, empty, ASCII spaces, and Unicode spaces.
     */
    public void testIsBlank() {
        assertFalse(Flashcard.isBlank("a"));
        assertFalse(Flashcard.isBlank(" a\u00A0"));
        assertTrue(Flashcard.isBlank(null));
        assertTrue(Flashcard.isBlank(""));
        assertTrue(Flashcard.isBlank(" \t "));
        assertTrue(Flashcard.isBlank("\u00A0\u3000"));
    }


    /**
     * Tests the length limit: exactly MAX_LENGTH is fine, one more is not.
     */
    public void testConstructorLength() {
        String max = repeat(Flashcard.MAX_LENGTH);
        assertNull(build(max, max));
        Exception longQuestion = build(repeat(Flashcard.MAX_LENGTH + 1), "4");
        assertTrue(longQuestion instanceof IllegalArgumentException);
        assertEquals(Flashcard.TOO_LONG_MESSAGE, longQuestion.getMessage());
        assertTrue(build("2+2", repeat(Flashcard.MAX_LENGTH
            + 1)) instanceof IllegalArgumentException);
    }


    /**
     * Tests getQuestion, getAnswer, and toString.
     */
    public void testGetters() {
        assertEquals("2+2", card.getQuestion());
        assertEquals("4", card.getAnswer());
        assertEquals("Q: 2+2 | A: 4", card.toString());
    }


    /**
     * Tests isCorrect with case and spaces, and with wrong, blank, and null
     * responses.
     */
    public void testIsCorrect() {
        Flashcard word = new Flashcard("Plural of card?", "Cards");
        assertTrue(word.isCorrect("Cards"));
        assertTrue(word.isCorrect("cards"));
        assertTrue(word.isCorrect(" CARDS "));
        assertFalse(word.isCorrect("card"));
        assertFalse(word.isCorrect(""));
        assertFalse(word.isCorrect(null));
    }


    /**
     * Tests copy: equal to the original, a different object, marked as a
     * retry, and the original is unchanged.
     */
    public void testCopy() {
        Flashcard copy = card.copy();
        assertEquals(card, copy);
        assertNotSame(card, copy);
        assertTrue(copy.isRetry());
        assertFalse(card.isRetry());
        assertEquals("2+2", card.getQuestion());
    }


    /**
     * Tests every path through equals and that hashCode agrees with it.
     */
    public void testEquals() {
        Flashcard sameIgnoringCase = new Flashcard("2+2", "4");
        Flashcard upper = new Flashcard("CAP OF VA?", "RICHMOND");
        Flashcard lower = new Flashcard("cap of va?", "richmond");
        assertTrue(card.equals(card));
        assertTrue(card.equals(sameIgnoringCase));
        assertTrue(upper.equals(lower));
        assertEquals(upper.hashCode(), lower.hashCode());
        assertFalse(card.equals(new Flashcard("2+2", "5")));
        assertFalse(card.equals(new Flashcard("3+1", "4")));
        assertFalse(card.equals(null));
        Object notACard = "2+2";
        assertFalse(card.equals(notACard));
    }


    /**
     * Tests fitToLength trims, leaves short text alone, and cuts long text
     * to MAX_LENGTH.
     */
    public void testFitToLength() {
        assertEquals("Cards", Flashcard.fitToLength("  Cards  "));
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < Flashcard.MAX_LENGTH + 100; i++) {
            huge.append('x');
        }
        assertEquals(Flashcard.MAX_LENGTH, Flashcard.fitToLength(huge
            .toString()).length());
    }


    /**
     * Tests fitToLength with null and with an emoji that would be split at
     * the limit.
     */
    public void testFitToLengthBadInput() {
        assertNull(Flashcard.fitToLength(null));
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < Flashcard.MAX_LENGTH - 1; i++) {
            text.append('x');
        }
        text.append("\uD83D\uDE00");
        String cut = Flashcard.fitToLength(text.toString());
        assertEquals(Flashcard.MAX_LENGTH - 1, cut.length());
    }
}
