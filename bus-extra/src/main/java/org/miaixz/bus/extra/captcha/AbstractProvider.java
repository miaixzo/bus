/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.captcha;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;
import org.miaixz.bus.extra.image.ImageKit;

/**
 * 抽象验证码 抽象验证码实现了验证码字符串的生成、验证，验证码图片的写出 实现类通过实现{@link #createImage(String)} 方法生成图片对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements CaptchaProvider {

    private static final long serialVersionUID = -1L;

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
    protected Color background = Color.WHITE;
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
    public AbstractProvider(final int width, final int height, final int codeCount, final int interfereCount) {
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
    public AbstractProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount) {
        this(width, height, generator, interfereCount, Normal.DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param generator      验证码生成器
     * @param interfereCount 验证码干扰元素个数
     * @param sizeBaseHeight 字体的大小 高度的倍数
     */
    public AbstractProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount,
            final float sizeBaseHeight) {
        this.width = width;
        this.height = height;
        this.generator = generator;
        this.interfereCount = interfereCount;
        // 字体高度设为验证码高度-2，留边距
        this.font = new Font(Font.SANS_SERIF, Font.PLAIN, (int) (this.height * sizeBaseHeight));
    }

    @Override
    public void create() {
        generateCode();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Image image = null;
        try {
            image = createImage(this.code);
            ImageKit.writePng(image, out);
        } finally {
            ImageKit.flush(image);
        }

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
    public boolean verify(final String userInputCode) {
        return this.generator.verify(get(), userInputCode);
    }

    /**
     * 验证码写出到文件
     *
     * @param path 文件路径
     * @throws InternalException IO异常
     */
    public void write(final String path) throws InternalException {
        this.write(FileKit.touch(path));
    }

    /**
     * 验证码写出到文件
     *
     * @param file 文件
     * @throws InternalException IO异常
     */
    public void write(final File file) throws InternalException {
        try (final OutputStream out = FileKit.getOutputStream(file)) {
            this.write(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void write(final OutputStream out) {
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
     * 获取验证码图 注意返回的{@link BufferedImage}使用完毕后需要调用{@link BufferedImage#flush()}释放资源
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
     * 获取图片带文件格式的 Base64
     *
     * @return 图片带文件格式的 Base64
     */
    public String getImageBase64Data() {
        return UrlKit.getDataUriBase64("image/png", getImageBase64());
    }

    /**
     * 自定义字体
     *
     * @param font 字体
     */
    public void setFont(final Font font) {
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
    public void setGenerator(final CodeStrategy generator) {
        this.generator = generator;
    }

    /**
     * 设置背景色，{@code null}表示透明背景
     *
     * @param background 背景色
     */
    public void setBackground(final Color background) {
        this.background = background;
    }

    /**
     * 设置文字透明度
     *
     * @param textAlpha 文字透明度，取值0~1，1表示不透明
     */
    public void setTextAlpha(final float textAlpha) {
        this.textAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha);
    }

}
