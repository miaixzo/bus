/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric;

import lombok.Data;
import org.aoju.bus.image.Node;

/**
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
@Data
public class AttributeContext {

    private final String tsuid;
    private final Node sourceNode;
    private final Node destinationNode;
    private Abort abort;
    private String abortMessage;

    public AttributeContext(String tsuid, Node sourceNode, Node destinationNode) {
        this.tsuid = tsuid;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.abort = Abort.NONE;
    }

    /**
     * Abort status allows to skip the file transfer or abort the DICOM association
     */
    public enum Abort {
        // Do nothing
        NONE,
        // Allows to skip the bulk data transfer to go to the next file
        FILE_EXCEPTION,
        // Stop the DICOM connection. Attention, this will abort other transfers when there are several destinations for one source.
        CONNECTION_EXCEPTION
    }

}
