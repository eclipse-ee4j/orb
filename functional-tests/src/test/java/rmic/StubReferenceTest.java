/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package rmic;

import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;
import org.glassfish.pfl.test.JUnitReportHelper;


/*
 * @test
 */
public class StubReferenceTest extends StubTest {

    public static final String[] ADDITIONAL_ARGS = {"-alwaysGenerate","-keep"};

    public static final String CLASS_LIST_FILE = ".classlist";
    public static final String FILE_EXT =  ".java";
    public static final String FILE_REF_EXT =  ".javaref";

    private String[] shouldCompileClasses = null;
    private Target[] targets = null;

    /**
     * Return an array of fully qualified class names for which generation
     * should occur. Return empty array if none.
     */
    protected String[] getGenerationClasses () throws Throwable {
        initClasses();
        return shouldCompileClasses;
    }

    /**
     * Perform the test.
     */
    protected void doTest () throws Throwable {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() ) ;
        
        try {
            // Those classes that should compile have been compiled. Check to
            // ensure that they match there reference files...
            
            for (int i = 0; i < targets.length; i++) {
                if (targets[i].shouldCompile) {    
                    helper.start( "test_" + i ) ;
                    String[] output = targets[i].output;
                    
                    try {
                        for (int j = 0; j < output.length; j++) {
                            compareResources(output[j],FILE_EXT,FILE_REF_EXT);
                        }
                    
                        helper.pass() ;
                    } catch (Throwable thr) {
                        helper.fail( thr ) ;
                        throw thr ;
                    }
                }
            }
            
            // Now ensure that those classes which should NOT compile, do in
            // fact fail as expected...
            
            for (int i = 0; i < targets.length; i++) {
                if (!targets[i].shouldCompile) {
                    helper.start( "test_" + i ) ;
                    Target target = targets[i];
                    boolean failed = false;
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        generate(target.inputClass,out);               
                    } catch (Exception e) {
                        failed = true;
                    }
                    
                    if (failed) {
                        // Make sure that the error output contains all the errorStrings in the
                        // output array...
                        
                        String[] errors = target.output;
                        String errorText = out.toString();
                        
                        for (int j = 1; j < errors.length; j++) {
                            if (errorText.indexOf(errors[j]) < 0) {
                                String msg = target.inputClass 
                                    + " error message did not contain '" 
                                    + errors[j] + "'. Got " + errorText ;

                                helper.fail( msg ) ;
                                throw new Error( msg ) ;
                            }
                        }
                        helper.pass() ;
                    } else {
                        String msg = target.inputClass + " should FAIL to compile but did not." ;
                        helper.fail( msg ) ;
                        throw new Error( msg ) ;
                    }
                }
            }
        } finally {
            helper.done() ;
        }
    }

    /**
     * Append additional (i.e. after -idl and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        return super.getAdditionalRMICArgs(ADDITIONAL_ARGS);
    }

    private synchronized void initClasses () throws Throwable {
        
        if (shouldCompileClasses == null) {
            String[] array = getResourceAsArray(getClass().getName(),CLASS_LIST_FILE,"#");
            int totalCount = array.length;
            targets = new Target[totalCount];
            int shouldCompileCount = 0;
            
            // Parse input into our Target array, keeping count of those that should
            // compile...
            
            for (int i = 0; i < totalCount; i++) {
                targets[i] = new Target(array[i]);
                
                if (targets[i].shouldCompile) {
                    shouldCompileCount++;
                }
            }
            
            int shouldNotCompileCount = totalCount - shouldCompileCount;
            
            // Allocate our array...
            
            shouldCompileClasses = new String[shouldCompileCount];

            // Fill them up...
            
            int shouldOffset = 0;
            for (int i = 0; i < totalCount; i++) {
                if (targets[i].shouldCompile) {
                    shouldCompileClasses[shouldOffset++] = targets[i].inputClass;
                }
            }
        }
    }
}

class Target {
    public String inputClass = null;
    public String[] output = null;
    public boolean shouldCompile = true;
    
    public Target (String entry) {
    
        // Parse the <inputclass>=<outputclass>[,<outputclass>] format...
   
        StringTokenizer s = new StringTokenizer(entry,"=,");
        int count = s.countTokens() - 1;
        output = new String[count];
        inputClass = s.nextToken().trim();
        int offset = 0;
        while (s.hasMoreTokens()) {
            output[offset++] = s.nextToken().trim();
        }
        
        // Set the shouldCompile flag if needed...
        
        if (output[0].equalsIgnoreCase("ERROR")) {
            shouldCompile = false;
        }
    }
}
