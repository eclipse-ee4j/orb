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
 * Manages text output with an indentation level.
 *
 * Default uses two spaces per indentation and System.out
 * for displaying.
 */
import java.io.PrintWriter;

public class TextOutputHandler
{
    private StringBuffer indentLevel;
    private String indentation;
    private PrintWriter writer;

    public TextOutputHandler() {
        this("  ", new PrintWriter(System.out, true));
    }

    public TextOutputHandler(String indentation) {
        this("  ", new PrintWriter(System.out, true));
    }

    public TextOutputHandler(String indentation,
                             PrintWriter writer) {
        this.indentation = indentation;
        this.writer = writer;

        indentLevel = new StringBuffer();
    }

    public void output(String msg) {
        writer.print(indentLevel);
        writer.println(msg);
    }

    public void increaseIndentLevel() {
        indentLevel.append(indentation);
    }

    public void decreaseIndentLevel() {
        indentLevel.setLength(indentLevel.length() - indentation.length());
    }
}
