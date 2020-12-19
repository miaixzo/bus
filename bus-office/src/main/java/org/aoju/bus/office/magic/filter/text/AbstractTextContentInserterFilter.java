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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.office.magic.filter.text;

import com.sun.star.awt.Size;
import com.sun.star.frame.XController;
import com.sun.star.text.*;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.filter.Filter;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用于将文本内容插入文档的所有筛选器的基类.
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public abstract class AbstractTextContentInserterFilter implements Filter {

    private final Dimension rectSize;
    private final Map<String, Object> shapeProperties;

    /**
     * 创建一个新的过滤器，它将在转换文档时在指定位置插入指定大小的文本内容(形状).
     *
     * @param size               将要插入的形状的尺寸(毫米).
     * @param horizontalPosition 在文档上插入文本内容的水平位置(毫米).
     * @param verticalPosition   在文档上插入文本内容的垂直位置(毫米).
     */
    public AbstractTextContentInserterFilter(
            final Dimension size, final int horizontalPosition, final int verticalPosition) {
        super();

        this.rectSize = new Dimension(size.width, size.height);
        this.shapeProperties = createDefaultShapeProperties(horizontalPosition, verticalPosition);
    }

    /**
     * 创建一个新的过滤器，它将在转换文档时使用指定的形状属性插入指定大小的文本内容(形状).
     *
     * @param size            将要插入的形状的尺寸(毫米).
     * @param shapeProperties 要应用于创建的形状的属性.
     */
    public AbstractTextContentInserterFilter(
            final Dimension size, final Map<String, Object> shapeProperties) {
        super();

        this.rectSize = new Dimension(size.width, size.height);
        this.shapeProperties = new LinkedHashMap<>(shapeProperties);
    }

    /**
     * 创建将在文档第一页的指定位置插入文本内容的默认形状属性.
     *
     * @param horizontalPosition 在文档上插入文本内容的水平位置(毫米).
     * @param verticalPosition   在文档上插入文本内容的垂直位置(毫米).
     * @return 包含默认形状属性的映射.
     */
    public static Map<String, Object> createDefaultShapeProperties(
            final int horizontalPosition, final int verticalPosition) {

        final Map<String, Object> props = new LinkedHashMap<>();

        // 设置锚类型
        props.put("AnchorType", TextContentAnchorType.AT_PAGE);

        // 设置水平位置 (1 = 0.01 mm)
        props.put("HoriOrient", VertOrientation.NONE);
        props.put("HoriOrientPosition", horizontalPosition * 100);
        props.put("HoriOrientRelation", RelOrientation.PAGE_FRAME);

        // 设置垂直位置 (1 = 0.01 mm)
        props.put("VertOrient", VertOrientation.NONE);
        props.put("VertOrientPosition", verticalPosition * 100);
        props.put("VertOrientRelation", RelOrientation.PAGE_FRAME);

        // 设置换行
        props.put("Surround", WrapTextMode.THROUGHT);

        return props;
    }

    /**
     * 将单位为毫米的指定大小转换为单位为1/100毫米的office大小.
     *
     * @param size 要转换的大小，以毫米为单位.
     * @return 转换后的大小实例，以1/100毫米为单位.
     */
    public static Size toOfficeSize(final Dimension size) {

        return new Size(size.width * 100, size.height * 100);
    }

    /**
     * 如果出现在形状属性中，则跳转到“AnchorPageNo”指定的页面
     *
     * @param docText    文档的文本接口.
     * @param textCursor 文档的文本光标.
     */
    protected void applyAnchorPageNoFix(final XTextDocument docText, final XTextCursor textCursor) {

        // 某些输出格式(doc、docx、rtf)似乎需要以下代码块，而不是“AnchorPageNo”属性.
        final Object anchorPageNo = shapeProperties.get("AnchorPageNo");
        if (anchorPageNo != null) {
            final XController controller = docText.getCurrentController();
            final XTextViewCursor viewCursor =
                    Lo.qi(XTextViewCursorSupplier.class, controller).getViewCursor();
            final XPageCursor pageCursor = Lo.qi(XPageCursor.class, viewCursor);
            pageCursor.jumpToPage(Short.parseShort(anchorPageNo.toString()));
            textCursor.gotoRange(viewCursor, false);
        }
    }

    /**
     * 获取要插入的形状的矩形大小.
     *
     * @return 表示形状大小的矩形。单位是毫米.
     */
    public Dimension getRectSize() {
        return rectSize;
    }

    /**
     * 获取将形状插入文档时要应用的形状属性.
     *
     * @return 包含属性的映射.
     */
    public Map<String, Object> getShapeProperties() {
        return shapeProperties;
    }

}
