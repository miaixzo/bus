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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.io.stream.FastByteOutputStream;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 系统运行时工具类
 * 用于执行系统命令的工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RuntimeKit {

    /**
     * 执行系统命令,使用系统默认编码
     *
     * @param cmds 命令列表,每个元素代表一条命令
     * @return 执行结果
     * @throws InternalException IO异常
     */
    public static String execForString(String... cmds) throws InternalException {
        return execForString(Charset.systemCharset(), cmds);
    }

    /**
     * 执行系统命令，使用传入的 {@link java.nio.charset.Charset charset} 编码
     *
     * @param charset 编码
     * @param cmds    命令列表,每个元素代表一条命令
     * @return 执行结果
     * @throws InternalException 内部处理异常
     */
    public static String execForString(java.nio.charset.Charset charset, String... cmds) throws InternalException {
        return getResult(exec(cmds), charset);
    }

    /**
     * 执行系统命令,使用系统默认编码
     *
     * @param cmds 命令列表,每个元素代表一条命令
     * @return 执行结果, 按行区分
     * @throws InternalException 内部处理异常
     */
    public static List<String> execForLines(String... cmds) throws InternalException {
        return execForLines(Charset.systemCharset(), cmds);
    }

    /**
     * 执行系统命令，使用传入的 {@link java.nio.charset.Charset charset} 编码
     *
     * @param charset 编码
     * @param cmds    命令列表,每个元素代表一条命令
     * @return 执行结果, 按行区分
     * @throws InternalException 内部处理异常
     */
    public static List<String> execForLines(java.nio.charset.Charset charset, String... cmds) throws InternalException {
        return getResultLines(exec(cmds), charset);
    }

    /**
     * 执行命令
     * 命令带参数时参数可作为其中一个参数,也可以将命令和参数组合为一个字符串传入
     *
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String... cmds) {
        if (ArrayKit.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringKit.isBlank(cmd)) {
                throw new NullPointerException("Command is empty !");
            }
            cmds = StringKit.splitToArray(cmd, Symbol.C_SPACE);
        }

        Process process;
        try {
            process = new ProcessBuilder(cmds).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return process;
    }

    /**
     * 执行命令
     * 命令带参数时参数可作为其中一个参数,也可以将命令和参数组合为一个字符串传入
     *
     * @param envp 环境变量参数,传入形式为key=value,null表示继承系统环境变量
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String[] envp, String... cmds) {
        return exec(envp, null, cmds);
    }

    /**
     * 执行命令
     * 命令带参数时参数可作为其中一个参数,也可以将命令和参数组合为一个字符串传入
     *
     * @param envp 环境变量参数,传入形式为key=value,null表示继承系统环境变量
     * @param dir  执行命令所在目录(用于相对路径命令执行),null表示使用当前进程执行的目录
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String[] envp, File dir, String... cmds) {
        if (ArrayKit.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringKit.isBlank(cmd)) {
                throw new NullPointerException("Command is empty !");
            }
            cmds = StringKit.splitToArray(cmd, Symbol.C_SPACE);
        }
        try {
            return Runtime.getRuntime().exec(cmds, envp, dir);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取命令执行结果,使用系统默认编码,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static List<String> getResultLines(Process process) {
        return getResultLines(process, Charset.systemCharset());
    }

    /**
     * 获取命令执行结果，使用传入的 {@link java.nio.charset.Charset charset} 编码，获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     */
    public static List<String> getResultLines(Process process, java.nio.charset.Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoKit.readLines(in, charset, new ArrayList<>());
        } finally {
            IoKit.close(in);
            destroy(process);
        }
    }

    /**
     * 获取命令执行结果,使用系统默认编码,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static String getResult(Process process) {
        return getResult(process, Charset.systemCharset());
    }

    /**
     * 获取命令执行结果,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     */
    public static String getResult(Process process, java.nio.charset.Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoKit.read(in, charset);
        } finally {
            IoKit.close(in);
            destroy(process);
        }
    }

    /**
     * 获取命令执行异常结果,使用系统默认编码,,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static String getErrorResult(Process process) {
        return getErrorResult(process, Charset.systemCharset());
    }

    /**
     * 获取命令执行异常结果,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     */
    public static String getErrorResult(Process process, java.nio.charset.Charset charset) {
        InputStream in = null;
        try {
            in = process.getErrorStream();
            return IoKit.read(in, charset);
        } finally {
            IoKit.close(in);
            destroy(process);
        }
    }

    /**
     * 销毁进程
     *
     * @param process 进程
     */
    public static void destroy(Process process) {
        if (null != process) {
            process.destroy();
        }
    }

    /**
     * 增加一个JVM关闭后的钩子,用于在JVM关闭时执行某些操作
     *
     * @param hook 钩子
     */
    public static void addShutdownHook(Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

    /**
     * 获得JVM可用的处理器数量（一般为CPU核心数）
     * <p>
     * 这里做一个特殊的处理,在特殊的CPU上面，会有获取不到CPU数量的情况，所以这里做一个保护;
     * 默认给一个7，真实的CPU基本都是偶数，方便区分。
     * 如果不做处理，会出现创建线程池时{@link ThreadPoolExecutor}，抛出异常：{@link IllegalArgumentException}
     *
     * @return 可用的处理器数量
     */
    public static int getProcessorCount() {
        int cpu = Runtime.getRuntime().availableProcessors();
        if (cpu <= 0) {
            cpu = 7;
        }
        return cpu;
    }

    /**
     * 获得JVM中剩余的内存数，单位byte
     *
     * @return JVM中剩余的内存数，单位byte
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * 获得JVM已经从系统中获取到的总共的内存数，单位byte
     *
     * @return JVM中剩余的内存数，单位byte
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * 获得JVM中可以从系统中获取的最大的内存数，单位byte，以-Xmx参数为准
     *
     * @return JVM中剩余的内存数，单位byte
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获得JVM最大可用内存，计算方法为：<br>
     * 最大内存-总内存+剩余内存
     *
     * @return 最大可用内存
     */
    public static long getUsableMemory() {
        return getMaxMemory() - getTotalMemory() + getFreeMemory();
    }

    /**
     * 获得当前进程的PID
     * 当失败时返回-1
     *
     * @return the int
     */
    public static int getPid() {
        final String processName = ManagementFactory.getRuntimeMXBean().getName();
        if (StringKit.isBlank(processName)) {
            throw new InternalException("Process name is blank!");
        }
        final int atIndex = processName.indexOf('@');
        if (atIndex > 0) {
            return Integer.parseInt(processName.substring(0, atIndex));
        } else {
            return processName.hashCode();
        }
    }

    /**
     * 返回应用启动到现在的毫秒数
     *
     * @return the long
     */
    public static long getUpTime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * 返回输入的JVM参数列表
     *
     * @return the string
     */
    public static String getVmArguments() {
        List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return CollKit.join(vmArguments, Symbol.SPACE);
    }

    /**
     * 获得完整消息,包括异常名,消息格式为：{SimpleClassName}: {ThrowableMessage}
     *
     * @param e 异常
     * @return 完整消息
     */
    public static String getMessage(Throwable e) {
        if (null == e) {
            return Normal.NULL;
        }
        return StringKit.format("{}: {}", e.getClass().getSimpleName(), e.getMessage());
    }

    /**
     * 获得消息,调用异常类的getMessage方法
     *
     * @param e 异常
     * @return 消息
     */
    public static String getSimpleMessage(Throwable e) {
        return (null == e) ? Normal.NULL : e.getMessage();
    }

    /**
     * 使用运行时异常包装编译异常
     * 如果传入参数已经是运行时异常，则直接返回，不再额外包装
     *
     * @param throwable 异常
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntime(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }

    /**
     * 将指定的消息包装为运行时异常
     *
     * @param message 异常消息
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntime(String message) {
        return new RuntimeException(message);
    }

    /**
     * 包装一个异常
     *
     * @param <T>           被包装的异常类型
     * @param throwable     异常
     * @param wrapThrowable 包装后的异常类
     * @return 包装后的异常
     */
    public static <T extends Throwable> T wrap(Throwable throwable, Class<T> wrapThrowable) {
        if (wrapThrowable.isInstance(throwable)) {
            return (T) throwable;
        }
        return ReflectKit.newInstance(wrapThrowable, throwable);
    }

    /**
     * 包装异常并重新抛出此异常
     * {@link RuntimeException} 和{@link Error} 直接抛出,其它检查异常包装为{@link UndeclaredThrowableException} 后抛出
     *
     * @param throwable 异常
     */
    public static void wrapAndThrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new UndeclaredThrowableException(throwable);
    }

    /**
     * 将消息包装为运行时异常并抛出
     *
     * @param message 异常消息
     */
    public static void wrapRuntimeAndThrow(String message) {
        throw new RuntimeException(message);
    }

    /**
     * 剥离反射引发的InvocationTargetException、
     * UndeclaredThrowableException中间异常,返回业务本身的异常
     *
     * @param wrapped 包装的异常
     * @return 剥离后的异常
     */
    public static Throwable unwrap(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    /**
     * 获取当前栈信息
     *
     * @return 当前栈信息
     */
    public static StackTraceElement[] getStackElements() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * 获取指定层的堆栈信息
     *
     * @param layers 堆栈层级
     * @return 指定层的堆栈信息
     */
    public static StackTraceElement getStackElement(int layers) {
        return getStackElements()[layers];
    }

    /**
     * 获取指定层的堆栈信息
     *
     * @param fqcn   指定类名为基础
     * @param layers 指定类名的类堆栈相对层数
     * @return 指定层的堆栈信息
     */
    public static StackTraceElement getStackElement(String fqcn, int layers) {
        final StackTraceElement[] stackTraceArray = getStackElements();
        final int index = ArrayKit.firstNonAll((ele) -> StringKit.equals(fqcn, ele.getClassName()), stackTraceArray);
        if (index > 0) {
            return stackTraceArray[index + layers];
        }

        return null;
    }

    /**
     * 获取入口堆栈信息
     *
     * @return 入口堆栈信息
     */
    public static StackTraceElement getRootStackElement() {
        final StackTraceElement[] stackElements = getStackElements();
        return stackElements[stackElements.length - 1];
    }

    /**
     * 堆栈转为单行完整字符串
     *
     * @param throwable 异常对象
     * @return 堆栈转为的字符串
     */
    public static String getStackTraceOneLine(Throwable throwable) {
        return getStackTraceOneLine(throwable, 3000);
    }

    /**
     * 堆栈转为单行完整字符串
     *
     * @param throwable 异常对象
     * @param limit     限制最大长度
     * @return 堆栈转为的字符串
     */
    public static String getStackTraceOneLine(Throwable throwable, int limit) {
        Map<Character, String> replaceCharToStrMap = new HashMap<>();
        replaceCharToStrMap.put(Symbol.C_CR, Symbol.SPACE);
        replaceCharToStrMap.put(Symbol.C_LF, Symbol.SPACE);
        replaceCharToStrMap.put(Symbol.C_TAB, Symbol.SPACE);

        return getStackTrace(throwable, limit, replaceCharToStrMap);
    }

    /**
     * 堆栈转为完整字符串
     *
     * @param throwable 异常对象
     * @return 堆栈转为的字符串
     */
    public static String getStackTrace(Throwable throwable) {
        return getStackTrace(throwable, -1);
    }

    /**
     * 堆栈转为完整字符串
     *
     * @param throwable 异常对象
     * @param limit     限制最大长度
     * @return 堆栈转为的字符串
     */
    public static String getStackTrace(Throwable throwable, int limit) {
        return getStackTrace(throwable, limit, null);
    }

    /**
     * 堆栈转为完整字符串
     *
     * @param throwable           异常对象
     * @param limit               限制最大长度，&gt;0表示不限制长度
     * @param replaceCharToStrMap 替换字符为指定字符串
     * @return 堆栈转为的字符串
     */
    public static String getStackTrace(Throwable throwable, int limit, Map<Character, String> replaceCharToStrMap) {
        final FastByteOutputStream baos = new FastByteOutputStream();
        throwable.printStackTrace(new PrintStream(baos));

        final String exceptionStr = baos.toString();
        final int length = exceptionStr.length();
        if (limit < 0 || limit > length) {
            limit = length;
        }

        if (MapKit.isNotEmpty(replaceCharToStrMap)) {
            final StringBuilder sb = StringKit.builder();
            char c;
            String value;
            for (int i = 0; i < limit; i++) {
                c = exceptionStr.charAt(i);
                value = replaceCharToStrMap.get(c);
                if (null != value) {
                    sb.append(value);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            if (limit == length) {
                return exceptionStr;
            }
            return StringKit.subPre(exceptionStr, limit);
        }
    }

    /**
     * 判断是否由指定异常类引起
     *
     * @param throwable    异常
     * @param causeClasses 定义的引起异常的类
     * @return 是否由指定异常类引起
     */
    public static boolean isCausedBy(Throwable throwable, Class<? extends Exception>... causeClasses) {
        return null != getCausedBy(throwable, causeClasses);
    }

    /**
     * 获取由指定异常类引起的异常
     *
     * @param throwable    异常
     * @param causeClasses 定义的引起异常的类
     * @return 是否由指定异常类引起
     */
    public static Throwable getCausedBy(Throwable throwable, Class<? extends Exception>... causeClasses) {
        Throwable cause = throwable;
        while (null != cause) {
            for (Class<? extends Exception> causeClass : causeClasses) {
                if (causeClass.isInstance(cause)) {
                    return cause;
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * 判断指定异常是否来自或者包含指定异常
     *
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @return true 来自或者包含
     */
    public static boolean isFromOrSuppressedThrowable(Throwable throwable, Class<? extends Throwable> exceptionClass) {
        return null != convertFromOrSuppressedThrowable(throwable, exceptionClass, true);
    }

    /**
     * 判断指定异常是否来自或者包含指定异常
     *
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @param checkCause     判断cause
     * @return true 来自或者包含
     */
    public static boolean isFromOrSuppressedThrowable(Throwable throwable, Class<? extends Throwable> exceptionClass, boolean checkCause) {
        return null != convertFromOrSuppressedThrowable(throwable, exceptionClass, checkCause);
    }

    /**
     * 转化指定异常为来自或者包含指定异常
     *
     * @param <T>            异常类型
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @return 结果为null 不是来自或者包含
     */
    public static <T extends Throwable> T convertFromOrSuppressedThrowable(Throwable throwable, Class<T> exceptionClass) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, true);
    }

    /**
     * 转化指定异常为来自或者包含指定异常
     *
     * @param <T>            异常类型
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @param checkCause     判断cause
     * @return 结果为null 不是来自或者包含
     */
    public static <T extends Throwable> T convertFromOrSuppressedThrowable(Throwable throwable, Class<T> exceptionClass, boolean checkCause) {
        if (null == throwable || null == exceptionClass) {
            return null;
        }
        if (exceptionClass.isAssignableFrom(throwable.getClass())) {
            return (T) throwable;
        }
        if (checkCause) {
            Throwable cause = throwable.getCause();
            if (null != cause && exceptionClass.isAssignableFrom(cause.getClass())) {
                return (T) cause;
            }
        }
        Throwable[] throwables = throwable.getSuppressed();
        if (ArrayKit.isNotEmpty(throwables)) {
            for (Throwable throwable1 : throwables) {
                if (exceptionClass.isAssignableFrom(throwable1.getClass())) {
                    return (T) throwable1;
                }
            }
        }
        return null;
    }

    /**
     * 获取异常链上所有异常的集合,如果{@link Throwable} 对象没有cause,返回只有一个节点的List
     * 如果传入null,返回空集合
     *
     * @param throwable 异常对象,可以为null
     * @return 异常链中所有异常集合
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (null != throwable && false == list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * 获取异常链中最尾端的异常,即异常最早发生的异常对象
     * 此方法通过调用{@link Throwable#getCause()} 直到没有cause为止,如果异常本身没有cause,返回异常本身
     * 传入null返回也为null
     *
     * @param throwable 异常对象,可能为null
     * @return 最尾端异常, 传入null参数返回也为null
     */
    public static Throwable getRootCause(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.size() < 1 ? null : list.get(list.size() - 1);
    }

    /**
     * 获取异常链中最尾端的异常的消息,
     * 消息格式为：{SimpleClassName}: {ThrowableMessage}
     *
     * @param th 异常
     * @return 消息
     */
    public static String getRootCauseMessage(final Throwable th) {
        return getMessage(getRootCause(th));
    }

}
