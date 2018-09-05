/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework;

// RTMUtilities class contains routines that are common to use for all RTM
// tests
public class RTMUtilities {

    private static boolean isOnTheList(String current, String list) {
        if (current != null && list != null) {
            if (list.indexOf(current) == -1)
                return false; // Not on the list
            else
                return true; // on the list
        } else
            return false; // either property isn't provided
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
