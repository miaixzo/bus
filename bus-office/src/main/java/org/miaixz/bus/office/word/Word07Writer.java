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
package org.miaixz.bus.office.word;

import java.io.*;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Word DOCX document generator.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Word07Writer implements Closeable {

    /**
     * The underlying {@link XWPFDocument} instance.
     */
    private final XWPFDocument doc;
    /**
     * The target file to write the document to.
     */
    protected File targetFile;
    /**
     * Flag indicating whether the writer has been closed.
     */
    protected boolean isClosed;

    /**
     * Constructs a new {@code Word07Writer} with a new blank document.
     */
    public Word07Writer() {
        this(new XWPFDocument());
    }

    /**
     * Constructs a new {@code Word07Writer} for the specified target file. If the file exists, it will be opened;
     * otherwise, a new document will be created.
     *
     * @param targetFile The file to write the document to.
     */
    public Word07Writer(final File targetFile) {
        this(WordKit.create(targetFile), targetFile);
    }

    /**
     * Constructs a new {@code Word07Writer} with the given {@link XWPFDocument}.
     *
     * @param doc The {@link XWPFDocument} to wrap.
     */
    public Word07Writer(final XWPFDocument doc) {
        this(doc, null);
    }

    /**
     * Constructs a new {@code Word07Writer} with the given {@link XWPFDocument} and target file.
     *
     * @param doc        The {@link XWPFDocument} to wrap.
     * @param targetFile The file to write the document to.
     */
    public Word07Writer(final XWPFDocument doc, final File targetFile) {
        this.doc = doc;
        this.targetFile = targetFile;
    }

    /**
     * Gets the underlying {@link XWPFDocument}.
     *
     * @return The {@link XWPFDocument} instance.
     */
    public XWPFDocument getDoc() {
        return this.doc;
    }

    /**
     * Sets the target file to write the document to.
     *
     * @param targetFile The target file.
     * @return This {@code Word07Writer} instance, for chaining.
     */
    public Word07Writer setTargetFile(final File targetFile) {
        this.targetFile = targetFile;
        return this;
    }

    /**
     * Adds a paragraph with the specified font and text content.
     *
     * @param font  The font information ({@link FontStyle}). May be {@code null} to use default font settings.
     * @param texts The text content for the paragraph. Multiple strings will be concatenated within the same paragraph.
     * @return This {@code Word07Writer} instance, for chaining.
     */
    public Word07Writer addText(final FontStyle font, final String... texts) {
        return addText(null, font, texts);
    }

    /**
     * Adds a paragraph with the specified alignment, font, and text content.
     *
     * @param align The paragraph alignment ({@link ParagraphAlignment}). May be {@code null} for default alignment.
     * @param font  The font information ({@link FontStyle}). May be {@code null} to use default font settings.
     * @param texts The text content for the paragraph. Multiple strings will be concatenated within the same paragraph.
     * @return This {@code Word07Writer} instance, for chaining.
     */
    public Word07Writer addText(final ParagraphAlignment align, final FontStyle font, final String... texts) {
        final XWPFParagraph p = this.doc.createParagraph();
        if (null != align) {
            p.setAlignment(align);
        }
        if (ArrayKit.isNotEmpty(texts)) {
            XWPFRun run;
            for (final String text : texts) {
                run = p.createRun();
                run.setText(text);
                if (null != font) {
                    font.fill(run);
                }
            }
        }
        return this;
    }

    /**
     * Adds table data to the document.
     *
     * @param data Table data, representing multiple rows. Each element can be a collection or array for a row, or a
     *             Map/Bean where keys represent headers and values are data.
     * @return This {@code Word07Writer} instance, for chaining.
     * @see WordTable#createTable(XWPFDocument, Iterable)
     */
    public Word07Writer addTable(final Iterable<?> data) {
        WordTable.createTable(this.doc, data);
        return this;
    }

    /**
     * Adds an image as a standalone paragraph.
     *
     * @param picFile The image file.
     * @param width   The width of the image in pixels.
     * @param height  The height of the image in pixels.
     * @return This {@code Word07Writer} instance, for chaining.
     */
    public Word07Writer addPicture(final File picFile, final int width, final int height) {
        final String fileName = picFile.getName();
        return addPicture(FileKit.getInputStream(picFile), WordKit.getType(fileName), fileName, width, height);
    }

    /**
     * Adds an image as a standalone paragraph. The image stream will be closed after adding. Default alignment is
     * center.
     *
     * @param in       The image input stream.
     * @param picType  The picture type, see {@link PictureType}.
     * @param fileName The file name of the image.
     * @param width    The width of the image in pixels.
     * @param height   The height of the image in pixels.
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException if an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    public Word07Writer addPicture(
            final InputStream in,
            final PictureType picType,
            final String fileName,
            final int width,
            final int height) {
        return addPicture(in, picType, fileName, width, height, ParagraphAlignment.CENTER);
    }

    /**
     * Adds an image as a standalone paragraph. The image stream will be closed after adding.
     *
     * @param in       The image input stream.
     * @param picType  The picture type, see {@link PictureType}.
     * @param fileName The file name of the image.
     * @param width    The width of the image in pixels.
     * @param height   The height of the image in pixels.
     * @param align    The alignment of the image within the paragraph.
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException if an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    public Word07Writer addPicture(
            final InputStream in,
            final PictureType picType,
            final String fileName,
            final int width,
            final int height,
            final ParagraphAlignment align) {
        final XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(align);
        final XWPFRun run = paragraph.createRun();
        try {
            run.addPicture(in, picType, fileName, Units.toEMU(width), Units.toEMU(height));
        } catch (final InvalidFormatException | IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }

        return this;
    }

    /**
     * Adds multiple images as standalone paragraphs. The image streams will be closed after adding.
     *
     * @param width    The uniform width for all images in pixels.
     * @param height   The uniform height for all images in pixels.
     * @param picFiles An array of image files.
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException if an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    public Word07Writer addPictures(final int width, final int height, final File... picFiles) {
        final XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run;
        try {
            for (final File picFile : picFiles) {
                run = paragraph.createRun();
                final String name = picFile.getName();
                try (final BufferedInputStream in = FileKit.getInputStream(picFile)) {
                    run.addPicture(in, WordKit.getType(name), name, Units.toEMU(width), Units.toEMU(height));
                }
            }
        } catch (final InvalidFormatException | IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Flushes the Word Workbook to the pre-defined target file. If no target file is defined, a
     * {@link NullPointerException} will be thrown. The target file can be set using {@link #setTargetFile(File)} or
     * defined during construction.
     *
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public Word07Writer flush() throws InternalException {
        return flush(this.targetFile);
    }

    /**
     * Flushes the Word Workbook to the specified file. If no target file is defined, a {@link NullPointerException}
     * will be thrown.
     *
     * @param destFile The file to write the document to.
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException    If an I/O error occurs.
     * @throws NullPointerException if {@code destFile} is {@code null}.
     */
    public Word07Writer flush(final File destFile) throws InternalException {
        Assert.notNull(
                destFile,
                "[destFile] is null, and you must call setDestFile(File) first or call flush(OutputStream).");
        return flush(FileKit.getOutputStream(destFile), true);
    }

    /**
     * Flushes the Word Workbook to the specified output stream.
     *
     * @param out The output stream to write the document to.
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public Word07Writer flush(final OutputStream out) throws InternalException {
        return flush(out, false);
    }

    /**
     * Flushes the Word Document to the specified output stream.
     *
     * @param out        The output stream to write the document to.
     * @param isCloseOut {@code true} to close the output stream after flushing, {@code false} otherwise.
     * @return This {@code Word07Writer} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public Word07Writer flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        Assert.isFalse(this.isClosed, "WordWriter has been closed!");
        try {
            this.doc.write(out);
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
     * Closes the Word document. If a target file is set, the document will be flushed to it before closing.
     */
    @Override
    public void close() {
        if (null != this.targetFile) {
            flush();
        }
        closeWithoutFlush();
    }

    /**
     * Closes the Word document without flushing its content.
     */
    protected void closeWithoutFlush() {
        IoKit.closeQuietly(this.doc);
        this.isClosed = true;
    }

}
