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
package org.miaixz.bus.core.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Command-line (console) utility methods. This class primarily encapsulates {@link System#out} and {@link System#err}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Console {

    /**
     * Constructs a new Console. Utility class constructor for static access.
     */
    private Console() {
    }

    /**
     * Prints a new line to the console, similar to {@code System.out.println()}.
     */
    public static void log() {
        System.out.println();
    }

    /**
     * Prints an object to the console, similar to {@code System.out.println()}. If the object is a {@link Throwable},
     * its stack trace is also printed.
     *
     * @param object The object to print.
     */
    public static void log(final Object object) {
        if (object instanceof Throwable e) {
            log(e, e.getMessage());
        } else {
            log(Symbol.DELIM, object);
        }
    }

    /**
     * Prints multiple objects to the console, separated by spaces. If the first object is a {@link Throwable}, its
     * stack trace is also printed.
     *
     * @param obj1      The first object to print.
     * @param otherObjs Other objects to print.
     */
    public static void log(final Object obj1, final Object... otherObjs) {
        if (ArrayKit.isEmpty(otherObjs)) {
            log(obj1);
        } else {
            log(buildTemplateSplitBySpace(otherObjs.length + 1), ArrayKit.insert(otherObjs, 0, obj1));
        }
    }

    /**
     * Prints a formatted message to the console, similar to {@code System.out.println()}. If the template does not
     * contain "{}", the arguments are printed separated by spaces.
     *
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    public static void log(final String template, final Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            logInternal(template, values);
        } else {
            logInternal(buildTemplateSplitBySpace(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * Prints a formatted message and a {@link Throwable}'s stack trace to the console, similar to
     * {@code System.out.println()}.
     *
     * @param t        The {@link Throwable} object whose stack trace is to be printed.
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    public static void log(final Throwable t, final String template, final Object... values) {
        System.out.println(StringKit.format(template, values));
        if (null != t) {
            t.printStackTrace(System.out);
            System.out.flush();
        }
    }

    /**
     * Internal method to print a formatted message to {@code System.out}.
     *
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    private static void logInternal(final String template, final Object... values) {
        log(null, template, values);
    }

    /**
     * Prints a console table to the console.
     *
     * @param consoleTable The {@link Table} object to print.
     */
    public static void table(final Table consoleTable) {
        print(consoleTable.toString());
    }

    /**
     * Prints an object to the console without a new line, similar to {@code System.out.print()}.
     *
     * @param object The object to print.
     */
    public static void print(final Object object) {
        print(Symbol.DELIM, object);
    }

    /**
     * Prints multiple objects to the console, separated by spaces, without a new line. If the first object is a
     * {@link Throwable}, its stack trace is also printed.
     *
     * @param object    The first object to print.
     * @param otherObjs Other objects to print.
     */
    public static void print(final Object object, final Object... otherObjs) {
        if (ArrayKit.isEmpty(otherObjs)) {
            print(object);
        } else {
            print(buildTemplateSplitBySpace(otherObjs.length + 1), ArrayKit.insert(otherObjs, 0, object));
        }
    }

    /**
     * Prints a formatted message to the console without a new line, similar to {@code System.out.print()}. If the
     * template does not contain "{}", the arguments are printed separated by spaces.
     *
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    public static void print(final String template, final Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            printInternal(template, values);
        } else {
            printInternal(buildTemplateSplitBySpace(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * Prints a progress bar to the console.
     *
     * @param showChar The character used to display the progress, e.g., '#'.
     * @param len      The length of the progress bar to print.
     */
    public static void printProgress(final char showChar, final int len) {
        print("{}{}", Symbol.C_CR, StringKit.repeat(showChar, len));
    }

    /**
     * Prints a progress bar to the console based on a given rate.
     *
     * @param showChar The character used to display the progress, e.g., '#'.
     * @param totalLen The total length of the progress bar.
     * @param rate     The progress rate, a value between 0 and 1 (inclusive).
     * @throws IllegalArgumentException if the rate is not between 0 and 1.
     */
    public static void printProgress(final char showChar, final int totalLen, final double rate) {
        Assert.isTrue(rate >= 0 && rate <= 1, "Rate must between 0 and 1 (both include)");
        printProgress(showChar, (int) (totalLen * rate));
    }

    /**
     * Internal method to print a formatted message to {@code System.out} without a new line.
     *
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    private static void printInternal(final String template, final Object... values) {
        System.out.print(StringKit.format(template, values));
    }

    /**
     * Prints a new line to the error console, similar to {@code System.err.println()}.
     */
    public static void error() {
        System.err.println();
    }

    /**
     * Prints an object to the error console, similar to {@code System.err.println()}. If the object is a
     * {@link Throwable}, its stack trace is also printed.
     *
     * @param object The object to print.
     */
    public static void error(final Object object) {
        if (object instanceof Throwable) {
            final Throwable e = (Throwable) object;
            error(e, e.getMessage());
        } else {
            error(Symbol.DELIM, object);
        }
    }

    /**
     * Prints multiple objects to the error console, separated by spaces. If the first object is a {@link Throwable},
     * its stack trace is also printed.
     *
     * @param object    The first object to print.
     * @param otherObjs Other objects to print.
     */
    public static void error(final Object object, final Object... otherObjs) {
        if (ArrayKit.isEmpty(otherObjs)) {
            error(object);
        } else {
            error(buildTemplateSplitBySpace(otherObjs.length + 1), ArrayKit.insert(otherObjs, 0, object));
        }
    }

    /**
     * Prints a formatted error message to the console, similar to {@code System.err.println()}. If the template does
     * not contain "{}", the arguments are printed separated by spaces.
     *
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    public static void error(final String template, final Object... values) {
        if (ArrayKit.isEmpty(values) || StringKit.contains(template, Symbol.DELIM)) {
            errorInternal(template, values);
        } else {
            errorInternal(buildTemplateSplitBySpace(values.length + 1), ArrayKit.insert(values, 0, template));
        }
    }

    /**
     * Prints a formatted error message and a {@link Throwable}'s stack trace to the console, similar to
     * {@code System.err.println()}.
     *
     * @param t        The {@link Throwable} object whose stack trace is to be printed.
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    public static void error(final Throwable t, final String template, final Object... values) {
        System.err.println(StringKit.format(template, values));
        if (null != t) {
            t.printStackTrace(System.err);
            System.err.flush();
        }
    }

    /**
     * Internal method to print a formatted message to {@code System.err}.
     *
     * @param template The text template, with "{}" as placeholders for arguments.
     * @param values   The arguments to fill into the template.
     */
    private static void errorInternal(final String template, final Object... values) {
        error(null, template, values);
    }

    /**
     * Creates a {@link Scanner} to read content from the console ({@code System.in}).
     *
     * @return A new {@link Scanner} instance.
     */
    public static Scanner scanner() {
        return new Scanner(System.in);
    }

    /**
     * Reads a line of content entered by the user from the console (until a newline character is encountered).
     *
     * @return The content entered by the user.
     */
    public static String input() {
        return scanner().nextLine();
    }

    /**
     * Returns the current code location and line number. (Not supported for use within Lambdas, inner classes, or
     * recursion).
     *
     * @return A string representing the current class, method, file, and line number.
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
     * Returns the current line number. (Not supported for use within Lambdas, inner classes, or recursion).
     *
     * @return The current line number.
     */
    public static Integer lineNumber() {
        return new Throwable().getStackTrace()[1].getLineNumber();
    }

    /**
     * Builds a space-separated template string, e.g., "{} {} {} {}".
     *
     * @param count The number of placeholders.
     * @return The generated template string.
     */
    private static String buildTemplateSplitBySpace(final int count) {
        return StringKit.repeatAndJoin(Symbol.DELIM, count, Symbol.SPACE);
    }

    /**
     * Utility class for printing tables to the console.
     */
    public static class Table {

        /**
         * Constructs a new Table.
         */
        public Table() {
        }

        private static final char ROW_LINE = '－';
        private static final char COLUMN_LINE = '|';

        private static final char SPACE = '\u3000';
        private static final char LF = Symbol.C_LF;
        /**
         * List of header rows for the table.
         */
        private final List<List<String>> headerList = new ArrayList<>();
        /**
         * List of body rows for the table.
         */
        private final List<List<String>> bodyList = new ArrayList<>();
        /**
         * Flag indicating whether to use full-width character mode. When set to true, all characters are converted to
         * full-width to ensure alignment when Chinese characters are present.
         */
        private boolean isSBCMode = true;
        /**
         * Maximum character count for each column.
         */
        private List<Integer> columnCharNumber;

        /**
         * Creates a new {@code Table} instance.
         *
         * @return A new {@code Table} object.
         */
        public static Table of() {
            return new Table();
        }

        /**
         * Sets whether to use full-width character mode. When Chinese characters are included, the output table may not
         * align correctly. In full-width mode, all characters are converted to full-width to ensure proper alignment.
         *
         * @param isSBCMode {@code true} to enable full-width mode, {@code false} otherwise.
         * @return This {@code Table} instance for method chaining.
         */
        public Table setSBCMode(final boolean isSBCMode) {
            this.isSBCMode = isSBCMode;
            return this;
        }

        /**
         * Adds a header row to the table.
         *
         * @param titles The column titles for the header row.
         * @return This {@code Table} instance for method chaining.
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
         * Adds a body row to the table.
         *
         * @param values The column values for the body row.
         * @return This {@code Table} instance for method chaining.
         */
        public Table addBody(final String... values) {
            final List<String> l = new ArrayList<>();
            bodyList.add(l);
            fillColumns(l, values);
            return this;
        }

        /**
         * Fills columns for a given row (either header or body) and updates the maximum column character count.
         *
         * @param l       The list representing the row to fill.
         * @param columns The array of strings representing the column values.
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
         * Generates the string representation of the table.
         *
         * @return The formatted table as a string.
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
         * Fills the rows of the table (either header or body) into the {@link StringBuilder}.
         *
         * @param sb   The {@link StringBuilder} to append the rows to.
         * @param list The list of rows (each row is a list of strings).
         */
        private void fillRows(final StringBuilder sb, final List<List<String>> list) {
            for (final List<String> row : list) {
                sb.append(COLUMN_LINE);
                fillRow(sb, row);
                sb.append(LF);
            }
        }

        /**
         * Fills a single row of data into the {@link StringBuilder}, ensuring proper alignment based on column widths.
         *
         * @param sb  The {@link StringBuilder} to append the row to.
         * @param row The list of strings representing the data in the row.
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
                sb.append(String.valueOf(SPACE).repeat(Math.max(0, (maxLength - length + (sbcCount / 2)))));
                sb.append(COLUMN_LINE);
            }
        }

        /**
         * Appends a border line to the {@link StringBuilder}.
         *
         * @param sb The {@link StringBuilder} to append the border to.
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
         * Prints the table to the console.
         */
        public void print() {
            Console.print(toString());
        }

        /**
         * Calculates the number of half-width characters in a string.
         *
         * @param value The string to check.
         * @return The count of half-width characters.
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
