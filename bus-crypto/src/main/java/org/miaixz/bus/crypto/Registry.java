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
package org.miaixz.bus.crypto;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.crypto.metric.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统中内置的策略映射 注解和实现之间映射
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Registry {

    /**
     * 组件信息
     */
    private static Map<String, Provider> ALGORITHM_CACHE = new ConcurrentHashMap<>();

    static {
        register(Algorithm.AES.getValue(), new AESProvider());
        register(Algorithm.DES.getValue(), new DESProvider());
        register(Algorithm.RC4.getValue(), new RC4Provider());
        register(Algorithm.RSA.getValue(), new RSAProvider());
        register(Algorithm.SM2.getValue(), new SM2Provider());
        register(Algorithm.SM4.getValue(), new SM4Provider());
    }

    /**
     * 注册组件
     *
     * @param name   组件名称
     * @param object 组件对象
     */
    public static void register(String name, Provider object) {
        if (ALGORITHM_CACHE.containsKey(name)) {
            throw new InternalException("Repeat registration of components with the same name：" + name);
        }
        Class<?> clazz = object.getClass();
        if (ALGORITHM_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InternalException("Repeat registration of components with the same name：" + clazz);
        }
        ALGORITHM_CACHE.putIfAbsent(name, object);
    }

    /**
     * 生成脱敏工具
     *
     * @param name 模型
     * @return the object
     */
    public static Provider require(String name) {
        Provider object = ALGORITHM_CACHE.get(name);
        if (ObjectKit.isEmpty(object)) {
            throw new IllegalArgumentException("None provider be found!, type:" + name);
        }
        return object;
    }

    /**
     * 是否包含指定名称算法
     *
     * @param name 组件名称
     * @return true：包含, false：不包含
     */
    public boolean contains(String name) {
        return ALGORITHM_CACHE.containsKey(name);
    }

}
