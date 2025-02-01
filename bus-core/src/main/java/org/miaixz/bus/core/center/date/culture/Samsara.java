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
package org.miaixz.bus.core.center.date.culture;

/**
 * 轮回的信息 季度/月/星期/日/小时/分钟等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Samsara extends Loops {

    /**
     * 名称列表
     */
    protected String[] names;

    /**
     * 索引，从0开始
     */
    protected int index;

    /**
     * 通过索引初始化
     *
     * @param names 名称列表
     * @param index 索引，支持负数，自动轮转
     */
    protected Samsara(String[] names, int index) {
        this.names = names;
        this.index = indexOf(index);
    }

    /**
     * 通过名称初始化
     *
     * @param names 名称列表
     * @param name  名称
     */
    protected Samsara(String[] names, String name) {
        this.names = names;
        this.index = indexOf(name);
    }

    /**
     * 名称
     *
     * @return 名称
     */
    public String getName() {
        return names[index];
    }

    /**
     * 索引
     *
     * @return 索引，从0开始
     */
    public int getIndex() {
        return index;
    }

    /**
     * 数量
     *
     * @return 数量
     */
    public int getSize() {
        return names.length;
    }

    /**
     * 名称对应的索引
     *
     * @param name 名称
     * @return 索引，从0开始
     */
    protected int indexOf(String name) {
        for (int i = 0, j = getSize(); i < j; i++) {
            if (names[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException(String.format("illegal name: %s", name));
    }

    /**
     * 转换为不超范围的索引
     *
     * @param index 索引
     * @return 索引，从0开始
     */
    protected int indexOf(int index) {
        return indexOf(index, getSize());
    }

    /**
     * 推移后的索引
     *
     * @param n 推移步数
     * @return 索引，从0开始
     */
    protected int nextIndex(int n) {
        return indexOf(index + n);
    }

    /**
     * 到目标索引的步数
     *
     * @param targetIndex 目标索引
     * @return 步数
     */
    public int stepsTo(int targetIndex) {
        return indexOf(targetIndex - index);
    }

}
