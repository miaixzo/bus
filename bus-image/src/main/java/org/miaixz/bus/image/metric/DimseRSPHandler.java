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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.internal.pdu.Presentation;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DimseRSPHandler {

    private final int msgId;
    private Presentation pc;
    private volatile Timeout timeout;
    private boolean stopOnPending;

    public DimseRSPHandler(int msgId) {
        this.msgId = msgId;
    }

    final void setPC(Presentation pc) {
        this.pc = pc;
    }

    public final int getMessageID() {
        return msgId;
    }

    final void setTimeout(Timeout timeout, boolean stopOnPending) {
        this.timeout = timeout;
        this.stopOnPending = stopOnPending;
    }

    boolean isStopOnPending() {
        return stopOnPending;
    }

    public void cancel(Association as) throws IOException {
        as.cancel(pc, msgId);
    }

    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        if (stopOnPending || !Status.isPending(cmd.getInt(Tag.Status, -1)))
            stopTimeout(as);
    }

    public void onClose(Association as) {
        stopTimeout(as);
    }

    private void stopTimeout(Association as) {
        if (null != timeout) {
            timeout.stop();
            timeout = null;
        }
    }

}
