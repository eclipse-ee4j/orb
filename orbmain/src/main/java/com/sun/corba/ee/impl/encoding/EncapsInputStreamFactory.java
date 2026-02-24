/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates.
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

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.org.omg.SendingContext.CodeBase;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author jwells
 *
 */
public class EncapsInputStreamFactory {
	public static EncapsInputStream newEncapsInputStream(final EncapsInputStream eis) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(eis);
					}
					
				});
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final byte[] buf,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, buf, size, byteOrder, version);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final byte[] data,
			final int size,
			final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, data, size, version);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final byte[] data,
			final int size) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, data, size);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final ByteBuffer byteBuffer,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, byteBuffer, size, byteOrder, version);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb, 
            final byte[] data, 
            final int size, 
            final GIOPVersion version, 
            final CodeBase codeBase) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, data, size, version, codeBase);
					}
					
				});
		
	}
	
	public static TypeCodeInputStream newTypeCodeInputStream(final org.omg.CORBA.ORB orb,
            final byte[] data,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<TypeCodeInputStream>() {

					@Override
					public TypeCodeInputStream run() {
						return new TypeCodeInputStream(orb, data, size, byteOrder, version);
					}
					
				});
		
	}
	
	public static TypeCodeInputStream newTypeCodeInputStream(final org.omg.CORBA.ORB orb,
			final byte[] data,
			final int size) {
		return AccessController.
				doPrivileged(new PrivilegedAction<TypeCodeInputStream>() {

					@Override
					public TypeCodeInputStream run() {
						return new TypeCodeInputStream(orb, data, size);
					}
					
				});
		
	}
	
	public static TypeCodeInputStream newTypeCodeInputStream(final org.omg.CORBA.ORB orb,
            final ByteBuffer byteBuffer,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<TypeCodeInputStream>() {

					@Override
					public TypeCodeInputStream run() {
						return new TypeCodeInputStream(orb, byteBuffer, size, byteOrder, version);
					}
					
				});
		
	}
}
