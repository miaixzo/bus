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
package org.miaixz.bus.core.image.element;

import org.miaixz.bus.core.lang.Scale;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * 图片元素
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageElement extends AbstractElement<ImageElement> {

    /**
     * 图片对象
     */
    private BufferedImage image;
    /**
     * 图片地址
     */
    private String imgUrl;
    /**
     * 绘制宽度
     */
    private Integer width;
    /**
     * 绘制高度
     */
    private Integer height;
    /**
     * 圆角大小
     */
    private Integer roundCorner;
    /**
     * 缩放模式
     */
    private Scale.Zoom zoom;
    /**
     * 旋转角度
     */
    private Integer rotate;
    /**
     * 高斯模糊
     */
    private Integer blur;

    /**
     * @param imgUrl 图片url
     * @param x      x坐标
     * @param y      y坐标
     */
    public ImageElement(String imgUrl, int x, int y) {
        this.imgUrl = imgUrl;
        this.width = getImage().getWidth();     //事先获得宽高，后面计算要用
        this.height = getImage().getHeight();
        this.zoom = Scale.Zoom.ORIGIN;
        super.setX(x);
        super.setY(y);
    }

    /**
     * @param image 图片对象
     * @param x     x坐标
     * @param y     y坐标
     */
    public ImageElement(BufferedImage image, int x, int y) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.zoom = Scale.Zoom.ORIGIN;
        super.setX(x);
        super.setY(y);
    }

    /**
     * @param imgUrl 图片url
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @param zoom   缩放模式
     */
    public ImageElement(String imgUrl, int x, int y, int width, int height, Scale.Zoom zoom) {
        this.imgUrl = imgUrl;
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        super.setX(x);
        super.setY(y);
    }

    /**
     * @param image  图片对象
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @param zoom   缩放模式
     */
    public ImageElement(BufferedImage image, int x, int y, int width, int height, Scale.Zoom zoom) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        super.setX(x);
        super.setY(y);
    }


    public BufferedImage getImage() {
        if (null == this.image) {
            try {
                this.image = ImageIO.read(new URL(this.imgUrl));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    public ImageElement setImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public ImageElement setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public ImageElement setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public ImageElement setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getRoundCorner() {
        return roundCorner;
    }

    public ImageElement setRoundCorner(Integer roundCorner) {
        this.roundCorner = roundCorner;
        return this;
    }

    public Scale.Zoom getZoom() {
        return zoom;
    }

    public ImageElement setZoom(Scale.Zoom zoom) {
        this.zoom = zoom;
        return this;
    }

    public Integer getRotate() {
        return rotate;
    }

    public ImageElement setRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }

    public Integer getBlur() {
        return blur;
    }

    public void setBlur(Integer blur) {
        this.blur = blur;
    }

}
