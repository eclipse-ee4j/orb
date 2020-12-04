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

package javax.rmi;

import test.Test;
import test.WebServer;
import java.io.File;
import com.sun.corba.ee.impl.util.JDKBridge;
import javax.rmi.CORBA.Util;
import java.rmi.server.RMIClassLoader;
import org.glassfish.pfl.test.JUnitReportHelper;

public class ClassLoadTest extends Test {
    private static int SUCCEED = 0;
    private static int FAIL_NOT_FOUND = 1;
    private static int FAIL_ILLEGAL_ARG = 2;
    private static int FAIL_NULL_PTR = 3;
    private static int FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11 = 4;
    
    private static String[] failure = {
        "SUCCEED",
        "ClassNotFoundException",
        "IllegalArgumentException",
        "NullPointerException",
        "ClassNotFoundException on 1.2 or IllegalArgumentException on 1.1",
    };
    
    private static int LOCAL = 4;
    private static int REMOTE = 5;
    private static boolean isPre12VM = false;
    private static boolean is12VM = true;
    
    private JUnitReportHelper helper = new JUnitReportHelper(
        this.getClass().getName() ) ;

    private String makeTestName( String className ) {
        if (className == null)
            return "null" ;

        StringBuilder sb = new StringBuilder() ;
        int numArrays = 0 ;
        while ((numArrays < className.length()) && className.charAt(numArrays) == '[') {
            numArrays++ ;
        }
            
        for (int a=numArrays; a<className.length(); a++ ) {
            char ch = className.charAt( a ) ;
            if (Character.isJavaIdentifierPart( ch ))
                sb.append( ch ) ;
            else
                sb.append( '_' ) ;
        } 


        for (int ctr2=0; ctr2<numArrays; ctr2++ )
            sb.append( "_array" ) ;

        return sb.toString() ;
    }

    private void doTests () {
        try {
            // Make sure we fail with null and empty names...

            loadClass(null,LOCAL,FAIL_NULL_PTR);
            loadClass("",LOCAL,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);
            
            // Make sure we can load an array of primitive class...
            
            loadClass("[[B",LOCAL,SUCCEED);          
            loadClass("[[[[Z",LOCAL,SUCCEED);          
            
            // Make sure we cannot load an array of primitive class when
            // the name is bogus...
            
            loadClass("[[Q",LOCAL,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);
            
            // Make sure we can load a system class array...
            
            loadClass("[Ljava.applet.AppletStub;",LOCAL,SUCCEED);            
            
            // Make sure we can load a system class...
            
            loadClass("java.applet.Applet",LOCAL,SUCCEED);            
            
            // Make sure we can load a system class array...
            
            loadClass("[Ljava.applet.AppletStub;",LOCAL,SUCCEED);            
            
            // Make sure we can load an application class...
            
            loadClass("rmic.HelloTest",LOCAL,SUCCEED);            
            
            // Make sure we can load an application class array...
            
            loadClass("[[[Lrmic.MangleMethods;",LOCAL,SUCCEED);            
            
            // Make sure we can load a remote class...
            
            loadClass("javax.rmi.download.values.DownloadA",REMOTE,SUCCEED);            
            
            // Make sure we cannot load a class array when the
            // name is formatted wrong...
            
            loadClass("[[Ljavax.rmi.download.values.DownloadB",REMOTE,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);    
            loadClass("[[Xjavax.rmi.download.values.DownloadB;",REMOTE,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);    
            loadClass("[[",REMOTE,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);    
            loadClass("[[[;",REMOTE,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);    
            loadClass("[",REMOTE,FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11);    
            
            // Make sure we can load a remote class array...
            
            loadClass("[[Ljavax.rmi.download.values.DownloadB;",REMOTE,SUCCEED);    
            
            // Make sure we can load a local inner class...
            
            loadClass("javax.rmi.ClassLoadTest$InnerA",LOCAL,SUCCEED);    
            
            // Make sure we can load a local inner class array...
            
            loadClass("[Ljavax.rmi.ClassLoadTest$InnerB;",LOCAL,SUCCEED);    
            
            // Make sure we can load a remote inner class...
            
            loadClass("javax.rmi.download.values.DownloadA$Inner",REMOTE,SUCCEED);    
            
            // Make sure we can load a remote inner class array...
            
            loadClass("[Ljavax.rmi.download.values.DownloadB$Inner;",REMOTE,SUCCEED);
            
            // Now test that our special JDKClassLoader.loadClass() actually walks
            // the stack as expected, and that Class.forName() does not...
            
    /*
            try {
                
                // Make sure that Class.forName() fails...
                
                boolean failed = false;
                try {
                    Class.forName("javax.rmi.download.values.DownloadB$Nested");
                } catch (ClassNotFoundException e) {
                    failed = true;
                }
                if (!failed) {
                    throw new Error ("Class.forName() loaded DownloadB$Nested!");
                }
                
                // Now, get an instance of DownLoadB...
                
                Class clz = Util.loadClass("javax.rmi.download.values.DownloadB",
                                           remoteCodebase,null);
                Object downloadB = clz.newInstance();
                
                // Call it's toString method, which tries to load DownloadB$Nested
                // using JDKBridge.loadClass(name,null).
                
                String result = downloadB.toString();
                if (!result.equals("Loaded DownloadB.Nested")) {
                    if (result.startsWith("DownLoadB.toString() failed to load")) {
                        
                        System.out.println("WARNING: On 1.2, DownLoadB.Nested failed to load.");

                        // _REVISIT_ This should not fail when in core, but for
                        // now, it must.  The problem is that JDKBridge is loaded
                        // by the extensions loader rather than the bootstrap loader, so
                        // the stack walking code finds the extension loader rather than
                        // the remote loader...
                        
                    } else {
                        throw new Error(result);
                    }
                }            
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error(e.toString());
            }
    */
        } finally {
            helper.done() ;
        }
    }
    
    private Class loadClass (String className, int where, int shouldFail) {
        helper.start( makeTestName( className ) ) ;
        Class result = null;

        try {
            result = Util.loadClass(className,remoteCodebase,null);
        } catch (ClassNotFoundException e) {
            if (shouldFail == FAIL_NOT_FOUND ||
                (shouldFail == FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11 &&
                 is12VM)) {
                helper.pass() ;
                return null;
            }
            e.printStackTrace();
            String msg = className+": Expected "+failure[shouldFail]+", got "+e.toString();
            helper.fail( msg ) ;
            throw new Error( msg ) ;
        } catch (IllegalArgumentException e) {
            if (shouldFail == FAIL_ILLEGAL_ARG ||
                (shouldFail == FAIL_NOT_FOUND12_OR_ILLEGAL_ARG11 &&
                 isPre12VM)) {
                helper.pass() ;
                return null;
            }
            e.printStackTrace();
            String msg = className+": Expected "+failure[shouldFail]+", got "+e.toString();
            helper.fail( msg ) ;
            throw new Error( msg ) ;
        } catch (NullPointerException e) {
            if (shouldFail == FAIL_NULL_PTR)  {
                helper.pass() ;
                return null;
            }
            e.printStackTrace();
            String msg = className+": Expected "+failure[shouldFail]+", got "+e.toString();
            helper.fail( msg ) ;
            throw new Error(msg) ;
        }
        
        if (result == null) {
            String msg = "Got null from Util.loadClass("+className+")!";
            helper.fail( msg ) ;
            throw new Error(msg) ;
        } else {
            if (shouldFail != SUCCEED) {
                String msg ="Loaded "+className+" but expected it to fail";
                helper.fail( msg ) ;
                throw new Error(msg) ;
            }
        }  
        
        // Make sure we got the name we expected...
        
        if (!result.getName().equals(className)) {
            String msg = "Returned class name '"+result.getName()+"' != expected '"+className+"'";
            helper.fail( msg ) ;
            throw new Error( msg ) ;
        }
        
        // Make sure we get the codebase we expected...
        
        if (where == REMOTE) {
            if (result.getClassLoader() == null) {
                String msg = "Got null class loader for "+result;    
                helper.fail( msg ) ;
                throw new Error(msg) ;
            }
            
            String codebase = RMIClassLoader.getClassAnnotation(result);
            
            if (codebase == null) {
                // On 1.2, multi-dimensional class arrays will return
                // null. Sigh. _REVISIT_ this special case should be
                // removed when the 1.2 bug in RMIClassLoader.getClassAnnotation()
                // is fixed.
                   
                if (is12VM && className.charAt(0) == '[') {
                    int last = className.lastIndexOf('[');
                    if (last > 0 && className.charAt(last+1) == 'L') {
                        helper.pass() ;
                        return null;   
                    }
                }
            
                String msg = "Got null codebase from JDKBridge.getCodebase("+result+")";
                helper.fail( msg ) ;
                throw new Error(msg) ;
            }

            if (!codebase.equals(remoteCodebase)) {
                String msg = "getCodebase('"+result+"') returned '"+codebase+"' != expected '"+remoteCodebase+"'";    
                helper.fail( msg ) ;
                throw new Error(msg) ;
            }
        }
        
        helper.pass() ;
        return result;
    }
    
    private WebServer webServer = null;
    private String remoteCodebase = null;
    
    public void setup () {        
        try {
            
            // Make sure WE know the codebase...
            
            remoteCodebase = System.getProperty("java.rmi.server.codebase");
            if (remoteCodebase == null) {
                throw new Error("No codebase set");
            }

            // Pull the port # out of the codebase...
            
            int port = 9090; // Default;
            
            int host = remoteCodebase.indexOf("//");
            if (host > 0) {
                host += 2;
                int colon = remoteCodebase.indexOf(":",host);
                if (colon > 0) {
                    int length = remoteCodebase.length();
                    int slash = remoteCodebase.indexOf('/',colon);
                    if (slash > 0) {
                        length = slash;   
                    }
                    String portStr = remoteCodebase.substring(colon+1,length);
                    port = Integer.parseInt(portStr);
                }
            }

            // Make sure that JDKBridge does not know the codebase...
            
            JDKBridge.setLocalCodebase(null);
            if (JDKBridge.getLocalCodebase() != null) {
                throw new Error("Could not clear local codebase.");
            }
            
            // Start the webserver pointing at the correct doc root...
            
            String rootPath = System.getProperty("http.server.root.directory");
            if (rootPath == null) {
                throw new Error("http.server.root.directory not set");
            }
            File rootDir = new File(rootPath);
            webServer = new WebServer(port,rootDir,1);
            webServer.start();
            if (!webServer.waitTillReady()){
                throw new Error("WebServer died");
            }
            
        } catch (Exception e) {
            throw new Error ("setup() failed, caught "+e);
        }
    }
    
    public void run() {
        try {
            doTests();
        } finally {
            if (webServer != null) {
                webServer.quit();
            }
        }
    }

    public class InnerA {}
    public interface InnerB {}
}
