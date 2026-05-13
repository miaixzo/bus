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
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the AbstractImageService type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractImageService implements ImageService {

    /**
     * The sop classes value.
     */
    private final String[] sopClasses;

    /**
     * Creates a new instance.
     *
     * @param sopClasses the sop classes.
     */
    protected AbstractImageService(String... sopClasses) {
        this.sopClasses = sopClasses.clone();
    }

    /**
     * Gets the sop classes.
     *
     * @return the sop classes.
     */
    @Override
    public String[] getSOPClasses() {
        return sopClasses;
    }

    /**
     * Executes the on close operation.
     *
     * @param as the as.
     */
    @Override
    public void onClose(Association as) {

    }

    /**
     * Executes the on dimse rq operation.
     *
     * @param as    the as.
     * @param pc    the pc.
     * @param dimse the dimse.
     * @param cmd   the cmd.
     * @param data  the data.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data)
            throws IOException {
        onDimseRQ(as, pc, dimse, cmd, readDataset(pc, data));
    }

    /**
     * Reads the dataset.
     *
     * @param pc   the pc.
     * @param data the data.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private Attributes readDataset(PresentationContext pc, PDVInputStream data) throws IOException {
        if (data == null)
            return null;

        Attributes dataset = data.readDataset(pc.getTransferSyntax());
        Logger.debug(
                false,
                "Image",
                "Dataset read completed: protocol=pdu, transferSyntax={}, attributeCount={}",
                pc.getTransferSyntax(),
                dataset == null ? 0 : dataset.size());
        return dataset;
    }

    /**
     * Executes the on dimse rq operation.
     *
     * @param as    the as.
     * @param pc    the pc.
     * @param dimse the dimse.
     * @param cmd   the cmd.
     * @param data  the data.
     * @throws IOException if the operation cannot be completed.
     */
    protected abstract void onDimseRQ(
            Association as,
            PresentationContext pc,
            Dimse dimse,
            Attributes cmd,
            Attributes data) throws IOException;

}
