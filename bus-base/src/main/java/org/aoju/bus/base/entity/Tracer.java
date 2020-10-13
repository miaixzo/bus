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
package org.aoju.bus.base.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Transient;

/**
 * 访问链路跟踪
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Tracer extends OAuth2 {

    /**
     * 当前主链ID
     */
    @Transient
    protected String x_trace_id;

    /**
     * 调用者ID
     */
    @Transient
    protected String x_span_id;

    /**
     * 被调用者ID
     */
    @Transient
    protected String x_child_Id;

    /**
     * 本地IP
     */
    @Transient
    protected String x_local_ip;

    /**
     * 远程IP
     */
    @Transient
    protected String x_remote_ip;

    /**
     * 渠道来源
     */
    @Transient
    protected String x_remote_channel;

    /**
     * 终端设备
     */
    @Transient
    protected String x_remote_terminal;

    /**
     * UA：浏览器信息(类型，版本号)
     */
    @Transient
    protected String x_remote_browser;

}
