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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Stretch goal "GUI": the same app as CramCardsApp in a Swing window, with a
 * Deck tab (add, list, and delete cards), a Quiz tab (regular quiz and
 * hardest-cards practice), and a Results tab. It uses the same Deck,
 * QuizSession, ScoreHistory, MissTracker, and DeckStore classes and the same
 * messages as the terminal app. Each button calls a public method, so the
 * tests can drive the window without clicking. Because there's an "End
 * quiz" button, QUIT is a normal answer here.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.13
 */
public final class CramCardsGui {

    /** Title of the window. */
    public static final String WINDOW_TITLE = "Cram Cards";
    /** Shown in place of the window when there is no screen. */
    public static final String NO_DISPLAY =
        "No screen is available. Run CramCardsApp for the terminal version.";
    /** Shown when an equal card is already in the deck. */
    public static final String DUPLICATE_QUESTION =
        "This card is already in your deck. Add it anyway?";
    /** Shown when Add anyway or Cancel is used with no card waiting. */
    public static final String NOTHING_WAITING =
        "There's no card waiting to be added.";
    /** Shown when Delete is used with no card picked. */
    public static final String PICK_A_CARD =
        "Pick a card in the list to delete.";
    /** Shown when an answer or End quiz comes with no quiz running. */
    public static final String START_A_QUIZ = "Start a quiz first.";
    /** Shown when a quiz is started while one is running. */
    public static final String QUIZ_RUNNING =
        "Finish or end the current quiz first.";
    /** Shown in the Quiz tab before a quiz starts. */
    public static final String QUIZ_IDLE =
        "Press Start quiz to begin, or practice your hardest cards.";
    /** Starts the feedback about the card just answered. */
    public static final String LAST_CARD = "Last card: ";
    /** Shown in the status bar when nothing else has happened yet. */
    public static final String WELCOME = "Add a card to get started.";
    /** Message used when the panel is built with a null random source. */
    public static final String NULL_RANDOM = "Random can't be null";
    /** Message used when createFrame gets a null panel. */
    public static final String NULL_PANEL = "The app can't be null";

    /** Name of the question field (for tests). */
    public static final String QUESTION_FIELD = "questionField";
    /** Name of the answer field (for tests). */
    public static final String ANSWER_FIELD = "answerField";
    /** Name of the Add card button (for tests). */
    public static final String ADD_BUTTON = "addButton";
    /** Name of the Add anyway button (for tests). */
    public static final String ADD_ANYWAY_BUTTON = "addAnywayButton";
    /** Name of the Cancel button (for tests). */
    public static final String CANCEL_BUTTON = "cancelButton";
    /** Name of the card list (for tests). */
    public static final String CARD_LIST = "cardList";
    /** Name of the Delete button (for tests). */
    public static final String DELETE_BUTTON = "deleteButton";
    /** Name of the Start quiz button (for tests). */
    public static final String START_BUTTON = "startButton";
    /** Name of the Practice hardest cards button (for tests). */
    public static final String PRACTICE_BUTTON = "practiceButton";
    /** Name of the quiz answer field (for tests). */
    public static final String RESPONSE_FIELD = "responseField";
    /** Name of the Submit button (for tests). */
    public static final String SUBMIT_BUTTON = "submitButton";
    /** Name of the End quiz button (for tests). */
    public static final String END_BUTTON = "endButton";

    private static final Color MAROON = new Color(134, 31, 65);
    private static final Color ORANGE = new Color(229, 117, 31);
    private static final Color PAPER = new Color(247, 245, 240);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color INK = new Color(38, 38, 38);
    private static final Color MUTED = new Color(110, 110, 110);
    private static final Color RIGHT_GREEN = new Color(30, 120, 60);
    private static final String FONT = "SansSerif";
    private static final int TITLE_SIZE = 24;
    private static final int CARD_TEXT_SIZE = 22;
    private static final int BODY_SIZE = 15;
    private static final int GAP = 12;
    private static final int SMALL_GAP = 6;
    private static final int FIELD_COLUMNS = 28;
    private static final int WIDTH = 680;
    private static final int HEIGHT = 620;
    private static final int CARD_WIDTH = 420;
    private static final int STATUS_WIDTH = 540;

    private JPanel root;
    private Random random;
    private Deck deck;
    private ScoreHistory history;
    private MissTracker misses;
    private DeckStore store;

    private QuizSession session;
    private Flashcard current;
    private boolean saveScore;
    private Flashcard waitingCard;
    private String questionText;
    private String status;
    private String resultsText;

    private JTextField questionField;
    private JTextField answerField;
    private JButton addButton;
    private JButton addAnywayButton;
    private JButton cancelButton;
    private DefaultListModel<Flashcard> cardModel;
    private JList<Flashcard> cardList;
    private JButton deleteButton;
    private JButton startButton;
    private JButton practiceButton;
    private JTextField responseField;
    private JButton submitButton;
    private JButton endButton;
    private JLabel questionLabel;
    private JLabel feedbackLabel;
    private JLabel progressLabel;
    private JLabel resultsLabel;
    private JLabel statusLabel;

    /**
     * Creates the window's contents with a new random source, saving to
     * DeckStore.DEFAULT_FILE.
     */
    public CramCardsGui() {
        this(new Random(), new DeckStore(new File(DeckStore.DEFAULT_FILE)));
    }


    /**
     * Creates the window's contents and loads any saved cards and scores.
     *
     * @param random
     *            used by each quiz to draw cards
     * @param store
     *            where cards and scores are saved; null to save nothing
     * @throws IllegalArgumentException
     *             if random is null
     */
    public CramCardsGui(Random random, DeckStore store) {
        if (random == null) {
            throw new IllegalArgumentException(NULL_RANDOM);
        }
        this.random = random;
        this.store = store;
        this.deck = new Deck();
        this.history = new ScoreHistory();
        this.misses = new MissTracker();
        this.questionText = "";
        this.status = WELCOME;
        buildComponents();
        layOut();
        loadSaved();
        refreshCards();
        refreshResults();
        showIdleQuiz();
        setQuizRunning(false);
        showDuplicateChoice(false);
    }


    /**
     * Opens the Cram Cards window.
     *
     * @param args
     *            not used
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * Builds and shows the window on the Swing thread.
             */
            @Override
            public void run() {
                JFrame frame = createFrame(new CramCardsGui());
                if (frame == null) {
                    System.err.println(NO_DISPLAY);
                }
                else {
                    frame.setVisible(true);
                }
            }
        });
    }


    /**
     * Puts the app in a new window, sized and centered but not shown yet.
     *
     * @param gui
     *            the app to show
     * @return the window, or null if there is no screen to show it on
     * @throws IllegalArgumentException
     *             if gui is null
     */
    public static JFrame createFrame(CramCardsGui gui) {
        if (gui == null) {
            throw new IllegalArgumentException(NULL_PANEL);
        }
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(gui.getPanel());
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        return frame;
    }


    /**
     * Adds a card (the Add card button). Blank text is refused with
     * BLANK_INPUT; text over Flashcard.MAX_LENGTH is cut and the user is
     * told. An equal card already in the deck waits for Add anyway or
     * Cancel.
     *
     * @param question
     *            the question typed
     * @param answer
     *            the answer typed
     * @return true if the card was added now
     */
    public boolean addCard(String question, String answer) {
        waitingCard = null;
        showDuplicateChoice(false);
        if (Flashcard.isBlank(question) || Flashcard.isBlank(answer)) {
            setStatus(CramCardsApp.BLANK_INPUT);
            return false;
        }
        String fitQuestion = Flashcard.fitToLength(question);
        String fitAnswer = Flashcard.fitToLength(answer);
        if (Flashcard.isBlank(fitQuestion) || Flashcard.isBlank(fitAnswer)) {
            setStatus(CramCardsApp.BLANK_INPUT);
            return false;
        }
        boolean cut = fitQuestion.length() < question.trim().length()
            || fitAnswer.length() < answer.trim().length();
        Flashcard card = new Flashcard(fitQuestion, fitAnswer);
        if (deck.containsCard(card)) {
            waitingCard = card;
            showDuplicateChoice(true);
            setStatus(DUPLICATE_QUESTION);
            return false;
        }
        addToDeck(card, cut);
        return true;
    }


    /**
     * Answers the duplicate question (the Add anyway and Cancel buttons).
     *
     * @param addAnyway
     *            true to add the waiting card, false to drop it
     * @return true if a card was waiting
     */
    public boolean confirmDuplicate(boolean addAnyway) {
        if (waitingCard == null) {
            setStatus(NOTHING_WAITING);
            return false;
        }
        Flashcard card = waitingCard;
        waitingCard = null;
        showDuplicateChoice(false);
        if (addAnyway) {
            addToDeck(card, false);
        }
        else {
            setStatus(CramCardsApp.CARD_NOT_ADDED);
        }
        return true;
    }


    /**
     * Deletes the card at a position in the list (the Delete button). Not
     * allowed during a quiz, so a quiz never asks a deleted card.
     *
     * @param index
     *            the card's position in the list; -1 when none is picked
     * @return true if a card was deleted
     */
    public boolean deleteCard(int index) {
        waitingCard = null;
        showDuplicateChoice(false);
        if (session != null) {
            setStatus(QUIZ_RUNNING);
            return false;
        }
        if (deck.isEmpty()) {
            setStatus(CramCardsApp.NO_CARDS_TO_DELETE);
            return false;
        }
        if (index < 0 || index >= deck.size()) {
            setStatus(PICK_A_CARD);
            return false;
        }
        deck.removeAt(index);
        refreshCards();
        refreshResults();
        setStatus(CramCardsApp.CARD_DELETED);
        save();
        return true;
    }


    /**
     * Starts a quiz on the whole deck (the Start quiz button). Its score is
     * saved when it finishes.
     *
     * @return true if a quiz started
     */
    public boolean startQuiz() {
        if (session != null) {
            setStatus(QUIZ_RUNNING);
            return false;
        }
        if (deck.isEmpty()) {
            setStatus(CramCardsApp.NO_CARDS);
            return false;
        }
        beginQuiz(deck, true);
        setStatus("Quiz started: " + deck.size() + " card(s).");
        return true;
    }


    /**
     * Starts a practice quiz on the cards missed at least
     * MissTracker.HARD_THRESHOLD times (the Practice hardest cards button).
     * Practice scores are not saved.
     *
     * @return true if a practice quiz started
     */
    public boolean practiceHardest() {
        if (session != null) {
            setStatus(QUIZ_RUNNING);
            return false;
        }
        Deck hard = misses.buildHardDeck(deck);
        if (hard.isEmpty()) {
            setStatus(CramCardsApp.NO_HARD_CARDS);
            return false;
        }
        beginQuiz(hard, false);
        setStatus("Practicing " + hard.size()
            + " hard card(s). Practice scores are not saved.");
        return true;
    }


    /**
     * Checks an answer to the current card (the Submit button or Enter). A
     * miss is recorded and the card comes back later in the quiz. After the
     * last card the quiz finishes and shows the score.
     *
     * @param response
     *            what the user typed (null or blank counts as wrong)
     * @return true if the answer was right
     */
    public boolean submitAnswer(String response) {
        if (session == null) {
            setStatus(START_A_QUIZ);
            return false;
        }
        boolean right = session.submitAnswer(current, response);
        if (right) {
            showFeedback(LAST_CARD + CramCardsApp.CORRECT, RIGHT_GREEN);
        }
        else {
            misses.recordMiss(current);
            showFeedback(LAST_CARD + CramCardsApp.WRONG_PREFIX + current
                .getAnswer(), MAROON);
        }
        responseField.setText("");
        if (session.hasNextCard()) {
            showNextCard();
        }
        else {
            finishQuiz();
        }
        return right;
    }


    /**
     * Ends the running quiz without saving its score (the End quiz button).
     * Misses so far are still counted.
     *
     * @return true if a quiz was running
     */
    public boolean endQuiz() {
        if (session == null) {
            setStatus(START_A_QUIZ);
            return false;
        }
        session = null;
        current = null;
        setQuizRunning(false);
        showIdleQuiz();
        showFeedback(CramCardsApp.QUIZ_ENDED_EARLY, MUTED);
        setStatus(CramCardsApp.QUIZ_ENDED_EARLY);
        refreshResults();
        save();
        return true;
    }


    /**
     * Tells whether a quiz is running.
     *
     * @return true between starting a quiz and finishing or ending it
     */
    public boolean isQuizRunning() {
        return session != null;
    }


    /**
     * Gets the text shown on the quiz card: the current question, the final
     * score, or the idle message.
     *
     * @return the quiz card's text
     */
    public String getQuestionText() {
        return questionText;
    }


    /**
     * Gets the status bar's text.
     *
     * @return the latest status message
     */
    public String getStatus() {
        return status;
    }


    /**
     * Gets the Results tab's text.
     *
     * @return the results summary
     */
    public String getResultsText() {
        return resultsText;
    }


    /**
     * Gets the panel holding the whole app, to put in a window.
     *
     * @return the app's main panel
     */
    public JPanel getPanel() {
        return root;
    }


    /**
     * Gets the deck.
     *
     * @return this window's deck
     */
    public Deck getDeck() {
        return deck;
    }


    /**
     * Gets the score history.
     *
     * @return this window's score history
     */
    public ScoreHistory getHistory() {
        return history;
    }


    /**
     * Gets the miss tracker.
     *
     * @return this window's miss tracker
     */
    public MissTracker getMissTracker() {
        return misses;
    }


    /**
     * Creates every component and connects each button to its method.
     */
    private void buildComponents() {
        questionField = textField(QUESTION_FIELD);
        answerField = textField(ANSWER_FIELD);
        addButton = button("Add card", ADD_BUTTON, true);
        addAnywayButton = button("Add anyway", ADD_ANYWAY_BUTTON, false);
        cancelButton = button("Cancel", CANCEL_BUTTON, false);
        cardModel = new DefaultListModel<Flashcard>();
        cardList = new JList<Flashcard>(cardModel);
        cardList.setName(CARD_LIST);
        cardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cardList.setFont(new Font(FONT, Font.PLAIN, BODY_SIZE));
        deleteButton = button("Delete selected card", DELETE_BUTTON, false);
        startButton = button("Start quiz", START_BUTTON, true);
        practiceButton = button("Practice hardest cards", PRACTICE_BUTTON,
            false);
        responseField = textField(RESPONSE_FIELD);
        submitButton = button("Submit", SUBMIT_BUTTON, true);
        endButton = button("End quiz", END_BUTTON, false);
        questionLabel = label(CARD_TEXT_SIZE, Font.BOLD, INK);
        feedbackLabel = label(BODY_SIZE, Font.PLAIN, MUTED);
        progressLabel = label(BODY_SIZE, Font.PLAIN, MUTED);
        resultsLabel = label(BODY_SIZE + 2, Font.PLAIN, INK);
        statusLabel = label(BODY_SIZE - 1, Font.PLAIN, CARD_WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        addButton.addActionListener(e -> addFromFields());
        questionField.addActionListener(e -> answerField
            .requestFocusInWindow());
        answerField.addActionListener(e -> addFromFields());
        addAnywayButton.addActionListener(e -> confirmDuplicate(true));
        cancelButton.addActionListener(e -> confirmDuplicate(false));
        deleteButton.addActionListener(e -> deleteCard(cardList
            .getSelectedIndex()));
        startButton.addActionListener(e -> startQuiz());
        practiceButton.addActionListener(e -> practiceHardest());
        submitButton.addActionListener(e -> submitAnswer(responseField
            .getText()));
        responseField.addActionListener(e -> submitAnswer(responseField
            .getText()));
        endButton.addActionListener(e -> endQuiz());
    }


    /**
     * Arranges the header, the three tabs, and the status bar.
     */
    private void layOut() {
        root = new JPanel(new BorderLayout());
        root.setBackground(PAPER);
        JLabel title = new JLabel(WINDOW_TITLE);
        title.setFont(new Font(FONT, Font.BOLD, TITLE_SIZE));
        title.setForeground(CARD_WHITE);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setBorder(BorderFactory.createEmptyBorder(GAP, GAP * 2, GAP,
            GAP));
        header.add(title, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(FONT, Font.BOLD, BODY_SIZE));
        tabs.addTab("Deck", buildDeckTab());
        tabs.addTab("Quiz", buildQuizTab());
        tabs.addTab("Results", buildResultsTab());

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(INK);
        statusBar.setBorder(BorderFactory.createEmptyBorder(SMALL_GAP, GAP * 2,
            SMALL_GAP, GAP));
        statusBar.add(statusLabel, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
    }


    /**
     * Builds the Deck tab: the add form above the card list.
     *
     * @return the tab
     */
    private JPanel buildDeckTab() {
        JPanel form = new JPanel(new GridLayout(0, 1, SMALL_GAP, SMALL_GAP));
        form.setOpaque(false);
        form.add(fieldLabel("Question"));
        form.add(questionField);
        form.add(fieldLabel("Answer"));
        form.add(answerField);
        form.add(row(addButton, addAnywayButton, cancelButton));

        JScrollPane scroll = new JScrollPane(cardList);
        scroll.setBorder(BorderFactory.createTitledBorder("Your cards"));

        JPanel tab = page();
        tab.add(form, BorderLayout.NORTH);
        tab.add(scroll, BorderLayout.CENTER);
        tab.add(row(deleteButton), BorderLayout.SOUTH);
        return tab;
    }


    /**
     * Builds the Quiz tab: start buttons, the card, and the answer row.
     *
     * @return the tab
     */
    private JPanel buildQuizTab() {
        JPanel card = new JPanel(new BorderLayout(SMALL_GAP, SMALL_GAP));
        card.setBackground(CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory
            .createMatteBorder(0, SMALL_GAP, 0, 0, ORANGE), BorderFactory
                .createEmptyBorder(GAP * 2, GAP * 2, GAP * 2, GAP * 2)));
        card.add(progressLabel, BorderLayout.NORTH);
        card.add(questionLabel, BorderLayout.CENTER);
        card.add(feedbackLabel, BorderLayout.SOUTH);

        JPanel answerRow = new JPanel(new BorderLayout(SMALL_GAP, 0));
        answerRow.setOpaque(false);
        answerRow.add(responseField, BorderLayout.CENTER);
        answerRow.add(row(submitButton, endButton), BorderLayout.EAST);

        JPanel tab = page();
        tab.add(row(startButton, practiceButton), BorderLayout.NORTH);
        tab.add(card, BorderLayout.CENTER);
        tab.add(answerRow, BorderLayout.SOUTH);
        return tab;
    }


    /**
     * Builds the Results tab.
     *
     * @return the tab
     */
    private JPanel buildResultsTab() {
        JPanel tab = page();
        tab.add(resultsLabel, BorderLayout.CENTER);
        return tab;
    }


    /**
     * Adds the card typed in the Deck tab, clearing the fields if it was
     * added.
     */
    private void addFromFields() {
        addCard(questionField.getText(), answerField.getText());
    }


    /**
     * Adds a card to the deck and the list, then saves.
     *
     * @param card
     *            the card to add
     * @param cut
     *            true if the typed text was cut to fit
     */
    private void addToDeck(Flashcard card, boolean cut) {
        deck.addCard(card);
        refreshCards();
        refreshResults();
        questionField.setText("");
        answerField.setText("");
        if (cut) {
            setStatus(CramCardsApp.CARD_ADDED + " "
                + CramCardsApp.TOO_LONG_INPUT);
        }
        else {
            setStatus(CramCardsApp.CARD_ADDED);
        }
        save();
    }


    /**
     * Starts a quiz session and shows its first card.
     *
     * @param quizDeck
     *            the cards to quiz on (not empty)
     * @param save
     *            true to record the score when the quiz finishes
     */
    private void beginQuiz(Deck quizDeck, boolean save) {
        waitingCard = null;
        showDuplicateChoice(false);
        session = new QuizSession(quizDeck, random);
        saveScore = save;
        setQuizRunning(true);
        showFeedback(" ", MUTED);
        showNextCard();
        responseField.requestFocusInWindow();
    }


    /**
     * Draws the next card and shows its question.
     */
    private void showNextCard() {
        current = session.nextCard();
        String label = "Card";
        if (current.isRetry()) {
            label = "Retry";
        }
        progressLabel.setText(label + "  |  Missed so far: " + session
            .getMissedCount());
        showQuestion(current.getQuestion());
    }


    /**
     * Shows the score of a quiz whose pile is empty, saves it if it was a
     * regular quiz, and resets the Quiz tab.
     */
    private void finishQuiz() {
        int score = session.getScorePercent();
        String summary = "Score: " + score + "% (" + session
            .getCorrectFirstTry() + " of " + session.getDeckSize()
            + " right on the first try)";
        if (saveScore) {
            Integer previous = history.getPrevious();
            history.record(score);
            String previousText = "none";
            if (previous != null) {
                previousText = previous + "%";
            }
            setStatus(summary + ". Previous: " + previousText + " | Best: "
                + history.getBest() + "%");
        }
        else {
            setStatus(summary + ". Practice scores are not saved.");
        }
        session = null;
        current = null;
        setQuizRunning(false);
        progressLabel.setText("Quiz finished");
        showQuestion(summary);
        refreshResults();
        save();
    }


    /**
     * Rebuilds the card list from the deck.
     */
    private void refreshCards() {
        cardModel.clear();
        Flashcard[] cards = deck.getCards();
        for (int i = 0; i < cards.length; i++) {
            cardModel.addElement(cards[i]);
        }
        deleteButton.setEnabled(session == null && !deck.isEmpty());
    }


    /**
     * Rebuilds the Results tab and the hardest-cards button.
     */
    private void refreshResults() {
        int hardCount = misses.buildHardDeck(deck).size();
        String hardLine = "Hard cards right now: " + hardCount + " (missed "
            + MissTracker.HARD_THRESHOLD + "+ times)";
        if (history.hasScores()) {
            resultsText = "Last score: " + history.getPrevious() + "% | Best: "
                + history.getBest() + "% | Average: " + String.format("%.1f",
                    history.getAverage()) + "% | Quizzes taken: " + history
                        .getCount() + "\n" + hardLine;
        }
        else {
            resultsText = CramCardsApp.NO_QUIZZES + "\n" + hardLine;
        }
        resultsLabel.setText(html(resultsText, CARD_WIDTH, "center"));
        practiceButton.setEnabled(session == null && hardCount > 0);
    }


    /**
     * Turns the Quiz tab's controls on or off.
     *
     * @param running
     *            true while a quiz is running
     */
    private void setQuizRunning(boolean running) {
        startButton.setEnabled(!running);
        responseField.setEnabled(running);
        submitButton.setEnabled(running);
        endButton.setEnabled(running);
        deleteButton.setEnabled(!running && !deck.isEmpty());
        practiceButton.setEnabled(!running && !misses.buildHardDeck(deck)
            .isEmpty());
    }


    /**
     * Shows or hides the Add anyway and Cancel buttons.
     *
     * @param show
     *            true while a duplicate card is waiting
     */
    private void showDuplicateChoice(boolean show) {
        addAnywayButton.setVisible(show);
        cancelButton.setVisible(show);
        addAnywayButton.setEnabled(show);
        cancelButton.setEnabled(show);
    }


    /**
     * Shows the idle message on the quiz card.
     */
    private void showIdleQuiz() {
        progressLabel.setText(" ");
        showQuestion(QUIZ_IDLE);
    }


    /**
     * Shows text on the quiz card.
     *
     * @param text
     *            the question or message
     */
    private void showQuestion(String text) {
        questionText = text;
        questionLabel.setText(html(text, CARD_WIDTH, "center"));
    }


    /**
     * Shows feedback under the quiz card.
     *
     * @param text
     *            the feedback
     * @param color
     *            its color
     */
    private void showFeedback(String text, Color color) {
        feedbackLabel.setText(text);
        feedbackLabel.setForeground(color);
    }


    /**
     * Sets the status bar's text.
     *
     * @param text
     *            the message
     */
    private void setStatus(String text) {
        status = text;
        statusLabel.setText(html(text, STATUS_WIDTH, "left"));
        statusLabel.setToolTipText(text);
    }


    /**
     * Loads the save file, if there is a store, and reports what was loaded.
     */
    private void loadSaved() {
        if (store == null) {
            setStatus(WELCOME);
            return;
        }
        try {
            int skipped = store.load(deck, history, misses);
            String summary = store.loadSummary(deck, history, skipped);
            if (summary.isEmpty()) {
                summary = WELCOME;
            }
            setStatus(summary);
        }
        catch (IOException e) {
            setStatus(DeckStore.LOAD_FAILED + e.getMessage());
        }
    }


    /**
     * Saves everything, if there is a store. A failure is added to the
     * status bar.
     */
    private void save() {
        if (store == null) {
            return;
        }
        try {
            store.save(deck, history, misses);
        }
        catch (IOException e) {
            setStatus(status + " " + DeckStore.SAVE_FAILED + e.getMessage());
        }
    }


    /**
     * Makes a background panel for a tab.
     *
     * @return the panel
     */
    private static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(GAP, GAP));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(GAP * 2, GAP * 2, GAP
            * 2, GAP * 2));
        return panel;
    }


    /**
     * Lines up components left to right.
     *
     * @param parts
     *            the components
     * @return a see-through row holding them
     */
    private static JPanel row(JComponent... parts) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, SMALL_GAP,
            0));
        panel.setOpaque(false);
        for (int i = 0; i < parts.length; i++) {
            panel.add(parts[i]);
        }
        return panel;
    }


    /**
     * Makes a named text field.
     *
     * @param name
     *            the component name
     * @return the field
     */
    private static JTextField textField(String name) {
        JTextField field = new JTextField(FIELD_COLUMNS);
        field.setName(name);
        field.setFont(new Font(FONT, Font.PLAIN, BODY_SIZE));
        return field;
    }


    /**
     * Makes a named button.
     *
     * @param text
     *            the button's text
     * @param name
     *            the component name
     * @param primary
     *            true for the filled maroon style
     * @return the button
     */
    private static JButton button(String text, String name, boolean primary) {
        JButton button = new JButton(text);
        button.setName(name);
        button.setFont(new Font(FONT, Font.BOLD, BODY_SIZE - 1));
        button.setFocusPainted(false);
        if (primary) {
            button.setBackground(MAROON);
            button.setForeground(CARD_WHITE);
            button.setOpaque(true);
        }
        return button;
    }


    /**
     * Makes a centered label.
     *
     * @param size
     *            font size
     * @param style
     *            Font.PLAIN or Font.BOLD
     * @param color
     *            text color
     * @return the label
     */
    private static JLabel label(int size, int style, Color color) {
        JLabel label = new JLabel(" ", SwingConstants.CENTER);
        label.setFont(new Font(FONT, style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }


    /**
     * Makes a small heading for a text field.
     *
     * @param text
     *            the heading
     * @return the label
     */
    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT, Font.BOLD, BODY_SIZE - 1));
        label.setForeground(MUTED);
        return label;
    }


    /**
     * Wraps plain text as HTML of a fixed width so long text wraps onto more
     * lines, escaping characters HTML would treat as markup.
     *
     * @param text
     *            the plain text; newlines become line breaks
     * @param width
     *            the width to wrap at, in pixels
     * @param align
     *            "left" or "center"
     * @return the HTML
     */
    private static String html(String text, int width, String align) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(
            ">", "&gt;").replace("\n", "<br>");
        return "<html><div style='width:" + width + "px;text-align:" + align
            + "'>" + safe + "</div></html>";
    }
}
