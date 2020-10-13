/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.core.image.element;

/**
 * 合并元素抽象类
 *
 * @author Kimi Liu
 * @version 6.1.1
 * @since JDK 1.8+
 */
public abstract class AbstractElement<T extends AbstractElement> {

    /**
     * 起始坐标x，相对左上角
     */
    private int x;
    /**
     * 起始坐标y，相对左上角
     */
    private int y;
    /**
     * 是否居中
     */
    private boolean center;
    /**
     * 透明度
     */
    private float alpha = 1.0f;

    public int getX() {
        return x;
    }

    public T setX(int x) {
        this.x = x;
        return (T) this;
    }

    public int getY() {
        return y;
    }

    public T setY(int y) {
        this.y = y;
        return (T) this;
    }

    public boolean isCenter() {
        return center;
    }

    public T setCenter(boolean center) {
        this.center = center;
        return (T) this;
    }

    public float getAlpha() {
        return alpha;
    }

    public T setAlpha(float alpha) {
        this.alpha = alpha;
        return (T) this;
    }

}
