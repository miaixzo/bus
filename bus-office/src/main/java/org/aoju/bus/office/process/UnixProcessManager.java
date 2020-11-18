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
package org.aoju.bus.office.process;

import org.aoju.bus.core.toolkit.ArrayKit;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Unix系统的{@link ProcessManager}实现
 * 使用{@code ps}和{@code kill}命令
 * 适用于Linux。除了{@code ps}返回的命令行字符串被限制为80个字符之外，
 * 这也适用于Solaris，这将影响{@link #find(ProcessQuery)}
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class UnixProcessManager extends AbstractProcessManager {

    private static final Pattern PS_OUTPUT_LINE =
            Pattern.compile("^\\s*(?<Pid>\\d+)\\s+(?<CommanLine>.*)$");

    private String[] runAsArgs;

    /**
     * 获取{@code UnixProcessManager}的默认实例
     *
     * @return 默认的{@code UnixProcessManager}实例
     */
    public static UnixProcessManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    @Override
    protected List<String> execute(final String[] cmdarray) throws IOException {

        if (runAsArgs == null) {
            return super.execute(cmdarray);
        }

        final String[] newarray = new String[runAsArgs.length + cmdarray.length];
        System.arraycopy(runAsArgs, 0, newarray, 0, runAsArgs.length);
        System.arraycopy(cmdarray, 0, newarray, runAsArgs.length, cmdarray.length);

        return super.execute(newarray);
    }

    @Override
    protected String[] getRunningProcessesCommand(final String process) {
        return new String[]{
                "/bin/sh", "-c", "/bin/ps -e -o pid,args | /bin/grep " + process + " | /bin/grep -v grep"
        };
    }

    @Override
    protected Pattern getRunningProcessLinePattern() {
        return PS_OUTPUT_LINE;
    }

    @Override
    public void kill(final Process process, final long pid) throws IOException {
        execute(new String[]{"/bin/kill", "-KILL", String.valueOf(pid)});
    }

    /**
     * 设置sudo命令参数
     *
     * @param runAsArgs sudo命令参数
     */
    public void setRunAsArgs(final String[] runAsArgs) {
        this.runAsArgs = ArrayKit.clone(runAsArgs);
    }

    private static class DefaultHolder {
        static final UnixProcessManager INSTANCE = new UnixProcessManager();
    }

}
