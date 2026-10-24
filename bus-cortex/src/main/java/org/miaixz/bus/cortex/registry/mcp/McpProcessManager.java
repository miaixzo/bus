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

import org.miaixz.bus.logger.Logger;

/**
 * Manages stdio MCP sub-processes.
 *
 * @author Kimi Liu
 */
public class McpProcessManager {

    /**
     * Managed subprocesses keyed by MCP process identifier.
     */
    private final Map<String, Process> processes = new ConcurrentHashMap<>();

    /**
     * Creates an empty MCP process manager.
     */
    public McpProcessManager() {
        // No initialization required.
    }

    /**
     * Starts a subprocess when its identifier does not already belong to a running process.
     *
     * @param id      process identifier
     * @param command command and arguments to launch
     * @throws IllegalStateException if a process with the same identifier is already running
     * @throws RuntimeException      if the process cannot be started
     */
    public void start(String id, String[] command) {
        processes.compute(id, (processId, existing) -> {
            if (existing != null && existing.isAlive()) {
                throw new IllegalStateException("Process is already running: " + processId);
            }
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.inheritIO();
                return pb.start();
            } catch (Exception e) {
                Logger.warn(
                        false,
                        "Cortex",
                        e,
                        "Cortex operation failed: provider={}, recoverable={}, exception={}",
                        "McpProcessManager",
                        false,
                        e.getClass().getSimpleName());
                throw new RuntimeException("Failed to start process " + processId, e);
            }
        });
    }

    /**
     * Requests forced termination and removes a subprocess, serialized with starts for the same identifier.
     *
     * @param id process identifier
     */
    public void stop(String id) {
        processes.computeIfPresent(id, (processId, process) -> {
            process.destroyForcibly();
            return null;
        });
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
