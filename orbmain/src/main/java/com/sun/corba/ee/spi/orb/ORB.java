/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb;

import com.sun.corba.ee.impl.corba.TypeCodeFactory;
import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.ior.WireObjectKeyTemplate;
import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.ee.impl.transport.ByteBufferPoolImpl;
import com.sun.corba.ee.spi.copyobject.CopierManager;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBClassLoader;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor;
import com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.protocol.ClientDelegateFactory;
import com.sun.corba.ee.spi.protocol.ClientInvocationInfo;
import com.sun.corba.ee.spi.protocol.PIHandler;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.resolver.LocalResolver;
import com.sun.corba.ee.spi.resolver.Resolver;
import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry;
import com.sun.corba.ee.spi.servicecontext.ServiceContextsCache;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.trace.*;
import com.sun.corba.ee.spi.trace.Shutdown;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.TransportManager;
import org.glassfish.gmbal.*;
import org.glassfish.pfl.basic.func.UnaryFunction;
import org.glassfish.pfl.tf.spi.MethodMonitorFactoryDefaults;
import org.glassfish.pfl.tf.spi.MethodMonitorRegistry;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.glassfish.pfl.tf.spi.annotation.MethodMonitorGroup;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.PortableServer.Servant;

import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

@OrbLifeCycle
@ManagedObject
@Description( "The Main ORB Implementation object" ) 
@AMXMetadata( type="ORB-Root" )
public abstract class ORB extends com.sun.corba.ee.org.omg.CORBA.ORB
    implements TypeCodeFactory
{   
    static {
        MethodMonitorFactoryDefaults.addPrefix( "com.sun.corba.ee", "ORB" ) ;
    }

    // As much as possible, this class should be stateless.  However,
    // there are a few reasons why it is not:
    //
    // 1. The ORB debug flags are defined here because they are accessed
    //    frequently, and we do not want a cast to the impl just for that.
    // 2. typeCodeMap and primitiveTypeCodeConstants are here because they
    //    are needed in both ORBImpl and ORBSingleton.
    // 3. Logging support is here so that we can avoid problems with
    //    incompletely initialized ORBs that need to perform logging.
    
    // This is not one of the xxxDebugFlags because it is used to debug the mechanism
    // that sets the xxxDebugFlags!
    public static final boolean orbInitDebug = AccessController.doPrivileged( 
        new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.getBoolean( ORBConstants.INIT_DEBUG_PROPERTY );
            }
        }
    ) ;

    // Currently defined debug flags.  Any additions must be called xxxDebugFlag.
    // All debug flags must be public boolean types.
    // These are set by passing the flag -ORBDebug x,y,z in the ORB init args.
    // Note that x,y,z must not contain spaces.
    // 
    // The annotations (when present) connect the ORB debug flags to the tracing
    // system.  Whenever a flag is set, the corresponding tracing annotation
    // is also set in the MethodMonitorRegistry to a standard tracing
    // MethodMonitorFactory.  A few cases still make direct access to this
    // flags, and the flags are much faster to test than the state of the
    // MethodMonitorRegistry.

    @Transport
    public boolean transportDebugFlag = false ;

    @Subcontract
    public boolean subcontractDebugFlag = false ;

    @Osgi
    public boolean osgiDebugFlag = false ;

    @Poa
    public boolean poaDebugFlag = false ;
    
    @PoaFSM
    public boolean poaFSMDebugFlag = false ;

    @Orbd
    public boolean orbdDebugFlag = false ;

    @Naming
    public boolean namingDebugFlag = false ;

    @TraceServiceContext
    public boolean serviceContextDebugFlag = false ;

    @TransientObjectManager
    public boolean transientObjectManagerDebugFlag = false ;

    @Shutdown
    public boolean shutdownDebugFlag = false;

    @Giop
    public boolean giopDebugFlag = false;

    public boolean giopSizeDebugFlag = false;
    public boolean giopReadDebugFlag = false;

    @TraceInterceptor
    public boolean interceptorDebugFlag = false ;

    @Folb
    public boolean folbDebugFlag = false ;

    public boolean cdrCacheDebugFlag = false ;

    @Cdr
    public boolean cdrDebugFlag = false ;

    @StreamFormatVersion
    public boolean streamFormatVersionDebugFlag = false ;

    @TraceValueHandler
    public boolean valueHandlerDebugFlag = false ;

    public boolean mbeanDebugFlag = false ;
    public boolean mbeanFineDebugFlag = false ;
    public boolean mbeanRuntimeDebugFlag = false ;

    @OrbLifeCycle
    public boolean orbLifecycleDebugFlag = false ;

    public boolean operationTraceDebugFlag = false ;

    @DynamicType
    public boolean dynamicTypeDebugFlag = false ;

    @IsLocal
    public boolean isLocalDebugFlag = false ;

    @ManagedAttribute
    @Description( "The current settings of the ORB debug flags" )
    private Map<String,Boolean> getDebugFlags() {
        Map<String,Boolean> result = new HashMap<String,Boolean>() ;
        for (Field fld : this.getClass().getFields()) {
            if (fld.getName().endsWith("DebugFlag")) {
                Boolean value = false ;
                try {
                    value = fld.getBoolean( this );
                    result.put( fld.getName(), value ) ;
                } catch (Exception exc) {
                }
            }
        }

        return result ;
    }

    @InfoMethod
    private void mbeanRegistrationSuspended(String oRBId) { }

    public enum DebugFlagResult { OK, BAD_NAME }

    @ManagedOperation
    @Description( "Enable debugging for several ORB debug flags")
    public DebugFlagResult setDebugFlags( String... names ) {
        return setDebugFlags( true, names ) ;
    }

    @ManagedOperation
    @Description( "Enable debugging for a particular ORB debug flag")
    public DebugFlagResult setDebugFlag( String name ) {
        return setDebugFlag( name, true ) ;
    }

    @ManagedOperation
    @Description( "Disable debugging for several ORB debug flags")
    public DebugFlagResult clearDebugFlags( String... names ) {
        return setDebugFlags( false, names ) ;
    }

    @ManagedOperation
    @Description( "Disable debugging for a particular ORB debug flag")
    public DebugFlagResult clearDebugFlag( String name ) {
        return setDebugFlag( name, false ) ;
    }
   
    private DebugFlagResult setDebugFlags( boolean flag, String... names ) {
        DebugFlagResult res = DebugFlagResult.OK ;
        for (String name : names) {
            DebugFlagResult lres = setDebugFlag( name, flag ) ;
            if (lres == DebugFlagResult.BAD_NAME) {
                res = DebugFlagResult.BAD_NAME ;
            }
        }
        return res ;
    }

    private DebugFlagResult setDebugFlag( String name, boolean value ) {
        try {
            Field fld = this.getClass().getField( name + "DebugFlag" ) ;
            fld.set( this, value ) ;

            Annotation[] annots = fld.getAnnotations() ;
            for (Annotation anno : annots) {
                Class<? extends Annotation> annoClass = anno.annotationType() ;

                if (annoClass.isAnnotationPresent(
                    MethodMonitorGroup.class )) {
                    if (value) {
                        MethodMonitorRegistry.register( annoClass,
                            MethodMonitorFactoryDefaults.dprint() );
                    } else {
                        MethodMonitorRegistry.clear( annoClass ) ;
                    }
                }
            }

            return DebugFlagResult.OK ;
        } catch (Exception exc) {
            return DebugFlagResult.BAD_NAME ;
        }
    }

    // mom MUST be initialized in a subclass by calling createManagedObjectManager.
    // In ORBSingleton, this happens in the constructor.  It ORBImpl, it cannot
    // happen in the constructor: instead, it must be called in post_init.
    protected ManagedObjectManager mom ;
    
    // SystemException log wrappers.  Protected so that they can be used in
    // subclasses.
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    protected static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    // This map is needed for resolving recursive type code placeholders
    // based on the unique repository id.
    private Map<String,TypeCodeImpl> typeCodeMap ;

    private TypeCodeImpl[] primitiveTypeCodeConstants ;

    // ByteBufferPool - needed by both ORBImpl and ORBSingleton
    ByteBufferPool byteBufferPool;

    // Cached WireObjectKeyTemplate singleton.
    WireObjectKeyTemplate wireObjectKeyTemplate;

    // Local testing
    public abstract boolean isLocalHost( String hostName ) ;
    public abstract boolean isLocalServerId( int subcontractId, int serverId ) ;

    // Invocation stack manipulation
    public abstract OAInvocationInfo peekInvocationInfo() ;
    public abstract void pushInvocationInfo( OAInvocationInfo info ) ;
    public abstract OAInvocationInfo popInvocationInfo() ;

    @ManagedAttribute
    @Description( "The ORB's transport manager" ) 
    public abstract TransportManager getCorbaTransportManager();

    public abstract LegacyServerSocketManager getLegacyServerSocketManager();

    private static PresentationManager presentationManager = PresentationDefaults.makeOrbPresentationManager();

    private UnaryFunction<String,Class<?>> classNameResolver = defaultClassNameResolver ;
    private ClassCodeBaseHandler ccbHandler = null ;

    @Override
    public synchronized void destroy() {
        typeCodeMap = null ;
        primitiveTypeCodeConstants = null ;
        byteBufferPool = null ;
        wireObjectKeyTemplate = null ;
    }

    /**
     * Returns the Presentation Manager for the current thread group, using the ThreadGroup-specific
     * AppContext to hold it. Creates and records one if needed.
     * @return The PresentationManager.
     */
    @ManagedAttribute
    @Description( "The presentation manager, which handles stub creation" ) 
    public static PresentationManager getPresentationManager() 
    {
        /**/
        return presentationManager;
        /*/
    	AppContext ac = AppContext.getAppContext();
        PresentationManager pm = (PresentationManager) ac.get(PresentationManager.class);
        if (pm == null) {
            pm = PresentationDefaults.makeOrbPresentationManager() ;
            ac.put(PresentationManager.class, pm);
        }

        return pm;
        /**/
    }

    /** Get the appropriate StubFactoryFactory.  This 
     * will be dynamic or static depending on whether
     * com.sun.corba.ee.ORBUseDynamicStub is true or false.
     * @return The stub factory factory.
     */
    public static PresentationManager.StubFactoryFactory 
        getStubFactoryFactory()
    {
    	PresentationManager gPM = getPresentationManager();
        boolean useDynamicStubs = gPM.useDynamicStubs() ;
        return useDynamicStubs ? gPM.getDynamicStubFactoryFactory() : gPM.getStaticStubFactoryFactory();
    }

    /** Obtain the InvocationInterceptor for this ORB instance.
     * By default this does nothing.  
     * @return The InvocationInterceptor.
     */
    public abstract InvocationInterceptor getInvocationInterceptor() ;

    /** Set the InvocationInterceptor for this ORB instance.
     * This will be used around all dynamic RMI-IIOP calls that
     * are mediated by this ORB instance.
     * @param interceptor The InvocationInterceptor to add.
     */
    public abstract void setInvocationInterceptor( 
        InvocationInterceptor interceptor ) ;
    
    protected ORB()
    {

        typeCodeMap = new HashMap<String,TypeCodeImpl>();

        wireObjectKeyTemplate = new WireObjectKeyTemplate(this);
    }

    protected void initializePrimitiveTypeCodeConstants() {
        primitiveTypeCodeConstants = new TypeCodeImpl[] {
            new TypeCodeImpl(this, TCKind._tk_null),    
            new TypeCodeImpl(this, TCKind._tk_void),
            new TypeCodeImpl(this, TCKind._tk_short),           
            new TypeCodeImpl(this, TCKind._tk_long),    
            new TypeCodeImpl(this, TCKind._tk_ushort),  
            new TypeCodeImpl(this, TCKind._tk_ulong),   
            new TypeCodeImpl(this, TCKind._tk_float),   
            new TypeCodeImpl(this, TCKind._tk_double),  
            new TypeCodeImpl(this, TCKind._tk_boolean), 
            new TypeCodeImpl(this, TCKind._tk_char),    
            new TypeCodeImpl(this, TCKind._tk_octet),
            new TypeCodeImpl(this, TCKind._tk_any),     
            new TypeCodeImpl(this, TCKind._tk_TypeCode),        
            new TypeCodeImpl(this, TCKind._tk_Principal),
            new TypeCodeImpl(this, TCKind._tk_objref),  
            null,       // tk_struct    
            null,       // tk_union     
            null,       // tk_enum      
            new TypeCodeImpl(this, TCKind._tk_string),          
            null,       // tk_sequence  
            null,       // tk_array     
            null,       // tk_alias     
            null,       // tk_except    
            new TypeCodeImpl(this, TCKind._tk_longlong),        
            new TypeCodeImpl(this, TCKind._tk_ulonglong),
            new TypeCodeImpl(this, TCKind._tk_longdouble),
            new TypeCodeImpl(this, TCKind._tk_wchar),           
            new TypeCodeImpl(this, TCKind._tk_wstring), 
            new TypeCodeImpl(this, TCKind._tk_fixed),   
            new TypeCodeImpl(this, TCKind._tk_value),   
            new TypeCodeImpl(this, TCKind._tk_value_box),
            new TypeCodeImpl(this, TCKind._tk_native),  
            new TypeCodeImpl(this, TCKind._tk_abstract_interface)
        } ;
    }

    // Typecode support: needed in both ORBImpl and ORBSingleton
    public TypeCodeImpl get_primitive_tc(int kind) 
    {
        try {
            return primitiveTypeCodeConstants[kind] ;
        } catch (Throwable t) {
            throw wrapper.invalidTypecodeKind( t, kind ) ;
        }
    }

    public synchronized void setTypeCode(String id, TypeCodeImpl code) 
    {
        typeCodeMap.put(id, code);
    }

    public synchronized TypeCodeImpl getTypeCode(String id) 
    {
        return typeCodeMap.get(id);
    }

    // Special non-standard set_parameters method for
    // creating a precisely controlled ORB instance.
    // An ORB created by this call is affected only by
    // those properties passes explicitly in props, not by
    // the system properties and orb.properties files as
    // with the standard ORB.init methods.
    public abstract void set_parameters( Properties props ) ;

    // Added to provide an API for creating an ORB that avoids the org.omg.CORBA.ORB API
    // to get around an OSGi problem.
    public abstract void setParameters( String[] args, Properties props ) ;

    // ORB versioning
    @ManagedAttribute
    @Description( "The implementation version of the ORB" )
    public abstract ORBVersion getORBVersion() ;

    public abstract void setORBVersion( ORBVersion version ) ;

    @ManagedAttribute
    @Description( "The IOR used for the Full Value Description" ) 
    public abstract IOR getFVDCodeBaseIOR() ;

    /**
     * Handle a bad server id for the given object key.  This should 
     * always through an exception: either a ForwardException to
     * allow another server to handle the request, or else an error
     * indication.  
     * @param okey The ObjectKey to check for a valid server id.
     */
    public abstract void handleBadServerId( ObjectKey okey ) ;
    public abstract void setBadServerIdHandler( BadServerIdHandler handler ) ;
    public abstract void initBadServerIdHandler() ;
    
    public abstract void notifyORB() ;

    @ManagedAttribute 
    @Description( "The PortableInterceptor Handler" ) 
    public abstract PIHandler getPIHandler() ;

    public abstract void createPIHandler() ;

    // Dispatch support: in the ORB because it is needed for shutdown.
    // This is used by the first level server side subcontract.
    public abstract boolean isDuringDispatch() ;
    public abstract void startingDispatch();
    public abstract void finishedDispatch();

    /** Return this ORB's transient server ID.  This is needed for 
     * initializing object adapters.
     * @return The transient server id.
     */
    @ManagedAttribute
    @Description( "The transient ServerId of this ORB instance" ) 
    public abstract int getTransientServerId();

    @ManagedAttribute
    @Description( "The registry for all ServerContext factories" ) 
    public abstract ServiceContextFactoryRegistry getServiceContextFactoryRegistry() ;

    @ManagedAttribute
    @Description( "The cache used to opimize marshaling of ServiceContexts" ) 
    public abstract ServiceContextsCache getServiceContextsCache();

    @ManagedAttribute
    @Description( "The RequestDispatcher registry, which contains the request handling code" ) 
    public abstract RequestDispatcherRegistry getRequestDispatcherRegistry();

    @ManagedAttribute
    @Description( "The ORB configuration data" ) 
    public abstract ORBData getORBData() ;

    public abstract void setClientDelegateFactory( ClientDelegateFactory factory ) ;

    @ManagedAttribute
    @Description( "The ClientDelegateFactory, which is used to create the ClientDelegate that represents an IOR" )
    public abstract ClientDelegateFactory getClientDelegateFactory() ;

    public abstract void setCorbaContactInfoListFactory( ContactInfoListFactory factory ) ;

    @ManagedAttribute
    @Description( "The CorbaContactInfoListFactory, which creates the contact info list that represents "
        + "possible endpoints in an IOR" ) 
    public abstract ContactInfoListFactory getCorbaContactInfoListFactory() ;

    /** Set the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     * 
     * @param resolver resolver to be used
     */
    public abstract void setResolver( Resolver resolver ) ;

    /** Get the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     * 
     * @return ORB Name resolver
     */
    @ManagedAttribute
    @Description( "ORB Name resolver" ) 
    public abstract Resolver getResolver() ;

    /** Set the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     * 
     * @param resolver ORB Local Name resolver
     */
    public abstract void setLocalResolver( LocalResolver resolver ) ;

    /** Get the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     * 
     * @return ORB Local Name resolver
     */
    @ManagedAttribute
    @Description( "ORB Local Name resolver" ) 
    public abstract LocalResolver getLocalResolver() ;

    /** Set the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     * 
     * @param operation to be used
     */
    public abstract void setURLOperation( Operation stringToObject ) ;

    /** Get the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     * 
     * @return operation used
     */
    public abstract Operation getURLOperation() ;

    /** Set the ServerRequestDispatcher that should be used for handling INS requests.
     * 
     * @param insDelegate dispatcher to be used
     */
    public abstract void setINSDelegate( ServerRequestDispatcher insDelegate ) ;

    /** Factory finders for the various parts of the IOR: tagged components, tagged
     * profiles, and tagged profile templates.
     * 
     * @return Finder of Factories for TaggedComponents of IORs
     */
    @ManagedAttribute
    @Description( "Finder of Factories for TaggedComponents of IORs" )
    public abstract TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() ;

    @ManagedAttribute
    @Description( "Finder of Factories for TaggedProfiles of IORs" )
    public abstract IdentifiableFactoryFinder<TaggedProfile> 
        getTaggedProfileFactoryFinder() ;

    @ManagedAttribute
    @Description( "Finder of Factories for TaggedProfileTemplates of IORs" )
    public abstract IdentifiableFactoryFinder<TaggedProfileTemplate> 
        getTaggedProfileTemplateFactoryFinder() ;

    @ManagedAttribute
    @Description( "Factory for creating ObjectKeys" )
    public abstract ObjectKeyFactory getObjectKeyFactory() ;

    public abstract void setObjectKeyFactory( ObjectKeyFactory factory ) ;

    // Logging SPI

    public static Logger getLogger( String name ) 
    {
        return Logger.getLogger( name, ORBConstants.LOG_RESOURCE_FILE ) ;
    }

    // get a reference to a ByteBufferPool, a pool of NIO ByteBuffers
    // NOTE: ByteBuffer pool must be unique per ORB, not per process.
    //       There can be more than one ORB per process.
    //       This method must also be inherited by both ORB and ORBSingleton.
    @ManagedAttribute
    @Description( "The ByteBuffer pool used in the ORB" ) 
    public ByteBufferPool getByteBufferPool()
    {
        if (byteBufferPool == null)
            byteBufferPool = new ByteBufferPoolImpl(this);

        return byteBufferPool;
    }

    public WireObjectKeyTemplate getWireObjectKeyTemplate() {
        return wireObjectKeyTemplate;
    }

    public abstract void setThreadPoolManager(ThreadPoolManager mgr);

    @ManagedAttribute
    @Description( "The ORB's threadpool manager" ) 
    public abstract ThreadPoolManager getThreadPoolManager();

    @ManagedAttribute
    @Description( "The ORB's object copier manager" ) 
    public abstract CopierManager getCopierManager() ;

    /** Returns a name for this ORB that is based on the ORB id (if any)
     * and guaranteed to be unique within the ClassLoader that loaded the
     * ORB class.  This is the default implementation inherited by the
     * ORBSingleton.
     * 
     * @return a unique name
     */
    @NameValue
    public String getUniqueOrbId()  {
        return "###DEFAULT_UNIQUE_ORB_ID###" ;
    }
    
    // Interfaces used only to define InheritedAttributes for other classes
    // If we register a class that has Servant in its inheritance, it will
    // pick up these InheritedAttributes.
    @ManagedData
    @Description( "A servant, which implements a remote object in the server" )
    @InheritedAttributes( {
        @InheritedAttribute( methodName="_get_delegate", id="delegate", 
            description="Delegate that implements this servant" ),
        @InheritedAttribute( methodName="_orb", id="orb",
            description="The ORB for this Servant" ),
        @InheritedAttribute( methodName="toString", id="representation",
            description="Representation of this Servant" ),
        @InheritedAttribute( methodName="_all_interfaces", id="typeIds",
            description="The types implemented by this Servant" ) } 
    )
    public interface DummyServant{}

    // DummyDelegate
    // DummyORB
    // DummyPOA

    private ObjectName rootParentObjectName = null ;

    public void setRootParentObjectName( ObjectName oname ) {
        rootParentObjectName = oname ;
    }

    @OrbLifeCycle
    public void createORBManagedObjectManager() {
        if (rootParentObjectName == null) {
            mom = ManagedObjectManagerFactory.createStandalone( "com.sun.corba" ) ;
        } else {
            mom = ManagedObjectManagerFactory.createFederated( rootParentObjectName ) ;
        }

        if (mbeanFineDebugFlag) {
            mom.setRegistrationDebug( ManagedObjectManager.RegistrationDebugLevel.FINE ) ;
        } else if (mbeanDebugFlag) {
            mom.setRegistrationDebug( ManagedObjectManager.RegistrationDebugLevel.NORMAL ) ;
        } else {
            mom.setRegistrationDebug( ManagedObjectManager.RegistrationDebugLevel.NONE ) ;
        }

        mom.addAnnotation( Servant.class, DummyServant.class.getAnnotation( ManagedData.class ) ) ;
        mom.addAnnotation( Servant.class, DummyServant.class.getAnnotation( Description.class ) ) ;
        mom.addAnnotation( Servant.class, DummyServant.class.getAnnotation( InheritedAttributes.class ) ) ;

        mom.setRuntimeDebug( mbeanRuntimeDebugFlag ) ;

        mom.stripPrefix( "com.sun.corba.ee", "com.sun.corba.ee.spi", "com.sun.corba.ee.spi.orb", 
            "com.sun.corba.ee.impl" ) ;

        mom.suspendJMXRegistration() ;

        mbeanRegistrationSuspended( getORBData().getORBId() ) ;

        mom.createRoot( this, getUniqueOrbId() ) ;
    }
    
    /** This method obtains an IOR from a CORBA object reference.
     * The result is never null.
     * @param obj CORBA object reference
     * @return obtained IOR
     * @throws org.omg.CORBA.BAD_OPERATION (from oi._get_delegate) if obj is a
     * normal objref, but does not have a delegate set.
     * @throws org.omg.CORBA.BAD_PARAM if obj is a local object
    */ 
    protected IOR getIOR( org.omg.CORBA.Object obj ) 
    {
        if (obj == null)
            throw wrapper.nullObjectReference() ;

        IOR ior = null ;
        if (StubAdapter.isStub(obj)) {
            org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate( 
                obj ) ;

            if (del instanceof ClientDelegate) {
                ClientDelegate cdel = (ClientDelegate)del ;
                ContactInfoList ccil = cdel.getContactInfoList() ;
                ior = ccil.getTargetIOR() ;
                if (ior == null)
                    throw wrapper.nullIor() ;

                return ior ;
            } 

            if (obj instanceof ObjectImpl) {
                // Get the ORB instance of obj so we can use that ORB
                // to marshal the object.
                ObjectImpl oi = ObjectImpl.class.cast( obj ) ;
                org.omg.CORBA.ORB oiorb = oi._orb() ;

                // obj is implemented by a foreign ORB, because the Delegate is not a
                // CorbaClientDelegate.  Here we need to marshal obj to an output stream,
                // then read the IOR back in.  Note that the output stream MUST be
                // created by the ORB to which obj is attached, otherwise we get an
                // infinite recursion between this code and 
                // CDROutputStream_1_0.write_Object.
                org.omg.CORBA.portable.OutputStream os = oiorb.create_output_stream() ;
                os.write_Object( obj ) ;
                org.omg.CORBA.portable.InputStream is = os.create_input_stream() ;
                ior = IORFactories.makeIOR( this,  
                    org.omg.CORBA_2_3.portable.InputStream.class.cast( is ) ) ; 
                return ior ;
            } else {
                throw wrapper.notAnObjectImpl() ;
            }
        } else
            throw wrapper.localObjectNotAllowed() ;
    }


    /** Get the IOR for the CORBA object.  If the object is an RMI-IIOP object that
     * is not connected, and connectIfNecessary is true, connect to this ORB.
     * This method will obtain an IOR for any non-local CORBA object, regardless of
     * what ORB implementation created it.  It may be more efficient for objrefs
     * that were created by this ORB implementation.
     *
     * @param obj CORBA object to get IOR for
     * @param connectIfNecessary connect to RMI-IIOP if not already
     * @return obtained IOR
     * @exception SystemException (nullObjectReference) if obj is null
     * @exception SystemException (localObjectNotAllowed) of obj is a local CORBA object.
     */
    public IOR getIOR( org.omg.CORBA.Object obj, boolean connectIfNecessary ) {
        // Note: this version ignores connectIfNecessary, since an objref can only
        // be connected to an ORBImpl, not an ORBSingleton.
        return getIOR( obj ) ;
    }

    /** The singleton ORB does not need the cache, so just return null here.
     * @param objKey ignored
     * @return null
     */
    public ObjectKeyCacheEntry extractObjectKeyCacheEntry(byte[] objKey) {
        return null ;
    }

    /** Return whether or not the ORB is shutdown.  A shutdown ORB cannot process
     * incoming requests.
     * @return true
     */
    public boolean orbIsShutdown() {
        return true ;
    }

    private static UnaryFunction<String,Class<?>> defaultClassNameResolver =
        new UnaryFunction<String,Class<?>>() {
            public Class<?> evaluate( String name ) {
                try {
                    return ORBClassLoader.getClassLoader().loadClass( name ) ;
                } catch (ClassNotFoundException exc) {
                    throw new RuntimeException( exc ) ;
                }
            }

            @Override
            public String toString() {
                return "ORBClassNameResolver" ;
            }
        } ;

    public static UnaryFunction<String,Class<?>> defaultClassNameResolver() {
        return defaultClassNameResolver ;
    }

    public UnaryFunction<String,Class<?>> makeCompositeClassNameResolver(
        final UnaryFunction<String,Class<?>> first,
        final UnaryFunction<String,Class<?>> second ) {

        return new UnaryFunction<String,Class<?>>() {
            public Class<?> evaluate( String className ) {
                Class<?> result = first.evaluate( className ) ;
                if (result == null) {
                    return second.evaluate( className ) ;
                } else {
                    return result ;
                }
            }

            @Override
            public String toString() {
                return "CompositeClassNameResolver[" + first + "," + second + "]" ;
            }
        } ;
    }

    public UnaryFunction<String,Class<?>> classNameResolver() {
        return classNameResolver ;
    }

    public void classNameResolver( UnaryFunction<String,Class<?>> arg ) {
        classNameResolver = arg ;
    }

    public ManagedObjectManager mom() {
        return mom ;
    }

    public ClassCodeBaseHandler classCodeBaseHandler() {
        return ccbHandler ;
    }

    public void classCodeBaseHandler( ClassCodeBaseHandler ccbh ) {
        ccbHandler = ccbh ;
    }

    public abstract ClientInvocationInfo createOrIncrementInvocationInfo() ;
    public abstract ClientInvocationInfo getInvocationInfo();
    public abstract void releaseOrDecrementInvocationInfo();

    public abstract TransportManager getTransportManager();

    
}

// End of file.
