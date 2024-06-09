/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.loader.spi.ListServiceLoader;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;

/**
 * 服务提供接口SPI（Service Provider interface）相关类
 * <p>
 * SPI机制中的服务加载工具类，流程如下
 *
 * <pre>
 *     1、创建接口，并创建实现类
 *     2、ClassPath/META-INF/services下创建与接口全限定类名相同的文件
 *     3、文件内容填写实现类的全限定类名
 * </pre>
 * 相关介绍见：<a href="https://www.jianshu.com/p/3a3edbcd8f24">https://www.jianshu.com/p/3a3edbcd8f24</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SPIKit {

    /**
     * 加载第一个可用服务，如果用户定义了多个接口实现类，只获取第一个不报错的服务
     *
     * @param <S>   服务类型
     * @param clazz 服务接口
     * @return 第一个服务接口实现对象，无实现返回{@code null}
     */
    public static <S> S loadFirstAvailable(final Class<S> clazz) {
        return loadFirstAvailable(loadList(clazz));
    }

    /**
     * 加载第一个可用服务，如果用户定义了多个接口实现类，只获取第一个不报错的服务
     *
     * @param <S>           服务类型
     * @param serviceLoader {@link ServiceLoader}
     * @return 第一个服务接口实现对象，无实现返回{@code null}
     */
    public static <S> S loadFirstAvailable(final ServiceLoader<S> serviceLoader) {
        final Iterator<S> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                return iterator.next();
            } catch (final Throwable ignore) {
                // ignore
            }
        }
        return null;
    }

    /**
     * 加载服务
     *
     * @param <T>   接口类型
     * @param clazz 服务接口
     * @return 服务接口实现列表
     */
    public static <T> ServiceLoader<T> loadList(final Class<T> clazz) {
        return loadList(clazz, null);
    }

    /**
     * 加载服务
     *
     * @param <T>    接口类型
     * @param clazz  服务接口
     * @param loader {@link ClassLoader}
     * @return 服务接口实现列表
     */
    public static <T> ServiceLoader<T> loadList(final Class<T> clazz, final ClassLoader loader) {
        return ListServiceLoader.of(clazz, loader);
    }

    public static class X {

        /**
         * 。加载第一个可用服务，如果用户定义了多个接口实现类，只获取第一个不报错的服务
         *
         * @param <T>   接口类型
         * @param clazz 服务接口
         * @return 第一个服务接口实现对象，无实现返回{@code null}
         */
        public static <T> T loadFirstAvailable(final Class<T> clazz) {
            final Iterator<T> iterator = load(clazz).iterator();
            while (iterator.hasNext()) {
                try {
                    return iterator.next();
                } catch (final ServiceConfigurationError ignore) {
                    // ignore
                }
            }
            return null;
        }

        /**
         * 加载第一个服务，如果用户定义了多个接口实现类，只获取第一个。
         *
         * @param <T>   接口类型
         * @param clazz 服务接口
         * @return 第一个服务接口实现对象，无实现返回{@code null}
         */
        public static <T> T loadFirst(final Class<T> clazz) {
            final Iterator<T> iterator = load(clazz).iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return null;
        }

        /**
         * 加载服务
         *
         * @param <T>   接口类型
         * @param clazz 服务接口
         * @return 服务接口实现列表
         */
        public static <T> java.util.ServiceLoader<T> load(final Class<T> clazz) {
            return load(clazz, null);
        }

        /**
         * 加载服务
         *
         * @param <T>    接口类型
         * @param clazz  服务接口
         * @param loader {@link ClassLoader}
         * @return 服务接口实现列表
         */
        public static <T> java.util.ServiceLoader<T> load(final Class<T> clazz, final ClassLoader loader) {
            return java.util.ServiceLoader.load(clazz, ObjectKit.defaultIfNull(loader, ClassKit::getClassLoader));
        }

        /**
         * 加载服务 并已list列表返回
         *
         * @param <T>   接口类型
         * @param clazz 服务接口
         * @return 服务接口实现列表
         */
        public static <T> List<T> loadList(final Class<T> clazz) {
            return loadList(clazz, null);
        }

        /**
         * 加载服务 并已list列表返回
         *
         * @param <T>    接口类型
         * @param clazz  服务接口
         * @param loader {@link ClassLoader}
         * @return 服务接口实现列表
         */
        public static <T> List<T> loadList(final Class<T> clazz, final ClassLoader loader) {
            return ListKit.of(false, load(clazz, loader));
        }

    }

}
