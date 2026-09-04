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

package com.sun.corba.ee.impl.orb ;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo ;
import com.sun.corba.ee.impl.legacy.connection.USLPort;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBData ;
import com.sun.corba.ee.spi.orb.ParserImplTableBase ;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.TcpTimeouts;

import java.net.InetAddress;

import org.glassfish.pfl.basic.contain.Pair;
import org.omg.PortableInterceptor.ORBInitializer ;


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

    @Override
    public String getORBInitialHost()
    {
        return ORBInitialHost;
    }

    @Override
    public int getORBInitialPort()
    {
        return ORBInitialPort;
    }

    @Override
    public String getORBServerHost()
    {
        return ORBServerHost;
    }

    @Override
    public boolean getListenOnAllInterfaces()
    {
        return listenOnAllInterfaces;
    }

    @Override
    public int getORBServerPort()
    {
        return ORBServerPort;
    }

    @Override
    public com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory getLegacySocketFactory()
    {
        return legacySocketFactory;
    }

    @Override
    public com.sun.corba.ee.spi.transport.ORBSocketFactory getSocketFactory()
    {
        return socketFactory;
    }

    @Override
    public USLPort[] getUserSpecifiedListenPorts ()
    {
        return userSpecifiedListenPorts;
    }

    @Override
    public IORToSocketInfo getIORToSocketInfo()
    {
        return iorToSocketInfo;
    }

    @Override
    public void setIORToSocketInfo(IORToSocketInfo x)
    {
        iorToSocketInfo = x;
    }

    @Override
    public IIOPPrimaryToContactInfo getIIOPPrimaryToContactInfo()
    {
        return iiopPrimaryToContactInfo;
    }

    @Override
    public void setIIOPPrimaryToContactInfo(IIOPPrimaryToContactInfo x)
    {
        iiopPrimaryToContactInfo = x;
    }

    @Override
    public String getORBId()
    {
        return orbId;
    }

    @Override
    public boolean isLocalOptimizationAllowed()
    {
        return allowLocalOptimization ;
    }

    @Override
    public GIOPVersion getGIOPVersion()
    {
        return giopVersion;
    }

    @Override
    public int getHighWaterMark()
    {
        return highWaterMark;
    }

    public int getLowWaterMark()
    {
        return lowWaterMark;
    }

    @Override
    public int getNumberToReclaim()
    {
        return numberToReclaim;
    }

    @Override
    public int getGIOPFragmentSize()
    {
        return giopFragmentSize;
    }

    @Override
    public int getGIOPBufferSize()
    {
        return giopBufferSize;
    }

    @Override
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
    @Override
    public short getGIOPTargetAddressPreference()
    {
        return giopTargetAddressPreference;
    }

    @Override
    public short getGIOPAddressDisposition()
    {
        return giopAddressDisposition;
    }

    @Override
    public boolean useByteOrderMarkers()
    {
        return useByteOrderMarkers;
    }

    @Override
    public boolean useByteOrderMarkersInEncapsulations()
    {
        return useByteOrderMarkersInEncaps;
    }

    @Override
    public boolean alwaysSendCodeSetServiceContext()
    {
        return alwaysSendCodeSetCtx;
    }

    @Override
    public boolean getPersistentPortInitialized()
    {
        return persistentPortInitialized ;
    }

    @Override
    public int getPersistentServerPort()
    {
        if ( persistentPortInitialized ) {
            return persistentServerPort;
        }
        else {
            throw wrapper.persistentServerportNotSet( ) ;
        }
    }

    @Override
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
    @Override
    public int getPersistentServerId()
    {
        if ( persistentServerIdInitialized ) {
            return persistentServerId;
        } else {
            throw wrapper.persistentServeridNotSet( ) ;
        }
    }

    @Override
    public boolean getServerIsORBActivated()
    {
        return serverIsORBActivated ;
    }

    @Override
    public Class<?> getBadServerIdHandler()
    {
        return badServerIdHandlerClass ;
    }

     /**
     * Get the prefered code sets for connections. Should the client send the code set service context on every
     * request?
     */
    @Override
    public CodeSetComponentInfo getCodeSetComponentInfo()
    {
        return codesets;
    }

    @Override
    public ORBInitializer[] getORBInitializers()
    {
        return orbInitializers ;
    }

    @Override
    public void addORBInitializer( ORBInitializer initializer )
    {
        ORBInitializer[] arr = new ORBInitializer[orbInitializers.length+1] ;
        System.arraycopy(orbInitializers, 0, arr, 0, orbInitializers.length);
        arr[orbInitializers.length] = initializer ;
        orbInitializers = arr ;
    }

    @Override
    public Pair<String,String>[] getORBInitialReferences()
    {
        return orbInitialReferences ;
    }

    @Override
    public String getORBDefaultInitialReference()
    {
        return defaultInitRef ;
    }

    @Override
    public String[] getORBDebugFlags()
    {
        return debugFlags ;
    }

    @Override
    public Acceptor[] getAcceptors()
    {
        return acceptors;
    }

    @Override
    public ContactInfoListFactory getCorbaContactInfoListFactory()
    {
        return corbaContactInfoListFactory;
    }

    @Override
    public String acceptorSocketType()
    {
        return acceptorSocketType;
    }
    @Override
    public boolean acceptorSocketUseSelectThreadToWait()
    {
        return acceptorSocketUseSelectThreadToWait;
    }
    @Override
    public boolean acceptorSocketUseWorkerThreadForEvent()
    {
        return acceptorSocketUseWorkerThreadForEvent;
    }
    @Override
    public String connectionSocketType()
    {
        return connectionSocketType;
    }
    @Override
    public boolean connectionSocketUseSelectThreadToWait()
    {
        return connectionSocketUseSelectThreadToWait;
    }
    @Override
    public boolean connectionSocketUseWorkerThreadForEvent()
    {
        return connectionSocketUseWorkerThreadForEvent;
    }
    @Override
    public boolean isJavaSerializationEnabled()
    {
        return enableJavaSerialization;
    }
    @Override
    public long getCommunicationsRetryTimeout()
    {
        return communicationsRetryTimeout;
    }
    @Override
    public long getWaitForResponseTimeout()
    {
        return waitForResponseTimeout;
    }
    @Override
    public TcpTimeouts getTransportTcpTimeouts()
    {
        return tcpTimeouts;
    }
    @Override
    public TcpTimeouts getTransportTcpConnectTimeouts()
    {
        return tcpConnectTimeouts;
    }
    @Override
    public boolean disableDirectByteBufferUse()
    {
        return disableDirectByteBufferUse ;
    }
    @Override
    public boolean useRepId()
    {
        return useRepId;
    }

    @Override
    public boolean showInfoMessages()
    {
        return showInfoMessages;
    }

    @Override
    public boolean getServiceContextReturnsNull()
    {
        return getServiceContextReturnsNull;
    }

    @Override
    public boolean isAppServerMode()
    {
        return isAppServerMode;

    }

    @Override
    public int getReadByteBufferSize() {
        return readByteBufferSize;
    }

    @Override
    public int getMaxReadByteBufferSizeThreshold() {
        return maxReadByteBufferSizeThreshold;
    }

    @Override
    public int getPooledDirectByteBufferSlabSize() {
        return pooledDirectByteBufferSlabSize;
    }

    @Override
    public boolean alwaysEnterBlockingRead() {
        return alwaysEnterBlockingRead;
    }

    @Override
    public void alwaysEnterBlockingRead(boolean b) {
        alwaysEnterBlockingRead = b;
    }

    @Override
    public boolean nonBlockingReadCheckMessageParser() {
        return nonBlockingReadCheckMessageParser;
    }

    @Override
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
    @Override
    public boolean timingPointsEnabled()
    {
        return timingPointsEnabled ;
    }

    @Override
    public boolean useEnumDesc()
    {
        return useEnumDesc ;
    }

    @Override
    public boolean environmentIsGFServer() {
        return environmentIsGFServer ;
    }

    @Override
    public boolean noDefaultAcceptors() {
        return noDefaultAcceptors ;
    }

    @Override
    public boolean registerMBeans() {
        return registerMBeans ;
    }

    @Override
    public int fragmentReadTimeout() {
        return fragmentReadTimeout ;
    }

    @Override
    public void setOrbInitArgs( String[] args ) {
        orbInitArgs = args ;
    }

    @Override
    public String[] getOrbInitArgs() {
        return orbInitArgs ;
    }

    @Override
    public boolean disableORBD() {
        return disableORBD ;
    }
}

// End of file.
