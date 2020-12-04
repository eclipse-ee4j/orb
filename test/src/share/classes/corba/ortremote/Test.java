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

package corba.ortremote ;

import com.sun.corba.ee.spi.oa.ObjectAdapter ;
import java.rmi.RemoteException ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import java.util.Properties ;
import org.omg.CORBA.ORB ;
import org.omg.CORBA.Policy ;
import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Util ;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.omg.CORBA.LocalObject ;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantRetentionPolicyValue;

public class Test
{
    public static ORTEcho makeServant( POA poa ) throws RemoteException 
    {
        return new ORTEchoImpl( poa ) ;
    }
   
    static class CounterServantLocator extends LocalObject implements ServantLocator
    {
        public Servant preinvoke( byte[] oid, POA poa, String operation,
            CookieHolder cookie ) throws ForwardRequest
        {
            ORTEcho impl = null ;

            try {
                impl = makeServant( poa ) ;
            } catch (RemoteException rexc) {
                RuntimeException exc = new RuntimeException( 
                    "Error in creating servant" ) ;
                exc.initCause( rexc ) ;
                throw exc ;
            }

            Servant servant = (Servant)Util.getTie( impl ) ;
            return servant ;
        }

        public void postinvoke( byte[] oid, POA poa, String operation,
            java.lang.Object cookie, Servant servant ) 
        {
            // NOP
        }
    }

    public static ServantLocator makeServantLocator()
    {
        return new CounterServantLocator() ;
    }

    public static ORB makeORB() 
    {
        Properties props = null ;
        String[] args = null ;

        return ORB.init( args, props ) ;
    }

    public static POA makePOA( ORB orb ) throws AdapterAlreadyExists,
        AdapterInactive, WrongPolicy, InvalidName, InvalidPolicy
    {
        POA rootPOA = (POA)orb.resolve_initial_references( "RootPOA" ) ;
        Policy[] tpolicy = new Policy[] {
            rootPOA.create_lifespan_policy(
                LifespanPolicyValue.TRANSIENT),
            rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
            rootPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN) } ;

        POA tpoa = rootPOA.create_POA("NonRetainPOA", null, tpolicy);
        tpoa.the_POAManager().activate();

        ServantLocator csl = makeServantLocator();
        tpoa.set_servant_manager(csl);
        return tpoa;
    }

    public static void main( String[] args )
    {
        TestSession session = new TestSession( System.out, Test.class ) ;

        ORB clientORB = makeORB() ;
        ORB serverORB = makeORB() ;
        POA poa = null ;

        try {
            poa = makePOA( serverORB ) ;
        } catch (Throwable thr ) {
            session.testAbort( "Error in makePOA", thr ) ;
        }

        byte[] id = "FOO".getBytes() ;

        org.omg.CORBA.Object serverObjref = poa.create_reference_with_id( id,
            "IDL:omg.org/Object:1.0" ) ;
        
        String serverObjrefStr = serverORB.object_to_string( serverObjref ) ;

        org.omg.CORBA.Object clientObjref = clientORB.string_to_object( serverObjrefStr ) ;

        final ORTEcho testRef = (ORTEcho)PortableRemoteObject.narrow( clientObjref, ORTEcho.class ) ;
        
        ObjectAdapter oa = (ObjectAdapter)poa ;
        ObjectReferenceFactory orf = oa.getCurrentFactory() ;
        ObjectReferenceTemplate ort = oa.getAdapterTemplate() ;

        session.start( "ORT marshalling test over RMI-IIOP" ) ;

        session.testForPass( "ObjectReferenceFactory",
            new NullaryFunction<Object>() {
                public Object evaluate() {
                    try {
                        return testRef.getORF() ;
                    } catch (Throwable thr) {
                        RuntimeException err = new RuntimeException(
                            "Unexpected exception in getORF()" ) ;
                        err.initCause( thr ) ;
                        throw err ;
                    }
                }
            },
            orf ) ;
        
        session.testForPass( "ObjectReferenceTemplate",
            new NullaryFunction<Object>() {
                public Object evaluate() {
                    try {
                        return testRef.getORT() ;
                    } catch (Throwable thr) {
                        RuntimeException err = new RuntimeException(
                            "Unexpected exception in getORT()" ) ;
                        err.initCause( thr ) ;
                        throw err ;
                    }
                }
            },
            ort ) ;

        clientORB.destroy() ;
        serverORB.destroy() ;
        session.end() ;
    }
}
