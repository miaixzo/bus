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

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.watch.DelayWatcher;
import org.miaixz.bus.core.io.watch.SimpleWatcher;
import org.miaixz.bus.core.io.watch.WatchMonitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.magic.AbstractSetting;
import org.miaixz.bus.setting.magic.GroupedMap;
import org.miaixz.bus.setting.metric.props.Props;

import java.io.File;
import java.io.Serial;
import java.net.URL;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * A utility class for handling {@code .setting} files, which are an enhanced version of Java's {@code .properties}
 * files with backward compatibility.
 * <p>
 * Features:
 * <ol>
 * <li>Supports variable substitution using the {@code ${variable_name}} syntax.</li>
 * <li>Supports grouping of properties under section headers (e.g., {@code [group_name]}). Keys under a group are
 * accessed as {@code group.key}.</li>
 * <li>Treats lines starting with '#' as comments.</li>
 * </ol>
 * Note: The {@code store} methods do not preserve comments.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Setting extends AbstractSetting implements Map<String, String> {

    @Serial
    private static final long serialVersionUID = 2852263091176L;

    /**
     * The default character set (UTF-8).
     */
    public static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;
    /**
     * The default file extension for settings files.
     */
    public static final String EXT_NAME = "setting";
    /**
     * The character set for this settings instance.
     */
    protected java.nio.charset.Charset charset;
    /**
     * Whether variable substitution is enabled.
     */
    protected boolean isUseVariable;
    /**
     * The resource representing the settings file.
     */
    protected Resource resource;
    /**
     * The underlying storage for key-value pairs, organized by group.
     */
    private GroupedMap groupedMap;
    /**
     * Whether to log a debug message when a requested key is not found.
     */
    private boolean logIfNull;

    /**
     * The loader responsible for parsing the file.
     */
    private Loader loader;
    /**
     * The monitor for watching file changes to support auto-reloading.
     */
    private WatchMonitor watchMonitor;

    /**
     * Constructs a new, empty {@code Setting} instance.
     */
    public Setting() {
        this.groupedMap = new GroupedMap();
    }

    /**
     * Constructs a {@code Setting} by loading a file from a relative or absolute path.
     *
     * @param path The path to the settings file.
     */
    public Setting(final String path) {
        this(path, false);
    }

    /**
     * Constructs a {@code Setting} by loading a file.
     *
     * @param path          The path to the settings file.
     * @param isUseVariable {@code true} to enable variable substitution.
     */
    public Setting(final String path, final boolean isUseVariable) {
        this(path, DEFAULT_CHARSET, isUseVariable);
    }

    /**
     * Constructs a {@code Setting} by loading a file from the classpath.
     *
     * @param path          The path to the file (relative or absolute).
     * @param charset       The character set to use.
     * @param isUseVariable {@code true} to enable variable substitution.
     */
    public Setting(final String path, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        this(ResourceKit.getResource(Assert.notBlank(path)), charset, isUseVariable);
    }

    /**
     * Constructs a {@code Setting} from a {@link File}.
     *
     * @param configFile    The configuration file.
     * @param charset       The character set to use.
     * @param isUseVariable {@code true} to enable variable substitution.
     */
    public Setting(final File configFile, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        this(ResourceKit.getResource(Assert.notNull(configFile)), charset, isUseVariable);
    }

    /**
     * Constructs a {@code Setting} from a {@link Resource}.
     *
     * @param resource      The resource representing the settings file.
     * @param charset       The character set to use.
     * @param isUseVariable {@code true} to enable variable substitution.
     */
    public Setting(final Resource resource, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        this(resource, new Loader(charset, isUseVariable));
    }

    /**
     * Constructs a {@code Setting} from a {@link Resource} using a custom loader.
     *
     * @param resource The resource representing the settings file.
     * @param loader   The custom loader to use for parsing.
     */
    public Setting(final Resource resource, Loader loader) {
        this.resource = resource;
        this.loader = Objects.requireNonNullElseGet(loader, () -> new Loader(DEFAULT_CHARSET, false));
        this.groupedMap = this.loader.load(resource);
    }

    /**
     * Creates a new, empty {@code Setting} instance for manual population.
     *
     * @return A new {@code Setting} instance.
     */
    public static Setting of() {
        return new Setting();
    }

    /**
     * Reloads the configuration from the original resource file.
     *
     * @return This {@code Setting} instance for chaining.
     */
    public synchronized Setting load() {
        Assert.notNull(this.loader, "SettingLoader must be not null!");
        this.groupedMap = loader.load(this.resource);
        return this;
    }

    /**
     * Enables automatic reloading of the configuration file when it changes on the filesystem.
     */
    public void autoLoad() {
        autoLoad(null);
    }

    /**
     * Enables automatic reloading of the configuration file when it changes, with a callback.
     *
     * @param callback A consumer to be called after the file has been successfully reloaded.
     */
    public void autoLoad(final Consumer<Setting> callback) {
        Assert.notNull(this.resource, "Setting resource must be not null !");
        IoKit.closeQuietly(this.watchMonitor); // Close any existing monitor
        this.watchMonitor = WatchKit.ofModify(resource.getUrl(), new DelayWatcher(new SimpleWatcher() {

            @Serial
            private static final long serialVersionUID = 2852560512835L;

            @Override
            public void onModify(final WatchEvent<?> event, final WatchKey key) {
                load();
                if (callback != null) {
                    callback.accept(Setting.this);
                }
            }
        }, 600));
        this.watchMonitor.start();
        Logger.debug("Auto-load enabled for [{}]", this.resource.getUrl());
    }

    /**
     * Stops the automatic reloading of the configuration file.
     */
    public void stopAutoLoad() {
        IoKit.closeQuietly(this.watchMonitor);
        this.watchMonitor = null;
    }

    /**
     * Gets the URL of the loaded settings file.
     *
     * @return The URL of the settings file.
     */
    public URL getSettingUrl() {
        return (null == this.resource) ? null : this.resource.getUrl();
    }

    /**
     * Gets the file path of the loaded settings file.
     *
     * @return The path of the settings file.
     */
    public String getSettingPath() {
        final URL settingUrl = getSettingUrl();
        return (null == settingUrl) ? null : settingUrl.getPath();
    }

    @Override
    public int size() {
        return this.groupedMap.size();
    }

    @Override
    public Object getObjectByGroup(final CharSequence key, final CharSequence group, final Object defaultValue) {
        /**
         * Gets an object value for the specified key in the specified group.
         *
         * @param key          the key to look up
         * @param group        the group to look in
         * @param defaultValue the default value to return if the key is not found
         * @return the value associated with the key, or the default value if not found
         */
        final String result = this.groupedMap.get(group, key);
        if (result == null && logIfNull) {
            Logger.debug("No data found for key [{}] in group [{}]", key, group);
        }
        return ObjectKit.defaultIfNull(result, defaultValue);
    }

    /**
     * Gets and removes a property value. It tries each key in the provided list until a non-null value is found, which
     * is then returned and removed.
     *
     * @param keys A list of keys to try, often used for aliases.
     * @return The string value, or null if no key is found.
     */
    public String getAndRemove(final String... keys) {
        String value = null;
        for (final String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return value;
    }

    /**
     * Gets all key-value pairs for a specific group as a mutable map.
     *
     * @param group The group name.
     * @return A map of the settings in the group.
     */
    public Map<String, String> getMap(final String group) {
        final LinkedHashMap<String, String> map = this.groupedMap.get(group);
        return (null != map) ? map : new LinkedHashMap<>(0);
    }

    /**
     * Gets all settings under a specific group as a new {@code Setting} object.
     *
     * @param group The group name.
     * @return A new {@code Setting} instance containing the properties of the group.
     */
    public Setting getSetting(final String group) {
        final Setting setting = new Setting();
        setting.putAll(this.getMap(group));
        return setting;
    }

    /**
     * Gets all settings under a specific group as a {@link java.util.Properties} object.
     *
     * @param group The group name.
     * @return A new {@code Properties} object.
     */
    public java.util.Properties getProperties(final String group) {
        final java.util.Properties properties = new java.util.Properties();
        properties.putAll(getMap(group));
        return properties;
    }

    /**
     * Gets all settings under a specific group as a {@link Props} object.
     *
     * @param group The group name.
     * @return A new {@code Props} object.
     */
    public Props getProps(final String group) {
        final Props props = new Props();
        props.putAll(getMap(group));
        return props;
    }

    /**
     * Stores the current settings to the original file, overwriting its content. This will not work if the file is
     * inside a JAR.
     */
    public void store() {
        final URL resourceUrl = getSettingUrl();
        Assert.notNull(resourceUrl, "Setting path must be not null!");
        store(FileKit.file(resourceUrl));
    }

    /**
     * Stores the current settings to a file at the specified absolute path.
     *
     * @param absolutePath The absolute path to the destination file.
     */
    public void store(final String absolutePath) {
        store(FileKit.touch(absolutePath));
    }

    /**
     * Stores the current settings to the specified file.
     *
     * @param file The destination file.
     */
    public void store(final File file) {
        Assert.notNull(this.loader, "SettingLoader must be not null!");
        this.loader.store(this.groupedMap, file);
    }

    /**
     * Converts this {@code Setting} to a {@link Props} object, flattening the groups into keys with prefixes (e.g.,
     * "group.key").
     *
     * @return A {@link Props} object.
     */
    public Props toProps() {
        final Props props = new Props();
        for (final Entry<String, LinkedHashMap<String, String>> groupEntry : this.groupedMap.entrySet()) {
            String group = groupEntry.getKey();
            for (final Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (null != key && null != value) {
                    props.setProperty(StringKit.isEmpty(group) ? key : group + Symbol.C_DOT + key, value);
                }
            }
        }
        return props;
    }

    /**
     * Gets the underlying {@link GroupedMap} that stores the settings.
     *
     * @return The underlying {@link GroupedMap} used for storage.
     */
    public GroupedMap getGroupedMap() {
        return this.groupedMap;
    }

    /**
     * Gets a list of all group names defined in this setting.
     *
     * @return A list of all group names in this setting.
     */
    public List<String> getGroups() {
        return ListKit.of(this.groupedMap.keySet());
    }

    /**
     * Sets the regular expression for identifying variables.
     *
     * @param regex The regular expression.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting setVarRegex(final String regex) {
        if (null == this.loader) {
            throw new NullPointerException("SettingLoader is null!");
        }
        this.loader.setVarRegex(regex);
        return this;
    }

    /**
     * Sets whether to log a debug message when a requested key is not found.
     *
     * @param logIfNull {@code true} to enable logging.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting setLogIfNull(final boolean logIfNull) {
        this.logIfNull = logIfNull;
        return this;
    }

    /**
     * Checks if a specific group is empty.
     *
     * @param group The group name.
     * @return {@code true} if the group is empty or does not exist.
     */
    public boolean isEmpty(final String group) {
        return this.groupedMap.isEmpty(group);
    }

    /**
     * Checks if a specific group contains a given key.
     *
     * @param group The group name.
     * @param key   The key.
     * @return {@code true} if the key exists in the group.
     */
    public boolean containsKey(final String group, final String key) {
        return this.groupedMap.containsKey(group, key);
    }

    /**
     * Checks if a specific group contains a given value.
     *
     * @param group The group name.
     * @param value The value.
     * @return {@code true} if the value exists in the group.
     */
    public boolean containsValue(final String group, final String value) {
        return this.groupedMap.containsValue(group, value);
    }

    /**
     * Puts a key-value pair into a specific group.
     *
     * @param key   The key.
     * @param group The group name.
     * @param value The value.
     * @return The previous value associated with the key, or null.
     */
    public String putByGroup(final String key, final String group, final String value) {
        return this.groupedMap.put(group, key, value);
    }

    /**
     * Removes a key from a specific group.
     *
     * @param group The group name.
     * @param key   The key to remove.
     * @return The removed value, or null if not found.
     */
    public String remove(final String group, final Object key) {
        return this.groupedMap.remove(group, Convert.toString(key));
    }

    /**
     * Puts all key-value pairs from a map into a specific group.
     *
     * @param group The group name.
     * @param m     The map of key-value pairs.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting putAll(final String group, final Map<? extends String, ? extends String> m) {
        this.groupedMap.putAll(group, m);
        return this;
    }

    /**
     * Merges all groups and settings from another {@code Setting} instance into this one.
     *
     * @param setting The {@code Setting} instance to merge.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting addSetting(final Setting setting) {
        for (final Entry<String, LinkedHashMap<String, String>> e : setting.getGroupedMap().entrySet()) {
            this.putAll(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * Clears all key-value pairs from a specific group.
     *
     * @param group The group name.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting clear(final String group) {
        this.groupedMap.clear(group);
        return this;
    }

    /**
     * Returns a set of all keys within a specific group.
     *
     * @param group The group name.
     * @return The set of keys.
     */
    public Set<String> keySet(final String group) {
        return this.groupedMap.keySet(group);
    }

    /**
     * Returns a collection of all values within a specific group.
     *
     * @param group The group name.
     * @return The collection of values.
     */
    public Collection<String> values(final String group) {
        return this.groupedMap.values(group);
    }

    /**
     * Returns a set of all key-value entries within a specific group.
     *
     * @param group The group name.
     * @return The set of entries.
     */
    public Set<Entry<String, String>> entrySet(final String group) {
        return this.groupedMap.entrySet(group);
    }

    /**
     * Sets a value in the default (empty) group.
     *
     * @param key   The key.
     * @param value The value.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting set(final String key, final String value) {
        this.put(key, value);
        return this;
    }

    /**
     * Sets multiple properties using an array of lambda method reference suppliers.
     * <p>
     * Example:
     * 
     * <pre>
     * User user = new User("test", "Test User");
     * Setting.of().setFields(user::getUsername, user::getNickname);
     * </pre>
     *
     * @param fields An array of suppliers, where each supplier returns a property value.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting setFields(final SupplierX<String>... fields) {
        Arrays.stream(fields).forEach(f -> set(LambdaKit.getFieldName(f), f.get()));
        return this;
    }

    /**
     * Sets a value in a specific group.
     *
     * @param key   The key.
     * @param group The group name.
     * @param value The value.
     * @return this {@code Setting} instance for chaining.
     */
    public Setting setByGroup(final String key, final String group, final String value) {
        this.putByGroup(key, group, value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return this.groupedMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.groupedMap.containsKey(DEFAULT_GROUP, Convert.toString(key));
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.groupedMap.containsValue(DEFAULT_GROUP, Convert.toString(value));
    }

    @Override
    public String get(final Object key) {
        return getString((String) key);
    }

    @Override
    public String put(final String key, final String value) {
        return this.groupedMap.put(DEFAULT_GROUP, key, value);
    }

    @Override
    public String remove(final Object key) {
        return remove(DEFAULT_GROUP, key);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends String> m) {
        this.groupedMap.putAll(DEFAULT_GROUP, m);
    }

    @Override
    public void clear() {
        this.groupedMap.clear(DEFAULT_GROUP);
    }

    @Override
    public Set<String> keySet() {
        return this.groupedMap.keySet(DEFAULT_GROUP);
    }

    @Override
    public Collection<String> values() {
        return this.groupedMap.values(DEFAULT_GROUP);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.groupedMap.entrySet(DEFAULT_GROUP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupedMap, resource);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Setting other = (Setting) obj;
        return Objects.equals(groupedMap, other.groupedMap) && Objects.equals(resource, other.resource);
    }

    @Override
    public String toString() {
        return groupedMap.toString();
    }

}
