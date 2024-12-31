/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Represents a slice of a data table.
 */
public final class TableSlice {
  /**
   * Metadata at the table and column levels. All slices of a table share the same metadata.
   */
  private TableMetadata tableMetadata;

  /**
   * An ordered list of column slices. Same number and order as in the metadata.
   */
  private ColumnSlice[] columns;

  /**
   * An ordered list of filtered columns. Same order as in the metadata.
   */
  private ColumnSlice[] filteredColumns;

  /**
   * The list of column names.
   */
  private String[] columnNames;

  /**
   * The list of column value types.
   */
  private ValueType[] columnDataTypes;

  /**
   * Initializes a new instance of the TableSlice class.
   * 
   * @param tableMetadata The table and column metadata.
   * @param columns A list of column slices, one for each column in the table.
   */
  TableSlice(TableMetadata tableMetadata, List<ColumnSlice> columns) {
    this.tableMetadata = tableMetadata;
    this.columns = columns.toArray(new ColumnSlice[columns.size()]);

    int size = columns.size();
    List<ColumnSlice> filteredColumnsList = new ArrayList<ColumnSlice>(size);
    
    for (int i = 0; i < size; ++i) {
      ColumnSlice column = columns.get(i);
      if (column != null) {
        filteredColumnsList.add(column);
      }
    }

    filteredColumns = filteredColumnsList.toArray(new ColumnSlice[filteredColumnsList.size()]);

    columnNames = new String[size];
    columnDataTypes = new ValueType[size];

    ColumnMetadata[] columnMetaDataArray = tableMetadata.getColumns();
    for (int i = 0; i < size; i++) {
      ColumnMetadata cmd = columnMetaDataArray[i];
      columnNames[i] = cmd.getName();
      columnDataTypes[i] = cmd.getDataType();
    }

  }

  /**
   * Gets unfiltered, complete meta data for this table slice.
   * 
   * @return the table metadata of this slice
   */
  public TableMetadata getMetaData() {
    return tableMetadata;
  }

  /**
   * Gets the column slices of this table slice.
   * 
   * @return the columns for this slice
   */
  public List<ColumnSlice> getColumns() {
    return Arrays.asList(filteredColumns);
  }

  /**
   * Gets the column names.
   * 
   * @return the column names for this slice
   */
  public List<String> getColumnNames() {
    return Arrays.asList(columnNames);
  }

  /**
   * Gets the column data types.
   * 
   * @return the column data types for this slice
   */
  public List<ValueType> getColumnDataTypes() {
    return Arrays.asList(columnDataTypes);
  }

  /**
   * Enumerates over all slices in the stream of the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetaData The table metadata.
   * @return An enumeration over all table slices.
   */
  public static Iterable<TableSlice> readSlices(BinaryReader binaryReader, TableMetadata tableMetaData) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);
    Robustness.validateArgumentNotNull("tableMetaData", tableMetaData);

    return readSlicesHelper(binaryReader, tableMetaData, null);
  }

  /**
   * Enumerates over all slices in the stream of the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetaData The table metadata.
   * @param columnSubset The subset of columns to read.
   * @return An enumeration over all table slices.
   */
  public static Iterable<TableSlice> readSlices(BinaryReader binaryReader, TableMetadata tableMetaData,
      boolean[] columnSubset) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);
    Robustness.validateArgumentNotNull("tableMetaData", tableMetaData);
    Robustness.validateArgumentNotNull("columnSubset", columnSubset);

    return readSlicesHelper(binaryReader, tableMetaData, columnSubset);
  }

  /**
   * Skips all table slices in the stream of the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetaData The table metadata
   */
  public static void skipSlices(BinaryReader binaryReader, TableMetadata tableMetaData) {

    int size = tableMetaData.getColumns().length;

    // no columns selected
    boolean[] subset = new boolean[size];
    Arrays.fill(subset, false);

    readSlices(binaryReader, tableMetaData, subset);
  }

  /**
   * Writes the end of table marker to the BinaryWriter.
   * 
   * @param binaryWriter The BinaryWriter.
   */
  public static void writeEndOfTableMarker(BinaryWriter binaryWriter) {
    SectionHeader.writeSectionType(binaryWriter, SectionTypeId.TABLE_END);
  }

  /**
   * Writes this table slice to a BinaryWriter.
   * 
   * @param binaryWriter The BinaryWriter.
   */
  public void write(BinaryWriter binaryWriter) {
    SectionHeader.writeSectionType(binaryWriter, SectionTypeId.TABLE_SLICE);

    if ((columns == null) || (columns.length == 0)) {
      binaryWriter.writeInt32(0);
    } else {
      binaryWriter.writeInt32(columns.length);

      for (ColumnSlice column : columns) {
        column.write(binaryWriter);
      }
    }
  }

  /**
   * Reads a table slice from a BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetadata The TableMetaData information.
   * @param sectionTypeId The section type id. Must be SectionTypeId.TABLE_SLICE.
   * @return A new TableSlice instance.
   */
  static TableSlice read(BinaryReader binaryReader, TableMetadata tableMetadata, SectionTypeId sectionTypeId) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);
    Robustness.validateArgumentNotNull("tableMetadata", tableMetadata);

    return readHelper(binaryReader, tableMetadata, sectionTypeId, null);
  }

  /**
   * Reads a table slice from a BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetadata The TableMetaData information.
   * @param sectionTypeId The section type id. Must be SectionTypeId.TABLE_SLICE.
   * @param columnSubset The subset of columns to create.
   * @return A new TableSlice instance.
   */
  static TableSlice read(BinaryReader binaryReader, TableMetadata tableMetadata, SectionTypeId sectionTypeId,
      boolean[] columnSubset) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);
    Robustness.validateArgumentNotNull("tableMetadata", tableMetadata);
    Robustness.validateArgumentNotNull("columnSubset", columnSubset);

    return readHelper(binaryReader, tableMetadata, sectionTypeId, columnSubset);
  }

  /**
   * Reads a TableSlice from the provided BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetadata The table meta data.
   * @param sectionTypeId The section type id. Must be SectionTypeId.TABLE_SLICE.
   * @param columnSubset The subset of columns. May be null.
   * @return A new TableSlice.
   */
  private static TableSlice readHelper(BinaryReader binaryReader, TableMetadata tableMetadata,
      SectionTypeId sectionTypeId, boolean[] columnSubset) {

    SectionHeader.verifySectionType(sectionTypeId, SectionTypeId.TABLE_SLICE);

    int columns = binaryReader.readInt32();

    if (columnSubset != null && columns != columnSubset.length) {
      throw Robustness.illegalArgumentException(
          "The number of columns in the metadata %s doesn't match the number of columns in the column subset %s.",
          tableMetadata.getColumns().length, columnSubset.length);
    }

    List<ColumnSlice> slices = new ArrayList<ColumnSlice>(columns);

    for (int column = 0; column < columns; ++column) {
      if (columnSubset == null || columnSubset[column]) {
        slices.add(ColumnSlice.read(binaryReader));
      } else {
        slices.add(null);
        ColumnSlice.skip(binaryReader);
      }
    }

    return new TableSlice(tableMetadata, slices);
  }

  /**
   * Enumerates over all table slices in the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param tableMetaData The table metadata.
   * @param columnSubset The column subset. If null, all columns are read.
   * @return An enumerator over all table slices.
   */
  private static Iterable<TableSlice> readSlicesHelper(BinaryReader binaryReader, TableMetadata tableMetaData,
      boolean[] columnSubset) {

    List<TableSlice> slices = new ArrayList<TableSlice>();

    for (;;) {
      SectionTypeId sti = SectionHeader.readSectionType(binaryReader);

      if (sti == SectionTypeId.TABLE_END) {
        break;
      }

      slices.add(TableSlice.readHelper(binaryReader, tableMetaData, sti, columnSubset));
    }
    return slices;
  }
}
