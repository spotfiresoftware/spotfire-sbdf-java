/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ValueArray class.
 */
public class ValueArrayTest {

  private static TypedArray[] arrays = new TypedArray[] {
    new TypedArray(new String[] {"Flygande", "bäckasiner", "söka", "hwila", "på", "mjuka", "tuvor"}),
    new TypedArray(new String[] {"foo bar"}),
    new TypedArray(new String[] {"", ""}),
    new TypedArray(new String[0]),
    new TypedArray(new byte[][] {new byte[] {(byte) 0xff, (byte) 0x80, (byte) 0x01},
      new byte[] {(byte) 0xea, (byte) 0x7b}}),
    new TypedArray(new byte[][] {new byte[] {(byte) 0x13, (byte) 0x6f}}), new TypedArray(new byte[0][]),
    new TypedArray(new boolean[0]),
    new TypedArray(new boolean[] {false, false, true, false, false, true, true, false}),
    new TypedArray(new boolean[] {true, false, false, true, false, false, true, true, false, true}),
    new TypedArray(new int[] {1, 2, 3, 4, 5}),
    new TypedArray(new int[] {1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5, 999, 999, -1})};

  private final ValueArray[] valueArrays = new ValueArray[] {
      ValueArray.createPlainArrayEncoding(ValueType.STRING, arrays[0]),
      ValueArray.createPlainArrayEncoding(ValueType.STRING, arrays[1]),
      ValueArray.createPlainArrayEncoding(ValueType.STRING, arrays[2]),
      ValueArray.createPlainArrayEncoding(ValueType.STRING, arrays[3]),
      ValueArray.createPlainArrayEncoding(ValueType.BINARY, arrays[4]),
      ValueArray.createPlainArrayEncoding(ValueType.BINARY, arrays[5]),
      ValueArray.createPlainArrayEncoding(ValueType.BINARY, arrays[6]),
      ValueArray.createBoolArrayEncoding(arrays[7].getBoolArray(), arrays[7].getCount()),
      ValueArray.createBoolArrayEncoding(arrays[8].getBoolArray(), arrays[8].getCount()),
      ValueArray.createBoolArrayEncoding(arrays[9].getBoolArray(), arrays[9].getCount()),
      ValueArray.createPlainArrayEncoding(ValueType.INT, arrays[10]),
      ValueArray.createRleArrayEncoding(ValueType.INT, arrays[11]) };

  private final byte[][] serialized = new byte[][] {
    new byte[] {1, 10, 7, 0, 0, 0, 49, 0, 0, 0, 8, 70, 108, 121, 103, 97, 110, 100, 101, 11, 98, (byte) 0xc3,
      (byte) 0xa4, 99, 107, 97, 115, 105, 110, 101, 114, 5, 115, (byte) 0xc3, (byte) 0xb6, 107, 97, 5, 104, 119,
      105, 108, 97, 3, 112, (byte) 0xc3, (byte) 0xa5, 5, 109, 106, 117, 107, 97, 5, 116, 117, 118, 111, 114},
    new byte[] {1, 10, 1, 0, 0, 0, 8, 0, 0, 0, 7, 102, 111, 111, 32, 98, 97, 114},
    new byte[] {1, 10, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0},
    new byte[] {1, 10, 0, 0, 0, 0, 0, 0, 0, 0},
    new byte[] {1, 12, 2, 0, 0, 0, 7, 0, 0, 0, 3, (byte) 0xff, (byte) 0x80, 1, 2, (byte) 0xea, 123},
    new byte[] {1, 12, 1, 0, 0, 0, 3, 0, 0, 0, 2, 19, 111},
    new byte[] {1, 12, 0, 0, 0, 0, 0, 0, 0, 0},
    new byte[] {3, 1, 0, 0, 0, 0},
    new byte[] {3, 1, 8, 0, 0, 0, 38},
    new byte[] {3, 1, 10, 0, 0, 0, (byte) 0x93, 64},
    new byte[] {1, 2, 5, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5, 0, 0, 0},
    new byte[] {2, 2, 18, 0, 0, 0, 7, 0, 0, 0, 4, 3, 2, 1, 0, 1, 0, 7, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0,
      0, 4, 0, 0, 0, 5, 0, 0, 0, (byte) 0xe7, 3, 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}};

  @Test
  public final void testSerialization() {
    for (int i = 0; i < valueArrays.length; ++i) {

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      BinaryWriter bw = new BinaryWriter(stream);

      valueArrays[i].serialize(bw);

      byte[] rep = ((ByteArrayOutputStream) bw.getStream()).toByteArray();

      StringBuilder repr = new StringBuilder();

      for (byte b : rep) {
        if (repr.length() > 0) {
          repr.append(", ");
        }

        repr.append(b);
      }

      assertArrayEquals(serialized[i], rep);
    }
  }

  @Test
  public final void testToArray() {
    for (int i = 0; i < valueArrays.length; ++i) {
      TypedArray array = valueArrays[i].toArray();
      @SuppressWarnings("rawtypes")
      Class componentType = arrays[i].getComponentType();
      ValueTypeId id = ValueType.getValueTypeId(componentType);
      ValueType vt = ValueType.getValueTypeFromId(id);
      ArrayTest.checkArraysEqual(vt, arrays[i], array);
    }
  }

  @Test
  public final void testDeserialization() {
    for (int i = 0; i < valueArrays.length; ++i) {
      ByteArrayInputStream stream = new ByteArrayInputStream(serialized[i]);
      BinaryReader br = new BinaryReader(stream);
      ValueArray deserialized = ValueArray.deserialize(br);
      TypedArray array = deserialized.toArray();
      ArrayTest.checkArraysEqual(deserialized.getValueType(), arrays[i], array);
    }
  }

  @Test
  public final void testSkip() throws Exception {
    for (ValueArray valueArray : valueArrays) {
      int pos = 0;
      byte[] bytes = null;
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BinaryWriter bw = new BinaryWriter(outputStream);
      valueArray.serialize(bw);
      bw.flush();
      pos = outputStream.size();
      outputStream.write("hello".getBytes());
      bytes = outputStream.toByteArray();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
      BinaryReader br = new BinaryReader(inputStream);
      int total = br.getStream().available();
      ValueArray.skip(br);
      assertEquals(total - pos, br.getStream().available());
    }
  }

  private final TypedArray[] defaultArrayValues = new TypedArray[] {
    new TypedArray(new String[] {"foo", "bar"}),
    new TypedArray(new byte[][] {"foo".getBytes(), "bar".getBytes()}),
    new TypedArray(new boolean[] {true, false}), new TypedArray(new int[] {1, 2, 3, 4})};

  private final ValueType[] defaultArrayValueTypes = new ValueType[] {
    ValueType.STRING,
    ValueType.BINARY,
    ValueType.BOOL,
    ValueType.INT};

  private final ValueArray.ArrayEncodingTypeId[] defaultArrayEncodings = new ValueArray.ArrayEncodingTypeId[] {
    ValueArray.ArrayEncodingTypeId.PLAIN_ARRAY_ENCODING_TYPE_ID,
    ValueArray.ArrayEncodingTypeId.PLAIN_ARRAY_ENCODING_TYPE_ID,
    ValueArray.ArrayEncodingTypeId.BIT_ARRAY_ENCODING_TYPE_ID,
    ValueArray.ArrayEncodingTypeId.PLAIN_ARRAY_ENCODING_TYPE_ID};

  @Test
  public final void testDefaultEncoding() {
    for (int i = 0; i < defaultArrayValues.length; ++i) {
      ValueArray va = ValueArray.createDefaultArrayEncoding(defaultArrayValueTypes[i], defaultArrayValues[i]);
      assertSame(defaultArrayEncodings[i], va.getEncodingId());
    }
  }

  @Test
  public final void testRLEArray() {
    Date now = new Date();
    TypedArray[] typedArrays = new TypedArray[] {
      new TypedArray(new boolean[] {true, true}),
      new TypedArray(new int[] {-1, -1}),
      new TypedArray(new long[] {-1, -1}),
      new TypedArray(new float[] {-1, -1}),
      new TypedArray(new double[] {255, 255}),
      new TypedArray(new java.math.BigDecimal[] {new java.math.BigDecimal("3.14"), new java.math.BigDecimal("3.14")}),
      new TypedArray(new Date[] {now, now}), new TypedArray(new TimeSpan[] {new TimeSpan(1), new TimeSpan(1)}),
      new TypedArray(new String[] {"foo", "foo"}),
      new TypedArray(new int[] {1, 1, 1, 1, 2, 2, 2, 3, 3, 4}),
      new TypedArray(new String[0]),
      null,
      null};

    // build one long array
    ArrayList<Integer> list = new ArrayList<>();
    for (int i = 0; i < 256; i++) {
      list.add(i / 300);
    }
    typedArrays[typedArrays.length - 2] = new TypedArray(ValueType.getRuntimeType(ValueType.INT), list,
        ValueType.INT.getDefaultValue());

    for (int i = 256; i < 999; i++) {
      list.add(i / 300);
    }
    typedArrays[typedArrays.length - 1] = new TypedArray(ValueType.getRuntimeType(ValueType.INT), list,
        ValueType.INT.getDefaultValue());

    for (TypedArray typedArray : typedArrays) {
      ValueTypeId id;
      if (typedArray.getCount() == 0) {
        id = ValueTypeId.STRING_TYPE;
      } else {
        id = ValueType.getValueTypeId(typedArray.getComponentType());
      }
      ValueType vt = ValueType.getValueTypeFromId(id);
      TypedArray array = typedArray;
      for (int ii = 0; ii < 2; ++ii) {
        ValueArray a = ValueArray.createRleArrayEncoding(vt, array);
        ArrayTest.checkArraysEqual(vt, array, a.toArray());
        assertEquals(array.getCount(), a.getCount());

        // Convert to array of boxed objects
        ArrayList<Object> objects = new ArrayList<>();
        for (int iii = 0; iii < array.getCount(); ++iii) {
          objects.add(array.getAsObject(iii));
        }
        array = new TypedArray(ValueType.getRuntimeType(vt), objects, vt.getDefaultValue());
      }
    }

    ValueArray.createRleArrayEncoding(ValueType.INT, new TypedArray(new int[0]));
  }

  @SuppressWarnings("unchecked")
  @Test
  public final void testRobustness() {

    try {
      @SuppressWarnings("rawtypes")
      List list = new ArrayList();
      list.add(123);
      list.add(3.14);
      ValueArray.createDefaultArrayEncoding(ValueType.INT, new TypedArray(ValueType.getRuntimeType(ValueType.DOUBLE),
          list, ValueType.DOUBLE.getDefaultValue()));
      fail("ValueArray.createDefaultArrayEncoding(ValueType.INT, new TypedArray("
          + "ValueType.INT, new Object[] {123, 3.14})) should fail");
    } catch (ClassCastException e) {
      // expected
    }

    try {
      ValueArray.deserialize(new BinaryReader(new ByteArrayInputStream(new byte[] {42})));
      fail("ValueArray.deserialize(new BinaryReader(new ByteArrayInputStream(new byte[] {42}))) should fail");
    } catch (SerializationException e) {
      // expected
    }
  }

  @Test
  public final void testBitArray() {
    boolean[] b = new boolean[203];

    for (int i = 0; i < 203; ++i) {
      b[i] = ((i % 2) == 0);
    }

    ArrayTest.checkArraysEqual(ValueType.BOOL, new TypedArray(b),
        ValueArray.createBoolArrayEncoding(b, b.length).toArray());
  }
}
