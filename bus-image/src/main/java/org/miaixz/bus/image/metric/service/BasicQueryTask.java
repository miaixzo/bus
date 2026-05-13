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
 * Represents the BasicQueryTask type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BasicQueryTask implements QueryTask {

    /**
     * The as value.
     */
    protected final Association as;

    /**
     * The pc value.
     */
    protected final PresentationContext pc;

    /**
     * The rq value.
     */
    protected final Attributes rq;

    /**
     * The keys value.
     */
    protected final Attributes keys;

    /**
     * The canceled value.
     */
    protected volatile boolean canceled;

    /**
     * The optional keys not supported value.
     */
    protected boolean optionalKeysNotSupported = false;

    /**
     * Creates a new instance.
     *
     * @param as   the as.
     * @param pc   the pc.
     * @param rq   the rq.
     * @param keys the keys.
     */
    public BasicQueryTask(Association as, PresentationContext pc, Attributes rq, Attributes keys) {
        this.as = as;
        this.pc = pc;
        this.rq = rq;
        this.keys = keys;
    }

    /**
     * Determines whether optional keys not supported.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isOptionalKeysNotSupported() {
        return optionalKeysNotSupported;
    }

    /**
     * Sets the optional keys not supported.
     *
     * @param optionalKeysNotSupported the optional keys not supported.
     */
    public void setOptionalKeysNotSupported(boolean optionalKeysNotSupported) {
        this.optionalKeysNotSupported = optionalKeysNotSupported;
    }

    /**
     * Executes the on cancel rq operation.
     *
     * @param as the as.
     */
    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    /**
     * Executes the run operation.
     */
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

    /**
     * Executes the close operation.
     */
    protected void close() {
    }

    /**
     * Executes the next match operation.
     *
     * @return the operation result.
     * @throws ImageServiceException if the operation cannot be completed.
     */
    protected Attributes nextMatch() throws ImageServiceException {
        throw new NoSuchElementException();
    }

    /**
     * Determines whether more matches.
     *
     * @return true if the condition is met; otherwise false.
     * @throws ImageServiceException if the operation cannot be completed.
     */
    protected boolean hasMoreMatches() throws ImageServiceException {
        return false;
    }

    /**
     * Executes the adjust operation.
     *
     * @param match the match.
     * @return the operation result.
     * @throws ImageServiceException if the operation cannot be completed.
     */
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
