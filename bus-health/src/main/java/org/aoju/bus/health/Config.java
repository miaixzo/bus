/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health;

import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * The global configuration utility. See
 * {@code src/main/resources/oshi.properties} for default values.
 *
 * @author Kimi Liu
 * @version 5.6.2
 * @since JDK 1.8+
 */
public final class Config {

    private static final Properties configuration = new Properties();

    static {
        // Load the configuration file from the classpath
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
                if (loader == null) {
                    throw new IOException();
                }
            }
            List<URL> resources = Collections.list(loader.getResources("oshi.properties"));
            if (resources.isEmpty()) {
                Logger.warn("No default configuration found");
            } else {
                if (resources.size() > 1) {
                    Logger.warn("Configuration conflict: there is more than one oshi.properties file on the classpath");
                }

                try (InputStream in = resources.get(0).openStream()) {
                    if (in != null) {
                        configuration.load(in);
                    }
                }
            }
        } catch (IOException e) {
            Logger.warn("Failed to load default configuration");
        }
    }

    private Config() {
    }

    /**
     * Get the {@code String} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static String get(String key, String def) {
        return configuration.getProperty(key, def);
    }

    /**
     * Get the {@code int} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static int get(String key, int def) {
        String value = configuration.getProperty(key);
        return value == null ? def : Builder.parseIntOrDefault(value, def);
    }

    /**
     * Get the {@code double} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static double get(String key, double def) {
        String value = configuration.getProperty(key);
        return value == null ? def : Builder.parseDoubleOrDefault(value, def);
    }

    /**
     * Get the {@code boolean} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static boolean get(String key, boolean def) {
        String value = configuration.getProperty(key);
        return value == null ? def : Boolean.parseBoolean(value);
    }

    /**
     * Set the given property, overwriting any existing value. If the given value is
     * {@code null}, the property is removed.
     *
     * @param key The property key
     * @param val The new value
     */
    public static void set(String key, Object val) {
        if (val == null) {
            configuration.remove(key);
        } else {
            configuration.setProperty(key, val.toString());
        }
    }

    /**
     * Reset the given property to its default value.
     *
     * @param key The property key
     */
    public static void remove(String key) {
        configuration.remove(key);
    }

    /**
     * Clear the configuration.
     */
    public static void clear() {
        configuration.clear();
    }

    /**
     * Load the given {@link java.util.Properties} into the global configuration.
     *
     * @param properties The new properties
     */
    public static void load(Properties properties) {
        configuration.putAll(properties);
    }

    /**
     * Indicates that a configuration value is invalid.
     */
    public static class PropertyException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * @param property The property name
         */
        public PropertyException(String property) {
            super("Invalid property: \"" + property + "\" = " + Config.get(property, null));
        }

        /**
         * @param property The property name
         * @param message  An exception message
         */
        public PropertyException(String property, String message) {
            super("Invalid property \"" + property + "\": " + message);
        }
    }
}
