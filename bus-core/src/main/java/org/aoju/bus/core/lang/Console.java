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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.toolkit.StringKit;

import java.lang.System;
import java.util.Scanner;

/**
 * 命令行(控制台)工具方法类
 * 此类主要针对{@link java.lang.System#out} 和 {@link java.lang.System#err} 做封装
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public class Console {

    /**
     * 打印控制台日志
     */
    public static void log() {
        System.out.println();
    }

    /**
     * 打印控制台日志
     * 如果传入打印对象为{@link Throwable}对象,那么同时打印堆栈
     *
     * @param obj 要打印的对象
     */
    public static void log(Object obj) {
        if (obj instanceof Throwable) {
            Throwable e = (Throwable) obj;
            log(e, e.getMessage());
        } else {
            log(Symbol.DELIM, obj);
        }
    }

    /**
     * 打印控制台日志
     *
     * @param template 文本模板,被替换的部分用 {} 表示
     * @param values   值
     */
    public static void log(String template, Object... values) {
        log(null, template, values);
    }

    /**
     * 打印控制台日志
     *
     * @param t        异常对象
     * @param template 文本模板,被替换的部分用 {} 表示
     * @param values   值
     */
    public static void log(Throwable t, String template, Object... values) {
        if (null != t) {
            t.printStackTrace();
            System.out.flush();
        }
    }

    /**
     * 打印控制台日志
     *
     * @param obj 要打印的对象
     */
    public static void print(Object obj) {
        print(Symbol.DELIM, obj);
    }

    /**
     * 打印控制台日志
     *
     * @param template 文本模板,被替换的部分用 {} 表示
     * @param values   值
     */
    public static void print(String template, Object... values) {
        System.out.println(StringKit.format(template, values));
    }

    /**
     * 打印进度条
     *
     * @param showChar 进度条提示字符，例如“#”
     * @param len      打印长度
     */
    public static void printProgress(char showChar, int len) {
        print("{}{}", Symbol.CR, StringKit.repeat(showChar, len));
    }

    /**
     * 打印进度条
     *
     * @param showChar 进度条提示字符，例如“#”
     * @param totalLen 总长度
     * @param rate     总长度所占比取值0~1
     */
    public static void printProgress(char showChar, int totalLen, double rate) {
        Assert.isTrue(rate >= 0 && rate <= 1, "Rate must between 0 and 1 (both include)");
        printProgress(showChar, (int) (totalLen * rate));
    }

    /**
     * 同 System.System.err.println()方法,打印控制台日志
     */
    public static void error() {
        System.err.println();
    }

    /**
     * 同 System.System.err.println()方法,打印控制台日志
     *
     * @param obj 要打印的对象
     */
    public static void error(Object obj) {
        if (obj instanceof Throwable) {
            Throwable e = (Throwable) obj;
            error(e, e.getMessage());
        } else {
            error(Symbol.DELIM, obj);
        }
    }

    /**
     * 同 System.System.err.println()方法,打印控制台日志
     *
     * @param template 文本模板,被替换的部分用 {} 表示
     * @param values   值
     */
    public static void error(String template, Object... values) {
        error(null, template, values);
    }

    /**
     * 同 System.System.err.println()方法,打印控制台日志
     *
     * @param t        异常对象
     * @param template 文本模板,被替换的部分用 {} 表示
     * @param values   值
     */
    public static void error(Throwable t, String template, Object... values) {
        System.err.println(StringKit.format(template, values));
        if (null != t) {
            t.printStackTrace(System.err);
            System.err.flush();
        }
    }

    /**
     * 读取用户输入的内容(在控制台敲回车前的内容)
     *
     * @return 用户输入的内容
     */
    public static String input() {
        return scanner().next();
    }

    /**
     * 创建从控制台读取内容的{@link Scanner}
     *
     * @return {@link Scanner}
     */
    public static Scanner scanner() {
        return new Scanner(System.in);
    }

    /**
     * 返回当前位置+行号 (不支持Lambda、内部类、递归内使用)
     *
     * @return 返回当前行号
     */
    public static String where() {
        final StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        final String className = stackTraceElement.getClassName();
        final String methodName = stackTraceElement.getMethodName();
        final String fileName = stackTraceElement.getFileName();
        final Integer lineNumber = stackTraceElement.getLineNumber();
        return String.format("%s.%s(%s:%s)", className, methodName, fileName, lineNumber);
    }

    /**
     * 返回当前行号 (不支持Lambda、内部类、递归内使用)
     *
     * @return 返回当前行号
     */
    public static Integer lineNumber() {
        return new Throwable().getStackTrace()[1].getLineNumber();
    }

}
