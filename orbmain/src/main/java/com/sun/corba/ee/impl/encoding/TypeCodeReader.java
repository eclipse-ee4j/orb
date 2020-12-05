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

package com.sun.corba.ee.impl.encoding;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.omg.CORBA.TypeCode ;
import org.omg.CORBA.StructMember ;
import org.omg.CORBA.UnionMember ;
import org.omg.CORBA.ValueMember ;
import org.omg.CORBA.TCKind ;
import org.omg.CORBA.Any ;
import org.omg.CORBA.BAD_TYPECODE ;
import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.MARSHAL ;

import org.omg.CORBA.TypeCodePackage.BadKind ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.impl.encoding.OSFCodeSetRegistry;
import com.sun.corba.ee.impl.encoding.MarshalInputStream;
import com.sun.corba.ee.impl.encoding.CodeSetConversion;

public interface TypeCodeReader extends MarshalInputStream {
    public void addTypeCodeAtPosition(TypeCodeImpl tc, int position);
    public TypeCodeImpl getTypeCodeAtPosition(int position);
    public void setEnclosingInputStream(InputStream enclosure);
    public TypeCodeReader getTopLevelStream();
    public int getTopLevelPosition();
    public int getPosition();
    public void printTypeMap();
}
