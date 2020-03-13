/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.health.common.windows;

import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;

import java.util.EnumMap;
import java.util.Map;

/**
 * <p>
 * PerfCounterQuery class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public class PerfCounterQuery<T extends Enum<T>> {

    /**
     * Constant <code>TOTAL_INSTANCE="_Total"</code>
     */
    public static final String TOTAL_INSTANCE = "_Total";
    /**
     * Constant <code>TOTAL_INSTANCES="*_Total"</code>
     */
    public static final String TOTAL_INSTANCES = "*_Total";
    /**
     * Constant <code>NOT_TOTAL_INSTANCE="^ + TOTAL_INSTANCE"</code>
     */
    public static final String NOT_TOTAL_INSTANCE = Symbol.CARET + TOTAL_INSTANCE;
    /**
     * Constant <code>NOT_TOTAL_INSTANCES="^ + TOTAL_INSTANCES"</code>
     */
    public static final String NOT_TOTAL_INSTANCES = Symbol.CARET + TOTAL_INSTANCES;
    protected final Class<T> propertyEnum;
    protected final String perfObject;
    protected final String perfWmiClass;
    protected final String queryKey;
    protected CounterDataSource source;

    /*
     * Multiple classes use these constants
     */
    protected PerfCounterHandler pdhQueryHandler;
    protected WmiQueryHandler wmiQueryHandler;
    protected WmiQuery<T> counterQuery = null;
    /*
     * Only one will be non-null depending on source
     */
    private EnumMap<T, PerfDataUtils.PerfCounter> counterMap = null;

    /**
     * Construct a new object to hold performance counter data source and results
     *
     * @param propertyEnum An enum which implements
     *                     {@link PerfCounterQuery.PdhCounterProperty}
     *                     and contains the WMI field (Enum value) and PDH Counter string
     *                     (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will
     *                     be refreshed at the same time
     * @param perfWmiClass The WMI PerfData_RawData_* class corresponding to the PDH object
     */
    public PerfCounterQuery(Class<T> propertyEnum, String perfObject, String perfWmiClass) {
        this(propertyEnum, perfObject, perfWmiClass, perfObject);
    }

    /**
     * Construct a new object to hold performance counter data source and results
     *
     * @param propertyEnum An enum which implements
     *                     {@link PerfCounterQuery.PdhCounterProperty}
     *                     and contains the WMI field (Enum value) and PDH Counter string
     *                     (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will
     *                     be refreshed at the same time
     * @param perfWmiClass The WMI PerfData_RawData_* class corresponding to the PDH object
     * @param queryKey     An optional key for PDH counter updates; defaults to the PDH
     *                     object name
     */
    public PerfCounterQuery(Class<T> propertyEnum, String perfObject, String perfWmiClass, String queryKey) {
        this.propertyEnum = propertyEnum;
        this.perfObject = perfObject;
        this.perfWmiClass = perfWmiClass;
        this.queryKey = queryKey;
        this.pdhQueryHandler = PerfCounterHandler.getInstance();
        this.wmiQueryHandler = WmiQueryHandler.createInstance();
        // Start off with PDH as source; if query here fails we will permanently
        // fall back to WMI
        this.source = CounterDataSource.PDH;
    }

    /**
     * Set the Data Source for these counters
     *
     * @param source The source of data
     * @return Whether the data source was successfully set
     */
    public boolean setDataSource(CounterDataSource source) {
        this.source = source;
        switch (source) {
            case PDH:
                Logger.debug("Attempting to set PDH Data Source.");
                unInitWmiCounters();
                return initPdhCounters();
            case WMI:
                Logger.debug("Attempting to set WMI Data Source.");
                unInitPdhCounters();
                initWmiCounters();
                return true;
            default:
                // This should never happen unless you've added a new source and
                // forgot to add a case for it
                throw new IllegalArgumentException("Invalid Data Source specified.");
        }
    }

    /**
     * Initialize PDH counters for this data source. Adds necessary counters to a
     * PDH Query.
     *
     * @return True if the counters were successfully added.
     */
    protected boolean initPdhCounters() {
        this.counterMap = new EnumMap<>(propertyEnum);
        for (T prop : propertyEnum.getEnumConstants()) {
            PerfDataUtils.PerfCounter counter = PerfDataUtils.createCounter(perfObject, ((PdhCounterProperty) prop).getInstance(),
                    ((PdhCounterProperty) prop).getCounter());
            counterMap.put(prop, counter);
            if (!pdhQueryHandler.addCounterToQuery(counter, this.queryKey)) {
                unInitPdhCounters();
                return false;
            }
        }
        return true;
    }

    /**
     * Uninitialize PDH counters for this data source. Removes necessary counters
     * from the PDH Query, releasing their handles.
     */
    protected void unInitPdhCounters() {
        pdhQueryHandler.removeAllCountersFromQuery(this.queryKey);
        this.counterMap = null;
    }

    /**
     * Initialize the WMI query object needed to retrieve counters for this data
     * source.
     */
    protected void initWmiCounters() {
        this.counterQuery = new WmiQuery<>(perfWmiClass, propertyEnum);
    }

    /**
     * Uninitializes the WMI query object needed to retrieve counters for this data
     * source, allowing it to be garbage collected.
     */
    protected void unInitWmiCounters() {
        this.counterQuery = null;
    }

    /**
     * Query the current data source (PDH or WMI) for the Performance Counter values
     * corresponding to the property enum.
     *
     * @return A map of the values by the counter enum.
     */
    public Map<T, Long> queryValues() {
        EnumMap<T, Long> valueMap = new EnumMap<>(propertyEnum);
        T[] props = this.propertyEnum.getEnumConstants();
        if (source.equals(CounterDataSource.PDH)) {
            // Set up the query and counter handles, and query
            if (initPdhCounters() && queryPdh(valueMap, props)) {
                // If both init and query return true, then valueMap contains
                // the results. Release the handles.
                unInitPdhCounters();
            } else {
                // If either init or query failed, switch to WMI
                setDataSource(CounterDataSource.WMI);
            }
        }
        if (source.equals(CounterDataSource.WMI)) {
            queryWmi(valueMap, props);
        }
        return valueMap;
    }

    private boolean queryPdh(Map<T, Long> valueMap, T[] props) {
        if (counterMap != null && 0 < pdhQueryHandler.updateQuery(this.queryKey)) {
            for (T prop : props) {
                valueMap.put(prop, pdhQueryHandler.queryCounter(counterMap.get(prop)));
            }
            return true;
        }
        // Zero timestamp means update failed after muliple
        // attempts; fallback to WMI
        return false;
    }

    private void queryWmi(Map<T, Long> valueMap, T[] props) {
        WmiResult<T> result = wmiQueryHandler.queryWMI(this.counterQuery);
        if (result.getResultCount() > 0) {
            for (T prop : props) {
                switch (result.getCIMType(prop)) {
                    case Wbemcli.CIM_UINT16:
                        valueMap.put(prop, Long.valueOf(WmiUtils.getUint16(result, prop, 0)));
                        break;
                    case Wbemcli.CIM_UINT32:
                        valueMap.put(prop, WmiUtils.getUint32asLong(result, prop, 0));
                        break;
                    case Wbemcli.CIM_UINT64:
                        valueMap.put(prop, WmiUtils.getUint64(result, prop, 0));
                        break;
                    default:
                        throw new ClassCastException("Unimplemented CIM Type Mapping.");
                }
            }
        }
    }

    /**
     * Source of performance counter data.
     */
    public enum CounterDataSource {
        /**
         * Performance Counter data will be pulled from a PDH Counter
         */
        PDH,
        /**
         * Performance Counter data will be pulled from a WMI PerfData_RawData_* table
         */
        WMI
    }

    /**
     * Contract for Counter Property Enums
     */
    public interface PdhCounterProperty {
        /**
         * @return Returns the instance.
         */
        String getInstance();

        /**
         * @return Returns the counter.
         */
        String getCounter();
    }
}
