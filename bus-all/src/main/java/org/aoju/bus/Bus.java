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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus;

import org.aoju.bus.core.Version;
import org.aoju.bus.core.lang.Console;
import org.aoju.bus.core.lang.Scaner;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.Set;

/**
 * <p>
 * Bus (应用/服务总线) 是一个微服务套件、基础框架，它基于Java8编写，参考、借鉴了大量已有
 * 框架、组件的设计，可以作为后端服务的开发基础中间件。代码简洁，架构清晰，非常适合学习使用
 * </p>
 *
 * <p>
 * 目标期望能努力打造一套从 基础框架 - 分布式微服务架构 - 持续集成 - 自动化部署 -系统监测
 * 等，快速实现业务需求的全栈式技术解决方案
 * </p>
 *
 * <p>
 * 欢迎各种形式的贡献，包括但不限于优化，添加功能，文档 代码的改进，问题和 bugs 的报告
 * </p>
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class Bus extends Version {

    /**
     * 显示所有的工具类
     */
    public static Set<Class<?>> getAll() {
        return Scaner.scanPackage("org.aoju",
                (clazz) -> (false == clazz.isInterface()) && StringKit.endWith(clazz.getSimpleName(), "Kit"));
    }

    /**
     * 控制台打印所有工具类
     */
    public static void print() {
        final Set<Class<?>> allUtils = getAll();
        final Console.Table table = Console.Table.create().addHeader("Kit name", "Package");
        for (Class<?> clazz : allUtils) {
            table.addBody(clazz.getSimpleName(), clazz.getPackage().getName());
        }
        table.print();
    }

}
