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
package org.miaixz.bus.cortex.health;

import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Status;

/**
 * Local process existence prober using PID.
 *
 * @author Kimi Liu
 */
public class ProcessProber implements Prober {

    /**
     * Creates a process-based health prober.
     */
    public ProcessProber() {
        // No initialization required.
    }

    /**
     * Returns whether this prober can check the supplied instance through its process ID.
     *
     * @param instance candidate instance
     * @return true when instance and PID are non-null; PID positivity is not checked here
     */
    @Override
    public boolean supports(Instance instance) {
        return instance != null && instance.getPid() != null;
    }

    /**
     * Checks whether ProcessHandle.of reports a process for the instance PID; it does not separately call isAlive.
     * Numeric PID validation and lookup behavior belong to the running JVM; this prober does not validate the PID.
     * Unchecked failures from process lookup propagate unchanged.
     *
     * @param instance non-null instance; a null PID produces a failed status
     * @return healthy status when a process handle exists; failed status for an absent PID or handle
     * @throws NullPointerException if instance is null
     */
    @Override
    public Status check(Instance instance) {
        Long pid = instance.getPid();
        if (pid == null) {
            return Status.fail("No PID available for process check", name());
        }
        long start = System.currentTimeMillis();
        boolean alive = ProcessHandle.of(pid).isPresent();
        long latency = System.currentTimeMillis() - start;
        if (alive) {
            return Status.ok(latency, name()).detail("pid", pid.toString());
        }
        return Status.fail("Process " + pid + " is not running", name()).detail("pid", pid.toString());
    }

}
