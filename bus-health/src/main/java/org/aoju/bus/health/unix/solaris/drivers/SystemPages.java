/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.unix.solaris.drivers;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.unix.solaris.KstatCtl;
import org.aoju.bus.health.unix.solaris.KstatCtl.KstatChain;

/**
 * Utility to query geom part list
 *
 * @author Kimi Liu
 * @version 5.9.8
 * @since JDK 1.8+
 */
@ThreadSafe
public final class SystemPages {

    private SystemPages() {
    }

    /**
     * Queries the {@code system_pages} kstat and returns available and physical
     * memory
     *
     * @return A pair with the available and total memory, in pages. Mutiply by page
     * size for bytes.
     */
    public static Pair<Long, Long> queryAvailableTotal() {
        long memAvailable = 0;
        long memTotal = 0;
        // Get first result
        try (KstatChain kc = KstatCtl.openChain()) {
            Kstat ksp = kc.lookup(null, -1, "system_pages");
            // Set values
            if (ksp != null && kc.read(ksp)) {
                memAvailable = KstatCtl.dataLookupLong(ksp, "availrmem"); // not a typo
                memTotal = KstatCtl.dataLookupLong(ksp, "physmem");
            }
        }
        return Pair.of(memAvailable, memTotal);
    }

}
