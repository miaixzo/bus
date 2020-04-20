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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

/**
 * Gif生成工具
 * 类GifEncoder—对包含一个或多个帧的GIF文件进行编码
 * <pre>
 * Example:
 *    GifEncoder e = new GifEncoder();
 *    e.start(outputFileName);
 *    e.setDelay(1000);
 *    e.addFrame(image1);
 *    e.addFrame(image2);
 *    e.finish();
 * </pre>
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class GifEncoder {

    /**
     * 图像大小
     */
    protected int width;
    protected int height;
    /**
     * 如果给定透明颜色
     */
    protected Color transparent = null;
    /**
     * 透明索引的颜色表
     */
    protected int transIndex;
    /**
     * 没有重复
     */
    protected int repeat = -1;
    /**
     * 帧延迟
     */
    protected int delay = 0;
    /**
     * 准备输出帧
     */
    protected boolean started = false;
    protected OutputStream out;
    /**
     * 当前帧
     */
    protected BufferedImage image;
    /**
     * BGR字节数组从帧
     */
    protected byte[] pixels;
    /**
     * 转换帧索引到调色板
     */
    protected byte[] indexedPixels;
    /**
     * 位平面数
     */
    protected int colorDepth;
    /**
     * RGB调色板
     */
    protected byte[] colorTab;
    /**
     * 激活的调色板条目
     */
    protected boolean[] usedEntry = new boolean[256];
    /**
     * 颜色表大小
     */
    protected int palSize = 7;
    /**
     * 处理代码(-1 = default)
     */
    protected int dispose = -1;
    /**
     * 完成后关闭流
     */
    protected boolean closeStream = false;
    protected boolean firstFrame = true;
    /**
     * 如果为假，则从第一帧获取大小
     */
    protected boolean sizeSet = false;
    /**
     * 量化器的默认采样间隔
     */
    protected int sample = 10;

    /**
     * 设置每帧之间的延迟时间，或对后续帧进行更改(适用于最后添加的帧)
     *
     * @param ms int延迟时间(毫秒)
     */
    public void setDelay(int ms) {
        delay = Math.round(ms / 10.0f);
    }

    /**
     * 设置最后添加帧和任何后续帧的GIF帧处理代码
     * 如果没有设置透明颜色，默认为0，否则为2
     *
     * @param code int处理代码
     */
    public void setDispose(int code) {
        if (code >= 0) {
            dispose = code;
        }
    }

    /**
     * 设置一组GIF帧的播放次数。默认是1;0表示无限期地玩下去
     * 必须在添加第一个映像之前调用。
     *
     * @param iter 迭代次数
     */
    public void setRepeat(int iter) {
        if (iter >= 0) {
            repeat = iter;
        }
    }

    /**
     * 设置最后添加的帧和任何后续帧的透明颜色
     * 由于所有颜色在量化过程中都要进行修改， 因此最接近给定颜色的每一帧的最终
     * 调色板中的颜色将成为该帧的透明颜色,可以设置为null来表示没有透明颜色
     *
     * @param c 颜色被当作透明显示
     */
    public void setTransparent(Color c) {
        transparent = c;
    }

    /**
     * 添加下一个GIF帧。帧不是立即写入的，而是延迟到接收到下一帧，
     * 以便插入计时数据。调用finish()刷新所有帧。如果setSize未被调用，
     * 则第一个图像的大小将用于所有后续帧。
     *
     * @param im BufferedImage 包含要写入的帧
     * @return 如果成功返回true
     */
    public boolean addFrame(BufferedImage im) {
        if ((im == null) || !started) {
            return false;
        }
        boolean ok = true;
        try {
            if (!sizeSet) {
                // 使用第一帧的大小
                setSize(im.getWidth(), im.getHeight());
            }
            image = im;
            getImagePixels(); // 必要时转换为正确的格式
            analyzePixels(); // 建立颜色表和地图像素
            if (firstFrame) {
                writeLSD(); // 逻辑屏幕
                writePalette(); // 全局颜色表
                if (repeat >= 0) {
                    // 使用NS应用程序扩展来指示代表
                    writeNetscapeExt();
                }
            }
            writeGraphicCtrlExt(); // 写图形控制扩展
            writeImageDesc(); // 图像描述符
            if (!firstFrame) {
                writePalette(); // 局部颜色列表
            }
            writePixels(); // 对像素数据进行编码和写入
            firstFrame = false;
        } catch (IOException e) {
            ok = false;
        }

        return ok;
    }

    public boolean outFlush() {
        boolean ok = true;
        try {
            out.flush();
            return ok;
        } catch (IOException e) {
            ok = false;
        }

        return ok;
    }

    public byte[] getFrameByteArray() {
        return ((ByteArrayOutputStream) out).toByteArray();
    }

    /**
     * 刷新任何挂起的数据并关闭输出文件
     * 如果写入OutputStream，则该流不是关闭的
     *
     * @return the true/false
     */
    public boolean finish() {
        if (!started) {
            return false;
        }
        boolean ok = true;
        started = false;
        try {
            out.write(0x3b);
            out.flush();
            if (closeStream) {
                out.close();
            }
        } catch (IOException e) {
            ok = false;
        }

        return ok;
    }

    public void reset() {
        // 重设待用
        transIndex = 0;
        out = null;
        image = null;
        pixels = null;
        indexedPixels = null;
        colorTab = null;
        closeStream = false;
        firstFrame = true;
    }

    /**
     * 以帧每秒为单位设置帧速率
     * 相当于<code>setDelay(1000/fps)</code>
     *
     * @param fps 浮点帧速率(帧/秒)
     */
    public void setFrameRate(float fps) {
        if (new Float(0F).equals(fps)) {
            delay = Math.round(100f / fps);
        }
    }

    /**
     * 设置颜色量化的质量(将图像转换为GIF规范允许的最大256色)
     * 较低的值(最小值= 1)产生较好的颜色，但处理速度明显较慢
     * 10是默认值，可以以合理的速度生成良好的颜色映射
     * 大于20的值不会产生显著的速度改进
     *
     * @param quality int大于0
     */
    public void setQuality(int quality) {
        if (quality < 1) {
            quality = 1;
        }
        sample = quality;
    }

    /**
     * 设置GIF帧大小。默认大小是未调用此方法时添加的第一个帧的大小
     *
     * @param w int框架宽度
     * @param h int框宽度
     */
    public void setSize(int w, int h) {
        if (started && !firstFrame) {
            return;
        }
        width = w;
        height = h;
        if (width < 1) {
            width = 320;
        }
        if (height < 1) {
            height = 240;
        }
        sizeSet = true;
    }

    /**
     * 启动给定流上的GIF文件创建。流不是自动关闭的
     *
     * @param os 输出流的GIF图像被写入
     * @return 如果初始写入失败，则为false
     */
    public boolean start(OutputStream os) {
        if (os == null) {
            return false;
        }
        boolean ok = true;
        closeStream = false;
        out = os;
        try {
            writeString("GIF89a");
        } catch (IOException e) {
            ok = false;
        }
        return started = ok;
    }

    /**
     * 初始化写入指定名称的GIF文件
     *
     * @param file 包含输出文件名的字符串
     * @return 如果打开或初始写入失败，则为false
     */
    public boolean start(String file) {
        boolean ok = true;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            ok = start(out);
            closeStream = true;
        } catch (IOException e) {
            ok = false;
        }
        return started = ok;
    }

    /**
     * 分析图像颜色并创建颜色地图
     */
    protected void analyzePixels() {
        int len = pixels.length;
        int nPix = len / 3;
        indexedPixels = new byte[nPix];
        Quant nq = new Quant(pixels, len, sample);
        // 初始化量化器
        colorTab = nq.process(); // 创建了调色板
        // 将映射从BGR转换为RGB
        for (int i = 0; i < colorTab.length; i += 3) {
            byte temp = colorTab[i];
            colorTab[i] = colorTab[i + 2];
            colorTab[i + 2] = temp;
            usedEntry[i / 3] = false;
        }
        // 将图像像素映射到新的调色板
        int k = 0;
        for (int i = 0; i < nPix; i++) {
            int index =
                    nq.map(pixels[k++] & 0xff,
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff);
            usedEntry[index] = true;
            indexedPixels[i] = (byte) index;
        }
        pixels = null;
        colorDepth = 8;
        palSize = 7;
        // 如果指定，获取与透明颜色最接近的匹配
        if (transparent != null) {
            transIndex = findClosest(transparent);
        }
    }

    /**
     * 返回调色板颜色最接近c的索引
     *
     * @param c 颜色
     * @return 返回索引
     */
    protected int findClosest(Color c) {
        if (colorTab == null) {
            return -1;
        }
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int minpos = 0;
        int dmin = 256 * 256 * 256;
        int len = colorTab.length;
        for (int i = 0; i < len; ) {
            int dr = r - (colorTab[i++] & 0xff);
            int dg = g - (colorTab[i++] & 0xff);
            int db = b - (colorTab[i] & 0xff);
            int d = dr * dr + dg * dg + db * db;
            int index = i / 3;
            if (usedEntry[index] && (d < dmin)) {
                dmin = d;
                minpos = index;
            }
            i++;
        }
        return minpos;
    }

    /**
     * 将图像像素提取到字节数组"pixels"中
     */
    protected void getImagePixels() {
        int w = image.getWidth();
        int h = image.getHeight();
        int type = image.getType();
        if ((w != width)
                || (h != height)
                || (type != BufferedImage.TYPE_3BYTE_BGR)) {
            // 创建大小/格式正确的新图像
            BufferedImage temp =
                    new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = temp.createGraphics();
            g.drawImage(image, 0, 0, null);
            image = temp;
        }
        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    /**
     * 写入图形控制扩展名
     *
     * @throws IOException IO异常
     */
    protected void writeGraphicCtrlExt() throws IOException {
        out.write(0x21); // 扩展介绍
        out.write(0xf9); // GCE label
        out.write(4); // 数据块大小
        int transp, disp;
        if (transparent == null) {
            transp = 0;
            disp = 0;
        } else {
            transp = 1;
            disp = 2; // 如果使用透明颜色，则强制清除
        }
        if (dispose >= 0) {
            disp = dispose & 7; // 用户覆盖
        }
        disp <<= 2;

        // 包装领域
        out.write(0 | // 1:3 保留
                disp | // 4:6 处理
                0 | // 7 input - 0 = none
                transp); // 8 透明标志

        writeShort(delay); // 延迟 1/100秒
        out.write(transIndex); // 透明色指数
        out.write(0); // 块终结者
    }

    /**
     * Writes Image Descriptor
     *
     * @throws IOException IO异常
     */
    protected void writeImageDesc() throws IOException {
        out.write(0x2c); // 图像分隔符
        writeShort(0); // 图像位置(x,y) =  0,0
        writeShort(0);
        writeShort(width); // 图像大小
        writeShort(height);
        // packed fields
        if (firstFrame) {
            // 没有LCT - GCT用于第一帧(或仅用于第一帧)
            out.write(0);
        } else {
            // 指定正常LCT
            out.write(0x80 | // 1 本地颜色表1=yes
                    0 | // 2 interlace - 0=no
                    0 | // 3 sorted - 0=no
                    0 | // 4-5 保留
                    palSize); // 6-8 颜色表大小
        }
    }

    /**
     * 写入逻辑屏幕描述符
     *
     * @throws IOException IO异常
     */
    protected void writeLSD() throws IOException {
        // 逻辑屏幕大小
        writeShort(width);
        writeShort(height);
        // packed fields
        out.write((0x80 | // 1   : 全局颜色表标志= 1(使用gct)
                0x70 | // 2-4 : 彩色分辨率= 7
                0x00 | // 5   : gct排序标志= 0
                palSize)); // 6-8: gct大小

        out.write(0); // 背景颜色指数
        out.write(0); // 像素长宽比-假设1:1
    }

    /**
     * 写入Netscape应用程序扩展名以定义重复计数
     *
     * @throws IOException IO异常
     */
    protected void writeNetscapeExt() throws IOException {
        out.write(0x21); // 扩展处理者
        out.write(0xff); // 应用程序扩展标签
        out.write(11); // 块大小
        writeString("NETSCAPE" + "2.0"); // 应用id + auth代码
        out.write(3); // 子块的大小
        out.write(1); // 循环子块id
        writeShort(repeat); // 循环计数(额外的迭代，0=永远重复)
        out.write(0); // 块终结者
    }

    /**
     * 写颜色表
     *
     * @throws IOException IO异常
     */
    protected void writePalette() throws IOException {
        out.write(colorTab, 0, colorTab.length);
        int n = (3 * 256) - colorTab.length;
        for (int i = 0; i < n; i++) {
            out.write(0);
        }
    }

    /**
     * 编码和写入像素数据
     *
     * @throws IOException IO异常
     */
    protected void writePixels() throws IOException {
        CaptchaEncoder captchaEncoder = new CaptchaEncoder(width, height, indexedPixels, colorDepth);
        captchaEncoder.encode(out);
    }

    /**
     * 将16位值写入输出流，首先是LSB
     *
     * @param value 16位值
     * @throws IOException IO异常
     */
    protected void writeShort(int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }

    /**
     * 将字符串写入输出流
     *
     * @param s 字符串信息
     * @throws IOException IO异常
     */
    protected void writeString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
        }
    }

}
