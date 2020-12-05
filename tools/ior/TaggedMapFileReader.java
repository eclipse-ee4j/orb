/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package tools.ior;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;

/**
 * A tagged map file is currently defined as follows:
 *
 * Space delimited entries, one per line, in the following
 * format:
 *
 * <tag number> <class name> [<encoding>] 
 *
 * <class name> is either the name of an EncapsHandler,
 * or the name of an IDL Helper class.  If you have a Helper
 * for a certain tagged component or profile, you don't need
 * to write your own EncapsHandler.
 *
 * If the class is a Helper, you can optionally specify
 * what encoding to use.  If you provide your own handler,
 * you are responsible for using the CodecFactory to
 * create the correct codec.
 *
 * If <encoding> is unspecified, it defaults to the GIOP 1.0
 * CDR encapsulation encoding.  Otherwise, use this format:
 *
 * ENCODING_CDR_ENCAPS <major> <minor>
 *              or
 * <short format> <major> <minor>
 *
 * Where <short format> is the number to be used in an
 * org.omg.IOP.Encoding.
 *
 * Any lines starting with double slashes are ignored.
 */
public class TaggedMapFileReader
{
    private static final Encoding DEFAULT_ENCODING
        = new Encoding(ENCODING_CDR_ENCAPS.value, (byte)1, (byte)0);

    /**
     * See above for how to optionally specify an encoding
     * for use with helper classes.
     */
    private Encoding parseEncodingForHelper(StringTokenizer strTok) {
            
        Encoding encoding = DEFAULT_ENCODING;

        if (strTok.hasMoreTokens()) {
            
            String encodingStr = strTok.nextToken();
            String majorStr = strTok.nextToken();
            String minorStr = strTok.nextToken();

            short encodingNum;

            if (encodingStr.equals("ENCODING_CDR_ENCAPS"))
                encodingNum = ENCODING_CDR_ENCAPS.value;
            else
                encodingNum = Short.parseShort(encodingStr);
            
            encoding = new Encoding(encodingNum,
                                    Byte.parseByte(majorStr),
                                    Byte.parseByte(minorStr));
        }

        return encoding;
    }

    /**
     * Create a TagHelperHandler which will delegate to the
     * given helper class, and unmarshal with a Codec of
     * the specified encoding.
     */
    private EncapsHandler createTagHelperHandler(String helperClassName,
                                                 Encoding encoding,
                                                 Utility util)
        throws ClassNotFoundException, 
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException,
               NoSuchMethodException,
               UnknownEncoding,
               SecurityException {

        Codec codec = util.getCodecFactory().create_codec(encoding);

        return new TagHelperHandler(helperClassName, codec);
    }

    /**
     * Parse a line of text, create the appropriate
     * EncapsHandler, and add it to the Map.
     */
    private void parseLine(String fullLine,
                           Map map,
                           Utility util) {
        
        StringTokenizer strTok 
            = new StringTokenizer(fullLine);

        String number = strTok.nextToken();
        
        // Allow comment lines
        if (number.startsWith("//"))
            return;

        Integer tag = Integer.valueOf(number);

        String className = strTok.nextToken();
        
        try {

            EncapsHandler handler;

            if (className.endsWith("Helper")) {
                handler 
                    = createTagHelperHandler(className,
                                             parseEncodingForHelper(strTok),
                                             util);
            } else {
                handler = (EncapsHandler)Class.forName(className).newInstance();
            }

            map.put(tag, handler);

        } catch (Exception ex) {
            System.out.println("Error parsing line: " + fullLine);
            ex.printStackTrace();
        }
    }

    /**
     * Read the given file, creating EncapsHandlers for each
     * valid line of input.
     */
    public void readMapFile(String fileName, Map map, Utility util)
        throws IOException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);

            BufferedReader reader
                = new BufferedReader(new InputStreamReader(fis));

            do {

                String input = reader.readLine();
                if (input == null)
                    break;

                // Skip blank lines
                if (input.length() == 0)
                    continue;

                parseLine(input, map, util);

            } while (true);

        } catch (FileNotFoundException fnfe) {
            // Silent, non-fatal
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {}
            }
        }
    }
}

