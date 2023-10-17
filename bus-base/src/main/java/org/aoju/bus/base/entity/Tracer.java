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
package org.aoju.bus.base.entity;

import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访问链路跟踪
 *
 * @author Kimi Liu
 * @since Java 17+
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
    protected String x_child_id;

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
     * 请求者渠道类型: 1-WEB, 2-APP, 3-钉钉，4-微信小程序，5-其他；
     */
    @Transient
    protected String x_remote_channel;

    /**
     * 请求者终端类型: 1-PC, 2-Android, 3-iPhone, 4-iPad, 5-WinPhone, 6-HarmonyOS，7-其他
     */
    @Transient
    protected String x_remote_terminal;

    /**
     * 请求者浏览器信息: APP 原生则传系统版本
     */
    @Transient
    protected String x_remote_browser;

}
