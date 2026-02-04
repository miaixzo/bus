/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.thread.threadlocal;

/**
 * A specialized {@link Thread} class that provides fast access to {@link FastThreadLocal} variables. This class is
 * designed to optimize the performance of {@link FastThreadLocal} operations by directly managing a
 * {@link ThreadLocalMap} for its thread-local variables.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpecificThread extends Thread {

    /**
     * The internal data structure that binds thread-local variables to this thread.
     */
    private ThreadLocalMap threadLocalMap;

    /**
     * Constructs a new {@code SpecificThread} instance. This constructor is equivalent to {@code new Thread()}.
     */
    public SpecificThread() {
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given name. This constructor is equivalent to
     * {@code new Thread(String)}.
     *
     * @param name The name of the new thread.
     */
    public SpecificThread(String name) {
        super(name);
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given {@link Runnable} as its run object. This
     * constructor is equivalent to {@code new Thread(Runnable)}.
     *
     * @param r The object whose {@code run} method is invoked when this thread is started.
     */
    public SpecificThread(Runnable r) {
        super(r);
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given {@link Runnable} as its run object and a name.
     * This constructor is equivalent to {@code new Thread(Runnable, String)}.
     *
     * @param r    The object whose {@code run} method is invoked when this thread is started.
     * @param name The name of the new thread.
     */
    public SpecificThread(Runnable r, String name) {
        super(r, name);
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given thread group and name. This constructor is
     * equivalent to {@code new Thread(ThreadGroup, String)}.
     *
     * @param group The thread group.
     * @param name  The name of the new thread.
     */
    public SpecificThread(ThreadGroup group, String name) {
        super(group, name);
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given thread group and {@link Runnable} as its run
     * object. This constructor is equivalent to {@code new Thread(ThreadGroup, Runnable)}.
     *
     * @param group The thread group.
     * @param r     The object whose {@code run} method is invoked when this thread is started.
     */
    public SpecificThread(ThreadGroup group, Runnable r) {
        super(group, r);
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given thread group, {@link Runnable} as its run object,
     * and a name. This constructor is equivalent to {@code new Thread(ThreadGroup, Runnable, String)}.
     *
     * @param group The thread group.
     * @param r     The object whose {@code run} method is invoked when this thread is started.
     * @param name  The name of the new thread.
     */
    public SpecificThread(ThreadGroup group, Runnable r, String name) {
        super(group, r, name);
    }

    /**
     * Constructs a new {@code SpecificThread} instance with the given thread group, {@link Runnable} as its run object,
     * a name, and a specified stack size. This constructor is equivalent to
     * {@code new Thread(ThreadGroup, Runnable, String, long)}.
     *
     * @param group     The thread group.
     * @param r         The object whose {@code run} method is invoked when this thread is started.
     * @param name      The name of the new thread.
     * @param stackSize The desired stack size for the new thread, or zero to indicate that this parameter is to be
     *                  ignored.
     */
    public SpecificThread(ThreadGroup group, Runnable r, String name, long stackSize) {
        super(group, r, name, stackSize);
    }

    /**
     * Returns the internal {@link ThreadLocalMap} data structure that binds thread-local variables to this thread. This
     * method is intended for internal use only and may change at any time.
     *
     * @return The {@link ThreadLocalMap} associated with this thread.
     */
    public final ThreadLocalMap getThreadLocalMap() {
        return threadLocalMap;
    }

    /**
     * Sets the internal {@link ThreadLocalMap} data structure that binds thread-local variables to this thread. This
     * method is intended for internal use only and may change at any time.
     *
     * @param threadLocalMap The {@link ThreadLocalMap} to associate with this thread.
     */
    public final void setThreadLocalMap(ThreadLocalMap threadLocalMap) {
        this.threadLocalMap = threadLocalMap;
    }

}
