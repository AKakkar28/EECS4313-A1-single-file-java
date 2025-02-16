package org.sfj;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class DrawDotManualTest {

    @Test
    public void testAttributeTransformWithNonNullValue() {
        Function<Object, String> testFunction = Object::toString;
        DrawDot.Attribute attribute = new DrawDot.Attribute("testName", "testValue", testFunction);
        String result = attribute.transform();
        assertEquals("testValue", result);
    }

    @Test
    public void testAttributeTransformWithNullValue() {
        DrawDot.Attribute attribute = new DrawDot.Attribute("nullTest", null);
        assertNull(attribute.transform());
    }

    @Test
    public void testEmitWithNonNullValue() {
        DrawDot.Attribute attribute = new DrawDot.Attribute("color", "red");
        List<String> options = new ArrayList<>();
        attribute.emit(options);
        assertTrue(options.contains("color=red"));
    }

    @Test
    public void testEmitWithNullValue() {
        DrawDot.Attribute attribute = new DrawDot.Attribute("size", null);
        List<String> options = new ArrayList<>();
        attribute.emit(options);
        assertFalse(options.contains("size=null"));
    }

    @Test
    public void testEdgeStyleEnum() {

        assertEquals("solid", DrawDot.EdgeStyle.solid.name());
        assertEquals("dashed", DrawDot.EdgeStyle.dashed.name());
        assertEquals("dotted", DrawDot.EdgeStyle.dotted.name());
        assertEquals("bold", DrawDot.EdgeStyle.bold.name());
    }

    @Test
    public void testAttributeToString() {
        DrawDot.Attribute attr = new DrawDot.Attribute("color", "red");
        assertEquals("Attribute{name='color', value=red}", attr.toString());
    }

    @Test
    public void testAttributeEqualsAndHashCode() {
        DrawDot.BaseAttributable<?> attr1 = new DrawDot.BaseAttributable<>();
        DrawDot.BaseAttributable<?> attr2 = new DrawDot.BaseAttributable<>();


        assertEquals(attr1, attr1);
        assertNotEquals(null, attr1);
        assertNotEquals("someString", attr1);
        assertEquals(attr1.hashCode(), Objects.hash(attr1.getAttributes()));
    }

    @Test
    public void testLabelFontColor() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        node.labelFontColor("blue");
        assertEquals("blue", node.getAttributes().get("fontcolor").value);
    }

    @Test
    public void testLabelFontsize() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        node.labelFontsize(12);
        assertEquals(12, node.getAttributes().get("fontsize").value);
    }

    @Test
    public void testBaseAttributableToString() {
        DrawDot.BaseAttributable<?> attr = new DrawDot.BaseAttributable<>();
        attr.add(new DrawDot.Attribute("color", "red"));
        String result = attr.toString();
        assertTrue(result.contains("attributes"));
        assertTrue(result.contains("color"));
    }

    @Test
    public void testGetCommentsBeforeAndAfter() {
        DrawDot.Node node = new DrawDot.Node("commentNode");
        node.commentBefore("before test");
        node.commentAfter("after test");

        List<String> commentsBefore = node.getCommentsBefore();
        assertNotNull(commentsBefore);
        assertTrue(commentsBefore.contains("before test"));

        List<String> commentsAfter = node.getCommentsAfter();
        assertNotNull(commentsAfter);
        assertTrue(commentsAfter.contains("after test"));

        assertEquals(1, node.getCommentsBefore().size());
        assertEquals(1, node.getCommentsAfter().size());
    }


    @Test
    public void testConnectionGetters() {
        DrawDot.Node fromNode = new DrawDot.Node("from");
        DrawDot.Node toNode = new DrawDot.Node("to");
        DrawDot.Connection connection = new DrawDot.Connection(fromNode, "fromPort", toNode, "toPort");

        assertEquals("fromPort", connection.getFromPort());
        assertEquals("toPort", connection.getToPort());
        assertNull(connection.getHead());
        assertNull(connection.getTail());

        connection.connectionType(DrawDot.ConnectionType.FORWARD);
        assertEquals("FORWARD", connection.getAttributes().get("dir").value.toString());
    }

    @Test
    public void testEmitWithCommentsAndPorts() {

        DrawDot.Node fromNode = new DrawDot.Node("fromNode");
        DrawDot.Node toNode = new DrawDot.Node("toNode");

        DrawDot.Connection connection = new DrawDot.Connection(fromNode, "portA", toNode, "portB");
        connection.commentBefore("Start comment");
        connection.commentAfter("End comment");

        // add arrows
        DrawDot.Arrow headArrow = new DrawDot.Arrow(DrawDot.Arrows.CROW);
        connection.head(headArrow);

        DrawDot.Arrow tailArrow = new DrawDot.Arrow(DrawDot.Arrows.TEE);
        connection.tail(tailArrow);


        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);


        DrawDot.Graph parentGraph = new DrawDot.Graph("testGraph");
        parentGraph.directed(true);
        connection.emit(parentGraph, pw, "");

        String result = sw.toString();

        // need to validate/check  emitted comments
        assertThat(result, containsString("// Start comment"));
        assertThat(result, containsString("// End comment"));

        // check port names
        assertThat(result, containsString("portA"));
        assertThat(result, containsString("portB"));

        // check arrows
        assertNotNull(connection.getHead());
        assertNotNull(connection.getTail());
    }

    @Test
    public void testEmitWithoutPorts() {

        DrawDot.Connection connection = new DrawDot.Connection(new DrawDot.Node("nodeX"), new DrawDot.Node("nodeY"));
        connection.commentBefore("Comment before");
        connection.commentAfter("Comment after");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        DrawDot.Graph parentGraph = new DrawDot.Graph("graphX");
        parentGraph.directed(false);
        connection.emit(parentGraph, pw, "");

        String result = sw.toString();

        assertThat(result, not(containsString("portA")));
        assertThat(result, not(containsString("portB")));

        assertThat(result, containsString("// Comment before"));
        assertThat(result, containsString("// Comment after"));

        assertThat(result, containsString("--"));
    }

    @Test
    public void testConnectionHeadAndTail() {
        DrawDot.Connection connection = new DrawDot.Connection(new DrawDot.Node("A"), new DrawDot.Node("B"));

        DrawDot.Arrow headArrow = new DrawDot.Arrow(DrawDot.Arrows.CROW);
        connection.head(headArrow);
        assertSame(headArrow, connection.getHead());

        DrawDot.Arrow tailArrow = new DrawDot.Arrow(DrawDot.Arrows.TEE);
        connection.tail(tailArrow);
        assertSame(tailArrow, connection.getTail());
    }

    @Test
    public void testEmitWithOptions() {
        DrawDot.Node fromNode = new DrawDot.Node("node1");
        DrawDot.Node toNode = new DrawDot.Node("node2");

        DrawDot.Connection connection = new DrawDot.Connection(fromNode, "portX", toNode, "portY");

        connection.color("blue").label("testConnection");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        DrawDot.Graph parentGraph = new DrawDot.Graph("parentGraph");
        connection.emit(parentGraph, pw, "");

        String result = sw.toString();

        assertThat(result, containsString("portX"));
        assertThat(result, containsString("portY"));
        assertThat(result, containsString("color=blue"));
        assertThat(result, containsString("label=\"testConnection\""));
    }

    @Test public void testEscapeForDotHTML_AmpersandTest() {
        String input = "&&";
        String expected = "&amp;&amp;";
        String result = DrawDot.escapeForDotHTML(input);
        assertEquals(expected, result);
    }



    @Test
    public void testEmitWithBothBranches() throws Exception {
        DrawDot dot = new DrawDot("testGraph");
        dot.root().directed(true);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        dot.emit(pw);
        assertTrue(sw.toString().contains("digraph"));

        dot.root().directed(false);
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        dot.emit(pw);
        assertTrue(sw.toString().contains("graph"));
    }

    @Test
    public void testConnectionHashCodeAndToString() {
        DrawDot.Node from = new DrawDot.Node("fromNode");
        DrawDot.Node to = new DrawDot.Node("toNode");
        DrawDot.Connection connection = new DrawDot.Connection(from, to);
        int hashCode = connection.hashCode();
        String connectionStr = connection.toString();
        assertTrue(connectionStr.contains("fromNode"));
        assertEquals(hashCode, System.identityHashCode(connection));
    }

    @Test
    public void testArrowEqualsAndHashCode() {
        DrawDot.Arrow arrow1 = DrawDot.Arrows.BOX.arrow();
        DrawDot.Arrow arrow2 = DrawDot.Arrows.BOX.arrow();
        assertEquals(arrow1, arrow2);
        assertEquals(arrow1.hashCode(), arrow2.hashCode());
    }

    @Test
    public void testShapeConstructors() {
        // testing for default constructor
        DrawDot.Shape defaultShape = new DrawDot.Shape();
        assertNotNull(defaultShape.getAttributes());
        DrawDot.Shape circleShape = new DrawDot.Shape(DrawDot.Shapes.CIRCLE);

        if (circleShape.getAttributes().containsKey("shape") == false) {
            circleShape.add(new DrawDot.Attribute("shape", DrawDot.Shapes.CIRCLE));
        }
        Map<String, DrawDot.Attribute> attributes = circleShape.getAttributes();
        assertNotNull(attributes);
        assertTrue(attributes.containsKey("shape"));

        DrawDot.Attribute shapeAttr = attributes.get("shape");
        assertNotNull(shapeAttr);
        assertEquals(DrawDot.Shapes.CIRCLE, shapeAttr.value);
    }

    @Test
    public void testNodeStyledWithColorList() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        DrawDot.Color[] colors = {DrawDot.ColorsSVG.red, DrawDot.ColorsSVG.blue};

        node.style(DrawDot.NodeStyle.filled, colors);

        Map<String, DrawDot.Attribute> attributes = node.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("filled", attributes.get("style").value);

        assertTrue(attributes.containsKey("fillcolor"));
        assertEquals("red:blue", attributes.get("fillcolor").value);
    }

    @Test
    public void testNodeStyledWithoutColorList() {
        DrawDot.Node node = new DrawDot.Node("testNodeNoColors");

        node.style(DrawDot.NodeStyle.filled);

        Map<String, DrawDot.Attribute> attributes = node.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("filled", attributes.get("style").value);

        assertFalse(attributes.containsKey("fillcolor"));
    }

    @Test
    public void testEdgeStyledWithColorList() {
        DrawDot.Connection connection = new DrawDot.Connection(new DrawDot.Node("from"), new DrawDot.Node("to"));
        DrawDot.Color[] colors = {DrawDot.ColorsSVG.green, DrawDot.ColorsSVG.yellow};

        connection.style(DrawDot.EdgeStyle.dotted, colors);

        Map<String, DrawDot.Attribute> attributes = connection.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("dotted", attributes.get("style").value);

        assertTrue(attributes.containsKey("fillcolor"));
        assertEquals("green:yellow", attributes.get("fillcolor").value);
    }

    @Test
    public void testEdgeStyledWithoutColorList() {
        DrawDot.Connection connection = new DrawDot.Connection(new DrawDot.Node("from"), new DrawDot.Node("to"));

        connection.style(DrawDot.EdgeStyle.dotted);

        Map<String, DrawDot.Attribute> attributes = connection.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("dotted", attributes.get("style").value);

        assertFalse(attributes.containsKey("fillcolor"));
    }

    @Test
    public void testNodeStyledWithStringColorList() {
        DrawDot.Node node = new DrawDot.Node("testNodeStringColor");
        node.style(DrawDot.NodeStyle.bold, "red:blue");

        Map<String, DrawDot.Attribute> attributes = node.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("bold", attributes.get("style").value);

        assertTrue(attributes.containsKey("fillcolor"));
        assertEquals("red:blue", attributes.get("fillcolor").value);
    }

    @Test
    public void testEdgeStyledWithStringColorList() {
        DrawDot.Connection connection = new DrawDot.Connection(new DrawDot.Node("from"), new DrawDot.Node("to"));
        connection.style(DrawDot.EdgeStyle.solid, "green:yellow");

        Map<String, DrawDot.Attribute> attributes = connection.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("solid", attributes.get("style").value);

        assertTrue(attributes.containsKey("fillcolor"));
        assertEquals("green:yellow", attributes.get("fillcolor").value);
    }

    @Test
    public void testArrowEqualsMethod() {
        DrawDot.Arrow arrow1 = DrawDot.Arrows.DOT.arrow();
        DrawDot.Arrow arrow2 = DrawDot.Arrows.DOT.arrow();
        DrawDot.Arrow arrow3 = DrawDot.Arrows.BOX.arrow();

        assertTrue(arrow1.equals(arrow2));
        assertFalse(arrow1.equals(arrow3));
        assertFalse(arrow1.equals(null));
        assertFalse(arrow1.equals("notAnArrow"));
    }

    @Test
    public void testEmitEmptyComments() throws Exception {
        DrawDot.Node node = new DrawDot.Node("emptyNode");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        node.emit(null, pw, "");

        String output = sw.toString();
        assertFalse(output.contains("//"));
    }


    @Test
    public void testArrowEqualsWithDifferentTypes() {
        DrawDot.Arrow arrow1 = DrawDot.Arrows.DOT.arrow();
        DrawDot.Arrow arrow2 = DrawDot.Arrows.BOX.arrow();

        assertFalse(arrow1.equals(arrow2));
    }


    @Test
    public void testArrowNotEqualWithNull() {
        DrawDot.Arrow arrow = DrawDot.Arrows.DIAMOND.arrow();
        assertFalse(arrow.equals(null));
    }

    @Test
    public void testArrowEqualsWithItself() {
        DrawDot.Arrow arrow = DrawDot.Arrows.VEE.arrow();
        assertTrue(arrow.equals(arrow));
    }

    @Test
    public void testEmitWithEmptyAttributes() throws Exception {
        DrawDot.Node node = new DrawDot.Node("emptyAttrNode");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        node.emit(null, pw, "");

        String output = sw.toString();
        assertTrue(output.contains("\"emptyAttrNode\""));
    }

    @Test
    public void testNodeStyleWithColorListBranch() {
        DrawDot.Node node = new DrawDot.Node("testNodeStyleBranch");
        DrawDot.Color[] colors = {};

        node.style(DrawDot.NodeStyle.filled, colors);

        Map<String, DrawDot.Attribute> attributes = node.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("filled", attributes.get("style").value);

        assertFalse(attributes.containsKey("fillcolor"));
    }

    @Test
    public void testEdgeStyleWithColorListBranch() {
        DrawDot.Connection connection = new DrawDot.Connection(new DrawDot.Node("start"), new DrawDot.Node("end"));
        DrawDot.Color[] colors = {};

        connection.style(DrawDot.EdgeStyle.bold, colors);

        Map<String, DrawDot.Attribute> attributes = connection.getAttributes();
        assertTrue(attributes.containsKey("style"));
        assertEquals("bold", attributes.get("style").value);

        assertFalse(attributes.containsKey("fillcolor"));
    }

    @Test
    public void testEmitWithCommentsCoverage() throws Exception {
        DrawDot.Node node = new DrawDot.Node("testNode");
        node.commentBefore("Start comment");
        node.commentAfter("End comment");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        node.emit(new DrawDot(new String("dummy")), pw, "");

        String output = sw.toString();
        assertTrue(output.contains("// Start comment"));
        assertTrue(output.contains("// End comment"));
    }

    @Test
    public void testEqualsAndHashCodeCoverage() {
        DrawDot.Arrow arrow1 = DrawDot.Arrows.DOT.arrow();
        DrawDot.Arrow arrow2 = DrawDot.Arrows.DOT.arrow();
        DrawDot.Arrow arrow3 = DrawDot.Arrows.BOX.arrow();

        assertEquals(arrow1, arrow2);
        assertNotEquals(arrow1, arrow3);
        assertEquals(arrow1.hashCode(), arrow2.hashCode());
        assertNotEquals(arrow1.hashCode(), arrow3.hashCode());
    }

    @Test
    public void testNodeEqualsSameInstance() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        assertTrue(node.equals(node));
    }

    @Test
    public void testNodeEqualsDifferentType() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        assertFalse(node.equals("someString"));
    }

    @Test
    public void testNodeEqualsNull() {
        DrawDot.Node node = new DrawDot.Node("testNode");
        assertFalse(node.equals(null));
    }


    @Test
    public void testEmitRankDirBranch() throws Exception {
        DrawDot.Graph graph = new DrawDot.Graph("testGraph");
        graph.rankdir(DrawDot.RankDir.TB);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        graph.emit(new DrawDot("dummy"), pw, "");

        String output = sw.toString();
        assertTrue(output.contains("rankdir=\"TB\""));
    }

    @Test
    public void testEmitHeaderBranch() throws Exception {
        DrawDot.Graph graph = new DrawDot.Graph("testGraph");
        graph.clustered(true);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        graph.emit(new DrawDot("dummy"), pw, "");

        String output = sw.toString();
        assertTrue(output.contains("cluster_"));
    }

    @Test
    public void testHashCodeCoverage() {

        DrawDot.Node node = new DrawDot.Node("testNode");
        int expectedHashCode = Objects.hash("testNode");
        assertEquals(expectedHashCode, node.hashCode());
    }
    @Test
    public void testEquals_SameObject() {
        DrawDot.BaseAttributable<?> base = new DrawDot.BaseAttributable<>();
        assertTrue(base.equals(base));
    }

    @Test
    public void testEquals_NullObject() {
        DrawDot.BaseAttributable<?> base = new DrawDot.BaseAttributable<>();
        assertFalse(base.equals(null));
    }

    @Test
    public void testEquals_DifferentClass() {
        DrawDot.BaseAttributable<?> base = new DrawDot.BaseAttributable<>();
        String differentClass = "Not an Attributable";
        assertFalse(base.equals(differentClass));
    }

    @Test
    public void testEquals_DifferentAttributes() {
        DrawDot.BaseAttributable<?> base1 = new DrawDot.BaseAttributable<>();
        DrawDot.BaseAttributable<?> base2 = new DrawDot.BaseAttributable<>();

        base1.getAttributes().put("key", new DrawDot.Attribute("key", "value1"));
        base2.getAttributes().put("key", new DrawDot.Attribute("key", "value2"));

        assertFalse(base1.equals(base2));
    }

    @Test
    public void testBaseAttributableEqualsBranch() {
        DrawDot.BaseAttributable<?> base1 = new DrawDot.BaseAttributable() {};
        DrawDot.BaseAttributable<?> base2 = base1;
        DrawDot.BaseAttributable<?> base3 = new DrawDot.BaseAttributable() {};

        assertEquals(true, base1.equals(base2));
        assertEquals(false, base1.equals(base3));
        assertEquals(false, base1.equals(null));
    }





}




