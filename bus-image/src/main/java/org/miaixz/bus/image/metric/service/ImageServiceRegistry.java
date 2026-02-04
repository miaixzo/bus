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
package org.miaixz.bus.image.metric.service;

import java.io.IOException;
import java.util.HashMap;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.DimseRQHandler;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.CommonExtended;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageServiceRegistry implements DimseRQHandler {

    private final HashMap<String, DimseRQHandler> services = new HashMap<>();

    public void addDicomService(ImageService service) {
        addDimseRQHandler(service, service.getSOPClasses());
    }

    public synchronized void addDimseRQHandler(DimseRQHandler service, String... sopClasses) {
        for (String uid : sopClasses)
            services.put(uid, service);
    }

    public void removeDicomService(ImageService service) {
        removeDimseRQHandler(service.getSOPClasses());
    }

    public synchronized void removeDimseRQHandler(String... sopClasses) {
        for (String uid : sopClasses)
            services.remove(uid);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data)
            throws IOException {
        try {
            lookupService(as, dimse, cmd).onDimseRQ(as, pc, dimse, cmd, data);
        } catch (ImageServiceException e) {
            Logger.info(
                    "{}: processing {} failed. Caused by:\t",
                    as,
                    dimse.toString(cmd, pc.getPCID(), pc.getTransferSyntax()),
                    e);
            rspForDimseRQException(as, pc, dimse, cmd, e);
        }
    }

    private void rspForDimseRQException(
            Association as,
            PresentationContext pc,
            Dimse dimse,
            Attributes cmd,
            ImageServiceException e) {
        Attributes rsp = e.mkRSP(dimse.commandFieldOfRSP(), cmd.getInt(Tag.MessageID, 0));
        as.tryWriteDimseRSP(pc, rsp, e.getDataset());
    }

    private DimseRQHandler lookupService(Association as, Dimse dimse, Attributes cmd) throws ImageServiceException {
        String cuid = cmd.getString(dimse.tagOfSOPClassUID());
        if (cuid == null)
            throw new ImageServiceException(Status.MistypedArgument);

        DimseRQHandler service = services.get(cuid);
        if (service != null)
            return service;

        if (dimse == Dimse.C_STORE_RQ) {
            CommonExtended commonExtNeg = as.getCommonExtendedNegotiationFor(cuid);
            if (commonExtNeg != null) {
                for (String uid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                    service = services.get(uid);
                    if (service != null)
                        return service;
                }
                service = services.get(commonExtNeg.getServiceClassUID());
                if (service != null)
                    return service;
            }
            service = services.get("*");
            if (service != null)
                return service;
        }
        throw new ImageServiceException(dimse.isCService() ? Status.SOPclassNotSupported : Status.NoSuchSOPclass);
    }

    @Override
    public void onClose(Association as) {
        for (DimseRQHandler service : services.values())
            service.onClose(as);
    }

}
