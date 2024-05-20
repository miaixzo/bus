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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行（控制台）工具方法类
 * 此类主要针对{@link System#out} 和 {@link System#err} 做封装。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Console {

    /**
     * 同 System.out.println()方法，打印控制台日志
     */
    public static void log() {
        System.out.println();
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param obj 要打印的对象
     */
    public static void log(final Object obj) {
        if (obj instanceof Throwable) {
            final Throwable e = (Throwable) obj;
            log(e, e.getMessage());
        } else {
            log(Symbol.DELIM, obj);
        }
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param obj1      第一个要打印的对象
     * @param otherObjs 其它要打印的对象
     */
    public static void log(final Object obj1, final Object... otherObjs) {
        if (ArrayKit.isEmpty(otherObjs)) {
            log(obj1);
        } else {
            log(buildTemplateSplitBySpace(otherObjs.length + 1), ArrayKit.insert(otherObjs, 0, obj1));
        }
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     * 当传入template无"{}"时，被认为非模板，直接打印多个参数以空格分隔
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void log(final String template, final Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            logInternal(template, values);
        } else {
            logInternal(buildTemplateSplitBySpace(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     *
     * @param t        异常对象
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void log(final Throwable t, final String template, final Object... values) {
        System.out.println(StringKit.format(template, values));
        if (null != t) {
            t.printStackTrace(System.out);
            System.out.flush();
        }
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    private static void logInternal(final String template, final Object... values) {
        log(null, template, values);
    }

    /**
     * 打印表格到控制台
     *
     * @param consoleTable 控制台表格
     */
    public static void table(final Table consoleTable) {
        print(consoleTable.toString());
    }

    /**
     * 同 System.out.print()方法，打印控制台日志
     *
     * @param obj 要打印的对象
     */
    public static void print(final Object obj) {
        print(Symbol.DELIM, obj);
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param obj1      第一个要打印的对象
     * @param otherObjs 其它要打印的对象
     */
    public static void print(final Object obj1, final Object... otherObjs) {
        if (ArrayKit.isEmpty(otherObjs)) {
            print(obj1);
        } else {
            print(buildTemplateSplitBySpace(otherObjs.length + 1), ArrayKit.insert(otherObjs, 0, obj1));
        }
    }

    /**
     * 同 System.out.print()方法，打印控制台日志
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void print(final String template, final Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            printInternal(template, values);
        } else {
            printInternal(buildTemplateSplitBySpace(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * 打印进度条
     *
     * @param showChar 进度条提示字符，例如“#”
     * @param len      打印长度
     */
    public static void printProgress(final char showChar, final int len) {
        print("{}{}", Symbol.C_CR, StringKit.repeat(showChar, len));
    }

    /**
     * 打印进度条
     *
     * @param showChar 进度条提示字符，例如“#”
     * @param totalLen 总长度
     * @param rate     总长度所占比取值0~1
     */
    public static void printProgress(final char showChar, final int totalLen, final double rate) {
        Assert.isTrue(rate >= 0 && rate <= 1, "Rate must between 0 and 1 (both include)");
        printProgress(showChar, (int) (totalLen * rate));
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    private static void printInternal(final String template, final Object... values) {
        System.out.print(StringKit.format(template, values));
    }

    /**
     * 同 System.err.println()方法，打印控制台日志
     */
    public static void error() {
        System.err.println();
    }

    /**
     * 同 System.err.println()方法，打印控制台日志
     *
     * @param obj 要打印的对象
     */
    public static void error(final Object obj) {
        if (obj instanceof Throwable) {
            final Throwable e = (Throwable) obj;
            error(e, e.getMessage());
        } else {
            error(Symbol.DELIM, obj);
        }
    }

    /**
     * 同 System.out.println()方法，打印控制台日志
     * 如果传入打印对象为{@link Throwable}对象，那么同时打印堆栈
     *
     * @param obj1      第一个要打印的对象
     * @param otherObjs 其它要打印的对象
     */
    public static void error(final Object obj1, final Object... otherObjs) {
        if (ArrayKit.isEmpty(otherObjs)) {
            error(obj1);
        } else {
            error(buildTemplateSplitBySpace(otherObjs.length + 1), ArrayKit.insert(otherObjs, 0, obj1));
        }
    }

    /**
     * 同 System.err.println()方法，打印控制台日志
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void error(final String template, final Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            errorInternal(template, values);
        } else {
            errorInternal(buildTemplateSplitBySpace(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * 同 System.err.println()方法，打印控制台日志
     *
     * @param t        异常对象
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    public static void error(final Throwable t, final String template, final Object... values) {
        System.err.println(StringKit.format(template, values));
        if (null != t) {
            t.printStackTrace(System.err);
            System.err.flush();
        }
    }

    /**
     * 同 System.err.println()方法，打印控制台日志
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param values   值
     */
    private static void errorInternal(final String template, final Object... values) {
        error(null, template, values);
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
    private static String buildTemplateSplitBySpace(final int count) {
        return StringKit.repeatAndJoin(Symbol.DELIM, count, Symbol.SPACE);
    }

    /**
     * 控制台打印表格工具
     */
    public static class Table {

        private static final char ROW_LINE = '－';
        private static final char COLUMN_LINE = '|';

        private static final char SPACE = '\u3000';
        private static final char LF = Symbol.C_LF;
        /**
         * 表格头信息
         */
        private final List<List<String>> headerList = new ArrayList<>();
        /**
         * 表格体信息
         */
        private final List<List<String>> bodyList = new ArrayList<>();
        private boolean isSBCMode = true;
        /**
         * 每列最大字符个数
         */
        private List<Integer> columnCharNumber;

        /**
         * 创建ConsoleTable对象
         *
         * @return Table
         */
        public static Table of() {
            return new Table();
        }

        /**
         * 设置是否使用全角模式
         * 当包含中文字符时，输出的表格可能无法对齐，因此当设置为全角模式时，全部字符转为全角。
         *
         * @param isSBCMode 是否全角模式
         * @return this
         */
        public Table setSBCMode(final boolean isSBCMode) {
            this.isSBCMode = isSBCMode;
            return this;
        }

        /**
         * 添加头信息
         *
         * @param titles 列名
         * @return 自身对象
         */
        public Table addHeader(final String... titles) {
            if (columnCharNumber == null) {
                columnCharNumber = new ArrayList<>(Collections.nCopies(titles.length, 0));
            }
            final List<String> l = new ArrayList<>();
            fillColumns(l, titles);
            headerList.add(l);
            return this;
        }

        /**
         * 添加体信息
         *
         * @param values 列值
         * @return 自身对象
         */
        public Table addBody(final String... values) {
            final List<String> l = new ArrayList<>();
            bodyList.add(l);
            fillColumns(l, values);
            return this;
        }

        /**
         * 填充表格头或者体
         *
         * @param l       被填充列表
         * @param columns 填充内容
         */
        private void fillColumns(final List<String> l, final String[] columns) {
            String column;
            for (int i = 0; i < columns.length; i++) {
                column = StringKit.toString(columns[i]);
                if (isSBCMode) {
                    column = Convert.toSBC(column);
                }
                l.add(column);
                final int width = column.length();
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
            final StringBuilder sb = new StringBuilder();
            fillBorder(sb);
            fillRows(sb, headerList);
            fillBorder(sb);
            fillRows(sb, bodyList);
            fillBorder(sb);
            return sb.toString();
        }

        /**
         * 填充表头或者表体信息（多行）
         *
         * @param sb   内容
         * @param list 表头列表或者表体列表
         */
        private void fillRows(final StringBuilder sb, final List<List<String>> list) {
            for (final List<String> row : list) {
                sb.append(COLUMN_LINE);
                fillRow(sb, row);
                sb.append(LF);
            }
        }

        /**
         * 填充一行数据
         *
         * @param sb  内容
         * @param row 一行数据
         */
        private void fillRow(final StringBuilder sb, final List<String> row) {
            final int size = row.size();
            String value;
            for (int i = 0; i < size; i++) {
                value = row.get(i);
                sb.append(SPACE);
                sb.append(value);
                final int length = value.length();
                final int sbcCount = sbcCount(value);
                if (sbcCount % 2 == 1) {
                    sb.append(Symbol.C_SPACE);
                }
                sb.append(SPACE);
                final int maxLength = columnCharNumber.get(i);
                for (int j = 0; j < (maxLength - length + (sbcCount / 2)); j++) {
                    sb.append(SPACE);
                }
                sb.append(COLUMN_LINE);
            }
        }

        /**
         * 拼装边框
         *
         * @param sb StringBuilder
         */
        private void fillBorder(final StringBuilder sb) {
            sb.append(Symbol.C_PLUS);
            for (final Integer width : columnCharNumber) {
                sb.append(StringKit.repeat(ROW_LINE, width + 2));
                sb.append(Symbol.C_PLUS);
            }
            sb.append(LF);
        }

        /**
         * 打印到控制台
         */
        public void print() {
            Console.print(toString());
        }

        /**
         * 半角字符数量
         *
         * @param value 字符串
         * @return 填充空格数量
         */
        private int sbcCount(final String value) {
            int count = 0;
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) < '\177') {
                    count++;
                }
            }

            return count;
        }
    }

}
