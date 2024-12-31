/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;



/**
 * Represents immutable table and column metadata in SBDF.
 */
public final class TableMetadata extends MetadataCollection {
  /**
   * Column metadata, one collection for each column.
   */
  private ColumnMetadata[] columns;

  /**
   * Initializes a new instance of the TableMetadata class.
   * 
   * @param columns The list of columns. The contents will be copied and stored internally.
   * @param metaData The collection of metadata. The contents will be copied and stored internally.
   */
  TableMetadata(List<ColumnMetadata> columns, MetadataCollection metaData) {

    int size = columns.size();
    this.columns = new ColumnMetadata[size];

    for (int i = 0; i < size; i++) {
      this.columns[i] = columns.get(i).immutableCopy();
    }

    addMetadataCollection(metaData);

    setImmutable();
  }

  /**
   * Gets the metadata for the columns of this table.
   * 
   * @return the columns as an array
   */
  public ColumnMetadata[] getColumns() {
    // return the actual array, not a copy because that would kill performance
    return columns;
  }

  /**
   * Reads a TableMetadata instance from an BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @return A new TableMetadata instance.
   */
  public static TableMetadata read(BinaryReader binaryReader) {
    SectionHeader.readSectionType(binaryReader, SectionTypeId.TABLE_METADATA);

    TableMetadataBuilder tableMetaDataBuilder = new TableMetadataBuilder();

    int tableMetaDataCount = binaryReader.readInt32();

    for (int i = 0; i < tableMetaDataCount; ++i) {
      MetadataProperty prop = new MetadataProperty(binaryReader);
      tableMetaDataBuilder.addProperty(prop);
    }

    int columnCount = binaryReader.readInt32();
    for (int i = 0; i < columnCount; ++i) {
      tableMetaDataBuilder.addColumn(new ColumnMetadata());
    }

    int allColumnMetaDataCount = binaryReader.readInt32();
    ArrayList<MetadataPropertyKey> allColumnMetaData = new ArrayList<MetadataPropertyKey>(allColumnMetaDataCount);

    for (int i = 0; i < allColumnMetaDataCount; ++i) {
      String name = IOHelpers.readString(binaryReader);
      ValueType vt = ValueType.read(binaryReader);
      Object defaultValue = null;
      if (binaryReader.readBool()) {
        defaultValue = IOHelpers.readValue(binaryReader, vt);
      }
      allColumnMetaData.add(new MetadataPropertyKey(name, vt, defaultValue));
    }

    for (ColumnMetadata cm : tableMetaDataBuilder.getColumns()) {
      for (MetadataPropertyKey key : allColumnMetaData) {
        if (binaryReader.readBool()) {
          // the metadata property applies to the column cm
          Object value = IOHelpers.readValue(binaryReader, key.getValueType());
          cm.addProperty(new MetadataProperty(key.getName(), key.getValueType(), value, key.getDefaultValue()));
        }
      }
    }

    return tableMetaDataBuilder.build();
  }

  /**
   * Writes this instance to the BinaryWriter.
   * 
   * @param binaryWriter The BinaryWriter.
   */
  public void write(BinaryWriter binaryWriter) {
    Robustness.validateArgumentNotNull("binaryWriter", binaryWriter);

    SectionHeader.writeSectionType(binaryWriter, SectionTypeId.TABLE_METADATA);

    binaryWriter.writeInt32(getCount());
    for (MetadataProperty property : this) {
      property.write(binaryWriter);
    }

    binaryWriter.writeInt32(columns.length);

    // Collect a list of all unique properties, ignoring their values.
    LinkedHashMap<String, MetadataProperty> allColumnMetaData = 
      new LinkedHashMap<String, MetadataProperty>();

    for (ColumnMetadata cm : getColumns()) {
      for (MetadataProperty p : cm) {
        MetadataProperty existing = allColumnMetaData.get(p.getName());
        if (existing != null) {
          if (!p.getValueType().equals(existing.getValueType())
              || !ValueType.objectsEqual(p.getDefaultValue(), existing.getDefaultValue())) {
            throw Robustness.illegalArgumentException(
                "The value types and default values for the properties '%s' must be the same.", p.getName());
          }
        } else {
          allColumnMetaData.put(p.getName(), p);
        }
      }
    }

    binaryWriter.writeInt32(allColumnMetaData.size());
    for (MetadataProperty p : allColumnMetaData.values()) {
      IOHelpers.writeString(binaryWriter, p.getName());
      p.getValueType().write(binaryWriter);
      if (p.getDefaultValue() == null) {
        binaryWriter.writeBool(false);
      } else {
        binaryWriter.writeBool(true);
        IOHelpers.writeValue(binaryWriter, p.getValueType(), p.getDefaultValue());
      }
    }

    for (ColumnMetadata cm : getColumns()) {
      for (MetadataProperty p : allColumnMetaData.values()) {
        MetadataProperty v = cm.getProperty(p.getName());
        if (v != null) {
          binaryWriter.writeBool(true);
          // v.getValueType() must be non-null
          IOHelpers.writeValue(binaryWriter, v.getValueType(), v.getValue());
        } else {
          binaryWriter.writeBool(false);
        }
      }
    }
  }

  /**
   * Defines a key class for MetadataProperty objects in a dictionary.
   */
  private static class MetadataPropertyKey {
    /**
     * The name of the property.
     */
    private String name;

    /**
     * The type of the property.
     */
    private ValueType valueType;

    /**
     * The default value.
     */
    private Object defaultValue;
    
    /**
     * Initializes a new instance of the MetadataPropertyKey class.
     * 
     * @param name The name of the property.
     * @param valueType The type of the property.
     * @param defaultValue The default value of the property.
     */
    public MetadataPropertyKey(String name, ValueType valueType, Object defaultValue) {
      this.name = name;
      this.valueType = valueType;
      this.defaultValue = defaultValue;
    }

    /**
     * Gets the name of the property.
     * 
     * @return the name of the property
     */
    public final String getName() {
      return name;
    }

    /**
     * Gets the type of the property.
     * 
     * @return the type of the property
     */
    public final ValueType getValueType() {
      return valueType;
    }

    /**
     * Gets the type of the default value.
     * 
     * @return the default value of the property
     */
    public final Object getDefaultValue() {
      return defaultValue;
    }

  }
}
