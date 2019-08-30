/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb ;

import java.applet.Applet ;
import java.util.Properties ;
import java.util.Vector ;

/** Interface for collecting all sources of ORB configuration properties
 * into a single properties object.   A PropertyParser is needed so that
 * the set of property names of interest is known.
 */
public interface DataCollector {
    /** Return true iff this DataCollector was created from
     * applet data.
     * 
     * @return if this was created from an applet
     */
    boolean isApplet() ;

    /** Return true iff the local host and ORB initial host are the same.
    * This is provided to avoid exposing the local host in insecure
    * contexts.
    * 
    * @return if the local host and ORB initial host are the same
    */
    boolean initialHostIsLocal() ;

    /** Set the parser which is used to obtain property names.
     * This must be called before getProperties 
     * may be called.  It may be called multiple times if different
     * sets of properties are needed for the same data sources.
     * 
     * @param parser parser used to obtain property names
     */
    void setParser( PropertyParser parser ) ;

    /** Return the consolidated property information to be used
     * for ORB configuration.  Note that -ORBInitRef arguments are
     * handled specially: all -ORBInitRef name=value arguments are
     * converted into ( org.omg.CORBA.ORBInitRef.name, value )
     * mappings in the resulting properties.  Also, -ORBInitialServices
     * is handled specially in applet mode: they are converted from
     * relative to absolute URLs.
     * @return consolidated property information
     * @throws IllegalStateException if setPropertyNames has not
     * been called.
     */
    Properties getProperties() ;
}
