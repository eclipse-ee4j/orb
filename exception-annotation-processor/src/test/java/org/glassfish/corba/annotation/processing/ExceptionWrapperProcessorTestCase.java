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

import com.meterware.simplestub.Stub;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

import static com.meterware.simplestub.Stub.createStub;
import static org.junit.Assert.*;

/**
 * A test for the ExceptionWrapper annotation Processor.
 *
 * @author Russell Gold
 */
public class ExceptionWrapperProcessorTestCase {

    private TestRoundEnvironment roundEnvironment;
    private ExceptionWrapperProcessor processor;
  private List<FileObject> files = new ArrayList<FileObject>();
    private Set<TypeElement> typeElements;
    private FileGenerator fileGenerator;
    private Date creationDate = new Date();
    private TestElement annotatedClassElement;
    private Map<Class<? extends Annotation>,Set<Element>> annotations = new HashMap<Class<? extends Annotation>, Set<Element>>();

    @Before
    public void setUp() throws Exception {
        roundEnvironment = new TestRoundEnvironment();
        processor = new ExceptionWrapperProcessor();
        processor.init(new TestProcessingEnvironment());
        typeElements = new HashSet<TypeElement>();
        annotatedClassElement = createAnnotatedClass("org.glassfish.corba.AnException", "SF");
        fileGenerator = new FileGenerator(annotatedClassElement, creationDate);
    }

    @Test
    public void processor_supportsExceptionWrapperAnnotation() {
        SupportedAnnotationTypes annotation = ExceptionWrapperProcessor.class.getAnnotation(SupportedAnnotationTypes.class);
        assertTrue(Arrays.asList(annotation.value()).contains(ExceptionWrapper.class.getName()));
    }

    @Test
    public void processor_supportsMessageAnnotation() {
        SupportedAnnotationTypes annotation = ExceptionWrapperProcessor.class.getAnnotation(SupportedAnnotationTypes.class);
        assertTrue(Arrays.asList(annotation.value()).contains(Message.class.getName()));
    }

    @Test
    public void process_supportsSourceVersion8() {
        SupportedSourceVersion annotation = ExceptionWrapperProcessor.class.getAnnotation(SupportedSourceVersion.class);
        assertEquals(SourceVersion.RELEASE_8, annotation.value());
    }

    @Test
    public void processor_extendsAbstractProcessor() {
        Class superclass = ExceptionWrapperProcessor.class.getSuperclass();
        assertEquals(AbstractProcessor.class, superclass);
    }

    @Test
    public void processer_isRegistered() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("META-INF/services/javax.annotation.processing.Processor");
        assertNotNull("Resource not found in classpath",inputStream);
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isr);
        assertEquals(ExceptionWrapperProcessor.class.getName(), reader.readLine());
    }

    @Test
    public void whenNoExceptionWrapperAnnotations_doNothing() {
        assertFalse(processor.process(typeElements, roundEnvironment));
        assertTrue(files.isEmpty());
    }

    @Test
    public void whenProcessingIsComplete_doNothing() {
        declareProcessingOver();
        assertFalse(processor.process(typeElements, roundEnvironment));
        assertTrue(files.isEmpty());
    }

    @Test
    public void givenAnnotationElement_canExtractPrefix() {
        assertEquals("SF", fileGenerator.getPrefix());
    }

    @Test
    public void givenAnnotationElement_canExtractPackage() {
        assertEquals("org.glassfish.corba", fileGenerator.getPackage());
    }

    @Test
    public void givenAnnotationElement_canCreateFileObject() throws IOException {
        FileObject fileObject = fileGenerator.createResource(new TestFiler());
        assertEquals(asUnixPath("file:" + System.getProperty("user.dir") + "/CLASS_OUTPUT/org/glassfish/corba/AnException.properties"),
                asUnixPath(fileObject.toUri().toString()));
    }

    private String asUnixPath( String path ) {
        String result = path.replace('\\', '/');
        if (result.startsWith("file:/C:")) result = "file:C:" + result.substring(8);
        return result;
    }

    private TestElement createAnnotatedClass(String className, String prefix) {
        TestElement classElement = createElement(className);
        ExceptionWrapper ew = new TestExceptionWrapper(prefix);
        classElement.addAnnotation(ew);
        getAnnotatedElements(ExceptionWrapper.class).add(classElement);
        return classElement;
    }

    private Set<Element> getAnnotatedElements(Class<? extends Annotation> annotationClass) {
        if (annotations.containsKey(annotationClass))
            return annotations.get(annotationClass);
        else
            return createAnnotatedElementsSet(annotationClass);
    }

    private Set<Element> createAnnotatedElementsSet(Class<? extends Annotation> annotationClass) {
        Set<Element> set = new HashSet<Element>();
        annotations.put(annotationClass, set);
        return set;
    }


    @Test
    public void withoutMethods_generatorWillNotRun() {
        assertFalse( fileGenerator.shouldWriteFile());
    }

    @Test
    public void withMethods_generatorWillRun() {
        fileGenerator.addMethod(createAnnotatedMethod("method1", "log message 1"));
        assertTrue( fileGenerator.shouldWriteFile());
    }

    @Test
    public void givenAnnotatedMethod_canGenerateProperties() throws IOException {
        fileGenerator.addMethod(createAnnotatedMethod("method1", "log message 1"));
        fileGenerator.addMethod(createAnnotatedMethod("method2", "log message 2"));

        StringWriter writer = new StringWriter();
        fileGenerator.writePropertyLines(writer);
        writer.close();
        assertEquals(".method1=\"SF: log message 1\"\n.method2=\"SF: log message 2\"\n", writer.toString());
    }

    @Test
    public void generator_canCreateHeader() throws IOException {
        StringWriter writer = new StringWriter();
        fileGenerator.writePropertyFileHeader( writer );
        writer.close();

        assertEquals("### Resource file generated on " + creationDate + "\n" +
                     "#\n" +
                     "# Resources for class org.glassfish.corba.AnException\n" +
                     "#\n",
                     writer.toString() );
    }

    private TestElement createAnnotatedMethod(String methodName, String message) {
        TestElement methodElement = createStub(TestElement.class, methodName, annotatedClassElement);
        org.glassfish.pfl.basic.logex.Message annotation = new TestMessage(message);
        methodElement.addAnnotation(annotation);
        getAnnotatedElements(Message.class).add(methodElement);
        return methodElement;
    }

    @Test
    public void whenOneClassAnnotated_createFile() {
        populateTypeElements();
        fileGenerator.addMethod(createAnnotatedMethod("method1", "log message 1"));
        assertTrue(processor.process(typeElements, roundEnvironment));
        assertFalse(files.isEmpty());
    }

    private void populateTypeElements() {
        addToTypeElements(ExceptionWrapper.class);
        addToTypeElements(Message.class);
    }

    private void addToTypeElements(Class<? extends Annotation> annotationClass) {
        typeElements.add(createElement(annotationClass.getName()));
    }

    private static TestElement createElement(String fullName) {
        TestElement leaf = null;
        for (String component : fullName.split("\\.")) {
            leaf = Stub.createStub(TestElement.class, component, leaf);
        }
        return leaf;
    }

    private void declareProcessingOver() {
        roundEnvironment.processingOver = true;
    }

    class TestRoundEnvironment implements RoundEnvironment {
        boolean processingOver;

        public boolean processingOver() {
            return processingOver;
        }

        public boolean errorRaised() { return false; }
        public Set<? extends Element> getRootElements() { return null; }

        @Override
        public Set<? extends Element> getElementsAnnotatedWith(TypeElement typeElement) {
            return null;
        }

        @Override
        public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> aClass) {
            return annotations.get(aClass);
        }
    }

    static class TestExceptionWrapper implements ExceptionWrapper {
        private String idPrefix;

        TestExceptionWrapper(String idPrefix) {
            this.idPrefix = idPrefix;
        }

        public String idPrefix() {
            return idPrefix;
        }

        public String loggerName() {
            return "";
        }

        public String resourceBundle() {
            return "";
        }

        public Class<? extends Annotation> annotationType() {
            return ExceptionWrapper.class;
        }
    }

    private class TestMessage implements Message {
        private String value;

        public TestMessage(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Message.class;
        }
    }

    class TestProcessingEnvironment implements ProcessingEnvironment {

        private TestFiler filer;

        @Override
        public Map<String, String> getOptions() {
            return null;
        }

        @Override
        public Messager getMessager() {
            return null;
        }

        @Override
        public Filer getFiler() {
            if (filer == null) filer = new TestFiler();
            return filer;
        }

        @Override
        public Elements getElementUtils() {
            return null;
        }

        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public SourceVersion getSourceVersion() {
            return null;
        }

        @Override
        public Locale getLocale() {
            return null;
        }


    }

    class TestFiler implements Filer {

        @Override
        public JavaFileObject createSourceFile(CharSequence charSequence, Element... elements) throws IOException {
            return null;
        }

        @Override
        public JavaFileObject createClassFile(CharSequence charSequence, Element... elements) throws IOException {
            return null;
        }

        @Override
        public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence trailing, Element... elements) throws IOException {
            File file = new File(location.getName());
            file = new File(file, pkg.toString().replace('.','/'));
            file = new File(file, trailing.toString());
            TestFileObject testFileObject = new TestFileObject(file.toURI());
            files.add(testFileObject);
            return testFileObject;
        }

        @Override
        public FileObject getResource(JavaFileManager.Location location, CharSequence charSequence, CharSequence charSequence1) throws IOException {
            return null;
        }


    }

    class TestFileObject implements FileObject {

        private URI uri;


        TestFileObject(URI uri) {
            this.uri = uri;
        }

        @Override
        public URI toUri() {
            return uri;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return null;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return null;
        }

        @Override
        public Reader openReader(boolean b) throws IOException {
            return null;
        }

        @Override
        public CharSequence getCharContent(boolean b) throws IOException {
            return null;
        }

        @Override
        public Writer openWriter() throws IOException {
          return new StringWriter();
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public boolean delete() {
            return false;
        }


    }

    abstract static class TestElement implements TypeElement {
        private Element enclosingElement;
        private Name simpleName;

        private List<Annotation> annotations = new ArrayList<Annotation>();

        TestElement(String component, Element enclosingElement) {
            simpleName = new TestName(component);
            this.enclosingElement = enclosingElement;
        }

        void addAnnotation(Annotation annotation) {
            annotations.add(annotation);
        }

        public <A extends Annotation> A getAnnotation(Class<A> aClass) {
            for (Annotation annotation : annotations)
                if (aClass.isInstance(annotation)) return castTo(annotation);
            return null;
        }

      @SuppressWarnings("unchecked")
      private <A extends Annotation> A castTo(Annotation annotation) {
        return (A) annotation;
      }

      public Name getSimpleName() {
            return simpleName;
        }

        public Element getEnclosingElement() {
            return enclosingElement;
        }

        @Override
        public String toString() {
            return enclosingElement == null ? simpleName.toString()
                    : enclosingElement + "." + simpleName;
        }


    }

    static class TestName implements Name {

        private CharSequence value;

        TestName(CharSequence value) {
            this.value = value;
        }

        @Override
        public boolean contentEquals(CharSequence charSequence) {
            return value.equals(charSequence);
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int i) {
            return value.charAt(i);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value.toString();
        }

    }
}
