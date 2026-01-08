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
package org.miaixz.bus.core.lang.thread;

import java.io.Serial;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A builder for creating {@link ThreadFactory} instances. Inspired by Guava's ThreadFactoryBuilder.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadFactoryBuilder implements Builder<ThreadFactory> {

    @Serial
    private static final long serialVersionUID = 2852280056509L;

    /**
     * The backing `ThreadFactory` to be used for thread creation.
     */
    private ThreadFactory backingThreadFactory;
    /**
     * The prefix for thread names.
     */
    private String namePrefix;
    /**
     * Whether the threads should be daemon threads.
     */
    private Boolean daemon;
    /**
     * The priority for created threads.
     */
    private Integer priority;
    /**
     * The handler for uncaught exceptions.
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * Creates a new {@code ThreadFactoryBuilder}.
     *
     * @return a new {@code ThreadFactoryBuilder}.
     */
    public static ThreadFactoryBuilder of() {
        return new ThreadFactoryBuilder();
    }

    /**
     * Builds a {@link ThreadFactory} from the builder settings.
     *
     * @param builder The {@code ThreadFactoryBuilder}.
     * @return a new {@link ThreadFactory}.
     */
    private static ThreadFactory build(final ThreadFactoryBuilder builder) {
        final ThreadFactory backingThreadFactory = (null != builder.backingThreadFactory) ? builder.backingThreadFactory
                : Executors.defaultThreadFactory();
        final String namePrefix = builder.namePrefix;
        final Boolean daemon = builder.daemon;
        final Integer priority = builder.priority;
        final UncaughtExceptionHandler handler = builder.uncaughtExceptionHandler;
        final AtomicLong count = (null == namePrefix) ? null : new AtomicLong();
        return r -> {
            final Thread thread = backingThreadFactory.newThread(r);
            if (null != namePrefix) {
                thread.setName(namePrefix + count.getAndIncrement());
            }
            if (null != daemon) {
                thread.setDaemon(daemon);
            }
            if (null != priority) {
                thread.setPriority(priority);
            }
            if (null != handler) {
                thread.setUncaughtExceptionHandler(handler);
            }
            return thread;
        };
    }

    /**
     * Sets the backing {@link ThreadFactory} to be used for creating new threads.
     *
     * @param backingThreadFactory The backing `ThreadFactory`.
     * @return this builder instance.
     */
    public ThreadFactoryBuilder setThreadFactory(final ThreadFactory backingThreadFactory) {
        this.backingThreadFactory = backingThreadFactory;
        return this;
    }

    /**
     * Sets the prefix for thread names. For example, with a prefix of "my-thread-", threads will be named
     * "my-thread-0", "my-thread-1", etc.
     *
     * @param namePrefix The thread name prefix.
     * @return this builder instance.
     */
    public ThreadFactoryBuilder setNamePrefix(final String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    /**
     * Sets whether the created threads should be daemon threads.
     *
     * @param daemon `true` for daemon threads, `false` otherwise.
     * @return this builder instance.
     */
    public ThreadFactoryBuilder setDaemon(final boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * Sets the priority for created threads.
     *
     * @param priority The thread priority.
     * @return this builder instance.
     * @see Thread#MIN_PRIORITY
     * @see Thread#NORM_PRIORITY
     * @see Thread#MAX_PRIORITY
     */
    public ThreadFactoryBuilder setPriority(final int priority) {
        if (priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(
                    StringKit.format("Thread priority ({}) must be >= {}", priority, Thread.MIN_PRIORITY));
        }
        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    StringKit.format("Thread priority ({}) must be <= {}", priority, Thread.MAX_PRIORITY));
        }
        this.priority = priority;
        return this;
    }

    /**
     * Sets the handler for uncaught exceptions.
     *
     * @param uncaughtExceptionHandler The {@link UncaughtExceptionHandler}.
     * @return this builder instance.
     */
    public ThreadFactoryBuilder setUncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    /**
     * Builds a new {@link ThreadFactory} with the configured settings.
     *
     * @return a new {@link ThreadFactory}.
     */
    @Override
    public ThreadFactory build() {
        return build(this);
    }

}
