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
 * Every quiz score from this session. Scores are stored in a LinkedChain of
 * Integer and treated as an unordered bag: best, average, and count do not
 * depend on order. The most recent score is kept in its own field so the
 * class never relies on where the chain places new entries.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class ScoreHistory {

    /** Lowest valid score. */
    public static final int MIN_SCORE = 0;

    /** Highest valid score. */
    public static final int MAX_SCORE = 100;

    /** Message used for a score outside MIN_SCORE to MAX_SCORE. */
    public static final String RANGE_MESSAGE =
        "Score must be between 0 and 100";

    /** Message used when best or average is asked for with no scores. */
    public static final String NO_SCORES_MESSAGE = "No scores yet";

    private LinkedChain<Integer> scores;
    private Integer lastScore;

    /**
     * Creates an empty history.
     */
    public ScoreHistory() {
        scores = new LinkedChain<Integer>();
        lastScore = null;
    }


    /**
     * Records a finished quiz's score and makes it the latest score.
     *
     * @param percent
     *            the score, 0 to 100
     * @throws IllegalArgumentException
     *             if percent is outside 0 to 100
     */
    public void record(int percent) {
        if (percent < MIN_SCORE || percent > MAX_SCORE) {
            throw new IllegalArgumentException(RANGE_MESSAGE);
        }
        scores.add(percent);
        lastScore = percent;
    }


    /**
     * Gets the most recently recorded score. The app calls this before
     * recording a new score, so it is the "previous" score at that moment.
     *
     * @return the latest score, or null if no quiz has been recorded
     */
    public Integer getPrevious() {
        return lastScore;
    }


    /**
     * Gets the highest score recorded.
     *
     * @return the best score
     * @throws IllegalStateException
     *             if no scores have been recorded
     */
    public int getBest() {
        Integer[] all = allScores();
        int best = all[0];
        for (int i = 1; i < all.length; i++) {
            if (all[i] > best) {
                best = all[i];
            }
        }
        return best;
    }


    /**
     * Gets the mean of all recorded scores.
     *
     * @return the average score
     * @throws IllegalStateException
     *             if no scores have been recorded
     */
    public double getAverage() {
        Integer[] all = allScores();
        int sum = 0;
        for (int i = 0; i < all.length; i++) {
            sum += all[i];
        }
        return (double)sum / all.length;
    }


    /**
     * Gets how many quizzes have been recorded.
     *
     * @return the number of scores
     */
    public int getCount() {
        return scores.size();
    }


    /**
     * Tells whether any score has been recorded.
     *
     * @return true if there is at least one score
     */
    public boolean hasScores() {
        return !scores.isEmpty();
    }


    /**
     * Copies every score into a new array, in no particular order. Use
     * getPrevious() for the most recent one.
     *
     * @return the scores; length 0 when there are none
     */
    public Integer[] getScores() {
        return scores.toArray(new Integer[scores.size()]);
    }


    /**
     * Copies every score into an array.
     *
     * @return the scores
     * @throws IllegalStateException
     *             if there are no scores
     */
    private Integer[] allScores() {
        if (scores.isEmpty()) {
            throw new IllegalStateException(NO_SCORES_MESSAGE);
        }
        return scores.toArray(new Integer[scores.size()]);
    }
}
