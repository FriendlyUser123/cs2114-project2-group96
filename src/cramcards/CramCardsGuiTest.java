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

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Tests for CramCardsGui. Most tests call the public methods the buttons
 * use; testButtons clicks the real buttons to check they are connected.
 * Tests that need saving use a temporary file, deleted afterward.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.13
 */
public class CramCardsGuiTest extends student.TestCase {

    /** Seed used for every quiz in these tests. */
    private static final int SEED = 42;

    private CramCardsGui gui;
    private File file;

    /**
     * Creates an app that saves nothing, and a temporary file name.
     *
     * @throws IOException
     *             if the temporary file can't be made
     */
    public void setUp() throws IOException {
        gui = new CramCardsGui(new Random(SEED), null);
        file = File.createTempFile("cramcards-gui-test", ".txt");
        file.delete();
    }


    /**
     * Deletes the temporary file (or folder) after each test.
     */
    public void tearDown() {
        file.delete();
    }


    /**
     * Finds a component in the app by its name.
     *
     * @param container
     *            where to search
     * @param name
     *            the component's name
     * @return the component, or null if none has that name
     */
    private Component find(Container container, String name) {
        Component[] parts = container.getComponents();
        for (int i = 0; i < parts.length; i++) {
            if (name.equals(parts[i].getName())) {
                return parts[i];
            }
            if (parts[i] instanceof Container) {
                Component inside = find((Container)parts[i], name);
                if (inside != null) {
                    return inside;
                }
            }
        }
        return null;
    }


    /**
     * Finds a button by name.
     *
     * @param name
     *            the button's name
     * @return the button
     */
    private AbstractButton button(String name) {
        return (AbstractButton)find(gui.getPanel(), name);
    }


    /**
     * Finds a text field by name.
     *
     * @param name
     *            the field's name
     * @return the field
     */
    private JTextField field(String name) {
        return (JTextField)find(gui.getPanel(), name);
    }


    /**
     * Answers every card right until the quiz is over.
     */
    private void answerAllRight() {
        while (gui.isQuizRunning()) {
            gui.submitAnswer(gui.getDeck().findByQuestion(gui.getQuestionText())
                .getAnswer());
        }
    }


    /**
     * Tests a new app: empty data, idle quiz, welcome message, and every
     * component in place.
     */
    public void testConstructor() {
        assertTrue(gui.getDeck().isEmpty());
        assertFalse(gui.getHistory().hasScores());
        assertEquals(0, gui.getMissTracker().getTotalMisses());
        assertFalse(gui.isQuizRunning());
        assertEquals(CramCardsGui.WELCOME, gui.getStatus());
        assertEquals(CramCardsGui.QUIZ_IDLE, gui.getQuestionText());
        assertTrue(gui.getResultsText().startsWith(CramCardsApp.NO_QUIZZES));
        assertNotNull(gui.getPanel());
        assertFalse(button(CramCardsGui.SUBMIT_BUTTON).isEnabled());
        assertFalse(button(CramCardsGui.PRACTICE_BUTTON).isEnabled());
        assertFalse(button(CramCardsGui.ADD_ANYWAY_BUTTON).isVisible());
        assertNotNull(new CramCardsGui().getDeck());
    }


    /**
     * Tests the constructor rejects a null random source.
     */
    public void testConstructorBadInput() {
        Exception thrown = null;
        try {
            new CramCardsGui(null, null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(CramCardsGui.NULL_RANDOM, thrown.getMessage());
    }


    /**
     * Tests createFrame makes a titled window holding the app (not shown).
     */
    public void testCreateFrame() {
        JFrame frame = CramCardsGui.createFrame(gui);
        assertNotNull(frame);
        assertEquals(CramCardsGui.WINDOW_TITLE, frame.getTitle());
        assertEquals(gui.getPanel(), frame.getContentPane());
        assertFalse(frame.isVisible());
        frame.dispose();
    }


    /**
     * Tests createFrame rejects a null app.
     */
    public void testCreateFrameBadInput() {
        Exception thrown = null;
        try {
            CramCardsGui.createFrame(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(CramCardsGui.NULL_PANEL, thrown.getMessage());
    }


    /**
     * Tests main opens a visible Cram Cards window (even with null args),
     * then closes it.
     *
     * @throws InterruptedException
     *             if the wait is interrupted
     * @throws InvocationTargetException
     *             if the Swing thread fails
     */
    public void testMain()
        throws InterruptedException,
        InvocationTargetException {
        CramCardsGui.main(null);
        SwingUtilities.invokeAndWait(new Runnable() {
            /**
             * Does nothing; waiting for it lets main's window open first.
             */
            @Override
            public void run() {
                // nothing to do
            }
        });
        int opened = 0;
        Frame[] frames = Frame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isVisible() && CramCardsGui.WINDOW_TITLE.equals(
                frames[i].getTitle())) {
                opened++;
                frames[i].dispose();
            }
        }
        assertEquals(1, opened);
    }


    /**
     * Tests addCard adds a card to the deck and the list.
     */
    public void testAddCard() {
        assertTrue(gui.addCard(" 2+2 ", "4"));
        assertEquals(1, gui.getDeck().size());
        assertEquals(CramCardsApp.CARD_ADDED, gui.getStatus());
        JList<?> list = (JList<?>)find(gui.getPanel(), CramCardsGui.CARD_LIST);
        assertEquals(1, list.getModel().getSize());
        assertEquals("Q: 2+2 | A: 4", list.getModel().getElementAt(0)
            .toString());
    }


    /**
     * Tests addCard with blank and null text (refused), text that is too
     * long (cut), and a duplicate (waits for a choice).
     */
    public void testAddCardBadInput() {
        assertFalse(gui.addCard("", "4"));
        assertEquals(CramCardsApp.BLANK_INPUT, gui.getStatus());
        assertFalse(gui.addCard("2+2", "\u00A0 "));
        assertFalse(gui.addCard(null, null));
        assertTrue(gui.getDeck().isEmpty());

        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < Flashcard.MAX_LENGTH * 2; i++) {
            huge.append('x');
        }
        assertTrue(gui.addCard(huge.toString(), "long"));
        assertEquals(CramCardsApp.CARD_ADDED + " "
            + CramCardsApp.TOO_LONG_INPUT, gui.getStatus());
        assertTrue(gui.addCard("short", huge.toString()));
        assertEquals(Flashcard.MAX_LENGTH, gui.getDeck().getCards()[1]
            .getAnswer().length());

        assertFalse(gui.addCard("SHORT", huge.toString()));
        assertEquals(CramCardsGui.DUPLICATE_QUESTION, gui.getStatus());
        assertTrue(button(CramCardsGui.ADD_ANYWAY_BUTTON).isVisible());
        assertEquals(2, gui.getDeck().size());
    }


    /**
     * Tests confirmDuplicate: Add anyway adds the waiting card; Cancel
     * drops it.
     */
    public void testConfirmDuplicate() {
        gui.addCard("2+2", "4");
        gui.addCard("2+2", "4");
        assertTrue(gui.confirmDuplicate(true));
        assertEquals(2, gui.getDeck().size());
        assertEquals(CramCardsApp.CARD_ADDED, gui.getStatus());
        assertFalse(button(CramCardsGui.ADD_ANYWAY_BUTTON).isVisible());

        gui.addCard("2+2", "4");
        assertTrue(gui.confirmDuplicate(false));
        assertEquals(2, gui.getDeck().size());
        assertEquals(CramCardsApp.CARD_NOT_ADDED, gui.getStatus());
    }


    /**
     * Tests confirmDuplicate with no card waiting, including after a new
     * add replaced the waiting card.
     */
    public void testConfirmDuplicateBadInput() {
        assertFalse(gui.confirmDuplicate(true));
        assertEquals(CramCardsGui.NOTHING_WAITING, gui.getStatus());
        gui.addCard("2+2", "4");
        gui.addCard("2+2", "4");
        gui.addCard("", "");
        assertFalse(gui.confirmDuplicate(true));
        assertEquals(1, gui.getDeck().size());
    }


    /**
     * Tests deleteCard removes the card at a position, including one of
     * two duplicates.
     */
    public void testDeleteCard() {
        gui.addCard("2+2", "4");
        gui.addCard("Capital of VA?", "Richmond");
        gui.addCard("2+2", "4");
        gui.confirmDuplicate(true);
        assertTrue(gui.deleteCard(2));
        assertEquals(2, gui.getDeck().size());
        assertEquals(CramCardsApp.CARD_DELETED, gui.getStatus());
        assertTrue(gui.deleteCard(0));
        assertEquals("Capital of VA?", gui.getDeck().getCards()[0]
            .getQuestion());
    }


    /**
     * Tests deleteCard on an empty deck and with no card picked or a
     * position outside the list.
     */
    public void testDeleteCardBadInput() {
        assertFalse(gui.deleteCard(0));
        assertEquals(CramCardsApp.NO_CARDS_TO_DELETE, gui.getStatus());
        gui.addCard("2+2", "4");
        assertFalse(gui.deleteCard(-1));
        assertEquals(CramCardsGui.PICK_A_CARD, gui.getStatus());
        assertFalse(gui.deleteCard(1));
        assertEquals(1, gui.getDeck().size());
    }


    /**
     * Tests a whole quiz: two cards with the same answer; the first card is
     * missed, so the score is 1 of 2 = 50%, it is saved, and the miss is
     * counted. A second quiz shows the first score as previous.
     */
    public void testStartQuiz() {
        gui.addCard("2+2", "4");
        gui.addCard("1+3", "4");
        assertTrue(gui.startQuiz());
        assertTrue(gui.isQuizRunning());
        assertNotNull(gui.getDeck().findByQuestion(gui.getQuestionText()));
        assertFalse(gui.submitAnswer("wrong"));
        assertTrue(gui.submitAnswer(" 4 "));
        assertTrue(gui.submitAnswer("4"));
        assertFalse(gui.isQuizRunning());
        assertEquals("Score: 50% (1 of 2 right on the first try)", gui
            .getQuestionText());
        assertTrue(gui.getStatus().endsWith("Previous: none | Best: 50%"));
        assertEquals(50, gui.getHistory().getPrevious().intValue());
        assertEquals(1, gui.getMissTracker().getTotalMisses());
        assertTrue(gui.getResultsText().startsWith("Last score: 50%"));

        assertTrue(gui.startQuiz());
        answerAllRight();
        assertTrue(gui.getStatus().endsWith("Previous: 50% | Best: 100%"));
        assertEquals(2, gui.getHistory().getCount());
    }


    /**
     * Tests startQuiz on an empty deck and while a quiz is running.
     */
    public void testStartQuizBadInput() {
        assertFalse(gui.startQuiz());
        assertEquals(CramCardsApp.NO_CARDS, gui.getStatus());
        gui.addCard("2+2", "4");
        gui.startQuiz();
        assertFalse(gui.startQuiz());
        assertEquals(CramCardsGui.QUIZ_RUNNING, gui.getStatus());
    }


    /**
     * Tests submitAnswer counts QUIT as a normal (wrong) answer, since the
     * window has an End quiz button.
     */
    public void testSubmitAnswer() {
        gui.addCard("Stop word?", "quit");
        gui.startQuiz();
        assertTrue(gui.submitAnswer("QUIT"));
        assertFalse(gui.isQuizRunning());
        assertEquals(100, gui.getHistory().getPrevious().intValue());
    }


    /**
     * Tests submitAnswer with no quiz running, and that null or blank
     * answers count as wrong.
     */
    public void testSubmitAnswerBadInput() {
        assertFalse(gui.submitAnswer("4"));
        assertEquals(CramCardsGui.START_A_QUIZ, gui.getStatus());
        gui.addCard("2+2", "4");
        gui.startQuiz();
        assertFalse(gui.submitAnswer(null));
        assertFalse(gui.submitAnswer("   "));
        assertTrue(gui.isQuizRunning());
        assertEquals(2, gui.getMissTracker().getTotalMisses());
    }


    /**
     * Tests endQuiz stops a quiz without saving its score but keeps its
     * misses.
     */
    public void testEndQuiz() {
        gui.addCard("2+2", "4");
        gui.startQuiz();
        gui.submitAnswer("5");
        assertTrue(gui.endQuiz());
        assertFalse(gui.isQuizRunning());
        assertEquals(CramCardsApp.QUIZ_ENDED_EARLY, gui.getStatus());
        assertEquals(CramCardsGui.QUIZ_IDLE, gui.getQuestionText());
        assertFalse(gui.getHistory().hasScores());
        assertEquals(1, gui.getMissTracker().getTotalMisses());
    }


    /**
     * Tests that cards can be added during a quiz: the quiz keeps its own
     * copy, and the hardest-cards button stays off until the quiz ends.
     */
    public void testAddCardDuringQuiz() {
        gui.addCard("2+2", "4");
        for (int i = 0; i < MissTracker.HARD_THRESHOLD; i++) {
            gui.getMissTracker().recordMiss(gui.getDeck().getCards()[0]);
        }
        gui.startQuiz();
        assertTrue(gui.addCard("3+3", "6"));
        assertFalse(button(CramCardsGui.PRACTICE_BUTTON).isEnabled());
        assertTrue(gui.submitAnswer("4"));
        assertFalse(gui.isQuizRunning());
        assertEquals(2, gui.getDeck().size());
        assertTrue(button(CramCardsGui.PRACTICE_BUTTON).isEnabled());
    }


    /**
     * Tests endQuiz with no quiz running.
     */
    public void testEndQuizBadInput() {
        assertFalse(gui.endQuiz());
        assertEquals(CramCardsGui.START_A_QUIZ, gui.getStatus());
    }


    /**
     * Tests practiceHardest: a card missed three times becomes hard, the
     * button turns on, and practicing it doesn't save a score.
     */
    public void testPracticeHardest() {
        gui.addCard("2+2", "4");
        gui.addCard("Capital of VA?", "Richmond");
        Flashcard math = gui.getDeck().getCards()[0];
        for (int i = 0; i < MissTracker.HARD_THRESHOLD; i++) {
            gui.getMissTracker().recordMiss(math);
        }
        gui.startQuiz();
        answerAllRight();
        assertTrue(button(CramCardsGui.PRACTICE_BUTTON).isEnabled());
        assertTrue(gui.getResultsText().endsWith(
            "Hard cards right now: 1 (missed 3+ times)"));
        assertTrue(gui.practiceHardest());
        assertEquals("2+2", gui.getQuestionText());
        assertTrue(gui.submitAnswer("4"));
        assertTrue(gui.getStatus().endsWith("Practice scores are not saved."));
        assertEquals(1, gui.getHistory().getCount());
    }


    /**
     * Tests practiceHardest with no hard cards and while a quiz is running.
     */
    public void testPracticeHardestBadInput() {
        assertFalse(gui.practiceHardest());
        assertEquals(CramCardsApp.NO_HARD_CARDS, gui.getStatus());
        gui.addCard("2+2", "4");
        gui.startQuiz();
        assertFalse(gui.practiceHardest());
        assertEquals(CramCardsGui.QUIZ_RUNNING, gui.getStatus());
    }


    /**
     * Tests the buttons and Enter keys run the right methods: type a card,
     * click Add, add a duplicate with Add anyway, cancel another, delete
     * the selected card, then quiz with Submit, Enter, and End quiz.
     */
    public void testButtons() {
        field(CramCardsGui.QUESTION_FIELD).setText("2+2");
        field(CramCardsGui.ANSWER_FIELD).setText("4");
        button(CramCardsGui.ADD_BUTTON).doClick();
        assertEquals(1, gui.getDeck().size());
        assertEquals("", field(CramCardsGui.QUESTION_FIELD).getText());

        field(CramCardsGui.QUESTION_FIELD).setText("2+2");
        field(CramCardsGui.ANSWER_FIELD).setText("4");
        field(CramCardsGui.ANSWER_FIELD).postActionEvent();
        button(CramCardsGui.ADD_ANYWAY_BUTTON).doClick();
        assertEquals(2, gui.getDeck().size());
        gui.addCard("2+2", "4");
        button(CramCardsGui.CANCEL_BUTTON).doClick();
        assertEquals(CramCardsApp.CARD_NOT_ADDED, gui.getStatus());

        JList<?> list = (JList<?>)find(gui.getPanel(), CramCardsGui.CARD_LIST);
        list.setSelectedIndex(1);
        button(CramCardsGui.DELETE_BUTTON).doClick();
        assertEquals(1, gui.getDeck().size());

        button(CramCardsGui.START_BUTTON).doClick();
        assertTrue(gui.isQuizRunning());
        field(CramCardsGui.RESPONSE_FIELD).setText("5");
        button(CramCardsGui.SUBMIT_BUTTON).doClick();
        assertEquals(1, gui.getMissTracker().getTotalMisses());
        field(CramCardsGui.RESPONSE_FIELD).setText("6");
        field(CramCardsGui.RESPONSE_FIELD).postActionEvent();
        assertEquals(2, gui.getMissTracker().getTotalMisses());
        button(CramCardsGui.END_BUTTON).doClick();
        assertFalse(gui.isQuizRunning());

        gui.getMissTracker().recordMiss(gui.getDeck().getCards()[0]);
        button(CramCardsGui.START_BUTTON).doClick();
        answerAllRight();
        button(CramCardsGui.PRACTICE_BUTTON).doClick();
        assertTrue(gui.isQuizRunning());
    }


    /**
     * Tests that cards, scores, and misses are saved and loaded by the next
     * app using the same file.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testSaveAndLoad() throws IOException {
        DeckStore store = new DeckStore(file);
        CramCardsGui first = new CramCardsGui(new Random(SEED), store);
        assertEquals(CramCardsGui.WELCOME, first.getStatus());
        first.addCard("2+2", "4");
        first.addCard("Capital of VA?", "Richmond");
        first.deleteCard(1);
        first.startQuiz();
        first.submitAnswer("5");
        first.endQuiz();
        first.startQuiz();
        first.submitAnswer("4");

        CramCardsGui second = new CramCardsGui(new Random(SEED), store);
        assertEquals(1, second.getDeck().size());
        assertEquals(100, second.getHistory().getPrevious().intValue());
        assertEquals(1, second.getMissTracker().getTotalMisses());
        assertEquals("Loaded 1 card(s) and 1 score(s) from " + file.getName()
            + ".", second.getStatus());
    }


    /**
     * Tests a save file path that is a folder: loading and saving report
     * the problem in the status bar instead of crashing.
     *
     * @throws IOException
     *             if the folder can't be made
     */
    public void testSaveAndLoadFailures() throws IOException {
        assertTrue(file.mkdir());
        CramCardsGui broken = new CramCardsGui(new Random(SEED), new DeckStore(
            file));
        assertTrue(broken.getStatus().startsWith(DeckStore.LOAD_FAILED));
        assertTrue(broken.addCard("2+2", "4"));
        assertTrue(broken.getStatus().contains(DeckStore.SAVE_FAILED));
        assertEquals(1, broken.getDeck().size());
    }


    /**
     * Tests that text which is blank once cut to the length limit is
     * refused instead of crashing.
     */
    public void testAddCardHiddenBlank() {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < Flashcard.MAX_LENGTH; i++) {
            spaces.append('\u00A0');
        }
        String hiddenBlank = spaces.append('x').toString();
        assertFalse(gui.addCard(hiddenBlank, "4"));
        assertFalse(gui.addCard("2+2", hiddenBlank));
        assertEquals(CramCardsApp.BLANK_INPUT, gui.getStatus());
        assertTrue(gui.getDeck().isEmpty());
    }


    /**
     * Tests that cards can't be deleted during a quiz, and that deleting or
     * starting a quiz drops a waiting duplicate.
     */
    public void testDeleteCardDuringQuiz() {
        gui.addCard("2+2", "4");
        gui.addCard("2+2", "4");
        assertTrue(button(CramCardsGui.ADD_ANYWAY_BUTTON).isVisible());
        assertFalse(gui.deleteCard(5));
        assertFalse(button(CramCardsGui.ADD_ANYWAY_BUTTON).isVisible());
        assertFalse(gui.confirmDuplicate(true));

        gui.addCard("2+2", "4");
        gui.startQuiz();
        assertFalse(button(CramCardsGui.ADD_ANYWAY_BUTTON).isVisible());
        assertFalse(button(CramCardsGui.DELETE_BUTTON).isEnabled());
        assertFalse(gui.deleteCard(0));
        assertEquals(CramCardsGui.QUIZ_RUNNING, gui.getStatus());
        assertEquals(1, gui.getDeck().size());
        gui.endQuiz();
        assertTrue(button(CramCardsGui.DELETE_BUTTON).isEnabled());
    }


    /**
     * Tests Enter in the question field moves on without adding a card.
     */
    public void testEnterInQuestionField() {
        field(CramCardsGui.QUESTION_FIELD).setText("2+2");
        field(CramCardsGui.QUESTION_FIELD).postActionEvent();
        assertTrue(gui.getDeck().isEmpty());
        assertEquals("2+2", field(CramCardsGui.QUESTION_FIELD).getText());
    }
}
