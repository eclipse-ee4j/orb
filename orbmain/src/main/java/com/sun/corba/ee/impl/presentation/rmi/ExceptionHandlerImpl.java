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

package com.sun.corba.ee.impl.presentation.rmi ;

import java.rmi.UnexpectedException ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA.portable.ApplicationException ;

import java.lang.reflect.Method ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;

public class ExceptionHandlerImpl implements ExceptionHandler 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self;

    private ExceptionRW[] rws ;

///////////////////////////////////////////////////////////////////////////////
// ExceptionRW interface and implementations.  
// Used to read and write exceptions.
///////////////////////////////////////////////////////////////////////////////

    public interface ExceptionRW
    {
        Class getExceptionClass() ;

        String getId() ;

        void write( OutputStream os, Exception ex ) ;

        Exception read( InputStream is ) ;
    }

    public abstract class ExceptionRWBase implements ExceptionRW
    {
        private Class cls ;
        private String id ;

        public ExceptionRWBase( Class cls ) 
        {
            this.cls = cls ;
        }

        public Class getExceptionClass() 
        {
            return cls ;
        }

        public String getId()
        {
            return id ;
        }

        void setId( String id )
        {
            this.id = id ;
        }
    }

    public class ExceptionRWIDLImpl extends ExceptionRWBase
    {
        private Method readMethod ;
        private Method writeMethod ;

        public ExceptionRWIDLImpl( Class cls ) 
        {
            super( cls ) ;

            String helperName = cls.getName() + "Helper" ;
            ClassLoader loader = cls.getClassLoader() ;
            Class helperClass ;

            try {
                helperClass = Class.forName( helperName, true, loader ) ;
                Method idMethod = helperClass.getDeclaredMethod( "id" ) ;
                setId( (String)idMethod.invoke( null ) ) ;
            } catch (Exception ex) {
                throw wrapper.badHelperIdMethod( ex, helperName ) ;
            }

            try {
                writeMethod = helperClass.getDeclaredMethod( "write", 
                    org.omg.CORBA.portable.OutputStream.class, cls ) ;
            } catch (Exception ex) {
                throw wrapper.badHelperWriteMethod( ex, helperName ) ;
            }

            try {
                readMethod = helperClass.getDeclaredMethod( "read", 
                    org.omg.CORBA.portable.InputStream.class ) ;
            } catch (Exception ex) {
                throw wrapper.badHelperReadMethod( ex, helperName ) ;
            }
        }

        public void write( OutputStream os, Exception ex ) 
        {
            try {
                writeMethod.invoke( null, os, ex ) ;
            } catch (Exception exc) {
                throw wrapper.badHelperWriteMethod( exc, 
                    writeMethod.getDeclaringClass().getName() ) ;
            }
        }

        public Exception read( InputStream is ) 
        {
            try {
                return (Exception)readMethod.invoke( null, is ) ;
            } catch (Exception ex) {
                throw wrapper.badHelperReadMethod( ex, 
                    readMethod.getDeclaringClass().getName() ) ;
            }
        }
    }

    public class ExceptionRWRMIImpl extends ExceptionRWBase
    {
        public ExceptionRWRMIImpl( Class cls ) 
        {
            super( cls ) ;
            setId( IDLNameTranslatorImpl.getExceptionId( cls ) ) ;
        }

        public void write( OutputStream os, Exception ex ) 
        {
            os.write_string( getId() ) ;
            os.write_value( ex, getExceptionClass() ) ;
        }

        public Exception read( InputStream is ) 
        {
            is.read_string() ; // read and ignore!
            return (Exception)is.read_value( getExceptionClass() ) ;
        }
    }

///////////////////////////////////////////////////////////////////////////////

    public ExceptionHandlerImpl( Class[] exceptions )
    {
        int count = 0 ;
        for (int ctr=0; ctr<exceptions.length; ctr++) {
            Class cls = exceptions[ctr] ;
            if (!ClassInfoCache.get(cls).isARemoteException(cls))
                count++ ;
        }

        rws = new ExceptionRW[count] ;

        int index = 0 ;
        for (int ctr=0; ctr<exceptions.length; ctr++) {
            Class cls = exceptions[ctr] ;
            ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cls ) ;
            if (!cinfo.isARemoteException(cls)) {
                ExceptionRW erw = null ;
                if (cinfo.isAUserException(cls))
                    erw = new ExceptionRWIDLImpl( cls ) ;
                else
                    erw = new ExceptionRWRMIImpl( cls ) ;

                /* The following check is not performed
                 * in order to maintain compatibility with 
                 * rmic.  See bug 4989312.
                 
                // Check for duplicate repository ID
                String repositoryId = erw.getId() ;
                int duplicateIndex = findDeclaredException( repositoryId ) ;
                if (duplicateIndex > 0) {
                    ExceptionRW duprw = rws[duplicateIndex] ;
                    String firstClassName = 
                        erw.getExceptionClass().getName() ;
                    String secondClassName = 
                        duprw.getExceptionClass().getName() ;
                    throw wrapper.duplicateExceptionRepositoryId(
                        firstClassName, secondClassName, repositoryId ) ;
                }

                */

                rws[index++] = erw ;
            }
        }
    }

    private int findDeclaredException( Class cls ) 
    {
        for (int ctr = 0; ctr < rws.length; ctr++) {
            Class next = rws[ctr].getExceptionClass() ;
            if (next.isAssignableFrom(cls))
                return ctr ;
        }

        return -1 ;
    }

    private int findDeclaredException( String repositoryId )
    {
        for (int ctr=0; ctr<rws.length; ctr++) {
            // This may occur when rws has not been fully 
            // populated, in which case the search should just fail.
            if (rws[ctr]==null)
                return -1 ;

            String rid = rws[ctr].getId() ;
            if (repositoryId.equals( rid )) 
                return ctr ;
        }

        return -1 ;
    }

    public boolean isDeclaredException( Class cls ) 
    {
        return findDeclaredException( cls ) >= 0 ;
    }

    public void writeException( OutputStream os, Exception ex ) 
    {
        int index = findDeclaredException( ex.getClass() ) ;
        if (index < 0)
            throw wrapper.writeUndeclaredException( ex,
                ex.getClass().getName() ) ;

        rws[index].write( os, ex ) ;
    }

    public Exception readException( ApplicationException ae ) 
    {
        // Note that the exception ID is present in both ae 
        // and in the input stream from ae.  The exception 
        // reader must actually read the exception ID from
        // the stream.
        InputStream is = (InputStream)ae.getInputStream() ;
        String excName = ae.getId() ;
        int index = findDeclaredException( excName ) ;
        if (index < 0) {
            excName = is.read_string() ;
            Exception res = new UnexpectedException( excName ) ;
            res.initCause( ae ) ;
            return res ;
        }

        return rws[index].read( is ) ;
    }

    // This is here just for the dynamicrmiiiop test
    public ExceptionRW getRMIExceptionRW( Class cls )
    {
        return new ExceptionRWRMIImpl( cls ) ;
    }
}

