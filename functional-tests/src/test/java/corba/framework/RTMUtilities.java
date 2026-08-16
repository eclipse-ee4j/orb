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

package corba.framework;

// RTMUtilities class contains routines that are common to use for all RTM
// tests
public class RTMUtilities {

    private static boolean isOnTheList(String current, String list) {
        if (current != null && list != null) {
            if (list.indexOf(current) == -1) {
                return false; // Not on the list
            } else {
                return true; // on the list
            }
        } else {
            return false; // either property isn't provided
        }
    }

    // Return "true" if "current" argument is NOT in the "notRunOnList" argument
    public static boolean isValidToRun(String current, String notRunOnList) {
        String currentProperty = System.getProperty(current);
        String notRunOnListProperty = System.getProperty(notRunOnList);
        return (!RTMUtilities.isOnTheList(currentProperty, notRunOnListProperty));
    }

    // Return "true" if "EXEC_ENV" property is NOT in the "RELEASE_NOT_RUN_ON"
    // property
    public static boolean isValidToRun() {
        // Get the build release that this test suite is currently running on
        String currentRelease = System.getProperty("EXEC_ENV");
        // Get the list of release that certain test scenario should NOT be
        // run on
        String doNotRunOnReleaseList = System.getProperty("RELEASE_NOT_RUN_ON");
        return (!RTMUtilities.isOnTheList(currentRelease, doNotRunOnReleaseList));
    }

}
