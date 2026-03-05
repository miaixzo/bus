/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.unix.platform.aix.driver.perfstat;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_id_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_protocol_t;

/**
 * Utility to query performance stats for network interfaces
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PerfstatProtocol {

    private static final Perfstat PERF = Perfstat.INSTANCE;

    /**
     * Queries perfstat_protocol for per-protocol usage statistics
     *
     * @return an array of usage statistics
     */
    public static perfstat_protocol_t[] queryProtocols() {
        perfstat_protocol_t protocol = new perfstat_protocol_t();
        // With null, null, ..., 0, returns total # of elements
        int total = PERF.perfstat_protocol(null, null, protocol.size(), 0);
        if (total > 0) {
            perfstat_protocol_t[] statp = (perfstat_protocol_t[]) protocol.toArray(total);
            perfstat_id_t firstprotocol = new perfstat_id_t(); // name is ""
            int ret = PERF.perfstat_protocol(firstprotocol, statp, protocol.size(), total);
            if (ret > 0) {
                return statp;
            }
        }
        return new perfstat_protocol_t[0];
    }

}
