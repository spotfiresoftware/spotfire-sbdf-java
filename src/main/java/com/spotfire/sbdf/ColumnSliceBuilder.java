/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.LinkedHashMap;



/**
 * Defines a builder for ColumnSlice objects.
 */
public final class ColumnSliceBuilder {
  /**
   * The valid values in this column slice.
   */
  private ValueArray values;

  /**
   * The value properties.
   */
  private LinkedHashMap<String, ValueArray> valueProperties;

  /**
   * Initializes a new instance of the ColumnSliceBuilder class.
   * 
   * @param values The values of this column.
   */
  public ColumnSliceBuilder(ValueArray values) {
    Robustness.validateArgumentNotNull("values", values);

    this.values = values;
    valueProperties = new LinkedHashMap<String, ValueArray>(1);
  }

  /**
   * Gets the total number of rows in this ColumnSliceBuilder.
   * 
   * @return the number of rows.
   */
  public int getRowCount() {
    return values.getCount();
  }

  /**
   * Gets the values in this ColumnSliceBuilder.
   * 
   * @return the values in the builder.
   */
  public ValueArray getValues() {
    return values;
  }

  /**
   * Sets the invalid values for this column.
   * 
   * @param valueArray the new invalid values.
   */
  public void setInvalidValues(ValueArray valueArray) {
    Robustness.validateArgumentNotNull("valueArray", valueArray);

    if (!valueArray.getValueType().equals(ValueType.BOOL)) {
      throw Robustness.illegalArgumentException("The property must be a ValueArray of type bool.");
    }

    setValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue(), valueArray);
  }

  /**
   * Sets the replaced values for this column.
   * 
   * @param valueArray the new replaced values.
   */
  public void setReplacedValues(ValueArray valueArray) {
    Robustness.validateArgumentNotNull("valueArray", valueArray);

    if (!valueArray.getValueType().equals(ValueType.BOOL)) {
      throw Robustness.illegalArgumentException("The property must be a ValueArray of type bool.");
    }

    setValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue(), valueArray);
  }

  /**
   * Sets the error codes for this column.
   * 
   * @param valueArray the new error codes.
   */
  public void setErrorCodes(ValueArray valueArray) {
    Robustness.validateArgumentNotNull("valueArray", valueArray);

    if (!valueArray.getValueType().equals(ValueType.STRING)) {
      throw Robustness.illegalArgumentException("The property must be a ValueArray of type string.");
    }

    setValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue(), valueArray);
  }

  /**
   * Sets a value property for the values in this ColumnSliceBuilder.
   * 
   * @param propertyName The property name. The ColumnSlice.ValueProperty class defines the standard property names.
   * @param values The property values for all rows in the slice.
   * @throws IllegalArgumentException if values is a standard value property of the wrong type.
   */
  public void setValueProperty(String propertyName, ValueArray values) {
    Robustness.validateArgumentNotNull("values", values);

    if (propertyName == null || "".equals(propertyName)) {
      throw Robustness.illegalArgumentException(
          "The 'name' parameter may not be null or empty.");
    }

    if (propertyName.equals(ColumnSlice.ValueProperty.IS_INVALID.getValue())) {
      if (values.getValueType() != ValueType.BOOL) {
        throw Robustness.illegalArgumentException(
            "The standard value property 'IS_INVALID' must be of type bool.");
      }
    } else if (propertyName.equals(ColumnSlice.ValueProperty.ERROR_CODE.getValue())) {
      if (values.getValueType() != ValueType.STRING) {
        throw Robustness.illegalArgumentException(
            "The standard value property 'ERROR_CODE' must be of type string.");
      }
    } else if (propertyName.equals(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue())) {
      if (values.getValueType() != ValueType.BOOL) {
        throw Robustness.illegalArgumentException(
            "The standard value property 'HAS_REPLACED_VALUE' must be of type bool.");
      }
    }

    if (values.getCount() != this.values.getCount()) {
      throw Robustness
          .illegalArgumentException(
              "The length of the passed value array does not match the length of the column data value array.");
    }

    valueProperties.put(propertyName, values);
  }

  /**
   * Gets the values for a named value property.
   * 
   * @param name The property name.
   * @return the property values for all rows in the slice, or null if the property was not set.
   */
  public ValueArray getValueProperty(String name) {
    return valueProperties.get(name);
  }

  /**
   * Builds a new ColumnSlice with the contents of this instance.
   * 
   * @return A new ColumnSlice.
   */
  public ColumnSlice build() {
    // the column slice constructor copies the valueProperties dictionary.
    return new ColumnSlice(values, valueProperties);
  }
}
