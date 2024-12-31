/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.IOException;
import java.io.OutputStream;



/**
 * A class that implements the writing of values in SBDF format to an OutputStream.
 */
public final class BinaryWriter {
  
  /**
   * The stream that data is written to.
   */
  private OutputStream stream;

  /**
   * Create a BinaryWriter for the given stream.
   * 
   * @param stream the stream that data will be written to.
   */
  public BinaryWriter(OutputStream stream) {
    Robustness.validateArgumentNotNull("stream", stream);
    this.stream = stream;
  }
  
  /**
   * Get the OutputStream that this BinaryWriter writes data to.
   * 
   * @return the stream.
   */
  public OutputStream getStream() {
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
   * Writes a boolean value to the stream.
   * 
   * @param boolValue the value.
   */
  void writeBool(boolean boolValue) {
    try {
      stream.write((byte) (boolValue ? 1 : 0));
    } catch (IOException e) {
      throw new SerializationException("IOException writing boolan value");
    }
  }

  /**
   * Writes a signed byte value to the stream, i.e. a byte in Java, an sbyte in .NET.
   * 
   * @param byteValue the value.
   */
  void writeSByte(byte byteValue) {
    try {
      stream.write(byteValue);
    } catch (IOException e) {
      throw new SerializationException("IOException writing byte value");
    }
  }

  /**
   * Writes a short value to the stream.
   * 
   * @param shortValue the value.
   */
  void writeInt16(short shortValue) {
    try {
      stream.write((byte) (shortValue));
      stream.write((byte) (shortValue >>> 8));
    } catch (IOException e) {
      throw new SerializationException("IOException writing short value");
    }
  }

  /**
   * Writes an integer value to the stream.
   * 
   * @param intValue the value.
   */
  void writeInt32(int intValue) {
    byte[] bytes = new byte[4];
    ByteArrayConverter.intToByteArray(intValue, bytes, 0);
    try {
      stream.write(bytes);
    } catch (IOException e) {
      throw new SerializationException("IOException writing integer value");
    }
  }

  /**
   * Writes a long value to the stream.
   * 
   * @param longValue the value.
   */
  void writeInt64(long longValue) {
    byte[] bytes = new byte[8];
    ByteArrayConverter.longToByteArray(longValue, bytes, 0);
    try {
      stream.write(bytes);
    } catch (IOException e) {
      throw new SerializationException("IOException writing long value");
    }
  }

  /**
   * Writes a float value to the stream.
   * 
   * @param floatValue the value.
   */
  void writeFloat(float floatValue) {
    byte[] bytes = new byte[4];
    ByteArrayConverter.floatToByteArray(floatValue, bytes, 0);
    try {
      stream.write(bytes);
    } catch (IOException e) {
      throw new SerializationException("IOException writing float value");
    }
  }

  /**
   * Writes a double value to the stream.
   * 
   * @param doubleValue the value.
   */
  void writeDouble(double doubleValue) {
    byte[] bytes = new byte[8];
    ByteArrayConverter.doubleToByteArray(doubleValue, bytes, 0);
    try {
      stream.write(bytes);
    } catch (IOException e) {
      throw new SerializationException("IOException writing double value");
    }
  }

  /**
   * Writes a byte array to the output stream without prepending the length.
   * 
   * @param packedArray the arrary to write
   */
  void write(byte[] packedArray) {
    try {
      stream.write(packedArray);
    } catch (IOException e) {
      throw new SerializationException("IOException writing byte array");
    }
  }

  /**
   * Flush the underlying stream.
   * @throws IOException if an error is encountered.
   */
  public void flush() throws IOException {
    stream.flush();
  }

}
