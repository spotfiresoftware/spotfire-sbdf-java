/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf.ApiTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import com.spotfire.sbdf.BinaryReader;
import com.spotfire.sbdf.BinaryWriter;

/**
 * This file contains basic API scenarios used for design purposes, and also verifies that the API works as expected.
 */
public class MixedApiTest {

  private BasicApiTest basic = new BasicApiTest();

  private AdvancedApiTest advanced = new AdvancedApiTest();

  @Test
  public final void testWriteBasicReadAdvanced() throws Exception {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(outputStream);
    for (BasicApiTest.TestTable table : BasicApiTest.TestTable.getTables()) {
      basic.writeFileForDummies(bw, table);
    }

    bw.flush();

    byte[] data = ((ByteArrayOutputStream) bw.getStream()).toByteArray();

    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
    BinaryReader br = new BinaryReader(inputStream);
    for (AdvancedApiTest.TestTable  table : AdvancedApiTest.TestTable.getTables()) {
      advanced.readFileAdvanced(br, table);
    }
  }

  @Test
  public final void testReadBasicWriteAdvanced() throws Exception {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(outputStream);
    for (AdvancedApiTest.TestTable table : AdvancedApiTest.TestTable.getTables()) {
      advanced.writeFileAdvanced(bw, table);
    }

    bw.flush();

    byte[] data = ((ByteArrayOutputStream) bw.getStream()).toByteArray();

    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
    BinaryReader br = new BinaryReader(inputStream);
    for (BasicApiTest.TestTable table : BasicApiTest.TestTable.getTables()) {
      basic.readFileForDummies(br, table);
    }
  }
}
