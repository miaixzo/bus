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
package org.miaixz.bus.office.excel.shape;

import java.io.File;

import org.apache.poi.ss.usermodel.Workbook;
import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Excel支持的图片类型枚举
 *
 * 
 * @see Workbook#PICTURE_TYPE_EMF
 * @see Workbook#PICTURE_TYPE_WMF
 * @see Workbook#PICTURE_TYPE_PICT
 * @see Workbook#PICTURE_TYPE_JPEG
 * @see Workbook#PICTURE_TYPE_PNG
 * @see Workbook#PICTURE_TYPE_DIB
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ExcelPictureType {
    /**
     * Extended windows meta file
     */
    EMF(Workbook.PICTURE_TYPE_EMF),

    /**
     * Windows Meta File
     */
    WMF(Workbook.PICTURE_TYPE_WMF),

    /**
     * Mac PICT format
     */
    PICT(Workbook.PICTURE_TYPE_PICT),

    /**
     * JPEG format
     */
    JPEG(Workbook.PICTURE_TYPE_JPEG),

    /**
     * PNG format
     */
    PNG(Workbook.PICTURE_TYPE_PNG),

    /**
     * Device independent bitmap
     */
    DIB(Workbook.PICTURE_TYPE_DIB);

    private final int value;

    /**
     * 构造
     *
     * @param value 类型编码
     */
    ExcelPictureType(final int value) {
        this.value = value;
    }

    /**
     * 获取图片类型
     *
     * @param imgFile 图片文件
     * @return 图片类型，默认PNG
     */
    public static ExcelPictureType getType(final File imgFile) {
        final String type = FileType.getType(imgFile);
        if (StringKit.equalsAnyIgnoreCase(type, "jpg", "jpeg")) {
            return JPEG;
        } else if (StringKit.equalsAnyIgnoreCase(type, "emf")) {
            return EMF;
        } else if (StringKit.equalsAnyIgnoreCase(type, "wmf")) {
            return WMF;
        } else if (StringKit.equalsAnyIgnoreCase(type, "pict")) {
            return PICT;
        } else if (StringKit.equalsAnyIgnoreCase(type, "dib")) {
            return DIB;
        }

        // 默认格式
        return PNG;
    }

    /**
     * 获取类型编码
     *
     * @return 编码
     */
    public int getValue() {
        return this.value;
    }

}
