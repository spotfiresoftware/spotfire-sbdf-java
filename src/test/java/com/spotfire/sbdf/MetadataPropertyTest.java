/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MetedataProperty class.
 */
public class MetadataPropertyTest {

  private static java.util.Date now = new java.util.Date();

  private static MetadataProperty construct(int i) {
    if (i == 0) {
      return new MetadataProperty("nm", "hello");
    }

    if (i == 1) {
      return new MetadataProperty("bar", 19);
    }

    if (i == 2) {
      return new MetadataProperty("foo", ValueType.DATETIME, now);
    }

    if (i == 3) {
      return new MetadataProperty("pi", ValueType.DOUBLE, 3.14, 3.0);
    }

    return null;
  }

  private static void verify(MetadataProperty mdp, int i) {
    if (i == 0) {
      assertEquals(ValueType.STRING, mdp.getValueType());
      assertEquals("hello", mdp.getValue());
      assertNull(mdp.getDefaultValue());
    }

    if (i == 1) {
      assertEquals(ValueType.INT, mdp.getValueType());
      assertEquals(Integer.valueOf(19), mdp.getValue());
      assertNull(mdp.getDefaultValue());
    }

    if (i == 2) {
      assertEquals(ValueType.DATETIME, mdp.getValueType());
      assertEquals(now, mdp.getValue());
      assertNull(mdp.getDefaultValue());
    }

    if (i == 3) {
      assertEquals(ValueType.DOUBLE, mdp.getValueType());
      assertEquals(Double.valueOf(3.14), mdp.getValue());
      assertEquals(Double.valueOf(3.0), mdp.getDefaultValue());
    }
  }

  @Test
  public final void testBasicProperties() {
    for (int i = 0;; ++i) {
      MetadataProperty mdp = construct(i);
      if (mdp == null) {
        break;
      }

      verify(mdp, i);
    }
  }

  @Test
  public final void testSerialization() {
    for (int i = 0;; ++i) {
      MetadataProperty mdp = construct(i);
      if (mdp == null) {
        break;
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      mdp.write(new BinaryWriter(outputStream));
      byte[] data = outputStream.toByteArray();

      mdp = new MetadataProperty(new BinaryReader(new ByteArrayInputStream(data)));

      verify(mdp, i);
    }
  }

  @Test
  public final void testRobustness() {

    try {
      new MetadataProperty(null, 1);
      fail("new MetadataProperty(null, 1) should throw an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new MetadataProperty("", 1);
      fail("new MetadataProperty(\"\", 1) should throw an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new MetadataProperty("foo", ValueType.STRING, 123);
      fail("new MetadataProperty(\"foo\", ValueType.STRING, 123) should throw an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new MetadataProperty("foo", ValueType.STRING, "bar", -22);
      fail("new MetadataProperty(\"foo\", ValueType.STRING, \"bar\", -22) should throw an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
