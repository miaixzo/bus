/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.pdu.RoleSelection;
import org.miaixz.bus.image.metric.service.BasicCStoreSCP;
import org.miaixz.bus.image.metric.service.ImageServiceException;
import org.miaixz.bus.image.metric.service.ImageServiceRegistry;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class GetSCU implements AutoCloseable {

    private static final int[] DEF_IN_FILTER = { Tag.SOPInstanceUID, Tag.StudyInstanceUID, Tag.SeriesInstanceUID };
    private static final String TMP_DIR = "tmp";
    private final Device device = new Device("getscu");
    private final ApplicationEntity ae;
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Attributes keys = new Attributes();
    private final Status state;
    private int priority;
    private InformationModel model;
    private File storageDir;
    private int[] inFilter = DEF_IN_FILTER;
    private Association as;
    private int cancelAfter;
    private DimseRSPHandler rspHandler;
    private long totalSize = 0;
    private final BasicCStoreSCP storageSCP = new BasicCStoreSCP(Symbol.STAR) {
        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            if (storageDir == null) {
                return;
            }

            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String tsuid = pc.getTransferSyntax();
            File file = new File(storageDir, TMP_DIR + File.separator + iuid);
            try {
                storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
                totalSize += file.length();
                File rename = new File(storageDir, iuid);
                renameTo(as, file, rename);
                ImageProgress p = state.getProgress();
                if (p != null) {
                    p.setProcessedFile(rename);
                }
            } catch (Exception e) {
                throw new ImageServiceException(Status.ProcessingFailure, e);
            }
            updateProgress(as, null);
        }
    };

    public GetSCU() {
        this(null);
    }

    public GetSCU(ImageProgress progress) {
        ae = new ApplicationEntity("GETSCU");
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        device.setDimseRQHandler(createServiceRegistry());
        state = new Status(progress);
    }

    public static void storeTo(Association as, Attributes fmi, PDVInputStream data, File file) throws IOException {
        Logger.debug("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        ImageOutputStream out = new ImageOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            IoKit.close(out);
        }
    }

    private static void renameTo(Association as, File from, File dest) throws IOException {
        Logger.info("{}: M-RENAME {} to {}", as, from, dest);
        Builder.prepareToWriteFile(dest);
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
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

    private ImageServiceRegistry createServiceRegistry() {
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(storageSCP);
        return serviceRegistry;
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null) {
            if (storageDir.mkdirs()) {
                System.out.println("M-WRITE " + storageDir);
            }
        }
        this.storageDir = storageDir;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setInformationModel(InformationModel model, String[] tss, boolean relational) {
        this.model = model;
        rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (relational) {
            rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, new byte[] { 1 }));
        }
        if (model.level != null) {
            addLevel(model.level);
        }
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
        if (!rq.containsPresentationContextFor(cuid)) {
            rq.addRoleSelection(new RoleSelection(cuid, false, true));
        }
        rq.addPresentationContext(new PresentationContext(2 * rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void retrieve(File f) throws IOException, InterruptedException {
        Attributes attrs = new Attributes();
        try (ImageInputStream dis = new ImageInputStream(f)) {
            attrs.addSelected(dis.readDataset(), inFilter);
        }
        attrs.addAll(keys);
        retrieve(attrs);
    }

    public void retrieve() throws IOException, InterruptedException {
        retrieve(keys);
    }

    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        final DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                updateProgress(as, cmd);
            }
        };

        retrieve(keys, rspHandler);
        if (cancelAfter > 0) {
            device.schedule(() -> {
                try {
                    rspHandler.cancel(as);
                } catch (IOException e) {
                    Logger.error("Cancel C-GET", e);
                }
            }, cancelAfter, TimeUnit.MILLISECONDS);
        }
    }

    public void retrieve(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        retrieve(keys, rspHandler);
    }

    private void retrieve(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.rspHandler = rspHandler;
        as.cget(model.getCuid(), priority, keys, null, rspHandler);
    }

    public Connection getConnection() {
        return conn;
    }

    public Status getState() {
        return state;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void stop() {
        try {
            close();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        Builder.shutdown((ExecutorService) device.getExecutor());
        Builder.shutdown(device.getScheduledExecutor());
    }

    private void updateProgress(Association as, Attributes cmd) {
        ImageProgress p = state.getProgress();
        if (p != null) {
            p.setAttributes(cmd);
            if (p.isCancel() && rspHandler != null) {
                try {
                    rspHandler.cancel(as);
                } catch (IOException e) {
                    Logger.error("Cancel C-GET", e);
                }
            }
        }
    }

    public enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelGet.uid, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelGet.uid, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelGet.uid, "STUDY"),
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveGet.uid, "IMAGE"),
        WithoutBulkData(UID.CompositeInstanceRetrieveWithoutBulkDataGet.uid, null),
        HangingProtocol(UID.HangingProtocolInformationModelGet.uid, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelGet.uid, null);

        final String level;
        private final String cuid;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        public String getCuid() {
            return cuid;
        }
    }

}
