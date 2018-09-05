/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA.serialization;

public class DateImpl extends Date 
{
    //nk
    private java.util.Date delegate = null;
    //nk

    public  DateImpl ()
    {
        //nk
        delegate = new java.util.Date();
        //nk
    }

    public  DateImpl (long arg0)
    {
        //nk
        delegate = new java.util.Date(arg0);    
        //nk
    }

    public  DateImpl (int arg0, int arg1, int arg2)
    {
        //nk
        delegate = new java.util.Date(arg0, arg1, arg2);
        //nk
    }

    public  DateImpl (int arg0, int arg1, int arg2, int arg3, int arg4)
    {
        //nk
        delegate = new java.util.Date(arg0, arg1, arg2, arg3, arg4);
        //nk
    }

    public  DateImpl (int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)
    {
        //nk
        delegate = new java.util.Date(arg0, arg1, arg2, arg3, arg4, arg5);
        //nk
        
    }

    public  DateImpl (String arg0)
    {
        //nk
        delegate = new java.util.Date(arg0);    
        //nk
    }

    public long UTC (int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)
    {
        //nk
        return delegate.UTC (arg0, arg1, arg2, arg3, arg4, arg5);
        //nk
    }

    public long parse (String arg0)
    {
        //nk
        return delegate.parse(arg0);
        //nk
    }

    public int year ()
    {
        //nk
        return delegate.getYear();
        //nk
    }

    public void year (int newYear)
    {
        //nk
        delegate.setYear(newYear);
        //nk
    }

    public int month ()
    {
        //nk
        return delegate.getMonth();
        //nk
    }

    public void month (int newMonth)
    {
        //nk
        delegate.setMonth(newMonth);
        //nk
    }

    public int date ()
    {
        //nk
        return delegate.getDate();
        //nk
    }

    public void date (int newDate)
    {
        //nk
        delegate.setDate(newDate);
        //nk
    }

    public int day ()
    {
        //nk
        return delegate.getDay();
        //nk
    }

    public int hours ()
    {
        //nk
        return delegate.getHours();
        //nk
        
    }

    public void hours (int newHours)
    {
        //nk
        delegate.setHours(newHours);
        //nk
    }

    public int minutes ()
    {
        //nk
        return delegate.getMinutes();
        //nk
    }

    public void minutes (int newMinutes)
    {
        //nk
        delegate.setMinutes(newMinutes);
        //nk
    }

    public int seconds ()
    {
        //nk
        return delegate.getSeconds();   
        //nk
    }

    public void seconds (int newSeconds)
    {
        //nk
        delegate.setSeconds(newSeconds);        
        //nk
    }

    public long time ()
    {
        //nk
        return delegate.getTime();
        //nk
    }

    public void time (long newTime)
    {
        //nk
        delegate.setTime(newTime);
        //nk
    }

    public boolean before (javax.rmi.CORBA.serialization.Date arg0)
    {
        //nk
        return delegate.before(((DateImpl)arg0).getDelegate());
        //nk
    }

    public boolean after (javax.rmi.CORBA.serialization.Date arg0)
    {
        //nk
        return delegate.after(((DateImpl)arg0).getDelegate());
        //nk
    }

    public boolean _equals (org.omg.CORBA.Any arg0)
    {
        //nk
        return false;
        //nk
    }

    public int _hashCode ()
    {
        //nk
        return delegate.hashCode();
        //nk    
    }

    public String _toString ()
    {
        //nk
        return delegate.toString();
        //nk
    }

    public String toLocaleString ()
    {
        //nk
        return delegate.toLocaleString();
        //nk
    }

    public String toGMTString ()
    {
        //nk
        return delegate.toGMTString();
        //nk
    }

    public int timezoneOffset ()
    {
        //nk
        return delegate.getTimezoneOffset();
        //nk
        
    }

    //nk
    public void setDelegate (java.util.Date delegate)
    {
        this.delegate = delegate;
    }

    public java.util.Date getDelegate() 
    {
        return delegate;
    }   
    //nk
        
  //nk
  //Methods to be implemented for Custom Marshalling
    public void marshal(org.omg.CORBA.DataOutputStream os)
    {
        os.write_octet((byte)1);
        os.write_boolean(false);
        os.write_longlong(delegate.getTime());
    }

    public void unmarshal(org.omg.CORBA.DataInputStream is)
    {
        is.read_octet();
        is.read_boolean();
        delegate = new java.util.Date(is.read_longlong());
    }
    //nk
} // class DateImpl
