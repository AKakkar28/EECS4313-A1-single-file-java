package org.sfj;

import org.junit.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import java.io.UTFDataFormatException;

public class ByteBufferStreamsLLMTest {

    @Test
    public void testWriteUTF() throws IOException {
        ByteBufferStreams.Output output = new ByteBufferStreams.Output(ByteBuffer.allocate(50));
        output.writeUTF("Hello");
        ByteBuffer buffer = output.getBuffer();
        buffer.flip();
        ByteBufferStreams.Input input = new ByteBufferStreams.Input(buffer);
        assertThat(input.readUTF(), is("Hello"));
    }

    @Test
    public void testCheckRoom() {
        ByteBufferStreams.Output output = new ByteBufferStreams.Output(ByteBuffer.allocate(5));
        assertThrows(IOException.class, () -> output.write(new byte[10]));
    }

    @Test
    public void testWriteBytes() throws IOException {
        ByteBufferStreams.Output output = new ByteBufferStreams.Output(ByteBuffer.allocate(10));
        output.writeBytes("Hi");
        ByteBuffer buffer = output.getBuffer();
        buffer.flip();
        ByteBufferStreams.Input input = new ByteBufferStreams.Input(buffer);
        byte[] bytes = new byte[2];
        input.readFully(bytes);
        assertThat(new String(bytes), is("Hi"));
    }

    @Test
    public void testWriteByteBuffer() throws IOException {
        ByteBuffer bufferToWrite = ByteBuffer.wrap(new byte[]{1, 2, 3});
        ByteBufferStreams.Output output = new ByteBufferStreams.Output(ByteBuffer.allocate(10));
        output.write(bufferToWrite);
        ByteBuffer buffer = output.getBuffer();
        buffer.flip();
        ByteBufferStreams.Input input = new ByteBufferStreams.Input(buffer);
        byte[] bytes = new byte[3];
        input.readFully(bytes);
        assertThat(bytes, is(new byte[]{1, 2, 3}));
    }

    @Test
    public void testAvailable() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[5]);
        buffer.flip();

        ByteBufferStreams.Input input = new ByteBufferStreams.Input(buffer);
        assertEquals(5, input.available());

        input.setBuffer(null);
        assertEquals(0, input.available());
    }

    @Test
    public void testReadBoolean() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 1);
        buffer.flip();

        ByteBufferStreams.Input input = new ByteBufferStreams.Input(buffer);
        assertTrue(input.readBoolean());

        buffer.clear();
        buffer.put((byte) 0);
        buffer.flip();
        input.setBuffer(buffer);
        assertFalse(input.readBoolean());
    }


    @Test
    public void testWriteUTFAdd() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        ByteBufferStreams.Output output = new ByteBufferStreams.Output(buffer);

        // ASCII string
        output.writeUTF("Hello");
        buffer.flip();
        assertEquals(7, buffer.remaining());

        buffer.clear();

        // UTF with 2-byte chars
        output.writeUTF("λ");
        buffer.flip();
        assertTrue(buffer.remaining() > 0);

        buffer.clear();

        // UTF with 3-byte chars
        output.writeUTF("𐍈");
        buffer.flip();
        assertTrue(buffer.remaining() > 0);

        buffer.clear();

        // Exceed max length
        char[] longString = new char[70000];
        for (int i = 0; i < 70000; i++) {
            longString[i] = 'a';
        }
        try {
            output.writeUTF(new String(longString));
            fail("Expected UTFDataFormatException");
        } catch (UTFDataFormatException e) {
            assertTrue(e.getMessage().contains("string too long"));
        }
    }



}
