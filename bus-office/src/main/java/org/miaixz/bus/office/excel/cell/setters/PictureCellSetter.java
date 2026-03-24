/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * {@link CellSetter} for picture values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PictureCellSetter implements CellSetter {

    /**
     * The picture data as a byte array.
     */
    private final byte[] pictureData;
    /**
     * The picture type.
     */
    private final ExcelPictureType pictureType;

    /**
     * Constructs a {@code PictureCellSetter} with picture data, defaulting to PNG image type.
     *
     * @param pictureData The byte array of the picture data.
     */
    public PictureCellSetter(final byte[] pictureData) {
        this(pictureData, ExcelPictureType.PNG);
    }

    /**
     * Constructs a {@code PictureCellSetter} with a picture file.
     *
     * @param picturefile The picture file.
     */
    public PictureCellSetter(final File picturefile) {
        this(FileKit.readBytes(picturefile), ExcelPictureType.getType(picturefile));
    }

    /**
     * Constructs a {@code PictureCellSetter} with picture data and a specified picture type.
     *
     * @param pictureData The byte array of the picture data.
     * @param pictureType The type of the picture.
     */
    public PictureCellSetter(final byte[] pictureData, final ExcelPictureType pictureType) {
        this.pictureData = pictureData;
        this.pictureType = pictureType;
    }

    /**
     * Sets the picture value to the specified cell by creating a picture in the sheet.
     *
     * @param cell The {@link Cell} to set the value to.
     */
    @Override
    public void setValue(final Cell cell) {
        final Sheet sheet = cell.getSheet();
        final int columnIndex = cell.getColumnIndex();
        final int rowIndex = cell.getRowIndex();

        ExcelDrawing.drawingPicture(
                sheet,
                this.pictureData,
                this.pictureType,
                new SimpleAnchor(columnIndex, rowIndex, columnIndex + 1, rowIndex + 1));
    }

}
