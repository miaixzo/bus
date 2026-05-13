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
package org.miaixz.bus.image.metric.pdu;

import java.util.*;

import org.miaixz.bus.core.center.map.IntHashMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Implementation;
import org.miaixz.bus.image.metric.Connection;

/**
 * Represents the AAssociateRQAC type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AAssociateRQAC {

    /**
     * The pcs value.
     */
    protected final List<PresentationContext> pcs = new ArrayList<>();

    /**
     * The pcid map value.
     */
    protected final IntHashMap<PresentationContext> pcidMap = new IntHashMap<>();

    /**
     * The role sel map value.
     */
    protected final LinkedHashMap<String, RoleSelection> roleSelMap = new LinkedHashMap<>();

    /**
     * The ext neg map value.
     */
    protected final LinkedHashMap<String, ExtendedNegotiation> extNegMap = new LinkedHashMap<>();

    /**
     * The common ext neg map value.
     */
    protected final LinkedHashMap<String, CommonExtended> commonExtNegMap = new LinkedHashMap<>();

    /**
     * The reserved bytes value.
     */
    protected byte[] reservedBytes = new byte[Normal._32];

    /**
     * The protocol version value.
     */
    protected int protocolVersion = 1;

    /**
     * The max pdu length value.
     */
    protected int maxPDULength = Connection.DEF_MAX_PDU_LENGTH;

    /**
     * The max ops invoked value.
     */
    protected int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;

    /**
     * The max ops performed value.
     */
    protected int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;

    /**
     * The called aet value.
     */
    protected String calledAET;

    /**
     * The calling aet value.
     */
    protected String callingAET;

    /**
     * The application context value.
     */
    protected String applicationContext = UID.DICOMApplicationContext.uid;

    /**
     * The impl class uid value.
     */
    protected String implClassUID = Implementation.getClassUID();

    /**
     * The impl version name value.
     */
    protected String implVersionName = Implementation.getVersionName();

    /**
     * The identity rq value.
     */
    protected IdentityRQ identityRQ;

    /**
     * The identity ac value.
     */
    protected IdentityAC identityAC;

    /**
     * Executes the check calling aet operation.
     */
    public void checkCallingAET() {
        if (callingAET == null)
            throw new IllegalStateException("Calling AET not initalized");
    }

    /**
     * Executes the check called aet operation.
     */
    public void checkCalledAET() {
        if (calledAET == null)
            throw new IllegalStateException("Called AET not initalized");
    }

    /**
     * Gets the protocol version.
     *
     * @return the protocol version.
     */
    public final int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the protocol version.
     *
     * @param protocolVersion the protocol version.
     */
    public final void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Gets the reserved bytes.
     *
     * @return the reserved bytes.
     */
    public final byte[] getReservedBytes() {
        return reservedBytes.clone();
    }

    /**
     * Sets the reserved bytes.
     *
     * @param reservedBytes the reserved bytes.
     */
    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32)
            throw new IllegalArgumentException("reservedBytes.length: " + reservedBytes.length);
        System.arraycopy(reservedBytes, 0, this.reservedBytes, 0, 32);
    }

    /**
     * Gets the called aet.
     *
     * @return the called aet.
     */
    public final String getCalledAET() {
        return calledAET;
    }

    /**
     * Sets the called aet.
     *
     * @param calledAET the called aet.
     */
    public final void setCalledAET(String calledAET) {
        if (calledAET.length() > 16)
            throw new IllegalArgumentException("calledAET: " + calledAET);
        this.calledAET = calledAET;
    }

    /**
     * Gets the calling aet.
     *
     * @return the calling aet.
     */
    public final String getCallingAET() {
        return callingAET;
    }

    /**
     * Sets the calling aet.
     *
     * @param callingAET the calling aet.
     */
    public final void setCallingAET(String callingAET) {
        if (callingAET.length() > Normal._16)
            throw new IllegalArgumentException("callingAET: " + callingAET);
        this.callingAET = callingAET;
    }

    /**
     * Gets the application context.
     *
     * @return the application context.
     */
    public final String getApplicationContext() {
        return applicationContext;
    }

    /**
     * Sets the application context.
     *
     * @param applicationContext the application context.
     */
    public final void setApplicationContext(String applicationContext) {
        if (applicationContext == null)
            throw new NullPointerException();

        this.applicationContext = applicationContext;
    }

    /**
     * Gets the max pdu length.
     *
     * @return the max pdu length.
     */
    public final int getMaxPDULength() {
        return maxPDULength;
    }

    /**
     * Sets the max pdu length.
     *
     * @param maxPDULength the max pdu length.
     */
    public final void setMaxPDULength(int maxPDULength) {
        this.maxPDULength = maxPDULength;
    }

    /**
     * Gets the max ops invoked.
     *
     * @return the max ops invoked.
     */
    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    /**
     * Sets the max ops invoked.
     *
     * @param maxOpsInvoked the max ops invoked.
     */
    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    /**
     * Gets the max ops performed.
     *
     * @return the max ops performed.
     */
    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    /**
     * Sets the max ops performed.
     *
     * @param maxOpsPerformed the max ops performed.
     */
    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    /**
     * Determines whether async ops.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isAsyncOps() {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    /**
     * Gets the impl class uid.
     *
     * @return the impl class uid.
     */
    public final String getImplClassUID() {
        return implClassUID;
    }

    /**
     * Sets the impl class uid.
     *
     * @param implClassUID the impl class uid.
     */
    public final void setImplClassUID(String implClassUID) {
        if (implClassUID == null)
            throw new NullPointerException();

        this.implClassUID = implClassUID;
    }

    /**
     * Gets the impl version name.
     *
     * @return the impl version name.
     */
    public final String getImplVersionName() {
        return implVersionName;
    }

    /**
     * Sets the impl version name.
     *
     * @param implVersionName the impl version name.
     */
    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    /**
     * Gets the user identity rq.
     *
     * @return the user identity rq.
     */
    public final IdentityRQ getUserIdentityRQ() {
        return identityRQ;
    }

    /**
     * Sets the identity rq.
     *
     * @param identityRQ the identity rq.
     */
    public void setIdentityRQ(IdentityRQ identityRQ) {
        this.identityRQ = identityRQ;
    }

    /**
     * Gets the user identity ac.
     *
     * @return the user identity ac.
     */
    public final IdentityAC getUserIdentityAC() {
        return identityAC;
    }

    /**
     * Sets the identity ac.
     *
     * @param identityAC the identity ac.
     */
    public void setIdentityAC(IdentityAC identityAC) {
        this.identityAC = identityAC;
    }

    /**
     * Gets the presentation contexts.
     *
     * @return the presentation contexts.
     */
    public List<PresentationContext> getPresentationContexts() {
        return Collections.unmodifiableList(pcs);
    }

    /**
     * Gets the number of presentation contexts.
     *
     * @return the number of presentation contexts.
     */
    public int getNumberOfPresentationContexts() {
        return pcs.size();
    }

    /**
     * Gets the presentation context.
     *
     * @param pcid the pcid.
     * @return the presentation context.
     */
    public PresentationContext getPresentationContext(int pcid) {
        return pcidMap.get(pcid);
    }

    /**
     * Adds the presentation context.
     *
     * @param pc the pc.
     */
    public void addPresentationContext(PresentationContext pc) {
        int pcid = pc.getPCID();
        if (pcidMap.containsKey(pcid))
            throw new IllegalStateException("Already contains Presentation Context with pid: " + pcid);
        pcidMap.put(pcid, pc);
        pcs.add(pc);
    }

    /**
     * Removes the presentation context.
     *
     * @param pc the pc.
     * @return true if the condition is met; otherwise false.
     */
    public boolean removePresentationContext(PresentationContext pc) {
        if (!pcs.remove(pc))
            return false;

        pcidMap.remove(pc.getPCID());
        return true;
    }

    /**
     * Gets the role selections.
     *
     * @return the role selections.
     */
    public Collection<RoleSelection> getRoleSelections() {
        return Collections.unmodifiableCollection(roleSelMap.values());
    }

    /**
     * Gets the role selection for.
     *
     * @param cuid the cuid.
     * @return the role selection for.
     */
    public RoleSelection getRoleSelectionFor(String cuid) {
        return roleSelMap.get(cuid);
    }

    /**
     * Adds the role selection.
     *
     * @param rs the rs.
     * @return the operation result.
     */
    public RoleSelection addRoleSelection(RoleSelection rs) {
        return roleSelMap.put(rs.getSOPClassUID(), rs);
    }

    /**
     * Removes the role selection for.
     *
     * @param cuid the cuid.
     * @return the operation result.
     */
    public RoleSelection removeRoleSelectionFor(String cuid) {
        return roleSelMap.remove(cuid);
    }

    /**
     * Gets the extended negotiations.
     *
     * @return the extended negotiations.
     */
    public Collection<ExtendedNegotiation> getExtendedNegotiations() {
        return Collections.unmodifiableCollection(extNegMap.values());
    }

    /**
     * Gets the ext negotiation for.
     *
     * @param cuid the cuid.
     * @return the ext negotiation for.
     */
    public ExtendedNegotiation getExtNegotiationFor(String cuid) {
        return extNegMap.get(cuid);
    }

    /**
     * Adds the extended negotiation.
     *
     * @param extNeg the ext neg.
     * @return the operation result.
     */
    public ExtendedNegotiation addExtendedNegotiation(ExtendedNegotiation extNeg) {
        return extNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    /**
     * Removes the extended negotiation for.
     *
     * @param cuid the cuid.
     * @return the operation result.
     */
    public ExtendedNegotiation removeExtendedNegotiationFor(String cuid) {
        return extNegMap.remove(cuid);
    }

    /**
     * Gets the common extended negotiations.
     *
     * @return the common extended negotiations.
     */
    public Collection<CommonExtended> getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(commonExtNegMap.values());
    }

    /**
     * Gets the common extended negotiation for.
     *
     * @param cuid the cuid.
     * @return the common extended negotiation for.
     */
    public CommonExtended getCommonExtendedNegotiationFor(String cuid) {
        return commonExtNegMap.get(cuid);
    }

    /**
     * Adds the common extended negotiation.
     *
     * @param extNeg the ext neg.
     * @return the operation result.
     */
    public CommonExtended addCommonExtendedNegotiation(CommonExtended extNeg) {
        return commonExtNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    /**
     * Removes the common extended negotiation for.
     *
     * @param cuid the cuid.
     * @return the operation result.
     */
    public CommonExtended removeCommonExtendedNegotiationFor(String cuid) {
        return commonExtNegMap.remove(cuid);
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public int length() {
        int len = 68; // Fix AA-RQ/AC PDU fields
        len += 4 + applicationContext.length();
        for (PresentationContext pc : pcs) {
            len += 4 + pc.length();
        }
        len += 4 + userInfoLength();
        return len;
    }

    /**
     * Executes the user info length operation.
     *
     * @return the operation result.
     */
    public int userInfoLength() {
        int len = 8; // Maximum length sub-item
        len += 4 + implClassUID.length();
        if (isAsyncOps())
            len += 8; // Asynchronous operation window sub-item
        for (RoleSelection rs : roleSelMap.values()) {
            len += 4 + rs.length();
        }
        if (implVersionName != null)
            len += 4 + implVersionName.length();
        for (ExtendedNegotiation en : extNegMap.values()) {
            len += 4 + en.length();
        }
        for (CommonExtended cen : commonExtNegMap.values()) {
            len += 4 + cen.length();
        }
        if (identityRQ != null)
            len += 4 + identityRQ.length();
        if (identityAC != null)
            len += 4 + identityAC.length();
        return len;
    }

    /**
     * Executes the prompt to operation.
     *
     * @param header the header.
     * @param sb     the sb.
     * @return the operation result.
     */
    protected StringBuilder promptTo(String header, StringBuilder sb) {
        sb.append(header).append(Builder.LINE_SEPARATOR).append("  calledAET: ").append(calledAET)
                .append(Builder.LINE_SEPARATOR).append("  callingAET: ").append(callingAET)
                .append(Builder.LINE_SEPARATOR).append("  applicationContext: ");
        UID.promptTo(applicationContext, sb).append(Builder.LINE_SEPARATOR).append("  implClassUID: ")
                .append(implClassUID).append(Builder.LINE_SEPARATOR).append("  implVersionName: ")
                .append(implVersionName).append(Builder.LINE_SEPARATOR).append("  maxPDULength: ").append(maxPDULength)
                .append(Builder.LINE_SEPARATOR).append("  maxOpsInvoked/maxOpsPerformed: ").append(maxOpsInvoked)
                .append("/").append(maxOpsPerformed).append(Builder.LINE_SEPARATOR);
        if (identityRQ != null)
            identityRQ.promptTo(sb).append(Builder.LINE_SEPARATOR);
        if (identityAC != null)
            identityAC.promptTo(sb).append(Builder.LINE_SEPARATOR);
        for (PresentationContext pc : pcs)
            pc.promptTo(sb).append(Builder.LINE_SEPARATOR);
        for (RoleSelection rs : roleSelMap.values())
            rs.promptTo(sb).append(Builder.LINE_SEPARATOR);
        for (ExtendedNegotiation extNeg : extNegMap.values())
            extNeg.promptTo(sb).append(Builder.LINE_SEPARATOR);
        for (CommonExtended extNeg : commonExtNegMap.values())
            extNeg.promptTo(sb).append(Builder.LINE_SEPARATOR);
        return sb.append("]");
    }

}
