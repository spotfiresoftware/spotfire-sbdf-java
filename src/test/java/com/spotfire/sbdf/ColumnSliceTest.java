/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ColumnSlice class.
 */
public class ColumnSliceTest {

  private static TypedArray array = new TypedArray(new int[] {4, 8, 15, 16, 23, 42});
  private static TypedArray illegalValues = new TypedArray(new boolean[] {true, false, false, false, true, false});
  private static TypedArray replacedValues = new TypedArray(new boolean[] {false, true, true, true, false, true});
  private static TypedArray errorCodes = new TypedArray(new String[] {"an error", "", "", "", "", ""});

  private ColumnSliceBuilder builder() {
    return new ColumnSliceBuilder(ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
  }

  private ValueArray array() {
    return ValueArray.createDefaultArrayEncoding(ValueType.INT, array);
  }

  private ValueArray illegalValues() {
    return ValueArray.createDefaultArrayEncoding(ValueType.BOOL, illegalValues);
  }

  private ValueArray replacedValues() {
    return ValueArray.createDefaultArrayEncoding(ValueType.BOOL, replacedValues);
  }

  private ValueArray errorCodes() {
    return ValueArray.createDefaultArrayEncoding(ValueType.STRING, errorCodes);
  }

  /**
   *  Verifies that the constructors validate their arguments correctly.
   */
  @Test
  public final void testBasicProperties() {
    ColumnSliceBuilder csb = builder();

    assertEquals(array.getCount(), csb.getRowCount());
    ValueArray va = csb.getValues();

    assertArrayEquals(array.getIntArray(), va.toArray().getIntArray());
  }

  @Test
  public final void testIllegalValues() {
    ColumnSliceBuilder csb = builder();
    assertNull(csb.build().getInvalidValues());

    csb.setInvalidValues(illegalValues());

    ValueArray va = csb.getValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue());
    assertNotNull(va);
    ArrayTest.checkArraysEqual(ValueType.BOOL, illegalValues, va.toArray());

    ColumnSlice cs = csb.build();

    ArrayTest.checkArraysEqual(ValueType.BOOL, illegalValues, cs.getInvalidValues().toArray());

    ValueArray va2 = cs.getValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue());
    assertNotNull(va2);
    ArrayTest.checkArraysEqual(ValueType.BOOL, illegalValues, va2.toArray());
  }

  @Test
  public final void testReplacedValues() {
    ColumnSliceBuilder csb = builder();
    assertNull(csb.build().getReplacedValues());

    csb.setReplacedValues(replacedValues());

    ValueArray va = csb.getValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue());
    assertNotNull(va);
    ArrayTest.checkArraysEqual(ValueType.BOOL, replacedValues, va.toArray());

    ColumnSlice cs = csb.build();

    ArrayTest.checkArraysEqual(ValueType.BOOL, replacedValues, cs.getReplacedValues().toArray());

    ValueArray va2 = cs.getValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue());
    assertNotNull(va2);
    ArrayTest.checkArraysEqual(ValueType.BOOL, replacedValues, va2.toArray());
  }

  @Test
  public final void testErrorCodes() {
    ColumnSliceBuilder csb = builder();
    assertNull(csb.build().getErrorCodes());

    csb.setErrorCodes(errorCodes());

    ValueArray va = csb.getValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue());
    assertNotNull(va);
    ArrayTest.checkArraysEqual(ValueType.STRING, errorCodes, va.toArray());

    ColumnSlice cs = csb.build();

    ArrayTest.checkArraysEqual(ValueType.STRING, errorCodes, cs.getErrorCodes().toArray());

    ValueArray va2 = cs.getValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue());
    assertNotNull(va2);
    ArrayTest.checkArraysEqual(ValueType.STRING, errorCodes, va2.toArray());
  }

  private static TypedArray ints(int end) {
    int[] result = new int[end];
    for (int i = 0; i < end; ++i) {
      result[i] = i;
    }

    return new TypedArray(result);
  }

  @Test
  public final void testCustomProperty() {
    ColumnSliceBuilder csb = builder();
    ValueArray va = csb.getValueProperty("foo");
    assertNull(va);

    va = ValueArray.createDefaultArrayEncoding(ints(csb.getRowCount()));

    csb.setValueProperty("foo", va);

    ValueArray va2 = csb.getValueProperty("foo");
    assertNotNull(va2);
    ArrayTest.checkArraysEqual(va.getValueType(), va.toArray(), va2.toArray());

    ColumnSlice cs = csb.build();

    ValueArray va3 = null;
    va3 = cs.getValueProperty("foo");
    assertNotNull(va3);
    ArrayTest.checkArraysEqual(va.getValueType(), va.toArray(), va3.toArray());
  }

  @Test
  public final void testSerialization() {
    byte[] rep = null;
    ColumnSliceBuilder csb = builder();
    csb.setInvalidValues(illegalValues());
    csb.setReplacedValues(replacedValues());
    csb.setErrorCodes(errorCodes());
    ColumnSlice cs = csb.build();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryWriter writer = new BinaryWriter(outputStream);
    cs.write(writer);
    rep = outputStream.toByteArray();

    ColumnSlice cs2 = null;

    ByteArrayInputStream inputStream = new ByteArrayInputStream(rep);
    BinaryReader reader = new BinaryReader(inputStream);

    cs2 = ColumnSlice.read(reader);

    ValueArray values = cs.getValues();
    ArrayTest.checkArraysEqual(values.getValueType(), values.toArray(), cs2.getValues().toArray());
    ValueArray invalidValues = cs.getInvalidValues();
    ArrayTest.checkArraysEqual(invalidValues.getValueType(),
        invalidValues.toArray(), cs2.getInvalidValues().toArray());

    ValueArray replaced = cs.getReplacedValues();
    ArrayTest.checkArraysEqual(replaced.getValueType(),
        replaced.toArray(), cs2.getReplacedValues().toArray());

    ValueArray errors = cs.getErrorCodes();
    ArrayTest.checkArraysEqual(errors.getValueType(),
        errors.toArray(), cs2.getErrorCodes().toArray());

    outputStream.reset();
    outputStream.write(rep, 0, rep.length);
    outputStream.write(rep, 0, rep.length);
    reader = new BinaryReader(new ByteArrayInputStream(outputStream.toByteArray()));
    ColumnSlice.skip(reader);
    assertEquals(((ByteArrayInputStream) reader.getStream()).available(), rep.length);
  }

  @Test
  public final void testRobustness() {
    try {
      new ColumnSliceBuilder(null);
      fail("ColumnSliceBuilder(null) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setInvalidValues(ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
      fail(
          "Builder().setInvalidValues(ValueArray.createDefaultArrayEncoding(ValueType.INT, array)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setReplacedValues(ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
      fail(
          "Builder().setReplacedValues(ValueArray.createDefaultArrayEncoding(ValueType.INT, array)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setErrorCodes(ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
      fail(
          "Builder().setErrorCodes(ValueArray.createDefaultArrayEncoding(ValueType.INT, array)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setInvalidValues(ValueArray.createDefaultArrayEncoding(ValueType.BOOL,
          new TypedArray(new boolean[] {true, true})));
      fail(
          "Builder().setInvalidValues(ValueArray.createDefaultArrayEncoding(ValueType.BOOL,"
          + " new TypedArray(new boolean[] {true, true}))) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setReplacedValues(ValueArray.createDefaultArrayEncoding(ValueType.BOOL,
          new TypedArray(new boolean[] {true, true})));
      fail(
          "Builder().setReplacedValues(ValueArray.createDefaultArrayEncoding(ValueType.BOOL,"
          + " new TypedArray(new boolean[] {true, true}))) should fail");
    }  catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setErrorCodes(ValueArray.createDefaultArrayEncoding(ValueType.STRING,
          new TypedArray(new String[] {"foo", "bar"})));
      fail(
          "Builder().setErrorCodes(ValueArray.createDefaultArrayEncoding(ValueType.STRING,"
          + " new TypedArray(new String[] {\"foo\", \"bar\"}))) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setInvalidValues(null);
      fail("Builder().setInvalidValues(null) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setReplacedValues(null);
      fail("Builder().setIReplacedValues(null) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setErrorCodes(null);
      fail("Builder().setErrorCodes(null) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue(),
          ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
      fail("Builder().setValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue(),"
          + " ValueArray.createDefaultArrayEncoding(ValueType.INT, array)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue(),
          ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
      fail("Builder().setValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue(),"
          + " ValueArray.createDefaultArrayEncoding(ValueType.INT, array)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue(),
          ValueArray.createDefaultArrayEncoding(ValueType.INT, array));
      fail(" should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue(),
          ValueArray.createDefaultArrayEncoding(ValueType.BOOL,
              new TypedArray(new boolean[] {true, true})));
      fail("Builder().setValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue(),"
          + " ValueArray.createDefaultArrayEncoding(ValueType.BOOL,"
          + " new typedArray(new boolean[] {true, true}))) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue(),
          ValueArray.createDefaultArrayEncoding(ValueType.BOOL, new TypedArray(new boolean[] {true, true})));
      fail("Builder().setValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue(),"
          + " ValueArray.createDefaultArrayEncoding(ValueType.BOOL,"
          + " new TypedArray(new boolean[] {true, true}))) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue(),
          ValueArray.createDefaultArrayEncoding(ValueType.STRING, new TypedArray(new String[] {"foo", "bar"})));
      fail("Builder().setValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue(),"
          + " ValueArray.createDefaultArrayEncoding(ValueType.STRING,"
          + " new TypedArray(new String[] {\"foo\", \"bar\"}))) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty("", array());
      fail("Builder().setValueProperty(\"\", Array()) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty(null, array());
      fail("Builder().setValueProperty(null, Array()) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      builder().setValueProperty("foo", null);
      fail("Builder().setValueProperty(\"foo\", null) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
