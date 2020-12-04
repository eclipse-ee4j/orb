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

package test;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.naming.NamingException;
import java.util.HashSet;
import com.sun.corba.ee.impl.util.Utility;
import java.io.File;

/*
 * @test
 */
public abstract class RemoteTest extends Test {

    public static final String NAME_SERVER_HOST_FLAG = "-nameserverhost";
    public static final String NAME_SERVER_PORT_FLAG = "-nameserverport";
    public static final String SKIP_RMIC_FLAG = "-normic";
    public static final String JRMP_FLAG = "-jrmp";
    public static final String LOCAL_SERVANTS_FLAG = "-localservants";
    public static final String NO_LOCAL_STUBS_FLAG = "-nolocalstubs";

    // NOTE:    To debug the server-side, invoke the test with -localservants
    //          flag which will cause any servant to be started in the same
    //          process as the test.  You may also want to pass the -normic
    //          flag if you know that the stubs/ties are already present.

    protected boolean iiop = true;

    private static HashSet stubs = new HashSet();

    /**
     * Return an array of fully qualified remote interface class
     * names for which stubs need to be generated. Return empty
     * array if none.
     */
    protected String[] getRemoteInterfaceClasses () {
        return new String[0];   
    }

    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected abstract String[] getRemoteServantClasses ();

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    protected abstract void doTest (ServantContext context) throws Throwable;

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {

        String[] result = currentArgs;
        String dir = (String)getArgs().get(OUTPUT_DIRECTORY);

        if (dir != null) {
            String[] extra = {"-d",dir};
            result = append(result,extra);
        }

        if (getArgs().get(NO_LOCAL_STUBS_FLAG) != null) {
            String[] extra = {NO_LOCAL_STUBS_FLAG};
            result = append(result,extra);
        }

        return result;
    }

    protected String[] append (String[] source, String[] append) {

        String result[] = source;
        int appendLen = append.length;

        if (appendLen > 0) {
            int sourceLen = source.length;
            result = new String[appendLen + sourceLen];
            System.arraycopy(source,0,result,0,sourceLen);
            System.arraycopy(append,0,result,sourceLen,appendLen);
        }

        return result;
    }

    /**
     * Return true if stubs should only be generated once per process, false
     * if they should be generated prior to each call to doTest(). Default
     * is true.
     */
    protected boolean generateStubsOnlyOnce () {
        return iiop ? true : false;
    }

    /**
     * Return true if stubs should be generated in an external process.
     * Default is false.
     */
    protected boolean generateStubsExternally () {
        return false;
    }

    /**
     * Generate stubs/ties.
     * @param classes An array of fully qualified class names for which stubs/ties need
     * to be generated.
     * @param additionalRMICArgs An array of additional arguments  (i.e. after -iiop and
     * before classes)to rmic.
     * @param onlyOnce True if stubs should only be generated once per process.
     * @param iiop True if iiop stubs should be generated.
     * @param external True if compile should be done in external process.
     */
    protected void generateStubs (  String[] classes,
                                    String[] additionalRMICArgs,
                                    boolean onlyOnce,
                                    boolean iiop,
                                    boolean external) throws Exception 
    {
        try {
            dprint( "RemoteTest.generateStubs called" ) ;
            dprint( "\tclasses = " + Test.display(classes)) ;
            dprint( "\tadditionalRMICArgs = " + Test.display(additionalRMICArgs)) ;
            dprint( "\tonlyOnce = " + onlyOnce ) ;
            dprint( "\tiiop = " + iiop ) ;
            dprint( "\texternal = " + external ) ;

            Vector list = new Vector(classes.length);
            for (int i = 0; i < classes.length; i++) {
                String theClass = classes[i];
                // Do we need to compile this class?
                if (!stubs.contains(theClass) || !onlyOnce) {
                    dprint( "RemoteTest.generateStubs: adding to list " + theClass ) ;
                    list.addElement(theClass);
                }
            }

            int classCount = list.size();
            if (classCount > 0) {
                String[] compileEm = new String[classCount];
                list.copyInto(compileEm);

                String genArg = null;
                if (iiop) {
                    genArg = "-iiop";
                } else {
                    genArg = "-vcompat";
                }

                // Util.rmic(null,null,null,external); // _REVSISIT_ Remove! Bug in 1.2b4.1
                Util.rmic(genArg,additionalRMICArgs,compileEm,external);

                if (onlyOnce) {
                    for (int i = 0; i < classCount; i++) {
                        stubs.add(compileEm[i]);
                    }
                }
            }
        } finally {
            dprint( "RemoteTest.generateStubs exiting" ) ;
        }
    }


    /**
     * Return the servant context. This implementation uses the
     * -nameserverhost and -nameserverport arguments if present, or
     * gets the default context if not.
     */
    protected ServantContext getServantContext () throws Exception {

        ServantContext result = null;

        Hashtable flags = getArgs();
        String host = (String)flags.get(NAME_SERVER_HOST_FLAG);
        String portString = (String)flags.get(NAME_SERVER_PORT_FLAG);

        Properties sysProps = System.getProperties();
        if (flags.get(LOCAL_SERVANTS_FLAG) != null) {
            if (verbose) System.out.print("(Local Servants)");
            sysProps.put(ServantContext.LOCAL_SERVANTS_FLAG,"true");
        } else {
            if (verbose) System.out.print("(Remote Servants)");
            sysProps.remove(ServantContext.LOCAL_SERVANTS_FLAG);
        }

        if (host == null && portString == null) {
            result = ServantContext.getDefaultContext(iiop);
        } else {
            int port = Util.getNameServerPort();

            if (portString != null) {
                port = Integer.parseInt(portString);
            }
            String orbDebugFlags = (String)flags.get( ORB_DEBUG ) ;
            result = ServantContext.getContext(host,port,true,iiop,orbDebugFlags);
        }

        dprint( "getServantContext returns " + result ) ;
        return result;
    }

    public void setup () {
        try {
            dprint( "RemoteTest.setup called" ) ;
            iiop = !getArgs().containsKey(JRMP_FLAG);

            if (!getArgs().containsKey(SKIP_RMIC_FLAG)) {
                if (verbose) {
                    if (iiop) {
                        System.out.print("(IIOP) ");
                    } else {
                        System.out.print("(JRMP) ");
                    }
                }

                // Get all the servant classes...
                String[] tieClasses = getRemoteServantClasses();
                String[] servantManager = {ServantContext.SERVANT_MANAGER_CLASS};
                tieClasses = append(tieClasses,servantManager);
                
                // Delete ties if we are doing jrmp...
                if (!iiop) {                
                    String dir = (String)getArgs().get(OUTPUT_DIRECTORY);
                    if (dir != null) {
                        File root = new File(dir);
                        for (int i = 0; i < tieClasses.length; i++) {
                            dprint( "RemoteTest.setup: tieClass = " + tieClasses[i] ) ;
                            String tieClass = Utility.tieName(tieClasses[i]);
                            tieClass = tieClass.replace('.',File.separatorChar);
                            tieClass += ".class";
                            File file = new File(root,tieClass);
                            dprint( "RemoteTest.setup: file = " + file ) ;
                            if (file.exists()) {
                                dprint( "RemoteTest.setup: deleting file" ) ;
                                file.delete();
                            }
                        }
                    }
                }

                // Generate needed stubs/ties/skels...
                String[] rmicClasses = append(tieClasses,getRemoteInterfaceClasses());
                String[] additionalArgs = new String[0];
                additionalArgs = getAdditionalRMICArgs(additionalArgs);
                boolean onlyOnce = generateStubsOnlyOnce();
                boolean external = generateStubsExternally();
                generateStubs(rmicClasses,additionalArgs,onlyOnce,iiop,external);
            }
        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            throw new Error(out.toString());
        } finally {
            dprint( "RemoteTest.setup exiting" ) ;
        }
    }

    public void run () {
        dprint( "run called" ) ;
        ServantContext theContext = null;

        try {
            // Do the test...
            theContext = getServantContext();

            dprint( "doTest called" ) ;
            doTest(theContext);
        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            status = new Error("Caught " + out.toString());
        }

        // Make sure we destroy the context...

        if (theContext != null) {
            theContext.destroy();
        }
    }
}
