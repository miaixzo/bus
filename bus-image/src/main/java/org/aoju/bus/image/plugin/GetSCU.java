/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.plugin;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.image.Device;
import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.DicomInputStream;
import org.aoju.bus.image.galaxy.io.DicomOutputStream;
import org.aoju.bus.image.metric.*;
import org.aoju.bus.image.metric.pdu.AAssociateRQ;
import org.aoju.bus.image.metric.pdu.ExtendedNegotiation;
import org.aoju.bus.image.metric.pdu.PresentationContext;
import org.aoju.bus.image.metric.pdu.RoleSelection;
import org.aoju.bus.image.metric.service.BasicCStoreSCP;
import org.aoju.bus.image.metric.service.DicomServiceException;
import org.aoju.bus.image.metric.service.DicomServiceRegistry;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class GetSCU {

    private static final int[] DEF_IN_FILTER = {
            Tag.SOPInstanceUID,
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID
    };
    private final Device device = new Device("getscu");
    private final ApplicationEntity ae;
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private InformationModel model;
    private File storageDir;
    private Attributes keys = new Attributes();
    private int[] inFilter = DEF_IN_FILTER;
    private Association as;
    private BasicCStoreSCP storageSCP = new BasicCStoreSCP("*") {

        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq,
                             PDVInputStream data, Attributes rsp)
                throws IOException {
            if (storageDir == null)
                return;

            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String tsuid = pc.getTransferSyntax();
            File file = new File(storageDir, iuid);
            try {
                storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid),
                        data, file);
            } catch (Exception e) {
                throw new DicomServiceException(Status.ProcessingFailure, e);
            }

        }


    };

    public GetSCU() throws IOException {
        ae = new ApplicationEntity("GETSCU");
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        device.setDimseRQHandler(createServiceRegistry());
    }

    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Association getAssociation() {
        return as;
    }

    public Device getDevice() {
        return device;
    }

    public Attributes getKeys() {
        return keys;
    }

    private void storeTo(Association as, Attributes fmi,
                         PDVInputStream data, File file) throws IOException {
        Logger.info("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            IoUtils.close(out);
        }
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(storageSCP);
        return serviceRegistry;
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            if (storageDir.mkdirs())
                Logger.info("M-WRITE " + storageDir);
        this.storageDir = storageDir;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss,
                                          boolean relational) {
        this.model = model;
        rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (relational)
            rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, new byte[]{1}));
        if (model.level != null)
            addLevel(model.level);
    }

    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    public void addKey(int tag, String... ss) {
        VR vr = ElementDictionary.vrOf(tag, keys.getPrivateCreator(tag));
        keys.setString(tag, vr, ss);
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
    }

    public void addOfferedStorageSOPClass(String cuid, String... tsuids) {
        if (!rq.containsPresentationContextFor(cuid))
            rq.addRoleSelection(new RoleSelection(cuid, false, true));
        rq.addPresentationContext(new PresentationContext(
                2 * rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    public void open() throws IOException, InterruptedException, InstrumentException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void retrieve(File f) throws IOException, InterruptedException {
        Attributes attrs = new Attributes();
        DicomInputStream dis = null;
        try {
            attrs.addSelected(new DicomInputStream(f).readDataset(-1, -1), inFilter);
        } finally {
            IoUtils.close(dis);
        }
        attrs.addAll(keys);
        retrieve(attrs);
    }

    public void retrieve() throws IOException, InterruptedException {
        retrieve(keys);
    }

    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                                   Attributes data) {
                super.onDimseRSP(as, cmd, data);
            }
        };

        retrieve(keys, rspHandler);
    }

    public void retrieve(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        retrieve(keys, rspHandler);
    }

    private void retrieve(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        as.cget(model.cuid, priority, keys, null, rspHandler);
    }

    public enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelGET, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelGET, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired, "STUDY"),
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveGET, "IMAGE"),
        WithoutBulkData(UID.CompositeInstanceRetrieveWithoutBulkDataGET, null),
        HangingProtocol(UID.HangingProtocolInformationModelGET, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelGET, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }
    }

}
