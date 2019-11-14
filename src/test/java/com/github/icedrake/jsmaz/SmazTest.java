/*
   Copyright 2011 icedrake

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.github.icedrake.jsmaz;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

/**
 * @author icedrake
 * @author davidmoten
 */
public final class SmazTest {

    @Test
    public void roundTripTestOnPlainAscii() {
        String s = "this is a simple test";
        assertEquals(s, Smaz.decompress(Smaz.compress(s)));
    }

    @Test
    public void testCompressionIsBetterThan50PercentForSimpleString() {
        String s = "this is a simple test";
        assertEquals(10, Smaz.compress(s).length);
    }

    @Test
    public void roundTripAllAsciiCharacters() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            b.append((char) i);
        }
        String s = b.toString();
        assertEquals(s, Smaz.decompress(Smaz.compress(s)));
    }

    @Test
    public void roundTripEmptyString() {
        String s = "";
        assertEquals(s, Smaz.decompress(Smaz.compress(s)));
    }

    @Test
    public void roundTripLongString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 1024 * 1024; i++) {
            b.append((char) (i % 128));
        }
        String s = b.toString();
        assertEquals(s, Smaz.decompress(Smaz.compress(s)));
    }

    @Test
    // minus a few that were off by 1 from the c version but match the ruby one
    public void originalSmazCTest() {

        Map<String, Integer> c = new LinkedHashMap<String, Integer>();
        c.put("This is a small string", 50);
        c.put("foobar", 34);
        c.put("the end", 58);
        c.put("not-a-g00d-Exampl333", -15);
        c.put("Smaz is a simple compression library", 39);
        c.put("1000 numbers 2000 will 10 20 30 compress very little", 10);
        c.put("and now a few italian sentences:", 41);
        c.put("Nel mezzo del cammin di nostra vita, mi ritrovai in una selva oscura", 33);
        c.put("Mi illumino di immenso", 37);
        c.put("try it against urls", 37);
        c.put("http://google.com", 59);
        c.put("http://programming.reddit.com", 52);
        c.put("http://github.com/antirez/smaz/tree/master", 46);

        for (Entry<String, Integer> entry : c.entrySet()) {
            byte[] origBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] compressedBytes = Smaz.compress(entry.getKey());
            // System.out.println(entry + ", originalLength="+ origBytes.length + ",
            // compressedLength=" + compressedBytes.length);
            int compressionLevel = 100 - ((100 * compressedBytes.length) / origBytes.length);
            assertEquals((int) entry.getValue(), compressionLevel);
        }
    }

    @Test
    public void roundTripTestOnUtf8() {
        String s = "g ÿa";
        assertEquals(s, Smaz.decompress(Smaz.compress(s)));
    }

    @Test
    public void roundTripTestOnLongUtf8() {
        String s = "ᚠᛇᚻ᛫ᛒᛦᚦ᛫ᚠᚱᚩᚠᚢᚱ᛫ᚠᛁᚱᚪ᛫ᚷᛖᚻᚹᛦᛚᚳᚢᛗ";
        assertEquals(s, Smaz.decompress(Smaz.compress(s)));
    }

}
