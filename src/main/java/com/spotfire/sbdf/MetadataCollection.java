/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.util.Iterator;
import java.util.LinkedHashMap;



/**
 * Represents metadata for a table or column in SBDF.
 */
public abstract class MetadataCollection implements Iterable<MetadataProperty> {
  /**
   * A dictionary that maps property names to metadata property instances.
   */
  private final LinkedHashMap<String, MetadataProperty> properties = new LinkedHashMap<String, MetadataProperty>(7);

  /**
   * Determines if the collection is modifiable.
   */
  private boolean readOnly;

  /**
   * Initializes a new instance of the MetadataCollection class.
   */
  MetadataCollection() {
  }

  /**
   * Gets the number of properties in the collection.
   * 
   * @return the number of properties.
   */
  public final int getCount() {
    return properties.size();
  }

  /**
   * Gets a value indicating whether this instance is immutable or not.
   * 
   * @return true if the instance is immutable, false otherwise.
   */
  public final boolean isImmutable() {
    return readOnly;
  }

  /**
   * Adds a new metadata property to the collection.
   * 
   * @param name The name of the property.
   * @param value The value of the property.
   */
  public final void addProperty(String name, String value) {
    Robustness.validateArgumentNotNullOrEmptyString("name", name);
    Robustness.validateArgumentNotNullOrEmptyString("value", value);

    addProperty(new MetadataProperty(name, value));
  }

  /**
   * Adds a new metadata property to the collection.
   * 
   * @param name The name of the property.
   * @param value The value of the property.
   */
  public final void addProperty(String name, int value) {
    Robustness.validateArgumentNotNullOrEmptyString("name", name);

    addProperty(new MetadataProperty(name, value));
  }

  /**
   * Adds a new metadata property to the collection.
   * 
   * @param property The new metadata property.
   */
  public final void addProperty(MetadataProperty property) {
    Robustness.validateArgumentNotNull("property", property);
    
    if (properties.containsKey(property.getName())) {
      throw Robustness.illegalArgumentException("A property with name '%s' already exists",
          property.getName());
    }

    validateModifiable();

    properties.put(property.getName(), property);
  }

  /**
   * Removes the property with the specified name from the collection.
   * 
   * @param name The property name.
   * @return true if the property was found and removed; otherwise, false.
   */
  public final boolean removeProperty(String name) {
    validateModifiable();

    return properties.remove(name) != null;
  }

  /**
   * Looks up a metadata property by name.
   * 
   * @param name The name of the property.
   * @return The Metadata property if there is one; otherwise, null.
   */
  public final MetadataProperty getProperty(String name) {
    Robustness.validateArgumentNotNullOrEmptyString("name", name);

    return properties.get(name);
  }

  /**
   * Gets the value of a metadata property.
   * 
   * @param name The name of the property.
   * @return The value of the property if found; otherwise, null.
   */
  public final Object getPropertyValue(String name) {
    MetadataProperty p = null;
    p = getProperty(name);
    return (p == null ? null : p.getValue());
  }

  /**
   * Gets the value of a metadata property of a given type.
   * 
   * @param <T> the type of the property returned
   * @param type the desired type
   * @param name the name of the property
   * @return the value of the property
   */
  @SuppressWarnings("unchecked")
  public final <T> T getPropertyOfType(Class<T> type, String name) {
    MetadataProperty p = getProperty(name);
    if (p == null) {
      throw Robustness.illegalArgumentException("No value defined for property '%s'", name);
    }
    return (T) p.getValue();
  }

  /**
   * Gets an enumerator with all metadata properties in the collection.
   * 
   * @return An enumerator instance.
   */
  @Override
  public final Iterator<MetadataProperty> iterator() {
    return properties.values().iterator();
  }

  /**
   * Adds the contents of source to this instance.
   * 
   * @param source The source of the contents.
   */
  protected final void addMetadataCollection(MetadataCollection source) {
    validateModifiable();

    for (Iterator<MetadataProperty> iter = source.iterator(); iter.hasNext();) {
      MetadataProperty prop = iter.next();
      properties.put(prop.getName(), prop);
    }
  }

  /**
   * Sets this instance as read-only.
   */
  protected final void setImmutable() {
    readOnly = true;
  }

  /**
   * Validates that this instance is modifiable.
   */
  private void validateModifiable() {
    if (readOnly) {
      throw new InvalidOperationException("This instance is read-only.");
    }
  }
}
