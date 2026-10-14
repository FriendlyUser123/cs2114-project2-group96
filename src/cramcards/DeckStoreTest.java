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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Tests for DeckStore. Each test uses its own temporary save file, deleted
 * afterward.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.13
 */
public class DeckStoreTest extends student.TestCase {

    private File file;
    private DeckStore store;
    private Deck deck;
    private ScoreHistory history;
    private MissTracker misses;

    /**
     * Creates an empty temporary file name and empty data before each test.
     *
     * @throws IOException
     *             if the temporary file can't be made
     */
    public void setUp() throws IOException {
        file = File.createTempFile("cramcards-test", ".txt");
        file.delete();
        store = new DeckStore(file);
        deck = new Deck();
        history = new ScoreHistory();
        misses = new MissTracker();
    }


    /**
     * Deletes the temporary file after each test.
     */
    public void tearDown() {
        file.delete();
    }


    /**
     * Writes lines straight into the save file.
     *
     * @param lines
     *            the file's lines
     * @throws IOException
     *             if the file can't be written
     */
    private void writeFile(String... lines) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(file), StandardCharsets.UTF_8));
        for (int i = 0; i < lines.length; i++) {
            writer.println(lines[i]);
        }
        writer.close();
    }


    /**
     * Tests the constructor and getFile.
     */
    public void testConstructor() {
        assertEquals(file, store.getFile());
    }


    /**
     * Tests the constructor rejects a null file.
     */
    public void testConstructorBadInput() {
        Exception thrown = null;
        try {
            new DeckStore(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(DeckStore.NULL_MESSAGE, thrown.getMessage());
    }


    /**
     * Tests a save then load brings back the cards in order, every score
     * with the latest still latest, and the miss counts, including text
     * with tabs, newlines, carriage returns, backslashes, and an emoji.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testSaveThenLoad() throws IOException {
        Flashcard tricky = new Flashcard(
            "Tab\there,\nnew line,\r\nback\\slash \uD83D\uDE00", "a\\tb");
        Flashcard math = new Flashcard("2+2", "4");
        deck.addCard(math);
        deck.addCard(tricky);
        deck.addCard(new Flashcard("2+2", "4"));
        history.record(90);
        history.record(70);
        history.record(90);
        history.record(50);
        misses.recordMiss(math);
        misses.recordMiss(math);
        misses.recordMiss(tricky);
        store.save(deck, history, misses);

        Deck loadedDeck = new Deck();
        ScoreHistory loadedHistory = new ScoreHistory();
        MissTracker loadedMisses = new MissTracker();
        assertEquals(0, store.load(loadedDeck, loadedHistory, loadedMisses));
        Flashcard[] cards = loadedDeck.getCards();
        assertEquals(3, cards.length);
        assertEquals(math, cards[0]);
        assertEquals(tricky.getQuestion(), cards[1].getQuestion());
        assertEquals(tricky.getAnswer(), cards[1].getAnswer());
        assertEquals(4, loadedHistory.getCount());
        assertEquals(50, loadedHistory.getPrevious().intValue());
        assertEquals(90, loadedHistory.getBest());
        assertEquals(75.0, loadedHistory.getAverage(), 0.001);
        assertEquals(2, loadedMisses.missCount(math));
        assertEquals(1, loadedMisses.missCount(tricky));
        assertEquals(3, loadedMisses.getTotalMisses());
    }


    /**
     * Tests that the latest score is still latest after loading even when
     * getScores() returns it last instead of first (the history is a bag,
     * so the save can't rely on its order).
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testSaveScoresAnyOrder() throws IOException {
        ScoreHistory oldestFirst = new ScoreHistory() {
            /**
             * Returns the scores oldest first.
             *
             * @return 40, 90, 60
             */
            @Override
            public Integer[] getScores() {
                return new Integer[] { 40, 90, 60 };
            }
        };
        oldestFirst.record(40);
        oldestFirst.record(90);
        oldestFirst.record(60);
        store.save(deck, oldestFirst, misses);
        store.load(deck, history, misses);
        assertEquals(3, history.getCount());
        assertEquals(60, history.getPrevious().intValue());
    }


    /**
     * Tests saving an empty deck and history, then loading: nothing comes
     * back and no line is skipped.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testSaveEmpty() throws IOException {
        store.save(deck, history, misses);
        assertTrue(file.exists());
        assertEquals(0, store.load(deck, history, misses));
        assertTrue(deck.isEmpty());
        assertFalse(history.hasScores());
        assertEquals(0, misses.getTotalMisses());
    }


    /**
     * Tests that misses for a card no longer in the deck aren't saved.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testSaveSkipsDeletedCardMisses() throws IOException {
        misses.recordMiss(new Flashcard("gone", "card"));
        store.save(deck, history, misses);
        MissTracker loaded = new MissTracker();
        store.load(deck, history, loaded);
        assertEquals(0, loaded.getTotalMisses());
    }


    /**
     * Tests save and load reject null arguments.
     *
     * @throws IOException
     *             never, because the null check comes first
     */
    public void testSaveLoadBadInput() throws IOException {
        Object[][] bad = { { null, history, misses }, { deck, null, misses }, {
            deck, history, null } };
        for (int i = 0; i < bad.length; i++) {
            Exception saveThrown = null;
            try {
                store.save((Deck)bad[i][0], (ScoreHistory)bad[i][1],
                    (MissTracker)bad[i][2]);
            }
            catch (IllegalArgumentException exception) {
                saveThrown = exception;
            }
            assertNotNull(saveThrown);
            Exception loadThrown = null;
            try {
                store.load((Deck)bad[i][0], (ScoreHistory)bad[i][1],
                    (MissTracker)bad[i][2]);
            }
            catch (IllegalArgumentException exception) {
                loadThrown = exception;
            }
            assertNotNull(loadThrown);
        }
    }


    /**
     * Tests loading when there is no save file yet: nothing is loaded and
     * nothing crashes.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testLoadMissingFile() throws IOException {
        assertFalse(file.exists());
        assertEquals(0, store.load(deck, history, misses));
        assertTrue(deck.isEmpty());
        assertFalse(history.hasScores());
    }


    /**
     * Tests that bad lines are skipped and counted while good lines still
     * load. Blank lines are ignored.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testLoadMalformedLines() throws IOException {
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i <= Flashcard.MAX_LENGTH; i++) {
            huge.append('x');
        }
        writeFile("CARD\t2+2\t4", "", "   ", "HELLO\tworld", "CARD\tonly one",
            "CARD\t \t4", "CARD\t" + huge + "\t4", "SCORE\tabc", "SCORE\t200",
            "SCORE\t-1", "SCORE\t80\textra", "SCORE\t 60 ", "MISS\t0\t2+2\t4",
            "MISS\tx\t2+2\t4", "MISS\t2\t\t4", "MISS\t1\t2+2",
            "MISS\t3\t2+2\t4", "CARD\tends with \\\tbackslash \\q",
            "MISS\t2000000000\t2+2\t4");
        assertEquals(13, store.load(deck, history, misses));
        assertEquals(2, deck.size());
        assertEquals("ends with \\", deck.getCards()[1].getQuestion());
        assertEquals("backslash \\q", deck.getCards()[1].getAnswer());
        assertEquals(1, history.getCount());
        assertEquals(60, history.getPrevious().intValue());
        assertEquals(3, misses.missCount(new Flashcard("2+2", "4")));
    }


    /**
     * Tests that a save file path that is a folder fails with an
     * IOException on both save and load.
     *
     * @throws IOException
     *             if the folder can't be made
     */
    public void testFolderInsteadOfFile() throws IOException {
        assertTrue(file.mkdir());
        Exception saveThrown = null;
        try {
            store.save(deck, history, misses);
        }
        catch (IOException exception) {
            saveThrown = exception;
        }
        assertNotNull(saveThrown);
        Exception loadThrown = null;
        try {
            store.load(deck, history, misses);
        }
        catch (IOException exception) {
            loadThrown = exception;
        }
        assertNotNull(loadThrown);
    }


    /**
     * Tests loadSummary with cards and scores, and with skipped lines.
     */
    public void testLoadSummary() {
        deck.addCard(new Flashcard("2+2", "4"));
        history.record(80);
        assertEquals("Loaded 1 card(s) and 1 score(s) from " + file.getName()
            + ".", store.loadSummary(deck, history, 0));
        assertEquals("Loaded 1 card(s) and 1 score(s) from " + file.getName()
            + ". Skipped 2 line(s) in the save file that weren't valid.", store
                .loadSummary(deck, history, 2));
    }


    /**
     * Tests loadSummary when nothing was loaded, when only bad lines were
     * found, and with null arguments.
     */
    public void testLoadSummaryEmpty() {
        assertEquals("", store.loadSummary(deck, history, 0));
        assertEquals("Skipped 1 line(s) in the save file that weren't valid.",
            store.loadSummary(deck, history, 1));
        history.record(80);
        assertTrue(store.loadSummary(deck, history, 0).startsWith(
            "Loaded 0 card(s) and 1 score(s)"));
        Exception thrown = null;
        try {
            store.loadSummary(null, history, 0);
        }
        catch (IllegalArgumentException exception) {
            thrown = exception;
        }
        assertNotNull(thrown);
        thrown = null;
        try {
            store.loadSummary(deck, null, 0);
        }
        catch (IllegalArgumentException exception) {
            thrown = exception;
        }
        assertNotNull(thrown);
    }


    /**
     * Tests that a byte order mark at the start of the file (added by some
     * editors, like Notepad) doesn't cost the first line.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testLoadByteOrderMark() throws IOException {
        writeFile("\uFEFFCARD\t2+2\t4", "SCORE\t90");
        assertEquals(0, store.load(deck, history, misses));
        assertEquals(1, deck.size());
        assertEquals(90, history.getPrevious().intValue());
    }


    /**
     * Tests files whose first line is empty or is only a byte order mark.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testLoadByteOrderMarkBadInput() throws IOException {
        writeFile("", "CARD\t2+2\t4");
        assertEquals(0, store.load(deck, history, misses));
        writeFile("\uFEFF");
        assertEquals(0, store.load(deck, history, misses));
        assertEquals(1, deck.size());
    }


    /**
     * Tests that saving leaves no temporary file behind, and that a failed
     * save (the save path is a folder) cleans up its temporary file.
     *
     * @throws IOException
     *             if the file can't be used
     */
    public void testSaveLeavesNoTempFile() throws IOException {
        File temp = new File(file.getPath() + ".tmp");
        store.save(deck, history, misses);
        assertTrue(file.exists());
        assertFalse(temp.exists());
        assertTrue(file.delete());
        assertTrue(file.mkdir());
        Exception thrown = null;
        try {
            store.save(deck, history, misses);
        }
        catch (IOException exception) {
            thrown = exception;
        }
        assertNotNull(thrown);
        assertFalse(temp.exists());
    }
}
