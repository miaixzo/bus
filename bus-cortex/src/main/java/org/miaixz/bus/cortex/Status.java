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
package org.miaixz.bus.cortex;

import lombok.Getter;
import lombok.Setter;

/**
 * Result of a health check probe.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Status {

    /**
     * Whether the instance is considered healthy.
     */
    private boolean healthy;
    /**
     * Probe round-trip latency in milliseconds.
     */
    private long latencyMs;
    /**
     * Human-readable status or error description.
     */
    private String message;

    /**
     * Creates a new Status.
     */
    private Status() {

    }

    /**
     * Creates a successful health result.
     *
     * @param latencyMs probe latency in milliseconds
     * @return healthy result
     */
    public static Status ok(long latencyMs) {
        Status r = new Status();
        r.healthy = true;
        r.latencyMs = latencyMs;
        r.message = "OK";
        return r;
    }

    /**
     * Creates a failed health result.
     *
     * @param message failure description
     * @return unhealthy result
     */
    public static Status fail(String message) {
        Status r = new Status();
        r.healthy = false;
        r.latencyMs = 0L;
        r.message = message;
        return r;
    }

}
