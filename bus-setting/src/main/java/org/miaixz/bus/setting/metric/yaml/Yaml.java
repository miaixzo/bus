/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.setting.metric.yaml;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.yaml.snakeyaml.DumperOptions;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for reading and writing YAML files, based on the SnakeYAML library.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Yaml {

    /**
     * Loads a YAML file from the classpath or an absolute path into a {@link Dictionary}.
     *
     * @param path The path to the YAML file (relative to the classpath or absolute).
     * @return The loaded content as a {@link Dictionary}.
     */
    public static Dictionary load(final String path) {
        return load(path, Dictionary.class);
    }

    /**
     * Loads a YAML file from the classpath or an absolute path and maps it to the specified class type.
     *
     * @param <T>  The type of the bean to map to.
     * @param path The path to the YAML file (relative to the classpath or absolute).
     * @param type The class type of the target bean.
     * @return The loaded content as an instance of the specified type.
     */
    public static <T> T load(final String path, final Class<T> type) {
        return load(ResourceKit.getStream(path), type);
    }

    /**
     * Loads YAML data from an {@link InputStream} and maps it to the specified class type. The stream is automatically
     * closed after loading.
     *
     * @param <T>  The type of the bean to map to.
     * @param in   The input stream containing the YAML data.
     * @param type The class type of the target bean.
     * @return The loaded content as an instance of the specified type.
     */
    public static <T> T load(final InputStream in, final Class<T> type) {
        return load(IoKit.toBomReader(in), type);
    }

    /**
     * Loads YAML data from a {@link Reader}, closing the reader upon completion.
     *
     * @param reader The reader containing the YAML data.
     * @return The loaded content as a {@link Dictionary}.
     */
    public static Dictionary load(final Reader reader) {
        return load(reader, Dictionary.class);
    }

    /**
     * Loads YAML data from a {@link Reader} and maps it to the specified class type, closing the reader upon
     * completion.
     *
     * @param <T>    The type of the bean to map to.
     * @param reader The reader containing the YAML data.
     * @param type   The class type of the target bean.
     * @return The loaded content as an instance of the specified type.
     */
    public static <T> T load(final Reader reader, final Class<T> type) {
        return load(reader, type, true);
    }

    /**
     * Loads YAML data from a {@link Reader} and maps it to the specified class type.
     *
     * @param <T>           The type of the bean to map to.
     * @param reader        The reader containing the YAML data.
     * @param type          The class type of the target bean.
     * @param isCloseReader If {@code true}, the reader will be closed after loading.
     * @return The loaded content as an instance of the specified type.
     */
    public static <T> T load(final Reader reader, Class<T> type, final boolean isCloseReader) {
        Assert.notNull(reader, "Reader must be not null!");
        if (null == type) {
            type = (Class<T>) Object.class;
        }

        final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        try {
            return yaml.loadAs(reader, type);
        } finally {
            if (isCloseReader) {
                IoKit.closeQuietly(reader);
            }
        }
    }

    /**
     * Parses a YAML string into a nested map structure and flattens it.
     *
     * @param <T>     The expected return type (typically Map).
     * @param content The YAML content as a string.
     * @return A flattened map with dot-separated keys.
     */
    public static <T> T parse(String content) {
        return parse(null, new org.yaml.snakeyaml.Yaml().load(content));
    }

    /**
     * Recursively parses a nested map structure, flattening it into a single map with dot-separated keys.
     *
     * @param <T>    The expected return type (typically Map).
     * @param prefix The current key prefix for flattening.
     * @param map    The map to parse.
     * @return A flattened map.
     */
    public static <T> T parse(String prefix, Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        if (map == null)
            return (T) result;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String currentKey = prefix == null ? entry.getKey() : prefix + Symbol.DOT + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(parse(currentKey, (Map<String, Object>) value));
            } else {
                result.put(currentKey, value);
            }
        }
        return (T) result;
    }

    /**
     * Dumps a Java object (e.g., a Map or a bean) to a {@link Writer} in YAML format using default pretty-printing
     * options.
     *
     * @param object The object to dump.
     * @param writer The writer to which the YAML data will be written.
     */
    public static void dump(final Object object, final Writer writer) {
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        dump(object, writer, options);
    }

    /**
     * Dumps a Java object to a {@link Writer} in YAML format using the specified dumper options.
     *
     * @param object        The object to dump.
     * @param writer        The writer to which the YAML data will be written.
     * @param dumperOptions The SnakeYAML dumper options to control the output format.
     */
    public static void dump(final Object object, final Writer writer, DumperOptions dumperOptions) {
        if (null == dumperOptions) {
            dumperOptions = new DumperOptions();
        }
        final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(dumperOptions);
        yaml.dump(object, writer);
    }

    /**
     * Replaces placeholders in the format {@code ${key}} or {@code ${key:defaultValue}} within a string. The values are
     * resolved from system properties, environment variables, and the provided {@link java.util.Properties} object, in
     * that order.
     *
     * @param properties The properties object to use for resolving placeholders.
     * @param value      The string containing placeholders.
     * @return The string with placeholders replaced.
     */
    public static String replaceRefValue(java.util.Properties properties, String value) {
        if (!value.contains(Symbol.DOLLAR + Symbol.BRACE_LEFT)) {
            return value;
        }

        StringBuilder finalValue = new StringBuilder();
        String[] segments = value.split("\\$\\{");
        finalValue.append(segments[0]);

        for (int i = 1; i < segments.length; ++i) {
            String seg = segments[i];
            int endIndex = seg.indexOf(Symbol.BRACE_RIGHT);
            if (endIndex < 0) {
                // No closing brace, treat as literal
                finalValue.append(Symbol.DOLLAR).append(Symbol.BRACE_LEFT).append(seg);
                continue;
            }

            String refKeyPart = seg.substring(0, endIndex).trim();
            String remainingPart = seg.substring(endIndex + 1);

            String defaultValue = null;
            int defaultValSplitterIndex = refKeyPart.indexOf(Symbol.COLON);
            if (defaultValSplitterIndex > 0) {
                defaultValue = refKeyPart.substring(defaultValSplitterIndex + 1);
                refKeyPart = refKeyPart.substring(0, defaultValSplitterIndex);
            }

            String refValue = System.getProperty(refKeyPart);
            if (StringKit.isBlank(refValue)) {
                refValue = System.getenv(refKeyPart);
            }
            if (StringKit.isBlank(refValue) && properties != null) {
                refValue = properties.getProperty(refKeyPart);
            }
            if (StringKit.isBlank(refValue)) {
                refValue = defaultValue;
            }

            if (StringKit.isBlank(refValue)) {
                // If still blank, append the original placeholder
                finalValue.append(Symbol.DOLLAR).append(Symbol.BRACE_LEFT).append(refKeyPart)
                        .append(Symbol.BRACE_RIGHT);
            } else {
                finalValue.append(refValue);
            }
            finalValue.append(remainingPart);
        }
        return finalValue.toString();
    }

}
