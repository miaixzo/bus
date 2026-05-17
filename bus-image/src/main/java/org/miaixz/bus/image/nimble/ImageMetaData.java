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

import org.w3c.dom.Node;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;

/**
 * Represents the ImageMetaData type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageMetaData extends IIOMetadata {

    /**
     * The file meta information value.
     */
    private final Attributes fileMetaInformation;

    /**
     * The dcm value.
     */
    private final Attributes dcm;

    /**
     * The desc value.
     */
    private final ImageDescriptor desc;

    /**
     * The transfer syntax uid value.
     */
    private final String transferSyntaxUID;

    /**
     * Creates a new instance.
     *
     * @param dcmStream the dcm stream.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Creates a new instance.
     *
     * @param dcm               the dcm.
     * @param transferSyntaxUID the transfer syntax uid.
     */
    public ImageMetaData(Attributes dcm, String transferSyntaxUID) {
        this.fileMetaInformation = null;
        this.dcm = Objects.requireNonNull(dcm);
        this.desc = new ImageDescriptor(dcm);
        this.transferSyntaxUID = Objects.requireNonNull(transferSyntaxUID);
    }

    /**
     * Gets the file meta information.
     *
     * @return the file meta information.
     */
    public final Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    /**
     * Gets the dicom object.
     *
     * @return the dicom object.
     */
    public final Attributes getDicomObject() {
        return dcm;
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    public final ImageDescriptor getImageDescriptor() {
        return desc;
    }

    /**
     * Determines whether read only.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Gets the as tree.
     *
     * @param formatName the format name.
     * @return the as tree.
     */
    @Override
    public Node getAsTree(String formatName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the merge tree operation.
     *
     * @param formatName the format name.
     * @param root       the root.
     */
    @Override
    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the reset operation.
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the transfer syntax uid.
     *
     * @return the transfer syntax uid.
     */
    public String getTransferSyntaxUID() {
        return transferSyntaxUID;
    }

    /**
     * Gets the media storage sop class uid.
     *
     * @return the media storage sop class uid.
     */
    public String getMediaStorageSOPClassUID() {
        return fileMetaInformation == null ? null : fileMetaInformation.getString(Tag.MediaStorageSOPClassUID);
    }

    /**
     * Determines whether video transfer syntax uid.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isVideoTransferSyntaxUID() {
        return transferSyntaxUID != null && transferSyntaxUID.startsWith("1.2.840.10008.1.2.4.10");
    }

    /**
     * Determines whether media storage directory.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isMediaStorageDirectory() {
        return "1.2.840.10008.1.3.10".equals(getMediaStorageSOPClassUID());
    }

    /**
     * Determines whether segmentation storage.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSegmentationStorage() {
        return "1.2.840.10008.5.1.4.1.1.66.4".equals(getMediaStorageSOPClassUID());
    }

}
