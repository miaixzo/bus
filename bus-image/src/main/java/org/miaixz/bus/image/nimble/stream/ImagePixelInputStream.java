/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.stream;

import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.codec.BytesWithImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;

import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImagePixelInputStream extends MemoryCacheImageInputStream
        implements BytesWithImageDescriptor {

    private final ImageInputStream dis;
    private final ImageDescriptor imageDescriptor;
    private final TransferSyntaxType tsType;
    private final byte[] basicOffsetTable;
    private final int frameStartWord;
    private int fragmStartWord;
    private long fragmEndPos;
    private long frameStartPos;
    private long frameEndPos = -1L;
    private boolean endOfStream;

    public ImagePixelInputStream(ImageInputStream dis, ImageDescriptor imageDescriptor)
            throws IOException {
        this(dis, imageDescriptor, TransferSyntaxType.forUID(dis.getTransferSyntax()));
    }

    public ImagePixelInputStream(ImageInputStream dis, ImageDescriptor imageDescriptor,
                                 TransferSyntaxType tsType) throws IOException {
        super(dis);
        this.dis = dis;
        this.imageDescriptor = imageDescriptor;
        this.tsType = tsType;
        dis.readItemHeader();
        byte[] b = new byte[dis.length()];
        dis.readFully(b);
        basicOffsetTable = b;
        readItemHeader();
        frameStartWord = fragmStartWord;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @Override
    public int read() throws IOException {
        if (endOfFrame())
            return -1;

        return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (endOfFrame())
            return -1;

        return super.read(b, off,
                Math.min(len, (int) ((frameEndPos < 0 ? fragmEndPos : frameEndPos) - streamPos)));
    }

    public void seekCurrentFrame() throws IOException {
        seek(frameStartPos);
    }

    public boolean seekNextFrame() throws IOException {
        if (endOfStream)
            return false;

        if (frameEndPos >= 0) {
            seek(frameEndPos);
            flush();
        } else {
            while (!endOfFrame()) {
                seek(fragmEndPos - 1);
                super.read(); // ensure to read wh ole Data Fragment from DicomInputStream
                flush();
            }
        }
        frameStartPos = streamPos;
        frameEndPos = -1L;
        return !endOfStream;
    }

    @Override
    public ByteBuffer getBytes() throws IOException {
        byte[] array = new byte[8192];
        int length = 0;
        int read;
        while ((read = this.read(array, length, array.length - length)) > 0) {
            if ((length += read) == array.length)
                array = Arrays.copyOf(array, array.length << 1);
        }
        return ByteBuffer.wrap(array, 0, length);
    }

    private boolean readItemHeader() throws IOException {
        if (!dis.readItemHeader()) {
            endOfStream = true;
            return false;
        }
        fragmEndPos = streamPos + dis.length();
        mark();
        fragmStartWord = (super.read() << 8) | super.read();
        reset();
        return true;
    }

    private boolean endOfFrame() throws IOException {
        if (frameEndPos >= 0)
            return streamPos >= frameEndPos;

        if (streamPos < fragmEndPos)
            return false;

        if (readItemHeader() && (!imageDescriptor.isMultiframe()
                || (tsType.mayFrameSpanMultipleFragments() && fragmStartWord != frameStartWord)))
            return false;

        frameEndPos = streamPos;
        return true;
    }

    public boolean isEndOfStream() {
        return endOfStream;
    }

}
