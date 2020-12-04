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

//package com.sun.enterprise.iiop;

// NOTE: This is the EXACT same file as in corba/folb unit test,
// except for parts commented out.
// REVISIT: Need to use from a direct copy of AS version.
// Just update Ant to build and use it AND provide dummy logging code.

package corba.folb_8_1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

//import com.sun.logging.LogDomains;

// These are "ee" in the AS version:
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.impl.misc.ORBUtility;

/**
 * This is the "sticky manager" - based on the 7.1 EE concept.
 * @author Harold Carr
 */
public class IIOPPrimaryToContactInfoImpl 
    implements IIOPPrimaryToContactInfo
{

    // REVISIT - log messages must be internationalized.
    /*
    private static Logger _logger = null;
    static {
       _logger = LogDomains.getLogger(LogDomains.CORBA_LOGGER);
    }
    */

    private static MyLogger _logger = new MyLogger();

    public final String baseMsg = IIOPPrimaryToContactInfoImpl.class.getName();

    // This is made package private for FOLB timing test.
    //private Map map;
    Map map;
    private boolean debugChecked;
    private boolean debug;
    public void setDebugChecked(boolean x) { debugChecked = x; }
    public void setDebug       (boolean x) { debug        = x; }

    public IIOPPrimaryToContactInfoImpl()
    {
        map = new HashMap();
        debugChecked = false;
        debug = false;
    }

    public synchronized void reset(ContactInfo primary)
    {
        try {
            if (debug) {
                dprint(".reset: " + getKey(primary));
            }
            map.remove(getKey(primary));
        } catch (Throwable t) {
            _logger.log(Level.WARNING,
                        "Problem in " + baseMsg + ".reset",
                        t);
            RuntimeException rte =
                new RuntimeException(baseMsg + ".reset error");
            rte.initCause(t);
            throw rte;
        }
    }

    public synchronized boolean hasNext(ContactInfo primary,
                                        ContactInfo previous,
                                        List contactInfos)
    {
        try {
            if (! debugChecked) {
                debugChecked = true;
                debug = ((ORB)primary.getBroker()).transportDebugFlag 
                        || _logger.isLoggable(Level.FINE);
            }

            if (debug) {
                dprint(".hasNext->: " 
                       + formatKeyPreviousList(getKey(primary),
                                               previous,
                                               contactInfos));
            }
            boolean result;
            if (previous == null) {
                result = true;
            } else {
                int previousIndex = contactInfos.indexOf(previous);
                int contactInfosSize = contactInfos.size();
                if (debug) {
                    dprint(".hasNext: " 
                           + previousIndex + " " + contactInfosSize);
                }
                if (previousIndex < 0) {
                    // This SHOULD not happen.
                    // It would only happen if the previous is NOT
                    // found in the current list of contactInfos.
                    RuntimeException rte = new RuntimeException(


                        "Problem in " + baseMsg + ".hasNext: previousIndex: "
                        + previousIndex);

                    _logger.log(Level.SEVERE, 
                        "Problem in " + baseMsg + ".hasNext: previousIndex: "
                        + previousIndex, rte);
                    throw rte;
                } else {
                    // Since this is a retry, ensure that there is a following
                    // ContactInfo for .next
                    result = (contactInfosSize - 1) > previousIndex;
                }
            }
            if (debug) {
                dprint(".hasNext<-: " + result);
            }
            return result;
        } catch (Throwable t) {
            _logger.log(Level.WARNING, 
                        "Problem in " + baseMsg + ".hasNext",
                        t);
            RuntimeException rte =
                new RuntimeException(baseMsg + ".hasNext error");
            rte.initCause(t);
            throw rte;
        }
    }

    public synchronized ContactInfo next(ContactInfo primary,
                                         ContactInfo previous,
                                         List contactInfos)
    {
        try {
            String debugMsg = null;

            if (debug) {
                debugMsg = "";
                dprint(".next->: " 
                       + formatKeyPreviousList(getKey(primary),
                                               previous,
                                               contactInfos));
                dprint(".next: map: " + formatMap(map));
            }

            Object result = null;

            if (previous == null) {
                // This is NOT a retry.
                result = map.get(getKey(primary));
                if (result == null) {
                    if (debug) {
                        debugMsg = ".next<-: initialize map: ";
                    }
                    // NOTE: do not map primary to primary.
                    // In case of local transport we NEVER use primary.
                    result = contactInfos.get(0);
                    map.put(getKey(primary), result);
                } else {
                    if (debug) {
                        dprint(".next: primary mapped to: " + result);
                    }
                    int position = contactInfos.indexOf(result);
                    if (position == -1) {
                        // It is possible that communication to the key
                        // took place on SharedCDR, then a corbaloc to 
                        // same location uses a SocketOrChannelContactInfo
                        // and vice versa.
                        if (debug) {
                            dprint(".next: cannot find mapped entry in current list.  Removing mapped entry and trying .next again.");
                        }
                        reset(primary);
                        return next(primary, previous, contactInfos);
                    }
                    // NOTE: This step is critical.  You do NOT want to
                    // return contact info from the map.  You want to find
                    // it, as a SocketInfo, in the current list, and then
                    // return that ContactInfo.  Otherwise you will potentially
                    // return a ContactInfo pointing to an incorrect IOR.
                    result = contactInfos.get(position);
                    if (debug) {
                        debugMsg = ".next<-: mapped: ";
                    }
                }
            } else {
                // This is a retry.
                // If previous is last element then .next is not called
                // because hasNext will return false.
                result = contactInfos.get(contactInfos.indexOf(previous) + 1);
                map.put(getKey(primary), result);

                _logger.log(Level.INFO, "IIOP failover to: " + result);

                if (debug) {
                    debugMsg = ".next<-: update map: " 
                        + " " + contactInfos.indexOf(previous)
                        + " " + contactInfos.size() + " ";
                }
            }
            if (debug) {
                dprint(debugMsg + result);
            }
            return (ContactInfo) result;
        } catch (Throwable t) {
            _logger.log(Level.WARNING,
                        "Problem in " + baseMsg + ".next",
                        t);
            RuntimeException rte =
                new RuntimeException(baseMsg + ".next error");
            rte.initCause(t);
            throw rte;
        }
    }

    private Object getKey(ContactInfo contactInfo)
    {
        if (((SocketInfo)contactInfo).getPort() == 0) {
            // When CSIv2 is used the primary will have a zero port.
            // Therefore type/host/port will NOT be unique.
            // So use the entire IOR for the key in that case.
            return ((ContactInfoList)contactInfo.getContactInfoList())
                .getEffectiveTargetIOR();
        } else {
            return contactInfo;
        }
    }

    private String formatKeyPreviousList(Object key,
                                         ContactInfo previous, List list)
    {
        String result =
              "\n  key     : " + key
            + "\n  previous: " + previous
            + "\n  list:";
        Iterator i = list.iterator();
        int count = 1;
        while (i.hasNext()) {
            result += "\n    " + count++ + "  " + i.next();
        }
        return result;
    }

    private String formatMap(Map map)
    {
        String result = "";
        synchronized (map) {
            Iterator i = map.entrySet().iterator();
            if (! i.hasNext()) {
                return "empty";
            }
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                result += 
                      "\n    key  : " + entry.getKey()
                    + "\n    value: " + entry.getValue()
                    + "\n";
            }
        }
        return result;
    }

    private void dprint(String msg)
    {
        _logger.log(Level.FINE, msg);
    }
}


class MyLogger
{
    void log(Level level, String msg)
    {
        log(level, msg, null);
    }

    void log(Level level, String msg, Throwable t)
    {
        ORBUtility.dprint("IIOPPrimaryToContactInfoImpl.MyLogger.log", msg);
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    boolean isLoggable(Level level)
    {
        return false;
    }
}


// End of file.
