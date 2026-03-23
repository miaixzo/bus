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
package org.miaixz.bus.cortex.registry.mcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages stdio MCP sub-processes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProcessManager {

    /**
     * Managed subprocesses keyed by MCP process identifier.
     */
    private final Map<String, Process> processes = new ConcurrentHashMap<>();

    /**
     * Starts a new subprocess with the given command.
     *
     * @param id      process identifier
     * @param command command and arguments to launch
     * @throws RuntimeException if the process cannot be started
     */
    public void start(String id, String[] command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            Process process = pb.start();
            processes.put(id, process);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start process " + id, e);
        }
    }

    /**
     * Stops and removes a subprocess by ID.
     *
     * @param id process identifier
     */
    public void stop(String id) {
        Process p = processes.remove(id);
        if (p != null) {
            p.destroyForcibly();
        }
    }

    /**
     * Returns whether the process with the given ID is alive.
     *
     * @param id process identifier
     * @return true if running
     */
    public boolean isAlive(String id) {
        Process p = processes.get(id);
        return p != null && p.isAlive();
    }

}
