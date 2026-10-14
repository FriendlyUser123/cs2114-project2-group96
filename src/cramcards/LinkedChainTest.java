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
 * Tests for the provided LinkedChain, including the removeAt method our
 * group added. Uses LinkedChain of String.
 *
 * @author Aditya Banerjee (adityab7)
 * @version 2026.10.11
 */
public class LinkedChainTest extends student.TestCase {

    private LinkedChain<String> chain;

    /**
     * Creates an empty chain before each test.
     */
    public void setUp() {
        chain = new LinkedChain<String>();
    }


    /**
     * Tests the constructor: a new chain is empty.
     */
    public void testConstructor() {
        assertEquals(0, chain.size());
        assertTrue(chain.isEmpty());
        assertEquals("[]", chain.toString());
        assertNull(chain.remove());
    }


    /**
     * Tests add (front) and rejecting null.
     */
    public void testAdd() {
        assertTrue(chain.add("a"));
        assertTrue(chain.add("b"));
        assertEquals("[b, a]", chain.toString());
        assertEquals(2, chain.size());

        Exception thrown = null;
        try {
            chain.add(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(2, chain.size());
    }


    /**
     * Tests addToEnd (back), including on an empty chain, and rejecting null.
     */
    public void testAddToEnd() {
        assertTrue(chain.addToEnd("a"));
        assertEquals(1, chain.size());
        chain.addToEnd("b");
        assertEquals("[a, b]", chain.toString());

        Exception thrown = null;
        try {
            chain.addToEnd(null);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalArgumentException);
    }


    /**
     * Tests remove() on a full and an empty chain.
     */
    public void testRemoveFirst() {
        chain.add("a");
        chain.add("b");
        assertEquals("b", chain.remove());
        assertEquals("[a]", chain.toString());
        assertEquals("a", chain.remove());
        assertNull(chain.remove());
    }


    /**
     * Tests remove(T): first match only, not there, null, and empty.
     */
    public void testRemoveEntry() {
        assertFalse(chain.remove("a"));
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("a");
        assertTrue(chain.remove("a"));
        assertEquals("[b, a]", chain.toString());
        assertFalse(chain.remove("z"));
        assertFalse(chain.remove(null));
        assertTrue(chain.remove("a"));
        assertEquals("[b]", chain.toString());
    }


    /**
     * Tests removeAt at the front, middle, and end, and out-of-bounds and
     * empty-chain cases.
     */
    public void testRemoveAt() {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("c");
        chain.addToEnd("d");
        assertEquals("b", chain.removeAt(1));
        assertEquals("[a, c, d]", chain.toString());
        assertEquals("d", chain.removeAt(2));
        assertEquals("a", chain.removeAt(0));
        assertEquals("[c]", chain.toString());
        assertEquals(1, chain.size());

        Exception tooBig = null;
        try {
            chain.removeAt(1);
        }
        catch (Exception exception) {
            tooBig = exception;
        }
        assertTrue(tooBig instanceof IndexOutOfBoundsException);

        Exception negative = null;
        try {
            chain.removeAt(-1);
        }
        catch (Exception exception) {
            negative = exception;
        }
        assertTrue(negative instanceof IndexOutOfBoundsException);

        chain.removeAt(0);
        Exception empty = null;
        try {
            chain.removeAt(0);
        }
        catch (Exception exception) {
            empty = exception;
        }
        assertNotNull(empty);
        assertTrue(empty instanceof IndexOutOfBoundsException);
    }


    /**
     * Tests clear on a full and an empty chain.
     */
    public void testClear() {
        chain.add("a");
        chain.add("b");
        chain.clear();
        assertTrue(chain.isEmpty());
        chain.clear();
        assertEquals(0, chain.size());
    }


    /**
     * Tests get and its out-of-bounds message.
     */
    public void testGet() {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("c");
        assertEquals("c", chain.get(2));
        assertEquals(3, chain.size());

        Exception thrown = null;
        try {
            chain.get(3);
        }
        catch (Exception exception) {
            thrown = exception;
        }
        assertTrue(thrown instanceof IndexOutOfBoundsException);
        assertEquals("Index 3 out of bounds for size 3", thrown.getMessage());

        Exception negative = null;
        try {
            chain.get(-1);
        }
        catch (Exception exception) {
            negative = exception;
        }
        assertTrue(negative instanceof IndexOutOfBoundsException);
    }


    /**
     * Tests contains and count, including not there and null.
     */
    public void testContainsAndCount() {
        assertFalse(chain.contains("a"));
        chain.add("a");
        chain.add("b");
        chain.add("a");
        assertTrue(chain.contains("b"));
        assertEquals(2, chain.count("a"));
        assertFalse(chain.contains("z"));
        assertEquals(0, chain.count("z"));
        assertEquals(0, chain.count(null));
    }


    /**
     * Tests toArray and its bad inputs.
     */
    public void testToArray() {
        chain.addToEnd("a");
        chain.addToEnd("b");
        String[] values = chain.toArray(new String[2]);
        assertEquals("a", values[0]);
        assertEquals("b", values[1]);

        Exception nullArray = null;
        try {
            chain.toArray(null);
        }
        catch (Exception exception) {
            nullArray = exception;
        }
        assertTrue(nullArray instanceof IllegalArgumentException);

        Exception small = null;
        try {
            chain.toArray(new String[1]);
        }
        catch (Exception exception) {
            small = exception;
        }
        assertTrue(small instanceof IllegalArgumentException);
    }
}
