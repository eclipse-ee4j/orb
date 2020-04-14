/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.IOException;

import com.sun.corba.ee.impl.util.RepositoryId;
import org.junit.Before;
import org.junit.Test;

import static com.sun.corba.ee.impl.encoding.FVDBuilder.defineFullValueDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FVDInputValueTest extends ValueTestBase {

  private static final int INT_TO_IGNORE = 7;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    defineOriginalValue1FVD();
    defineEvolvedValue1FVD();
  }

  private void defineOriginalValue1FVD() {
    defineFullValueDescription(Value1.REPID)
          .withMember("aChar", char.class)
          .withMember("anInt", int.class)
          .build();
  }

  private void defineEvolvedValue1FVD() {
    defineFullValueDescription(VALUE1_REPID2)
          .withMember("aChar", char.class)
          .withMember("ignoreMe", int.class)
          .withMember("anInt", int.class)
          .build();
  }

  static final String VALUE1_REPID2 = getVariantRepId(Value1.REPID);

  private static String getVariantRepId(String localRepId) {
      final String[] split = localRepId.split(":", 4);
      split[2] = reverse(split[2]);
      return String.join(":", split);
  }

  private static String reverse(String s) {
      return new StringBuilder(s).reverse().toString();
  }

  @Test
  public void canReadSerializedValueWithMismatchedRepIDAndExtraFields() throws IOException {
      writeValueTag(ONE_REPID_ID);
      writeRepId(VALUE1_REPID2);

      writeWchar_1_2('x');
      writeInt(7);
      writeInt(3);

    Value1 value1 = readValueFromGeneratedBody(Value1.class);
    assertEquals('x', value1.aChar);
    assertEquals(3, value1.anInt);
  }

  @SuppressWarnings("unchecked")
  private <T> T readValueFromGeneratedBody(Class<?> valueClass) {
    setMessageBody(getGeneratedBody());

    Object object = getInputObject().read_value();
    assertTrue(valueClass.isInstance(object));
    return (T) object;
  }

  @Test
  public void canReadDerivedValueWithExtraFields() throws IOException {
    defineDerivedValueFVD();

    writeValueTag(ONE_REPID_ID);
    writeRepId(DERIVED_VALUE_REPID);

    writeWchar_1_2('x');
    writeInt(7);
    writeInt(3);

    writeByte(1);
    writeByte(0x34);
    writeFloat(0.25f);
    writeShort((short) 24);
    
    DerivedValue value = readValueFromGeneratedBody(DerivedValue.class);

    assertEquals('x', value.aChar);
    assertEquals(3, value.anInt);
    assertTrue(value.ready);
    assertEquals(0x34, value.aByte);
    assertEquals(24, value.aShort);
  }

  private void defineDerivedValueFVD() {
    defineFullValueDescription(DERIVED_VALUE_REPID)
          .withBaseRepId(VALUE1_REPID2)
          .withMember("ready", boolean.class)
          .withMember("aByte", byte.class)
          .withMember("extra", float.class)
          .withMember("aShort", short.class)
          .build();
  }

  /**
   * A derived class that can be serialized.
   */
  static class DerivedValue extends Value1 {
    boolean ready;
    byte aByte;
    short aShort;
  }

  static final String DERIVED_VALUE_REPID = getVariantRepId(RepositoryId.createForJavaType(DerivedValue.class));

  @Test
  public void canReadCompoundValueWithExtraFields() throws IOException {
    final long long_value = 1234567890L;
    defineValue2Fvd();

    writeValueTag(ONE_REPID_ID);
    writeRepId(VALUE2_REPID2);

    writeLong(long_value);
    writeFloat(73.0F);
    writeValueTag(ONE_REPID_ID + USE_CHUNKING);
    writeRepId(VALUE1_REPID2);
    startChunk();
    writeWchar_1_2('x');
    writeInt(INT_TO_IGNORE);
    writeInt(3);
    endChunk();
    writeEndTag(-1);

    Value2 value = readValueFromGeneratedBody(Value2.class);

    assertEquals('x', value.aValue.aChar);
    assertEquals(3, value.aValue.anInt);
    assertEquals(long_value, value.aLong);
  }

  private void defineValue2Fvd() {
    defineFullValueDescription(VALUE2_REPID2)
          .withMember("aLong", long.class)
          .withMember("extra", float.class)
          .withMember("aValue", VALUE1_REPID2)
          .build();
  }

  static final String VALUE2_REPID2 = getVariantRepId(Value2.REPID);

  @Test
  public void canReadValueWithCustomMarshaling() throws IOException {
    defineCustomMarshalledValueFvd();

    useStreamFormatVersion1();
    writeValueTag(ONE_REPID_ID);
    writeRepId(CUSTOM_VALUE_REPID);
    startCustomMarshalingFormat(true);
    writeDouble(12.34);

    writeValueTag(ONE_REPID_ID);
    writeRepId(Value1.REPID);
    writeWchar_1_2('x');
    writeInt(3);

    writeInt(INT_TO_IGNORE);
    writeFloat(127.0F);

    writeDouble(12.0);

    CustomMarshalledValue value = readValueFromGeneratedBody(CustomMarshalledValue.class);

    assertEquals('x', value.value1.aChar);
    assertEquals(3, value.value1.anInt);
    assertEquals(12.34, value.aDouble, 0.01);
    assertEquals(127.0F, value.aFloat, 0.01);
    assertEquals(12.0, value.customDouble, 0.01);
  }

  private void defineCustomMarshalledValueFvd() {
    defineFullValueDescription(CUSTOM_VALUE_REPID)
          .withCustomMarshalling()
          .withMember("aDouble", double.class)
          .withMember("value1", Value1.REPID)
          .withMember("extra", int.class)
          .withMember("aFloat", float.class)
          .build();
  }

  static final String CUSTOM_VALUE_REPID = getVariantRepId(RepositoryId.createForJavaType(CustomMarshalledValue.class));

  @Test
  public void canReadValueWithCustomWriteMarshaling() throws IOException {
    defineCustomWriteValueFvd();

    useStreamFormatVersion1();
    writeValueTag(ONE_REPID_ID);
    writeRepId(CUSTOM_WRITE_VALUE_REPID);
    startCustomMarshalingFormat(true);
    writeInt(INT_TO_IGNORE);
    writeInt(73);

    CustomWriteClass value = readValueFromGeneratedBody(CustomWriteClass.class);

    assertEquals(73, value.aPositiveValue);
  }

  private void defineCustomWriteValueFvd() {
    defineFullValueDescription(CUSTOM_WRITE_VALUE_REPID)
          .withCustomMarshalling()
          .withMember("extra", int.class)
          .withMember("aPositiveValue", int.class)
          .build();
  }

  static final String CUSTOM_WRITE_VALUE_REPID = getVariantRepId(RepositoryId.createForJavaType(CustomWriteClass.class));

  @Test //@Ignore("Cannot have custom read that calls defaultReadObject without custom write, apparently")
  public void canReadValueWithCustomReadMarshaling() throws IOException {
    defineCustomReadValueFvd();

    useStreamFormatVersion1();
    writeValueTag(ONE_REPID_ID);
    writeRepId(CUSTOM_READ_VALUE_REPID);
    startCustomMarshalingFormat(false);
    writeInt(INT_TO_IGNORE);
    writeInt(-73);

    CustomReadClass value = readValueFromGeneratedBody(CustomReadClass.class);

    assertEquals(1, value.aPositiveValue);
  }

  private void defineCustomReadValueFvd() {
    defineFullValueDescription(CUSTOM_READ_VALUE_REPID)
          .withCustomMarshalling()
          .withMember("extra", int.class)
          .withMember("aPositiveValue", int.class)
          .build();
  }

  static final String CUSTOM_READ_VALUE_REPID = getVariantRepId(RepositoryId.createForJavaType(CustomReadClass.class));

  // todo serial version 1: custom marshalling, more field types
  // todo serial version 2: ordinary class. serializable super class, nested class, custom marshalling
}
