/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.health.windows;

import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.aoju.bus.core.annotation.NotThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;

import java.util.EnumMap;
import java.util.Map;

/**
 * 封装性能计数器查询的信息
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
@NotThreadSafe
public class PerfCounterQuery<T extends Enum<T>> {

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
    /*
     * 设置实例化
     */
    protected final Class<T> propertyEnum;
    protected final String perfObject;
    protected final String perfWmiClass;
    protected final String queryKey;

    /*
     * 多个类使用这些常量
     */
    protected CounterDataSource source;
    protected PerfCounterQueryHandler pdhQueryHandler;
    protected com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery counterQuery = null;
    /*
     * 枚举源，只有一个是非空的
     */
    private EnumMap<T, PerfDataCounter.PerfCounter> counterMap = null;

    /**
     * 构造一个新对象来保存性能计数器数据源和结果
     *
     * @param propertyEnum 实现{@link PerfCounterQuery} 的枚举
     *                     包含WMI字段(Enum value)和PDH计数器字符串(实例和计数器)
     * @param perfObject   此计数器的PDH对象;此对象上的所有计数器将同时刷新
     * @param perfWmiClass 对应于PDH对象的WMI PerfData_RawData_*类
     */
    public PerfCounterQuery(Class<T> propertyEnum, String perfObject, String perfWmiClass) {
        this(propertyEnum, perfObject, perfWmiClass, perfObject);
    }

    /**
     * 构造一个新对象来保存性能计数器数据源和结果
     *
     * @param propertyEnum 实现{@link PerfCounterQuery} 的枚举
     *                     包含WMI字段(Enum value)和PDH计数器字符串(实例和计数器)
     * @param perfObject   此计数器的PDH对象;此对象上的所有计数器将同时刷新
     * @param perfWmiClass 对应于PDH对象的WMI PerfData_RawData_*类
     * @param queryKey     用于PDH计数器更新的可选密钥;默认为PDH对象名称
     */
    public PerfCounterQuery(Class<T> propertyEnum, String perfObject, String perfWmiClass, String queryKey) {
        this.propertyEnum = propertyEnum;
        this.perfObject = perfObject;
        this.perfWmiClass = perfWmiClass;
        this.queryKey = queryKey;
        this.pdhQueryHandler = new PerfCounterQueryHandler();
        this.source = CounterDataSource.PDH;
    }

    /**
     * 设置这些计数器的数据源
     *
     * @param source 数据来源
     * @return 数据源是否设置成功
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
                throw new IllegalArgumentException("Invalid Data Source specified.");
        }
    }

    /**
     * 初始化此数据源的PDH计数器。向PDH查询添加必要的计数器
     *
     * @return 如果计数器被成功添加，则为真
     */
    protected boolean initPdhCounters() {
        this.counterMap = new EnumMap<>(propertyEnum);
        for (T prop : propertyEnum.getEnumConstants()) {
            PerfDataCounter.PerfCounter counter = PerfDataCounter.createCounter(perfObject, ((PdhCounterProperty) prop).getInstance(),
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
     * 不初始化此数据源的PDH计数器。从PDH查询中删除必要的计数器，释放它们的句柄
     */
    protected void unInitPdhCounters() {
        pdhQueryHandler.removeAllCountersFromQuery(this.queryKey);
        this.counterMap = null;
    }

    /**
     * 初始化检索此数据源的计数器所需的WMI查询对象
     */
    protected void initWmiCounters() {
        this.counterQuery = new com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery(perfWmiClass, propertyEnum);
    }

    /**
     * 未初始化检索此数据源的计数器所需的WMI查询对象，从而允许对其进行垃圾收集
     */
    protected void unInitWmiCounters() {
        this.counterQuery = null;
    }

    /**
     * 查询当前数据源(PDH或WMI)，以获得与属性enum对应的性能计数器值
     *
     * @return 计数器枚举值的映射
     */
    public Map<T, Long> queryValues() {
        EnumMap<T, Long> valueMap = new EnumMap<>(propertyEnum);
        T[] props = this.propertyEnum.getEnumConstants();
        if (source.equals(CounterDataSource.PDH)) {
            // 设置查询和计数器句柄以及查询
            if (initPdhCounters() && queryPdh(valueMap, props)) {
                // 如果init和查询都返回true，则valueMap包含结果。释放句柄
                unInitPdhCounters();
            } else {
                // 如果init或查询失败，则切换到WMI
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
        // 0时间戳表示多次尝试后更新失败;回退到WMI
        return false;
    }

    private void queryWmi(Map<T, Long> valueMap, T[] props) {
        WmiResult<T> result = WmiQueryHandler.createInstance().queryWMI(this.counterQuery);
        if (result.getResultCount() > 0) {
            for (T prop : props) {
                switch (result.getCIMType(prop)) {
                    case Wbemcli.CIM_UINT16:
                        valueMap.put(prop, Long.valueOf(WmiQuery.getUint16(result, prop, 0)));
                        break;
                    case Wbemcli.CIM_UINT32:
                        valueMap.put(prop, WmiQuery.getUint32asLong(result, prop, 0));
                        break;
                    case Wbemcli.CIM_UINT64:
                        valueMap.put(prop, WmiQuery.getUint64(result, prop, 0));
                        break;
                    default:
                        throw new ClassCastException("Unimplemented CIM Type Mapping.");
                }
            }
        }
    }

    /**
     * 性能计数器数据的来源
     */
    public enum CounterDataSource {
        /**
         * 性能计数器数据将从PDH计数器中提取
         */
        PDH,
        /**
         * 性能计数器数据将从WMI PerfData_RawData_*表中提取
         */
        WMI
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
