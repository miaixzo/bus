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

import java.io.IOException;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.io.ImageInputHandler;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DcmDump implements ImageInputHandler {

    /**
     * default number of characters per line
     */
    private static final int DEFAULT_WIDTH = 78;

    private int width = DEFAULT_WIDTH;

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException();
        this.width = width;
    }

    public void parse(ImageInputStream dis) throws IOException {
        dis.setDicomInputHandler(this);
        dis.readDataset();
    }

    @Override
    public void startDataset(ImageInputStream dis) {
        promptPreamble(dis.getPreamble());
    }

    @Override
    public void endDataset(ImageInputStream dis) {
    }

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
            dis.readValue(dis, attrs);
            if (undeflen) {
                line.setLength(0);
                appendPrefix(dis, line);
                appendHeader(dis, line);
                appendKeyword(dis, privateCreator, line);
            }
            return;
        }
        byte[] b = probeValue(dis);
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), attrs.getSpecificCharacterSet(), width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, privateCreator, line);
        }
        if (tag == Tag.FileMetaInformationGroupLength)
            dis.setFileMetaInformationGroupLength(b);
        else if (tag == Tag.TransferSyntaxUID || tag == Tag.SpecificCharacterSet || tag == Tag.PixelRepresentation
                || Tag.isPrivateCreator(tag))
            attrs.setBytes(tag, vr, b);
    }

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

    @Override
    public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
        String privateCreator = seq.getParent().getPrivateCreator(dis.tag());
        StringBuilder line = new StringBuilder(width);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        appendKeyword(dis, privateCreator, line);
        appendNumber(seq.size() + 1, line);
        boolean undeflen = dis.length() == -1;
        dis.readValue(dis, seq);
        if (undeflen) {
            line.setLength(0);
            appendPrefix(dis, line);
            appendHeader(dis, line);
            appendKeyword(dis, privateCreator, line);
        }
    }

    @Override
    public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
        StringBuilder line = new StringBuilder(width + 20);
        appendPrefix(dis, line);
        appendHeader(dis, line);
        appendFragment(line, dis, frags.vr());
    }

    private void appendPrefix(ImageInputStream dis, StringBuilder line) {
        line.append(dis.getTagPosition()).append(": ");
        int level = dis.level();
        while (level-- > 0)
            line.append(Symbol.C_GT);
    }

    private void appendHeader(ImageInputStream dis, StringBuilder line) {
        line.append(Tag.toString(dis.tag())).append(Symbol.C_SPACE);
        VR vr = dis.vr();
        if (null != vr)
            line.append(vr).append(Symbol.C_TAB);
        line.append(Symbol.C_SHAPE).append(dis.length());
    }

    private void appendKeyword(ImageInputStream dis, String privateCreator, StringBuilder line) {
        if (line.length() < width) {
            line.append(Symbol.SPACE);
            line.append(ElementDictionary.keywordOf(dis.tag(), privateCreator));
            if (line.length() > width)
                line.setLength(width);
        }
    }

    private void appendNumber(int number, StringBuilder line) {
        if (line.length() < width) {
            line.append(" #");
            line.append(number);
            if (line.length() > width)
                line.setLength(width);
        }
    }

    private void appendFragment(StringBuilder line, ImageInputStream dis, VR vr) throws IOException {
        byte[] b = probeValue(dis);
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), null, width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, null, line);
        }
    }

    private void promptPreamble(byte[] preamble) {
        if (preamble == null)
            return;

        StringBuilder line = new StringBuilder(width);
        line.append("0: [");
        if (VR.OB.prompt(preamble, false, null, width - 5, line))
            line.append(']');
    }

}
