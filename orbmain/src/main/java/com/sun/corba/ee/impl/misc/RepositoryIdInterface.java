/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import org.omg.CORBA.ORB;
import java.io.Serializable;
import java.net.MalformedURLException;

/**
 * Methods on specific instances of RepositoryId.  Hides
 * versioning of our RepositoryId class.
 */
public interface RepositoryIdInterface
{
    Class getClassFromType() throws ClassNotFoundException;

    Class getClassFromType(String codebaseURL)
        throws ClassNotFoundException, MalformedURLException;

    Class getClassFromType(Class expectedType,
                           String codebaseURL) 
        throws ClassNotFoundException, MalformedURLException;

    String getClassName();
}
