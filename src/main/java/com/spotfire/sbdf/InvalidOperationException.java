/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * An exception to indicate that an invalid operation has been encountered.
 *
 */
@SuppressWarnings("serial")
public class InvalidOperationException extends RuntimeException {

  /**
   * Creates an InvalidOperationException.
   * 
   * @param string description of the cause of the exception
   */
  public InvalidOperationException(String string) {
    super(string);
  }

}
