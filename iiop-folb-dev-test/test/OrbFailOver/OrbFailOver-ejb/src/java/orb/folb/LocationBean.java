/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package orb.folb;

import jakarta.ejb.Stateless;
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
