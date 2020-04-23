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
import java.math.BigInteger;

import com.sun.corba.ee.impl.util.RepositoryId;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FieldsIOValueTest extends ValueTestBase {


  @Test
  public void whenFieldsWrittenWithFormatVersion1_includeInSingleValue() throws IOException {
    useStreamFormatVersion1();
    BigInteger bigInteger = new BigInteger("12345");
    getOutputObject().write_value(bigInteger);

    serializeDefaultData(bigInteger);
    writeEndTag(-1);

    setMessageBody(getGeneratedBody());
    expectByteArray(getGeneratedBody());
  }

  /**
   * Write all but the end tag(s) for the Big Integer.
   *
   * @param bigInteger the value to write
   * @throws IOException if something goes wrong
   */
  private void serializeDefaultData(BigInteger bigInteger) throws IOException {
    writeValueTag(ONE_REPID_ID | USE_CHUNKING);  // custom marshalling requires a chunk
    writeRepId(RepositoryId.createForJavaType(BigInteger.class));
    startChunk();
    startCustomMarshalingFormat(true);
    writeInt(BIG_INTEGER_BIT_COUNT);
    writeInt(BIG_INTEGER_BIT_LENGTH);
    writeInt(BIG_INTEGER_FIRST_NONZERO_BYTE_NUM);
    writeInt(BIG_INTEGER_LOWEST_SET_BIT);
    writeInt(1);    // sign value
    endChunk();

    writeValueTag(ONE_REPID_ID | USE_CHUNKING);  // custom marshalling requires a chunk
    writeRepId(RepositoryId.createForJavaType(byte[].class));
    startChunk();
    writeByteArray(bigInteger.toByteArray());
    endChunk();
  }

  // legacy values
  final static int BIG_INTEGER_BIT_COUNT = -1;
  final static int BIG_INTEGER_BIT_LENGTH = -1;
  final static int BIG_INTEGER_FIRST_NONZERO_BYTE_NUM = -2;
  final static int BIG_INTEGER_LOWEST_SET_BIT = -2;

  @Test
  public void whenFieldsWrittenWithFormatVersion2_writeNullOptionalData() throws IOException {
    useStreamFormatVersion2();
    BigInteger bigInteger = new BigInteger("123450");
    getOutputObject().write_value(bigInteger);

    serializeDefaultData(bigInteger);
    writeEndTag(-2);

    startChunk();
    writeInt(0);        // null to indicate no optional data is present (format version 2 only)
    endChunk();
    writeEndTag(-1);
    setMessageBody(getGeneratedBody());
    expectByteArray(getGeneratedBody());
  }

    @Test
    public void whenFieldsWrittenWithFormatVersion1_ignoreOptionalData() throws IOException {
      useStreamFormatVersion1();
      BigInteger bigInteger = new BigInteger("123450");

      serializeDefaultData(bigInteger);
      writeEndTag(-1);

      BigInteger value = readValueFromGeneratedBody(BigInteger.class);
      assertThat(value, equalTo(bigInteger));
    }

    @Test
    public void whenFieldsWrittenWithFormatVersion2_readNullOptionalData() throws IOException {
      useStreamFormatVersion2();
      BigInteger bigInteger = new BigInteger("123450");

      serializeDefaultData(bigInteger);
      writeEndTag(-2);

      startChunk();
      writeInt(0);        // null to indicate no optional data is present (format version 2 only)
      endChunk();
      writeEndTag(-1);

      BigInteger value = readValueFromGeneratedBody(BigInteger.class);
      assertThat(value, equalTo(bigInteger));
    }
}
