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

package com.sun.tools.corba.ee.idl.toJavaPortable;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * Tests IDLJ by comparing the generated source files against the expected files.
 */
public class IdljGenerationTest {

    private static int testNum = 0;
    private static File rootDir;
    private static final boolean COMPILE_GENERATED = true;  // set false to check generated files without compiling

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeClass
    public static void clearRootDir() throws IOException {
        rootDir = Files.createTempDirectory("idlj").toFile();
    }

    @AfterClass
    public static void cleanRootDir() throws IOException {
        FileUtils.deleteDirectory(rootDir);
    }

    @Test
    public void generateClassesWithoutOptions() throws Exception {
        GenerationControl generator = new GenerationControl("src/main/java/com/sun/tools/corba/ee/idl/ir.idl");

        generator.generate();

        checkGeneratedFiles(generator, "ir");
    }

    @Test
    public void generateStructureClasses() throws Exception {
        GenerationControl generator = new GenerationControl("src/test/idl/CorbaServerTest.idl");
        generator.addArgs("-oldImplBase", "-fall");
        generator.addIncludePath("src/main/java/com/sun/tools/corba/ee/idl");

        generator.generate();

        checkGeneratedFiles(generator, "structures");
    }

    // Confirms that the generated files match those in the specified directory of master files
    private void checkGeneratedFiles(GenerationControl generator, String mastersSubDir) throws IOException {
        File masterDir = new File(getModuleRoot(), "src/test/masters/" + mastersSubDir);

        String[] generatedFilePaths = getFilePaths(generator.getDestDir());
        String[] expectedFilePaths = getFilePaths(masterDir);

        assertThat("In " + generator.getDestDir(), generatedFilePaths, arrayContaining(expectedFilePaths));
        compareGeneratedFiles(masterDir, generator.getDestDir(), expectedFilePaths);
    }

    private static File getModuleRoot() {
        String classPathString = TestUtils.getClassPathString();
        return new File(classPathString.substring(0, classPathString.lastIndexOf("/target/")));
    }

    // Returns a sorted array of paths to files with the specified suffix under the specified directory, relative to that directory
    private String[] getFilePaths(File rootDir) {
        ArrayList<String> files = new ArrayList<>();
        appendFiles(files, rootDir, rootDir.getAbsolutePath().length() + 1, ".java");
        Collections.sort(files);
        return files.toArray(new String[files.size()]);
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
            fail("Generated file mismatch in " + actualFile + " at line " + actual.getLineNumber() +
                    "\nshould be <" + expectedLine + "> " +
                    "\nbut found <" + actualLine + ">");

    }

    private boolean linesMatch(String expectedLine, String actualLine) {
        return expectedLine.equals(actualLine) || expectedLine.trim().startsWith("* ");
    }


    private class GenerationControl {
        private ArrayList<String> argList = new ArrayList<>();
        private String[] idlFiles;
        private File destDir;
        private String warning;

        @SuppressWarnings("ResultOfMethodCallIgnored")
        GenerationControl(String... idlFiles) {
            this.idlFiles = idlFiles;

            destDir = new File(rootDir + "/" + (++testNum));
            destDir.mkdirs();
            addArgs("-td", destDir.getAbsolutePath());
        }

        private void addIncludePath(String includePath) {
            addArgs("-i", new File(getModuleRoot(), includePath).getAbsolutePath());
        }

        private void addArgs(String... args) {
            argList.addAll(Arrays.asList(args));
        }

        File getDestDir() {
            return destDir;
        }

        String getWarning() {
            return warning;
        }

        private void generate() throws IOException {
            if (argList.contains("-iiop") && !COMPILE_GENERATED) addArgs("-Xnocompile");
            for (String name : idlFiles)
                addArgs(new File(getModuleRoot(), name).getAbsolutePath());
            Compile.compiler = new Compile();
            String[] argv = argList.toArray(new String[argList.size()]);
            Compile.compiler.start(argv);
        }

    }

    private static String[] toNameList(Class<?>[] classes) {
        String[] nameList = new String[classes.length];
        for (int i = 0; i < classes.length; i++)
            nameList[i] = classes[i].getName();
        return nameList;
    }
}
