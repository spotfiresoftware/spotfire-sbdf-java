/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * A runtime exception indicating that the data being read do not
 * match the SBDF format.
 *
 */
@SuppressWarnings("serial")
public class FormatException extends RuntimeException {

  /**
   * A runtime exception indicating that the data being read do not
   * match the SBDF format.
   * 
   * @param message description of where the error was encountered.
   */
  public FormatException(String message) {
    super(message);
  }

}
