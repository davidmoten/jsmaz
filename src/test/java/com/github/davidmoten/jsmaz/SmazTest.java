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
package com.github.davidmoten.jsmaz;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Test;

/**
 * @author icedrake
 * @author davidmoten
 */
public final class SmazTest {

    @Test
    public void roundTripTestOnPlainAscii() {
        checkRoundTrip("this is a simple test");
    }

    private static void checkRoundTrip(String s) {
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
        checkRoundTrip(s);
    }

    @Test
    public void roundTripUtf8Extremes() {
        checkRoundTrip("\u0000\u1000\u9999");
    }

    @Test
    public void roundTripSpecialCharacters() {
        checkRoundTrip(".com");
    }

    @Test
    public void roundTripRandomAsciiManyTimes() {
        Random r = new Random();
        for (int i = 0; i < 10000; i++) {
            List<Integer> chars = new ArrayList<>();
            StringBuilder b = new StringBuilder();
            for (int j = 0; j < 1024; j++) {
                int n = r.nextInt(128);
                b.append((char) n);
                chars.add(n);
            }
            String s = b.toString();
            String actual = "";
            try {
                actual = Smaz.decompress(Smaz.compress(s));
            } catch (Throwable t) {
                System.out.println("these chars failed: " + chars);
                throw t;
            }
            if (!actual.equals(s)) {
                System.out.println("these chars failed: " + chars);
                throw new AssertionError("round trip failed");
            }
        }
    }

    @Test
    public void roundTripEmptyString() {
        checkRoundTrip("");
    }

    @Test
    public void roundTripLongString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 1024 * 1024; i++) {
            b.append((char) (i % 128));
        }
        checkRoundTrip(b.toString());
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
        c.put("http://google.com", 48);
        c.put("http://programming.reddit.com", 45);
        c.put("http://github.com/antirez/smaz/tree/master", 41);

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
        checkRoundTrip("g ÿa");
    }

    @Test
    public void roundTripTestOnLongUtf8() {
        checkRoundTrip("ᚠᛇᚻ᛫ᛒᛦᚦ᛫ᚠᚱᚩᚠᚢᚱ᛫ᚠᛁᚱᚪ᛫ᚷᛖᚻᚹᛦᛚᚳᚢᛗ");
    }

    @Test
    public void printCompressionFactorForTypicalLogLine() {
        String s = "2019-11-14 23:15:11.712 INFO  au.gov.amsa.er.craft.tracking.actions.FixBehaviour [default-akka.actor.default-dispatcher-4] - secondary match identifiers=[MMSI=503432000], craftId=18418467884";
        System.out.println("previousLength = " + s.length() + ", compressed length = " + Smaz.compress(s).length);
    }

}
