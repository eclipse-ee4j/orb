/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payaera Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb;

import java.util.Properties;

import java.applet.Applet;

import java.net.URL;



import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.NVList;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.Request;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.ValueMember;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;

import org.omg.CORBA.portable.OutputStream;

import com.sun.corba.ee.spi.protocol.ClientInvocationInfo ;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ConnectionCache;
import com.sun.corba.ee.spi.transport.Selector ;
import com.sun.corba.ee.spi.transport.TransportManager;

import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.Operation;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.protocol.ClientDelegateFactory;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.protocol.PIHandler;
import com.sun.corba.ee.spi.resolver.Resolver;
import com.sun.corba.ee.spi.resolver.LocalResolver;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory ;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.copyobject.CopierManager;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults;

import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry;
import com.sun.corba.ee.spi.servicecontext.ServiceContextsCache;

import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.corba.NVListImpl;
import com.sun.corba.ee.impl.corba.NamedValueImpl;
import com.sun.corba.ee.impl.corba.ExceptionListImpl;
import com.sun.corba.ee.impl.corba.ContextListImpl;
import com.sun.corba.ee.impl.corba.EnvironmentImpl;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.impl.encoding.BufferManagerFactory;
import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import org.glassfish.pfl.basic.func.NullaryFunction;

/**
 * The restricted singleton ORB implementation.
 *
 * For now, this class must implement just enough functionality to be
 * used as a factory for immutable TypeCode instances.
 *
 * See ORBImpl.java for the real ORB implementation.
 * @see ORBImpl
 */
public class ORBSingleton extends ORB
{
    // This is used to support read_Object.
    private static ORB fullORB;
    private static PresentationManager.StubFactoryFactory staticStubFactoryFactory =
        PresentationDefaults.getStaticStubFactoryFactory() ;
    // private TimerManager timerManager ;

    public ORBSingleton() {
        // timerManager = makeTimerManager( null ) ;
        initializePrimitiveTypeCodeConstants() ;
    }

    @Override
    public void setParameters( String params[], Properties props ) {
        // this is never called by ORB.init() 
    }

    @Override
    public void set_parameters( Properties props ) {
        // this is never called by ORB.init() 
    }

    @Override
    protected void set_parameters(Applet app, Properties props) {
        // this is never called by ORB.init() 
    }

    @Override
    protected void set_parameters (String params[], Properties props) {
        // this is never called by ORB.init() 
    }

    @Override
    public OutputStream create_output_stream() {
        return OutputStreamFactory.newEncapsOutputStream(this);
    }

    @Override
    public TypeCode create_struct_tc(String id,
                                     String name,
                                     StructMember[] members)
    {
        return new TypeCodeImpl(this, TCKind._tk_struct, id, name, members);
    }
  
    @Override
    public TypeCode create_union_tc(String id,
                                    String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members)
    {
        return new TypeCodeImpl(this,
                                TCKind._tk_union, 
                                id, 
                                name, 
                                discriminator_type, 
                                members);
    }
  
    @Override
    public TypeCode create_enum_tc(String id, String name, String[] members)
    {
        return new TypeCodeImpl(this, TCKind._tk_enum, id, name, members);
    }
  
    @Override
    public TypeCode create_alias_tc(String id, String name, TypeCode original_type)
    {
        return new TypeCodeImpl(this, TCKind._tk_alias, id, name, original_type);
    }
  
    @Override
    public TypeCode create_exception_tc(String id, String name, StructMember[] members)
    {
        return new TypeCodeImpl(this, TCKind._tk_except, id, name, members);
    }
  
    @Override
    public TypeCode create_interface_tc(String id, String name)
    {
        return new TypeCodeImpl(this, TCKind._tk_objref, id, name);
    }
  
    @Override
    public TypeCode create_string_tc(int bound) {
        return new TypeCodeImpl(this, TCKind._tk_string, bound);
    }
  
    @Override
    public TypeCode create_wstring_tc(int bound) {
        return new TypeCodeImpl(this, TCKind._tk_wstring, bound);
    }
  
    @Override
    public TypeCode create_sequence_tc(int bound,
                                       TypeCode element_type)
    {
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, element_type);
    }
  
    @Override
    public TypeCode create_recursive_sequence_tc(int bound, int offset)
    {
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, offset);
    }
  
    @Override
    public TypeCode create_array_tc(int length, TypeCode element_type)
    {
        return new TypeCodeImpl(this, TCKind._tk_array, length, element_type);
    }

    @Override
    public org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name)
    {
        return new TypeCodeImpl(this, TCKind._tk_native, id, name);
    }

    @Override
    public org.omg.CORBA.TypeCode create_abstract_interface_tc(
                                                               String id,
                                                               String name)
    {
        return new TypeCodeImpl(this, TCKind._tk_abstract_interface, id, name);
    }

    @Override
    public org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale)
    {
        return new TypeCodeImpl(this, TCKind._tk_fixed, digits, scale);
    }

    // orbos 98-01-18: Objects By Value -- begin

    @Override
    public org.omg.CORBA.TypeCode create_value_tc(String id,
                                                  String name,
                                                  short type_modifier,
                                                  TypeCode concrete_base,
                                                  ValueMember[] members)
    {
        return new TypeCodeImpl(this, TCKind._tk_value, id, name,
                                type_modifier, concrete_base, members);
    }

    @Override
    public org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        return new TypeCodeImpl(this, id);
    }

    @Override
    public org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type)
    {
        return new TypeCodeImpl(this, TCKind._tk_value_box, id, name, boxed_type);
    }

    @Override
    public TypeCode get_primitive_tc( TCKind tckind )
    {
        return get_primitive_tc( tckind.value() ) ;
    }

    @Override
    public Any create_any() {
        return new AnyImpl(this);
    }

    // TypeCodeFactory interface methods.
    // Keeping track of type codes by repository id.
    /*
     * Not strictly needed for TypeCode factory duty but these seem
     * harmless enough.
     */

    @Override
    public NVList create_list(int count) {
        return new NVListImpl(this, count);
    }

    @Override
    public org.omg.CORBA.NVList create_operation_list(org.omg.CORBA.Object oper) {
        throw wrapper.genericNoImpl() ;
    }

    @Override
    public org.omg.CORBA.NamedValue create_named_value(String s, Any any, int flags) {
        return new NamedValueImpl(this, s, any, flags);
    }

    @Override
    public org.omg.CORBA.ExceptionList create_exception_list() {
        return new ExceptionListImpl();
    }

    @Override
    public org.omg.CORBA.ContextList create_context_list() {
        return new ContextListImpl(this);
    }

    @Override
    public org.omg.CORBA.Context get_default_context() 
    {
        throw wrapper.genericNoImpl() ;
    }

    @Override
    public org.omg.CORBA.Environment create_environment() 
    {
        return new EnvironmentImpl();
    }

    @Override
    public org.omg.CORBA.Current get_current() 
    {
        throw wrapper.genericNoImpl() ;
    }

    /*
     * Things that aren't allowed.
     */

    @Override
    public String[] list_initial_services () 
    {
        throw wrapper.genericNoImpl() ;
    }

    @Override
    public org.omg.CORBA.Object resolve_initial_references(String identifier)
        throws InvalidName
    {
        throw wrapper.genericNoImpl() ;
    }

    @Override
    public void register_initial_reference(String id, org.omg.CORBA.Object obj ) throws InvalidName
    {
        throw wrapper.genericNoImpl() ;
    }

    @Override
    public void send_multiple_requests_oneway(Request[] req) {
        throw new SecurityException("ORBSingleton: access denied");
    }
 
    @Override
    public void send_multiple_requests_deferred(Request[] req) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public boolean poll_next_response() {
        throw new SecurityException("ORBSingleton: access denied");
    }
 
    @Override
    public org.omg.CORBA.Request get_next_response() {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public String object_to_string(org.omg.CORBA.Object obj) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public org.omg.CORBA.Object string_to_object(String s) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public java.rmi.Remote string_to_remote(String s)
        throws java.rmi.RemoteException
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void connect(org.omg.CORBA.Object servant) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void disconnect(org.omg.CORBA.Object obj) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void run()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void shutdown(boolean wait_for_completion)
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    protected void shutdownServants(boolean wait_for_completion) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    protected void destroyConnections() {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void destroy() {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public boolean work_pending()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void perform_work()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public org.omg.CORBA.portable.ValueFactory register_value_factory(String repositoryID, org.omg.CORBA.portable.ValueFactory factory)
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void unregister_value_factory(String repositoryID)
    {
        throw new SecurityException("ORBSingleton: access denied");
    }
    
    @Override
    public org.omg.CORBA.portable.ValueFactory lookup_value_factory(String repositoryID)
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public TransportManager getTransportManager()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public TransportManager getCorbaTransportManager()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public LegacyServerSocketManager getLegacyServerSocketManager()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

/*************************************************************************
    These are methods from com.sun.corba.ee.impl.se.core.ORB
 ************************************************************************/

    private synchronized ORB getFullORB()
    {
        if (fullORB == null) {
            Properties props = new Properties() ;
            // Do NOT allow this special ORB to be created with the
            // empty name, which could conflict with app server
            // requirement that first ORB created with the empty name has
            // root monitoring agent name "orb" 
            props.setProperty( ORBConstants.ORB_ID_PROPERTY,
                "_$$$_INTERNAL_FULL_ORB_ID_$$$_" ) ;

            // We do not want to initialize the activation code here, because it is
            // not included in the GF ORB bundles.  There is no need for 
            // activation code, because this internal "full" ORB is only used
            // to create typecodes that require a full ORB.
            props.setProperty( ORBConstants.DISABLE_ORBD_INIT_PROPERTY,
                "true" ) ;

            fullORB = new ORBImpl() ;
            fullORB.set_parameters( props ) ;
        }

        return fullORB ;
    }

    @Override
    public InvocationInterceptor getInvocationInterceptor() {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void setInvocationInterceptor(InvocationInterceptor interceptor ) {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public RequestDispatcherRegistry getRequestDispatcherRegistry()
    {
        // To enable read_Object.

        return getFullORB().getRequestDispatcherRegistry();
    }

    /*
     * Return the service context registry
     * @return throws {@link SecurityException}
     */
    @Override
    public ServiceContextFactoryRegistry getServiceContextFactoryRegistry()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    /**
     * Return the service context cache as null
     * @return {@code null}
     */
    @Override
    public ServiceContextsCache getServiceContextsCache()
    {
        return null;
    }

    /**
     * Get the transient server ID
     * @return throws {@link SecurityException}
     */
    @Override
    public int getTransientServerId()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    /**
     * Return the bootstrap naming port specified in the ORBInitialPort param.
     * @return throws {@link SecurityException}
     */
    public int getORBInitialPort()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    /**
     * Return the bootstrap naming host specified in the ORBInitialHost param.
     * @return throws {@link SecurityException}
     */
    public String getORBInitialHost()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public String getORBServerHost()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getORBServerPort()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public CodeSetComponentInfo getCodeSetComponentInfo() 
    {
            return new CodeSetComponentInfo();
    }

    @Override
    public boolean isLocalHost( String host )
    {
        // To enable read_Object.
        return false;
    }

    @Override
    public boolean isLocalServerId( int subcontractId, int serverId )
    {
        // To enable read_Object.
        return false;
    }

    /*
     * Things from corba.ORB.
     */
    @Override
    public ORBVersion getORBVersion()
    {
        // Always use our latest ORB version (latest fixes, etc)
        return ORBVersionFactory.getORBVersion();
    }

    @Override
    public void setORBVersion(ORBVersion verObj)
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public String getAppletHost()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public URL getAppletCodeBase()
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getHighWaterMark(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getLowWaterMark(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getNumberToReclaim(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getGIOPFragmentSize() {
        return ORBConstants.GIOP_DEFAULT_BUFFER_SIZE;
    }

    public int getGIOPBuffMgrStrategy(GIOPVersion gv) {
        return BufferManagerFactory.GROW;
    }

    @Override
    public IOR getFVDCodeBaseIOR(){
        throw new SecurityException("ORBSingleton: access denied");
    }
            
    @Override
    public Policy create_policy( int type, Any val ) throws PolicyError 
    {
        throw new NO_IMPLEMENT();
    }

    public LegacyServerSocketEndPointInfo getServerEndpoint() 
    {
        return null ;
    }

    public void setPersistentServerId( int id ) 
    {
    }

    @Override
    public TypeCodeImpl getTypeCodeForClass( Class c ) 
    {
        return null ;
    }

    @Override
    public void setTypeCodeForClass( Class c, TypeCodeImpl tcimpl ) 
    {
    }

    public boolean alwaysSendCodeSetServiceContext() 
    {
        return true ;
    }

    @Override
    public boolean isDuringDispatch() 
    {
        return false ;
    }

    @Override
    public void notifyORB() { }

    @Override
    public PIHandler getPIHandler() 
    {
        return null ;
    }

    @Override
    public void createPIHandler() 
    {
    }

    public void checkShutdownState()
    {
    }

    @Override
    public void startingDispatch()
    {
    }

    @Override
    public void finishedDispatch()
    {
    }

    public void registerInitialReference( String id, 
        NullaryFunction<org.omg.CORBA.Object> closure )
    {
    }

    @Override
    public ORBData getORBData() 
    {
        return getFullORB().getORBData() ;
    }

    @Override
    public void setClientDelegateFactory( ClientDelegateFactory factory ) 
    {
    }

    @Override
    public ClientDelegateFactory getClientDelegateFactory() 
    {
        return getFullORB().getClientDelegateFactory() ;
    }

    @Override
    public void setCorbaContactInfoListFactory( ContactInfoListFactory factory )
    {
    }

    @Override
    public ContactInfoListFactory getCorbaContactInfoListFactory()
    {
        return getFullORB().getCorbaContactInfoListFactory() ;
    }

    @Override
    public Operation getURLOperation()
    {
        return null ;
    }

    @Override
    public void setINSDelegate( ServerRequestDispatcher sdel )
    {
    }

    @Override
    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() 
    {
        return getFullORB().getTaggedComponentFactoryFinder() ;
    }

    @Override
    public IdentifiableFactoryFinder getTaggedProfileFactoryFinder() 
    {
        return getFullORB().getTaggedProfileFactoryFinder() ;
    }

    @Override
    public IdentifiableFactoryFinder getTaggedProfileTemplateFactoryFinder() 
    {
        return getFullORB().getTaggedProfileTemplateFactoryFinder() ;
    }

    @Override
    public ObjectKeyFactory getObjectKeyFactory() 
    {
        return getFullORB().getObjectKeyFactory() ;
    }

    @Override
    public void setObjectKeyFactory( ObjectKeyFactory factory ) 
    {
        throw new SecurityException("ORBSingleton: access denied");
    }

    @Override
    public void handleBadServerId( ObjectKey okey ) 
    {
    }

    @Override
    public OAInvocationInfo peekInvocationInfo() 
    {
        return null ;
    }

    @Override
    public void pushInvocationInfo( OAInvocationInfo info ) 
    {
    }

    @Override
    public OAInvocationInfo popInvocationInfo() 
    {
        return null ;
    }

    @Override
    public ClientInvocationInfo createOrIncrementInvocationInfo() 
    {
        return null ;
    }
    
    @Override
    public void releaseOrDecrementInvocationInfo() 
    {
    }
    
    @Override
    public ClientInvocationInfo getInvocationInfo() 
    {
        return null ;
    }

    public ConnectionCache getConnectionCache(ContactInfo contactInfo)
    {
        return null;
    }
    
    @Override
    public void setResolver( Resolver resolver ) 
    {
    }

    @Override
    public Resolver getResolver() 
    {
        return null ;
    }

    @Override
    public void setLocalResolver( LocalResolver resolver ) 
    {
    }

    @Override
    public LocalResolver getLocalResolver() 
    {
        return null ;
    }

    @Override
    public void setURLOperation( Operation stringToObject ) 
    {
    }

    // NOTE: REMOVE THIS METHOD ONCE WE HAVE A ORT BASED ORBD
    @Override
    public void setBadServerIdHandler( BadServerIdHandler handler )
    {
    }

    // NOTE: REMOVE THIS METHOD ONCE WE HAVE A ORT BASED ORBD
    @Override
    public void initBadServerIdHandler()
    {
    }

    public Selector getSelector(int x)
    {
        return null;
    }

    @Override
    public void setThreadPoolManager(ThreadPoolManager mgr) {
    }

    @Override
    public ThreadPoolManager getThreadPoolManager() {
        return null;
    }

    @Override
    public CopierManager getCopierManager() {
        return null ;
    }

    /*
    public TimerManager getTimerManager() {
        return timerManager ;
    }
    */
}

// End of file.
