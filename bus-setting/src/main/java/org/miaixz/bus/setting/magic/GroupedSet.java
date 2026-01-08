/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting.magic;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * A class representing a collection of grouped sets, parsed from a configuration file. In the file, groups are
 * separated by square brackets {@code []}, and each group contains a set of unique string values. Ungrouped values and
 * values in a {@code []} group are merged. Duplicate group names are also merged.
 * <p>
 * Example file format:
 * 
 * <pre>
 * [group1]
 * aaa
 * bbb
 * ccc
 *
 * [group2]
 * aaa
 * ccc
 * ddd
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GroupedSet extends HashMap<String, LinkedHashSet<String>> {

    @Serial
    private static final long serialVersionUID = 2852227976051L;

    /**
     * The characters that surround a group name (e.g., '[' and ']').
     */
    private static final char[] GROUP_SURROUND = { '[', ']' };

    /**
     * The character set used to read the settings file.
     */
    private java.nio.charset.Charset charset;
    /**
     * The URL of the settings file.
     */
    private URL groupedSetUrl;

    /**
     * Constructs a new, empty {@code GroupedSet} with a specified charset. The configuration must be initialized
     * manually by calling {@link #init} or {@link #load}.
     *
     * @param charset The character set to use.
     */
    public GroupedSet(final java.nio.charset.Charset charset) {
        this.charset = charset;
    }

    /**
     * Constructs a {@code GroupedSet} by loading a file from the classpath.
     *
     * @param pathOnClasspath The relative path from the root of the classpath.
     * @param charset         The character set to use.
     */
    public GroupedSet(String pathOnClasspath, final java.nio.charset.Charset charset) {
        if (null == pathOnClasspath) {
            pathOnClasspath = Normal.EMPTY;
        }

        final URL url = UrlKit.getURL(pathOnClasspath);
        if (url == null) {
            throw new RuntimeException(StringKit.format("Can not find GroupSet file: [{}]", pathOnClasspath));
        }
        this.init(url, charset);
    }

    /**
     * Constructs a {@code GroupedSet} from a {@link File}.
     *
     * @param configFile The configuration file.
     * @param charset    The character set to use.
     */
    public GroupedSet(final File configFile, final java.nio.charset.Charset charset) {
        if (configFile == null) {
            throw new RuntimeException("Null GroupSet file!");
        }
        final URL url = UrlKit.getURL(configFile);
        this.init(url, charset);
    }

    /**
     * Constructs a {@code GroupedSet} from a path relative to a given class.
     *
     * @param path    The path relative to the class.
     * @param clazz   The class to which the path is relative.
     * @param charset The character set to use.
     */
    public GroupedSet(final String path, final Class<?> clazz, final java.nio.charset.Charset charset) {
        final URL url = UrlKit.getURL(path, clazz);
        if (url == null) {
            throw new RuntimeException(StringKit.format("Can not find GroupSet file: [{}]", path));
        }
        this.init(url, charset);
    }

    /**
     * Constructs a {@code GroupedSet} from a {@link URL}.
     *
     * @param url     The URL of the configuration file.
     * @param charset The character set to use.
     */
    public GroupedSet(final URL url, final java.nio.charset.Charset charset) {
        if (url == null) {
            throw new RuntimeException("Null url define!");
        }
        this.init(url, charset);
    }

    /**
     * Constructs a {@code GroupedSet} by loading a file from the classpath with UTF-8 encoding.
     *
     * @param pathOnClasspath The relative path from the root of the classpath.
     */
    public GroupedSet(final String pathOnClasspath) {
        this(pathOnClasspath, Charset.UTF_8);
    }

    /**
     * Initializes this {@code GroupedSet} by loading from a URL.
     *
     * @param groupedSetUrl The URL of the configuration file.
     * @param charset       The character set to use.
     * @return {@code true} if loading was successful.
     */
    public boolean init(final URL groupedSetUrl, final java.nio.charset.Charset charset) {
        if (groupedSetUrl == null) {
            throw new RuntimeException("Null GroupSet url or charset define!");
        }
        this.charset = charset;
        this.groupedSetUrl = groupedSetUrl;

        return this.load(groupedSetUrl);
    }

    /**
     * Loads the settings from the specified URL.
     *
     * @param groupedSetUrl The URL of the configuration file.
     * @return {@code true} if loading was successful, {@code false} otherwise.
     */
    public synchronized boolean load(final URL groupedSetUrl) {
        if (groupedSetUrl == null) {
            throw new RuntimeException("Null GroupSet url define!");
        }
        try (InputStream settingStream = groupedSetUrl.openStream()) {
            load(settingStream);
        } catch (final IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Reloads the configuration file from the original URL.
     */
    public void reload() {
        this.load(groupedSetUrl);
    }

    /**
     * Loads settings from an {@link InputStream}. This method does not close the stream.
     *
     * @param settingStream The input stream to read from.
     * @throws IOException if an I/O error occurs.
     */
    public void load(final InputStream settingStream) throws IOException {
        super.clear();
        try (BufferedReader reader = IoKit.toReader(settingStream, charset)) {
            String group = null;
            LinkedHashSet<String> valueSet = null;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (StringKit.isBlank(line) || line.startsWith(Symbol.HASH)) {
                    continue; // Skip blank lines and comments
                } else if (line.startsWith(Symbol.BACKSLASH + Symbol.HASH)) {
                    line = line.substring(1); // Unescape lines starting with \#
                }

                if (line.charAt(0) == GROUP_SURROUND[0] && line.charAt(line.length() - 1) == GROUP_SURROUND[1]) {
                    group = line.substring(1, line.length() - 1).trim();
                    valueSet = super.computeIfAbsent(group, k -> new LinkedHashSet<>());
                } else {
                    if (valueSet == null) {
                        valueSet = super.computeIfAbsent(Normal.EMPTY, k -> new LinkedHashSet<>());
                    }
                    valueSet.add(line);
                }
            }
        }
    }

    /**
     * @return The path of the loaded settings file.
     */
    public String getPath() {
        return groupedSetUrl != null ? groupedSetUrl.getPath() : null;
    }

    /**
     * @return A set of all group names.
     */
    public Set<String> getGroups() {
        return super.keySet();
    }

    /**
     * Gets the set of values for a specific group.
     *
     * @param group The group name. If null, the default (ungrouped) group is used.
     * @return The set of values, or null if the group does not exist.
     */
    public LinkedHashSet<String> getValues(String group) {
        if (group == null) {
            group = Normal.EMPTY;
        }
        return super.get(group);
    }

    /**
     * Checks if a group contains one or more specified values.
     *
     * @param group       The group name.
     * @param value       The primary value to check for.
     * @param otherValues Additional values to check for.
     * @return {@code true} if the group's set contains the value(s).
     */
    public boolean contains(final String group, final String value, final String... otherValues) {
        if (ArrayKit.isNotEmpty(otherValues)) {
            final List<String> valueList = new ArrayList<>(otherValues.length + 1);
            valueList.add(value);
            Collections.addAll(valueList, otherValues);
            return contains(group, valueList);
        } else {
            final LinkedHashSet<String> valueSet = getValues(group);
            return valueSet != null && valueSet.contains(value);
        }
    }

    /**
     * Checks if a group contains all values from a given collection.
     *
     * @param group  The group name.
     * @param values The collection of values to check for.
     * @return {@code true} if the group's set contains all the specified values.
     */
    public boolean contains(final String group, final Collection<String> values) {
        final LinkedHashSet<String> valueSet = getValues(group);
        return CollKit.isNotEmpty(values) && CollKit.isNotEmpty(valueSet) && valueSet.containsAll(values);
    }

}
