/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;



/**
 * Represents the file header (which identifies the file type) of an SBDF file.
 */
public final class FileHeader extends SectionHeader {
  /**
   * The current major version of SBDF.
   */
  public static final int CURRENT_MAJOR_VERSION = 1;

  /**
   * The current minor version of SBDF.
   */
  public static final int CURRENT_MINOR_VERSION = 0;

  /**
   * The major format version number.
   */
  private int majorVersion;

  /**
   * The minor format version number.
   */
  private int minorVersion;

  /**
   * A list of format versions, used for validation.
   */
  private static final int[][] VERSION_HISTORY = {new int[] {1, 0}};

  /**
   * Initializes a new instance of the FileHeader class.
   * 
   * @param majorVersion The major format version.
   * @param minorVersion The minor format version.
   */
  FileHeader(int majorVersion, int minorVersion) {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    validate(majorVersion, minorVersion);
  }

  /**
   * Initializes a new instance of the FileHeader class using the current format version.
   */
  FileHeader() {
    this(CURRENT_MAJOR_VERSION, CURRENT_MINOR_VERSION);
    // Empty.
  }

  /**
   * Gets the major version of Sbdf used in this file.
   * 
   * @return the major version.
   */
  public int getMajorVersion() {
    return majorVersion;
  }

  /**
   * Gets the minor version of Sbdf used in this file.
   * 
   * @return the minor version.
   */
  public int getMinorVersion() {
    return minorVersion;
  }

  /* (non-Javadoc)
   * @see com.spotfire.ws.dat.sbdf.SectionHeader#getSectionType()
   */
  @Override
  public SectionTypeId getSectionType() {
    return SectionTypeId.FILE_HEADER;
  }

  /**
   * Reads a file header section from a BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @return A new file header section instance.
   */
  public static FileHeader read(BinaryReader reader) {
    SectionHeader.readSectionType(reader, SectionTypeId.FILE_HEADER);
    return FileHeader.readAfterTypeCheck(reader);
  }

  /**
   * Reads a file header section without the section type from an input stream.
   * 
   * @param reader The BinaryReader.
   * @return A new file header section instance.
   */
  public static FileHeader readAfterTypeCheck(BinaryReader reader) {
    int majorVersion = reader.readSByte();
    int minorVersion = reader.readSByte();
    return new FileHeader(majorVersion, minorVersion);
  }

  /**
   * Writes the current version of the file header to a BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   */
  public static void writeCurrentVersion(BinaryWriter writer) {
    new FileHeader().writeInstance(writer);
  }

  /**
   * Writes this file header to a BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   */
  public void write(BinaryWriter writer) {
    SectionHeader.writeSectionHeader(writer, this);
  }

  /**
   * Validates the content of a FileHeader.
   * 
   * @param majorVersion The major version number.
   * @param minorVersion The minor version number.
   */
  public static void validate(int majorVersion, int minorVersion) {
    for (int i = 0; i < VERSION_HISTORY.length; ++i) {
      if ((majorVersion == VERSION_HISTORY[i][0]) && (minorVersion == VERSION_HISTORY[i][1])) {
        return;
      }
    }

    throw Robustness.illegalArgumentException("%s.%s is not a valid format version.", majorVersion, minorVersion);
  }

  /* (non-Javadoc)
   * @see com.spotfire.ws.dat.sbdf.SectionHeader#writeInstance(java.io.OutputStream)
   */
  @Override
  void writeInstance(BinaryWriter writer) {
    SectionHeader.writeSectionType(writer, SectionTypeId.FILE_HEADER);
    writer.writeSByte((byte) majorVersion);
    writer.writeSByte((byte) minorVersion);
  }
}
