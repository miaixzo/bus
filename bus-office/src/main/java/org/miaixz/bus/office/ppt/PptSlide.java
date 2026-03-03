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
package org.miaixz.bus.office.ppt;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * Wrapper around {@link XSLFSlide} providing convenient operations for a single slide, including adding text, pictures,
 * tables, shapes, and configuring background.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PptSlide {

    /**
     * The underlying {@link XSLFSlide} instance.
     */
    private final XSLFSlide slide;

    /**
     * The parent {@link XMLSlideShow} for adding picture data.
     */
    private final XMLSlideShow ppt;

    /**
     * Constructs a new {@code PptSlide} wrapping the given slide.
     *
     * @param slide The {@link XSLFSlide} to wrap.
     * @param ppt   The parent {@link XMLSlideShow}.
     */
    public PptSlide(final XSLFSlide slide, final XMLSlideShow ppt) {
        this.slide = slide;
        this.ppt = ppt;
    }

    /**
     * Gets the underlying {@link XSLFSlide}.
     *
     * @return The wrapped slide.
     */
    public XSLFSlide getSlide() {
        return this.slide;
    }

    /**
     * Adds a text box to this slide.
     *
     * @param text   The text content.
     * @param x      The x position in points.
     * @param y      The y position in points.
     * @param width  The width in points.
     * @param height The height in points.
     * @return A {@link PptShape} wrapping the created text box.
     */
    public PptShape addText(
            final String text,
            final double x,
            final double y,
            final double width,
            final double height) {
        return addText(text, x, y, width, height, null);
    }

    /**
     * Adds a text box to this slide with the specified style.
     *
     * @param text   The text content.
     * @param x      The x position in points.
     * @param y      The y position in points.
     * @param width  The width in points.
     * @param height The height in points.
     * @param style  The style to apply. May be {@code null} for default styling.
     * @return A {@link PptShape} wrapping the created text box.
     */
    public PptShape addText(
            final String text,
            final double x,
            final double y,
            final double width,
            final double height,
            final PptStyle style) {
        final XSLFTextBox textBox = this.slide.createTextBox();
        textBox.setAnchor(new Rectangle2D.Double(x, y, width, height));
        final XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        final XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(text);
        if (null != style) {
            style.fill(run);
            style.fill(paragraph);
        }
        return new PptShape(textBox);
    }

    /**
     * Adds a picture to this slide from a file.
     *
     * @param picFile The image file.
     * @param x       The x position in points.
     * @param y       The y position in points.
     * @param width   The width in points.
     * @param height  The height in points.
     * @return A {@link PptShape} wrapping the created picture shape.
     */
    public PptShape addPicture(
            final java.io.File picFile,
            final double x,
            final double y,
            final double width,
            final double height) {
        return addPicture(FileKit.readBytes(picFile), PptKit.toPictureType(picFile.getName()), x, y, width, height);
    }

    /**
     * Adds a picture to this slide from byte data.
     *
     * @param data    The image byte data.
     * @param picType The picture type.
     * @param x       The x position in points.
     * @param y       The y position in points.
     * @param width   The width in points.
     * @param height  The height in points.
     * @return A {@link PptShape} wrapping the created picture shape.
     */
    public PptShape addPicture(
            final byte[] data,
            final PictureData.PictureType picType,
            final double x,
            final double y,
            final double width,
            final double height) {
        try {
            final XSLFPictureData pictureData = this.ppt.addPicture(data, picType);
            final XSLFPictureShape pic = this.slide.createPicture(pictureData);
            pic.setAnchor(new Rectangle2D.Double(x, y, width, height));
            return new PptShape(pic);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Adds a table to this slide.
     *
     * @param data   The table data. Each element represents a row, and each row is an array of cell values.
     * @param x      The x position in points.
     * @param y      The y position in points.
     * @param width  The width in points.
     * @param height The height in points.
     * @return A {@link PptShape} wrapping the created table.
     */
    public PptShape addTable(
            final String[][] data,
            final double x,
            final double y,
            final double width,
            final double height) {
        if (null == data || data.length == 0) {
            return null;
        }
        final int numRows = data.length;
        final int numCols = data[0].length;
        final XSLFTable table = this.slide.createTable(numRows, numCols);
        table.setAnchor(new Rectangle2D.Double(x, y, width, height));
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols && col < data[row].length; col++) {
                table.getCell(row, col).setText(data[row][col]);
            }
        }
        return new PptShape(table);
    }

    /**
     * Adds an auto shape to this slide.
     *
     * @param x      The x position in points.
     * @param y      The y position in points.
     * @param width  The width in points.
     * @param height The height in points.
     * @return A {@link PptShape} wrapping the created auto shape.
     */
    public PptShape addShape(final double x, final double y, final double width, final double height) {
        final XSLFAutoShape shape = this.slide.createAutoShape();
        shape.setAnchor(new Rectangle2D.Double(x, y, width, height));
        return new PptShape(shape);
    }

    /**
     * Sets the background color of this slide.
     *
     * @param color The background color.
     * @return This {@code PptSlide} instance, for chaining.
     */
    public PptSlide setBackground(final Color color) {
        final XSLFBackground background = this.slide.getBackground();
        background.setFillColor(color);
        return this;
    }

    /**
     * Gets all shapes on this slide as {@link PptShape} wrappers.
     *
     * @return A list of {@link PptShape}.
     */
    public List<PptShape> getShapes() {
        final List<PptShape> result = new ArrayList<>();
        for (final XSLFShape shape : this.slide.getShapes()) {
            result.add(new PptShape(shape));
        }
        return result;
    }

    /**
     * Gets the slide layout name.
     *
     * @return The layout name, or {@code null} if no layout is set.
     */
    public String getLayoutName() {
        final XSLFSlideLayout layout = this.slide.getSlideLayout();
        return null != layout ? layout.getName() : null;
    }

    /**
     * Gets the slide number (1-based).
     *
     * @return The slide number.
     */
    public int getSlideNumber() {
        return this.slide.getSlideNumber();
    }

    /**
     * Gets all text content from this slide's shapes, concatenated.
     *
     * @return The combined text content.
     */
    public String getText() {
        final StringBuilder sb = new StringBuilder();
        for (final PptShape shape : getShapes()) {
            final PptShape.Data data = shape.toData();
            if (null != data.getText() && !data.getText().isEmpty()) {
                sb.append(data.getText()).append('\n');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Gets structured data for all shapes on this slide.
     *
     * @return A list of {@link PptShape.Data}.
     */
    public List<PptShape.Data> getShapeData() {
        final List<PptShape.Data> result = new ArrayList<>();
        for (final PptShape shape : getShapes()) {
            result.add(shape.toData());
        }
        return result;
    }

    /**
     * Gets structured data for table shapes only.
     *
     * @return A list of table {@link PptShape.Data}.
     */
    public List<PptShape.Data> getTableData() {
        final List<PptShape.Data> tables = new ArrayList<>();
        for (final PptShape shape : getShapes()) {
            if (shape.isTable()) {
                tables.add(shape.toData());
            }
        }
        return tables;
    }

    /**
     * Gets structured data for picture shapes only.
     *
     * @return A list of picture {@link PptShape.Data}.
     */
    public List<PptShape.Data> getPictureData() {
        final List<PptShape.Data> pictures = new ArrayList<>();
        for (final PptShape shape : getShapes()) {
            if (shape.isPictureShape()) {
                pictures.add(shape.toData());
            }
        }
        return pictures;
    }

}
