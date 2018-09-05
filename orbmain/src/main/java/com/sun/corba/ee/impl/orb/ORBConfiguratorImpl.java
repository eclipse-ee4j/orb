/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb ;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction ;
import java.security.AccessController ;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher ;

import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults ;
import com.sun.corba.ee.spi.copyobject.CopierManager ;

import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories ;

import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;

import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;

import com.sun.corba.ee.spi.oa.OADefault ;
import com.sun.corba.ee.spi.oa.ObjectAdapterFactory ;

import com.sun.corba.ee.spi.orb.Operation ;
import com.sun.corba.ee.spi.orb.OperationFactory ;
import com.sun.corba.ee.spi.orb.ORBData ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.ORBConfigurator ;
import com.sun.corba.ee.spi.orb.ParserImplBase ;
import com.sun.corba.ee.spi.orb.PropertyParser ;
import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry ;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;
import com.sun.corba.ee.spi.protocol.RequestDispatcherDefault ;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcherFactory ;

import com.sun.corba.ee.spi.resolver.LocalResolver ;
import com.sun.corba.ee.spi.resolver.Resolver ;
import com.sun.corba.ee.spi.resolver.ResolverDefault ;

import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.spi.transport.TransportDefault ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults ;

import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults ;
import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.impl.transport.AcceptorImpl;

import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.ee.impl.legacy.connection.SocketFactoryAcceptorImpl;
import com.sun.corba.ee.impl.legacy.connection.SocketFactoryContactInfoListImpl;
import com.sun.corba.ee.impl.legacy.connection.USLPort;

import com.sun.corba.ee.impl.dynamicany.DynAnyFactoryImpl ;
import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.spi.transport.Acceptor;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

public class ORBConfiguratorImpl implements ORBConfigurator {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    protected void persistentServerInitialization(ORB theOrb) {
        // Does nothing, but can be overridden in subclass.
    }

    public static class ConfigParser extends ParserImplBase {
        private ORB orb ;

        public ConfigParser( ORB orb ) {
            this.orb = orb ;
        } ;

        public Class<?>[] userConfigurators = null ;

        public PropertyParser makeParser()
        {
            PropertyParser parser = new PropertyParser() ;
            Operation action = OperationFactory.compose( 
                OperationFactory.suffixAction(),
                OperationFactory.classAction( orb.classNameResolver() )
            ) ;
            parser.addPrefix( ORBConstants.USER_CONFIGURATOR_PREFIX, action, 
                "userConfigurators", Class.class ) ;
            return parser ;
        }
    }

    public void configure( DataCollector collector, ORB orb ) 
    {
        ORB theOrb = orb ;

        initObjectCopiers( theOrb ) ;
        initIORFinders( theOrb ) ;

        theOrb.setClientDelegateFactory( 
            // REVISIT: this should be ProtocolDefault.
            TransportDefault.makeClientDelegateFactory( theOrb )) ;

        initializeTransport(theOrb) ;

        initializeNaming( theOrb ) ;
        initServiceContextRegistry( theOrb ) ;
        initRequestDispatcherRegistry( theOrb ) ;
        registerInitialReferences( theOrb ) ;
        
        // Set up the PIHandler now.  The user configurator call is the
        // earliest point at which an invocation on this ORB can occur due to
        // external code extending the ORB through a configurator.
        // persistentServerInitialization also needs to make invocations to ORBD.
        // ORB invocations can also occur during the execution of
        // the ORBInitializers.  
        theOrb.createPIHandler() ;

        theOrb.setInvocationInterceptor( 
            PresentationDefaults.getNullInvocationInterceptor() ) ;

        persistentServerInitialization( theOrb ) ;

        runUserConfigurators( collector, theOrb ) ;
    }

    private void runUserConfigurators( DataCollector collector, ORB orb ) 
    {
        // Run any pluggable configurators.  This is a lot like 
        // ORBInitializers, only it uses the internal ORB and has
        // access to all data for parsing.  
        ConfigParser parser = new ConfigParser( orb )  ;
        parser.init( collector ) ;
        if (parser.userConfigurators != null) {
            for (int ctr=0; ctr<parser.userConfigurators.length; ctr++) {
                Class cls = parser.userConfigurators[ctr] ;
                try {
                    ORBConfigurator config = (ORBConfigurator)(cls.newInstance()) ;
                    config.configure( collector, orb ) ;
                } catch (Exception exc) {
                    wrapper.userConfiguratorException( exc ) ;
                }
            }
        }
    }


    /**
     * This is made somewhat complex because we are currently supporting
     * the ContactInfoList/Acceptor *AND* the legacy SocketFactory 
     * transport architecture.
     */
    private void initializeTransport(final ORB orb)
    {
        ORBData od = orb.getORBData();

        ContactInfoListFactory contactInfoListFactory =
            od.getCorbaContactInfoListFactory();
        Acceptor[] acceptors = od.getAcceptors();

        // BEGIN Legacy
        ORBSocketFactory legacySocketFactory = od.getLegacySocketFactory();
        USLPort[] uslPorts = od.getUserSpecifiedListenPorts() ;
        setLegacySocketFactoryORB(orb, legacySocketFactory);
        // END Legacy

        // Check for incorrect configuration.
        if (legacySocketFactory != null && contactInfoListFactory != null) {
            throw wrapper.socketFactoryAndContactInfoListAtSameTime();
        }

        if (acceptors.length != 0 && legacySocketFactory != null) {
            throw wrapper.acceptorsAndLegacySocketFactoryAtSameTime();
        }

        // Client and Server side setup.
        od.getSocketFactory().setORB(orb);

        // Set up client side.
        if (legacySocketFactory != null) {
            // BEGIN Legacy
            // Since the user specified a legacy socket factory we need to
            // use a ContactInfoList that will use the legacy socket factory.
            contactInfoListFactory =
                new ContactInfoListFactory() {
                        public void setORB(ORB orb) { }
                        public ContactInfoList create( IOR ior ) {
                            return new SocketFactoryContactInfoListImpl( 
                                orb, ior);
                        }
                    };
            // END Legacy
        } else if (contactInfoListFactory != null) {
            // The user specified an explicit ContactInfoListFactory.
            contactInfoListFactory.setORB(orb);
        } else {
            // Use the default.
            contactInfoListFactory =
                TransportDefault.makeCorbaContactInfoListFactory(orb);
        }
        orb.setCorbaContactInfoListFactory(contactInfoListFactory);

        //
        // Set up server side.
        //

        if (!od.noDefaultAcceptors()) {
            //
            // Maybe allocate the Legacy default listener.
            //
            // If old legacy properties set, or there are no explicit
            // acceptors then register a default listener.  Type of
            // default listener depends on presence of legacy socket factory.
            //
            // Note: this must happen *BEFORE* registering explicit acceptors.
            //

            // BEGIN Legacy
            int port = -1;
            if (od.getORBServerPort() != 0) {
                port = od.getORBServerPort();
            } else if (od.getPersistentPortInitialized()) {
                port = od.getPersistentServerPort();
            } else if ((acceptors.length == 0)) {
                port = 0;
            }
            if (port != -1) {
                createAndRegisterAcceptor(orb, legacySocketFactory, port,
                            LegacyServerSocketEndPointInfo.DEFAULT_ENDPOINT,
                            SocketInfo.IIOP_CLEAR_TEXT);
            }
            // END Legacy

            for (int i = 0; i < acceptors.length; i++) {
                orb.getCorbaTransportManager().registerAcceptor(acceptors[i]);
            }

            // BEGIN Legacy
            // Allocate user listeners.
            USLPort[] ports = od.getUserSpecifiedListenPorts() ;
            if (ports != null) {
                for (int i = 0; i < ports.length; i++) {
                    createAndRegisterAcceptor(
                        orb, legacySocketFactory, ports[i].getPort(),
                        LegacyServerSocketEndPointInfo.NO_NAME,
                        ports[i].getType());
                }
            }
            // END Legacy
        }
    }

    /*
     * Legacy: name.
     */
    // REVISIT: see ORBD. make factory in TransportDefault.
    private void createAndRegisterAcceptor(ORB orb,
                                           ORBSocketFactory legacySocketFactory,
                                           int port, String name, String type)
    {
        Acceptor acceptor;
        if (legacySocketFactory == null) {
            acceptor =
                new AcceptorImpl(orb, port, name, type);
        } else {
            acceptor =
                new SocketFactoryAcceptorImpl(orb, port, name, type);
        }
        orb.getCorbaTransportManager().registerAcceptor(acceptor);
    }

    private void setLegacySocketFactoryORB(
        final ORB orb, final ORBSocketFactory legacySocketFactory)
    {
        if (legacySocketFactory == null) {
            return;
        }

        // Note: the createServerSocket and createSocket methods on the
        // DefaultSocketFactory need to get data from the ORB but
        // we cannot change the interface.  So set the ORB (if it's ours)
        // by reflection.

        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object>() {
                    public Object run()
                        throws InstantiationException, IllegalAccessException
                    {
                        try {
                            Method method =
                                legacySocketFactory.getClass().getMethod(
                                  "setORB", ORB.class );
                            method.invoke(legacySocketFactory, orb);
                        } catch (NoSuchMethodException e) {
                            // NOTE: If there is no method then it
                            // is not ours - so ignore it.
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
                }
            );
        } catch (Throwable t) {
            throw wrapper.unableToSetSocketFactoryOrb(t);
        }
    }

    private void initializeNaming( ORB orb )
    { 
        LocalResolver localResolver = ResolverDefault.makeLocalResolver() ;
        orb.setLocalResolver( localResolver ) ;

        Resolver bootResolver = ResolverDefault.makeBootstrapResolver( orb,
            orb.getORBData().getORBInitialHost(),
            orb.getORBData().getORBInitialPort() ) ;

        Operation urlOperation = ResolverDefault.makeINSURLOperation( orb ) ;
        orb.setURLOperation( urlOperation ) ;

        Resolver irResolver = ResolverDefault.makeORBInitRefResolver( urlOperation,
            orb.getORBData().getORBInitialReferences() ) ;

        Resolver dirResolver = ResolverDefault.makeORBDefaultInitRefResolver( 
            urlOperation, orb.getORBData().getORBDefaultInitialReference() ) ;

        Resolver resolver = 
            ResolverDefault.makeCompositeResolver( localResolver,
                ResolverDefault.makeCompositeResolver( irResolver,
                    ResolverDefault.makeCompositeResolver( dirResolver, 
                        bootResolver ) ) ) ;
        orb.setResolver( resolver ) ;
    }

    private void initServiceContextRegistry( ORB orb ) 
    {
        ServiceContextFactoryRegistry scr = orb.getServiceContextFactoryRegistry() ;

        scr.register( 
            ServiceContextDefaults.makeUEInfoServiceContextFactory() ) ;
        scr.register( 
            ServiceContextDefaults.makeCodeSetServiceContextFactory() ) ;
        scr.register( 
            ServiceContextDefaults.makeSendingContextServiceContextFactory() ) ;
        scr.register( 
            ServiceContextDefaults.makeORBVersionServiceContextFactory() ) ;
        scr.register( 
            ServiceContextDefaults.makeMaxStreamFormatVersionServiceContextFactory() ) ;
    }

    private void registerInitialReferences( final ORB orb ) 
    {
        // Register the Dynamic Any factory
        NullaryFunction<org.omg.CORBA.Object> closure =
            new NullaryFunction<org.omg.CORBA.Object>() {
                public org.omg.CORBA.Object evaluate() {
                    return new DynAnyFactoryImpl( orb ) ;
                }
            } ;

        NullaryFunction<org.omg.CORBA.Object> future =
            NullaryFunction.Factory.makeFuture( closure ) ;
        orb.getLocalResolver().register( ORBConstants.DYN_ANY_FACTORY_NAME, 
            future ) ;
    }

    private static final int ORB_STREAM = 0 ;

    private void initObjectCopiers( ORB orb )
    {
        // No optimization or policy selection here.
        ObjectCopierFactory orbStream = 
            CopyobjectDefaults.makeORBStreamObjectCopierFactory( orb ) ;

        CopierManager cm = orb.getCopierManager() ;
        cm.setDefaultId( ORB_STREAM ) ;

        cm.registerObjectCopierFactory( orbStream, ORB_STREAM ) ;
    }

    private void initIORFinders( ORB orb ) 
    {
        IdentifiableFactoryFinder profFinder = 
            orb.getTaggedProfileFactoryFinder() ;
        profFinder.registerFactory( IIOPFactories.makeIIOPProfileFactory() ) ;

        IdentifiableFactoryFinder profTempFinder = 
            orb.getTaggedProfileTemplateFactoryFinder() ;
        profTempFinder.registerFactory( 
            IIOPFactories.makeIIOPProfileTemplateFactory() ) ;

        IdentifiableFactoryFinder compFinder = 
            orb.getTaggedComponentFactoryFinder() ;
        compFinder.registerFactory( 
            IIOPFactories.makeCodeSetsComponentFactory() ) ;
        compFinder.registerFactory( 
            IIOPFactories.makeJavaCodebaseComponentFactory() ) ;
        compFinder.registerFactory( 
            IIOPFactories.makeORBTypeComponentFactory() ) ;
        compFinder.registerFactory( 
            IIOPFactories.makeMaxStreamFormatVersionComponentFactory() ) ;
        compFinder.registerFactory( 
            IIOPFactories.makeAlternateIIOPAddressComponentFactory() ) ;
        compFinder.registerFactory( 
            IIOPFactories.makeRequestPartitioningComponentFactory() ) ;
        compFinder.registerFactory(
            IIOPFactories.makeJavaSerializationComponentFactory());
        compFinder.registerFactory(
            IIOPFactories.makeLoadBalancingComponentFactory());
        compFinder.registerFactory(
            IIOPFactories.makeClusterInstanceInfoComponentFactory());

        // Register the ValueFactory instances for ORT
        IORFactories.registerValueFactories( orb ) ;

        // Register an ObjectKeyFactory
        orb.setObjectKeyFactory( IORFactories.makeObjectKeyFactory(orb) ) ;
    }

    private void initRequestDispatcherRegistry( ORB orb ) 
    {
        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;

        // register client subcontracts
        ClientRequestDispatcher csub =
            RequestDispatcherDefault.makeClientRequestDispatcher() ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.TOA_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.TRANSIENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.PERSISTENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.SC_TRANSIENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.SC_PERSISTENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub,  
            ORBConstants.IISC_TRANSIENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.IISC_PERSISTENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.MINSC_TRANSIENT_SCID ) ;
        scr.registerClientRequestDispatcher( csub, 
            ORBConstants.MINSC_PERSISTENT_SCID ) ;
        
        // register server delegates
        ServerRequestDispatcher sd =
            RequestDispatcherDefault.makeServerRequestDispatcher( orb );
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.TOA_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.TRANSIENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.PERSISTENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.SC_TRANSIENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.SC_PERSISTENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.IISC_TRANSIENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.IISC_PERSISTENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.MINSC_TRANSIENT_SCID ) ;
        scr.registerServerRequestDispatcher( sd, 
            ORBConstants.MINSC_PERSISTENT_SCID ) ;
        
        orb.setINSDelegate( 
            RequestDispatcherDefault.makeINSServerRequestDispatcher( orb ) ) ;
            
        // register local client subcontracts
        LocalClientRequestDispatcherFactory lcsf = 
            RequestDispatcherDefault.makeJIDLLocalClientRequestDispatcherFactory( 
                orb ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.TOA_SCID ) ;

        lcsf = 
            RequestDispatcherDefault.makePOALocalClientRequestDispatcherFactory( 
                orb ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.TRANSIENT_SCID ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.PERSISTENT_SCID ) ;

        lcsf = RequestDispatcherDefault.
            makeFullServantCacheLocalClientRequestDispatcherFactory( orb ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.SC_TRANSIENT_SCID ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.SC_PERSISTENT_SCID ) ;

        lcsf = RequestDispatcherDefault.
            makeInfoOnlyServantCacheLocalClientRequestDispatcherFactory( orb ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.IISC_TRANSIENT_SCID ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.IISC_PERSISTENT_SCID ) ;

        lcsf = RequestDispatcherDefault.
            makeMinimalServantCacheLocalClientRequestDispatcherFactory( orb ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.MINSC_TRANSIENT_SCID ) ;
        scr.registerLocalClientRequestDispatcherFactory( lcsf, 
            ORBConstants.MINSC_PERSISTENT_SCID ) ;

        /* Register the server delegate that implements the ancient bootstrap
         * naming protocol.  This takes an object key of either "INIT" or 
         * "TINI" to allow for big or little endian implementations.
         */
        ServerRequestDispatcher bootsd =
            RequestDispatcherDefault.makeBootstrapServerRequestDispatcher( 
                orb ) ;
        scr.registerServerRequestDispatcher( bootsd, "INIT" ) ;
        scr.registerServerRequestDispatcher( bootsd, "TINI" ) ;

        // Register object adapter factories
        ObjectAdapterFactory oaf = OADefault.makeTOAFactory( orb ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.TOA_SCID ) ;

        oaf = OADefault.makePOAFactory( orb ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.TRANSIENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.PERSISTENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.SC_TRANSIENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.SC_PERSISTENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.IISC_TRANSIENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.IISC_PERSISTENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.MINSC_TRANSIENT_SCID ) ;
        scr.registerObjectAdapterFactory( oaf, ORBConstants.MINSC_PERSISTENT_SCID ) ;
    } 
}

// End of file.
