/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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
