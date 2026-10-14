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
 * One flashcard: a question and its answer. A card owns the rule for checking
 * an answer (trim outer spaces, ignore case). A copy made after a missed
 * answer is marked as a retry so the quiz can tell it apart from a card's
 * first showing; the retry mark does not affect equals().
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class Flashcard {

    /** Longest question or answer, in characters, that a card may hold. */
    public static final int MAX_LENGTH = 500;

    /** Message used when a question or answer is null or blank. */
    public static final String BLANK_MESSAGE =
        "Question and answer can't be blank";

    /** Message used when a question or answer is too long. */
    public static final String TOO_LONG_MESSAGE =
        "Question and answer can be at most " + MAX_LENGTH + " characters";

    /** Multiplier used to combine the two hash codes. */
    private static final int HASH_MULTIPLIER = 31;

    private final String question;
    private final String answer;
    private final boolean retry;

    /**
     * Creates a card. Both parts are stored with outer spaces trimmed.
     *
     * @param question
     *            the question shown to the user
     * @param answer
     *            the correct answer
     * @throws IllegalArgumentException
     *             if either part is null or blank, or longer than
     *             MAX_LENGTH after trimming
     */
    public Flashcard(String question, String answer) {
        this(question, answer, false);
    }


    /**
     * Creates a card that may be marked as a retry copy.
     *
     * @param question
     *            the question shown to the user
     * @param answer
     *            the correct answer
     * @param retry
     *            true if this card is a copy re-added after a miss
     */
    private Flashcard(String question, String answer, boolean retry) {
        if (isBlank(question) || isBlank(answer)) {
            throw new IllegalArgumentException(BLANK_MESSAGE);
        }
        String trimmedQuestion = question.trim();
        String trimmedAnswer = answer.trim();
        if (trimmedQuestion.length() > MAX_LENGTH || trimmedAnswer
            .length() > MAX_LENGTH) {
            throw new IllegalArgumentException(TOO_LONG_MESSAGE);
        }
        this.question = trimmedQuestion;
        this.answer = trimmedAnswer;
        this.retry = retry;
    }


    /**
     * Tells whether text is null or made only of whitespace, including
     * Unicode spaces such as the non-breaking space that trim() keeps.
     *
     * @param text
     *            the text to check
     * @return true if text is null, empty, or all whitespace
     */
    public static boolean isBlank(String text) {
        if (text == null) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c) && !Character.isSpaceChar(c)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Trims text and cuts it to at most MAX_LENGTH characters, never
     * splitting an emoji (a surrogate pair) in half.
     *
     * @param text
     *            the text to fit
     * @return the trimmed, cut text; null if text is null
     */
    public static String fitToLength(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.length() <= MAX_LENGTH) {
            return trimmed;
        }
        int end = MAX_LENGTH;
        if (Character.isHighSurrogate(trimmed.charAt(end - 1))) {
            end--;
        }
        return trimmed.substring(0, end).trim();
    }


    /**
     * Gets the question.
     *
     * @return the trimmed question
     */
    public String getQuestion() {
        return question;
    }


    /**
     * Gets the answer.
     *
     * @return the trimmed answer
     */
    public String getAnswer() {
        return answer;
    }


    /**
     * Tells whether this card is a copy re-added after a missed answer.
     *
     * @return true for a retry copy, false for an original card
     */
    public boolean isRetry() {
        return retry;
    }


    /**
     * Checks a response against the answer, ignoring case and outer spaces.
     *
     * @param response
     *            what the user typed
     * @return true if it matches the answer; false for null, blank, or a
     *         different answer
     */
    public boolean isCorrect(String response) {
        if (response == null) {
            return false;
        }
        return response.trim().equalsIgnoreCase(answer);
    }


    /**
     * Makes the copy that goes back into a quiz pile after a miss.
     *
     * @return a new card with the same question and answer, marked as a retry
     */
    public Flashcard copy() {
        return new Flashcard(question, answer, true);
    }


    /**
     * Two cards are equal when their questions match and their answers match,
     * ignoring case. The retry mark is not compared.
     *
     * @param other
     *            the object to compare to
     * @return true if other is an equal Flashcard
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        Flashcard card = (Flashcard)other;
        return question.toLowerCase().equals(card.question.toLowerCase())
            && answer.toLowerCase().equals(card.answer.toLowerCase());
    }


    /**
     * Hash code consistent with equals().
     *
     * @return a hash of the lower-case question and answer
     */
    @Override
    public int hashCode() {
        return question.toLowerCase().hashCode() * HASH_MULTIPLIER + answer
            .toLowerCase().hashCode();
    }


    /**
     * Readable form used by View cards.
     *
     * @return "Q: question | A: answer"
     */
    @Override
    public String toString() {
        return "Q: " + question + " | A: " + answer;
    }
}
