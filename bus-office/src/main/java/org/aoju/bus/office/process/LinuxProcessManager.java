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

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.office.support.ProcessQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ProcessManager} implementation for Linux.
 * Should Work on Solaris too, except that the command line string
 * returned by <tt>ps</tt> there is limited to 80 characters and this affects
 *
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public class LinuxProcessManager implements ProcessManager {

    private static final Pattern PS_OUTPUT_LINE = Pattern.compile("^\\s*(\\d+)\\s+(.*)$");

    private String[] runAsArgs;

    public void setRunAsArgs(String... runAsArgs) {
        this.runAsArgs = runAsArgs;
    }

    protected String[] psCommand() {
        return new String[]{"/bin/ps", "-e", "-o", "pid,args"};
    }

    public long findPid(ProcessQuery query) throws IOException {
        String regex = Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument());
        Pattern commandPattern = Pattern.compile(regex);
        for (String line : execute(psCommand())) {
            Matcher lineMatcher = PS_OUTPUT_LINE.matcher(line);
            if (lineMatcher.matches()) {
                String command = lineMatcher.group(2);
                Matcher commandMatcher = commandPattern.matcher(command);
                if (commandMatcher.find()) {
                    return Long.parseLong(lineMatcher.group(1));
                }
            }
        }
        return PID_NOT_FOUND;
    }

    public void kill(Process process, long pid) throws IOException {
        if (pid <= 0) {
            throw new IllegalArgumentException("invalid pid: " + pid);
        }
        execute("/bin/kill", "-KILL", Long.toString(pid));
    }

    private List<String> execute(String... args) throws IOException {
        String[] command;
        if (runAsArgs != null) {
            command = new String[runAsArgs.length + args.length];
            System.arraycopy(runAsArgs, 0, command, 0, runAsArgs.length);
            System.arraycopy(args, 0, command, runAsArgs.length, args.length);
        } else {
            command = args;
        }
        Process process = new ProcessBuilder(command).start();
        return IoUtils.readLines(process.getInputStream(), Charset.DEFAULT_UTF_8, new ArrayList<>());
    }

}
