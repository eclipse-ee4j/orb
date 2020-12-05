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

public abstract class OBVTestObjectOne implements org.omg.CORBA.portable.StreamableValue
{
  // We mark these as transients just so we can make sure during the test
  // that only by using the Streamable interface could this class be serialized.
  protected transient int fInt = (int)0;
  protected transient long fLong = (long)0;
  protected transient float fFloat = (float)0;
  protected transient double fDouble = (double)0;
  protected transient String fString = null;
    
  private static String[] _truncatable_ids = {
    OBVTestObjectOneHelper.id ()
  };
    
  public String[] _truncatable_ids() {
    return _truncatable_ids;
    }

  public void _read (org.omg.CORBA.portable.InputStream istream)
    {
    this.fInt = istream.read_long ();
    this.fLong = istream.read_longlong ();
    this.fFloat = istream.read_float ();
    this.fDouble = istream.read_double ();
    this.fString = istream.read_string ();
    }

  public void _write (org.omg.CORBA.portable.OutputStream ostream)
    {
    ostream.write_long (this.fInt);
    ostream.write_longlong (this.fLong);
    ostream.write_float (this.fFloat);
    ostream.write_double (this.fDouble);
    ostream.write_string (this.fString);
            }

  public org.omg.CORBA.TypeCode _type ()
    {
    return OBVTestObjectOneHelper.type ();
    }
}
