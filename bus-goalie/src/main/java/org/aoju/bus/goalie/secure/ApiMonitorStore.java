/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.goalie.secure;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.goalie.consts.MetricConsts;
import org.aoju.bus.goalie.manual.ApiParam;
import org.aoju.bus.goalie.manual.ApiSearch;
import org.aoju.bus.goalie.manual.Pagable;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * 存放监控数据
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public class ApiMonitorStore implements MonitorStore {

    private static final String COLUMN_VISITCOUNT = "visitCount";
    private static final String COLUMN_AVGCONSUMEMILLISECONDS = "avgConsumeMilliseconds";
    private static final String COLUMN_MAXCONSUMEMILLISECONDS = "maxConsumeMilliseconds";
    private static final String COLUMN_ERRORCOUNT = "errorCount";

    /**
     * key:"name version"
     */
    private Map<String, MonitorApiInfo> store = new HashMap<>();

    /**
     * 对List进行分页
     *
     * @param list      待分页list
     * @param pageIndex 页索引，从1开始
     * @param pageSize  每页大小，如果为0，返回原list
     * @return 返回结果集
     */
    private static <T extends Pagable> List<T> page(List<T> list, int pageIndex, int pageSize) {
        if (pageSize == 0) {
            return list;
        } else {
            // 起始位置
            int start = ((pageIndex - 1) * pageSize);
            // 总记录数
            int total = list.size();
            // 剩余长度
            int leftLimit = total - start;
            // 偏移量
            int limit = pageSize > leftLimit ? leftLimit : pageSize;

            Pagable[] arr = list.toArray(new Pagable[total]);
            Pagable[] newArr = new Pagable[limit];

            System.arraycopy(arr, start, newArr, 0, limit);

            return (List<T>) Arrays.asList(newArr);
        }
    }

    @Override
    public synchronized void stat(ApiParam param, long starTimeMillis, long endTimeMillis, Object argu, Object result,
                                  Exception e) {
        String key = this.getKey(param.fatchName(), param.fatchVersion());
        MonitorApiInfo monitorApiInfo = store.get(key);
        if (monitorApiInfo == null) {
            monitorApiInfo = new MonitorApiInfo();
            monitorApiInfo.setName(param.fatchName());
            monitorApiInfo.setVersion(param.fatchVersion());
            store.put(key, monitorApiInfo);
        }
        // 访问次数+1
        long visitCount = monitorApiInfo.getVisitCount() + 1;
        // 本次访问耗时
        long consumeMilliseconds = endTimeMillis - starTimeMillis;
        // 总耗时
        long sumConsumeMilliseconds = monitorApiInfo.getSumConsumeMilliseconds() + consumeMilliseconds;
        // 平均耗时=总耗时/访问次数
        BigDecimal avgConsume = new BigDecimal(sumConsumeMilliseconds).divide(new BigDecimal(visitCount), 2,
                BigDecimal.ROUND_UP);
        double avgConsumeMilliseconds = avgConsume.doubleValue();
        // 上一次最大耗时
        long maxConsumeMilliseconds = monitorApiInfo.getMaxConsumeMilliseconds();

        // 出错次数
        int errorCount = monitorApiInfo.getErrorCount();
        if (e != null) {
            this.errorHandler(param, argu, result, e, monitorApiInfo);
            errorCount++;
        }

        monitorApiInfo.setMaxConsumeMilliseconds(Math.max(consumeMilliseconds, maxConsumeMilliseconds));

        monitorApiInfo.setVisitCount(visitCount);
        monitorApiInfo.setSumConsumeMilliseconds(sumConsumeMilliseconds);
        monitorApiInfo.setAvgConsumeMilliseconds(avgConsumeMilliseconds);
        monitorApiInfo.setErrorCount(errorCount);
    }

    @Override
    public int getTotal(ApiSearch apiSearch) {
        String name = apiSearch.getName();
        int total = 0;
        Set<String> keys = store.keySet();
        if (name == null) {
            return keys.size();
        }
        for (String key : keys) {
            if (key.contains(name)) {
                total++;
            }
        }
        return total;
    }

    @Override
    public List<MonitorApiInfo> getList(final ApiSearch apiSearch) {
        String name = apiSearch.getName();
        Collection<Entry<String, MonitorApiInfo>> entrys = store.entrySet();

 /*       if (name != null) {
            entrys = CollUtils.select(entrys, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    Entry<String, MonitorApiInfo> apiInfo = (Entry<String, MonitorApiInfo>) object;
                    return apiInfo.getKey().contains(apiSearch.getName());
                }
            });
        }*/

        List<MonitorApiInfo> retList = new ArrayList<>(entrys.size());

        for (Entry<String, MonitorApiInfo> entry : entrys) {
            retList.add(entry.getValue());
        }

        // 排序
        Collections.sort(retList, (o1, o2) -> {
            MonitorApiInfo monitorApiInfo1 = o1;
            MonitorApiInfo monitorApiInfo2 = o2;
            String sortname = apiSearch.getSort();
            String sortorder = apiSearch.getOrder();
            if (MetricConsts.SORT_DESC.equalsIgnoreCase(sortorder)) {
                monitorApiInfo1 = o2;
                monitorApiInfo2 = o1;
            }
            if (COLUMN_VISITCOUNT.equals(sortname)) {
                return Long.compare(monitorApiInfo1.getVisitCount(), monitorApiInfo2.getVisitCount());
            } else if (COLUMN_AVGCONSUMEMILLISECONDS.equals(sortname)) {
                return Double.compare(monitorApiInfo1.getAvgConsumeMilliseconds(),
                        monitorApiInfo2.getAvgConsumeMilliseconds());
            } else if (COLUMN_MAXCONSUMEMILLISECONDS.equals(sortname)) {
                return Long.compare(monitorApiInfo1.getMaxConsumeMilliseconds(),
                        monitorApiInfo2.getMaxConsumeMilliseconds());
            } else if (COLUMN_ERRORCOUNT.equals(sortname)) {
                return Integer.compare(monitorApiInfo1.getErrorCount(), monitorApiInfo2.getErrorCount());
            } else {
                return monitorApiInfo1.getName().compareTo(monitorApiInfo2.getName());
            }
        });

        // 分页
        int pageIndex = apiSearch.getPage();
        int pageSize = apiSearch.getRows();

        return page(retList, pageIndex, pageSize);
    }

    @Override
    public void clean(String name, String version) {
        if (StringUtils.isEmpty(name)) {
            store.clear();
        } else {
            String key = this.getKey(name, version);
            store.remove(key);
        }
    }

    @Override
    public void errorHandler(ApiParam param, Object argu, Object result, Exception e, MonitorApiInfo t) {
        String errorMsg = this.getErrorMsg(param, e);
        this.setErrorMsg(t, errorMsg);
    }

    private String getKey(String name, String version) {
        if (version == null) {
            version = Normal.EMPTY;
        }
        return ApiParam.buildNameVersion(name, version);
    }

    protected String getErrorMsg(ApiParam param, Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        StringBuilder msg = new StringBuilder();
        String paramStr = param.toJSONString();
        try {
            paramStr = URLDecoder.decode(paramStr, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
        }
        msg.append("请求参数:").append(paramStr).append("\r\n").append("错误信息:").append(writer.toString());
        return msg.toString();
    }

    protected <T extends MonitorApiInfo> void setErrorMsg(T t, String errorMsg) {
        t.getErrors().offer(errorMsg);
    }

}
