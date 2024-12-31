/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;



/**
 * This class provides a simple, row-based interface for importing data from the SBDF format.
 */
public final class TableReader implements Iterable<Object> {
  /**
   * The BinaryReader.
   */
  private BinaryReader reader;

  /**
   * The table metadata.
   */
  private TableMetadata metadata;

  /**
   * The current column index.
   */
  private int columnIndex;

  /**
   * The current row index.
   */
  private int rowIndex;

  /**
   * The array of data value arrays.
   */
  private TypedArray[] dataArrays;

  /**
   * The array of invalid value arrays.
   */
  private boolean[][] invalidArrays;

  /**
   * The array of error value arrays.
   */
  private String[][] errorArrays;

  /**
   * The array of replaced value arrays.
   */
  private boolean[][] replacedArrays;

  /**
   * The filtered columns.
   */
  private boolean[] filteredColumns;

  /**
   * The (filtered) column meta data.
   */
  private ColumnMetadata[] columnMetaData;

  /**
   * The number of (filtered) columns.
   */
  private int columnCount;

  /**
   * Initializes a new instance of the TableReader class.
   * 
   * @param binaryReader The BinaryReader. The caller owns the reader and is responsible for closing it after the 
   * TableReader has been disposed.
   * @param metadata The table metadata, which must include metadata for all columns. The reader is read to the
   * end of its underlying reader.
   */
  public TableReader(BinaryReader binaryReader, TableMetadata metadata) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);
    Robustness.validateArgumentNotNull("metadata", metadata);

    ColumnMetadata[] columns = metadata.getColumns();

    this.reader = binaryReader;
    this.metadata = metadata;
    columnIndex = 0;
    rowIndex = 0;
    columnCount = columns.length;
    filteredColumns = null;
    columnMetaData = Arrays.copyOf(columns, columns.length);
  }

  /**
   * Initializes a new instance of the TableReader class.
   * 
   * @param binaryReader The BinaryReader. The caller owns the reader and is responsible for closing it after the
   *  TableReader has been disposed.
   * @param metadata The table metadata, which must include metadata for all columns.
   * @param filteredColumns Determines which columns to read. The reader is read to the end of its underlying reader.
   */
  public TableReader(BinaryReader binaryReader, TableMetadata metadata, boolean[] filteredColumns) {
    Robustness.validateArgumentNotNull("binaryReader", binaryReader);
    Robustness.validateArgumentNotNull("metadata", metadata);
    Robustness.validateArgumentNotNull("metadata", filteredColumns);

    this.reader = binaryReader;
    this.metadata = metadata;
    this.columnIndex = 0;
    this.rowIndex = 0;
    List<ColumnMetadata> columnMetaDataList = new ArrayList<ColumnMetadata>(metadata.getCount());

    // setup filter
    this.filteredColumns = Arrays.copyOf(filteredColumns, filteredColumns.length);

    ColumnMetadata[] columns = metadata.getColumns();
    
    if (columns.length != this.filteredColumns.length) {
      throw Robustness.illegalArgumentException(
          "The number of columns in the filter %s does not match the number of columns in the metadata %s.",
          this.filteredColumns.length, columns.length);
    }

    int column = 0;
    for (boolean b : this.filteredColumns) {
      if (b) {
        ++columnCount;
        columnMetaDataList.add(columns[column]);
      }

      ++column;
    }

    this.columnMetaData = columnMetaDataList.toArray(new ColumnMetadata[columnMetaDataList.size()]);
    
  }

  /**
   * Gets the metadata for the columns to read.
   * 
   * @return the metadata of the columns
   */
  public List<ColumnMetadata> getColumns() {
    return Arrays.asList(columnMetaData);
  }
  
  /**
   * Gets the column count.
   * 
   * @return the number of columns
   */
  public int getColumnCount() {
    return columnCount;
  }
  
  /**
   * Gets the data values for the current table slice. May be empty if there are no columns.
   * 
   * @return the data values for the current table slice.
   */
  public TypedArray[] getValueArrays() {
    return (dataArrays == null ? null : Arrays.copyOf(dataArrays, dataArrays.length));
  }

  /**
   * Gets the error codes for the current table slice. May be null if there are no errors.
   * 
   * @return the error codes for the current table slice. May be null if there are no errors.
   */
  public String[][] getErrorCodeArrays() {
    return (errorArrays == null ? null : Arrays.copyOf(errorArrays, errorArrays.length));
  }

  /**
   * Gets the is invalid values for the current table slice. May be null if there are no invalid values.
   * 
   * @return the is invalid values for the current table slice. May be null if there are no invalid values.
   */
  public boolean[][] getIsInvalidArrays() {
    return (invalidArrays == null ? null : Arrays.copyOf(invalidArrays, invalidArrays.length));
  }

  /**
   * Gets the is replaced values for the current table slice. May be null if there are no replaced values.
   * 
   * @return the is replaced values for the current table slice. May be null if there are no replaced values.
   */
  public boolean[][] getReplacedValueArrays() {
    return (replacedArrays == null ? null : Arrays.copyOf(replacedArrays, replacedArrays.length));
  }

  /**
   * Returns the next value from the table.
   * 
   * @return An object representing the next value of the table.
   */
  public Object readValue() {
    if (dataArrays == null || (dataArrays.length > 0 && rowIndex == dataArrays[0].getCount())) {
      if (!readNextTableSlice()) {
        return null;
      } else {
        rowIndex = 0;
      }
    }
    
    if (dataArrays == null || dataArrays.length == 0) {
      // empty table
      return null;
    }

    try {
      Object value = null;
      TypedArray typedArray = dataArrays[columnIndex];
      if (typedArray.getCount() > 0) {
        value = typedArray.getAsObject(rowIndex);
      }

      boolean[] boolList = invalidArrays[columnIndex];
      if (boolList != null && boolList[rowIndex]) {
        return columnMetaData[columnIndex].getDataType().getInvalidValue();
      }

      String[] stringList = errorArrays[columnIndex];
      if (stringList != null) {
        String string = stringList[rowIndex];
        if (string != null && !"".equals(string)) {
          return columnMetaData[columnIndex].getDataType().errorValue(stringList[rowIndex]);
        }
      }

      boolList = replacedArrays[columnIndex];
      if (boolList != null && boolList[rowIndex]) {
        return columnMetaData[columnIndex].getDataType().replacedValue(value);
      }

      return value;
    } finally {
      if (++columnIndex == columnCount) {
        ++rowIndex;
        columnIndex = 0;
      }
    }
  }

  /**
   * Reads the next table slice.
   * 
   * @return true if the table slice was read or false if the end-of-table marker was reached.
   */
  public boolean readNextTableSlice() {
    
    if (dataArrays == null) {
      dataArrays = new TypedArray[columnCount];
      invalidArrays = new boolean[columnCount][];
      errorArrays = new String[columnCount][];
      replacedArrays = new boolean[columnCount][];
    }

    TableSlice ts = null;
    SectionTypeId sti = SectionHeader.readSectionType(reader);

    if (sti == SectionTypeId.TABLE_END) {
      return false;
    }

    if (filteredColumns != null) {
      ts = TableSlice.read(reader, metadata, sti, filteredColumns);
    } else {
      ts = TableSlice.read(reader, metadata, sti);
    }

    for (int column = 0; column < ts.getColumns().size(); ++column) {
      ColumnSlice cs = ts.getColumns().get(column);
      dataArrays[column] = cs.getValues().toArray();

      ValueArray va = cs.getValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue());
      if (va != null) {
        errorArrays[column] = va.toArray().getStringArray();
      } else {
        errorArrays[column] = null;
      }

      va = cs.getValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue());
      if (va != null) {
        invalidArrays[column] = va.toArray().getBoolArray();
      } else {
        invalidArrays[column] = null;
      }

      va = cs.getValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue());
      if (va != null) {
        replacedArrays[column] = va.toArray().getBoolArray();
      } else {
        replacedArrays[column] = null;
      }
    }

    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<Object> iterator() {
    return new TableReaderIterator(this);
  }

  /**
   * Internal class that provides an iterator over the values in the reader.
   * Note that this is not thread-safe and that having two iterators over the
   * same reader will result in some values going to one iterator and others
   * going to the other iterator.
   */
  private static final class TableReaderIterator implements Iterator<Object> {
    
    /**
     * The TableReader being iterated over.
     */
    private final TableReader reader;
    
    /**
     * Used to store the next value to return from the iterator.
     */
    private Object next;

    /**
     * Create an iterator for this reader.
     * 
     * @param tableReader the reader
     */
    TableReaderIterator(TableReader tableReader) {
      reader = tableReader;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      if (next == null) {
        next = reader.readValue();
      }
      return (next != null);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public Object next() {
      if (next != null) {
        Object result = next;
        next = null;
        return result;
      } else {
        Object result = reader.readValue();
        if (result == null) {
          throw new NoSuchElementException();
        }
        return result;
      }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove values from the TableReader");
    }
  }

}
