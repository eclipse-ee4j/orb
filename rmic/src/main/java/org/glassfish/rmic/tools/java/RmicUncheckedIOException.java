/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Objects;

/**
 * Wraps an {@link IOException} with an unchecked exception. Copied from the JDK's UncheckedIOException, as it was only
 * added in JDK 8. Once the orb switches to JDK8 as a minimum, this can be replaced with the JDK class.
 */
public class RmicUncheckedIOException extends RuntimeException {
    private static final long serialVersionUID = -8134305061645241065L;

    /**
     * Constructs an instance of this class.
     *
     * @param message the detail message, can be null
     * @param cause the {@code IOException}
     *
     * @throws NullPointerException if the cause is {@code null}
     */
    public RmicUncheckedIOException(String message, IOException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    /**
     * Constructs an instance of this class.
     *
     * @param cause the {@code IOException}
     *
     * @throws NullPointerException if the cause is {@code null}
     */
    public RmicUncheckedIOException(IOException cause) {
        super(Objects.requireNonNull(cause));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return the {@code IOException} which is the cause of this exception.
     */
    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }

    /**
     * Called to read the object from a stream.
     *
     * @throws InvalidObjectException if the object is invalid or has a cause that is not an {@code IOException}
     */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof IOException))
            throw new InvalidObjectException("Cause must be an IOException");
    }
}
