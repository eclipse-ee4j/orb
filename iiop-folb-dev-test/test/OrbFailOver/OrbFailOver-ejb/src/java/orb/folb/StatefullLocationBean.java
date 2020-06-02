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

import jakarta.ejb.Stateful;

/**
 *
 * @author ken
 */
@Stateful
public class StatefullLocationBean implements StatefullLocationBeanRemote {
    @Override
    public String getLocation() {
        try {
            String instanceName = System.getProperty("com.sun.aas.instanceName");
            return instanceName;
        } catch (Exception e) {
            return null;
        }
    }

}
