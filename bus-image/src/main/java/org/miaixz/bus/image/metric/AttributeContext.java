/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.metric;

import lombok.Data;
import org.miaixz.bus.image.Node;

/**
 * @author Kimi Liu
 * @since Java 17+
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
     * 中止状态允许跳过文件传输或中止DICOM关联
     */
    public enum Abort {
        // 没做什么
        NONE,
        // 允许跳过批量数据传输以转到下一个文件
        FILE_EXCEPTION,
        // 停止DICOM连接。注意，当一个源有多个目的地时，这将中止其他传输
        CONNECTION_EXCEPTION
    }

}
