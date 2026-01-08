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
package org.miaixz.bus.core.xyz;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.io.watch.WatchKind;
import org.miaixz.bus.core.io.watch.WatchMonitor;
import org.miaixz.bus.core.io.watch.Watcher;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Watch service utility class, mainly for the convenient creation of file watchers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchKit {

    /**
     * Creates and initializes a file watcher.
     *
     * @param url    The URL of the file or directory to watch.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final URL url, final WatchEvent.Kind<?>... events) {
        return of(url, 0, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param url    The URL.
     * @param max    The maximum depth to watch recursively (1 or less means no recursion).
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final URL url, final int max, final WatchEvent.Kind<?>... events) {
        return of(UrlKit.toURI(url), max, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param uri    The URI.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final URI uri, final WatchEvent.Kind<?>... events) {
        return of(uri, 0, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param uri    The URI.
     * @param max    The maximum depth to watch recursively.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final URI uri, final int max, final WatchEvent.Kind<?>... events) {
        return of(Paths.get(uri), max, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param file   The file or directory.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final File file, final WatchEvent.Kind<?>... events) {
        return of(file, 0, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param file   The file or directory.
     * @param max    The maximum depth to watch recursively.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final File file, final int max, final WatchEvent.Kind<?>... events) {
        return of(file.toPath(), max, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param path   The file or directory path.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final String path, final WatchEvent.Kind<?>... events) {
        return of(path, 0, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param path   The file or directory path.
     * @param max    The maximum depth to watch recursively.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final String path, final int max, final WatchEvent.Kind<?>... events) {
        return of(Paths.get(path), max, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param path   The `Path` to watch.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final Path path, final WatchEvent.Kind<?>... events) {
        return of(path, 0, events);
    }

    /**
     * Creates and initializes a file watcher.
     *
     * @param path   The `Path` to watch.
     * @param max    The maximum depth to watch recursively.
     * @param events The events to watch for.
     * @return A `WatchMonitor` instance.
     */
    public static WatchMonitor of(final Path path, final int max, final WatchEvent.Kind<?>... events) {
        return new WatchMonitor(path, max, events);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param url     The URL to watch.
     * @param watcher The {@link Watcher} to handle events.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final URL url, final Watcher watcher) {
        return ofAll(url, 0, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param url     The URL to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final URL url, final int max, final Watcher watcher) {
        return ofAll(UrlKit.toURI(url), max, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param uri     The URI to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final URI uri, final Watcher watcher) {
        return ofAll(uri, 0, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param uri     The URI to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final URI uri, final int max, final Watcher watcher) {
        return ofAll(Paths.get(uri), max, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param file    The file or directory to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final File file, final Watcher watcher) {
        return ofAll(file, 0, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param file    The file or directory to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final File file, final int max, final Watcher watcher) {
        return ofAll(file.toPath(), max, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param path    The path to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final String path, final Watcher watcher) {
        return ofAll(path, 0, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param path    The path to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final String path, final int max, final Watcher watcher) {
        return ofAll(Paths.get(path), max, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param path    The `Path` to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final Path path, final Watcher watcher) {
        return ofAll(path, 0, watcher);
    }

    /**
     * Creates a watcher for all event types.
     *
     * @param path    The `Path` to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofAll(final Path path, final int max, final Watcher watcher) {
        return of(path, max, WatchKind.ALL).setWatcher(watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param url     The URL to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final URL url, final Watcher watcher) {
        return ofModify(url, 0, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param url     The URL to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final URL url, final int max, final Watcher watcher) {
        return ofModify(UrlKit.toURI(url), max, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param uri     The URI to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final URI uri, final Watcher watcher) {
        return ofModify(uri, 0, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param uri     The URI to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final URI uri, final int max, final Watcher watcher) {
        return ofModify(Paths.get(uri), max, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param file    The file to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final File file, final Watcher watcher) {
        return ofModify(file, 0, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param file    The file to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final File file, final int max, final Watcher watcher) {
        return ofModify(file.toPath(), max, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param path    The path to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final String path, final Watcher watcher) {
        return ofModify(path, 0, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param path    The path to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final String path, final int max, final Watcher watcher) {
        return ofModify(Paths.get(path), max, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param path    The `Path` to watch.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final Path path, final Watcher watcher) {
        return ofModify(path, 0, watcher);
    }

    /**
     * Creates a watcher for modification events.
     *
     * @param path    The `Path` to watch.
     * @param max     The maximum depth to watch recursively.
     * @param watcher The {@link Watcher}.
     * @return A {@link WatchMonitor} instance.
     */
    public static WatchMonitor ofModify(final Path path, final int max, final Watcher watcher) {
        final WatchMonitor watchMonitor = of(path, max, WatchKind.MODIFY.getValue());
        watchMonitor.setWatcher(watcher);
        return watchMonitor;
    }

    /**
     * Registers a `Watchable` object with a `WatchService`.
     *
     * @param watchable The object to register.
     * @param watcher   The `WatchService`.
     * @param events    The events to watch for.
     * @return The {@link WatchKey}.
     */
    public static WatchKey register(
            final Watchable watchable,
            final WatchService watcher,
            final WatchEvent.Kind<?>... events) {
        try {
            return watchable.register(watcher, events);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Resolves the full path of a file from a `WatchEvent`.
     *
     * @param event The event.
     * @param key   The {@link WatchKey}.
     * @return The full `Path`.
     */
    public static Path resolvePath(final WatchEvent<?> event, final WatchKey key) {
        Assert.notNull(event, "WatchEvent must be not null!");
        Assert.notNull(key, "WatchKey must be not null!");

        return PathResolve.of((Path) key.watchable(), (Path) event.context());
    }

}
