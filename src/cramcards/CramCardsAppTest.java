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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;

/**
 * Tests for CramCardsApp. Each test scripts the user's input with a Scanner
 * over a String and reads what the app printed from a byte stream.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class CramCardsAppTest extends student.TestCase {

    /** Seed used for every quiz in these tests. */
    private static final int SEED = 42;

    private ByteArrayOutputStream output;
    private File file;

    /**
     * Clears the captured output and picks a temporary save file name before
     * each test.
     *
     * @throws IOException
     *             if the temporary file can't be made
     */
    public void setUp() throws IOException {
        output = new ByteArrayOutputStream();
        file = File.createTempFile("cramcards-app-test", ".txt");
        file.delete();
    }


    /**
     * Deletes the temporary save file (or folder) after each test.
     */
    public void tearDown() {
        file.delete();
    }


    /**
     * Makes an app that saves to the temporary file.
     *
     * @param lines
     *            the user's input, one line per element
     * @return the app
     */
    private CramCardsApp savingAppWith(String... lines) {
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            input.append(lines[i]).append('\n');
        }
        return new CramCardsApp(new Scanner(input.toString()), new PrintStream(
            output), new Random(SEED), new DeckStore(file));
    }


    /**
     * Makes an app that reads the given lines and prints into output.
     *
     * @param lines
     *            the user's input, one line per element
     * @return the app
     */
    private CramCardsApp appWith(String... lines) {
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            input.append(lines[i]).append('\n');
        }
        return new CramCardsApp(new Scanner(input.toString()), new PrintStream(
            output), new Random(SEED));
    }


    /**
     * Gets everything the app printed.
     *
     * @return the printed text
     */
    private String printed() {
        return output.toString();
    }


    /**
     * Adds a card straight to an app's deck.
     *
     * @param app
     *            the app
     * @param question
     *            the question
     * @param answer
     *            the answer
     */
    private void addCard(CramCardsApp app, String question, String answer) {
        app.getDeck().addCard(new Flashcard(question, answer));
    }


    /**
     * Tests both constructors: a new app has an empty deck, history, and
     * miss tracker.
     */
    public void testConstructors() {
        CramCardsApp app = appWith();
        assertTrue(app.getDeck().isEmpty());
        assertFalse(app.getHistory().hasScores());
        assertEquals(0, app.getMissTracker().getTotalMisses());
        CramCardsApp keyboard = new CramCardsApp();
        assertTrue(keyboard.getDeck().isEmpty());
    }


    /**
     * Tests that a null scanner, output, or random is rejected.
     */
    public void testConstructorBadInput() {
        Scanner scanner = new Scanner("");
        PrintStream print = new PrintStream(output);
        Random random = new Random(SEED);
        Object[][] bad = { { null, print, random }, { scanner, null, random }, {
            scanner, print, null } };
        for (int i = 0; i < bad.length; i++) {
            Exception thrown = null;
            try {
                new CramCardsApp((Scanner)bad[i][0], (PrintStream)bad[i][1],
                    (Random)bad[i][2]);
            }
            catch (Exception exception) {
                thrown = exception;
            }
            assertTrue(thrown instanceof IllegalArgumentException);
            assertEquals(CramCardsApp.NULL_ARGUMENT, thrown.getMessage());
            Exception withStore = null;
            try {
                new CramCardsApp((Scanner)bad[i][0], (PrintStream)bad[i][1],
                    (Random)bad[i][2], new DeckStore(file));
            }
            catch (Exception exception) {
                withStore = exception;
            }
            assertTrue(withStore instanceof IllegalArgumentException);
        }
    }


    /**
     * Tests main with scripted keyboard input: a bad menu choice shows the
     * error, then 7 quits with Goodbye.
     */
    public void testMain() {
        InputStream realIn = System.in;
        PrintStream realOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream("abc\n7\n".getBytes()));
            System.setOut(new PrintStream(output));
            CramCardsApp.main(new String[0]);
        }
        finally {
            System.setIn(realIn);
            System.setOut(realOut);
        }
        assertTrue(printed().contains(CramCardsApp.MENU_ERROR));
        assertTrue(printed().contains(CramCardsApp.GOODBYE));
    }


    /**
     * Tests parseMenuChoice for every valid choice and with spaces.
     */
    public void testParseMenuChoice() {
        CramCardsApp app = appWith();
        assertEquals(CramCardsApp.ADD_CHOICE, app.parseMenuChoice("1"));
        assertEquals(CramCardsApp.QUIZ_CHOICE, app.parseMenuChoice(" 4 "));
        assertEquals(CramCardsApp.QUIT_CHOICE, app.parseMenuChoice("7"));
    }


    /**
     * Tests parseMenuChoice rejects out-of-range, non-number, blank, and
     * null input with the menu error message.
     */
    public void testParseMenuChoiceBadInput() {
        CramCardsApp app = appWith();
        String[] bad = { "9", "0", "abc", "", null, "+7", "07", "\u0667",
            "\uFF11", "7.0" };
        for (int i = 0; i < bad.length; i++) {
            Exception thrown = null;
            try {
                app.parseMenuChoice(bad[i]);
            }
            catch (Exception exception) {
                thrown = exception;
            }
            assertTrue(thrown instanceof IllegalArgumentException);
            assertEquals(CramCardsApp.MENU_ERROR, thrown.getMessage());
        }
    }


    /**
     * Tests readNonBlank returns trimmed text.
     */
    public void testReadNonBlank() {
        CramCardsApp app = appWith("  Cards  ");
        assertEquals("Cards", app.readNonBlank("Answer:"));
    }


    /**
     * Tests readNonBlank re-prompts on blank lines, cuts long input, and
     * returns null when input ends.
     */
    public void testReadNonBlankBadInput() {
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            huge.append('x');
        }
        CramCardsApp app = appWith("", "   ", "Cards", huge.toString());
        assertEquals("Cards", app.readNonBlank("Answer:"));
        assertEquals(2, printed().split(CramCardsApp.BLANK_INPUT, -1).length
            - 1);
        assertEquals(Flashcard.MAX_LENGTH, app.readNonBlank("Answer:")
            .length());
        assertTrue(printed().contains(CramCardsApp.TOO_LONG_INPUT));
        assertNull(app.readNonBlank("Answer:"));
    }


    /**
     * Tests readNonBlank treats Unicode spaces as blank and never cuts an
     * emoji in half at the length limit.
     */
    public void testReadNonBlankUnicode() {
        StringBuilder emojiAtEnd = new StringBuilder();
        for (int i = 0; i < Flashcard.MAX_LENGTH - 1; i++) {
            emojiAtEnd.append('x');
        }
        emojiAtEnd.append("\uD83D\uDE00").append("yyy");
        CramCardsApp app = appWith("\u00A0\u3000", "ok", emojiAtEnd.toString());
        assertEquals("ok", app.readNonBlank("Answer:"));
        assertTrue(printed().contains(CramCardsApp.BLANK_INPUT));
        String cut = app.readNonBlank("Answer:");
        assertEquals(Flashcard.MAX_LENGTH - 1, cut.length());
        assertFalse(Character.isHighSurrogate(cut.charAt(cut.length() - 1)));
    }


    /**
     * Tests readYesNo accepts y, yes, n, and no in any case.
     */
    public void testReadYesNo() {
        CramCardsApp app = appWith("Y", "no", "YES", "n");
        assertTrue(app.readYesNo("?"));
        assertFalse(app.readYesNo("?"));
        assertTrue(app.readYesNo("?"));
        assertFalse(app.readYesNo("?"));
    }


    /**
     * Tests readYesNo re-prompts on other input and is false when input
     * ends.
     */
    public void testReadYesNoBadInput() {
        CramCardsApp app = appWith("maybe", "y");
        assertTrue(app.readYesNo("?"));
        assertTrue(printed().contains(CramCardsApp.YES_NO_ERROR));
        assertFalse(app.readYesNo("?"));
    }


    /**
     * Tests addCardFlow adds a new card.
     */
    public void testAddCardFlow() {
        CramCardsApp app = appWith("2+2", "4");
        app.addCardFlow();
        assertEquals(1, app.getDeck().size());
        assertTrue(printed().contains(CramCardsApp.CARD_ADDED));
    }


    /**
     * Tests addCardFlow with a duplicate (declined, then accepted), blank
     * input, and input that ends early.
     */
    public void testAddCardFlowBadInput() {
        CramCardsApp app = appWith("", "2+2", "4", " 2+2 ", "4", "n", "2+2",
            "4", "maybe", "y", "3+3");
        app.addCardFlow();
        assertTrue(printed().contains(CramCardsApp.BLANK_INPUT));
        app.addCardFlow();
        assertTrue(printed().contains(CramCardsApp.DUPLICATE_PROMPT));
        assertTrue(printed().contains(CramCardsApp.CARD_NOT_ADDED));
        assertEquals(1, app.getDeck().size());
        app.addCardFlow();
        assertTrue(printed().contains(CramCardsApp.YES_NO_ERROR));
        assertEquals(2, app.getDeck().size());
        app.addCardFlow();
        assertEquals(2, app.getDeck().size());
        appWith().addCardFlow();
    }


    /**
     * Tests viewCards lists cards in the order added.
     */
    public void testViewCards() {
        CramCardsApp app = appWith();
        addCard(app, "2+2", "4");
        addCard(app, "Capital of VA?", "Richmond");
        app.viewCards();
        assertTrue(printed().contains("1. Q: 2+2 | A: 4"));
        assertTrue(printed().contains("2. Q: Capital of VA? | A: Richmond"));
    }


    /**
     * Tests viewCards on an empty deck.
     */
    public void testViewCardsEmpty() {
        CramCardsApp app = appWith();
        app.viewCards();
        assertTrue(printed().contains(CramCardsApp.NO_CARDS));
    }


    /**
     * Tests deleteCardFlow removes the matching card.
     */
    public void testDeleteCardFlow() {
        CramCardsApp app = appWith(" 2+2 ");
        addCard(app, "2+2", "4");
        app.deleteCardFlow();
        assertTrue(app.getDeck().isEmpty());
        assertTrue(printed().contains(CramCardsApp.CARD_DELETED));
    }


    /**
     * Tests deleteCardFlow on an empty deck, with a card that isn't there,
     * and when input ends.
     */
    public void testDeleteCardFlowBadInput() {
        CramCardsApp empty = appWith();
        empty.deleteCardFlow();
        assertTrue(printed().contains(CramCardsApp.NO_CARDS_TO_DELETE));

        CramCardsApp app = appWith("3+3");
        addCard(app, "2+2", "4");
        app.deleteCardFlow();
        assertTrue(printed().contains(CramCardsApp.CARD_NOT_FOUND));
        assertEquals(1, app.getDeck().size());
        app.deleteCardFlow();
        assertEquals(1, app.getDeck().size());
    }


    /**
     * Tests quizFlow: two cards with the same answer, the first one drawn is
     * missed, then everything is right, so the score is 1 of 2 = 50%, it is
     * saved, and the miss is tracked. A second quiz shows the first score as
     * previous.
     */
    public void testQuizFlow() {
        CramCardsApp app = appWith("wrong", "4", "4", "4", "4");
        addCard(app, "2+2", "4");
        addCard(app, "1+3", "4");
        app.quizFlow();
        assertTrue(printed().contains("Score: 50% (1 of 2"));
        assertTrue(printed().contains("Previous: none | Best: 50%"));
        assertEquals(50, app.getHistory().getPrevious().intValue());
        assertEquals(1, app.getMissTracker().getTotalMisses());
        app.quizFlow();
        assertTrue(printed().contains("Previous: 50% | Best: 100%"));
        assertEquals(2, app.getHistory().getCount());
    }


    /**
     * Tests quizFlow on an empty deck, with QUIT, and when input ends: no
     * crash and no score saved.
     */
    public void testQuizFlowBadInput() {
        CramCardsApp empty = appWith();
        empty.quizFlow();
        assertTrue(printed().contains(CramCardsApp.NO_CARDS));

        CramCardsApp app = appWith(" quit ");
        addCard(app, "2+2", "4");
        app.quizFlow();
        assertTrue(printed().contains(CramCardsApp.QUIZ_ENDED_EARLY));
        assertFalse(app.getHistory().hasScores());
        app.quizFlow();
        assertFalse(app.getHistory().hasScores());
    }


    /**
     * Tests showResults after a quiz.
     */
    public void testShowResults() {
        CramCardsApp app = appWith();
        app.getHistory().record(70);
        app.getHistory().record(90);
        app.showResults();
        assertTrue(printed().contains(
            "Last score: 90% | Best: 90% | Average: 80.0% | Quizzes taken: 2"));
    }


    /**
     * Tests showResults before any quiz.
     */
    public void testShowResultsEmpty() {
        CramCardsApp app = appWith();
        app.showResults();
        assertTrue(printed().contains(CramCardsApp.NO_QUIZZES));
    }


    /**
     * Tests practiceHardestFlow: a card missed three times becomes a hard
     * card, and practicing it does not save a score.
     */
    public void testPracticeHardestFlow() {
        CramCardsApp app = appWith("5", "5", "5", "4", "4");
        addCard(app, "2+2", "4");
        app.quizFlow();
        assertEquals(MissTracker.HARD_THRESHOLD, app.getMissTracker().missCount(
            new Flashcard("2+2", "4")));
        app.practiceHardestFlow();
        assertTrue(printed().contains("Practicing 1 hard card(s)."));
        assertTrue(printed().contains("Practice scores are not saved."));
        assertEquals(1, app.getHistory().getCount());
    }


    /**
     * Tests practiceHardestFlow with no hard cards and with an empty deck.
     */
    public void testPracticeHardestFlowEmpty() {
        CramCardsApp app = appWith();
        app.practiceHardestFlow();
        addCard(app, "2+2", "4");
        app.getMissTracker().recordMiss(new Flashcard("2+2", "4"));
        app.practiceHardestFlow();
        assertEquals(2, printed().split(CramCardsApp.NO_HARD_CARDS, -1).length
            - 1);
    }


    /**
     * Tests run on the MVP path: add a card, view, quiz, view results, quit.
     */
    public void testRun() {
        CramCardsApp app = appWith("1", "2+2", "4", "2", "4", "4", "5", "7");
        app.run();
        assertEquals(1, app.getDeck().size());
        assertEquals(100, app.getHistory().getPrevious().intValue());
        assertTrue(printed().contains("Quizzes taken: 1"));
        assertTrue(printed().endsWith(CramCardsApp.GOODBYE + System
            .lineSeparator()));
    }


    /**
     * Tests run with bad menu input, every choice on an empty deck, and input
     * that ends without choosing Quit: no crash, and Goodbye is printed.
     */
    public void testRunBadInput() {
        CramCardsApp app = appWith("abc", "9", "2", "3", "4", "5", "6");
        app.run();
        String text = printed();
        assertEquals(2, text.split(CramCardsApp.MENU_ERROR, -1).length - 1);
        assertTrue(text.contains(CramCardsApp.NO_CARDS));
        assertTrue(text.contains(CramCardsApp.NO_CARDS_TO_DELETE));
        assertTrue(text.contains(CramCardsApp.NO_QUIZZES));
        assertTrue(text.contains(CramCardsApp.NO_HARD_CARDS));
        assertTrue(text.contains(CramCardsApp.GOODBYE));
    }


    /**
     * Tests that a saving app keeps cards, scores, and misses for the next
     * run: add two cards, delete one, quiz with a miss, practice, quit; the
     * next run loads them.
     */
    public void testSaveAndLoad() {
        savingAppWith("1", "2+2", "4", "1", "3+3", "6", "3", "3+3", "4", "5",
            "4", "6", "7").run();
        assertFalse(printed().contains(DeckStore.SAVE_FAILED));
        CramCardsApp next = savingAppWith("2", "7");
        next.run();
        assertTrue(printed().contains("Loaded 1 card(s) and 1 score(s) from "
            + file.getName() + "."));
        assertTrue(printed().contains("1. Q: 2+2 | A: 4"));
        assertEquals(0, next.getHistory().getPrevious().intValue());
        assertEquals(1, next.getMissTracker().getTotalMisses());
    }


    /**
     * Tests a save file path that is a folder: the load and every save print
     * a message instead of crashing, and the app keeps working.
     */
    public void testSaveAndLoadFailures() {
        assertTrue(file.mkdir());
        CramCardsApp app = savingAppWith("1", "2+2", "4", "3", "2+2", "4", "7");
        app.run();
        assertTrue(printed().contains(DeckStore.LOAD_FAILED));
        assertTrue(printed().contains(DeckStore.SAVE_FAILED));
        assertTrue(printed().contains(CramCardsApp.CARD_DELETED));
        assertTrue(printed().contains(CramCardsApp.NO_CARDS));
    }


    /**
     * Tests that input which is blank once cut to the length limit (500
     * non-breaking spaces, then a letter) is treated as blank, not crashed
     * on.
     */
    public void testReadNonBlankHiddenBlank() {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < Flashcard.MAX_LENGTH; i++) {
            spaces.append('\u00A0');
        }
        String hiddenBlank = spaces.append('x').toString();
        CramCardsApp app = appWith(hiddenBlank, "ok");
        assertEquals("ok", app.readNonBlank("Question:"));
        assertTrue(printed().contains(CramCardsApp.BLANK_INPUT));
    }


    /**
     * Tests the warning after adding a card whose answer is the terminal's
     * quit word, and that other answers get no warning.
     */
    public void testAddCardFlowQuitAnswer() {
        CramCardsApp app = appWith("Stop word?", " Quit ", "2+2", "4");
        app.addCardFlow();
        assertTrue(printed().contains(CramCardsApp.QUIT_ANSWER_WARNING));
        output.reset();
        app.addCardFlow();
        assertFalse(printed().contains(CramCardsApp.QUIT_ANSWER_WARNING));
        assertEquals(2, app.getDeck().size());
    }
}
