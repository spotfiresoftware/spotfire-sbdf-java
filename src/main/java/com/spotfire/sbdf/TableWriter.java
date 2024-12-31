/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * This class provides a simple, row-based interface for exporting data to the SBDF format.
 */
public final class TableWriter {
  /**
   * The upper limit of rows in a column slice.
   */
  public static final int COLUMN_SLICE_LIMIT = 10000;

  /**
   * The BinaryWriter.
   */
  private BinaryWriter writer;

  /**
   * The table metadata.
   */
  private TableMetadata metadata;

  /**
   * The column data.
   */
  private Object[][] columnData;

  /**
   * The index of the column receiving the next data value.
   */
  private int columnIndex;

  /**
   * The index of the column receiving the next data value.
   */
  private int rowIndex;
  
  /**
   * Determines if the data is dirty and flushing is necessary.
   */
  private boolean dirty;

  /**
   * Initializes a new instance of the TableWriter class.
   * 
   * @param binaryWriter The BinaryWriter. The caller owns the writer and is responsible for closing it after the
   * TableWriter has been disposed (since a file may contain more than one table).
   * @param metadata The table metadata, which must include metadata for all columns.
   */
  public TableWriter(BinaryWriter binaryWriter, TableMetadata metadata) {
    Robustness.validateArgumentNotNull("binaryWriter", binaryWriter);
    Robustness.validateArgumentNotNull("metadata", metadata);

    this.writer = binaryWriter;
    this.metadata = metadata;
    columnIndex = 0;
    rowIndex = 0;

    columnData = new Object[this.metadata.getColumns().length][];

    for (int i = 0; i < this.metadata.getColumns().length; ++i) {
      // use max number of values for a slice
      Object[] data = new Object[COLUMN_SLICE_LIMIT];
      columnData[i] = data;
    }

    dirty = true;
  }

  /**
   * Adds a value to the table. Values are added in a row-based manner. For each row, values
   * must be provided for all columns.
   * 
   * @param value The new value. The object's type has to match the current column.
   */
  public void addValue(Object value) {
    Robustness.validateArgumentNotNull("value", value);

    ColumnMetadata[] columnMetadata = metadata.getColumns();
    ValueType vt = columnMetadata[columnIndex].getDataType();

    if (!vt.isInvalidValue(value) && !vt.isErrorValue(value) && !vt.isReplacedValue(value)) {
      ValueType.validateAssignment(vt, value);
    }

    // add value to column at columnIndex
    columnData[columnIndex][rowIndex] = value;
    dirty = true;

    // increase columnIndex. wrap if necessary.
    if (++columnIndex == columnMetadata.length) {
      columnIndex = 0;
      // reached the end of the current row
      rowIndex++;

      // flush slices
      if (rowIndex == COLUMN_SLICE_LIMIT) {
        flush();
      }
    }
  }

  /**
   * Writes a marker to indicate the end of the table. Will flush the writer but does not close
   * it. Note that the caller owns the writer and is responsible for closing it after the TableWriter
   * has been disposed (since a file may contain more than one table).
   */
  public void writeEndOfTable() {
    // flush the slices
    flush();
    TableSlice.writeEndOfTableMarker(writer);
  }
  
  /**
   * Flushes and releases the internal buffers.
   */
  private void flush() {
    // flush columndata by creating column slices
    if (dirty) {
      dirty = false;
      TableSliceBuilder tsb = new TableSliceBuilder(metadata);

      for (int column = 0; column < columnData.length; ++column) {
        List<String> errors = null;
        List<Boolean> empties = null;
        List<Boolean> replaced = null;
        List<Object> values = new ArrayList<Object>(rowIndex);
        Object[] data = columnData[column];
        ValueType valueType = metadata.getColumns()[column].getDataType();

        for (int row = 0; row < rowIndex; ++row) {
          
          Object value = data[row];
          
          if (valueType.isInvalidValue(value)) {
            if (empties == null) {
              empties = new ArrayList<Boolean>(rowIndex);
              for (int i = 0; i < row; ++i) {
                empties.add(false);
              }
            }

            empties.add(true);
            value = null;
            
          } else if (empties != null) {
            empties.add(false);
          }
            
          if (valueType.isReplacedValue(value)) {
            if (replaced == null) {
              replaced = new ArrayList<Boolean>(rowIndex);
              for (int i = 0; i < row; ++i) {
                replaced.add(false);
              }
            }

            replaced.add(true);
            value = valueType.getReplacedValue(value);

          } else if (replaced != null) {
            replaced.add(false);
          }
          
          if (valueType.isErrorValue(value)) {
            if (errors == null) {
              errors = new ArrayList<String>(rowIndex);
              for (int i = 0; i < row; ++i) {
                errors.add("");
              }
            }

            errors.add(valueType.getErrorString(value));
            value = null;

          } else if (errors != null) {
            errors.add("");
          }

          values.add((value != null) ? value : valueType.getDefaultValue());
        }

        ValueArray va = ValueArray.createDefaultArrayEncoding(valueType, 
            new TypedArray(ValueType.getRuntimeType(valueType), values, valueType.getDefaultValue()));
        ColumnSliceBuilder csb = new ColumnSliceBuilder(va);

        if (empties != null) {
          csb.setValueProperty(ColumnSlice.ValueProperty.IS_INVALID.getValue(), ValueArray.createDefaultArrayEncoding(
              ValueType.BOOL, 
              new TypedArray(ValueType.getRuntimeType(ValueType.BOOL), empties, ValueType.BOOL.getDefaultValue())));
        }

        if (errors != null) {
          csb.setValueProperty(ColumnSlice.ValueProperty.ERROR_CODE.getValue(), ValueArray.createDefaultArrayEncoding(
              ValueType.STRING, new TypedArray(errors.toArray(new String[errors.size()]))));
        }

        if (replaced != null) {
          csb.setValueProperty(ColumnSlice.ValueProperty.HAS_REPLACED_VALUE.getValue(),
              ValueArray.createDefaultArrayEncoding(ValueType.BOOL, 
                  new TypedArray(ValueType.getRuntimeType(ValueType.BOOL), replaced,
                      ValueType.BOOL.getDefaultValue())));
        }

        tsb.addColumn(csb.build());
      }

      TableSlice ts = tsb.build();

      ts.write(writer);
    }

    try {
      writer.flush();
    } catch (IOException e) {
      throw new SerializationException("IOException flushing data to output writer");
    }
    
    columnIndex = 0;
    rowIndex = 0;
  }

}
