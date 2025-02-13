package org.sfj;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DrawDotLLMTest {

    @Test
    public void testEscapeForDotHTML() {
        assertThat(DrawDot.escapeForDotHTML("Hello & World"), is("Hello &amp; World"));
        assertThat(DrawDot.escapeForDotHTML("A < B"), is("A &lt; B"));
        assertThat(DrawDot.escapeForDotHTML("C > D"), is("C &gt; D"));
        assertThat(DrawDot.escapeForDotHTML("\"quoted\""), is("&quot;quoted&quot;"));
        assertThat(DrawDot.escapeForDotHTML("NoChange"), is("NoChange"));
    }

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

    @Test
    public void testLambdaStatic1() {
        assertThat(DrawDot.XFORM_QUOTED.apply("test"), is("\"test\""));
        assertThat(DrawDot.XFORM_HTML.apply("html"), is("<html>"));
        assertThat(DrawDot.XFORM_ENUM_LOWERCASE.apply(DrawDot.ColorsSVG.red), is("red"));
    }

    @Test
    public void testStaticInitialization() {
        DrawDot dot = new DrawDot("initTest");
        assertThat(dot.root().id(), is("initTest"));
    }

    @Test
    public void testGraphAttributes() {
        DrawDot.Graph graph = new DrawDot.Graph("myGraph");
        graph.clustered(true).directed(false).rankdir(DrawDot.RankDir.LR);
        assertThat(graph.toString(), containsString("myGraph"));
        assertThat(graph.getChildren().isEmpty(), is(true));
        assertThat(graph.getConnections().isEmpty(), is(true));
    }

    @Test
    public void testNodeAttributes() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        node.color("blue").label("TestNode").shaped(DrawDot.Shapes.CIRCLE);
        assertThat(node.getAttributes().get("color").value, is("blue"));
        assertThat(node.getAttributes().get("label").value, is("TestNode"));
        assertThat(node.getAttributes().get("shape").value.toString(), is("CIRCLE"));
    }

    @Test
    public void testConnectionAttributes() {
        DrawDot.Node from = new DrawDot.Node("start");
        DrawDot.Node to = new DrawDot.Node("end");
        DrawDot.Connection conn = new DrawDot.Connection(from, to);
        conn.color("red").label("connect").penWidth(2);
        assertThat(conn.getAttributes().get("color").value, is("red"));
        assertThat(conn.getAttributes().get("label").value, is("connect"));
        assertThat(conn.getAttributes().get("penwidth").value, is(2));
    }

    @Test
    public void testEdgeAndNodeStyles() {
        DrawDot.Node node = new DrawDot.Node("styledNode");
        node.style(DrawDot.NodeStyle.bold, DrawDot.ColorsSVG.green);
        assertThat(node.getAttributes().get("style").value, is("bold"));
        assertThat(node.getAttributes().get("fillcolor").value.toString(), containsString("green"));
    }

    @Test
    public void testEscapeForDotHTMLSpecialCases() {
        assertThat(DrawDot.escapeForDotHTML("&<>\""), is("&amp;&lt;&gt;&quot;"));
    }

    @Test
    public void testGraphEmit() throws IOException {
        DrawDot dot = new DrawDot("emitTest");
        dot.root().add(new DrawDot.Node("n1").label("N1"));
        dot.root().add(new DrawDot.Node("n2").label("N2"));
        dot.root().add(new DrawDot.Connection(new DrawDot.Node("n1"), new DrawDot.Node("n2")).color("black"));

        StringWriter sw = new StringWriter();
        dot.emit(sw);
        assertThat(sw.toString(), containsString("emitTest"));
    }

    @Test
    public void testColorsX11Enum() {
        for (DrawDot.ColorsX11 color : DrawDot.ColorsX11.values()) {
            assertThat(color.colorName(), is(color.name().toLowerCase()));
        }
    }

    @Test
    public void testGraphEmitMethod() throws IOException {
        DrawDot dot = new DrawDot("graphTest");
        DrawDot.Graph graph = dot.root();
        graph.commentBefore("Before comment")
                .commentHeader("Header comment")
                .commentFooter("Footer comment")
                .commentAfter("After comment");

        graph.add(new DrawDot.Node("node1").label("Node1"));
        graph.add(new DrawDot.Node("node2").label("Node2"));
        graph.add(new DrawDot.Connection(new DrawDot.Node("node1"), new DrawDot.Node("node2")));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        graph.emit(dot, pw, "");

        String output = sw.toString();
        assertThat(output, containsString("Before comment"));
        assertThat(output, containsString("Header comment"));
        assertThat(output, containsString("Footer comment"));
        assertThat(output, containsString("After comment"));
        assertThat(output, containsString("node1"));
        assertThat(output, containsString("node2"));
    }

    @Test
    public void testGraphEqualsMethod() {
        DrawDot.Graph graph1 = new DrawDot.Graph("graphName");
        DrawDot.Graph graph2 = new DrawDot.Graph("graphName");
        DrawDot.Graph graph3 = new DrawDot.Graph("differentName");

        assertThat(graph1.equals(graph1), is(true));
        assertThat(graph1.equals(graph2), is(true));
        assertThat(graph1.equals(graph3), is(false));
        assertThat(graph1.equals(null), is(false));
        assertThat(graph1.equals("notAGraph"), is(false));
    }

    @Test
    public void testGraphHeaderMethod() {
        DrawDot.Graph directedGraph = new DrawDot.Graph("myGraph").directed(true);
        DrawDot.Graph undirectedGraph = new DrawDot.Graph("myGraph").directed(false);

        assertThat(directedGraph.header(), is("digraph myGraph {"));
        assertThat(undirectedGraph.header(), is("graph myGraph {"));
    }

    @Test
    public void testGraphHashCodeMethod() {
        DrawDot.Graph graph1 = new DrawDot.Graph("graphName");
        DrawDot.Graph graph2 = new DrawDot.Graph("graphName");
        DrawDot.Graph graph3 = new DrawDot.Graph("differentName");

        assertThat(graph1.hashCode(), is(graph2.hashCode()));
        assertThat(graph1.hashCode(), is(not(graph3.hashCode())));
    }



}
