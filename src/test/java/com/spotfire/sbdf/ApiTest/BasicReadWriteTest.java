/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf.ApiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.spotfire.sbdf.TestUtil;
import com.spotfire.sbdf.BinaryReader;
import com.spotfire.sbdf.BinaryWriter;
import com.spotfire.sbdf.ColumnMetadata;
import com.spotfire.sbdf.FileHeader;
import com.spotfire.sbdf.MetadataProperty;
import com.spotfire.sbdf.TableMetadata;
import com.spotfire.sbdf.TableMetadataBuilder;
import com.spotfire.sbdf.TableReader;
import com.spotfire.sbdf.TableWriter;
import com.spotfire.sbdf.ValueType;

/**
 * Test the ability of the basic API to read SBDF files generated on other platforms.
 *
 */
public class BasicReadWriteTest {

  @Test
  public final void testReadWrite() throws Exception  {
    File sbdfDir = TestUtil.getFile("files" + File.separator + "sbdf");
    String[] fileNames = sbdfDir.list();
    if (fileNames == null) {
      return;
    }
    for (String readFileName : fileNames) {
      if (readFileName.endsWith(".sbdf")) {
        File readFile = new File(sbdfDir, readFileName);
        File writeFile = File.createTempFile(readFileName, null);
        doReadWrite(readFile, writeFile);
        compareFiles(readFile, writeFile);
        writeFile.delete();
      }
    }
  }

  /**
   * Read the readFile and write out a copy of the data to the writeFile.
   *
   * @param readFile the file to read
   * @param writeFile the file to write
   * @throws IOException if an error was encountered reading or writing to
   * files
   */
  private static void doReadWrite(File readFile, File writeFile) throws IOException {
    BinaryReader br = new BinaryReader(new FileInputStream(readFile));
    FileHeader header = FileHeader.read(br);
    FileHeader.validate(header.getMajorVersion(), header.getMinorVersion());
    TableMetadata tableMetadataIn = TableMetadata.read(br);
    TableReader reader = new TableReader(br, tableMetadataIn);

    BinaryWriter bw = new BinaryWriter(new FileOutputStream(writeFile));

    FileHeader.writeCurrentVersion(bw);

    // Create the table metadata.
    TableMetadataBuilder tableMetadataBuilder = new TableMetadataBuilder();
    // add column metadata
    for (ColumnMetadata cmdIn : reader.getColumns()) {
      String columnName = cmdIn.getName();
      ValueType valueType = cmdIn.getDataType();
      ColumnMetadata cmdOut = new ColumnMetadata(columnName, valueType);
      for (MetadataProperty columnProp : cmdIn.getAssignedProperties()) {
        cmdOut.addProperty(columnProp);
      }
      tableMetadataBuilder.addColumn(cmdOut);
    }

    // add properties
    for (MetadataProperty tableProp : tableMetadataIn) {
      tableMetadataBuilder.addProperty(tableProp);
    }

    TableMetadata tableMetadataOut = tableMetadataBuilder.build();

    tableMetadataOut.write(bw);

    // Use a row-based table writer to export the data.
    TableWriter tableWriter = new TableWriter(bw, tableMetadataOut);
    for (Object value : reader) {
      tableWriter.addValue(value);
    }
    tableWriter.writeEndOfTable();

    br.close();
    bw.close();
  }

  /**
   * Compare two files byte-by-byte.
   *
   * @param expectedFile the file that contains the expected data
   * @param actualFile the file that contains the actual data
   * @throws IOException if an error was encountered reading the files.
   */
  private static void compareFiles(File expectedFile, File actualFile) throws IOException {
    try (FileInputStream expectedStream = new FileInputStream(expectedFile); FileInputStream actualStream = new FileInputStream(actualFile)) {
      long count = 0;
      while (true) {

        int expectedValue = expectedStream.read();
        int actualValue = actualStream.read();
        count++;

        if (expectedValue == -1) {
          assertEquals(-1, actualValue);
          break;
        } else if (actualValue == -1) {
          fail("Actual stream for file " + expectedFile.getName() + " is shorter than expected, it is only "
                  + count + " bytes. Expected is " + (count + expectedStream.available()) + " bytes.");
        } else {
          assertEquals(expectedValue,
                  actualValue,
                  "Streams for " + expectedFile.getName() + " differ at byte " + count);
        }
      }
    }
  }
}
