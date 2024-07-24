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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Collection;
import java.util.List;

/**
 * [start:end:step] 模式节点
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RangeNode implements Node {

    private final int start;
    private final int end;
    private final int step;

    /**
     * 构造
     *
     * @param expression 表达式
     */
    public RangeNode(final String expression) {
        final List<String> parts = CharsBacker.splitTrim(expression, Symbol.COLON);
        this.start = Integer.parseInt(parts.get(0));
        this.end = Integer.parseInt(parts.get(1));
        int step = 1;
        if (3 == parts.size()) {
            step = Integer.parseInt(parts.get(2));
        }
        this.step = step;
    }

    @Override
    public Object getValue(final Object bean) {
        if (bean instanceof Collection) {
            return CollKit.sub((Collection<?>) bean, this.start, this.end, this.step);
        } else if (ArrayKit.isArray(bean)) {
            return ArrayKit.sub(bean, this.start, this.end, this.step);
        }

        throw new UnsupportedOperationException("Can not get range value for: " + bean.getClass());
    }

    @Override
    public Object setValue(final Object bean, final Object value) {
        throw new UnsupportedOperationException("Can not set value with step name.");
    }

    @Override
    public String toString() {
        return StringKit.format("[{}:{}:{}]", this.start, this.end, this.step);
    }

}
