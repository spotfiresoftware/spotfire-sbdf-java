/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents column-level metadata in SBDF.
 */
public final class ColumnMetadata extends MetadataCollection {
  
  /**
   * The name of the property used to store the name of the column.
   */
  private static final String NAME_PROPERTY = "Name";
  
  /**
   * The name of the property used to store the data type of the column.
   */
  private static final String DATA_TYPE_PROPERTY = "DataType";

  /**
   * The name of the property used to store the TimeZone of the column.
   */
  public static final String TIME_ZONE_PROPERTY = "TimeZone";
  
  /**
   * Cached ValueType to avoid having to looking it up all the time.
   */
  private ValueType valueType;
  
  /**
   * Initializes a new instance of the ColumnMetadata class.
   * 
   * @param columnName The column name.
   * @param dataType The column data type.
   */
  public ColumnMetadata(String columnName, ValueType dataType) {
    setName(columnName);
    setDataType(dataType);
  }

  /**
   * Initializes a new instance of the ColumnMetadata class.
   */
  ColumnMetadata() {
  }

  /**
   * Gets the name of the column.
   * 
   * @return the name
   */
  public String getName() {
    return getPropertyOfType(String.class, NAME_PROPERTY);
  }

  /**
   * Sets the name of the column.
   * 
   * @param value the name for the column.
   */
  private void setName(String value) {
    validateName(value);

    removeProperty(NAME_PROPERTY);

    addProperty(new MetadataProperty(NAME_PROPERTY, ValueType.STRING, value));
  }

  /**
   * Gets the data type of the column.
   * 
   * @return the type of the column.
   */
  public ValueType getDataType() {

    if (valueType == null) {
      MetadataProperty p = getProperty(DATA_TYPE_PROPERTY);
      if (p == null) {
        throw Robustness.illegalArgumentException("No value defined for property '%s'", DATA_TYPE_PROPERTY);
      }
      byte[] bytes = (byte[]) p.getValue();
      valueType = ValueType.fromBinary(bytes);
    }

    return valueType;
  }

  /**
   * Sets the type of the column.
   * 
   * @param value the type of the column.
   */
  private void setDataType(ValueType value) {
    validateDatatype(value);

    removeProperty(DATA_TYPE_PROPERTY);

    addProperty(new MetadataProperty(DATA_TYPE_PROPERTY, ValueType.BINARY, ValueType.convertValueTypeToBinary(value)));
    valueType = value;
  }

  /**
   * Get the non-standard metadata properties of the column.
   * 
   * @return a list of the non-standard metadata properties of the column.
   */
  public List<MetadataProperty> getAssignedProperties() {
    List<MetadataProperty> list = new ArrayList<MetadataProperty>();
    for (MetadataProperty property : this) {
      if (!(NAME_PROPERTY.equals(property.getName())) && !(DATA_TYPE_PROPERTY.equals(property.getName()))) {
        list.add(property);
      }
    }
    return list;
  }
  
  /**
   * Returns a mutable copy of this instance.
   * 
   * @return A mutable copy of this instance.
   */
  public ColumnMetadata mutableCopy() {
    ColumnMetadata result = new ColumnMetadata();

    result.addMetadataCollection(this);

    return result;
  }

  /**
   * Returns an immutable copy of this instance.
   * 
   * @return An immutable copy of this instance.
   * @throws IllegalArgumentException if the 'Name' or 'DataType' properties are invalid.
   */
  public ColumnMetadata immutableCopy() {
    if (isImmutable()) {
      return this;
    }

    ColumnMetadata result = mutableCopy();

    result.validateComplete();

    result.setImmutable();

    return result;
  }

  /**
   * Validates that the properties 'Name' and 'DateType' are correctly set. Throws an ArgumentException if they are not
   * valid.
   * 
   * @throws IllegalArgumentException if the 'Name' or 'DataType' properties are invalid.
   */
  public void validateComplete() {
    //  if immutable, the instance is already complete
    if (!isImmutable()) {
      validateName(getName());
      validateDatatype(getDataType());
    }
  }

  /**
   * Throws IllegalArgumentException if the name provided is not valid.
   * 
   * @param name the name to be checked.
   */
  private static void validateName(String name) {
    if (name == null || "".equals(name)) {
      throw Robustness.illegalArgumentException("The column name is not valid.");
    }
  }

  /**
   * Throws IllegalArgumentException if the datatype provided is not valid.
   * 
   * @param dataType the datatype to be checked.
   */
  private static void validateDatatype(ValueType dataType) {
    if (dataType.isUserDefinedType() || dataType.getTypeId() == ValueTypeId.UNKNOWN_TYPE) {
      throw Robustness.illegalArgumentException("'%s' is not a valid data type.", dataType);
    }
  }

}
