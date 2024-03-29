/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

package com.sun.corba.ee.impl.encoding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

class AllTypesFields implements Serializable {
  private boolean aBooleanField;
  private byte aByteField;
  private char aCharField;
  private short aShortField;
  private int anIntField;
  private long aLongField;
  private float aFloatField;
  private double aDoubleField;
  private String aStringField;

  private static final ObjectStreamField[] serialPersistentFields = {
      new ObjectStreamField("aBoolean", boolean.class),
      new ObjectStreamField("aByte", byte.class),
      new ObjectStreamField("aChar", char.class),
      new ObjectStreamField("aShort", short.class),
      new ObjectStreamField("anInt", int.class),
      new ObjectStreamField("aLong", long.class),
      new ObjectStreamField("aFloat", float.class),
      new ObjectStreamField("aDouble", double.class),
      new ObjectStreamField("aString", String.class)
      };

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    final ObjectInputStream.GetField fields = in.readFields();
    aBooleanField = fields.get("aBoolean", false);
    aByteField = fields.get("aByte", (byte) 0xff);
    aCharField = fields.get("aChar", 'z');
    aShortField = fields.get("aShort", Short.MIN_VALUE);
    anIntField = fields.get("anInt", Integer.MIN_VALUE);
    aLongField = fields.get("aLong", Long.MIN_VALUE);
    aFloatField = fields.get("aFloat", Float.MIN_VALUE);
    aDoubleField = fields.get("aDouble", Double.MIN_VALUE);
    aStringField = (String) fields.get("aString", "nothing here");
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    final ObjectOutputStream.PutField fields = out.putFields();
    fields.put("aBoolean", aBooleanField);
    fields.put("aByte", aByteField);
    fields.put("aChar", aCharField);
    fields.put("aShort", aShortField);
    fields.put("anInt", anIntField);
    fields.put("aLong", aLongField);
    fields.put("aFloat", aFloatField);
    fields.put("aDouble", aDoubleField);
    fields.put("aString", aStringField);
    out.writeFields();
  }

  boolean getBooleanField() {
    return aBooleanField;
  }

  void setBooleanField(boolean aBooleanField) {
    this.aBooleanField = aBooleanField;
  }

  byte getByteField() {
    return aByteField;
  }

  void setByteField(byte aByteField) {
    this.aByteField = aByteField;
  }

  char getCharField() {
    return aCharField;
  }

  void setCharField(char aCharField) {
    this.aCharField = aCharField;
  }

  short getShortField() {
    return aShortField;
  }

  void setShortField(short aShortField) {
    this.aShortField = aShortField;
  }

  int getIntField() {
    return anIntField;
  }

  void setIntField(int anIntField) {
    this.anIntField = anIntField;
  }

  long getLongField() {
    return aLongField;
  }

  void setLongField(long aLongField) {
    this.aLongField = aLongField;
  }

  float getFloatField() {
    return aFloatField;
  }

  void setFloatField(float aFloatField) {
    this.aFloatField = aFloatField;
  }

  double getDoubleField() {
    return aDoubleField;
  }

  void setDoubleField(double aDoubleField) {
    this.aDoubleField = aDoubleField;
  }

  String getStringField() {
    return aStringField;
  }

  void setStringField(String aStringField) {
    this.aStringField = aStringField;
  }
}
