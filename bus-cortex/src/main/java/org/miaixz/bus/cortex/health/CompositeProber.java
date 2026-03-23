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

import java.util.List;

import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Status;
import org.miaixz.bus.cortex.Instance;

/**
 * Composite prober that aggregates multiple probers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompositeProber implements Prober {

    /**
     * Delegate probers executed in order.
     */
    private final List<Prober> checkers;

    /**
     * Creates a CompositeProber with the given list of delegate probers.
     *
     * @param checkers ordered list of probers to run
     */
    public CompositeProber(List<Prober> checkers) {
        this.checkers = checkers;
    }

    /**
     * Runs all delegate checkers in order; returns the first failure or a combined success.
     *
     * @param instance instance to probe
     * @return first failing result, or a healthy result with the maximum observed latency
     */
    @Override
    public Status check(Instance instance) {
        long maxLatency = 0L;
        for (Prober prober : checkers) {
            Status result = prober.check(instance);
            if (!result.isHealthy()) {
                return result;
            }
            if (result.getLatencyMs() > maxLatency) {
                maxLatency = result.getLatencyMs();
            }
        }
        return Status.ok(maxLatency);
    }

}
