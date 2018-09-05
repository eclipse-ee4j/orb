/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.tbbc ;

import org.glassfish.pfl.dynamic.codegen.spi.Expression;
import org.glassfish.pfl.dynamic.codegen.spi.Type;
import java.lang.reflect.Method ;

import static java.lang.reflect.Modifier.* ;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.* ;

public class Sample {

    public static void main( String[] args ) {
        Type ArrayList = _import( "java.util.ArrayList" ) ;
        Type StringArray = Type._array( Type._String() ) ;
        Type System = _import( "java.lang.String" ) ;

        _class( PUBLIC, "MyClass", Type._Object() ) ; {
            final Expression list = _data( PUBLIC, ArrayList, "list" ) ;

            _constructor( PUBLIC )  ; {
                final Expression a = _arg( Type._String(), "a" ) ;
                final Expression b = _arg( ArrayList, "b" ) ;
                _body() ;
                    _super() ;
                    _assign( list, _call( _this(), "bar", a, b ) ) ;
                _end() ; // of constructor
            }

            _method( PUBLIC|STATIC, _thisClass(), "foo" ) ; {
                final Expression a = _arg( Type._String(), "a" ) ;
                _body() ;
                    _return( _new( _thisClass(), a, _new( ArrayList ) ) ) ;
                _end() ; // of method
            }

            _method( PUBLIC, ArrayList, "bar" ) ; {
                final Expression a = _arg( Type._String(), "a" ) ;
                final Expression b = _arg( ArrayList, "b" ) ;
                _body() ;
                    _call( b, "add", _call( a, "toLowerCase" ) ) ;
                    _return( b ) ;
                _end() ; // of method
            }

            _method( PUBLIC, ArrayList, "getList" ) ; {
                _body() ;
                    _return( list ) ;
                _end() ; // of method
            }

            _method( PUBLIC|STATIC, Type._void(), "main" ) ; {
                final Expression margs = _arg( StringArray, "args" ) ;
                _body() ;
                    Expression sout = _field( System, "out" ) ;
                    Expression fooArgs0 = _call( _thisClass(), "foo", _index( margs, _const( 0 ) ) ) ;
                    _call( sout, "println", _call(  fooArgs0, "getList" ) ) ;
                _end() ; // of method
            }

            _end() ; // of class
        }

        Class genClass = Sample.class ;
        Class cls = _generate( genClass.getClassLoader(), genClass.getProtectionDomain(), null ) ;
        
        try {
            Method m = cls.getDeclaredMethod( "main", String[].class ) ;
            m.invoke( null, (Object[])args ) ;
        } catch (Exception exc) {
            java.lang.System.out.println( "Exception: " + exc ) ;
            exc.printStackTrace() ;
        }
    }
}
