/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

/**
 * An object to represent a timespan.
 *
 */
public class TimeSpan {

  /**
   * A time span of zero milliseconds.
   */
  public static final TimeSpan ZERO = new TimeSpan(0);

  private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
  
  private long ticks;

  /**
   * Create a TimeSpan object representing a timespan of a given number of milliseconds.
   * 
   * @param ticks the number of milliseconds in the timespan.
   */
  public TimeSpan(long ticks) {
    this.ticks = ticks;
  }

  /**
   * Get the length of the timespan in milliseconds.
   * 
   * @return the length of the timespan in milliseconds
   */
  public final long getTicks() {
    return ticks;
  }

  /**
   * Create a TimeSpan object representing a time span of n days.
   *
   * @param n the number of days.
   * @return a new TimeSpan object representing a time span of n days.
   */
  public static final TimeSpan fromDays(int n) {
    long ticks = n * MILLIS_PER_DAY;
    return new TimeSpan(ticks);
  }

  /**
   * Determines if two TimeSpan objects are equal.
   * 
   * @param obj The TimeSpan object to compare this instance to.
   * @return true if the instances are equal, false otherwise.
   */
  @Override 
  public final boolean equals(Object obj) {

    TimeSpan ts = (TimeSpan) ((obj != null && (obj instanceof TimeSpan)) ? obj : null);

    return ts != null && ticks == ts.ticks;
  }

  /**
   * Calculates the hash code of this instance.
   * 
   * @return The hash code of this instance.
   */
  @Override
  public final int hashCode() {
    return (int) ticks;
  }

}
