/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * A runtime exception indicating that there was an error while serializing the data.
 *
 */
@SuppressWarnings("serial")
public class SerializationException extends RuntimeException {

  /**
   * Creates a new instance of this exception.
   * 
   * @param message a description of the error
   */
  public SerializationException(String message) {
    super(message);
  }
}
