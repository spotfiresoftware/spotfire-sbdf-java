/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * An interface used in place of the .NET delegate pattern.
 *
 */
public interface Deserializer {

  /**
   * Deserializes.
   * 
   * @param valueType The type of the held values.
   * @param reader The BinaryReader, from which the contents are read.
   * @param skip true if the contents should be overread, false otherwise.
   * @return A new ValueArray.
   */
  ValueArray deserialize(ValueType valueType, BinaryReader reader, boolean skip);

}
