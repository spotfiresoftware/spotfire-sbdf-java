/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * Helper class to convert various primitive types and arrays of them to/from byte arrays.
 */
final class ByteArrayConverter {
  
  private static final int CSHARP_SINGLE_NAN_INT = 0xffc00000;
  private static final long CSHARP_DOUBLE_NAN_LONG = 0xfff8000000000000L;
  
  private static final byte[] CSHARP_SINGLE_NAN_BYTES = new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0xc0, (byte) 0xff};
  private static final byte[] CSHARP_DOUBLE_NAN_BYTES = new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0xf8, (byte) 0xff};
  
  /**
   * Private default constructor.
   */
  private ByteArrayConverter() {
  }

  /**
   * Converts from short to byte[].
   * 
   * @param shortValue the short that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int shortToByteArray(short shortValue, byte[] dest, int start) {
    int pos = start;
    short value = shortValue;
    dest[pos++] = (byte) value;
    dest[pos++] = (byte) (value >>> 8);
    return pos;
  }

  /**
   * Converts from int to byte[].
   * 
   * @param intValue the int that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int intToByteArray(int intValue, byte[] dest, int start) {
    int pos = start;
    int value = intValue;
    for (int i = 0; i < 4; i++, value >>>= 8) {
      dest[pos++] = (byte) value;
    }
    return pos;
  }

  /**
   * Converts from long to byte[].
   * 
   * @param longValue the long that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int longToByteArray(long longValue, byte[] dest, int start) {
    int pos = start;
    long value = longValue;
    for (int i = 0; i < 8; i++, value >>>= 8) {
      dest[pos++] = (byte) value;
    }
    return pos;
  }

  /**
   * Converts from float to byte[].
   * 
   * @param floatValue the float that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int floatToByteArray(float floatValue, byte[] dest, int start) {
    if (Float.isNaN(floatValue)) {
      // .NET uses a different sign for NaN
      System.arraycopy(CSHARP_SINGLE_NAN_BYTES, 0, dest, start, 4);
      return start + 4;
    } else {
      return intToByteArray(Float.floatToIntBits(floatValue), dest, start);
    }
  }
  
  /**
   * Converts from double to byte[].
   * 
   * @param doubleValue the double that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int doubleToByteArray(double doubleValue, byte[] dest, int start) {
    if (Double.isNaN(doubleValue)) {
      // .NET uses a different sign for NaN
      System.arraycopy(CSHARP_DOUBLE_NAN_BYTES, 0, dest, start, 8);
      return start + 8;
    } else {
      return longToByteArray(Double.doubleToLongBits(doubleValue), dest, start);
    }
  }
  
  /**
   * Converts from boolean[] to byte[]. The value of count determines how many elements are converted.
   * 
   * @param array the booleans that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @param count the number of elements to copy
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int toByteArray(boolean[] array, byte[] dest, int start, int count) {
    int pos = start;
    for (int i = 0; i < count; i++) {
      dest[pos++] = (array[i] ? (byte) 1 : (byte) 0);
    }
    return pos;
  }

  /**
   * Converts from int[] to byte[]. The value of count determines how many elements are converted.
   * 
   * @param array the ints that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @param count the number of elements to copy
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int toByteArray(int[] array, byte[] dest, int start, int count) {
    int pos = start;
    for (int i = 0; i < count; i++) {
      pos = intToByteArray(array[i], dest, pos);
    }
    return pos;
  }

  /**
   * Converts from long[] to byte[]. The value of count determines how many elements are converted.
   * 
   * @param array the longs that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @param count the number of elements to copy
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int toByteArray(long[] array, byte[] dest, int start, int count) {
    int pos = start;
    for (int i = 0; i < count; i++) {
      pos = longToByteArray(array[i], dest, pos);
    }
    return pos;
  }

  /**
   * Converts from float[] to byte[]. The value of count determines how many elements are converted.
   * 
   * @param array the floats that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @param count the number of elements to copy
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int toByteArray(float[] array, byte[] dest, int start, int count) {
    int pos = start;
    for (int i = 0; i < count; i++) {
      pos = floatToByteArray(array[i], dest, pos);
    }
    return pos;
  }

  /**
   * Converts from double[] to byte[]. The value of count determines how many elements are converted.
   * 
   * @param array the doubles that shall be converted
   * @param dest the destination array. This must be large enough to store the bytes
   * @param start the start index in the destination array where the data shall be stored
   * @param count the number of elements to copy
   * @return an index pointing to the first element in the destination array after the stored data 
   * (may not be valid if the data filled the destination completely)
   */
  public static int toByteArray(double[] array, byte[] dest, int start, int count) {
    int pos = start;
    for (int i = 0; i < count; i++) {
      pos = doubleToByteArray(array[i], dest, pos);
    }
    return pos;
  }
  
  /**
   * Converts from byte[] to short. Note that the caller must compute the new position in the
   * byte array.
   * 
   * @param bytes the source array. This must be large enough to contain the value
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return the short value
   */
  public static short shortFromByteArray(byte[] bytes, int start) {
    return  (short) (((bytes[start + 1] << 8) & 0xff00) + ((bytes[start]) & 0xff));
  }
  
  /**
   * Converts from byte[] to int. Note that the caller must compute the new position in the
   * byte array.
   * 
   * @param bytes the source array. This must be large enough to contain the value
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return the int value
   */
  public static int intFromByteArray(byte[] bytes, int start) {
    return  (((bytes[start + 3] << 24)) + ((bytes[start + 2] << 16) & 0x00ff0000) 
        + ((bytes[start + 1] << 8) & 0x0000ff00) + ((bytes[start]) & 0xff));
  }
  
  /**
   * Converts from byte[] to long. Note that the caller must compute the new position in the
   * byte array.
   * 
   * @param bytes the source array. This must be large enough to contain the value
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return the long value
   */
  public static long longFromByteArray(byte[] bytes, int start) {
    return ((((long) bytes[start + 7]) << 56) & 0xff00000000000000L)
      + ((((long) bytes[start + 6]) << 48) & 0x00ff000000000000L)
      + ((((long) bytes[start + 5]) << 40) & 0x0000ff0000000000L)
      + ((((long) bytes[start + 4]) << 32) & 0x000000ff00000000L)
      + ((((long) bytes[start + 3]) << 24) & 0x00000000ff000000L) 
      + ((((long) bytes[start + 2]) << 16) & 0x0000000000ff0000L) 
      + ((((long) bytes[start + 1]) << 8)  & 0x000000000000ff00L)
      + ((bytes[start])       & 0x00000000000000ffL);
  }
  
  /**
   * Converts from byte[] to float. Note that the caller must compute the new position in the
   * byte array.
   * 
   * @param bytes the source array. This must be large enough to contain the value
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return the long value
   */
  public static float floatFromByteArray(byte[] bytes, int start) {
    int intValue = intFromByteArray(bytes, start);
    if (intValue == CSHARP_SINGLE_NAN_INT) {
      return Float.NaN;
    } else {
      return Float.intBitsToFloat(intValue);
    }
  }
  
  /**
   * Converts from byte[] to double. Note that the caller must compute the new position in the
   * byte array.
   * 
   * @param bytes the source array. This must be large enough to contain the value
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return the long value
   */
  public static double doubleFromByteArray(byte[] bytes, int start) {
    long longValue = longFromByteArray(bytes, start);
    if (longValue == CSHARP_DOUBLE_NAN_LONG) {
      return Double.NaN;
    } else {
      return Double.longBitsToDouble(longValue);
    }
  }
  
  /**
   * Converts from byte[] to boolean[]. The size of dest determines how many elements are converted.
   * 
   * @param bytes the source array. This must be large enough to contain the values needed
   * @param dest the destination of the booleans that will be converted
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return an index into bytes that points to the first element after the converted data
   * (may not be valid if the data filled the byte array completely)
   */
  public static int fromByteArray(byte[] bytes, boolean[] dest, int start) {
    int pos = start;
    for (int i = 0; i < dest.length; i++) {
      dest[i] = (bytes[pos++] == 1);
    }
    return pos;
  }
  
  /**
   * Converts from byte[] to int[]. The size of dest determines how many elements are converted.
   * 
   * @param bytes the source array. This must be large enough to contain the values needed
   * @param dest the destination of the ints that will be converted
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return an index into bytes that points to the first element after the converted data
   * (may not be valid if the data filled the byte array completely)
   */
  public static int fromByteArray(byte[] bytes, int[] dest, int start) {
    int pos = start;
    for (int i = 0; i < dest.length; i++) {
      dest[i] = intFromByteArray(bytes, pos);
      pos += 4;
    }
    return pos;
  }
  
  /**
   * Converts from byte[] to long[]. The size of dest determines how many elements are converted.
   * 
   * @param bytes the source array. This must be large enough to contain the values needed
   * @param dest the destination of the longs that will be converted
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return an index into bytes that points to the first element after the converted data
   * (may not be valid if the data filled the byte array completely)
   */
  public static int fromByteArray(byte[] bytes, long[] dest, int start) {
    int pos = start;
    for (int i = 0; i < dest.length; i++) {
      dest[i] = longFromByteArray(bytes, pos);
      pos += 8;
    }
    return pos;
  }
  
  /**
   * Converts from byte[] to float[]. The size of dest determines how many elements are converted.
   * 
   * @param bytes the source array. This must be large enough to contain the values needed
   * @param dest the destination of the floats that will be converted
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return an index into bytes that points to the first element after the converted data
   * (may not be valid if the data filled the byte array completely)
   */
  public static int fromByteArray(byte[] bytes, float[] dest, int start) {
    int pos = start;
    for (int i = 0; i < dest.length; i++) {
      dest[i] = floatFromByteArray(bytes, pos);
      pos += 4;
    }
    return pos;
  }

  /**
   * Converts from byte[] to double[]. The size of dest determines how many elements are converted.
   * 
   * @param bytes the source array. This must be large enough to contain the values needed
   * @param dest the destination of the doubles that will be converted
   * @param start the start index in bytes from which the bytes shall be retrieved
   * @return an index into bytes that points to the first element after the converted data
   * (may not be valid if the data filled the byte array completely)
   */
  public static int fromByteArray(byte[] bytes, double[] dest, int start) {
    int pos = start;
    for (int i = 0; i < dest.length; i++) {
      dest[i] = doubleFromByteArray(bytes, pos);
      pos += 8;
    }
    return pos;
  }
}
