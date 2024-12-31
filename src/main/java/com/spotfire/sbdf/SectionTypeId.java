/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.HashMap;

/**
 * Type identifiers for SBDF sections and subsections.
 */
public enum SectionTypeId {
  /**
   * An unknown section type.
   */
  UNKNOWN(0x00),

  /**
   * A file header section.
   */
  FILE_HEADER(0x01),

  /**
   * A table metadata section, marking the beginning of a complete table.
   */
  TABLE_METADATA(0x02),

  /**
   * A table slice section.
   */
  TABLE_SLICE(0x03),

  /**
   * A column slice section.
   */
  COLUMN_SLICE(0x04),

  /**
   * Marks the end of a complete data table.
   */
  TABLE_END(0x05);

  private int intValue;
  private static HashMap<Integer, SectionTypeId> mappings;

  /**
   * Map to cache the mapping between enum constants and the values used to
   * represent them.
   * 
   * @return the mapping
   */
  private static synchronized HashMap<Integer, SectionTypeId> getMappings() {
    if (mappings == null) {
      mappings = new HashMap<Integer, SectionTypeId>();
    }
    return mappings;
  }

  /**
   * Private constructor.
   * 
   * @param value the value used to represent this constant.
   */
  SectionTypeId(int value) {
    intValue = value;
    SectionTypeId.getMappings().put(value, this);
  }

  /**
   * Returns the integer value used to represent this constant.
   * 
   * @return the integer value used to represent this constant.
   */
  int getValue() {
    return intValue;
  }

  /**
   * Returns the enum constant that is represented by this value.
   * 
   * @param value the value
   * @return the constant, or null
   */
  static SectionTypeId forValue(int value) {
    return getMappings().get(value);
  }

  /**
   * Indicates whether or not the given value represents a known section type.
   * 
   * @param type the value for the type
   * @return true if the value represents an unknown type
   */
  static boolean isUnknownSectionTypeId(int type) {
    return (type < SectionTypeId.FILE_HEADER.intValue || type > SectionTypeId.TABLE_END.intValue);
  }
}
