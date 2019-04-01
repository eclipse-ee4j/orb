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

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class is used to represent a file loaded from the class path, and is a regular file.
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
final class FileClassFile extends ClassFile {
    private final File file;

    /**
     * Constructor for instance representing a regular file
     */
    public FileClassFile(File file) {
        this.file = file;
    }

    @Override
    public boolean isZipped() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public String getName() {
        return file.getName();
    }

//JCOV
    @Override
    public String getAbsoluteName() {
        String absoluteName;
        try {
            absoluteName = file.getCanonicalPath();
        } catch (IOException e) {
            absoluteName = file.getAbsolutePath();
        }
        return absoluteName;
    }
// end JCOV

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
