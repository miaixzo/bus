/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.miaixz.bus.core.lang.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 包装 {@link NodeList} 的{@link Iterator} 此 iterator 不支持 {@link #remove()} 方法。
 *
 * @author Kimi Liu
 * @see NodeList
 * @since Java 17+
 */
public class NodeListIterator implements ResettableIterator<Node> {

    private final NodeList nodeList;
    /**
     * 当前位置索引
     */
    private int index = 0;

    /**
     * 构造, 根据给定{@link NodeList} 创建{@code NodeListIterator}
     *
     * @param nodeList {@link NodeList}，非空
     */
    public NodeListIterator(final NodeList nodeList) {
        this.nodeList = Assert.notNull(nodeList, "NodeList must not be null.");
    }

    @Override
    public boolean hasNext() {
        return nodeList != null && index < nodeList.getLength();
    }

    @Override
    public Node next() {
        if (nodeList != null && index < nodeList.getLength()) {
            return nodeList.item(index++);
        }
        throw new NoSuchElementException("underlying nodeList has no more elements");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method not supported for a NodeListIterator.");
    }

    @Override
    public void reset() {
        this.index = 0;
    }

}
