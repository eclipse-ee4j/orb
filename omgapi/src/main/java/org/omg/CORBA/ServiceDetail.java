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

/** 
 * An object that represents an ORB service: its <code>service_detail_type</code>
 * field contains the type of the ORB service, and its <code>service_detail</code>
 * field contains a description of the ORB service.

 *
 * @author RIP Team
 * @version 1.11 11/15/00
 */
// @SuppressWarnings({"serial"})
public final class ServiceDetail implements org.omg.CORBA.portable.IDLEntity
{
    /**
     * The type of the ORB service that this <code>ServiceDetail</code> 
     * object represents.
     */
    public int service_detail_type;

    /** 
     * The data describing the ORB service that this <code>ServiceDetail</code>
     * object represents.
     */
    public byte[] service_detail;

    /**
     * Constructs a <code>ServiceDetail</code> object with 0 for the type of
     * ORB service and an empty description.
     */
    public ServiceDetail() { }

    /**
     * Constructs a <code>ServiceDetail</code> object with the given 
     * ORB service type and the given description.
     *
     * @param service_detail_type an <code>int</code> specifying the type of 
     *                            ORB service
     * @param service_detail a <code>byte</code> array describing the ORB service
     */
    public ServiceDetail(int service_detail_type, byte[] service_detail) {
        this.service_detail_type = service_detail_type;
        this.service_detail = service_detail;
    }
}
