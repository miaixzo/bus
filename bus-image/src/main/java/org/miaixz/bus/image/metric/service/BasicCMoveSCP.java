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

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.pdu.PresentationContext;

/**
 * Represents the BasicCMoveSCP type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BasicCMoveSCP extends AbstractImageService {

    /**
     * Creates a new instance.
     *
     * @param sopClasses the sop classes.
     */
    public BasicCMoveSCP(String... sopClasses) {
        super(sopClasses);
    }

    /**
     * Executes the on dimse rq operation.
     *
     * @param as    the as.
     * @param pc    the pc.
     * @param dimse the dimse.
     * @param cmd   the cmd.
     * @param keys  the keys.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes keys)
            throws IOException {
        if (dimse != Dimse.C_MOVE_RQ)
            throw new ImageServiceException(Status.UnrecognizedOperation);

        RetrieveTask retrieveTask = calculateMatches(as, pc, cmd, keys);
        if (retrieveTask != null)
            as.getApplicationEntity().getDevice().execute(retrieveTask);
        else
            as.tryWriteDimseRSP(pc, Commands.mkCMoveRSP(cmd, Status.Success));
    }

    /**
     * Executes the calculate matches operation.
     *
     * @param as   the as.
     * @param pc   the pc.
     * @param rq   the rq.
     * @param keys the keys.
     * @return the operation result.
     */
    protected RetrieveTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) {
        return null;
    }

}
