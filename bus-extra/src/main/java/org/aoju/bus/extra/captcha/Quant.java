/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.extra.captcha;

/**
 * @author Kimi Liu
 * @version 5.6.6
 * @since JDK 1.8+
 */
public class Quant {
    /**
     * 所用颜色数
     */
    protected static final int netsize = 256;

    /**
     * 4个接近500的质数——假设没有一幅图像的长度大到可以被所有4个质数整除
     */
    protected static final int prime1 = 499;
    protected static final int prime2 = 491;
    protected static final int prime3 = 487;
    protected static final int prime4 = 503;

    protected static final int minpicturebytes = (3 * prime4);

    protected static final int maxnetpos = (netsize - 1);
    protected static final int netbiasshift = 4;
    protected static final int ncycles = 100;

    protected static final int intbiasshift = 16;
    protected static final int intbias = (1 << intbiasshift);
    protected static final int gammashift = 10;
    protected static final int gamma = (1 << gammashift);
    protected static final int betashift = 10;
    protected static final int beta = (intbias >> betashift);
    protected static final int betagamma =
            (intbias << (gammashift - betashift));

    /**
     * 256颜色，半径开始
     */
    protected static final int initrad = (netsize >> 3);
    /**
     * 在32.0偏置6位
     */
    protected static final int radiusbiasshift = 6;
    protected static final int radiusbias = (1 << radiusbiasshift);
    protected static final int initradius = (initrad * radiusbias);
    /**
     * 每循环1/30
     */
    protected static final int radiusdec = 30;

    /**
     * alpha值从1.0开始
     */
    protected static final int alphabiasshift = 10;

    protected static final int initalpha = (1 << alphabiasshift);
    /**
     * radbias和alpharadbias用于radpower计算
     */
    protected static final int radbiasshift = 8;
    protected static final int radbias = (1 << radbiasshift);
    protected static final int alpharadbshift = (alphabiasshift + radbiasshift);
    protected static final int alpharadbias = (1 << alpharadbshift);
    /**
     * 偏移10位
     */
    protected int alphadec;

    /**
     * 输入图像本身
     */
    protected byte[] thepicture;
    /**
     * engthcount = H * W * 3
     */
    protected int lengthcount;

    /**
     * 1 . . 30采样因素
     */
    protected int samplefac;

    /**
     * 网络本身- [netsize][4]
     */
    protected int[][] network;

    /**
     * 用于网络查找——实际上是256
     */
    protected int[] netindex = new int[256];

    protected int[] bias = new int[netsize];
    /**
     * 基本偏差和频率阵列
     */
    protected int[] freq = new int[netsize];
    protected int[] radpower = new int[initrad];

    /**
     * 初始化网络的范围(0,0,0)到(255,255,255)，并设置参数
     *
     * @param thepic 图片
     * @param len    长度
     * @param sample 样品
     */
    public Quant(byte[] thepic, int len, int sample) {

        int i;
        int[] p;

        thepicture = thepic;
        lengthcount = len;
        samplefac = sample;

        network = new int[netsize][];
        for (i = 0; i < netsize; i++) {
            network[i] = new int[4];
            p = network[i];
            p[0] = p[1] = p[2] = (i << (netbiasshift + 8)) / netsize;
            freq[i] = intbias / netsize;
            bias[i] = 0;
        }
    }

    public byte[] colorMap() {
        byte[] map = new byte[3 * netsize];
        int[] index = new int[netsize];
        for (int i = 0; i < netsize; i++) {
            index[network[i][3]] = i;
        }
        int k = 0;
        for (int i = 0; i < netsize; i++) {
            int j = index[i];
            map[k++] = (byte) (network[j][0]);
            map[k++] = (byte) (network[j][1]);
            map[k++] = (byte) (network[j][2]);
        }
        return map;
    }

    /**
     * 网络的插入排序与netindex的建立[0..]255](在无偏移之后做)
     */
    public void inxbuild() {

        int i, j, smallpos, smallval;
        int[] p;
        int[] q;
        int previouscol, startpos;

        previouscol = 0;
        startpos = 0;
        for (i = 0; i < netsize; i++) {
            p = network[i];
            smallpos = i;
            smallval = p[1];
            for (j = i + 1; j < netsize; j++) {
                q = network[j];
                if (q[1] < smallval) {
                    smallpos = j;
                    smallval = q[1];
                }
            }
            q = network[smallpos];
            if (i != smallpos) {
                j = q[0];
                q[0] = p[0];
                p[0] = j;
                j = q[1];
                q[1] = p[1];
                p[1] = j;
                j = q[2];
                q[2] = p[2];
                p[2] = j;
                j = q[3];
                q[3] = p[3];
                p[3] = j;
            }
            if (smallval != previouscol) {
                netindex[previouscol] = (startpos + i) >> 1;
                for (j = previouscol + 1; j < smallval; j++) {
                    netindex[j] = i;
                }
                previouscol = smallval;
                startpos = i;
            }
        }
        netindex[previouscol] = (startpos + maxnetpos) >> 1;
        for (j = previouscol + 1; j < 256; j++) {
            netindex[j] = maxnetpos; /* really 256 */
        }
    }

    public void learn() {

        int i, j, b, g, r;
        int radius, rad, alpha, step, delta, samplepixels;
        byte[] p;
        int pix, lim;

        if (lengthcount < minpicturebytes) {
            samplefac = 1;
        }
        alphadec = 30 + ((samplefac - 1) / 3);
        p = thepicture;
        pix = 0;
        lim = lengthcount;
        samplepixels = lengthcount / (3 * samplefac);
        delta = samplepixels / ncycles;
        alpha = initalpha;
        radius = initradius;

        rad = radius >> radiusbiasshift;
        if (rad <= 1) {
            rad = 0;
        }
        for (i = 0; i < rad; i++) {
            radpower[i] =
                    alpha * (((rad * rad - i * i) * radbias) / (rad * rad));
        }

        if (lengthcount < minpicturebytes) {
            step = 3;
        } else if ((lengthcount % prime1) != 0) {
            step = 3 * prime1;
        } else {
            if ((lengthcount % prime2) != 0) {
                step = 3 * prime2;
            } else {
                if ((lengthcount % prime3) != 0) {
                    step = 3 * prime3;
                } else {
                    step = 3 * prime4;
                }
            }
        }

        i = 0;
        while (i < samplepixels) {
            b = (p[pix + 0] & 0xff) << netbiasshift;
            g = (p[pix + 1] & 0xff) << netbiasshift;
            r = (p[pix + 2] & 0xff) << netbiasshift;
            j = contest(b, g, r);

            altersingle(alpha, j, b, g, r);
            if (rad != 0) {
                alterneigh(rad, j, b, g, r);
            }

            pix += step;
            if (pix >= lim) {
                pix -= lengthcount;
            }

            i++;
            if (delta == 0) {
                delta = 1;
            }
            if (i % delta == 0) {
                alpha -= alpha / alphadec;
                radius -= radius / radiusdec;
                rad = radius >> radiusbiasshift;
                if (rad <= 1) {
                    rad = 0;
                }
                for (j = 0; j < rad; j++) {
                    radpower[j] =
                            alpha * (((rad * rad - j * j) * radbias) / (rad * rad));
                }
            }
        }
    }

    /**
     * 搜索BGR值0..255(经过净是无偏的)和返回颜色指数
     *
     * @param b 蓝
     * @param g 绿
     * @param r 红
     * @return the int
     */
    public int map(int b, int g, int r) {

        int i, j, dist, a, bestd;
        int[] p;
        int best;

        bestd = 1000; // 最大的距离是256*3
        best = -1;
        i = netindex[g];
        j = i - 1; // 从netindex[g]开始，向外工作

        while ((i < netsize) || (j >= 0)) {
            if (i < netsize) {
                p = network[i];
                dist = p[1] - g;
                if (dist >= bestd) {
                    i = netsize;
                } else {
                    i++;
                    if (dist < 0) {
                        dist = -dist;
                    }
                    a = p[0] - b;
                    if (a < 0) {
                        a = -a;
                    }
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0) {
                            a = -a;
                        }
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
            if (j >= 0) {
                p = network[j];
                dist = g - p[1];
                if (dist >= bestd) {
                    j = -1;
                } else {
                    j--;
                    if (dist < 0) {
                        dist = -dist;
                    }
                    a = p[0] - b;
                    if (a < 0) {
                        a = -a;
                    }
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0) {
                            a = -a;
                        }
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
        }
        return (best);
    }

    public byte[] process() {
        learn();
        unbiasnet();
        inxbuild();
        return colorMap();
    }

    /**
     * 无偏见网络给字节值0..并记录位置i，为排序做准备
     */
    public void unbiasnet() {

        int i;

        for (i = 0; i < netsize; i++) {
            network[i][0] >>= netbiasshift;
            network[i][1] >>= netbiasshift;
            network[i][2] >>= netbiasshift;
            network[i][3] = i;
        }
    }

    /**
     * 通过radpower[|i-j|]中预先计算的alpha*(1-(((i-j)^2/[r]^2))移动相邻神经元
     *
     * @param rad 弧度
     * @param i   偏移量
     * @param b   蓝
     * @param g   绿
     * @param r   红
     */
    protected void alterneigh(int rad, int i, int b, int g, int r) {

        int j, k, lo, hi, a, m;
        int[] p;

        lo = i - rad;
        if (lo < -1) {
            lo = -1;
        }
        hi = i + rad;
        if (hi > netsize) {
            hi = netsize;
        }

        j = i + 1;
        k = i - 1;
        m = 1;
        while ((j < hi) || (k > lo)) {
            a = radpower[m++];
            if (j < hi) {
                p = network[j++];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch (Exception e) {
                }
            }
            if (k > lo) {
                p = network[k--];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 通过因子将神经元i向偏置(b,g,r)移动
     *
     * @param alpha 开端
     * @param i     偏移量
     * @param b     蓝
     * @param g     绿
     * @param r     红
     */
    protected void altersingle(int alpha, int i, int b, int g, int r) {
        int[] n = network[i];
        n[0] -= (alpha * (n[0] - b)) / initalpha;
        n[1] -= (alpha * (n[1] - g)) / initalpha;
        n[2] -= (alpha * (n[2] - r)) / initalpha;
    }

    /**
     * 搜索有偏差的BGR值
     *
     * @param b 蓝
     * @param g 绿
     * @param r 红
     * @return BGR值
     */
    protected int contest(int b, int g, int r) {

        int i, dist, a, biasdist, betafreq;
        int bestpos, bestbiaspos, bestd, bestbiasd;
        int[] n;

        bestd = ~(1 << 31);
        bestbiasd = bestd;
        bestpos = -1;
        bestbiaspos = bestpos;

        for (i = 0; i < netsize; i++) {
            n = network[i];
            dist = n[0] - b;
            if (dist < 0) {
                dist = -dist;
            }
            a = n[1] - g;
            if (a < 0) {
                a = -a;
            }
            dist += a;
            a = n[2] - r;
            if (a < 0) {
                a = -a;
            }
            dist += a;
            if (dist < bestd) {
                bestd = dist;
                bestpos = i;
            }
            biasdist = dist - ((bias[i]) >> (intbiasshift - netbiasshift));
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist;
                bestbiaspos = i;
            }
            betafreq = (freq[i] >> betashift);
            freq[i] -= betafreq;
            bias[i] += (betafreq << gammashift);
        }
        freq[bestpos] += beta;
        bias[bestpos] -= betagamma;
        return (bestbiaspos);
    }

}