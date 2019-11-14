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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Smaz class for compression small strings. Port to java from
 * <a href="https://github.com/antirez/smaz/">antirez</a> This class is
 * immutable.
 *
 * @author icedrake
 * @author davidmoten made some improvements, notably UTF-8 support with thanks
 *         to https://github.com/tmbo/scala-smaz.
 */
public final class Smaz {

    private static final int MAX_ASCII = 127;

    private static final int CODE_UTF8_AHEAD = 253;
    private static final int CODE_SINGLE_CHAR_ASCII = 254;
    private static final int CODE_MULTI_CHAR_ASCII = 255;
    
    private static final int MAXIMAL_VERB_BUFFER_LENGTH = 256;

    private static final int CODE_HASH_MAP_SIZE = 241;

    private Smaz() {
        // prevent instantiation
    }

    /* Compression CODEBOOK, used for compression */
    private static final String[] CODEBOOK = { "\002s,\266", "\003had\232\002leW", "\003on \216", "", "\001yS",
            "\002ma\255\002li\227", "\003or \260", "", "\002ll\230\003s t\277", "\004fromg\002mel", "", "\003its\332",
            "\001z\333", "\003ingF", "\001>\336", "\001 \000\003   (\002nc\344", "\002nd=\003 on\312",
            "\002ne\213\003hat\276\003re q", "", "\002ngT\003herz\004have\306\003s o\225", "",
            "\003ionk\003s a\254\002ly\352", "\003hisL\003 inN\003 be\252", "", "\003 fo\325\003 of \003 ha\311", "",
            "\002of\005", "\003 co\241\002no\267\003 ma\370", "", "", "\003 cl\356\003enta\003 an7",
            "\002ns\300\001\"e", "\003n t\217\002ntP\003s, \205", "\002pe\320\003 we\351\002om\223", "\002on\037", "",
            "\002y G", "\003 wa\271", "\003 re\321\002or*", "", "\002=\"\251\002ot\337", "\003forD\002ou[", "\003 toR",
            "\003 th\r", "\003 it\366", "\003but\261\002ra\202\003 wi\363\002</\361", "\003 wh\237", "\002  4",
            "\003nd ?", "\002re!", "", "\003ng c", "", "\003ly \307\003ass\323\001a\004\002rir", "", "", "", "\002se_",
            "\003of \"", "\003div\364\002ros\003ere\240", "", "\002ta\310\001bZ\002si\324", "",
            "\003and\u0007\002rs\335", "\002rt\362", "\002teE", "\003ati\316", "\002so\263", "\002th\021",
            "\002tiJ\001c\034\003allp", "\003ate\345", "\002ss\246", "\002stM", "", "\002><\346", "\002to\024",
            "\003arew", "\001d\030", "\002tr\303", "", "\001\n1\003 a \222", "\003f tv\002veo", "\002un\340", "",
            "\003e o\242", "\002a \243\002wa\326\001e\002", "\002ur\226\003e a\274", "\002us\244\003\n\r\n\247",
            "\002ut\304\003e c\373", "\002we\221", "", "", "\002wh\302", "\001f,", "", "", "", "\003d t\206", "", "",
            "\003th \343", "\001g;", "", "", "\001\r9\003e s\265", "\003e t\234", "", "\003to Y", "\003e\r\n\236",
            "\002d \036\001h\022", "", "\001,Q", "\002 a\031", "\002 b^", "\002\r\n\025\002 cI", "\002 d\245",
            "\002 e\253", "\002 fh\001i\b\002e \013", "", "\002 hU\001-\314", "\002 i8", "", "", "\002 l\315",
            "\002 m{", "\002f :\002 n\354", "\002 o\035", "\002 p}\001.n\003\r\n\r\250", "", "\002 r\275", "\002 s>",
            "\002 t\016", "", "\002g \235\005which+\003whi\367", "\002 w5", "\001/\305", "\003as \214", "\003at \207",
            "", "\003who\331", "", "\001l\026\002h \212", "", "\002, $", "", "\004withV", "", "", "", "\001m-", "", "",
            "\002ac\357", "\002ad\350", "\003TheH", "", "", "\004this\233\001n\t", "", "\002. y", "",
            "\002alX\003e, \365", "\003tio\215\002be\\", "\002an\032\003ver\347", "", "\004that0\003tha\313\001o\006",
            "\003was2", "\002arO", "\002as.", "\002at'\003the\001\004they\200\005there\322\005theird", "\002ce\210",
            "\004were]", "", "\002ch\231\002l \264\001p<", "", "", "\003one\256", "", "\003he \023\002dej",
            "\003ter\270", "\002cou", "", "\002by\177\002di\201\002eax", "", "\002ec\327", "\002edB", "\002ee\353", "",
            "", "\001r\f\002n )", "", "", "", "\002el\262", "", "\003in i\002en3", "", "\002o `\001s\n", "",
            "\002er\033", "\003is t\002es6", "", "\002ge\371", "\004.com\375", "\002fo\334\003our\330",
            "\003ch \301\001t\003", "\002hab", "", "\003men\374", "", "\002he\020", "", "", "\001u&", "\002hif", "",
            "\003not\204\002ic\203", "\003ed @\002id\355", "", "", "\002ho\273", "\002r K\001vm", "", "", "",
            "\003t t\257\002il\360", "\002im\342", "\003en \317\002in\017", "\002io\220", "\002s \027\001wA", "",
            "\003er |", "\003es ~\002is%", "\002it/", "", "\002iv\272", "", "\002t #\u0007http://C\001x\372",
            "\002la\211", "\001<\341", "\003, a\224" };

    /* Reverse compression CODEBOOK, used for decompression */
    private static final String[] REVERSE_CODEBOOK = { " ", "the", "e", "t", "a", "of", "o", "and", "i", "n", "s", "e ",
            "r", " th", " t", "in", "he", "th", "h", "he ", "to", "\r\n", "l", "s ", "d", " a", "an", "er", "c", " o",
            "d ", "on", " of", "re", "of ", "t ", ", ", "is", "u", "at", "   ", "n ", "or", "which", "f", "m", "as",
            "it", "that", "\n", "was", "en", "  ", " w", "es", " an", " i", "\r", "f ", "g", "p", "nd", " s", "nd ",
            "ed ", "w", "ed", "http://", "for", "te", "ing", "y ", "The", " c", "ti", "r ", "his", "st", " in", "ar",
            "nt", ",", " to", "y", "ng", " h", "with", "le", "al", "to ", "b", "ou", "be", "were", " b", "se", "o ",
            "ent", "ha", "ng ", "their", "\"", "hi", "from", " f", "in ", "de", "ion", "me", "v", ".", "ve", "all",
            "re ", "ri", "ro", "is ", "co", "f t", "are", "ea", ". ", "her", " m", "er ", " p", "es ", "by", "they",
            "di", "ra", "ic", "not", "s, ", "d t", "at ", "ce", "la", "h ", "ne", "as ", "tio", "on ", "n t", "io",
            "we", " a ", "om", ", a", "s o", "ur", "li", "ll", "ch", "had", "this", "e t", "g ", "e\r\n", " wh", "ere",
            " co", "e o", "a ", "us", " d", "ss", "\n\r\n", "\r\n\r", "=\"", " be", " e", "s a", "ma", "one", "t t",
            "or ", "but", "el", "so", "l ", "e s", "s,", "no", "ter", " wa", "iv", "ho", "e a", " r", "hat", "s t",
            "ns", "ch ", "wh", "tr", "ut", "/", "have", "ly ", "ta", " ha", " on", "tha", "-", " l", "ati", "en ", "pe",
            " re", "there", "ass", "si", " fo", "wa", "ec", "our", "who", "its", "z", "fo", "rs", ">", "ot", "un", "<",
            "im", "th ", "nc", "ate", "><", "ver", "ad", " we", "ly", "ee", " n", "id", " cl", "ac", "il", "</", "rt",
            " wi", "div", "e, ", " it", "whi", " ma", "ge", "x", "e c", "men", ".com" };

    /**
     * Returns compressed byte array for the specified string
     *
     * @param inString
     * @return byte array
     */
    public static byte[] compress(String inString) {

        StringBuilder verb = new StringBuilder();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CharBuffer charBuffer = CharBuffer.wrap(inString);
        int remainingCharacters = charBuffer.remaining();

        while (remainingCharacters > 0) {
            int hashForLength1 = 0;
            int hashForLength2 = 0;
            int hashForLength3 = 0;
            charBuffer.mark();
            char current = charBuffer.get();
            if (isAscii(current)) {
                hashForLength1 = current << 3;
                hashForLength2 = hashForLength1;

                if (remainingCharacters > 1) {
                    hashForLength2 += charBuffer.get();
                }
                if (remainingCharacters > 2) {
                    hashForLength3 = hashForLength2 ^ charBuffer.get();
                } else {
                    hashForLength3 = 0;
                }

                charBuffer.reset();
                int j = 7;
                if (j > remainingCharacters) {
                    j = remainingCharacters;
                }
                boolean found = false;
                while (j > 0) {
                    final CharBuffer slot;
                    if (j == 1) {
                        slot = CharBuffer.wrap(CODEBOOK[hashForLength1 % CODE_HASH_MAP_SIZE]);
                    } else if (j == 2) {
                        slot = CharBuffer.wrap(CODEBOOK[hashForLength2 % CODE_HASH_MAP_SIZE]);
                    } else {
                        slot = CharBuffer.wrap(CODEBOOK[hashForLength3 % CODE_HASH_MAP_SIZE]);
                    }
                    int slotLength = slot.length();
                    int slotIndex = 0;
                    int slotEndIndex = slotIndex + j + 1;
                    while (!found && slotLength > 0 && slotEndIndex <= slotLength) {
                        if (slot.get(slotIndex) == j && remainingCharacters >= j
                                && (slot.subSequence(slotIndex + 1, slotEndIndex).toString()
                                        .equals(charBuffer.subSequence(0, j).toString()))) {
                            flushVerbBuffer(verb, output);
                            output.write(slot.get(slot.get(slotIndex) + 1 + slotIndex));
                            charBuffer.position(charBuffer.position() + j);
                            remainingCharacters -= j;
                            found = true;
                        } else {
                            slotIndex += 1;
                            slotEndIndex = slotIndex + j + 1;
                        }
                    }
                    j -= 1;
                }
                if (!found) {
                    if (remainingCharacters > 0) {
                        remainingCharacters -= 1;
                        verb.append(charBuffer.subSequence(0, 1).toString());
                    }
                    charBuffer.position(charBuffer.position() + 1);
                }
                int verbLength = verb.length();
                if (verbLength == MAXIMAL_VERB_BUFFER_LENGTH || verbLength > 0 && remainingCharacters == 0) {
                    flushVerbBuffer(verb, output);
                }
            } else {
                //////////////////
                // handleUTF8
                /////////////////
                StringBuilder utf8Str = new StringBuilder();
                int next = current;
                boolean reachedEndOfString = false;
                // Read until we read the first ascii character or we reach the end of the
                // string
                while (!isAscii(next) && !reachedEndOfString) {
                    utf8Str.append((char) next);
                    remainingCharacters -= 1;
                    if (remainingCharacters > 0) {
                        next = charBuffer.get();
                    } else {
                        reachedEndOfString = true;
                    }
                }
                flushVerbBuffer(verb, output);
                ByteBuffer encoded = StandardCharsets.UTF_8.encode(utf8Str.toString());
                output.write(CODE_UTF8_AHEAD);
                output.write(encoded.limit());
                try {
                    output.write(Arrays.copyOf(encoded.array(), encoded.limit()));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                // Reposition the cursor on the first character that is an
                // ascii char after the utf8 string
                if (remainingCharacters > 0) {
                    charBuffer.position(charBuffer.position() - 1);
                }
            }
            remainingCharacters = charBuffer.remaining();
        }
        return output.toByteArray();
    }

    private static boolean isAscii(int c) {
        return c <= MAX_ASCII;
    }

    private static void flushVerbBuffer(StringBuilder verb, ByteArrayOutputStream output) {
        if (verb.length() > 0) {
            outputVerb(output, verb.toString());
            verb.setLength(0);
        }
    }

    /**
     * Outputs the verbatim string to the output stream
     *
     * @param baos
     * @param str
     */
    private static void outputVerb(ByteArrayOutputStream baos, String str) {
        if (str.length() == 1) {
            baos.write(CODE_SINGLE_CHAR_ASCII);
            baos.write(str.toCharArray()[0]);
        } else {
            baos.write(CODE_MULTI_CHAR_ASCII);
            baos.write(str.length());
            try {
                baos.write(str.getBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Decompress byte array from compress back into String
     *
     * @param strBytes bytes to decompress
     * @return decompressed String
     * @see Smaz#compress(String)
     */
    public static String decompress(byte[] strBytes) {
        return decompress(strBytes, 0, strBytes.length);
    }

    public static String decompress(byte[] strBytes, int offset, int length) {

        StringBuilder out = new StringBuilder();
        int i = offset;
        while (i < length + offset) {
            char b = (char) (0xFF & strBytes[i]);
            if (b == CODE_UTF8_AHEAD) {
                i += 1;
                byte utf8Length = strBytes[i];
                out.append(new String(strBytes, i + 1, utf8Length, StandardCharsets.UTF_8));
                i += utf8Length;
            } else if (b == CODE_SINGLE_CHAR_ASCII) {
                i += 1;
                out.append((char) strBytes[i]);
            } else if (b == CODE_MULTI_CHAR_ASCII) {
                i += 1;
                byte decodedLength = strBytes[i];
                int j = 1;
                while (j <= decodedLength) {
                    out.append((char) strBytes[i + j]);
                    j += 1;
                }
                i += decodedLength;
            } else {
                int loc = 0xFF & b;
                out.append(REVERSE_CODEBOOK[loc]);
            }
            i += 1;
        }
        return out.toString();
    }
}
