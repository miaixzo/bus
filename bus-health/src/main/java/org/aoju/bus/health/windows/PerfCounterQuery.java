/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.windows;

import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.PdhUtil;
import com.sun.jna.platform.win32.PdhUtil.PdhException;
import com.sun.jna.platform.win32.Win32Exception;
import org.aoju.bus.core.annotation.GuardeBy;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 封装性能计数器查询的信息
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
@ThreadSafe
public final class PerfCounterQuery {

    /**
     * 常量 <code>TOTAL_INSTANCE="_Total"</code>
     */
    public static final String TOTAL_INSTANCE = "_Total";
    /**
     * 常量 <code>TOTAL_INSTANCES="*_Total"</code>
     */
    public static final String TOTAL_INSTANCES = "*_Total";
    /**
     * 常量 <code>NOT_TOTAL_INSTANCE="^ + TOTAL_INSTANCE"</code>
     */
    public static final String NOT_TOTAL_INSTANCE = Symbol.CARET + TOTAL_INSTANCE;
    /**
     * 常量 <code>NOT_TOTAL_INSTANCES="^ + TOTAL_INSTANCES"</code>
     */
    public static final String NOT_TOTAL_INSTANCES = Symbol.CARET + TOTAL_INSTANCES;

    // Use a map to cache failed pdh queries
    @GuardeBy("failedQueryCacheLock")
    private static final Set<String> failedQueryCache = new HashSet<>();
    private static final ReentrantLock failedQueryCacheLock = new ReentrantLock();
    // Use a map to cache localization strings
    private static final ConcurrentHashMap<String, String> localizeCache = new ConcurrentHashMap<>();

    private PerfCounterQuery() {

    }

    /**
     * Query the a Performance Counter using PDH, with WMI backup on failure, for
     * values corresponding to the property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements
     *                     {@link PerfCounterQuery.PdhCounterProperty}
     *                     and contains the WMI field (Enum value) and PDH Counter string
     *                     (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will
     *                     be refreshed at the same time
     * @param perfWmiClass The WMI PerfData_RawData_* class corresponding to the PDH object
     * @return An {@link EnumMap} of the values indexed by {@code propertyEnum} on
     * success, or an empty map if both PDH and WMI queries failed.
     */
    public static <T extends Enum<T>> Map<T, Long> queryValues(Class<T> propertyEnum, String perfObject,
                                                               String perfWmiClass) {
        // Check without locking for performance
        if (!failedQueryCache.contains(perfObject)) {
            failedQueryCacheLock.lock();
            try {
                // Double check lock
                if (!failedQueryCache.contains(perfObject)) {
                    Map<T, Long> valueMap = queryValuesFromPDH(propertyEnum, perfObject);
                    if (!valueMap.isEmpty()) {
                        return valueMap;
                    }
                    // If we are here, query failed
                    Logger.warn("Disabling further attempts to query {}.", perfObject);
                    failedQueryCache.add(perfObject);
                }
            } finally {
                failedQueryCacheLock.unlock();
            }
        }
        return queryValuesFromWMI(propertyEnum, perfWmiClass);
    }

    /**
     * Query the a Performance Counter using PDH for values corresponding to the
     * property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements
     *                     {@link PerfCounterQuery.PdhCounterProperty}
     *                     and contains the WMI field (Enum value) and PDH Counter string
     *                     (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will
     *                     be refreshed at the same time
     * @return An {@link EnumMap} of the values indexed by {@code propertyEnum} on
     * success, or an empty map if the PDH query failed.
     */
    public static <T extends Enum<T>> Map<T, Long> queryValuesFromPDH(Class<T> propertyEnum, String perfObject) {
        T[] props = propertyEnum.getEnumConstants();
        String perfObjectLocalized = localize(perfObject);
        EnumMap<T, PerfDataKit.PerfCounter> counterMap = new EnumMap<>(propertyEnum);
        EnumMap<T, Long> valueMap = new EnumMap<>(propertyEnum);
        try (PerfCounterQueryHandler pdhQueryHandler = new PerfCounterQueryHandler()) {
            // Set up the query and counter handles
            for (T prop : props) {
                PerfDataKit.PerfCounter counter = PerfDataKit.createCounter(perfObjectLocalized,
                        ((PdhCounterProperty) prop).getInstance(), ((PdhCounterProperty) prop).getCounter());
                counterMap.put(prop, counter);
                if (!pdhQueryHandler.addCounterToQuery(counter)) {
                    return valueMap;
                }
            }
            // And then query. Zero timestamp means update failed
            if (0 < pdhQueryHandler.updateQuery()) {
                for (T prop : props) {
                    valueMap.put(prop, pdhQueryHandler.queryCounter(counterMap.get(prop)));
                }
            }
        }
        return valueMap;
    }

    /**
     * Query the a Performance Counter using WMI for values corresponding to the
     * property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements
     *                     {@link PerfCounterQuery.PdhCounterProperty}
     *                     and contains the WMI field (Enum value) and PDH Counter string
     *                     (instance and counter)
     * @param wmiClass     The WMI PerfData_RawData_* class corresponding to the PDH object
     * @return An {@link EnumMap} of the values indexed by {@code propertyEnum} if
     * successful, an empty map if the WMI query failed.
     */
    public static <T extends Enum<T>> Map<T, Long> queryValuesFromWMI(Class<T> propertyEnum, String wmiClass) {
        WmiQuery<T> query = new WmiQuery<>(wmiClass, propertyEnum);
        WmiResult<T> result = WmiQueryHandler.createInstance().queryWMI(query);
        EnumMap<T, Long> valueMap = new EnumMap<>(propertyEnum);
        if (result.getResultCount() > 0) {
            for (T prop : propertyEnum.getEnumConstants()) {
                switch (result.getCIMType(prop)) {
                    case Wbemcli.CIM_UINT16:
                        valueMap.put(prop, Long.valueOf(WmiKit.getUint16(result, prop, 0)));
                        break;
                    case Wbemcli.CIM_UINT32:
                        valueMap.put(prop, WmiKit.getUint32asLong(result, prop, 0));
                        break;
                    case Wbemcli.CIM_UINT64:
                        valueMap.put(prop, WmiKit.getUint64(result, prop, 0));
                        break;
                    case Wbemcli.CIM_DATETIME:
                        valueMap.put(prop, WmiKit.getDateTime(result, prop, 0).toInstant().toEpochMilli());
                        break;
                    default:
                        throw new ClassCastException("Unimplemented CIM Type Mapping.");
                }
            }
        }
        return valueMap;
    }

    /**
     * Localize a PerfCounter string. English counter names should normally be in
     * {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows
     * NT\CurrentVersion\Perflib\009\Counter}, but language manipulations may delete
     * the {@code 009} index. In this case we can assume English must be the
     * language and continue. We may still fail to match the name if the assumption
     * is wrong but it's better than nothing.
     *
     * @param perfObject A String to localize
     * @return The localized string if localization successful, or the original
     * string otherwise.
     */
    public static String localize(String perfObject) {
        return localizeCache.computeIfAbsent(perfObject, k -> localizeUsingPerfIndex(k));
    }

    private static String localizeUsingPerfIndex(String perfObject) {
        String localized = perfObject;
        try {
            localized = PdhUtil.PdhLookupPerfNameByIndex(null, PdhUtil.PdhLookupPerfIndexByEnglishName(perfObject));
        } catch (Win32Exception e) {
            Logger.warn(
                    "Unable to locate English counter names in registry Perflib 009. Assuming English counters. Error {}. {}",
                    String.format("0x%x", e.getHR().intValue()),
                    "See https://support.microsoft.com/en-us/help/300956/how-to-manually-rebuild-performance-counter-library-values");
        } catch (PdhException e) {
            Logger.warn("Unable to localize {} performance counter.  Error {}.", perfObject,
                    String.format("0x%x", e.getErrorCode()));
        }
        if (localized.isEmpty()) {
            return perfObject;
        }
        Logger.debug("Localized {} to {}", perfObject, localized);
        return localized;
    }

    /**
     * 属性枚举计数器
     */
    public interface PdhCounterProperty {
        /**
         * @return 返回的实例
         */
        String getInstance();

        /**
         * @return 返回计数器
         */
        String getCounter();
    }

}
