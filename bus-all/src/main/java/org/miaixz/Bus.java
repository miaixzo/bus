/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz;

import org.miaixz.bus.core.Version;

/**
 * <p>
 * Bus (应用/服务总线) 是一个微服务套件、基础框架，它基于Java8编写，参考、借鉴了大量已有 框架、组件的设计，可以作为后端服务的开发基础中间件。代码简洁，架构清晰，非常适合学习使用
 * </p>
 *
 * <p>
 * 目标期望能努力打造一套从 基础框架 - 分布式微服务架构 - 持续集成 - 自动化部署 -系统监测 等，快速实现业务需求的全栈式技术解决方案
 * 同时封装的工具涵盖了字符串、数字、集合、编码、日期、文件、IO、加解密、JSON、HTTP客户端等
 * </p>
 *
 * <p>
 * 欢迎各种形式的贡献，包括但不限于优化，添加功能，文档 代码的改进，问题和 bugs 的报告
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Bus extends Version {

    /**
     * 版本对象，格式：tok+ ( '-' tok+)? ( '+' tok+)?，版本之间使用'.'或'-'分隔，版本号可能包含'+' 数字部分按照大小比较，字符串按照字典顺序比较。
     *
     * <ol>
     * <li>sequence: 主版本号</li>
     * <li>pre: 次版本号</li>
     * <li>build: 构建版本</li>
     * </ol>
     *
     * @param v 版本字符串
     */
    public Bus(String v) {
        super(v);
    }

}
