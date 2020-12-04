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
 * An abstract value type that is meant to 
 * be used by the ORB, not the user. Semantically it is treated 
 * as a custom valuetype's implicit base class, although the custom 
 * valuetype does not actually inherit it in IDL. The implementer 
 * of a custom value type shall provide an implementation of the 
 * <tt>CustomMarshal</tt> operations. The manner in which this is done is 
 * specified in the IDL to Java language mapping. Each custom 
 * marshaled value type shall have its own implementation.
 * @see DataOutputStream
 * @see DataInputStream
 */
public interface CustomMarshal {
    /**
     * Marshal method has to be implemented by the Customized Marshal class
     * This is the method invoked for Marshalling.
     * 
     * @param os a DataOutputStream
     */ 
    void marshal(DataOutputStream os);
    /**
     * Unmarshal method has to be implemented by the Customized Marshal class
     * This is the method invoked for Unmarshalling.
     * 
     * @param is a DataInputStream
     */ 
    void unmarshal(DataInputStream is);
}
