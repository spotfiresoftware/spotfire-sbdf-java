/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf.ApiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import com.spotfire.sbdf.ArrayTest;
import com.spotfire.sbdf.BinaryReader;
import com.spotfire.sbdf.BinaryWriter;
import com.spotfire.sbdf.ColumnMetadata;
import com.spotfire.sbdf.ColumnSlice;
import com.spotfire.sbdf.ColumnSliceBuilder;
import com.spotfire.sbdf.TableMetadata;
import com.spotfire.sbdf.TableMetadataBuilder;
import com.spotfire.sbdf.TableReader;
import com.spotfire.sbdf.TableSlice;
import com.spotfire.sbdf.TableSliceBuilder;
import com.spotfire.sbdf.TypedArray;
import com.spotfire.sbdf.ValueArray;
import com.spotfire.sbdf.ValueType;

/**
 * Unit tests for large tables.
 */
public class LargeTablesTest {

  private static TypedArray ints(int limit, int seed) {
    int[] result = new int[limit];
    for (int i = 0; i < limit; ++i) {
      result[i] = i + seed;
    }

    return new TypedArray(result);
  }

  private static TypedArray strings(int limit, int seed) {
    String[] result = new String[limit];
    for (int i = 0; i < limit; ++i) {
      result[i] = Integer.toString(i + seed);
    }

    return new TypedArray(result);
  }

  /**
   * Verifies that the writing and reading a subset of a large table works.
   */
  @Test
  public final void testLargeTable() {
    TableMetadataBuilder tmd = new TableMetadataBuilder();
    ColumnMetadata col1 = new ColumnMetadata("first", ValueType.INT);
    ColumnMetadata col2 = new ColumnMetadata("second", ValueType.STRING);
    tmd.addColumn(col1);
    tmd.addColumn(col2);

    TableMetadata tm = tmd.build();
    byte[] data = null;

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(outputStream);
    tm.write(bw);
    for (int i = 0; i < 10; ++i) {
      TableSliceBuilder tsb = new TableSliceBuilder(tm);
      tsb.addColumn(new ColumnSliceBuilder(ValueArray.createDefaultArrayEncoding(ints(10000, i * 10000))).build());
      tsb.addColumn(new ColumnSliceBuilder(ValueArray.createDefaultArrayEncoding(strings(10000, i * 10000))).build());
      TableSlice ts = tsb.build();
      ts.write(bw);
    }
    TableSlice.writeEndOfTableMarker(bw);

    data = outputStream.toByteArray();

    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
    BinaryReader br = new BinaryReader(inputStream);
    tm = TableMetadata.read(br);
    int seed = 0;
    for (TableSlice ts : TableSlice.readSlices(br, tm, new boolean[] {true, false})) {
      assertEquals(1, ts.getColumns().size());
      ColumnSlice cs = ts.getColumns().get(0);
      ArrayTest.checkArraysEqual(ValueType.INT, ints(10000, seed), cs.getValues().toArray());
      seed += 10000;
    }
    assertEquals(100000, seed);
    br = new BinaryReader(new ByteArrayInputStream(data));
    tm = TableMetadata.read(br);
    TableReader tr = new TableReader(br, tm, new boolean[] {false, true});
    seed = 0;
    for (Object value : tr) {
      // assertEquals(Integer.toString(seed++), value);

      String one = Integer.toString(seed++);
      String two = (String) value;
      if (!one.equals(two)) {
        System.out.println("one=" + one + " two=" + two);
      }

    }
    assertEquals(100000, seed);
  }
}
