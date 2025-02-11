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
import java.io.StringWriter;
import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.not;

public class JSONOneLLMTest {

    @Test
    public void testEscapeString() {
        assertThat(JSONOne.escapeString("\"test\"").toString(), is("\\\"test\\\""));
        assertThat(JSONOne.escapeString("\nnew line").toString(), is("\\nnew line"));
        assertThat(JSONOne.escapeString("\r carriage").toString(), is("\\r carriage"));
        assertThat(JSONOne.escapeString("\t tab").toString(), is("\\t tab"));
        assertThat(JSONOne.escapeString("\b backspace").toString(), is("\\b backspace"));
        assertThat(JSONOne.escapeString("\f formfeed").toString(), is("\\f formfeed"));
        assertThat(JSONOne.escapeString("simple text").toString(), is("simple text"));
    }

    @Test
    public void testUnescapeString() {
        assertThat(JSONOne.unescapeString("\\\"test\\\"").toString(), is("\"test\""));
        assertThat(JSONOne.unescapeString("\\nnew line").toString(), is("\nnew line"));
        assertThat(JSONOne.unescapeString("\\r carriage").toString(), is("\r carriage"));
        assertThat(JSONOne.unescapeString("\\t tab").toString(), is("\t tab"));
        assertThat(JSONOne.unescapeString("\\b backspace").toString(), is("\b backspace"));
        assertThat(JSONOne.unescapeString("\\f formfeed").toString(), is("\f formfeed"));
        assertThat(JSONOne.unescapeString("simple text").toString(), is("simple text"));
    }

    @Test
    public void testJSONOneConstructor() {
        JSONOne jsonOne = new JSONOne();
        assertThat(jsonOne, not(nullValue()));
    }

    @Test
    public void testEmptyJArray() {
        JSONOne.JArray array = new JSONOne.JArray();
        assertThat(array.size(), is(0));

        // Ensure adding elements works
        array.addString("test");
        assertThat(array.size(), is(1));
        assertThat(array.get(0).stringValue(), is("test"));

        // Ensure handling of null values
        array.addNull();
        assertThat(array.get(1).getType(), is(JSONOne.Type.NULL));
    }

    @Test
    public void testEmptyJMap() {
        JSONOne.JMap map = new JSONOne.JMap();
        assertThat(map.size(), is(0));

        // Adding elements
        map.putString("key", "value");
        assertThat(map.getString("key", null), is("value"));

        // Checking handling of missing keys
        assertThat(map.getNumber("missing", null), nullValue());

        // Ensure handling of null values
        map.putNull("nullKey");
        assertThat(map.isNull("nullKey", false), is(true));
    }

    @Test
    public void testPrintJArray() throws IOException {
        JSONOne.JArray array = new JSONOne.JArray();
        array.addString("test");
        array.addNumber(42);
        array.addBoolean(true);
        String output = array.print();

        assertThat(output.contains("\"test\""), is(true));
        assertThat(output.contains("42"), is(true));
        assertThat(output.contains("true"), is(true));
    }

    @Test
    public void testPrintJMap() throws IOException {
        JSONOne.JMap map = new JSONOne.JMap();
        map.putString("name", "test");
        map.putNumber("count", 100);
        String output = map.print();

        assertThat(output.contains("\"name\""), is(true));
        assertThat(output.contains("\"test\""), is(true));
        assertThat(output.contains("100"), is(true));
    }

    @Test
    public void testParseInvalidJSON() {
        JSONOne.Parser parser = new JSONOne.Parser("{ invalid json }");
        try {
            parser.singleObject();
            assertThat("Exception expected", false);
        } catch (ParseException e) {
            assertThat("ParseException caught", true);
        }
    }

    @Test
    public void testRoundTripJSON() throws IOException, ParseException {
        String json = "{ \"name\": \"test\", \"count\": 5 }";
        JSONOne.Parser parser = new JSONOne.Parser(json);
        JSONOne.JObject obj = parser.singleObject();
        String printed = obj.print();

        assertThat(printed.contains("\"name\""), is(true));
        assertThat(printed.contains("\"test\""), is(true));
        assertThat(printed.contains("5"), is(true));

        JSONOne.Parser roundTripParser = new JSONOne.Parser(printed);
        JSONOne.JObject roundTripObj = roundTripParser.singleObject();
        assertThat(roundTripObj, is(obj));
    }

    @Test
    public void testEmptyMapPrinting() throws IOException {
        JSONOne.JMap map = new JSONOne.JMap();
        String printed = map.print();
        assertThat(printed, is("{}"));
    }

    @Test
    public void testEmptyArrayPrinting() throws IOException {
        JSONOne.JArray array = new JSONOne.JArray();
        String printed = array.print();
        assertThat(printed, is("[]"));
    }

    @Test
    public void testComplexNesting() throws IOException, ParseException {
        JSONOne.JMap map = new JSONOne.JMap();
        JSONOne.JArray array = new JSONOne.JArray();

        array.addString("item1");
        array.addString("item2");

        map.putArray("items", array);
        map.putString("status", "ok");

        String printed = map.print();
        assertThat(printed.contains("\"status\""), is(true));
        assertThat(printed.contains("\"ok\""), is(true));
        assertThat(printed.contains("\"items\""), is(true));
        assertThat(printed.contains("\"item1\""), is(true));
        assertThat(printed.contains("\"item2\""), is(true));

        JSONOne.Parser parser = new JSONOne.Parser(printed);
        JSONOne.JObject parsedObj = parser.singleObject();
        assertThat(parsedObj, is(map));
    }
}
