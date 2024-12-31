/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;



/**
 * Abstract base class for the SBDF section headers. Also provides some internal static utility methods.
 */
public abstract class SectionHeader {
  /**
   * Initializes a new instance of the SectionHeader class.
   */
  SectionHeader() {
    // Empty.
  }

  /**
   * Gets the type tag used to identify a section header.
   * 
   * @return the type id of the section header
   */
  public abstract SectionTypeId getSectionType();

  /**
   * Reads a section header from a BinaryReader. The section type is inferred from the type tag.
   * 
   * @param reader The BinaryReader.
   * @return A new section header.
   */
  public static SectionHeader readSectionHeader(BinaryReader reader) {
    SectionTypeId type = readSectionType(reader);
    return readSectionHeaderBody(reader, type);
  }

  /**
   * Reads a section header of a certain type from an BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param expectedType The expected section type.
   * @return A new section header instance.
   */
  public static SectionHeader readSectionHeader(BinaryReader binaryReader, SectionTypeId expectedType) {
    readSectionType(binaryReader, expectedType);
    return readSectionHeaderBody(binaryReader, expectedType);
  }

  /**
   * Writes a section header to an BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param section The section header.
   */
  public static void writeSectionHeader(BinaryWriter writer, SectionHeader section) {
    Robustness.validateArgumentNotNull("writer", writer);
    Robustness.validateArgumentNotNull("section", section);

    section.writeInstance(writer);
  }

  /**
   * Reads and validates the Sbdf magic number from a BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   */
  static void readMagicNumber(BinaryReader binaryReader) {
    short s = binaryReader.readInt16();
    if (s != 0x5bdf) {
      throw new FormatException(String.format("Expected magic number 0x5bdf, read %s.", s));
    }
  }

  /**
   * Writes the Sbdf magic number to a BinaryWriter.
   * 
   * @param binaryWriter The BinaryWriter.
   */
  static void writeMagicNumber(BinaryWriter binaryWriter) {
    binaryWriter.writeInt16((short) 0x5bdf);
  }

  /**
   * Reads a section type tag (including the magic number) from an BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @return The section type.
   */
  static SectionTypeId readSectionType(BinaryReader binaryReader) {
    readMagicNumber(binaryReader);

    int type = binaryReader.readSByte();

    if (SectionTypeId.isUnknownSectionTypeId(type)) {
      throw new FormatException(String.format("Unrecognized section type: %s.", type));
    } else {
      return SectionTypeId.forValue(type);
    }
  }

  /**
   * Reads a validates a section type tag (including the magic number) from a BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @param expectedType The expected section type.
   */
  static void readSectionType(BinaryReader binaryReader, SectionTypeId expectedType) {
    SectionTypeId type = readSectionType(binaryReader);

    verifySectionType(type, expectedType);
  }

  /**
   * Verifies that the type matches the expectedType.
   * 
   * @param type The actual section type.
   * @param expectedType The expected section type.
   * @throws IllegalArgumentException if the actual and passed types differ.
   */
  static void verifySectionType(SectionTypeId type, SectionTypeId expectedType) {
    if (type != expectedType) {
      throw Robustness.illegalArgumentException("Expected section type %s, read %s.", expectedType, type);
    }
  }

  /**
   * Writes a section type tag (including the magic number) to a BinaryWriter.
   * 
   * @param binaryWriter The BinaryWriter.
   * @param type The section type.
   */
  static void writeSectionType(BinaryWriter binaryWriter, SectionTypeId type) {
    writeMagicNumber(binaryWriter);
    binaryWriter.writeSByte((byte) type.getValue());
  }

  /**
   * Reads a section header body of a specified type from an BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @param type The section type.
   * @return A new section header instance.
   */
  static SectionHeader readSectionHeaderBody(BinaryReader reader, SectionTypeId type) {
    switch (type) {
      case FILE_HEADER:
        return FileHeader.readAfterTypeCheck(reader);

      default:
        throw Robustness.illegalArgumentException("Unrecognized section type: %s", type);
    }
  }

  /**
   * Writes a section header to an BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   */
  abstract void writeInstance(BinaryWriter writer);
}
