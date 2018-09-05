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

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.*;

/**
 * This class creates properties files for annotated exception interfaces. Applicable interfaces are annotated with the
 * {@link ExceptionWrapper} annotation. An entry will be made for each method with a {@link Message} annotation.
 */
@SupportedAnnotationTypes({"org.glassfish.pfl.basic.logex.ExceptionWrapper", "org.glassfish.pfl.basic.logex.Message"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ExceptionWrapperProcessor extends AbstractProcessor {

    Map<Element,FileGenerator> annotatedClasses = new HashMap<Element, FileGenerator>();
    Date creationDate = new Date();

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) return false;
        if (typeElements.isEmpty()) return false;

        processClassElements(roundEnvironment.getElementsAnnotatedWith(ExceptionWrapper.class));
        processMethodElements(roundEnvironment.getElementsAnnotatedWith(Message.class));

        for (FileGenerator generator : annotatedClasses.values())
            writeFile(generator);
        return true;
    }

    private void writeFile(FileGenerator generator) {
        try {
            if (generator.shouldWriteFile())
                generator.writeFile(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClassElements(Set<? extends Element> classElements) {
        for (Element classElement : classElements)
            annotatedClasses.put(classElement,new FileGenerator(classElement, creationDate));
    }

    private void processMethodElements(Set<? extends Element> methodElements) {
        for (Element methodElement : methodElements)
            if (annotatedClasses.containsKey(methodElement.getEnclosingElement()))
                annotatedClasses.get(methodElement.getEnclosingElement()).addMethod(methodElement);
    }

}
