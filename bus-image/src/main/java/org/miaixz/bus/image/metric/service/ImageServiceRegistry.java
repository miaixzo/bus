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
import java.util.HashMap;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.errors.ComponentFailure;
import org.miaixz.bus.errors.ErrorType;
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
 * Dispatches incoming DICOM DIMSE requests to registered SOP Class handlers. A service failure is observed through Bus
 * and sent to the peer with its original DIMSE status.
 *
 * @author Kimi Liu
 */
public class ImageServiceRegistry implements DimseRQHandler {

    /**
     * Reports DICOM command failures while the association retains ownership of the DIMSE response.
     */
    private static final ComponentFailure FAILURES = new ComponentFailure("bus-image", "dicom-dimse");

    /**
     * DIMSE request handlers indexed by SOP Class UID.
     */
    private final HashMap<String, DimseRQHandler> services = new HashMap<>();

    /**
     * Creates a registry without any DIMSE request handlers.
     */
    public ImageServiceRegistry() {
        // No initialization required.
    }

    /**
     * Registers a DICOM service for all SOP classes it declares.
     *
     * @param service DICOM service whose declared SOP classes are registered
     */
    public void addDicomService(ImageService service) {
        addDimseRQHandler(service, service.getSOPClasses());
    }

    /**
     * Registers a DIMSE request handler for each given SOP Class UID.
     *
     * @param service    DIMSE request handler to register
     * @param sopClasses SOP Class UIDs affected by this registration change
     */
    public synchronized void addDimseRQHandler(DimseRQHandler service, String... sopClasses) {
        for (String uid : sopClasses)
            services.put(uid, service);
    }

    /**
     * Removes registrations for the SOP classes declared by a service.
     *
     * @param service DICOM service whose declared SOP classes are removed
     */
    public void removeDicomService(ImageService service) {
        removeDimseRQHandler(service.getSOPClasses());
    }

    /**
     * Removes the handler registration for each given SOP Class UID.
     *
     * @param sopClasses SOP Class UIDs affected by this registration change
     */
    public synchronized void removeDimseRQHandler(String... sopClasses) {
        for (String uid : sopClasses)
            services.remove(uid);
    }

    /**
     * Dispatches a DIMSE request and writes the original protocol error if the service rejects it.
     *
     * @param as    association that received the request
     * @param pc    presentation context negotiated for the request
     * @param dimse DIMSE request command
     * @param cmd   request command attributes
     * @param data  request data stream, when the command carries a data set
     * @throws IOException if the selected service cannot read or process the request stream
     */
    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data)
            throws IOException {
        try {
            lookupService(as, dimse, cmd).onDimseRQ(as, pc, dimse, cmd, data);
        } catch (ImageServiceException e) {
            FAILURES.failed(ErrorType.EXECUTION_FAILURE, e);
            Logger.info(
                    false,
                    "Image",
                    "{}: processing {} failed. Caused by:\t",
                    as,
                    dimse.toString(cmd, pc.getPCID(), pc.getTransferSyntax()),
                    e);
            rspForDimseRQException(as, pc, dimse, cmd, e);
        }
    }

    /**
     * Constructs and attempts to write the DIMSE failure response for the original request.
     *
     * @param as    association that received the request
     * @param pc    presentation context negotiated for the request
     * @param dimse DIMSE request command
     * @param cmd   request command attributes
     * @param e     failure carrying the original protocol status and optional data set
     */
    private void rspForDimseRQException(
            Association as,
            PresentationContext pc,
            Dimse dimse,
            Attributes cmd,
            ImageServiceException e) {
        Attributes rsp = e.mkRSP(dimse.commandFieldOfRSP(), cmd.getInt(Tag.MessageID, 0));
        as.tryWriteDimseRSP(pc, rsp, e.getDataset());
    }

    /**
     * Selects a handler by SOP Class UID and applies negotiated C-STORE fallback rules.
     *
     * @param as    association that received the request
     * @param dimse DIMSE request command
     * @param cmd   request command attributes
     * @return handler that supports the requested or negotiated SOP class
     * @throws ImageServiceException if the command omits its SOP class or no handler supports it
     */
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
            service = services.get(Symbol.STAR);
            if (service != null)
                return service;
        }
        throw new ImageServiceException(dimse.isCService() ? Status.SOPclassNotSupported : Status.NoSuchSOPclass);
    }

    /**
     * Notifies registered DIMSE handlers when the association closes.
     *
     * @param as closed DICOM association
     */
    @Override
    public void onClose(Association as) {
        for (DimseRQHandler service : services.values())
            service.onClose(as);
    }

}
