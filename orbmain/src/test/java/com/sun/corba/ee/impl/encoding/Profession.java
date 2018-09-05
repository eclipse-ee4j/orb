/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A class which uses externalization to serialize values into the stream.
 */
public class Profession implements Externalizable {
    private static final long serialVersionUID = 0x7123456789ABCDEFL;
    public static final String REPID = "RMI:" + Profession.class.getName() + ":0000000000000001:" + Long.toHexString(serialVersionUID).toUpperCase();

    private static final String STRINGS[] = {"Rich Man", "Poor Man", "Beggar Man", "Thief", "Doctor", "Lawyer", "Indian Chief"};

    static final Profession DOCTOR = new Profession("Doctor");
    private static final int NOT_FOUND = -1;

    private String profession;

    public Profession() {
    }

    private Profession(String profession) {
        this.profession = profession;
    }

    String getProfession() {
        return profession;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int i = getProfessionIndex();
        out.writeInt(i);
        if (i == NOT_FOUND) out.writeObject(profession);
    }

    private int getProfessionIndex() {
        for (int i = 0; i < STRINGS.length; i++)
            if (STRINGS[i].equalsIgnoreCase(profession)) return i;

        return NOT_FOUND;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int index = in.readInt();
        if (index == NOT_FOUND)
            profession = (String) in.readObject();
        else
            profession = STRINGS[index];
    }
}
