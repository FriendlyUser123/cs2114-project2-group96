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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;

/**
 * Cram Cards: the menu loop and everything that touches the terminal. This is
 * the only terminal class that reads input or prints, so every bad-input
 * check that needs to re-prompt the user lives here. Storage and rules live
 * in Deck, QuizSession, ScoreHistory, MissTracker, and Flashcard. With a
 * DeckStore, saved cards and scores are loaded when run() starts and saved
 * after every change (stretch goal "Persistence"). CramCardsGui is the
 * window version of the same app.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class CramCardsApp {

    /** Menu choice: add a card. */
    public static final int ADD_CHOICE = 1;
    /** Menu choice: view cards. */
    public static final int VIEW_CHOICE = 2;
    /** Menu choice: delete a card. */
    public static final int DELETE_CHOICE = 3;
    /** Menu choice: start a quiz. */
    public static final int QUIZ_CHOICE = 4;
    /** Menu choice: view results. */
    public static final int RESULTS_CHOICE = 5;
    /** Menu choice: practice the hardest cards (stretch goal). */
    public static final int HARD_CHOICE = 6;
    /** Menu choice: quit. */
    public static final int QUIT_CHOICE = 7;

    /**
     * Word that ends a quiz early (reserved; it can't be used as an answer).
     */
    public static final String QUIT_COMMAND = "QUIT";

    /** Shown for a menu choice that isn't 1 to 7. */
    public static final String MENU_ERROR = "Enter a number 1 to 7.";
    /** Shown for blank input. */
    public static final String BLANK_INPUT = "This can't be blank. Try again.";
    /** Shown when long input is cut. */
    public static final String TOO_LONG_INPUT = "That was over "
        + Flashcard.MAX_LENGTH + " characters, so it was cut to "
        + Flashcard.MAX_LENGTH + ".";
    /** Shown when the deck is empty for View cards or a quiz. */
    public static final String NO_CARDS = "No cards yet. Add some cards first.";
    /** Shown when Delete a card is chosen with an empty deck. */
    public static final String NO_CARDS_TO_DELETE =
        "There are no cards to delete.";
    /** Asked when an equal card is already in the deck. */
    public static final String DUPLICATE_PROMPT =
        "This card is already in your deck. Add it anyway? (y/n)";
    /** Shown when a yes/no answer isn't y, yes, n, or no. */
    public static final String YES_NO_ERROR = "Please type y or n.";
    /** Shown after adding a card whose answer is the quit word. */
    public static final String QUIT_ANSWER_WARNING = "Heads up: typing "
        + QUIT_COMMAND + " ends a terminal quiz, so answer this card in the "
        + "window version (CramCardsGui).";
    /** Shown when the user declines a duplicate card. */
    public static final String CARD_NOT_ADDED = "Card not added.";
    /** Shown after a card is added. */
    public static final String CARD_ADDED = "Card added.";
    /** Shown after a card is deleted. */
    public static final String CARD_DELETED = "Card deleted.";
    /** Shown when no card has the given question. */
    public static final String CARD_NOT_FOUND = "Card not found.";
    /** Shown when a quiz is ended with QUIT. */
    public static final String QUIZ_ENDED_EARLY =
        "Quiz ended early. This score was not saved.";
    /** Shown after a right answer. */
    public static final String CORRECT = "Correct!";
    /** Shown before the right answer after a miss. */
    public static final String WRONG_PREFIX = "Not quite. The answer was: ";
    /** Shown when View results is chosen before any quiz. */
    public static final String NO_QUIZZES = "No quizzes taken yet.";
    /** Shown when no card has been missed enough to count as hard. */
    public static final String NO_HARD_CARDS =
        "No hard cards yet. A card counts as hard after you miss it "
            + MissTracker.HARD_THRESHOLD + " times.";
    /** Message used when the app is built with a null argument. */
    public static final String NULL_ARGUMENT =
        "Scanner, output, and random can't be null";
    /** Shown when the program ends. */
    public static final String GOODBYE = "Goodbye!";

    private Scanner scanner;
    private PrintStream out;
    private Random random;
    private Deck deck;
    private ScoreHistory history;
    private MissTracker misses;
    private DeckStore store;

    /**
     * Creates the app reading the keyboard, printing to the screen, and
     * saving to DeckStore.DEFAULT_FILE.
     */
    public CramCardsApp() {
        this(new Scanner(System.in), System.out, new Random(), new DeckStore(
            new File(DeckStore.DEFAULT_FILE)));
    }


    /**
     * Creates the app with its input, output, and random source passed in, so
     * tests can script the input and read the output.
     *
     * @param scanner
     *            where user input is read from
     * @param out
     *            where prompts and results are printed
     * @param random
     *            used by each quiz to draw cards
     * @throws IllegalArgumentException
     *             if any argument is null
     */
    public CramCardsApp(Scanner scanner, PrintStream out, Random random) {
        this(scanner, out, random, null);
    }


    /**
     * Creates the app with its input, output, random source, and save file
     * passed in.
     *
     * @param scanner
     *            where user input is read from
     * @param out
     *            where prompts and results are printed
     * @param random
     *            used by each quiz to draw cards
     * @param store
     *            where cards and scores are saved; null to save nothing
     * @throws IllegalArgumentException
     *             if scanner, out, or random is null
     */
    public CramCardsApp(
        Scanner scanner,
        PrintStream out,
        Random random,
        DeckStore store) {
        if (scanner == null || out == null || random == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT);
        }
        this.scanner = scanner;
        this.out = out;
        this.random = random;
        this.deck = new Deck();
        this.history = new ScoreHistory();
        this.misses = new MissTracker();
        this.store = store;
    }


    /**
     * Starts Cram Cards.
     *
     * @param args
     *            not used
     */
    public static void main(String[] args) {
        new CramCardsApp().run();
    }


    /**
     * Loads any saved cards and scores, then shows the menu until the user
     * quits or input ends. Bad menu input prints MENU_ERROR and the menu
     * again.
     */
    public void run() {
        loadSaved();
        boolean running = true;
        while (running) {
            printMenu();
            String line = readLine();
            if (line == null) {
                running = false;
            }
            else {
                running = handleChoice(line);
            }
        }
        out.println(GOODBYE);
    }


    /**
     * Turns menu input into a choice.
     *
     * @param input
     *            what the user typed
     * @return a choice from 1 to 7 (exactly one digit, outer spaces allowed)
     * @throws IllegalArgumentException
     *             with MENU_ERROR for anything else
     */
    public int parseMenuChoice(String input) {
        if (input == null || input.trim().length() != 1) {
            throw new IllegalArgumentException(MENU_ERROR);
        }
        int choice = input.trim().charAt(0) - '0';
        if (choice < ADD_CHOICE || choice > QUIT_CHOICE) {
            throw new IllegalArgumentException(MENU_ERROR);
        }
        return choice;
    }


    /**
     * Asks for text until it is not blank. Text longer than
     * Flashcard.MAX_LENGTH is cut to that length and the user is told.
     *
     * @param prompt
     *            the question to print
     * @return the trimmed text, or null if input ended
     */
    public String readNonBlank(String prompt) {
        while (true) {
            out.println(prompt);
            String line = readLine();
            if (line == null) {
                return null;
            }
            String text = Flashcard.fitToLength(line);
            if (Flashcard.isBlank(text)) {
                out.println(BLANK_INPUT);
            }
            else {
                if (text.length() < line.trim().length()) {
                    out.println(TOO_LONG_INPUT);
                }
                return text;
            }
        }
    }


    /**
     * Asks a yes/no question until the answer is y, yes, n, or no (any
     * case).
     *
     * @param prompt
     *            the question to print
     * @return true for yes; false for no or if input ended
     */
    public boolean readYesNo(String prompt) {
        while (true) {
            out.println(prompt);
            String line = readLine();
            if (line == null) {
                return false;
            }
            String text = line.trim().toLowerCase();
            if (text.equals("y") || text.equals("yes")) {
                return true;
            }
            if (text.equals("n") || text.equals("no")) {
                return false;
            }
            out.println(YES_NO_ERROR);
        }
    }


    /**
     * Menu choice 1: reads a question and answer and adds the card. An equal
     * card already in the deck needs a y/n confirmation.
     */
    public void addCardFlow() {
        String question = readNonBlank("Question:");
        if (question == null) {
            return;
        }
        String answer = readNonBlank("Answer:");
        if (answer == null) {
            return;
        }
        Flashcard card = new Flashcard(question, answer);
        if (deck.containsCard(card) && !readYesNo(DUPLICATE_PROMPT)) {
            out.println(CARD_NOT_ADDED);
            return;
        }
        deck.addCard(card);
        out.println(CARD_ADDED);
        if (answer.equalsIgnoreCase(QUIT_COMMAND)) {
            out.println(QUIT_ANSWER_WARNING);
        }
        save();
    }


    /**
     * Menu choice 2: lists every card, numbered, in the order added.
     */
    public void viewCards() {
        if (deck.isEmpty()) {
            out.println(NO_CARDS);
            return;
        }
        Flashcard[] all = deck.getCards();
        for (int i = 0; i < all.length; i++) {
            out.println((i + 1) + ". " + all[i]);
        }
    }


    /**
     * Menu choice 3: deletes the card whose question the user types.
     */
    public void deleteCardFlow() {
        if (deck.isEmpty()) {
            out.println(NO_CARDS_TO_DELETE);
            return;
        }
        String question = readNonBlank("Question of the card to delete:");
        if (question == null) {
            return;
        }
        if (deck.removeByQuestion(question)) {
            out.println(CARD_DELETED);
            save();
        }
        else {
            out.println(CARD_NOT_FOUND);
        }
    }


    /**
     * Menu choice 4: quizzes the user on the whole deck and saves the score.
     */
    public void quizFlow() {
        if (deck.isEmpty()) {
            out.println(NO_CARDS);
            return;
        }
        runQuiz(deck, true);
        save();
    }


    /**
     * Menu choice 5: shows the latest score, best, average, and count.
     */
    public void showResults() {
        if (!history.hasScores()) {
            out.println(NO_QUIZZES);
            return;
        }
        out.println("Last score: " + history.getPrevious() + "% | Best: "
            + history.getBest() + "% | Average: " + String.format("%.1f",
                history.getAverage()) + "% | Quizzes taken: " + history
                    .getCount());
    }


    /**
     * Menu choice 6 (stretch goal): quizzes the user on only the cards missed
     * at least MissTracker.HARD_THRESHOLD times. Practice scores are not
     * saved to the history.
     */
    public void practiceHardestFlow() {
        Deck hard = misses.buildHardDeck(deck);
        if (hard.isEmpty()) {
            out.println(NO_HARD_CARDS);
            return;
        }
        out.println("Practicing " + hard.size() + " hard card(s).");
        runQuiz(hard, false);
        save();
    }


    /**
     * Gets the deck (used by tests).
     *
     * @return this session's deck
     */
    public Deck getDeck() {
        return deck;
    }


    /**
     * Gets the score history (used by tests).
     *
     * @return this session's score history
     */
    public ScoreHistory getHistory() {
        return history;
    }


    /**
     * Gets the miss tracker (used by tests).
     *
     * @return this session's miss tracker
     */
    public MissTracker getMissTracker() {
        return misses;
    }


    /**
     * Runs one menu choice.
     *
     * @param line
     *            the menu input
     * @return false when the user chose Quit, true otherwise
     */
    private boolean handleChoice(String line) {
        int choice;
        try {
            choice = parseMenuChoice(line);
        }
        catch (IllegalArgumentException e) {
            out.println(e.getMessage());
            return true;
        }
        if (choice == ADD_CHOICE) {
            addCardFlow();
        }
        else if (choice == VIEW_CHOICE) {
            viewCards();
        }
        else if (choice == DELETE_CHOICE) {
            deleteCardFlow();
        }
        else if (choice == QUIZ_CHOICE) {
            quizFlow();
        }
        else if (choice == RESULTS_CHOICE) {
            showResults();
        }
        else if (choice == HARD_CHOICE) {
            practiceHardestFlow();
        }
        else {
            return false;
        }
        return true;
    }


    /**
     * Asks every card in a quiz until the pile is empty or the user types
     * QUIT. A finished quiz shows the score next to the previous and best
     * scores; the previous score is read before the new one is recorded.
     *
     * @param quizDeck
     *            the cards to quiz on (not empty)
     * @param saveScore
     *            true to record the score in the history
     */
    private void runQuiz(Deck quizDeck, boolean saveScore) {
        QuizSession session = new QuizSession(quizDeck, random);
        while (session.hasNextCard()) {
            Flashcard card = session.nextCard();
            out.println("Q: " + card.getQuestion());
            out.println("Your answer (or " + QUIT_COMMAND + " to stop):");
            String response = readLine();
            if (response == null || response.trim().equalsIgnoreCase(
                QUIT_COMMAND)) {
                out.println(QUIZ_ENDED_EARLY);
                return;
            }
            if (session.submitAnswer(card, response)) {
                out.println(CORRECT);
            }
            else {
                misses.recordMiss(card);
                out.println(WRONG_PREFIX + card.getAnswer());
            }
        }
        int score = session.getScorePercent();
        String summary = "Score: " + score + "% (" + session
            .getCorrectFirstTry() + " of " + session.getDeckSize()
            + " right on the first try)";
        if (!saveScore) {
            out.println(summary + ". Practice scores are not saved.");
            return;
        }
        Integer previous = history.getPrevious();
        history.record(score);
        out.println(summary);
        String previousText = "none";
        if (previous != null) {
            previousText = previous + "%";
        }
        out.println("Previous: " + previousText + " | Best: " + history
            .getBest() + "%");
    }


    /**
     * Loads the save file, if there is a store, and says what was loaded.
     */
    private void loadSaved() {
        if (store == null) {
            return;
        }
        try {
            int skipped = store.load(deck, history, misses);
            String summary = store.loadSummary(deck, history, skipped);
            if (!summary.isEmpty()) {
                out.println(summary);
            }
        }
        catch (IOException e) {
            out.println(DeckStore.LOAD_FAILED + e.getMessage());
        }
    }


    /**
     * Saves everything, if there is a store. Only a failure is printed.
     */
    private void save() {
        if (store == null) {
            return;
        }
        try {
            store.save(deck, history, misses);
        }
        catch (IOException e) {
            out.println(DeckStore.SAVE_FAILED + e.getMessage());
        }
    }


    /**
     * Prints the menu.
     */
    private void printMenu() {
        out.println();
        out.println("Cram Cards: " + ADD_CHOICE + " Add a card, " + VIEW_CHOICE
            + " View cards, " + DELETE_CHOICE + " Delete a card, " + QUIZ_CHOICE
            + " Start quiz, " + RESULTS_CHOICE + " View results, " + HARD_CHOICE
            + " Practice hardest cards, " + QUIT_CHOICE + " Quit");
    }


    /**
     * Reads one line of input.
     *
     * @return the line, or null if input has ended
     */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }
}
