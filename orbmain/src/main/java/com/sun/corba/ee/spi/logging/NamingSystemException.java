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

package com.sun.corba.ee.spi.logging ;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;
import com.sun.corba.ee.spi.logex.corba.CS;
import com.sun.corba.ee.spi.logex.corba.CSValue;

import com.sun.corba.ee.spi.logex.corba.ORBException ;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension ;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.NamingGroup )
public interface NamingSystemException {
    NamingSystemException self = WrapperGenerator.makeWrapper( 
        NamingSystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=0 )
    @Message( "Port 0 is not a valid port in the transient name server" )
    BAD_PARAM transientNameServerBadPort(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "A null hostname is not a valid hostname in the "
        + "transient name server" )
    BAD_PARAM transientNameServerBadHost(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Object is null" )
    BAD_PARAM objectIsNull() ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Bad host address in -ORBInitDef" )
    BAD_PARAM insBadAddress(  ) ;
    
    @Log( level=LogLevel.WARNING, id=0 )
    @Message( "Updated context failed for bind" )
    UNKNOWN bindUpdateContextFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "bind failure" )
    UNKNOWN bindFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Resolve conversion failed" )
    @CS( CSValue.MAYBE )
    UNKNOWN resolveConversionFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Resolve failure" )
    @CS( CSValue.MAYBE )
    UNKNOWN resolveFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Unbind failure" )
    @CS( CSValue.MAYBE )
    UNKNOWN unbindFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=50 )
    @Message( "SystemException in transient name service while initializing" )
    INITIALIZE transNsCannotCreateInitialNcSys( @Chain SystemException exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=51 )
    @Message( "Java exception in transient name service while initializing" )
    INITIALIZE transNsCannotCreateInitialNc( @Chain Exception exc ) ;

    String namingCtxRebindAlreadyBound = 
        "Unexpected AlreadyBound exception in rebind" ;

    @Log( level=LogLevel.WARNING, id=0 )
    @Message( namingCtxRebindAlreadyBound )
    INTERNAL namingCtxRebindAlreadyBound( @Chain Exception exc ) ;

    @Log( level=LogLevel.WARNING, id=0 )
    @Message( namingCtxRebindAlreadyBound )
    INTERNAL namingCtxRebindAlreadyBound() ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Unexpected AlreadyBound exception in rebind_context" )
    INTERNAL namingCtxRebindctxAlreadyBound( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Bad binding type in internal binding implementation" )
    INTERNAL namingCtxBadBindingtype(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Object reference that is not CosNaming::NamingContext "
        + "bound as a context" )
    INTERNAL namingCtxResolveCannotNarrowToCtx(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Error in creating POA for BindingIterator" )
    INTERNAL namingCtxBindingIteratorCreate( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=100 )
    @Message( "Bind implementation encountered a previous bind" )
    INTERNAL transNcBindAlreadyBound(  ) ;
    
    @Log( level=LogLevel.WARNING, id=101 )
    @Message( "list operation caught an unexpected Java exception while "
        + "creating list iterator" )
    INTERNAL transNcListGotExc( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=102 )
    @Message( "new_context operation caught an unexpected Java exception "
        + "creating the NewContext servant" )
    INTERNAL transNcNewctxGotExc( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=103 )
    @Message( "Destroy operation caught a Java exception while "
        + "disconnecting from ORB" )
    INTERNAL transNcDestroyGotExc( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=105 )
    @Message( "Stringified object reference with unknown protocol specified" )
    INTERNAL insBadSchemeName(  ) ;
    
    @Log( level=LogLevel.WARNING, id=107 )
    @Message( "Malformed URL in -ORBInitDef" )
    INTERNAL insBadSchemeSpecificPart(  ) ;
    
    @Log( level=LogLevel.WARNING, id=108 )
    @Message( "Malformed URL in -ORBInitDef" )
    INTERNAL insOther(  ) ;

    @Log( level=LogLevel.WARNING, id=109 )
    @Message( "Initial port value {0} is not a valid number" )
    INTERNAL badInitialPortValue(String ips, @Chain NumberFormatException e);
}
