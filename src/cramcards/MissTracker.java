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
 * Stretch goal "Advanced card analysis": remembers every missed answer this
 * session and builds a deck of the hardest cards (missed HARD_THRESHOLD or
 * more times). Each miss is stored as a String key in a LinkedChain, so
 * duplicates are the whole point: count() of a key is how many times that
 * card was missed. This is the chain's third element type.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class MissTracker {

    /** Misses after which a card counts as hard (from our scope). */
    public static final int HARD_THRESHOLD = 3;

    /** Message used when a null card or deck is passed in. */
    public static final String NULL_MESSAGE = "Card and deck can't be null";

    private LinkedChain<String> missedKeys;

    /**
     * Creates a tracker with no misses.
     */
    public MissTracker() {
        missedKeys = new LinkedChain<String>();
    }


    /**
     * Records one missed answer for a card.
     *
     * @param card
     *            the card that was answered wrong
     * @throws IllegalArgumentException
     *             if card is null
     */
    public void recordMiss(Flashcard card) {
        if (card == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
        missedKeys.add(keyFor(card));
    }


    /**
     * Gets how many times a card (or any equal card) has been missed.
     *
     * @param card
     *            the card to look up
     * @return the number of misses; 0 for null or a card never missed
     */
    public int missCount(Flashcard card) {
        if (card == null) {
            return 0;
        }
        return missedKeys.count(keyFor(card));
    }


    /**
     * Gets the total number of misses recorded this session.
     *
     * @return the number of misses
     */
    public int getTotalMisses() {
        return missedKeys.size();
    }


    /**
     * Builds a new deck of the cards in the given deck that have been missed
     * at least HARD_THRESHOLD times. The given deck is not changed.
     *
     * @param deck
     *            the user's deck
     * @return a deck of hard cards; empty if none qualify
     * @throws IllegalArgumentException
     *             if deck is null
     */
    public Deck buildHardDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
        Deck hard = new Deck();
        Flashcard[] all = deck.getCards();
        for (int i = 0; i < all.length; i++) {
            if (missCount(all[i]) >= HARD_THRESHOLD) {
                hard.addCard(all[i]);
            }
        }
        return hard;
    }


    /**
     * Makes the key stored for a card. It matches Flashcard.equals(): same
     * question and answer, ignoring case. The question's length goes first,
     * so two different cards can never share a key.
     *
     * @param card
     *            the card
     * @return the question's length, then the lower-case question and answer
     */
    private static String keyFor(Flashcard card) {
        String question = card.getQuestion().toLowerCase();
        return question.length() + ":" + question + card.getAnswer()
            .toLowerCase();
    }
}
