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
