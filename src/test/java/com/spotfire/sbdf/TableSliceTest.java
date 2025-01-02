/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TableSlicTest class.
 */
public class TableSliceTest {

  /**
   * Test the basics.
   */
  @Test
  public final void testBasicPropierties() {

    TableMetadata tm = TableMetadataTest.build();
    ValueArray va = ValueArray.createDefaultArrayEncoding(new TypedArray(new String[] {"1", "2", "3", "4"}));
    ColumnSliceBuilder csb = new ColumnSliceBuilder(va);


    TableSliceBuilder tsb = new TableSliceBuilder(tm);
    tsb.addColumn(csb.build());
    va = ValueArray.createDefaultArrayEncoding(
        new TypedArray(new double[] {1.0, 2.0, 3.0, 4.0}));
    csb = new ColumnSliceBuilder(va);
    tsb.addColumn(csb.build());
    assertNotNull(tm.getColumns()[0]);
    TableSlice ts = tsb.build();

    assertEquals(tm, ts.getMetaData());
    assertEquals(2, tm.getColumns().length);

    List<String> cns = ts.getColumnNames();
    List<ValueType> cts = ts.getColumnDataTypes();
    assertEquals(2, cns.size());
    assertEquals(2, cts.size());
    assertEquals("foo", cns.get(0));
    assertEquals("qwe", cns.get(1));
    assertSame(ValueType.STRING, cts.get(0));
    assertSame(ValueType.DOUBLE, cts.get(1));


    BinaryWriter bw = new BinaryWriter(new ByteArrayOutputStream());
    ts.write(bw);
    TableSlice.writeEndOfTableMarker(bw);
    ByteArrayInputStream inputStream =
      new ByteArrayInputStream(((ByteArrayOutputStream) bw.getStream()).toByteArray());
    TableSlice.skipSlices(new BinaryReader(inputStream), tm);
    assertEquals(0, inputStream.available());
  }

  private static byte[] emptyRep = {(byte) 0xdf, 0x5b, 0x3, 0x0, 0x0, 0x0, 0x0};

  // private static byte[] nonEmptyRep = {(byte) 0xdf, 0x5b, 0x3, 0x0, 0x0, 0x0, 0x0};

  @Test
  public void testEmpty() {
    TableMetadata tm = new TableMetadataBuilder().build();
    TableSliceBuilder tsb = new TableSliceBuilder(tm);
    TableSlice ts = tsb.build();
    BinaryWriter bw = new BinaryWriter(new ByteArrayOutputStream());
    ts.write(bw);
    byte[] rep = ((ByteArrayOutputStream) bw.getStream()).toByteArray();
    assertArrayEquals(emptyRep, rep);
  }

  @Test
  public final void testRobustness() {
    TableMetadata tm = TableMetadataTest.build();
    TableSliceBuilder tsb = new TableSliceBuilder(tm);

    ValueArray va = ValueArray.createDefaultArrayEncoding(new TypedArray(new String[] {"1", "2", "3", "4"}));

    ColumnSliceBuilder csb = new ColumnSliceBuilder(va);
    tsb.addColumn(csb.build());

    va = ValueArray.createDefaultArrayEncoding(new TypedArray(new String[] {"1"}));
    csb = new ColumnSliceBuilder(va);

    // row count mismatch
    try {
      tsb.addColumn(csb.build());
      fail("tsb.addColumn(csb.build()) should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // column count mismatch
    try {
      tsb.build();
      fail("tsb.build() should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
