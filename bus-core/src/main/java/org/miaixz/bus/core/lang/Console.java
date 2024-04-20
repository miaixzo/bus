/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.System;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行(控制台)方法类
 * 此类主要针对{@link java.lang.System#out} 和 {@link java.lang.System#err} 做封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Console {

    /**
     * 表格头信息
     */
    private final List<List<String>> headers = new ArrayList<>();
    /**
     * 表格体信息
     */
    private final List<List<String>> bodys = new ArrayList<>();
    /**
     * 每列最大字符个数
     */
    private List<Integer> maxChar;


    /**
     * 打印控制台日志,同System.out.println()方法
     */
    public static void log() {
        System.out.println();
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     * 如果传入打印对象为{@link Throwable}对象,那么同时打印堆栈
     *
     * @param object 要打印的对象
     */
    public static void log(Object object) {
        if (object instanceof Throwable) {
            final Throwable e = (Throwable) object;
            log(e, e.getMessage());
        } else {
            log(Symbol.DELIM, object);
        }
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param object 打印模板
     * @param args   模板参数
     */
    public static void log(Object object, Object... args) {
        if (ArrayKit.isEmpty(args)) {
            log(object);
        } else {
            log(build(args.length + 1), ArrayKit.insert(args, 0, object));
        }
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     * 当传入template无"{}"时，被认为非模板，直接打印多个参数以空格分隔
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void log(String template, Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            logInternal(template, values);
        } else {
            logInternal(build(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     *
     * @param t        异常对象
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void log(Throwable t, String template, Object... values) {
        System.out.println(StringKit.format(template, values));
        if (null != t) {
            t.printStackTrace(System.out);
            System.out.flush();
        }
    }

    /**
     * 打印控制台日志,同System.out.print()方法
     *
     * @param object 要打印的对象
     */
    public static void print(Object object) {
        print(Symbol.DELIM, object);
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param object 打印模板
     * @param args   模板参数
     */
    public static void print(Object object, Object... args) {
        if (ArrayKit.isEmpty(args)) {
            print(object);
        } else {
            print(build(args.length + 1), ArrayKit.insert(args, 0, object));
        }
    }

    /**
     * 打印控制台日志,同System.out.print()方法
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void print(String template, Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            printInternal(template, values);
        } else {
            printInternal(build(values.length + 1), ArrayKit.insert(values, 0, template));
        }
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
     * 打印控制台日志,同System.err.println()方法同
     */
    public static void error() {
        System.err.println();
    }

    /**
     * 打印控制台日志,同System.err.println()方法同
     *
     * @param object 要打印的对象
     */
    public static void error(Object object) {
        if (object instanceof Throwable) {
            Throwable e = (Throwable) object;
            error(e, e.getMessage());
        } else {
            error(Symbol.DELIM, object);
        }
    }

    /**
     * 打印控制台日志,同System.err.println()方法同
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param object 打印模板
     * @param args   模板参数
     */
    public static void error(Object object, Object... args) {
        if (ArrayKit.isEmpty(args)) {
            error(args);
        } else {
            error(build(args.length + 1), ArrayKit.insert(args, 0, object));
        }
    }

    /**
     * 打印控制台日志,同System.err.println()方法同
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void error(String template, Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            errorInternal(template, values);
        } else {
            errorInternal(build(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * 打印控制台日志,同System.err.println()方法同
     *
     * @param t        异常对象
     * @param template 文本模板，被替换的部分用 {} 表示
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
     * 创建从控制台读取内容的{@link Scanner}
     *
     * @return {@link Scanner}
     */
    public static Scanner scanner() {
        return new Scanner(System.in);
    }

    /**
     * 读取用户输入的内容（在控制台敲回车前的内容）
     *
     * @return 用户输入的内容
     */
    public static String input() {
        return scanner().nextLine();
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

    /**
     * 构建空格分隔的模板，类似于"{} {} {} {}"
     *
     * @param count 变量数量
     * @return 模板
     */
    private static String build(int count) {
        return StringKit.repeatAndJoin(Symbol.DELIM, count, Symbol.SPACE);
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    private static void logInternal(String template, Object... values) {
        log(null, template, values);
    }

    /**
     * 打印控制台日志,同System.out.println()方法
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    private static void printInternal(String template, Object... values) {
        System.out.print(StringKit.format(template, values));
    }

    /**
     * 打印控制台日志,同System.err.println()方法同
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    private static void errorInternal(String template, Object... values) {
        error(null, template, values);
    }

    /**
     * 添加头信息
     *
     * @param columns 列名
     * @return 自身对象
     */
    public Console addHeader(String... columns) {
        maxChar = new ArrayList<>(Collections.nCopies(columns.length, 0));
        List<String> l = new ArrayList<>();
        headers.add(l);
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String col = Convert.toSBC(column);
            l.add(col);
            int width = col.length();
            maxChar.set(i, width);
        }
        return this;
    }

    /**
     * 添加体信息
     *
     * @param values 列值
     * @return 自身对象
     */
    public Console addBody(String... values) {
        List<String> l = new ArrayList<>();
        bodys.add(l);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            String val = Convert.toSBC(value);
            l.add(val);
            int width = val.length();
            if (width > maxChar.get(i)) {
                maxChar.set(i, width);
            }
        }
        return this;
    }

    /**
     * 获取表格字符串
     *
     * @return 表格字符串
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        border(sb);
        for (List<String> headers : headers) {
            for (int i = 0; i < headers.size(); i++) {
                if (i == 0) {
                    sb.append(Symbol.C_OR);
                }
                String header = headers.get(i);
                sb.append(Convert.toSBC(Symbol.SPACE));
                sb.append(header);
                sb.append(Convert.toSBC(Symbol.SPACE));
                int l = header.length();
                int lw = maxChar.get(i);
                if (lw > l) {
                    for (int j = 0; j < (lw - l); j++) {
                        sb.append(Convert.toSBC(Symbol.SPACE));
                    }
                }
                sb.append(Symbol.C_OR);
            }
            sb.append(Symbol.C_LF);
        }
        border(sb);
        for (List<String> bodys : bodys) {
            for (int i = 0; i < bodys.size(); i++) {
                if (i == 0) {
                    sb.append(Symbol.C_OR);
                }
                String body = bodys.get(i);
                sb.append(Convert.toSBC(Symbol.SPACE));
                sb.append(body);
                sb.append(Convert.toSBC(Symbol.SPACE));
                int l = body.length();
                int lw = maxChar.get(i);
                if (lw > l) {
                    for (int j = 0; j < (lw - l); j++) {
                        sb.append(Convert.toSBC(Symbol.SPACE));
                    }
                }
                sb.append(Symbol.C_OR);
            }
            sb.append(Symbol.C_LF);
        }
        border(sb);
        return sb.toString();
    }

    private void border(StringBuilder sb) {
        sb.append(Symbol.STAR);
        for (Integer width : maxChar) {
            sb.append(Convert.toSBC(StringKit.fillAfter(Normal.EMPTY, Symbol.C_MINUS, width + 2)));
            sb.append(Symbol.STAR);
        }
        sb.append(Symbol.C_LF);
    }

    public static class Table {

        /**
         * 表格头信息
         */
        private final List<List<String>> HEADER_LIST = new ArrayList<>();
        /**
         * 表格体信息
         */
        private final List<List<String>> BODY_LIST = new ArrayList<>();
        /**
         * 每列最大字符个数
         */
        private List<Integer> columnCharNumber;

        /**
         * 创建Table对象
         *
         * @return Table
         */
        public static Table of() {
            return new Table();
        }

        /**
         * 添加头信息
         *
         * @param titles 列名
         * @return 自身对象
         */
        public Table addHeader(String... titles) {
            if (null == columnCharNumber) {
                columnCharNumber = new ArrayList<>(Collections.nCopies(titles.length, 0));
            }
            List<String> l = new ArrayList<>();
            fillColumns(l, titles);
            HEADER_LIST.add(l);
            return this;
        }

        /**
         * 添加体信息
         *
         * @param values 列值
         * @return 自身对象
         */
        public Table addBody(String... values) {
            List<String> l = new ArrayList<>();
            BODY_LIST.add(l);
            fillColumns(l, values);
            return this;
        }

        /**
         * 填充表格头或者体
         *
         * @param l       被填充列表
         * @param columns 填充内容
         */
        private void fillColumns(List<String> l, String[] columns) {
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                String col = Convert.toSBC(column);
                l.add(col);
                int width = column.length();
                if (width > columnCharNumber.get(i)) {
                    columnCharNumber.set(i, width);
                }
            }
        }

        /**
         * 获取表格字符串
         *
         * @return 表格字符串
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            fillBorder(sb);
            fillRows(sb, HEADER_LIST);
            fillBorder(sb);
            fillRows(sb, BODY_LIST);
            fillBorder(sb);
            return sb.toString();
        }

        /**
         * 填充表头或者表体信息（多行）
         *
         * @param sb   内容
         * @param list 表头列表或者表体列表
         */
        private void fillRows(StringBuilder sb, List<List<String>> list) {
            for (List<String> row : list) {
                sb.append(Symbol.C_OR);
                fillRow(sb, row);
                sb.append(Symbol.C_LF);
            }
        }

        /**
         * 填充一行数据
         *
         * @param sb  内容
         * @param row 一行数据
         */
        private void fillRow(StringBuilder sb, List<String> row) {
            final int size = row.size();
            String value;
            for (int i = 0; i < size; i++) {
                value = row.get(i);
                sb.append(Symbol.C_SPACE);
                sb.append(value);
                sb.append(Symbol.C_SPACE);
                int length = value.length();
                int maxLength = columnCharNumber.get(i);
                if (maxLength > length) {
                    for (int j = 0; j < (maxLength - length); j++) {
                        sb.append(Symbol.C_SPACE);
                    }
                }
                sb.append(Symbol.C_OR);
            }
        }

        /**
         * 拼装边框
         *
         * @param sb StringBuilder
         */
        private void fillBorder(StringBuilder sb) {
            sb.append(Symbol.C_PLUS);
            for (Integer width : columnCharNumber) {
                sb.append(StringKit.repeat(Symbol.C_MINUS, width + 2));
                sb.append(Symbol.C_PLUS);
            }
            sb.append(Symbol.C_LF);
        }

        /**
         * 打印到控制台
         */
        public void print() {
            Console.print(toString());
        }

    }

}
