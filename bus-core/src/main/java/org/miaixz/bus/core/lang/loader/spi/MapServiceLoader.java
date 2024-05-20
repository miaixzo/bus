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
package org.miaixz.bus.core.lang.loader.spi;

import org.miaixz.bus.core.cache.SimpleCache;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * 键值对服务加载器，使用{@link Properties}加载并存储服务
 * 服务文件默认位于"META-INF/bus/"下，文件名为服务接口类全名。
 * 内容类似于：
 * <pre>
 *     # 我是注释
 *     service1 = service.Service1
 *     service2 = service.Service2
 * </pre>
 * 通过调用{@link #getService(String)}方法，传入等号前的名称，即可获取对应服务。
 *
 * @param <S> 服务类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapServiceLoader<S> extends AbsServiceLoader<S> {

    // TODO update const
    private static final String PREFIX = Normal.META_INF + "/bus/";
    private final SimpleCache<String, S> serviceCache;
    private Properties serviceProperties;

    /**
     * 构造
     *
     * @param pathPrefix   路径前缀
     * @param serviceClass 服务名称
     * @param classLoader  自定义类加载器, {@code null}表示使用默认当前的类加载器
     * @param charset      编码，默认UTF-8
     */
    public MapServiceLoader(final String pathPrefix, final Class<S> serviceClass,
                            final ClassLoader classLoader, final Charset charset) {
        super(pathPrefix, serviceClass, classLoader, charset);

        this.serviceCache = new SimpleCache<>(new HashMap<>());
        load();
    }

    /**
     * 构建KVServiceLoader
     *
     * @param <S>          服务类型
     * @param serviceClass 服务名称
     * @return KVServiceLoader
     */
    public static <S> MapServiceLoader<S> of(final Class<S> serviceClass) {
        return of(serviceClass, null);
    }

    /**
     * 构建KVServiceLoader
     *
     * @param <S>          服务类型
     * @param serviceClass 服务名称
     * @param classLoader  自定义类加载器, {@code null}表示使用默认当前的类加载器
     * @return KVServiceLoader
     */
    public static <S> MapServiceLoader<S> of(final Class<S> serviceClass, final ClassLoader classLoader) {
        return of(PREFIX, serviceClass, classLoader);
    }

    /**
     * 构建KVServiceLoader
     *
     * @param <S>          服务类型
     * @param pathPrefix   路径前缀
     * @param serviceClass 服务名称
     * @param classLoader  自定义类加载器, {@code null}表示使用默认当前的类加载器
     * @return KVServiceLoader
     */
    public static <S> MapServiceLoader<S> of(final String pathPrefix, final Class<S> serviceClass,
                                             final ClassLoader classLoader) {
        return new MapServiceLoader<>(pathPrefix, serviceClass, classLoader, null);
    }

    /**
     * 加载或重新加载全部服务
     */
    @Override
    public void load() {
        // 解析同名的所有service资源
        // 按照资源加载优先级，先加载和解析的资源优先使用，后加载的同名资源丢弃
        final Properties properties = new Properties();
        ResourceKit.loadAllTo(
                properties,
                pathPrefix + serviceClass.getName(),
                classLoader,
                charset,
                // 非覆盖模式
                false);
        this.serviceProperties = properties;
    }

    @Override
    public int size() {
        return this.serviceProperties.size();
    }

    @Override
    public List<String> getServiceNames() {
        return ListKit.view(this.serviceCache.keys());
    }

    @Override
    public Class<S> getServiceClass(final String serviceName) {
        final String serviceClassName = this.serviceProperties.getProperty(serviceName);
        if (StringKit.isBlank(serviceClassName)) {
            return null;
        }

        return ClassKit.loadClass(serviceClassName);
    }

    @Override
    public S getService(final String serviceName) {
        return this.serviceCache.get(serviceName, () -> createService(serviceName));
    }

    @Override
    public Iterator<S> iterator() {
        return new Iterator<S>() {
            private final Iterator<String> nameIter =
                    serviceProperties.stringPropertyNames().iterator();

            @Override
            public boolean hasNext() {
                return nameIter.hasNext();
            }

            @Override
            public S next() {
                return getService(nameIter.next());
            }
        };
    }

    /**
     * 创建服务，无缓存
     *
     * @param serviceName 服务名称
     * @return 服务对象
     */
    private S createService(final String serviceName) {
        return ReflectKit.newInstance(getServiceClass(serviceName));
    }

}
