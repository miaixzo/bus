/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.setting;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.setting.metric.ini.IniSetting;
import org.miaixz.bus.setting.metric.props.Props;
import org.miaixz.bus.setting.metric.setting.Setting;
import org.miaixz.bus.setting.metric.yaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

/**
 * A builder and facade for creating and accessing various configuration file types like {@link IniSetting},
 * {@link Props}, and {@link Yaml}. This class provides static helper methods that delegate to the specific format
 * handlers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Builder() {
    }

    /**
     * Loads a YAML file from the classpath or an absolute path into a {@link Dictionary}.
     *
     * @param path The path to the YAML file.
     * @return The loaded content as a {@link Dictionary}.
     */
    public static Dictionary loadYaml(final String path) {
        return Yaml.load(path, Dictionary.class);
    }

    /**
     * Loads a YAML file from the classpath or an absolute path and maps it to the specified class type.
     *
     * @param <T>  The type of the bean to map to.
     * @param path The path to the YAML file.
     * @param type The class type of the target bean.
     * @return The loaded content as an instance of the specified type.
     */
    public static <T> T loadYaml(final String path, final Class<T> type) {
        return Yaml.load(ResourceKit.getStream(path), type);
    }

    /**
     * Loads YAML data from an {@link InputStream} and maps it to the specified class type.
     *
     * @param <T>  The type of the bean to map to.
     * @param in   The input stream containing the YAML data.
     * @param type The class type of the target bean.
     * @return The loaded content as an instance of the specified type.
     */
    public static <T> T loadYaml(final InputStream in, final Class<T> type) {
        return Yaml.load(IoKit.toBomReader(in), type);
    }

    /**
     * Loads YAML data from a {@link Reader}, closing the reader upon completion.
     *
     * @param reader The reader containing the YAML data.
     * @return The loaded content as a {@link Dictionary}.
     */
    public static Dictionary loadYaml(final Reader reader) {
        return Yaml.load(reader, Dictionary.class);
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
    public static <T> T loadYaml(final Reader reader, final Class<T> type) {
        return Yaml.load(reader, type, true);
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
    public static <T> T loadYaml(final Reader reader, Class<T> type, final boolean isCloseReader) {
        return Yaml.load(reader, type, isCloseReader);
    }

    /**
     * Parses a YAML string into a nested map structure and flattens it.
     *
     * @param <T>     The expected return type (typically Map).
     * @param content The YAML content as a string.
     * @return A flattened map with dot-separated keys.
     */
    public static <T> T parseYaml(String content) {
        return Yaml.parse(content);
    }

    /**
     * Recursively parses a nested map structure, flattening it into a single map with dot-separated keys.
     *
     * @param <T>    The expected return type (typically Map).
     * @param prefix The current key prefix for flattening.
     * @param map    The map to parse.
     * @return A flattened map.
     */
    public static <T> T parseYaml(String prefix, Map<String, Object> map) {
        return Yaml.parse(prefix, map);
    }

    /**
     * Dumps a Java object (e.g., a Map or a bean) to a {@link Writer} in YAML format.
     *
     * @param object The object to dump.
     * @param writer The writer to which the YAML data will be written.
     */
    public static void dumpYaml(final Object object, final Writer writer) {
        Yaml.dump(object, writer);
    }

    /**
     * Dumps a Java object to a {@link Writer} in YAML format using the specified dumper options.
     *
     * @param object        The object to dump.
     * @param writer        The writer to which the YAML data will be written.
     * @param dumperOptions The SnakeYAML dumper options to control the output format.
     */
    public static void dumpYaml(final Object object, final Writer writer, DumperOptions dumperOptions) {
        Yaml.dump(object, writer, dumperOptions);
    }

    /**
     * Replaces placeholders in a string using values from a Properties object.
     *
     * @param properties The properties object containing the replacement values.
     * @param value      The string with placeholders.
     * @return The string with placeholders replaced.
     */
    public static String replaceYamlValue(final java.util.Properties properties, String value) {
        return Yaml.replaceRefValue(properties, value);
    }

    /**
     * Gets a {@link Properties} object containing the current system properties.
     *
     * @return A {@code Properties} instance with system properties.
     */
    public static Properties getProperties() {
        return Props.getProperties();
    }

    /**
     * Gets a {@code Props} instance for a given properties file from the classpath.
     *
     * @param name The name of the properties file. If no extension is provided, ".properties" is assumed.
     * @return The loaded {@code Props} instance.
     */
    public static Properties getProperties(final String name) {
        return Props.get(name);
    }

    /**
     * Gets the first {@code Props} instance that can be successfully loaded from a list of resource names.
     *
     * @param names The resource names to try.
     * @return The first found {@code Props} instance, or null if none are found.
     */
    public static Properties getPropertiesFound(final String... names) {
        return Props.getFirstFound(names);
    }

    /**
     * Gets a cached {@code Setting} instance for a given resource name from the classpath.
     *
     * @param name The name of the settings file. If no extension is provided, ".setting" is assumed.
     * @return The cached or newly loaded {@code Setting} instance.
     */
    public static org.miaixz.bus.setting.Setting getSetting(final String name) {
        return Setting.get(name);
    }

    /**
     * Gets the first {@code Setting} instance that can be successfully loaded from a list of resource names.
     *
     * @param names The resource names to try.
     * @return The first found {@code Setting} instance, or null if none are found.
     */
    public static org.miaixz.bus.setting.Setting getSettingFirstFound(final String... names) {
        return Setting.getFirstFound(names);
    }

}
