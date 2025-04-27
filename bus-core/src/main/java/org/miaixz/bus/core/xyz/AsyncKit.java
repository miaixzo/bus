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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * {@link CompletableFuture}异步工具类 {@link CompletableFuture} 是 Future 的改进，可以通过传入回调对象，在任务完成后调用之
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsyncKit {

    /**
     * 等待所有任务执行完毕，包裹了异常
     *
     * @param tasks 并行任务
     * @throws UndeclaredThrowableException 未受检异常
     */
    public static void waitAll(final CompletableFuture<?>... tasks) {
        try {
            CompletableFuture.allOf(tasks).get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 等待任意一个任务执行完毕，包裹了异常
     *
     * @param <T>   任务返回值类型
     * @param tasks 并行任务
     * @return 执行结束的任务返回值
     * @throws UndeclaredThrowableException 未受检异常
     */
    public static <T> T waitAny(final CompletableFuture<?>... tasks) {
        try {
            return (T) CompletableFuture.anyOf(tasks).get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取异步任务结果，包裹了异常
     *
     * @param <T>  任务返回值类型
     * @param task 异步任务
     * @return 任务返回值
     * @throws RuntimeException 未受检异常
     */
    public static <T> T get(final CompletableFuture<T> task) {
        try {
            return task.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取所有任务的返回值
     *
     * @param <T>   任务返回值类型
     * @param tasks 任务集合
     * @return 任务结果集合
     */
    public static <T> List<T> allOfGet(final List<CompletableFuture<T>> tasks) {
        Assert.notEmpty(tasks);

        return allOfGet(tasks, null);
    }

    /**
     * 获取所有任务的返回值，重载方法
     *
     * @param <T>   任务返回值类型
     * @param tasks 任务集合
     * @return 任务结果集合
     */
    @SafeVarargs
    public static <T> List<T> allOfGet(final CompletableFuture<T>... tasks) {
        Assert.notEmpty(tasks);

        return allOfGet(Arrays.asList(tasks), null);
    }

    /**
     * 获取所有任务的返回值，可以为异常任务添加异常处理方法
     *
     * @param <T>      任务内返回值的类型
     * @param tasks    任务集合
     * @param eHandler 异常处理方法
     * @return 任务结果集合
     */
    public static <T> List<T> allOfGet(final CompletableFuture<T>[] tasks, final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return allOfGet(Arrays.asList(tasks), eHandler);
    }

    /**
     * 获取所有任务的返回值，可以为异常任务添加异常处理方法，重载方法
     *
     * @param <T>      任务返回值类型
     * @param tasks    任务集合
     * @param eHandler 异常处理方法
     * @return 任务结果集合
     */
    public static <T> List<T> allOfGet(final List<CompletableFuture<T>> tasks, final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return execute(tasks, eHandler, false);
    }

    /**
     * 获取所有任务的返回值，并行执行，重载方法
     *
     * @param <T>   任务返回值类型
     * @param tasks 任务集合
     * @return 任务结果集合
     */
    @SafeVarargs
    public static <T> List<T> parallelAllOfGet(final CompletableFuture<T>... tasks) {
        Assert.notEmpty(tasks);

        return parallelAllOfGet(Arrays.asList(tasks), null);
    }

    /**
     * 获取所有任务的返回值，并行执行
     *
     * @param <T>   任务返回值类型
     * @param tasks 任务集合
     * @return 任务结果集合
     */
    public static <T> List<T> parallelAllOfGet(final List<CompletableFuture<T>> tasks) {
        Assert.notEmpty(tasks);

        return parallelAllOfGet(tasks, null);
    }

    /**
     * 获取所有任务的返回值，并行执行，可以为异常任务添加异常处理方法
     *
     * @param <T>      任务返回值类型
     * @param tasks    任务集合
     * @param eHandler 异常处理方法
     * @return 任务结果集合
     */
    public static <T> List<T> parallelAllOfGet(final CompletableFuture<T>[] tasks,
            final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return parallelAllOfGet(Arrays.asList(tasks), eHandler);
    }

    /**
     * 获取所有任务的返回值，并行执行，可以为异常任务添加异常处理方法，重载方法
     *
     * @param <T>      任务返回值类型
     * @param tasks    任务集合
     * @param eHandler 异常处理方法
     * @return 任务结果集合
     */
    public static <T> List<T> parallelAllOfGet(final List<CompletableFuture<T>> tasks,
            final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return execute(tasks, eHandler, true);
    }

    /**
     * 处理任务集合
     *
     * @param <T>        任务返回值类型
     * @param tasks      任务集合
     * @param eHandler   异常处理方法
     * @param isParallel 是否是并行 {@link Stream}
     * @return 任务结果集合
     */
    private static <T> List<T> execute(final List<CompletableFuture<T>> tasks, final Function<Exception, T> eHandler,
            final boolean isParallel) {
        return StreamKit.of(tasks, isParallel).map(e -> {
            try {
                return e.get();
            } catch (final InterruptedException | ExecutionException ex) {
                if (eHandler != null) {
                    return eHandler.apply(ex);
                } else {
                    throw ExceptionKit.wrapRuntime(ex);
                }
            }
        }).collect(Collectors.toList());
    }

}
