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
package org.miaixz.bus.office.ofd;

import java.io.*;
import java.nio.file.Path;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.ofdrw.font.Font;
import org.ofdrw.layout.OFDDoc;
import org.ofdrw.layout.edit.Annotation;
import org.ofdrw.layout.element.Div;
import org.ofdrw.layout.element.Img;
import org.ofdrw.layout.element.Paragraph;
import org.ofdrw.reader.OFDReader;

/**
 * OFD file generator.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OfdWriter implements Serializable, Closeable {

    @Serial
    private static final long serialVersionUID = 2852287375522L;

    /**
     * The OFD document instance.
     */
    private final OFDDoc doc;

    /**
     * Constructs an {@code OfdWriter} for the specified file.
     *
     * @param file The file to generate the OFD document into.
     */
    public OfdWriter(final File file) {
        this(file.toPath());
    }

    /**
     * Constructs an {@code OfdWriter} for the specified path.
     *
     * @param file The path to generate the OFD document into.
     * @throws InternalException if an I/O error occurs during document creation or opening.
     */
    public OfdWriter(final Path file) {
        try {
            if (PathResolve.exists(file, true)) {
                this.doc = new OFDDoc(new OFDReader(file), file);
            } else {
                this.doc = new OFDDoc(file);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Constructs an {@code OfdWriter} that writes to the given output stream.
     *
     * @param out The output stream to write the OFD document to.
     */
    public OfdWriter(final OutputStream out) {
        this.doc = new OFDDoc(out);
    }

    /**
     * Adds text content to the OFD document.
     *
     * @param font  The font to use for the text. May be {@code null} to use the default font.
     * @param texts An array of text strings to add.
     * @return This {@code OfdWriter} instance, for chaining.
     */
    public OfdWriter addText(final Font font, final String... texts) {
        final Paragraph paragraph = new Paragraph();
        if (null != font) {
            paragraph.setDefaultFont(font);
        }
        for (final String text : texts) {
            paragraph.add(text);
        }
        return add(paragraph);
    }

    /**
     * Appends an image to the OFD document.
     *
     * @param picFile The image file.
     * @param width   The width of the image.
     * @param height  The height of the image.
     * @return This {@code OfdWriter} instance, for chaining.
     */
    public OfdWriter addPicture(final File picFile, final int width, final int height) {
        return addPicture(picFile.toPath(), width, height);
    }

    /**
     * Appends an image to the OFD document.
     *
     * @param picFile The path to the image file.
     * @param width   The width of the image.
     * @param height  The height of the image.
     * @return This {@code OfdWriter} instance, for chaining.
     * @throws InternalException if an I/O error occurs while creating the image element.
     */
    public OfdWriter addPicture(final Path picFile, final int width, final int height) {
        final Img img;
        try {
            img = new Img(width, height, picFile);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return add(img);
    }

    /**
     * Adds a Div element to the OFD document.
     *
     * @param div The Div element to add, which can be a paragraph, Canvas, Img, or fill.
     * @return This {@code OfdWriter} instance, for chaining.
     */
    public OfdWriter add(final Div div) {
        this.doc.add(div);
        return this;
    }

    /**
     * Adds an annotation, such as a watermark, to a specific page.
     *
     * @param page       The page number to add the annotation to.
     * @param annotation The annotation element to add, which can be a paragraph, Canvas, Img, or fill.
     * @return This {@code OfdWriter} instance, for chaining.
     * @throws InternalException if an I/O error occurs while adding the annotation.
     */
    public OfdWriter add(final int page, final Annotation annotation) {
        try {
            this.doc.addAnnotation(page, annotation);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.doc);
    }

}
