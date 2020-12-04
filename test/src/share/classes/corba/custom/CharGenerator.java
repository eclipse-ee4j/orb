/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
        StringBuilder sbuf = new StringBuilder();
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
