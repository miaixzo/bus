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
package org.miaixz.bus.extra.captcha.provider;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.ImageKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.extra.captcha.CaptchaProvider;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 抽象验证码
 * 抽象验证码实现了验证码字符串的生成、验证，验证码图片的写出
 * 实现类通过实现{@link #createImage(String)} 方法生成图片对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements CaptchaProvider {

    /**
     * 图片的宽度
     */
    protected int width;
    /**
     * 图片的高度
     */
    protected int height;
    /**
     * 验证码干扰元素个数
     */
    protected int interfereCount;
    /**
     * 字体
     */
    protected Font font;
    /**
     * 验证码
     */
    protected String code;
    /**
     * 验证码图片
     */
    protected byte[] imageBytes;
    /**
     * 验证码生成器
     */
    protected CodeStrategy generator;
    /**
     * 背景色
     */
    protected Color background;
    /**
     * 文字透明度
     */
    protected AlphaComposite textAlpha;

    /**
     * 构造，使用随机验证码生成器生成验证码
     *
     * @param width          图片宽
     * @param height         图片高
     * @param codeCount      字符个数
     * @param interfereCount 验证码干扰元素个数
     */
    public AbstractProvider(int width, int height, int codeCount, int interfereCount) {
        this(width, height, new RandomStrategy(codeCount), interfereCount);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param generator      验证码生成器
     * @param interfereCount 验证码干扰元素个数
     */
    public AbstractProvider(int width, int height, CodeStrategy generator, int interfereCount) {
        this.width = width;
        this.height = height;
        this.generator = generator;
        this.interfereCount = interfereCount;
        // 字体高度设为验证码高度-2，留边距
        this.font = new Font(Font.SANS_SERIF, Font.PLAIN, (int) (this.height * 0.75));
    }

    @Override
    public void create() {
        generateCode();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageKit.writePng(createImage(this.code), out);
        this.imageBytes = out.toByteArray();
    }

    /**
     * 生成验证码字符串
     */
    protected void generateCode() {
        this.code = generator.generate();
    }

    /**
     * 根据生成的code创建验证码图片
     *
     * @param code 验证码
     * @return Image
     */
    protected abstract Image createImage(String code);

    @Override
    public String get() {
        if (null == this.code) {
            create();
        }
        return this.code;
    }

    @Override
    public boolean verify(String inputCode) {
        return this.generator.verify(get(), inputCode);
    }

    /**
     * 验证码写出到文件
     *
     * @param path 文件路径
     * @throws InternalException IO异常
     */
    public void write(String path) throws InternalException {
        this.write(FileKit.touch(path));
    }

    /**
     * 验证码写出到文件
     *
     * @param file 文件
     * @throws InternalException IO异常
     */
    public void write(File file) throws InternalException {
        try (OutputStream out = FileKit.getOutputStream(file)) {
            this.write(out);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void write(OutputStream out) {
        IoKit.write(out, false, getImageBytes());
    }

    /**
     * 获取图形验证码图片bytes
     *
     * @return 图形验证码图片bytes
     */
    public byte[] getImageBytes() {
        if (null == this.imageBytes) {
            create();
        }
        return this.imageBytes;
    }

    /**
     * 获取验证码图
     *
     * @return 验证码图
     */
    public BufferedImage getImage() {
        return ImageKit.read(IoKit.toStream(getImageBytes()));
    }

    /**
     * 获得图片的Base64形式
     *
     * @return 图片的Base64
     */
    public String getImageBase64() {
        return Base64.encode(getImageBytes());
    }

    /**
     * 自定义字体
     *
     * @param font 字体
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * 获取验证码生成器
     *
     * @return 验证码生成器
     */
    public CodeStrategy getGenerator() {
        return generator;
    }

    /**
     * 设置验证码生成器
     *
     * @param generator 验证码生成器
     */
    public void setGenerator(CodeStrategy generator) {
        this.generator = generator;
    }

    /**
     * 设置背景色
     *
     * @param background 背景色
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
     * 设置文字透明度
     *
     * @param textAlpha 文字透明度，取值0~1，1表示不透明
     */
    public void setTextAlpha(float textAlpha) {
        this.textAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha);
    }

}
