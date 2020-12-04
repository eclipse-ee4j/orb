/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.corba;

import org.omg.CORBA.TCKind;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA.TypeCodePackage.BadKind;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import java.io.Serializable;
import java.math.BigDecimal;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/**
 *  Static functions for TypeCode interpretation.
 */
public final class TCUtility {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    @SuppressWarnings({"deprecation"})
    static void marshalIn(org.omg.CORBA.portable.OutputStream s, TypeCode typeCode, long l, Object o) {
        switch (typeCode.kind().value()) {
        case TCKind._tk_null:
        case TCKind._tk_void:
        case TCKind._tk_native:
            // nothing to write
            break;

        case TCKind._tk_short:
            s.write_short((short)(l & 0xFFFFL));
            break;

        case TCKind._tk_ushort:
            s.write_ushort((short)(l & 0xFFFFL));
            break;

        case TCKind._tk_enum:
        case TCKind._tk_long:
            s.write_long((int)(l & 0xFFFFFFFFL));
            break;

        case TCKind._tk_ulong:
            s.write_ulong((int)(l & 0xFFFFFFFFL));
            break;

        case TCKind._tk_float:
            s.write_float(Float.intBitsToFloat((int)(l & 0xFFFFFFFFL)));
            break;

        case TCKind._tk_double:
            s.write_double(Double.longBitsToDouble(l));
            break;

        case TCKind._tk_boolean:
            if ( l == 0 )
                s.write_boolean(false);
            else        
                s.write_boolean(true);
            break;

        case TCKind._tk_char:
            s.write_char((char)(l & 0xFFFFL));
            break;

        case TCKind._tk_octet:
            s.write_octet((byte)(l & 0xFFL));
            break;

        case TCKind._tk_any:
            s.write_any((Any)o);
            break;

        case TCKind._tk_TypeCode:
            s.write_TypeCode((TypeCode)o);
            break;

        case TCKind._tk_Principal:
            s.write_Principal(org.omg.CORBA.Principal.class.cast(o));
            break;

        case TCKind._tk_objref:
            s.write_Object((org.omg.CORBA.Object)o);
            break;

        case TCKind._tk_longlong:
            s.write_longlong(l);
            break;

        case TCKind._tk_ulonglong:
            s.write_ulonglong(l);
            break;

        case TCKind._tk_wchar:
            s.write_wchar((char)(l & 0xFFFFL));
            break;

        case TCKind._tk_string:
            s.write_string((String)o);
            break;

        case TCKind._tk_wstring:
            s.write_wstring((String)o);
            break;

        case TCKind._tk_value:
        case TCKind._tk_value_box:
            ((org.omg.CORBA_2_3.portable.OutputStream)s).write_value((Serializable)o);
            break;

        case TCKind._tk_fixed:
            // _REVISIT_ As soon as the java-rtf adds digits and scale parameters to
            // OutputStream, this check will be unnecessary
            if (s instanceof CDROutputObject) {
                try {
                    ((CDROutputObject)s).write_fixed((BigDecimal)o,
                                                    typeCode.fixed_digits(),
                                                    typeCode.fixed_scale());
                } catch (BadKind badKind) { // impossible
                }
            } else {
                s.write_fixed((BigDecimal)o);
            }
            break;

        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_sequence:
        case TCKind._tk_array:
        case TCKind._tk_alias:
        case TCKind._tk_except:
            ((Streamable)o)._write(s);
            break;

        case TCKind._tk_abstract_interface:
            ((org.omg.CORBA_2_3.portable.OutputStream)s).write_abstract_interface(o);
            break;

        case TCKind._tk_longdouble:
            // Unspecified for Java
        default:
            throw wrapper.typecodeNotSupported() ;
        }
    }

    static void unmarshalIn(org.omg.CORBA.portable.InputStream s, TypeCode typeCode, long[] la, Object[] oa) 
    {
        int type = typeCode.kind().value();
        long l=0;
        Object o=oa[0];

        switch (type) {
        case TCKind._tk_null:
        case TCKind._tk_void:
        case TCKind._tk_native:
            // Nothing to read
            break;

        case TCKind._tk_short:
            l = s.read_short() & 0xFFFFL;
            break;

        case TCKind._tk_ushort:
            l = s.read_ushort() & 0xFFFFL;
            break;

        case TCKind._tk_enum:
        case TCKind._tk_long:
            l = s.read_long() & 0xFFFFFFFFL;
            break;
 
        case TCKind._tk_ulong:
            l = s.read_ulong() & 0xFFFFFFFFL;
            break;  
 
        case TCKind._tk_float:
            l = Float.floatToIntBits(s.read_float()) & 0xFFFFFFFFL;
            break;
 
        case TCKind._tk_double:
            l = Double.doubleToLongBits(s.read_double());
            break;
 
        case TCKind._tk_char:
            l = s.read_char() & 0xFFFFL;
            break;

        case TCKind._tk_octet:
            l = s.read_octet() & 0xFFL;
            break;

        case TCKind._tk_boolean:
            if ( s.read_boolean() )
                l = 1;
            else
                l = 0;
            break;

        case TCKind._tk_any:
            o = s.read_any();
            break;

        case TCKind._tk_TypeCode:
            o = s.read_TypeCode();
            break;

        case TCKind._tk_Principal:
            o = s.read_Principal();
            break;

        case TCKind._tk_objref:
            if (o instanceof Streamable)
                ((Streamable)o)._read(s);
            else
                o = s.read_Object();
            break;
 
        case TCKind._tk_longlong:
            l = s.read_longlong();
            break;
 
        case TCKind._tk_ulonglong:
            l = s.read_ulonglong();
            break;  

        case TCKind._tk_wchar:
            l = s.read_wchar() & 0xFFFFL;
            break;
 
        case TCKind._tk_string:
            o = s.read_string();
            break;

        case TCKind._tk_wstring:
            o = s.read_wstring();
            break;

        case TCKind._tk_value:
        case TCKind._tk_value_box:
            o = ((org.omg.CORBA_2_3.portable.InputStream)s).read_value ();
            break;

        case TCKind._tk_fixed:
            try {
                // _REVISIT_ As soon as the java-rtf adds digits and scale parameters to
                // InputStream, this check will be unnecessary
                if (s instanceof CDRInputObject) {
                    o = ((CDRInputObject)s).read_fixed(typeCode.fixed_digits(),
                                                                typeCode.fixed_scale());
                } else {
                    BigDecimal bigDecimal = s.read_fixed();
                    o = bigDecimal.movePointLeft((int)typeCode.fixed_scale());
                }
            } catch (BadKind badKind) { // impossible
            }
            break;

        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_sequence:
        case TCKind._tk_array:
        case TCKind._tk_alias:
        case TCKind._tk_except:
            ((Streamable)o)._read(s);
            break;

        case TCKind._tk_abstract_interface:
            o = ((org.omg.CORBA_2_3.portable.InputStream)s).read_abstract_interface();
            break;

        case TCKind._tk_longdouble:
            // Unspecified for Java
        default:
            throw wrapper.typecodeNotSupported() ;
        }

        oa[0] = o;
        la[0] = l;
    }

}
