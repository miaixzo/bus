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

import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Status;

/**
 * Composite prober that aggregates multiple probers.
 *
 * @author Kimi Liu
 */
public class CompositeProber implements Prober {

    /**
     * Delegate probers executed in order.
     */
    private final List<Prober> checkers;

    /**
     * Stores the delegate list by reference; later caller changes affect subsequent probes.
     *
     * @param checkers required ordered list; null delegate elements are skipped by check, but the list itself is not validated or copied
     */
    public CompositeProber(List<Prober> checkers) {
        this.checkers = checkers;
    }

    /**
     * Runs supporting, non-null delegates in order and returns the first unhealthy result.
     * No matching delegates, including an empty list, yields success with zero latency. Delegate exceptions are not caught.
     *
     * @param instance instance passed to each delegate's supports method
     * @return first unhealthy delegate result, otherwise success with the maximum observed latency
     * @throws NullPointerException if the delegate list is null, a delegate returns null, or a delegate requires a non-null instance
     */
    @Override
    public Status check(Instance instance) {
        long maxLatency = 0L;
        for (Prober prober : checkers) {
            if (prober == null || !prober.supports(instance)) {
                continue;
            }
            Status result = prober.check(instance);
            if (!result.isHealthy()) {
                return result;
            }
            if (result.getLatencyMs() > maxLatency) {
                maxLatency = result.getLatencyMs();
            }
        }
        return Status.ok(maxLatency, name());
    }

}
