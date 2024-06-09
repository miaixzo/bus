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
package org.miaixz.bus.core.lang.ansi;

import org.miaixz.bus.core.lang.Assert;

import java.awt.*;
import java.awt.color.ColorSpace;

/**
 * 表示以 LAB 形式存储的颜色
 * <ul>
 *     <li>L: 亮度</li>
 *     <li>a: 正数代表红色，负端代表绿色</li>
 *     <li>b: 正数代表黄色，负端代表蓝色</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LabColor {

    private static final ColorSpace XYZ_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    /**
     * L: 亮度
     */
    private final double l;
    /**
     * A: 正数代表红色，负端代表绿色
     */
    private final double a;
    /**
     * B: 正数代表黄色，负端代表蓝色
     */
    private final double b;

    /**
     * 构造
     *
     * @param rgb RGB颜色
     */
    public LabColor(final Integer rgb) {
        this((rgb != null) ? new Color(rgb) : null);
    }

    /**
     * 构造
     *
     * @param color {@link Color}
     */
    public LabColor(final Color color) {
        Assert.notNull(color, "Ansi4BitColor must not be null");
        final float[] lab = fromXyz(color.getColorComponents(XYZ_COLOR_SPACE, null));
        this.l = lab[0];
        this.a = lab[1];
        this.b = lab[2];
    }

    /**
     * 从xyz换算
     * L=116f(y)-16
     * a=500[f(x/0.982)-f(y)]
     * b=200[f(y)-f(z/1.183 )]
     * 其中： f(x)=7.787x+0.138, x<0.008856; f(x)=(x)1/3,x>0.008856
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return Lab
     */
    private static float[] fromXyz(final float x, final float y, final float z) {
        final double l = (f(y) - 16.0) * 116.0;
        final double a = (f(x) - f(y)) * 500.0;
        final double b = (f(y) - f(z)) * 200.0;
        return new float[]{(float) l, (float) a, (float) b};
    }

    private static double f(final double t) {
        return (t > (216.0 / 24389.0)) ? Math.cbrt(t) : (1.0 / 3.0) * Math.pow(29.0 / 6.0, 2) * t + (4.0 / 29.0);
    }

    /**
     * 获取颜色差
     *
     * @param other 其他Lab颜色
     * @return 颜色差
     */
    // See https://en.wikipedia.org/wiki/Color_difference#CIE94
    public double getDistance(final LabColor other) {
        final double c1 = Math.sqrt(this.a * this.a + this.b * this.b);
        final double deltaC = c1 - Math.sqrt(other.a * other.a + other.b * other.b);
        final double deltaA = this.a - other.a;
        final double deltaB = this.b - other.b;
        final double deltaH = Math.sqrt(Math.max(0.0, deltaA * deltaA + deltaB * deltaB - deltaC * deltaC));
        return Math.sqrt(Math.max(0.0, Math.pow((this.l - other.l), 2)
                + Math.pow(deltaC / (1 + 0.045 * c1), 2) + Math.pow(deltaH / (1 + 0.015 * c1), 2.0)));
    }

    private float[] fromXyz(final float[] xyz) {
        return fromXyz(xyz[0], xyz[1], xyz[2]);
    }

}
