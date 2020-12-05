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

package com.sun.corba.ee.impl.naming.pcosnaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * @version     1.6, 99/10/07
 * @author      Rohit Garg
 * @since       JDK1.2
 */

public class ServantManagerImpl extends org.omg.CORBA.LocalObject implements ServantLocator
{
    private static final long serialVersionUID = 4028710359865748280L;

    private transient ORB orb;

    private transient NameService theNameService;

    private File logDir;

    private HashMap<String,NamingContextImpl> contexts;

    private transient CounterDB counterDb;

    private int counter;

    private final static String objKeyPrefix = "NC";

    ServantManagerImpl(ORB orb, File logDir, NameService aNameService)
    {
        this.logDir = logDir;
        this.orb    = orb;
        // initialize the counter database
        counterDb   = new CounterDB(logDir);
        contexts    = new HashMap<String,NamingContextImpl>();
        theNameService = aNameService;
    }


    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder cookie) throws ForwardRequest
    {

        String objKey = new String(oid);

        Servant servant = contexts.get(objKey);

        if (servant == null) {
            servant =  readInContext(objKey);
        }

        return servant;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation,
                           java.lang.Object cookie, Servant servant)
    {
        // nada
    }

    public NamingContextImpl readInContext(String objKey)
    {
        NamingContextImpl context = contexts.get(objKey);
        if( context != null ) {
            // Returning Context from Cache
            return context;
        }       

        File contextFile = new File(logDir, objKey);
        if (contextFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(contextFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                context = (NamingContextImpl) ois.readObject();
                context.setORB( orb );
                context.setServantManagerImpl( this );
                context.setRootNameService( theNameService );
                ois.close();
            } catch (Exception ex) {
                throw new RuntimeException( "No file for context " + objKey ) ;
            }
        }

        if (context != null) {
            contexts.put(objKey, context);
        }
        return context;
    }

    public NamingContextImpl addContext(String objKey, 
                                        NamingContextImpl context)
    {
        File contextFile =  new File(logDir, objKey);

        if (contextFile.exists()) {
            context = readInContext(objKey);
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(contextFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(context);
                oos.close();
            } catch (Exception ex) {
                throw new RuntimeException( "Error in adding new context " 
                    + objKey, ex) ;
            }
        }

        try {
            contexts.remove( objKey );
        } catch( Exception e) {
            throw new RuntimeException( "Error in removing old context "
                + objKey, e) ;
        }

        contexts.put(objKey, context);

        return context;
    }   

    public void updateContext( String objKey,
                                   NamingContextImpl context )
    {
        File contextFile =  new File(logDir, objKey);
        if (contextFile.exists()) {
                contextFile.delete( );
                contextFile =  new File(logDir, objKey);
        }
                
        try {
            FileOutputStream fos = new FileOutputStream(contextFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(context);
            oos.close();
        } catch (Exception ex) {
            throw new RuntimeException( "Could not update context " + objKey,
                ex ) ;
        }
    }

    public static String getRootObjectKey()
    {
        return objKeyPrefix + CounterDB.rootCounter;
    }

    public String getNewObjectKey()
    {
        return objKeyPrefix + counterDb.getNextCounter();
    }
}

class CounterDB {
    public static final int rootCounter = 0;

    private static final String counterFileName = "counter";

    private Integer counter;
    private transient File counterFile;

    CounterDB (File logDir)
    {
        counterFile = new File(logDir, counterFileName);
        if (!counterFile.exists()) {
            counter = Integer.valueOf(rootCounter);
            writeCounter();
        } else {
            readCounter();
        }
    }

    private void readCounter()
    {
        try {
            FileInputStream fis = new FileInputStream(counterFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            counter = (Integer) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            throw new RuntimeException( "Could not read counter",
                ex ) ;
        }
    }

    private void writeCounter()
    {
        try {
            counterFile.delete();
            FileOutputStream fos = new FileOutputStream(counterFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(counter);
            oos.flush();
            oos.close();

        } catch (Exception ex) {
            throw new RuntimeException( "Could not write counter",
                ex ) ;
        }
    }

    public synchronized int getNextCounter()
    {
        int counterVal = counter.intValue();
        counter = Integer.valueOf(++counterVal); 
        writeCounter();

        return counterVal;
    }
}
