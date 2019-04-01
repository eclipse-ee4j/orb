/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA;

/**
 * An IDL struct in the CORBA module that stores information about a CORBA service available in the ORB implementation
 * and is obtained from the <tt>ORB.get_service_information</tt> method.
 */
// @SuppressWarnings({"serial"})
public final class ServiceInformation implements org.omg.CORBA.portable.IDLEntity {
    /**
     * Array of ints representing service options.
     */
    public int[] service_options;

    /**
     * Array of ServiceDetails giving more details about the service.
     */
    public org.omg.CORBA.ServiceDetail[] service_details;

    /**
     * Constructs a ServiceInformation object with empty service_options and service_details.
     */
    public ServiceInformation() {
    }

    /**
     * Constructs a ServiceInformation object with the given service_options and service_details.
     *
     * @param __service_options An array of ints describing the service options.
     * @param __service_details An array of ServiceDetails describing the service details.
     */
    public ServiceInformation(int[] __service_options, org.omg.CORBA.ServiceDetail[] __service_details) {
        service_options = __service_options;
        service_details = __service_details;
    }
}
