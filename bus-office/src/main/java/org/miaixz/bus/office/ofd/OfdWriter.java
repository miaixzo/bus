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
 * @since Java 21+
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

    /**
     * Implements the behavior defined by the supertype.
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.doc);
    }

}
