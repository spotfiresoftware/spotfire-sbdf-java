/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf.ApiTest;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.spotfire.sbdf.ArrayTest;
import com.spotfire.sbdf.BinaryReader;
import com.spotfire.sbdf.BinaryWriter;
import com.spotfire.sbdf.ColumnMetadata;
import com.spotfire.sbdf.ColumnSlice;
import com.spotfire.sbdf.ColumnSliceBuilder;
import com.spotfire.sbdf.FileHeader;
import com.spotfire.sbdf.TableMetadata;
import com.spotfire.sbdf.TableMetadataBuilder;
import com.spotfire.sbdf.TableReader;
import com.spotfire.sbdf.TableSlice;
import com.spotfire.sbdf.TableSliceBuilder;
import com.spotfire.sbdf.TypedArray;
import com.spotfire.sbdf.ValueArray;
import com.spotfire.sbdf.ValueType;


/**
 * This file contains advanced API scenarios used for design purposes, and also verifies that the API works as
 * expected.
 */
public class AdvancedApiTest {

  /**
   * Verifies that it is possible to write and read a data table using the simple API.
   * @throws Exception if an error is encountered
   */
  @Test
  public final void testWriteAndReadAdvancedTable() throws Exception {
    for (TestTable table : TestTable.getTables()) {
      final String filePrefix = "hello";
      final String fileSuffix = ".sbdf";
      File file = File.createTempFile(filePrefix, fileSuffix);
      file.deleteOnExit();
      final String fileName = file.getCanonicalPath();
      try (BinaryWriter binaryWriter = new BinaryWriter(new FileOutputStream(file))) {
        writeFileAdvanced(binaryWriter, table);
      }

      file = new File(fileName);
      BinaryReader binaryReader = new BinaryReader(new FileInputStream(file));
      try {
        readFileAdvanced(binaryReader, table);
      } finally {
        binaryReader.close();
      }

      file = new File(fileName);
      binaryReader = new BinaryReader(new FileInputStream(file));
      try {
        readFileAdvanced2(binaryReader, table);
      } finally {
        binaryReader.close();
        file.delete();
      }
    }
  }

  /**
   * This scenario shows how to export a plain table to an SBDF file, using the column-based advanced API.
   *
   * @param binaryWriter The output binary writer, owned by the caller.
   * @param table The table to be exported.
   */
  public final void writeFileAdvanced(BinaryWriter binaryWriter, TestTable table) {
    // Write the file header.
    FileHeader.writeCurrentVersion(binaryWriter);

    // Create the table metadata.
    TableMetadataBuilder tableMetadataBuilder = new TableMetadataBuilder();
    for (int columnIndex = 0; columnIndex < table.getColumnCount(); ++columnIndex) {
      String columnName = table.getColumnNames()[columnIndex];
      ValueType valueType = ValueType.getValueType(table.getValueTypes()[columnIndex]);
      tableMetadataBuilder.addColumn(new ColumnMetadata(columnName, valueType));
    }

    TableMetadata tableMetadata = tableMetadataBuilder.build();

    // Write the table metadata
    tableMetadata.write(binaryWriter);

    // Create the table slice
    TableSliceBuilder tsb = new TableSliceBuilder(tableMetadata);
    for (int columnIndex = 0; columnIndex < table.getColumnCount(); ++columnIndex) {
      ValueArray values = ValueArray.createDefaultArrayEncoding(
          tableMetadata.getColumns()[columnIndex].getDataType(),
          table.getDataColumns()[columnIndex]);
      ColumnSliceBuilder csb = new ColumnSliceBuilder(values);

      // Write the invalid values, representing missing rows. Value type is bool.
      if (table.getInvalidValues()[columnIndex] != null) {
        csb.setInvalidValues(ValueArray.createDefaultArrayEncoding(
            new TypedArray(table.getInvalidValues()[columnIndex])));
      }

      // Write the replaced values, representing null-value replaced rows. Value type is bool.
      if (table.getReplacedValues()[columnIndex] != null) {
        csb.setReplacedValues(ValueArray.createDefaultArrayEncoding(
            new TypedArray(table.getReplacedValues()[columnIndex])));
      }

      // Write the error codes, representing a textual stand-in for erroneous values. Value type is string.
      if (table.getErrorCodes()[columnIndex] != null) {
        csb.setErrorCodes(ValueArray.createDefaultArrayEncoding(
            new TypedArray(table.getErrorCodes()[columnIndex])));
      }

      // Add the built column.
      tsb.addColumn(csb.build());
    }

    // Build and write the table slice.
    TableSlice ts = tsb.build();
    ts.write(binaryWriter);
    TableSlice.writeEndOfTableMarker(binaryWriter);
  }

  /**
   * This scenario shows how to import a plain table from an SBDF file, using the column-based advanced API.
   *
   * @param binaryReader The binary reader, from which the table is read.
   * @param table The table reference, used to verify the contents of the read tables.
   */
  public final void readFileAdvanced(BinaryReader binaryReader, TestTable table) {
    FileHeader header = FileHeader.read(binaryReader);
    FileHeader.validate(header.getMajorVersion(), header.getMinorVersion());

    TableMetadata tableMetaData = TableMetadata.read(binaryReader);

    for (TableSlice tableSlice : TableSlice.readSlices(binaryReader, tableMetaData)) {
      for (int column = 0; column < tableSlice.getColumns().size(); ++column) {
        ColumnSlice columnSlice = tableSlice.getColumns().get(column);

        ValueArray values = columnSlice.getValues();
        ArrayTest.checkArraysEqual(values.getValueType(), table.getDataColumns()[column], values.toArray());

        ArrayTest.checkArraysEqual(ValueType.BOOL,
            table.getInvalidValues()[column] == null ? null : new TypedArray(table.getInvalidValues()[column]),
            columnSlice.getInvalidValues() == null ? null : columnSlice.getInvalidValues().toArray());

        ArrayTest.checkArraysEqual(ValueType.BOOL,
            table.getReplacedValues()[column] == null ? null : new TypedArray(table.getReplacedValues()[column]),
            columnSlice.getReplacedValues() == null ? null : columnSlice.getReplacedValues().toArray());

        ArrayTest.checkArraysEqual(ValueType.STRING,
            table.getErrorCodes()[column] == null ? null : new TypedArray(table.getErrorCodes()[column]),
            columnSlice.getErrorCodes() == null ? null : columnSlice.getErrorCodes().toArray());
      }
    }
  }

  /**
   * This test the internal reader methods.
   *
   * @param binaryReader The binary reader, from which the table is read.
   * @param table The table reference, used to verify the contents of the read tables.
   */
  public final void readFileAdvanced2(BinaryReader binaryReader, TestTable table) {
    FileHeader header = FileHeader.read(binaryReader);
    FileHeader.validate(header.getMajorVersion(), header.getMinorVersion());

    TableMetadata tableMetaData = TableMetadata.read(binaryReader);

    TableReader reader = new TableReader(binaryReader, tableMetaData);
    while (reader.readNextTableSlice()) {
      assert reader.getColumnCount() == reader.getColumns().size();

      for (int column = 0; column < reader.getColumnCount(); ++column) {

        ArrayTest.checkArraysEqual(reader.getColumns().get(column).getDataType(),
            table.getDataColumns()[column], reader.getValueArrays()[column]);

        ArrayTest.checkArraysEqual(ValueType.BOOL,
            table.getInvalidValues()[column] == null ? null : new TypedArray(table.getInvalidValues()[column]),
            reader.getIsInvalidArrays()[column] == null
            ? null : new TypedArray(reader.getIsInvalidArrays()[column]));

        ArrayTest.checkArraysEqual(ValueType.BOOL,
            table.getReplacedValues()[column] == null ? null : new TypedArray(table.getReplacedValues()[column]),
            reader.getReplacedValueArrays()[column] == null
            ? null : new TypedArray(reader.getReplacedValueArrays()[column]));

        ArrayTest.checkArraysEqual(ValueType.STRING,
            table.getErrorCodes()[column] == null ? null : new TypedArray(table.getErrorCodes()[column]),
            reader.getErrorCodeArrays()[column] == null
            ? null : new TypedArray(reader.getErrorCodeArrays()[column]));
      }
    }
  }

  /**
   * A simple table class used for testing purposes.
   */
  public static class TestTable {
    /**
     * A test table with 3 columns and 3 rows.
     */
    private static TestTable threeByThree;

    /**
     * A test table with 2 columns and 4 rows.
     */
    private static TestTable twoByFour;

    /**
     * The column names.
     */
    private String[] columnNames;

    /**
     * The value types.
     */
    private String[] valueTypes;

    /**
     * The data columns.
     */
    private TypedArray[] dataColumns;

    /**
     * The invalid values.
     */
    private boolean[][] invalidValues;

    /**
     * The replaced values.
     */
    private boolean[][] replacedValues;

    /**
     * The error values.
     */
    private String[][] errorCodes;

    /**
     * The number of rows.
     */
    private int rowCount;

    /**
     * Initializes static members of the TestTable class.
     */
    static {
      Object[] threeByThreeColumns = new Object[]{new int[]{1, 2, 0}, new String[]{"Foo", "Fie", "Fum"},
              new double[]{3.5, 100.0, Double.NaN}};

      boolean[][] threeByThreeEmpties = new boolean[][]{new boolean[]{false, false, true}, null, null};

      boolean[][] threeByThreeReplaced = new boolean[][]{null, null, null};

      String[][] threeByThreeErrors = new String[][]{null, null, null};

      TestTable.threeByThree = new TestTable(new String[]{"A", "B", "C"},
              new String[]{"Int", "String", "Double"}, threeByThreeColumns, threeByThreeEmpties, threeByThreeReplaced,
              threeByThreeErrors, 3);

      Object[] twoByFourColumns = new Object[]{new int[]{1, 2}, new String[]{"Foo", "Fie"},
              new double[]{3.5, 100.0},
              new java.util.Date[]{new java.util.Date(123450000), new Date(ValueType.START_DATE)}};

      boolean[][] twoByFourEmpties = new boolean[][]{null, null, null, null};

      boolean[][] twoByFourReplaced = new boolean[][]{null, null, new boolean[]{true, false}, null};

      String[][] twoByFourErrors = new String[][]{null, null, null, new String[]{"", "illegal value"}};

      TestTable.twoByFour = new TestTable(new String[]{"A", "B", "C", "D"}, new String[]{"Int", "String",
              "Double", "Date"}, twoByFourColumns, twoByFourEmpties, twoByFourReplaced, twoByFourErrors, 2);
    }

    /**
     * Initializes a new instance of the TestTable class.
     *
     * @param columnNames    The column names.
     * @param valueTypes     The value types, as strings.
     * @param dataColumns    The data columns. Null means invalid value.
     * @param invalidValues  The invalid values.
     * @param replacedValues The replaced values.
     * @param errorValues    The error strings.
     * @param rowCount       The number of rows.
     */
    public TestTable(String[] columnNames, String[] valueTypes, Object[] dataColumns, boolean[][] invalidValues,
                     boolean[][] replacedValues, String[][] errorValues, int rowCount) {
      this.columnNames = columnNames;
      this.valueTypes = valueTypes;
      assert columnNames.length == valueTypes.length;
      assert dataColumns.length == valueTypes.length;

      this.dataColumns = new TypedArray[dataColumns.length];
      for (int i = 0; i < dataColumns.length; i++) {
        this.dataColumns[i] = createTypedArray(dataColumns[i]);
      }
      this.invalidValues = invalidValues;
      this.replacedValues = replacedValues;
      this.errorCodes = errorValues;
      this.rowCount = rowCount;
    }

    private TypedArray createTypedArray(Object object) {
      Class<?> componentType = object.getClass().getComponentType();
      if (componentType.equals(Integer.TYPE)) {
        return new TypedArray((int[]) object);
      } else if (componentType.equals(String.class)) {
        return new TypedArray((String[]) object);
      } else if (componentType.equals(Double.TYPE)) {
        return new TypedArray((double[]) object);
      } else if (componentType.equals(Date.class)) {
        return new TypedArray((Date[]) object);
      }

      fail("Unimplemented array type");
      return null;
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
      return new TestTable[]{getThreeByThree(), getTwoByFour()};
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
      return rowCount;
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
    public final String[] getValueTypes() {
      return valueTypes;
    }

    /**
     * Gets the data rows.
     *
     * @return data columns
     */
    public final TypedArray[] getDataColumns() {
      return dataColumns;
    }

    /**
     * Gets the invalid values.
     *
     * @return invalid values
     */
    public final boolean[][] getInvalidValues() {
      return invalidValues;
    }

    /**
     * Gets the replaced values.
     *
     * @return replaced values
     */
    public final boolean[][] getReplacedValues() {
      return replacedValues;
    }

    /**
     * Gets the error codes.
     *
     * @return error codes
     */
    public final String[][] getErrorCodes() {
      return errorCodes;
    }
  }
}
