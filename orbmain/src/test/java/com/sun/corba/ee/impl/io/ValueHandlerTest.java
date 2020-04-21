/*
 * Copyright (c) 2016, 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.io;

import com.sun.corba.ee.impl.encoding.Enum1;
import com.sun.corba.ee.impl.encoding.ValueTestBase;
import com.sun.corba.ee.impl.util.RepositoryId;
import org.junit.Assert;
import org.junit.Test;
import org.omg.CORBA_2_3.portable.InputStream;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import javax.rmi.CORBA.ValueHandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class ValueHandlerTest extends ValueTestBase {

  ValueHandler valueHandler = ValueHandlerImpl.getInstance();

  /**
   * Ensures that value handler impl is final
   */
  @Test
  public void testValueHandlerImplIsFinal() {
    int mods = ValueHandlerImpl.class.getModifiers();

    Assert.assertTrue(Modifier.isFinal(mods));
  }

  /**
   * Ensures the SharedSecrets class works
   */
  @Test
  public void testSharedSecrets() {
    Assert.assertNotNull(SharedSecrets.getJavaCorbaAccess());
  }

  @Test
  public void canReadStringValue() throws IOException {
    writeStringValue_1_2("This, too!");

    assertThat(readWithValueHandler(String.class), equalTo("This, too!"));
  }

  @SuppressWarnings("unchecked")
  private <T extends Serializable> T readWithValueHandler(Class<T> theClass) {
    setMessageBody(getGeneratedBody());
    InputStream inputStream = getInputObject();
    Serializable value = valueHandler.readValue(inputStream, 0, theClass,
          RepositoryId.createForJavaType(theClass), getInputObject().getCodeBase());
    assertThat(value, instanceOf(theClass));
    return (T) value;
  }

  @Test
  public void canReadSerializedEnum() throws IOException {
    writeValueTag(ONE_REPID_ID);
    writeRepId(RepositoryId.kWStringValueRepID);
    writeStringValue_1_2(Enum1.strange.toString());

    assertThat(readWithValueHandler(Enum1.class), equalTo(Enum1.strange));
  }

  @Test
  public void canReadSerializedEnumWithAbstractMethod() throws IOException {
    writeValueTag(ONE_REPID_ID);
    writeRepId(RepositoryId.kWStringValueRepID);
    writeStringValue_1_2("second");

    assertThat(readWithValueHandler(EnumWithAbstractMethod.class).getValue(), equalTo(2));
  }



  static enum EnumWithAbstractMethod {

    first {
      @Override
      int getValue() {
        return 1;
      }
    }, second {
      @Override
      int getValue() {
        return 2;
      }
    }, third {
      @Override
      int getValue() {
        return 3;
      }
    };
    abstract int getValue();
  }

}
