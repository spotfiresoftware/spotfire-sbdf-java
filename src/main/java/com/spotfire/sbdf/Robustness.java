/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * Static helper class used to verify the values of parameters.
 * 
 * @author Ehsan Yazdani
 * @since 11.0
 */
final class Robustness {
  
  /**
   * Private constructor.
   */
  private Robustness() {
    // Empty
  }
  
  /**
   * Throws an {@link IllegalArgumentException} if the argument with the given
   * name is null.
   * 
   * @param argumentName  the name of the argument to validate.
   * @param value         the (value of the) argument to validate.
   * 
   * @throws IllegalArgumentException
   *                      if value is null.
   */
  static void validateArgumentNotNull(String argumentName, Object value) {
    if (value == null) {
      throw new IllegalArgumentException(
          "Null value not allowed for argument " + argumentName + ".");
    }
  }
  
  /**
   * Throws an {@link IllegalArgumentException} if the argument with the given
   * name is null or an empty {@link String}.
   * 
   * @param argumentName  the name of the argument to validate.
   * @param value         the (value of the) argument to validate.
   * 
   * @throws IllegalArgumentException
   *                      if value is null.
   */
  static void validateArgumentNotNullOrEmptyString(
      String argumentName, 
      String value) {
    
    validateArgumentNotNull(argumentName, value);
    
    if (value.length() == 0) {
      throw new IllegalArgumentException(
          "Empty String is not allowed for argument " + argumentName + ".");
    }
  }

  /**
   * Creates an IllegalArgumentException with the given message.
   * @param string the message
   * @param args optional format args
   * @return a new IllegalArgumentException
   */
  static IllegalArgumentException illegalArgumentException(String string, Object... args) {
    return new IllegalArgumentException(String.format(string, args));
  }

}
