/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;

/**
 * This class creates properties files for annotated exception interfaces. Applicable interfaces are annotated with the
 * {@link ExceptionWrapper} annotation. An entry will be made for each method with a {@link Message} annotation.
 */
@SupportedAnnotationTypes({"org.glassfish.pfl.basic.logex.ExceptionWrapper", "org.glassfish.pfl.basic.logex.Message"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ExceptionWrapperProcessor extends AbstractProcessor {

    Map<Element,FileGenerator> annotatedClasses = new HashMap<Element, FileGenerator>();
    Date creationDate = new Date();

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver() || typeElements.isEmpty()) {
            return false;
        }
        processClassElements(roundEnvironment.getElementsAnnotatedWith(ExceptionWrapper.class));
        processMethodElements(roundEnvironment.getElementsAnnotatedWith(Message.class));

        for (FileGenerator generator : annotatedClasses.values()) {
            writeFile(generator);
        }
        return true;
    }

    private void writeFile(FileGenerator generator) {
        try {
            if (generator.shouldWriteFile()) {
                generator.writeFile(processingEnv.getFiler());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClassElements(Set<? extends Element> classElements) {
        for (Element classElement : classElements) {
            annotatedClasses.put(classElement,new FileGenerator(classElement, creationDate));
        }
    }

    private void processMethodElements(Set<? extends Element> methodElements) {
        for (Element methodElement : methodElements) {
            if (annotatedClasses.containsKey(methodElement.getEnclosingElement())) {
                annotatedClasses.get(methodElement.getEnclosingElement()).addMethod(methodElement);
            }
        }
    }

}
