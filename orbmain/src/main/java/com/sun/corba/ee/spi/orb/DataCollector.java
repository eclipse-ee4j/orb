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

package com.sun.corba.ee.spi.orb;

import java.util.Properties;

/**
 * Interface for collecting all sources of ORB configuration properties into a single properties object. A
 * PropertyParser is needed so that the set of property names of interest is known.
 */
public interface DataCollector {
    /**
     * Return true iff this DataCollector was created from applet data.
     * 
     * @return if this was created from an applet
     */
    boolean isApplet();

    /**
     * Return true iff the local host and ORB initial host are the same. This is provided to avoid exposing the local host
     * in insecure contexts.
     * 
     * @return if the local host and ORB initial host are the same
     */
    boolean initialHostIsLocal();

    /**
     * Set the parser which is used to obtain property names. This must be called before getProperties may be called. It may
     * be called multiple times if different sets of properties are needed for the same data sources.
     * 
     * @param parser parser used to obtain property names
     */
    void setParser(PropertyParser parser);

    /**
     * Return the consolidated property information to be used for ORB configuration. Note that -ORBInitRef arguments are
     * handled specially: all -ORBInitRef name=value arguments are converted into ( org.omg.CORBA.ORBInitRef.name, value )
     * mappings in the resulting properties. Also, -ORBInitialServices is handled specially in applet mode: they are
     * converted from relative to absolute URLs.
     * 
     * @return consolidated property information
     * @throws IllegalStateException if setPropertyNames has not been called.
     */
    Properties getProperties();
}
