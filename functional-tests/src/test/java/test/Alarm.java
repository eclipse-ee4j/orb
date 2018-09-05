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
 * Alarm provides a one-shot mechanism to schedule asynchronous calls
 * to an AlarmHandler.
 * Typical usage:
 * <pre>
 *   Alarm.scheduleWakeupFromNow(myAlarmHandler,1000); // Wait 1 second.
 * </pre>
 * @author Bryan Atsatt
 */
public class Alarm implements Runnable
{
    private static Vector   fgAlarms;
    private static Thread   fgThread;
    private static boolean      fgStarted;

    private AlarmHandler        fHandler;
    private long                        fWakeupTime;

    /**
     * Constructor.
     * @param handler the alarm handler to invoke at wake up.
     * @param wakeupTime the time to wake up.
     */
    public Alarm (AlarmHandler handler, long wakeupTime)
    {
        fHandler = handler;
        fWakeupTime = wakeupTime;
    }

    /**
     * Check if this alarm's wakeup time is before a given alarm.
     */
    public boolean isBefore (Alarm alarm)
    {
        return fWakeupTime < alarm.fWakeupTime;
    }

    /**
     * Get handler.
     */
    public AlarmHandler getHandler ()
    {
        return fHandler;
    }

    /**
     * Set handler.
     */
    public void setHandler (AlarmHandler handler)
    {
        fHandler = handler;
    }

    /**
     * Get wakeup time.
     */
    public long getWakeupTime ()
    {
        return fWakeupTime;
    }

    /**
     * Set wakeup time. This does <i>not</i> schedule a wake up call.
     */
    public void setWakeupTime (long wakeupTime)
    {
        fWakeupTime = wakeupTime;
    }

    /**
     * Schedule a wake up call relative to now. Alarms are one-shot and
     * therefore must be rescheduled after wakeup if another wakeup is desired.
     * @param handler the alarm handler to invoke at wake up.
     * @param wakeupDeltaMillis the number of milliseconds from now at which to wake up.
     * @return the scheduled alarm.
     */
    public static Alarm scheduleWakeupFromNow (AlarmHandler handler, long wakeupDeltaMillis)
    {
        return scheduleWakeup(new Alarm(handler,System.currentTimeMillis() + wakeupDeltaMillis));
    }

    /**
     * Schedule a wake up call relative to now. Alarms are one-shot and
     * therefore must be rescheduled after wakeup if another wakeup is desired.
     * @param theAlarm the alarm to schedule.
     * @param wakeupDeltaMillis the number of milliseconds from now at which to wake up.
     * @return the scheduled alarm.
     */
    public static Alarm scheduleWakeupFromNow (Alarm theAlarm, long wakeupDeltaMillis)
    {
        theAlarm.setWakeupTime(System.currentTimeMillis() + wakeupDeltaMillis);
        return scheduleWakeup(theAlarm);
    }

    /**
     * Schedule an alarm. Alarms are one-shot and therefore must be rescheduled after
     * wakeup if another wakeup is desired.
     * @param handler the alarm handler to invoke at wake up.
     * @param wakeupDelta the number of milliseconds from now at which to wake up.
     * @return the scheduled alarm.
     */
    public static Alarm scheduleWakeup (Alarm theAlarm)
    {
        synchronized (fgAlarms)
            {
                // Start our thread if needed...

                if (fgStarted == false)
                    {
                        fgStarted = true;
                        fgThread.start();
                    }

                // Insert alarm such that the next alarm is at the lowest index.
                // Do binary search till gap is 2 or less...

                int low = 0;
                int high = fgAlarms.size();

                while (high - low > 2)
                    {
                        int middle = (low + high) / 2;

                        if (theAlarm.isBefore( (Alarm)fgAlarms.elementAt(middle)) )
                            {
                                // Shift to low half of array...

                                high = middle;
                            }
                        else
                            {
                                // Shift to high half of array...

                                low = middle + 1;
                            }
                    }

                // Do linear search on remaining...

                while (low < high)
                    {
                        if (((Alarm)fgAlarms.elementAt(low)).isBefore(theAlarm))
                            {
                                low++;
                            }
                        else
                            {
                                break;
                            }
                    }

                // Ok, do insert...

                fgAlarms.insertElementAt(theAlarm,low);

                // Notify the alarm thread...

                fgAlarms.notify();
            }

        return theAlarm;
    }

    /**
     * Cancel a scheduled an alarm. Cancellation may fail if
     * the alarm has already been fired.
     * @param theAlarm the alarm to cancel.
     * @return true if canceled, false otherwise.
     */
    public static boolean cancelWakeup (Alarm theAlarm)
    {
        boolean result = false;

        synchronized (fgAlarms)
            {
                // Is the alarm in the queue?

                int count = fgAlarms.size();

                for (int index = 0; index < count; index++)
                    {
                        if (fgAlarms.elementAt(index) == theAlarm)
                            {
                                // Found it. Do remove...

                                fgAlarms.removeElementAt(index);

                                // Set result and notify the alarm thread.

                                result = true;
                                fgAlarms.notify();
                            }
                    }
            }

        return result;
    }

    /**
     * Wakeup and call handler.
     */
    private void wakeup (long nextWakeupTime)
    {
        fHandler.wakeup(this, nextWakeupTime);
    }

    /**
     * The method that is executed when a Runnable object is activated.  The run() method
     * is the "soul" of a Thread.  It is in this method that all of the action of a
     * Thread takes place.
     * @see Thread#run
     */
    public void run()
    {
        while (true)
            {
                synchronized (fgAlarms)
                    {
                                // Wait till we have something to schedule...

                        while (fgAlarms.isEmpty())
                            {
                                try
                                    {
                                        fgAlarms.wait();
                                    }
                                catch (Throwable e){}
                            }
                    }

                // Wait till wakeup time of first element. Note that the lock is deliberately
                // released inside the loop in order to provide access to scheduleWakeup()...

                while (true)
                    {
                        synchronized (fgAlarms)
                            {
                                if (fgAlarms.isEmpty()) break; // Can happen if canceled.

                                Alarm theAlarm = (Alarm) fgAlarms.firstElement();

                                long delta = theAlarm.getWakeupTime() - System.currentTimeMillis();

                                if (delta > 0)
                                    {
                                        try
                                            {
                                                fgAlarms.wait(delta);
                                            }
                                        catch (Throwable e){}
                                    }
                                else
                                    {
                                        // Time to wakeup...

                                        try
                                            {
                                                // Remove the current alarm...

                                                fgAlarms.removeElementAt(0);

                                                // Get the next wakeup time, if any...

                                                long nextWakeup = 0;

                                                if (fgAlarms.isEmpty() == false)
                                                    {
                                                        nextWakeup = ((Alarm) fgAlarms.firstElement()).getWakeupTime();
                                                    }

                                                // Wake 'em up...

                                                theAlarm.wakeup(nextWakeup);
                                            }
                                        catch (Throwable e){}

                                        break;   // Break out of loop.
                                    }
                            }
                    }
            }
    }

    // Only for fgThread...

    private Alarm()
    {
    }

    // Init our static data...

    static
    {
        fgAlarms = new Vector();
        fgThread = new Thread(new Alarm(),"AlarmThread");
        fgThread.setDaemon(true);
        fgStarted = false;
    }
}
