/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.word;

import org.apache.poi.xwpf.usermodel.Document;

/**
 * Word中的图片类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PicType {
    EMF(Document.PICTURE_TYPE_EMF),
    WMF(Document.PICTURE_TYPE_WMF),
    PICT(Document.PICTURE_TYPE_PICT),
    JPEG(Document.PICTURE_TYPE_JPEG),
    PNG(Document.PICTURE_TYPE_PNG),
    DIB(Document.PICTURE_TYPE_DIB),
    GIF(Document.PICTURE_TYPE_GIF),
    TIFF(Document.PICTURE_TYPE_TIFF),
    EPS(Document.PICTURE_TYPE_EPS),
    WPG(Document.PICTURE_TYPE_WPG);

    private int value;

    /**
     * 构造
     *
     * @param value 图片类型值
     */
    PicType(int value) {
        this.value = value;
    }

    /**
     * 获取图片类型对应值
     *
     * @return 图片值
     */
    public int getValue() {
        return this.value;
    }

}
