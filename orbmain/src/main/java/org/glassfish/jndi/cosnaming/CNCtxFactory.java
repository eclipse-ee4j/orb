/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jndi.cosnaming;

import javax.naming.spi.InitialContextFactory;
import javax.naming.*;

import java.util.Hashtable;

/**
  * Implements the JNDI SPI InitialContextFactory interface used to
  * create  the InitialContext objects.
  *
  * @author Raj Krishnamurthy
  */

public class CNCtxFactory implements InitialContextFactory {

  /**
    * Creates the InitialContext object. Properties parameter should
    * should contain the ORB object for the value jndi.corba.orb.
    * @param env Properties object
    */

  public Context getInitialContext(Hashtable<?,?> env) throws NamingException {
      return new CNCtx(env);
  }
}
