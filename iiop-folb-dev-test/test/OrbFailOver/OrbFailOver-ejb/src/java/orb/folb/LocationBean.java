/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package orb.folb;

import javax.ejb.Stateless;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Enumeration;

/**
 *
 * @author hv51393
 */
@Stateless
public class LocationBean implements LocationBeanRemote {

    public String getLocation() {

        try {
            String instanceName = System.getProperty("com.sun.aas.instanceName");
            return instanceName;
        } catch (Exception e) {
            return null;
        }
    }

    public String getHostName() {

        try {
            String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            return hostName ;
        } catch (Exception e) {
            return null;
        }
    }

    public void printSystemProperties() {
        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) p.get(key);
            System.out.println(key + ": " + value);
        }
    }
}
