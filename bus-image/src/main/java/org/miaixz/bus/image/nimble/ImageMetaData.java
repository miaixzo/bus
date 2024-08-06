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
package org.miaixz.bus.image.nimble;

import java.io.IOException;
import java.util.Objects;

import javax.imageio.metadata.IIOMetadata;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.w3c.dom.Node;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageMetaData extends IIOMetadata {

    private final Attributes fileMetaInformation;
    private final Attributes dcm;
    private final ImageDescriptor desc;
    private final String transferSyntaxUID;

    public ImageMetaData(ImageInputStream dcmStream) throws IOException {
        this.fileMetaInformation = Objects.requireNonNull(dcmStream).readFileMetaInformation();
        this.dcm = dcmStream.readDataset();
        this.desc = new ImageDescriptor(dcm);
        String uid;
        if (fileMetaInformation == null) {
            uid = dcmStream.getTransferSyntax();
        } else {
            uid = fileMetaInformation.getString(Tag.TransferSyntaxUID, dcmStream.getTransferSyntax());
        }
        this.transferSyntaxUID = uid;
    }

    public ImageMetaData(Attributes dcm, String transferSyntaxUID) {
        this.fileMetaInformation = null;
        this.dcm = Objects.requireNonNull(dcm);
        this.desc = new ImageDescriptor(dcm);
        this.transferSyntaxUID = Objects.requireNonNull(transferSyntaxUID);
    }

    public final Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    public final Attributes getDicomObject() {
        return dcm;
    }

    public final ImageDescriptor getImageDescriptor() {
        return desc;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    public String getTransferSyntaxUID() {
        return transferSyntaxUID;
    }

    public String getMediaStorageSOPClassUID() {
        return fileMetaInformation == null ? null : fileMetaInformation.getString(Tag.MediaStorageSOPClassUID);
    }

    public boolean isVideoTransferSyntaxUID() {
        return transferSyntaxUID != null && transferSyntaxUID.startsWith("1.2.840.10008.1.2.4.10");
    }

    public boolean isMediaStorageDirectory() {
        return "1.2.840.10008.1.3.10".equals(getMediaStorageSOPClassUID());
    }

    public boolean isSegmentationStorage() {
        return "1.2.840.10008.5.1.4.1.1.66.4".equals(getMediaStorageSOPClassUID());
    }

}
