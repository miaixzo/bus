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
package org.miaixz.bus.core.bean.path;

/**
 * BeanPath节点对应的Bean工厂，提供Bean的创建、获取和设置接口
 *
 * @param <T> Bean类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NodeBeanFactory<T> {

    /**
     * 创建Bean beanPath对应当前的路径，即如果父对象为：a，则beanPath为：a.b，则创建的Bean为：a.b.c对应的Bean对象
     * 给定的a一定存在，但是本路径中b对应的Bean不存在，则创建的对象是b的值，这个值用c表示
     *
     * @param parent   父Bean
     * @param beanPath 当前路径
     * @return Bean
     */
    T create(final T parent, final BeanPath<T> beanPath);

    /**
     * 获取Bean对应节点的值
     *
     * @param bean     bean对象
     * @param beanPath 当前路径
     * @return 节点值
     */
    Object getValue(T bean, final BeanPath<T> beanPath);

    /**
     * 设置节点值
     *
     * @param bean     bean对象
     * @param value    节点值
     * @param beanPath 当前路径
     * @return bean对象。如果在原Bean对象基础上设置值，返回原Bean，否则返回新的Bean
     */
    T setValue(T bean, Object value, final BeanPath<T> beanPath);

}
