/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;



/**
 * Represents the type of a data value or metadata property in Sbdf.
 */
public final class ValueType {

  /**
   * The number of milliseconds between 12:00 A.M., January 1, 0001 GMT and January 1, 1970, 00:00:00 GMT.
   */
  public static final long DATE_DIFFERENCE = 62135596800000L;

  /**
   * The number of (Java) ticks that represent 12:00 A.M., January 1, 1583 GMT, the first date we support.
   */
  public static final long START_DATE = -12212553600000L;
  
  /**
   * The Boolean value type.
   */
  public static final ValueType BOOL = new ValueType(ValueTypeId.BOOL_TYPE);

  /**
   * The Integer value type.
   */
  public static final ValueType INT = new ValueType(ValueTypeId.INT_TYPE);

  /**
   * The Long integer value type.
   */
  public static final ValueType LONG = new ValueType(ValueTypeId.LONG_TYPE);

  /**
   * The Float value type.
   */
  public static final ValueType FLOAT = new ValueType(ValueTypeId.FLOAT_TYPE);

  /**
   * The Double value type.
   */
  public static final ValueType DOUBLE = new ValueType(ValueTypeId.DOUBLE_TYPE);

  /**
   * The DateTime value type.
   */
  public static final ValueType DATETIME = new ValueType(ValueTypeId.DATETIME_TYPE);

  /**
   * The Date value type.
   */
  public static final ValueType DATE = new ValueType(ValueTypeId.DATE_TYPE);

  /**
   * The Time value type.
   */
  public static final ValueType TIME = new ValueType(ValueTypeId.TIME_TYPE);

  /**
   * The TimeSpan value type.
   */
  public static final ValueType TIMESPAN = new ValueType(ValueTypeId.TIMESPAN_TYPE);

  /**
   * The String value type.
   */
  public static final ValueType STRING = new ValueType(ValueTypeId.STRING_TYPE);

  /**
   * The Binary value type.
   */
  public static final ValueType BINARY = new ValueType(ValueTypeId.BINARY_TYPE);

  /**
   * The Decimal value type.
   */
  public static final ValueType DECIMAL = new ValueType(ValueTypeId.DECIMAL_TYPE);

  /**
   * The unknown type.
   */
  private static final ValueType UNKNOWN = new ValueType(ValueTypeId.UNKNOWN_TYPE);

  /**
   * Holds default values for all known data types.
   */
  private static final Object[] DEFAULTS = new Object[ValueTypeId.DECIMAL_TYPE.getValue() + 1];

  /**
   * Holds binary representations of known value types.
   */
  private static final byte[][] CACHED_VALUE_TYPES = new byte[ValueTypeId.DECIMAL_TYPE.ordinal() + 1][];
  
  /**
   * Initializes static members of the ValueType class.
   */
  static {
    DEFAULTS[ValueTypeId.BOOL_TYPE.ordinal()] = false;
    DEFAULTS[ValueTypeId.INT_TYPE.ordinal()] = 0;
    DEFAULTS[ValueTypeId.LONG_TYPE.ordinal()] = 0L;
    DEFAULTS[ValueTypeId.FLOAT_TYPE.ordinal()] = 0F;
    DEFAULTS[ValueTypeId.DOUBLE_TYPE.ordinal()] = 0D;
    DEFAULTS[ValueTypeId.DATETIME_TYPE.ordinal()] = new Date(ValueType.START_DATE);
    DEFAULTS[ValueTypeId.DATE_TYPE.ordinal()] = new Date(ValueType.START_DATE);
    DEFAULTS[ValueTypeId.TIME_TYPE.ordinal()] = new Date(0);
    DEFAULTS[ValueTypeId.TIMESPAN_TYPE.ordinal()] = new TimeSpan(0);
    DEFAULTS[ValueTypeId.STRING_TYPE.ordinal()] = "";
    DEFAULTS[ValueTypeId.BINARY_TYPE.ordinal()] = new byte[0];
    DEFAULTS[ValueTypeId.DECIMAL_TYPE.ordinal()] = BigDecimal.ZERO;
    
    CACHED_VALUE_TYPES[ValueTypeId.BOOL_TYPE.ordinal()] = createBinary(ValueType.BOOL);
    CACHED_VALUE_TYPES[ValueTypeId.INT_TYPE.ordinal()] = createBinary(ValueType.INT);
    CACHED_VALUE_TYPES[ValueTypeId.LONG_TYPE.ordinal()] = createBinary(ValueType.LONG);
    CACHED_VALUE_TYPES[ValueTypeId.FLOAT_TYPE.ordinal()] = createBinary(ValueType.FLOAT);
    CACHED_VALUE_TYPES[ValueTypeId.DOUBLE_TYPE.ordinal()] = createBinary(ValueType.DOUBLE);
    CACHED_VALUE_TYPES[ValueTypeId.DATETIME_TYPE.ordinal()] = createBinary(ValueType.DATETIME);
    CACHED_VALUE_TYPES[ValueTypeId.DATE_TYPE.ordinal()] = createBinary(ValueType.DATE);
    CACHED_VALUE_TYPES[ValueTypeId.TIME_TYPE.ordinal()] = createBinary(ValueType.TIME);
    CACHED_VALUE_TYPES[ValueTypeId.TIMESPAN_TYPE.ordinal()] = createBinary(ValueType.TIMESPAN);
    CACHED_VALUE_TYPES[ValueTypeId.STRING_TYPE.ordinal()] = createBinary(ValueType.STRING);
    CACHED_VALUE_TYPES[ValueTypeId.BINARY_TYPE.ordinal()] = createBinary(ValueType.BINARY);
    CACHED_VALUE_TYPES[ValueTypeId.DECIMAL_TYPE.ordinal()] = createBinary(ValueType.DECIMAL);
    
  }

  
  /**
   * Type string constant.
   */
  private static final String TYPE = "_TYPE";

  /**
   * The type identifier of this value type.
   */
  private ValueTypeId typeId = ValueTypeId.forValue(0);

  /**
   * Initializes a new instance of the ValueType class.
   * 
   * @param typeId The type identifier.
   */
  ValueType(ValueTypeId typeId) {
    this.typeId = typeId;
  }

  /**
   * Gets the type identifier of this value type.
   * 
   * @return the type identifier of this value type
   */
  public ValueTypeId getTypeId() {
    return typeId;
  }

  /**
   * Indicates whether this value type is decimal.
   * 
   * @return true if this is a decimal type, false otherwise.
   */
  public boolean isDecimal() {
    return typeId == ValueTypeId.DECIMAL_TYPE;
  }

  /**
   * Indicates whether this is a simple value type or not.
   * 
   * @return true if this is a simple value type, false otherwise.
   */
  public boolean isSimpleType() {
    return isSimpleTypeId(typeId);
  }

  /**
   * Indicates whether this is an array type or not.
   * 
   * @return true if this is an array type, false otherwise.
   */
  public boolean isArrayType() {
    return isArrayTypeId(typeId);
  }

  /**
   * Indicates whether this is a complex type or not.
   * 
   * @return true if this is a complex type, false otherwise.
   */
  public boolean isComplexType() {
    return isComplexTypeId(typeId);
  }

  /**
   * Indicates whether this is a user-defined type or not.
   * 
   * @return true if this is a user-defined type, false otherwise.
   */
  public boolean isUserDefinedType() {
    return isUserDefinedTypeId(typeId);
  }

  /**
   * Gets the default value for this type.
   * 
   * @return The default value.
   */
  public Object getDefaultValue() {
    return DEFAULTS[typeId.ordinal()];
  }

  /**
   * Gets an object representing an invalid value.
   * 
   * @return An object representing an invalid value.
   */
  public Object getInvalidValue() {
    return InvalidHolder.getInstance();
  }

  /**
   * Gets a value indicating whether a given type identifier corresponds to a simple type.
   * 
   * @param typeId The type identifier.
   * @return True for a simple type; otherwise, false.
   */
  public static boolean isSimpleTypeId(ValueTypeId typeId) {
    return (typeId == ValueTypeId.BOOL_TYPE) || (typeId == ValueTypeId.INT_TYPE) || (typeId == ValueTypeId.LONG_TYPE)
        || (typeId == ValueTypeId.FLOAT_TYPE) || (typeId == ValueTypeId.DOUBLE_TYPE)
        || (typeId == ValueTypeId.DATETIME_TYPE) || (typeId == ValueTypeId.DATE_TYPE)
        || (typeId == ValueTypeId.TIME_TYPE) || (typeId == ValueTypeId.TIMESPAN_TYPE)
        || (typeId == ValueTypeId.DECIMAL_TYPE);
  }

  /**
   * Gets a value indicating whether a given type identifier corresponds to an array type.
   * 
   * @param typeId The type identifier.
   * @return True for an array type; otherwise, false.
   */
  public static boolean isArrayTypeId(ValueTypeId typeId) {
    return (typeId == ValueTypeId.STRING_TYPE) || (typeId == ValueTypeId.BINARY_TYPE);
  }

  /**
   * Gets a value indicating whether a given type identifier corresponds to a complex type.
   * 
   * @param typeId The type identifier.
   * @return True for a complex type; otherwise, false.
   */
  public static boolean isComplexTypeId(ValueTypeId typeId) {
    return (typeId == ValueTypeId.USER_DEFINED_TYPE);
  }

  /**
   * Gets a value indicating whether a given type identifier corresponds to a user-defined type.
   * 
   * @param typeId The type identifier.
   * @return True for a user-defined type; otherwise, false.
   */
  public static boolean isUserDefinedTypeId(ValueTypeId typeId) {
    return typeId == ValueTypeId.USER_DEFINED_TYPE;
  }

  /**
   * Gets a value type instance for a given type identifier.
   * 
   * @param typeId The type identifier.
   * @return A ValueType instance.
   * @throws IllegalArgumentException If the type identifier is neither simple nor an array.
   */
  public static ValueType getValueTypeFromId(ValueTypeId typeId) {

    if (!isSimpleTypeId(typeId) && !isArrayTypeId(typeId)) {
      throw Robustness.illegalArgumentException("The typeId must be either a simple type or an array type.");
    }

    ValueType type;

    switch (typeId) {
      case BOOL_TYPE:
        type = ValueType.BOOL;
        break;

      case INT_TYPE:
        type = ValueType.INT;
        break;

      case LONG_TYPE:
        type = ValueType.LONG;
        break;

      case FLOAT_TYPE:
        type = ValueType.FLOAT;
        break;

      case DOUBLE_TYPE:
        type = ValueType.DOUBLE;
        break;

      case DATETIME_TYPE:
        type = ValueType.DATETIME;
        break;

      case DATE_TYPE:
        type = ValueType.DATE;
        break;

      case TIME_TYPE:
        type = ValueType.TIME;
        break;

      case TIMESPAN_TYPE:
        type = ValueType.TIMESPAN;
        break;

      case STRING_TYPE:
        type = ValueType.STRING;
        break;

      case BINARY_TYPE:
        type = ValueType.BINARY;
        break;

      case DECIMAL_TYPE:
        type = ValueType.DECIMAL;
        break;

      default:
        throw Robustness.illegalArgumentException("Unrecognized type id: %s", typeId);
    }
    return type;
  }

  /**
   * Create a binary representation of the given ValueType.
   * 
   * @param valueType The ValueType
   * @return A byte array
   */
  private static byte[] createBinary(ValueType valueType) {
    BinaryWriter writer = new BinaryWriter(new ByteArrayOutputStream());

    valueType.write(writer);

    return ((ByteArrayOutputStream) writer.getStream()).toByteArray();
  }
  
  /**
   * Converts a ValueType instance to a byte array.
   * 
   * @param valueType The ValueType instance.
   * @return A byte array.
   */
  public static byte[] convertValueTypeToBinary(ValueType valueType) {
    
    byte[] binRep = null;

    ValueTypeId id = valueType.getTypeId();
    if (id != ValueTypeId.USER_DEFINED_TYPE) {
      binRep = CACHED_VALUE_TYPES[id.ordinal()];
    }
    
    if (binRep == null) {
      binRep = createBinary(valueType);
    }

    return binRep;
  }

  /**
   * Converts a byte array to a ValueType instance.
   * 
   * @param bytes The byte array.
   * @return A ValueType instance.
   */
  public static ValueType fromBinary(byte[] bytes) {

    Robustness.validateArgumentNotNull("binary", bytes);

    BinaryReader reader = new BinaryReader(new ByteArrayInputStream(bytes));

    return ValueType.read(reader);
  }

  /**
   * Reads a ValueType from the BinaryReader.
   * 
   * @param binaryReader The BinaryReader.
   * @return The valuetype.
   */
  public static ValueType read(BinaryReader binaryReader) {

    ValueTypeId id = ValueTypeId.forValue(binaryReader.readSByte());

    if (isSimpleTypeId(id) || isArrayTypeId(id)) {
      return getValueTypeFromId(id);
    }

    throw new InvalidOperationException(String.format("Invalid type %s", id));
  }

  /**
   * Constructs a ValueType from a type. A ValueType with ValueTypeId.UNKNOWN_TYPE is returned for unknown types.
   * 
   * @param type The type object.
   * @return A corresponding ValueType instance.
   */
  public static ValueType fromRuntimeType(@SuppressWarnings("rawtypes") Class type) {
    
    ValueType result = UNKNOWN;

    if (type.equals(Boolean.TYPE)) {
      result = ValueType.BOOL;
    } else if (type.equals(Integer.TYPE)) {
      result = ValueType.INT;
    } else if (type.equals(Long.TYPE)) {
      result = ValueType.LONG;
    } else if (type.equals(Float.TYPE)) {
      result = ValueType.FLOAT;
    } else if (type.equals(Double.TYPE)) {
      result = ValueType.DOUBLE;
    } else if (type.equals(Date.class)) {
      result = ValueType.DATETIME;
    } else if (type.equals(TimeSpan.class)) {
      result = ValueType.TIMESPAN;
    } else if (type.equals(String.class)) {
      result = ValueType.STRING;
    } else if (byte[].class.equals(type)) {
      result = ValueType.BINARY;
    } else if (type.equals(java.math.BigDecimal.class)) {
      result = ValueType.DECIMAL;
    }

    return result;
  }

  /**
   * Returns an object representing a replaced null value.
   * 
   * @param value The value.
   * @return An object representing a replaced null value
   */
  public Object replacedValue(Object value) {

    Robustness.validateArgumentNotNull("value", value);

    validateAssignment(this, value);

    return new ReplacedHolder(value);
  }

  /**
   * Returns an object representing an error value.
   * 
   * @param error The specific error string.
   * @return An object representing an error value.
   */
  public Object errorValue(String error) {
    return new ErrorHolder(error);
  }

  /**
   * Determines if value is an invalid value.
   * 
   * @param value The object to examine.
   * @return true if the object is an invalid value, false otherwise.
   */
  public boolean isInvalidValue(Object value) {
    return value instanceof InvalidHolder;
  }

  /**
   * Determines if value is a replaced value.
   * 
   * @param value The object to examine.
   * @return true if the object is an replaced value, false otherwise.
   */
  public boolean isReplacedValue(Object value) {
    return value instanceof ReplacedHolder;
  }

  /**
   * Determines if value is an error value.
   * 
   * @param value The object to examine.
   * @return true if the object is an error value, false otherwise.
   */
  public boolean isErrorValue(Object value) {
    return value instanceof ErrorHolder;
  }

  /**
   * Gets the error string from the error value held in value.
   * 
   * @param value The error value.
   * @return The error string.
   * @throws IllegalArgumentException if value doesn't represent an error value.
   */
  public String getErrorString(Object value) {

    ErrorHolder eh = (ErrorHolder) ((value instanceof ErrorHolder) ? value : null);

    if (eh == null) {
      throw Robustness.illegalArgumentException("The passed value is not an error value.");
    }

    return (eh.getError() != null) ? eh.getError() : "";
  }

  /**
   * Gets the inner replaced value held in value.
   * 
   * @param value The object representing the replaced value.
   * @return The inner replaced value.
   * @throws IllegalArgumentException if value doesn't represent a replaced value.
   */
  public Object getReplacedValue(Object value) {

    ReplacedHolder eh = (ReplacedHolder) ((value instanceof ReplacedHolder) ? value : null);

    if (eh == null) {
      throw Robustness.illegalArgumentException("The passed value is not a replaced value.");
    }

    return eh.getValue();
  }

  /**
   * Writes the contents of this instance to stream.
   * 
   * @param writer The BinaryWriter receiving the contents.
   */
  public void write(BinaryWriter writer) {

    writer.writeSByte((byte) typeId.getValue());
  }

  /**
   * Returns a string representation of this instance.
   * 
   * @return A string representation of this instance.
   */
  @Override
  public String toString() {
    return getString();
  }

  /**
   * Determines if two ValueType objects are equal.
   * 
   * @param obj The ValueType object to compare this instance to.
   * @return true if the instances are equal, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {

    ValueType vt = (ValueType) ((obj != null && (obj instanceof ValueType)) ? obj : null);

    return vt != null && typeId == vt.typeId;
  }

  /**
   * Calculates the hash code of this instance.
   * 
   * @return The hash code of this instance.
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + typeId.getValue();
    return hash;
  }

  /**
   * Gets the runtime type corresponding to a given ValueType.
   * 
   * @param valueType The ValueType.
   * @return A Type instance.
   * @throws IllegalArgumentException If the type is neither simple nor an array.
   */
  @SuppressWarnings("rawtypes")
  public static Class getRuntimeType(ValueType valueType) {

    ValueTypeId typeId = valueType.getTypeId();

    Class c = null;

    switch (typeId) {
      case BOOL_TYPE:
        c = Boolean.TYPE;
        break;

      case INT_TYPE:
        c = Integer.TYPE;
        break;

      case LONG_TYPE:
        c = Long.TYPE;
        break;

      case FLOAT_TYPE:
        c = Float.TYPE;
        break;

      case DOUBLE_TYPE:
        c = Double.TYPE;
        break;

      case DATETIME_TYPE:
      case DATE_TYPE:
      case TIME_TYPE:
        c = Date.class;
        break;

      case TIMESPAN_TYPE:
        c = TimeSpan.class;
        break;

      case STRING_TYPE:
        c = String.class;
        break;

      case BINARY_TYPE:
        c = byte[].class;
        break;

      case DECIMAL_TYPE:
        c = BigDecimal.class;
        break;

      default:
        break;
    }

    if (c != null) {
      return c;
    }

    if (!isSimpleTypeId(typeId) && !isArrayTypeId(typeId)) {
      throw Robustness.illegalArgumentException("The typeId must be either a simple type or an array type.");
    }

    throw Robustness.illegalArgumentException("Unrecognized type id: %s", typeId);

  }

  /**
   * Gets the boxed type corresponding to a given ValueType. For non-primitive types, the boxed type is the same as the
   * runtime type.
   * 
   * @param valueType The ValueType.
   * @return A Type instance.
   * @throws IllegalArgumentException If the type is neither simple nor an array.
   */
  @SuppressWarnings("rawtypes")
  static Class getBoxedType(ValueType valueType) {

    ValueTypeId typeId = valueType.getTypeId();

    switch (typeId) {
      case BOOL_TYPE:
        return Boolean.class;

      case INT_TYPE:
        return Integer.class;

      case LONG_TYPE:
        return Long.class;

      case FLOAT_TYPE:
        return Float.class;

      case DOUBLE_TYPE:
        return Double.class;

      default:
        return getRuntimeType(valueType);
    }

  }

  /**
   * Determines if two objects are equal. If both objects are arrays, the contents of the arrays are examined for
   * equality.
   * 
   * @param first The first object.
   * @param second The second object.
   * @return A boolean value indicating whether the two objects are equal.
   */
  static boolean objectsEqual(Object first, Object second) {

    if (first == second) {
      return true;
    }

    if (first == null || second == null) {
      return false;
    }

    if (first.getClass().isArray() && second.getClass().isArray()) {
      @SuppressWarnings("rawtypes")
      Class c1 = first.getClass().getComponentType();
      @SuppressWarnings("rawtypes")
      Class c2 = second.getClass().getComponentType();
      if (c1.equals(c2)) {
        return arrayCompare(first, second, c1.getName());
      } else {
        return false;
      }
    } else if (!first.getClass().isArray() && !second.getClass().isArray()) {
      return first.equals(second);
    } else {
      return false;
    }
  }

  /**
   * Determines if two objects that are arrays of type componentType are equal.
   * 
   * @param first the first array
   * @param second the second array
   * @param componentType the type of the elements of the array
   * @return true if the contents of the arrays are equal.
   */
  private static boolean arrayCompare(Object first, Object second, String componentType) {

    boolean result;

    if ("boolean".equals(componentType)) {
      result = Arrays.equals((boolean[]) first, (boolean[]) second);
    } else if ("byte".equals(componentType)) {
      result = Arrays.equals((byte[]) first, (byte[]) second);
    } else if ("char".equals(componentType)) {
      result = Arrays.equals((char[]) first, (char[]) second);
    } else if ("double".equals(componentType)) {
      result = Arrays.equals((double[]) first, (double[]) second);
    } else if ("float".equals(componentType)) {
      result = Arrays.equals((float[]) first, (float[]) second);
    } else if ("int".equals(componentType)) {
      result = Arrays.equals((int[]) first, (int[]) second);
    } else if ("long".equals(componentType)) {
      result = Arrays.equals((long[]) first, (long[]) second);
    } else if ("short".equals(componentType)) {
      result = Arrays.equals((short[]) first, (short[]) second);
    } else {
      result = Arrays.equals((Object[]) first, (Object[]) second);
    }

    return result;
  }

  /**
   * Gets the ValueTypeId id of a type by examining the type.
   * 
   * @param type The type object.
   * @return The ValueTypeId corresponding to the type, or ValueTypeId.UNKNOWN_TYPE for unknown types.
   */
  static ValueTypeId getValueTypeId(@SuppressWarnings("rawtypes") Class type) {
    return fromRuntimeType(type).getTypeId();
  }

  /**
   * Validates that a given value can be assigned to a property of a certain ValueType.
   * 
   * @param type The ValueType.
   * @param value The value.
   */
  @SuppressWarnings("unchecked")
  static void validateAssignment(ValueType type, Object value) {

    if (value != null) {
      if (!getBoxedType(type).isAssignableFrom(value.getClass())) {
        throw Robustness
            .illegalArgumentException("Cannot assign %s to a property of type %s.", value, type.getTypeId());
      }
    }
  }

  /**
   * Gets a ValueType from a string representation.
   * 
   * @param input The string.
   * @return A ValueType instance.
   * @throws IllegalArgumentException If the type is not recognized.
   */
  public static ValueType getValueType(String input) {

    Robustness.validateArgumentNotNull("input", input);

    String s = input.toUpperCase();

    try {
      return ValueType.getValueTypeFromId(ValueTypeId.valueOf(s + TYPE));
    } catch (IllegalArgumentException e) {
      throw Robustness.illegalArgumentException("The type '%s' is not recognized.", s);
    }

  }

  /**
   * Returns a string representation of this instance.
   * 
   * @return A string representation of this instance.
   */
  String getString() {

    String s = typeId.name();
    return s.substring(0, s.length() - TYPE.length());
  }

  /**
   * Defines a class holding an error string.
   */
  private static class ErrorHolder {
    /**
     * The error string.
     */
    private String error;

    /**
     * Initializes a new instance of the ErrorHolder class.
     * 
     * @param error The error string. May be null / empty.
     */
    ErrorHolder(String error) {
      this.error = error;
    }

    /**
     * Gets the error string.
     * 
     * @return the error string
     */
    public final String getError() {
      return error;
    }

    /**
     * Implements equals.
     * 
     * @param obj The other object.
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {

      ErrorHolder other = (ErrorHolder) ((obj != null && (obj instanceof ErrorHolder)) ? obj : null);

      return other != null && error.equals(other.error);
    }

    /**
     * Implements hashCode.
     * 
     * @return The hash code for this instance.
     */
    @Override
    public int hashCode() {
      return error.hashCode();
    }
  }

  /**
   * Defines a class holding an invalid value.
   */
  private static final class InvalidHolder {
    
    /**
     * The static instance.
     */
    private static InvalidHolder instance = new InvalidHolder();
    
    /**
     * Initializes a new instance of the InvalidHolder class.
     */
    private InvalidHolder() {
    }

    /**
     * Gets the static instance.
     * 
     * @return the static instance.
     */
    public static InvalidHolder getInstance() {
      return instance;
    }
    
    /**
     * Implements equals.
     * 
     * @param obj The other object.
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
      return obj instanceof InvalidHolder;
    }

    /**
     * Implements hashCode.
     * 
     * @return The hash code for this instance.
     */
    @Override
    public int hashCode() {
      return 7;
    }
  }

  /**
   * Defines a class holding a replaced value.
   */
  private static class ReplacedHolder {
    /**
     * The replaced value.
     */
    private Object value;

    /**
     * Initializes a new instance of the ReplacedHolder class.
     * 
     * @param value The replaced value. May be null.
     */
    ReplacedHolder(Object value) {
      this.value = value;
    }

    /**
     * Gets the replaced value.
     * 
     * @return the replaced value
     */
    public final Object getValue() {
      return value;
    }

    /**
     * Implements equals.
     * 
     * @param obj The other object.
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {

      ReplacedHolder other = (ReplacedHolder) ((obj != null && (obj instanceof ReplacedHolder)) ? obj : null);

      return other != null && value.equals(other.value);
    }

    /**
     * Implements hashCode.
     * 
     * @return The hash code for this instance.
     */
    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }

}
