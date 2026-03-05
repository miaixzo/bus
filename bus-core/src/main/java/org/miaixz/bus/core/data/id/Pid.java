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
package org.miaixz.bus.core.data.id;

import java.lang.management.ManagementFactory;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Singleton encapsulation of the process ID (PID). The PID information is obtained by calling
 * {@link ManagementFactory#getRuntimeMXBean()} on the first access, and the cached value is used thereafter.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Pid {

    /**
     * Singleton instance.
     */
    INSTANCE;

    private final int pid;

    Pid() {
        this.pid = getPid();
    }

    /**
     * Gets the current process ID. It first gets the process name, reads the ID value before the '@', and if it does
     * not exist, it reads the hash value of the process name.
     *
     * @return The process ID.
     * @throws InternalException if the process name is blank.
     */
    public static int getPid() throws InternalException {
        final String processName = ManagementFactory.getRuntimeMXBean().getName();
        if (StringKit.isBlank(processName)) {
            throw new InternalException("Process name is blank!");
        }
        final int atIndex = processName.indexOf(Symbol.C_AT);
        if (atIndex > 0) {
            return Integer.parseInt(processName.substring(0, atIndex));
        } else {
            return processName.hashCode();
        }
    }

    /**
     * Gets the PID value.
     *
     * @return the pid
     */
    public int get() {
        return this.pid;
    }

}
