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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Node;
import org.miaixz.bus.image.metric.Editable;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.op.MaskArea;

import java.util.Objects;
import java.util.Properties;

import static org.miaixz.bus.image.nimble.Transcoder.getMaskedImage;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class EditorContext {

    private final String tsuid;
    private final Node sourceNode;
    private final Node destinationNode;
    private final Properties properties;
    private Abort abort;
    private String abortMessage;
    private MaskArea maskArea;

    public EditorContext(String tsuid, Node sourceNode, Node destinationNode) {
        this.tsuid = tsuid;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.abort = Abort.NONE;
        this.properties = new Properties();
    }

    public Abort getAbort() {
        return abort;
    }

    public void setAbort(Abort abort) {
        this.abort = abort;
    }

    public String getAbortMessage() {
        return abortMessage;
    }

    public void setAbortMessage(String abortMessage) {
        this.abortMessage = abortMessage;
    }

    public String getTsuid() {
        return tsuid;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public Node getDestinationNode() {
        return destinationNode;
    }

    public MaskArea getMaskArea() {
        return maskArea;
    }

    public void setMaskArea(MaskArea maskArea) {
        this.maskArea = maskArea;
    }

    public Properties getProperties() {
        return properties;
    }

    public Editable<PlanarImage> getEditable() {
        return getMaskedImage(getMaskArea());
    }

    public boolean hasPixelProcessing() {
        return Objects.nonNull(getMaskArea())
                || Builder.getEmptytoFalse(getProperties().getProperty("defacing"));
    }

    /**
     * Abort status allows to skip the file transfer or abort the DICOM association
     */
    public enum Abort {
        // Do nothing
        NONE,
        // Allows to skip the bulk data transfer to go to the next file
        FILE_EXCEPTION,
        // Stop the DICOM connection. Attention, this will abort other transfers when there are several
        // destinations for one source.
        CONNECTION_EXCEPTION
    }

}
