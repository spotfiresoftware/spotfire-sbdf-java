/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.IOException;
import java.io.InputStream;

/**
 * A class that implements the reading of values in SBDF format from an InputStream.
 *
 */
public final class BinaryReader {

  /**
   * The stream that data is read from.
   */
  private InputStream stream;

  /**
   * Create a BinaryReader for the given stream.
   * 
   * @param stream the stream that data will be read from.
   */
  public BinaryReader(InputStream stream) {
    Robustness.validateArgumentNotNull("stream", stream);
    this.stream = stream;
  }
  
  /**
   * Get the InputStream that this BinaryReader reads data from.
   * 
   * @return the stream.
   */
  public InputStream getStream() {
    return stream;
  }

  /**
   * Close the underlying stream.
   * 
   * @throws IOException if there was an error
   */
  public void close() throws IOException {
    stream.close();
  }
  
  /**
   * Reads a boolean value from the stream.
   * 
   * @return the value
   */
  boolean readBool() {
    return (this.readSByte() == 1);
  }

  /**
   * Reads a signed byte value (i.e. a byte in Java, an sbyte in .NET) from the stream.
   * 
   * @return the value
   */
  byte readSByte() {
    try {
      int val = stream.read();
      if (val == -1) {
        throw new FormatException("Unexpected end of file");
      }
      return (byte) val;
    } catch (IOException e) {
      throw new SerializationException("IOException reading byte value");
    }
  }

  /**
   * Reads a short value from the stream.
   * 
   * @return the value
   */
  short readInt16() {
    byte b1 = readSByte();
    byte b2 = readSByte();
    return (short) (((b2 << 8)) + (b1 & 0xff));
  }

  /**
   * Reads an integer value from the stream.
   * 
   * @return the value
   */
  int readInt32() {
    byte[] bytes = read(4);
    return ByteArrayConverter.intFromByteArray(bytes, 0);
  }

  /**
   * Reads a long value from the stream.
   * 
   * @return the value
   */
  long readInt64() {
    byte[] bytes = read(8);
    return ByteArrayConverter.longFromByteArray(bytes, 0);
  }

  /**
   * Reads a float value from the stream.
   * 
   * @return the value
   */
  float readFloat() {
    byte[] bytes = read(4);
    return ByteArrayConverter.floatFromByteArray(bytes, 0);
  }

  /**
   * Reads a double value from the stream.
   * 
   * @return the value
   */
  double readDouble() {
    byte[] bytes = read(8);
    return ByteArrayConverter.doubleFromByteArray(bytes, 0);
  }

  /**
   * Reads a raw array of bytes, i.e. an array that does not include a length, from the stream. If the expected number
   * of bytes are not available, then an EndOfFileException is thrown.
   * 
   * @param arrayLen the number of bytes to read
   * @return the byte array
   */
  byte[] read(int arrayLen) {
    if (arrayLen == 0) {
      return new byte[0];
    }
    try {
      byte[] bytes = new byte[arrayLen];
      int read = 0;
      do {
        int r = stream.read(bytes, read, arrayLen - read);
        if (r == -1) {
          throw new FormatException("Unexpected end of file");
        } else {
          read += r;
        }
      } while (read < arrayLen);
      return bytes;
    } catch (IOException e) {
      throw new SerializationException("IOException reading byte array");
    }
  }
}
