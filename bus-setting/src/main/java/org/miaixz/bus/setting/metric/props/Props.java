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
package org.miaixz.bus.setting.metric.props;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Writer;
import java.net.URL;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.watch.SimpleWatcher;
import org.miaixz.bus.core.io.watch.WatchMonitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.getter.TypeGetter;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.Setting;

/**
 * A wrapper class for reading and handling {@code .properties} files. It extends {@code java.util.Properties} with
 * additional convenience methods for typed data retrieval, automatic reloading, and bean mapping.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Props extends java.util.Properties implements TypeGetter<CharSequence> {

    @Serial
    private static final long serialVersionUID = 2852230820065L;

    /**
     * A cache for {@code Props} instances, keyed by file path.
     */
    private static final Map<String, Props> CACHE_PROPS = new ConcurrentHashMap<>();
    /**
     * The {@link Resource} object representing the properties file.
     */
    private Resource resource;
    /**
     * A monitor for watching file changes to support auto-reloading.
     */
    private WatchMonitor watchMonitor;
    /**
     * The character set used for reading the properties file.
     */
    private transient java.nio.charset.Charset charset = Charset.ISO_8859_1;

    /**
     * Constructs a new, empty {@code Props} object.
     */
    public Props() {
    }

    /**
     * Constructs a {@code Props} object by loading a properties file from the classpath.
     *
     * @param path The path to the properties file, relative to the classpath root, or an absolute path.
     */
    public Props(final String path) {
        this(path, null);
    }

    /**
     * Constructs a {@code Props} object by loading a properties file with a specified charset.
     *
     * @param path    The path to the properties file (relative or absolute).
     * @param charset The character set to use for reading the file.
     */
    public Props(final String path, final java.nio.charset.Charset charset) {
        Assert.notBlank(path, "Blank properties file path!");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(ResourceKit.getResource(path));
    }

    /**
     * Constructs a {@code Props} object from a {@link File}.
     *
     * @param propertiesFile The properties file.
     */
    public Props(final File propertiesFile) {
        this(propertiesFile, null);
    }

    /**
     * Constructs a {@code Props} object from a {@link File} with a specified charset.
     *
     * @param propertiesFile The properties file.
     * @param charset        The character set to use.
     */
    public Props(final File propertiesFile, final java.nio.charset.Charset charset) {
        Assert.notNull(propertiesFile, "Null properties file!");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(ResourceKit.getResource(propertiesFile));
    }

    /**
     * Constructs a {@code Props} object from a {@link Resource} with a specified charset.
     *
     * @param resource The resource representing the properties file.
     * @param charset  The character set to use.
     */
    public Props(final Resource resource, final java.nio.charset.Charset charset) {
        Assert.notNull(resource, "Null properties resource!");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(resource);
    }

    /**
     * Constructs a {@code Props} object from an existing {@link java.util.Properties} object.
     *
     * @param properties The properties to copy.
     */
    public Props(final java.util.Properties properties) {
        if (MapKit.isNotEmpty(properties)) {
            this.putAll(properties);
        }
    }

    /**
     * Creates a new, empty {@code Props} object.
     *
     * @return A new {@code Props} instance.
     */
    public static Props of() {
        return new Props();
    }

    /**
     * Creates a {@code Props} object by loading a file from the classpath.
     *
     * @param resource The path to the resource, relative to the classpath root.
     * @return A new {@code Props} instance.
     */
    public static Props of(final String resource) {
        return new Props(resource);
    }

    /**
     * Creates a {@code Props} object by loading a file from the classpath with a specified charset.
     *
     * @param resource The path to the resource.
     * @param charset  The character set to use.
     * @return A new {@code Props} instance.
     */
    public static Props of(final String resource, final java.nio.charset.Charset charset) {
        return new Props(resource, charset);
    }

    /**
     * Creates a copy of an existing {@code Props} object.
     *
     * @param properties The {@code Props} object to copy.
     * @return A new {@code Props} instance.
     */
    public static Props of(final Props properties) {
        return new Props(properties);
    }

    /**
     * Gets a cached {@code Props} instance for a given resource name. If the name has no extension, {@code .properties}
     * is assumed.
     *
     * @param name The name of the properties file.
     * @return A cached or new {@code Props} instance.
     */
    public static Props get(final String name) {
        return CACHE_PROPS.computeIfAbsent(name, (filePath) -> {
            final String extName = FileName.extName(filePath);
            if (StringKit.isEmpty(extName)) {
                filePath = filePath + "." + Setting.EXT_NAME;
            }
            return new Props(filePath);
        });
    }

    /**
     * Parses a string content in {@code .properties} format and populates a map.
     *
     * @param result  The map to populate with the parsed key-value pairs.
     * @param content The string content to parse.
     */
    public static void parse(Map<String, Object> result, String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (StringKit.isBlank(line) || line.startsWith(Symbol.HASH) || !line.contains(Symbol.EQUAL)) {
                continue;
            }
            String key = line.substring(0, line.indexOf(Symbol.EQUAL)).trim();
            String value = line.substring(line.indexOf(Symbol.EQUAL) + 1).trim();
            if (StringKit.isNotBlank(value)) {
                result.put(key, value);
            }
        }
    }

    /**
     * Gets the first {@code Props} instance that can be successfully loaded from a list of resource names.
     *
     * @param names The resource names to try.
     * @return The first found {@code Props} instance, or null if none are found.
     */
    public static Props getFirstFound(final String... names) {
        for (final String name : names) {
            try {
                return get(name);
            } catch (final InternalException e) {
                // Ignore and try the next name.
            }
        }
        return null;
    }

    /**
     * Gets a {@code Props} instance containing the current system properties.
     *
     * @return A new {@code Props} instance with system properties.
     */
    public static Props getProperties() {
        return new Props(System.getProperties());
    }

    /**
     * Initializes this {@code Props} object by loading from the given {@link URL}.
     *
     * @param url The URL of the properties file.
     */
    public void load(final URL url) {
        load(ResourceKit.getResource(url));
    }

    /**
     * Initializes this {@code Props} object by loading from the given {@link Resource}.
     *
     * @param resource The resource to load.
     */
    public void load(final Resource resource) {
        Assert.notNull(resource, "Properties resource must be not null!");
        this.resource = resource;
        ResourceKit.loadTo(this, resource, this.charset);
    }

    /**
     * Reloads the properties from the original resource.
     */
    public void load() {
        this.load(this.resource);
    }

    /**
     * Enables or disables automatic reloading of the properties file when it changes on the filesystem.
     *
     * @param autoReload {@code true} to enable auto-reloading, {@code false} to disable it.
     */
    public void autoLoad(final boolean autoReload) {
        if (autoReload) {
            Assert.notNull(this.resource, "Properties resource must be not null for auto-reloading!");
            IoKit.closeQuietly(this.watchMonitor); // Close any existing monitor.
            this.watchMonitor = WatchKit.ofModify(this.resource.getUrl(), new SimpleWatcher() {

                @Serial
                private static final long serialVersionUID = 2853080953378L;

                @Override
                public void onModify(final WatchEvent<?> event, final WatchKey key) {
                    load();
                }
            });
            this.watchMonitor.start();
        } else {
            IoKit.closeQuietly(this.watchMonitor);
            this.watchMonitor = null;
        }
    }

    @Override
    public Object getObject(final CharSequence key, final Object defaultValue) {
        Assert.notNull(key, "Key must be not null!");
        return ObjectKit.defaultIfNull(getProperty(key.toString()), defaultValue);
    }

    /**
     * Gets a value using a lambda method reference to resolve the property name and return type.
     *
     * @param func The method reference (e.g., {@code Config::getUsername}).
     * @param <P>  The type of the class containing the method.
     * @param <T>  The return type of the method.
     * @return The property value converted to the specified type.
     */
    public <P, T> T get(final FunctionX<P, T> func) {
        final LambdaX lambdaX = LambdaKit.resolve(func);
        return get(lambdaX.getFieldName(), lambdaX.getReturnType());
    }

    /**
     * Gets and removes a property value. It tries each key in the provided list until a non-null value is found, which
     * is then returned and removed.
     *
     * @param keys A list of keys to try, often used for aliases.
     * @return The string value, or null if no key is found.
     */
    public String getAndRemoveString(final String... keys) {
        Object value = null;
        for (final String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return (String) value;
    }

    /**
     * Extracts a subset of properties that share a common prefix. The prefix is removed from the keys in the resulting
     * {@code Props} object.
     * <p>
     * Example:
     * 
     * <pre>
     * a.b = 1
     * a.c = 2
     * b.a = 3
     * </pre>
     * 
     * Calling {@code getSubProps("a")} would return a {@code Props} object containing:
     * 
     * <pre>
     * b = 1
     * c = 2
     * </pre>
     *
     * @param prefix The prefix to match. A dot is automatically appended if not present.
     * @return A new {@code Props} object containing the subset of properties.
     */
    public Props getSubProps(final String prefix) {
        final Props subProps = new Props();
        final String finalPrefix = StringKit.addSuffixIfNot(prefix, Symbol.DOT);
        final int prefixLength = finalPrefix.length();

        forEach((key, value) -> {
            final String keyStr = key.toString();
            if (StringKit.startWith(keyStr, finalPrefix)) {
                subProps.set(StringKit.subSuf(keyStr, prefixLength), value);
            }
        });

        return subProps;
    }

    /**
     * Creates a new {@code Props} instance containing all the properties from this one.
     *
     * @return A new {@code Props} instance.
     */
    public Props toProperties() {
        final Props properties = new Props();
        properties.putAll(this);
        return properties;
    }

    /**
     * Maps the properties to a new Java Bean object. Supports nested properties.
     *
     * @param <T>       The type of the bean.
     * @param beanClass The class of the Java Bean to create and populate.
     * @return The newly created and populated bean object.
     */
    public <T> T toBean(final Class<T> beanClass) {
        return toBean(beanClass, null);
    }

    /**
     * Maps a subset of properties (filtered by a prefix) to a new Java Bean object.
     *
     * @param <T>       The type of the bean.
     * @param beanClass The class of the Java Bean to create and populate.
     * @param prefix    The prefix to filter properties by. Only properties starting with this prefix will be mapped.
     * @return The newly created and populated bean object.
     */
    public <T> T toBean(final Class<T> beanClass, final String prefix) {
        final T bean = ReflectKit.newInstanceIfPossible(beanClass);
        return toBean(bean, prefix);
    }

    /**
     * Maps the properties to an existing Java Bean object.
     *
     * @param <T>  The type of the bean.
     * @param bean The Java Bean object to populate.
     * @return The populated bean object.
     */
    public <T> T toBean(final T bean) {
        return toBean(bean, null);
    }

    /**
     * Maps a subset of properties (filtered by a prefix) to an existing Java Bean object.
     *
     * @param <T>    The type of the bean.
     * @param bean   The Java Bean object to populate.
     * @param prefix The prefix to filter properties by.
     * @return The populated bean object.
     */
    public <T> T toBean(final T bean, String prefix) {
        prefix = StringKit.toStringOrEmpty(StringKit.addSuffixIfNot(prefix, Symbol.DOT));

        for (final java.util.Map.Entry<Object, Object> entry : this.entrySet()) {
            String key = (String) entry.getKey();
            if (!StringKit.startWith(key, prefix)) {
                continue; // Ignore properties that don't match the prefix
            }
            try {
                BeanKit.setProperty(bean, StringKit.subSuf(key, prefix.length()), entry.getValue());
            } catch (final Exception e) {
                // Ignore fields that fail to set (they might be for other configurations)
                Logger.debug("Ignore property: [{}], because of: {}", key, e.getMessage());
            }
        }

        return bean;
    }

    /**
     * Sets a property value. If the key does not exist, it is created. This change is not persisted to the file until
     * {@link #store(String)} is called.
     *
     * @param key   The property key.
     * @param value The property value.
     */
    public void set(final String key, final Object value) {
        super.setProperty(key, value.toString());
    }

    /**
     * Sets multiple properties using an array of lambda method reference suppliers. This is useful for setting
     * properties from a bean's getters.
     * <p>
     * Example:
     * 
     * <pre>
     * User user = new User("test", "Test User");
     * Props.of().setFields(user::getUsername, user::getNickname);
     * </pre>
     *
     * @param fields An array of suppliers, where each supplier returns a property value.
     * @return This {@code Props} instance for chaining.
     */
    public Props setFields(final SupplierX<?>... fields) {
        Arrays.stream(fields).forEach(f -> set(LambdaKit.getFieldName(f), f.get()));
        return this;
    }

    /**
     * Stores the current properties to a file at the specified absolute path, overwriting its previous content.
     *
     * @param absolutePath The absolute path to the destination file.
     * @throws InternalException if an I/O error occurs.
     */
    public void store(final String absolutePath) throws InternalException {
        try (Writer writer = FileKit.getWriter(absolutePath, charset, false)) {
            super.store(writer, null);
        } catch (final IOException e) {
            throw new InternalException(e, "Store properties to [{}] error!", absolutePath);
        }
    }

    /**
     * Stores the current properties to a file path relative to a given class.
     *
     * @param path  The path relative to the class.
     * @param clazz The class to which the path is relative.
     */
    public void store(final String path, final Class<?> clazz) {
        this.store(FileKit.getAbsolutePath(path, clazz));
    }

}
