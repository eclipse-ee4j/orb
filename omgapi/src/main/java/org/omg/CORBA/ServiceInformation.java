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

package org.omg.CORBA;


/** An IDL struct in the CORBA module that
 *  stores information about a CORBA service available in the
 *  ORB implementation and is obtained from the <tt>ORB.get_service_information</tt>
 *  method.
 */
// @SuppressWarnings({"serial"})
public final class ServiceInformation implements org.omg.CORBA.portable.IDLEntity
{
    /** Array of ints representing service options.
    */
    public int[] service_options;

    /** Array of ServiceDetails giving more details about the service.
    */
    public org.omg.CORBA.ServiceDetail[] service_details;

    /** Constructs a ServiceInformation object with empty service_options
    * and service_details.
    */
    public ServiceInformation() { }

    /** Constructs a ServiceInformation object with the given service_options
    * and service_details.
    * @param __service_options An array of ints describing the service options.
    * @param __service_details An array of ServiceDetails describing the service
    * details.
    */
    public ServiceInformation(int[] __service_options,
                              org.omg.CORBA.ServiceDetail[] __service_details)
    {
        service_options = __service_options;
        service_details = __service_details;
    }
}

