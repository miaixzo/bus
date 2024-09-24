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

import java.util.Iterator;

import org.miaixz.bus.core.bean.path.node.Node;
import org.miaixz.bus.core.bean.path.node.NodeFactory;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Bean路径表达式，用于获取多层嵌套Bean中的字段值或Bean对象 根据给定的表达式，查找Bean中对应的属性值对象。 表达式分为两种：
 * <ol>
 * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
 * <li>[]表达式，可以获取集合等对象中对应index的值</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanPath<T> implements Iterator<BeanPath<T>> {

    /**
     * 表达式边界符号数组
     */
    private static final char[] EXP_CHARS = { Symbol.C_DOT, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT };

    private final Node node;
    private final String child;
    private final NodeBeanFactory<T> beanFactory;

    /**
     * 构造
     *
     * @param expression  表达式
     * @param beanFactory NodeBean工厂，用于Bean的值创建、获取和设置
     */
    public BeanPath(final String expression, final NodeBeanFactory<T> beanFactory) {
        this.beanFactory = beanFactory;
        final int length = expression.length();
        final StringBuilder builder = new StringBuilder();

        char c;
        boolean isNumStart = false;// 下标标识符开始
        boolean isInWrap = false; // 标识是否在引号内
        for (int i = 0; i < length; i++) {
            c = expression.charAt(i);
            if ('\'' == c) {
                // 结束
                isInWrap = (!isInWrap);
                continue;
            }

            if (!isInWrap && ArrayKit.contains(EXP_CHARS, c)) {
                // 处理边界符号
                if (Symbol.C_BRACKET_RIGHT == c) {
                    // 中括号（数字下标）结束
                    if (!isNumStart) {
                        throw new IllegalArgumentException(
                                StringKit.format("Bad expression '{}':{}, we find ']' but no '[' !", expression, i));
                    }
                    isNumStart = false;
                    // 中括号结束加入下标
                } else {
                    if (isNumStart) {
                        // 非结束中括号情况下发现起始中括号报错（中括号未关闭）
                        throw new IllegalArgumentException(
                                StringKit.format("Bad expression '{}':{}, we find '[' but no ']' !", expression, i));
                    } else if (Symbol.C_BRACKET_LEFT == c) {
                        // 数字下标开始
                        isNumStart = true;
                    }
                    // 每一个边界符之前的表达式是一个完整的KEY，开始处理KEY
                }
                if (builder.length() > 0) {
                    this.node = NodeFactory.createNode(builder.toString());
                    // 如果以[结束，表示后续还有表达式，需保留'['，如name[0]
                    this.child = StringKit.nullIfEmpty(expression.substring(c == Symbol.C_BRACKET_LEFT ? i : i + 1));
                    return;
                }
            } else {
                // 非边界符号，追加字符
                builder.append(c);
            }
        }

        // 最后的节点
        if (isNumStart) {
            throw new IllegalArgumentException(
                    StringKit.format("Bad expression '{}':{}, we find '[' but no ']' !", expression, length - 1));
        } else {
            this.node = NodeFactory.createNode(builder.toString());
            this.child = null;
        }
    }

    /**
     * 创建Bean路径
     *
     * @param expression 表达式
     * @return BeanPath
     */
    public static BeanPath<Object> of(final String expression) {
        return new BeanPath<>(expression, DefaultNodeBeanFactory.INSTANCE);
    }

    /**
     * 创建Bean路径
     *
     * @param expression  表达式
     * @param beanFactory NodeBean工厂，用于Bean的值创建、获取和设置
     * @param <T>         Bean类型
     * @return BeanPath
     */
    public static <T> BeanPath<T> of(final String expression, final NodeBeanFactory<T> beanFactory) {
        return new BeanPath<>(expression, beanFactory);
    }

    /**
     * 获取节点
     *
     * @return 节点
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * 获取子表达式
     *
     * @return 子表达式
     */
    public String getChild() {
        return this.child;
    }

    @Override
    public boolean hasNext() {
        return null != this.child;
    }

    @Override
    public BeanPath<T> next() {
        return new BeanPath<>(this.child, this.beanFactory);
    }

    /**
     * 获取路径对应的值
     *
     * @param bean Bean对象
     * @return 路径对应的值
     */
    public Object getValue(final T bean) {
        final Object value = beanFactory.getValue(bean, this);
        if (null == value) {
            return null;
        }
        if (!hasNext()) {
            return value;
        }
        return next().getValue((T) value);
    }

    /**
     * 设置路径对应的值，如果路径节点为空，自动创建之
     *
     * @param bean  Bean对象
     * @param value 设置的值
     * @return bean 如果在原Bean对象基础上设置值，返回原Bean，否则返回新的Bean
     */
    public Object setValue(final T bean, final Object value) {
        final NodeBeanFactory<T> beanFactory = this.beanFactory;
        if (!hasNext()) {
            // 根节点，直接赋值
            return beanFactory.setValue(bean, value, this);
        }

        final BeanPath<T> childBeanPath = next();
        Object subBean = beanFactory.getValue(bean, this);
        if (null == subBean) {
            subBean = beanFactory.create(bean, this);
            beanFactory.setValue(bean, subBean, this);
            // 如果自定义put方法修改了value，返回修改后的value，避免值丢失
            subBean = beanFactory.getValue(bean, this);
        }
        // 递归逐层查找子节点，赋值
        final Object newSubBean = childBeanPath.setValue((T) subBean, value);
        if (newSubBean != subBean) {
            // 对于数组对象，set新值后，会返回新的数组，此时将新对象再加入父bean中，覆盖旧数组
            beanFactory.setValue(bean, newSubBean, this);
        }
        return bean;
    }

    @Override
    public String toString() {
        return "BeanPath{" + "node=" + node + ", child='" + child + '\'' + '}';
    }

}