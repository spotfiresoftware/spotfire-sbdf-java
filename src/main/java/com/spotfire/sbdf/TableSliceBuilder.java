/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.ArrayList;
import java.util.List;



/**
 * A builder class used for constructing a table slice.
 */
public final class TableSliceBuilder {
  /**
   * Holds the table metadata.
   */
  private TableMetadata tableMetadata;

  /**
   * Holds the list of column slices added so far.
   */
  private List<ColumnSlice> columns;

  /**
   * Initializes a new instance of the TableSliceBuilder class.
   * 
   * @param tableMetadata The table meta data.
   */
  public TableSliceBuilder(TableMetadata tableMetadata) {
    this.tableMetadata = tableMetadata;
    columns = new ArrayList<ColumnSlice>(tableMetadata.getCount());
  }

  /**
   * Adds a ColumnSlice to the list of columns.
   * 
   * @param columnSlice The ColumnSlice to add.
   */
  public void addColumn(ColumnSlice columnSlice) {
    if (columns.size() > 0) {
      if (columnSlice.getRowCount() != columns.get(0).getRowCount()) {
        throw Robustness.illegalArgumentException("The column slice row counts do not match.");
      }
    }

    columns.add(columnSlice);
  }

  /**
   * Create a new TableSlice from the contents of this instance.
   * 
   * @return A new TableSlice.
   */
  public TableSlice build() {
    if (tableMetadata.getColumns().length != columns.size()) {
      throw Robustness
          .illegalArgumentException(
              "The number of columns of the table metadata does not match the number of column slices.");
    }

    return new TableSlice(tableMetadata, columns);
  }
}
