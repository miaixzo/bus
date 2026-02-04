/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric;

import java.io.IOException;

import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.pdu.PresentationContext;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DimseRSPHandler {

    private final int msgId;
    private PresentationContext pc;
    private volatile Timeout timeout;
    private volatile boolean stopOnPending;
    private volatile boolean canceled;

    public DimseRSPHandler(int msgId) {
        this.msgId = msgId;
    }

    public final void setPC(PresentationContext pc) {
        this.pc = pc;
    }

    public final int getMessageID() {
        return msgId;
    }

    public final void setTimeout(Timeout timeout, boolean stopOnPending) {
        this.timeout = timeout;
        this.stopOnPending = stopOnPending;
    }

    public boolean isStopOnPending() {
        return stopOnPending;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel(Association as) throws IOException {
        as.cancel(pc, msgId);
        canceled = true;
    }

    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        if (stopOnPending || !Status.isPending(cmd.getInt(Tag.Status, -1)))
            stopTimeout(as);
    }

    public void onClose(Association as) {
        stopTimeout(as);
    }

    public void stopTimeout(Association as) {
        if (timeout != null) {
            timeout.stop();
            timeout = null;
        }
    }

}
