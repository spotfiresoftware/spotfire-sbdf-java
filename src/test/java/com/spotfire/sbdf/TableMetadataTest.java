/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TabeMetadataTest class.
 */
public class TableMetadataTest {

  public static TableMetadata build() {
    TableMetadataBuilder tmb = new TableMetadataBuilder();
    ColumnMetadata cmd = new ColumnMetadata("foo", ValueType.STRING);
    cmd.addProperty("prop", 1);
    tmb.addColumn(cmd);
    cmd = new ColumnMetadata("qwe", ValueType.DOUBLE);
    cmd.addProperty("bar", 12);
    cmd.addProperty("sprop", "1");
    tmb.addColumn(cmd);
    tmb.addProperty("test", "ok");
    return tmb.build();
  }

  public static void verify(TableMetadata tm) {
    assertEquals(2, tm.getColumns().length);
    assertEquals(1, tm.getCount());
    assertEquals("ok", tm.getPropertyOfType(String.class, "test"));
    ColumnMetadata cmd = tm.getColumns()[0];
    assertEquals(3, cmd.getCount());
    assertEquals("foo", cmd.getName());
    assertEquals(ValueType.STRING, cmd.getDataType());
    assertEquals(Integer.valueOf(1), cmd.getPropertyOfType(Integer.class, "prop"));
    cmd = tm.getColumns()[1];
    assertEquals(4, cmd.getCount());
    assertEquals("qwe", cmd.getName());
    assertEquals(ValueType.DOUBLE, cmd.getDataType());
    assertEquals(Integer.valueOf(12), cmd.getPropertyOfType(Integer.class, "bar"));
    assertEquals("1", cmd.getPropertyOfType(String.class, "sprop"));
  }

  /**
   * Verifies that the constructors validate their arguments correctly.
   */
  @Test
  public final void testBasicProperties() {
    TableMetadata tm = build();
    verify(tm);

    verify((new TableMetadataBuilder(tm)).build());
  }

  @Test
  public final void testMutable() {
    TableMetadata tm = build();
    ColumnMetadata cmd = tm.getColumns()[0];

    assertTrue(tm.isImmutable());
    assertTrue(cmd.isImmutable());

    try {
      tm.addProperty("foo", 999);
      fail("tm.addProperty(\"foo\", 999); should fail");
    } catch (InvalidOperationException e) {
      // expected
    }

    try {
      tm.removeProperty("test");
      fail("tm.removeProperty(\"test\"); should fail");
    } catch (InvalidOperationException e) {
      // expected
    }

    try {
      cmd.addProperty("foo", 999);
      fail("tm.addProperty(\"foo\", 999); should fail");
    } catch (InvalidOperationException e) {
      // expected
    }

    try {
      cmd.removeProperty("Name");
      fail("tm.removeProperty(\"Name\"); should fail");
    } catch (InvalidOperationException e) {
      // expected
    }
  }

  @Test
  public final void testSerialization() {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryWriter writer = new BinaryWriter(outputStream);
    build().write(writer);
    byte[] data = outputStream.toByteArray();

    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
    BinaryReader reader = new BinaryReader(inputStream);
    TableMetadata tm = TableMetadata.read(reader);
    verify(tm);
  }

  @Test
  public final void testRobustness() {
    TableMetadataBuilder tmb = new TableMetadataBuilder();

    try {
      tmb.addProperty(null, "foo");
      fail("tm.addProperty(null, \"foo\"); should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      tmb.addProperty("foo", null);
      fail("tm.addProperty(\"foo\", null); should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
