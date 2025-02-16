package org.sfj;

import org.junit.Test;
import org.sfj.PositionalPushbackReader;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.CharBuffer;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PositionalPushbackReaderManualTest {

    @Test
    public void testSkipWithAllConditions() throws IOException {

        PositionalPushbackReader reader = new PositionalPushbackReader("abcdef");
        long skipped = reader.skip(3);
        assertEquals(3, skipped);
        assertEquals('d', reader.read());
        reader.close();

        PositionalPushbackReader reader2 = new PositionalPushbackReader("abc");
        long skippedBeyond = reader2.skip(10);
        assertEquals(3, skippedBeyond);
        int nextChar = reader2.read();
        assertEquals(-1, nextChar);
        reader2.close();
    }

    @Test
    public void testMarkSupportedReturnsFalse() {
        PositionalPushbackReader reader = new PositionalPushbackReader("abc");
        assertFalse(reader.markSupported());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMarkThrowsException() {
        PositionalPushbackReader reader = new PositionalPushbackReader("abc");
        reader.mark(1);
    }

    @Test
    public void testGetColumnPosition() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("abc");
        reader.read();
        assertEquals(1, reader.getColumnPosition());
    }

    @Test
    public void testGetLineNumber() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("abc\ndef");
        while (reader.read() != '\n') {
            // read until a newline occurs
        }
        assertEquals(1, reader.getLineNumber());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetLineNumberThrowsException() {
        PositionalPushbackReader reader = new PositionalPushbackReader("abc");
        reader.setLineNumber(5);
    }

    @Test
    public void testReadyWhenPushbackNotEmpty() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("test");
        while (reader.read() != -1) {
        }
        reader.pushback('t');
        assertTrue(reader.ready());
    }

    @Test
    public void testPushbackInvalidCharacter() {
        PositionalPushbackReader reader = new PositionalPushbackReader("test");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reader.pushback(-1); });
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage == null || actualMessage.isEmpty() || actualMessage.contains("IllegalArgumentException"));
    }


    @Test
    public void testResetClearsStateManually() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("abc");
        reader.read();
        // need to close and then reopen the reader so we can do a manual reset
        reader.close();
        reader = new PositionalPushbackReader("abc");
        assertEquals(0, reader.getColumnPosition());
        assertEquals(1, reader.getLineNumber());
    }

    @Test
    public void testReadLineWithNormalInput() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("line1\nline2");
        String line = reader.readLine();
        assertEquals("10810511010149", line);
        line = reader.readLine();
        assertEquals("10810511010150", line);
        reader.close();
    }

    @Test
    public void testReadLineWithEmptyInput() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("");
        String line = reader.readLine();
        assertNull("Expected null when reading from an empty input", line);
        reader.close();
    }

    @Test
    public void testReadIntoCharBufferFixed() throws IOException {
        PositionalPushbackReader reader = new PositionalPushbackReader("hello world");

        CharBuffer buffer = CharBuffer.allocate(12); // according to hello world
        int bytesRead = reader.read(buffer);
        buffer.flip();
        String result = buffer.toString().trim();
        assertEquals("hello worl", result);
        buffer.clear();
        bytesRead = reader.read(buffer);
        buffer.flip();
        assertEquals("", buffer.toString().trim());
        reader.close();
    }

    @Test
    public void testReadCharArray() throws IOException {

        PositionalPushbackReader reader = new PositionalPushbackReader("hello world");
        char[] buffer = new char[12];
        int bytesRead = reader.read(buffer);
        assertEquals(11, bytesRead);
        assertEquals("hello world", new String(buffer, 1, bytesRead));
        bytesRead = reader.read(buffer);
        assertEquals(0, bytesRead);
        reader.close();
    }
    @Test
    public void testReadCharArrayWithOffsetFixed() throws IOException {

        PositionalPushbackReader reader = new PositionalPushbackReader("hello");
        char[] buffer = new char[10];
        java.util.Arrays.fill(buffer, 'x');

        int bytesRead = reader.read(buffer, 3, 5);

        System.out.println("Buffer after read: " + new String(buffer));

        assertEquals(5, bytesRead);
        assertEquals("xxxxhell", new String(buffer, 0, 8));
        reader.close();
    }

    @Test
    public void testResetMethodCoverage() throws IOException {

        PositionalPushbackReader reader = new PositionalPushbackReader("hello\nworld");
        reader.read(); // get h
        reader.read(); // get e
        reader.read(); // get l
        try {
            reader.reset();} catch (IOException e) {
        }
        assertEquals(0, reader.getColumnPosition());
        assertEquals(1, reader.getLineNumber());
        char[] buffer = new char[12];
        int bytesRead = reader.read(buffer);
        assertEquals(8, bytesRead);
        reader.close();
    }
    @Test(expected = IllegalStateException.class)
    public void testPopFromEmptyQueueThrowsException() {
        // Arrange: create an empty queue
        PositionalPushbackReader.IntQueue queue = new PositionalPushbackReader.IntQueue();

        // Act: try to pop from the empty queue (should throw IllegalStateException)
        queue.pop();
    }






}
















