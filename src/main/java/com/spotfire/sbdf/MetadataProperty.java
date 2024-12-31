/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;



/**
 * Represents a metadata property for a data table or column in Sbdf.
 */
public final class MetadataProperty {
  /**
   * The property name.
   */
  private String name;

  /**
   * The value type of the property.
   */
  private ValueType type;

  /**
   * The value of the property.
   */
  private Object value;

  /**
   * The default value of the property.
   */
  private Object defaultValue;

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("MetadataProperty:[name='");
    builder.append(name);
    builder.append("', type='");
    builder.append(type);
    builder.append("', value='");
    builder.append(value);
    builder.append("', defaultValue='");
    builder.append(defaultValue);
    builder.append("']");
    return builder.toString();
  }

  /**
   * Initializes a new instance of the MetadataProperty class.
   * 
   * @param name The name of the property.
   * @param value The value of the property.
   */
  public MetadataProperty(String name, String value) {
    this(name, ValueType.STRING, value, null);
    // Empty.
  }

  /**
   * Initializes a new instance of the MetadataProperty class.
   * 
   * @param name The name of the property.
   * @param value The value of the property.
   */
  public MetadataProperty(String name, int value) {
    this(name, ValueType.INT, value, null);
    // Empty.
  }

  /**
   * Initializes a new instance of the MetadataProperty class.
   * 
   * @param name The name of the property.
   * @param valueType The value type of the property.
   * @param value The value of the property.
   */
  public MetadataProperty(String name, ValueType valueType, Object value) {
    this(name, valueType, value, null);
    // Empty.
  }

  /**
   * Initializes a new instance of the MetadataProperty class.
   * 
   * @param name The name of the property.
   * @param valueType The value type of the property.
   * @param value The value of the property.
   * @param defaultValue The default value of the property.
   */
  public MetadataProperty(String name, ValueType valueType, Object value, Object defaultValue) {
    Robustness.validateArgumentNotNullOrEmptyString("name", name);
    Robustness.validateArgumentNotNull("valueType", valueType);
    Robustness.validateArgumentNotNull("value", value);

    this.name = name;
    this.type = valueType;
    this.value = value;
    this.defaultValue = defaultValue;

    validateAssignment();
  }

  /**
   * Initializes a new instance of the MetadataProperty class by reading the contents from the BinaryReader.
   * 
   * @param reader The BinaryReader from which the contents are read.
   */
  public MetadataProperty(BinaryReader reader) {
    name = IOHelpers.readString(reader);
    type = ValueType.read(reader);

    if (reader.readBool()) {
      value = IOHelpers.readValue(reader, type);
    }

    if (reader.readBool()) {
      defaultValue = IOHelpers.readValue(reader, type);
    }

    validateAssignment();
  }

  /**
   * Gets the name of the metadata property.
   * 
   * @return the name of the property
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the value type of the metadata property.
   * 
   * @return the type of the property
   */
  public ValueType getValueType() {
    return type;
  }

  /**
   * Gets the value of the metadata property.
   * 
   * @return the value of the property
   */
  public Object getValue() {
    return value;
  }

  /**
   * Gets the default value of the metadata property.
   * 
   * @return the default value of the property
   */
  public Object getDefaultValue() {
    return defaultValue;
  }

  /**
   * Writes the contents of this metadata property to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   */
  public void write(BinaryWriter writer) {
    IOHelpers.writeString(writer, name);
    type.write(writer);

    if (value != null) {
      writer.writeBool(true);
      IOHelpers.writeValue(writer, type, value);
    } else {
      writer.writeBool(false);
    }

    if (defaultValue != null) {
      writer.writeBool(true);
      IOHelpers.writeValue(writer, type, defaultValue);
    } else {
      writer.writeBool(false);
    }
  }

  /**
   * Validates that that the value and defaultValue are of the correct type.
   */
  private void validateAssignment() {
    if (value != null) {
      ValueType.validateAssignment(type, value);
    }

    if (defaultValue != null) {
      ValueType.validateAssignment(type, defaultValue);
    }
  }
}
