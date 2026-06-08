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

package com.sun.corba.ee.spi.orb ;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo ;
import com.sun.corba.ee.impl.legacy.connection.USLPort;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.TcpTimeouts;

import java.util.concurrent.TimeUnit;

import org.glassfish.gmbal.AMXMetadata ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.pfl.basic.contain.Pair ;
import org.omg.PortableInterceptor.ORBInitializer ;

// Which attributes should become setters?  NOT everything, but only
// those that we think might actually be useful to set.  This may change
// over time.  On the other hande, essentially everything should be readable.

@ManagedObject
@Description( "ORB Configuration data" )
@AMXMetadata( isSingleton=true )
public interface ORBData {
    @ManagedAttribute
    @Description("Value of ORBInitialHost, the host name of the remote name service")
    String getORBInitialHost();
    // XXX add setter?

    @ManagedAttribute
    @Description("Value of ORBInitialPort, the port number of the remote name service")
    int getORBInitialPort();
    // XXX add setter?

    @ManagedAttribute
    @Description("DESC")
    String getORBServerHost();
    // XXX add setter?

    @ManagedAttribute
    @Description("DESC")
    int getORBServerPort();
    // XXX add setter?

    @ManagedAttribute
    @Description("If true, the ORB listens at its ports on all IP interfaces on the host")
    boolean getListenOnAllInterfaces();
    // XXX add setter?

    @ManagedAttribute
    @Description("The implementation of the legacy ORBSocketFactory interface in use (if any)")
    com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory getLegacySocketFactory();

    @ManagedAttribute
    @Description("The implementation of the ORBSocketFactory interface in use (if any)")
    com.sun.corba.ee.spi.transport.ORBSocketFactory getSocketFactory();

    @ManagedAttribute
    @Description("Return the user-specified listen ports, on which the ORB listens for incoming requests")
    USLPort[] getUserSpecifiedListenPorts();
    // XXX This is legacy: can we remove it?

    @ManagedAttribute
    @Description("Return the instance of the IORToSocketInfo interface, which is used to get SocketInfo from IORs")
    IORToSocketInfo getIORToSocketInfo();

    // XXX Make the setter visible to JMX?
    void setIORToSocketInfo(IORToSocketInfo x);

    @ManagedAttribute
    @Description("Return the instance of the IIOPPrimaryToContactInfo interface")
    IIOPPrimaryToContactInfo getIIOPPrimaryToContactInfo();

    // XXX Make the setter visible to JMX?
    void setIIOPPrimaryToContactInfo(IIOPPrimaryToContactInfo x);

    @ManagedAttribute
    @Description("Return the configured ORB ID")
    String getORBId();

    @ManagedAttribute
    @Description("Returns true if the RMI-IIOP local optimization (caching servant in local subcontract) is allowed.")
    boolean isLocalOptimizationAllowed();

    @ManagedAttribute
    @Description("Return the GIOP version that will be prefered for sending requests")
    GIOPVersion getGIOPVersion();

    @ManagedAttribute
    @Description("Return the high water mark for the connection cache")
    int getHighWaterMark();
    // XXX add setter?

    @ManagedAttribute
    @Description("Return the number of connections to attempt to reclaim"
        + " when the total number of connections exceeds the high water mark")
    int getNumberToReclaim();
    // XXX add setter?

    @ManagedAttribute
    @Description("Return the ")
    int getGIOPFragmentSize();
    // XXX add setter?

    // Probably don't really want to expose this
    // @ManagedAttribute
    // @Description( "Buffer size to use for Java serialization encoding (NOT SUPPORTED)" )
    int getGIOPBufferSize() ;
    // XXX add setter?

    // Can't have an argument: what is the correct action here?
    // @ManagedAttribute
    //  @Description( "Int describing GIOP buffer management strategy: "
        // + "0:grow, 1:collect, 2:stream (the default)")
    int getGIOPBuffMgrStrategy(GIOPVersion gv) ;

    /**
     * @return the GIOP Target Addressing preference of the ORB.
     * This ORB by default supports all addressing dispositions unless specified
     * otherwise via a java system property ORBConstants.GIOP_TARGET_ADDRESSING
     */
    @ManagedAttribute
    @Description("The ORB required target addressing mode: "
        + "0:ObjectKey, 1:TaggedProfile, 2:EntireIOR, 3:Accept All (default)")
    short getGIOPTargetAddressPreference();

    @ManagedAttribute
    @Description("The ORB required target addressing mode: "
        + "0:ObjectKey, 1:TaggedProfile, 2:EntireIOR, 3:Accept All (default)")
    short getGIOPAddressDisposition();

    @ManagedAttribute
    @Description("DESC")
    boolean useByteOrderMarkers();

    @ManagedAttribute
    @Description("DESC")
    boolean useByteOrderMarkersInEncapsulations();

    @ManagedAttribute
    @Description("DESC")
    boolean alwaysSendCodeSetServiceContext();

    @ManagedAttribute
    @Description("DESC")
    boolean getPersistentPortInitialized();

    @ManagedAttribute
    @Description("DESC")
    int getPersistentServerPort();

    @ManagedAttribute
    @Description("DESC")
    boolean getPersistentServerIdInitialized();

    /** Return the persistent-server-id of this server. This id is the same
     *  across multiple activations of this server.
     *  The user/environment is required to supply the
     *  persistent-server-id every time this server is started, in
     *  the ORBServerId parameter, System properties, or other means.
     *  The user is also required to ensure that no two persistent servers
     *  on the same host have the same server-id.
     *
     * @return persistent-server-id of server
     */
    @ManagedAttribute
    @Description("DESC")
    int getPersistentServerId();

    @ManagedAttribute
    @Description("DESC")
    boolean getServerIsORBActivated();

    @ManagedAttribute
    @Description("DESC")
    Class getBadServerIdHandler();

    /**
    * Get the preferred code sets for connections. Should the client send the
    * code set service context on every request?
    * @return code sets for connections
    */
    @ManagedAttribute
    @Description("DESC")
    CodeSetComponentInfo getCodeSetComponentInfo();

    @ManagedAttribute
    @Description("DESC")
    ORBInitializer[] getORBInitializers();

    /** Added to allow user configurators to add ORBInitializers
     * for PI.  This makes it possible to add interceptors from
     * an ORBConfigurator.
     * XXX Should this be an operation, or a set only attribute?
     * Should it even be exposed in the MBean?
     * @param init used to initialize resolve_initial_references
     */
    // @ManagedAttribute
    // @Description( "DESC" )
    void addORBInitializer(ORBInitializer init);

    @ManagedAttribute
    @Description("Pair of (name, CORBA URL) used to initialize resolve_initial_references")
    Pair<String, String>[] getORBInitialReferences();

    String getORBDefaultInitialReference();

    @ManagedAttribute
    @Description("DESC")
    String[] getORBDebugFlags();
    // Add operation to set flags

    @ManagedAttribute
    @Description("DESC")
    Acceptor[] getAcceptors();

    @ManagedAttribute
    @Description("DESC")
    ContactInfoListFactory getCorbaContactInfoListFactory();

    @ManagedAttribute
    @Description("DESC")
    String acceptorSocketType();

    @ManagedAttribute
    @Description("DESC")
    boolean acceptorSocketUseSelectThreadToWait();

    @ManagedAttribute
    @Description("DESC")
    boolean acceptorSocketUseWorkerThreadForEvent();

    @ManagedAttribute
    @Description("DESC")
    String connectionSocketType();

    @ManagedAttribute
    @Description("DESC")
    boolean connectionSocketUseSelectThreadToWait();

    @ManagedAttribute
    @Description("DESC")
    boolean connectionSocketUseWorkerThreadForEvent();

    @ManagedAttribute
    @Description("DESC")
    long getCommunicationsRetryTimeout();
    // XXX add setter

    @ManagedAttribute
    @Description("DESC")
    long getWaitForResponseTimeout();
    // XXX add setter

    @ManagedAttribute
    @Description("DESC")
    TcpTimeouts getTransportTcpTimeouts();
    // XXX add setter

    @ManagedAttribute
    @Description("DESC")
    TcpTimeouts getTransportTcpConnectTimeouts();
    // XXX add setter

    @ManagedAttribute
    @Description("DESC")
    boolean disableDirectByteBufferUse();

    @ManagedAttribute
    @Description("DESC")
    boolean isJavaSerializationEnabled();

    @ManagedAttribute
    @Description("DESC")
    boolean useRepId();

    @ManagedAttribute
    @Description("DESC")
    boolean showInfoMessages();

    @ManagedAttribute
    @Description("DESC")
    boolean getServiceContextReturnsNull();

    // this method tells whether the current ORB was created from within the app server
    // This helps in performance improvement (for certain computations that donot need to be
    //performed again and again. For e.g. getMaxStreamFormatVersion())
    @ManagedAttribute
    @Description("DESC")
    boolean isAppServerMode();

    // Get the ByteBuffer size to use when reading from a SocketChannel,
    // i.e optimized read strategy
    @ManagedAttribute
    @Description("DESC")
    int getReadByteBufferSize();

    // Get maximum read ByteBuffer size to re-allocate
    @ManagedAttribute
    @Description("DESC")
    int getMaxReadByteBufferSizeThreshold();

    // Get the pooled DirectByteBuffer slab size
    @ManagedAttribute
    @Description("DESC")
    int getPooledDirectByteBufferSlabSize();

    // Should a blocking read always be done when using the optimized read
    // strategy ?
    @ManagedAttribute
    @Description("DESC")
    boolean alwaysEnterBlockingRead();

    // Set whether the read optimization should always enter a blocking read
    // after doing a non-blocking read
    @ManagedAttribute
    @Description("DESC")
    void alwaysEnterBlockingRead(boolean b);

    // Should the optimized non-blocking read include in its while loop the
    // condition to check the MessageParser if it is expecting more data?
    @ManagedAttribute
    @Description("DESC")
    boolean nonBlockingReadCheckMessageParser();

    // Should the optimized blocking read include in its while loop the
    // condition to check the MessageParser if it is expecting more data?
    @ManagedAttribute
    @Description("DESC")
    boolean blockingReadCheckMessageParser();

    @ManagedAttribute
    @Description("DESC")
    boolean timingPointsEnabled();
    // XXX add setter

    @ManagedAttribute
    @Description("DESC")
    // Should marshaling of enums be done with EnumDesc, or by simply
    // marshaling as a value type with receiver-make-right? Use EnumDesc
    // if this returns true. The default is false, but the ORB will do
    // the right thing if it receives an EnumDesc in any case.
    boolean useEnumDesc();

    @ManagedAttribute
    @Description("Returns true if ORB is running inside the GFv3 application server")
    boolean environmentIsGFServer();

    @ManagedAttribute
    @Description("If true, do not start any acceptors in the transport by default")
    boolean noDefaultAcceptors();

    // No reason to make this an attribute: if false, we won't see MBeans!
    boolean registerMBeans();

    @ManagedAttribute
    @Description("The time that a CDRInputStream will wait for more data before throwing an exception")
    int fragmentReadTimeout();

    void setOrbInitArgs(String[] args);

    @ManagedAttribute
    @Description("The String[] args that were passed to the ORB init call (used for interceptor initialization)")
    String[] getOrbInitArgs();

    @ManagedAttribute
    @Description("True if ORBD should not be used in this ORB instance")
    boolean disableORBD();

    default void waitNanos(Object obj, long waitNanos) throws InterruptedException {
        TimeUnit.NANOSECONDS.timedWait(obj, waitNanos);
    }
}
