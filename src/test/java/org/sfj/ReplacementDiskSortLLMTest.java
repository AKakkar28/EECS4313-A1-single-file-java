package org.sfj;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ReplacementDiskSortLLMTest {



    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    static class IntElement extends ReplacementDiskSort.Element {
        public IntElement(int i) {
            super(i);
        }

        @Override
        public Integer getData() {
            return (Integer) super.getData();
        }
    }

    /**
     * ✅ Fixed: Properly implements `append()` method.
     */
    private static ReplacementDiskSort.ExternalAppender<IntElement> makeAppender(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f, true);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos, 32 * 1024));

        return new ReplacementDiskSort.ExternalAppender<IntElement>() { // ✅ Fixed: Specify type explicitly
            @Override
            public void append(IntElement elem) throws IOException {
                dos.writeInt(elem.getData());
            }

            @Override
            public void close() {
                try {
                    dos.flush();
                    dos.close();
                } catch (IOException ignored) {}
            }
        };
    }


    /**
     * ✅ Fixed: Explicitly implements `ExternalIterator<IntElement>`.
     */
    private static ReplacementDiskSort.ExternalIterator<IntElement> makeIter(File file) throws FileNotFoundException {
        return new ReplacementDiskSort.ExternalIterator<IntElement>() {
            private final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file), 32 * 1024));

            @Override
            public IntElement next() {
                try {
                    if (dis.available() > 0) {
                        return new IntElement(dis.readInt());
                    }
                } catch (IOException e) {
                    try {
                        dis.close();
                    } catch (IOException ignored) {
                    }
                }
                return null;
            }
        };
    }

    /**
     * ✅ Test sorting an empty file
     * Ensures sorting an empty file does not fail and produces an empty result.
     */
    @Test
    public void testSortEmptyFile() throws IOException {
        File folder = tmp.newFolder();
        File src = new File(folder, "empty_source");
        src.createNewFile(); // Create an empty file
        File dest = new File(folder, "sorted_output");

        ReplacementDiskSort<IntElement> sorter = new ReplacementDiskSort<>(
                ReplacementDiskSortLLMTest::makeIter,
                ReplacementDiskSortLLMTest::makeAppender,
                Comparator.comparing(IntElement::getData),
                true
        );

        sorter.run(src, 100, 10, dest, folder);
        assertThat(dest.length(), is(0L)); // Ensure output is empty
    }

    /**
     * ✅ Test sorting with an invalid input file (non-existent file)
     */
    @Test(expected = IOException.class)
    public void testSortWithInvalidFile() throws IOException {
        File folder = tmp.newFolder();
        File src = new File(folder, "non_existent");
        File dest = new File(folder, "sorted_output");

        ReplacementDiskSort.runOnce(
                ReplacementDiskSortLLMTest::makeIter,
                ReplacementDiskSortLLMTest::makeAppender,
                Comparator.comparing(IntElement::getData),
                true, src, 10, 5, dest, folder
        );
    }

    /**
     * ✅ Test sorting a large file to check performance
     */
    @Test
    public void testSortLargeFile() throws IOException {
        File folder = tmp.newFolder();
        File src = new File(folder, "large_source");
        Random rand = new Random(0);
        genIntFile(src, rand, 100000, false);

        File dest = new File(folder, "sorted_output");
        ReplacementDiskSort.runOnce(
                ReplacementDiskSortLLMTest::makeIter,
                ReplacementDiskSortLLMTest::makeAppender,
                Comparator.comparing(IntElement::getData),
                true, src, 1000, 100, dest, folder
        );

        verifyOrder(dest, ReplacementDiskSortLLMTest::makeIter, Comparator.comparing(IntElement::getData));
    }

    /**
     * ✅ Ensures order correctness of sorted output.
     */
    private static <E extends ReplacementDiskSort.Element> void verifyOrder(File f,
                                                                            ReplacementDiskSort.IterMaker<E> iterMaker,
                                                                            Comparator<E> comp) {
        try {
            ReplacementDiskSort.ExternalIterator<E> iter = iterMaker.make(f);
            E last = null;
            while (true) {
                E p = iter.next();
                if (p == null) break;
                if (last != null) {
                    assertThat(comp.compare(p, last), greaterThanOrEqualTo(0));
                }
                last = p;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static File genIntFile(File dest, Random rand, int many, boolean allowDuplicates) throws IOException {
        FileChannel output = FileChannel.open(dest.toPath(), APPEND, CREATE_NEW);
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
        for (int i = 0; i < many; i++) {
            b.clear();
            b.putInt(0, allowDuplicates ? rand.nextInt(50) : rand.nextInt(2 * many));
            b.flip();
            output.write(b);
        }
        output.close();
        return dest;
    }

}
