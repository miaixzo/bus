/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows;

import java.util.Locale;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseableLONGLONGByReference;
import org.miaixz.bus.health.builtin.jna.Struct.CloseablePdhRawCounter;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.Pdh;
import com.sun.jna.platform.win32.PdhMsg;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;

/**
 * Helper class to centralize the boilerplate portions of PDH counter setup and allow applications to easily add, query,
 * and remove counters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PerfDataKit {

    private static final DWORD_PTR PZERO = new DWORD_PTR(0);
    private static final DWORDByReference PDH_FMT_RAW = new DWORDByReference(new DWORD(Pdh.PDH_FMT_RAW));
    private static final Pdh PDH = Pdh.INSTANCE;

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    /**
     * Update a query and get the timestamp
     *
     * @param query The query to update all counters in
     * @return The update timestamp of the first counter in the query
     */
    public static long updateQueryTimestamp(HANDLEByReference query) {
        try (CloseableLONGLONGByReference pllTimeStamp = new CloseableLONGLONGByReference()) {
            int ret = IS_VISTA_OR_GREATER ? PDH.PdhCollectQueryDataWithTime(query.getValue(), pllTimeStamp)
                    : PDH.PdhCollectQueryData(query.getValue());
            // Due to race condition, initial update may fail with PDH_NO_DATA.
            int retries = 0;
            while (ret == PdhMsg.PDH_NO_DATA && retries++ < 3) {
                // Exponential fallback.
                Builder.sleep(1L << retries);
                ret = IS_VISTA_OR_GREATER ? PDH.PdhCollectQueryDataWithTime(query.getValue(), pllTimeStamp)
                        : PDH.PdhCollectQueryData(query.getValue());
            }
            if (ret != WinError.ERROR_SUCCESS) {
                if (Logger.isWarnEnabled()) {
                    Logger.warn("Failed to update counter. Error code: {}",
                            String.format(Locale.ROOT, Formats.formatError(ret)));
                }
                return 0L;
            }
            // Perf Counter timestamp is in local time
            return IS_VISTA_OR_GREATER ? Parsing.filetimeToUtcMs(pllTimeStamp.getValue().longValue(), true)
                    : System.currentTimeMillis();
        }
    }

    /**
     * Create a Performance Counter
     *
     * @param object   The object/path for the counter
     * @param instance The instance of the counter, or null if no instance
     * @param counter  The counter name
     * @return A PerfCounter object encapsulating the object, instance, and counter
     */
    public static PerfCounter createCounter(String object, String instance, String counter) {
        return new PerfCounter(object, instance, counter);
    }

    /**
     * Open a pdh query
     *
     * @param q pointer to the query
     * @return true if successful
     */
    public static boolean openQuery(HANDLEByReference q) {
        int ret = PDH.PdhOpenQuery(null, PZERO, q);
        if (ret != WinError.ERROR_SUCCESS) {
            if (Logger.isErrorEnabled()) {
                Logger.error("Failed to open PDH Query. Error code: {}",
                        String.format(Locale.ROOT, Formats.formatError(ret)));
            }
            return false;
        }
        return true;
    }

    /**
     * Get value of pdh counter
     *
     * @param counter The counter to get the value of
     * @return long value of the counter, or negative value representing an error code
     */
    public static long queryCounter(HANDLEByReference counter) {
        try (CloseablePdhRawCounter counterValue = new CloseablePdhRawCounter()) {
            int ret = PDH.PdhGetRawCounterValue(counter.getValue(), PDH_FMT_RAW, counterValue);
            if (ret != WinError.ERROR_SUCCESS) {
                if (Logger.isWarnEnabled()) {
                    Logger.warn("Failed to get counter. Error code: {}",
                            String.format(Locale.ROOT, Formats.formatError(ret)));
                }
                return ret;
            }
            return counterValue.FirstValue;
        }
    }

    /**
     * Close a pdh query
     *
     * @param q pointer to the query
     * @return true if successful
     */
    public static boolean closeQuery(HANDLEByReference q) {
        return WinError.ERROR_SUCCESS == PDH.PdhCloseQuery(q.getValue());
    }

    /**
     * Get value of pdh counter's second value (base counters)
     *
     * @param counter The counter to get the value of
     * @return long value of the counter's second value, or negative value representing an error code
     */
    public static long querySecondCounter(HANDLEByReference counter) {
        try (CloseablePdhRawCounter counterValue = new CloseablePdhRawCounter()) {
            int ret = PDH.PdhGetRawCounterValue(counter.getValue(), PDH_FMT_RAW, counterValue);
            if (ret != WinError.ERROR_SUCCESS) {
                if (Logger.isWarnEnabled()) {
                    Logger.warn("Failed to get counter. Error code: {}",
                            String.format(Locale.ROOT, Formats.formatError(ret)));
                }
                return ret;
            }
            return counterValue.SecondValue;
        }
    }

    /**
     * Adds a pdh counter to a query
     *
     * @param query Pointer to the query to add the counter
     * @param path  String name of the PerfMon counter. For Vista+, must be in English. Must localize this path for
     *              pre-Vista.
     * @param p     Pointer to the counter
     * @return true if successful
     */
    public static boolean addCounter(HANDLEByReference query, String path, HANDLEByReference p) {
        int ret = IS_VISTA_OR_GREATER ? PDH.PdhAddEnglishCounter(query.getValue(), path, PZERO, p)
                : PDH.PdhAddCounter(query.getValue(), path, PZERO, p);
        if (ret != WinError.ERROR_SUCCESS) {
            if (Logger.isWarnEnabled()) {
                Logger.warn("Failed to add PDH Counter: {}, Error code: {}", path,
                        String.format(Locale.ROOT, Formats.formatError(ret)));
            }
            return false;
        }
        return true;
    }

    /**
     * Remove a pdh counter
     *
     * @param p pointer to the counter
     * @return true if successful
     */
    public static boolean removeCounter(HANDLEByReference p) {
        return WinError.ERROR_SUCCESS == PDH.PdhRemoveCounter(p.getValue());
    }

    /**
     * Encapsulates the three string components of a performance counter
     */
    @Immutable
    public static class PerfCounter {
        private final String object;
        private final String instance;
        private final String counter;
        private final boolean baseCounter;

        public PerfCounter(String objectName, String instanceName, String counterName) {
            this.object = objectName;
            this.instance = instanceName;
            int baseIdx = counterName.indexOf("_Base");
            if (baseIdx > 0) {
                this.counter = counterName.substring(0, baseIdx);
                this.baseCounter = true;
            } else {
                this.counter = counterName;
                this.baseCounter = false;
            }
        }

        /**
         * @return Returns the object.
         */
        public String getObject() {
            return object;
        }

        /**
         * @return Returns the instance.
         */
        public String getInstance() {
            return instance;
        }

        /**
         * @return Returns the counter.
         */
        public String getCounter() {
            return counter;
        }

        /**
         * @return Returns whether the counter is a base counter
         */
        public boolean isBaseCounter() {
            return baseCounter;
        }

        /**
         * Returns the path for this counter
         *
         * @return A string representing the counter path
         */
        public String getCounterPath() {
            StringBuilder sb = new StringBuilder();
            sb.append('\\').append(object);
            if (instance != null) {
                sb.append(Symbol.C_PARENTHESE_LEFT).append(instance).append(Symbol.C_PARENTHESE_RIGHT);
            }
            sb.append('\\').append(counter);
            return sb.toString();
        }
    }

}
