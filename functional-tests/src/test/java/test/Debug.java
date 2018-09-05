/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Enumeration;

public class Debug {
    
    private String name = null;
    
    public Debug (String name) {
        if (name == null) {
            name = "";
        } else {
            this.name = name + ": ";
        }
        setTop();
    }
    
    public final void log (String msg) {
        doLog(name,msg);
    }
    
    public final void log (byte[] data) {
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append((char)ASCII_HEX[(data[i] & 0xF0) >>> 4]);
            buf.append((char)ASCII_HEX[(data[i] & 0x0F)]);
        }
        
        doLog(name,buf.toString());
    }
    
    public final void logStack () {
        doLogStack(name);    
    }
    
    public final void logSystemProperties () {
        doLogSystemProperties(name);    
    }
    
    public final void logException (Throwable e) {
        doLogException(name,e);
    }
    
    
    private static synchronized void setTop() {
        lastName = "atsatt :-)";
    }
    
    private static synchronized void doLog (String name, String msg) {
        if (log == null) {
            initLog(name);
        } else {
            if (name != null && !name.equals(lastName)) {
                log.println("  ---");
            }
        }
        lastName = name;
        log.println(name + msg);    
    }

    private static String getStack(int trimSize) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(os);
        new Exception().printStackTrace(pw);
        pw.flush();
        String result = os.toString();
        if (trimSize > 0) {
            return result.substring(trimSize);
        }
        return result;
    }

    private static void doLogStack (String name) {
        doLog(name, getStack(0));
    }

    private static void doLogSystemProperties (String name) {
        StringBuffer buf = new StringBuffer();
        Properties props = System.getProperties();
        buf.append("System Properties:");
        buf.append(eol);
        for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) {
            String key = (String) e.nextElement();
            String value = (String) props.get(key);
            buf.append("   ");
            buf.append(key);
            buf.append(" = ");
            buf.append(value);
            buf.append(eol);
        }
        doLog(name, buf.toString());
    }
    
    private static void doLogException (String name, Throwable e) {
        doLog(name,"Caught " + e + eol + getStack(0));
    }
    
    public static void main (String[] args) {
        Debug d = new Debug("main");
        d.log("Testing...");
        d.logStack();
        d.logSystemProperties();
    }
    
    private static PrintWriter log = null;
    private static final String LOG_NAME = "DebugLog";
    private static final String LOG_EXT = ".txt";
    private static File rootDir = null;
    private static String lastName = null;
    private static String eol = (String)System.getProperty("line.separator");
    public static final byte ASCII_HEX[] =      {
        (byte)'0',
        (byte)'1',
        (byte)'2',
        (byte)'3',
        (byte)'4',
        (byte)'5',
        (byte)'6',
        (byte)'7',
        (byte)'8',
        (byte)'9',
        (byte)'A',
        (byte)'B',
        (byte)'C',
        (byte)'D',
        (byte)'E',
        (byte)'F',
    };
        
    private static synchronized void initLog(String name) {
        if (log == null) {
            rootDir = new File(System.getProperty("user.dir"));
            log = createLog(0);
            log.println("Log created by " +name+" at " + new java.util.Date().toString());
            log.println();
        }
    }
    
    private static PrintWriter createLog (int number) {
        String fileName = LOG_NAME + Integer.toString(number) + LOG_EXT;
        File file = new File(rootDir,fileName);
        
        // If file exists, assume another vm process owns it, and
        // bump the number...
        
        if (file.exists()) {
            return createLog(++number);
        }
        try {
            FileOutputStream os = new FileOutputStream(file);
            return new PrintWriter(os,true);
        } catch (IOException e) {
            System.err.println("Failed to create vmlog. Caught: "+e);
            return null;
        }
    }
}
