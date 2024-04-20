/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.metric.service;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.DimseRQHandler;
import org.miaixz.bus.image.metric.ImageException;
import org.miaixz.bus.image.metric.PDVInputStream;
import org.miaixz.bus.image.metric.internal.pdu.CommonExtended;
import org.miaixz.bus.image.metric.internal.pdu.Presentation;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ServiceHandler implements DimseRQHandler {

    private final Map<String, DimseRQHandler> services = new HashMap<>();

    public void addService(ImageService service) {
        addDimseRQHandler(service, service.getSOPClasses());
    }

    public void removeService(ImageService service) {
        removeDimseRQHandler(service.getSOPClasses());
    }

    public synchronized void addDimseRQHandler(DimseRQHandler service,
                                               String... sopClasses) {
        for (String uid : sopClasses) {
            services.put(uid, service);
        }

    }

    public synchronized void removeDimseRQHandler(String... sopClasses) {
        for (String uid : sopClasses) {
            services.remove(uid);
        }
    }

    @Override
    public void onDimse(Association as,
                        Presentation pc,
                        Dimse dimse,
                        Attributes cmd,
                        PDVInputStream data) throws IOException {
        try {
            lookupService(as, dimse, cmd).onDimse(as, pc, dimse, cmd, data);
        } catch (ImageException e) {
            Logger.info("{}: processing {} failed. Caused by:\t",
                    as,
                    dimse.toString(cmd, pc.getPCID(), pc.getTransferSyntax()),
                    e);
            Attributes rsp = e.mkRSP(dimse.commandFieldOfRSP(), cmd.getInt(Tag.MessageID, 0));
            as.tryWriteDimseRSP(pc, rsp, e.getDataset());
        }
    }

    private DimseRQHandler lookupService(Association as, Dimse dimse, Attributes cmd)
            throws ImageException {
        String cuid = cmd.getString(dimse.tagOfSOPClassUID());
        if (null == cuid)
            throw new ImageException(Status.MistypedArgument);

        DimseRQHandler service = services.get(cuid);
        if (null != service)
            return service;

        if (dimse == Dimse.C_STORE_RQ) {
            CommonExtended commonExtNeg = as.getCommonExtendedNegotiationFor(cuid);
            if (null != commonExtNeg) {
                for (String uid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                    service = services.get(uid);
                    if (null != service)
                        return service;
                }
                service = services.get(commonExtNeg.getServiceClassUID());
                if (null != service)
                    return service;
            }
            service = services.get(Symbol.STAR);
            if (null != service)
                return service;
        }
        throw new ImageException(dimse.isCService()
                ? Status.SOPclassNotSupported
                : Status.NoSuchSOPclass);
    }


    @Override
    public void onClose(Association as) {
        for (DimseRQHandler service : services.values())
            service.onClose(as);
    }

}
