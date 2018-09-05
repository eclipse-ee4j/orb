/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package CodeSetTester;

import java.util.Arrays;
import org.omg.CORBA.DataOutputStream;
import org.omg.CORBA.DataInputStream;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.CharSeqHelper;
import org.omg.CORBA.WCharSeqHelper;

// Custom marshaled valuetype with all the data types we're interested in.

public class CustomMarshaledValueImpl extends CustomMarshaledValue
{
    public CustomMarshaledValueImpl() {
        str = "";
        wstr = "";
    }

    public CustomMarshaledValueImpl(char ch, 
                                    char wch, 
                                    String str, 
                                    String wstr,
                                    char[] chSeq,
                                    char[] wchSeq) {
        this.ch = ch;
        this.wch = wch;
        if (str == null)
            this.str = "";
        else 
            this.str = new String(str);
        if (wstr == null)
            this.wstr = "";
        else
            this.wstr = new String(wstr);
        this.chSeq = chSeq;
        this.wchSeq = wchSeq;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();

        sbuf.append("ch: ");
        sbuf.append((int)ch);
        sbuf.append(" wch: ");
        sbuf.append((int)wch);
        sbuf.append(" str: ");
        if (str == null)
            sbuf.append("null");
        else
            sbuf.append(str.length());
        sbuf.append(" wstr: ");
        if (wstr == null)
            sbuf.append("null");
        else
            sbuf.append(wstr.length());

        sbuf.append(" chSeq: ");
        if (chSeq == null)
            sbuf.append("null");
        else {
            for (int i = 0; i < chSeq.length; i++) {
                sbuf.append((int)chSeq[i]);
                sbuf.append(' ');
            }
        }

        sbuf.append(" wchSeq: ");
        if (wchSeq == null)
            sbuf.append("null");
        else {
            for (int i = 0; i < wchSeq.length; i++) {
                sbuf.append((int)wchSeq[i]);
                sbuf.append(' ');
            }
        }

        return sbuf.toString();
    }

    public boolean equals(Object obj) {
        try {
            CustomMarshaledValue cmv = (CustomMarshaledValue)obj;

            return (ch == cmv.ch &&
                    wch == cmv.wch &&
                    ((str == null && cmv.str == null) ||
                     (str.equals(cmv.str))) &&
                    ((wstr == null && cmv.wstr == null) ||
                     (wstr.equals(cmv.wstr))) &&
                    Arrays.equals(chSeq, cmv.chSeq) &&
                    Arrays.equals(wchSeq, cmv.wchSeq));

        } catch (ClassCastException cce) {
            return false;
        }
    }

    public void unmarshal(DataInputStream is) {
        System.out.println("Unmarshaling custom data...");
        ch = is.read_char();
        wch = is.read_wchar();
        str = is.read_string();
        wstr = is.read_wstring();

        chSeq = CharSeqHelper.read((InputStream)is);
        wchSeq = WCharSeqHelper.read((InputStream)is);

        System.out.println("Done");
    }

    public void marshal(DataOutputStream os) {
        System.out.println("Marshaling custom data...");
        os.write_char(ch);
        os.write_wchar(wch);
        os.write_string(str);
        os.write_wstring(wstr);

        if (chSeq == null)
            os.write_long(0);
        else
            CharSeqHelper.write((OutputStream)os, chSeq);

        if (wchSeq == null)
            os.write_long(0);
        else
            WCharSeqHelper.write((OutputStream)os, wchSeq);

        System.out.println("Done");
    }
}
