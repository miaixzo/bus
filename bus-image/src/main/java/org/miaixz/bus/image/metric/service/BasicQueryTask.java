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
package org.miaixz.bus.image.metric.service;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.pdu.PresentationContext;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicQueryTask implements QueryTask {

    protected final Association as;
    protected final PresentationContext pc;
    protected final Attributes rq;
    protected final Attributes keys;
    protected volatile boolean canceled;
    protected boolean optionalKeysNotSupported = false;

    public BasicQueryTask(Association as, PresentationContext pc, Attributes rq, Attributes keys) {
        this.as = as;
        this.pc = pc;
        this.rq = rq;
        this.keys = keys;
    }

    public boolean isOptionalKeysNotSupported() {
        return optionalKeysNotSupported;
    }

    public void setOptionalKeysNotSupported(boolean optionalKeysNotSupported) {
        this.optionalKeysNotSupported = optionalKeysNotSupported;
    }

    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    @Override
    public void run() {
        try {
            int msgId = rq.getInt(Tag.MessageID, -1);
            as.addCancelRQHandler(msgId, this);
            try {
                while (!canceled && hasMoreMatches()) {
                    Attributes match = adjust(nextMatch());
                    if (match != null) {
                        int status = optionalKeysNotSupported ? Status.PendingWarning : Status.Pending;
                        as.writeDimseRSP(pc, Commands.mkCFindRSP(rq, status), match);
                    }
                }
                int status = canceled ? Status.Cancel : Status.Success;
                as.writeDimseRSP(pc, Commands.mkCFindRSP(rq, status));
            } catch (ImageServiceException e) {
                Attributes rsp = e.mkRSP(0x8020, msgId);
                as.writeDimseRSP(pc, rsp, e.getDataset());
            } finally {
                as.removeCancelRQHandler(msgId);
                close();
            }
        } catch (IOException e) {
            // handled by Association
        }
    }

    protected void close() {
    }

    protected Attributes nextMatch() throws ImageServiceException {
        throw new NoSuchElementException();
    }

    protected boolean hasMoreMatches() throws ImageServiceException {
        return false;
    }

    protected Attributes adjust(Attributes match) throws ImageServiceException {
        if (match == null)
            return null;

        Attributes filtered = new Attributes(match.size());
        // include SpecificCharacterSet also if not in keys
        if (!keys.contains(Tag.SpecificCharacterSet)) {
            String[] ss = match.getStrings(Tag.SpecificCharacterSet);
            if (ss != null)
                filtered.setString(Tag.SpecificCharacterSet, VR.CS, ss);
        }
        filtered.addSelected(match, keys);
        filtered.supplementEmpty(keys);
        return filtered;
    }

}
