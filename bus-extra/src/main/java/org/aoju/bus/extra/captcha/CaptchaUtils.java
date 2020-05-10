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
import java.io.OutputStream;
import java.util.Random;

/**
 * 验证码工具类
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public class CaptchaUtils {

    /**
     * 输出验证码
     *
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String out(OutputStream outputStream) {
        return out(5, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param len          长度
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String out(int len, OutputStream outputStream) {
        return out(130, 48, len, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param len          长度
     * @param font         字体
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String out(int len, Font font, OutputStream outputStream) {
        return out(130, 48, len, font, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param width        宽度
     * @param height       高度
     * @param len          长度
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String out(int width, int height, int len, OutputStream outputStream) {
        return out(width, height, len, null, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param width        宽度
     * @param height       高度
     * @param len          长度
     * @param font         字体
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String out(int width, int height, int len, Font font, OutputStream outputStream) {
        int cType = new Random().nextInt(6);
        return outCaptcha(width, height, len, font, cType, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String outPng(OutputStream outputStream) {
        return outPng(5, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param len          长度
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String outPng(int len, OutputStream outputStream) {
        return outPng(130, 48, len, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param len          长度
     * @param font         字体
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String outPng(int len, Font font, OutputStream outputStream) {
        return outPng(130, 48, len, font, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param width        宽度
     * @param height       高度
     * @param len          长度
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String outPng(int width, int height, int len, OutputStream outputStream) {
        return outPng(width, height, len, null, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param width        宽度
     * @param height       高度
     * @param len          长度
     * @param font         字体
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    public static String outPng(int width, int height, int len, Font font, OutputStream outputStream) {
        int cType = new Random().nextInt(6);
        return outCaptcha(width, height, len, font, cType, outputStream);
    }

    /**
     * 输出验证码
     *
     * @param width        宽度
     * @param height       高度
     * @param len          长度
     * @param font         字体
     * @param cType        类型
     * @param outputStream OutputStream
     * @return 验证码结果
     */
    private static String outCaptcha(int width, int height, int len, Font font, int cType, OutputStream outputStream) {
        AbstractCaptcha captcha = null;
        if (cType == 0) {
            captcha = new SpecCaptcha(width, height, len);
        } else if (cType == 1) {
            captcha = new GifCaptcha(width, height, len);
        } else if (cType == 2) {
            captcha = new ChineseCaptcha(width, height, len);
        } else if (cType == 3) {
            captcha = new ChineseGifCaptcha(width, height, len);
        } else if (cType == 4) {
            captcha = new MathCaptcha(width, height, 4);
        } else if (cType == 5) {
            captcha = new MathGifCaptcha(width, height, 4);
        }
        if (font != null) {
            captcha.setFont(font);
        }
        captcha.out(outputStream);
        return captcha.text().toLowerCase();
    }

}
