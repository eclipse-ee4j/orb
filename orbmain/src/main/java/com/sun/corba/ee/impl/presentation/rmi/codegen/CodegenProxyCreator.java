/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package com.sun.corba.ee.impl.presentation.rmi.codegen;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.glassfish.pfl.basic.contain.Pair;
import org.glassfish.pfl.dynamic.codegen.spi.Expression;
import org.glassfish.pfl.dynamic.codegen.spi.MethodInfo;
import org.glassfish.pfl.dynamic.codegen.spi.Primitives;
import org.glassfish.pfl.dynamic.codegen.spi.Type;
import org.glassfish.pfl.dynamic.codegen.spi.Utility;
import org.glassfish.pfl.dynamic.codegen.spi.Variable;

import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.DUMP_AFTER_SETUP_VISITOR;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.TRACE_BYTE_CODE_GENERATION;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.USE_ASM_VERIFIER;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._Object;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._arg;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._body;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._call;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._cast;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._class;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._clear;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._const;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._constructor;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._define;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._end;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._expr;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._generate;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._method;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._new_array_init;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._package;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._return;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._setClassLoader;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._super;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._this;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper._void;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.splitClassName;

/** Generate a proxy with a specified base class.
 */
public class CodegenProxyCreator {
    private final String className ;
    private final Type superClass ;
    private final List<Type> interfaces ;
    private final List<MethodInfo> methods ;

    private static final Properties debugProps = new Properties() ;
    private static final Properties emptyProps = new Properties() ;

    static {
        debugProps.setProperty( DUMP_AFTER_SETUP_VISITOR, "true" ) ;
        debugProps.setProperty( TRACE_BYTE_CODE_GENERATION, "true" ) ;
        debugProps.setProperty( USE_ASM_VERIFIER, "true" ) ;
    }

    public CodegenProxyCreator( String className, Class sc,
        Class[] interfaces, Method[] methods ) {

        this.className = className ;
        this.superClass = Type.type( sc ) ;

        this.interfaces = new ArrayList<>() ;
        for (Class cls : interfaces) {
            this.interfaces.add( Type.type( cls ) ) ;
        }

        this.methods = new ArrayList<>() ;
        for (Method method : methods) {
            this.methods.add( Utility.getMethodInfo( method ) ) ;
        }
    }

    /**
     * Construct a generator for a proxy class
     * that implements the given interfaces and extends superClass.
     * superClass must satisfy the following requirements:
     * <ol>
     * <li>It must have an accessible no args constructor</li>
     * <li>It must have a method satisfying the signature
     * <code>    Object invoke( int methodNumber, Object[] args ) throws Throwable
     * </code>
     * </li>
     * <li>The invoke method described above must be accessible
     * to the generated class (generally either public or
     * protected.</li>
     * </ol>
     * <p>
     * Each method in methods is implemented by a method that:
     * <ol>
     * <li>Creates an array sized to hold the args</li>
     * <li>Wraps args of primitive type in the appropriate wrapper.</li>
     * <li>Copies each arg or wrapper arg into the array.</li>
     * <li>Calls invoke with a method number corresponding to the
     * index of the method in methods.  Note that the invoke implementation
     * must use the same method array to figure out which method has been
     * invoked.</li>
     * <li>Return the result (if any), extracting values from wrappers
     * as needed to handle a return value of a primitive type.</li>
     * </ol>
     * <p>
     * Note that the generated methods ignore exceptions.
     * It is assumed that the invoke method may throw any
     * desired exception.
     * @param anchorClass a class in whose classloader the new class should be generated
     * @param debug if true, generate debug messages
     * @param ps a PrintStream to which the debug messages should be written
     * @return Generator for class
     * @since 4.2.1
     */
    public Class<?> create( Class<?> anchorClass, boolean debug, PrintStream ps ) {

        Pair<String,String> nm = splitClassName( className ) ;

        _clear() ;
        _setClassLoader( anchorClass.getClassLoader() ) ;
        _package( nm.first() ) ;
        _class( PUBLIC, nm.second(), superClass, interfaces ) ;

        _constructor( PUBLIC ) ;
        _body() ;
            _expr(_super());
        _end() ;

        _method( PRIVATE, _Object(), "writeReplace" ) ;
        _body() ;
            _return(_call(_this(), "selfAsBaseClass" )) ;
        _end() ;

        int ctr=0 ;
        for (MethodInfo method : methods) {
            createMethod( ctr++, method ) ;
        }

        _end() ; // of _class

        return _generate( anchorClass, debug ? debugProps : emptyProps, ps ) ;
    }

    private static final Type objectArrayType = Type._array(_Object()) ;

    private static void createMethod( int mnum, MethodInfo method ) {
        Type rtype = method.returnType() ;
        _method( method.modifiers() & ~ABSTRACT, rtype, method.name()) ;

        List<Expression> args = new ArrayList<>() ;
        for (Variable var : method.arguments() ) {
            args.add( _arg( var.type(), var.ident() ) ) ;
        }

        _body() ;
            List<Expression> wrappedArgs = new ArrayList<>() ;
            for (Expression arg : args) {
                wrappedArgs.add( Primitives.wrap( arg ) ) ;
            }

            Expression invokeArgs = _define( objectArrayType, "args",
                _new_array_init( _Object(), wrappedArgs ) ) ;

            // create expression to call the invoke method
            Expression invokeExpression = _call(
                _this(), "invoke", _const(mnum), invokeArgs ) ;

            // return result if non-void
            if (rtype == _void()) {
                _expr( invokeExpression ) ;
                _return() ;
            } else {
                Expression resultExpr = _define( _Object(), "result", invokeExpression ) ;

                if (rtype != _Object()) {
                    if (rtype.isPrimitive()) {
                        // Must cast resultExpr to expected type, or unwrap won't work!
                        Type ctype = Primitives.getWrapperTypeForPrimitive( rtype ) ;
                        Expression cexpr = _cast( ctype, resultExpr ) ;
                        _return( Primitives.unwrap( cexpr ) ) ;
                    } else {
                        _return(_cast(rtype, resultExpr )) ;
                    }
                } else {
                    _return( resultExpr ) ;
                }
            }
        _end() ; // of method
    }
}
