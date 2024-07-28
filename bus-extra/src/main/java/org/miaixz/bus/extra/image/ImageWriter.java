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
package org.miaixz.bus.extra.image;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图片写出封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageWriter implements Flushable {

    private final RenderedImage image;
    private final javax.imageio.ImageWriter writer;
    private ImageWriteParam writeParam;

    /**
     * 构造
     *
     * @param image     {@link Image}
     * @param imageType 图片类型（图片扩展名），{@code null}表示使用RGB模式（JPG）
     */
    public ImageWriter(final Image image, final String imageType) {
        this.image = ImageKit.castToRenderedImage(image, imageType);
        this.writer = ImageKit.getWriter(image, imageType);
    }

    /**
     * 创建图片写出器
     *
     * @param image           图片
     * @param imageType       图片类型（图片扩展名），{@code null}表示使用RGB模式（JPG）
     * @param backgroundColor 背景色{@link Color}，{@code null}表示黑色或透明
     * @return {@code ImgWriter}
     */
    public static ImageWriter of(final Image image, final String imageType, final Color backgroundColor) {
        return of(ImageKit.toBufferedImage(image, imageType, backgroundColor), imageType);
    }

    /**
     * 创建图片写出器
     *
     * @param image     图片
     * @param imageType 图片类型（图片扩展名），{@code null}表示使用RGB模式（JPG）
     * @return {@code ImgWriter}
     */
    public static ImageWriter of(final Image image, final String imageType) {
        return new ImageWriter(image, imageType);
    }

    /**
     * 构建图片写出参数
     *
     * @param renderedImage 图片
     * @param writer        {@link javax.imageio.ImageWriter}
     * @param quality       质量，范围0~1
     * @return {@link ImageWriteParam} or {@code null}
     */
    private static ImageWriteParam buildParam(final RenderedImage renderedImage, final javax.imageio.ImageWriter writer,
            final float quality) {
        // 设置质量
        ImageWriteParam imgWriteParams = null;
        if (quality > 0 && quality < 1) {
            imgWriteParams = writer.getDefaultWriteParam();
            if (imgWriteParams.canWriteCompressed()) {
                imgWriteParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imgWriteParams.setCompressionQuality(quality);
                final ColorModel colorModel = renderedImage.getColorModel();// ColorModel.getRGBdefault();
                imgWriteParams.setDestinationType(
                        new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));
            }
        }
        return imgWriteParams;
    }

    /**
     * 设置写出质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     *
     * @param quality 写出质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     * @return this
     */
    public ImageWriter setQuality(final float quality) {
        this.writeParam = buildParam(this.image, this.writer, quality);
        return this;
    }

    /**
     * 写出图像：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG 此方法并不关闭流
     *
     * @param out 写出到的目标流
     * @throws InternalException IO异常
     */
    public void write(final OutputStream out) throws InternalException {
        write(ImageKit.getImageOutputStream(out));
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param targetFile 目标文件
     * @throws InternalException IO异常
     */
    public void write(final File targetFile) throws InternalException {
        FileKit.touch(targetFile);
        ImageOutputStream out = null;
        try {
            out = ImageKit.getImageOutputStream(targetFile);
            write(out);
        } finally {
            IoKit.closeQuietly(out);
        }
    }

    /**
     * 通过{@link javax.imageio.ImageWriter}写出图片到输出流
     *
     * @param output 输出的Image流{@link ImageOutputStream}， 非空
     */
    public void write(final ImageOutputStream output) {
        Assert.notNull(output);

        final javax.imageio.ImageWriter writer = this.writer;
        final RenderedImage image = this.image;
        writer.setOutput(output);
        // 设置质量
        try {
            if (null != this.writeParam) {
                writer.write(null, new IIOImage(image, null, null), this.writeParam);
            } else {
                writer.write(image);
            }
            output.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            writer.dispose();
        }
    }

    @Override
    public void flush() {
        final RenderedImage renderedImage = this.image;
        if (renderedImage instanceof BufferedImage) {
            ImageKit.flush((BufferedImage) renderedImage);
        } else if (renderedImage instanceof Image) {
            ImageKit.flush((Image) renderedImage);
        }
    }

}
