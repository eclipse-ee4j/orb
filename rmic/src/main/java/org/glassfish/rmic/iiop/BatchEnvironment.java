/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.ClassPath;

import java.io.File;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * BatchEnvironment for iiop extends rmic's version to add
 * parse state.
 */
public class BatchEnvironment extends org.glassfish.rmic.BatchEnvironment implements Constants {

    /*
     * If the following flag is true, then the IDL generator can map
     * the methods and constants of non-conforming types. However,
     * this is very expensive, so the default should be false.
     */
    private boolean parseNonConforming = false;

    /**
     * This flag indicates that the stubs and ties need to be generated without
     * the package prefix (org.omg.stub).
     */
    private boolean standardPackage;

    /* Common objects used within package */

    HashSet<Type> alreadyChecked = new HashSet<>();
    Hashtable<String, Type> allTypes = new Hashtable<>(3001, 0.5f);
    Hashtable<Type, String> invalidTypes = new Hashtable<>(256, 0.5f);
    DirectoryLoader loader = null;
    ClassPathLoader classPathLoader = null;
    Hashtable<String, NameContext> nameContexts = null;
    Hashtable<String, String> namesCache = new Hashtable<>();
    NameContext modulesContext = new NameContext(false);

    ClassDefinition defRemote = null;
    ClassDefinition defError = null;
    ClassDefinition defException = null;
    ClassDefinition defRemoteException = null;
    ClassDefinition defCorbaObject = null;
    ClassDefinition defSerializable = null;
    ClassDefinition defExternalizable = null;
    ClassDefinition defThrowable = null;
    ClassDefinition defRuntimeException = null;
    ClassDefinition defIDLEntity = null;
    ClassDefinition defValueBase = null;

    org.glassfish.rmic.tools.java.Type typeRemoteException = null;
    org.glassfish.rmic.tools.java.Type typeIOException = null;
    org.glassfish.rmic.tools.java.Type typeException = null;
    org.glassfish.rmic.tools.java.Type typeThrowable = null;

    ContextStack contextStack = null;

    /**
     * Create a BatchEnvironment for rmic with the given class path,
     * stream for messages and the destination directory.
     */
    public BatchEnvironment(OutputStream out, ClassPath path, File destinationDir) {

        super(out, path, destinationDir);

        // Make sure we have our definitions...

        try {
            defRemote =
                getClassDeclaration(idRemote).getClassDefinition(this);
            defError =
                getClassDeclaration(idJavaLangError).getClassDefinition(this);
            defException =
                getClassDeclaration(idJavaLangException).getClassDefinition(this);
            defRemoteException =
                getClassDeclaration(idRemoteException).getClassDefinition(this);
            defCorbaObject =
                getClassDeclaration(idCorbaObject).getClassDefinition(this);
            defSerializable =
                getClassDeclaration(idJavaIoSerializable).getClassDefinition(this);
            defRuntimeException =
                getClassDeclaration(idJavaLangRuntimeException).getClassDefinition(this);
            defExternalizable =
                getClassDeclaration(idJavaIoExternalizable).getClassDefinition(this);
            defThrowable=
                getClassDeclaration(idJavaLangThrowable).getClassDefinition(this);
            defIDLEntity=
                getClassDeclaration(idIDLEntity).getClassDefinition(this);
            defValueBase=
                getClassDeclaration(idValueBase).getClassDefinition(this);
            typeRemoteException = defRemoteException.getClassDeclaration().getType();
            typeException = defException.getClassDeclaration().getType();
            typeIOException = getClassDeclaration(idJavaIoIOException).getType();
            typeThrowable = getClassDeclaration(idJavaLangThrowable).getType();

            classPathLoader = new ClassPathLoader(path);

        } catch (ClassNotFound e) {
            error(0, "rmic.class.not.found", e.name);
            throw new Error();
        }
    }

    /**
     * Return whether or not to parse non-conforming types.
     */
    public boolean getParseNonConforming () {
        return parseNonConforming;
    }

    /**
     * Set whether or not to parse non-conforming types.
     */
    public void setParseNonConforming (boolean parseEm) {

        // If we are transitioning from not parsing to
        // parsing, we need to throw out any previously
        // parsed types...

        if (parseEm && !parseNonConforming) {
            reset();
        }

        parseNonConforming = parseEm;
    }

    void setStandardPackage(boolean standardPackage) {
        this.standardPackage = standardPackage;
    }

    boolean getStandardPackage() {
        return standardPackage;
    }

    /**
     * Clear out any data from previous executions.
     */
    @Override
    public void reset () {

        // First, find all Type instances and call destroy()
        // on them...

        for (Enumeration<Type> e = allTypes.elements() ; e.hasMoreElements() ;) {
            Type type = e.nextElement();
            type.destroy();
        }

        for (Enumeration<Type> e = invalidTypes.keys() ; e.hasMoreElements() ;) {
            Type type = e.nextElement();
            type.destroy();
        }

        for (Type type : alreadyChecked) {
            type.destroy();
        }

        if (contextStack != null) contextStack.clear();

        // Remove and clear all NameContexts in the
        // nameContexts cache...

        if (nameContexts != null) {
            for (Enumeration<NameContext> e = nameContexts.elements() ; e.hasMoreElements() ;) {
                NameContext context = e.nextElement();
                context.clear();
            }
            nameContexts.clear();
        }

        // Now remove all table entries...

        allTypes.clear();
        invalidTypes.clear();
        alreadyChecked.clear();
        namesCache.clear();
        modulesContext.clear();

        // Clean up remaining...
        loader = null;
        parseNonConforming = false;

        // REVISIT - can't clean up classPathLoader here
    }

    /**
     * Release resources, if any.
     */
    @Override
    public void shutdown() {
        if (alreadyChecked != null) {
            //System.out.println();
            //System.out.println("allTypes.size() = "+ allTypes.size());
            //System.out.println("    InstanceCount before reset = "+Type.instanceCount);
            reset();
            //System.out.println("    InstanceCount AFTER reset = "+Type.instanceCount);

            alreadyChecked = null;
            allTypes = null;
            invalidTypes = null;
            nameContexts = null;
            namesCache = null;
            modulesContext = null;
            defRemote = null;
            defError = null;
            defException = null;
            defRemoteException = null;
            defCorbaObject = null;
            defSerializable = null;
            defExternalizable = null;
            defThrowable = null;
            defRuntimeException = null;
            defIDLEntity = null;
            defValueBase = null;
            typeRemoteException = null;
            typeIOException = null;
            typeException = null;
            typeThrowable = null;

            super.shutdown();
        }
    }
}
