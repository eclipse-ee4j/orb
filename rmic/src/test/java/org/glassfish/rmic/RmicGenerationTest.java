/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.glassfish.rmic.classes.covariantReturn.DogFinder;
import org.glassfish.rmic.classes.errorClasses.InterfaceWithNonRemoteMethod;
import org.glassfish.rmic.classes.errorClasses.NotRemoteClass;
import org.glassfish.rmic.classes.exceptiondetailsc.ExceptionSourceServantPOA;
import org.glassfish.rmic.classes.giopheaderpadding.FooServantPOA;
import org.glassfish.rmic.classes.hcks.RmiII;
import org.glassfish.rmic.classes.hcks.RmiIIServant;
import org.glassfish.rmic.classes.hcks.RmiIIServantPOA;
import org.glassfish.rmic.classes.inneraccess.Rainbow;
import org.glassfish.rmic.classes.islocal.MessageBuilderServantPOA;
import org.glassfish.rmic.classes.preinvokepostinvoke.MyServant;
import org.glassfish.rmic.classes.primitives.InterfaceWithConstantArray;
import org.glassfish.rmic.classes.primitives.InterfaceWithNonPrimitiveConstant;
import org.glassfish.rmic.classes.primitives.RmiTestRemoteImpl;
import org.glassfish.rmic.classes.rmipoacounter.CounterImpl;
import org.glassfish.rmic.classes.systemexceptions.ServerInvokerServantPOA;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests RMIC by comparing the kept generated source files against the expected files.
 */
public class RmicGenerationTest {

    private static int testNum = 0;
    private static File rootDir;
    private static final boolean COMPILE_GENERATED = true;  // set false to check generated files without compiling

    @BeforeClass
    public static void clearRootDir() throws IOException {
        rootDir = Files.createTempDirectory("rmic").toFile();
    }

    @AfterClass
    public static void cleanRootDir() throws IOException {
        FileUtils.deleteDirectory(rootDir);
    }

    @Test
    public void whenDefaultGeneratorSpecified_reportJRMPNoLongerSupported() throws Exception {
        GenerationControl generator = new GenerationControl(FooServantPOA.class);

        try {
            generator.generate();
            fail("Should have reported JRMP no longer supported");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("no longer supported"));
        }
     }

    @Test
    public void generateIiopStubsWithoutPoa() throws Exception {
        GenerationControl generator = new GenerationControl(RmiIIServant.class);
        generator.addArgs("-iiop", "-keep");

        generator.generate();

        checkGeneratedFiles(generator, "without_poas", ".java");
    }

    @Test
    public void whenKeepNotSpecified_deleteGeneratedJavaFiles() throws Exception {
        assumeTrue(COMPILE_GENERATED);
        GenerationControl generator = new GenerationControl(RmiIIServant.class);
        generator.addArgs("-iiop");

        generator.generate();

        checkGeneratedFilesDeleted(generator);
    }

    @Test
    public void afterProcessing_classFilesArePresent() throws Exception {
        assumeTrue(COMPILE_GENERATED);
        GenerationControl generator = new GenerationControl(RmiIIServant.class);
        generator.addArgs("-iiop");

        generator.generate();

        checkClassFilesPresent(generator, "without_poas");
    }


    @Test
    public void generateIiopStubsFromInterface() throws Exception {
        GenerationControl generator = new GenerationControl(RmiII.class);
        generator.addArgs("-iiop", "-keep");

        generator.generate();

        checkGeneratedFiles(generator, "stub_from_interface", ".java");
    }

    @Test
    public void generateIiopStubsWithPoa() throws Exception {
        GenerationControl generator = new GenerationControl(
                ExceptionSourceServantPOA.class, MyServant.class, MessageBuilderServantPOA.class, CounterImpl.class,
                ServerInvokerServantPOA.class, RmiIIServantPOA.class, FooServantPOA.class);
        generator.addArgs("-iiop", "-keep", "-poa");
        generator.generate();

        checkGeneratedFiles(generator, "with_poas", ".java");
    }

    @Test
    public void generateIiopStubsWithConstant() throws Exception {
        GenerationControl generator = new GenerationControl(RmiTestRemoteImpl.class);
        generator.addArgs("-iiop","-always", "-keep");
        generator.generate();

        checkGeneratedFiles(generator, "primitives", ".java");
    }

    @Test(expected = AssertionError.class)
    public void dontGenerateIiopStubsWithConstantArray() throws Exception {
        GenerationControl generator = new GenerationControl(InterfaceWithConstantArray.class);
        generator.addArgs("-iiop","-always", "-keep");
        generator.generate();
    }

    @Test(expected = AssertionError.class)
    public void dontGenerateIiopStubsWithConstantException() throws Exception {
        GenerationControl generator = new GenerationControl(InterfaceWithNonPrimitiveConstant.class);
        generator.addArgs("-iiop","-always", "-keep");
        generator.generate();
    }

    @Test
    public void generateIdlForInnerClass() throws Exception {
        GenerationControl generator = new GenerationControl(Rainbow.getInterfaceCheckerClass());
        generator.addArgs("-idl", "-keep");
        generator.generate();

        checkGeneratedFiles(generator, "idl", ".idl");
    }

    @Test
    public void whenKeepFlagNotSpecified_dontDeleteGeneratedIdlFiles() throws Exception {
        GenerationControl generator = new GenerationControl(Rainbow.getInterfaceCheckerClass());
        generator.addArgs("-idl");
        generator.generate();

        checkGeneratedFiles(generator, "idl", ".idl");
    }

    @Test
    public void generateIdlForInnerClassUsingDotNotation() throws Exception {
        GenerationControl generator = new GenerationControl(Rainbow.getQualifiedCheckerClassName());
        generator.addArgs("-idl", "-keep");
        generator.generate();

        checkGeneratedFiles(generator, "idl", ".idl");
    }

    // NOTE: The test case from which this was based (http://hg.openjdk.java.net/jdk/jdk/file/9a29aa153c20/test/jdk/sun/rmi/rmic/covariantReturns)
    // doesn't actually seem to test the feature. This one verifies that we can generate the stubs, but they appear to me to be incorrect,
    // in that the generated derived type is actually returning the same type as its parent rather than the specified covariant type.
    // More testing will be needed to see if it matters.
    @Test
    public void canHandleCovariantReturns() throws Exception {
        GenerationControl generator = new GenerationControl(DogFinder.class);
        generator.addArgs("-iiop", "-keep");
        generator.generate();
    }

    @Test
    public void whenBinaryIsMissing_dontCompileSources() throws Exception {
        File generatedFile = new File(getTargetClassDir() + "Interface.java");
        BufferedWriter writer = new BufferedWriter(new FileWriter(generatedFile));
        writer.write("public class Interface implements java.rmi.Remote { }");
        writer.close();

        GenerationControl generator = new GenerationControl("Interface");
        generator.addArgs("-iiop");

        try {
            generator.generate();
            fail("Should not have succeeded in generating a stub");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("Class Interface not found"));
        }
    }

    private String getTargetClassDir() {
        return TestUtils.getClassPathString().split(File.pathSeparator)[0];
    }

    @Test(expected = AssertionError.class)
    public void whenClassDoesNotImplementRemote_cannotGenerate() throws Exception {
        GenerationControl generator = new GenerationControl(NotRemoteClass.class);
        generator.addArgs("-iiop");
        generator.generate();
    }
    
    @Test(expected = AssertionError.class)
    public void whenInterfaceHasNonRemoteMethod_cannotGenerate() throws Exception {
        GenerationControl generator = new GenerationControl(InterfaceWithNonRemoteMethod.class);
        generator.addArgs("-iiop");
        generator.generate();
    }

    @Test
    public void canGenerateNonPublicClass() throws Exception {
        GenerationControl generator = new GenerationControl("org.glassfish.rmic.classes.errorClasses.PackageInterface");
        generator.addArgs("-iiop");
        generator.generate();
    }

    // Confirms that the generated files match those in the specified directory of master files
    private void checkGeneratedFiles(GenerationControl generator, String mastersSubDir, String suffix) throws IOException {
        File masterDir = new File(getModuleRoot(), "src/test/masters/" + mastersSubDir);

        String[] generatedFilePaths = getFilePaths(generator.getDestDir(), suffix);
        String[] expectedFilePaths = getFilePaths(masterDir, suffix);

        assertThat("In " + generator.getDestDir(), generatedFilePaths, arrayContaining(expectedFilePaths));
        compareGeneratedFiles(masterDir, generator.getDestDir(), expectedFilePaths);
    }

    private File getModuleRoot() {
        String classPathString = getTargetClassDir();
        return new File(classPathString.substring(0, classPathString.lastIndexOf("/target/")));
    }

    // Verifies that the generated files were deleted
    private void checkGeneratedFilesDeleted(GenerationControl generator) {
        String[] generatedFilePaths = getFilePaths(generator.getDestDir(), ".java");

        assertThat("In " + generator.getDestDir(), generatedFilePaths, emptyArray());
    }

    // Confirms that the generated files match those in the specified directory of master files
    @SuppressWarnings("SameParameterValue")
    private void checkClassFilesPresent(GenerationControl generator, String mastersSubDir) {
        File masterDir = new File(getModuleRoot(), "src/test/masters/" + mastersSubDir);

        String[] generatedFilePaths = getFilePaths(generator.getDestDir(), ".class");
        String[] expectedFilePaths = toClassFilePaths(getFilePaths(masterDir, ".java"));

        assertThat("In " + generator.getDestDir(), generatedFilePaths, arrayContaining(expectedFilePaths));
    }

    private String[] toClassFilePaths(String[] sourceFilePaths) {
        String[] result = new String[sourceFilePaths.length];
        for (int i = 0; i < sourceFilePaths.length; i++)
            result[i] = sourceFilePaths[i].replace(".java", ".class");
        return result;
    }

    // Returns a sorted array of paths to files with the specified suffix under the specified directory, relative to that directory
    private String[] getFilePaths(File rootDir, String suffix) {
        ArrayList<String> files = new ArrayList<>();
        appendFiles(files, rootDir, rootDir.getAbsolutePath().length() + 1, suffix);
        Collections.sort(files);
        return files.toArray(new String[0]);
    }

    @SuppressWarnings("ConstantConditions")
    private void appendFiles(ArrayList<String> files, File currentDir, int rootDirLength, String suffix) {
        for (File file : currentDir.listFiles())
            if (file.isDirectory())
                appendFiles(files, file, rootDirLength, suffix);
            else if (file.getName().endsWith(suffix))
                files.add(getRelativePath(file, rootDirLength));
    }

    private String getRelativePath(File file, int rootDirLength) {
        return file.getAbsolutePath().substring(rootDirLength);
    }
    
    private void compareGeneratedFiles(File expectedDir, File actualDir, String... generatedFileNames) throws IOException {
        for (String filePath : generatedFileNames)
            compareFiles(filePath, expectedDir, actualDir);
    }

    private void compareFiles(String filePath, File masterDirectory, File generationDirectory) throws IOException {
        File expectedFile = new File(masterDirectory, filePath);
        File actualFile = new File(generationDirectory, filePath);

        compareFiles(expectedFile, actualFile);
    }

    private void compareFiles(File expectedFile, File actualFile) throws IOException {
        LineNumberReader expected = new LineNumberReader(new FileReader(expectedFile));
        LineNumberReader actual = new LineNumberReader(new FileReader(actualFile));

        String expectedLine = "";
        String actualLine = "";
        while (expectedLine != null && actualLine != null && linesMatch(expectedLine, actualLine)) {
            expectedLine = expected.readLine();
            actualLine = actual.readLine();
        }

        if (expectedLine == null && actualLine == null) return;

        if (expectedLine == null)
            fail("Unexpected line in generated file at " + actual.getLineNumber() + ": " + actualLine);
        else if (actualLine == null)
            fail("Actual file ends unexpectedly at line " + expected.getLineNumber());
        else
            fail("Generated file mismatch at line " + actual.getLineNumber() +
                    "\nshould be <" + expectedLine + "> " +
                    "\nbut found <" + actualLine + ">");

    }

    private boolean linesMatch(String expectedLine, String actualLine) {
        return expectedLine.equals(actualLine) || expectedLine.trim().startsWith("* ");
    }


    private class GenerationControl {
        private ArrayList<String> argList = new ArrayList<>();
        private String[] classNames;
        private File destDir;

        @SuppressWarnings("ResultOfMethodCallIgnored")
        GenerationControl(String... classNames) {
            this.classNames = classNames;

            String classPath = TestUtils.getClassPathString();
            destDir = new File(rootDir + "/" + (++testNum));
            destDir.mkdirs();
            addArgs("-classpath", classPath, "-d", destDir.getAbsolutePath());
        }

        private GenerationControl(Class<?>... classes) {
            this(toNameList(classes));
        }

        private void addArgs(String... args) {
            argList.addAll(Arrays.asList(args));
        }

        File getDestDir() {
            return destDir;
        }

        private void generate() throws IOException {
            if (argList.contains("-iiop") && !COMPILE_GENERATED) addArgs("-Xnocompile");
            for (String name : classNames)
                addArgs(name);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Main compiler = new Main(out, "rmic");
            String[] argv = argList.toArray(new String[0]);
            if (!compiler.compile(argv))
                throw createException(out);
        }

        private AssertionError createException(ByteArrayOutputStream out) throws IOException {
            String message = toMessage(out);
            if (message == null) message = "No error message reported";
            return new AssertionError(message);
        }

        private String toMessage(ByteArrayOutputStream out) throws IOException {
            out.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
            BufferedReader reader = new BufferedReader(new InputStreamReader(bais));

            StringBuilder sb;
            String line = reader.readLine();
            if (line == null)
                return null;
            else {
                sb = new StringBuilder(line);
                while ((line = reader.readLine()) != null && !line.startsWith("Usage:"))
                    sb.append("/n").append(line);
                return sb.toString();
            }
        }
    }

    private static String[] toNameList(Class<?>[] classes) {
        String[] nameList = new String[classes.length];
        for (int i = 0; i < classes.length; i++)
            nameList[i] = classes[i].getName();
        return nameList;
    }
}
