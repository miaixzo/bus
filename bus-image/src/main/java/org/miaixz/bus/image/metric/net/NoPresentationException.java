/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.metric.net;

import java.io.IOException;
import java.io.Serial;

import org.miaixz.bus.image.UID;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoPresentationException extends IOException {

    @Serial
    private static final long serialVersionUID = 2852679071588L;

    public NoPresentationException(String cuid) {
        super(toMessage(cuid));
    }

    public NoPresentationException(String cuid, String tsuid) {
        super(toMessage(cuid, tsuid));
    }

    private static String toMessage(String cuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Presentation Context for Abstract Syntax: ");
        UID.promptTo(cuid, sb);
        sb.append(" negotiated");
        return sb.toString();
    }

    private static String toMessage(String cuid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Presentation Context for Abstract Syntax: ");
        UID.promptTo(cuid, sb);
        sb.append(" with Transfer Syntax: ");
        UID.promptTo(tsuid, sb);
        sb.append(" negotiated");
        return sb.toString();
    }

}
