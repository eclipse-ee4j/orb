/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.annotation.processing;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
* This class generates properties files based on annotations.
*/
class FileGenerator {
    private Element classElement;
    private Date creationDate;
    private List<Element> methodElements = new ArrayList<Element>();

    FileGenerator(Element classElement, Date creationDate) {
        this.classElement = classElement;
        this.creationDate = creationDate;
    }

    String getPrefix() {
        ExceptionWrapper wrapper = classElement.getAnnotation(ExceptionWrapper.class);
        return wrapper.idPrefix();
    }

    FileObject createResource(Filer filer) throws IOException {
        return filer.createResource(StandardLocation.CLASS_OUTPUT, getPackage(), getName() + ".properties");
    }

    void addMethod(Element methodElement) {
        methodElements.add(methodElement);
    }

    String getPackage() {
        return classElement.getEnclosingElement().toString();
    }

    private String getName() {
        return classElement.getSimpleName().toString();
    }

    boolean shouldWriteFile() {
        return !methodElements.isEmpty();
    }

    void writePropertyFileHeader(Writer writer) throws IOException {
        writer.append("### Resource file generated on ").append(creationDate.toString()).append('\n');
        writer.append("#\n");
        writer.append("# Resources for class ").append(classElement.toString()).append('\n');
        writer.append("#\n");
    }

    void writePropertyLines(Writer writer) throws IOException {
        for (Element methodElement : methodElements)
            writePropertyLine(writer, methodElement);
    }

    private void writePropertyLine(Writer writer, Element methodElement) throws IOException {
        writer.append('.').append(methodElement.getSimpleName()).append("=\"").append(getPrefix())
              .append(": ").append(getMessage(methodElement)).append("\"\n");
    }

    private String getMessage(Element methodElement) {
        return methodElement.getAnnotation(Message.class).value();
    }

    void writeContents(Writer writer) throws IOException {
        writePropertyFileHeader(writer);
        writePropertyLines(writer);
        writer.close();
    }

    void writeFile(Filer filer) throws IOException {
        FileObject file = createResource(filer);
        writeContents(file.openWriter());
    }
}
