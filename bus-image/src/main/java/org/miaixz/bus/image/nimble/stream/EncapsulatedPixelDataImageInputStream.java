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
package org.miaixz.bus.image.nimble.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.stream.MemoryCacheImageInputStream;

import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;

/**
 * Represents the EncapsulatedPixelDataImageInputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EncapsulatedPixelDataImageInputStream extends MemoryCacheImageInputStream
        implements BytesWithImageImageDescriptor {

    /**
     * The dis value.
     */
    private final ImageInputStream dis;

    /**
     * The image descriptor value.
     */
    private final ImageDescriptor imageDescriptor;

    /**
     * The ts type value.
     */
    private final TransferSyntaxType tsType;

    /**
     * The basic offset table value.
     */
    private final byte[] basicOffsetTable;

    /**
     * The frame start word value.
     */
    private final int frameStartWord;

    /**
     * The fragm start word value.
     */
    private int fragmStartWord;

    /**
     * The fragm end pos value.
     */
    private long fragmEndPos;

    /**
     * The frame start pos value.
     */
    private long frameStartPos;

    /**
     * The frame end pos value.
     */
    private long frameEndPos = -1L;

    /**
     * The end of stream value.
     */
    private boolean endOfStream;

    /**
     * Creates a new instance.
     *
     * @param dis             the dis.
     * @param imageDescriptor the image descriptor.
     * @throws IOException if the operation cannot be completed.
     */
    public EncapsulatedPixelDataImageInputStream(ImageInputStream dis, ImageDescriptor imageDescriptor)
            throws IOException {
        this(dis, imageDescriptor, TransferSyntaxType.forUID(dis.getTransferSyntax()));
    }

    /**
     * Creates a new instance.
     *
     * @param dis             the dis.
     * @param imageDescriptor the image descriptor.
     * @param tsType          the ts type.
     * @throws IOException if the operation cannot be completed.
     */
    public EncapsulatedPixelDataImageInputStream(ImageInputStream dis, ImageDescriptor imageDescriptor,
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

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        if (endOfFrame())
            return -1;

        return super.read();
    }

    /**
     * Executes the read operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (endOfFrame())
            return -1;

        return super.read(b, off, Math.min(len, (int) ((frameEndPos < 0 ? fragmEndPos : frameEndPos) - streamPos)));
    }

    /**
     * Executes the seek current frame operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void seekCurrentFrame() throws IOException {
        seek(frameStartPos);
    }

    /**
     * Executes the seek next frame operation.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Gets the bytes.
     *
     * @return the bytes.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Reads the item header.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean readItemHeader() throws IOException {
        if (!dis.readItemHeader()) {
            endOfStream = true;
            return false;
        }
        fragmEndPos = streamPos + dis.unsignedLength();
        mark();
        fragmStartWord = (super.read() << 8) | super.read();
        reset();
        return true;
    }

    /**
     * Executes the end of frame operation.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Determines whether end of stream.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEndOfStream() {
        return endOfStream;
    }

}
