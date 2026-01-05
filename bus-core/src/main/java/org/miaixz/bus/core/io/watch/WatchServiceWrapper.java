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
package org.miaixz.bus.core.io.watch;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A wrapper class for {@link java.nio.file.WatchService} that provides optional listening events and options. This
 * class simplifies the process of monitoring file system changes. Key functionalities include:
 * <ul>
 * <li>Registration: {@link #registerPath(Path, int)} to register paths for monitoring.</li>
 * <li>Monitoring: {@link #watch(Watcher, Predicate)} to start monitoring and define actions upon event triggers.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchServiceWrapper extends SimpleWrapper<WatchService> implements WatchService, Serializable {

    @Serial
    private static final long serialVersionUID = 2852236630789L;

    /**
     * The array of {@link WatchEvent.Kind} representing the types of events to listen for, such as creation,
     * modification, and deletion.
     */
    private WatchEvent.Kind<?>[] events;
    /**
     * The array of {@link WatchEvent.Modifier} representing optional modifiers for the watch service, such as
     * monitoring frequency.
     */
    private WatchEvent.Modifier[] modifiers;
    /**
     * A flag indicating whether this {@code WatchServiceWrapper} has been closed.
     */
    private boolean isClosed;

    /**
     * Constructs a new {@code WatchServiceWrapper} with the specified event kinds. A new underlying
     * {@link WatchService} is created for this wrapper.
     *
     * @param events An array of {@link WatchEvent.Kind} representing the types of events to listen for.
     */
    public WatchServiceWrapper(final WatchEvent.Kind<?>... events) {
        // Initialize the underlying WatchService.
        super(newWatchService());
        this.events = events;
    }

    /**
     * Creates a new {@code WatchServiceWrapper} instance with the given event kinds.
     *
     * @param events An array of {@link WatchEvent.Kind} representing the types of events to listen for, such as
     *               creation, modification, and deletion.
     * @return A new {@code WatchServiceWrapper} instance.
     */
    public static WatchServiceWrapper of(final WatchEvent.Kind<?>... events) {
        return new WatchServiceWrapper(events);
    }

    /**
     * Creates a new {@link WatchService} instance from the default file system.
     *
     * @return A new {@link WatchService} instance.
     * @throws InternalException If an {@link IOException} occurs while creating the watch service.
     */
    private static WatchService newWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if this {@code WatchServiceWrapper} has been closed.
     *
     * @return {@code true} if the watch service is closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * Closes the underlying {@link WatchService} and marks this wrapper as closed. This method is idempotent; calling
     * it multiple times has no further effect after the first call.
     */
    @Override
    public void close() {
        if (!this.isClosed) {
            this.isClosed = true;
            IoKit.closeQuietly(this.raw);
        }
    }

    /**
     * Retrieves a {@link WatchKey} from the underlying {@link WatchService} if one is immediately available.
     *
     * @return A {@link WatchKey} that has been queued, or {@code null} if none is immediately available.
     */
    @Override
    public WatchKey poll() {
        return this.raw.poll();
    }

    /**
     * Retrieves a {@link WatchKey} from the underlying {@link WatchService}, waiting up to the specified timeout.
     *
     * @param timeout How long to wait in units of {@code unit}.
     * @param unit    The unit of the {@code timeout} argument.
     * @return A {@link WatchKey} that has been queued, or {@code null} if none is available within the given timeout.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    @Override
    public WatchKey poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.raw.poll(timeout, unit);
    }

    /**
     * Retrieves a {@link WatchKey} from the underlying {@link WatchService}, waiting if necessary until one is
     * available.
     *
     * @return A {@link WatchKey} that has been queued.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    @Override
    public WatchKey take() throws InterruptedException {
        return this.raw.take();
    }

    /**
     * Sets the array of {@link WatchEvent.Kind} for this wrapper. These events will be used when registering new paths.
     *
     * @param kinds An array of {@link WatchKind} enum constants representing the event types.
     * @return This {@code WatchServiceWrapper} instance, allowing for method chaining.
     */
    public WatchServiceWrapper setEvents(final WatchKind... kinds) {
        if (ArrayKit.isNotEmpty(kinds)) {
            setEvents(ArrayKit.mapToArray(kinds, WatchKind::getValue, WatchEvent.Kind<?>[]::new));
        }
        return this;
    }

    /**
     * Sets the array of {@link WatchEvent.Kind} for this wrapper. These events will be used when registering new paths.
     *
     * @param events An array of {@link WatchEvent.Kind} representing the event types.
     * @return This {@code WatchServiceWrapper} instance, allowing for method chaining.
     */
    public WatchServiceWrapper setEvents(final WatchEvent.Kind<?>... events) {
        this.events = events;
        return this;
    }

    /**
     * Sets the array of {@link WatchEvent.Modifier} for this wrapper. These modifiers will be used when registering new
     * paths. Modifiers can include options like monitoring frequency.
     * <p>
     * Examples of modifiers:
     * <ol>
     * <li>{@code com.sun.nio.file.StandardWatchEventKinds}</li>
     * <li>{@code com.sun.nio.file.SensitivityWatchEventModifier}</li>
     * </ol>
     *
     * @param modifiers An array of {@link WatchEvent.Modifier} to apply during registration.
     * @return This {@code WatchServiceWrapper} instance, allowing for method chaining.
     */
    public WatchServiceWrapper setModifiers(final WatchEvent.Modifier... modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    /**
     * Registers a single {@link Watchable} object (e.g., a {@link Path}) with the underlying {@link WatchService}. The
     * events and modifiers configured in this wrapper will be used for registration.
     *
     * @param watchable The object to register for monitoring.
     * @return The {@link WatchKey} representing the registration, or {@code null} if registration failed (e.g., due to
     *         permissions).
     * @see Watchable#register(WatchService, WatchEvent.Kind[])
     * @see Watchable#register(WatchService, WatchEvent.Kind[], WatchEvent.Modifier...)
     * @throws InternalException If an {@link IOException} (other than {@link AccessDeniedException}) occurs during
     *                           registration.
     */
    public WatchKey register(final Watchable watchable) {
        final WatchEvent.Kind<?>[] kinds = ArrayKit.defaultIfEmpty(this.events, WatchKind.ALL);

        WatchKey watchKey = null;
        try {
            if (ArrayKit.isEmpty(this.modifiers)) {
                watchKey = watchable.register(this.raw, kinds);
            } else {
                watchKey = watchable.register(this.raw, kinds, this.modifiers);
            }
        } catch (final IOException e) {
            if (!(e instanceof AccessDeniedException)) {
                throw new InternalException(e);
            }
            // If AccessDeniedException, registration failed, return null.
        }

        return watchKey;
    }

    /**
     * Recursively registers the specified path and its subdirectories for monitoring. If the provided path is a
     * directory, it will be monitored, along with its subdirectories and files, up to the specified {@code maxDepth}.
     *
     * @param path     The {@link Path} to register for monitoring.
     * @param maxDepth The maximum depth for recursive directory monitoring. A value of 0 means only the current path is
     *                 registered. Use a positive value for recursive monitoring.
     * @return This {@code WatchServiceWrapper} instance, allowing for method chaining.
     */
    public WatchServiceWrapper registerPath(final Path path, final int maxDepth) {
        // Register the current directory or file.
        if (null == register(path)) {
            // Registration failed (e.g., due to permissions), skip this path.
            return this;
        }

        // Recursively register subdirectories up to maxDepth.
        PathResolve.walkFiles(path, maxDepth, new SimpleFileVisitor<>() {

            /**
             * Postvisitdirectory method.
             *
             * @return the FileVisitResult value
             */
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                // Continue adding directories.
                registerPath(dir, 0); // Register subdirectories with depth 0 (only the current subdirectory).
                return super.postVisitDirectory(dir, exc);
            }
        });
        return this;
    }

    /**
     * Executes the event retrieval and processing loop. This method blocks the current thread until a watch event is
     * available. Events are then dispatched to the provided {@link Watcher}.
     * <p>
     * {@link WatchEvent#context()} provides the relative path of the affected file or directory to the monitored path.
     * {@link WatchKey#watchable()} provides the {@link Path} that is being monitored.
     *
     * @param watcher     The {@link Watcher} instance to which events are dispatched.
     * @param watchFilter An optional {@link Predicate} to filter watch events. Only events for which
     *                    {@link Predicate#test(Object)} returns {@code true} will be processed. If {@code null}, no
     *                    filtering is applied.
     */
    public void watch(final Watcher watcher, final Predicate<WatchEvent<?>> watchFilter) {
        watch((event, watchKey) -> {
            final WatchEvent.Kind<?> kind = event.kind();

            if (kind == WatchKind.CREATE.getValue()) {
                watcher.onCreate(event, watchKey);
            } else if (kind == WatchKind.MODIFY.getValue()) {
                watcher.onModify(event, watchKey);
            } else if (kind == WatchKind.DELETE.getValue()) {
                watcher.onDelete(event, watchKey);
            } else if (kind == WatchKind.OVERFLOW.getValue()) {
                watcher.onOverflow(event, watchKey);
            }
        }, watchFilter);
    }

    /**
     * Executes the event retrieval and processing loop. This method blocks the current thread until a watch event is
     * available. Events are then processed by the provided {@link BiConsumer}.
     * <p>
     * {@link WatchEvent#context()} provides the relative path of the affected file or directory to the monitored path.
     * {@link WatchKey#watchable()} provides the {@link Path} that is being monitored.
     *
     * @param action The {@link BiConsumer} functional interface to handle the {@link WatchEvent} and {@link WatchKey}.
     */
    public void watch(final BiConsumer<WatchEvent<?>, WatchKey> action) {
        watch(action, null);
    }

    /**
     * Executes the event retrieval and processing loop. This method blocks the current thread until a watch event is
     * available. Events are then processed by the provided {@link BiConsumer}, optionally filtered by a
     * {@link Predicate}.
     * <p>
     * {@link WatchEvent#context()} provides the relative path of the affected file or directory to the monitored path.
     * {@link WatchKey#watchable()} provides the {@link Path} that is being monitored.
     *
     * @param action      The {@link BiConsumer} functional interface to handle the {@link WatchEvent} and
     *                    {@link WatchKey}.
     * @param watchFilter An optional {@link Predicate} to filter watch events. Only events for which
     *                    {@link Predicate#test(Object)} returns {@code true} will be processed. If {@code null}, no
     *                    filtering is applied.
     */
    public void watch(final BiConsumer<WatchEvent<?>, WatchKey> action, final Predicate<WatchEvent<?>> watchFilter) {
        final WatchKey wk;
        try {
            wk = raw.take();
        } catch (final InterruptedException | ClosedWatchServiceException e) {
            // User interrupted or watch service closed.
            close();
            return;
        }

        for (final WatchEvent<?> event : wk.pollEvents()) {
            // If monitoring a specific file, check if the current event is related to that file.
            if (null != watchFilter && !watchFilter.test(event)) {
                continue;
            }
            action.accept(event, wk);
        }

        wk.reset();
    }

}
