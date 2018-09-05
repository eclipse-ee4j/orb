/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.primitives;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiTestRemote extends Remote {
	public static final String JNDI_NAME = "IIOP_RmiTestRemote";
	public static final boolean A_BOOLEAN = true;
	public static final char A_CHAR = 'x';
	public static final byte A_BYTE = 0x34;
	public static final short A_SHORT = 12;
	public static final int AN_INT = 17;
	public static final long A_LONG = 1234567;
	public static final float A_FLOAT = 123.5f;
	public static final double A_DOUBLE = 123.567;

   	void test_ping() throws RemoteException;

   	int test_int(int x) throws RemoteException;
}
