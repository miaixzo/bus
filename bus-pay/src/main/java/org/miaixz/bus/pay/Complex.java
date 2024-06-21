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
package org.miaixz.bus.pay;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.pay.metric.AbstractProvider;

import java.util.Arrays;
import java.util.Optional;

/**
 *
 * 支付平台的API地址的统一接口，提供以下方法：
 * 1) {@link Complex#sandbox()}: 获取沙箱url. 非必须实现接口（部分平台不支持）
 * 2) {@link Complex#service()}: 获取生产url. 必须实现
 * 注：
 * ①、如需通过扩展实现第三方授权，请参考{@link Registry}自行创建对应的枚举类并实现{@link Complex}接口
 * ②、如果不是使用的枚举类，需要单独处理source字段的赋值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Complex {

    /**
     * 根据 url 获取枚举值
     *
     * @param clazz 枚举class
     * @param url   url
     * @param <E>   枚举类
     * @return 枚举值
     */
    static <E extends Enum<?> & Complex> Optional<E> of(Class<E> clazz, String url) {
        return Arrays.stream(clazz.getEnumConstants()).filter(e -> e.method().equals(url)).findFirst();
    }

    /**
     * 是否沙箱环境
     *
     * @return the string
     */
    default boolean isSandbox() {
        return false;
    }

    /**
     * 沙箱环境
     *
     * @return the string
     */
    default String sandbox() {
        return Protocol.HOST_IPV4;
    }

    /**
     * 生产环境
     *
     * @return the string
     */
    default String service() {
        return Protocol.HOST_IPV4;
    }

    /**
     * 获取接口/方法
     *
     * @return the string
     */
    default String method() {
        return HTTP.NONE;
    }

    /**
     * 平台对应的 Provider 实现类，必须继承自 {@link AbstractProvider}
     *
     * @return the class
     */
    default Class<? extends AbstractProvider> getTargetClass() {
        return AbstractProvider.class;
    }

}
