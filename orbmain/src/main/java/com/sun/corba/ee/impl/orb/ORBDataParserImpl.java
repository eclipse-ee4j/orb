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

import java.net.InetAddress;

import org.omg.PortableInterceptor.ORBInitializer ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBData ;
import com.sun.corba.ee.spi.orb.ParserImplTableBase ;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.TcpTimeouts;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo ;
import com.sun.corba.ee.impl.legacy.connection.USLPort;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.transport.Acceptor;
import org.glassfish.pfl.basic.contain.Pair;


public class ORBDataParserImpl extends ParserImplTableBase implements ORBData 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private String ORBInitialHost ; 
    private int ORBInitialPort ; 
    private String ORBServerHost ; 
    private int ORBServerPort ; 
    private boolean listenOnAllInterfaces;
    private com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory legacySocketFactory ; 
    private com.sun.corba.ee.spi.transport.ORBSocketFactory socketFactory;
    private USLPort[] userSpecifiedListenPorts ; 
    private IORToSocketInfo iorToSocketInfo;
    private IIOPPrimaryToContactInfo iiopPrimaryToContactInfo;
    private String orbId ; 
    private boolean allowLocalOptimization ; 
    private GIOPVersion giopVersion ; 
    private int highWaterMark ; 
    private int lowWaterMark ; 
    private int numberToReclaim ; 
    private int giopFragmentSize ; 
    private int giopBufferSize ; 
    private int giop11BuffMgr ; 
    private int giop12BuffMgr ; 
    private short giopTargetAddressPreference ; 
    private short giopAddressDisposition ; 
    private boolean useByteOrderMarkers ; 
    private boolean useByteOrderMarkersInEncaps ; 
    private boolean alwaysSendCodeSetCtx ; 
    private boolean persistentPortInitialized ; 
    private int persistentServerPort ; 
    private boolean persistentServerIdInitialized ; 
    private int persistentServerId ; 
    private boolean serverIsORBActivated ; 
    private Class<?> badServerIdHandlerClass ;
    private CodeSetComponentInfo.CodeSetComponent charData ; 
    private CodeSetComponentInfo.CodeSetComponent wcharData ; 
    private ORBInitializer[] orbInitializers ; 
    private Pair<String,String>[] orbInitialReferences ; 
    private String defaultInitRef ;
    private String[] debugFlags ;
    private Acceptor[] acceptors;
    private ContactInfoListFactory corbaContactInfoListFactory;
    private String acceptorSocketType;
    private boolean acceptorSocketUseSelectThreadToWait;
    private boolean acceptorSocketUseWorkerThreadForEvent;
    private String connectionSocketType;
    private boolean connectionSocketUseSelectThreadToWait;
    private boolean connectionSocketUseWorkerThreadForEvent;
    private long communicationsRetryTimeout;
    private long waitForResponseTimeout;
    private TcpTimeouts tcpTimeouts;
    private TcpTimeouts tcpConnectTimeouts;
    private boolean disableDirectByteBufferUse;
    private boolean enableJavaSerialization;
    private boolean useRepId;
    private boolean showInfoMessages;
    private boolean getServiceContextReturnsNull;
    private boolean isAppServerMode;
    private int readByteBufferSize;
    private int maxReadByteBufferSizeThreshold;
    private int pooledDirectByteBufferSlabSize;
    private boolean alwaysEnterBlockingRead;
    private boolean nonBlockingReadCheckMessageParser;
    private boolean blockingReadCheckMessageParser;
    private boolean timingPointsEnabled;
    private boolean useEnumDesc ;
    private boolean environmentIsGFServer ;
    private boolean noDefaultAcceptors ;
    private boolean registerMBeans ;
    private int fragmentReadTimeout ;

    // This is not initialized from ParserTable.
    private CodeSetComponentInfo codesets ;

    private String[] orbInitArgs ;
    private boolean disableORBD;

// Public accessor methods ========================================================================

    public String getORBInitialHost() 
    { 
        return ORBInitialHost; 
    }

    public int getORBInitialPort() 
    { 
        return ORBInitialPort; 
    }

    public String getORBServerHost() 
    { 
        return ORBServerHost; 
    }

    public boolean getListenOnAllInterfaces()
    { 
        return listenOnAllInterfaces;
    }

    public int getORBServerPort() 
    { 
        return ORBServerPort; 
    }

    public com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory getLegacySocketFactory()
    {
        return legacySocketFactory;
    }

    public com.sun.corba.ee.spi.transport.ORBSocketFactory getSocketFactory() 
    { 
        return socketFactory; 
    }

    public USLPort[] getUserSpecifiedListenPorts () 
    { 
        return userSpecifiedListenPorts; 
    }

    public IORToSocketInfo getIORToSocketInfo()
    {
        return iorToSocketInfo;
    }

    public void setIORToSocketInfo(IORToSocketInfo x)
    {
        iorToSocketInfo = x;
    }

    public IIOPPrimaryToContactInfo getIIOPPrimaryToContactInfo()
    {
        return iiopPrimaryToContactInfo;
    }

    public void setIIOPPrimaryToContactInfo(IIOPPrimaryToContactInfo x)
    {
        iiopPrimaryToContactInfo = x;
    }

    public String getORBId() 
    { 
        return orbId; 
    }

    public boolean isLocalOptimizationAllowed() 
    { 
        return allowLocalOptimization ; 
    }

    public GIOPVersion getGIOPVersion() 
    { 
        return giopVersion; 
    }

    public int getHighWaterMark() 
    { 
        return highWaterMark; 
    }

    public int getLowWaterMark() 
    { 
        return lowWaterMark; 
    }

    public int getNumberToReclaim() 
    { 
        return numberToReclaim; 
    }

    public int getGIOPFragmentSize() 
    { 
        return giopFragmentSize; 
    }

    public int getGIOPBufferSize() 
    { 
        return giopBufferSize; 
    }

    public int getGIOPBuffMgrStrategy(GIOPVersion gv) 
    {
        if(gv!=null){
            if (gv.equals(GIOPVersion.V1_0)) {
                return 0;
            } //Always grow for 1.0
            if (gv.equals(GIOPVersion.V1_1)) {
                return giop11BuffMgr;
            }
            if (gv.equals(GIOPVersion.V1_2)) {
                return giop12BuffMgr;
            }
        }
        //If a "faulty" GIOPVersion is passed, it's going to return 0;
        return 0;
    }

    /**
     * @return the GIOP Target Addressing preference of the ORB.
     * This ORB by default supports all addressing dispositions unless specified
     * otherwise via a java system property ORBConstants.GIOP_TARGET_ADDRESSING
     */
    public short getGIOPTargetAddressPreference() 
    { 
        return giopTargetAddressPreference; 
    }

    public short getGIOPAddressDisposition() 
    { 
        return giopAddressDisposition;    
    }

    public boolean useByteOrderMarkers() 
    { 
        return useByteOrderMarkers; 
    }

    public boolean useByteOrderMarkersInEncapsulations() 
    { 
        return useByteOrderMarkersInEncaps; 
    }

    public boolean alwaysSendCodeSetServiceContext() 
    { 
        return alwaysSendCodeSetCtx; 
    }

    public boolean getPersistentPortInitialized() 
    { 
        return persistentPortInitialized ; 
    }

    public int getPersistentServerPort()
    {
        if ( persistentPortInitialized ) {
            return persistentServerPort;
        }
        else {
            throw wrapper.persistentServerportNotSet( ) ;
        }
    }

    public boolean getPersistentServerIdInitialized() 
    { 
        return persistentServerIdInitialized; 
    }

    /** Return the persistent-server-id of this server. This id is the same
     *  across multiple activations of this server. This is in contrast to
     *  com.sun.corba.ee.impl.iiop.ORB.getTransientServerId() which 
     *  returns a transient id that is guaranteed to be different 
     *  across multiple activations of
     *  this server. The user/environment is required to supply the 
     *  persistent-server-id every time this server is started, in 
     *  the ORBServerId parameter, System properties, or other means.
     *  The user is also required to ensure that no two persistent servers
     *  on the same host have the same server-id.
     */
    public int getPersistentServerId()
    {
        if ( persistentServerIdInitialized ) {
            return persistentServerId;
        } else {
            throw wrapper.persistentServeridNotSet( ) ;
        }
    }

    public boolean getServerIsORBActivated() 
    { 
        return serverIsORBActivated ; 
    }

    public Class<?> getBadServerIdHandler()
    {
        return badServerIdHandlerClass ;
    }

     /**
     * Get the prefered code sets for connections. Should the client send the code set service context on every
     * request?
     */
    public CodeSetComponentInfo getCodeSetComponentInfo() 
    { 
        return codesets; 
    }

    public ORBInitializer[] getORBInitializers()
    {
        return orbInitializers ;
    }

    public void addORBInitializer( ORBInitializer initializer ) 
    {
        ORBInitializer[] arr = new ORBInitializer[orbInitializers.length+1] ;
        System.arraycopy(orbInitializers, 0, arr, 0, orbInitializers.length);
        arr[orbInitializers.length] = initializer ;
        orbInitializers = arr ;
    }

    public Pair<String,String>[] getORBInitialReferences()
    {
        return orbInitialReferences ;
    }

    public String getORBDefaultInitialReference()
    {
        return defaultInitRef ;
    }

    public String[] getORBDebugFlags() 
    {
        return debugFlags ;
    }

    public Acceptor[] getAcceptors()
    {
        return acceptors;
    }

    public ContactInfoListFactory getCorbaContactInfoListFactory()
    {
        return corbaContactInfoListFactory;
    }

    public String acceptorSocketType()
    {
        return acceptorSocketType;
    }
    public boolean acceptorSocketUseSelectThreadToWait()
    {
        return acceptorSocketUseSelectThreadToWait;
    }
    public boolean acceptorSocketUseWorkerThreadForEvent()
    {
        return acceptorSocketUseWorkerThreadForEvent;
    }
    public String connectionSocketType()
    {
        return connectionSocketType;
    }
    public boolean connectionSocketUseSelectThreadToWait()
    {
        return connectionSocketUseSelectThreadToWait;
    }
    public boolean connectionSocketUseWorkerThreadForEvent()
    {
        return connectionSocketUseWorkerThreadForEvent;
    }
    public boolean isJavaSerializationEnabled()
    {
        return enableJavaSerialization;
    }
    public long getCommunicationsRetryTimeout()
    {
        return communicationsRetryTimeout;
    }
    public long getWaitForResponseTimeout()
    {
        return waitForResponseTimeout;
    }
    public TcpTimeouts getTransportTcpTimeouts()
    {
        return tcpTimeouts;
    }
    public TcpTimeouts getTransportTcpConnectTimeouts()
    {
        return tcpConnectTimeouts;
    }
    public boolean disableDirectByteBufferUse() 
    {
        return disableDirectByteBufferUse ;
    }
    public boolean useRepId() 
    {
        return useRepId;
    }

    public boolean showInfoMessages()
    {
        return showInfoMessages;
    }
    
    public boolean getServiceContextReturnsNull()
    {
        return getServiceContextReturnsNull;
    }

    public boolean isAppServerMode() 
    {
        return isAppServerMode;

    }

    public int getReadByteBufferSize() {
        return readByteBufferSize;
    }

    public int getMaxReadByteBufferSizeThreshold() {
        return maxReadByteBufferSizeThreshold;
    }
    
    public int getPooledDirectByteBufferSlabSize() {
        return pooledDirectByteBufferSlabSize;
    }

    public boolean alwaysEnterBlockingRead() {
        return alwaysEnterBlockingRead;
    }

    public void alwaysEnterBlockingRead(boolean b) {
        alwaysEnterBlockingRead = b;
    }

    public boolean nonBlockingReadCheckMessageParser() {
        return nonBlockingReadCheckMessageParser;
    }

    public boolean blockingReadCheckMessageParser() {
        return blockingReadCheckMessageParser;
    }

    // ====== Methods for constructing and initializing this object =========

    public ORBDataParserImpl( ORB orb, DataCollector coll )
    {
        super( ParserTable.get( 
            ORB.defaultClassNameResolver() ).getParserData() ) ;
        init( coll ) ;
    }

    @Override
    public void complete() 
    {
        codesets = new CodeSetComponentInfo(charData, wcharData);
        initializeServerHostInfo();
    }

    private void initializeServerHostInfo()
    {
        if (ORBServerHost == null || 
            ORBServerHost.equals("") ||
            ORBServerHost.equals("0.0.0.0") ||
            ORBServerHost.equals("::") ||
            ORBServerHost.toLowerCase().equals("::ffff:0.0.0.0"))
        {
            try
            {
                ORBServerHost = InetAddress.getLocalHost().getHostAddress();
            }
            catch (Exception ex)
            {
                throw wrapper.getLocalHostFailed(ex);
            }
            listenOnAllInterfaces = true;
        }
        else
        {
            listenOnAllInterfaces = false;
        }
    }
    public boolean timingPointsEnabled() 
    {
        return timingPointsEnabled ;
    }

    public boolean useEnumDesc() 
    {
        return useEnumDesc ;
    }

    public boolean environmentIsGFServer() {
        return environmentIsGFServer ;
    }

    public boolean noDefaultAcceptors() {
        return noDefaultAcceptors ;
    }

    public boolean registerMBeans() {
        return registerMBeans ;
    }

    public int fragmentReadTimeout() {
        return fragmentReadTimeout ;
    }

    public void setOrbInitArgs( String[] args ) {
        orbInitArgs = args ;
    }

    public String[] getOrbInitArgs() {
        return orbInitArgs ;
    }

    public boolean disableORBD() {
        return disableORBD ;
    }
}

// End of file.
