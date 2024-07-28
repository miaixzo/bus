/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MoveSCU extends Device implements AutoCloseable {

    private static final int[] DEF_IN_FILTER = { Tag.SOPInstanceUID, Tag.StudyInstanceUID, Tag.SeriesInstanceUID };
    private final ApplicationEntity ae = new ApplicationEntity("MOVESCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final transient AAssociateRQ rq = new AAssociateRQ();
    private final Attributes keys = new Attributes();
    private final transient Status state;
    private int priority;
    private String destination;
    private InformationModel model;
    private int[] inFilter = DEF_IN_FILTER;
    private transient Association as;
    private int cancelAfter;
    private boolean releaseEager;

    public MoveSCU() {
        this(null);
    }

    public MoveSCU(ImageProgress progress) {
        super("movescu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
        state = new Status(progress);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public void setReleaseEager(boolean releaseEager) {
        this.releaseEager = releaseEager;
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

    public final void setDestination(String destination) {
        this.destination = destination;
    }

    public void addKey(int tag, String... ss) {
        VR vr = ElementDictionary.vrOf(tag, keys.getPrivateCreator(tag));
        keys.setString(tag, vr, ss);
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
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

    public Attributes getKeys() {
        return keys;
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
            attrs.addSelected(dis.readDataset(-1, -1), inFilter);
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
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                ImageProgress p = state.getProgress();
                if (p != null) {
                    p.setAttributes(cmd);
                    if (p.isCancel()) {
                        try {
                            this.cancel(as);
                        } catch (IOException e) {
                            Logger.error("Cancel C-MOVE", e);
                        }
                    }
                }
            }
        };
        as.cmove(model.cuid, priority, keys, null, destination, rspHandler);
        if (cancelAfter > 0) {
            schedule(() -> {
                try {
                    rspHandler.cancel(as);
                    if (releaseEager) {
                        as.release();
                    }
                } catch (IOException e) {
                    Logger.error("Cancel after C-MOVE", e);
                }
            }, cancelAfter, TimeUnit.MILLISECONDS);
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public Status getState() {
        return state;
    }

    public enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelMove.uid, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelMove.uid, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelMove.uid, "STUDY"),
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveMove.uid, "IMAGE"),
        HangingProtocol(UID.HangingProtocolInformationModelMove.uid, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelMove.uid, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        public String getCuid() {
            return cuid;
        }
    }

}