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

package javax.rmi.CORBA.serialization;

public class DateImpl extends Date {
    // nk
    private java.util.Date delegate = null;
    // nk

    public DateImpl() {
        // nk
        delegate = new java.util.Date();
        // nk
    }

    public DateImpl(long arg0) {
        // nk
        delegate = new java.util.Date(arg0);
        // nk
    }

    public DateImpl(int arg0, int arg1, int arg2) {
        // nk
        delegate = new java.util.Date(arg0, arg1, arg2);
        // nk
    }

    public DateImpl(int arg0, int arg1, int arg2, int arg3, int arg4) {
        // nk
        delegate = new java.util.Date(arg0, arg1, arg2, arg3, arg4);
        // nk
    }

    public DateImpl(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        // nk
        delegate = new java.util.Date(arg0, arg1, arg2, arg3, arg4, arg5);
        // nk

    }

    public DateImpl(String arg0) {
        // nk
        delegate = new java.util.Date(arg0);
        // nk
    }

    @Override
    public long UTC(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        // nk
        return java.util.Date.UTC(arg0, arg1, arg2, arg3, arg4, arg5);
        // nk
    }

    @Override
    public long parse(String arg0) {
        // nk
        return java.util.Date.parse(arg0);
        // nk
    }

    @Override
    public int year() {
        // nk
        return delegate.getYear();
        // nk
    }

    @Override
    public void year(int newYear) {
        // nk
        delegate.setYear(newYear);
        // nk
    }

    @Override
    public int month() {
        // nk
        return delegate.getMonth();
        // nk
    }

    @Override
    public void month(int newMonth) {
        // nk
        delegate.setMonth(newMonth);
        // nk
    }

    @Override
    public int date() {
        // nk
        return delegate.getDate();
        // nk
    }

    @Override
    public void date(int newDate) {
        // nk
        delegate.setDate(newDate);
        // nk
    }

    @Override
    public int day() {
        // nk
        return delegate.getDay();
        // nk
    }

    @Override
    public int hours() {
        // nk
        return delegate.getHours();
        // nk

    }

    @Override
    public void hours(int newHours) {
        // nk
        delegate.setHours(newHours);
        // nk
    }

    @Override
    public int minutes() {
        // nk
        return delegate.getMinutes();
        // nk
    }

    @Override
    public void minutes(int newMinutes) {
        // nk
        delegate.setMinutes(newMinutes);
        // nk
    }

    @Override
    public int seconds() {
        // nk
        return delegate.getSeconds();
        // nk
    }

    @Override
    public void seconds(int newSeconds) {
        // nk
        delegate.setSeconds(newSeconds);
        // nk
    }

    @Override
    public long time() {
        // nk
        return delegate.getTime();
        // nk
    }

    @Override
    public void time(long newTime) {
        // nk
        delegate.setTime(newTime);
        // nk
    }

    @Override
    public boolean before(javax.rmi.CORBA.serialization.Date arg0) {
        // nk
        return delegate.before(((DateImpl) arg0).getDelegate());
        // nk
    }

    @Override
    public boolean after(javax.rmi.CORBA.serialization.Date arg0) {
        // nk
        return delegate.after(((DateImpl) arg0).getDelegate());
        // nk
    }

    @Override
    public boolean _equals(org.omg.CORBA.Any arg0) {
        // nk
        return false;
        // nk
    }

    @Override
    public int _hashCode() {
        // nk
        return delegate.hashCode();
        // nk
    }

    @Override
    public String _toString() {
        // nk
        return delegate.toString();
        // nk
    }

    @Override
    public String toLocaleString() {
        // nk
        return delegate.toLocaleString();
        // nk
    }

    @Override
    public String toGMTString() {
        // nk
        return delegate.toGMTString();
        // nk
    }

    @Override
    public int timezoneOffset() {
        // nk
        return delegate.getTimezoneOffset();
        // nk

    }

    // nk
    public void setDelegate(java.util.Date delegate) {
        this.delegate = delegate;
    }

    public java.util.Date getDelegate() {
        return delegate;
    }
    // nk

    // nk
    // Methods to be implemented for Custom Marshalling
    @Override
    public void marshal(org.omg.CORBA.DataOutputStream os) {
        os.write_octet((byte) 1);
        os.write_boolean(false);
        os.write_longlong(delegate.getTime());
    }

    @Override
    public void unmarshal(org.omg.CORBA.DataInputStream is) {
        is.read_octet();
        is.read_boolean();
        delegate = new java.util.Date(is.read_longlong());
    }
    // nk
} // class DateImpl
