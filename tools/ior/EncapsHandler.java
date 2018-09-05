/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package tools.ior;

/**
 * If you don't have a Helper class for a certain
 * tagged component or tagged profile, you can implement
 * this interface to display it's data properly.
 *
 * Otherwise, it will merely be printed as hex.
 *
 * See CodeBaseHandler for an example.
 *
 * See the two configuration files and TaggedMapFileReader
 * for how to associate your handler with a certain tag.
 */
public interface EncapsHandler
{
    /**
     * Decode and display the given data (which represents
     * a tagged profile or tagged component).
     */
    public void display(byte[] data, 
                        TextOutputHandler out,
                        Utility util) throws DecodingException;
}
