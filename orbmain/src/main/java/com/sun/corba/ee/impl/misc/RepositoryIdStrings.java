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

package com.sun.corba.ee.impl.misc;

import java.io.Serializable;
import com.sun.corba.ee.impl.io.TypeMismatchException;

import com.sun.corba.ee.impl.misc.ClassInfoCache;

/**
 * Factory methods for creating various repository ID strings and instances.
 */
public interface RepositoryIdStrings {
    String createForAnyType(Class type);

    String createForAnyType(Class type, ClassInfoCache.ClassInfo cinfo);

    String createForJavaType(Serializable ser) throws TypeMismatchException;

    String createForJavaType(Class clz) throws TypeMismatchException;

    String createForJavaType(Class clz, ClassInfoCache.ClassInfo cinfo) throws TypeMismatchException;

    String createSequenceRepID(java.lang.Object ser);

    String createSequenceRepID(java.lang.Class clazz);

    RepositoryIdInterface getFromString(String repIdString);

    String getClassDescValueRepId();

    String getWStringValueRepId();
}
