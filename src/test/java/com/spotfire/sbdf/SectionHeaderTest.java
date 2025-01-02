/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/** 
 * Test the section header.
 */
public class SectionHeaderTest {

  private static byte[] stm = new byte[] {(byte) 0xdf, (byte) 0x5b, (byte) 0x01, (byte) 0x01, (byte) 0x00};
  private static byte[] stm2 = new byte[] {(byte) 0xdd, (byte) 0x5b, (byte) 0x01, (byte) 0x01, (byte) 0x00};
  private static byte[] stm3 = new byte[] {(byte) 0xdf, (byte) 0x5b, (byte) 0x99, (byte) 0x01, (byte) 0x00};

  @Test
  public final void testRead() {
    ByteArrayInputStream stream = new ByteArrayInputStream(stm);
    BinaryReader reader = new BinaryReader(stream);
    SectionHeader.readSectionHeader(reader);
    stream.reset();
    SectionHeader.readMagicNumber(reader);
    stream.reset();
    SectionHeader.readSectionHeader(reader, SectionTypeId.FILE_HEADER);
  }

  @Test
  public final void testWrite() {
    byte[] result;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    BinaryWriter writer = new BinaryWriter(stream);
    (new FileHeader()).write(writer);
    result = ((ByteArrayOutputStream) writer.getStream()).toByteArray();
    assertArrayEquals(stm, result);
  }

  @Test
  public final void testRobustness() {

    try {
      SectionHeader.readSectionHeader(new BinaryReader(new ByteArrayInputStream(stm2)));
      fail("SectionHeader.readSectionHeader(new BinaryReader(new ByteArrayInputStream(stm2))) should fail");
    } catch (FormatException e) {
      // expected
    }

    try {
      SectionHeader.readSectionHeader(new BinaryReader(new ByteArrayInputStream(stm)),
          SectionTypeId.TABLE_METADATA);
      fail(
          "SectionHeader.readSectionHeader(new BinaryReader(new ByteArrayInputStream(stm)),"
          + " SectionTypeId.TABLE_METADATA) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      SectionHeader.readSectionHeader(new BinaryReader(new ByteArrayInputStream(stm3)));
      fail("SectionHeader.readSectionHeader(new BinaryReader(new ByteArrayInputStream(stm3))) should fail");
    } catch (FormatException e) {
      // expected
    }

    try {
      SectionHeader.readSectionHeader(
          new BinaryReader(new ByteArrayInputStream(
              new byte[] {(byte) 0xdf, 0x5b, (byte) SectionTypeId.TABLE_END.getValue()})), SectionTypeId.TABLE_END);
      fail("SectionHeader.readSectionHeader("
          + "new BinaryReader(new ByteArrayInputStream("
          + "new byte[] {(byte) 0xdf, 0x5b, (byte)SectionTypeId.TABLE_END.getValue()})), "
          + "SectionTypeId.TABLE_END) should fail");

    } catch (IllegalArgumentException e) {
      // expected
    }

  }
}
