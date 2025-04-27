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
package org.miaixz.bus.office.excel.cell.setters;

import java.io.File;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.office.excel.SimpleAnchor;
import org.miaixz.bus.office.excel.shape.ExcelPictureType;
import org.miaixz.bus.office.excel.writer.ExcelDrawing;

/**
 * 图片单元格值设置器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PictureCellSetter implements CellSetter {

    private final byte[] pictureData;
    private final ExcelPictureType pictureType;

    /**
     * 构造，默认PNG图片
     *
     * @param pictureData 图片数据
     */
    public PictureCellSetter(final byte[] pictureData) {
        this(pictureData, ExcelPictureType.PNG);
    }

    /**
     * 构造
     *
     * @param picturefile 图片数据
     */
    public PictureCellSetter(final File picturefile) {
        this(FileKit.readBytes(picturefile), ExcelPictureType.getType(picturefile));
    }

    /**
     * 构造
     *
     * @param pictureData 图片数据
     * @param pictureType 图片类型
     */
    public PictureCellSetter(final byte[] pictureData, final ExcelPictureType pictureType) {
        this.pictureData = pictureData;
        this.pictureType = pictureType;
    }

    @Override
    public void setValue(final Cell cell) {
        final Sheet sheet = cell.getSheet();
        final int columnIndex = cell.getColumnIndex();
        final int rowIndex = cell.getRowIndex();

        ExcelDrawing.drawingPicture(sheet, this.pictureData, this.pictureType,
                new SimpleAnchor(columnIndex, rowIndex, columnIndex + 1, rowIndex + 1));
    }

}
