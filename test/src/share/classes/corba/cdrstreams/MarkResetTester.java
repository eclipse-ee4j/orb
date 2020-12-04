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

package corba.cdrstreams;

import java.io.*;

public class MarkResetTester implements Serializable
{
    int fragmentSize;
    private static final long TESTVALUE = 275125891;
    private static final int FRAGMENT_SIZE_MULTIPLIER = 3;

    public MarkResetTester(int fragmentSize) {
        this.fragmentSize = fragmentSize;
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        // Write how many longs are going to be sent
        int numLongs = (int)(FRAGMENT_SIZE_MULTIPLIER 
                             * Math.floor(fragmentSize / 8.0));
        out.writeInt(numLongs);

        System.out.println("Writing " + numLongs + " longs");

        for (int i = 0; i < numLongs; i++)
            out.writeLong((long)i);

        // Do the same thing but with Longs
        out.writeInt(numLongs);

        System.out.println("Writing " + numLongs + " Longs");

        // Intermix indirected and new instances with the same value
        Long indirectedLong = new Long(TESTVALUE);
        for (int i = 0; i < numLongs; i++) {
            if (i % 3 == 0)
                out.writeObject(indirectedLong);
            else
                out.writeObject(new Long(TESTVALUE));
        }

        // Try arrays to test special chunking code
        int arraySize = (int)(Math.ceil(fragmentSize / 2.0));
        int numArrays = 7;
        
        System.out.println("Writing " + numArrays + " arrays of "
                           + arraySize + " ints");

        out.writeInt(numArrays);
        out.writeInt(arraySize);

        int indirectedArray[] = new int[arraySize];
        for (int i = 0; i < indirectedArray.length; i++)
            indirectedArray[i] = i + 111;

        for (int i = 0; i < numArrays; i++) {
            if (i % 4 == 0)
                out.writeObject(indirectedArray);
            else {
                int newArray[] = new int[arraySize];
                System.arraycopy(indirectedArray, 0, newArray, 0, arraySize);
                out.writeObject(newArray);
            }
        }

        System.out.println("Finished writing");
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        // Read how many longs are going to follow
        int numLongs = in.readInt();

        System.out.println("Reading " + numLongs + " longs");

        if (numLongs != (int)(FRAGMENT_SIZE_MULTIPLIER
                              * Math.floor(fragmentSize / 8.0)))
            throw new IOException("Incorrect number of longs: " + numLongs
                                  + " expected " + (FRAGMENT_SIZE_MULTIPLIER
                                                    * Math.floor(fragmentSize / 8.0)));

        System.out.println("Testing all possible mark/resets");
        for (int markedLong = 0; markedLong < numLongs; markedLong++) {

            for (int longsToRead = 1; longsToRead <= numLongs - markedLong; longsToRead++) {

                long expectedValue = markedLong;
                in.mark(0);

                System.out.println("Mark set for long " + markedLong
                                   + " reading " + longsToRead + " longs");

                for (int i = 0; i < longsToRead; i++) {

                    long value = in.readLong();
                    if (value != expectedValue) {
                        throw new IOException("Expected " + expectedValue + " but read "
                                              + value + " at markedLong=" + markedLong
                                              + " longsToRead=" + longsToRead
                                              + " i=" + i);
                    }

                    expectedValue++;

                }

                in.reset();
            }

            in.readLong();
        }

        int numLongObjs = in.readInt();
        System.out.println("Reading " + numLongObjs + " Longs");
        Long expectedLong = new Long(TESTVALUE);

        if (in.markSupported()) {
            // The assumption is that while we use mark/reset internally,
            // we don't guarantee it for user level custom marshaling code.
            // This is because they could make the following assumption
            // (probably indirectly in a graph)
            //
            // in.mark(100);
            // Object obj1a = in.readObject();
            // in.reset();
            // Object obj1b = in.readObject();
            //
            // assert obj1a == obj1b
            //
            // If the stream says markSupported is true, then it should
            // allow the above assertion, so test it here:
            System.out.println("markSupported true");

            in.mark(100);
            Object obj1a = in.readObject();
            in.reset();
            in.mark(100);
            Object obj1b = in.readObject();
            in.reset();
            if (obj1a != obj1b)
                throw new IOException("markSupported returned true, but violated contract");
        }

        System.out.println("Testing Longs...");
        
        for (int markedLong = 0; markedLong < numLongs; markedLong++) {
            for (int longsToRead = 1; longsToRead <= numLongs - markedLong; longsToRead++) {
                in.mark(0);
                System.out.println("Mark set for Long " + markedLong
                                   + " reading " + longsToRead + " Longs");

                for (int i = 0; i < longsToRead; i++) {

                    Object objectRead = in.readObject();
                    
                    if (!expectedLong.equals(objectRead)) {
                        throw new IOException("Object " + objectRead + " doesn't match expected "
                                              + expectedLong + " at markedLong=" + markedLong
                                              + " longsToRead=" + longsToRead
                                              + " i=" + i);
                    }
                }

                in.reset();
            }

            in.readObject();
        }

        System.out.println("Now reading arrays of ints");
        int numArrays = in.readInt();
        int arraySize = in.readInt();

        System.out.println("Array size: " + arraySize);
        System.out.println("Number of arrays: " + numArrays);

        int expectedArray[] = new int[arraySize];
        for (int i = 0; i < expectedArray.length; i++)
            expectedArray[i] = i + 111;

        for (int markedArray = 0; markedArray < numArrays; markedArray++) {
            for (int arraysToRead = 1; arraysToRead <= numArrays - markedArray; arraysToRead++) {
                in.mark(0);

                System.out.println("Mark set for array " + markedArray
                                   + " reading " + arraysToRead + " arrays");

                for (int i = 0; i < arraysToRead; i++) {
                    Object objectRead = in.readObject();

                    try {
                        int arrayRead[] = (int[])objectRead;

                        for (int x = 0; x < arraySize; x++) {
                            if (arrayRead[x] != expectedArray[x]) {
                                throw new IOException("Expected and read arrays differ at index "
                                                      + x + ": " + arrayRead[x] + " != "
                                                      + expectedArray[x]);
                            }
                        }
                    } catch (ClassCastException cce) {
                        throw new IOException("Received non-array: " + objectRead.getClass().getName());
                    }
                }

                in.reset();
            }

            in.readObject();
        }

        System.out.println("Finished reading");
    }
}
