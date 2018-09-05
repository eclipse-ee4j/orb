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

package com.sun.corba.ee.impl.javax.rmi.CORBA; // Util (sed marker, don't remove!)

import com.sun.corba.ee.impl.io.SharedSecrets;
import com.sun.corba.ee.impl.misc.ClassInfoCache;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.util.JDKBridge;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.copyobject.CopierManager;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.spi.logging.UtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import org.glassfish.pfl.basic.logex.OperationTracer;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopier;
import org.glassfish.pfl.dynamic.copyobject.spi.ReflectiveCopyException;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.UnknownException;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.ValueHandler;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.rmi.UnexpectedException;
import java.rmi.server.RMIClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.Object;

// These classes only exist in Java SE 6 and later.
// This class must be able to function with non-Sun ORBs.
// This means that any of the following com.sun.corba classes
// must only occur in contexts that also handle the non-Sun case.

/**
 * Provides utility methods that can be used by stubs and ties to
 * perform common operations.
 */
public class Util implements javax.rmi.CORBA.UtilDelegate 
{
    // Runs as long as there are exportedServants
    private static KeepAlive keepAlive = null;

    // Maps targets to ties.
    private static final IdentityHashMap<Remote,Tie> exportedServants = new IdentityHashMap<>();

    private static ValueHandler valueHandlerSingleton;          

    private static final UtilSystemException utilWrapper = UtilSystemException.self ;

    private static Util instance = null;

    // XXX Would like to have a WeakConcurrentHashMap here to reduce contention,
    // but that is only available with Google collections at present.
    private WeakHashMap<java.lang.Class<?>, String> annotationMap = new WeakHashMap<>();

    private static final java.lang.Object annotObj = new java.lang.Object();

    static {
        // Note: there uses to be code here to use the JDK value handler for embedded
        // web logic.  I removed it after 3.1.0-b008.
        valueHandlerSingleton = SharedSecrets.getJavaCorbaAccess().newValueHandlerImpl();
    }

    // This constructor MUST be public, or Util.createDelegateIfSpecified will fail!
    // It is only called from getInstance and javax.rmi.CORBA.Util.createDelegateIfSpecified (in
    // the newInstance call).
    //
    // Note that it is possible to construct more than one instance of Util.  This can happen
    // if we both do something to javax.rmi.CORBA.Util that causes the delegate to be initialized
    // AND we call getInstance inside the ORB.  This MAY cause problems with ORB shutdown and
    // the unexport.  I tried to modify this class to guarantee that only one copy gets created.
    // That resulted in Issue 3080 filed against GlassFish v2 b49, where an ORB client called
    // new InitialContext, which initialized the se version of the UtilDelegate, which consequently caused
    // every call to getInstance to return null.  BAD BUG!  As it is currently (5/30/07) very 
    // close to the end of the development cycle for GFv2, I am reverting this change back to the
    // previous version, which has worked well in GF.
    public Util() { }

    public static synchronized Util getInstance() {
        if (instance == null) {
            instance = new Util() ;
        }

        return instance ;
    }

    // Used by TOAFactory.shutdown to unexport all targets for this
    // particular ORB.  This happens during ORB shutdown.
    public void unregisterTargetsForORB(org.omg.CORBA.ORB orb) 
    {
        // Copy exportedServants set we don't get a 
        // ConcurrentModificationException.
        Map<Remote,Tie> copy = new IdentityHashMap<>(exportedServants) ;

        for (Remote key : copy.keySet() ) {
            Remote target = key instanceof Tie ? ((Tie)key).getTarget() : key ;

            // Bug 4476347: BAD_OPERATION is thrown if the ties delegate isn't set.
            // We can ignore this because it means the tie is not connected to an ORB.
            try {
                if (orb == getTie(target).orb()) {
                    try {
                        unexportObject(target);
                    } catch( java.rmi.NoSuchObjectException ex ) {
                        // We neglect this exception if at all if it is
                        // raised. It is not harmful.
                    }
                }
            } catch (SystemException se) {
                utilWrapper.handleSystemException(se);
            }
        }
    }

   /**
     * Maps a SystemException to a RemoteException.
     * @param ex the SystemException to map.
     * @return the mapped exception.
     */
    public RemoteException mapSystemException(SystemException ex) 
    {
        if (ex instanceof UnknownException) {
            Throwable orig = ((UnknownException)ex).originalEx;
            if (orig instanceof Error) {
                return new ServerError("Error occurred in server thread",(Error)orig);
            } else if (orig instanceof RemoteException) {
                return new ServerException("RemoteException occurred in server thread",
                    (Exception)orig);
            } else if (orig instanceof RuntimeException) {
                throw (RuntimeException) orig;
            }
        }

        // Build the message string...
        String name = ex.getClass().getName();
        String corbaName = name.substring(name.lastIndexOf('.')+1);
        String status;
        switch (ex.completed.value()) {
            case CompletionStatus._COMPLETED_YES:
                status = "Yes";
                break;
            case CompletionStatus._COMPLETED_NO:
                status = "No";
                break;
            case CompletionStatus._COMPLETED_MAYBE:
            default:
                status = "Maybe";
                break;
        }
        
        String message = "CORBA " + corbaName + " " + ex.minor + " " + status;

        // Now map to the correct RemoteException type...
        if (ex instanceof COMM_FAILURE) {
            return new MarshalException(message, ex);
        } else if (ex instanceof INV_OBJREF) {
            RemoteException newEx = new NoSuchObjectException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof NO_PERMISSION) {
            return new AccessException(message, ex);
        } else if (ex instanceof MARSHAL) {
            return new MarshalException(message, ex);
        } else if (ex instanceof OBJECT_NOT_EXIST) {
            RemoteException newEx = new NoSuchObjectException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof TRANSACTION_REQUIRED) {
            RemoteException newEx = new TransactionRequiredException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof TRANSACTION_ROLLEDBACK) {
            RemoteException newEx = new TransactionRolledbackException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof INVALID_TRANSACTION) {
            RemoteException newEx = new InvalidTransactionException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof BAD_PARAM) {
            Exception inner = ex;

            // Pre-Merlin Sun ORBs used the incorrect minor code for
            // this case.  See Java to IDL ptc-00-01-08 1.4.8.
            if (ex.minor == ORBConstants.LEGACY_SUN_NOT_SERIALIZABLE ||
                ex.minor == OMGSystemException.NOT_SERIALIZABLE) {

                if (ex.getMessage() != null) {
                    inner = new NotSerializableException(ex.getMessage());
                } else {
                    inner = new NotSerializableException();
                }

                inner.initCause( ex ) ;
            }

            return new MarshalException(message,inner);
        }

        // Just map to a generic RemoteException...
        return new RemoteException(message, ex);
    }

    /**
     * Writes any java.lang.Object as a CORBA any.
     * @param out the stream in which to write the any.
     * @param obj the object to write as an any.
     */
    public void writeAny( org.omg.CORBA.portable.OutputStream out, 
                         java.lang.Object obj) 
    {
        org.omg.CORBA.ORB orb = out.orb();

        // Make sure we have a connected object...
        java.lang.Object newObj = Utility.autoConnect(obj, orb, false);

        // In JSG (Java Serialization with GIOP) we avoid using Any and
        // Typecode implementations, as they are not necessary for
        // serialization using Java. Further, the current Typecode IO
        // stream objects are error-prone, and they tend to use encoding
        // specific details such as indirections, etc. 
        if (ORBUtility.getEncodingVersion() != ORBConstants.CDR_ENC_VERSION) {
            ((org.omg.CORBA_2_3.portable.OutputStream)out).
                                        write_abstract_interface(newObj);
            return;
        }

        // Create Any
        Any any = orb.create_any();

        if (newObj instanceof org.omg.CORBA.Object) {
            any.insert_Object((org.omg.CORBA.Object)newObj);
        } else {
            if (newObj == null) {
                // Handle the null case, including backwards
                // compatibility issues
                any.insert_Value(null, createTypeCodeForNull(orb));
            } else {
                if (newObj instanceof Serializable) {
                    // If they're our Any and ORB implementations,
                    // we may want to do type code related versioning.
                    TypeCode tc = createTypeCode((Serializable)newObj, any, orb);
                    if (tc == null) {
                        any.insert_Value((Serializable) newObj);
                    } else {
                        any.insert_Value((Serializable) newObj, tc);
                    }
                } else if (newObj instanceof Remote) {
                    ORBUtility.throwNotSerializableForCorba(newObj.getClass().getName());
                } else {
                    ORBUtility.throwNotSerializableForCorba(newObj.getClass().getName());
                }
            }
        }

        out.write_any(any);
    }

    /**
     * When using our own ORB and Any implementations, we need to get
     * the ORB version and create the type code appropriately.  This is
     * to overcome a bug in which the JDK 1.3.x ORBs used a tk_char
     * rather than a tk_wchar to describe a Java char field.
     *
     * This only works in RMI-IIOP with Util.writeAny since we actually
     * know what ORB and stream we're writing with when we insert
     * the value.
     *
     * Returns null if it wasn't possible to create the TypeCode (means
     * it wasn't our ORB or Any implementation).
     *
     * This does not handle null objs.
     */
    private TypeCode createTypeCode(Serializable obj,
                                    org.omg.CORBA.Any any,
                                    org.omg.CORBA.ORB orb) {

        if (any instanceof com.sun.corba.ee.impl.corba.AnyImpl &&
            orb instanceof ORB) {

            com.sun.corba.ee.impl.corba.AnyImpl anyImpl
                = (com.sun.corba.ee.impl.corba.AnyImpl)any;

            ORB ourORB = (ORB)orb;

            return anyImpl.createTypeCodeForClass(obj.getClass(), ourORB);
        } else {
            return null;
        }
    }


    /**
     * This is used to create the TypeCode for a null reference.
     * It also handles backwards compatibility with JDK 1.3.x.
     *
     * This method will not return null.
     */
    private TypeCode createTypeCodeForNull(org.omg.CORBA.ORB orb) 
    {
        if (orb instanceof ORB) {

            ORB ourORB = (ORB)orb;

            // Preserve backwards compatibility with Kestrel and Ladybird
            // by not fully implementing interop issue resolution 3857,
            // and returning a null TypeCode with a tk_value TCKind.
            // If we're not talking to Kestrel or Ladybird, fall through
            // to the abstract interface case (also used for foreign ORBs).
            if (!ORBVersionFactory.getFOREIGN().equals(ourORB.getORBVersion()) &&
                ORBVersionFactory.getNEWER().compareTo(ourORB.getORBVersion()) > 0) {

                return orb.get_primitive_tc(TCKind.tk_value);
            }
        }

        // Use tk_abstract_interface as detailed in the resolution

        // REVISIT: Define this in IDL and get the ID in generated code
        String abstractBaseID = "IDL:omg.org/CORBA/AbstractBase:1.0";

        return orb.create_abstract_interface_tc(abstractBaseID, "");
    }

    /**
     * Reads a java.lang.Object as a CORBA any.
     * @param in the stream from which to read the any.
     * @return the object read from the stream.
     */
    public Object readAny(InputStream in) 
    {
        // In JSG (Java Serialization with GIOP) we avoid using Any and
        // Typecode implementations, as they are not necessary for
        // serialization using Java. Further, the current Typecode IO
        // stream objects are error-prone, and they tend to use encoding
        // specific details such as indirections, etc. 
        if (ORBUtility.getEncodingVersion() != ORBConstants.CDR_ENC_VERSION) {
            return ((org.omg.CORBA_2_3.portable.InputStream)in).
                                          read_abstract_interface();
        }

        Any any = in.read_any();
        if ( any.type().kind().value() == TCKind._tk_objref ) {
            return any.extract_Object();
        } else {
            return any.extract_Value();
        }
    }

    /**
     * Writes a java.lang.Object as a CORBA Object. If <code>obj</code> is
     * an exported RMI-IIOP server object, the tie is found
     * and wired to <code>obj</code>, then written to <code>out.write_Object(org.omg.CORBA.Object)</code>. 
     * If <code>obj</code> is a CORBA Object, it is written to 
     * <code>out.write_Object(org.omg.CORBA.Object)</code>.
     * @param out the stream in which to write the object.
     * @param obj the object to write.
     */
    public void writeRemoteObject(OutputStream out, java.lang.Object obj) 
    {
        // Make sure we have a connected object, then
        // write it out...
    
        Object newObj = Utility.autoConnect(obj,out.orb(),false);
        out.write_Object((org.omg.CORBA.Object)newObj);
    }
    
    /**
     * Writes a java.lang.Object as either a value or a CORBA Object. 
     * If <code>obj</code> is a value object or a stub object, it is written to 
     * <code>out.write_abstract_interface(java.lang.Object)</code>. If <code>obj</code> is an exported 
     * RMI-IIOP server object, the tie is found and wired to <code>obj</code>,
     * then written to <code>out.write_abstract_interface(java.lang.Object)</code>. 
     * @param out the stream in which to write the object.
     * @param obj the object to write.
     */
    public void writeAbstractObject( OutputStream out, java.lang.Object obj ) 
    {
        // Make sure we have a connected object, then
        // write it out...
    
        Object newObj = Utility.autoConnect(obj,out.orb(),false);
        ((org.omg.CORBA_2_3.portable.OutputStream)out).write_abstract_interface(newObj);
    }
    
    /**
     * Registers a target for a tie. Adds the tie to an internal table and calls
     * {@link Tie#setTarget} on the tie object.
     * @param tie the tie to register.
     * @param target the target for the tie.
     */
    @SuppressWarnings("unchecked")
    public void registerTarget(javax.rmi.CORBA.Tie tie, java.rmi.Remote target) 
    {
        synchronized (exportedServants) {
            // Do we already have this target registered?
            if (lookupTie(target) == null) {
                // No, so register it and set the target...
                exportedServants.put(target,tie);
                tie.setTarget(target);
            
                // Do we need to instantiate our keep-alive thread?
                if (keepAlive == null) {
                    // Yes. Instantiate our keep-alive thread and start
                    // it up...
                    keepAlive = (KeepAlive)AccessController.doPrivileged(
                        new PrivilegedAction<Object>() {
                            public java.lang.Object run() {
                                return new KeepAlive();
                            }
                        });
                    keepAlive.start();
                }
            }
        }
    }
    
    /**
     * Removes the associated tie from an internal table and calls {@link Tie#deactivate} 
     * to deactivate the object.
     * @param target the object to unexport.
     */
    public void unexportObject(java.rmi.Remote target) 
        throws java.rmi.NoSuchObjectException 
    {
        synchronized (exportedServants) {
            Tie cachedTie = lookupTie(target);
            if (cachedTie != null) {
                exportedServants.remove(target);
                Utility.purgeStubForTie(cachedTie);
                Utility.purgeTieAndServant(cachedTie);
                try {
                    cleanUpTie(cachedTie);
                } catch (BAD_OPERATION e) {
                    // ignore
                } catch (org.omg.CORBA.OBJ_ADAPTER e) {
                    // This can happen when the target was never associated with a POA.
                    // We can safely ignore this case.
                }
                
                // Is it time to shut down our keep alive thread?
                if (exportedServants.isEmpty()) {
                    keepAlive.quit();
                    keepAlive = null;
                }
            } else {
                throw new java.rmi.NoSuchObjectException("Tie not found" );
            }
        }
    }

    protected void cleanUpTie(Tie cachedTie) 
        throws java.rmi.NoSuchObjectException 
    {
        cachedTie.setTarget(null);
        cachedTie.deactivate();
    }
    
    /**
     * Returns the tie (if any) for a given target object.
     * @return the tie or null if no tie is registered for the given target.
     */
    public Tie getTie (Remote target) 
    {
        synchronized (exportedServants) {
            return lookupTie(target);
        }
    }

    /**
     * An unsynchronized version of getTie() for internal use.
     */
    private static Tie lookupTie (Remote target) 
    {
        Tie result = exportedServants.get(target);
        if (result == null && target instanceof Tie) {
            if (exportedServants.containsKey(target)) {
                result = (Tie)target;
            }
        }
        return result;
    }

    /**
     * Returns a singleton instance of a class that implements the
     * {@link ValueHandler} interface. 
     * @return a class which implements the ValueHandler interface.
     */
    public ValueHandler createValueHandler() 
    {
        return valueHandlerSingleton;
    }

    /**
     * Returns the codebase, if any, for the given class. 
     * @param clz the class to get a codebase for.
     * @return a space-separated list of URLs, or null.
     */
    public String getCodebase(java.lang.Class clz) {
        String annot ;
        synchronized (annotObj) {
            annot = annotationMap.get(clz);
        }

        if (annot == null) {
            // This can be an expensive operation, so don't hold the lock here.
            annot = RMIClassLoader.getClassAnnotation(clz);

            synchronized( annotObj ) {
                annotationMap.put(clz, annot);
            }
        }

        return annot;
    }

    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class.
     * @param remoteCodebase a space-separated list of URLs at which
     * the class might be found. May be null.
     * @param loader a ClassLoader who may be used to
     * load the class if all other methods fail.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception ClassNotFoundException if class cannot be loaded.
     */
    public Class loadClass( String className, String remoteCodebase,    
        ClassLoader loader) throws ClassNotFoundException 
    {
        return JDKBridge.loadClass(className,remoteCodebase,loader);                                
    }

    /**
     * The <tt>isLocal</tt> method has the same semantics as the 
     * ObjectImpl._is_local method, except that it can throw a RemoteException.
     * (no it doesn't but the spec says it should.)
     *   
     * The <tt>_is_local()</tt> method is provided so that stubs may determine 
     * if a particular object is implemented by a local servant and hence local
     * invocation APIs may be used.
     * 
     * @param stub the stub to test.
     *
     * @return The <tt>_is_local()</tt> method returns true if
     * the servant incarnating the object is located in the same process as
     * the stub and they both share the same ORB instance.  The <tt>_is_local()</tt>
     * method returns false otherwise. The default behavior of <tt>_is_local()</tt> is
     * to return false.
     *
     * @throws RemoteException The Java to IDL specification does to
     * specify the conditions that cause a RemoteException to be thrown.
     */
    public boolean isLocal(javax.rmi.CORBA.Stub stub) throws RemoteException 
    {
        boolean result = false ;

        try {
            org.omg.CORBA.portable.Delegate delegate = stub._get_delegate() ;
            if (delegate instanceof ClientDelegate) {
                // For the Sun ORB
                ClientDelegate cdel = (ClientDelegate)delegate ;
                ContactInfoList cil = cdel.getContactInfoList() ;
                LocalClientRequestDispatcher lcs =
                    cil.getLocalClientRequestDispatcher() ;
                result = lcs.useLocalInvocation( null ) ;
            } else {
                // For a non-Sun ORB
                result = delegate.is_local( stub ) ;
            }
        } catch (SystemException e) {
            throw mapSystemException(e);
        }

        return result ;
    }
    
    /**
     * Wraps an exception thrown by an implementation
     * method.  It returns the corresponding client-side exception. 
     * @param orig the exception to wrap.
     * @return the wrapped exception.
     */
    public RemoteException wrapException(Throwable orig) 
    {
        if (orig instanceof SystemException) {
            return mapSystemException((SystemException)orig);
        }
        
        if (orig instanceof Error) {
            return new ServerError("Error occurred in server thread",(Error)orig);   
        } else if (orig instanceof RemoteException) {
            return new ServerException("RemoteException occurred in server thread",
                                       (Exception)orig);   
        } else if (orig instanceof RuntimeException) {
            throw (RuntimeException) orig;
        }       
        
        if (orig instanceof Exception) {
            return new UnexpectedException(orig.toString(),
                (Exception) orig);
        } else {
            return new UnexpectedException(orig.toString());
        }
    }

    /**
     * Copies or connects an array of objects. Used by local stubs
     * to copy any number of actual parameters, preserving sharing
     * across parameters as necessary to support RMI semantics.
     * @param obj the objects to copy or connect.
     * @param orb the ORB.
     * @return the copied or connected objects.
     * @exception RemoteException if any object could not be copied or connected.
     */
    public Object[] copyObjects (Object[] obj, org.omg.CORBA.ORB orb)
        throws RemoteException 
    {
        if (obj == null) {
            throw new NullPointerException();
        }

        Class<?> compType = obj.getClass().getComponentType() ;
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( compType ) ;
        if (cinfo.isARemote(compType) && cinfo.isInterface()) {
            // obj is an array of remote impl types.  This
            // causes problems with stream copier, so we copy
            // it over to an array of Remotes instead.
            Remote[] result = new Remote[obj.length] ;
            System.arraycopy( (Object)obj, 0, (Object)result, 0, obj.length ) ;
            return (Object[])copyObject( result, orb ) ;
        } else {
            return (Object[]) copyObject( obj, orb );
        }
    }

    /**
     * Copies or connects an object. Used by local stubs to copy 
     * an actual parameter, result object, or exception.
     * @param obj the object to copy.
     * @param orb the ORB.
     * @return the copy or connected object.
     * @exception RemoteException if the object could not be copied or connected.
     */
    public Object copyObject (Object obj, org.omg.CORBA.ORB orb)
        throws RemoteException 
    {
        try {
            if (((ORB)orb).operationTraceDebugFlag) {
                OperationTracer.enable() ;
            }

            OperationTracer.begin( "copyObject") ;

            if (orb instanceof ORB) {
                ORB lorb = (ORB)orb ;

                try {
                    try {
                        // This gets the copier for the current invocation, which was
                        // previously set by preinvoke.
                        return lorb.peekInvocationInfo().getCopierFactory().make().copy( obj ) ;
                    } catch (java.util.EmptyStackException exc) {
                        // copyObject was invoked outside of an invocation, probably by
                        // a test.  Get the default copier from the ORB.
                        CopierManager cm = lorb.getCopierManager() ;
                        ObjectCopier copier = cm.getDefaultObjectCopierFactory().make() ;
                        return copier.copy( obj ) ;
                    }
                } catch (ReflectiveCopyException exc) {
                    RemoteException rexc = new RemoteException() ;
                    rexc.initCause( exc ) ;
                    throw rexc ;
                }
            } else {
                if (obj instanceof Remote) {
                    // Make sure obj is connected and converted to a stub,
                    // if necessary.
                    return Utility.autoConnect( obj, orb, true ) ;
                }

                org.omg.CORBA_2_3.portable.OutputStream out =
                    (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();
                out.write_value((Serializable)obj);
                org.omg.CORBA_2_3.portable.InputStream in =
                    (org.omg.CORBA_2_3.portable.InputStream)out.create_input_stream();
                return in.read_value();
            }
        } finally {
            OperationTracer.disable();
            OperationTracer.finish(); 
        }
    }
}

class KeepAlive extends Thread 
{
    boolean quit = false;
    
    public KeepAlive () 
    {
        setDaemon(false);
    }
    
    @Override
    public synchronized void run () 
    {
        while (!quit) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }
    
    public synchronized void quit () 
    {
        quit = true;
        notifyAll();
    }  
}
