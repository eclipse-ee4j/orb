/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.travelmuse;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.transport.MessageData;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import corba.util.TransportManagerUtil;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.Assert ;

import corba.framework.TestngRunner;

/**
 *
 * @author Ken Cavanaugh
 * @author daraniramu
 */
public class Client {
    private Properties p = new Properties();
    private org.omg.CORBA.ORB orb;
    private com.sun.corba.ee.spi.orb.ORB myOrb;

    private static void msg( String msg ) {
        System.out.println( msg ) ;
    }

    public Client() {
    }

    @Configuration( beforeTest=true ) 
    public void setUp() {
        msg( "Configuring ORB" ) ;
        p.put("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl");
        p.put("com.sun.corba.ee.ORBDebug","cdr,streamFormatVersion,valueHandler");
        orb=  com.sun.corba.ee.spi.orb.ORB.init(new String[0],p);
        myOrb = (com.sun.corba.ee.spi.orb.ORB)orb ;
        myOrb.setDebugFlags( "cdr", "streamFormatVersion", "valueHandler" ) ;
    }

    @Configuration( afterTest=true ) 
    public void tearDown() {
        msg( "Cleaning up" ) ;
        orb.destroy();
        myOrb.destroy();
    }

   
    @Test
    public void travelMuse() {
        try {
            msg( "test case travelMuse" ) ;
            InputStream inputFile ;
            inputFile = new FileInputStream("../src/share/classes/corba/travelmuse/mtm.bin");
            ObjectInputStream in = new ObjectInputStream(inputFile);
            Object baResult=in.readObject();
            byte[][] baResult1=(byte[][])baResult;
            MessageData md = TransportManagerUtil.getMessageData(baResult1, myOrb);
            int bnum = 0 ;
            for (byte[] data : baResult1) {
                ByteBuffer bb = ByteBuffer.wrap( data ) ;
                bb.position( bb.capacity() ) ;
                ORBUtility.printBuffer( "Dumping buffer " + bnum++, bb, System.out ) ;
            }
            Object cdrstream1=javax.rmi.CORBA.Util.readAny( md.getStream());
        } catch (Exception exc) {
            exc.printStackTrace() ;
            Assert.fail( exc.toString() ) ;
        }
    }

    public static void main( String[] args ) {
        msg( "Test start: workding dir is " + System.getProperty( "user.dir" ) ) ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        try {
            runner.run() ;
        } catch (Exception exc ) {
            exc.printStackTrace() ;
        }
        runner.systemExit() ;
    }
}
