/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.math.BigDecimal;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ColumnMetaData class.
 */
public class ColumnMetadataTest {

  /**
   * Verifies that the constructors validate their arguments correctly.
   */
  @Test
  public final void testBasicProperties() {
    ColumnMetadata cmd = new ColumnMetadata(); // empty constructor

    cmd = new ColumnMetadata("name", ValueType.INT);

    assertEquals("name", cmd.getName());
    assertEquals(ValueType.INT, cmd.getDataType());

    cmd.addProperty("bar", 123);
    assertEquals(Integer.valueOf(123), cmd.getPropertyOfType(Integer.class, "bar"));

    cmd.addProperty(new MetadataProperty("pi", ValueType.DECIMAL, new BigDecimal("3.14")));
    MetadataProperty pi = cmd.getProperty("pi");
    assertNotNull(pi);
    assertEquals(new BigDecimal("3.14"), pi.getValue());
    assertEquals(ValueType.DECIMAL, pi.getValueType());
    assertTrue(cmd.removeProperty("pi"));
    assertNull(cmd.getProperty("pi"));
    assertFalse(cmd.removeProperty("pi"));

    cmd.addProperty("hello", "world");
    assertNotNull(cmd.getProperty("hello"));
    assertNull(cmd.getProperty("unknown property"));
    int count = 0;
    for (MetadataProperty metadataProperty : cmd) {
      ++count;
    }
    assertEquals(4, count);
  }

  @Test
  public final void testMutable() {
    ColumnMetadata cmd = new ColumnMetadata("name", ValueType.INT);

    assertFalse(cmd.isImmutable());
    assertNotSame(cmd, cmd.mutableCopy());
    ColumnMetadata immutable = cmd.immutableCopy();
    assertNotSame(cmd, immutable);
    assertTrue(immutable.isImmutable());
    assertEquals("name", immutable.getName());
    assertEquals(ValueType.INT, immutable.getDataType());
    assertSame(immutable, immutable.immutableCopy());

    try {
      immutable.addProperty("foo", 123);
      fail("immutable.addProperty(\"foo\", 123) should fail");
    } catch (InvalidOperationException e) {
      // expected
    }

    cmd.addProperty("foo", 123);

    try {
      cmd.addProperty("foo", 123);
      fail("cmd.addProperty(\"foo\", 123) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    cmd = new ColumnMetadata("name", ValueType.INT);
    assertTrue(cmd.removeProperty("Name"));

    try {
      cmd.immutableCopy();
      fail("cmd.immutableCopy() should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    cmd = new ColumnMetadata("name", ValueType.INT);
    assertTrue(cmd.removeProperty("DataType"));

    try {
      cmd.immutableCopy();
      fail("cmd.immutableCopy() should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public final void testIterator() {
    ColumnMetadata cmd = new ColumnMetadata("foo", ValueType.STRING);
    cmd.addProperty(new MetadataProperty("value", ValueType.INT, 42, 43));
    for (MetadataProperty mdp : cmd) {
      if ("Name".equals(mdp.getName())) {
        assertEquals("foo", mdp.getValue());
        assertEquals(ValueType.STRING, mdp.getValueType());
        assertNull(mdp.getDefaultValue());
      } else if ("DataType".equals(mdp.getName())) {
        assertArrayEquals(new byte[] {10}, (byte[]) mdp.getValue());
        assertEquals(ValueType.BINARY, mdp.getValueType());
        assertNull(mdp.getDefaultValue());

      } else if ("value".equals(mdp.getName())) {
        assertEquals(42, mdp.getValue());
        assertEquals(43, mdp.getDefaultValue());
        assertEquals(ValueType.INT, mdp.getValueType());

      } else {
        fail("Unknown metadataproperty.");
      }
    }
  }

  @Test
  public final void testRobustness() {

    try {
      new ColumnMetadata(null, ValueType.INT);
      fail("new ColumnMetadata(null, ValueType.INT) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new ColumnMetadata("xx", new ValueType(ValueTypeId.UNKNOWN_TYPE));
      fail("new ColumnMetadata(\"xx\", new ValueType(ValueTypeId.UNKNOWN_TYPE)) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    ColumnMetadata cmd2 = new ColumnMetadata("foo", ValueType.STRING);
    cmd2.removeProperty("foo");
  }
}
