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
