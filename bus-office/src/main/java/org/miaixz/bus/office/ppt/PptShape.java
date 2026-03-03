/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.ppt;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import org.apache.poi.xslf.usermodel.*;

/**
 * Wrapper around {@link XSLFShape} providing unified operations for text boxes, pictures, auto shapes, and tables on a
 * PowerPoint slide.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PptShape {

    /**
     * The underlying {@link XSLFShape} instance.
     */
    private final XSLFShape shape;

    /**
     * Constructs a new {@code PptShape} wrapping the given shape.
     *
     * @param shape The {@link XSLFShape} to wrap.
     */
    public PptShape(final XSLFShape shape) {
        this.shape = shape;
    }

    /**
     * Gets the underlying {@link XSLFShape}.
     *
     * @return The wrapped shape.
     */
    public XSLFShape getShape() {
        return this.shape;
    }

    /**
     * Sets the anchor (position and size) of this shape. Only applies to {@link XSLFSimpleShape} subclasses.
     *
     * @param x      The x position in points.
     * @param y      The y position in points.
     * @param width  The width in points.
     * @param height The height in points.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape setAnchor(final double x, final double y, final double width, final double height) {
        if (this.shape instanceof XSLFSimpleShape simpleShape) {
            simpleShape.setAnchor(new Rectangle2D.Double(x, y, width, height));
        }
        return this;
    }

    /**
     * Gets the anchor rectangle of this shape.
     *
     * @return The anchor as a {@link Rectangle2D}, or {@code null} if not a simple shape.
     */
    public Rectangle2D getAnchor() {
        if (this.shape instanceof XSLFSimpleShape simpleShape) {
            return simpleShape.getAnchor();
        }
        return null;
    }

    /**
     * Sets the rotation angle of this shape. Only applies to {@link XSLFSimpleShape} subclasses.
     *
     * @param angle The rotation angle in degrees.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape setRotation(final double angle) {
        if (this.shape instanceof XSLFSimpleShape simpleShape) {
            simpleShape.setRotation(angle);
        }
        return this;
    }

    /**
     * Sets text content if this shape is a text shape. Clears existing text and adds new content with optional style.
     *
     * @param text  The text content.
     * @param style The style to apply. May be {@code null} for default styling.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape setText(final String text, final PptStyle style) {
        if (this.shape instanceof XSLFTextShape textShape) {
            textShape.clearText();
            final XSLFTextParagraph paragraph = textShape.addNewTextParagraph();
            final XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(text);
            if (null != style) {
                style.fill(run);
                style.fill(paragraph);
            }
        }
        return this;
    }

    /**
     * Adds a text paragraph to this shape if it is a text shape.
     *
     * @param text  The text content.
     * @param style The style to apply. May be {@code null} for default styling.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape addText(final String text, final PptStyle style) {
        if (this.shape instanceof XSLFTextShape textShape) {
            final XSLFTextParagraph paragraph = textShape.addNewTextParagraph();
            final XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(text);
            if (null != style) {
                style.fill(run);
                style.fill(paragraph);
            }
        }
        return this;
    }

    /**
     * Sets the fill color of this shape if it is a simple shape.
     *
     * @param color The fill color.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape setFillColor(final Color color) {
        if (this.shape instanceof XSLFSimpleShape simpleShape) {
            simpleShape.setFillColor(color);
        }
        return this;
    }

    /**
     * Sets the line (border) color of this shape if it is a simple shape.
     *
     * @param color The line color.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape setLineColor(final Color color) {
        if (this.shape instanceof XSLFSimpleShape simpleShape) {
            simpleShape.setLineColor(color);
        }
        return this;
    }

    /**
     * Sets the line (border) width of this shape if it is a simple shape.
     *
     * @param width The line width in points.
     * @return This {@code PptShape} instance, for chaining.
     */
    public PptShape setLineWidth(final double width) {
        if (this.shape instanceof XSLFSimpleShape simpleShape) {
            simpleShape.setLineWidth(width);
        }
        return this;
    }

    /**
     * Gets the {@link Type} of this shape.
     *
     * @return The shape type.
     */
    public Type getType() {
        if (this.shape instanceof XSLFTable) {
            return Type.TABLE;
        } else if (this.shape instanceof XSLFPictureShape) {
            return Type.PICTURE;
        } else if (this.shape instanceof XSLFAutoShape) {
            return Type.AUTO_SHAPE;
        } else if (this.shape instanceof XSLFGroupShape) {
            return Type.GROUP;
        } else if (this.shape instanceof XSLFTextShape) {
            return Type.TEXT;
        }
        return Type.OTHER;
    }

    /**
     * Checks if this shape is a text shape.
     *
     * @return {@code true} if the shape is a text shape.
     */
    public boolean isTextShape() {
        return this.shape instanceof XSLFTextShape;
    }

    /**
     * Checks if this shape is a picture shape.
     *
     * @return {@code true} if the shape is a picture shape.
     */
    public boolean isPictureShape() {
        return this.shape instanceof XSLFPictureShape;
    }

    /**
     * Checks if this shape is a table.
     *
     * @return {@code true} if the shape is a table.
     */
    public boolean isTable() {
        return this.shape instanceof XSLFTable;
    }

    /**
     * Checks if this shape is an auto shape.
     *
     * @return {@code true} if the shape is an auto shape.
     */
    public boolean isAutoShape() {
        return this.shape instanceof XSLFAutoShape;
    }

    /**
     * Returns the shape as a text shape, or {@code null} if it is not one.
     *
     * @return The {@link XSLFTextShape}, or {@code null}.
     */
    public XSLFTextShape asTextShape() {
        return this.shape instanceof XSLFTextShape ts ? ts : null;
    }

    /**
     * Returns the shape as a picture shape, or {@code null} if it is not one.
     *
     * @return The {@link XSLFPictureShape}, or {@code null}.
     */
    public XSLFPictureShape asPictureShape() {
        return this.shape instanceof XSLFPictureShape ps ? ps : null;
    }

    /**
     * Returns the shape as a table, or {@code null} if it is not one.
     *
     * @return The {@link XSLFTable}, or {@code null}.
     */
    public XSLFTable asTable() {
        return this.shape instanceof XSLFTable t ? t : null;
    }

    /**
     * Returns the shape as an auto shape, or {@code null} if it is not one.
     *
     * @return The {@link XSLFAutoShape}, or {@code null}.
     */
    public XSLFAutoShape asAutoShape() {
        return this.shape instanceof XSLFAutoShape as ? as : null;
    }

    /**
     * Extracts structured data from this shape into a {@link Data} object.
     *
     * @return The extracted {@link Data}.
     */
    public Data toData() {
        final Data sd = new Data();
        sd.setName(this.shape.getShapeName());
        sd.setType(getType());
        sd.setAnchor(getAnchor());

        // Extract text for all text-capable shapes
        if (this.shape instanceof XSLFTextShape textShape) {
            final StringBuilder sb = new StringBuilder();
            for (final XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                sb.append(paragraph.getText()).append('\n');
            }
            sd.setText(sb.toString().trim());
        }

        // Extract table data
        if (this.shape instanceof XSLFTable table) {
            final int rows = table.getNumberOfRows();
            final int cols = table.getNumberOfColumns();
            final String[][] tableData = new String[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    tableData[r][c] = table.getCell(r, c).getText();
                }
            }
            sd.setTableData(tableData);
        }

        // Extract picture metadata
        if (this.shape instanceof XSLFPictureShape picShape) {
            final XSLFPictureData picData = picShape.getPictureData();
            if (null != picData) {
                sd.setPictureContentType(picData.getContentType());
                sd.setPictureFileName(picData.getFileName());
            }
        }

        return sd;
    }

    /**
     * Shape type enumeration.
     */
    public enum Type {
        /**
         * Text box or text shape.
         */
        TEXT,
        /**
         * Picture/image shape.
         */
        PICTURE,
        /**
         * Table shape.
         */
        TABLE,
        /**
         * Auto shape (rectangle, oval, etc.).
         */
        AUTO_SHAPE,
        /**
         * Group shape.
         */
        GROUP,
        /**
         * Other/unknown shape type.
         */
        OTHER
    }

    /**
     * Structured data extracted from a single shape, including type, position, text content, table data, and picture
     * metadata.
     */
    public static class Data {

        /**
         * The shape type.
         */
        private Type type;

        /**
         * The shape name.
         */
        private String name;

        /**
         * The text content (for text shapes and auto shapes).
         */
        private String text;

        /**
         * The anchor position and size.
         */
        private Rectangle2D anchor;

        /**
         * Table data as a 2D string array (for table shapes only).
         */
        private String[][] tableData;

        /**
         * The picture content type (e.g., "image/png") for picture shapes.
         */
        private String pictureContentType;

        /**
         * The picture file name for picture shapes.
         */
        private String pictureFileName;

        /**
         * Gets the shape type.
         *
         * @return The {@link Type}.
         */
        public Type getType() {
            return type;
        }

        /**
         * Sets the shape type.
         *
         * @param type The shape type.
         */
        public void setType(final Type type) {
            this.type = type;
        }

        /**
         * Gets the shape name.
         *
         * @return The shape name.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the shape name.
         *
         * @param name The shape name.
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Gets the text content.
         *
         * @return The text content, or {@code null} if not a text shape.
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the text content.
         *
         * @param text The text content.
         */
        public void setText(final String text) {
            this.text = text;
        }

        /**
         * Gets the anchor rectangle.
         *
         * @return The anchor as a {@link Rectangle2D}.
         */
        public Rectangle2D getAnchor() {
            return anchor;
        }

        /**
         * Sets the anchor rectangle.
         *
         * @param anchor The anchor rectangle.
         */
        public void setAnchor(final Rectangle2D anchor) {
            this.anchor = anchor;
        }

        /**
         * Gets the table data.
         *
         * @return A 2D string array, or {@code null} if not a table shape.
         */
        public String[][] getTableData() {
            return tableData;
        }

        /**
         * Sets the table data.
         *
         * @param tableData The 2D string array.
         */
        public void setTableData(final String[][] tableData) {
            this.tableData = tableData;
        }

        /**
         * Gets the picture content type.
         *
         * @return The content type string, or {@code null} if not a picture shape.
         */
        public String getPictureContentType() {
            return pictureContentType;
        }

        /**
         * Sets the picture content type.
         *
         * @param pictureContentType The content type string.
         */
        public void setPictureContentType(final String pictureContentType) {
            this.pictureContentType = pictureContentType;
        }

        /**
         * Gets the picture file name.
         *
         * @return The file name, or {@code null} if not a picture shape.
         */
        public String getPictureFileName() {
            return pictureFileName;
        }

        /**
         * Sets the picture file name.
         *
         * @param pictureFileName The file name.
         */
        public void setPictureFileName(final String pictureFileName) {
            this.pictureFileName = pictureFileName;
        }

    }

}
