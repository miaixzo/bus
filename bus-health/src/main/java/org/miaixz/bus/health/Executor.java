/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Platform;

/**
 * 用于在命令行上执行命令并返回执行结果的类。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Executor {

    /** 默认环境变量设置，用于确保命令输出使用标准语言格式 */
    private static final String[] DEFAULT_ENV = getDefaultEnv();

    /**
     * 获取默认环境变量设置。
     *
     * @return Windows 系统返回 {"LANGUAGE=C"}，其他系统返回 {"LC_ALL=C"}
     */
    private static String[] getDefaultEnv() {
        if (Platform.isWindows()) {
            return new String[] { "LANGUAGE=C" };
        } else {
            return new String[] { "LC_ALL=C" };
        }
    }

    /**
     * 在本地命令行上执行命令并返回结果。这是一个便捷方法，用于调用 {@link java.lang.Runtime#exec(String)} 并捕获结果输出为字符串列表。在 Windows 上，
     * 与可执行程序无关的内置命令可能需要在命令前添加 {@code cmd.exe /c}。
     *
     * @param cmdToRun 要运行的命令
     * @return 表示命令结果的字符串列表，如果命令失败则返回空列表
     */
    public static List<String> runNative(String cmdToRun) {
        String[] cmd = cmdToRun.split(Symbol.SPACE);
        return runNative(cmd);
    }

    /**
     * 在本地命令行上执行命令并逐行返回结果。这是一个便捷方法，用于调用 {@link java.lang.Runtime#exec(String[])} 并捕获结果输出为字符串列表。在 Windows 上，
     * 与可执行程序无关的内置命令可能需要在数组前添加字符串 {@code cmd.exe} 和 {@code /c}。
     *
     * @param cmdToRunWithArgs 要运行的命令及其参数，以数组形式
     * @return 表示命令结果的字符串列表，如果命令失败则返回空列表
     */
    public static List<String> runNative(String[] cmdToRunWithArgs) {
        return runNative(cmdToRunWithArgs, DEFAULT_ENV);
    }

    /**
     * 在本地命令行上执行命令并逐行返回结果。这是一个便捷方法，用于调用 {@link java.lang.Runtime#exec(String[])} 并捕获结果输出为字符串列表。在 Windows 上，
     * 与可执行程序无关的内置命令可能需要在数组前添加字符串 {@code cmd.exe} 和 {@code /c}。
     *
     * @param cmdToRunWithArgs 要运行的命令及其参数，以数组形式
     * @param envp             环境变量设置的字符串数组，每个元素格式为 name=value， 如果为 null，则子进程继承当前进程的环境
     * @return 表示命令结果的字符串列表，如果命令失败则返回空列表
     */
    public static List<String> runNative(String[] cmdToRunWithArgs, String[] envp) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs, envp);
            return getProcessOutput(p, cmdToRunWithArgs);
        } catch (SecurityException | IOException e) {
            Logger.trace("Couldn't run command {}: {}", Arrays.toString(cmdToRunWithArgs), e.getMessage());
        } finally {
            // 确保所有资源被释放
            if (p != null) {
                // Windows 和 Solaris 在 destroy 时不会关闭描述符，
                // 因此必须单独处理
                if (Platform.isWindows() || Platform.isSolaris()) {
                    try {
                        p.getOutputStream().close();
                    } catch (IOException e) {
                        // 失败时不做任何操作
                    }
                    try {
                        p.getInputStream().close();
                    } catch (IOException e) {
                        // 失败时不做任何操作
                    }
                    try {
                        p.getErrorStream().close();
                    } catch (IOException e) {
                        // 失败时不做任何操作
                    }
                }
                p.destroy();
            }
        }
        return Collections.emptyList();
    }

    /**
     * 从进程获取输出并存储为字符串列表。
     *
     * @param p   运行的进程
     * @param cmd 执行的命令数组
     * @return 进程输出的字符串列表
     */
    private static List<String> getProcessOutput(Process p, String[] cmd) {
        ArrayList<String> sa = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        } catch (IOException e) {
            Logger.trace("Problem reading output from {}: {}", Arrays.toString(cmd), e.getMessage());
        } catch (InterruptedException ie) {
            Logger.trace("Interrupted while reading output from {}: {}", Arrays.toString(cmd), ie.getMessage());
            Thread.currentThread().interrupt();
        }
        return sa;
    }

    /**
     * 返回指定命令的第一行响应。
     *
     * @param cmd2launch 要启动的命令
     * @return 响应字符串，如果命令失败则返回空字符串
     */
    public static String getFirstAnswer(String cmd2launch) {
        return getAnswerAt(cmd2launch, 0);
    }

    /**
     * 返回运行指定命令后指定行索引（基于 0）的响应。
     *
     * @param cmd2launch 要启动的命令
     * @param answerIdx  命令响应中的行索引
     * @return 响应中的整行，如果索引无效或命令运行失败则返回空字符串
     */
    public static String getAnswerAt(String cmd2launch, int answerIdx) {
        List<String> sa = Executor.runNative(cmd2launch);

        if (answerIdx >= 0 && answerIdx < sa.size()) {
            return sa.get(answerIdx);
        }
        return Normal.EMPTY;
    }

}