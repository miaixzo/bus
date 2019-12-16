/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.office.process;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 用于Windows的{@link ProcessManager}实现.
 * 需要wmic.exe和taskkill.exe，至少在Windows XP、Windows Vista和Windows 7上可用(家庭版除外)
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class WindowsProcessManager extends AbstractProcessManager {

    private static final Pattern PROCESS_GET_LINE =
            Pattern.compile("^\\s*(?<CommanLine>.*?)\\s+(?<Pid>\\d+)\\s*$");

    /**
     * 获取{@code WindowsProcessManager}的默认实例.
     *
     * @return 默认的{@code WindowsProcessManager}实例.
     */
    public static WindowsProcessManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    @Override
    protected String[] getRunningProcessesCommand(final String process) {
        return new String[]{
                "cmd", "/c", "wmic process where(name like '" + process + "%') get commandline,processid"
        };
    }

    @Override
    protected Pattern getRunningProcessLinePattern() {
        return PROCESS_GET_LINE;
    }

    /**
     * 获取需要的命令是否对Windows操作系统可用.
     *
     * @return {@code true}如果需要的命令可用，{@code false}否则.
     */
    public boolean isUsable() {
        try {
            execute(new String[]{"wmic", "quit"});
            execute(new String[]{"taskkill", "/?"});
            return true;
        } catch (IOException ioEx) {
            return false;
        }
    }

    @Override
    public void kill(final Process process, final long pid) throws IOException {
        execute(new String[]{"taskkill", "/t", "/f", "/pid", String.valueOf(pid)});
    }

    private static class DefaultHolder {
        static final WindowsProcessManager INSTANCE = new WindowsProcessManager();
    }

}
