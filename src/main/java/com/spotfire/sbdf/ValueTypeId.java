/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.HashMap;

/**
 * Type identifiers for value types in Sbdf.
 */
public enum ValueTypeId {

  /**
   * An unknown value type.
   */
  UNKNOWN_TYPE(0x00),

  /*
   * Simple Types
   */

  /**
   * The Boolean value type.
   */
  BOOL_TYPE(0x01),

  /**
   * The Integer value type.
   */
  INT_TYPE(0x02),

  /**
   * The Long integer value type.
   */
  LONG_TYPE(0x03),

  /**
   * The Float value type.
   */
  FLOAT_TYPE(0x04),

  /**
   * The Double value type.
   */
  DOUBLE_TYPE(0x05),

  /**
   * The DateTime value type.
   */
  DATETIME_TYPE(0x06),

  /**
   * The Date value type.
   */
  DATE_TYPE(0x07),

  /**
   * The Time value type.
   */
  TIME_TYPE(0x08),

  /**
   * The TimeSpan value type.
   */
  TIMESPAN_TYPE(0x09),

  /*
   * Array Types
   */

  /**
   * The String value type.
   */
  STRING_TYPE(0x0a),

  // commenting this out for future use
  //  /**
  //   * The Text value type.
  //   */
  //  TEXT_TYPE(0x0b),

  /**
   * The Binary value type.
   */
  BINARY_TYPE(0x0c),

  /*
   * Complex Types
   */

  /**
   * A Decimal value type.
   */
  DECIMAL_TYPE(0x0d),

  /**
   * A user-defined value type.
   */
  USER_DEFINED_TYPE(0xFF);

  private int intValue;
  private static HashMap<Integer, ValueTypeId> mappings;

  /**
   * Map to cache the mapping between enum constants and the values used to
   * represent them.
   * 
   * @return the mapping
   */
  private static synchronized HashMap<Integer, ValueTypeId> getMappings() {
    if (mappings == null) {
      mappings = new HashMap<Integer, ValueTypeId>();
    }
    return mappings;
  }

  /**
   * Private constructor.
   * 
   * @param value the value used to represent this constant.
   */
  ValueTypeId(int value) {
    intValue = value;
    ValueTypeId.getMappings().put(value, this);
  }

  /**
   * Returns the integer value used to represent this constant.
   * 
   * @return the integer value used to represent this constant.
   */
  public int getValue() {
    return intValue;
  }

  /**
   * Returns the enum constant that is represented by this value.
   * 
   * @param value the value
   * @return the constant, or null if the value is not valid
   */
  public static ValueTypeId forValue(int value) {
    return getMappings().get(value);
  }
}
