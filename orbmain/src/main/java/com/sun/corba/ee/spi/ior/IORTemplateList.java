/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior ;

import java.util.List ;

/** An IORTemplateList is a list of IORTemplate instances.  It can be used to create IORs.
 * This is useful for representing IORs made of profiles from different object
 * adapters.
 * Note that any IORFactory can be added to an IORTemplateList, but it is flattened
 * so that the result is just a list of IORTemplate instances.
 */
public interface IORTemplateList extends List<IORTemplate>, 
    IORFactory, MakeImmutable 
{
}

