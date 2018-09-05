/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import org.junit.Test;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.portable.IndirectionException;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

public class CDRInputValueTest extends ValueTestBase {


    @Test
    public void canReadStringValue() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(RepositoryId.kWStringValueRepID);
        writeStringValue_1_2("This, too!");
        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue( object instanceof String);
        assertEquals("This, too!", object);
    }

    @Test
    public void canReadStringValueInAChunk() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(RepositoryId.kWStringValueRepID);

        startChunk();
        writeStringValue_1_2("This, too!");
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof String);
        assertEquals("This, too!", object);
    }

    @Test(expected = MARSHAL.class)
    public void whenRepIdNotRecognized_throwException() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId("RMI:com.sun.corba.ee.impl.encoding.NoSuchValue:3E1F37A79F0D0984:F72C4A0542764A7B");

        writeWchar_1_2('x');
        writeInt(3);

        setMessageBody( getGeneratedBody() );

        getInputObject().read_value();
    }
/*

    static final String VALUE1_REPID2 = "RMI:com.sun.corba.ee.impl.encoding.Value1:3E1F37A79F0B1235:F72C4A0542764A7B";

    @Test
    public void canReadSerializedValueWithMismatchedRepID() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(VALUE1_REPID2);

        writeWchar_1_2('x');
        writeInt(7);
        writeInt(3);

        setMessageBody(getGeneratedBody());

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals(3, value1.anInt);
    }
*/

    @Test
    public void canReadSerializedValue() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');
        writeInt(3);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals(3, value1.anInt);
    }

    @Test
    public void canReadSerializedEnum() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Enum1.REPID);

        writeString(Enum1.strange.toString());

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertEquals( Enum1.strange, object);
    }

    @Test
    public void canReadIDLEntity() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(IDLValue.REPID);

        int aByte = 0x45;
        writeByte(aByte);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof IDLValue);
        IDLValue value = (IDLValue) object;
        assertEquals(0x45, value.aByte);
        assertEquals(0x450, value.anInt);
    }

    @Test
    public void canReadSerializedValueWithIndirection() throws IOException {
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID | USE_CODEBASE);
        writeCodebase("ignore this");
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');
        writeInt(3);
        writeIndirectionTo(location);

        setMessageBody( getGeneratedBody() );

        Object object1 = getInputObject().read_value();
        Object object2 = getInputObject().read_value();
        assertSame(object1, object2);
    }

    @Test
    public void canReadSerializedValueWithIndirection_in1_1() throws IOException {
        useV1_1();
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_1('x');
        writeInt(3);
        writeIndirectionTo(location);

        setMessageBody( getGeneratedBody() );

        Object object1 = getInputObject().read_value();
        Object object2 = getInputObject().read_value();
        assertSame(object1, object2);
        assertEquals('x', ((Value1) object1).aChar);
    }

    @Test
    public void canReadSerializedValueWithIndirection_in1_0() throws IOException {
        useV1_0();
        setOrbVersion(ORBVersionFactory.getOLD());
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_1('x');
        writeInt(3);
        writeIndirectionTo(location);

        setMessageBody( getGeneratedBody() );

        Object object1 = getInputObject().read_value();
        Object object2 = getInputObject().read_value();
        assertSame(object1, object2);
        assertEquals( 'x', ((Value1) object1).aChar);
    }

    @Test
    public void canReadSerializedValueInChunk() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals(3, value1.anInt);
    }

    @Test
    public void canReadSerializedValueWithContinuationChunk() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        endChunk();

        startChunk();
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals(3, value1.anInt);
    }

    @Test
    public void canReadSerializedValueWithNestedValue() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value2.REPID);

        startChunk();
        writeLong(750);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);
        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value2);
        Value2 value2 = (Value2) object;
        assertEquals(750,value2.aLong);
        assertEquals('x', value2.aValue.aChar);
        assertEquals(3, value2.aValue.anInt);
    }

    @Test(expected = MARSHAL.class)
    public void whenEndTagTooSmall_throwException() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value2.REPID);

        startChunk();
        writeLong(750);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);
        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-3);
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        getInputObject().read_value();
    }

    @Test
    public void whenTalkingtoLegacyORBAndEndTagTooSmall_ignoreIt() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value2.REPID);

        startChunk();
        writeLong(750);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);
        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value2);
        Value2 value2 = (Value2) object;
        assertEquals(750,value2.aLong);
        assertEquals('x', value2.aValue.aChar);
        assertEquals(3, value2.aValue.anInt);
    }

    @Test
    public void canReadSerializedValueUsingDefaultFactory() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CODEBASE);
        writeCodebase("http://localhost/myClasses");
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value(Value1.REPID);
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals('x', value1.anInt);
    }

    @Test
    public void canReadNullValueUsingDefaultFactory() throws IOException {
        writeNull();
        setMessageBody( getGeneratedBody() );

        assertNull(getInputObject().read_value(Value1.REPID));
    }

    @Test(expected = IndirectionException.class)
    public void whenIndirectionHasNoAntecedent_throwExceptionWhenUsingRepId() throws IOException {
        writeIndirectionTo(0);
        setMessageBody( getGeneratedBody() );
        getInputObject().read_value(Value1.REPID);
    }

    @Test
    public void canReadSerializedValueUsingDefaultFactoryAndIndirection() throws IOException {
        int location = getCurrentLocation();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);
        startChunk();
        writeWchar_1_2('x');
        endChunk();
        writeEndTag(-1);

        writeIndirectionTo(location);

        setMessageBody( getGeneratedBody() );

        Object object1 = getInputObject().read_value(Value1.REPID);
        Object object2 = getInputObject().read_value(Value1.REPID);
        assertSame(object1, object2);
    }


    @Test
    public void whenObjectImplementsReadResolve_resultingValueMatchesOriginal() throws Exception {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Gender.REPID);
        writeInt(0);  // the serialized form of the MALE constant, produced by writeReplace
        setMessageBody(getGeneratedBody());

        assertThat(getInputObject().read_value(), sameInstance((Serializable) Gender.MALE));
    }

    @Test
    public void whenInaccessibleObjectImplementsReadResolve_resultingValueEqualToOriginal() throws Exception {
        String InetAddressRepId = "RMI:java.net.InetAddress:C156A93A2ABC4FAF:2D9B57AF9FE3EBDB";

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);  // custom marshalling requires a chunk
        writeRepId(InetAddressRepId);

        startChunk();
        writeInt(0x01010000);
        writeInt(0x7F000001);  // 127.0.0.1
        writeInt(0x00000002);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);  // custom marshalling requires a chunk
        writeRepId("IDL:omg.org/CORBA/WStringValue:1.0");
        startChunk();
        writeStringValue_1_2("localhost");
        endChunk();
        writeEndTag(-1);

        setMessageBody(getGeneratedBody());

        assertThat(getInputObject().read_value(), equalTo((Serializable) InetAddress.getLoopbackAddress()));
    }

    @Test
    public void whenObjectExternalizable_callReadExternal() throws Exception {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);  // custom marshalling requires a chunk
        writeRepId(Profession.REPID);

        startChunk();
        writeByte(1);    // serial format version
        writeInt(5);
        endChunk();
        writeEndTag(-1);

        setMessageBody(getGeneratedBody());
        Serializable value = getInputObject().read_value();

        assertThat(value, instanceOf(Profession.class));
        Profession profession = (Profession) value;
        assertThat(profession.getProfession(), equalTo("Lawyer"));
    }
}
