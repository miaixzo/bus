/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org opencv.org and other contributors.         ~
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
package org.opencv.core;

//javadoc:Scalar_
public class Scalar {

    public double val[];

    public Scalar(double v0, double v1, double v2, double v3) {
        val = new double[] { v0, v1, v2, v3 };
    }

    public Scalar(double v0, double v1, double v2) {
        val = new double[] { v0, v1, v2, 0 };
    }

    public Scalar(double v0, double v1) {
        val = new double[] { v0, v1, 0, 0 };
    }

    public Scalar(double v0) {
        val = new double[] { v0, 0, 0, 0 };
    }

    public Scalar(double[] vals) {
        if (vals != null && vals.length == 4)
            val = vals.clone();
        else {
            val = new double[4];
            set(vals);
        }
    }

    public static Scalar all(double v) {
        return new Scalar(v, v, v, v);
    }

    public void set(double[] vals) {
        if (vals != null) {
            val[0] = vals.length > 0 ? vals[0] : 0;
            val[1] = vals.length > 1 ? vals[1] : 0;
            val[2] = vals.length > 2 ? vals[2] : 0;
            val[3] = vals.length > 3 ? vals[3] : 0;
        } else
            val[0] = val[1] = val[2] = val[3] = 0;
    }

    public Scalar clone() {
        return new Scalar(val);
    }

    public Scalar mul(Scalar it, double scale) {
        return new Scalar(val[0] * it.val[0] * scale, val[1] * it.val[1] * scale, val[2] * it.val[2] * scale,
                val[3] * it.val[3] * scale);
    }

    public Scalar mul(Scalar it) {
        return mul(it, 1);
    }

    public Scalar conj() {
        return new Scalar(val[0], -val[1], -val[2], -val[3]);
    }

    public boolean isReal() {
        return val[1] == 0 && val[2] == 0 && val[3] == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + java.util.Arrays.hashCode(val);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Scalar))
            return false;
        Scalar it = (Scalar) obj;
        if (!java.util.Arrays.equals(val, it.val))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + val[0] + ", " + val[1] + ", " + val[2] + ", " + val[3] + "]";
    }

}
