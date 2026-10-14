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
 * Tests for ScoreHistory.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class ScoreHistoryTest extends student.TestCase {

    /** Tolerance for comparing averages. */
    private static final double DELTA = 0.0001;

    private ScoreHistory history;

    /**
     * Creates an empty history before each test.
     */
    public void setUp() {
        history = new ScoreHistory();
    }


    /**
     * Tests the empty state: no count, no previous score.
     */
    public void testConstructor() {
        assertEquals(0, history.getCount());
        assertFalse(history.hasScores());
        assertNull(history.getPrevious());
    }


    /**
     * Tests record, including both ends of the valid range.
     */
    public void testRecord() {
        history.record(80);
        assertEquals(1, history.getCount());
        assertTrue(history.hasScores());
        history.record(ScoreHistory.MIN_SCORE);
        history.record(ScoreHistory.MAX_SCORE);
        assertEquals(3, history.getCount());
    }


    /**
     * Tests that out-of-range scores are rejected and not stored.
     */
    public void testRecordBadInput() {
        Exception tooHigh = null;
        try {
            history.record(101);
        }
        catch (Exception exception) {
            tooHigh = exception;
        }
        assertTrue(tooHigh instanceof IllegalArgumentException);
        assertEquals(ScoreHistory.RANGE_MESSAGE, tooHigh.getMessage());

        Exception negative = null;
        try {
            history.record(-1);
        }
        catch (Exception exception) {
            negative = exception;
        }
        assertTrue(negative instanceof IllegalArgumentException);
        assertEquals(0, history.getCount());
        assertNull(history.getPrevious());
    }


    /**
     * Tests that getPrevious is the most recently recorded score.
     */
    public void testGetPrevious() {
        history.record(70);
        history.record(90);
        assertEquals(90, history.getPrevious().intValue());
        history.record(60);
        assertEquals(60, history.getPrevious().intValue());
    }


    /**
     * Tests getBest with duplicate scores, and on an empty history.
     */
    public void testGetBest() {
        Exception empty = null;
        try {
            history.getBest();
        }
        catch (Exception exception) {
            empty = exception;
        }
        assertTrue(empty instanceof IllegalStateException);
        assertEquals(ScoreHistory.NO_SCORES_MESSAGE, empty.getMessage());

        history.record(70);
        history.record(90);
        history.record(90);
        history.record(40);
        assertEquals(90, history.getBest());
    }


    /**
     * Tests getAverage, and on an empty history.
     */
    public void testGetAverage() {
        Exception empty = null;
        try {
            history.getAverage();
        }
        catch (Exception exception) {
            empty = exception;
        }
        assertTrue(empty instanceof IllegalStateException);

        history.record(70);
        history.record(90);
        assertEquals(80.0, history.getAverage(), DELTA);
        history.record(65);
        assertEquals(75.0, history.getAverage(), DELTA);
    }


    /**
     * Tests getScores returns every score.
     */
    public void testGetScores() {
        history.record(70);
        history.record(90);
        history.record(70);
        Integer[] scores = history.getScores();
        assertEquals(3, scores.length);
        int sum = 0;
        for (int i = 0; i < scores.length; i++) {
            sum += scores[i].intValue();
        }
        assertEquals(230, sum);
    }


    /**
     * Tests getScores on an empty history, and that changing the array
     * doesn't change the history.
     */
    public void testGetScoresEmpty() {
        assertEquals(0, history.getScores().length);
        history.record(50);
        history.getScores()[0] = Integer.valueOf(99);
        assertEquals(50, history.getBest());
    }
}
