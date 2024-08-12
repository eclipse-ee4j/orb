/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import org.junit.Test;
import org.omg.CORBA.VM_TRUNCATABLE;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import static com.meterware.simplestub.Stub.createStrictStub;

public class CDROutputValueTest extends ValueTestBase {

    static final String ARRAY_LIST_REPID = "RMI:java.util.ArrayList:F655154F32815380:7881D21D99C7619D";

    Value1Helper value1Helper = createStrictStub(Value1Helper.class);

    @Test
    public void canWriteStringValue() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(RepositoryId.kWStringValueRepID);
        writeStringValue_1_2("This, too!");

        getOutputObject().write_value("This, too!");
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValue() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');
        writeInt(3);

        Value1 value1 = new Value1('x', 3);
        getOutputObject().write_value(value1);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }

    /**
     * ArrayLists always use chunking because they have custom marshalling. The Value1 type does not, normally. When a
     * Value1 instance is contained in an ArrayList, it must use chunking to comply with the CORBA spec.
     * 
     * @throws IOException
     */
    @Test
    public void valuesNestedUnderChunkedValuesAreChunked() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(ARRAY_LIST_REPID);

        startChunk();
        writeByte(1); // array header
        writeByte(1); // true: overriding write object
        writeInt(1);
        writeInt(1); // size of array list
        writeByte(0);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        ArrayList<Value1> value = new ArrayList<Value1>(1);
        value.add(new Value1('x', 3));
        getOutputObject().write_value(value);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }

    /**
     * A ComplexValue does not need chunking; however, it contains an ArrayList which does. The next field is a Value1,
     * which should not use chunking.
     * 
     * @throws IOException
     */
    @Test
    public void valuesFollowingChunkedValuesNeedNotBeChunked() throws IOException {
        setFragmentSize(500);
        writeValueTag(ONE_REPID_ID);
        writeRepId(ComplexValue.REPID);
        writeInt(3); // anInt

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(ARRAY_LIST_REPID);

        startChunk();
        writeByte(1); // array header
        writeByte(1);
        writeInt(1);
        writeInt(1); // ArrayList size
        writeByte(0);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        int valueRepIdLocation = getCurrentLocation();
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        writeValueTag(ONE_REPID_ID);
        writeIndirectionTo(valueRepIdLocation);

        writeWchar_1_2('X');
        writeInt(4);

        ComplexValue value = new ComplexValue('x', 3);
        getOutputObject().write_value(value);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueInChunk() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        value1Helper.setModifier(VM_TRUNCATABLE.value);
        useRepId();
        getOutputObject().write_value(new Value1('x', 3), value1Helper);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedEnum() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Enum1.REPID);

        writeString(Enum1.strange.toString());

        getOutputObject().write_value(Enum1.strange);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteIDLEntity() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(IDLValue.REPID);

        byte aByte = 0x45;

        startChunk();
        writeByte(aByte);
        endChunk();
        writeEndTag(-1);

        IDLValue value = new IDLValue(aByte);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueWithIndirection() throws IOException {
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');
        writeInt(3);
        writeIndirectionTo(location);

        Value1 value = new Value1('x', 3);
        getOutputObject().write_value(value);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueWithIndirection_in1_1() throws IOException {
        useV1_1();
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_1('x');
        writeInt(3);
        writeIndirectionTo(location);

        Value1 value = new Value1('x', 3);
        getOutputObject().write_value(value);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueWithIndirection_in1_0() throws IOException {
        useV1_0();
        setOrbVersion(ORBVersionFactory.getOLD());
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_1('x');
        writeInt(3);
        writeIndirectionTo(location);

        Value1 value = new Value1('x', 3);
        getOutputObject().write_value(value);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteCustomValueInChunk_withCompactedEndTags() throws IOException {
        getOutputObject().start_value("ID1");
        getOutputObject().write_long(73);
        getOutputObject().start_value("ID2");
        getOutputObject().write_long(37);
        getOutputObject().end_value();
        getOutputObject().end_value();

        expectByteArrays(new byte[] { 0x7F, FF, FF, 0x0A, 0, 0, 0, 4, 'I', 'D', '1', 0, 0, 0, 0, 4, 0, 0, 0, 73, 0x7F, FF, FF, 0x0A, 0, 0,
                0, 4, 'I', 'D', '2', 0, 0, 0, 0, 4, 0, 0, 0, 37, FF, FF, FF, FF });
    }

    @Test
    public void whenBufferFull_sendFragment() {
        setFragmentSize(Message.GIOPMessageHeaderLength + 8);
        getOutputObject().write_long(1);
        getOutputObject().write_short((short) 2);
        getOutputObject().write_long(3);

        expectByteArrays(new byte[] { 0, 0, 0, 1, 0, 2, 0, 0 }, new byte[] { 0, 0, 0, 3 });
    }

    @Test
    public void whenBufferFullInV1_1_sendFragment() {
        useV1_1();
        setFragmentSize(Message.GIOPMessageHeaderLength + 8);
        getOutputObject().write_long(1);
        getOutputObject().write_short((short) 2);
        getOutputObject().write_long(3);

        expectByteArrays(new byte[] { 0, 0, 0, 1, 0, 2 }, new byte[] { 0, 0, 0, 3 });
    }

    @Test
    public void whenBufferFullInV1_0_expandIt() {
        useV1_0();
        setBufferSize(Message.GIOPMessageHeaderLength + 8);
        getOutputObject().write_long(1);
        getOutputObject().write_short((short) 2);
        getOutputObject().write_long(3);

        expectByteArray(0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 3);
    }

    @Test
    public void whenBufferFullWhileWritingPrimitive_generateContinuationAfterFirstPrimitiveInNewFragment() {
        setFragmentSize(Message.GIOPMessageHeaderLength + 16);
        getOutputObject().start_block();
        getOutputObject().write_long(1);
        getOutputObject().write_long(2);
        getOutputObject().write_long(3);
        getOutputObject().write_long(5);
        getOutputObject().write_long(6);
        getOutputObject().write_long(9);
        getOutputObject().end_block();

        expectByteArrays(new byte[] { 0, 0, 0, 16, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3 },
                new byte[] { 0, 0, 0, 5, 0, 0, 0, 8, 0, 0, 0, 6, 0, 0, 0, 9 });
    }

    @Test
    public void whenBufferFullWhileMidChunkAndWritingArray_generateContinuationAfterArray() {
        setFragmentSize(Message.GIOPMessageHeaderLength + 16);
        getOutputObject().start_block();
        getOutputObject().write_long_array(new int[] { 1, 2, 3, 5, 6 }, 0, 5);
        getOutputObject().write_long(9);
        getOutputObject().end_block();

        expectByteArrays(new byte[] { 0, 0, 0, 20, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3 },
                new byte[] { 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 4, 0, 0, 0, 9 });
    }

    @Test
    public void whenObjectImplementsWriteReplace_outputStreamContainsReplacementValue() throws Exception {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Gender.REPID);

        writeInt(0); // the serialized form of the MALE constant, produced by writeReplace

        getOutputObject().write_value(Gender.MALE);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void whenInaccessibleObjectImplementsWriteReplace_outputStreamContainsReplacementValue() throws Exception {
        String InetAddressRepId = "RMI:java.net.InetAddress:C156A93A2ABC4FAF:2D9B57AF9FE3EBDB";

        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        writeValueTag(ONE_REPID_ID | USE_CHUNKING); // custom marshalling requires a chunk
        writeRepId(InetAddressRepId);

        startChunk();
        writeInt(0x01010000);
        writeInt(0x7F000001); // 127.0.0.1
        writeInt(0x00000002);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING); // custom marshalling requires a chunk
        writeRepId("IDL:omg.org/CORBA/WStringValue:1.0");
        startChunk();
        writeStringValue_1_2("localhost");
        endChunk();
        writeEndTag(-1);

        getOutputObject().write_value(loopbackAddress);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void whenExternalizableObjectWritten_invokeWriteExternalMethod() throws Exception {
        Profession profession = Profession.DOCTOR;
        getOutputObject().write_value(profession);

        writeValueTag(ONE_REPID_ID | USE_CHUNKING); // custom marshalling requires a chunk
        writeRepId(Profession.REPID);

        startChunk();
        writeByte(1); // serial format version
        writeInt(4);
        endChunk();
        writeEndTag(-1);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }
    /*
     * 
     * // write codebase
     * 
     * @Test public void canReadSerializedValueWithContinuationChunk() throws IOException { writeValueTag(ONE_REPID_ID |
     * USE_CHUNKING); writeRepId(Value1.REPID);
     * 
     * startChunk(); writeWchar_1_2('x'); endChunk();
     * 
     * startChunk(); writeInt(3); endChunk(); writeEndTag(-1);
     * 
     * setMessageBody( getGeneratedBody() );
     * 
     * Object object = getInputObject().read_value(); assertTrue(object instanceof Value1); Value1 value1 = (Value1) object;
     * assertEquals('x', value1.aChar); assertEquals(3, value1.anInt); }
     * 
     * @Test public void canReadSerializedValueWithNestedValue() throws IOException { writeValueTag(ONE_REPID_ID |
     * USE_CHUNKING); writeRepId(Value2.REPID);
     * 
     * startChunk(); writeLong(750); endChunk();
     * 
     * writeValueTag(ONE_REPID_ID | USE_CHUNKING); writeRepId(Value1.REPID); startChunk(); writeWchar_1_2('x'); writeInt(3);
     * endChunk(); writeEndTag(-1);
     * 
     * setMessageBody( getGeneratedBody() );
     * 
     * Object object = getInputObject().read_value(); assertTrue(object instanceof Value2); Value2 value2 = (Value2) object;
     * assertEquals(750,value2.aLong); assertEquals('x', value2.aValue.aChar); assertEquals(3, value2.aValue.anInt); }
     * 
     * @Test public void canReadSerializedValueUsingDefaultFactory() throws IOException { writeValueTag(ONE_REPID_ID |
     * USE_CODEBASE); writeCodebase("http://localhost/myClasses"); writeRepId(Value1.REPID);
     * 
     * writeWchar_1_2('x');
     * 
     * setMessageBody( getGeneratedBody() );
     * 
     * Object object = getInputObject().read_value(Value1.REPID); assertTrue(object instanceof Value1); Value1 value1 =
     * (Value1) object; assertEquals('x', value1.aChar); assertEquals('x', value1.anInt); }
     * 
     * @Test public void canReadNullValueUsingDefaultFactory() throws IOException { writeNull(); setMessageBody(
     * getGeneratedBody() );
     * 
     * assertNull(getInputObject().read_value(Value1.REPID)); }
     * 
     * @Test(expected = IndirectionException.class) public void
     * whenIndirectionHasNoAntecedent_throwExceptionWhenUsingRepId() throws IOException { writeIndirectionTo(0);
     * setMessageBody( getGeneratedBody() ); getInputObject().read_value(Value1.REPID); }
     * 
     * @Test public void canReadSerializedValueUsingDefaultFactoryAndIndirection() throws IOException { int location =
     * getCurrentLocation();
     * 
     * writeValueTag(ONE_REPID_ID | USE_CHUNKING); writeRepId(Value1.REPID); startChunk(); writeWchar_1_2('x'); endChunk();
     * writeEndTag(-1);
     * 
     * writeIndirectionTo(location);
     * 
     * setMessageBody( getGeneratedBody() );
     * 
     * Object object1 = getInputObject().read_value(Value1.REPID); Object object2 =
     * getInputObject().read_value(Value1.REPID); assertSame(object1, object2); }
     * 
     */
}
