/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.custom;

/**
 * Utility class for generating characters in Latin-1 and Unicode.
 */
public class CharGenerator
{
    public static char[] getLatin1Chars() {
        return CharGenerator.getChars(new LatinSelector());
    }

    public static char[] getSomeUnicodeChars() {
        return CharGenerator.getChars(new WideSelector());
    }

    public static char[] getChars(CharSelector selector) {
        StringBuffer sbuf = new StringBuffer();
        for (char ch = Character.MIN_VALUE; ch < Character.MAX_VALUE; ch++) {
            if (selector.testThisCharacter(ch))
                sbuf.append(ch);
        }

        char[] result = new char[sbuf.length()];

        sbuf.getChars(0, result.length, result, 0);

        return result;
    }

    private static abstract class CharSelector
    {
        public abstract boolean testThisCharacter(char ch);
    }
    
    private static class LatinSelector extends CharSelector
    {
        public boolean testThisCharacter(char ch)
        {
            Character.UnicodeBlock blk = Character.UnicodeBlock.of(ch);
            
            return (blk != null &&
                    blk == Character.UnicodeBlock.BASIC_LATIN);
        }
    }

    private static class WideSelector extends LatinSelector
    {
        public boolean testThisCharacter(char ch)
        {
            Character.UnicodeBlock blk = Character.UnicodeBlock.of(ch);
            return (blk != null && 
                    (blk == Character.UnicodeBlock.KATAKANA ||
                     blk == Character.UnicodeBlock.HIRAGANA ||
                     blk == Character.UnicodeBlock.LATIN_1_SUPPLEMENT ||
                     blk == Character.UnicodeBlock.LATIN_EXTENDED_A ||
                     blk == Character.UnicodeBlock.LATIN_EXTENDED_B ||
                     blk == Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL ||
                     blk == Character.UnicodeBlock.NUMBER_FORMS ||
                     blk == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS ||
                     blk == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                     blk == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                     blk == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                     blk == Character.UnicodeBlock.ARABIC ||
                     super.testThisCharacter(ch)));
        }
    }
}
