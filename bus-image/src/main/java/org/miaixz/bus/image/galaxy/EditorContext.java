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
package org.miaixz.bus.image.galaxy;

import static org.miaixz.bus.image.nimble.Transcoder.getMaskedImage;

import java.util.Objects;
import java.util.Properties;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Node;
import org.miaixz.bus.image.metric.Editable;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.op.MaskArea;

/**
 * Represents the context for an editing operation within the image processing workflow. This class holds information
 * about the transfer syntax, source and destination nodes, properties, and any abort conditions or masking areas.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
public class EditorContext {

    /**
     * The Transfer Syntax UID (TSUID) being used for the operation.
     */
    private final String tsuid;
    /**
     * The source node involved in the operation.
     */
    private final Node sourceNode;
    /**
     * The destination node involved in the operation.
     */
    private final Node destinationNode;
    /**
     * A set of properties associated with the editing context.
     */
    private final Properties properties;
    /**
     * The current abort status, indicating if the operation should be aborted and why.
     */
    private Abort abort;
    /**
     * A message providing more details about the abort condition.
     */
    private String abortMessage;
    /**
     * The mask area applied during image processing, if any.
     */
    private MaskArea maskArea;

    /**
     * Constructs an {@code EditorContext} with the specified transfer syntax UID, source node, and destination node.
     * Initializes abort status to {@link Abort#NONE} and creates an empty {@link Properties} object.
     * 
     * @param tsuid           The Transfer Syntax UID.
     * @param sourceNode      The source node.
     * @param destinationNode The destination node.
     */
    public EditorContext(String tsuid, Node sourceNode, Node destinationNode) {
        this.tsuid = tsuid;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.abort = Abort.NONE;
        this.properties = new Properties();
    }

    /**
     * Returns the current abort status.
     * 
     * @return The {@link Abort} status.
     */
    public Abort getAbort() {
        return abort;
    }

    /**
     * Sets the abort status for the editing operation.
     * 
     * @param abort The {@link Abort} status to set.
     */
    public void setAbort(Abort abort) {
        this.abort = abort;
    }

    /**
     * Returns the abort message, if any.
     * 
     * @return The abort message string.
     */
    public String getAbortMessage() {
        return abortMessage;
    }

    /**
     * Sets the abort message for the editing operation.
     * 
     * @param abortMessage The message to set.
     */
    public void setAbortMessage(String abortMessage) {
        this.abortMessage = abortMessage;
    }

    /**
     * Returns the Transfer Syntax UID.
     * 
     * @return The TSUID string.
     */
    public String getTsuid() {
        return tsuid;
    }

    /**
     * Returns the source node.
     * 
     * @return The {@link Node} representing the source.
     */
    public Node getSourceNode() {
        return sourceNode;
    }

    /**
     * Returns the destination node.
     * 
     * @return The {@link Node} representing the destination.
     */
    public Node getDestinationNode() {
        return destinationNode;
    }

    /**
     * Returns the mask area applied during image processing.
     * 
     * @return The {@link MaskArea} object, or {@code null} if no mask is applied.
     */
    public MaskArea getMaskArea() {
        return maskArea;
    }

    /**
     * Sets the mask area for image processing.
     * 
     * @param maskArea The {@link MaskArea} object to set.
     */
    public void setMaskArea(MaskArea maskArea) {
        this.maskArea = maskArea;
    }

    /**
     * Returns the properties associated with this editing context.
     * 
     * @return The {@link Properties} object.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns an {@link Editable} representation of a {@link PlanarImage} based on the current mask area.
     * 
     * @return An {@link Editable} image.
     */
    public Editable<PlanarImage> getEditable() {
        return getMaskedImage(getMaskArea());
    }

    /**
     * Checks if pixel processing is enabled, either by a mask area or a "defacing" property.
     * 
     * @return {@code true} if pixel processing is enabled, {@code false} otherwise.
     */
    public boolean hasPixelProcessing() {
        return Objects.nonNull(getMaskArea()) || Builder.getEmptytoFalse(getProperties().getProperty("defacing"));
    }

    /**
     * Abort status allows to skip the file transfer or abort the DICOM association.
     */
    public enum Abort {
        /**
         * No abort action specified.
         */
        NONE,
        /**
         * Allows to skip the bulk data transfer to go to the next file.
         */
        FILE_EXCEPTION,
        /**
         * Stop the DICOM connection. Attention, this will abort other transfers when there are several destinations for
         * one source.
         */
        CONNECTION_EXCEPTION
    }

}
