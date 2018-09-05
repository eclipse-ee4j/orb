/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport ;


import com.sun.corba.ee.spi.ior.IOR;
import java.util.Iterator;

public abstract interface ContactInfoListIterator
    extends Iterator<ContactInfo> {

    public void reportAddrDispositionRetry(ContactInfo contactInfo,
                                           short disposition);

    public void reportRedirect(ContactInfo contactInfo,
                               IOR forwardedIOR);

    public ContactInfoList getContactInfoList();

    public void reportSuccess(ContactInfo contactInfo);

    public boolean reportException(ContactInfo contactInfo, RuntimeException exception);

    public RuntimeException getFailureException();

}

// End of file.

