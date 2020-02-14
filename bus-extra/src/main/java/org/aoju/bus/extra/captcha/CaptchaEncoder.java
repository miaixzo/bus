/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.extra.captcha;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kimi Liu
 * @version 5.6.0
 * @since JDK 1.8+
 */
public class CaptchaEncoder {

    static final int BITS = 12;
    /**
     * 80% 占用率
     */
    static final int HSIZE = 5003;
    private static final int EOF = -1;
    /**
     * number of bits/code
     */
    int n_bits;
    /**
     * 用户可设置的最大
     */
    int maxbits = BITS;
    /**
     * 最大代码
     */
    int maxcode;
    /**
     * 永远不应该生成这段代码
     */
    int maxmaxcode = 1 << BITS;
    int[] htab = new int[HSIZE];
    int[] codetab = new int[HSIZE];
    /**
     * 对于动态表调整大小
     */
    int hsize = HSIZE;
    /**
     * 第一个未使用的条目
     */
    int free_ent = 0;
    /**
     * 块压缩参数——在所有代码用完并改变压缩率之后，重新开始
     */
    boolean clear_flg = false;
    int g_init_bits;
    int ClearCode;
    int EOFCode;
    int cur_accum = 0;
    int cur_bits = 0;

    /**
     * 算法:对前缀代码/下一个字符组合使用开放寻址双哈希(没有链接)
     */
    int[] masks = {
            0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F, 0x00FF,
            0x01FF, 0x03FF, 0x07FF, 0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
    };

    int a_count;
    /**
     * 定义包累加器的存储
     */
    byte[] accum = new byte[256];

    /**
     * 图片的宽高
     */
    private int imgW, imgH;
    private byte[] pixAry;
    /**
     * 验证码位数
     */
    private int initCodeSize;
    /**
     * 剩余数量
     */
    private int remaining;
    /**
     * 像素
     */
    private int curPixel;

    /**
     * @param width       宽度
     * @param height      高度
     * @param pixels      像素
     * @param color_depth 颜色
     */
    CaptchaEncoder(int width, int height, byte[] pixels, int color_depth) {
        imgW = width;
        imgH = height;
        pixAry = pixels;
        initCodeSize = Math.max(2, color_depth);
    }

    /**
     * @param c    字节
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void char_out(byte c, OutputStream outs) throws IOException {
        accum[a_count++] = c;
        if (a_count >= 254) {
            flush_char(outs);
        }
    }

    /**
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void cl_block(OutputStream outs) throws IOException {
        cl_hash(hsize);
        free_ent = ClearCode + 2;
        clear_flg = true;

        output(ClearCode, outs);
    }

    /**
     * @param hsize int
     */
    void cl_hash(int hsize) {
        for (int i = 0; i < hsize; ++i) {
            htab[i] = -1;
        }
    }

    /**
     * @param init_bits int
     * @param outs      输出流
     * @throws IOException IO异常
     */
    void compress(int init_bits, OutputStream outs) throws IOException {
        int fcode;
        int i /* = 0 */;
        int c;
        int ent;
        int disp;
        int hsize_reg;
        int hshift;

        // 设置全局变量
        g_init_bits = init_bits;

        // 设置必要的值
        clear_flg = false;
        n_bits = g_init_bits;
        maxcode = MAXCODE(n_bits);

        ClearCode = 1 << (init_bits - 1);
        EOFCode = ClearCode + 1;
        free_ent = ClearCode + 2;

        a_count = 0;

        ent = nextPixel();

        hshift = 0;
        for (fcode = hsize; fcode < 65536; fcode *= 2) {
            ++hshift;
        }
        hshift = 8 - hshift; // 设置哈希码的范围界限

        hsize_reg = hsize;
        cl_hash(hsize_reg); // 清除希表

        output(ClearCode, outs);

        outer_loop:
        while ((c = nextPixel()) != EOF) {
            fcode = (c << maxbits) + ent;
            i = (c << hshift) ^ ent;

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) {
                disp = hsize_reg - i;
                if (i == 0) {
                    disp = 1;
                }
                do {
                    if ((i -= disp) < 0) {
                        i += hsize_reg;
                    }

                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }
            output(ent, outs);
            ent = c;
            if (free_ent < maxmaxcode) {
                codetab[i] = free_ent++;
                htab[i] = fcode;
            } else {
                cl_block(outs);
            }
        }
        // 输出最后的代码
        output(ent, outs);
        output(EOFCode, outs);
    }

    /**
     * @param os 输出流
     * @throws IOException IO异常
     */
    void encode(OutputStream os) throws IOException {
        // 编写"initial code size"字节
        os.write(initCodeSize);

        // 重置变量
        remaining = imgW * imgH;
        curPixel = 0;

        // 压缩和写入像素数据
        compress(initCodeSize + 1, os);
        // 写块终结者
        os.write(0);
    }

    /**
     * 将数据包刷新到磁盘，并重新设置累加器
     *
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void flush_char(OutputStream outs) throws IOException {
        if (a_count > 0) {
            outs.write(a_count);
            outs.write(accum, 0, a_count);
            a_count = 0;
        }
    }

    /**
     * @param n_bits int
     * @return int
     */
    final int MAXCODE(int n_bits) {
        return (1 << n_bits) - 1;
    }

    /**
     * 从图像中返回下一个像素
     *
     * @return int
     */
    private int nextPixel() {
        if (remaining == 0) {
            return EOF;
        }

        --remaining;

        byte pix = pixAry[curPixel++];

        return pix & 0xff;
    }

    /**
     * @param code int
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void output(int code, OutputStream outs) throws IOException {
        cur_accum &= masks[cur_bits];

        if (cur_bits > 0) {
            cur_accum |= (code << cur_bits);
        } else {
            cur_accum = code;
        }

        cur_bits += n_bits;

        while (cur_bits >= 8) {
            char_out((byte) (cur_accum & 0xff), outs);
            cur_accum >>= 8;
            cur_bits -= 8;
        }

        // 如果下一个条目对于代码大小来说太大了，那么可以增加它
        if (free_ent > maxcode || clear_flg) {
            if (clear_flg) {
                maxcode = MAXCODE(n_bits = g_init_bits);
                clear_flg = false;
            } else {
                ++n_bits;
                if (n_bits == maxbits) {
                    maxcode = maxmaxcode;
                } else {
                    maxcode = MAXCODE(n_bits);
                }
            }
        }

        if (code == EOFCode) {
            // 在EOF时，写入缓冲区的其余部分
            while (cur_bits > 0) {
                char_out((byte) (cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            flush_char(outs);
        }
    }

}
