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

package tools.ior;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.IIOP.Version;

/**
 * Utility for dumping IOR information.  Uses two files which
 * map numbers (tags) to EncapsHandlers or Helpers for
 * profiles and components.
 */
public class IORDecoder implements Utility
{
    public static final String DEFAULT_TAG_PROFILE_FILE = "tagprofs.txt";
    public static final String DEFAULT_TAG_COMP_FILE = "tagcomps.txt";

    private ORB orb;
    private Map tagProfileMap;
    private Map tagCompMap;
    private TextOutputHandler out;
    private Utility util;
    private CodecFactory codecFactory;

    /**
     * Uses default names for the tagged profile and component
     * map files, and a default TextOutputHandler.
     */
    public IORDecoder(ORB orb) throws InitializationException {
        this(orb, 
             DEFAULT_TAG_PROFILE_FILE, 
             DEFAULT_TAG_COMP_FILE, 
             new TextOutputHandler());
    }

    /**
     * Resolves a CodecFactory and processes the tagged component and 
     * tagged profile handler files.
     */
    public IORDecoder(ORB orb,
                      String taggedProfileMapFile,
                      String taggedComponentMapFile,
                      TextOutputHandler textOutputHandler)
        throws InitializationException {

        try {

            this.orb = orb;
            out = textOutputHandler;
            util = this;

            org.omg.CORBA.Object objRef 
                = orb.resolve_initial_references("CodecFactory");

            if (objRef == null)
                throw new InitializationException("Can't find CodecFactory");

            codecFactory = CodecFactoryHelper.narrow(objRef);

            // Read the mapping files for tagged components
            // and tagged profiles
            initializeHandlers(taggedProfileMapFile,
                               taggedComponentMapFile);

        } catch (org.omg.CORBA.ORBPackage.InvalidName in) {
            throw new InitializationException("Can't find CodecFactory: "
                                              + in.getMessage());
        } catch (IOException ioe) {
            throw new InitializationException("Error reading handler file: "
                                              + ioe);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InitializationException(ex.getMessage());
        }
    }

    /**
     * Main entry point for printing IORs after initialization.
     */
    public void display(String iorString) throws DecodingException {

        out.output("Number of known TaggedProfiles: "
                   + tagProfileMap.size());

        out.output("Number of known TaggedComponents: "
                   + tagCompMap.size());

        byte[] iorBytes = stringifiedIORToBytes(iorString);

        try {

            Codec codec = util.getCDREncapsCodec(Utility.GIOP_1_0);

            Any any = codec.decode_value(iorBytes, IORHelper.type());
            org.omg.IOP.IOR ior = IORHelper.extract(any);

            util.recursiveDisplay("IOR", ior, out);

        } catch (Exception ex) {
            out.output("Error decoding IOR: " + ex.getMessage());
            dumpData(iorBytes, out);
        }
    }

    public static void main(String[] args)
    {
        try {
            
            if (args.length != 1) {
                System.out.println("IORDecoder <stringified IOR>");
                return;
            }
            
            ORB orb = ORB.init(args, System.getProperties());

            IORDecoder printIOR = new IORDecoder(orb);

            printIOR.display(args[0]);

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Read the two tag map files.
     */
    private void initializeHandlers(String tagProfileMapFile,
                                    String tagCompMapFile)
        throws IOException {

        tagProfileMap = new HashMap();
        tagCompMap = new HashMap();

        TaggedMapFileReader mapUtil = new TaggedMapFileReader();

        mapUtil.readMapFile(tagProfileMapFile,
                            tagProfileMap,
                            util);

        mapUtil.readMapFile(tagCompMapFile,
                            tagCompMap,
                            util);
    }


    /**
     * Copied from com.sun.corba.ee.impl.corba.IOR
     */
    private static final String STRINGIFY_PREFIX = "IOR:" ;
    private static final int PREFIX_LENGTH = STRINGIFY_PREFIX.length() ;
    private static final int NIBBLES_PER_BYTE = 2 ;
    private static final int UN_SHIFT = 4 ; // "UPPER NIBBLE" shift factor for <<
    private byte[] stringifiedIORToBytes(String ior) {
        if ((ior.length() & 1) == 1)
            throw new IllegalArgumentException("Stringified IOR has odd length");

        byte[] buf = new byte[(ior.length() - PREFIX_LENGTH) / NIBBLES_PER_BYTE];

        for (int i=PREFIX_LENGTH, j=0; i < ior.length(); i +=NIBBLES_PER_BYTE, j++) {
            buf[j] = (byte)((hexOf(ior.charAt(i)) << UN_SHIFT) & 0xF0);
            buf[j] |= (byte)(hexOf(ior.charAt(i+1)) & 0x0F);
        }

        return buf;
    }

    /**
     * Pretty prints the buffer as hex with ASCII
     * interpretation on the side.
     */
    public void printBuffer(byte[] buffer, 
                            TextOutputHandler out) {

        StringBuilder msg = new StringBuilder();
        char[] charBuf = new char[16];

        for (int i = 0; i < buffer.length; i += 16) {
            
            int j = 0;
            
            msg.setLength(0);
            
            // For every 16 bytes, there is one line
            // of output.  First, the hex output of
            // the 16 bytes with each byte separated
            // by a space.
            while (j < 16 && j + i < buffer.length) {
                int k = buffer[i + j];
                if (k < 0)
                    k = 256 + k;
                String hex = Integer.toHexString(k);
                if (hex.length() == 1)
                    hex = "0" + hex;
                
                msg.append(hex);
                msg.append(' ');
                
                j++;
            }
            
            // Add any extra spaces to align the
            // text column in case we didn't end
            // at 16
            while (j < 16) {
                msg.append("   ");
                j++;
            }
            
            // Now output the ASCII equivalents.  Non-ASCII
            // characters are shown as periods.
            int x = 0;
            
            while (x < 16 && x + i < buffer.length) {
                if (Character.isLetterOrDigit((char)buffer[i + x]))
                    charBuf[x] = (char)buffer[i + x];
                else
                    charBuf[x] = '.';
                x++;
            }
            
            msg.append(charBuf, 0, x);

            out.output(msg.toString());
        }
    }

    /**
     * Copied from com.sun.corba.ee.impl.util.Utility.
     */
    public static int hexOf(char x)
    {
        int val;

        val = x - '0';
        if (val >=0 && val <= 9)
            return val;

        val = (x - 'a') + 10;
        if (val >= 10 && val <= 15)
            return val;

        val = (x - 'A') + 10;
        if (val >= 10 && val <= 15)
            return val;

        throw new IllegalArgumentException("Bad hex digit: " + x);
    }

    /**
     * Recursively display the fields of the given object.
     * Cases:
     *      Core Java classes          Call toString
     *      TaggedComponent            Look up handler in map
     *      TaggedProfile              Look up handler in map
     *      Arrays                     Recursively display elements
     *      Other                      Recursively display fields
     */
    public void recursiveDisplay(String name,
                                 java.lang.Object object,
                                 TextOutputHandler out) {
        try {

            Class cl = object.getClass();

            // Print any core JDK classes rather than
            // diving inside them.  This handles the
            // java.lang primitive wrappers well.
            if (cl.getName().startsWith("java"))
                out.output(name + ": " + object);
            else
            if (cl.isArray())
                displayArray(name, object, cl.getComponentType(), out);
            else
            if (cl.equals(TaggedComponent.class)) {
                // TaggedComponents are special cases.  We have a Map
                // from the tag to a EncapsHandler.
                TaggedComponent tc = (TaggedComponent)object;

                displayTaggedEntry(name, tc.tag, tc.component_data, tagCompMap, out);
            } else
            if (cl.equals(TaggedProfile.class)) {
                // We also have a Map from the tag to an EncapsHandler
                // for TaggedProfiles
                TaggedProfile tp = (TaggedProfile)object;
                
                displayTaggedEntry(name, tp.tag, tp.profile_data, tagProfileMap, out);
            } else {
                out.output(name + ':');
                out.increaseIndentLevel();

                Field[] fields = cl.getDeclaredFields();

                for (int i = 0; i < fields.length; i++) {
                    recursiveDisplay(fields[i].getName(),
                                     fields[i].get(object),
                                     out);
                }

                out.decreaseIndentLevel();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Used for TaggedProfiles and TaggedComponents
     */
    private void displayTaggedEntry(String name,
                                    int tag,
                                    byte[] data,
                                    Map tagToHandlerMap,
                                    TextOutputHandler out) {
        EncapsHandler handler
            = (EncapsHandler)tagToHandlerMap.get(new Integer(tag));

        out.output(name);
        out.increaseIndentLevel();
        out.output("tag: " + tag);

        if (handler == null) {
            // We don't know anything about this this,
            // so dump its data as hex.
            dumpData(data, out);

        } else {
            try {
                // Let the handler display the data
                handler.display(data, out, this);
            } catch (Exception ex) {
                out.output("Error while decoding: " + ex.getMessage());
                dumpData(data, out);
            }
        }
        
        out.decreaseIndentLevel();
    }

    private void dumpData(byte[] data, TextOutputHandler out) {
        out.output("data:");
        out.increaseIndentLevel();
        printBuffer(data, out);
        out.decreaseIndentLevel();
    }

    private void displayArray(String name,
                              java.lang.Object object,
                              Class componentType,
                              TextOutputHandler out) {

        if (componentType.equals(Byte.TYPE)) {
            // Show byte arrays as pretty hex dumps
            out.output(name + ':');
            out.increaseIndentLevel();
            printBuffer((byte[])object, out);
            out.decreaseIndentLevel();
        } else {
            // Make an effort to look like we know
            // what we're dealing with
            out.output(name + " array [length " + Array.getLength(object) + ']');
            out.increaseIndentLevel();

            for (int i = 0; i < Array.getLength(object); i++) {
                String itemName = name + '[' + i + ']';
                recursiveDisplay(itemName,
                                 Array.get(object, i),
                                 out);
            }

            out.decreaseIndentLevel();
        }
    }

    public org.omg.CORBA.ORB getORB() {
        return orb;
    }

    public CodecFactory getCodecFactory() {
        return codecFactory;
    }

    public Codec getCDREncapsCodec(Version giopVersion)
        throws UnknownEncoding {

        return codecFactory.create_codec(new Encoding(ENCODING_CDR_ENCAPS.value,
                                                      giopVersion.major,
                                                      giopVersion.minor));
    }
}
