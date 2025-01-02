/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the FileHeader class.
 */
public class FileHeaderTest {

  /**
   * Verifies that the constructors validate their arguments correctly.
   */
  @Test
  public final void testConstruction() {
    FileHeader header;

    // Default format version should be 1.0.
    header = new FileHeader();
    assertEquals(1, header.getMajorVersion());
    assertEquals(0, header.getMinorVersion());
    assertEquals(1, FileHeader.CURRENT_MAJOR_VERSION);
    assertEquals(0, FileHeader.CURRENT_MINOR_VERSION);

    // Explicit format version is okay if valid.
    header = new FileHeader(1, 0);
    assertEquals(1, header.getMajorVersion());
    assertEquals(0, header.getMinorVersion());

    // An invalid or future format version is not allowed.
    try {
      header = new FileHeader(2, 1);
      fail("Creating a FileHeader with a future version should fail.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    assertEquals(SectionTypeId.FILE_HEADER, header.getSectionType());
  }

  /**
   * Verifies that reading and writing a FileHeader works as expected.
   */
  @Test
  public final void testReadWrite() {
    FileHeader header = new FileHeader();

    // Verify content before write.
    assertEquals(1, header.getMajorVersion());
    assertEquals(0, header.getMinorVersion());

    // Write header.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryWriter writer = new BinaryWriter(outputStream);
    header.write(writer);

    // Read header.
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    BinaryReader reader = new BinaryReader(inputStream);
    header = FileHeader.read(reader);

    // Verify content after read.
    assertEquals(1, header.getMajorVersion());
    assertEquals(0, header.getMinorVersion());
  }

  @Test
  public final void testRobustness() {
    try {
      FileHeader.read(new BinaryReader(new ByteArrayInputStream(new byte[0])));
      fail("FileHeader.read(new BinaryReader(new ByteArrayInputStream(new byte[0]))) should fail");
    } catch (FormatException e) {
      // expected
    }
    try {
      FileHeader.read(new BinaryReader(new ByteArrayInputStream(
          new byte[] {(byte) 0xdf, (byte) 0x5b, (byte) 0xea})));
      fail("FileHeader.read(new BinaryReader(new ByteArrayInputStream("
          + "new byte[] {(byte) 0xdf, (byte) 0x5b, (byte) 0xea}))) should fail");
    } catch (FormatException e) {
      // expected
    }
  }
}
