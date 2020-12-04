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

package test;

import java.util.*;

/**
 * AlarmHandler provides an interface which is called asynchronously by Alarm
 * at the scheduled wakeup time.
 * @see Alarm
 * @author Bryan Atsatt
 */
public interface AlarmHandler
{
    /**
     * This method is called asynchronously at the scheduled wakeup time. May be later
     * than the current time, depending on system activity and/or on the amount of time
     * used by previous alarm handlers.
     * <p>
     * The calling thread is owned by the Alarm class and is responsible for <i>all</i>
     * alarms;  therefore, it is recommended that implementations perform
     * only small amounts of processing in order to ensure timely delivery of
     * alarms.  If, in a given process, all usage of alarms is known, implementations
     * may safely use more time.  The nextAlarmWakeupTime argument passed to this
     * method can be used to determine how much time can be used;  however, it is
     * expected that few clients can effectively use this information.
     * @param theAlarm The alarm.
     * @param nextAlarmWakeupTime The next (currently) scheduled wakeup time for any
     * alarm.  May be zero, in which case there are no currently scheduled alarms. (This
     * is not the next scheduled wakeup for <i>this</i> alarm -- alarms are one-shot and
     * must be rescheduled once wakeup is called.)
     */
    public abstract void wakeup (Alarm theAlarm, long nextAlarmWakeupTime);
}
