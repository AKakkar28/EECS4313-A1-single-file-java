/*
 * Copyright 2020 C. Schanck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sfj;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class DrawDotLLMTest {

    /**
     * ✅ Test for escapeForDotHTML(String)
     * Ensures special characters are correctly escaped.
     */
    @Test
    public void testEscapeForDotHTML() {
        assertThat(DrawDot.escapeForDotHTML("Hello & World"), is("Hello &amp; World"));
        assertThat(DrawDot.escapeForDotHTML("A < B"), is("A &lt; B"));
        assertThat(DrawDot.escapeForDotHTML("C > D"), is("C &gt; D"));
        assertThat(DrawDot.escapeForDotHTML("\"quoted\""), is("&quot;quoted&quot;"));
        assertThat(DrawDot.escapeForDotHTML("NoChange"), is("NoChange"));
    }

    /**
     * ✅ Test for emit(PrintWriter)
     * Ensures that the DOT representation is correctly emitted.
     */
    @Test
    public void testEmitPrintWriter() throws IOException {
        DrawDot dot = new DrawDot("testGraph");
        dot.root().add(new DrawDot.Node("A"));
        dot.root().add(new DrawDot.Node("B"));
        dot.root().add(new DrawDot.Connection(new DrawDot.Node("A"), new DrawDot.Node("B")));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        dot.emit(pw);

        String output = sw.toString();
        assertThat(output, containsString("digraph testGraph"));
        assertThat(output, containsString("\"A\""));
        assertThat(output, containsString("\"B\""));
        assertThat(output, containsString("\"A\" -> \"B\""));
    }

    /**
     * ✅ Test for lambda$static$1(Object)
     * Ensures that the lambda correctly processes values.
     */
    @Test
    public void testLambdaStatic1() {
        // Based on DrawDot's transformation functions
        assertThat(DrawDot.XFORM_QUOTED.apply("test"), is("\"test\""));
        assertThat(DrawDot.XFORM_HTML.apply("html"), is("<html>"));
        assertThat(DrawDot.XFORM_ENUM_LOWERCASE.apply(DrawDot.ColorsSVG.red), is("red"));
    }

    /**
     * ✅ Test for static {...} (Class initialization block)
     * Ensures that DrawDot is initialized correctly.
     */
    @Test
    public void testStaticInitialization() {
        DrawDot dot = new DrawDot("initTest");
        assertThat(dot.root().id(), is("initTest"));
    }
}
