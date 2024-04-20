/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.image.element;

import org.miaixz.bus.core.lang.Scale;

/**
 * 合并元素抽象类
 *
 * @param <T> 对象泛型
 * @author Kimi Liu
 * @since Java 17+
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
    /**
     * 绘制方向
     */
    private Scale.Direction direction = Scale.Direction.LEFT_RIGHT;
    /**
     * 平铺
     */
    private boolean repeat;
    /**
     * 平铺水平间距
     */
    private int horizontal;
    /**
     * 平铺垂直间距
     */
    private int vertical;

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

    public Scale.Direction getDirection() {
        return direction;
    }

    public T setDirection(Scale.Direction direction) {
        this.direction = direction;
        return (T) this;
    }

    public float getAlpha() {
        return alpha;
    }

    public T setAlpha(float alpha) {
        this.alpha = alpha;
        return (T) this;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public T setRepeat(boolean repeat) {
        this.repeat = repeat;
        return (T) this;
    }

    public T setRepeat(boolean repeat, int padding) {
        this.repeat = repeat;
        this.horizontal = padding;
        this.vertical = padding;
        return (T) this;
    }

    public T setRepeat(boolean repeat, int horizontal, int vertical) {
        this.repeat = repeat;
        this.horizontal = horizontal;
        this.vertical = vertical;
        return (T) this;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public int getVertical() {
        return vertical;
    }

}
