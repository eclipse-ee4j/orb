/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
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
package com.sun.corba.ee.impl.interceptors;

import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.impl.encoding.EncodingTestBase;
import com.sun.corba.ee.spi.orb.ORB;

import java.math.BigDecimal;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;

import static com.sun.corba.ee.impl.interceptors.CodecFactoryImplTest.TestStructMatcher.equalStruct;
import static com.sun.corba.ee.impl.interceptors.CodecFactoryImplTest.TestUnionMatcher.equalUnion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CodecFactoryImplTest extends EncodingTestBase {

  private static final int GIOP_MAJOR_VERSION = 1;
  private static final int MAX_GIOP_MINOR_VERSION = 2;
  private static final double TOLERANCE = 0.01;
  private static final BigDecimal FIXED_TOLERANCE = new BigDecimal(".001");

  private ORB orb = getOrb();
  private CodecFactory codecFactory = new CodecFactoryImpl(orb);
  private Any any = createAny();
  private Codec codec;

  @Before
  public void setUpCodec() throws Exception {
    codec = createCodec(GIOP_MAJOR_VERSION, MAX_GIOP_MINOR_VERSION);
  }

  private Any createAny() {
    return new AnyImpl(orb);
  }

  @Test
  public void supportCodecsForAllVersions() throws UnknownEncoding {
    for (int minor = 0; minor <= MAX_GIOP_MINOR_VERSION; minor++) {
      createCodec(GIOP_MAJOR_VERSION, minor);
    }
  }

  private Codec createCodec(int major, int minor) throws UnknownEncoding {
    return codecFactory.create_codec(createEncoding(major, minor));
  }

  private Encoding createEncoding(int majorVersion, int minor_version) {
    return new Encoding(ENCODING_CDR_ENCAPS.value, (byte) majorVersion, (byte) minor_version);
  }

  @Test (expected = UnknownEncoding.class)
  public void codecNotSupportedFor_minorVersionTooHigh() throws UnknownEncoding {
    createCodec(GIOP_MAJOR_VERSION, MAX_GIOP_MINOR_VERSION + 1);
  }

  @Test (expected = UnknownEncoding.class)
  public void codecNotSupportedFor_majorVersionTooHigh() throws UnknownEncoding {
    createCodec(GIOP_MAJOR_VERSION + 1, 0);
  }

  @Test
  public void encodeDecodeNull() throws InvalidTypeForEncoding, FormatMismatch {
    assertThat(codec.encode(any).length, equalTo(8));
    assertThat(codec.encode_value(any).length, equalTo(1));
    assertThat(encodeDecode().type().kind(), equalTo(TCKind.tk_null));
  }

  private Any encodeDecode() throws InvalidTypeForEncoding, FormatMismatch {
    final byte[] bytes = codec.encode(any);
    return codec.decode(bytes);
  }

  @Test
  public void encodeDecodeBoolean() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_boolean(true);

    assertThat(codec.encode(any).length, equalTo(9));
    assertThat(codec.encode_value(any).length, equalTo(2));
    assertThat(encodeDecode().extract_boolean(), is(true));
    assertThat(encodeDecodeValue().extract_boolean(), is(true));
  }

  private Any encodeDecodeValue() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    final Codec codec = createCodec(GIOP_MAJOR_VERSION, MAX_GIOP_MINOR_VERSION);
    final byte[] bytes = codec.encode_value(any);
    return codec.decode_value(bytes, any.type());
  }

  @Test
  public void encodeDecodeByte() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_octet((byte) 123);

    assertThat(codec.encode(any).length, equalTo(9));
    assertThat(codec.encode_value(any).length, equalTo(2));
    assertThat(encodeDecode().extract_octet(), equalTo((byte) 123));
    assertThat(encodeDecodeValue().extract_octet(), equalTo((byte) 123));
  }

  @Test
  public void encodeDecodeChar() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_char('w');

    assertThat(codec.encode(any).length, equalTo(9));
    assertThat(codec.encode_value(any).length, equalTo(2));
    assertThat(encodeDecode().extract_char(), equalTo('w'));
    assertThat(encodeDecodeValue().extract_char(), equalTo('w'));
  }

  @Test
  public void encodeDecodeShort() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_short((short) 123);

    assertThat(codec.encode(any).length, equalTo(10));
    assertThat(codec.encode_value(any).length, equalTo(4));
    assertThat(encodeDecode().extract_short(), equalTo((short) 123));
    assertThat(encodeDecodeValue().extract_short(), equalTo((short) 123));
  }

  @Test
  public void encodeDecodeInt() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_long(4520);

    assertThat(codec.encode(any).length, equalTo(12));
    assertThat(codec.encode_value(any).length, equalTo(8));
    assertThat(encodeDecode().extract_long(), equalTo(4520));
    assertThat(encodeDecodeValue().extract_long(), equalTo(4520));
  }

  @Test
  public void encodeDecodeLong() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_longlong(45203456L);

    assertThat(codec.encode(any).length, equalTo(16));
    assertThat(codec.encode_value(any).length, equalTo(16));
    assertThat(encodeDecode().extract_longlong(), equalTo(45203456L));
    assertThat(encodeDecodeValue().extract_longlong(), equalTo(45203456L));
  }

  @Test
  public void encodeDecodeFloat() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_float(78.234f);

    assertThat(codec.encode(any).length, equalTo(12));
    assertThat(codec.encode_value(any).length, equalTo(8));
    assertThat((double) encodeDecode().extract_float(), is(closeTo(78.234, TOLERANCE)));
    assertThat((double) encodeDecodeValue().extract_float(), is(closeTo(78.234, TOLERANCE)));
  }

  @Test
  public void encodeDecodeDouble() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    any.insert_double(12378.234);

    assertThat(codec.encode(any).length, equalTo(16));
    assertThat(codec.encode_value(any).length, equalTo(16));
    assertThat(encodeDecode().extract_double(), is(closeTo(12378.234, TOLERANCE)));
    assertThat(encodeDecodeValue().extract_double(), is(closeTo(12378.234, TOLERANCE)));
  }

  @Test
  public void encodeBigDecimal() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    final BigDecimal value = new BigDecimal("1234.5678");
    any.insert_fixed(value);

    assertThat(codec.encode(any).length, equalTo(17));
    assertThat(codec.encode_value(any).length, equalTo(6));
    assertThat(encodeDecode().extract_fixed(), is(closeTo(value, FIXED_TOLERANCE)));
    assertThat(encodeDecodeValue().extract_fixed(), is(closeTo(value, FIXED_TOLERANCE)));
  }

  @Test
  public void encodeString() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    final String value = "hello, world";
    any.insert_string(value);

    assertThat(codec.encode(any).length, equalTo(29));
    assertThat(codec.encode_value(any).length, equalTo(21));
    assertThat(encodeDecode().extract_string(), equalTo(value));
    assertThat(encodeDecodeValue().extract_string(), equalTo(value));
  }

  @Test
  public void encodeWideString() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    final String value = "hello, world";
    any.insert_wstring(value);

    assertThat(codec.encode(any).length, equalTo(40));
    assertThat(codec.encode_value(any).length, equalTo(32));
    assertThat(encodeDecode().extract_wstring(), equalTo(value));
    assertThat(encodeDecodeValue().extract_wstring(), equalTo(value));
  }

  @Test
  public void encodeStruct() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    TestStruct testStruct = new TestStruct( (short) 10, (short) 20 );
    TestStructHelper.insert(any, testStruct);

    assertThat(codec.encode(any).length, equalTo(100));
    assertThat(codec.encode_value(any).length, equalTo(6));
    assertThat(TestStructHelper.extract(encodeDecode()), equalStruct(testStruct));
    assertThat(TestStructHelper.extract(encodeDecodeValue()), equalStruct(testStruct));
  }

  @SuppressWarnings("unused")
  static class TestStructMatcher extends TypeSafeDiagnosingMatcher<TestStruct> {
    private TestStruct expected;

    private TestStructMatcher(TestStruct expected) {
      this.expected = expected;
    }

    public static TestStructMatcher equalStruct(TestStruct expected) {
      return new TestStructMatcher(expected);
    }

    @Override
    protected boolean matchesSafely(TestStruct testStruct, Description description) {
      if (testStruct.x == expected.x && testStruct.y == expected.y) return true;
      description.appendText(toString(testStruct));
      return false;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(toString(expected));
    }

    private String toString(TestStruct o) {
      return String.format("TestStruct[ x=%d, y=%d ]", o.x, o.y);
    }
  }

  @Test
  public void encodeUnion() throws UnknownEncoding, InvalidTypeForEncoding, FormatMismatch, TypeMismatch {
    TestUnion testUnion = new TestUnion();
    testUnion.f(3.45f);
    TestUnionHelper.insert(any, testUnion);

    assertThat(codec.encode(any).length, equalTo(120));
    assertThat(codec.encode_value(any).length, equalTo(8));
    assertThat(TestUnionHelper.extract(encodeDecode()), equalUnion(testUnion));
    assertThat(TestUnionHelper.extract(encodeDecodeValue()), equalUnion(testUnion));
  }

  @SuppressWarnings("unused")
  static class TestUnionMatcher extends TypeSafeDiagnosingMatcher<TestUnion> {
    private TestUnion expected;

    private TestUnionMatcher(TestUnion expected) {
      this.expected = expected;
    }

    public static TestUnionMatcher equalUnion(TestUnion expected) {
      return new TestUnionMatcher(expected);
    }

    @Override
    protected boolean matchesSafely(TestUnion TestUnion, Description description) {
      if (equals(TestUnion, expected)) return true;
      description.appendText(toString(TestUnion));
      return false;
    }

    private boolean equals(TestUnion first, TestUnion second) {
      if (first.discriminator() != second.discriminator()) return false;

      switch (first.discriminator()) {
        case 1: return first.d() == second.d();
        case 2: return first.f() == second.f();
        default: return false;
      }
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(toString(expected));
    }

    private String toString(TestUnion o) {
      switch (o.discriminator()) {
        case 1: return String.format("TestUnion[ d = %f.3 ]", o.d());
        case 2: return String.format("TestUnion[ f = %f.3 ]", o.f());
        default: return "not initialized";
      }
    }
  }
}
