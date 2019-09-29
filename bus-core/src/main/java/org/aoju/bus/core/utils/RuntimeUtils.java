package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统运行时工具类
 * 用于执行系统命令的工具
 *
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public class RuntimeUtils {

    /**
     * 执行系统命令，使用系统默认编码
     *
     * @param cmds 命令列表，每个元素代表一条命令
     * @return 执行结果
     * @throws InstrumentException IO异常
     */
    public static String execForStr(String... cmds) throws InstrumentException {
        return execForStr(CharsetUtils.systemCharset(), cmds);
    }

    /**
     * 执行系统命令，使用系统默认编码
     *
     * @param charset 编码
     * @param cmds    命令列表，每个元素代表一条命令
     * @return 执行结果
     * @throws InstrumentException 内部处理异常
     * @since 3.1.2
     */
    public static String execForStr(Charset charset, String... cmds) throws InstrumentException {
        return getResult(exec(cmds), charset);
    }

    /**
     * 执行系统命令，使用系统默认编码
     *
     * @param cmds 命令列表，每个元素代表一条命令
     * @return 执行结果，按行区分
     * @throws InstrumentException 内部处理异常
     */
    public static List<String> execForLines(String... cmds) throws InstrumentException {
        return execForLines(CharsetUtils.systemCharset(), cmds);
    }

    /**
     * 执行系统命令，使用系统默认编码
     *
     * @param charset 编码
     * @param cmds    命令列表，每个元素代表一条命令
     * @return 执行结果，按行区分
     * @throws InstrumentException 内部处理异常
     * @since 3.1.2
     */
    public static List<String> execForLines(Charset charset, String... cmds) throws InstrumentException {
        return getResultLines(exec(cmds), charset);
    }

    /**
     * 执行命令<br>
     * 命令带参数时参数可作为其中一个参数，也可以将命令和参数组合为一个字符串传入
     *
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String... cmds) {
        if (ArrayUtils.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringUtils.isBlank(cmd)) {
                throw new NullPointerException("Command is empty !");
            }
            cmds = StringUtils.splitToArray(cmd, Symbol.C_SPACE);
        }

        Process process;
        try {
            process = new ProcessBuilder(cmds).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return process;
    }

    /**
     * 执行命令<br>
     * 命令带参数时参数可作为其中一个参数，也可以将命令和参数组合为一个字符串传入
     *
     * @param envp 环境变量参数，传入形式为key=value，null表示继承系统环境变量
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String[] envp, String... cmds) {
        return exec(envp, null, cmds);
    }

    /**
     * 执行命令<br>
     * 命令带参数时参数可作为其中一个参数，也可以将命令和参数组合为一个字符串传入
     *
     * @param envp 环境变量参数，传入形式为key=value，null表示继承系统环境变量
     * @param dir  执行命令所在目录（用于相对路径命令执行），null表示使用当前进程执行的目录
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String[] envp, File dir, String... cmds) {
        if (ArrayUtils.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringUtils.isBlank(cmd)) {
                throw new NullPointerException("Command is empty !");
            }
            cmds = StringUtils.splitToArray(cmd, Symbol.C_SPACE);
        }
        try {
            return Runtime.getRuntime().exec(cmds, envp, dir);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取命令执行结果，使用系统默认编码，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static List<String> getResultLines(Process process) {
        return getResultLines(process, CharsetUtils.systemCharset());
    }

    /**
     * 获取命令执行结果，使用系统默认编码，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     * @since 3.1.2
     */
    public static List<String> getResultLines(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoUtils.readLines(in, charset, new ArrayList<String>());
        } finally {
            IoUtils.close(in);
            destroy(process);
        }
    }

    /**
     * 获取命令执行结果，使用系统默认编码，，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     * @since 3.1.2
     */
    public static String getResult(Process process) {
        return getResult(process, CharsetUtils.systemCharset());
    }

    /**
     * 获取命令执行结果，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     * @since 3.1.2
     */
    public static String getResult(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoUtils.read(in, charset);
        } finally {
            IoUtils.close(in);
            destroy(process);
        }
    }

    /**
     * 获取命令执行异常结果，使用系统默认编码，，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static String getErrorResult(Process process) {
        return getErrorResult(process, CharsetUtils.systemCharset());
    }

    /**
     * 获取命令执行异常结果，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     */
    public static String getErrorResult(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getErrorStream();
            return IoUtils.read(in, charset);
        } finally {
            IoUtils.close(in);
            destroy(process);
        }
    }

    /**
     * 销毁进程
     *
     * @param process 进程
     * @since 3.1.2
     */
    public static void destroy(Process process) {
        if (null != process) {
            process.destroy();
        }
    }

    /**
     * 增加一个JVM关闭后的钩子，用于在JVM关闭时执行某些操作
     *
     * @param hook 钩子
     */
    public static void addShutdownHook(Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

}
