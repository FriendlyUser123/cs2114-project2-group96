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
 * The user's set of flashcards. A deck adds, lists, finds, and deletes cards
 * and answers whether a card is already present. It never runs a quiz; a
 * QuizSession works on a copy of the cards instead. Storage is a
 * LinkedChain of Flashcard objects.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class Deck {

    private LinkedChain<Flashcard> cards;

    /**
     * Creates an empty deck.
     */
    public Deck() {
        cards = new LinkedChain<Flashcard>();
    }


    /**
     * Adds a card at the end, so View cards lists cards in the order they
     * were made. O(n) because the chain has no tail reference.
     *
     * @param card
     *            the card to add
     * @throws IllegalArgumentException
     *             if card is null (rejected by LinkedChain)
     */
    public void addCard(Flashcard card) {
        cards.addToEnd(card);
    }


    /**
     * Tells whether an equal card (same question and answer, ignoring case)
     * is already in the deck. Used for the duplicate warning.
     *
     * @param card
     *            the card to look for
     * @return true if an equal card is present; false for null or an empty
     *         deck
     */
    public boolean containsCard(Flashcard card) {
        return cards.contains(card);
    }


    /**
     * Finds the first card whose question matches, ignoring case and outer
     * spaces.
     *
     * @param question
     *            the question to look for
     * @return the matching card, or null if the question is null, blank, or
     *         not in the deck
     */
    public Flashcard findByQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return null;
        }
        String key = question.trim().toLowerCase();
        Flashcard[] all = getCards();
        for (int i = 0; i < all.length; i++) {
            if (all[i].getQuestion().toLowerCase().equals(key)) {
                return all[i];
            }
        }
        return null;
    }


    /**
     * Removes the first card whose question matches, ignoring case and outer
     * spaces.
     *
     * @param question
     *            the question of the card to delete
     * @return true if a card was removed; false if the question is null,
     *         blank, or not found (the deck is unchanged)
     */
    public boolean removeByQuestion(String question) {
        Flashcard match = findByQuestion(question);
        if (match == null) {
            return false;
        }
        return cards.remove(match);
    }


    /**
     * Removes the card at a position in the list, so one of two duplicate
     * cards can be deleted on its own.
     *
     * @param index
     *            the card's position, 0 for the first card added
     * @return the removed card
     * @throws IndexOutOfBoundsException
     *             if index is not 0 to size() - 1
     */
    public Flashcard removeAt(int index) {
        return cards.removeAt(index);
    }


    /**
     * Copies every card into a new array in insertion order. Changing the
     * array does not change the deck.
     *
     * @return the cards, length 0 when the deck is empty
     */
    public Flashcard[] getCards() {
        return cards.toArray(new Flashcard[cards.size()]);
    }


    /**
     * Makes a new chain holding the same cards for a QuizSession to use up.
     * The cards are added back to front with add(), which keeps their order
     * and costs O(1) each. Cards are shared, which is safe because a
     * Flashcard never changes.
     *
     * @return a new chain with every card; empty for an empty deck
     */
    public LinkedChain<Flashcard> copyCards() {
        LinkedChain<Flashcard> copy = new LinkedChain<Flashcard>();
        Flashcard[] all = getCards();
        for (int i = all.length - 1; i >= 0; i--) {
            copy.add(all[i]);
        }
        return copy;
    }


    /**
     * Gets the number of cards.
     *
     * @return the number of cards in the deck
     */
    public int size() {
        return cards.size();
    }


    /**
     * Tells whether the deck has no cards.
     *
     * @return true if the deck is empty
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
