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
package org.miaixz.bus.office.excel.cell;

import java.io.Serializable;
import java.util.Objects;

/**
 * 单元格位置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellLocation implements Serializable {

    private static final long serialVersionUID = -1L;

    private int x;
    private int y;

    /**
     * 构造
     *
     * @param x 列号，从0开始
     * @param y 行号，从0开始
     */
    public CellLocation(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 获取x（列）号
     *
     * @return x（列）号
     */
    public int getX() {
        return x;
    }

    /**
     * 设置x（列）号
     *
     * @param x x（列）号
     */
    public void setX(final int x) {
        this.x = x;
    }

    /**
     * 获取y（行）号
     *
     * @return y（行）号
     */
    public int getY() {
        return y;
    }

    /**
     * 设置y（行）号
     *
     * @param y y（行）号
     */
    public void setY(final int y) {
        this.y = y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CellLocation that = (CellLocation) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "CellLocation{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

}
