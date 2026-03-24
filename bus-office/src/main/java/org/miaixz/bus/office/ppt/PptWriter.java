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
import java.io.*;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * PowerPoint PPTX document generator.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PptWriter implements Closeable {

    /**
     * The underlying {@link XMLSlideShow} instance.
     */
    private final XMLSlideShow ppt;
    /**
     * The target file to write the presentation to.
     */
    protected File targetFile;
    /**
     * Flag indicating whether the writer has been closed.
     */
    protected boolean isClosed;

    /**
     * Constructs a new {@code PptWriter} with a new blank presentation.
     */
    public PptWriter() {
        this(new XMLSlideShow());
    }

    /**
     * Constructs a new {@code PptWriter} for the specified target file. If the file exists, it will be opened;
     * otherwise, a new presentation will be created.
     *
     * @param targetFile The file to write the presentation to.
     */
    public PptWriter(final File targetFile) {
        this(PptKit.create(targetFile), targetFile);
    }

    /**
     * Constructs a new {@code PptWriter} with the given {@link XMLSlideShow}.
     *
     * @param ppt The {@link XMLSlideShow} to wrap.
     */
    public PptWriter(final XMLSlideShow ppt) {
        this(ppt, null);
    }

    /**
     * Constructs a new {@code PptWriter} with the given {@link XMLSlideShow} and target file.
     *
     * @param ppt        The {@link XMLSlideShow} to wrap.
     * @param targetFile The file to write the presentation to.
     */
    public PptWriter(final XMLSlideShow ppt, final File targetFile) {
        this.ppt = ppt;
        this.targetFile = targetFile;
    }

    /**
     * Gets the underlying {@link XMLSlideShow}.
     *
     * @return The {@link XMLSlideShow} instance.
     */
    public XMLSlideShow getPpt() {
        return this.ppt;
    }

    /**
     * Sets the target file to write the presentation to.
     *
     * @param targetFile The target file.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter setTargetFile(final File targetFile) {
        this.targetFile = targetFile;
        return this;
    }

    /**
     * Sets the slide size in points.
     *
     * @param width  The width in points.
     * @param height The height in points.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter setSlideSize(final double width, final double height) {
        this.ppt.setPageSize(new Dimension((int) width, (int) height));
        return this;
    }

    /**
     * Adds a new blank slide to the presentation.
     *
     * @return A {@link PptSlide} wrapping the newly created slide.
     */
    public PptSlide addSlide() {
        return new PptSlide(this.ppt.createSlide(), this.ppt);
    }

    /**
     * Adds a new slide with the specified layout.
     *
     * @param layout The {@link XSLFSlideLayout} to use.
     * @return A {@link PptSlide} wrapping the newly created slide.
     */
    public PptSlide addSlide(final XSLFSlideLayout layout) {
        return new PptSlide(this.ppt.createSlide(layout), this.ppt);
    }

    /**
     * Adds a text box with the specified text to the given slide.
     *
     * @param slide  The slide to add the text box to.
     * @param text   The text content.
     * @param x      The x position in pixels.
     * @param y      The y position in pixels.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter addText(
            final XSLFSlide slide,
            final String text,
            final double x,
            final double y,
            final double width,
            final double height) {
        return addText(slide, text, x, y, width, height, null, 0, null);
    }

    /**
     * Adds a text box with the specified text and font settings to the given slide.
     *
     * @param slide    The slide to add the text box to.
     * @param text     The text content.
     * @param x        The x position in pixels.
     * @param y        The y position in pixels.
     * @param width    The width in pixels.
     * @param height   The height in pixels.
     * @param fontName The font family name. May be {@code null} for default.
     * @param fontSize The font size in points. Use 0 for default.
     * @param color    The font color. May be {@code null} for default.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter addText(
            final XSLFSlide slide,
            final String text,
            final double x,
            final double y,
            final double width,
            final double height,
            final String fontName,
            final double fontSize,
            final Color color) {
        return addText(slide, text, x, y, width, height, fontName, fontSize, color, null);
    }

    /**
     * Adds a text box with the specified text, font settings, and alignment to the given slide.
     *
     * @param slide    The slide to add the text box to.
     * @param text     The text content.
     * @param x        The x position in pixels.
     * @param y        The y position in pixels.
     * @param width    The width in pixels.
     * @param height   The height in pixels.
     * @param fontName The font family name. May be {@code null} for default.
     * @param fontSize The font size in points. Use 0 for default.
     * @param color    The font color. May be {@code null} for default.
     * @param align    The text alignment. May be {@code null} for default.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter addText(
            final XSLFSlide slide,
            final String text,
            final double x,
            final double y,
            final double width,
            final double height,
            final String fontName,
            final double fontSize,
            final Color color,
            final TextParagraph.TextAlign align) {
        final XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new java.awt.geom.Rectangle2D.Double(x, y, width, height));
        final XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        if (null != align) {
            paragraph.setTextAlign(align);
        }
        final XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(text);
        if (null != fontName) {
            run.setFontFamily(fontName);
        }
        if (fontSize > 0) {
            run.setFontSize(fontSize);
        }
        if (null != color) {
            run.setFontColor(color);
        }
        return this;
    }

    /**
     * Adds a picture to the given slide.
     *
     * @param slide   The slide to add the picture to.
     * @param picFile The image file.
     * @param x       The x position in pixels.
     * @param y       The y position in pixels.
     * @param width   The width in pixels.
     * @param height  The height in pixels.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter addPicture(
            final XSLFSlide slide,
            final File picFile,
            final double x,
            final double y,
            final double width,
            final double height) {
        return addPicture(
                slide,
                FileKit.readBytes(picFile),
                PptKit.toPictureType(picFile.getName()),
                x,
                y,
                width,
                height);
    }

    /**
     * Adds a picture to the given slide from byte data.
     *
     * @param slide   The slide to add the picture to.
     * @param data    The image byte data.
     * @param picType The picture type.
     * @param x       The x position in pixels.
     * @param y       The y position in pixels.
     * @param width   The width in pixels.
     * @param height  The height in pixels.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter addPicture(
            final XSLFSlide slide,
            final byte[] data,
            final PictureData.PictureType picType,
            final double x,
            final double y,
            final double width,
            final double height) {
        try {
            final XSLFPictureData pictureData = this.ppt.addPicture(data, picType);
            final XSLFPictureShape pic = slide.createPicture(pictureData);
            pic.setAnchor(new java.awt.geom.Rectangle2D.Double(x, y, width, height));
        } catch (final Exception e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Adds a table to the given slide.
     *
     * @param slide  The slide to add the table to.
     * @param data   The table data. Each element represents a row, and each row is an array of cell values.
     * @param x      The x position in pixels.
     * @param y      The y position in pixels.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @return This {@code PptWriter} instance, for chaining.
     */
    public PptWriter addTable(
            final XSLFSlide slide,
            final String[][] data,
            final double x,
            final double y,
            final double width,
            final double height) {
        if (null == data || data.length == 0) {
            return this;
        }
        final int numRows = data.length;
        final int numCols = data[0].length;
        final XSLFTable table = slide.createTable(numRows, numCols);
        table.setAnchor(new java.awt.geom.Rectangle2D.Double(x, y, width, height));
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols && col < data[row].length; col++) {
                table.getCell(row, col).setText(data[row][col]);
            }
        }
        return this;
    }

    /**
     * Adds a rectangle shape to the given slide.
     *
     * @param slide  The slide to add the shape to.
     * @param x      The x position in pixels.
     * @param y      The y position in pixels.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @return The created {@link XSLFAutoShape}.
     */
    public XSLFAutoShape addShape(
            final XSLFSlide slide,
            final double x,
            final double y,
            final double width,
            final double height) {
        final XSLFAutoShape shape = slide.createAutoShape();
        shape.setAnchor(new java.awt.geom.Rectangle2D.Double(x, y, width, height));
        return shape;
    }

    /**
     * Gets a slide layout by name from the first slide master.
     *
     * @param name The layout name (e.g., "Title Slide", "Title and Content", "Blank").
     * @return The {@link XSLFSlideLayout}, or {@code null} if not found.
     */
    public XSLFSlideLayout getLayout(final String name) {
        for (final XSLFSlideMaster master : this.ppt.getSlideMasters()) {
            final XSLFSlideLayout layout = master.getLayout(name);
            if (null != layout) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Flushes the presentation to the pre-defined target file.
     *
     * @return This {@code PptWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public PptWriter flush() throws InternalException {
        return flush(this.targetFile);
    }

    /**
     * Flushes the presentation to the specified file.
     *
     * @param destFile The file to write the presentation to.
     * @return This {@code PptWriter} instance, for chaining.
     * @throws InternalException    If an I/O error occurs.
     * @throws NullPointerException if {@code destFile} is {@code null}.
     */
    public PptWriter flush(final File destFile) throws InternalException {
        Assert.notNull(
                destFile,
                "[destFile] is null, and you must call setTargetFile(File) first or call flush(OutputStream).");
        return flush(FileKit.getOutputStream(destFile), true);
    }

    /**
     * Flushes the presentation to the specified output stream.
     *
     * @param out The output stream to write the presentation to.
     * @return This {@code PptWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public PptWriter flush(final OutputStream out) throws InternalException {
        return flush(out, false);
    }

    /**
     * Flushes the presentation to the specified output stream.
     *
     * @param out        The output stream to write the presentation to.
     * @param isCloseOut {@code true} to close the output stream after flushing, {@code false} otherwise.
     * @return This {@code PptWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public PptWriter flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        Assert.isFalse(this.isClosed, "PptWriter has been closed!");
        try {
            this.ppt.write(out);
            out.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                IoKit.closeQuietly(out);
            }
        }
        return this;
    }

    /**
     * Closes the presentation. If a target file is set, the presentation will be flushed to it before closing.
     */
    @Override
    public void close() {
        if (null != this.targetFile) {
            flush();
        }
        closeWithoutFlush();
    }

    /**
     * Closes the presentation without flushing its content.
     */
    protected void closeWithoutFlush() {
        IoKit.closeQuietly(this.ppt);
        this.isClosed = true;
    }

}
