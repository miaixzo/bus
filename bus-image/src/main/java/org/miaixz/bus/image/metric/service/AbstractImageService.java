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
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractImageService implements ImageService {

    private final String[] sopClasses;

    protected AbstractImageService(String... sopClasses) {
        this.sopClasses = sopClasses.clone();
    }

    @Override
    public String[] getSOPClasses() {
        return sopClasses;
    }

    @Override
    public void onClose(Association as) {

    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data)
            throws IOException {
        onDimseRQ(as, pc, dimse, cmd, readDataset(pc, data));
    }

    private Attributes readDataset(PresentationContext pc, PDVInputStream data) throws IOException {
        if (data == null)
            return null;

        Attributes dataset = data.readDataset(pc.getTransferSyntax());
        Logger.debug("Dataset:\n{}", dataset);
        return dataset;
    }

    protected abstract void onDimseRQ(
            Association as,
            PresentationContext pc,
            Dimse dimse,
            Attributes cmd,
            Attributes data) throws IOException;

}
