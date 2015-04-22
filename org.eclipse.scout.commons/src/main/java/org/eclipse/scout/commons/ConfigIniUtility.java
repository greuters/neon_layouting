/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;


/**
 * Utility to extract properties stored in the <code>config.ini</code> file of scout applications.
 * <p>
 * The file is located on the classpath, typically in WEB-INF/classes/config.ini or in development src/main/resources
 * <p>
 * It can also be specified by setting the system property <code>-Dconfig.ini=path-to-config.ini-file</code>
 * <p>
 * Properties are simple key-value-pairs.<br>
 * Properties may contain placeholders for other variables: <code>${variableName}</code>. These variables are then
 * resolved once when the properties are initialized.<br>
 * </p>
 * Examples: <code>
 * <ul>
 * <li>customProperty=customValue</li>
 * <li>myProperty=${customProperty}/subfolder</li>
 * </ul>
 * </code>
 */
public final class ConfigIniUtility {

  /**
   * Property to specify the configuration file. If not specified then {@link ClassLoader#getResource(String)} with
   * "/config.ini" is used.
   */
  public static final String CONFIG_INI = "config.ini";

  private final static PropertiesHelper INSTANCE = new PropertiesHelper(CONFIG_INI);

  /**
   * Gets the property with given key. If there is no property with given key, <code>null</code> is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in properties defined in a <code>config.ini</code>.</li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property or <code>null</code>.
   */
  public static String getProperty(String key) {
    return INSTANCE.getProperty(key);
  }

  /**
   * Gets the property with given key. If there is no property with given key, the given default value is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in properties defined in a <code>config.ini</code></li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property, the given default value if the property could not be found or
   *         <code>null</code> if the key is <code>null</code>.
   */
  public static String getProperty(String key, String defaultValue) {
    return INSTANCE.getProperty(key, defaultValue);
  }

  /**
   * Gets the property with given key as boolean. If a property with given key does not exist or is no valid boolean
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid boolean associated with it.
   * @return The boolean value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static boolean getPropertyBoolean(String key, boolean defaultValue) {
    return INSTANCE.getPropertyBoolean(key, defaultValue);
  }

  /**
   * Gets the property with given key as int. If a property with given key does not exist or is no valid int
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid int associated with it.
   * @return The int value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static int getPropertyInt(String key, int defaultValue) {
    return INSTANCE.getPropertyInt(key, defaultValue);
  }

  /**
   * Gets the property with given key as long. If a property with given key does not exist or is no valid long
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid long associated with it.
   * @return The long value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static long getPropertyLong(String key, long defaultValue) {
    return INSTANCE.getPropertyLong(key, defaultValue);
  }

  /**
   * Gets the property with given key as float. If a property with given key does not exist or is no valid float
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid float associated with it.
   * @return The float value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static float getPropertyFloat(String key, float defaultValue) {
    return INSTANCE.getPropertyFloat(key, defaultValue);
  }

  /**
   * Gets the property with given key as double. If a property with given key does not exist or is no valid double
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid double associated with it.
   * @return The double value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static double getPropertyDouble(String key, double defaultValue) {
    return INSTANCE.getPropertyDouble(key, defaultValue);
  }

  /**
   * Resolves all variables of format <code>${variableName}</code> in the given expression according to the current
   * application context.
   *
   * @param s
   *          The expression to resolve.
   * @return A {@link String} where all variables have been replaced with their values.
   * @throws IllegalArgumentException
   *           if a variable could not be resolved in the current context.
   */
  public static String resolve(String s) {
    return INSTANCE.resolve(s);
  }

  /**
   * Specifies if a config.ini has been found and loaded.
   *
   * @return <code>true</code> if a config.ini has been loaded, <code>false</code> otherwise.
   */
  public static boolean isInitialized() {
    return INSTANCE.isInitialized();
  }
}
