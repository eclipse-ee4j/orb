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

import java.io.Serializable;
import com.sun.corba.ee.impl.io.TypeMismatchException;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;

/**
 * Factory methods for creating various repository ID strings
 * and instances.
 */
public interface RepositoryIdStrings
{
    String createForAnyType(Class type);

    String createForAnyType(Class type, ClassInfoCache.ClassInfo cinfo );
    
    String createForJavaType(Serializable ser)
        throws TypeMismatchException;
    
    String createForJavaType(Class clz)
        throws TypeMismatchException;
    
    String createForJavaType(Class clz, ClassInfoCache.ClassInfo cinfo )
        throws TypeMismatchException;
    
    String createSequenceRepID(java.lang.Object ser);
    
    String createSequenceRepID(java.lang.Class clazz);
    
    RepositoryIdInterface getFromString(String repIdString);

    String getClassDescValueRepId();
    String getWStringValueRepId();
}
