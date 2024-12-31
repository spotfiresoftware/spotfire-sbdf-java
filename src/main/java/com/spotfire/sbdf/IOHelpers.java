/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Defines low-level methods for reading and writing sbdf data.
 */
public final class IOHelpers {

  /**
   * Private constructor to prevent construction.
   */
  private IOHelpers() {

  }

  /**
   * An empty byte array.
   */
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  /**
   * Max significand.
   */
  private static final BigInteger MAX_SIGNIFICAND = new BigInteger("9999999999999999999999999999999999");
  
  /**
   *  The number of milliseconds in a day.
   */
  private static final long ONE_DAY_IN_MILLIS = (24 * 60 * 60 * 1000);
  
  /**
   * Advances the reader by pos bytes.
   * 
   * @param reader The BinaryReader.
   * @param pos The number of bytes to advance.
   */
  static void advance(BinaryReader reader, int pos) {
    reader.read(pos);
  }

  /**
   * Writes a value to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param valueType The ValueType, specifying the type of the value object.
   * @param value The value to write.
   */
  static void writeValue(BinaryWriter writer, ValueType valueType, Object value) {

    switch (valueType.getTypeId()) {
      case BOOL_TYPE:
        writer.writeBool((Boolean) value);
        break;

      case INT_TYPE:
        writer.writeInt32((Integer) value);
        break;

      case LONG_TYPE:
        writer.writeInt64((Long) value);
        break;

      case FLOAT_TYPE:
        writer.writeFloat((Float) value);
        break;

      case DOUBLE_TYPE:
        writer.writeDouble((Double) value);
        break;

      case DECIMAL_TYPE:
        writeDecimal(writer, (BigDecimal) value);
        break;

      case DATETIME_TYPE:
      case DATE_TYPE:
        writeDateTime(writer, (Date) value);
        break;

      case TIME_TYPE:
        writeTime(writer, (Date) value);
        break;

      case TIMESPAN_TYPE:
        writeTimeSpan(writer, (TimeSpan) value);
        break;

      case STRING_TYPE:
        writeString(writer, (String) value);
        break;

      case BINARY_TYPE:
        writeBinaryData(writer, (byte[]) value);
        break;

      default:
        throw new SerializationException(String.format("Type %s is not supported", valueType));

    }
  }

  /**
   * Writes an array of values to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param valueType The ValueType, specifying the type of the value objects.
   * @param array The array of values to write.
   * @param length The number of items in the array
   */
  static void writeArray(BinaryWriter writer, ValueType valueType, TypedArray array, int length) {
    writer.writeInt32(length);

    switch (valueType.getTypeId()) {
      case BOOL_TYPE: {
        boolean[] typed = array.getBoolArray();
        byte[] ser = new byte[length * getTypeSize(valueType)];
        ByteArrayConverter.toByteArray(typed, ser, 0, length);
        writer.write(ser);
        break;
      }
      case INT_TYPE: {
        int[] typed = array.getIntArray();
        byte[] ser = new byte[length * getTypeSize(valueType)];
        ByteArrayConverter.toByteArray(typed, ser, 0, length);
        writer.write(ser);
        break;
      }
      case LONG_TYPE: {
        long[] typed = array.getLongArray();
        byte[] ser = new byte[length * getTypeSize(valueType)];
        ByteArrayConverter.toByteArray(typed, ser, 0, length);
        writer.write(ser);
        break;
      }
      case FLOAT_TYPE: {
        float[] typed = array.getFloatArray();
        byte[] ser = new byte[length * getTypeSize(valueType)];
        ByteArrayConverter.toByteArray(typed, ser, 0, length);
        writer.write(ser);
        break;
      }
      case DOUBLE_TYPE: {
        double[] typed = array.getDoubleArray();
        byte[] ser = new byte[length * getTypeSize(valueType)];
        ByteArrayConverter.toByteArray(typed, ser, 0, length);
        writer.write(ser);
        break;
      }
      case DECIMAL_TYPE: {
        BigDecimal[] typed = array.getDecimalArray();
        byte[] ser = new byte[length * getTypeSize(valueType)];
        int index = 0;
        for (int i = 0; i < length; i++) {
          BigDecimal v = typed[i];
          index = writeDecimal(ser, index, v);
        }
        writer.write(ser);
        break;
      }
      case DATETIME_TYPE:
      case DATE_TYPE: {
        Date[] typed = array.getDateArray();
        byte[] ser = new byte[length * getTypeSize(ValueType.DATETIME)];
        int index = 0;
        for (int i = 0; i < length; i++) {
          Date v = typed[i];
          index = writeDateTime(ser, index, v);
        }
        writer.write(ser);
        break;
      }
      case TIME_TYPE: {
        Date[] typed = array.getDateArray();
        byte[] ser = new byte[length * getTypeSize(ValueType.TIME)];
        int index = 0;
        for (int i = 0; i < length; i++) {
          Date v = typed[i];
          index = writeTime(ser, index, v);
        }
        writer.write(ser);
        break;
      }
      case TIMESPAN_TYPE: {
        TimeSpan[] typed = array.getTimeSpanArray();
        byte[] ser = new byte[length * getTypeSize(ValueType.TIMESPAN)];
        int index = 0;
        for (int i = 0; i < length; i++) {
          TimeSpan v = typed[i];
          index = writeTimeSpan(ser, index, v);
        }
        writer.write(ser);
        break;
      }
      case STRING_TYPE:
        writeBinaryArray(writer, array.getStringArray(), length);
        break;

      case BINARY_TYPE:
        writeBinaryArray(writer, array.getBinaryArray(), length);
        break;

      default:
        throw new SerializationException(String.format("Type %s is not supported", valueType));
    }
  }

  /**
   * Reads a value from the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @param valueType The ValueType, specifying the type of the object to read.
   * @return A value object.
   */
  static Object readValue(BinaryReader reader, ValueType valueType) {

    Object obj = null;

    switch (valueType.getTypeId()) {
      case BOOL_TYPE:
        obj = reader.readBool();
        break;

      case INT_TYPE:
        obj = reader.readInt32();
        break;

      case LONG_TYPE:
        obj = reader.readInt64();
        break;

      case FLOAT_TYPE:
        obj = reader.readFloat();
        break;

      case DOUBLE_TYPE:
        obj = reader.readDouble();
        break;

      case DECIMAL_TYPE:
        obj = readDecimal(reader);
        break;

      case DATETIME_TYPE:
      case DATE_TYPE:
        obj = readDateTime(reader);
        break;

      case TIME_TYPE:
        obj = readTime(reader);
        break;

      case TIMESPAN_TYPE:
        obj = readTimeSpan(reader);
        break;

      case STRING_TYPE:
        obj = readString(reader);
        break;

      case BINARY_TYPE:
        obj = readBinaryData(reader);
        break;

      default:
        throw new SerializationException(String.format("Type %s is not supported", valueType));
    }
    return obj;
  }

  /**
   * Reads an array of values from the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @param valueType The ValueType, specifying the type of the objects to read.
   * @return The array of read values.
   */
  static TypedArray readArray(BinaryReader reader, ValueType valueType) {
    int l = reader.readInt32();
    TypedArray array;
    switch (valueType.getTypeId()) {
      case BOOL_TYPE: {
        boolean[] boolArray = new boolean[l];
        ByteArrayConverter.fromByteArray(reader.read(l * getTypeSize(valueType)), boolArray, 0);
        array = new TypedArray(boolArray);
        break;
      }

      case INT_TYPE: {
        int[] intArray = new int[l];
        ByteArrayConverter.fromByteArray(reader.read(l * getTypeSize(valueType)), intArray, 0);
        array = new TypedArray(intArray);
        break;
      }

      case LONG_TYPE: {
        long[] longArray = new long[l];
        ByteArrayConverter.fromByteArray(reader.read(l * getTypeSize(valueType)), longArray, 0);
        array = new TypedArray(longArray);
        break;
      }

      case FLOAT_TYPE: {
        float[] floatArray = new float[l];
        ByteArrayConverter.fromByteArray(reader.read(l * getTypeSize(valueType)), floatArray, 0);
        array = new TypedArray(floatArray);
        break;
      }

      case DOUBLE_TYPE: {
        double[] doubleArray = new double[l];
        ByteArrayConverter.fromByteArray(reader.read(l * getTypeSize(valueType)), doubleArray, 0);
        array = new TypedArray(doubleArray);
        break;
      }

      case DECIMAL_TYPE: {
        BigDecimal[] decimalArray = new BigDecimal[l];
        int size = getTypeSize(valueType);
        byte[] bytes = reader.read(l * size);
        int index = 0;
        for (int i = 0; i < l; ++i) {
          decimalArray[i] = readDecimal(bytes, index);
          index += size;
        }
        array = new TypedArray(decimalArray);
        break;
      }

      case DATETIME_TYPE:
      case DATE_TYPE: {
        Date[] dateArray = new Date[l];
        int size = getTypeSize(valueType);
        byte[] bytes = reader.read(l * size);
        int index = 0;
        for (int i = 0; i < l; ++i) {
          dateArray[i] = readDateTime(bytes, index);
          index += size;
        }
        array = new TypedArray(dateArray);
        break;
      }

      case TIME_TYPE: {
        Date[] timeArray = new Date[l];
        int size = getTypeSize(valueType);
        byte[] bytes = reader.read(l * size);
        int index = 0;
        for (int i = 0; i < l; ++i) {
          timeArray[i] = readTime(bytes, index);
          index += size;
        }
        array = new TypedArray(timeArray);
        break;
      }

      case TIMESPAN_TYPE: {
        TimeSpan[] timeSpanArray = new TimeSpan[l];
        int size = getTypeSize(valueType);
        byte[] bytes = reader.read(l * size);
        int index = 0;
        for (int i = 0; i < l; ++i) {
          timeSpanArray[i] = readTimeSpan(bytes, index);
          index += size;
        }
        array = new TypedArray(timeSpanArray);
        break;
      }

      case STRING_TYPE: {
        String[] stringArray = new String[l];
        readBinaryArray(reader, stringArray);
        array = new TypedArray(stringArray);
        break;
      }

      case BINARY_TYPE: {
        byte[][] byteArrayArray = new byte[l][];
        readBinaryArray(reader, byteArrayArray);
        array = new TypedArray(byteArrayArray);

        break;
      }
      default:
        throw new SerializationException(String.format("Type %s is not supported", valueType));
    }

    return array;

  }

  /**
   * Skips over the array at the current position of the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @param valueType The value type.
   */
  static void skipArray(BinaryReader reader, ValueType valueType) {
    int l = reader.readInt32();
    if (ValueType.isArrayTypeId(valueType.getTypeId())) {
      // text, string & binary
      skipBinaryArray(reader, l);
    } else {
      advance(reader, l * getTypeSize(valueType));
    }
  }

  /**
   * Writes string data to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param value The string data. If null, an empty string is written.
   */
  static void writeString(BinaryWriter writer, String value) {
    writeBinaryData(writer, ((value == null || "".equals(value)) ? EMPTY_BYTE_ARRAY : getStringAsBytes(value)));
  }

  /**
   * Writes a byte array to the BinaryWriter, including the length.
   * 
   * @param binaryWriter The BinaryWriter.
   * @param array The byte array. If null, an empty byte array is written.
   */
  static void writeBinaryData(BinaryWriter binaryWriter, byte[] array) {
    byte[] bytes = array;
    if (bytes == null) {
      bytes = EMPTY_BYTE_ARRAY;
    }

    binaryWriter.writeInt32(bytes.length);

    if (bytes.length != 0) {
      binaryWriter.write(bytes);
    }
  }

  /**
   * Writes a decimal value to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param value The decimal value.
   */
  static void writeDecimal(BinaryWriter writer, BigDecimal value) {

    byte[] output = new byte[16];
    writeDecimal(output, 0, value);
    
    writer.write(output);
  }

  /**
   * Writes a decimal value to the destination array.
   * 
   * @param dest The destination array.
   * @param start The destination array index.
   * @param value The decimal value.
   * @return The new array index.
   */
  static int writeDecimal(byte[] dest, int start, BigDecimal value) {

    BigDecimal roundedValue = value.round(new MathContext(32, RoundingMode.HALF_EVEN));
    
    int scale = roundedValue.scale();
    // exponent is biased by 6176 and scale is (-1) * exponent 
    int exp = 6176 - scale;
    
    boolean isNegative = false;
    BigInteger unscaledValue = roundedValue.unscaledValue();
    if (unscaledValue.signum() == -1) {
      // significand is negative so we need the absolute value of it
      unscaledValue = unscaledValue.negate();
      isNegative = true;
    }
    
    boolean extended = false;
    byte[] significandBytes = unscaledValue.toByteArray();
    if (significandBytes.length > 14) {
      // Max allowed significand is 9999999999999999999999999999999999 but only
      // test large numbers. This max limits the size of the byte array.
      if (MAX_SIGNIFICAND.compareTo(unscaledValue) == -1) {
        throw new NumberFormatException("Unscaled value is too large");
      }
      
      // test top 3 bits of the number to see if they are 100 which would imply
      // we need to use the extended form
      if ((significandBytes[0] & 0x03) == 0x02
          && (significandBytes[1] & 0x80) == 0x00) {
        extended = true;
      }
    }
    
    // write the output in Little-Endian order
    int i = 0;
    int j = significandBytes.length - 1;
    for (; i < 16 && j > -1; i++, j--) {
      dest[start + i] = significandBytes[j];
    }

    if (extended) {
      // write the bottom bit of the exponent into the top bit of the 14th byte
      dest[start + 13] = (byte) (dest[start + 13] | 0x01 & exp << 7);
      // write the next 8 bits of the exponent into the 15th byte
      dest[start + 14] = (byte) ((exp >>> 1) & 0xff);
      // 16th byte is [sign]11[top 5 bits of exp]
      dest[start + 15] = (byte) ((isNegative ? 0x80 : 0x00) | 0x60 | (exp >>> 9 & 0x1f)); 
    } else {
      // bottom 7 bits of the exponent get shifted left then combined with input[14]
      dest[start + 14] = (byte) ((0x7f & exp << 1) | dest[start + 14]);
      // top 7 bits of the exponent get shifted right then combined with the sign bit
      dest[start + 15] = (byte) ((((0x7f) & (exp >>> 7))) | (isNegative ? (byte) 0x80 : (byte) 0x00));
    }
    return start + 16;
  }

  /**
   * Writes a date time value to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param value The date time value.
   */
  static void writeDateTime(BinaryWriter writer, Date value) {
    long javaTicks = value.getTime();
    if (javaTicks < ValueType.START_DATE) {
      throw new SerializationException(
          "Dates prior to 12:00 AM UTC, January 1st 1583 (by the Gregorian Calendar) are not supported");
    }
    writer.writeInt64(javaTicks + ValueType.DATE_DIFFERENCE);
  }

  /**
   * Writes a date time value to the destination array.
   * 
   * @param dest The destination array.
   * @param start The destination index.
   * @param value The date time value.
   * @return The new index.
   */
  static int writeDateTime(byte[] dest, int start, Date value) {
    long javaTicks = value.getTime();
    if (javaTicks < ValueType.START_DATE) {
      throw new SerializationException(
          "Dates prior to 12:00 AM UTC, January 1st 1583 (by the Gregorian Calendar) are not supported");
    }
    return ByteArrayConverter.longToByteArray(javaTicks + ValueType.DATE_DIFFERENCE, dest, start);
  }

  /**
   * Writes a time value to the BinaryWriter. The expected input is a Date object where the date is set to January 1,
   * 1970 and the time is set to the required time (UTC/GMT).
   * 
   * @param writer The BinaryWriter.
   * @param value A date object representing the time value.
   */
  static void writeTime(BinaryWriter writer, Date value) {
    writer.writeInt64(normalizeTime(value));
  }

  /**
   * Writes a time value to the desination array. The expected input is a Date object where the date is set to
   * January 1, 1970 and the time is set to the required time (UTC/GMT).
   * 
   * @param dest The destination array.
   * @param start The destination index.
   * @param value A date object representing the time value.
   * @return The new index.
   */
  static int writeTime(byte[] dest, int start, Date value) {
    return ByteArrayConverter.longToByteArray(normalizeTime(value), dest, start);
  }

  /**
   * Writes a time span to the BinaryWriter.
   * 
   * @param writer The BinaryWriter.
   * @param value The time span value.
   */
  static void writeTimeSpan(BinaryWriter writer, TimeSpan value) {
    writer.writeInt64(value.getTicks());
  }

  /**
   * Writes a time span to the destination array.
   * 
   * @param dest The destination array.
   * @param start The destination index.
   * @param value The time span value.
   * @return The new index.
   */
  static int writeTimeSpan(byte[] dest, int start, TimeSpan value) {
    return ByteArrayConverter.longToByteArray(value.getTicks(), dest, start);
  }

  /**
   * Reads a string value from the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @return A string value.
   */
  static String readString(BinaryReader binaryReader) {
    byte[] data = readBinaryData(binaryReader);

    try {
      return data == EMPTY_BYTE_ARRAY ? "" : new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new SerializationException("Unsupported encoding");
    }
  }

  /**
   * Skips over the string at the current position of the input stream.
   * 
   * @param reader The BinaryReader.
   */
  static void skipString(BinaryReader reader) {
    skipBinarydata(reader);
  }

  /**
   * Reads a byte array from the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @return A byte array.
   */
  static byte[] readBinaryData(BinaryReader reader) {
    int length = reader.readInt32();

    if (length == 0) {
      return EMPTY_BYTE_ARRAY;
    } else {
      return reader.read(length);
    }
  }

  /**
   * Skips over the byte array at the current position of the BinaryReader.
   * 
   * @param reader The BinaryReader.
   */
  static void skipBinarydata(BinaryReader reader) {
    int length = reader.readInt32();
    advance(reader, length);
  }

  /**
   * Reads a decimal value from the BinaryReader.
   * 
   * @param reader The BinaryReader
   * @return A decimal value.
   */
  static BigDecimal readDecimal(BinaryReader reader) {
    
    byte[] array = reader.read(16);
    return readDecimal(array, 0);
  }

  /**
   * Reads a decimal value from the array.
   * 
   * @param array The array.
   * @param start The array index.
   * @return A decimal value.
   */
  static BigDecimal readDecimal(byte[] array, int start) {

    byte[] arrayMSB = new byte[15];

    // copy most of the data over, reversing the array as we go
    int i = 0;
    int j = 14;
    for (; i < 15 && j > -1; i++, j--) {
      arrayMSB[i] = array[start + j];
    }
    
    boolean isNegative = (array[start + 15] & 0x80) == 0x80;
    
    int top;
    int bottom;
    
    if ((array[start + 15] & 0x60) == 0x60) {
      // extended form
      if ((array[start + 15] & 0x78) == 0x78) {
        throw new NumberFormatException("NaN not supported");
      }
      
      top = (array[start + 15] & 0x1f) << 9;
      bottom = (array[start + 14] << 1) + (array[start + 13] >>> 7);
      // set the top bit of arrayMSB[1] to zero
      arrayMSB[1] = (byte) (arrayMSB[1] & 0x7f);
      // set the MSB to binary 10
      arrayMSB[0] = 2;
    } else {
      top = (array[start + 15] & 0x7f) << 7;
      bottom = array[start + 14] >>> 1;
      // mask out the MSB because only the bottom bit is for the significand
      arrayMSB[0] = (byte) (arrayMSB[0] & 0x01);
    }
    
    int biasedExponent = top + bottom;
    
    int scale = 6176 - biasedExponent;
    
    BigInteger unscaledValue = new BigInteger(arrayMSB);
    if (isNegative) {
      unscaledValue = unscaledValue.negate();
    }
    return new BigDecimal(unscaledValue, scale);
  }

  /**
   * Reads a date time value from the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @return The date time value.
   */
  static Date readDateTime(BinaryReader reader) {
    // convert base from 12:00 A.M., January 1, 0001 GMT to January 1, 1970, 00:00:00 GMT
    long javaTicks = reader.readInt64() - ValueType.DATE_DIFFERENCE;
    if (javaTicks < ValueType.START_DATE) {
      throw new SerializationException("Date out of valid range");
    }
    return new Date(javaTicks);
  }

  /**
   * Reads a date time value from the array.
   * 
   * @param array The array.
   * @param start The array index.
   * @return The date time value.
   */
  static Date readDateTime(byte[] array, int start) {
    // convert base from 12:00 A.M., January 1, 0001 GMT to January 1, 1970, 00:00:00 GMT
    long javaTicks = ByteArrayConverter.longFromByteArray(array, start) - ValueType.DATE_DIFFERENCE;
    if (javaTicks < ValueType.START_DATE) {
      throw new SerializationException("Date out of valid range");
    }
    return new Date(javaTicks);
  }

  /**
   * Reads a time value from the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @return A Date object with date set to January 1, 1970 and the time value set to the time GMT/UTC.
   */
  static Date readTime(BinaryReader reader) {
    long ticks = reader.readInt64();
    return new Date(ticks);
  }

  /**
   * Reads a time value from the array.
   * 
   * @param array The array.
   * @param start The array index.
   * @return A Date object with date set to January 1, 1970 and the time value set to the time GMT/UTC.
   */
  static Date readTime(byte[] array, int start) {
    long ticks = ByteArrayConverter.longFromByteArray(array, start);
    return new Date(ticks);
  }

  /**
   * Reads a time span value from the BinaryReader.
   * 
   * @param reader The BinaryReader.
   * @return A time span value.
   */
  static TimeSpan readTimeSpan(BinaryReader reader) {
    long ticks = reader.readInt64();
    return new TimeSpan(ticks);
  }

  /**
   * Reads a time span value from the array.
   * 
   * @param array The array.
   * @param start The array index.
   * @return A time span value.
   */
  static TimeSpan readTimeSpan(byte[] array, int start) {
    long ticks = ByteArrayConverter.longFromByteArray(array, start);
    return new TimeSpan(ticks);
  }

  /**
   * Gets the length of a packed integer.
   * 
   * @param val The integer.
   * @return The length in bytes.
   */
  public static int getPackedIntLen(int val) {
    if (val < (1 << 7)) {
      return 1;
    } else if (val < (1 << 14)) {
      return 2;
    } else if (val < (1 << 21)) {
      return 3;
    } else if (val < (1 << 28)) {
      return 4;
    } else {
      return 5;
    }
  }

  /**
   * Writes a packed integer to data.
   * 
   * @param data The buffer to receive the packed integer.
   * @param start The buffer index.
   * @param ival The integer to write.
   * @return the new index into the buffer after the value that was written
   */
  static int writePackedInt(byte[] data, int start, int ival) {
    int index = start;
    int val = ival;
    for (;;) {
      byte v = (byte) (val & 0x7f);
      if (val > 0x7f) {
        data[index++] = (byte) (v | 0x80);
        val >>= 7;
      } else {
        data[index++] = v;
        break;
      }
    }
    return index;
  }

  /**
   * Reads a packed integer from data.
   * 
   * @param data The data buffer, from which the integer is read.
   * @param start The buffer index.
   * @return The read integer value.
   */
  static int readPackedInt(byte[] data, int start) {
    int index = start;
    int result = 0;
    int shl = 0;

    for (;;) {
      byte v = data[index++];
      result |= (v & 0x7f) << shl;
      if ((v & 0x80) == 0x80) {
        shl += 7;
      } else {
        break;
      }
    }

    return result;
  }

  /**
   * Remove the number of days/months/years component from the Date
   * and return a number of ticks that is strictly less than one day. If the 
   * millis in the Date object is negative, then convert this to a positive
   * number that represents the same time.
   * 
   * @param time a Date object representing the given time
   * @return a positive quantity that represents the time in millis
   */
  private static long normalizeTime(Date time) {
    
    // start by removing the number of days
    long normalizedTicks = time.getTime() % ONE_DAY_IN_MILLIS;
    
    if (normalizedTicks >= 0) {
      return normalizedTicks;
    } else {
      return ONE_DAY_IN_MILLIS + normalizedTicks;
    }
  }
  
  /**
   * Gets the byte size of the given value type.
   * 
   * @param valueType The value type instance.
   * @return The byte size.
   */
  private static int getTypeSize(ValueType valueType) {

    int size = 0;

    switch (valueType.getTypeId()) {
      case BOOL_TYPE:
        size = 1;
        break;

      case INT_TYPE:
      case FLOAT_TYPE:
        size = 4;
        break;

      case LONG_TYPE:
      case DATETIME_TYPE:
      case DATE_TYPE:
      case TIME_TYPE:
      case TIMESPAN_TYPE:
      case DOUBLE_TYPE:
        size = 8;
        break;

      case DECIMAL_TYPE:
        size = 16;
        break;

      default:
        throw Robustness.illegalArgumentException("UNKNOWN ValueType.");
    }
    return size;
  }

  /**
   * Reads a packed array of strings.
   * 
   * @param reader The BinaryReader, from which the strings are read.
   * @param strings The array of strings.
   */
  private static void readBinaryArray(BinaryReader reader, String[] strings) {
    int s = strings.length;
    int blockLen = reader.readInt32();
    byte[] data = reader.read(blockLen);
    int index = 0;

    for (int i = 0; i < s; ++i) {
      int l = readPackedInt(data, index);
      index += getPackedIntLen(l);
      try {
        strings[i] = data == EMPTY_BYTE_ARRAY ? "" : new String(data, index, l, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new SerializationException("Unsupported encoding");
      }
      index += l;
    }
  }

  /**
   * Writes a packed array of strings.
   * 
   * @param writer The BinaryWriter.
   * @param strings The array of strings.
   * @param count The number of strings to write.
   */
  private static void writeBinaryArray(BinaryWriter writer, String[] strings, int count) {
    int s = count;
    byte[][] stringsAsBytes = new byte[s][];
    int blockLen = 0;
    for (int i = 0; i < s; ++i) {
      stringsAsBytes[i] = getStringAsBytes(strings[i]);
      int l = stringsAsBytes[i].length;
      blockLen += getPackedIntLen(l) + l;
    }

    int index = 0;
    byte[] block = new byte[blockLen];
    for (int i = 0; i < s; ++i) {
      int length = stringsAsBytes[i].length;
      index = writePackedInt(block, index, length);
      System.arraycopy(stringsAsBytes[i], 0, block, index, length);
      index += length;
    }

    writer.writeInt32(blockLen);
    writer.write(block);
  }

  /**
   * Reads a packed array of binary data.
   * 
   * @param reader The BinaryReader, from which the binary data are read.
   * @param binary The array of binary data.
   */
  private static void readBinaryArray(BinaryReader reader, byte[][] binary) {
    int s = binary.length;
    int blockLen = reader.readInt32();
    byte[] data = reader.read(blockLen);
    int index = 0;

    for (int i = 0; i < s; ++i) {
      int l = readPackedInt(data, index);
      index += getPackedIntLen(l);
      binary[i] = new byte[l];
      System.arraycopy(data, index, binary[i], 0, l);
      index += l;
    }
  }

  /**
   * Writes a packed array of binary data.
   * 
   * @param writer The BinaryWriter.
   * @param binary The array of byte arrays.
   * @param count The number of byte arrays to write.
   */
  private static void writeBinaryArray(BinaryWriter writer, byte[][] binary, int count) {
    int s = count;
    int blockLen = 0;
    for (int i = 0; i < s; ++i) {
      blockLen += getPackedIntLen(binary[i].length) + binary[i].length;
    }

    int index = 0;
    byte[] block = new byte[blockLen];
    for (int i = 0; i < s; ++i) {
      int length = binary[i].length;
      index = writePackedInt(block, index, length);
      System.arraycopy(binary[i], 0, block, index, length);
      index += length;
    }

    writer.writeInt32(blockLen);
    writer.write(block);
  }

  /**
   * Skips an array of data.
   * 
   * @param reader The BinaryReader.
   * @param elements The number of elements in the array.
   */
  private static void skipBinaryArray(BinaryReader reader, int elements) {
    int sz = reader.readInt32();
    IOHelpers.advance(reader, sz);
  }

  /**
   * Do our own UTF-8 encoding. Note that, like the built-in String.getBytes("UTF-8") 
   * method, we replace invalid sequences of characters such as high and
   * low surrogates on their own, with '?'.
   * 
   * @param s the string to be encoded
   * @return a byte array containing the string encoded in UTF-8
   */
  private static byte[] getStringAsBytes(String s) {
    int strlen = s.length();
    int elen = 0;
    char c = 0;
    int count = 0;

    for (int i = 0; i < strlen; i++) {
      c = s.charAt(i);
      if (c < 0x0080) {
        // 7-bit ascii
        elen++;
      } else if (c < 0x0800) {
        // 2-byte encoding required
        elen += 2;
      } else if (Character.isHighSurrogate(c)) {
        // verify that there is a next character and that it is a low surrogate
        if ((i + 1) < strlen && Character.isLowSurrogate(s.charAt(i + 1))) {
          // we will have a surrogate pair which will require 4 bytes
          elen += 4;
          // skip over the next character
          i++;
        } else {
          // the bad character will be replaced by a '?'
          elen++;
        }
      } else if (Character.isLowSurrogate(c)) {
        // low surrogates cannot appear on their own, the character will be replaced by a '?'
        elen++;
      } else {
        elen += 3;
      }
    }

    byte[] data = new byte[elen];
    
    int i = 0;

    // see how far we can get with just 7-bit ascii
    for (; i < strlen; i++) {
      
      c = s.charAt(i);
      
      if (c < 0x0080) {
        
        data[i] = (byte) c;
        
      } else {
        
        // not all ascii
        break;
        
      }
    }
    
    // carry on from where we left off in the previous loop
    count = i;
    for (; i < strlen; i++) {
      
      c = s.charAt(i);
      
      if (c < 0x0080) {
        
        data[count++] = (byte) c;
        
      } else if (c < 0x0800) {
        
        data[count++] = (byte) (0xc0 | ((c >> 0x06) & 0x1f));
        data[count++] = (byte) (0x80 | ((c >> 0x00) & 0x3f));
        
      } else if (Character.isHighSurrogate(c)) {
        
        // a high surrogate must be followed by a low surrogate, if it isn't replace with '?'
        if ((i + 1) < strlen) {
          
          char nextChar = s.charAt(i + 1);
          
          if (Character.isLowSurrogate(nextChar)) {
            
            int codePoint = Character.toCodePoint(c, nextChar);

            data[count++] = (byte) (0xf0 | ((codePoint >> 0x12)));
            data[count++] = (byte) (0x80 | ((codePoint >> 0x0c) & 0x3f));
            data[count++] = (byte) (0x80 | ((codePoint >> 0x06) & 0x3f));
            data[count++] = (byte) (0x80 | (codePoint & 0x3f));

            i++;
          } else {
            
            // the high surrogate must be followed by a low surrogate
            data[count++] = (byte) '?';
            
          }
          
        } else {
          
          // the string cannot end with a high surrogate
          data[count++] = (byte) '?';
          
        }

      } else if (Character.isLowSurrogate(c)) {
        
        // low surrogates cannot appear on their own
        data[count++] = (byte) '?';
      
      } else {
        
        data[count++] = (byte) (0xe0 | ((c >> 0x0c) & 0x0f));
        data[count++] = (byte) (0x80 | ((c >> 0x06) & 0x3f));
        data[count++] = (byte) (0x80 | ((c >> 0x00) & 0x3f));
        
      }
    }
    
    return data;
  }
}
