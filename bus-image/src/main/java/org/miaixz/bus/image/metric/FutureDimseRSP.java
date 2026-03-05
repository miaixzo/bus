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
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class FutureDimseRSP extends DimseRSPHandler implements DimseRSP {

    private Entry entry = new Entry(null, null);
    private boolean finished;
    private int autoCancel;
    private int remainingCapacity = Integer.MAX_VALUE;
    private IOException ex;

    public FutureDimseRSP(int msgID) {
        super(msgID);
    }

    @Override
    public synchronized void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        super.onDimseRSP(as, cmd, data);
        Entry last = entry;
        while (last.next != null)
            last = last.next;

        last.next = new Entry(cmd, data);
        if (Status.isPending(cmd.getInt(Tag.Status, 0))) {
            if (autoCancel > 0 && --autoCancel == 0)
                try {
                    super.cancel(as);
                } catch (IOException e) {
                    ex = e;
                }
        } else {
            finished = true;
        }
        notifyAll();
        if (!finished && --remainingCapacity == 0) {
            try {
                Logger.debug("Wait for consuming DIMSE RSP");
                while (ex != null && remainingCapacity == 0) {
                    wait();
                }
                Logger.debug("Stop waiting for consuming DIMSE RSP");
            } catch (InterruptedException e) {
                Logger.warn("Failed to wait for consuming DIMSE RSP", e);
            }
        }
    }

    @Override
    public synchronized void onClose(Association as) {
        super.onClose(as);
        if (!finished) {
            ex = as.getException();
            if (ex == null)
                ex = new IOException(
                        "Association to " + as.getRemoteAET() + " released before receive of outstanding DIMSE RSP");
            notifyAll();
        }
    }

    public synchronized void setAutoCancel(int autoCancel) {
        this.autoCancel = autoCancel;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity: " + capacity);
        this.remainingCapacity = capacity;
    }

    @Override
    public void cancel(Association a) throws IOException {
        if (ex != null)
            throw ex;
        if (!finished)
            super.cancel(a);
    }

    public final Attributes getCommand() {
        return entry.command;
    }

    public final Attributes getDataset() {
        return entry.dataset;
    }

    public synchronized boolean next() throws IOException, InterruptedException {
        if (entry.next == null) {
            if (finished)
                return false;

            if (entry.next == null && ex == null) {
                Logger.debug("Wait for next DIMSE RSP");
                while (entry.next == null && ex == null) {
                    wait();
                }
                Logger.debug("Stop waiting for next DIMSE RSP");
            }

            if (ex != null)
                throw ex;
        }
        entry = entry.next;
        if (remainingCapacity++ == 0)
            notifyAll();
        return true;
    }

    private static class Entry {

        final Attributes command;
        final Attributes dataset;
        Entry next;

        public Entry(Attributes command, Attributes dataset) {
            this.command = command;
            this.dataset = dataset;
        }
    }

}
