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
package org.miaixz.bus.setting.nimble.yaml;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.events.*;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Reads and writing YAML files, based on the SnakeYAML library.
 *
 * @author Kimi Liu
 */
public class Yaml {

    /**
     * Constructs a new Yaml instance.
     */
    public Yaml() {
        // No initialization required.
    }

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
        Logger.info(
                true,
                "Setting",
                "YAML path load started: path={}, targetType={}",
                path,
                type == null ? null : type.getName());
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
        Logger.debug(
                true,
                "Setting",
                "YAML stream load started: inputPresent={}, targetType={}",
                in != null,
                type == null ? null : type.getName());
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
        final long startedAt = System.nanoTime();
        if (null == type) {
            type = (Class<T>) Object.class;
        }

        Logger.info(
                true,
                "Setting",
                "YAML reader load started: targetType={}, closeReader={}",
                type.getName(),
                isCloseReader);
        final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        try {
            final T result = yaml.loadAs(reader, type);
            Logger.info(
                    false,
                    "Setting",
                    "YAML reader load completed: targetType={}, resultType={}, closeReader={}, elapsedMs={}",
                    type.getName(),
                    result == null ? "null" : result.getClass().getName(),
                    isCloseReader,
                    (System.nanoTime() - startedAt) / 1_000_000L);
            return result;
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Setting",
                    e,
                    "YAML reader load failed: targetType={}, closeReader={}, exception={}, elapsedMs={}",
                    type.getName(),
                    isCloseReader,
                    e.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000L);
            throw e;
        } finally {
            if (isCloseReader) {
                Logger.debug(true, "Setting", "YAML reader close requested: targetType={}", type.getName());
                IoKit.closeQuietly(reader);
                Logger.debug(false, "Setting", "YAML reader closed: targetType={}", type.getName());
            }
        }
    }

    /**
     * Loads one bounded YAML document as standard YAML values without constructing application classes. Aliases and
     * duplicate mapping keys are rejected. Node and collection-depth limits are enforced while the existing parser
     * supplies events to its composer, before the corresponding object graph is materialized. Source text, parser
     * diagnostics and nested causes are never logged or included in the reported failure. Existing typed load methods
     * retain their original behavior.
     *
     * @param reader            the YAML source reader
     * @param maximumDepth      maximum collection nesting depth, including the root collection
     * @param maximumNodes      maximum scalar and collection nodes, including mapping keys
     * @param maximumCodePoints maximum input document size in Unicode code points
     * @param isCloseReader     whether to close the supplied reader on success or failure
     * @return standard YAML values, or null for an empty document
     * @throws IllegalArgumentException when limits are invalid or input is malformed, unsupported or exceeds a limit
     */
    public static Object load(
            final Reader reader,
            final int maximumDepth,
            final int maximumNodes,
            final int maximumCodePoints,
            final boolean isCloseReader) {
        Assert.notNull(reader, "Reader must be not null!");
        try {
            if (maximumDepth < 1 || maximumNodes < 1 || maximumCodePoints < 1) {
                throw new IllegalArgumentException("YAML limits must be positive");
            }
            final LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);
            options.setWarnOnDuplicateKeys(false);
            options.setAllowRecursiveKeys(false);
            options.setMaxAliasesForCollections(0);
            options.setNestingDepthLimit(maximumDepth);
            options.setCodePointLimit(maximumCodePoints);
            final Parser delegate = new ParserImpl(new StreamReader(reader), options);
            final Parser bounded = new Parser() {

                /**
                 * Number of scalar and collection nodes consumed for the current document.
                 */
                private long nodes;
                /**
                 * Collection nesting depth at the current parser position.
                 */
                private int depth;

                /**
                 * Checks the next event without consuming or counting it.
                 *
                 * @param choice requested event type
                 * @return whether the next event has that type
                 */
                @Override
                public boolean checkEvent(final Event.ID choice) {
                    return delegate.checkEvent(choice);
                }

                /**
                 * Peeks at the existing parser event without consuming or counting it.
                 *
                 * @return the next parser event, or null
                 */
                @Override
                public Event peekEvent() {
                    return delegate.peekEvent();
                }

                /**
                 * Enforces graph limits before the composer consumes a node, including scalar aliases.
                 *
                 * @return the next allowed parser event, or null
                 */
                @Override
                public Event getEvent() {
                    final Event event = delegate.getEvent();
                    if (event instanceof AliasEvent) {
                        throw new IllegalArgumentException("YAML aliases are not supported");
                    }
                    if (event instanceof NodeEvent && ++nodes > maximumNodes) {
                        throw new IllegalArgumentException("YAML node limit exceeded");
                    }
                    if (event instanceof CollectionStartEvent && ++depth > maximumDepth) {
                        throw new IllegalArgumentException("YAML depth limit exceeded");
                    }
                    if (event instanceof CollectionEndEvent) {
                        depth--;
                    }
                    return event;
                }
            };
            final SafeConstructor constructor = new SafeConstructor(options);
            constructor.setAllowDuplicateKeys(false);
            constructor.setWarnOnDuplicateKeys(false);
            constructor.setComposer(new Composer(bounded, new Resolver(), options));
            return constructor.getSingleData(Object.class);
        } catch (final RuntimeException failure) {
            throw new IllegalArgumentException("Invalid, unsupported or oversized YAML input");
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
        Logger.debug(
                true,
                "Setting",
                "YAML content parse started: contentLength={}",
                content == null ? 0 : content.length());
        final T result = parse(null, new org.yaml.snakeyaml.Yaml().load(content));
        Logger.debug(
                false,
                "Setting",
                "YAML content parse completed: contentLength={}, resultType={}",
                content == null ? 0 : content.length(),
                result == null ? "null" : result.getClass().getName());
        return result;
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
        Logger.debug(
                true,
                "Setting",
                "YAML map flatten started: prefix={}, entryCount={}",
                prefix,
                map == null ? 0 : map.size());
        Map<String, Object> result = new HashMap<>();
        if (map == null) {
            Logger.debug(
                    false,
                    "Setting",
                    "YAML map flatten completed: prefix={}, entryCount={}",
                    prefix,
                    result.size());
            return (T) result;
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String currentKey = prefix == null ? entry.getKey() : prefix + Symbol.DOT + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(parse(currentKey, (Map<String, Object>) value));
            } else {
                result.put(currentKey, value);
            }
        }
        Logger.debug(false, "Setting", "YAML map flatten completed: prefix={}, entryCount={}", prefix, result.size());
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
        Logger.info(
                true,
                "Setting",
                "YAML dump started: objectType={}, writerPresent={}, flowStyle={}",
                object == null ? "null" : object.getClass().getName(),
                writer != null,
                dumperOptions.getDefaultFlowStyle());
        final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(dumperOptions);
        try {
            yaml.dump(object, writer);
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Setting",
                    e,
                    "YAML dump failed: objectType={}, exception={}",
                    object == null ? "null" : object.getClass().getName(),
                    e.getClass().getSimpleName());
            throw e;
        }
        Logger.info(
                false,
                "Setting",
                "YAML dump completed: objectType={}",
                object == null ? "null" : object.getClass().getName());
    }

    /**
     * Replaces placeholders in the format {@code ${key}} or {@code ${key:defaultValue}} within a string. The values are
     * resolved from system properties, environment variables, and the provided {@link Properties} object, in that
     * order.
     *
     * @param properties The properties object to use for resolving placeholders.
     * @param value      The string containing placeholders.
     * @return The string with placeholders replaced.
     */
    public static String replaceRefValue(Properties properties, String value) {
        Logger.debug(
                true,
                "Setting",
                "YAML reference replacement started: valueLength={}, propertyCount={}",
                value == null ? 0 : value.length(),
                properties == null ? 0 : properties.size());
        if (!value.contains(Symbol.DOLLAR + Symbol.BRACE_LEFT)) {
            Logger.debug(
                    false,
                    "Setting",
                    "YAML reference replacement skipped: valueLength={}, reason={}",
                    value == null ? 0 : value.length(),
                    "noPlaceholder");
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
        final String replaced = finalValue.toString();
        Logger.debug(
                false,
                "Setting",
                "YAML reference replacement completed: valueLength={}, resultLength={}",
                value == null ? 0 : value.length(),
                replaced.length());
        return replaced;
    }

}
