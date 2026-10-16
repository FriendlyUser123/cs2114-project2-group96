# Cram Cards — CS 2114 Project 2 (Group 96)

A flashcard app for cramming, in two versions: a **terminal app** and a **Swing window**.
You build a deck of question/answer cards and quiz yourself in random order. Any card
you miss comes back later in the same quiz, cards you miss 3 or more times can be
practiced on their own, and everything is saved between runs.

All storage goes through one generic class, `LinkedChain<T>`, with three element types:
`Flashcard`, `Integer`, and `String`.

**Stretch goals from our scope: all three are done.**

| Stretch goal | Where |
|---|---|
| GUI | `CramCardsGui`: Deck, Quiz, and Results tabs in a Swing window |
| Advanced card analysis | `MissTracker`: cards missed 3+ times become a "hardest cards" practice deck |
| Persistence | `DeckStore`: cards, scores, and miss counts saved to `cramcards-data.txt` |

## How to compile and run

**Eclipse:** File → Import → Existing Projects into Workspace, then pick this folder.
The course's `CS2-Support` project must also be in your workspace.

- To run the window version, right-click `src/cramcards/CramCardsGui.java` → **Run As → Java Application**.
- To run the terminal version, right-click `src/cramcards/CramCardsApp.java` → **Run As → Java Application**.
- To run the tests, right-click `src` → **Run As → JUnit Test**.

**Command line** (from this folder). Running the apps needs nothing extra:

```
javac -d bin -sourcepath src src/cramcards/CramCardsGui.java src/cramcards/CramCardsApp.java
java -cp bin cramcards.CramCardsGui
java -cp bin cramcards.CramCardsApp
```

The tests need `student.jar` from `CS2-Support`, which includes JUnit. Copy it into this folder first:

```
javac -d bin -cp student.jar src/cramcards/*.java
java -cp bin:student.jar org.junit.runner.JUnitCore cramcards.LinkedChainTest cramcards.FlashcardTest cramcards.DeckTest cramcards.QuizSessionTest cramcards.ScoreHistoryTest cramcards.MissTrackerTest cramcards.DeckStoreTest cramcards.CramCardsAppTest cramcards.CramCardsGuiTest
```

On Java 18 or newer, add `-Djava.security.manager=allow` after `java`, because `student.jar` installs a security manager.
Expected result: `OK (120 tests)`.

`CramCardsGuiTest.testMain` opens the window for a moment and closes it, so the tests need a screen.
Tests that save use temporary files, so they never write to your real `cramcards-data.txt` (a few tests that start the app normally do read it).

**Saving:** both versions save to `cramcards-data.txt` in the folder the app runs from (in Eclipse, the
project folder) after every change, and load it at start. Delete the file to start over.

## Using it

### Window (`CramCardsGui`)

- **Deck tab:** type a question, press Enter (or click) to move to the answer, then press **Add card**
  (or Enter). If the card is already in your deck, choose **Add anyway** or **Cancel**. Select a card in
  the list and press **Delete selected card** to remove that exact card, even if it has a duplicate.
  Deleting is turned off while a quiz is running.
- **Quiz tab:** **Start quiz** quizzes you on the whole deck. Type an answer and press **Submit** (or
  Enter); you'll see whether the last card was right. **End quiz** stops early without saving the score.
  **Practice hardest cards** turns on once a card has been missed 3 times; practice scores aren't saved.
- **Results tab:** last score, best, average, number of quizzes, and how many hard cards you have.
- The dark bar at the bottom shows every message, such as errors, the score, and what was loaded.

### Terminal (`CramCardsApp`)

At the menu, type a number:

1. **Add a card** — type a question and an answer.
2. **View cards** — lists every card in the order you added them.
3. **Delete a card** — type the card's question.
4. **Start quiz** — answer each card. Type `QUIT` to stop early; the score is then not saved.
5. **View results** — shows your last score, best score, average, and how many quizzes you've taken.
6. **Practice hardest cards** — quizzes you on cards missed 3+ times. Practice scores are not saved.
7. **Quit**

In both versions, answers ignore case and extra spaces, so "Cards", "cards", and " CARDS " all count as
right. Your score counts only the cards you answered right the first time each one came up.

## Design

| Class | Job | LinkedChain it owns |
|---|---|---|
| `LinkedChain<T>` | Storage only. Provided by staff; we added `removeAt`. | — |
| `Flashcard` | One card: question, answer, the answer-checking rule, `copy()` for retries, the blank and length rules | — |
| `Deck` | The user's cards: add, list, find, delete (by question or position), duplicate check | `LinkedChain<Flashcard>` |
| `QuizSession` | One quiz: random draw, check answers, re-add retry copies, score | 2 × `LinkedChain<Flashcard>` (pile, missed) |
| `ScoreHistory` | Every score: latest, best, average, count | `LinkedChain<Integer>` |
| `MissTracker` | Stretch goal: counts misses per card, builds the "hardest cards" deck | `LinkedChain<String>` |
| `DeckStore` | Stretch goal: saves and loads the deck, scores, and miss counts as a text file | — |
| `CramCardsApp` | Terminal view: menu loop, all terminal input/output and re-prompting | — |
| `CramCardsGui` | Stretch goal: window view with the same features, messages, and model classes | — |

The two views share every model class and message, so the rules (blank checks, the 500-character
limit, scoring, hard cards) are written once.

No app class touches a `Node`. Every app class uses the chain only through its public
methods: `add`, `addToEnd`, `remove(T)`, `removeAt`, `contains`, `count`, `size`,
`isEmpty`, and `toArray`. All application logic lives in the app classes.

### Big-O of the app methods compared with our D2 estimates

n = cards in the deck, m = total misses recorded, s = number of scores, h = number of hard cards.

| Method | Actual | D2 estimate | Matches? |
|---|---|---|---|
| `Deck.addCard` | O(n), `addToEnd` walks the chain | O(n) | yes |
| `Deck.containsCard` | O(n), `contains` | O(n) | yes |
| `Deck.findByQuestion` | O(n), one `toArray` and one scan | (new method) | — |
| `Deck.removeByQuestion` | O(n), find then `remove(T)` | O(n) | yes |
| `Deck.removeAt` | O(n), `removeAt` | (new method) | — |
| `Deck.getCards` | O(n), `toArray` | O(n) | yes |
| `Deck.copyCards` | O(n), `toArray` plus n O(1) `add` calls back to front | O(n) | yes |
| `QuizSession(deck, random)` | O(n), `copyCards` | O(n) | yes |
| `QuizSession.nextCard` | O(n), `removeAt(random index)` | O(n) | yes |
| `QuizSession.submitAnswer` | **O(1)**: `isCorrect` plus two O(1) `add` calls | O(n) | **no, it's faster.** We now re-add with `add()` instead of `addToEnd()`, and check a retry flag instead of `missed.contains`. |
| `QuizSession.getScorePercent` | O(1) | O(1) | yes |
| `ScoreHistory.record` / `getPrevious` / `getCount` | O(1) | O(1) | yes |
| `ScoreHistory.getBest` / `getAverage` / `getScores` | O(s), `toArray` and one pass | O(n) | yes |
| `MissTracker.recordMiss` | O(1), `add` | (new class) | — |
| `MissTracker.missCount` | O(m), `count` | (new class) | — |
| `MissTracker.buildHardDeck` | O(n·m + h²): a `count` per card, plus `addToEnd` for each hard card | (new class) | — |
| `DeckStore.save` | O(n·(n + m) + s): per card, a `missCount` and a duplicate check | (new class) | — |
| `DeckStore.load` | O(n² + s + m): each card goes in with `addToEnd` | (new class) | — |
| `CramCardsApp.addCardFlow` / `deleteCardFlow` | O(n) to change the deck, plus a save (`DeckStore.save`) | O(n) | yes, apart from the save added for persistence |
| `CramCardsApp.viewCards` | O(n), `getCards` | O(n) | yes |
| `CramCardsApp.quizFlow` | O(n) per card asked, plus one save | O(n) per card | yes |
| `CramCardsApp.showResults` | O(s) | O(n) | yes |
| `CramCardsApp.practiceHardestFlow` | O(n·m + h²), then a quiz | (new) | — |
| `CramCardsGui.addCard` / `deleteCard` | O(n·(n + m) + h² + s): change the deck, rebuild the list and results, save | (new class) | — |
| `CramCardsGui.startQuiz` | O(n·m + h²) to start (it rechecks whether Practice hardest cards can be on), then O(n) for the first card | (new class) | — |
| `CramCardsGui.submitAnswer` | O(n) per card, like the terminal quiz; the last answer also rebuilds the results and saves | (new class) | — |
| `CramCardsGui.practiceHardest` | O(n·m + h²), then a quiz | (new class) | — |

Terminal deck operations are O(n). The n·m + h² terms come from `MissTracker.buildHardDeck`, which
the window reruns to keep the Practice hardest cards button up to date; the n² terms come from saving
and loading, which walk the deck once per card. That's fine at flashcard-deck sizes.

### Bad input: where it's caught and what happens (all from our scope and spec)

| Bad input | Caught by | Response |
|---|---|---|
| Blank question or answer (including Unicode spaces, and text that is only spaces once cut to 500 characters) | `CramCardsApp.readNonBlank` / `CramCardsGui.addCard`, using `Flashcard.isBlank` (backed up by the `Flashcard` constructor) | Terminal re-prompts with "This can't be blank. Try again."; the window shows the same message and adds nothing. The constructor throws `IllegalArgumentException("Question and answer can't be blank")`. |
| Different case or extra spaces in an answer | `Flashcard.isCorrect` | Marked right; trims and ignores case |
| Empty deck for View cards or a quiz | `CramCardsApp.viewCards` / `quizFlow`, `CramCardsGui.startQuiz` (backed up by the `QuizSession` constructor) | "No cards yet. Add some cards first." The constructor throws `IllegalArgumentException("Cannot start a quiz with an empty deck")`. |
| Duplicate card | `CramCardsApp.addCardFlow` (`Deck.containsCard`, `readYesNo`); `CramCardsGui.addCard` | Terminal: "This card is already in your deck. Add it anyway? (y/n)"; `n` gives "Card not added."; anything else gives "Please type y or n." Window: the same question with **Add anyway** / **Cancel** buttons. |
| Invalid menu choice (9, 0, abc, +7, 07) | `CramCardsApp.parseMenuChoice`, called from `run` (must be exactly one digit 1–7) | `IllegalArgumentException("Enter a number 1 to 7.")`. `run` prints it and shows the menu again. (The window has no menu to mistype.) |
| Deleting a card that isn't there | `Deck.removeByQuestion` returns `false`; `CramCardsGui.deleteCard` checks the position | "Card not found." in the terminal; "Pick a card in the list to delete." in the window. The deck is unchanged. |
| Deleting from an empty deck | `CramCardsApp.deleteCardFlow` / `CramCardsGui.deleteCard` | "There are no cards to delete." |
| `null` added to a chain | `LinkedChain.add` / `addToEnd` | `IllegalArgumentException("Cannot add null to chain")` |
| Very long input (10,000 characters) | `Flashcard.fitToLength`, used by both views (backed up by the `Flashcard` constructor) | Cut to 500 characters (never splitting an emoji), with "That was over 500 characters, so it was cut to 500." |
| A quiz the user can't finish | `CramCardsApp.quizFlow` / `CramCardsGui.endQuiz` | Terminal: type `QUIT`. Window: **End quiz**. Both print "Quiz ended early. This score was not saved." |
| Input ends (Ctrl+D) at any terminal prompt | `CramCardsApp.readLine` returns `null` | The current step stops and the program prints "Goodbye!" with no crash. Mid-quiz it first prints "Quiz ended early. This score was not saved."; at the duplicate prompt it first prints "Card not added." |
| No save file yet (first run) | `DeckStore.load` | Starts with an empty deck; no message, no crash |
| Damaged save file (bad lines, scores over 100, blank cards, absurd miss counts over 10,000) | `DeckStore.load` skips each bad line and keeps the good ones; a byte order mark from editors like Notepad is ignored | "Loaded N card(s) and M score(s) from cramcards-data.txt. Skipped K line(s) in the save file that weren't valid." |
| Save file can't be read or written (e.g. it's a folder) | `DeckStore.load` / `save` throw `IOException`; both views catch it | "Couldn't read the save file: …" or "Couldn't save your cards: …"; the app keeps working without saving |

## Reflection

### What changed from the specification, and why

| What the spec said | What we built | Why | Where the change came from |
|---|---|---|---|
| Stretch goal: GUI | New `CramCardsGui` (Swing): Deck, Quiz, and Results tabs that reuse every model class and message | Makes studying easier than a numbered menu; each button calls a public method, so `CramCardsGuiTest` can test it without clicking | Own plan (our scope's stretch goal), implemented with GenAI |
| Stretch goal: persistence | New `DeckStore`: a tab-separated text file with `CARD`, `SCORE`, and `MISS` lines, saved after every change | Saving after every change means nothing is lost if the window is closed or the terminal is killed. Plain text can be read and fixed by hand. | Own plan (scope), implemented with GenAI |
| — | Miss counts are saved too, not just cards and scores | Without them, "Practice hardest cards" would reset every run. Tested in `DeckStoreTest.testSaveThenLoad`. | Own reflection |
| — | `ScoreHistory.getScores()`; `DeckStore` writes the latest score last | Saving needs every score, and the latest one must still be "previous" after loading. Tested in `DeckStoreTest.testSaveScoresAnyOrder`. | GenAI |
| `CramCardsApp(Scanner, Random)` | `CramCardsApp(Scanner, PrintStream, Random)` and `(Scanner, PrintStream, Random, DeckStore)` | Tests read the printed output from a byte stream (avoiding `student.TestCase`'s output capture, which we found unreliable in Project 1) and save to temporary files | Own reflection (Project 1 testing experience) |
| Delete only by question | `Deck.removeAt(index)`; the window deletes the selected card | Deleting by question always removes the *first* of two duplicates | Own reflection (our own D3 "What doesn't work yet") |
| `QUIT` ends a quiz | The window uses an **End quiz** button instead, so "quit" is an ordinary answer there (`CramCardsGuiTest.testSubmitAnswer`). The terminal warns when you add a card whose answer is "quit" (`CramCardsAppTest.testAddCardFlowQuitAnswer`). | In the terminal, a card whose answer is "quit" can't be answered | Own reflection |
| — | Text is cut to 500 characters *before* the blank check; the save file is written to a temporary file and then moved into place; miss counts over 10,000 are rejected; `MissTracker` keys start with the question's length; the window refuses deletes during a quiz | Each fixes a bug found by testing hostile input: 500 non-breaking spaces plus a letter crashed both views; a crash mid-save could empty the file; a huge miss count ran out of memory at start-up; two different cards could share a miss key; deleting the card being asked left it in the quiz | GenAI (independent GenAI review of this prototype) |
| 500-character cut lived in `CramCardsApp.readNonBlank` | `Flashcard.fitToLength`, used by both views | One rule, written once, for both views | GenAI |
| Missed copies go back with `pile.addToEnd(card.copy())` | `pile.add(card.copy())` | `nextCard` draws from a random position, so where the copy goes doesn't matter. `add` is O(1) and `addToEnd` is O(n). | GenAI (an independent GenAI review of our D2 spec) |
| First-try points decided by `missed.contains(card)` | `Flashcard.copy()` marks the copy as a retry (`isRetry()`), and only non-retry cards earn a point | With `contains`, a duplicate card whose twin was missed could never earn its own point. Tested in `QuizSessionTest.testDuplicateCardsScoreSeparately`. | GenAI review of D2 |
| `missed` gets each card once; `getMissedCount` = distinct misses | Every miss is added; `getMissedCount` = total misses | Counting every miss is what the hardest-cards stretch goal needs. | GenAI review of D2 |
| 500-character limit only in `CramCardsApp.MAX_INPUT_LENGTH` | `Flashcard.MAX_LENGTH`, enforced by the `Flashcard` constructor (`TOO_LONG_MESSAGE`); both views use the same constant | The spec listed this as a `Flashcard` invariant, but nothing in `Flashcard` enforced it | GenAI review of D2 |
| `Flashcard` overrides `equals` | It also overrides `hashCode` | Java requires equal objects to have equal hash codes | GenAI review of D2 |
| Menu 1–6 (6 = Quit) | Menu 1–7 (6 = Practice hardest cards, 7 = Quit); the error message became "Enter a number 1 to 7." | Room for the stretch goal | Own plan (our scope's stretch goal) |
| No `MissTracker` class | New `MissTracker` with a `LinkedChain<String>` of missed-card keys | Implements the "Advanced card analysis" stretch goal; `count()` gives misses per card | Own plan (scope), implemented with GenAI |
| `Deck` had no find method | New `Deck.findByQuestion`, used by `removeByQuestion` | The deletion search now lives in one testable method | GenAI |
| `copyCards` used `add` in array order (which reverses the cards) | `add` from the back of the array to the front | Keeps the original order at the same O(n) cost | GenAI review of D2 |
| No messages were constants | Every user-facing message is a `public static final String` | Both views, the tests, and this README all use the exact same text | GenAI review of D2 |
| Quiz answers: unclear whether blank re-prompts | A quiz answer is read as typed; a blank answer simply counts as wrong | Only questions and answers *when making a card* must be non-blank | Own reflection |
| Blank means empty after `trim()` | `Flashcard.isBlank` also treats Unicode spaces (like the non-breaking space) as blank | `trim()` keeps non-breaking spaces, so a card could look blank | GenAI (independent GenAI review of this prototype) |
| `parseMenuChoice` trims and parses any integer | It accepts exactly one digit 1–7 | `Integer.parseInt` also accepted `+7`, `07`, and non-ASCII digits | GenAI review of this prototype |
| End of input: "stops cleanly" | Every prompt returns `null` at end of input, and each flow stops; `run` prints "Goodbye!" | Makes "stops cleanly" concrete and testable (`CramCardsAppTest.testRunBadInput`) | GenAI |

Everything else matches the spec: the classes and their jobs, the menu flows, the
validation locations and messages, the scoring rule (first-try correct ÷ deck size,
rounded down), and the "previous" score read before the new score is recorded.

### What changed in the LinkedChain

We added one method, `removeAt(int index)`, as our spec planned. It removes and
returns the entry at a position, throws `IndexOutOfBoundsException` like `get` does,
and is O(n), or O(1) at index 0. `QuizSession.nextCard` uses it to draw a random card
in one walk of the chain instead of `get(i)` followed by `remove(T)`, and `Deck.removeAt`
uses it so the window can delete one exact card. We also added the
`package cramcards;` line, the Honor Pledge/LLM header, and a `removeAt` line in the class
Javadoc's Big-O list, and ran the file through the course formatter, which moves braces
but doesn't change code. Everything else is the staff version, and its tests are
in `LinkedChainTest`.

### What happened to our unknowns

- **Analyzing missed cards (figured out).** `MissTracker` stores a key for every miss in a
  `LinkedChain<String>`, and `count(key)` gives the number of misses per card. Cards with 3 or
  more misses form the hard deck, which menu option 6 and the window's **Practice hardest cards** quiz on.
- **Java Swing for a GUI (figured out).** The window is one class, `CramCardsGui`, that owns a `JPanel`
  with three tabs. The answer to "how do we test a GUI?" was to make every button call a public method
  (`addCard`, `deleteCard`, `startQuiz`, `submitAnswer`, `endQuiz`, …). The tests call those methods
  directly, and `testButtons` clicks the real buttons with `doClick()` to check they're connected.
  For "what makes it nice to use": one job per tab, Enter submits, the duplicate question appears as
  buttons instead of a y/n prompt, and every message shows in one status bar.

### What doesn't work yet

- **`QUIT` is reserved in the terminal.** A card whose real answer is "quit" can't be answered correctly
  in the terminal quiz; the terminal warns when you add one. The window doesn't have this problem.
- **Terminal deletes by question remove the first match.** With two cards that share a question, the
  terminal's Delete a card removes the first one. The window can delete either.
- **One save file per folder.** Both versions save to `cramcards-data.txt` in the folder they run from,
  so there's one deck per folder, and running both versions at the same time would overwrite each other's saves.
- **Hard cards never leave the hard deck.** `MissTracker` only adds misses, so a card stays hard (missed
  3+ times) even after you answer it right in practice. Only deleting the card (or the save file) removes it.
- **Only one deck.** All cards go into a single deck; there's no way to keep separate decks (for example,
  one per class) and pick which one to study.
- **Bad lines in a save file are dropped on the next save.** Loading skips them (and says how many), and
  the next save writes only the good data.
- **Untested branch: "no screen".** `CramCardsGui.createFrame` returns `null` (and `main` prints a message)
  when Java has no display. Tests run with a display, so those two statements are the only code the 120
  tests don't reach.
- **Everything else works:** every MVP feature, all three stretch goals, and every bad-input case from our scope.

## Next Steps

The MVP and all three stretch goals are done. Remaining work, in order:

| # | Item | Owner | New tests |
|---|---|---|---|
| 1 | Update the system diagram for D4 (add `DeckStore`, `CramCardsGui`, and the `String` chain) and put it in the repo | Aditya Banerjee | — |
| 2 | Change the terminal's quit word to `:quit` so no real answer can collide with it | Aditya Banerjee | `CramCardsAppTest.testQuizFlowAnswerIsQuit` (a card whose answer is "quit" can be answered); update `testQuizFlowBadInput` |
| 3 | Let the terminal's Delete a card take the number View cards shows, using `Deck.removeAt` | Aditya Banerjee | `CramCardsAppTest.testDeleteByNumber`, plus out-of-range and non-number bad cases |
| 4 | **Hard cards can "graduate":** each right answer in hardest-cards practice removes one miss key with `LinkedChain.remove(key)`, through a new `MissTracker.recordRight(Flashcard)`, so a card leaves the hard deck once its count drops below 3 | Aditya Banerjee | `MissTrackerTest.testRecordRight` (count drops by one), `testRecordRightNotThere` (a card with no misses stays at 0; nothing breaks), `testRecordRightNull`; `CramCardsGuiTest.testPracticeGraduates` and `CramCardsAppTest.testPracticeGraduates` (a hard card answered right enough times is no longer offered) |
| 5 | **Multiple decks:** a new `DeckLibrary` class holds named decks in a `LinkedChain<Deck>` (a fourth element type through the same chain) with `createDeck`, `renameDeck`, `deleteDeck`, `getDeck`, and `listNames`. The window gets a deck picker; the terminal gets a "Switch deck" menu choice; each quiz, score history, and hard deck belongs to the chosen deck. `DeckStore` adds a `DECK name` line before each deck's records, and old one-deck save files load into a deck called "My cards". | Aditya Banerjee | `DeckLibraryTest`: create, rename, delete, and switch decks; bad input: blank or duplicate names, renaming or deleting a deck that isn't there, deleting the last deck, an empty library. `DeckStoreTest.testSaveLoadSeveralDecks` and `testLoadOldOneDeckFile`. `CramCardsGuiTest.testSwitchDeck` and `CramCardsAppTest.testSwitchDeckFlow` (normal and bad menu input) |
| 6 | Final slides and a rehearsed demo: add a card, a quiz with a miss, results, a bad input, hardest cards, and closing and reopening to show saving | Aditya Banerjee | — |

**Stretch goals from our scope:**
- **GUI:** committed and done (`CramCardsGui`, tested by `CramCardsGuiTest`).
- **Advanced card analysis:** committed and done (`MissTracker`, menu option 6 and the window's **Practice hardest cards**).
- **Persistence:** committed and done (`DeckStore`, tested by `DeckStoreTest`, plus `testSaveAndLoad` in both view tests).

## Attribution

| File | Who | How |
|---|---|---|
| `LinkedChain.java` | CS 2114 staff; Aditya Banerjee | Provided by staff. `removeAt` written with GenAI (Claude); reviewed and tested by Aditya |
| `Flashcard.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 spec; reviewed by Aditya |
| `Deck.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 spec; reviewed by Aditya |
| `QuizSession.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 spec; reviewed by Aditya |
| `ScoreHistory.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 spec; reviewed by Aditya |
| `MissTracker.java` | Aditya Banerjee | Written with GenAI (Claude) for our scope's stretch goal; reviewed by Aditya |
| `DeckStore.java` | Aditya Banerjee | Written with GenAI (Claude) for our scope's persistence stretch goal; reviewed and run by Aditya |
| `CramCardsApp.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 spec; reviewed and run by Aditya |
| `CramCardsGui.java` | Aditya Banerjee | Written with GenAI (Claude) for our scope's GUI stretch goal; reviewed and run by Aditya |
| `LinkedChainTest.java` | Aditya Banerjee | Written with GenAI (Claude); reviewed and run by Aditya |
| `FlashcardTest.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 test plan; reviewed and run by Aditya |
| `DeckTest.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 test plan; reviewed and run by Aditya |
| `QuizSessionTest.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 test plan; reviewed and run by Aditya |
| `ScoreHistoryTest.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 test plan; reviewed and run by Aditya |
| `MissTrackerTest.java` | Aditya Banerjee | Written with GenAI (Claude); reviewed and run by Aditya |
| `DeckStoreTest.java` | Aditya Banerjee | Written with GenAI (Claude); reviewed and run by Aditya |
| `CramCardsAppTest.java` | Aditya Banerjee | Written with GenAI (Claude) from our D2 test plan; reviewed and run by Aditya |
| `CramCardsGuiTest.java` | Aditya Banerjee | Written with GenAI (Claude); reviewed and run by Aditya |
| `README.md` | Aditya Banerjee | Drafted with GenAI (Claude); reviewed by Aditya |

**How we used GenAI:** We used Claude (Anthropic) to write the code and tests from our
D2 specification, test plan, and scope's stretch goals. Before writing code, we had an independent
GenAI review check the D2 spec for contradictions. That review produced most of the changes in the
Reflection table: the retry flag, re-adding with `add()`, enforcing the 500-character
limit in `Flashcard`, `hashCode`, and the message constants.

We then checked the result by:
- running all 120 tests in Eclipse 2023-09;
- confirming that the tests execute every line and every branch of every class except the "no screen"
  branch above (measured with JaCoCo, the tool behind Eclipse's Coverage view);
- running both versions by hand through each feature and each bad-input case, including closing and
  reopening them to check saving.

Every file also states this in its LLM statement at the top.
