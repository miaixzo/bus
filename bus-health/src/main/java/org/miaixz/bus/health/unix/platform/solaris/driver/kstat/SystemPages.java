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
package org.miaixz.bus.health.unix.platform.solaris.driver.kstat;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;
import org.miaixz.bus.health.unix.platform.solaris.software.SolarisOperatingSystem;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;

/**
 * Utility to query geom part list
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class SystemPages {

    /**
     * Queries the {@code system_pages} kstat and returns available and physical memory
     *
     * @return A pair with the available and total memory, in pages. Mutiply by page size for bytes.
     */
    public static Pair<Long, Long> queryAvailableTotal() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryAvailableTotal2();
        }
        long memAvailable = 0;
        long memTotal = 0;
        // Get first result
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup(null, -1, "system_pages");
            // Set values
            if (ksp != null && kc.read(ksp)) {
                memAvailable = KstatKit.dataLookupLong(ksp, "availrmem"); // not a typo
                memTotal = KstatKit.dataLookupLong(ksp, "physmem");
            }
        }
        return Pair.of(memAvailable, memTotal);
    }

    private static Pair<Long, Long> queryAvailableTotal2() {
        Object[] results = KstatKit.queryKstat2("kstat:/pages/unix/system_pages", "availrmem", "physmem");
        long avail = results[0] == null ? 0L : (long) results[0];
        long total = results[1] == null ? 0L : (long) results[1];
        return Pair.of(avail, total);
    }

}
