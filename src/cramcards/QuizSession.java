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
 * One run through a deck. The session works on its own copy of the cards
 * (the pile), draws cards at random, and puts a retry copy of every missed
 * card back into the pile, so a quiz only ends once every card has been
 * answered right. The score counts only cards answered right on their first
 * showing.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class QuizSession {

    /** Message used when a quiz is started on an empty deck. */
    public static final String EMPTY_DECK_MESSAGE =
        "Cannot start a quiz with an empty deck";

    /** Message used when a card is drawn from an empty pile. */
    public static final String NO_CARDS_LEFT_MESSAGE =
        "No cards left in this quiz";

    /** Message used when a required argument is null. */
    public static final String NULL_MESSAGE =
        "Deck, random, and card can't be null";

    /** Multiplier that turns a fraction into a percentage. */
    private static final int PERCENT = 100;

    private LinkedChain<Flashcard> pile;
    private LinkedChain<Flashcard> missed;
    private Random random;
    private int deckSize;
    private int correctFirstTry;

    /**
     * Starts a quiz on a copy of the deck; the deck itself is never changed.
     *
     * @param deck
     *            the cards to quiz on
     * @param random
     *            picks which card to draw (a fixed seed makes tests repeat)
     * @throws IllegalArgumentException
     *             if either argument is null or the deck is empty
     */
    public QuizSession(Deck deck, Random random) {
        if (deck == null || random == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
        if (deck.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_DECK_MESSAGE);
        }
        this.pile = deck.copyCards();
        this.missed = new LinkedChain<Flashcard>();
        this.random = random;
        this.deckSize = deck.size();
        this.correctFirstTry = 0;
    }


    /**
     * Tells whether any cards are left to ask.
     *
     * @return true while the pile is not empty
     */
    public boolean hasNextCard() {
        return !pile.isEmpty();
    }


    /**
     * Removes and returns a random card from the pile.
     *
     * @return the card to ask next
     * @throws IllegalStateException
     *             if the pile is empty
     */
    public Flashcard nextCard() {
        if (pile.isEmpty()) {
            throw new IllegalStateException(NO_CARDS_LEFT_MESSAGE);
        }
        return pile.removeAt(random.nextInt(pile.size()));
    }


    /**
     * Checks an answer. A right answer earns a point only when the card is
     * not a retry copy. A wrong answer records the miss and puts a retry
     * copy of the card back into the pile with add(); because draws are
     * random, where the copy goes in the pile does not matter.
     *
     * @param card
     *            the card that was asked
     * @param response
     *            what the user typed (null counts as wrong)
     * @return true if the response was right
     * @throws IllegalArgumentException
     *             if card is null
     */
    public boolean submitAnswer(Flashcard card, String response) {
        if (card == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
        if (card.isCorrect(response)) {
            if (!card.isRetry()) {
                correctFirstTry++;
            }
            return true;
        }
        missed.add(card);
        pile.add(card.copy());
        return false;
    }


    /**
     * Gets the score: cards right on the first try out of the deck size,
     * rounded down. Retries never raise it, so it is always 0 to 100.
     *
     * @return the score as a whole percentage
     */
    public int getScorePercent() {
        return correctFirstTry * PERCENT / deckSize;
    }


    /**
     * Gets how many cards were answered right on their first showing.
     *
     * @return the first-try count
     */
    public int getCorrectFirstTry() {
        return correctFirstTry;
    }


    /**
     * Gets the number of cards in the deck when the quiz started.
     *
     * @return the deck size
     */
    public int getDeckSize() {
        return deckSize;
    }


    /**
     * Gets how many wrong answers were given in this quiz (every miss is
     * counted, including misses on retry copies).
     *
     * @return the number of misses
     */
    public int getMissedCount() {
        return missed.size();
    }
}
