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

package com.sun.corba.ee.tool ;

import org.omg.CORBA.portable.ObjectImpl ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.protocol.ClientDelegate ;
import com.sun.corba.ee.spi.transport.ContactInfoList ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.impl.ior.GenericIdentifiable ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.TaggedComponent ;
import org.glassfish.pfl.basic.algorithm.Printer;

public class IORDump {
    private static Printer pr = new Printer( System.out ) ;

    private static final String[] STANDARD_TAGGED_COMPONENT_NAMES = new String[]{
        "ORB_TYPE",                     // 0
        "CODE_SETS",
        "POLICIES",
        "ALTERNATE_IIOP_ADDRESS",
        null,
        "COMPLETE_OBJECT_KEY",
        "ENDPOINT_ID_POSITION",
        null,
        null,
        null,                           
        null,                           // 10
        null,
        "LOCATION_POLICY",
        "ASSOCIATION_OPTIONS",
        "SEC_NAME",
        "SPKM_1_SEC_MECH",
        "SPKM_2_SEC_MECH",
        "KergerosV5_SEC_MECH",
        "CSI_ECMA_Secret_SEC_MECH",
        "CSI_ECMA_Hybrid_SEC_MECH",
        "SSL_SEC_TRANS",                // 20
        "CSI_ECMA_Public_SEC_MECH",
        "GENERIC_SEC_MECH",
        "FIREWALL_TRANS",
        "SCCP_CONTACT_INFO",
        "JAVA_CODEBASE",
        "TRANSACTION_POLICY",
        "FT_GROUP",
        "FT_PRIMARY",
        "FT_HEARTBEAT_ENABLED",
        "MESSAGE_ROUTERS",              // 30
        "OTS_POLICY",
        "INV_POLICY",
        "CSI_SEC_MECH_LIST",
        "NULL_TAG",
        "SECIOP_SEC_TRANS",
        "TLS_SEC_TRANS",
        "ACTIVITY_POLICY",
        "RMI_CUSTOM_MAX_STREAM_FORMAT",
        null,
        null,                           // 40
        null, null, null, null, null,
        null, null, null, null, null,   // 50
        null, null, null, null, null,
        null, null, null, null, null,   // 60
        null, null, null, null, null,
        null, null, null, null, null,   // 70
        null, null, null, null, null,
        null, null, null, null, null,   // 80
        null, null, null, null, null,
        null, null, null, null, null,   // 90
        null, null, null, null, null,
        null, null, null, null, 
        "DCE_STRING_BINDING",            // 100
        "DCE_BINDING_NAME",
        "DCE_NO_PIPES",
        "DCE_SEC_MECH",
        null, null,
        null, null, null, null, null,   // 110
        null, null, null, null, null,   
        null, null, null, null, null,   // 120
        null, null, 
        "INET_SEC_TRANS"
    } ;

    private static String getTaggedComponentName( int id ) {
        String entry = null ;
        if ((id >= 0) && (id < STANDARD_TAGGED_COMPONENT_NAMES.length))
            entry = STANDARD_TAGGED_COMPONENT_NAMES[id] ;

        if (entry == null)
            entry = "UNASSIGNED_" + id ;

        return entry ;
    }

    public static void main( String[] args ) {
        if (args.length != 1) {
            System.out.println( "Syntax: iordump <stringified IOR or URL>" ) ;
        } else {
            try {
                String iorString = args[0] ;
                String[] initArgs = null ;
                ORB orb = (ORB)ORB.init( initArgs, null ) ;
                org.omg.CORBA.Object obj = orb.string_to_object( iorString ) ;
                ObjectImpl oimpl = (ObjectImpl)obj ;
                ClientDelegate delegate = (ClientDelegate)(oimpl._get_delegate()) ;
                ContactInfoList cilist = (ContactInfoList)(delegate.getContactInfoList()) ;
                IOR ior = cilist.getTargetIOR() ;
                dumpIOR( ior ) ;
            } catch (Exception exc) {
                System.out.println( "Caught exception: " + exc ) ;
                exc.printStackTrace() ;
            }
        }
    }

    public static void dumpIOR( IOR ior ) {
        pr.nl().p( "Dump of IOR:" ).in() ;
        pr.nl().p( "typeId = " + ior.getTypeId() ) ;
        pr.nl().p( "tagged profiles:" ).in() ;
        for (TaggedProfile tprof : ior) {
            dumpTaggedProfile( tprof ) ;
        }
        pr.out().out().nl() ; 
    }

    public static void dumpTaggedProfile( TaggedProfile tprof ) {
        pr.nl().p( "Id = ", tprof.getId(), ":" ).in() ;

        TaggedProfileTemplate tpt = tprof.getTaggedProfileTemplate() ;
        if (tpt instanceof IIOPProfileTemplate) {
            IIOPProfileTemplate iptemp = (IIOPProfileTemplate)tpt ;
            pr.nl().p( "GIOPVersion    = ", iptemp.getGIOPVersion() ) ;
            pr.nl().p( "PrimaryAddress = ", iptemp.getPrimaryAddress() ) ;
        }

        pr.nl().p( "ObjectId:" ).in() ;
        pr.printBuffer( tprof.getObjectId().getId() ).out() ;

        pr.nl().p( "ObjectKeyTemplate:" ).in() ;
        dumpObjectKeyTemplate( tprof.getObjectKeyTemplate() ) ;
        pr.out() ;

        pr.nl().p( "Tagged components:" ).in() ;
        for (TaggedComponent tcomp : tpt ) {
            int id = tcomp.getId() ;
            pr.nl().p( "id = ", tcomp.getId(), 
                " (", getTaggedComponentName( id ), ")" ).in() ;
            if (tcomp instanceof GenericIdentifiable) {
                GenericIdentifiable gid = (GenericIdentifiable)tcomp ;
                pr.printBuffer( gid.getData() ) ;
            } else {
                pr.nl().p( tcomp ) ;
            }
            pr.out() ;
        }
        pr.out() ;

        pr.out() ;
    }

    public static void dumpObjectKeyTemplate( ObjectKeyTemplate oktemp ) {
        pr.nl().p( "ORBVersion      = " ).p( oktemp.getORBVersion() ) ;
        pr.nl().p( "SubcontractId   = " ).p( oktemp.getSubcontractId() ) ;
        pr.nl().p( "ServerId        = " ).p( oktemp.getServerId() ) ;
        pr.nl().p( "ORBId           = " ).p( oktemp.getORBId() ) ;
        pr.nl().p( "ObjectAdapterId = " ).p( oktemp.getObjectAdapterId() ) ;
    }
}
