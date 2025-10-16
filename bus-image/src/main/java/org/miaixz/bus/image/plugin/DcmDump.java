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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.io.ImageInputHandler;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;

import java.io.IOException;

/**
 * The {@code DcmDump} class provides a utility to print a textual representation of a DICOM file's structure and
 * content. It implements the {@link ImageInputHandler} interface to process the DICOM stream and format the output.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DcmDump implements ImageInputHandler {

    /**
     * Default number of characters per line for the formatted output.
     */
    private static final int DEFAULT_WIDTH = 78;

    /**
     * The maximum width of a line in the output.
     */
    private int width = DEFAULT_WIDTH;

    /**
     * Gets the current line width for the output.
     *
     * @return The line width.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Sets the line width for the output.
     *
     * @param width The desired line width. Must be at least 40.
     * @throws IllegalArgumentException if the width is less than 40.
     */
    public final void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException("width must be >= 40");
        this.width = width;
    }

    /**
     * Parses a DICOM input stream and prints its contents to standard output.
     *
     * @param dis The DICOM input stream to parse.
     * @throws IOException if an I/O error occurs.
     */
    public void parse(ImageInputStream dis) throws IOException {
        dis.setDicomInputHandler(this);
        dis.readDataset();
    }

    /**
     * Called at the beginning of a dataset. Prints the preamble if it exists.
     *
     * @param dis The DICOM input stream.
     */
    @Override
    public void startDataset(ImageInputStream dis) {
        promptPreamble(dis.getPreamble());
    }

    /**
     * Called at the end of a dataset. This implementation does nothing.
     *
     * @param dis The DICOM input stream.
     */
    @Override
    public void endDataset(ImageInputStream dis) {
        // No action needed at the end of the dataset.
    }

    /**
     * Called for each data element in the dataset. It formats and prints the element's information.
     *
     * @param dis   The DICOM input stream.
     * @param attrs The attributes of the current dataset, used for context (e.g., private creators).
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void readValue(ImageInputStream dis, Attributes attrs) throws IOException {
        StringBuilder line = new StringBuilder(width + 30);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        VR vr = dis.vr();
        int vallen = dis.length();
        boolean undeflen = vallen == -1;
        int tag = dis.tag();
        String privateCreator = attrs.getPrivateCreator(tag);
        if (vr == VR.SQ || undeflen) {
            appendKeyword(dis, privateCreator, line);
            System.out.println(line);
            dis.readValue(dis, attrs);
            if (undeflen) {
                line.setLength(0);
                appendPrefix(dis, line);
                appendHeader(dis, line);
                appendKeyword(dis, privateCreator, line);
                System.out.println(line);
            }
            return;
        }
        byte[] b = probeValue(dis);
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), attrs.getSpecificCharacterSet(), width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, privateCreator, line);
        }
        System.out.println(line);
        if (tag == Tag.FileMetaInformationGroupLength)
            dis.setFileMetaInformationGroupLength(b);
        else if (tag == Tag.TransferSyntaxUID || tag == Tag.SpecificCharacterSet || tag == Tag.PixelRepresentation
                || Tag.isPrivateCreator(tag))
            attrs.setBytes(tag, vr, b);
    }

    /**
     * Reads a portion of the data element's value for display.
     *
     * @param dis The DICOM input stream.
     * @return A byte array containing the probed value.
     * @throws IOException if an I/O error occurs.
     */
    private byte[] probeValue(ImageInputStream dis) throws IOException {
        long len = dis.unsignedLength();
        if (len == 0)
            return Normal.EMPTY_BYTE_ARRAY;
        int read = (int) Math.min(len, (width + 7) & ~7);
        byte[] b = new byte[read];
        dis.readFully(b);
        dis.skipFully(len - read);
        return b;
    }

    /**
     * Called for each item in a sequence. Formats and prints the item's header.
     *
     * @param dis The DICOM input stream.
     * @param seq The sequence to which the item belongs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
        String privateCreator = seq.getParent().getPrivateCreator(dis.tag());
        StringBuilder line = new StringBuilder(width);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        appendKeyword(dis, privateCreator, line);
        appendNumber(seq.size() + 1, line);
        System.out.println(line);
        boolean undeflen = dis.length() == -1;
        dis.readValue(dis, seq);
        if (undeflen) {
            line.setLength(0);
            appendPrefix(dis, line);
            appendHeader(dis, line);
            appendKeyword(dis, privateCreator, line);
            System.out.println(line);
        }
    }

    /**
     * Called for each fragment in a pixel data sequence. Formats and prints the fragment's information.
     *
     * @param dis   The DICOM input stream.
     * @param frags The fragments sequence.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
        StringBuilder line = new StringBuilder(width + 20);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        System.out.println(line);
        appendFragment(line, dis, frags.vr());
    }

    /**
     * Appends the prefix to the line, indicating file position and nesting level.
     *
     * @param dis  The DICOM input stream.
     * @param line The StringBuilder to append to.
     */
    private void appendPrefix(ImageInputStream dis, StringBuilder line) {
        line.append(dis.getTagPosition()).append(": ");
        int level = dis.level();
        while (level-- > 0)
            line.append(Symbol.C_GT);
    }

    /**
     * Appends the element header (Tag, VR, Length) to the line.
     *
     * @param dis  The DICOM input stream.
     * @param line The StringBuilder to append to.
     */
    private void appendHeader(ImageInputStream dis, StringBuilder line) {
        line.append(Tag.toString(dis.tag())).append(Symbol.C_SPACE);
        VR vr = dis.vr();
        if (null != vr)
            line.append(vr).append(Symbol.C_TAB);
        line.append(Symbol.C_HASH).append(dis.length());
    }

    /**
     * Appends the DICOM keyword to the line, if space permits.
     *
     * @param dis            The DICOM input stream.
     * @param privateCreator The private creator identifier, if applicable.
     * @param line           The StringBuilder to append to.
     */
    private void appendKeyword(ImageInputStream dis, String privateCreator, StringBuilder line) {
        if (line.length() < width) {
            line.append(Symbol.SPACE);
            line.append(ElementDictionary.keywordOf(dis.tag(), privateCreator));
            if (line.length() > width)
                line.setLength(width);
        }
    }

    /**
     * Appends an item number to the line, if space permits.
     *
     * @param number The item number.
     * @param line   The StringBuilder to append to.
     */
    private void appendNumber(int number, StringBuilder line) {
        if (line.length() < width) {
            line.append(" #");
            line.append(number);
            if (line.length() > width)
                line.setLength(width);
        }
    }

    /**
     * Appends a formatted representation of a data fragment's value to the line.
     *
     * @param line The StringBuilder to append to.
     * @param dis  The DICOM input stream.
     * @param vr   The value representation of the fragment.
     * @throws IOException if an I/O error occurs.
     */
    private void appendFragment(StringBuilder line, ImageInputStream dis, VR vr) throws IOException {
        byte[] b = probeValue(dis);
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), null, width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, null, line);
        }
        System.out.println(line);
    }

    /**
     * Prints the DICOM file preamble if it exists.
     *
     * @param preamble The 128-byte preamble.
     */
    private void promptPreamble(byte[] preamble) {
        if (preamble == null)
            return;

        StringBuilder line = new StringBuilder(width);
        line.append("0: [");
        if (VR.OB.prompt(preamble, false, null, width - 5, line))
            line.append(']');
        System.out.println(line);
    }

}
