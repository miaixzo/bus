/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.office.magic.filter.text;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.bridge.LocalOfficeContextAware;
import org.aoju.bus.office.magic.Info;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Props;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.FilterChain;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 此筛选器用于将图形插入到文档中.
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
public class GraphicInserterFilter extends AbstractTextContentInserterFilter {

    private final String imagePath;

    /**
     * 创建一个新过滤器，在转换文档时将在指定位置插入指定的图像.
     *
     * @param imagePath          到磁盘上的映像(文件)的路径.
     * @param horizontalPosition 在文档上插入图像的水平位置(毫米).
     * @param verticalPosition   在文档上插入图像的垂直位置(毫米).
     * @throws InstrumentException 如果无法检测到图像的大小.
     */
    public GraphicInserterFilter(
            final String imagePath, final int horizontalPosition, final int verticalPosition)
            throws InstrumentException {
        super(getImageSize(new File(imagePath)), horizontalPosition, verticalPosition);

        this.imagePath = imagePath;
    }

    /**
     * 创建一个新过滤器，在转换文档时将在指定位置插入指定的图像.
     *
     * @param imagePath          到磁盘上的映像(文件)的路径.
     * @param width              要插入的图像的宽度。原始图像将根据需要调整大小(毫米).
     * @param height             要插入的图像的高度。原始图像将根据需要调整大小(毫米).
     * @param horizontalPosition 在文档上插入图像的水平位置(毫米).
     * @param verticalPosition   在文档上插入图像的垂直位置(毫米).
     */
    public GraphicInserterFilter(
            final String imagePath,
            final int width,
            final int height,
            final int horizontalPosition,
            final int verticalPosition) {
        super(new Dimension(width, height), horizontalPosition, verticalPosition);

        this.imagePath = imagePath;
    }

    /**
     * 创建一个新的过滤器，它将在转换文档时使用指定的属性插入指定的图像.
     *
     * @param imagePath       到磁盘上的映像(文件)的路径.
     * @param width           要插入的图像的宽度。原始图像将根据需要调整大小(毫米).
     * @param height          要插入的图像的高度。原始图像将根据需要调整大小(毫米).
     * @param shapeProperties 要应用于创建的图形形状的属性.
     */
    public GraphicInserterFilter(
            final String imagePath,
            final int width,
            final int height,
            final Map<String, Object> shapeProperties) {
        super(new Dimension(width, height), shapeProperties);

        this.imagePath = imagePath;
    }

    /**
     * 创建一个新的过滤器，它将在转换文档时使用指定的属性插入指定的图像.
     *
     * @param imagePath       到磁盘上的映像(文件)的路径.
     * @param shapeProperties 要应用于创建的图形形状的属性
     * @throws InstrumentException 如果无法检测到图像的大小.
     */
    public GraphicInserterFilter(final String imagePath, final Map<String, Object> shapeProperties)
            throws InstrumentException {
        super(getImageSize(new File(imagePath)), shapeProperties);

        this.imagePath = imagePath;
    }

    /**
     * 检测图像的大小，而不加载到内存
     *
     * @param image 文件信息
     * @return 图像精度信息
     * @throws InstrumentException 如果无法检测到图像的大小
     */
    private static Dimension getImageSize(final File image) throws InstrumentException {
        try {
            try (ImageInputStream inputStream = ImageIO.createImageInputStream(image)) {
                final Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
                if (readers.hasNext()) {
                    final ImageReader reader = readers.next();
                    try {
                        reader.setInput(inputStream);

                        // 在不解码像素值的情况下，获取流中第一个图像的尺寸
                        return new Dimension(
                                pixelsToMillimeters(reader.getWidth(0)), pixelsToMillimeters(reader.getHeight(0)));
                    } finally {
                        reader.dispose();
                    }
                } else {
                    throw new InstrumentException("Unable to detect the image size: No reader found");
                }
            }
        } catch (IOException ioEx) {
            throw new InstrumentException("Unable to detect the image size", ioEx);
        }
    }

    private static int pixelsToMillimeters(final int pixels) {
        return Math.round(pixels * 0.26458333333333f);
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {

        // 此筛选器只能用于文本文档
        if (Write.isText(document)) {
            insertGraphic(((LocalOfficeContextAware) context).getComponentContext(), document);
        }

        // 调用链中的下一个过滤器
        chain.doFilter(context, document);
    }

    private void insertGraphic(final XComponentContext context, final XComponent document)
            throws Exception {

        // 在XTextDocument上查询接口XMultiServiceFactory(文本服务工厂)
        final XMultiServiceFactory serviceFactory = Lo.getServiceFactory(document);

        // 创建图形形状服务
        final Object graphicShape =
                serviceFactory.createInstance("com.sun.star.drawing.GraphicObjectShape");

        // 访问GraphicObjectShape的XShape接口
        final XShape shape = Lo.qi(XShape.class, graphicShape);

        // 使用XShape的'setSize'设置新文本框的大小
        shape.setSize(toOfficeSize(getRectSize()));

        // 向文档插入图像
        final File sourceFile = new File(imagePath);
        final String strUrl = Builder.toUrl(sourceFile);

        // 查询图形形状服务的属性接口
        final XPropertySet propSet = Lo.qi(XPropertySet.class, graphicShape);

        if (Info.isLibreOffice(context)
                && Info.compareVersions("6.1", Info.getOfficeVersionShort(context), 2) >= 0) {

            // 在全局服务管理器中创建一个图形提供程序.
            final XGraphicProvider graphicProvider =
                    Lo.createInstanceMCF(
                            context, XGraphicProvider.class, "com.sun.star.graphic.GraphicProvider");

            // 从6.1开始，必须使用“Graphic”而不是“GraphicURL”
            propSet.setPropertyValue(
                    "Graphic",
                    graphicProvider.queryGraphic(Props.makeProperties("URL", strUrl, "LoadAsLink", false)));

        } else {
            // 创建位图容器服务
            final XNameContainer bitmapContainer =
                    Lo.createInstanceMSF(
                            serviceFactory, XNameContainer.class, "com.sun.star.drawing.BitmapTable");

            final String uuid = UUID.randomUUID().toString();
            bitmapContainer.insertByName(uuid, strUrl);

            // 将图像内部URL分配给图形形状属性
            propSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(uuid));
        }

        // 分配所有其他属性
        for (final Map.Entry<String, Object> entry : getShapeProperties().entrySet()) {
            propSet.setPropertyValue(entry.getKey(), entry.getValue());
        }

        // 在XComponent上查询接口XTextDocument(文本接口)
        final XTextDocument docText = Write.getTextDoc(document);

        // 获取文本字段接口
        final XText text = docText.getText();

        // 获取文本光标
        final XTextCursor textCursor = text.createTextCursor();

        // 应用AnchorPageNo修复
        applyAnchorPageNoFix(docText, textCursor);

        // 将图形形状转换为文本内容项
        final XTextContent textContent = Lo.qi(XTextContent.class, graphicShape);

        text.insertTextContent(textCursor, textContent, false);
    }

}
