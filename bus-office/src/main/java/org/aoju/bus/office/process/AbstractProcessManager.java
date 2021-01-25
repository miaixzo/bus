/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.process;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Builder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 包含的所有流程管理器实现的基类.
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public abstract class AbstractProcessManager implements ProcessManager {

    /**
     * 初始化类的新实例.
     */
    protected AbstractProcessManager() {
        super();
    }

    private String buildOutput(final List<String> lines) {
        Objects.requireNonNull(lines, "lines must not be null");

        return lines.stream().filter(StringKit::isNotBlank).collect(Collectors.joining(Symbol.LF));
    }

    /**
     * 执行指定的命令并返回输出.
     *
     * @param cmdarray 包含要调用的命令及其参数的数组.
     * @return 命令执行输出.
     * @throws IOException 如果发生I/O错误.
     */
    protected List<String> execute(final String[] cmdarray) throws IOException {
        final Process process = Runtime.getRuntime().exec(cmdarray);

        final LinesPumpStreamHandler streamsHandler =
                new LinesPumpStreamHandler(process.getInputStream(), process.getErrorStream());

        streamsHandler.start();
        try {
            process.waitFor();
            streamsHandler.stop();
        } catch (InterruptedException ex) {
            Logger.warn("The current thread was interrupted while waiting for command execution output.", ex);
            Thread.currentThread().interrupt();
        }

        final List<String> outLines = streamsHandler.getOutputPumper().getLines();

        final String out = buildOutput(outLines);
        final String err = buildOutput(streamsHandler.getErrorPumper().getLines());

        if (!StringKit.isBlank(out)) {
            Logger.trace("Command Output: {}", out);
        }

        if (!StringKit.isBlank(err)) {
            Logger.trace("Command Error: {}", err);
        }

        return outLines;
    }

    @Override
    public long find(final ProcessQuery query) throws IOException {
        final Pattern commandPattern =
                Pattern.compile(
                        Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument()));
        final Pattern processLinePattern = getRunningProcessLinePattern();
        final String[] currentProcessesCommand = getRunningProcessesCommand(query.getCommand());

        Logger.trace(
                "Finding PID using\n"
                        + "Command to get current running processes: {}\n"
                        + "Regex used to match current running process lines: {}\n"
                        + "Regex used to match running office process we are looking for: {}",
                currentProcessesCommand,
                processLinePattern.pattern(),
                commandPattern.pattern());

        final List<String> lines = execute(currentProcessesCommand);
        for (final String line : lines) {
            if (StringKit.isBlank(line)) {
                continue;
            }
            Logger.trace(
                    "Checking if process line matches the process line regex\nProcess line: {}", line);
            final Matcher lineMatcher = processLinePattern.matcher(line);
            if (lineMatcher.matches()) {
                final String pid = lineMatcher.group("Pid");
                final String commandLine = lineMatcher.group("CommanLine");

                Logger.trace(
                        "Line matches!\n"
                                + "pid: {}; Command line: {}\n"
                                + "Checking if this command line matches the office command line regex",
                        pid,
                        commandLine);

                final Matcher commandMatcher = commandPattern.matcher(commandLine);
                if (commandMatcher.find()) {
                    Logger.debug("Command line matches! Returning pid: {}", pid);
                    return Long.parseLong(pid);
                }
            }
        }
        return Builder.PID_NOT_FOUND;
    }

    /**
     * 获取要执行的命令，以获取由指定参数(进程)标识的所有运行进程的快照.
     *
     * @param process 要查询的进程的名称.
     * @return 包含要调用的命令及其参数的数组.
     */
    protected abstract String[] getRunningProcessesCommand(String process);

    /**
     * 获取用于匹配包含有关正在运行的进程的信息的输出行的模式.
     * 根据此模式测试的输出行是执行getRunningProcessesCommand函数返回的命令的结果.
     *
     * @return 表达式信息.
     * @see #getRunningProcessesCommand(String)
     */
    protected abstract Pattern getRunningProcessLinePattern();

}
