package org.glassfish.rmic.tools.java;

import java.io.File;

import org.junit.Test;

public class ClassPathTest {

    @Test
    public void init() {
        final String classPathString = "abc" + File.pathSeparator + "def";

        // this should not throw an exception
        new ClassPath(classPathString);
    }

}
