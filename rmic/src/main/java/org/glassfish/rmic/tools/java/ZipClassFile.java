/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This class is used to represent a file loaded from the class path, and
 * is a zip file entry.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
final
class ZipClassFile extends ClassFile {
    private final ZipFile zipFile;
    private final ZipEntry zipEntry;

    /**
     * Constructor for instance representing a zip file entry
     */
    public ZipClassFile(ZipFile zf, ZipEntry ze) {
        this.zipFile = zf;
        this.zipEntry = ze;
    }

    @Override
    public boolean isZipped() {
        return true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return zipFile.getInputStream(zipEntry);
        } catch (ZipException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return zipEntry.getName().endsWith("/");
    }

    @Override
    public long lastModified() {
        return zipEntry.getTime();
    }

    @Override
    public String getPath() {
        return zipFile.getName() + "(" + zipEntry.getName() + ")";
    }

    @Override
    public String getName() {
        return zipEntry.getName();
    }

//JCOV
    @Override
    public String getAbsoluteName() {
        return zipFile.getName() + "(" + zipEntry.getName() + ")";
    }
// end JCOV

    @Override
    public long length() {
        return zipEntry.getSize();
    }

    @Override
    public String toString() {
        return zipEntry.toString();
    }
}
