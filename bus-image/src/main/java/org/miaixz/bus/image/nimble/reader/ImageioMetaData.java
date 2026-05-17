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
package org.miaixz.bus.image.nimble.reader;

import javax.imageio.metadata.IIOMetadata;

import org.w3c.dom.Node;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents the ImageioMetaData type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageioMetaData extends IIOMetadata {

    /**
     * The file meta information value.
     */
    private final Attributes fileMetaInformation;

    /**
     * The attributes value.
     */
    private final Attributes attributes;

    /**
     * Creates a new instance.
     *
     * @param fileMetaInformation the file meta information.
     * @param attributes          the attributes.
     */
    public ImageioMetaData(Attributes fileMetaInformation, Attributes attributes) {
        this.fileMetaInformation = fileMetaInformation;
        this.attributes = attributes;
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
     * Gets the attributes.
     *
     * @return the attributes.
     */
    public final Attributes getAttributes() {
        return attributes;
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
        return getFileMetaInformation().getString(Tag.TransferSyntaxUID);
    }

    /**
     * Executes the big endian operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean bigEndian() {
        return getAttributes().bigEndian();
    }

}
