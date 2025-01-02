/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf.ApiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;

import com.spotfire.sbdf.BinaryReader;
import com.spotfire.sbdf.BinaryWriter;
import com.spotfire.sbdf.ColumnMetadata;
import com.spotfire.sbdf.FileHeader;
import com.spotfire.sbdf.TableMetadata;
import com.spotfire.sbdf.TableMetadataBuilder;
import com.spotfire.sbdf.TableReader;
import com.spotfire.sbdf.TableWriter;
import com.spotfire.sbdf.ValueType;
import com.spotfire.sbdf.ValueTypeId;

/**
 * This file contains basic API scenarios used for design purposes, and also verifies that the API works as expected.
 */
public class BasicApiTest {

  /**
   * Verifies that it is possible to write and read a data table using the simple API.
   * @throws Exception If an error occurred.
   */
  @Test
  public final void testWriteAndReadSimpleTable() throws Exception {
    for (TestTable table : TestTable.getTables()) {
      final String filePrefix = "hello";
      final String fileSuffix = ".sbdf";
      File file = File.createTempFile(filePrefix, fileSuffix);
      file.deleteOnExit();
      final String fileName = file.getCanonicalPath();
      try (BinaryWriter binaryWriter = new BinaryWriter(new FileOutputStream(file))) {
        writeFileForDummies(binaryWriter, table);
      }

      file = new File(fileName);
      BinaryReader binaryReader = new BinaryReader(new FileInputStream(file));
      try {
        readFileForDummies(binaryReader, table);
      } finally {
        file.delete();
      }
    }
  }

  /**
   * This scenario shows how to export a plain table to an SBDF file, using the simplest possible code. A "dummy" is
   * assumed to know nothing about tables slices, sections, or encodings.
   *
   * @param binaryWriter The output binary writer, owned by the caller.
   * @param table The table to be exported.
   */
  public final void writeFileForDummies(BinaryWriter binaryWriter, TestTable table) {
    // Write the file header.
    FileHeader.writeCurrentVersion(binaryWriter);

    // Create the table metadata.
    TableMetadataBuilder tableMetadataBuilder = new TableMetadataBuilder();
    for (int columnIndex = 0; columnIndex < table.getColumnCount(); ++columnIndex) {
      String columnName = table.getColumnNames()[columnIndex];
      ValueType valueType = ValueType.getValueTypeFromId(table.getValueTypes()[columnIndex]);
      tableMetadataBuilder.addColumn(new ColumnMetadata(columnName, valueType));
    }

    TableMetadata tableMetadata = tableMetadataBuilder.build();

    tableMetadata.write(binaryWriter);

    // Use a row-based table writer to export the data.
    TableWriter tableWriter = new TableWriter(binaryWriter, tableMetadata);
    for (int rowIndex = 0; rowIndex < table.getRowCount(); ++rowIndex) {
      for (int columnIndex = 0; columnIndex < table.getColumnCount(); ++columnIndex) {
        Object value = table.getDataRows()[rowIndex][columnIndex];
        tableWriter.addValue(value);
      }
    }
    tableWriter.writeEndOfTable();
  }

  /**
   * This scenario shows how to import a plain table from an SBDF file, using the simplest possible code. A "dummy" is
   * assumed to know nothing about tables slices, sections, or encodings.
   *
   * @param binaryReader The binary reader, from which the table is read.
   * @param table The table reference, used to verify the contents of the read tables.
   */
  public final void readFileForDummies(BinaryReader binaryReader, TestTable table) {
    FileHeader header = FileHeader.read(binaryReader);
    FileHeader.validate(header.getMajorVersion(), header.getMinorVersion());

    TableMetadata tableMetaData = TableMetadata.read(binaryReader);

    int rowIndex = 0;
    int columnIndex = 0;

    TableReader reader = new TableReader(binaryReader, tableMetaData);

    for (Object value : reader) {
      assertEquals(table.getDataRows()[rowIndex][columnIndex], value, "Incorrect value in table.");

      if (++columnIndex == table.getColumnCount()) {
        columnIndex = 0;
        ++rowIndex;
      }
    }
  }

  /**
   * A simple table class used for testing purposes.
   */
  public static class TestTable {
    /**
     * A test table with 3 rows and 3 columns.
     */
    private static TestTable threeByThree;

    /**
     * A test table with 2 rows and 4 columns.
     */
    private static TestTable twoByFour;

    /**
     * The column names.
     */
    private String[] columnNames;

    /**
     * The value types.
     */
    private ValueTypeId[] valueTypes;

    /**
     * The data rows.
     */
    private Object[][] dataRows;

    /**
     * Initializes static members of the TestTable class.
     */
    static {
      Object[][] threeByThreeRows = new Object[][] {new Object[] {1, "Foo", 3.5}, new Object[] {2, "Fie", 100.0},
        new Object[] {ValueType.INT.getInvalidValue(), "Fum", Double.NaN}};

      TestTable.threeByThree = new TestTable(new String[] {"A", "B", "C"},
        new ValueTypeId[] {ValueTypeId.INT_TYPE, ValueTypeId.STRING_TYPE, ValueTypeId.DOUBLE_TYPE}, threeByThreeRows);

      Object[][] twoByFourRows = new Object[][] {
        new Object[] {1, "Foo", ValueType.DOUBLE.replacedValue(3.5), new java.util.Date(123450000)},
        new Object[] {2, "Fie", 100.0, ValueType.DATETIME.errorValue("illegal value")}};

      TestTable.twoByFour = new TestTable(new String[] {"A", "B", "C", "D"},
        new ValueTypeId[] {ValueTypeId.INT_TYPE, ValueTypeId.STRING_TYPE,
          ValueTypeId.DOUBLE_TYPE, ValueTypeId.DATE_TYPE}, twoByFourRows);
    }

    /**
     * Initializes a new instance of the TestTable class.
     *
     * @param columnNames The column names.
     * @param valueTypes The value types, as strings.
     * @param dataRows The data rows. Null means invalid value.
     */
    public TestTable(String[] columnNames, ValueTypeId[] valueTypes, Object[][] dataRows) {
      this.columnNames = columnNames;
      this.valueTypes = valueTypes;
      assertEquals(columnNames.length,
        valueTypes.length,
        "Must have the same number of column names and value types.");

      this.dataRows = dataRows;
      for (Object[] dataRow : dataRows) {
        assertEquals(dataRow.length, columnNames.length, "Row does not contain the correct number of columns.");
      }
    }

    /**
     * Gets a standard test table with three rows and three columns.
     *
     * @return TestTable
     */
    public static TestTable getThreeByThree() {
      return TestTable.threeByThree;
    }

    /**
     * Gets a standard test table with two rows and four columns.
     *
     * @return TestTable
     */
    public static TestTable getTwoByFour() {
      return TestTable.twoByFour;
    }

    /**
     * Gets the list of all tables.
     *
     * @return TestTable
     */
    public static TestTable[] getTables() {
      return new TestTable[] {getThreeByThree(), getTwoByFour()};
    }

    /**
     * Gets the number of columns in the test table.
     *
     * @return column count
     */
    public final int getColumnCount() {
      return columnNames.length;
    }

    /**
     * Gets the number of rows in the test table.
     *
     * @return row count
     */
    public final int getRowCount() {
      return dataRows.length;
    }

    /**
     * Gets the column names.
     *
     * @return column names
     */
    public final String[] getColumnNames() {
      return columnNames;
    }

    /**
     * Gets the value types.
     *
     * @return value types
     */
    public final ValueTypeId[] getValueTypes() {
      return valueTypes;
    }

    /**
     * Gets the data rows.
     *
     * @return data rows
     */
    public final Object[][] getDataRows() {
      return dataRows;
    }
  }
}
