/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.tracer.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GcEntity {

    /**
     * 当前应用进程ID
     */
    private Integer id;
    /**
     * 当前应用名称
     */
    private String name;
    /**
     * 时间 格式 MM/dd HH:mm"
     */
    private String date;

    /**
     * Survivor0空间的大小。单位KB。
     */
    private String s0c;
    /**
     * Survivor1空间的大小。单位KB。
     */
    private String s1c;
    /**
     * Survivor0已用空间的大小。单位KB
     */
    private String s0u;
    /**
     * Survivor1已用空间的大小。单位KB。
     */
    private String s1u;
    /**
     * Eden空间的大小。单位KB。
     */
    private String ec;
    /**
     * Eden已用空间的大小。单位KB。
     */
    private String eu;
    /**
     * 老年代空间的大小。单位KB。
     */
    private String oc;
    /**
     * 老年代已用空间的大小。单位KB。
     */
    private String OU;
    /**
     * 元空间的大小（Metaspace）
     */
    private String mc;
    /**
     * 元空间已使用大小（KB）
     */
    private String mu;
    /**
     * 压缩类空间大小（compressed class space）
     */
    private String ccsc;
    /**
     * 压缩类空间已使用大小（KB）
     */
    private String ccsu;
    /**
     * 新生代gc次数
     */
    private String ygc;
    /**
     * 新生代gc耗时（秒）
     */
    private String ygct;
    /**
     * Full gc次数
     */
    private String fgc;
    /**
     * Full gc耗时（秒）
     */
    private String fgct;
    /**
     * gc总耗时（秒）
     */
    private String gct;

}
