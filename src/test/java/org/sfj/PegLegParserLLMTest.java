package org.sfj;

import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.ExpectedException;
import org.sfj.PegLegParser;
import org.sfj.PegLegParser.RuleReturn;

import static org.junit.Assert.*;

public class PegLegParserLLMTest {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test for not meeting the minimum number of repetitions.
     */
    @Test
    public void testTimesOf_MinNotMet() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("aa");
        RuleReturn<Object> result = parser.timesOf(3, "a").rule(); // Expecting to match 'a' at least 3 times
        assertFalse(result.matched());
    }

    /**
     * Test for exactly meeting the required repetitions.
     */
    @Test
    public void testTimesOf_ExactMatch() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("aaa");
        RuleReturn<Object> result = parser.timesOf(3, "a").rule(); // Exactly 3 'a's
        assertTrue(result.matched());
    }

    /**
     * Test varying flags for the ruleReturn method.
     */


    /**
     * Test character range boundary conditions.
     */
    @Test
    public void testCharRange_BoundaryConditions() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("a");
        RuleReturn<Object> result = parser.charRange('a', 'a').rule(); // Only one character in range
        assertTrue(result.matched());

        parser.using("b");
        result = parser.charRange('a', 'a').rule(); // Character outside of range
        assertFalse(result.matched());
    }

    /**
     * Test complex rule combinations.
     */
    @Test
    public void testComplexRuleCombination() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("abc");
        RuleReturn<Object> result = parser.seqOf(parser.testOf(parser.str("a")), parser.anyChar(), parser.anyChar()).rule();
        assertTrue(result.matched());
    }


    @Test
    public void testLambdaTimesOf() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("abcabcabc");
        RuleReturn<Object> result = parser.timesOf(3, parser.seqOf("abc")).rule(); // Expected to match 'abc' 3 times
        assertTrue(result.matched());

        parser.using("abcabc");
        result = parser.timesOf(3, parser.seqOf("abc")).rule(); // Fails to match 'abc' 3 times
        assertFalse(result.matched());
    }


    @Test
    public void testNothing() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("any input");
        PegLegParser.RuleReturn<Object> result = parser.nothing().rule();
        assertFalse(result.matched());
    }

    /**
     * Test that empty() always succeeds.
     */
    @Test
    public void testEmpty() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("any input");
        PegLegParser.RuleReturn<Object> result = parser.empty().rule();
        assertTrue(result.matched());
    }

    @Test
    public void testSeqOf_Success() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("abcd");
        PegLegParser.RuleReturn<Object> result = parser.seqOf('a', 'b', 'c', 'd').rule();
        assertTrue(result.matched());
    }

    /**
     * Test seqOf where a part of the sequence does not match.
     */
    @Test
    public void testSeqOf_PartialMatchFail() {
        PegLegParser<Object> parser = new PegLegParser<>();
        parser.using("abcx");
        PegLegParser.RuleReturn<Object> result = parser.seqOf('a', 'b', 'c', 'd').rule();
        assertFalse(result.matched());
    }




}
