/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import sun.tools.java.*;
import sun.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/**
 * Given two classpaths and a list of common classes, this
 * will show differences between the same class in the
 * different classpaths.
 *
 * Differences are for public or protected members only.
 *
 * Requires tools.jar, but has no dependency on rip-int.
 *
 * Should take into account:
 *       Methods
 *          Name
 *          Return value
 *          Parameter types
 *          Modifier differences of {public, protected, static}
 *
 *       Fields
 *          Name
 *          Type
 *          All Modifier differences
 *
 *       Classes
 *          Name
 *          Superclass name
 *          Implemented interfaces
 *          Defined classes
 *          Fields
 *          Methods
 *
 * This is a dirty tool, not a work of high performance art (though
 * it's pretty fast in practice).
 *
 * WARNING: Possibility of infinite loop when each class has a field
 * of the other.
 */

public class ClassComparer
{
    private static final String LINE_SEPARATOR;
    static {
        LINE_SEPARATOR = System.getProperty("line.separator");
    }

    /**
     * Used for Methods, Fields, and Classes.
     */
    private interface CCComparable {

        /**
         * Is this equivalent to the given value?  This is a
         * weaker comparison than equals, used to match up
         * what should be equal members.  For instance, a static
         * field foo in Class c and a non-static field foo in
         * Class c' are equivalent but not equal.
         */
        public boolean isEquivalent(Object obj);

        /**
         * Strong comparison -- should be identical in all
         * elements used in our test.
         */
        public boolean equals(Object obj);

        /**
         * Even though HashSet doesn't use this for its
         * equals comparison, best to provide it.
         */
        public int hashCode();
    }

    /**
     * Wrapper for a java.reflect.Field, hiding differences
     * in ClassLoader.
     */
    private static class CCField implements CCComparable {

        public Field field;

        public CCField(Field field) {
            this.field = field;
        }

        /**
         * Checks for
         *
         * 1. Equivalency
         * 2. Modifiers
         * 3. Type
         */
        public boolean equals(Object obj) {

            if (obj == null)
                return false;

            if (this == obj)
                return true;

            CCField other = (CCField)obj;

            return (isEquivalent(other) &&
                    field.getModifiers() == other.field.getModifiers() &&
                    field.getType().getName().equals(other.field.getType().getName()));
        }

        /**
         * Checks for
         *
         * 1. Name
         */
        public boolean isEquivalent(Object obj) {

            CCField other = (CCField)obj;

            if (other == null)
                return false;

            return field.getName().equals(other.field.getName());
        }

        public int hashCode() {
            return field.getName().hashCode();
        }

        public String toString() {
            return field.toString();
        }
    }

    /**
     * Wrapper for a java.reflect.Method, hiding differences
     * in ClassLoader.
     */
    private static class CCMethod implements CCComparable {

        public Method method;

        private static final int MODIFIER_MASK = (Modifier.PUBLIC &
                                                  Modifier.PROTECTED &
                                                  Modifier.STATIC);

        public CCMethod(Method method) {
            this.method = method;
        }

        /**
         * Checks for 
         *
         * 1. Name
         * 2. Parameter types (in certain order)
         */
        public boolean isEquivalent(Object obj) {
            CCMethod other = (CCMethod)obj;

            if (other == null)
                return false;

            return (method.getName().equals(other.method.getName()) &&
                    classNamesCheck(method.getParameterTypes(),
                                    other.method.getParameterTypes()));
        }

        /**
         * Checks for
         *
         * 1. Equivalency
         * 2. Return type class names
         * 3. Exception types (in certain order)
         * 4. Differences in modifiers {public, protected, static}
         */
        public boolean equals(Object obj) {

            CCMethod other = (CCMethod)obj;

            if (other == null)
                return false;

            if (this == obj)
                return true;

            return (isEquivalent(other) &&
                    method.getReturnType().getName().equals(other.method.getReturnType().getName()) &&
                    classNamesCheck(method.getExceptionTypes(),
                                    other.method.getExceptionTypes()) &&
                    getRealModifiers() == other.getRealModifiers());
        }

        public int hashCode() {
            return method.getName().hashCode();
        }

        public String toString() {
            return method.toString();
        }

        private int getRealModifiers() {
            return method.getModifiers() & MODIFIER_MASK;
        }

        /**
         * Makes sure arrays are of equal length, and all class names
         * are the same.  Will return false for any order mismatch.
         */
        private boolean classNamesCheck(Class[] classes1, Class[] classes2) {
            if (classes1.length != classes2.length)
                return false;
            
            for (int i = 0; i < classes1.length; i++)
                if (!classes1[i].getName().equals(classes2[i].getName()))
                    return false;
            
            return true;
        }
    }

    /**
     * Wrapper for a java.lang.Class, hiding differences
     * in ClassLoader.
     */
    private static class CCClass implements CCComparable {
        public String desc;
        public Class cl;
        public Set methods;
        public Set fields;
        public Set classes;
        public Set interfaceNames;

        public CCClass(String desc, Class cl) {
            this.desc = desc;
            this.cl = cl;
            this.methods = getMethods(cl);
            this.fields = getFields(cl);
            this.classes = getCCClasses(desc, cl.getDeclaredClasses());
            this.interfaceNames = getInterfaceNames(cl);
        }

        public String toString() {
            return cl.getName();
        }

        /**
         * Checks for
         *
         * 1. Name
         */
        public boolean isEquivalent(Object obj) {
            CCClass other = (CCClass)obj;

            if (other == null)
                return false;

            return cl.getName().equals(other.cl.getName());
        }

        /**
         * Checks for
         *
         * 1. Equivalency
         * 2. Superclass name
         * 3. Full non-ordered set comparison of Methods (CCMethod equals)
         * 4. Full non-ordered set comparison of Fields (CCField equals)
         * 5. Full non-ordered set comparison of Classes (CCClass equals)
         * 6. Full non-ordered set comparison of interface class names
         */
        public boolean equals(Object obj) {
            CCClass other = (CCClass)obj;

            if (other == null)
                return false;

            if (this == other)
                return true;

            return (isEquivalent(other) &&
                    methods.equals(other.methods) &&
                    fields.equals(other.fields) &&
                    classes.equals(other.classes) &&
                    ((cl.getSuperclass() == null &&
                      other.cl.getSuperclass() == null) ||
                     (cl.getSuperclass() != null &&
                      other.cl.getSuperclass() != null &&
                      cl.getSuperclass().getName().equals(other.cl.getSuperclass().getName()))) &&
                    interfaceNames.equals(other.interfaceNames));
        }

        // Necessary for use in HashSet
        public int hashCode() {
            return cl.getName().hashCode();
        }
        
        /**
         * Filter declared methods to get public and private method
         * Set of CCMethods.
         */
        private Set getMethods(Class cl) {
            Method[] declMethods = cl.getDeclaredMethods();
            
            Set result = new HashSet(declMethods.length);
            for (int i = 0; i < declMethods.length; i++) {
                if (!Modifier.isPublic(declMethods[i].getModifiers()) &&
                    !Modifier.isProtected(declMethods[i].getModifiers()))
                    continue;

                result.add(new CCMethod(declMethods[i]));
            }

            return result;
        }

        private Set getFields(Class cl) {
            Field[] declFields = cl.getDeclaredFields();

            Set result = new HashSet(declFields.length);
            for (int i = 0; i < declFields.length; i++) {
                if (!Modifier.isPublic(declFields[i].getModifiers()) &&
                    !Modifier.isProtected(declFields[i].getModifiers()))
                    continue;

                result.add(new CCField(declFields[i]));
            }

            return result;
        }

        private Set getCCClasses(String newDesc, Class[] input) {
            Set result = new HashSet(input.length);

            for (int i = 0; i < input.length; i++) {
                if (!Modifier.isPublic(input[i].getModifiers()) &&
                    !Modifier.isProtected(input[i].getModifiers()))
                    continue;

                result.add(new CCClass(newDesc, input[i]));
            }

            return result;
        }

        private Set getInterfaceNames(Class cl) {
            Class[] interfaces = cl.getInterfaces();

            Set result = new HashSet(interfaces.length);

            for (int i = 0; i < interfaces.length; i++)
                result.add(interfaces[i].getName());

            return result;
        }

        /**
         * Deep comparison resulting in formatted string.  Probably
         * should have formatting knowledge separated out of CCClass.
         */
        public String compare(CCClass other) {
            StringBuffer sbuf = new StringBuffer();

            CCClass.compareCCComparables(this, 
                                         methods, 
                                         other, 
                                         other.methods, 
                                         sbuf);

            CCClass.compareCCComparables(this, 
                                         fields, 
                                         other, 
                                         other.fields, 
                                         sbuf);

            CCClass.compareCCComparables(this,
                                         classes,
                                         other,
                                         other.classes,
                                         sbuf);

            compareInterfaces(other, sbuf);

            if (sbuf.length() > 0)
                return sbuf.toString();
            else
                return null;
        }

        /**
         * Returns the set that is A - B (giving you all
         * elements in A not in B).
         */
        private static Set setDifference(Set a, Set b) {

            HashSet result = new HashSet(a);

            result.removeAll(b);

            return result;
        }

        /**
         * Provides human friendly differences in interfaces.
         */
        private void compareInterfaces(CCClass other, StringBuffer sbuf) {

            Set notInSet1 = CCClass.setDifference(other.interfaceNames,
                                                  this.interfaceNames);

            Set notInSet2 = CCClass.setDifference(this.interfaceNames,
                                                  other.interfaceNames);

            reportLackingInterfaces(this.desc, notInSet1, sbuf);
            reportLackingInterfaces(other.desc, notInSet2, sbuf);
        }

        private void reportLackingInterfaces(String desc, Set set, StringBuffer sbuf) {
            if (set.size() > 0) {
                sbuf.append(desc);
                sbuf.append(" does not implement ");

                Iterator names = set.iterator();

                while (names.hasNext()) {
                    sbuf.append(names.next());
                    sbuf.append(' ');
                }
                
                sbuf.append(LINE_SEPARATOR);
            }
        }

        /** 
         * Used to diff Sets of CCMethods, CCFields, and CCClasses.
         */
        public static void compareCCComparables(CCClass c1, 
                                                Set ccSet1,
                                                CCClass c2,
                                                Set ccSet2,
                                                StringBuffer sbuf) {

            // Tries to find an equivalent in ccSet2 for each
            // element in ccSet1
            Iterator set1Iter = ccSet1.iterator();

            while (set1Iter.hasNext()) {

                CCComparable c1Value = (CCComparable)set1Iter.next();

                CCComparable c2Equiv = ClassComparer.findEquivalent(c1Value, ccSet2);

                // If there was no equivalent, note that c1 has this
                // element but c2 does not.
                if (c2Equiv == null) {
                    sbuf.append(c1.desc);
                    sbuf.append(": ");
                    sbuf.append(c1Value.toString());
                    sbuf.append(LINE_SEPARATOR);
                } else
                if (!c1Value.equals(c2Equiv)) {

                    // If there was an equivalent, but it isn't equal to
                    // the one in c1, note the difference.

                    sbuf.append(LINE_SEPARATOR);
                    sbuf.append(c1.desc);
                    sbuf.append(": ");
                    sbuf.append(c1Value.toString());

                    sbuf.append(LINE_SEPARATOR);
                    sbuf.append(c2.desc);
                    sbuf.append(": ");
                    sbuf.append(c2Equiv.toString());
                    sbuf.append(LINE_SEPARATOR);
                    sbuf.append(LINE_SEPARATOR);
                }
            }

            // Now look through c2 to see if there are things it has
            // that c1 does not.
            Iterator set2Iter = ccSet2.iterator();

            while (set2Iter.hasNext()) {

                CCComparable c2Value = (CCComparable)set2Iter.next();

                CCComparable c1Equiv = ClassComparer.findEquivalent(c2Value,
                                                                    ccSet1);

                // Found something that c1 doesn't have.
                if (c1Equiv == null) {
                    sbuf.append(c2.desc);
                    sbuf.append(": ");
                    sbuf.append(c2Value.toString());
                    sbuf.append(LINE_SEPARATOR);
                }
            }
        }
    }

    public static void compare(CCClass cl1, CCClass cl2) {
        String diffs = cl1.compare(cl2);
        if (diffs != null) {
            putHeader(cl1.cl.getName());
            System.out.println(diffs);
        }
    }

    public static void putHeader(String className) {
        System.out.println("-------------------------------------------");
        System.out.println(className);
        System.out.println();
    }

    public static void compareClasses(String classpath1,
                                      String classpath2,
                                      String[] classes)
        throws ClassNotFoundException {

        PrivateLoader cl1 = new PrivateLoader(classpath1);
        PrivateLoader cl2 = new PrivateLoader(classpath2);

        for (int i = 0; i < classes.length; i++) {

            CCClass c1 = new CCClass("cp1", cl1.loadClass(classes[i]));
            CCClass c2 = new CCClass("cp2", cl2.loadClass(classes[i]));

            compare(c1, c2);
        }
    }

    /**
     * Used to find an equivalent to value in the given Set.
     * Returns null on failure.
     */
    public static CCComparable findEquivalent(CCComparable value,
                                              Set set) {
        Iterator iter = set.iterator();
        
        while (iter.hasNext()) {
            CCComparable testValue = (CCComparable)iter.next();
            
            if (value.isEquivalent(testValue))
                return value;
        }
        
        return null;
    }
    
    /**
     * Arguments:
     *
     * 1. Class Path 1
     * 2. Class Path 2
     * 3. List of fully qualified class names to compare that are
     * common to both.
     *
     */
    public static void main(String[] args) {
        try {
            if (args.length < 3) {
                System.out.println("ClassComparer <classpath1> <classpath2> class1...classN");
                System.exit(1);
            }

            String[] classes = new String[args.length - 2];
            System.arraycopy(args, 2, classes, 0, classes.length);

            compareClasses(args[0], args[1], classes);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static class PrivateLoader extends ClassLoader
    {
        private ClassPath classPath;

        public PrivateLoader(ClassPath classPath) {
            this.classPath = classPath;
        }

        public PrivateLoader(String classPathString) {
            this.classPath = new ClassPath(classPathString);
        }

        // Called by the super class 
        protected Class findClass(String name) throws ClassNotFoundException 
        {
            byte[] b = loadClassData(name);
            return defineClass(name, b, 0, b.length);
        }

        protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException
        {
            // First, check if the class has already been loaded
            Class c = findLoadedClass(name);
            if (c == null) {

                if (name.startsWith("java.")) {
                    c = Thread.currentThread().getContextClassLoader().loadClass(name);
                } else {
                    c = findClass(name);
                }
            }

            if (resolve)
                resolveClass(c);

            return c;
        }

        /**
         * Load the class with the given fully qualified name from the ClassPath.
         */
        private byte[] loadClassData(String className) 
            throws ClassNotFoundException
        {
            // Build the file name and subdirectory from the
            // class name
            String filename = className.replace('.', File.separatorChar) 
                + ".class";

            // Have ClassPath find the file for us, and wrap it in a 
            // ClassFile.  Note:  This is where it looks inside jar files that
            // are specified in the path.
            ClassFile classFile = classPath.getFile(filename);
 
            if (classFile != null) {

                // Provide the most specific reason for failure in addition
                // to ClassNotFound
                Exception reportedError = null;
                byte data[] = null;

                try {
                    // ClassFile is beautiful because it shields us from
                    // knowing if it's a separate file or an entry in a
                    // jar file.
                    DataInputStream input 
                        = new DataInputStream(classFile.getInputStream());

                    // Can't rely on input available() since it will be 
                    // something unusual if it's a jar file!  May need
                    // to worry about a possible problem if someone
                    // makes a jar file entry with a size greater than
                    // max int.
                    data = new byte[(int)classFile.length()];
                
                    try {
                        input.readFully(data);
                    } catch (IOException ex) {
                        // Something actually went wrong reading the file.  This
                        // is a real error so save it to report it.
                        data = null;
                        reportedError = ex;
                    } finally {
                        try { input.close(); } catch (IOException ex) {}
                    }
                } catch (IOException ex) {
                    // Couldn't get the input stream for the file.  This is
                    // probably also a real error.
                    reportedError = ex;
                }

                if (data == null)
                    throw new ClassNotFoundException(className, reportedError);

                return data;
            }

            // Couldn't find the file in the class path.
            throw new ClassNotFoundException(className + " in " + classPath);
        }
    }
}
