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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xslf.usermodel.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * PowerPoint PPTX document reader.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PptReader implements Closeable {

    /**
     * The underlying {@link XMLSlideShow} instance.
     */
    private final XMLSlideShow ppt;

    /**
     * Constructs a new {@code PptReader} from a file.
     *
     * @param file The PPTX file to read.
     */
    public PptReader(final File file) {
        this(PptKit.create(file));
    }

    /**
     * Constructs a new {@code PptReader} from an input stream.
     *
     * @param in The input stream of the PPTX file.
     */
    public PptReader(final InputStream in) {
        try {
            this.ppt = new XMLSlideShow(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Constructs a new {@code PptReader} with the given {@link XMLSlideShow}.
     *
     * @param ppt The {@link XMLSlideShow} to wrap.
     */
    public PptReader(final XMLSlideShow ppt) {
        this.ppt = ppt;
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
     * Gets the number of slides in the presentation.
     *
     * @return The slide count.
     */
    public int getSlideCount() {
        return this.ppt.getSlides().size();
    }

    /**
     * Gets all slides in the presentation.
     *
     * @return A list of {@link XSLFSlide}.
     */
    public List<XSLFSlide> getSlides() {
        return this.ppt.getSlides();
    }

    /**
     * Gets a slide by index (0-based).
     *
     * @param index The slide index.
     * @return The {@link XSLFSlide} at the specified index.
     */
    public XSLFSlide getSlide(final int index) {
        return this.ppt.getSlides().get(index);
    }

    /**
     * Gets a slide by index (0-based) as a {@link PptSlide} wrapper.
     *
     * @param index The slide index.
     * @return A {@link PptSlide} wrapping the slide at the specified index.
     */
    public PptSlide getPptSlide(final int index) {
        return new PptSlide(getSlide(index), this.ppt);
    }

    /**
     * Gets all slides as {@link PptSlide} wrappers.
     *
     * @return A list of {@link PptSlide}.
     */
    public List<PptSlide> getPptSlides() {
        final List<PptSlide> result = new ArrayList<>();
        for (final XSLFSlide slide : this.ppt.getSlides()) {
            result.add(new PptSlide(slide, this.ppt));
        }
        return result;
    }

    /**
     * Reads all text content from all slides.
     *
     * @return A list of strings, one per slide, containing all text from that slide.
     */
    public List<String> readText() {
        final List<String> result = new ArrayList<>();
        for (final XSLFSlide slide : this.ppt.getSlides()) {
            result.add(readSlideText(slide));
        }
        return result;
    }

    /**
     * Reads all text content from a specific slide.
     *
     * @param index The slide index (0-based).
     * @return The text content of the slide.
     */
    public String readText(final int index) {
        return readSlideText(getSlide(index));
    }

    /**
     * Reads all text content from a given slide.
     *
     * @param slide The {@link XSLFSlide} to read text from.
     * @return The text content of the slide.
     */
    public static String readSlideText(final XSLFSlide slide) {
        final StringBuilder sb = new StringBuilder();
        for (final XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape textShape) {
                for (final XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                    sb.append(paragraph.getText()).append('\n');
                }
            }
        }
        return sb.toString().trim();
    }

    /**
     * Gets all picture data from the presentation.
     *
     * @return A list of {@link XSLFPictureData}.
     */
    public List<XSLFPictureData> getPictures() {
        return this.ppt.getPictureData();
    }

    /**
     * Gets all shapes from a specific slide.
     *
     * @param index The slide index (0-based).
     * @return A list of {@link XSLFShape} on the slide.
     */
    public List<XSLFShape> getShapes(final int index) {
        return getSlide(index).getShapes();
    }

    /**
     * Gets all tables from a specific slide.
     *
     * @param index The slide index (0-based).
     * @return A list of {@link XSLFTable} on the slide.
     */
    public List<XSLFTable> getTables(final int index) {
        final List<XSLFTable> tables = new ArrayList<>();
        for (final XSLFShape shape : getSlide(index).getShapes()) {
            if (shape instanceof XSLFTable table) {
                tables.add(table);
            }
        }
        return tables;
    }

    /**
     * Gets the slide size as a {@link java.awt.Dimension}.
     *
     * @return The slide size.
     */
    public java.awt.Dimension getSlideSize() {
        return this.ppt.getPageSize();
    }

    /**
     * Creates a {@link PptWriter} from this reader's presentation for modification.
     *
     * @return A new {@link PptWriter} wrapping the same {@link XMLSlideShow}.
     */
    public PptWriter getWriter() {
        return new PptWriter(this.ppt);
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.ppt);
    }

}
