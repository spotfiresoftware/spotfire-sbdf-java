/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;



/**
 * Defines a class for holding and persisting arrays of values.
 */
public abstract class ValueArray {

  /**
   * The list of known array encodings. The order is the same as in DESERIALIZERS.
   */
  private static final ArrayEncodingTypeId[] ENCODING_TYPE_IDS = 
    new ArrayEncodingTypeId[] {
      ArrayEncodingTypeId.PLAIN_ARRAY_ENCODING_TYPE_ID,
      ArrayEncodingTypeId.RUN_LENGTH_ENCODING_TYPE_ID,
      ArrayEncodingTypeId.BIT_ARRAY_ENCODING_TYPE_ID };

  /**
   * The list of known array deserializers. The order is the same as in ENCODING_TYPE_IDS.
   */
  private static final Deserializer[] DESERIALIZERS = 
    new Deserializer[] {
      new PlainArrayEncodingSpecificDeserializer(),
      new RleArrayEncodingSpecificDeserializer(),
      new PackedBitArrayEncodingSpecificDeserializer() };

  /**
   * The type of the values held by this instance.
   */
  private ValueType valueType;

  /**
   * The array encoding used by this instance.
   */
  private ArrayEncodingTypeId encodingId = ArrayEncodingTypeId.forValue(0);

  /**
   * Initializes a new instance of the ValueArray class.
   * 
   * @param valueType The type of the values in the array.
   * @param encodingId The encoding of the array.
   */
  ValueArray(ValueType valueType, ArrayEncodingTypeId encodingId) {
    this.valueType = valueType;
    this.encodingId = encodingId;
  }

  /**
   * Defines the different array encodings.
   */
  public enum ArrayEncodingTypeId {
    /**
     * Defines the plain array encoding. Arrays of this type are encoded as a normal array (i.e. no encoding).
     */
    PLAIN_ARRAY_ENCODING_TYPE_ID(0x1),

    /**
     * Defines the run length encoding. Arrays of this type are encoded as a list with the number of occurrences
     * followed by the data.
     */
    RUN_LENGTH_ENCODING_TYPE_ID(0x2),

    /**
     * Defines the packed bit array encoding. Arrays of this type are encoded as packed bit arrays. Only arrays of type
     * bool are supported.
     */
    BIT_ARRAY_ENCODING_TYPE_ID(0x3);

    private int intValue;
    private static HashMap<Integer, ArrayEncodingTypeId> mappings;

    /**
     * Returns a mapping between integers and the enum constants they represent.
     * 
     * @return the mapping.
     */
    private static synchronized HashMap<Integer, ArrayEncodingTypeId> getMappings() {
      if (mappings == null) {
        mappings = new HashMap<Integer, ArrayEncodingTypeId>();
      }
      return mappings;
    }

    /**
     * Private constructor.
     * 
     * @param value the serialized value for the encoding id.
     */
    ArrayEncodingTypeId(int value) {
      intValue = value;
      ArrayEncodingTypeId.getMappings().put(value, this);
    }

    /**
     * Returns the value used to represent the enum constant.
     * 
     * @return the value used to represent the enum constant.
     */
    public int getValue() {
      return intValue;
    }

    /**
     * Returns the enum constant for a given value.
     * 
     * @param value the value used to represent the enum constant
     * @return the enum constant corresponding to the given value, or null if it is not valid
     */
    public static ArrayEncodingTypeId forValue(int value) {
      return getMappings().get(value);
    }
  }

  /**
   * Gets the type of the values of the array.
   * 
   * @return the type of the values of the array.
   */
  public final ValueType getValueType() {
    return valueType;
  }

  /**
   * Gets the encoding type id of this array.
   * 
   * @return the encoding type id of this array.
   */
  public final ArrayEncodingTypeId getEncodingId() {
    return encodingId;
  }

  /**
   * Gets the count of the items in the array.
   * 
   * @return the count of the items in the array.
   */
  public abstract int getCount();

  /**
   * Creates a new plain array encoded ValueArray.
   * 
   * @param valueType The type of the values.
   * @param array The values to hold.
   * @return A new ValueArray.
   */
  public static ValueArray createPlainArrayEncoding(ValueType valueType, TypedArray array) {
    Robustness.validateArgumentNotNull("valueType", valueType);
    Robustness.validateArgumentNotNull("array", array);

    return new PlainArray(valueType, array);
  }

  /**
   * Creates a new ValueArray holding a packed bit array.
   * 
   * @param values The bool-values to hold.
   * @param count The number of elements to encode
   * @return A new ValueArray.
   */
  public static ValueArray createBoolArrayEncoding(boolean[] values, int count) {
    Robustness.validateArgumentNotNull("values", values);

    return new PackedBitArray(values, count);
  }

  /**
   * Creates a new ValueArray holding a RLE array.
   * 
   * @param valueType The type of the values to hold.
   * @param array The values to hold.
   * @return A new ValueArray.
   */
  public static ValueArray createRleArrayEncoding(ValueType valueType, TypedArray array) {
    Robustness.validateArgumentNotNull("array", array);

    return new RleArray(valueType, array);
  }

  /**
   * Deserializes an instance of a ValueArray.
   * 
   * @param reader The BinaryReader from which the contents are read.
   * @return A new ValueArray.
   */
  public static ValueArray deserialize(BinaryReader reader) {
    Robustness.validateArgumentNotNull("reader", reader);

    return deserializeHelper(reader, false);
  }

  /**
   * Skips reading an array and advances to the next position in the BinaryReader.
   * 
   * @param reader The BinaryReader from which the contents are skipped.
   */
  public static void skip(BinaryReader reader) {
    Robustness.validateArgumentNotNull("reader", reader);

    deserializeHelper(reader, true);
  }

  /**
   * Creates a default array encoding from the given parameters.
   * 
   * @param array The values to hold.
   * @return A new ValueArray. The type of the values of the array is automatically deduced.
   */
  public static ValueArray createDefaultArrayEncoding(TypedArray array) {
    Robustness.validateArgumentNotNull("array", array);

    ValueTypeId id = ValueTypeId.UNKNOWN_TYPE;

    if (array.getCount() > 0) {
      id = ValueType.getValueTypeId(array.getComponentType());
    }

    return createDefaultArrayEncoding(ValueType.getValueTypeFromId(id), array);
  }

  /**
   * Creates a default array encoding from the given parameters.
   * 
   * @param valueType The type of the values.
   * @param array The values to hold.
   * @return A new ValueArray.
   */
  public static ValueArray createDefaultArrayEncoding(ValueType valueType, TypedArray array) {
    Robustness.validateArgumentNotNull("array", array);

    if (valueType.equals(ValueType.BOOL)) {
      return createBoolArrayEncoding(array.getBoolArray(), array.getCount());
    } else if (valueType.isArrayType() || valueType.isSimpleType() || valueType.isDecimal()) {
      return createPlainArrayEncoding(valueType, array);
    } else {
      throw Robustness.illegalArgumentException("'%s' is not a supported value type.", valueType);
    }
  }

  /**
   * Converts the contents on this instance to a TypedArray.
   * 
   * @return A TypedArray, holding the contents of this instance.
   */
  public abstract TypedArray toArray();

  /**
   * Serializes this instance.
   * 
   * @param writer The BinaryWriter.
   */
  public final void serialize(BinaryWriter writer) {
    Robustness.validateArgumentNotNull("writer", writer);

    for (int i = 0; i < ENCODING_TYPE_IDS.length; ++i) {
      if (encodingId == ENCODING_TYPE_IDS[i]) {
        writer.writeSByte((byte) encodingId.getValue());
        valueType.write(writer);
        serializeEncodingSpecific(writer);
      }
    }
  }

  /**
   * Serializes the encoding specific parts of this instance.
   * 
   * @param writer The BinaryWriter.
   */
  public abstract void serializeEncodingSpecific(BinaryWriter writer);

  /**
   * Deserializes an instance of a ValueArray.
   * 
   * @param binaryReader The BinaryReader from which the contents are read.
   * @param skip true if the data should be overread, false otherwise.
   * @return A new ValueArray or null if the data was overread.
   */
  private static ValueArray deserializeHelper(BinaryReader binaryReader, boolean skip) {
    ArrayEncodingTypeId encodingId = ArrayEncodingTypeId.forValue(binaryReader.readSByte());
    Deserializer deserializer = null;
    for (int i = 0; i < ENCODING_TYPE_IDS.length; ++i) {
      if (encodingId == ENCODING_TYPE_IDS[i]) {
        deserializer = DESERIALIZERS[i];
        break;
      }
    }

    if (deserializer == null) {
      throw new SerializationException(String.format("The array encoding type id %s is unknown.", encodingId));
    }

    ValueType vt = ValueType.read(binaryReader);

    return deserializer.deserialize(vt, binaryReader, skip);
  }

  /**
   * Defines a class for working with arrays encoded as plain arrays (i.e. no encoding)
   */
  static class PlainArray extends ValueArray {

    /**
     * The held array.
     */
    private TypedArray array;
    
    /**
     * Initializes a new instance of the PlainArray class.
     * 
     * @param valueType The type of the held values.
     * @param values The values to hold.
     */
    PlainArray(ValueType valueType, TypedArray values) {
      super(valueType, ArrayEncodingTypeId.PLAIN_ARRAY_ENCODING_TYPE_ID);
      if (!ValueType.getRuntimeType(valueType).equals(values.getComponentType())) {
        throw new ClassCastException(String.format("ValueType '%s' does not match class of TypedArray '%s'",
            valueType, values.getComponentType()));
      }
      array = values;
    }

    /**
     * Gets the number of items in this instance.
     * 
     * @return the number of items in this instance.
     */
    @Override
    public final int getCount() {
      return array.getCount();
    }

    /**
     * Serializes encoding specific parts of this instance.
     * 
     * @param writer The BinaryWriter, receiving the output array.
     */
    @Override
    public final void serializeEncodingSpecific(BinaryWriter writer) {
      IOHelpers.writeArray(writer, getValueType(), array, array.getCount());
    }

    /**
     * Converts the contents of this to a TypedArray.
     * 
     * @return A TypedArray, holding the contents of this instance.
     */
    @Override
    public final TypedArray toArray() {
      return array;
    }
  }

  /**
   * This takes the place of the delegate in the C# implementation.
   */
  private static final class PlainArrayEncodingSpecificDeserializer implements Deserializer {
    
    /**
     * Private constructor.
     */
    private PlainArrayEncodingSpecificDeserializer() {
      
    }

    /**
     * Deserializes encoding specific parts.
     * 
     * @param vt The type of the held values.
     * @param reader The BinaryReader, from which the contents are read.
     * @param skip true if the contents should be overread, false otherwise.
     * @return A new PlainArray.
     */
    @Override
    public PlainArray deserialize(ValueType vt, BinaryReader reader, boolean skip) {
      if (skip) {
        IOHelpers.skipArray(reader, vt);
        return null;
      } else {
        TypedArray array = IOHelpers.readArray(reader, vt);
        return new PlainArray(vt, array);
      }
    }

  }

  /**
   * Defines a class for holding an array of bools as a packed bit array.
   */
  public static class PackedBitArray extends ValueArray {

    /**
     * The number of bools held by this instance.
     */
    private int count;

    /**
     * The packed bit data.
     */
    private byte[] packedArray;

    /**
     * Initializes a new instance of the PackedBitArray class.
     * 
     * @param values The array of bool values.
     * @param countIn The number of elements to encode
     */
    public PackedBitArray(boolean[] values, int countIn) {

      super(ValueType.BOOL, ArrayEncodingTypeId.BIT_ARRAY_ENCODING_TYPE_ID);

      count = countIn;

      int packedLen = getPackedArrayLength(count);
      packedArray = new byte[packedLen];

      // First process entire multiples of 8 bits
      int input = 0;
      int output = 0;
      int remainingBits = count % 8;
      int limit = packedLen - ((remainingBits != 0) ? 1 : 0);
      while (output < limit) {
        int value = 0;
        for (int inner = 0; inner < 8; ++inner) {
          value = value << 1;

          if (values[input++]) {
            value += 1;
          }
        }

        packedArray[output++] = (byte) value;
      }

      // Add final run
      if (remainingBits != 0) {
        int value = 0;
        for (int i = 0; i < remainingBits; ++i) {
          value = value << 1;

          if (values[input++]) {
            value += 1;
          }
        }

        value = value << (8 - remainingBits);

        packedArray[output] = (byte) value;
      }
    }

    /**
     * Initializes a new instance of the PackedBitArray class.
     * 
     * @param count The number of bools in packedArray.
     * @param packedArray The packed bit array.
     */
    private PackedBitArray(int count, byte[] packedArray) {

      super(ValueType.BOOL, ArrayEncodingTypeId.BIT_ARRAY_ENCODING_TYPE_ID);

      this.count = count;
      this.packedArray = packedArray;

    }

    /**
     * Gets the number of items held by this instance.
     * 
     * @return the number of items held by this instance
     */
    @Override
    public final int getCount() {
      return count;
    }

    /**
     * Serializes encoding specific data.
     * 
     * @param writer The binary writer.
     */
    @Override
    public final void serializeEncodingSpecific(BinaryWriter writer) {
      writer.writeInt32(count);
      writer.write(packedArray);
    }

    /**
     * Converts the contents of this instance to a TypedArray.
     * 
     * @return A TypedArray.
     */
    @Override
    public final TypedArray toArray() {
      boolean[] result = new boolean[count];

      // First process entire multiples of 8 bits
      int input = 0;
      int output = 0;
      boolean remainder = (count % 8) != 0;
      int limit = getPackedArrayLength(count) - (remainder ? 1 : 0);
      while (input < limit) {
        int value = packedArray[input++];
        for (int inner = 0; inner < 8; ++inner) {
          result[output++] = (value & 128) != 0;
          value = value << 1;
        }
      }

      // Add final run
      if (remainder) {
        int value = packedArray[input];
        for (int i = 0; i < count % 8; ++i) {
          result[output++] = (value & 128) != 0;
          value = value << 1;
        }
      }

      return new TypedArray(result);
    }

    /**
     * Gets the byte length of the packed bit array holding len entries.
     * 
     * @param len The number of items in the array.
     * @return The packed byte array length.
     */
    private static int getPackedArrayLength(int len) {
      int arrayLen = len / 8;
      if ((len % 8) != 0) {
        ++arrayLen;
      }

      return arrayLen;
    }
  }

  /**
   * This takes the place of the delegate in the C# implementation.
   */
  private static final class PackedBitArrayEncodingSpecificDeserializer implements Deserializer {
    
    /**
     * Private constructor.
     */
    private PackedBitArrayEncodingSpecificDeserializer() {
      
    }

    /**
     * Deserializes encoding specific information.
     * 
     * @param vt The ValueType of this instance. Must be ValueType.BOOL.
     * @param reader The BinaryReader.
     * @param skip true if the contents should be overread, false otherwise.
     * @return A ValueArray instance or null if the contents were skipped.
     */
    @Override
    public ValueArray deserialize(ValueType vt, BinaryReader reader, boolean skip) {
      int len = reader.readInt32();
      int arrayLen = PackedBitArray.getPackedArrayLength(len);

      if (skip) {
        IOHelpers.advance(reader, arrayLen);
        return null;
      } else {
        return new PackedBitArray(len, reader.read(arrayLen));
      }
    }
  }

  /**
   * Defines a class for holding an array of values run length encoded.
   */
  public static class RleArray extends ValueArray {

    /**
     * The total number of items held by this instance.
     */
    private int count;

    /**
     * The number of occurrences or runs for each value.
     */
    private byte[] occurrences;

    /**
     * The value array.
     */
    private TypedArray data;

    /**
     * Initializes a new instance of the RleArray class.
     * 
     * @param valueType The type of the values.
     * @param array The values.
     */
    public RleArray(ValueType valueType, TypedArray array) {
      super(valueType, ArrayEncodingTypeId.RUN_LENGTH_ENCODING_TYPE_ID);
      count = array.getCount();

      switch (valueType.getTypeId()) {
        case BOOL_TYPE: {
          packRLE(array.getBoolArray());
          break;
        }
        case INT_TYPE: {
          packRLE(array.getIntArray());
          break;
        }
        case LONG_TYPE: {
          packRLE(array.getLongArray());
          break;
        }
        case FLOAT_TYPE: {
          packRLE(array.getFloatArray());
          break;
        }
        case DOUBLE_TYPE: {
          packRLE(array.getDoubleArray());
          break;
        }
        case DECIMAL_TYPE: {
          packRLE(array.getDecimalArray());
          break;
        }
        case DATETIME_TYPE:
        case DATE_TYPE:
        case TIME_TYPE: {
          packRLE(array.getDateArray());
          break;
        }
        case TIMESPAN_TYPE: {
          packRLE(array.getTimeSpanArray());
          break;
        }
        case STRING_TYPE: {
          packRLE(array.getStringArray());
          break;
        }
        default:
          throw Robustness.illegalArgumentException("Unsupported ValueType %s for the RLE value array.", valueType);
      }

    }

    /**
     * Initializes a new instance of the RleArray class.
     * 
     * @param valueType The type of the values.
     * @param count The total number of values.
     * @param occurrences The occurrence of each value.
     * @param data The values.
     */
    private RleArray(ValueType valueType, int count, byte[] occurrences, TypedArray data) {
      super(valueType, ArrayEncodingTypeId.RUN_LENGTH_ENCODING_TYPE_ID);
      this.count = count;
      this.occurrences = occurrences;
      this.data = data;
    }

    /**
     * Gets the total number of items held by this instance.
     * 
     * @return the total number of items held by this instance
     */
    @Override
    public final int getCount() {
      return count;
    }

    /**
     * Serializes encoding specific data.
     * 
     * @param writer The BinaryWriter.
     */
    @Override
    public final void serializeEncodingSpecific(BinaryWriter writer) {
      writer.writeInt32(count);
      IOHelpers.writeValue(writer, ValueType.BINARY, occurrences);
      IOHelpers.writeArray(writer, getValueType(), data, data.getCount());
    }

    /**
     * Converts the contents on this instance to a TypedArray.
     * 
     * @return A TypedArray, holding the unpacked contents of this instance.
     */
    @Override
    public final TypedArray toArray() {
      
      TypedArray values = null;
      
      switch (getValueType().getTypeId()) {
        case BOOL_TYPE: {
          values = unpackBooleanRLE();
          break;
        }
        case INT_TYPE: {
          values = unpackIntRLE();
          break;
        }
        case LONG_TYPE: {
          values = unpackLongRLE();
          break;
        }
        case FLOAT_TYPE: {
          values = unpackFloatRLE();
          break;
        }
        case DOUBLE_TYPE: {
          values = unpackDoubleRLE();
          break;
        }
        case DECIMAL_TYPE: {
          values = unpackDecimalRLE();
          break;
        }
        case DATETIME_TYPE:
        case DATE_TYPE:
        case TIME_TYPE: {
          values = unpackDateRLE();
          break;
        }
        case TIMESPAN_TYPE: {
          values = unpackTimeSpanRLE();
          break;
        }
        case STRING_TYPE: {
          values = unpackStringRLE();
          break;
        }
        default:
          throw Robustness.illegalArgumentException("Unsupported ValueType %s for the RLE value array.", 
              getValueType());
      }
      return values;
    }

    
    /**
     * Unpacks the contents of this instance into an array of booleans.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackBooleanRLE() {
      boolean[] dest = new boolean[count];
      int offset = 0;
      boolean[] src = data.getBoolArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of ints.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackIntRLE() {
      int[] dest = new int[count];
      int offset = 0;
      int[] src = data.getIntArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of longs.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackLongRLE() {
      long[] dest = new long[count];
      int offset = 0;
      long[] src = data.getLongArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of floats.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackFloatRLE() {
      float[] dest = new float[count];
      int offset = 0;
      float[] src = data.getFloatArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of doubles.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackDoubleRLE() {
      double[] dest = new double[count];
      int offset = 0;
      double[] src = data.getDoubleArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of Decimals.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackDecimalRLE() {
      BigDecimal[] dest = new BigDecimal[count];
      int offset = 0;
      BigDecimal[] src = data.getDecimalArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of Dates.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackDateRLE() {
      Date[] dest = new Date[count];
      int offset = 0;
      Date[] src = data.getDateArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of TimeSpans.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackTimeSpanRLE() {
      TimeSpan[] dest = new TimeSpan[count];
      int offset = 0;
      TimeSpan[] src = data.getTimeSpanArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * Unpacks the contents of this instance into an array of Strings.
     * 
     * @return the array of unpacked values
     */
    private TypedArray unpackStringRLE() {
      String[] dest = new String[count];
      int offset = 0;
      String[] src = data.getStringArray();
      for (int i = 0; i < occurrences.length; ++i) {
        int limit = occurrences[i] & 0xff;
        for (int ii = limit; ii >= 0; --ii) {
          dest[offset++] = src[i];
        }
      }
      return new TypedArray(dest);
    }

    /**
     * RLE-packs an array of booleans.
     * 
     * @param typedList the boolean values to pack
     */
    private void packRLE(boolean[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      boolean[] output = new boolean[count];
      
      int run = 0;
      boolean previous = false;
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        boolean current = typedList[i++];
        if (!(current == previous) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      boolean[] packedData = new boolean[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }
    
    /**
     * RLE-packs an array of ints.
     * 
     * @param typedList the int values to pack
     */
    private void packRLE(int[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      int[] output = new int[count];
      
      int run = 0;
      int previous = 0;
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        int current = typedList[i++];
        if (!(current == previous) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      int[] packedData = new int[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }
    
    /**
     * RLE-packs an array of longs.
     * 
     * @param typedList the long values to pack
     */
    private void packRLE(long[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      long[] output = new long[count];
      
      int run = 0;
      long previous = 0;
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        long current = typedList[i++];
        if (!(current == previous) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      long[] packedData = new long[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }

    /**
     * RLE-packs an array of floats.
     * 
     * @param typedList the float values to pack
     */
    private void packRLE(float[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      float[] output = new float[count];
      
      int run = 0;
      float previous = 0;
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        float current = typedList[i++];
        if (Float.compare(current, previous) != 0 || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      float[] packedData = new float[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }
    
    /**
     * RLE-packs an array of doubles.
     * 
     * @param typedList the double values to pack
     */
    private void packRLE(double[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      double[] output = new double[count];
      
      int run = 0;
      double previous = 0;
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        double current = typedList[i++];
        if (Double.compare(current, previous) != 0 || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      double[] packedData = new double[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }

    /**
     * RLE-packs an array of decimals.
     * 
     * @param typedList the decimal values to pack
     */
    private void packRLE(BigDecimal[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      BigDecimal[] output = new BigDecimal[count];
      
      int run = 0;
      BigDecimal previous = BigDecimal.ZERO;
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        BigDecimal current = typedList[i++];
        if (!(current.compareTo(previous) == 0) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      BigDecimal[] packedData = new BigDecimal[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }
  
    /**
     * RLE-packs an array of Dates.
     * 
     * @param typedList the Dates values to pack
     */
    private void packRLE(Date[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      Date[] output = new Date[count];
      
      int run = 0;
      Date previous = new Date(0);
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        Date current = typedList[i++];
        if (!(current.equals(previous)) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      Date[] packedData = new Date[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }

    /**
     * RLE-packs an array of TimeSpans.
     * 
     * @param typedList the TimeSpans values to pack
     */
    private void packRLE(TimeSpan[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      TimeSpan[] output = new TimeSpan[count];
      
      int run = 0;
      TimeSpan previous = new TimeSpan(0);
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        TimeSpan current = typedList[i++];
        if (!(current.equals(previous)) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      TimeSpan[] packedData = new TimeSpan[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }

    /**
     * RLE-packs an array of Strings.
     * 
     * @param typedList the Strings values to pack
     */
    private void packRLE(String[] typedList) {
      
      // allocate arrays large enough for worst case scenario
      byte[] occ = new byte[count];
      String[] output = new String[count];
      
      int run = 0;
      String previous = "";
      int i = 0;
      int outputCount = 0;
      
      if (count != 0) {
        previous = typedList[i++];
        ++run;
      }
      
      while (i < count) {
        String current = typedList[i++];
        if (!(current.equals(previous)) || run == 256) {
          output[outputCount] = previous;
          occ[outputCount++] = (byte) (run - 1);
          run = 1;
          previous = current;
        } else {
          ++run;
        }
      }
      
      if (count != 0) {
        output[outputCount] = previous;
        occ[outputCount++] = (byte) (run - 1);
      }
      
      occurrences = new byte[outputCount];
      System.arraycopy(occ, 0, occurrences, 0, outputCount);
      String[] packedData = new String[outputCount];
      System.arraycopy(output, 0, packedData, 0, outputCount);
      data = new TypedArray(packedData);
      
    }

  }
  
  /**
   * This takes the place of the delegate in the C# implementation.
   */
  private static final class RleArrayEncodingSpecificDeserializer implements Deserializer {
    
    /**
     * Private constructor.
     */
    private RleArrayEncodingSpecificDeserializer() {
      
    }

    /**
     * Deserializes encoding specific information.
     * 
     * @param vt The ValueType of this instance. Must be ValueType.BOOL.
     * @param reader The BinaryReader.
     * @param skip true if the contents should be overread, false otherwise.
     * @return A ValueArray instance or null if the contents were skipped.
     */
    @Override
    public ValueArray deserialize(ValueType vt, BinaryReader reader, boolean skip) {

      int count = reader.readInt32();

      if (skip) {
        IOHelpers.skipBinarydata(reader);
        IOHelpers.skipArray(reader, vt);
        return null;
      } else {
        byte[] occurrences = IOHelpers.readBinaryData(reader);
        TypedArray data = IOHelpers.readArray(reader, vt);
        return new RleArray(vt, count, occurrences, data);
      }
    }
  }
}
