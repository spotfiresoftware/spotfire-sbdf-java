/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.ArrayList;
import java.util.List;



/**
 * Represents table and column metadata in SBDF.
 */
public final class TableMetadataBuilder extends MetadataCollection {
  /**
   * Column metadata, one collection for each column.
   */
  private final List<ColumnMetadata> columns = new ArrayList<ColumnMetadata>(7);

  /**
   * Initializes a new instance of the TableMetadataBuilder class.
   */
  public TableMetadataBuilder() {
    // Empty.
  }

  /**
   * Initializes a new instance of the TableMetadataBuilder class.
   * 
   * @param other The source TableMetadata object to fill the contents of this instance with.
   */
  public TableMetadataBuilder(TableMetadata other) {
    for (ColumnMetadata column : other.getColumns()) {
      addColumn(column);
    }

    addMetadataCollection(other);
  }

  /**
   * Gets the metadata for the columns of this table.
   * 
   * @return the columns of this table
   */
  public List<ColumnMetadata> getColumns() {
    return columns;
  }

  /**
   * Adds metadata for the next column in the table.
   * 
   * @param columnMetadata The column metadata.
   */
  public void addColumn(ColumnMetadata columnMetadata) {
    Robustness.validateArgumentNotNull("columnMetadata", columnMetadata);

    columns.add(columnMetadata);
  }

  /**
   * Builds a new instance of the TableMetadata class, filled with the contents of this instance.
   * 
   * @return A new TableMetadata instance.
   * @throws IllegalArgumentException if the underlying data is invalid.
   */
  public TableMetadata build() {
    for (ColumnMetadata column : columns) {
      column.validateComplete();
    }

    return new TableMetadata(columns, this);
  }
}
