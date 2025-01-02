/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ValueType class and the ValueTypeId enum.
 */
public class ValueTypeTest {

  private static final ValueType[] VALUE_TYPES = new ValueType[] {ValueType.BOOL, ValueType.INT, ValueType.LONG,
    ValueType.FLOAT, ValueType.DOUBLE, ValueType.DATETIME, ValueType.DATE, ValueType.TIME, ValueType.TIMESPAN,
    ValueType.STRING, ValueType.BINARY, ValueType.DECIMAL,
    new ValueType(ValueTypeId.USER_DEFINED_TYPE), new ValueType(ValueTypeId.UNKNOWN_TYPE)};

  private static final ValueTypeId[] TYPE_IDS = new ValueTypeId[] {ValueTypeId.BOOL_TYPE, ValueTypeId.INT_TYPE,
    ValueTypeId.LONG_TYPE, ValueTypeId.FLOAT_TYPE, ValueTypeId.DOUBLE_TYPE, ValueTypeId.DATETIME_TYPE,
    ValueTypeId.DATE_TYPE, ValueTypeId.TIME_TYPE, ValueTypeId.TIMESPAN_TYPE, ValueTypeId.STRING_TYPE,
    ValueTypeId.BINARY_TYPE, ValueTypeId.DECIMAL_TYPE, ValueTypeId.USER_DEFINED_TYPE, ValueTypeId.UNKNOWN_TYPE};

  private static final String[] STRING_REP = new String[] {"BOOL", "INT", "LONG", "FLOAT", "DOUBLE", "DATETIME",
    "DATE", "TIME", "TIMESPAN", "STRING", "BINARY", "DECIMAL", "USER_DEFINED", "UNKNOWN"};

  private static final byte[][] BINARY_REP = new byte[][] {new byte[] {1}, new byte[] {2}, new byte[] {3},
    new byte[] {4}, new byte[] {5}, new byte[] {6}, new byte[] {7}, new byte[] {8}, new byte[] {9},
    new byte[] {10}, new byte[] {12}, new byte[] {13}, new byte[] {(byte) 255}, new byte[] {0}};

  private static final boolean[] SIMPLE = new boolean[] {true, true, true, true, true, true, true, true, true,
    false, false, true, false, false};

  private static final boolean[] ARRAY = new boolean[] {false, false, false, false, false, false, false, false,
    false, true, true, false, false, false};

  private static final boolean[] COMPLEX = new boolean[] {false, false, false, false, false, false, false, false,
    false, false, false, false, true, false};

  // private static final boolean[] supported = new boolean[] {false, true, false, false, true, true, true, true,
  // false,
  // true, false, true, true, true, false, false};

  private static final Object[] VALUES = new Object[] {true, 894954894, Long.MAX_VALUE, (float) 3.14,
    2.46, new java.util.Date(), new java.util.Date(), new java.util.Date(), TimeSpan.fromDays(22),
    "hello", new byte[] {1, 2, 3, 4}, new java.math.BigDecimal("1.25"), null, null};

  @SuppressWarnings("rawtypes")
  private static final java.lang.Class[] JAVA_TYPE = new java.lang.Class[] {Boolean.TYPE, Integer.TYPE, Long.TYPE,
    Float.TYPE, Double.TYPE, java.util.Date.class, java.util.Date.class, java.util.Date.class, TimeSpan.class,
    String.class, byte[].class, java.math.BigDecimal.class, null, null};

  @Test
  public final void testBasicProperties() {
    for (int i = 0; i < VALUE_TYPES.length; ++i) {
      ValueType vt = VALUE_TYPES[i];
      ValueTypeId vtid = vt.getTypeId();
      @SuppressWarnings("rawtypes")
      java.lang.Class t = JAVA_TYPE[i];

      assertEquals(TYPE_IDS[i], vtid);
      assertEquals(STRING_REP[i], vt.getString());
      if (t != null) {
        String sr = STRING_REP[i];
        assertEquals(vt, ValueType.getValueType(sr));

        if (vtid != ValueTypeId.DATE_TYPE && vtid != ValueTypeId.TIME_TYPE) {
          assertEquals(vtid, ValueType.getValueTypeId(t));
        }
      }

      assertArrayEquals(BINARY_REP[i], ValueType.convertValueTypeToBinary(VALUE_TYPES[i]));

      if (t != null) {
        assertEquals(vt, ValueType.fromBinary(BINARY_REP[i]));
      }

      if (TYPE_IDS[i] != ValueTypeId.DECIMAL_TYPE && t != null) {
        assertEquals(vt, ValueType.getValueTypeFromId(TYPE_IDS[i]));
      }

      assertEquals(SIMPLE[i], vt.isSimpleType());
      assertEquals(SIMPLE[i], ValueType.isSimpleTypeId(vtid));

      assertEquals(ARRAY[i], vt.isArrayType());
      assertEquals(ARRAY[i], ValueType.isArrayTypeId(vtid));

      assertEquals(COMPLEX[i], vt.isComplexType());
      assertEquals(COMPLEX[i], ValueType.isComplexTypeId(vtid));

      if (t != null) {
        assertEquals(JAVA_TYPE[i], ValueType.getRuntimeType(vt));
        ValueType vt2 = ValueType.fromRuntimeType(JAVA_TYPE[i]);

        if (JAVA_TYPE[i].equals(java.util.Date.class)) {
          assertEquals(ValueType.DATETIME, vt2);
        } else {
          assertEquals(vt, vt2);
        }
      }
    }
  }

  @Test
  public final void testDefaults() {
    assertEquals(false, ValueType.BOOL.getDefaultValue());
    assertEquals(0, ValueType.INT.getDefaultValue());
    assertEquals(0L, ValueType.LONG.getDefaultValue());
    assertEquals(0F, ValueType.FLOAT.getDefaultValue());
    assertEquals(0D, ValueType.DOUBLE.getDefaultValue());
    assertEquals(new Date(ValueType.START_DATE), ValueType.DATETIME.getDefaultValue());
    assertEquals(new Date(ValueType.START_DATE), ValueType.DATE.getDefaultValue());
    assertEquals(new Date(0), ValueType.TIME.getDefaultValue());
    assertEquals(new TimeSpan(0), ValueType.TIMESPAN.getDefaultValue());
    assertEquals("", ValueType.STRING.getDefaultValue());
    assertArrayEquals(new byte[0], (byte[]) ValueType.BINARY.getDefaultValue());
    assertEquals(BigDecimal.ZERO, ValueType.DECIMAL.getDefaultValue());
  }

  @Test
  public final void testEmpty() {
    for (ValueType vt : VALUE_TYPES) {
      Object o = vt.getInvalidValue();
      assertTrue(vt.isInvalidValue(o));
      assertEquals(o, vt.getInvalidValue());
      assertEquals(o.hashCode(), vt.getInvalidValue().hashCode());
    }
  }

  @Test
  public final void testError() {
    for (ValueType vt : VALUE_TYPES) {
      // object o = vt.ErrorValue();
      // IsTrue(vt.IsErrorValue(o));
      // AreEqual(string.Empty, vt.GetErrorString(o));
      // AreNotEqual(vt.ErrorValue("error"), o);

      Object o = vt.errorValue("error");
      assertTrue(vt.isErrorValue(o));
      assertEquals(vt.errorValue("error"), o);
      assertEquals("error", vt.getErrorString(o));
      Object o2 = vt.errorValue("error");
      assertEquals(o, o2);
      assertEquals(o.hashCode(), o2.hashCode());
    }
  }

  @Test
  public final void testReplaced() {
    for (int i = 0; i < VALUE_TYPES.length; ++i) {
      ValueType vt = VALUE_TYPES[i];
      if (VALUES[i] != null) {
        Object o = vt.replacedValue(VALUES[i]);
        assertTrue(vt.isReplacedValue(o));
        assertEquals(VALUES[i], vt.getReplacedValue(o));
        assertEquals(o, vt.replacedValue(VALUES[i]));
        assertEquals(o.hashCode(), vt.replacedValue(VALUES[i]).hashCode());
      }
    }
  }

  // private delegate void Tester();

  @Test
  public final void testEqual() {
    assertEquals(ValueType.BOOL, new ValueType(ValueTypeId.BOOL_TYPE));
    assertEquals(ValueType.BOOL.hashCode(), new ValueType(ValueTypeId.BOOL_TYPE).hashCode());

    assertEquals(ValueType.DECIMAL, new ValueType(ValueTypeId.DECIMAL_TYPE));
    assertEquals(ValueType.DECIMAL.hashCode(), new ValueType(ValueTypeId.DECIMAL_TYPE).hashCode());

  }

  @Test
  public final void testNotEqual() {
    assertNotSame(ValueType.BOOL, ValueType.STRING);
  }

  @Test
  public final void testMiscProperties() {
    assertTrue(ValueType.DECIMAL.isDecimal());
    assertFalse(ValueType.DATETIME.isDecimal());
    assertFalse(ValueType.BOOL.isUserDefinedType());
  }

  @Test
  public final void testObjectsEqual() {
    assertTrue(ValueType.objectsEqual(new int[] {1, 2, 3}, new int[] {1, 2, 3}));
    assertFalse(ValueType.objectsEqual(new int[] {1, 2, 3}, new long[] {1, 2, 3}));
    assertTrue(ValueType.objectsEqual(1, 1));
    assertFalse(ValueType.objectsEqual(1, 2));
  }

  @Test
  public final void testRobustness() {
    try {
      ValueType.validateAssignment(ValueType.BOOL, 123);
      fail("validateAssignment(ValueType.BOOL, 123) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueType("foo bar");
      fail("getValueType(\"foo bar\") should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueType("decimal(19,22)");
      fail("getValueType(\"decimal(19,22)\") should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueType("decimal(0,0)");
      fail("getValueType(\"decimal(0,0)\") should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueType("decimal(1,1");
      fail("getValueType(\"decimal(1,1)\") should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueType("decimal(1,1,1)");
      fail("getValueType(\"decimal(1,1,1)\") should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueType("decimal(x,y)");
      fail("getValueType(\"decimal(x,y)\") should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getRuntimeType(new ValueType(ValueTypeId.USER_DEFINED_TYPE));
      fail("getRuntimeType(new ValueType(ValueTypeId.USER_DEFINED_TYPE)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getRuntimeType(new ValueType(ValueTypeId.UNKNOWN_TYPE));
      fail("getRuntimeType(new ValueType(ValueTypeId.UNKNOWN_TYPE)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.getValueTypeFromId(ValueTypeId.USER_DEFINED_TYPE);
      fail("getValueTypeFromId(ValueTypeId.USER_DEFINED_TYPE) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.fromBinary(new byte[] {(byte) 254});
      fail("fromBinary(new byte[] {(byte) 254}) should fail");
    } catch (InvalidOperationException e) {
      // expected
    }
    try {
      ValueType.fromBinary(new byte[] {(byte) 255});
      fail("fromBinary(new byte[] {(byte) 255}) should fail");
    } catch (InvalidOperationException e) {
      // expected
    }
    try {
      ValueType.fromBinary(new byte[] {32, 99, 99});
      fail("fromBinary(new byte[] {32, 99, 99}) should fail");
    } catch (InvalidOperationException e) {
      // expected
    }
    try {
      ValueType.getValueTypeFromId(ValueTypeId.UNKNOWN_TYPE);
      fail("getValueTypeFromId(ValueTypeId.UNKNOWN_TYPE) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.INT.getErrorString(Integer.valueOf(13));
      fail("INT.getErrorString(Integer.valueOf(13)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ValueType.INT.getReplacedValue(Integer.valueOf(13));
      fail("INT.getReplacedValue(Integer.valueOf(13)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    assertEquals(ValueTypeId.UNKNOWN_TYPE, ValueType.fromRuntimeType(this.getClass()).getTypeId());

    // null should be ignored
    ValueType.validateAssignment(ValueType.INT, null);
  }
}
