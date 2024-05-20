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
package org.miaixz.bus.core.lang.loader.classloader;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源类加载器，可以加载任意类型的资源类
 *
 * @param <T> {@link Resource}接口实现类
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceClassLoader<T extends Resource> extends SecureClassLoader {

    private final Map<String, T> resourceMap;
    /**
     * 缓存已经加载的类
     */
    private final Map<String, Class<?>> cacheClassMap;

    /**
     * 构造
     *
     * @param parentClassLoader 父类加载器，null表示默认当前上下文加载器
     * @param resourceMap       资源map
     */
    public ResourceClassLoader(final ClassLoader parentClassLoader, final Map<String, T> resourceMap) {
        super(ObjectKit.defaultIfNull(parentClassLoader, ClassKit::getClassLoader));
        this.resourceMap = ObjectKit.defaultIfNull(resourceMap, HashMap::new);
        this.cacheClassMap = new HashMap<>();
    }

    /**
     * 增加需要加载的类资源
     *
     * @param resource 资源，可以是文件、流或者字符串
     * @return this
     */
    public ResourceClassLoader<T> addResource(final T resource) {
        this.resourceMap.put(resource.getName(), resource);
        return this;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final Class<?> clazz = cacheClassMap.computeIfAbsent(name, this::defineByName);
        if (clazz == null) {
            return super.findClass(name);
        }
        return clazz;
    }

    /**
     * 从给定资源中读取class的二进制流，然后生成类
     * 如果这个类资源不存在，返回{@code null}
     *
     * @param name 类名
     * @return 定义的类
     */
    private Class<?> defineByName(final String name) {
        final Resource resource = resourceMap.get(name);
        if (null != resource) {
            final byte[] bytes = resource.readBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }

}
