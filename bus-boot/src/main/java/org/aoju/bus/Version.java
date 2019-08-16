/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus;

/**
 * 用于识别当前版本号和版权声明!
 * Version is Licensed under the MIT License, Version 3.0.0 (the "License")
 *
 * @author Kimi Liu
 * @version 3.0.6
 * @since JDK 1.8
 */
public class Version {

    /**
     * 获取 Version 的版本号，版本号的命名规范
     *
     * <pre>
     * [大版本].[小版本].[发布流水号]
     * </pre>
     * <p>
     * 这里有点说明
     * <ul>
     * <li>大版本 - 表示API的版本，如果没有重大变化，基本上同样的大版本号，使用方式是一致的
     * <li>质量号 - alpha内部测试, beta 公测品质,RELEASE 生产品质
     * <li>小版本 - 每次发布增加1
     * </ul>
     *
     * @return 项目的版本号
     */

    public static String version() {
        return major() + "." + minor() + "." + stage() + "." + level();
    }

    /**
     * 主要版本号
     *
     * @return 版本号
     */
    public static String major() {
        return "3";
    }

    /**
     * 次要版本号
     *
     * @return 次要号
     */
    public static String minor() {
        return "0";
    }

    /**
     * 阶段版本号
     *
     * @return 阶段号
     */
    public static String stage() {
        return "5";
    }

    /**
     * 版本质量
     *
     * @return 质量
     */
    public static String level() {
        return "RELEASE";
    }

}
