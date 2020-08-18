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

import lombok.Data;
import org.aoju.bus.goalie.ApiContext;
import org.aoju.bus.goalie.manual.LimitQueue;
import org.aoju.bus.goalie.manual.Pagable;

import java.io.Serializable;
import java.util.Queue;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
@Data
public class MonitorApiInfo implements Pagable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口名
     */
    private String name;
    /**
     * 版本号
     */
    private String version;
    /**
     * 访问次数
     */
    private long visitCount;
    /**
     * 出错次数
     */
    private int errorCount;
    /**
     * 平均访问耗时
     */
    private double avgConsumeMilliseconds;
    /**
     * 总耗时
     */
    private long sumConsumeMilliseconds;
    /**
     * 最大耗时
     */
    private long maxConsumeMilliseconds;
    /**
     * 出错信息
     */
    private Queue<String> errors = new LimitQueue<>(ApiContext.getConfig().getMonitorErrorQueueSize());

}
