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

package com.sun.corba.ee.impl.dynamicany;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.omg.CORBA.TypeCodePackage.BadKind;

import com.sun.corba.ee.spi.orb.ORB ;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynFixed;

public class DynFixedImpl extends DynAnyBasicImpl implements DynFixed
{
    private static final long serialVersionUID = -426296363713464920L;
    //
    // Constructors
    //

    private DynFixedImpl() {
        this(null, (Any)null, false);
    }

    protected DynFixedImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }

    // Sets the current position to -1 and the value to zero.
    protected DynFixedImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        index = NO_INDEX;
    }

    //
    // DynAny interface methods
    //
/*
    public int component_count() {
        return 0;
    }
*/
    //
    // DynFixed interface methods
    //

    public String get_value () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return any.extract_fixed().toString();
    }

    // Initializes the value of the DynFixed.
    // The val string must contain a fixed string constant in the same format
    // as used for IDL fixed-point literals.
    //
    // It may consist of an integer part, an optional decimal point,
    // a fraction part and an optional letter d or D.
    // The integer and fraction parts both must be sequences of decimal (base 10) digits.
    // Either the integer part or the fraction part, but not both, may be missing.
    //
    // If val contains a value whose scale exceeds that of the DynFixed or is not initialized,
    // the operation raises InvalidValue.
    // The return value is true if val can be represented as the DynFixed without loss of precision.
    // If val has more fractional digits than can be represented in the DynFixed,
    // fractional digits are truncated and the return value is false.
    // If val does not contain a valid fixed-point literal or contains extraneous characters
    // other than leading or trailing white space, the operation raises TypeMismatch.
    //
    public boolean set_value (String val)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        int digits = 0;
        boolean preservedPrecision = true;
        try {
            digits = any.type().fixed_digits();
        } catch (BadKind ex) { // impossible
        }
        // First get rid of leading or trailing whitespace which is allowed
        String string = val.trim();
        if (string.length() == 0) {
            throw new TypeMismatch();
        }
        // Now scan for the sign
        String sign = "";
        if (string.charAt(0) == '-') {
            sign = "-";
            string = string.substring(1);
        } else if (string.charAt(0) == '+') {
            sign = "+";
            string = string.substring(1);
        }
        // Now get rid of the letter d or D.
        int dIndex = string.indexOf('d');
        if (dIndex == -1) {
            dIndex = string.indexOf('D');
        }
        if (dIndex != -1) {
            string = string.substring(0, dIndex);
        }
        // Just to be sure
        if (string.length() == 0) {
            throw new TypeMismatch();
        }
        // Now look for the dot to determine the integer part
        String integerPart;
        String fractionPart;
        int currentScale;
        int currentDigits;
        int dotIndex = string.indexOf('.');
        if (dotIndex == -1) {
            integerPart = string;
            fractionPart = null;
            currentScale = 0;
            currentDigits = integerPart.length();
        } else if (dotIndex == 0 ) {
            integerPart = null;
            fractionPart = string;
            currentScale = fractionPart.length();
            currentDigits = currentScale;
        } else {
            integerPart = string.substring(0, dotIndex);
            fractionPart = string.substring(dotIndex + 1);
            currentScale = fractionPart.length();
            currentDigits = integerPart.length() + currentScale;
        }

        int integerPartLength = (integerPart == null) ? 0 
            : integerPart.length() ;    
        
        // Let's see if we have to drop some precision
        if (currentDigits > digits) {
            preservedPrecision = false;
            // truncate the fraction part
            if (integerPartLength < digits) {
                fractionPart = fractionPart.substring(0, digits - integerPartLength ) ;
            } else if (integerPartLength == digits) {
                // currentScale > 0
                // drop the fraction completely
                fractionPart = null;
            } else {
                // integerPartLength > digits
                // unable to truncate fraction part 
                throw new InvalidValue();
            }
        }
        // If val contains a value whose scale exceeds that of the DynFixed or is not initialized,
        // the operation raises InvalidValue.
        // Reinterpreted to mean raise InvalidValue only if the integer part exceeds precision,
        // which is handled above (integerPart.length() > digits)
/*
        if (currentScale > scale) {
            throw new InvalidValue("Scale exceeds " + scale);
        }
*/
        // Now check whether both parts are valid numbers
        BigDecimal result;
        try {
            if (fractionPart == null) {
                result = new BigDecimal(sign + integerPart);
            } else {
                result = new BigDecimal(sign + integerPart + "." + fractionPart);
            }
        } catch (NumberFormatException nfe) {
            throw new TypeMismatch();
        }
        any.insert_fixed(result, any.type());
        return preservedPrecision;
    }

    @Override
    public String toString() {
        int digits = 0;
        int scale = 0;
        try {
            digits = any.type().fixed_digits();
            scale = any.type().fixed_scale();
        } catch (BadKind ex) { // impossible
        }
        return "DynFixed with value=" + this.get_value() + ", digits=" + digits + ", scale=" + scale;
    }
}
