/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.presentation.rmi;

import java.io.Serializable;
import java.io.Externalizable;

import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA.portable.ApplicationException;

import java.lang.reflect.Method;

import java.rmi.RemoteException;

import com.sun.corba.ee.spi.presentation.rmi.DynamicMethodMarshaller;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

import com.sun.corba.ee.impl.misc.ClassInfoCache;
import org.glassfish.pfl.basic.logex.OperationTracer;

public class DynamicMethodMarshallerImpl implements DynamicMethodMarshaller {
    private Method method;
    private ExceptionHandler ehandler;
    private boolean hasArguments = true;
    private boolean hasVoidResult = true;
    private boolean needsArgumentCopy; // true if copyObjects call needs for args
    private boolean needsResultCopy; // true if copyObject call needs for result
    private ReaderWriter[] argRWs = null;
    private ReaderWriter resultRW = null;

    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private static boolean isAnyClass(Class cls) {
        return cls.equals(Object.class) || cls.equals(Serializable.class) || cls.equals(Externalizable.class);
    }

    // Assume that cls is not Remote, !isAnyClass(cls), and
    // !org.omg.CORBA.Object.class.isAssignableFrom( cls ).
    // Then return whether cls is an RMI-IIOP abstract interface.
    private static boolean isAbstractInterface(Class cls) {
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get(cls);
        // Either cls is an interface that extends IDLEntity, or else
        // cls does not extend java.rmi.Remote and all of its methods
        // throw RemoteException.
        if (cinfo.isAIDLEntity(cls)) {
            return cinfo.isInterface();
        } else {
            return cinfo.isInterface() && allMethodsThrowRemoteException(cls);
        }
    }

    private static boolean allMethodsThrowRemoteException(Class cls) {
        Method[] methods = cls.getMethods();

        // Check that all methods (other than those declared in java.lang.Object)
        // throw an exception that is a subclass of RemoteException.
        for (int ctr = 0; ctr < methods.length; ctr++) {
            Method method = methods[ctr];
            if (method.getDeclaringClass() != Object.class) {
                if (!throwsRemote(method)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean throwsRemote(Method method) {
        Class[] exceptionTypes = method.getExceptionTypes();

        // Check that some exceptionType is a subclass of RemoteException
        for (int ctr = 0; ctr < exceptionTypes.length; ctr++) {
            Class exceptionType = exceptionTypes[ctr];
            if (ClassInfoCache.get(exceptionType).isARemoteException(exceptionType)) {
                return true;
            }
        }

        return false;
    }

    public interface ReaderWriter {
        Object read(InputStream is);

        void write(OutputStream os, Object value);
    }

    abstract static class ReaderWriterBase implements ReaderWriter {
        private String name;

        public ReaderWriterBase(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "ReaderWriter[" + name + "]";
        }
    }

    private static ReaderWriter booleanRW = new ReaderWriterBase("boolean") {
        public Object read(InputStream is) {
            boolean value = is.read_boolean();
            return Boolean.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Boolean val = (Boolean) value;
            os.write_boolean(val.booleanValue());
        }
    };

    private static ReaderWriter byteRW = new ReaderWriterBase("byte") {
        public Object read(InputStream is) {
            byte value = is.read_octet();
            return Byte.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Byte val = (Byte) value;
            os.write_octet(val.byteValue());
        }
    };

    private static ReaderWriter charRW = new ReaderWriterBase("char") {
        public Object read(InputStream is) {
            char value = is.read_wchar();
            return Character.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Character val = (Character) value;
            os.write_wchar(val.charValue());
        }
    };

    private static ReaderWriter shortRW = new ReaderWriterBase("short") {
        public Object read(InputStream is) {
            short value = is.read_short();
            return Short.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Short val = (Short) value;
            os.write_short(val.shortValue());
        }
    };

    private static ReaderWriter intRW = new ReaderWriterBase("int") {
        public Object read(InputStream is) {
            int value = is.read_long();
            return Integer.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Integer val = (Integer) value;
            os.write_long(val.intValue());
        }
    };

    private static ReaderWriter longRW = new ReaderWriterBase("long") {
        public Object read(InputStream is) {
            long value = is.read_longlong();
            return Long.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Long val = (Long) value;
            os.write_longlong(val.longValue());
        }
    };

    private static ReaderWriter floatRW = new ReaderWriterBase("float") {
        public Object read(InputStream is) {
            float value = is.read_float();
            return Float.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Float val = (Float) value;
            os.write_float(val.floatValue());
        }
    };

    private static ReaderWriter doubleRW = new ReaderWriterBase("double") {
        public Object read(InputStream is) {
            double value = is.read_double();
            return Double.valueOf(value);
        }

        public void write(OutputStream os, Object value) {
            Double val = (Double) value;
            os.write_double(val.doubleValue());
        }
    };

    private static ReaderWriter corbaObjectRW = new ReaderWriterBase("org.omg.CORBA.Object") {
        public Object read(InputStream is) {
            return is.read_Object();
        }

        public void write(OutputStream os, Object value) {
            os.write_Object((org.omg.CORBA.Object) value);
        }
    };

    private static ReaderWriter anyRW = new ReaderWriterBase("any") {
        public Object read(InputStream is) {
            try {
                return Util.getInstance().readAny(is);
            } finally {
                OperationTracer.finish();
            }
        }

        public void write(OutputStream os, Object value) {
            Util.getInstance().writeAny(os, value);
        }
    };

    private static ReaderWriter abstractInterfaceRW = new ReaderWriterBase("abstract_interface") {
        public Object read(InputStream is) {
            try {
                return is.read_abstract_interface();
            } finally {
                OperationTracer.finish();
            }
        }

        public void write(OutputStream os, Object value) {
            Util.getInstance().writeAbstractObject(os, value);
        }
    };

    public static ReaderWriter makeReaderWriter(final Class cls) {
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get(cls);
        if (cls.equals(boolean.class)) {
            return booleanRW;
        } else if (cls.equals(byte.class)) {
            return byteRW;
        } else if (cls.equals(char.class)) {
            return charRW;
        } else if (cls.equals(short.class)) {
            return shortRW;
        } else if (cls.equals(int.class)) {
            return intRW;
        } else if (cls.equals(long.class)) {
            return longRW;
        } else if (cls.equals(float.class)) {
            return floatRW;
        } else if (cls.equals(double.class)) {
            return doubleRW;
        } else if (cinfo.isARemote(cls)) {
            return new ReaderWriterBase("remote(" + cls.getName() + ")") {

                public Object read(InputStream is) {
                    return PortableRemoteObject.narrow(is.read_Object(), cls);
                }

                public void write(OutputStream os, Object value) {
                    Util.getInstance().writeRemoteObject(os, value);
                }
            };
        } else if (cls.equals(org.omg.CORBA.Object.class)) {
            return corbaObjectRW;
        } else if (cinfo.isACORBAObject(cls)) {
            return new ReaderWriterBase("org.omg.CORBA.Object(" + cls.getName() + ")") {

                public Object read(InputStream is) {
                    return is.read_Object(cls);
                }

                public void write(OutputStream os, Object value) {
                    os.write_Object((org.omg.CORBA.Object) value);
                }
            };
        } else if (isAnyClass(cls)) {
            return anyRW;
        } else if (isAbstractInterface(cls)) {
            return abstractInterfaceRW;
        }

        // For anything else, just read it as a value type.
        return new ReaderWriterBase("value(" + cls.getName() + ")") {
            public Object read(InputStream is) {
                try {
                    return is.read_value(cls);
                } finally {
                    OperationTracer.finish();
                }
            }

            public void write(OutputStream os, Object value) {
                if (value == null) {
                    os.write_value(null, cls);
                } else if (value instanceof Serializable) {
                    os.write_value((Serializable) value, cls);
                } else {
                    wrapper.objectNotSerializable(value.getClass().getName());
                }
            }
        };
    }

    public DynamicMethodMarshallerImpl(Method method) {
        this.method = method;
        ehandler = new ExceptionHandlerImpl(method.getExceptionTypes());
        needsArgumentCopy = false;

        Class[] argTypes = method.getParameterTypes();
        hasArguments = argTypes.length > 0;
        if (hasArguments) {
            argRWs = new ReaderWriter[argTypes.length];
            for (int ctr = 0; ctr < argTypes.length; ctr++) {
                // This could be further optimized to avoid
                // copying if argTypes contains at most one
                // immutable object type.
                if (!argTypes[ctr].isPrimitive()) {
                    needsArgumentCopy = true;
                }
                argRWs[ctr] = makeReaderWriter(argTypes[ctr]);
            }
        }

        Class resultType = method.getReturnType();
        needsResultCopy = false;
        hasVoidResult = resultType.equals(void.class);
        if (!hasVoidResult) {
            needsResultCopy = !resultType.isPrimitive();
            resultRW = makeReaderWriter(resultType);
        }
    }

    public Method getMethod() {
        return method;
    }

    public Object[] copyArguments(Object[] args, ORB orb) throws RemoteException {
        if (needsArgumentCopy) {
            return Util.getInstance().copyObjects(args, orb);
        } else {
            return args;
        }
    }

    public Object[] readArguments(InputStream is) {
        Object[] result = null;

        if (hasArguments) {
            result = new Object[argRWs.length];
            for (int ctr = 0; ctr < argRWs.length; ctr++) {
                result[ctr] = argRWs[ctr].read(is);
            }
        }

        return result;
    }

    public void writeArguments(OutputStream os, Object[] args) {
        if (hasArguments) {
            if (args.length != argRWs.length) {
                throw new IllegalArgumentException("Expected " + argRWs.length + " arguments, but got " + args.length + " arguments.");
            }

            for (int ctr = 0; ctr < argRWs.length; ctr++) {
                argRWs[ctr].write(os, args[ctr]);
            }
        }
    }

    public Object copyResult(Object result, ORB orb) throws RemoteException {
        if (needsResultCopy) {
            return Util.getInstance().copyObject(result, orb);
        } else {
            return result;
        }
    }

    public Object readResult(InputStream is) {
        if (hasVoidResult) {
            return null;
        } else {
            return resultRW.read(is);
        }
    }

    public void writeResult(OutputStream os, Object result) {
        if (!hasVoidResult) {
            resultRW.write(os, result);
        }
    }

    public boolean isDeclaredException(Throwable thr) {
        return ehandler.isDeclaredException(thr.getClass());
    }

    public void writeException(OutputStream os, Exception ex) {
        ehandler.writeException(os, ex);
    }

    public Exception readException(ApplicationException ae) {
        return ehandler.readException(ae);
    }
}
