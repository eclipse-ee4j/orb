/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This class is used to represent a file loaded from the class path, and
 * is represented by nio Path.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
final
class PathClassFile extends ClassFile {
    private final Path path;
    private final BasicFileAttributes attrs;

    /**
     * Constructor for instance representing a Path
     */
    public PathClassFile(Path path) {
        this.path = path;
        try {
            this.attrs = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException ioExp) {
            throw new RmicUncheckedIOException(ioExp);
        }
    }

    @Override
    public boolean isZipped() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return attrs.isDirectory();
    }

    @Override
    public long lastModified() {
        return attrs.lastModifiedTime().toMillis();
    }

    @Override
    public String getPath() {
        return path.toUri().toString();
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

//JCOV
    @Override
    public String getAbsoluteName() {
        return path.toAbsolutePath().toUri().toString();
    }
// end JCOV

    @Override
    public long length() {
        return attrs.size();
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
