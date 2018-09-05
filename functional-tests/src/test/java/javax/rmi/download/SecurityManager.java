/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.download;

import java.io.FileDescriptor;
import java.util.Hashtable;
import java.net.InetAddress;
import java.lang.reflect.Member;
//import java.security.*;

public class SecurityManager extends java.rmi.RMISecurityManager {

    //   public void checkPermission(Permission perm) {
    //    }

    public void checkCreateClassLoader() { } 
    public void checkAccess(Thread g) { }
    public void checkAccess(ThreadGroup g) { }
    public void checkExit(int status) { }
    public void checkExec(String cmd) { }
    public void checkLink(String lib) { }
    public void checkRead(FileDescriptor fd) { }
    public void checkRead(String file) { }
    public void checkRead(String file, Object context) { }
    public void checkWrite(FileDescriptor fd) { }
    public void checkWrite(String file) { }
    public void checkDelete(String file) { }
    public void checkConnect(String host, int port) { }
    public void checkConnect(String host, int port, Object context) { }
    public void checkListen(int port) { }
    public void checkAccept(String host, int port) { }
    public void checkMulticast(InetAddress maddr) { }
    public void checkMulticast(InetAddress maddr, byte ttl) { }
    public void checkPropertiesAccess() { }
    public void checkPropertyAccess(String key) { }
    public void checkPropertyAccess(String key, String def) { }
    public boolean checkTopLevelWindow(Object window) { return true; }
    public void checkPrintJobAccess() { }
    public void checkSystemClipboardAccess() { }
    public void checkAwtEventQueueAccess() { }
    public void checkPackageAccess(String pkg) { }
    public void checkPackageDefinition(String pkg) { }
    public void checkSetFactory() { }
    public void checkMemberAccess(Class<?> clazz, int which) { }
    public void checkSecurityAccess(String provider) { }
}       
