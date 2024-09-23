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
package org.miaixz.bus.core.bean.path.node;

import org.miaixz.bus.core.bean.DynaBean;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 处理名称节点或序号节点，如：
 * <ul>
 * <li>name</li>
 * <li>1</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NameNode implements Node {

    private final String name;

    /**
     * 构造
     *
     * @param name 节点名
     */
    public NameNode(final String name) {
        this.name = name;
    }

    /**
     * 是否为数字节点
     *
     * @return 是否为数字节点
     */
    public boolean isNumber() {
        return MathKit.isInteger(name);
    }

    @Override
    public Object getValue(final Object bean) {
        if (null == bean) {
            return null;
        }
        if (Symbol.DOLLAR.equals(name)) {
            return bean;
        }
        Object value = DynaBean.of(bean).get(this.name);
        if (null == value && StringKit.lowerFirst(ClassKit.getClassName(bean, true)).equals(this.name)) {
            // 如果bean类名与属性名相同，则返回bean本身
            value = bean;
        }
        return value;
    }

    @Override
    public Object setValue(final Object bean, final Object value) {
        return DynaBean.of(bean).set(this.name, value).getBean();
    }

    @Override
    public String toString() {
        return this.name;
    }

}
