/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.requestpartitioning;

import com.sun.corba.ee.spi.orb.ORB;

public class TesterImpl extends TesterPOA
{
    private ORB orb = null;

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    // return the thread pool where request was executed
    public int getThreadPoolIdForThisRequest(String theString) {
        int result;

        // Get the current thread's name, parse the thread id.
        // Thread's name looks like  "p: xx; w: yy"
        //        parsing indexes --- 0123456789

        String threadName = Thread.currentThread().getName();
        String tmpStr = null;
        if (threadName.charAt(5) == ';') {
            // double digit thread id
            tmpStr = threadName.substring(3,5);
        }
        else {
            // single digit thread id
            tmpStr = threadName.substring(3,4);
        }

        result = Integer.valueOf(tmpStr).intValue();

        return result;
    }
}
