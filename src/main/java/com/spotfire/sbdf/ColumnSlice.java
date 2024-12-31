/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;



/**
 * Represents a read-only block of data in a column.
 */
public final class ColumnSlice {
  /**
   * The valid values in this column slice. The representation of invalid values in the value array is
   * undefined/arbitrary, and the IS_INVALID and ERROR_CODE properties must be used to distinguish valid values from
   * invalid.
   */
  private ValueArray values;

  /**
   * The values in a column can have associated properties. There are two standard properties, IS_INVALID
   * and ERROR_CODE. The former is of type Boolean while the latter is of type string. If IS_INVALID is true,
   * it means there is no value available for that particular row. If the ERROR_CODE is non-empty there
   * is something wrong with the value, but the interpretation of the ERROR_CODE is application specific.
   * If IS_INVALID is true and the ERROR_CODE is non-empty, the ERROR_CODE most likely describes why the value
   * is missing.
   * If custom value properties are used, the should follow the standard Spotfire naming conventions for
   * custom properties. That is, the property name should have a prefix, like this: "Acme.Property". Property names
   * without prefixes are reserved for future use as standard properties.
   */
  private LinkedHashMap<String, ValueArray> valueProperties;

  /**
   * Initializes a new instance of the ColumnSlice class.
   * 
   * @param values The valid values.
   * @param valueProperties The dictionary of value properties.
   */
  ColumnSlice(ValueArray values, Map<String, ValueArray> valueProperties) {

    Robustness.validateArgumentNotNull("values", values);
    Robustness.validateArgumentNotNull("valueProperties", valueProperties);

    this.values = values;
    this.valueProperties = new LinkedHashMap<String, ValueArray>(valueProperties);
  }

  /**
   * Initializes a new instance of the ColumnSlice class by reading the contents from stream.
   * 
   * @param reader The BinaryReader from which the input is read.
   */
  private ColumnSlice(BinaryReader reader) {
    
    SectionHeader.readSectionType(reader, SectionTypeId.COLUMN_SLICE);

    values = ValueArray.deserialize(reader);

    int props = reader.readInt32();

    valueProperties = new LinkedHashMap<String, ValueArray>(props);

    while (props-- > 0) {
      String key = IOHelpers.readString(reader);
      ValueArray value = ValueArray.deserialize(reader);
      valueProperties.put(key, value);
    }
  }

  /**
   * Gets the total number of rows in this ColumnSlice.
   * 
   * @return the number of rows in this ColumnSlice.
   */
  public int getRowCount() {
    return values.getCount();
  }

  /**
   * Gets the values in this ColumnSlice.
   * 
   * @return the values in this ColumnSlice.
   */
  public ValueArray getValues() {
    return values;
  }

  /**
   * Gets the invalid values. Returns a ValueArray of ValueType.BOOL or null if there are no invalid values.
   * 
   * @return the invalid values in this ColumnSlice.
   */
  public ValueArray getInvalidValues() {
    ValueArray va = getValueProperty(ValueProperty.IS_INVALID.getValue());
    if (va != null && va.getValueType().equals(ValueType.BOOL)) {
      return va;
    } else {
      return null;
    }
  }

  /**
   * Gets the replaced values. Returns a ValueArray of ValueType.BOOL or null if there are no replaced values.
   * 
   * @return the replaced values in this ColumnSlice.
   */
  public ValueArray getReplacedValues() {
    ValueArray va = getValueProperty(ValueProperty.HAS_REPLACED_VALUE.getValue());
    if (va != null && va.getValueType().equals(ValueType.BOOL)) {
      return va;
    } else {
      return null;
    }
  }

  /**
   * Gets the error codes. Returns a ValueArray of ValueType.STRING or null if there are no error codes.
   * 
   * @return the error codes in this ColumnSlice.
   */
  public ValueArray getErrorCodes() {
    ValueArray va = getValueProperty(ValueProperty.ERROR_CODE.getValue());
    if (va != null && va.getValueType().equals(ValueType.STRING)) {
      return va;
    } else {
      return null;
    }
  }

  /**
   * Reads a column slice from the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @return A column slice.
   */
  public static ColumnSlice read(BinaryReader binaryReader) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);

    return new ColumnSlice(binaryReader);
  }

  /**
   * Skips over a ColumnSlice section in the BinaryReader.
   * 
   * @param reader The BinaryReader.
   */
  public static void skip(BinaryReader reader) {
    SectionHeader.readSectionType(reader, SectionTypeId.COLUMN_SLICE);
    ValueArray.skip(reader);
    int props = reader.readInt32();
    while (props-- > 0) {
      IOHelpers.skipString(reader);
      ValueArray.skip(reader);
    }
  }

  /**
   * Writes the contents of this column slice to writer.
   * 
   * @param writer The BinaryWriter receiving the output.
   */
  public void write(BinaryWriter writer) {
    Robustness.validateArgumentNotNull("writer", writer);

    SectionHeader.writeSectionType(writer, SectionTypeId.COLUMN_SLICE);
    
    values.serialize(writer);

    writer.writeInt32(valueProperties.size());
    for (Iterator<Entry<String, ValueArray>> iter = valueProperties.entrySet().iterator(); iter.hasNext();) {
      Entry<String, ValueArray> entry = iter.next();
      IOHelpers.writeString(writer, entry.getKey());
      entry.getValue().serialize(writer);
    }
  }

  /**
   * Tries to get the values for a named value property.
   * 
   * @param propertyName The property.
   * @return The property values for all rows in the slice; otherwise null.
   */
  public ValueArray getValueProperty(String propertyName) {

    return valueProperties.get(propertyName);
  }

  /**
   * Defines the names of the standard value properties.
   */
  enum ValueProperty {
    /**
     * The IS_INVALID property is used to mark invalid values. Value type Boolean.
     */
    IS_INVALID("IsInvalid"),

    /**
     * The ERROR_CODE is used to assign arbitrary tags to the values, commonly error codes for missing values.
     * Value type string.
     */
    ERROR_CODE("ErrorCode"),

    /**
     * The HAS_REPLACED_VALUE property is used to denote that an empty value has been replaced. Value type bool.
     */
    HAS_REPLACED_VALUE("HasReplacedValue");

    private String value;

    /**
     * Private constructor.
     * 
     * @param value the property
     */
    ValueProperty(String value) {
      this.value = value;
    }

    /**
     * Returns a string representation of the property.
     * 
     * @return a string representation of the property
     */
    String getValue() {
      return value;
    }
  }
}
