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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * 启用框架扩展和替换组件,服务提供发现机制, 实现方制定接口并完成对接口的实现
 *
 * <pre>
 *   1.支持自定义实现类为单例/多例
 *   2.支持设置默认的实现类
 *   3.支持实现类order排序
 *   4.支持实现类定义特征属性category，用于区分多维度的不同类别
 *   5.支持根据category属性值来搜索实现类
 *   6.支持自动扫描实现类
 *   7.支持手动添加实现类
 *   8.支持获取所有实现类
 *   9.支持只创建所需实现类，解决JDK原生的全量方式
 *   10.支持自定义ClassLoader来加载class
 *
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SPI {

    /**
     * 默认实现ID
     *
     * @return 标识
     */
    String value() default Normal.EMPTY;

    /**
     * 声明每次获取实现类时是否需要创建 新对象，也就是说，是否为单例对象
     *
     * @return 是否单例
     */
    boolean single() default false;

}
