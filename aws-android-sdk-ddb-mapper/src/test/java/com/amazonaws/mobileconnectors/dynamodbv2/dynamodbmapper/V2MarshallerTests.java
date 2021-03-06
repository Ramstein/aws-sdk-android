/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper;

import static org.junit.Assert.assertEquals;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.StringUtils;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class V2MarshallerTests {

    private static final ItemConverter CONVERTER = ConversionSchemas.V2
            .getConverter(new ConversionSchema.Dependencies());

    @Test
    public void testBoolean() {
        // These are all native booleans by default in the v2 schema
        assertEquals(true, convert("getBoolean", true).getBOOL());
        assertEquals(false, convert("getBoolean", false).getBOOL());
        assertEquals(true, convert("getBoxedBoolean", true).getBOOL());
        assertEquals(false, convert("getBoxedBoolean", false).getBOOL());
        assertEquals(true, convert("getNativeBoolean", true).getBOOL());
        assertEquals(false, convert("getNativeBoolean", false).getBOOL());
    }

    @Test
    public void testString() {
        assertEquals("abc", convert("getString", "abc").getS());

        assertEquals(RandomUUIDMarshaller.randomUUID,
                convert("getCustomString", "abc").getS());
    }

    @Test
    public void testDate() {
        assertEquals("1970-01-01T00:00:00.000Z",
                convert("getDate", new Date(0)).getS());

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);

        assertEquals("1970-01-01T00:00:00.000Z",
                convert("getCalendar", c).getS());
    }

    @Test
    public void testNumbers() {
        assertEquals("0", convert("getByte", (byte) 0).getN());
        assertEquals("1", convert("getByte", (byte) 1).getN());
        assertEquals("0", convert("getBoxedByte", (byte) 0).getN());
        assertEquals("1", convert("getBoxedByte", (byte) 1).getN());

        assertEquals("0", convert("getShort", (short) 0).getN());
        assertEquals("1", convert("getShort", (short) 1).getN());
        assertEquals("0", convert("getBoxedShort", (short) 0).getN());
        assertEquals("1", convert("getBoxedShort", (short) 1).getN());

        assertEquals("0", convert("getInt", 0).getN());
        assertEquals("1", convert("getInt", 1).getN());
        assertEquals("0", convert("getBoxedInt", 0).getN());
        assertEquals("1", convert("getBoxedInt", 1).getN());

        assertEquals("0", convert("getLong", 0l).getN());
        assertEquals("1", convert("getLong", 1l).getN());
        assertEquals("0", convert("getBoxedLong", 0l).getN());
        assertEquals("1", convert("getBoxedLong", 1l).getN());

        assertEquals("0", convert("getBigInt", BigInteger.ZERO).getN());
        assertEquals("1", convert("getBigInt", BigInteger.ONE).getN());

        assertEquals("0.0", convert("getFloat", 0f).getN());
        assertEquals("1.0", convert("getFloat", 1f).getN());
        assertEquals("0.0", convert("getBoxedFloat", 0f).getN());
        assertEquals("1.0", convert("getBoxedFloat", 1f).getN());

        assertEquals("0.0", convert("getDouble", 0d).getN());
        assertEquals("1.0", convert("getDouble", 1d).getN());
        assertEquals("0.0", convert("getBoxedDouble", 0d).getN());
        assertEquals("1.0", convert("getBoxedDouble", 1d).getN());

        assertEquals("0", convert("getBigDecimal", BigDecimal.ZERO).getN());
        assertEquals("1", convert("getBigDecimal", BigDecimal.ONE).getN());
    }

    @Test
    public void testBinary() {
        final ByteBuffer value = ByteBuffer.wrap("value".getBytes(StringUtils.UTF8));
        assertEquals(value.slice(), convert("getByteArray", "value".getBytes(StringUtils.UTF8))
                .getB());
        assertEquals(value.slice(), convert("getByteBuffer", value.slice()).getB());
    }

    @Test
    public void testBooleanSet() {
        // Marshalling Set<Boolean> is no longer supported in the V2 schema
        // since DynamoDB doesn't have a native Boolean Set type.
        try {
            convert("getBooleanSet", Collections.singleton(true));
            Assert.fail("Expected DynamoDBMappingException");
        } catch (final DynamoDBMappingException e) {
        }
    }

    @Test
    public void testStringSet() {
        assertEquals(Collections.singletonList("a"),
                convert("getStringSet", Collections.singleton("a")).getSS());
        assertEquals(Collections.singletonList("b"),
                convert("getStringSet", Collections.singleton("b")).getSS());

        assertEquals(Arrays.asList("a", "b", "c"),
                convert("getStringSet", new TreeSet<String>() {
                    {
                        add("a");
                        add("b");
                        add("c");
                    }
                }).getSS());
    }

    @Test
    public void testDateSet() {
        assertEquals(Collections.singletonList("1970-01-01T00:00:00.000Z"),
                convert("getDateSet", Collections.singleton(new Date(0)))
                        .getSS());

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);

        assertEquals(Collections.singletonList("1970-01-01T00:00:00.000Z"),
                convert("getCalendarSet", Collections.singleton(c))
                        .getSS());
    }

    @Test
    public void testNumberSet() {
        assertEquals(Collections.singletonList("0"),
                convert("getByteSet", Collections.singleton((byte) 0)).getNS());
        assertEquals(Collections.singletonList("0"),
                convert("getShortSet", Collections.singleton((short) 0)).getNS());
        assertEquals(Collections.singletonList("0"),
                convert("getIntSet", Collections.singleton(0)).getNS());
        assertEquals(Collections.singletonList("0"),
                convert("getLongSet", Collections.singleton(0l)).getNS());
        assertEquals(Collections.singletonList("0"),
                convert("getBigIntegerSet", Collections.singleton(BigInteger.ZERO))
                        .getNS());
        assertEquals(Collections.singletonList("0.0"),
                convert("getFloatSet", Collections.singleton(0f)).getNS());
        assertEquals(Collections.singletonList("0.0"),
                convert("getDoubleSet", Collections.singleton(0d)).getNS());
        assertEquals(Collections.singletonList("0"),
                convert("getBigDecimalSet", Collections.singleton(BigDecimal.ZERO))
                        .getNS());

        assertEquals(Arrays.asList("0", "1", "2"),
                convert("getLongSet", new TreeSet<Number>() {
                    {
                        add(0);
                        add(1);
                        add(2);
                    }
                }).getNS());
    }

    @Test
    public void testBinarySet() {
        final ByteBuffer test = ByteBuffer.wrap("test".getBytes(StringUtils.UTF8));
        final ByteBuffer test2 = ByteBuffer.wrap("test2".getBytes(StringUtils.UTF8));

        assertEquals(
                Collections.singletonList(test.slice()),
                convert("getByteArraySet", Collections.singleton("test".getBytes(StringUtils.UTF8)))
                        .getBS());

        assertEquals(Collections.singletonList(test.slice()),
                convert("getByteBufferSet", Collections.singleton(test.slice()))
                        .getBS());

        assertEquals(Arrays.asList(test.slice(), test2.slice()),
                convert("getByteBufferSet", new TreeSet<ByteBuffer>() {
                    {
                        add(test.slice());
                        add(test2.slice());
                    }
                }).getBS());
    }

    @Test
    public void testObjectSet() {
        // V2 schema no longer supports marshalling Set<Object> as a DynamoDB
        // StringSet, since there's no way to read it back out.
        try {
            convert("getObjectSet", Collections.singleton(new Object()));
            Assert.fail("Expected DynamoDBMappingException");
        } catch (final DynamoDBMappingException e) {
        }
    }

    @Test
    public void testList() {
        assertEquals(Arrays.asList(
                new AttributeValue("a"),
                new AttributeValue("b"),
                new AttributeValue("c")),
                convert("getList", Arrays.asList("a", "b", "c")).getL());

        assertEquals(Arrays.asList(new AttributeValue().withNULL(true)),
                convert("getList", Collections.<String> singletonList(null)).getL());
    }

    @Test
    public void testSetList() {
        assertEquals(
                Arrays.asList(new AttributeValue().withSS("a")),
                convert("getSetList", Arrays.asList(
                        Collections.<String> singleton("a"))).getL());

        final List<Set<String>> list = new ArrayList<Set<String>>();
        list.add(null);

        assertEquals(
                Arrays.asList(new AttributeValue().withNULL(true)),
                convert("getSetList", list).getL());
    }

    @Test
    public void testMap() {
        assertEquals(new HashMap<String, AttributeValue>() {
            {
                put("a", new AttributeValue("b"));
                put("c", new AttributeValue("d"));
                put("e", new AttributeValue("f"));
            }
        },
                convert("getMap", new HashMap<String, String>() {
                    {
                        put("a", "b");
                        put("c", "d");
                        put("e", "f");
                    }
                }).getM());

        assertEquals(Collections.singletonMap("a", new AttributeValue().withNULL(true)),
                convert("getMap", Collections.<String, String> singletonMap("a", null)).getM());
    }

    @Test
    public void testSetMap() {
        assertEquals(new HashMap<String, AttributeValue>() {
            {
                put("a", new AttributeValue().withSS("a", "b"));
            }
        },
                convert("getSetMap", new HashMap<String, Set<String>>() {
                    {
                        put("a", new TreeSet<String>(Arrays.asList("a", "b")));
                    }
                }).getM());

        assertEquals(new HashMap<String, AttributeValue>() {
            {
                put("a", new AttributeValue().withSS("a"));
                put("b", new AttributeValue().withNULL(true));
            }
        },
                convert("getSetMap", new HashMap<String, Set<String>>() {
                    {
                        put("a", new TreeSet<String>(Arrays.asList("a")));
                        put("b", null);
                    }
                }).getM());
    }

    @Test
    public void testObject() {
        assertEquals(new HashMap<String, AttributeValue>() {
            {
                put("name", new AttributeValue("name"));
                put("value", new AttributeValue().withN("123"));
            }
        },
                convert("getObject", new SubClass()).getM());
    }

    @Test
    public void testUnannotatedObject() throws Exception {
        try {
            CONVERTER.convert(UnannotatedSubClass.class.getMethod("getChild"),
                    new UnannotatedSubClass());

            Assert.fail("Expected DynamoDBMappingException");
        } catch (final DynamoDBMappingException e) {
        }
    }

    @Test
    public void testS3Link() {
        assertEquals("{\"s3\":{"
                + "\"bucket\":\"bucket\","
                + "\"key\":\"key\","
                + "\"region\":null}}",
                convert("getS3Link",
                        new S3Link(new S3ClientCache((AWSCredentialsProvider) null), "bucket", "key"))
                        .getS());
    }

    @Test
    public void getFieldModelString() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getString"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.S);
    }

    @Test
    public void getFieldModelStringSet() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getStringSet"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.SS);
    }

    @Test
    public void getFieldModelNumber() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getNumber"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.N);
    }

    @Test
    public void getFieldModelNumberSet() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getNumberSet"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.NS);
    }

    @Test
    public void getFieldModelBinary() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getBinary"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.B);
    }

    @Test
    public void getFieldModelBinarySet() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getBinarySet"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.BS);
    }

    @Test
    public void getFieldModelBoolean() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getBool"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.BOOL);
    }

    @Test
    public void getFieldModelList() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getList"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.L);
    }

    @Test
    public void getFieldModelMap() throws NoSuchMethodException, SecurityException {
        final MockClass mock = new MockClass();

        final DynamoDBMapperFieldModel model = CONVERTER.getFieldModel(mock.getClass().getMethod(
                "getMap"));
        final DynamoDBAttributeType type = model.getDynamoDBAttributeType();
        assertEquals(type, DynamoDBAttributeType.M);
    }

    private static class MockClass {

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public Set<String> getStringSet() {
            return stringSet;
        }

        public void setStringSet(Set<String> stringSet) {
            this.stringSet = stringSet;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public Set<Integer> getNumberSet() {
            return numberSet;
        }

        public void setNumberSet(Set<Integer> numberSet) {
            this.numberSet = numberSet;
        }

        public ByteBuffer getBinary() {
            return binary;
        }

        public void setBinary(ByteBuffer binary) {
            this.binary = binary;
        }

        public Set<ByteBuffer> getBinarySet() {
            return binarySet;
        }

        public void setBinarySet(Set<ByteBuffer> binarySet) {
            this.binarySet = binarySet;
        }

        public boolean getBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        String string;
        Set<String> stringSet;
        int number;
        Set<Integer> numberSet;
        ByteBuffer binary;
        Set<ByteBuffer> binarySet;
        boolean bool;
        Map<String, String> map;
        List<String> list;
    }

    private static AttributeValue convert(String method, Object value) {
        try {

            return CONVERTER.convert(TestClass.class.getMethod(method), value);

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
