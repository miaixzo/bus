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

import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * 列表节点 [num0,num1,num2...]模式或者['key0','key1']模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListNode implements Node {

    final List<String> names;

    /**
     * 列表节点
     *
     * @param expression 表达式
     */
    public ListNode(final String expression) {
        this.names = CharsBacker.splitTrim(expression, Symbol.COMMA);
    }

    /**
     * 获取列表中的name，不去除单引号
     *
     * @return name列表
     */
    public String[] getNames() {
        return this.names.toArray(new String[0]);
    }

    /**
     * 将列表中的name，去除单引号
     *
     * @return 处理后的name列表
     */
    public String[] getUnWrappedNames() {
        final String[] unWrappedNames = new String[names.size()];
        for (int i = 0; i < unWrappedNames.length; i++) {
            unWrappedNames[i] = CharsBacker.unWrap(names.get(i), Symbol.C_SINGLE_QUOTE);
        }

        return unWrappedNames;
    }

    @Override
    public String toString() {
        return this.names.toString();
    }

}