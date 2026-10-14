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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Stretch goal "Persistence": saves the deck, the score history, and the
 * miss counts to a text file, and loads them back the next time the app
 * starts. Each line is one record with tab-separated fields:
 *
 * <pre>
 * CARD  question  answer
 * SCORE percent
 * MISS  count  question  answer
 * </pre>
 *
 * Tabs, newlines, and backslashes inside a question or answer are written as
 * \t, \n, and \\ so every record stays on one line. Scores are written with
 * the most recent one last, so it is still the "previous" score after
 * loading. A line that isn't a valid record is skipped and counted instead
 * of stopping the load.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.13
 */
public class DeckStore {

    /** Save file used by the app when none is given. */
    public static final String DEFAULT_FILE = "cramcards-data.txt";

    /** Message used when a required argument is null. */
    public static final String NULL_MESSAGE =
        "File, deck, history, and tracker can't be null";

    /** Start of the message shown when saving fails. */
    public static final String SAVE_FAILED = "Couldn't save your cards: ";

    /** Start of the message shown when loading fails. */
    public static final String LOAD_FAILED = "Couldn't read the save file: ";

    private static final String CARD = "CARD";
    private static final String SCORE = "SCORE";
    private static final String MISS = "MISS";
    private static final String SEPARATOR = "\t";
    private static final int CARD_FIELDS = 3;
    private static final int SCORE_FIELDS = 2;
    private static final int MISS_FIELDS = 4;
    private static final int CARD_QUESTION = 1;
    private static final int CARD_ANSWER = 2;
    private static final int VALUE = 1;
    private static final int MISS_QUESTION = 2;
    private static final int MISS_ANSWER = 3;
    private static final int MAX_MISSES = 10000;
    private static final char BYTE_ORDER_MARK = '\uFEFF';
    private static final String TEMP_SUFFIX = ".tmp";

    private File file;

    /**
     * Creates a store that reads and writes one file.
     *
     * @param file
     *            the save file (it does not have to exist yet)
     * @throws IllegalArgumentException
     *             if file is null
     */
    public DeckStore(File file) {
        if (file == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
        this.file = file;
    }


    /**
     * Gets the save file.
     *
     * @return the file this store uses
     */
    public File getFile() {
        return file;
    }


    /**
     * Writes everything to the save file, replacing what was there. It
     * writes a temporary file first and then moves it into place, so a
     * crash mid-save never leaves a half-written file. Miss counts are saved
     * for the cards in the deck (once per distinct card), since only those
     * can become hard cards.
     *
     * @param deck
     *            the cards to save
     * @param history
     *            the scores to save
     * @param misses
     *            the miss counts to save
     * @throws IOException
     *             if the file can't be written
     * @throws IllegalArgumentException
     *             if any argument is null
     */
    public void save(Deck deck, ScoreHistory history, MissTracker misses)
        throws IOException {
        requireAll(deck, history, misses);
        File temp = new File(file.getPath() + TEMP_SUFFIX);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(temp), StandardCharsets.UTF_8));
        try {
            Flashcard[] cards = deck.getCards();
            for (int i = 0; i < cards.length; i++) {
                writeLine(writer, CARD + SEPARATOR + cardFields(cards[i]));
            }
            writeScores(writer, history);
            Deck written = new Deck();
            for (int i = 0; i < cards.length; i++) {
                int count = misses.missCount(cards[i]);
                if (count > 0 && !written.containsCard(cards[i])) {
                    written.addCard(cards[i]);
                    writeLine(writer, MISS + SEPARATOR + count + SEPARATOR
                        + cardFields(cards[i]));
                }
            }
        }
        finally {
            writer.close();
        }
        try {
            Files.move(temp.toPath(), file.toPath(),
                StandardCopyOption.ATOMIC_MOVE);
        }
        finally {
            temp.delete();
        }
    }


    /**
     * Reads the save file and adds what it holds to the given deck,
     * history, and tracker (normally all empty). A missing file is not an
     * error: nothing is loaded.
     *
     * @param deck
     *            receives the saved cards, in their saved order
     * @param history
     *            receives the saved scores
     * @param misses
     *            receives the saved miss counts
     * @return how many lines were skipped because they weren't valid
     * @throws IOException
     *             if the file exists but can't be read
     * @throws IllegalArgumentException
     *             if any argument is null
     */
    public int load(Deck deck, ScoreHistory history, MissTracker misses)
        throws IOException {
        requireAll(deck, history, misses);
        if (!file.exists()) {
            return 0;
        }
        int skipped = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(file), StandardCharsets.UTF_8));
        try {
            String line = reader.readLine();
            if (line != null && !line.isEmpty() && line.charAt(
                0) == BYTE_ORDER_MARK) {
                line = line.substring(1);
            }
            while (line != null) {
                if (!line.trim().isEmpty() && !readRecord(line, deck, history,
                    misses)) {
                    skipped++;
                }
                line = reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        return skipped;
    }


    /**
     * Describes what a load found, for the app to show at start-up.
     *
     * @param deck
     *            the loaded deck
     * @param history
     *            the loaded history
     * @param skipped
     *            the number of lines load() skipped
     * @return a one-line summary, or "" if nothing was loaded or skipped
     * @throws IllegalArgumentException
     *             if deck or history is null
     */
    public String loadSummary(Deck deck, ScoreHistory history, int skipped) {
        if (deck == null || history == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
        String summary = "";
        if (!deck.isEmpty() || history.hasScores()) {
            summary = "Loaded " + deck.size() + " card(s) and " + history
                .getCount() + " score(s) from " + file.getName() + ".";
        }
        if (skipped > 0) {
            summary = (summary + " Skipped " + skipped
                + " line(s) in the save file that weren't valid.").trim();
        }
        return summary;
    }


    /**
     * Writes the scores with the most recent one last.
     *
     * @param writer
     *            where to write
     * @param history
     *            the scores
     * @throws IOException
     *             if writing fails
     */
    private void writeScores(BufferedWriter writer, ScoreHistory history)
        throws IOException {
        Integer last = history.getPrevious();
        Integer[] scores = history.getScores();
        boolean lastSkipped = false;
        for (int i = 0; i < scores.length; i++) {
            if (!lastSkipped && scores[i].equals(last)) {
                lastSkipped = true;
            }
            else {
                writeLine(writer, SCORE + SEPARATOR + scores[i]);
            }
        }
        if (last != null) {
            writeLine(writer, SCORE + SEPARATOR + last);
        }
    }


    /**
     * Writes one line ending in a newline.
     *
     * @param writer
     *            where to write
     * @param line
     *            the line's text
     * @throws IOException
     *             if writing fails
     */
    private static void writeLine(BufferedWriter writer, String line)
        throws IOException {
        writer.write(line);
        writer.write('\n');
    }


    /**
     * Reads one record into the deck, history, or tracker.
     *
     * @param line
     *            the line from the file
     * @param deck
     *            receives a CARD
     * @param history
     *            receives a SCORE
     * @param misses
     *            receives a MISS
     * @return false if the line isn't a valid record
     */
    private boolean readRecord(
        String line,
        Deck deck,
        ScoreHistory history,
        MissTracker misses) {
        String[] fields = line.split(SEPARATOR, -1);
        try {
            if (fields[0].equals(CARD) && fields.length == CARD_FIELDS) {
                deck.addCard(new Flashcard(unescape(fields[CARD_QUESTION]),
                    unescape(fields[CARD_ANSWER])));
                return true;
            }
            if (fields[0].equals(SCORE) && fields.length == SCORE_FIELDS) {
                history.record(Integer.parseInt(fields[VALUE].trim()));
                return true;
            }
            if (fields[0].equals(MISS) && fields.length == MISS_FIELDS) {
                int count = Integer.parseInt(fields[VALUE].trim());
                Flashcard card = new Flashcard(unescape(fields[MISS_QUESTION]),
                    unescape(fields[MISS_ANSWER]));
                if (count <= 0 || count > MAX_MISSES) {
                    return false;
                }
                for (int i = 0; i < count; i++) {
                    misses.recordMiss(card);
                }
                return true;
            }
        }
        catch (IllegalArgumentException e) {
            // a bad number or a blank/too-long card: skip this line
            return false;
        }
        return false;
    }


    /**
     * Makes the escaped "question TAB answer" part of a record.
     *
     * @param card
     *            the card
     * @return the two escaped fields joined by a tab
     */
    private static String cardFields(Flashcard card) {
        return escape(card.getQuestion()) + SEPARATOR + escape(card
            .getAnswer());
    }


    /**
     * Escapes backslashes, tabs, newlines, and carriage returns.
     *
     * @param text
     *            the text to escape
     * @return text that fits on one line with no tabs
     */
    private static String escape(String text) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                out.append("\\\\");
            }
            else if (c == '\t') {
                out.append("\\t");
            }
            else if (c == '\n') {
                out.append("\\n");
            }
            else if (c == '\r') {
                out.append("\\r");
            }
            else {
                out.append(c);
            }
        }
        return out.toString();
    }


    /**
     * Reverses escape(). An unknown escape, or a backslash at the very end,
     * is kept as written.
     *
     * @param text
     *            the escaped text
     * @return the original text
     */
    private static String unescape(String text) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == 't') {
                    out.append('\t');
                }
                else if (next == 'n') {
                    out.append('\n');
                }
                else if (next == 'r') {
                    out.append('\r');
                }
                else if (next == '\\') {
                    out.append('\\');
                }
                else {
                    out.append(c).append(next);
                }
                i += 2;
            }
            else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }


    /**
     * Checks that the deck, history, and tracker were all given.
     *
     * @param deck
     *            the deck
     * @param history
     *            the history
     * @param misses
     *            the tracker
     * @throws IllegalArgumentException
     *             if any is null
     */
    private static void requireAll(
        Deck deck,
        ScoreHistory history,
        MissTracker misses) {
        if (deck == null || history == null || misses == null) {
            throw new IllegalArgumentException(NULL_MESSAGE);
        }
    }
}
