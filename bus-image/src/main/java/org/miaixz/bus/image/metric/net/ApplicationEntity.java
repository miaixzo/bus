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
package org.miaixz.bus.image.metric.net;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.pdu.*;
import org.miaixz.bus.logger.Logger;

/**
 * Provides DICOM processing details. Provides DICOM processing details.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ApplicationEntity implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852269956092L;

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashSet<String> acceptedCallingAETs = new LinkedHashSet<>();

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashSet<String> otherAETs = new LinkedHashSet<>();

    /**
     * Collection of called application entity titles with asynchronous mode disabled
     */
    private final LinkedHashSet<String> noAsyncModeCalledAETs = new LinkedHashSet<>();

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashMap<String, String> masqueradeCallingAETs = new LinkedHashMap<>();

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashMap<String, String> masqueradeCalledAETs = new LinkedHashMap<>();

    /**
     * Provides DICOM processing details.
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashMap<String, TransferCapability> scuTCs = new LinkedHashMap<>();

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashMap<String, TransferCapability> scpTCs = new LinkedHashMap<>();

    /**
     * Provides DICOM processing details.
     */
    private final LinkedHashMap<Class<? extends AEExtension>, AEExtension> extensions = new LinkedHashMap<>();

    /**
     * Provides DICOM processing details.
     */
    private Device device;

    /**
     * Provides DICOM processing details.
     */
    private String aet;

    /**
     * Provides DICOM processing details.
     */
    private String description;

    /**
     * Provides DICOM processing details.
     */
    private byte[][] vendorData = {};

    /**
     * Provides DICOM processing details.
     */
    private String[] applicationClusters = {};

    /**
     * Provides DICOM processing details.
     */
    private String[] prefCalledAETs = {};

    /**
     * Provides DICOM processing details.
     */
    private String[] prefCallingAETs = {};

    /**
     * Provides DICOM processing details.
     */
    private String[] prefTransferSyntaxes = {};

    /**
     * Provides DICOM processing details.
     */
    private String[] supportedCharacterSets = {};

    /**
     * Determines whether the condition is met.
     */
    private boolean acceptor = true;

    /**
     * Determines whether the condition is met.
     */
    private boolean initiator = true;

    /**
     * Determines whether the condition is met.
     */
    private Boolean installed;

    /**
     * Determines whether the condition is met.
     */
    private Boolean roleSelectionNegotiationLenient;

    /**
     * Provides DICOM processing details.
     */
    private String shareTransferCapabilitiesFromAETitle;

    /**
     * Provides DICOM processing details.
     */
    private String hl7ApplicationName;

    /**
     * Provides DICOM processing details.
     */
    private transient DimseRQHandler dimseRQHandler;

    /**
     * Creates a new instance.
     */
    public ApplicationEntity() {
        // No initialization required.
    }

    /**
     * Creates a new instance.
     *
     * @param aeTitle the ae title.
     */
    public ApplicationEntity(String aeTitle) {
        setAETitle(aeTitle);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * Sets the related value.
     *
     * @param device the device.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    public void setDevice(Device device) {
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
            for (Connection conn : conns)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " + device.getDeviceName());
        }
        this.device = device;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String getAETitle() {
        return aet;
    }

    /**
     * Sets the related value.
     *
     * @param aet the aet.
     * @throws IllegalArgumentException if the operation cannot be completed.
     */
    public void setAETitle(String aet) {
        if (aet.isEmpty())
            throw new IllegalArgumentException("AE title cannot be empty");
        Device device = this.device;
        if (device != null)
            device.removeApplicationEntity(this.aet);
        this.aet = aet;
        if (device != null)
            device.addApplicationEntity(this);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Sets the related value.
     *
     * @param description the description.
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * Sets the related value.
     *
     * @param vendorData the vendor data.
     */
    public final void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    /**
     * Sets the related value.
     *
     * @param clusters the clusters.
     */
    public void setApplicationClusters(String... clusters) {
        applicationClusters = clusters;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getPreferredCalledAETitles() {
        return prefCalledAETs;
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setPreferredCalledAETitles(String... aets) {
        prefCalledAETs = aets;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getPreferredCallingAETitles() {
        return prefCallingAETs;
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setPreferredCallingAETitles(String... aets) {
        prefCallingAETs = aets;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    /**
     * Sets the related value.
     *
     * @param transferSyntaxes the transfer syntaxes.
     * @throws IllegalArgumentException if the operation cannot be completed.
     */
    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes = Builder.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETs.toArray(new String[acceptedCallingAETs.size()]);
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setAcceptedCallingAETitles(String... aets) {
        acceptedCallingAETs.clear();
        Collections.addAll(acceptedCallingAETs, aets);
    }

    /**
     * Determines whether the condition is met.
     *
     * @param aet the aet.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isAcceptedCallingAETitle(String aet) {
        return acceptedCallingAETs.isEmpty() || acceptedCallingAETs.contains(aet);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getOtherAETitles() {
        return otherAETs.toArray(new String[otherAETs.size()]);
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setOtherAETitles(String... aets) {
        otherAETs.clear();
        Collections.addAll(otherAETs, aets);
    }

    /**
     * Determines whether the condition is met.
     *
     * @param aet the aet.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isOtherAETitle(String aet) {
        return otherAETs.contains(aet);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getNoAsyncModeCalledAETitles() {
        return noAsyncModeCalledAETs.toArray(new String[noAsyncModeCalledAETs.size()]);
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setNoAsyncModeCalledAETitles(String... aets) {
        noAsyncModeCalledAETs.clear();
        Collections.addAll(noAsyncModeCalledAETs, aets);
    }

    /**
     * Determines whether the condition is met.
     *
     * @param calledAET the called aet.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isNoAsyncModeCalledAETitle(String calledAET) {
        return noAsyncModeCalledAETs.contains(calledAET);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getMasqueradeCallingAETitles() {
        String[] aets = new String[masqueradeCallingAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : masqueradeCallingAETs.entrySet()) {
            aets[i++] = entry.getKey().equals(Symbol.STAR) ? entry.getValue()
                    : Symbol.C_BRACKET_LEFT + entry.getKey() + Symbol.C_BRACKET_RIGHT + entry.getValue();
        }
        return aets;
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setMasqueradeCallingAETitles(String... aets) {
        masqueradeCallingAETs.clear();
        for (String aet : aets) {
            if (aet.charAt(0) == '[') {
                int end = aet.indexOf(Symbol.C_BRACKET_RIGHT);
                if (end > 0)
                    masqueradeCallingAETs.put(aet.substring(1, end), aet.substring(end + 1));
            } else {
                masqueradeCallingAETs.put(Symbol.STAR, aet);
            }
        }
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getMasqueradeCalledAETitles() {
        String[] aets = new String[masqueradeCalledAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : masqueradeCalledAETs.entrySet()) {
            aets[i++] = entry.getKey() + Symbol.C_COLON + entry.getValue();
        }
        return aets;
    }

    /**
     * Sets the related value.
     *
     * @param aets the aets.
     */
    public void setMasqueradeCalledAETitles(String... aets) {
        masqueradeCalledAETs.clear();
        for (String aet : aets) {
            int index = aet.indexOf(Symbol.C_COLON);
            if (index > 0)
                masqueradeCalledAETs.put(aet.substring(0, index), aet.substring(index + 1));
        }
    }

    /**
     * Gets the related value.
     *
     * @param calledAET the called aet.
     * @return the result.
     */
    public String getCallingAETitle(String calledAET) {
        String callingAET = masqueradeCallingAETs.get(calledAET);
        if (callingAET == null) {
            callingAET = masqueradeCallingAETs.get(Symbol.STAR);
            if (callingAET == null)
                callingAET = aet;
        }
        return callingAET;
    }

    /**
     * Determines whether the condition is met.
     *
     * @param calledAET the called aet.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isMasqueradeCallingAETitle(String calledAET) {
        return masqueradeCallingAETs.containsKey(calledAET) || masqueradeCallingAETs.containsKey("*");
    }

    /**
     * Gets the related value.
     *
     * @param calledAET the called aet.
     * @return the result.
     */
    public String masqueradeCalledAETitle(String calledAET) {
        return masqueradeCalledAETs.getOrDefault(calledAET, calledAET);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getSupportedCharacterSets() {
        return supportedCharacterSets;
    }

    /**
     * Sets the related value.
     *
     * @param characterSets the character sets.
     */
    public void setSupportedCharacterSets(String... characterSets) {
        supportedCharacterSets = characterSets;
    }

    /**
     * Determines whether the condition is met.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isAssociationAcceptor() {
        return acceptor;
    }

    /**
     * Sets the related value.
     *
     * @param acceptor the acceptor.
     */
    public final void setAssociationAcceptor(boolean acceptor) {
        this.acceptor = acceptor;
    }

    /**
     * Determines whether the condition is met.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isAssociationInitiator() {
        return initiator;
    }

    /**
     * Sets the related value.
     *
     * @param initiator the initiator.
     */
    public final void setAssociationInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    /**
     * Determines whether the condition is met.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    /**
     * Gets the related value.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final Boolean getInstalled() {
        return installed;
    }

    /**
     * Sets the related value.
     *
     * @param installed the installed.
     */
    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    /**
     * Determines whether the condition is met.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient != null ? roleSelectionNegotiationLenient.booleanValue()
                : device != null && device.isRoleSelectionNegotiationLenient();
    }

    /**
     * Gets the related value.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final Boolean getRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    /**
     * Sets the related value.
     *
     * @param roleSelectionNegotiationLenient the role selection negotiation lenient.
     */
    public void setRoleSelectionNegotiationLenient(Boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String getShareTransferCapabilitiesFromAETitle() {
        return shareTransferCapabilitiesFromAETitle;
    }

    /**
     * Sets the related value.
     *
     * @param shareTransferCapabilitiesFromAETitle the share transfer capabilities from aetitle.
     */
    public void setShareTransferCapabilitiesFromAETitle(String shareTransferCapabilitiesFromAETitle) {
        this.shareTransferCapabilitiesFromAETitle = shareTransferCapabilitiesFromAETitle;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public ApplicationEntity transferCapabilitiesAE() {
        return shareTransferCapabilitiesFromAETitle != null
                ? device.getApplicationEntity(shareTransferCapabilitiesFromAETitle)
                : this;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    /**
     * Sets the related value.
     *
     * @param hl7ApplicationName the hl7 application name.
     */
    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public DimseRQHandler getDimseRQHandler() {
        DimseRQHandler handler = dimseRQHandler;
        if (handler != null)
            return handler;
        Device device = this.device;
        return device != null ? device.getDimseRQHandler() : null;
    }

    /**
     * Sets the related value.
     *
     * @param dimseRQHandler the dimse rqhandler.
     */
    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    /**
     * Determines whether the condition is met.
     *
     * @throws IllegalStateException if the operation cannot be completed.
     */
    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    /**
     * Determines whether the condition is met.
     *
     * @throws IllegalStateException if the operation cannot be completed.
     */
    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    /**
     * Provides DICOM processing details.
     *
     * @param as       the as.
     * @param pc       the pc.
     * @param cmd      the cmd.
     * @param cmdAttrs the cmd attrs.
     * @param data     the data.
     * @throws IOException if the operation cannot be completed.
     */
    public void onDimseRQ(Association as, PresentationContext pc, Dimse cmd, Attributes cmdAttrs, PDVInputStream data)
            throws IOException {
        DimseRQHandler tmp = getDimseRQHandler();
        if (tmp == null) {
            Logger.error(false, "Image", "DimseRQHandler not initalized");
            throw new AAbort();
        }
        tmp.onDimseRQ(as, pc, cmd, cmdAttrs, data);
    }

    /**
     * Adds the related value.
     *
     * @param conn the conn.
     * @throws IllegalArgumentException if the operation cannot be completed.
     * @throws IllegalStateException    if the operation cannot be completed.
     */
    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.DICOM)
            throw new IllegalArgumentException("protocol != DICOM - " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by Device: " + device.getDeviceName());
        conns.add(conn);
    }

    /**
     * Removes the related value.
     *
     * @param conn the conn.
     * @return true if the condition is met; otherwise false.
     */
    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public List<Connection> getConnections() {
        return conns;
    }

    /**
     * Adds the related value.
     *
     * @param tc the tc.
     * @return true if the condition is met; otherwise false.
     */
    public TransferCapability addTransferCapability(TransferCapability tc) {
        tc.setApplicationEntity(this);
        TransferCapability prev = (tc.getRole() == TransferCapability.Role.SCU ? scuTCs : scpTCs)
                .put(tc.getSopClass(), tc);
        if (prev != null && prev != tc)
            prev.setApplicationEntity(null);
        return prev;
    }

    /**
     * Removes the related value.
     *
     * @param sopClass the sop class.
     * @param role     the role.
     * @return true if the condition is met; otherwise false.
     */
    public TransferCapability removeTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        TransferCapability tc = (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).remove(sopClass);
        if (tc != null)
            tc.setApplicationEntity(null);
        return tc;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs = new ArrayList<>(scuTCs.size() + scpTCs.size());
        tcs.addAll(scpTCs.values());
        tcs.addAll(scuTCs.values());
        return tcs;
    }

    /**
     * Gets the related value.
     *
     * @param role the role, either SCU or SCP.
     * @return the result.
     */
    public Collection<TransferCapability> getTransferCapabilitiesWithRole(TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).values();
    }

    /**
     * Gets the related value.
     *
     * @param sopClass the sop class.
     * @param role     the role.
     * @return true if the condition is met; otherwise false.
     */
    public TransferCapability getTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).get(sopClass);
    }

    /**
     * Determines whether the condition is met.
     *
     * @param sopClass the sop class.
     * @param role     the role.
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).containsKey(sopClass);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param rq   A-ASSOCIATE-RQ PDU
     * @param ac   A-ASSOCIATE-AC PDU
     * @param rqpc the rqpc.
     * @return the result.
     */
    public PresentationContext negotiate(AAssociateRQ rq, AAssociateAC ac, PresentationContext rqpc) {
        String as = rqpc.getAbstractSyntax();
        TransferCapability tc = roleSelection(rq, ac, as);
        int pcid = rqpc.getPCID();
        if (tc == null)
            return new PresentationContext(pcid, PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED,
                    rqpc.getTransferSyntax());
        String ts = tc.selectTransferSyntax(rqpc.getTransferSyntaxes());
        if (ts == null)
            return new PresentationContext(pcid, PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED,
                    rqpc.getTransferSyntax());
        byte[] info = negotiate(rq.getExtNegotiationFor(as), tc);
        if (info != null)
            ac.addExtendedNegotiation(new ExtendedNegotiation(as, info));
        return new PresentationContext(pcid, PresentationContext.ACCEPTANCE, ts);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param rq    A-ASSOCIATE-RQ PDU
     * @param ac    A-ASSOCIATE-AC PDU
     * @param asuid the asuid.
     * @return the result.
     */
    private TransferCapability roleSelection(AAssociateRQ rq, AAssociateAC ac, String asuid) {
        RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
        if (rqrs == null)
            return getTC(scpTCs, asuid, rq);
        RoleSelection acrs = ac.getRoleSelectionFor(asuid);
        if (acrs != null)
            return getTC(acrs.isSCU() ? scpTCs : scuTCs, asuid, rq);
        TransferCapability tcscu = null;
        TransferCapability tcscp = null;
        boolean scu = rqrs.isSCU() && (tcscp = getTC(scpTCs, asuid, rq)) != null;
        boolean scp = rqrs.isSCP() && (tcscu = getTC(scuTCs, asuid, rq)) != null;
        ac.addRoleSelection(new RoleSelection(asuid, scu, scp));
        return scu ? tcscp : tcscu;
    }

    /**
     * Gets the related value.
     *
     * @param tcs   the tcs.
     * @param asuid the asuid.
     * @param rq    A-ASSOCIATE-RQ PDU
     * @return true if the condition is met; otherwise false.
     */
    private TransferCapability getTC(HashMap<String, TransferCapability> tcs, String asuid, AAssociateRQ rq) {
        TransferCapability tc = tcs.get(asuid);
        if (tc != null)
            return tc;
        CommonExtended commonExtNeg = rq.getCommonExtendedNegotiationFor(asuid);
        if (commonExtNeg != null) {
            for (String cuid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                tc = tcs.get(cuid);
                if (tc != null)
                    return tc;
            }
            tc = tcs.get(commonExtNeg.getServiceClassUID());
            if (tc != null)
                return tc;
        }
        return tcs.get("*");
    }

    /**
     * Provides DICOM processing details.
     *
     * @param exneg the exneg.
     * @param tc    the tc.
     * @return true if the condition is met; otherwise false.
     */
    private byte[] negotiate(ExtendedNegotiation exneg, TransferCapability tc) {
        if (exneg == null)
            return null;
        StorageOptions storageOptions = tc.getStorageOptions();
        if (storageOptions != null)
            return storageOptions.toExtendedNegotiationInformation();
        EnumSet<QueryOption> queryOptions = tc.getQueryOptions();
        if (queryOptions != null) {
            EnumSet<QueryOption> commonOpts = QueryOption.toOptions(exneg);
            commonOpts.retainAll(queryOptions);
            return QueryOption.toExtendedNegotiationInformation(commonOpts);
        }
        return null;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param local  the local.
     * @param remote the remote.
     * @param rq     A-ASSOCIATE-RQ PDU
     * @return the result.
     * @throws IOException              if the operation cannot be completed.
     * @throws InterruptedException     if the operation cannot be completed.
     * @throws InternalException        if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public Association connect(Connection local, Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        if (rq.getCallingAET() == null)
            rq.setCallingAET(getCallingAETitle(rq.getCalledAET()));
        if (!isNoAsyncModeCalledAETitle(rq.getCalledAET())) {
            rq.setMaxOpsInvoked(local.getMaxOpsInvoked());
            rq.setMaxOpsPerformed(local.getMaxOpsPerformed());
        }
        rq.setMaxPDULength(local.getReceivePDULength());
        Socket sock = local.connect(remote);
        AssociationMonitor monitor = device.getAssociationMonitor();
        Association as = null;
        try {
            as = new Association(this, local, sock);
            as.write(rq);
            as.waitForLeaving(State.Sta5);
            if (monitor != null)
                monitor.onAssociationEstablished(as);
            return as;
        } catch (InterruptedException | IOException e) {
            IoKit.close(sock);
            if (as != null && monitor != null)
                monitor.onAssociationFailed(as, e);
            throw e;
        }
    }

    /**
     * Provides DICOM processing details.
     *
     * @param remote the remote.
     * @param rq     A-ASSOCIATE-RQ PDU
     * @return the result.
     * @throws IOException              if the operation cannot be completed.
     * @throws InterruptedException     if the operation cannot be completed.
     * @throws InternalException        if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public Association connect(Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote, rq);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param remoteConn the remote conn.
     * @return the result.
     * @throws InternalException if the operation cannot be completed.
     */
    public Connection findCompatibleConnection(Connection remoteConn) throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException("No compatible connection to " + remoteConn + " available on " + aet);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param remote the remote.
     * @return the result.
     * @throws InternalException if the operation cannot be completed.
     */
    public Compatible findCompatibleConnection(ApplicationEntity remote) throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException("No compatible connection to " + remote.getAETitle() + " available on " + aet);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param remote the remote.
     * @param rq     A-ASSOCIATE-RQ PDU
     * @return the result.
     * @throws IOException              if the operation cannot be completed.
     * @throws InterruptedException     if the operation cannot be completed.
     * @throws InternalException        if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public Association connect(ApplicationEntity remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        if (rq.getCalledAET() == null)
            rq.setCalledAET(masqueradeCalledAETitle(remote.getAETitle()));
        return connect(cc.getLocalConnection(), cc.getRemoteConnection(), rq);
    }

    /**
     * Returns the related value.
     *
     * @return the result.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    /**
     * Adds the related value.
     *
     * @param sb     the sb.
     * @param indent the indent.
     * @return the result.
     */
    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "ApplicationEntity[title: ", aet);
        Builder.appendLine(sb, indent2, "desc: ", description);
        Builder.appendLine(sb, indent2, "acceptor: ", acceptor);
        Builder.appendLine(sb, indent2, "initiator: ", initiator);
        Builder.appendLine(sb, indent2, "installed: ", getInstalled());
        for (Connection conn : conns)
            conn.promptTo(sb, indent2).append(Builder.LINE_SEPARATOR);
        for (TransferCapability tc : getTransferCapabilities())
            tc.promptTo(sb, indent2).append(Builder.LINE_SEPARATOR);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param src the src.
     */
    public void reconfigure(ApplicationEntity src) {
        setApplicationEntityAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureTransferCapabilities(src);
        reconfigureAEExtensions(src);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param src the src.
     */
    private void reconfigureTransferCapabilities(ApplicationEntity src) {
        scuTCs.clear();
        scuTCs.putAll(src.scuTCs);
        scpTCs.clear();
        scpTCs.putAll(src.scpTCs);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param from the from.
     */
    private void reconfigureAEExtensions(ApplicationEntity from) {
        extensions.keySet().removeIf(aClass -> !from.extensions.containsKey(aClass));
        for (AEExtension src : from.extensions.values()) {
            Class<? extends AEExtension> clazz = src.getClass();
            AEExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addAEExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    /**
     * Sets the related value.
     *
     * @param from the from.
     */
    protected void setApplicationEntityAttributes(ApplicationEntity from) {
        description = from.description;
        vendorData = from.vendorData;
        applicationClusters = from.applicationClusters;
        prefCalledAETs = from.prefCalledAETs;
        prefCallingAETs = from.prefCallingAETs;
        acceptedCallingAETs.clear();
        acceptedCallingAETs.addAll(from.acceptedCallingAETs);
        otherAETs.clear();
        otherAETs.addAll(from.otherAETs);
        noAsyncModeCalledAETs.clear();
        noAsyncModeCalledAETs.addAll(from.noAsyncModeCalledAETs);
        masqueradeCallingAETs.clear();
        masqueradeCallingAETs.putAll(from.masqueradeCallingAETs);
        supportedCharacterSets = from.supportedCharacterSets;
        prefTransferSyntaxes = from.prefTransferSyntaxes;
        shareTransferCapabilitiesFromAETitle = from.shareTransferCapabilitiesFromAETitle;
        hl7ApplicationName = from.hl7ApplicationName;
        acceptor = from.acceptor;
        initiator = from.initiator;
        installed = from.installed;
        roleSelectionNegotiationLenient = from.roleSelectionNegotiationLenient;
    }

    /**
     * Adds the related value.
     *
     * @param ext the ext.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    public void addAEExtension(AEExtension ext) {
        Class<? extends AEExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains AE Extension:" + clazz);
        ext.setApplicationEntity(this);
        extensions.put(clazz, ext);
    }

    /**
     * Removes the related value.
     *
     * @param ext the ext.
     * @return true if the condition is met; otherwise false.
     */
    public boolean removeAEExtension(AEExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;
        ext.setApplicationEntity(null);
        return true;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    public Collection<AEExtension> listAEExtensions() {
        return extensions.values();
    }

    /**
     * Gets the related value.
     *
     * @param <T>   the t type.
     * @param clazz the clazz.
     * @return true if the condition is met; otherwise false.
     */
    public <T extends AEExtension> T getAEExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    /**
     * Gets the related value.
     *
     * @param <T>   the t type.
     * @param clazz the clazz.
     * @return the result.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    public <T extends AEExtension> T getAEExtensionNotNull(Class<T> clazz) {
        T aeExt = getAEExtension(clazz);
        if (aeExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for AE: " + aet);
        return aeExt;
    }

}
