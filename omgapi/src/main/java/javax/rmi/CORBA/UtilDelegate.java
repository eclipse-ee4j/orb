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

package javax.rmi.CORBA;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.SystemException;

/**
 * Supports delegation for method implementations in {@link Util}.  The
 * delegate is a singleton instance of a class that implements this
 * interface and provides a replacement implementation for all the
 * methods of <code>javax.rmi.CORBA.Util</code>.
 *
 * Delegation is enabled by providing the delegate's class name as the
 * value of the 
 * <code>javax.rmi.CORBA.UtilClass</code>
 * system property.
 *
 * @see Util
 */
public interface UtilDelegate {

    /**
     * Delegation call for {@link Util#mapSystemException}.
     * @param ex the SystemException to map.
     * @return the mapped exception.
     */
    RemoteException mapSystemException(SystemException ex);

    /**
     * Delegation call for {@link Util#writeAny}.
     * @param out the stream in which to write the any.
     * @param obj the object to write as an any.
     */
    void writeAny(OutputStream out, Object obj);

    /**
     * Delegation call for {@link Util#readAny}.
     * @param in the stream from which to read the any.
     * @return the object read from the stream.
     */
    java.lang.Object readAny(InputStream in);

    /**
     * Delegation call for {@link Util#writeRemoteObject}.
     * @param out the stream in which to write the object.
     * @param obj the object to write.
     */
    void writeRemoteObject(OutputStream out, Object obj);

    /**
     * Delegation call for {@link Util#writeAbstractObject}.
     * @param out the stream in which to write the object.
     * @param obj the object to write.
     */
    void writeAbstractObject(OutputStream out, Object obj);

    /**
     * Delegation call for {@link Util#registerTarget}.
     * @param tie tie to register
     * @param target target for the tie
     */
    void registerTarget(Tie tie, Remote target);
    
    /**
     * Delegation call for {@link Util#unexportObject}.
     * @param target the object to unexport
     * @throws NoSuchObjectException if the target object does not exist
     */
    void unexportObject(Remote target) throws NoSuchObjectException;
    
    /**
     * Delegation call for {@link Util#getTie}.
     * @param target the object to get the tie for
     * @return the tie or null if no tie is registered for the given target.
     */
    Tie getTie(Remote target);

    /**
     * Delegation call for {@link Util#createValueHandler}.
     * @return a class which implements the ValueHandler interface.
     */
    ValueHandler createValueHandler();

    /**
     * Delegation call for {@link Util#getCodebase}.
     * @param clz the class to get a codebase for.
     * @return a space-separated list of URLs, or null.
     */
    String getCodebase(Class clz);

    /**
     * Delegation call for {@link Util#loadClass}.
     * @param className the name of the class.
     * @param remoteCodebase a space-separated list of URLs at which
     * the class might be found. May be null.
     * @param loader a <tt>ClassLoader</tt> that may be used to
     * load the class if all other methods fail.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception ClassNotFoundException if class cannot be loaded.
     */
    Class loadClass(String className, String remoteCodebase, ClassLoader loader) 
        throws ClassNotFoundException;

    /**
     * Delegation call for {@link Util#isLocal}.
     * @param stub the stub to test.
     * @return The is_local() method returns true if the servant incarnating
     * the object is located in the same process as the stub and they both
     * share the same ORB instance. The is_local() method returns false
     * otherwise. The default behaviour of is_local() is to return false.
     * @throws RemoteException The Java to IDL specification does not
     * specify the conditions that cause a <tt>RemoteException</tt> to be thrown.
     */
    boolean isLocal(Stub stub) throws RemoteException;

    /**
     * Delegation call for {@link Util#wrapException}.
     * @param obj the exception to wrap. 
     * @return the wrapped exception.
     */
    RemoteException wrapException(Throwable obj);

    /**
     * Delegation call for {@link Util#copyObject}.
     * @param obj the object to copy or connect.
     * @param orb the ORB
     * @return the copied or connected object.
     * @throws RemoteException if the object could not be copied or connected.
     */
    Object copyObject(Object obj, ORB orb) throws RemoteException;
    
    /**
     * Delegation call for {@link Util#copyObjects}.
     * @param obj the objects to copy or connect.
     * @param orb the ORB
     * @return the copied or connected objects.
     * @throws RemoteException if the objects could not be copied or connected.
     */
    Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException;

}
            
