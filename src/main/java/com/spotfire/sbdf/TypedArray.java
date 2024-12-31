/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Container class to hold arrays of either primitive types or non-primitive types.
 *
 */
public final class TypedArray {

  /**
   * The array.
   */
  private Object array;
  
  /**
   * The componentType of the elements in the array.
   */
  @SuppressWarnings("rawtypes")
  private Class componentType;
  
  /**
   * The size of the array.
   */
  private int size;
  
  /**
   * The number of items in the array.
   */
  private int count;
  
  /**
   * Get the type of the elements of the array.
   * 
   * @return the type of components in the array
   */
  @SuppressWarnings("rawtypes")
  public Class getComponentType() {
    return componentType;
  }
  
  /**
   * Get the size of the underlying array.
   * 
   * @return the size of the underlying array
   */
  public int getSize() {
    return size;
  }
  
  /**
   * Get the number of elements actually stored in the array.
   * 
   * @return the number of elements that have been stored in the underlying array
   */
  public int getCount() {
    return count;
  }

  /**
   * Resets the count of the array to make the array appear empty.
   */
  public void clear() {
    count = 0;
  }
  
  /**
   * Creates a new TypedArray object from the source one by copying the source's
   * array. Any unused elements are trimmed and the resulting TypedArray is full. 
   * 
   * @param source the TypedArray object to copy
   */
  public TypedArray(TypedArray source) {
    this.componentType = source.componentType;
    this.size = source.count;
    this.count = source.count;
    this.array = Array.newInstance(source.componentType, source.count);
    System.arraycopy(source.array, 0, this.array, 0, source.count);
  }

  /**
   * Create a TypedArray object to hold the given array of booleans.
   * 
   * @param array the array
   */
  public TypedArray(boolean[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Boolean.TYPE;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of bytes.
   * 
   * @param array the array
   */
  TypedArray(byte[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Byte.TYPE;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of integers.
   * 
   * @param array the array
   */
  public TypedArray(int[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Integer.TYPE;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of longs.
   * 
   * @param array the array
   */
  public TypedArray(long[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Long.TYPE;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of floats.
   * 
   * @param array the array
   */
  public TypedArray(float[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Float.TYPE;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of doubles.
   * 
   * @param array the array
   */
  public TypedArray(double[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Double.TYPE;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of decimals.
   * 
   * @param array the array
   */
  public TypedArray(BigDecimal[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = BigDecimal.class;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of Dates.
   * 
   * @param array the array
   */
  public TypedArray(Date[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = Date.class;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of TimeSpans.
   * 
   * @param array the array
   */
  public TypedArray(TimeSpan[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = TimeSpan.class;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of Strings.
   * 
   * @param array the array
   */
  public TypedArray(String[] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = String.class;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Create a TypedArray object to hold the given array of byte arrays.
   * 
   * @param array the array
   */
  public TypedArray(byte[][] array) {
    Robustness.validateArgumentNotNull("array", array);
    this.array = array;
    this.componentType = byte[].class;
    this.size = array.length;
    this.count = array.length;
  }
 
  /**
   * Special constructor to convert a List of boxed values into a typed
   * array. Any invalid values are overridden with default values.
   * 
   * @param componentType the type of the values in the List
   * @param list the list
   * @param defaultValue the default value for this type
   */
  @SuppressWarnings("unchecked")
  public TypedArray(@SuppressWarnings("rawtypes") Class componentType,
                    @SuppressWarnings("rawtypes") List list,
                    Object defaultValue) {
    Robustness.validateArgumentNotNull("array", list);
    this.componentType = componentType;
    this.size = list.size();
    this.count = this.size;
    if (this.componentType.isPrimitive()) {
      // primitive types need to be unboxed
      this.array = unbox(this.componentType, list, defaultValue);
    } else {
      this.array = convertArrayType(this.componentType, list, defaultValue);
    }
  }
  
  /**
   * Create a new TypedArray object with the given size.
   * 
   * @param componentType the type of the values to be stored
   * @param size the capacity of the underlying array
   */
  public TypedArray(@SuppressWarnings("rawtypes") Class componentType, int size) {
    this.componentType = componentType;
    this.size = size;
    this.count = 0;
    this.array = Array.newInstance(this.componentType, this.size);
  }

  /**
   * Return the underlying array as an array of booleans.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public boolean[] getBoolArray() {
    if (!componentType.equals(Boolean.TYPE)) {
      throw new InvalidOperationException("array is not an array of booleans");
    }
    return (boolean[]) array;
  }
  
  /**
   * Return the underlying array as an array of bytes.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  byte[] getByteArray() {
    if (!componentType.equals(Byte.TYPE)) {
      throw new InvalidOperationException("array is not an array of bytes");
    }
    return (byte[]) array;
  }
  
  /**
   * Return the underlying array as an array of ints.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public int[] getIntArray() {
    if (!componentType.equals(Integer.TYPE)) {
      throw new InvalidOperationException("array is not an array of ints");
    }
    return (int[]) array;
  }
  
  /**
   * Return the underlying array as an array of longs.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public long[] getLongArray() {
    if (!componentType.equals(Long.TYPE)) {
      throw new InvalidOperationException("array is not an array of longs");
    }
    return (long[]) array;
  }
  
  /**
   * Return the underlying array as an array of floats.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public float[] getFloatArray() {
    if (!componentType.equals(Float.TYPE)) {
      throw new InvalidOperationException("array is not an array of floats");
    }
    return (float[]) array;
  }
  
  /**
   * Return the underlying array as an array of doubles.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public double[] getDoubleArray() {
    if (!componentType.equals(Double.TYPE)) {
      throw new InvalidOperationException("array is not an array of doubles");
    }
    return (double[]) array;
  }
  
  /**
   * Return the underlying array as an array of decimals.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public BigDecimal[] getDecimalArray() {
    if (!componentType.equals(BigDecimal.class)) {
      throw new InvalidOperationException("array is not an array of decimals");
    }
    return (BigDecimal[]) array;
  }
  
  /**
   * Return the underlying array as an array of Dates.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public Date[] getDateArray() {
    if (!componentType.equals(Date.class)) {
      throw new InvalidOperationException("array is not an array of Dates");
    }
    return (Date[]) array;
  }
  
  /**
   * Return the underlying array as an array of TimeSpans.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public TimeSpan[] getTimeSpanArray() {
    if (!componentType.equals(TimeSpan.class)) {
      throw new InvalidOperationException("array is not an array of TimeSpans");
    }
    return (TimeSpan[]) array;
  }
  
  /**
   * Return the underlying array as an array of Strings.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public String[] getStringArray() {
    if (!componentType.equals(String.class)) {
      throw new InvalidOperationException("array is not an array of Strings");
    }
    return (String[]) array;
  }
  
  /**
   * Return the underlying array as an array of byte arrays.
   * 
   * Note that the returned array is the whole array so it may contain stale data
   * above the current value of count.
   * 
   * @return the array
   */
  public byte[][] getBinaryArray() {
    if (!byte[].class.equals(componentType)) {
      throw new InvalidOperationException("array is not an array of byte arrays");
    }
    return (byte[][]) array;
  }

  /**
   * Returns the specified element as an object. If the underlying array is an
   * array of primitive types, the necessary boxing is applied.
   * 
   * @param index the index of the element
   * @return the element as an object
   */
  public Object getAsObject(int index) {
    if (index > count - 1) {
      throw Robustness.illegalArgumentException("index %d is out of bounds, array only contains %d elements",
          index, count);
    }
    return Array.get(array, index);
  }

  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(boolean value) {

    if (!componentType.equals(Boolean.TYPE)) {
      throw new InvalidOperationException("array is not an array of booleans");
    }
    ((boolean[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(int value) {

    if (!componentType.equals(Integer.TYPE)) {
      throw new InvalidOperationException("array is not an array of ints");
    }
    ((int[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(long value) {

    if (!componentType.equals(Long.TYPE)) {
      throw new InvalidOperationException("array is not an array of longs");
    }
    ((long[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(float value) {

    if (!componentType.equals(Float.TYPE)) {
      throw new InvalidOperationException("array is not an array of floats");
    }
    ((float[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(double value) {

    if (!componentType.equals(Double.TYPE)) {
      throw new InvalidOperationException("array is not an array of doubles");
    }
    ((double[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(BigDecimal value) {

    if (!componentType.equals(BigDecimal.class)) {
      throw new InvalidOperationException("array is not an array of decimals");
    }
    ((BigDecimal[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(Date value) {

    if (!componentType.equals(Date.class)) {
      throw new InvalidOperationException("array is not an array of dates");
    }
    ((Date[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(TimeSpan value) {

    if (!componentType.equals(TimeSpan.class)) {
      throw new InvalidOperationException("array is not an array of TimeSpans");
    }
    ((TimeSpan[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(String value) {

    if (!componentType.equals(String.class)) {
      throw new InvalidOperationException("array is not an array of Strings");
    }
    ((String[]) array)[count++] = value;
  }
  
  /**
   * Sets the value as the next value in the array.
   * 
   * @param value the value to set
   */
  public void setNext(byte[] value) {

    if (!byte[].class.equals(componentType)) {
      throw new InvalidOperationException("array is not an array of byte arrays");
    }
    ((byte[][]) array)[count++] = value;
  }
  
  /**
   * Unboxes a List of objects into an array of the primitive type.
   * 
   * @param componentType the primitive type that the values should be unboxed into
   * @param list the List of boxed values
   * @param defaultValue the default value
   * @return an object that is an array of the unboxed values
   */
  private static Object unbox(@SuppressWarnings("rawtypes") Class componentType, List<Object> list, Object defaultValue) {
    
    Object unboxedArray;
    
    if (Boolean.TYPE.equals(componentType)) {
      unboxedArray = unboxBooleans(list, (Boolean) defaultValue);
    } else if (Byte.TYPE.equals(componentType)) {
      unboxedArray = unboxBytes(list, (Byte) defaultValue);
    } else if (Integer.TYPE.equals(componentType)) {
      unboxedArray = unboxIntegers(list, (Integer) defaultValue);
    } else if (Long.TYPE.equals(componentType)) {
      unboxedArray = unboxLongs(list, (Long) defaultValue);
    } else if (Float.TYPE.equals(componentType)) {
      unboxedArray = unboxFloats(list, (Float) defaultValue);
    } else if (Double.TYPE.equals(componentType)) {
      unboxedArray = unboxDoubles(list, (Double) defaultValue);
    } else {
      throw new InvalidOperationException("Cannot unbox arrays of " + componentType);
    }
    
    return unboxedArray;
  }

  /**
   * Unbox the List.
   * 
   * @param list the array of boxed (non-primitive) objects
   * @param defaultValue the default value
   * @return the unboxed array
   */
  private static boolean[] unboxBooleans(List<Object> list, Boolean defaultValue) {
    boolean[] unboxed = new boolean[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object object = list.get(i);
      if (object instanceof Boolean) {
        unboxed[i] = ((Boolean) object).booleanValue();
      } else {
        unboxed[i] = defaultValue.booleanValue();
      }
    }
    return unboxed;
  }
  
  
  /**
   * Unbox the List.
   * 
   * @param list the List of boxed (non-primitive) objects
   * @param defaultValue the default value
   * @return the unboxed array
   */
  private static byte[] unboxBytes(List<Object> list, Byte defaultValue) {
    byte[] unboxed = new byte[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object object = list.get(i);
      if (object instanceof Byte) {
        unboxed[i] = ((Byte) object).byteValue();
      } else {
        unboxed[i] = defaultValue.byteValue();
      }
    }
    return unboxed;
  }
  
  /**
   * Unbox the List.
   * 
   * @param list the List of boxed (non-primitive) objects
   * @param defaultValue the default value
   * @return the unboxed array
   */
  private static int[] unboxIntegers(List<Object> list, Integer defaultValue) {
    int[] unboxed = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object object = list.get(i);
      if (object instanceof Integer) {
        unboxed[i] = ((Integer) object).intValue();
      } else {
        unboxed[i] = defaultValue.intValue();
      }
    }
    return unboxed;
  }
  
  /**
   * Unbox the List.
   * 
   * @param list the List of boxed (non-primitive) objects
   * @param defaultValue the default value
   * @return the unboxed array
   */
  private static long[] unboxLongs(List<Object> list, Long defaultValue) {
    long[] unboxed = new long[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object object = list.get(i);
      if (object instanceof Long) {
        unboxed[i] = ((Long) object).longValue();
      } else {
        unboxed[i] = defaultValue.longValue();
      }
    }
    return unboxed;
  }
  
  /**
   * Unbox the List.
   * 
   * @param list the List of boxed (non-primitive) objects
   * @param defaultValue the default value
   * @return the unboxed array
   */
  private static float[] unboxFloats(List<Object> list, Float defaultValue) {
    float[] unboxed = new float[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object object = list.get(i);
      if (object instanceof Float) {
        unboxed[i] = ((Float) object).floatValue();
      } else {
        unboxed[i] = defaultValue.floatValue();
      }
    }
    return unboxed;
  }
  
  /**
   * Unbox the List.
   * 
   * @param list the List of boxed (non-primitive) objects
   * @param defaultValue the default value
   * @return the unboxed array
   */
  private static double[] unboxDoubles(List<Object> list, Double defaultValue) {
    double[] unboxed = new double[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object object = list.get(i);
      if (object instanceof Double) {
        unboxed[i] = ((Double) object).doubleValue();
      } else {
        unboxed[i] = defaultValue.doubleValue();
      }
    }
    return unboxed;
  }

  /**
   * Converts a List of Objects to an array of the specified type.
   * 
   * @param componentType the type of the elements in the List
   * @param objectList the List of Objects to be converted
   * @param defaultValue the default value
   * @return an array of type componentType[]
   */
  private static Object convertArrayType(@SuppressWarnings("rawtypes") Class componentType,
                                         List<Object> objectList,
                                         Object defaultValue) {
    
    Object convertedArray = Array.newInstance(componentType, objectList.size());
    
    for (int i = 0; i < objectList.size(); i++) {
      Object value = objectList.get(i);
      if (componentType.isInstance(value)) {
        Array.set(convertedArray, i, value);
      } else {
        Array.set(convertedArray, i, defaultValue);
      }
    }
    return convertedArray;
  }

}
