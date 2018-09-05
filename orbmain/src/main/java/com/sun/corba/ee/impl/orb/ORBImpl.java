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

import java.applet.Applet;

import java.io.IOException ;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.WeakHashMap ;

import java.net.InetAddress ;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.NVList;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Request;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.ValueMember;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;

import org.omg.CORBA.portable.ValueFactory;

import org.omg.CORBA.ORBPackage.InvalidName;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.ee.spi.protocol.ClientInvocationInfo ;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory ;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.oa.ObjectAdapterFactory;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.Operation;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.orb.ParserImplBase;
import com.sun.corba.ee.spi.orb.PropertyParser;
import com.sun.corba.ee.spi.orb.OperationFactory;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.spi.protocol.ClientDelegateFactory;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.protocol.PIHandler;
import com.sun.corba.ee.spi.resolver.Resolver;
import com.sun.corba.ee.spi.resolver.LocalResolver;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.ee.spi.copyobject.CopierManager ;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry;
import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.ee.spi.servicecontext.ServiceContextsCache;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.corba.NVListImpl;
import com.sun.corba.ee.impl.corba.ExceptionListImpl;
import com.sun.corba.ee.impl.corba.ContextListImpl;
import com.sun.corba.ee.impl.corba.NamedValueImpl;
import com.sun.corba.ee.impl.corba.EnvironmentImpl;
import com.sun.corba.ee.impl.corba.AsynchInvoke;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.encoding.CachedCodeBase;
import com.sun.corba.ee.impl.interceptors.PIHandlerImpl;
import com.sun.corba.ee.impl.ior.TaggedComponentFactoryFinderImpl;
import com.sun.corba.ee.impl.ior.TaggedProfileFactoryFinderImpl;
import com.sun.corba.ee.impl.ior.TaggedProfileTemplateFactoryFinderImpl;
import com.sun.corba.ee.impl.oa.toa.TOAFactory;
import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.ee.impl.oa.poa.POAFactory;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.threadpool.ThreadPoolManagerImpl;
import com.sun.corba.ee.impl.protocol.RequestDispatcherRegistryImpl;
import com.sun.corba.ee.impl.protocol.InvocationInfo;
import com.sun.corba.ee.impl.transport.TransportManagerImpl;
import com.sun.corba.ee.impl.legacy.connection.LegacyServerSocketManagerImpl;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.copyobject.CopierManagerImpl;
import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;
import com.sun.corba.ee.impl.misc.ByteArrayWrapper;
import com.sun.corba.ee.spi.trace.OrbLifeCycle;
import com.sun.corba.ee.spi.trace.Subcontract;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import org.glassfish.pfl.basic.algorithm.ObjectUtility;
import org.glassfish.pfl.basic.concurrent.WeakCache;
import org.glassfish.pfl.basic.contain.StackImpl;
import org.glassfish.pfl.basic.contain.ResourceFactory;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
         
/**
 * The JavaIDL ORB implementation.
 */
@OrbLifeCycle
@Subcontract
public class ORBImpl extends com.sun.corba.ee.spi.orb.ORB
{
    private boolean set_parameters_called = false ;

    protected TransportManager transportManager;
    protected LegacyServerSocketManager legacyServerSocketManager;

    private ThreadLocal<StackImpl<OAInvocationInfo>>
        OAInvocationInfoStack ;

    private ThreadLocal<StackImpl<ClientInvocationInfo>>
        clientInvocationInfoStack ;

    // pure java orb, caching the servant IOR per ORB
    private CodeBase codeBase = null ; 
    private IOR codeBaseIOR = null ;

    // List holding deferred Requests
    private final List<Request>  dynamicRequests =
        new ArrayList<Request>();

    private final SynchVariable svResponseReceived = new SynchVariable();


    private final Object runObj = new Object();
    private final Object shutdownObj = new Object();
    private final AtomicInteger numWaiters = new AtomicInteger() ;
    private final Object waitForCompletionObj = new Object();
    private static final byte STATUS_OPERATING = 1;
    private static final byte STATUS_SHUTTING_DOWN = 2;
    private static final byte STATUS_SHUTDOWN = 3;
    private static final byte STATUS_DESTROYED = 4;
    private final ReadWriteLock statueLock = new ReentrantReadWriteLock() ;
    private byte status = STATUS_OPERATING;

    private final java.lang.Object invocationObj = new java.lang.Object();
    private AtomicInteger numInvocations = new AtomicInteger();

    // thread local variable to store a boolean to detect deadlock in 
    // ORB.shutdown(true).
    private ThreadLocal<Boolean> isProcessingInvocation = 
        new ThreadLocal<Boolean> () {
        @Override
            protected Boolean initialValue() {
                return false ;
            }
        };

    // This map is caching TypeCodes created for a certain class (key)
    // and is used in Util.writeAny()
    private Map<Class<?>,TypeCodeImpl> typeCodeForClassMap ;

    // Cache to hold ValueFactories (Helper classes) keyed on repository ids
    private Map<String,ValueFactory> valueFactoryCache = 
        new HashMap<String,ValueFactory>();

    // thread local variable to store the current ORB version.
    // default ORB version is the version of ORB with correct Rep-id
    // changes
    private ThreadLocal<ORBVersion> orbVersionThreadLocal ; 

    private RequestDispatcherRegistry requestDispatcherRegistry ;

    private CopierManager copierManager ;

    private int transientServerId ;

    private ServiceContextFactoryRegistry serviceContextFactoryRegistry ;

    private ServiceContextsCache serviceContextsCache;

    // Needed here to implement connect/disconnect
    private ResourceFactory<TOAFactory> toaFactory =
        new ResourceFactory<TOAFactory>(
            new NullaryFunction<TOAFactory>()  {
                public TOAFactory evaluate() {
                    return (TOAFactory)requestDispatcherRegistry.getObjectAdapterFactory(
                        ORBConstants.TOA_SCID) ;
                }
            }
        );

    // Needed here for set_delegate
    private ResourceFactory<POAFactory> poaFactory =
        new ResourceFactory<POAFactory>(
            new NullaryFunction<POAFactory>()  {
                public POAFactory evaluate() {
                    return (POAFactory)requestDispatcherRegistry.getObjectAdapterFactory(
                        ORBConstants.TRANSIENT_SCID) ;
                }
            }
        );

    // The interceptor handler, which provides portable interceptor services for
    // subcontracts and object adapters.
    private PIHandler pihandler ;

    private ORBData configData ;

    private BadServerIdHandler badServerIdHandler ;

    private ClientDelegateFactory clientDelegateFactory ;

    private ContactInfoListFactory corbaContactInfoListFactory ;

    // All access to resolver, localResolver, and urlOperation must be protected using
    // the appropriate locks.  Do not hold the ORBImpl lock while accessing
    // resolver, or deadlocks may occur.
    // Note that we now have separate locks for each resolver type.  This is due
    // to bug 6238477, which was caused by a deadlock while resolving a
    // corbaname: URL that contained a reference to the same ORB as the
    // ORB making the call to string_to_object.  This caused a deadlock between the
    // client thread holding the single lock for access to the urlOperation,
    // and the server thread handling the client is_a request waiting on the
    // same lock to access the localResolver.

    // Used for resolver_initial_references and list_initial_services
    private Resolver resolver ;

    // Used for register_initial_references
    private LocalResolver localResolver ;

    // ServerRequestDispatcher used for all INS object references.
    private ServerRequestDispatcher insNamingDelegate ;

    // resolverLock must be used for all access to either resolver or
    // localResolver, since it is possible for the resolver to indirectly
    // refer to the localResolver.  Also used to protect access to
    // insNamingDelegate.
    private final Object resolverLock = new Object() ;

    // Converts strings to object references for resolvers and string_to_object
    private Operation urlOperation ;
    private final Object urlOperationLock = new java.lang.Object() ;

    private TaggedComponentFactoryFinder
        taggedComponentFactoryFinder ;

    private IdentifiableFactoryFinder<TaggedProfile>
        taggedProfileFactoryFinder ;

    private IdentifiableFactoryFinder<TaggedProfileTemplate>
        taggedProfileTemplateFactoryFinder ;

    private ObjectKeyFactory objectKeyFactory ;

    private boolean orbOwnsThreadPoolManager = false ;

    private ThreadPoolManager threadpoolMgr;

    private InvocationInterceptor invocationInterceptor ;

    private WeakCache<ByteArrayWrapper, ObjectKeyCacheEntry> objectKeyCache =
        new WeakCache<ByteArrayWrapper, ObjectKeyCacheEntry> () {
            @Override
            protected ObjectKeyCacheEntry lookup(ByteArrayWrapper key) {
                ObjectKey okey = ORBImpl.this.getObjectKeyFactory().create(
                    key.getObjKey());
                ObjectKeyCacheEntry entry = new ObjectKeyCacheEntryImpl( okey ) ;
                return entry ;
            }
        } ;

    public InvocationInterceptor getInvocationInterceptor() {
        return invocationInterceptor ;
    }

    public void setInvocationInterceptor( InvocationInterceptor interceptor ) {
        this.invocationInterceptor = interceptor ;
    }
    
    ////////////////////////////////////////////////////
    //
    // NOTE:
    //
    // Methods that are synchronized MUST stay synchronized.
    //
    // Methods that are NOT synchronized must stay that way to avoid deadlock.
    //
    //
    // REVISIT:
    //
    // checkShutDownState - lock on different object - and normalize usage.
    // starting/FinishDispatch and Shutdown
    // 

    public ORBData getORBData() 
    {
        return configData ;
    }
 
    public PIHandler getPIHandler()
    {
        return pihandler ;
    }

    public void createPIHandler() 
    {
        this.pihandler = new PIHandlerImpl( this, configData.getOrbInitArgs() ) ;
    }

    /**
     * Create a new ORB. Should be followed by the appropriate
     * set_parameters() call.
     */
    public ORBImpl()
    {
        // All initialization is done through set_parameters().
    }

    public ORBVersion getORBVersion()
    {
        return orbVersionThreadLocal.get() ;
    }

    public void setORBVersion(ORBVersion verObj)
    {
        orbVersionThreadLocal.set(verObj);
    }


    @OrbLifeCycle
    private void initManagedObjectManager() {
        createORBManagedObjectManager() ;
        mom.registerAtRoot( configData ) ;
    }

/****************************************************************************
 * The following methods are ORB initialization
 ****************************************************************************/

    // preInit initializes all non-pluggable ORB data that is independent
    // of the property parsing.
    private void preInit( String[] params, Properties props )
    {
        // This is the unique id of this server (JVM). Multiple incarnations
        // of this server will get different ids.
        // Compute transientServerId = milliseconds since Jan 1, 1970
        // Note: transientServerId will wrap in about 2^32 / 86400000 = 49.7 days.
        // If two ORBS are started at the same time then there is a possibility
        // of having the same transientServerId. This may result in collision 
        // and may be a problem in ior.isLocal() check to see if the object 
        // belongs to the current ORB. This problem is taken care of by checking
        // to see if the IOR port matches ORB server port in legacyIsLocalServerPort()
        // method.
        transientServerId = (int)System.currentTimeMillis();

        orbVersionThreadLocal  = new ThreadLocal<ORBVersion>() {
            @Override
            protected ORBVersion initialValue() {
                // set default to version of the ORB with correct Rep-ids
                return ORBVersionFactory.getORBVersion() ;
            }
        };

        requestDispatcherRegistry = new RequestDispatcherRegistryImpl( 
            ORBConstants.DEFAULT_SCID);
        copierManager = new CopierManagerImpl() ;

        taggedComponentFactoryFinder = 
            new TaggedComponentFactoryFinderImpl(this) ;
        taggedProfileFactoryFinder = 
            new TaggedProfileFactoryFinderImpl(this) ;
        taggedProfileTemplateFactoryFinder = 
            new TaggedProfileTemplateFactoryFinderImpl(this) ;

        OAInvocationInfoStack = 
            new ThreadLocal<StackImpl<OAInvocationInfo>> () {
                @Override
                protected StackImpl<OAInvocationInfo> initialValue() {
                    return new StackImpl<OAInvocationInfo>();
                } 
            };

        clientInvocationInfoStack = 
            new ThreadLocal<StackImpl<ClientInvocationInfo>>() {
                @Override
                protected StackImpl<ClientInvocationInfo> initialValue() {
                    return new StackImpl<ClientInvocationInfo>();
                }
            };

        serviceContextFactoryRegistry = 
            ServiceContextDefaults.makeServiceContextFactoryRegistry( this ) ;
    }

    @InfoMethod
    private void configDataParsingComplete(String oRBId) { }

    @InfoMethod
    private void transportInitializationComplete(String oRBId) { }

    @InfoMethod
    private void userConfiguratorExecutionComplete(String oRBId) { }

    @InfoMethod
    private void interceptorInitializationComplete(String oRBId) { }

    @InfoMethod
    private void mbeansRegistereed(String oRBId) { }

    @InfoMethod
    private void initializationComplete(String oRBId) { }

    @InfoMethod
    private void startingShutdown(String oRBId) { }

    @InfoMethod
    private void startingDestruction(String oRBId) { }

    @InfoMethod
    private void isLocalServerIdInfo(int subcontractId, int serverId,
        int transientServerId, boolean aTransient,
        boolean persistentServerIdInitialized, int psid) { }

    // Class that defines a parser that gets the name of the
    // ORBConfigurator class.
    private class ConfigParser extends ParserImplBase {
        // The default here is the ORBConfiguratorImpl that we define,
        // but this can be replaced.
        public Class<?> configurator ;

        public ConfigParser( boolean disableORBD ) {
            // Default configurator
            configurator = ORBConfiguratorImpl.class ;            

            if (!disableORBD) {
                // Note: this class is NOT included in the GF bundles!
                // Try to load it if present.
                String cname = 
                    "com.sun.corba.ee.impl.activation.ORBConfiguratorPersistentImpl" ;

                try {
                    configurator = Class.forName(cname);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ORBImpl.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        public PropertyParser makeParser()
        {
            PropertyParser parser = new PropertyParser() ;
            parser.add( ORBConstants.SUN_PREFIX + "ORBConfigurator",
                OperationFactory.classAction( classNameResolver() ),
                    "configurator" ) ;
            return parser ;
        }
    }

    // Map String to Integer to count number of ORBs with the 
    // same ORBId.
    private static final Map<String,Integer> idcount =
        new HashMap<String,Integer>() ;
    private String rootName = null ;

    @Override
    public synchronized String getUniqueOrbId() {
        if (rootName == null) {
            String orbid = getORBData().getORBId() ;
            if (orbid.length() == 0) {
                orbid = "orb";
            }

            int num = 1 ;
            // Look up the current count of ORB instances with 
            // the same ORBId.  If this is the first instance,
            // the count is 1, otherwise increment the count.
            synchronized (idcount) {
                if (idcount.containsKey( orbid )) {
                    num = idcount.get( orbid ) + 1 ;
                }

                idcount.put( orbid, num ) ;
            }

            if (num != 1) {
                rootName = orbid + "_" + num ;
            } else {
                rootName = orbid ;
            }
        }

        return rootName ;
    }

    @OrbLifeCycle
    private void postInit( String[] params, DataCollector dataCollector ) {
        // First, create the standard ORB config data.
        // This must be initialized before the ORBConfigurator
        // is executed. Note that the orbId is initialized here.
        configData = new ORBDataParserImpl( this, dataCollector) ;
        if (orbInitDebug) {
            System.out.println( "Contents of ORB configData:" ) ;
            System.out.println( ObjectUtility.defaultObjectToString( configData ) ) ;
        }
        configData.setOrbInitArgs( params ) ;

        // Set the debug flags early so they can be used by other
        // parts of the initialization.
        setDebugFlags( configData.getORBDebugFlags() ) ;
        configDataParsingComplete( getORBData().getORBId() ) ;

        initManagedObjectManager() ;

        // The TimerManager must be
        // initialized BEFORE the pihandler.initialize() call, in
        // case we want to time interceptor setup.  Obviously we
        // want to initialize the timerManager as early as possible
        // so we can time parts of initialization if desired.
        // timerManager = makeTimerManager( mom ) ;

        // This requires a valid TimerManager.
        initializePrimitiveTypeCodeConstants() ;

        // REVISIT: this should go away after more transport init cleanup
        // and going to ORT based ORBD.  
        transportManager = new TransportManagerImpl(this);
        getLegacyServerSocketManager();

        transportInitializationComplete( getORBData().getORBId() ) ;

        super.getByteBufferPool();
        serviceContextsCache = new ServiceContextsCache(this);

        // Create a parser to get the configured ORBConfigurator.
        ConfigParser parser = new ConfigParser( configData.disableORBD() ) ;
        parser.init( dataCollector ) ;

        ORBConfigurator configurator =  null ;
        String name = "NO NAME AVAILABLE" ;
        if (parser.configurator == null) {
            throw wrapper.badOrbConfigurator( name ) ;
        } else {
            try {
                configurator = 
                    (ORBConfigurator)(parser.configurator.newInstance()) ;
            } catch (Exception iexc) {
                name = parser.configurator.getName() ;
                throw wrapper.badOrbConfigurator( iexc, name ) ;
            }
        }

        // Finally, run the configurator.  Note that the default implementation allows
        // other configurators with their own parsers to run,
        // using the same DataCollector.
        try {
            configurator.configure( dataCollector, this ) ;
        } catch (Exception exc) {
            throw wrapper.orbConfiguratorError( exc ) ;
        }

        userConfiguratorExecutionComplete( getORBData().getORBId()  ) ;

        // Initialize the thread manager pool 
        // so it may be initialized & accessed without synchronization.
        // This must take place here so that a user conifigurator can 
        // set the threadpool manager first.
        getThreadPoolManager();

        // Last of all, run the ORB initializers.
        // Interceptors will not be executed until 
        // after pihandler.initialize().  A request that starts before
        // initialize completes and completes after initialize completes does
        // not see any interceptors.
        pihandler.initialize() ;

        interceptorInitializationComplete( getORBData().getORBId() ) ;

        // Now the ORB is ready, so finish all of the MBean registration
        if (configData.registerMBeans()) {
            mom.resumeJMXRegistration() ;
            mbeansRegistereed( getORBData().getORBId() ) ;
        }
    }

    private POAFactory getPOAFactory() {
        return poaFactory.get() ;
    }

    private TOAFactory getTOAFactory() {
        return toaFactory.get() ;
    }

    public void check_set_parameters() {
        if (set_parameters_called) {
            throw wrapper.setParameterCalledAgain() ;
        } else {
            set_parameters_called = true ;
        }
    }

    @OrbLifeCycle
    public void set_parameters( Properties props )
    {
        preInit( null, props ) ;
        DataCollector dataCollector = 
            DataCollectorFactory.create( props, getLocalHostName() ) ;
        postInit( null, dataCollector ) ;
        initializationComplete( getORBData().getORBId() ) ;
    }

    @OrbLifeCycle
    protected void set_parameters(Applet app, Properties props)
    {
        preInit( null, props ) ;
        DataCollector dataCollector = 
            DataCollectorFactory.create( app, props, getLocalHostName() ) ;
        postInit( null, dataCollector ) ;
        initializationComplete( getORBData().getORBId() ) ;
    }

    public void setParameters( String[] params, Properties props ) {
        set_parameters( params, props ) ;
    }

  /* we can't create object adapters inside the ORB init path, or else we'll get this same problem
   * in slightly different ways. (address in use exception)
   * Having an IORInterceptor (TxSecIORInterceptor) get called during ORB init always results in a
   * nested ORB.init call because of the call to getORB in the IORInterceptor.
   */
    protected void set_parameters (String[] params, Properties props)
    {
        preInit( params, props ) ;
        DataCollector dataCollector = 
            DataCollectorFactory.create( params, props, getLocalHostName() ) ;
        postInit( params, dataCollector ) ;
    }

/****************************************************************************
 * The following methods are standard public CORBA ORB APIs
 ****************************************************************************/

    public synchronized org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        return OutputStreamFactory.newEncapsOutputStream(this);
    }

    /**
     * Get a Current pseudo-object.
     * The Current interface is used to manage thread-specific
     * information for use by the transactions, security and other
     * services. This method is deprecated,
     * and replaced by ORB.resolve_initial_references("NameOfCurrentObject");
     *
     * @return          a Current pseudo-object.
     * @deprecated
     */
    @Override
    public synchronized org.omg.CORBA.Current get_current()
    {
        checkShutdownState();

        /* _REVISIT_
           The implementation of get_current is not clear. How would
           ORB know whether the caller wants a Current for transactions
           or security ?? Or is it assumed that there is just one
           implementation for both ? If Current is thread-specific,
           then it should not be instantiated; so where does the
           ORB get a Current ? 
           
           This should probably be deprecated. */

        throw wrapper.genericNoImpl() ;
    }

    /**
     * Create an NVList
     *
     * @param count     size of list to create
     * @return          NVList created
     *
     * @see NVList
     */
    public synchronized NVList create_list(int count)
    {
        checkShutdownState();
        return new NVListImpl(this, count);
    }

    /**
     * Create an NVList corresponding to an OperationDef
     *
     * @param oper      operation def to use to create list
     * @return          NVList created
     *
     * @see NVList
     */
    @Override
    public synchronized NVList create_operation_list(org.omg.CORBA.Object oper)
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }

    /**
     * Create a NamedValue
     *
     * @return          NamedValue created
     */
    public synchronized NamedValue create_named_value(String s, Any any, int flags)
    {
        checkShutdownState();
        return new NamedValueImpl(this, s, any, flags);
    }

    /**
     * Create an ExceptionList
     *
     * @return          ExceptionList created
     */
    public synchronized org.omg.CORBA.ExceptionList create_exception_list()
    {
        checkShutdownState();
        return new ExceptionListImpl();
    }

    /**
     * Create a ContextList
     *
     * @return          ContextList created
     */
    public synchronized org.omg.CORBA.ContextList create_context_list()
    {
        checkShutdownState();
        return new ContextListImpl(this);
    }

    /**
     * Get the default Context object
     *
     * @return          the default Context object
     */
    public synchronized org.omg.CORBA.Context get_default_context()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }

    /**
     * Create an Environment
     *
     * @return          Environment created
     */
    public synchronized org.omg.CORBA.Environment create_environment()
    {
        checkShutdownState();
        return new EnvironmentImpl();
    }

    public synchronized void send_multiple_requests_oneway(Request[] req)
    {
        checkShutdownState();

        // Invoke the send_oneway on each new Request
        for (int i = 0; i < req.length; i++) {
            req[i].send_oneway();
        }
    }

    /**
     * Send multiple dynamic requests asynchronously.
     *
     * @param req         an array of request objects.
     */
    public synchronized void send_multiple_requests_deferred(Request[] req)
    {
        checkShutdownState();
        dynamicRequests.addAll(Arrays.asList(req));

        // Invoke the send_deferred on each new Request
        for (Request r : req) {
            AsynchInvoke invokeObject = new AsynchInvoke( this, 
                (com.sun.corba.ee.impl.corba.RequestImpl)r, true);
            new Thread(invokeObject).start();
        }
    }

    /**
     * Find out if any of the deferred invocations have a response yet.
     */
    public synchronized boolean poll_next_response()
    {
        checkShutdownState();

        // poll on each pending request
        synchronized(dynamicRequests) {
            for (Request r : dynamicRequests) {
                if (r.poll_response()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the next request that has gotten a response.
     *
     * @return            the next request ready with a response.
     */
    public org.omg.CORBA.Request get_next_response()
        throws org.omg.CORBA.WrongTransaction
    {
        synchronized( this ) {
            checkShutdownState();
        }

        while (true) {
            // check if there already is a response
            synchronized ( dynamicRequests ) {
                Iterator<Request> iter = dynamicRequests.iterator() ;
                while (iter.hasNext()) {
                    Request curr = iter.next() ;
                    if (curr.poll_response()) {
                        curr.get_response() ;
                        iter.remove() ;
                        return curr ;
                    }
                }
            }

            // wait for a response
            synchronized(this.svResponseReceived) {
                while (!this.svResponseReceived.value()) {
                    try {
                        this.svResponseReceived.wait();
                    } catch(java.lang.InterruptedException ex) {
                        // NO-OP
                    }
                }
                // reinitialize the response flag
                this.svResponseReceived.reset();
            }
        }
    }

    /**
     * Notify response to ORB for get_next_response
     */
    public void notifyORB() 
    {
        synchronized (this.svResponseReceived) {
            this.svResponseReceived.set();
            this.svResponseReceived.notify();
        }
    }

    /**
     * Convert an object ref to a string.
     * @param obj The object to stringify.
     * @return A stringified object reference.
     */
    public synchronized String object_to_string(org.omg.CORBA.Object obj)
    {
        checkShutdownState();

        // Handle the null objref case
        if (obj == null) {
            IOR nullIOR = IORFactories.makeIOR( this ) ;
            return nullIOR.stringify();
        }

        IOR ior = null ;

        try {
            ior = getIOR( obj, true ) ;
        } catch (BAD_PARAM bp) {
            // Throw MARSHAL instead if this is a LOCAL_OBJECT_NOT_ALLOWED error.
            if (bp.minor == ORBUtilSystemException.LOCAL_OBJECT_NOT_ALLOWED) {
                throw omgWrapper.notAnObjectImpl( bp ) ;
            } else {
                throw bp;
            }
        }

        return ior.stringify() ;
    }

    /**
     * Convert a stringified object reference to the object it represents.
     * @param str The stringified object reference.
     * @return The unstringified object reference.
     */
    public org.omg.CORBA.Object string_to_object(String str)
    {
        Operation op ;

        synchronized (this) {
            checkShutdownState();
            op = urlOperation ;
        }

        if (str == null) {
            throw wrapper.nullParam();
        }

        synchronized (urlOperationLock) {
            org.omg.CORBA.Object obj = (org.omg.CORBA.Object)op.operate( str ) ;
            return obj ;
        }
    }

    // pure java orb support, moved this method from FVDCodeBaseImpl.
    // Note that we connect this if we have not already done so.
    public synchronized IOR getFVDCodeBaseIOR()
    {
        if (codeBaseIOR == null) {
            ValueHandler vh = ORBUtility.createValueHandler();
            codeBase = (CodeBase)vh.getRunTimeCodeBase();
            codeBaseIOR = getIOR( codeBase, true ) ;
        }

        return codeBaseIOR;
    }

    /**
     * Get the TypeCode for a primitive type.
     *
     * @param tcKind    the integer kind for the primitive type
     * @return          the requested TypeCode
     */
    public TypeCode get_primitive_tc(TCKind tcKind) {
        return get_primitive_tc( tcKind.value() ) ; 
    }

    /**
     * Create a TypeCode for a structure.
     *
     * @param id                the logical id for the typecode.
     * @param name      the name for the typecode.
     * @param members   an array describing the members of the TypeCode.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_struct_tc(String id,
                                     String name,
                                     StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_struct, id, name, members);
    }

    /**
     * Create a TypeCode for a union.
     *
     * @param id                the logical id for the typecode.
     * @param name      the name for the typecode.
     * @param discriminator_type
     *                  the type of the union discriminator.
     * @param members   an array describing the members of the TypeCode.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_union_tc(String id,
                                    String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this,
                                TCKind._tk_union,
                                id,
                                name,
                                discriminator_type,
                                members);
    }

    /**
     * Create a TypeCode for an enum.
     *
     * @param id                the logical id for the typecode.
     * @param name      the name for the typecode.
     * @param members   an array describing the members of the TypeCode.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_enum_tc(String id,
                                   String name,
                                   String[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_enum, id, name, members);
    }

    /**
     * Create a TypeCode for an alias.
     *
     * @param id                the logical id for the typecode.
     * @param name      the name for the typecode.
     * @param original_type
     *                  the type this is an alias for.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_alias_tc(String id,
                                    String name,
                                    TypeCode original_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_alias, id, name, original_type);
    }

    /**
     * Create a TypeCode for an exception.
     *
     * @param id                the logical id for the typecode.
     * @param name      the name for the typecode.
     * @param members   an array describing the members of the TypeCode.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_exception_tc(String id,
                                        String name,
                                        StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_except, id, name, members);
    }

    /**
     * Create a TypeCode for an interface.
     *
     * @param id                the logical id for the typecode.
     * @param name      the name for the typecode.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_interface_tc(String id,
                                        String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_objref, id, name);
    }

    /**
     * Create a TypeCode for a string.
     *
     * @param bound     the bound for the string.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_string_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_string, bound);
    }

    /**
     * Create a TypeCode for a wide string.
     *
     * @param bound     the bound for the string.
     * @return          the requested TypeCode.
     */
    public synchronized TypeCode create_wstring_tc(int bound) {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_wstring, bound);
    }

    public synchronized TypeCode create_sequence_tc(int bound, 
        TypeCode element_type) {

        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, element_type);
    }


    @SuppressWarnings("deprecation")
    public synchronized TypeCode create_recursive_sequence_tc(int bound,
                                                 int offset) {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, offset);
    }


    public synchronized TypeCode create_array_tc(int length,
                                    TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_array, length, element_type);
    }


    @Override
    public synchronized org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_native, id, name);
    }

    @Override
    public synchronized org.omg.CORBA.TypeCode create_abstract_interface_tc(
                                                               String id,
                                                               String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_abstract_interface, id, name);
    }

    @Override
    public synchronized org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_fixed, digits, scale);
    }

    @Override
    public synchronized org.omg.CORBA.TypeCode create_value_tc(String id,
                                                  String name,
                                                  short type_modifier,
                                                  TypeCode concrete_base,
                                                  ValueMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value, id, name,
                                type_modifier, concrete_base, members);
    }

    @Override
    public synchronized org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        checkShutdownState();
        return new TypeCodeImpl(this, id);
    }

    @Override
    public synchronized org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value_box, id, name, 
            boxed_type);
    }

    public synchronized Any create_any()
    {
        checkShutdownState();
        return new AnyImpl(this);
    }

    // TypeCodeFactory interface methods.
    // Keeping track of type codes by repository id.

    // Keeping a cache of TypeCodes associated with the class
    // they got created from in Util.writeAny().

    public synchronized void setTypeCodeForClass(Class c, TypeCodeImpl tci) 
    {
        if (typeCodeForClassMap == null) {
            typeCodeForClassMap = new WeakHashMap<Class<?>, TypeCodeImpl>(64);
        }

        // Store only one TypeCode per class.
        if ( ! typeCodeForClassMap.containsKey(c)) {
            typeCodeForClassMap.put(c, tci);
        }
    }

    public synchronized TypeCodeImpl getTypeCodeForClass(Class c) 
    {
        if (typeCodeForClassMap == null) {
            return null;
        }
        return typeCodeForClassMap.get(c);
    }

/****************************************************************************
 * The following methods deal with listing and resolving the initial
 * (bootstrap) object references such as "NameService".
 ****************************************************************************/

    public String[] list_initial_services() {
        Resolver res ;

        synchronized( this ) {
            checkShutdownState();
            res = resolver ;
        }

        synchronized (resolverLock) {
            java.util.Set<String> keys = res.list() ;
            return keys.toArray( new String[keys.size()] ) ;
        }
    }

    public org.omg.CORBA.Object resolve_initial_references(
        String identifier) throws InvalidName {
        Resolver res ;

        synchronized( this ) {
            checkShutdownState();
            res = resolver ;
        }

        org.omg.CORBA.Object result = res.resolve( identifier ) ;
        
        if (result == null) {
            throw new InvalidName(identifier + " not found");
        } else {
            return result;
        }
    }

    @Override
    public void register_initial_reference(
        String id, org.omg.CORBA.Object obj ) throws InvalidName {
        ServerRequestDispatcher insnd ;

        if ((id == null) || (id.length() == 0)) {
            throw new InvalidName("Null or empty id string");
        }

        synchronized (this) {
            checkShutdownState();
        }

        synchronized (resolverLock) {
            insnd = insNamingDelegate ;

            java.lang.Object obj2 = localResolver.resolve( id ) ;
            if (obj2 != null) {
                throw new InvalidName(id + " already registered");
            }

            localResolver.register( id, 
                NullaryFunction.Factory.makeConstant( obj )) ;
        }
      
        synchronized (this) {
            if (StubAdapter.isStub(obj)) {
                requestDispatcherRegistry.registerServerRequestDispatcher(insnd, id);
            }
        }
    }

/****************************************************************************
 * The following methods (introduced in POA / CORBA2.1) deal with
 * shutdown / single threading.
 ****************************************************************************/

    @Override
    public void run() 
    {
        synchronized (this) {
            checkShutdownState();
        }

        synchronized (runObj) {
            try {
                runObj.wait();
            } catch ( InterruptedException ex ) {}
        }
    }

    @Override
    @OrbLifeCycle
    public void shutdown(boolean wait_for_completion) {
        boolean wait = false ;

        synchronized (this) {
            checkShutdownState();
            
            // This is to avoid deadlock: don't allow a thread that is 
            // processing a request to call shutdown( true ), because
            // the shutdown would block waiting for the request to complete,
            // while the request would block waiting for shutdown to complete.
            if (wait_for_completion &&
                isProcessingInvocation.get() == Boolean.TRUE) {
                throw omgWrapper.shutdownWaitForCompletionDeadlock() ;
            }

            if (status == STATUS_SHUTTING_DOWN) {
                if (wait_for_completion) {
                    wait = true ;
                } else {
                    return ;
                }
            }

            status = STATUS_SHUTTING_DOWN ;
        } 

        // Avoid more than one thread performing shutdown at a time.
        synchronized (shutdownObj) {
            // At this point, the ORB status is certainly STATUS_SHUTTING_DOWN.
            // If wait is true, another thread already called shutdown( true ),
            // and so we wait for completion
            if (wait) {
                while (true) {
                    synchronized (this) {
                        if (status == STATUS_SHUTDOWN) {
                            break;
                        }
                    }

                    try {
                        shutdownObj.wait() ;
                    } catch (InterruptedException exc) {
                        // NOP: just loop and wait until state is changed
                    }
                }
            } else {
                startingShutdown( getORBData().getORBId() ) ;
                
                // perform the actual shutdown
                shutdownServants(wait_for_completion);

                if (wait_for_completion) {
                    synchronized ( waitForCompletionObj ) {
                        while (numInvocations.get() > 0) {
                            try {
                                numWaiters.incrementAndGet() ;
                                waitForCompletionObj.wait();
                            } catch (InterruptedException ex) {
                                // ignore
                            } finally {
                                numWaiters.decrementAndGet() ;
                            }
                        }
                    }
                }

                synchronized ( runObj ) {
                    runObj.notifyAll();
                }

                status = STATUS_SHUTDOWN;

                shutdownObj.notifyAll() ;
            }
        }
    }

    // Cause all ObjectAdapaterFactories to clean up all of their internal state, which 
    // may include activated objects that have associated state and callbacks that must
    // complete in order to shutdown.  This will cause new request to be rejected.
    @OrbLifeCycle
    protected void shutdownServants(boolean wait_for_completion) {
        Set<ObjectAdapterFactory> oaset ;
        synchronized(this) {
            oaset = new HashSet<ObjectAdapterFactory>( 
                requestDispatcherRegistry.getObjectAdapterFactories() ) ;
        }

        for (ObjectAdapterFactory oaf : oaset) {
            oaf.shutdown(wait_for_completion);
        }
    }

    // Note that the caller must hold the ORBImpl lock.
    private void checkShutdownState()
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.orbDestroyed() ;
        }

        if (status == STATUS_SHUTDOWN) {
            throw omgWrapper.badOperationAfterShutdown() ;
        }
    }

    public boolean isDuringDispatch() {
        return isProcessingInvocation.get() ;
    }

    public void startingDispatch() {
        isProcessingInvocation.set(true);
        numInvocations.incrementAndGet() ;
    }

    public void finishedDispatch() {
        isProcessingInvocation.set(false);
        int ni = numInvocations.decrementAndGet() ;
        if (ni < 0) {
            throw wrapper.numInvocationsAlreadyZero() ;
        }

        if (numWaiters.get() > 0 && ni == 0) {
            synchronized (waitForCompletionObj) {
                waitForCompletionObj.notifyAll();
            }
        }
    }

    /**
     *  formal/99-10-07 p 159: "If destroy is called on an ORB that has
     *  not been shut down, it will start the shutdown process and block until
     *  the ORB has shut down before it destroys the ORB."
     */
    @Override
    @OrbLifeCycle
    public void destroy() {
        boolean shutdownFirst = false ;
        synchronized (this) {
            shutdownFirst = (status == STATUS_OPERATING) ;
        }

        if (shutdownFirst) {
            shutdown(true);
        }

        synchronized (this) {
            if (status < STATUS_DESTROYED) {
                getCorbaTransportManager().close();
                getPIHandler().destroyInterceptors() ;
                // timerManager.destroy() ;
                // timerManager = null ;
                status = STATUS_DESTROYED;
            } else {
                // Already destroyed: don't want to throw null pointer exceptions.
                return ;
            }
        }

        startingDestruction( getORBData().getORBId() ) ;

        ThreadPoolManager tpToClose = null ;
        synchronized (threadPoolManagerAccessLock) {
            if (orbOwnsThreadPoolManager) {
                tpToClose = threadpoolMgr ;
                threadpoolMgr = null ;
            }
        }

        if (tpToClose != null) {
            try {
                tpToClose.close() ;
            } catch (IOException exc) {
                wrapper.ioExceptionOnClose( exc ) ;
            }
        }

        CachedCodeBase.cleanCache( this ) ;
        try {
            pihandler.close() ;
        } catch (IOException exc) {
            wrapper.ioExceptionOnClose( exc ) ;
        }

        super.destroy() ;

        synchronized (this) {
            corbaContactInfoListFactoryAccessLock = null ; 
            corbaContactInfoListFactoryReadLock = null ;
            corbaContactInfoListFactoryWriteLock = null ;

            transportManager = null ;
            legacyServerSocketManager = null ;
            OAInvocationInfoStack  = null ; 
            clientInvocationInfoStack  = null ; 
            codeBase = null ; 
            codeBaseIOR = null ;
            dynamicRequests.clear() ;
            isProcessingInvocation = null ;
            typeCodeForClassMap  = null ;
            valueFactoryCache = null ;
            orbVersionThreadLocal = null ; 
            requestDispatcherRegistry = null ;
            copierManager = null ;
            serviceContextFactoryRegistry = null ;
            serviceContextsCache= null ;
            toaFactory = null ;
            poaFactory = null ;
            pihandler = null ;
            configData = null ;
            badServerIdHandler = null ;
            clientDelegateFactory = null ;
            corbaContactInfoListFactory = null ;
            resolver = null ;
            localResolver = null ;
            insNamingDelegate = null ;
            urlOperation = null ;
            taggedComponentFactoryFinder = null ;
            taggedProfileFactoryFinder = null ;
            taggedProfileTemplateFactoryFinder = null ;
            objectKeyFactory = null ;
            invocationInterceptor = null ;
            objectKeyCache.clear() ;
        }

        try {
            mom.close() ;
        } catch (Exception exc) {
            // ignore: stupid close exception
        }
    }

    /**
     * Registers a value factory for a particular repository ID.
     *
     * @param repositoryID the repository ID.
     * @param factory the factory.
     * @return the previously registered factory for the given repository ID, 
     * or null if no such factory was previously registered.
     * @exception org.omg.CORBA.BAD_PARAM if the registration fails.
     **/
    @Override
    public synchronized ValueFactory register_value_factory(String repositoryID, 
        ValueFactory factory) 
    {
        checkShutdownState();

        if ((repositoryID == null) || (factory == null)) {
            throw omgWrapper.unableRegisterValueFactory();
        }

        return valueFactoryCache.put(repositoryID, factory);
    }

    /**
     * Unregisters a value factory for a particular repository ID.
     *
     * @param repositoryID the repository ID.
     **/
    @Override
    public synchronized void unregister_value_factory(String repositoryID) 
    {
        checkShutdownState();

        if (valueFactoryCache.remove(repositoryID) == null) {
            throw wrapper.nullParam();
        }
    }

    /**
     * Finds and returns a value factory for the given repository ID.
     * The value factory returned was previously registered by a call to
     * {@link #register_value_factory} or is the default factory.
     *
     * @param repositoryID the repository ID.
     * @return the value factory.
     * @exception org.omg.CORBA.BAD_PARAM if unable to locate a factory.
     **/
    @Override
    public synchronized ValueFactory lookup_value_factory(String repositoryID) 
    {
        checkShutdownState();

        ValueFactory factory = valueFactoryCache.get(repositoryID);

        if (factory == null) {
            try {
                factory = Utility.getFactory(null, null, null, repositoryID);
            } catch(org.omg.CORBA.MARSHAL ex) {
                throw wrapper.unableFindValueFactory( ex ) ;
            }
        }

        return factory ;
    }

    public OAInvocationInfo peekInvocationInfo() {
        return OAInvocationInfoStack.get().peek() ;
    }

    public void pushInvocationInfo( OAInvocationInfo info ) {
        OAInvocationInfoStack.get().push( info ) ;
    }

    public OAInvocationInfo popInvocationInfo() {
        return OAInvocationInfoStack.get().pop() ;
    }

    /**
     * The bad server id handler is used by the Locator to
     * send back the location of a persistant server to the client.
     */

    private final Object badServerIdHandlerAccessLock = new Object();

    public void initBadServerIdHandler() 
    {
        synchronized (badServerIdHandlerAccessLock) {
            Class<?> cls = configData.getBadServerIdHandler() ;
            if (cls != null) {
                try {
                    Class<?>[] params = new Class<?>[] { org.omg.CORBA.ORB.class };
                    java.lang.Object[] args = new java.lang.Object[]{this};
                    Constructor<?> cons = cls.getConstructor(params);
                    badServerIdHandler = 
                        (BadServerIdHandler) cons.newInstance(args);
                } catch (Exception e) {
                    throw wrapper.errorInitBadserveridhandler( e ) ;
                }
            }
        }
    }

    public void setBadServerIdHandler( BadServerIdHandler handler ) 
    {
        synchronized (badServerIdHandlerAccessLock) {
            badServerIdHandler = handler;
        }
    }

    public void handleBadServerId( ObjectKey okey ) 
    {
        synchronized (badServerIdHandlerAccessLock) {
            if (badServerIdHandler == null) {
                throw wrapper.badServerId();
            } else {
                badServerIdHandler.handle(okey);
            }
        }
    }

    @Override
    public synchronized org.omg.CORBA.Policy create_policy( int type, 
        org.omg.CORBA.Any val ) throws org.omg.CORBA.PolicyError
    {
        checkShutdownState() ;

        return pihandler.create_policy( type, val ) ;
    }

    @Override
    public synchronized void connect(org.omg.CORBA.Object servant)
    {
        checkShutdownState();
        if (getTOAFactory() == null) {
            throw wrapper.noToa();
        }

        try {
            String codebase = Util.getInstance().getCodebase( servant.getClass() ) ;
            getTOAFactory().getTOA( codebase ).connect( servant ) ;
        } catch ( Exception ex ) {
            throw wrapper.orbConnectError( ex ) ;
        }
    }

    @Override
    public synchronized void disconnect(org.omg.CORBA.Object obj)
    {
        checkShutdownState();
        if (getTOAFactory() == null) {
            throw wrapper.noToa();
        }

        try {
            getTOAFactory().getTOA().disconnect( obj ) ;
        } catch ( Exception ex ) {
            throw wrapper.orbConnectError( ex ) ;
        }
    }

    public int getTransientServerId()
    {
        if( configData.getPersistentServerIdInitialized( ) ) {
            // ORBServerId is specified then use that value
            return configData.getPersistentServerId( );
        }
        return transientServerId;
    }

    public RequestDispatcherRegistry getRequestDispatcherRegistry()
    {
        return requestDispatcherRegistry;
    }

    public ServiceContextFactoryRegistry getServiceContextFactoryRegistry()
    {
        return serviceContextFactoryRegistry ;
    } 

    public ServiceContextsCache getServiceContextsCache() 
    {
        return serviceContextsCache;
    }

    // XXX All of the isLocalYYY checking needs to be revisited.
    // First of all, all three of these methods are called from
    // only one place in impl.ior.IORImpl.  Second, we have problems
    // both with multi-homed hosts and with multi-profile IORs.
    // A possible strategy: like the LocalClientRequestDispatcher, we need
    // to determine this more abstractly at the ContactInfo level.
    // This level should probably just get the CorbaContactInfoList from
    // the IOR, then iterator over ContactInfo.  If any ContactInfo is
    // local, the IOR is local, and we can pick one to create the 
    // LocalClientRequestDispatcher as well.  Bottom line: this code needs to move.
    public boolean isLocalHost( String hostName ) 
    {
        return hostName.equals( configData.getORBServerHost() ) ||
            hostName.equals( getLocalHostName() ) ;
    }

    @Subcontract
    public boolean isLocalServerId( int subcontractId, int serverId )
    {
        if (subcontractDebugFlag) {
            int psid = -1;
            if (configData.getPersistentServerIdInitialized()) {
                psid = configData.getPersistentServerId();
            }

            isLocalServerIdInfo( subcontractId, serverId, 
                 getTransientServerId(), 
                 ORBConstants.isTransient(subcontractId),
                 configData.getPersistentServerIdInitialized(), psid ) ;
        }

        if ((subcontractId < ORBConstants.FIRST_POA_SCID) || 
            (subcontractId > ORBConstants.MAX_POA_SCID)) {
            return serverId == getTransientServerId();
        }
                
        // XXX isTransient info should be stored in subcontract registry
        if (ORBConstants.isTransient( subcontractId )) {
            return serverId == getTransientServerId();
        } else if (configData.getPersistentServerIdInitialized()) {
            return serverId == configData.getPersistentServerId();
        } else {
            return false;
        }
    }

    /*************************************************************************
     *  The following public methods are for ORB shutdown.
     *************************************************************************/

    private String getHostName(String host) 
        throws java.net.UnknownHostException 
    {
        return InetAddress.getByName( host ).getHostAddress();
    }

    /* keeping a copy of the getLocalHostName so that it can only be called 
     * internally and the unauthorized clients cannot have access to the
     * localHost information, originally, the above code was calling 
     * getLocalHostName from Connection.java.  If the hostname is cached in 
     * Connection.java, then
     * it is a security hole, since any unauthorized client has access to
     * the host information.  With this change it is used internally so the
     * security problem is resolved.  Also in Connection.java, the 
     * getLocalHost() implementation has changed to always call the 
     * InetAddress.getLocalHost().getHostAddress()
     * The above mentioned method has been removed from the connection class
     */

    private static String localHostString = null;

    private synchronized String getLocalHostName() 
    {
        if (localHostString == null) {
            try {
                localHostString = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
                throw wrapper.getLocalHostFailed( ex ) ;
            }
        }
        return localHostString ;
    }

 /******************************************************************************
 *  The following public methods are for ORB shutdown. 
 *
 ******************************************************************************/

    /** This method always returns false because the ORB never needs the
     *  main thread to do work.
     */
    @Override
    public synchronized boolean work_pending()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }
  
    /** This method does nothing. It is not required by the spec to do anything!
     */
    @Override
    public synchronized void perform_work()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }

    @Override
    public synchronized void set_delegate(java.lang.Object servant){
        checkShutdownState();

        POAFactory pf = getPOAFactory() ;
        if (pf != null) {
            ((org.omg.PortableServer.Servant) servant)._set_delegate(pf.getDelegateImpl());
        } else {
            throw wrapper.noPoa();
        }
    }

    @InfoMethod
    private void invocationInfoChange( String msg ) { }

    @Subcontract
    public ClientInvocationInfo createOrIncrementInvocationInfo() {
        ClientInvocationInfo clientInvocationInfo = null;
        StackImpl<ClientInvocationInfo> invocationInfoStack =
            clientInvocationInfoStack.get();
        if (!invocationInfoStack.empty()) {
            clientInvocationInfo = invocationInfoStack.peek();
        }
        if ((clientInvocationInfo == null) ||
            (!clientInvocationInfo.isRetryInvocation()))
        {
            // This is a new call - not a retry.
            clientInvocationInfo = new InvocationInfo();
            invocationInfoStack.push(clientInvocationInfo);
            invocationInfoChange( "new call" ) ;
        } else {
            invocationInfoChange( "retry" ) ;
        }
        // Reset retry so recursive calls will get a new info object.
        clientInvocationInfo.setIsRetryInvocation(false);
        clientInvocationInfo.incrementEntryCount();
        return clientInvocationInfo;
    }
    
    @Subcontract
    public void releaseOrDecrementInvocationInfo() {
        int entryCount = -1;
        ClientInvocationInfo clientInvocationInfo = null;
        StackImpl<ClientInvocationInfo> invocationInfoStack =
            clientInvocationInfoStack.get();
        if (!invocationInfoStack.empty()) {
            clientInvocationInfo = invocationInfoStack.peek();
        } else {
            throw wrapper.invocationInfoStackEmpty() ;
        }

        clientInvocationInfo.decrementEntryCount();
        entryCount = clientInvocationInfo.getEntryCount();
        if (clientInvocationInfo.getEntryCount() == 0
            // 6763340: don't pop if this is a retry!
            && !clientInvocationInfo.isRetryInvocation()) {

            invocationInfoStack.pop();
            invocationInfoChange( "pop" ) ;
        }
    }
    
    public ClientInvocationInfo getInvocationInfo() {
        return clientInvocationInfoStack.get().peek() ;
    }

    ////////////////////////////////////////////////////
    //
    //
    //

    private final Object clientDelegateFactoryAccessorLock = new Object();

    public void setClientDelegateFactory( ClientDelegateFactory factory ) 
    {
        synchronized (clientDelegateFactoryAccessorLock) {
            clientDelegateFactory = factory ;
        }
    }

    public ClientDelegateFactory getClientDelegateFactory() 
    {
        synchronized (clientDelegateFactoryAccessorLock) {
            return clientDelegateFactory ;
        }
    }

    private ReentrantReadWriteLock 
          corbaContactInfoListFactoryAccessLock = new ReentrantReadWriteLock();
    private Lock corbaContactInfoListFactoryReadLock =
                               corbaContactInfoListFactoryAccessLock.readLock();
    private Lock corbaContactInfoListFactoryWriteLock = 
                              corbaContactInfoListFactoryAccessLock.writeLock();
    
    public void setCorbaContactInfoListFactory( ContactInfoListFactory factory )
    {
        corbaContactInfoListFactoryWriteLock.lock() ;
        try {
            corbaContactInfoListFactory = factory ;
        } finally {
            corbaContactInfoListFactoryWriteLock.unlock() ;
        }
    }

    public ContactInfoListFactory getCorbaContactInfoListFactory()
    {
        corbaContactInfoListFactoryReadLock.lock() ;
        try {
            return corbaContactInfoListFactory ;
        } finally {
            corbaContactInfoListFactoryReadLock.unlock() ;
        }
    }

    public void setResolver( Resolver resolver ) {
        synchronized (resolverLock) {
            this.resolver = resolver ;
        }
    }

    public Resolver getResolver() {
        synchronized (resolverLock) {
            return resolver ;
        }
    }

    public void setLocalResolver( LocalResolver resolver ) {
        synchronized (resolverLock) {
            this.localResolver = resolver ;
        }
    }

    public LocalResolver getLocalResolver() {
        synchronized (resolverLock) {
            return localResolver ;
        }
    }

    public void setURLOperation( Operation stringToObject ) {
        synchronized (urlOperationLock) {
            urlOperation = stringToObject ;
        }
    }

    public Operation getURLOperation() {
        synchronized (urlOperationLock) {
            return urlOperation ;
        }
    }

    public void setINSDelegate( ServerRequestDispatcher sdel ) {
        synchronized (resolverLock) {
            insNamingDelegate = sdel ;
        }
    }

    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() {
        return taggedComponentFactoryFinder ;
    }

    public IdentifiableFactoryFinder<TaggedProfile>
        getTaggedProfileFactoryFinder() {
        return taggedProfileFactoryFinder ;
    }

    public IdentifiableFactoryFinder<TaggedProfileTemplate>
        getTaggedProfileTemplateFactoryFinder() {
        return taggedProfileTemplateFactoryFinder ;
    }

    private final Object objectKeyFactoryAccessLock = new Object();

    public ObjectKeyFactory getObjectKeyFactory() 
    {
        synchronized (objectKeyFactoryAccessLock) {
            return objectKeyFactory ;
        }
    }

    public void setObjectKeyFactory( ObjectKeyFactory factory ) 
    {
        synchronized (objectKeyFactoryAccessLock) {
            objectKeyFactory = factory ;
        }
    }

    public TransportManager getTransportManager()
    {
        return transportManager;
    }

    public TransportManager getCorbaTransportManager()
    {
        return getTransportManager();
    }

    private final Object legacyServerSocketManagerAccessLock = new Object();

    public LegacyServerSocketManager getLegacyServerSocketManager()
    {
        synchronized (legacyServerSocketManagerAccessLock) {
            if (legacyServerSocketManager == null) {
                legacyServerSocketManager = new LegacyServerSocketManagerImpl(this);
            }
            return legacyServerSocketManager;
        }
    }

    private final Object threadPoolManagerAccessLock = new Object();

    public void setThreadPoolManager(ThreadPoolManager mgr) {
        synchronized (threadPoolManagerAccessLock) {
            threadpoolMgr = mgr;
        }
    }

    public ThreadPoolManager getThreadPoolManager() {
        synchronized (threadPoolManagerAccessLock) {
            if (threadpoolMgr == null) {
                threadpoolMgr = new ThreadPoolManagerImpl();
                orbOwnsThreadPoolManager = true ;
            }
            return threadpoolMgr;
        }
    }

    public CopierManager getCopierManager() {
        return copierManager ;
    }

    @Override
    public IOR getIOR( org.omg.CORBA.Object obj, boolean connectIfNecessary ) {
        IOR result ;

        if (connectIfNecessary) {
            try {
                result = getIOR( obj ) ;
            } catch (BAD_OPERATION bop) {
                if (StubAdapter.isStub(obj)) {
                    try {
                        StubAdapter.connect( obj, this ) ;
                    } catch (java.rmi.RemoteException exc) {
                        throw wrapper.connectingServant( exc ) ;
                    }
                } else {
                    connect( obj ) ;
                }

                result = getIOR( obj ) ;
            }
        } else {
            // Let any exceptions propagate out
            result = getIOR( obj ) ;
        }
    
        return result ;
    }
    
    @Override
    public ObjectKeyCacheEntry extractObjectKeyCacheEntry(byte[] objKey) {
        if (objKey == null) {
            throw wrapper.invalidObjectKey();
        }

        ByteArrayWrapper newObjKeyWrapper = new ByteArrayWrapper(objKey);

        return objectKeyCache.get( newObjKeyWrapper ) ;
    }

    @Override
    public synchronized boolean orbIsShutdown() {
        return ((status == STATUS_DESTROYED) || 
            (status == STATUS_SHUTDOWN)) ;
    }
} // Class ORBImpl

////////////////////////////////////////////////////////////////////////
/// Helper class for a Synchronization Variable
////////////////////////////////////////////////////////////////////////

class SynchVariable {
    // Synchronization Variable
    public boolean _flag;

    // Constructor
    SynchVariable() {
        _flag = false;
    }

    // set Flag to true
    public void set() {
        _flag = true;
    }

        // get value
    public boolean value() {
        return _flag;
    }

    // reset Flag to true
    public void reset() {
        _flag = false;
    }
}

// End of file.
