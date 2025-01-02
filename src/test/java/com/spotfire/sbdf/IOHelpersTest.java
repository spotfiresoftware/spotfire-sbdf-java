/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class IOHelpersTest {

  private static final ValueType[] VALUE_TYPES = new ValueType[] {
    ValueType.BOOL,
    ValueType.BOOL,
    ValueType.INT,
    ValueType.INT,
    ValueType.INT,
    ValueType.LONG,
    ValueType.LONG,
    ValueType.LONG,
    ValueType.FLOAT,
    ValueType.FLOAT,

    ValueType.FLOAT,
    ValueType.FLOAT,
    ValueType.FLOAT,
    ValueType.DOUBLE,
    ValueType.DOUBLE,
    ValueType.DOUBLE,
    ValueType.DOUBLE,
    ValueType.DOUBLE,
    ValueType.DATETIME,
    ValueType.DATETIME,

    ValueType.DATE,
    ValueType.DATE,
    ValueType.TIME,
    ValueType.TIME,
    ValueType.TIMESPAN,
    ValueType.TIMESPAN,
    ValueType.DECIMAL,
    ValueType.DECIMAL,
    ValueType.DECIMAL,
    ValueType.DECIMAL,

    ValueType.DECIMAL,
    ValueType.STRING,
    ValueType.STRING,
    ValueType.STRING,
    ValueType.STRING,
    ValueType.BINARY,
    ValueType.BINARY};

  private static final Object[] VALUES = {
    true,
    false,
    Integer.MIN_VALUE,
    0,
    Integer.MAX_VALUE,
    Long.MIN_VALUE,
    (long) 777,
    Long.MAX_VALUE,
    Float.MAX_VALUE * (-1),
    Float.MAX_VALUE,

    Float.NaN,
    0f,
    3.14f,
    Double.MAX_VALUE * (-1),
    Double.MAX_VALUE,
    Double.NaN,
    0d,
    1.234567d,
    getDate(1993, 4, 14, 12, 34, 56),
    getDate(2009, 5, 23, 17, 43, 44),

    getDate(1993, 4, 14),
    getDate(2009, 05, 23),
    getTime(12, 34, 56),
    getTime(17, 43, 44),
    TimeSpan.ZERO,
    TimeSpan.fromDays(1),
    new java.math.BigDecimal("123.123456789"),
    new java.math.BigDecimal(0.0),
    new java.math.BigDecimal("-123.123456789"),
    new java.math.BigDecimal("1.1"),

    new java.math.BigDecimal("1.1"),
    "",
    "",
    "åäö",
    "\ufffc\ud999\udeee",
    new byte[0],
    new byte[] {0, 1, 2, 3}};

  private static Date getDate(int year, int month, int day) {
    return getDate(year, month, day, 0, 0, 0);
  }

  private static Date getTime(int hours, int minutes, int seconds) {
    return getDate(1970, 0, 1, hours, minutes, seconds);
  }

  private static Date getDate(int year, int month, int day, int hours, int minutes, int seconds) {
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    cal.clear();
    cal.set(year, month, day, hours, minutes, seconds);
    return cal.getTime();
  }

  private static final byte[][] BINARY_REP = {
    new byte[] {1},
    new byte[] {0},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00},
    new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x80},
    new byte[] {(byte) 0x09, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
      (byte) 0x7f},
    new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0x7f, (byte) 0xff},
    new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0x7f, (byte) 0x7f},

    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0xc0, (byte) 0xff},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00},
    new byte[] {(byte) 0xc3, (byte) 0xf5, (byte) 0x48, (byte) 0x40},
    new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xef,
      (byte) 0xff},
    new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xef,
      (byte) 0x7f},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xf8,
      (byte) 0xff},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x87, (byte) 0x88, (byte) 0x9b, (byte) 0x53, (byte) 0xc9, (byte) 0xc0, (byte) 0xf3,
      (byte) 0x3f},
    new byte[] {(byte) 0x80, (byte) 0xe9, (byte) 0x9e, (byte) 0xc1, (byte) 0x2e, (byte) 0x39, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x80, (byte) 0x30, (byte) 0x64, (byte) 0x20, (byte) 0xa5, (byte) 0x39, (byte) 0x00,
      (byte) 0x00},

    new byte[] {(byte) 0x00, (byte) 0xc0, (byte) 0xeb, (byte) 0xbe, (byte) 0x2e, (byte) 0x39, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x00, (byte) 0x50, (byte) 0x96, (byte) 0x1c, (byte) 0xa5, (byte) 0x39, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x80, (byte) 0x29, (byte) 0xb3, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x80, (byte) 0xe0, (byte) 0xcd, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x00, (byte) 0x5c, (byte) 0x26, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00},
    new byte[] {(byte) 0x15, (byte) 0xdb, (byte) 0xba, (byte) 0xaa, (byte) 0x1c, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2e,
      (byte) 0x30},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40,
      (byte) 0x30},
    new byte[] {(byte) 0x15, (byte) 0xdb, (byte) 0xba, (byte) 0xaa, (byte) 0x1c, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2e,
      (byte) 0xb0},
    new byte[] {(byte) 0x0b, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3e,
      (byte) 0x30},

    new byte[] {(byte) 0x0b, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3e,
      (byte) 0x30},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00},
    new byte[] {(byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xa5, (byte) 0xc3,
      (byte) 0xa4, (byte) 0xc3, (byte) 0xb6},
    new byte[] {(byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xef, (byte) 0xbf, (byte) 0xbc,
      (byte) 0xf1, (byte) 0xb6, (byte) 0x9b, (byte) 0xae},
    new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00},
    new byte[] {(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02,
      (byte) 0x03}};

  private List<Integer> getArrayLen() {
    List<Integer> counts = new ArrayList<>();

    ValueType previous = null;
    int result = 0;

    for (ValueType vt : VALUE_TYPES) {

      if (vt.isArrayType()) {
        break;
      }

      if (previous != null && !vt.equals(previous)) {
        counts.add(result);
        result = 1;
      } else {
        ++result;
      }
      previous = vt;
    }

    // add the final result, never zero (unless the VALUE_TYPES array is empty)
    counts.add(result);

    return counts;
  }

  private void addBytes(List<Byte> slice, byte[] bs) {
    for (byte b : bs) {
      slice.add(b);
    }
  }

  private byte[] toByteArray(List<Byte> list) {
    byte[] bytes = new byte[list.size()];
    for (int i = 0; i < list.size(); i++) {
      bytes[i] = list.get(i);
    }
    return bytes;
  }

  private List<Byte> getBytesList(int i) {
    try (ByteArrayOutputStream stream = new ByteArrayOutputStream(4);
      BinaryWriter writer = new BinaryWriter(stream)) {
      writer.writeInt32(i);
      List<Byte> list = new ArrayList<>(4);
      addBytes(list, stream.toByteArray());
      return list;
    } catch (IOException e) {
      throw new RuntimeException("Failed to getBytesList", e);
    }
  }

  private static void setMinus(byte[] array, int minus) {
    if (array != null) {
      if (minus == 0) {
        array[array.length - 1] &= 0x7f;
      } else {
        array[array.length - 1] |= 0x80;
      }
    }
  }

  @Test
  public void testDecimal() {
    BigDecimal[] values = {
      new BigDecimal("0", MathContext.DECIMAL128),
      new BigDecimal("0.1", MathContext.DECIMAL128),
      new BigDecimal("0.0000000000000000000000000001", MathContext.DECIMAL128),
      new BigDecimal(BigInteger.ONE, 29, MathContext.DECIMAL128), new BigDecimal("1", MathContext.DECIMAL128),
      new BigDecimal("79228162514264337593543950335", MathContext.DECIMAL128),
      new BigDecimal("0", MathContext.DECIMAL128),
      // .NET precision is limited, but we can support the full 34 bits
      new BigDecimal("284987427729500.958188778798907905", MathContext.DECIMAL128),
      new BigDecimal(0, MathContext.DECIMAL128),
      new BigDecimal("10384593717069655257060992658.440192")};

    byte[][] rep = {
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 48},
      new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0, 0, 64, 48},
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, (byte) 0xb0}, // -0
      new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 28, 48},
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x78}, // NaN
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x0d, 0x6c}};

    byte[][] spotrep = {
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 48},
      new byte[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 48},
      new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0, 0, 64, 48},
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 48}, // 0
      new byte[] { (byte) 0x66, (byte) 0xb3, (byte) 0x66, (byte) 0x80, 0, (byte) 0xe7, (byte) 0x33, (byte) 0xe7,
        (byte) 0x0, (byte) 0x81, (byte) 0x67, (byte) 0xb4, (byte) 0x67, (byte) 0x1, (byte) 0x1e, (byte) 0x30 },
      null,
      new byte[] { (byte) 0x8, (byte) 0xac, (byte) 0x1c, (byte) 0x5a, (byte) 0x64, (byte) 0x3b, (byte) 0xdf,
        (byte) 0x4f, (byte) 0x8d, (byte) 0x97, (byte) 0x6e, (byte) 0x12, (byte) 0x83, (byte) 0x0, (byte) 0x3a,
        (byte) 0x30 } };

    boolean[] exceptions = {false, false, false, false, false, false, false,
      false, true, false};

    for (int minus = 0; minus < 2; ++minus) {

      for (int i = 0; i < values.length; ++i) {

        setMinus(rep[i], minus);
        if (values[i].compareTo(BigDecimal.ZERO) != 0) {
          setMinus(spotrep[i], minus);
        }

        if (minus == 0 && values[i].compareTo(BigDecimal.ZERO) == -1 || minus == 1
            && values[i].compareTo(BigDecimal.ZERO) == 1) {
          values[i] = values[i].negate();
        }

        ByteArrayInputStream input = new ByteArrayInputStream(rep[i]);
        BinaryReader br = new BinaryReader(input);
        if (!exceptions[i]) {
          BigDecimal d = IOHelpers.readDecimal(br);
          assertEquals(0, values[i].compareTo(d), "Value " + i + " was not read correctly");
        } else {
          try {
            IOHelpers.readDecimal(br);
            fail("IOHelpers.readDecimal(br); should fail");
          } catch (NumberFormatException e) {
            // expected
          }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BinaryWriter bw = new BinaryWriter(output);
        if (!exceptions[i]) {
          IOHelpers.writeDecimal(bw, values[i]);
          assertArrayEquals(spotrep[i], output.toByteArray(), "Value " + i + " was not written correctly");
        }
      }
    }
  }

  @Test
  public final void testWriteValue() {
    for (int i = 0; i < VALUE_TYPES.length; ++i) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      BinaryWriter writer = new BinaryWriter(stream);
      IOHelpers.writeValue(writer, VALUE_TYPES[i], VALUES[i]);
      byte[] rep = stream.toByteArray();
      assertArrayEquals(BINARY_REP[i], rep, String.format("testWriteValue failed at object %s %s", i, VALUES[i]));
    }
  }

  @Test
  public final void testReadValue() {
    for (int i = 0; i < VALUE_TYPES.length; ++i) {
      ByteArrayInputStream stream = new ByteArrayInputStream(BINARY_REP[i]);
      BinaryReader reader = new BinaryReader(stream);
      ValueType vt = VALUE_TYPES[i];
      Object value = IOHelpers.readValue(reader, vt);
      if (ValueType.BINARY.equals(vt)) {
        assertArrayEquals((byte[]) VALUES[i],
          (byte[]) value,
          String.format("testReadValue failed at object %s %s", i, VALUES[i]));
      } else if (vt.isDecimal()) {
        assertEquals(0, ((BigDecimal) VALUES[i]).compareTo((BigDecimal) value), String.format("testReadValue failed at object %s %s", i, VALUES[i]));
      } else {
        assertEquals(VALUES[i], value, String.format("testReadValue failed at object %s %s", i, VALUES[i]));
      }
    }
  }

  @Test
  public final void testReadArray() {
    int ofs = 0;
    for (int len : getArrayLen()) {
      ValueType vt = VALUE_TYPES[ofs];
      for (int i = 0; i <= len; ++i) {
        ArrayList<Byte> slice = new ArrayList<>(getBytesList(i));
        // vals is an array of boxed values
        List<Object> vals = new ArrayList<>(i);

        for (int ii = 0; ii < i; ++ii) {
          addBytes(slice, BINARY_REP[ofs + ii]);
          vals.add(VALUES[ofs + ii]);
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(toByteArray(slice));
        BinaryReader reader = new BinaryReader(stream);

        TypedArray result = IOHelpers.readArray(reader, vt);
        if (vt.isDecimal()) {
          BigDecimal[] decimalArray = result.getDecimalArray();
          assertEquals(vals.size(), decimalArray.length, "Decimal arrays are not of the same length");
          for (int ii = 0; ii < vals.size(); ii++) {
            assertEquals(0, ((BigDecimal) vals.get(ii)).compareTo(decimalArray[ii]), String.format("testReadArray failed at object %s %s", ii, vals.get(ii)));
          }
        } else {
          ArrayTest.checkArraysEqual(vt, new TypedArray(ValueType.getRuntimeType(vt), vals, vt.getDefaultValue()),
              result);
        }
      }

      ofs += len;
    }
  }

  @Test
  public final void testWriteArray() {
    int ofs = 0;
    for (int len : getArrayLen()) {
      ValueType vt = VALUE_TYPES[ofs];
      for (int i = 0; i <= len; ++i) {
        ArrayList<Byte> slice = new ArrayList<>(getBytesList(i));
        List<Object> vals = new ArrayList<>();

        for (int ii = 0; ii < i; ++ii) {
          addBytes(slice, BINARY_REP[ofs + ii]);
          vals.add(VALUES[ofs + ii]);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(stream);

        IOHelpers.writeArray(writer, vt,
            new TypedArray(ValueType.getRuntimeType(vt), vals, vt.getDefaultValue()), vals.size());
        assertArrayEquals(toByteArray(slice), stream.toByteArray());
      }
      ofs += len;
    }
  }

  @Test
  public final void testSkipArray() {
    int ofs = 0;
    int count = 0;
    for (int len : getArrayLen()) {
      ValueType vt = VALUE_TYPES[ofs];
      for (int i = 0; i <= len; ++i) {
        ArrayList<Byte> slice = new ArrayList<>(getBytesList(i));

        for (int ii = 0; ii < i; ++ii) {
          addBytes(slice, BINARY_REP[ofs + ii]);
        }

        byte[] bytes = "random data".getBytes();
        addBytes(slice, bytes);

        ByteArrayInputStream stream = new ByteArrayInputStream(toByteArray(slice));
        BinaryReader reader = new BinaryReader(stream);
        IOHelpers.skipArray(reader, vt);
        assertEquals(bytes.length, stream.available(), "testSkipArray failed at " + count);
      }
      ofs += len;
      count++;
    }
  }

  @Test
  public final void testArrays() throws Exception {
    TypedArray[] arrays = new TypedArray[] {
      new TypedArray(new String[] {"hello", "sbdf", "world"}),
      new TypedArray(new byte[][] {new byte[] {(byte) 0xde, 111}, new byte[] {12, 13, 14, 15},
        new byte[] {99, 88, 77, 66, 55, 44, 33, 22, 11}})};

    ValueType[] vts = new ValueType[] {ValueType.STRING, ValueType.BINARY};
    byte[][] rep = new byte[][] {
      new byte[] {3, 0, 0, 0, 17, 0, 0, 0, 5, 104, 101, 108, 108, 111, 4, 115, 98, 100, 102, 5, 119, 111, 114,
        108, 100},
      new byte[] {3, 0, 0, 0, 18, 0, 0, 0, 2, (byte) 0xde, 111, 4, 12, 13, 14, 15, 9, 99, 88, 77, 66, 55, 44, 33,
        22, 11}};

    for (int i = 0; i < arrays.length; ++i) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BinaryWriter bw = new BinaryWriter(outputStream);
      IOHelpers.writeArray(bw, vts[i], arrays[i], arrays[i].getCount());
      assertArrayEquals(outputStream.toByteArray(), rep[i]);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
      BinaryReader br = new BinaryReader(inputStream);
      TypedArray arr = IOHelpers.readArray(br, vts[i]);
      ArrayTest.checkArraysEqual(vts[i], arrays[i], arr);

      br.getStream().reset();
      int len = br.getStream().available();
      IOHelpers.skipArray(br, vts[i]);
      assertEquals(len, rep[i].length);
      assertEquals(0, br.getStream().available());
    }

    StringBuilder test = new StringBuilder();
    List<Byte> expectedList = new ArrayList<>(Arrays
        .asList(new Byte[] {1, 0, 0, 0, 0, 1, 0, 0, (byte) 0xfe, 1}));

    for (int i = 0; i < 127; ++i) {
      test.append("Ä");
      expectedList.add((byte) 0xc3);
      expectedList.add((byte) 0x84);
    }
    String[] tests = {test.toString()};
    byte[] expected = toByteArray(expectedList);
    byte[] actual = null;

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (BinaryWriter wr = new BinaryWriter(outputStream)) {
      IOHelpers.writeArray(wr, ValueType.STRING, new TypedArray(tests), tests.length);
      actual = outputStream.toByteArray();
      assertArrayEquals(expected, actual);
    }

    ByteArrayInputStream inputStream = new ByteArrayInputStream(expected);
    BinaryReader br = new BinaryReader(inputStream);
    try {
      TypedArray arr = IOHelpers.readArray(br, ValueType.STRING);
      ArrayTest.checkArraysEqual(ValueType.STRING, new TypedArray(tests), arr);
    } finally {
      br.close();
    }

    ByteArrayInputStream ms = new ByteArrayInputStream(expected);
    BinaryReader br2 = new BinaryReader(ms);
    try {
      int before = ms.available();
      IOHelpers.skipArray(br2, ValueType.STRING);
      int after = ms.available();
      assertEquals(expected.length, (before - after));
    } finally {
      br2.close();
    }
  }

  @Test
  public final void testEmptyArray() {
    byte[] rep = {0, 0, 0, 0};
    ByteArrayOutputStream ms = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(ms);
    IOHelpers.writeArray(bw, ValueType.INT, new TypedArray(new int[0]), 0);
    assertArrayEquals(rep, ms.toByteArray());

    BinaryReader br = new BinaryReader(new ByteArrayInputStream(rep));
    TypedArray array = IOHelpers.readArray(br, ValueType.INT);
    assertEquals(Integer.TYPE, array.getComponentType());
    assertEquals(0, array.getCount());
  }

  @Test
  public final void testPackedInt() {
    int[] values = {0, 127, 16383, 16384, 2097151, 2097152, 268435455, 268435456, Integer.MAX_VALUE};
    byte[][] ser = new byte[][] {new byte[] {0}, new byte[] {127}, new byte[] {(byte) 0xff, 127},
      new byte[] {(byte) 0x80, (byte) 0x80, 1}, new byte[] {(byte) 0xff, (byte) 0xff, 127},
      new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, 1},
      new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, 127},
      new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 1},
      new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 7}};

    for (int i = 0; i < values.length; ++i) {
      assertEquals(ser[i].length, IOHelpers.getPackedIntLen(values[i]));
      byte[] blob = new byte[ser[i].length];
      int index = 0;
      index = IOHelpers.writePackedInt(blob, index, values[i]);
      assertEquals(ser[i].length, index);
      assertArrayEquals(ser[i], blob);

      index = 0;
      int v = IOHelpers.readPackedInt(blob, index);
      assertEquals(ser[i].length, IOHelpers.getPackedIntLen(v));
      assertEquals(values[i], v);
    }
  }

  @Test
  public final void testShanghai() {

    String shanghai = "\u4E0A\u6D77";
    ByteArrayOutputStream ms = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(ms);

    IOHelpers.writeString(bw, shanghai);

    byte[] bytes = ms.toByteArray();

    BinaryReader br = new BinaryReader(new ByteArrayInputStream(bytes));
    String s = IOHelpers.readString(br);
    assertEquals(shanghai, s);

  }

  @Test
  public final void testEarlyDatesWriter() {

    ByteArrayOutputStream ms = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(ms);

    // SBDF does not support dates prior to 12:00 AM GTM on 1st Jan 1583
    Date startDate = getDate(1583, 0, 1);
    IOHelpers.writeDateTime(bw, startDate);

    Date badDate = getDate(1582, 11, 31);
    try {
      IOHelpers.writeDateTime(bw, badDate);
      fail("Writing 31st Dec 1582 should fail");
    } catch (SerializationException e) {
      // expected
    }

    Date veryBadDate = getDate(1, 0, 1);
    try {
      IOHelpers.writeDateTime(bw, veryBadDate);
      fail("Writing 1st Jan 0001 should fail");
    } catch (SerializationException e) {
      // expected
    }

    byte[] bytes = ms.toByteArray();
    BinaryReader br = new BinaryReader(new ByteArrayInputStream(bytes));
    Date d = IOHelpers.readDateTime(br);
    assertEquals(startDate, d);

  }

  @Test
  public final void testEarlyDatesBuffer() {

    byte[] bytes = new byte[100];

    // SBDF does not support dates prior to 12:00 AM GTM on 1st Jan 1583
    Date startDate = getDate(1583, 0, 1);
    int pos = IOHelpers.writeDateTime(bytes, 0, startDate);

    Date badDate = getDate(1582, 11, 31);
    try {
      pos = IOHelpers.writeDateTime(bytes, pos, badDate);
      fail("Writing 31st Dec 1582 should fail");
    } catch (SerializationException e) {
      // expected
    }

    Date veryBadDate = getDate(1, 0, 1);
    try {
      pos = IOHelpers.writeDateTime(bytes, pos, veryBadDate);
      fail("Writing 1st Jan 0001 should fail");
    } catch (SerializationException e) {
      // expected
    }

    BinaryReader br = new BinaryReader(new ByteArrayInputStream(bytes));
    Date d = IOHelpers.readDateTime(br);
    assertEquals(startDate, d);

  }
}