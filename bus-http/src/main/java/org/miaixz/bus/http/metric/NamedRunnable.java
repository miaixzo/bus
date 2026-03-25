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
package org.miaixz.bus.http.metric;

/**
 * An abstract {@link Runnable} implementation that always sets its thread name.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class NamedRunnable implements Runnable {

    /**
     * The name of the runnable, which will be set as the thread name.
     */
    protected final String name;

    /**
     * Constructs a new {@code NamedRunnable} with a formatted name.
     *
     * @param format The format string for the name.
     * @param args   The arguments for the format string.
     */
    public NamedRunnable(String format, Object... args) {
        this.name = String.format(format, args);
    }

    /**
     * Executes the runnable, setting the thread name before execution and restoring it afterwards.
     */
    @Override
    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    /**
     * The abstract method to be implemented by subclasses, containing the actual logic to be executed.
     */
    protected abstract void execute();

}
