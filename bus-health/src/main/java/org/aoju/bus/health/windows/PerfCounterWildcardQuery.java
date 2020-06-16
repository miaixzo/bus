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
package org.aoju.bus.health.windows;

import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.PdhUtil;
import com.sun.jna.platform.win32.PdhUtil.PdhEnumObjectItems;
import com.sun.jna.platform.win32.PdhUtil.PdhException;
import com.sun.jna.platform.win32.Win32Exception;
import org.aoju.bus.core.annotation.NotThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;

import java.util.*;

/**
 * 封装性能计数器查询的信息
 *
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
@NotThreadSafe
public class PerfCounterWildcardQuery<T extends Enum<T>> extends PerfCounterQuery<T> {

    private final String perfObjectLocalized;
    private final String instanceFilter;
    private EnumMap<T, List<PerfDataCounter.PerfCounter>> counterListMap = null;
    private List<String> instancesFromLastQuery = new ArrayList<>();

    /**
     * 构造一个新对象来保存性能计数器数据源和结果
     *
     * @param propertyEnum 实现{@link PerfCounterWildcardQuery.PdhCounterWildcardProperty} 的枚举。包含WMI字段(Enum值)和
     *                     PDH计数器字符串(实例或计数器)
     * @param perfObject   此计数器的PDH对象;此对象上的所有计数器将同时刷新
     * @param perfWmiClass 对应于PDH对象的WMI PerfData_RawData_*类
     */
    public PerfCounterWildcardQuery(Class<T> propertyEnum, String perfObject, String perfWmiClass) {
        this(propertyEnum, perfObject, perfWmiClass, perfObject);
    }

    /**
     * 构造一个新对象来保存性能计数器数据源和结果
     *
     * @param propertyEnum 实现{@link PerfCounterWildcardQuery.PdhCounterWildcardProperty} 的枚举
     *                     包含WMI字段(Enum值)和PDH计数器字符串(实例或计数器)
     * @param perfObject   此计数器的PDH对象;此对象上的所有计数器将同时刷新
     * @param perfWmiClass 对应于PDH对象的WMI PerfData_RawData_*类
     * @param queryKey     用于PDH计数器更新的可选密钥;默认为PDH对象名称
     */
    public PerfCounterWildcardQuery(Class<T> propertyEnum, String perfObject, String perfWmiClass, String queryKey) {
        super(propertyEnum, perfObject, perfWmiClass, queryKey);

        if (propertyEnum.getEnumConstants().length < 2) {
            throw new IllegalArgumentException("Enum " + propertyEnum.getName()
                    + " must have at least two elements, an instance filter and a counter.");
        }
        this.instanceFilter = ((PdhCounterWildcardProperty) propertyEnum.getEnumConstants()[0]).getCounter()
                .toLowerCase();
        this.perfObjectLocalized = localize(this.perfObject);
    }

    /**
     * 本地化一个PerfCounter字符串。英文计数器名通常应该在HKEY_LOCAL_MACHINE\SOFTWARE\
     * Microsoft\Windows NT\CurrentVersion\Perflib\009\ counter中，但是语言操作可能
     * 会删除009索引。在这种情况下，我们可以假设英语必须是语言并继续。如果这个假设是错误的
     * 我们可能仍然无法匹配名称，但这总比没有好
     *
     * @param perfObject 要本地化的字符串
     * @return 如果本地化成功，则为本地化后的字符串，否则为原始字符串
     */
    private static String localize(String perfObject) {
        String localized = null;
        try {
            localized = PdhUtil.PdhLookupPerfNameByIndex(null, PdhUtil.PdhLookupPerfIndexByEnglishName(perfObject));
        } catch (Win32Exception e) {
            Logger.error(
                    "Unable to locate English counter names in registry Perflib 009. Assuming English counters. Error {}. {}",
                    String.format("0x%x", e.getHR().intValue()),
                    "See https://support.microsoft.com/en-us/help/300956/how-to-manually-rebuild-performance-counter-library-values");
        } catch (PdhException e) {
            Logger.error("Unable to localize {} performance counter.  Error {}.", perfObject,
                    String.format("0x%x", e.getErrorCode()));
        }
        if (localized == null || localized.length() == 0) {
            return perfObject;
        }
        Logger.debug("Localized {} to {}", perfObject, localized);
        return localized;
    }

    /**
     * Tests if a String matches another String with a wildcard pattern.
     *
     * @param text    The String to test
     * @param pattern The String containing a wildcard pattern where ? represents a
     *                single character and * represents any number of characters. If the
     *                first character of the pattern is a carat (^) the test is
     *                performed against the remaining characters and the result of the
     *                test is the opposite.
     * @return True if the String matches or if the first character is ^ and the
     * remainder of the String does not match.
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == Symbol.C_CARET) {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace(Symbol.STAR, ".*?"));
    }

    @Override
    protected boolean initPdhCounters() {
        return fillCounterListMap();
    }

    @Override
    protected void unInitPdhCounters() {
        pdhQueryHandler.removeAllCountersFromQuery(this.queryKey);
        this.counterListMap = null;
    }

    @Override
    public Map<T, Long> queryValues() {
        throw new UnsupportedOperationException("Use queryValuesWildcard() on this class.");
    }

    /**
     * 查询当前数据源(PDH或WMI)，以获得与属性enum对应的性能计数器值
     *
     * @return 计数器枚举值的映射
     */
    public Map<T, List<Long>> queryValuesWildcard() {
        EnumMap<T, List<Long>> valueMap = new EnumMap<>(propertyEnum);
        this.instancesFromLastQuery.clear();
        T[] props = this.propertyEnum.getEnumConstants();
        if (source.equals(CounterDataSource.PDH)) {
            // 设置查询和计数器句柄以及查询
            if (initPdhCounters() && queryPdhWildcard(valueMap, props)) {
                // 如果init和查询都返回true，则valueMap包含结果。释放处理
                unInitPdhCounters();
            } else {
                // 如果init或查询失败，则切换到WMI
                setDataSource(CounterDataSource.WMI);
            }
        }
        if (source.equals(CounterDataSource.WMI)) {
            queryWmiWildcard(valueMap, props);
        }
        return valueMap;
    }

    private boolean queryPdhWildcard(Map<T, List<Long>> valueMap, T[] props) {
        if (this.counterListMap != null && 0 < pdhQueryHandler.updateQuery(this.queryKey)) {
            for (int i = 1; i < props.length; i++) {
                T prop = props[i];
                List<Long> values = new ArrayList<>();
                for (PerfDataCounter.PerfCounter counter : counterListMap.get(prop)) {
                    values.add(pdhQueryHandler.queryCounter(counter));
                    if (i == 1) {
                        instancesFromLastQuery.add(counter.getInstance());
                    }
                }
                valueMap.put(prop, values);
            }
            return true;
        }
        // 0时间戳表示多次尝试后更新失败;回到WMI
        return false;
    }

    private void queryWmiWildcard(Map<T, List<Long>> valueMap, T[] props) {
        WmiResult<T> result = WmiQueryHandler.createInstance().queryWMI(this.counterQuery);
        if (result.getResultCount() > 0) {
            // 第一个元素是实例名
            for (int i = 0; i < result.getResultCount(); i++) {
                instancesFromLastQuery.add(WmiQuery.getString(result, props[0], i));
            }
            // 其余元素是计数器
            for (int p = 1; p < props.length; p++) {
                T prop = props[p];
                List<Long> values = new ArrayList<>();
                for (int i = 0; i < result.getResultCount(); i++) {
                    switch (result.getCIMType(prop)) {
                        case Wbemcli.CIM_UINT16:
                            values.add(Long.valueOf(WmiQuery.getUint16(result, prop, i)));
                            break;
                        case Wbemcli.CIM_UINT32:
                            values.add(WmiQuery.getUint32asLong(result, prop, i));
                            break;
                        case Wbemcli.CIM_UINT64:
                            values.add(WmiQuery.getUint64(result, prop, i));
                            break;
                        case Wbemcli.CIM_DATETIME:
                            values.add(WmiQuery.getDateTime(result, prop, i).toInstant().toEpochMilli());
                            break;
                        default:
                            throw new ClassCastException("Unimplemented CIM Type Mapping.");
                    }
                }
                valueMap.put(prop, values);
            }
        }
    }

    /**
     * 列出与值映射列表对应的实例
     *
     * @return 它们在值映射查询中返回的顺序的列表
     */
    public List<String> getInstancesFromLastQuery() {
        return this.instancesFromLastQuery;
    }

    private boolean fillCounterListMap() {
        // 获取实例列表
        final PdhEnumObjectItems objectItems;
        try {
            objectItems = PdhUtil.PdhEnumObjectItems(null, null, perfObjectLocalized, 100);
        } catch (PdhException e) {
            return false;
        }
        List<String> instances = objectItems.getInstances();
        // 过滤掉不匹配的实例
        instances.removeIf(i -> !wildcardMatch(i.toLowerCase(), this.instanceFilter));
        // 跟踪不在计数器列表中的实例，以便添加
        Set<String> instancesToAdd = new HashSet<>(instances);
        // 用要添加的实例填充映射。跳过第一个计数器，它定义了实例过滤器
        this.counterListMap = new EnumMap<>(propertyEnum);
        for (int i = 1; i < propertyEnum.getEnumConstants().length; i++) {
            T prop = propertyEnum.getEnumConstants()[i];
            List<PerfDataCounter.PerfCounter> counterList = new ArrayList<>(instances.size());
            for (String instance : instancesToAdd) {
                PerfDataCounter.PerfCounter counter = PerfDataCounter.createCounter(perfObject, instance,
                        ((PdhCounterWildcardProperty) prop).getCounter());
                if (!pdhQueryHandler.addCounterToQuery(counter, this.queryKey)) {
                    unInitPdhCounters();
                    return false;
                }
                counterList.add(counter);
            }
            this.counterListMap.put(prop, counterList);
        }
        return this.counterListMap.size() > 0;
    }

    /**
     * 枚举属性计数器
     */
    public interface PdhCounterWildcardProperty {

        /**
         * @return 返回计数器枚举的第一个元素将返回实例过滤器，而不是计数器
         */
        String getCounter();

    }

}
