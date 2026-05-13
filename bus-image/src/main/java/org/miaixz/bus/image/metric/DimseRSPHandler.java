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
package org.miaixz.bus.image.metric;

import java.io.IOException;

import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.pdu.PresentationContext;

/**
 * Represents the DimseRSPHandler type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DimseRSPHandler {

    /**
     * The msg id value.
     */
    private final int msgId;

    /**
     * The pc value.
     */
    private PresentationContext pc;

    /**
     * The timeout value.
     */
    private volatile Timeout timeout;

    /**
     * The stop on pending value.
     */
    private volatile boolean stopOnPending;

    /**
     * The canceled value.
     */
    private volatile boolean canceled;

    /**
     * Creates a new instance.
     *
     * @param msgId the msg id.
     */
    public DimseRSPHandler(int msgId) {
        this.msgId = msgId;
    }

    /**
     * Sets the pc.
     *
     * @param pc the pc.
     */
    public final void setPC(PresentationContext pc) {
        this.pc = pc;
    }

    /**
     * Gets the message id.
     *
     * @return the message id.
     */
    public final int getMessageID() {
        return msgId;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout       the timeout.
     * @param stopOnPending the stop on pending.
     */
    public final void setTimeout(Timeout timeout, boolean stopOnPending) {
        this.timeout = timeout;
        this.stopOnPending = stopOnPending;
    }

    /**
     * Determines whether stop on pending.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isStopOnPending() {
        return stopOnPending;
    }

    /**
     * Determines whether canceled.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Determines whether cel.
     *
     * @param as the as.
     * @throws IOException if the operation cannot be completed.
     */
    public void cancel(Association as) throws IOException {
        as.cancel(pc, msgId);
        canceled = true;
    }

    /**
     * Executes the on dimse rsp operation.
     *
     * @param as   the as.
     * @param cmd  the cmd.
     * @param data the data.
     */
    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        if (stopOnPending || !Status.isPending(cmd.getInt(Tag.Status, -1)))
            stopTimeout(as);
    }

    /**
     * Executes the on close operation.
     *
     * @param as the as.
     */
    public void onClose(Association as) {
        stopTimeout(as);
    }

    /**
     * Executes the stop timeout operation.
     *
     * @param as the as.
     */
    public void stopTimeout(Association as) {
        if (timeout != null) {
            timeout.stop();
            timeout = null;
        }
    }

}
