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
