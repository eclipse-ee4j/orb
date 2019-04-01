/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import java.util.StringTokenizer;

import java.lang.reflect.Array;

import java.net.URL;
import java.net.MalformedURLException;
import org.glassfish.pfl.basic.algorithm.ObjectUtility;
import org.glassfish.pfl.basic.contain.Pair;
import org.glassfish.pfl.basic.func.UnaryFunction;

/**
 * This is a static factory class for commonly used operations for property parsing. The following operations are
 * supported:
 * <ul>
 * <li>maskErrorAction( Operation op ) executes op and returns the result. If op throws an exception, the result is
 * null.
 * <li>indexAction( int arg ) returns the [arg] element of value, which must be an Object[]</li>
 * <li>identityAction() return the value</li>
 * <li>booleanAction() return a Boolean representing true or false values of the String value</li>
 * <li>integerAction() returns an Integer for the String value, which must be a decimal integer</li>
 * <li>stringAction() returns the String value</li>
 * <li>classAction() returns a class for the String value, as loaded by the ORB classloader</li>
 * <li>setFlagAction() always return Boolean.TRUE</li>
 * <li>URLAction() returns a java.net.URL for the String value, which must be a valid URL</li>
 * <li>integerRangeAction( int min, int max ) returns an Integer for the String value, which must be a decimal integer
 * in the range min to max inclusive</li>
 * <li>listAction( String sep, Operation ) tokenizes the String value with sep as separator, then applies the Operation
 * to each token, and returns an array of the result</li>
 * <li>sequenceAction( String, Operation[] ) tokenizes the String value with sep as separator, then applies each
 * Operation in the Operation array to successive tokens, and returns an array of the results</li>
 * <li>compose( Operation op1, Operation op2 ) is the operation that applies op2 to the result of applying op1 to the
 * value</li>
 * <li>mapAction( Operation ) applies the Operation to each element of an array of objects, and returns an array of the
 * results</li>
 * <li>mapSequenceAction( Operation[] ) applies the corresponding element of the Operation array to an element of the
 * Object[] value, and returns an array of the results</li>
 * <li>convertIntegerToShort coerces an Integer into a Short.</li>
 * </ul>
 * Other operations can be directly defined, and combined using these basic operations.
 * <p>
 * This class also provides a simple ArgumentParser that can be used to parse simple String[] data using the Operations
 * framework.
 */
public abstract class OperationFactory {
    // I18N for errors through log wrappers: bug 5051545
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private OperationFactory() {
    }

    private static String getString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        } else {
            throw wrapper.stringExpectedInOperation();
        }
    }

    private static Object[] getObjectArray(Object obj) {
        if (obj instanceof Object[]) {
            return (Object[]) obj;
        } else {
            throw wrapper.objectArrayExpected();
        }
    }

    private static Pair<String, String> getStringPair(Object obj) {
        if (obj instanceof Pair) {
            return (Pair<String, String>) obj;
        } else {
            throw wrapper.pairStringStringExpected();
        }
    }

    private static abstract class OperationBase implements Operation {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof OperationBase)) {
                return false;
            }

            OperationBase other = (OperationBase) obj;

            return toString().equals(other.toString());
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }
    }

    private static class MaskErrorAction extends OperationBase {
        private Operation op;

        public MaskErrorAction(Operation op) {
            this.op = op;
        }

        public Object operate(Object arg) {
            try {
                return op.operate(arg);
            } catch (java.lang.Exception exc) {
                return null;
            }
        }

        @Override
        public String toString() {
            return "maskErrorAction(" + op + ")";
        }
    }

    public static Operation maskErrorAction(Operation op) {
        return new MaskErrorAction(op);
    }

    private static class IndexAction extends OperationBase {
        private int index;

        public IndexAction(int index) {
            this.index = index;
        }

        public Object operate(Object value) {
            return getObjectArray(value)[index];
        }

        @Override
        public String toString() {
            return "indexAction(" + index + ")";
        }
    }

    public static Operation indexAction(int index) {
        return new IndexAction(index);
    }

    private static class SuffixAction extends OperationBase {
        public Object operate(Object value) {
            return getStringPair(value).first();
        }

        @Override
        public String toString() {
            return "suffixAction";
        }
    }

    private static Operation suffixActionImpl = new SuffixAction();

    private static class ValueAction extends OperationBase {
        public Object operate(Object value) {
            return getStringPair(value).second();
        }

        @Override
        public String toString() {
            return "valueAction";
        }
    }

    private static Operation valueActionImpl = new ValueAction();

    private static class IdentityAction extends OperationBase {
        public Object operate(Object value) {
            return value;
        }

        @Override
        public String toString() {
            return "identityAction";
        }
    }

    private static Operation identityActionImpl = new IdentityAction();

    private static class BooleanAction extends OperationBase {
        public Object operate(Object value) {
            return Boolean.valueOf(getString(value));
        }

        @Override
        public String toString() {
            return "booleanAction";
        }
    }

    private static Operation booleanActionImpl = new BooleanAction();

    private static class IntegerAction extends OperationBase {
        public Object operate(Object value) {
            return Integer.valueOf(getString(value));
        }

        @Override
        public String toString() {
            return "integerAction";
        }
    }

    private static Operation integerActionImpl = new IntegerAction();

    private static class StringAction extends OperationBase {
        public Object operate(Object value) {
            return value;
        }

        @Override
        public String toString() {
            return "stringAction";
        }
    }

    private static Operation stringActionImpl = new StringAction();

    private static class ClassAction extends OperationBase {
        private UnaryFunction<String, Class<?>> resolver;

        public ClassAction(UnaryFunction<String, Class<?>> resolver) {
            this.resolver = resolver;
        }

        public Object operate(Object value) {
            String className = getString(value);

            try {
                Class<?> result = resolver.evaluate(className);
                return result;
            } catch (Exception exc) {
                throw wrapper.classActionException(exc, className);
            }
        }

        @Override
        public String toString() {
            return "classAction[" + resolver + "]";
        }
    }

    private static class SetFlagAction extends OperationBase {
        public Object operate(Object value) {
            return Boolean.TRUE;
        }

        @Override
        public String toString() {
            return "setFlagAction";
        }
    }

    private static Operation setFlagActionImpl = new SetFlagAction();

    private static class URLAction extends OperationBase {
        public Object operate(Object value) {
            String val = (String) value;
            try {
                return new URL(val);
            } catch (MalformedURLException exc) {
                throw wrapper.badUrlInAction(exc, val);
            }
        }

        @Override
        public String toString() {
            return "URLAction";
        }
    }

    private static Operation URLActionImpl = new URLAction();

    public static Operation identityAction() {
        return identityActionImpl;
    }

    public static Operation suffixAction() {
        return suffixActionImpl;
    }

    public static Operation valueAction() {
        return valueActionImpl;
    }

    public static Operation booleanAction() {
        return booleanActionImpl;
    }

    public static Operation integerAction() {
        return integerActionImpl;
    }

    public static Operation stringAction() {
        return stringActionImpl;
    }

    public static Operation classAction(final UnaryFunction<String, Class<?>> resolver) {
        return new ClassAction(resolver);
    }

    public static Operation setFlagAction() {
        return setFlagActionImpl;
    }

    public static Operation URLAction() {
        return URLActionImpl;
    }

    private static class IntegerRangeAction extends OperationBase {
        private int min;
        private int max;

        IntegerRangeAction(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public Object operate(Object value) {
            int result = Integer.parseInt(getString(value));
            if ((result >= min) && (result <= max)) {
                return Integer.valueOf(result);
            } else {
                throw wrapper.valueNotInRange(result, min, max);
            }
        }

        @Override
        public String toString() {
            return "integerRangeAction(" + min + "," + max + ")";
        }
    }

    public static Operation integerRangeAction(int min, int max) {
        return new IntegerRangeAction(min, max);
    }

    private static class ListAction extends OperationBase {
        private String sep;
        private Operation act;

        ListAction(String sep, Operation act) {
            this.sep = sep;
            this.act = act;
        }

        // Note that this method carefully constructs an array of the type
        // of the first result, rather than just using Object[], which is
        // not convertible into the correct type. Also note that no tokens
        // results in a null result.
        public Object operate(Object value) {
            StringTokenizer st = new StringTokenizer(getString(value), sep);
            int length = st.countTokens();
            Object result = null;
            int ctr = 0;
            while (st.hasMoreTokens()) {
                String next = st.nextToken();
                Object val = act.operate(next);
                if (result == null) {
                    result = Array.newInstance(val.getClass(), length);
                }
                Array.set(result, ctr++, val);
            }

            return result;
        }

        @Override
        public String toString() {
            return "listAction(separator=\"" + sep + "\",action=" + act + ")";
        }
    }

    public static Operation listAction(String sep, Operation act) {
        return new ListAction(sep, act);
    }

    private static class SequenceAction extends OperationBase {
        private String sep;
        private Operation[] actions;

        SequenceAction(String sep, Operation[] actions) {
            this.sep = sep;
            this.actions = actions;
        }

        public Object operate(Object value) {
            StringTokenizer st = new StringTokenizer(getString(value), sep);

            int numTokens = st.countTokens();
            if (numTokens != actions.length) {
                throw wrapper.numTokensActionsDontMatch(numTokens, actions.length);
            }

            int ctr = 0;
            Object[] result = new Object[numTokens];
            while (st.hasMoreTokens()) {
                Operation act = actions[ctr];
                String next = st.nextToken();
                result[ctr++] = act.operate(next);
            }

            return result;
        }

        @Override
        public String toString() {
            return "sequenceAction(separator=\"" + sep + "\",actions=" + ObjectUtility.compactObjectToString(actions) + ")";
        }
    }

    public static Operation sequenceAction(String sep, Operation[] actions) {
        return new SequenceAction(sep, actions);
    }

    private static class ComposeAction extends OperationBase {
        private Operation op1;
        private Operation op2;

        ComposeAction(Operation op1, Operation op2) {
            this.op1 = op1;
            this.op2 = op2;
        }

        public Object operate(Object value) {
            return op2.operate(op1.operate(value));
        }

        @Override
        public String toString() {
            return "composition(" + op1 + "," + op2 + ")";
        }
    }

    public static Operation compose(Operation op1, Operation op2) {
        return new ComposeAction(op1, op2);
    }

    private static class MapAction extends OperationBase {
        private Operation op;

        MapAction(Operation op) {
            this.op = op;
        }

        public Object operate(Object value) {
            Object[] values = (Object[]) value;
            Object[] result = new Object[values.length];
            for (int ctr = 0; ctr < values.length; ctr++) {
                result[ctr] = op.operate(values[ctr]);
            }
            return result;
        }

        @Override
        public String toString() {
            return "mapAction(" + op + ")";
        }
    }

    public static Operation mapAction(Operation op) {
        return new MapAction(op);
    }

    private static class MapSequenceAction extends OperationBase {
        private Operation[] op;

        public MapSequenceAction(Operation[] op) {
            this.op = op;
        }

        public Object operate(Object value) {
            Object[] values = (Object[]) value;
            Object[] result = new Object[values.length];
            for (int ctr = 0; ctr < values.length; ctr++) {
                result[ctr] = op[ctr].operate(values[ctr]);
            }
            return result;
        }

        @Override
        public String toString() {
            return "mapSequenceAction(" + ObjectUtility.compactObjectToString(op) + ")";
        }
    }

    public static Operation mapSequenceAction(Operation[] op) {
        return new MapSequenceAction(op);
    }

    private static class ConvertIntegerToShort extends OperationBase {
        public Object operate(Object value) {
            Integer val = (Integer) value;
            return Short.valueOf(val.shortValue());
        }

        public String toString() {
            return "ConvertIntegerToShort";
        }
    }

    private static Operation convertIntegerToShortImpl = new ConvertIntegerToShort();

    public static Operation convertIntegerToShort() {
        return convertIntegerToShortImpl;
    }
}
