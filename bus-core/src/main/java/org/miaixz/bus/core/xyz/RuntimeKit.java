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
package org.miaixz.bus.core.xyz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.miaixz.bus.core.data.id.Pid;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * System runtime utility class for executing system commands.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RuntimeKit {

    /**
     * Executes a system command using the default system character set.
     *
     * @param cmds The commands to execute.
     * @return The execution result as a string.
     * @throws InternalException for IO errors.
     */
    public static String execForString(final String... cmds) throws InternalException {
        return execForString(Charset.systemCharset(), cmds);
    }

    /**
     * Executes a system command using the specified character set.
     *
     * @param charset The character set for the output.
     * @param cmds    The commands to execute.
     * @return The execution result as a string.
     * @throws InternalException for IO errors.
     */
    public static String execForString(final java.nio.charset.Charset charset, final String... cmds)
            throws InternalException {
        return getResult(exec(cmds), charset);
    }

    /**
     * Executes a system command and returns the output as a list of lines, using the default system character set.
     *
     * @param cmds The commands to execute.
     * @return The execution result as a list of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> execForLines(final String... cmds) throws InternalException {
        return execForLines(Charset.systemCharset(), cmds);
    }

    /**
     * Executes a system command and returns the output as a list of lines, using the specified character set.
     *
     * @param charset The character set for the output.
     * @param cmds    The commands to execute.
     * @return The execution result as a list of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> execForLines(final java.nio.charset.Charset charset, final String... cmds)
            throws InternalException {
        return getResultLines(exec(cmds), charset);
    }

    /**
     * Executes a command.
     *
     * @param cmds The command and its arguments.
     * @return The {@link Process} object.
     */
    public static Process exec(final String... cmds) {
        final Process process;
        try {
            process = new ProcessBuilder(handleCmds(cmds)).redirectErrorStream(true).start();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return process;
    }

    /**
     * Executes a command with specified environment variables.
     *
     * @param envp Environment variables in "key=value" format. If `null`, inherits system environment.
     * @param cmds The command and its arguments.
     * @return The {@link Process} object.
     */
    public static Process exec(final String[] envp, final String... cmds) {
        return exec(envp, null, cmds);
    }

    /**
     * Executes a command with specified environment variables and working directory.
     *
     * @param envp Environment variables in "key=value" format.
     * @param dir  The working directory. If `null`, uses the current process's directory.
     * @param cmds The command and its arguments.
     * @return The {@link Process} object.
     */
    public static Process exec(final String[] envp, final File dir, final String... cmds) {
        try {
            return Runtime.getRuntime().exec(handleCmds(cmds), envp, dir);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the result of a command execution as a list of lines and destroys the process.
     *
     * @param process The {@link Process}.
     * @return The command output as a list of lines.
     */
    public static List<String> getResultLines(final Process process) {
        return getResultLines(process, Charset.systemCharset());
    }

    /**
     * Gets the result of a command execution as a list of lines with a specified charset and destroys the process.
     *
     * @param process The {@link Process}.
     * @param charset The character set.
     * @return The command output as a list of lines.
     */
    public static List<String> getResultLines(final Process process, final java.nio.charset.Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoKit.readLines(in, charset, new ArrayList<>());
        } finally {
            IoKit.closeQuietly(in);
            destroy(process);
        }
    }

    /**
     * Gets the result of a command execution as a single string and destroys the process.
     *
     * @param process The {@link Process}.
     * @return The command output as a string.
     */
    public static String getResult(final Process process) {
        return getResult(process, Charset.systemCharset());
    }

    /**
     * Gets the result of a command execution as a single string with a specified charset and destroys the process.
     *
     * @param process The {@link Process}.
     * @param charset The character set.
     * @return The command output as a string.
     */
    public static String getResult(final Process process, final java.nio.charset.Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoKit.read(in, charset);
        } finally {
            IoKit.closeQuietly(in);
            destroy(process);
        }
    }

    /**
     * Gets the error stream result of a command execution and destroys the process.
     *
     * @param process The {@link Process}.
     * @return The error output as a string.
     */
    public static String getErrorResult(final Process process) {
        return getErrorResult(process, Charset.systemCharset());
    }

    /**
     * Gets the error stream result of a command execution with a specified charset and destroys the process.
     *
     * @param process The {@link Process}.
     * @param charset The character set.
     * @return The error output as a string.
     */
    public static String getErrorResult(final Process process, final java.nio.charset.Charset charset) {
        InputStream in = null;
        try {
            in = process.getErrorStream();
            return IoKit.read(in, charset);
        } finally {
            IoKit.closeQuietly(in);
            destroy(process);
        }
    }

    /**
     * Destroys a process if it is not null.
     *
     * @param process The process.
     */
    public static void destroy(final Process process) {
        if (null != process) {
            process.destroy();
        }
    }

    /**
     * Adds a shutdown hook to be executed when the JVM is shutting down.
     *
     * @param hook The hook as a `Runnable`.
     */
    public static void addShutdownHook(final Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

    /**
     * Gets the number of available processors for the JVM. Returns a default of 7 if the actual count cannot be
     * determined or is &lt;= 0.
     *
     * @return The number of available processors.
     */
    public static int getProcessorCount() {
        int cpu = Runtime.getRuntime().availableProcessors();
        if (cpu <= 0) {
            cpu = 7;
        }
        return cpu;
    }

    /**
     * Gets the amount of free memory in the JVM in bytes.
     *
     * @return The amount of free memory.
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Gets the total amount of memory currently available to the JVM in bytes.
     *
     * @return The total amount of memory.
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Gets the maximum amount of memory that the JVM will attempt to use in bytes.
     *
     * @return The maximum amount of memory.
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Gets the amount of usable memory for the JVM.
     *
     * @return The usable memory.
     */
    public static long getUsableMemory() {
        return getMaxMemory() - getTotalMemory() + getFreeMemory();
    }

    /**
     * Gets the current process ID (PID).
     *
     * @return The process ID.
     * @throws InternalException if the process name is empty.
     */
    public static int getPid() throws InternalException {
        return Pid.INSTANCE.get();
    }

    /**
     * Handles command strings, splitting a single command line into an array if necessary.
     *
     * @param cmds The commands.
     * @return The processed command array.
     */
    private static String[] handleCmds(String... cmds) {
        if (ArrayKit.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringKit.isBlank(cmd)) {
                throw new NullPointerException("Command is blank !");
            }
            cmds = cmdSplit(cmd);
        }
        return cmds;
    }

    /**
     * Splits a command string into an array, respecting single and double quotes.
     *
     * @param cmd The command string (e.g., "git commit -m 'test commit'").
     * @return The split command array.
     */
    private static String[] cmdSplit(final String cmd) {
        final List<String> cmds = new ArrayList<>();
        final int length = cmd.length();
        final Stack<Character> stack = new Stack<>();
        boolean inWrap = false;
        final StringBuilder cache = new StringBuilder();

        char c;
        for (int i = 0; i < length; i++) {
            c = cmd.charAt(i);
            switch (c) {
                case Symbol.C_SINGLE_QUOTE:
                case Symbol.C_DOUBLE_QUOTES:
                    if (inWrap) {
                        if (c == stack.peek()) {
                            stack.pop();
                            inWrap = false;
                        }
                        cache.append(c);
                    } else {
                        stack.push(c);
                        cache.append(c);
                        inWrap = true;
                    }
                    break;

                case Symbol.C_SPACE:
                    if (inWrap) {
                        cache.append(c);
                    } else {
                        cmds.add(cache.toString());
                        cache.setLength(0);
                    }
                    break;

                default:
                    cache.append(c);
                    break;
            }
        }
        if (!cache.isEmpty()) {
            cmds.add(cache.toString());
        }
        return cmds.toArray(new String[0]);
    }

}
