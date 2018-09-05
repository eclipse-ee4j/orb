/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.org.omg.CORBA.portable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.BoxedValueHelper;

/**
 * An interface that is implemented by valuetype helper classes.
 * This interface appeared in CORBA 2.3 drafts but was removed from
 * the published CORBA 2.3 specification.
 * <P>
 * @deprecated Deprecated by CORBA 2.3.
 */
@Deprecated
public interface ValueHelper extends BoxedValueHelper {
    Class get_class();
    String[] get_truncatable_base_ids();
    TypeCode get_type();
}

