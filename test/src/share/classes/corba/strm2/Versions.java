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

package corba.strm2;

/**
 * As of this writing, the following combinations pass.
 * class5 is ready but has issues with class4 since
 * the CDRInputStream_1_0 with class5 can't seem to know that it's
 * at the end of class4's TestObjectSuper's optional data.
 *
 * class6 and class7 need to be modified (toString and
 * equals methods should take into account possible defaulting
 * as in other classes -- see class4 or class5).  Also,
 * consult Bob Scheifler to see if these are valid
 * tests.  They involve the following evolutionary scenarios:
 *
 * class2:  Super1 extended by Sub
 *
 * class6:  Super0 extended by Super1 extended by Super2 extended by Sub
 *
 * class7:  Classes Super0 and Super2 become custom marshaled
 *
 * class4:  This test is very similar to java.math.BigInteger test, writeObject
 *          calls putfields without calling DefaultWriteObject.
 *
 */
public interface Versions
{
    String[] testableVersions = new String[] { 
        "class0", "class1", "class2", "class3", "class4" };

}

