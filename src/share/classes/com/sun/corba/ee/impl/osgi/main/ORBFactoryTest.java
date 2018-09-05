/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.osgi.main ;

import org.osgi.framework.Bundle ;
import org.osgi.framework.BundleActivator ;
import org.osgi.framework.BundleEvent ;
import org.osgi.framework.BundleContext ;
import org.osgi.framework.SynchronousBundleListener ;
import org.osgi.framework.ServiceReference ;

import org.osgi.service.packageadmin.PackageAdmin ;
import org.osgi.service.packageadmin.ExportedPackage ;

import java.util.Properties ;

import com.sun.corba.ee.spi.osgi.ORBFactory ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager ;

public class ORBFactoryTest implements BundleActivator, SynchronousBundleListener {
    private static PackageAdmin pkgAdmin ;
    
    private static String getBundleEventType( int type ) {
        if (type == BundleEvent.INSTALLED) 
            return "INSTALLED" ;
        else if (type == BundleEvent.LAZY_ACTIVATION)
            return "LAZY_ACTIVATION" ;
        else if (type == BundleEvent.RESOLVED)
            return "RESOLVED" ;
        else if (type == BundleEvent.STARTED)
            return "STARTED" ;
        else if (type == BundleEvent.STARTING)
            return "STARTING" ;
        else if (type == BundleEvent.STOPPED)
            return "STOPPED" ;
        else if (type == BundleEvent.STOPPING)
            return "STOPPING" ;
        else if (type == BundleEvent.UNINSTALLED)
            return "UNINSTALLED" ;
        else if (type == BundleEvent.UNRESOLVED)
            return "UNRESOLVED" ;
        else if (type == BundleEvent.UPDATED)
            return "UPDATED" ;
        else 
            return "ILLEGAL-EVENT-TYPE" ;
    }

    private static void msg( String arg ) {
        System.out.println( "ORBFactoryTest: " + arg ) ;
    }

    private ORB orb = null ;

    public void start( BundleContext context ) {
        msg( "Starting ORBFactoryTest" ) ;
        context.addBundleListener( this ) ;

        try {
            ServiceReference sref = context.getServiceReference( "org.osgi.service.packageadmin.PackageAdmin" ) ;
            pkgAdmin = (PackageAdmin)context.getService( sref ) ;

            dumpInfo( context, pkgAdmin ) ;
        } catch (Exception exc) {
            msg( "Exception in getting PackageAdmin: " + exc ) ;
        }
    }

    private void dumpInfo( BundleContext context, PackageAdmin pkgAdmin ) {
        msg( "Dumping bundle information" ) ;
        for (Bundle bundle : context.getBundles()) {
            msg( "\tBundle: " + bundle.getSymbolicName() ) ;
            for (ExportedPackage ep : pkgAdmin.getExportedPackages( bundle ) ) {
                msg( "\t\tExport: " + ep.getName() ) ;
            }
        }
    }

    public void stop( BundleContext context ) {
        msg( "Stopping ORBFactoryTest" ) ;
        context.removeBundleListener( this ) ;
    }

    public void bundleChanged(BundleEvent event) {
        int type = event.getType() ;
        String name = event.getBundle().getSymbolicName() ;

        msg( "Received event type " 
            + getBundleEventType( type ) + " for bundle " + name ) ;

        // Only want to know when this bundle changes state, not the others.
        if (!name.equals( "glassfish-corba-osgi-test" )) {
            return ;
        }

        if ((type & (BundleEvent.STARTED | BundleEvent.STARTING)) != 0) {
            try {
                String[] args = {} ;
                Properties props = new Properties() ;
                props.setProperty( ORBConstants.RFM_PROPERTY, "dummy" ) ;
                orb = ORBFactory.create() ;
                ORBFactory.initialize( orb, args, props, true ) ;
                ReferenceFactoryManager rfm = 
                    (ReferenceFactoryManager)orb.resolve_initial_references(
                        ORBConstants.REFERENCE_FACTORY_MANAGER ) ;
                msg( "ORB successfully created" ) ;
            } catch (Exception exc) {
                exc.printStackTrace() ;
            }
        } else if ((type & (BundleEvent.STOPPED | BundleEvent.STOPPING)) != 0) {
            if (orb != null) {
                orb.destroy() ;
            }
            msg( "ORB destroyed" ) ;
        }
    }
}
