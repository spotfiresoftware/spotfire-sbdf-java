/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Utility class for checking arrays.
 *
 */
public final class ArrayTest {
  
  /**
   * Private constructor.
   */
  private ArrayTest() {
    
  }

  /**
   * Checks that the 'actual' object is an array containing the same values as expected.
   * 
   * @param valueType the expected value type for the array
   * @param expected an array of the expected values
   * @param actual an object that should be an array of boolean values
   */
  public static void checkArraysEqual(ValueType valueType, TypedArray expected, TypedArray actual) {
    if (expected == null) {
      assertNull(actual, "actual is not null when it should be");
      return;
    }
    assertNotNull(actual);
    int expectedCount = expected.getCount();
    int actualCount = actual.getCount();
    assertEquals(expectedCount, actualCount, "actual array is not of correct length");
    switch (valueType.getTypeId()) {
      case BOOL_TYPE:
        checkArrays(expected.getBoolArray(), actual.getBoolArray());
        break;
      case BINARY_TYPE:
        assertArrayEquals(expected.getBinaryArray(), actual.getBinaryArray());
        break;
      case DATE_TYPE:
      case DATETIME_TYPE:
      case TIME_TYPE:
        assertArrayEquals(expected.getDateArray(), actual.getDateArray());
        break;
      case DECIMAL_TYPE:
        assertArrayEquals(expected.getDecimalArray(), actual.getDecimalArray());
        break;
      case DOUBLE_TYPE:
        checkArrays(expected.getDoubleArray(), actual.getDoubleArray());
        break;
      case FLOAT_TYPE:
        checkArrays(expected.getFloatArray(), actual.getFloatArray());
        break;
      case INT_TYPE:
        assertArrayEquals(expected.getIntArray(), actual.getIntArray());
        break;
      case LONG_TYPE:
        assertArrayEquals(expected.getLongArray(), actual.getLongArray());
        break;
      case STRING_TYPE:
        assertArrayEquals(expected.getStringArray(), actual.getStringArray());
        break;
      case TIMESPAN_TYPE:
        assertArrayEquals(expected.getTimeSpanArray(), actual.getTimeSpanArray());
        break;
      default:
        fail("unimplemented check");
    }
  }

  /**
   * Checks that the 'actual' object is an array of booleans with the expected values.
   * 
   * @param expected an array of the expected values
   * @param actual an array of the actual values
   */
  private static void checkArrays(boolean[] expected, boolean[] actual) {
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i], String
              .format("actual array differs from expected at index %d, expected %s actual %s", i, expected[i], actual[i]));
    }
  }

  /**
   * Checks that the 'actual' object is an array of doubles with the expected values.
   * 
   * @param expected an array of the expected values
   * @param actual an array of the actual values
   */
  private static void checkArrays(double[] expected, double[] actual) {
    for (int i = 0; i < expected.length; i++) {
      assertEquals(0, Double.compare(expected[i], actual[i]), String
              .format("actual array differs from expected at index %d, expected %s actual %s", i, expected[i], actual[i]));
    }
  }

  /**
   * Checks that the 'actual' object is an array of floats with the expected values.
   * 
   * @param expected an array of the expected values
   * @param actual an array of the actual values
   */
  private static void checkArrays(float[] expected, float[] actual) {
    for (int i = 0; i < expected.length; i++) {
      assertEquals(0, Float.compare(expected[i], actual[i]), String
              .format("actual array differs from expected at index %d, expected %s actual %s", i, expected[i], actual[i]));
    }
  }

}
