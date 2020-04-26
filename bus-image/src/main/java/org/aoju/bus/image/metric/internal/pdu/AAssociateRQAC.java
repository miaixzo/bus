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
package org.aoju.bus.image.metric.internal.pdu;

import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.Capacity;
import org.aoju.bus.image.galaxy.Property;
import org.aoju.bus.image.galaxy.data.Implementation;
import org.aoju.bus.image.metric.Connection;

import java.util.*;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public abstract class AAssociateRQAC {

    protected final ArrayList<PresentationContext> pcs = new ArrayList<>();
    protected final Capacity<PresentationContext> pcidMap = new Capacity<>();
    protected final LinkedHashMap<String, RoleSelection> roleSelMap = new LinkedHashMap<>();
    protected final LinkedHashMap<String, ExtendedNegotiation> extNegMap = new LinkedHashMap<>();
    protected final LinkedHashMap<String, CommonExtended> commonExtNegMap = new LinkedHashMap<>();
    protected byte[] reservedBytes = new byte[32];
    protected int protocolVersion = 1;
    protected int maxPDULength = Connection.DEF_MAX_PDU_LENGTH;
    protected int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;
    protected int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;
    protected String calledAET;
    protected String callingAET;
    protected String applicationContext = UID.DICOMApplicationContextName;
    protected String implClassUID = Implementation.getClassUID();
    protected String implVersionName = Implementation.getVersionName();
    protected UserIdentityRQ userIdentityRQ;
    protected UserIdentityAC userIdentityAC;

    public void checkCallingAET() {
        if (callingAET == null)
            throw new IllegalStateException("Calling AET not initalized");
    }

    public void checkCalledAET() {
        if (calledAET == null)
            throw new IllegalStateException("Called AET not initalized");
    }

    public final int getProtocolVersion() {
        return protocolVersion;
    }

    public final void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public final byte[] getReservedBytes() {
        return reservedBytes.clone();
    }

    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32)
            throw new IllegalArgumentException("reservedBytes.length: "
                    + reservedBytes.length);
        System.arraycopy(reservedBytes, 0, this.reservedBytes, 0, 32);
    }

    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String calledAET) {
        if (calledAET.length() > 16)
            throw new IllegalArgumentException("calledAET: " + calledAET);
        this.calledAET = calledAET;
    }

    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        if (callingAET.length() > 16)
            throw new IllegalArgumentException("callingAET: " + callingAET);
        this.callingAET = callingAET;
    }

    public final String getApplicationContext() {
        return applicationContext;
    }

    public final void setApplicationContext(String applicationContext) {
        if (applicationContext == null)
            throw new NullPointerException();

        this.applicationContext = applicationContext;
    }

    public final int getMaxPDULength() {
        return maxPDULength;
    }

    public final void setMaxPDULength(int maxPDULength) {
        this.maxPDULength = maxPDULength;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final boolean isAsyncOps() {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    public final String getImplClassUID() {
        return implClassUID;
    }

    public final void setImplClassUID(String implClassUID) {
        if (implClassUID == null)
            throw new NullPointerException();

        this.implClassUID = implClassUID;
    }

    public final String getImplVersionName() {
        return implVersionName;
    }

    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    public final UserIdentityRQ getUserIdentityRQ() {
        return userIdentityRQ;
    }

    public void setUserIdentityRQ(UserIdentityRQ userIdentityRQ) {
        this.userIdentityRQ = userIdentityRQ;
    }

    public final UserIdentityAC getUserIdentityAC() {
        return userIdentityAC;
    }

    public void setUserIdentityAC(UserIdentityAC userIdentityAC) {
        this.userIdentityAC = userIdentityAC;
    }

    public List<PresentationContext> getPresentationContexts() {
        return Collections.unmodifiableList(pcs);
    }

    public int getNumberOfPresentationContexts() {
        return pcs.size();
    }

    public PresentationContext getPresentationContext(int pcid) {
        return pcidMap.get(pcid);
    }

    public void addPresentationContext(PresentationContext pc) {
        int pcid = pc.getPCID();
        if (pcidMap.containsKey(pcid))
            throw new IllegalStateException(
                    "Already contains Presentation Context with pid: "
                            + pcid);
        pcidMap.put(pcid, pc);
        pcs.add(pc);
    }

    public boolean removePresentationContext(PresentationContext pc) {
        if (!pcs.remove(pc))
            return false;

        pcidMap.remove(pc.getPCID());
        return true;
    }

    public Collection<RoleSelection> getRoleSelections() {
        return Collections.unmodifiableCollection(roleSelMap.values());
    }

    public RoleSelection getRoleSelectionFor(String cuid) {
        return roleSelMap.get(cuid);
    }

    public RoleSelection addRoleSelection(RoleSelection rs) {
        return roleSelMap.put(rs.getSOPClassUID(), rs);
    }

    public RoleSelection removeRoleSelectionFor(String cuid) {
        return roleSelMap.remove(cuid);
    }

    public Collection<ExtendedNegotiation> getExtendedNegotiations() {
        return Collections.unmodifiableCollection(extNegMap.values());
    }

    public ExtendedNegotiation getExtNegotiationFor(String cuid) {
        return extNegMap.get(cuid);
    }

    public ExtendedNegotiation addExtendedNegotiation(ExtendedNegotiation extNeg) {
        return extNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public ExtendedNegotiation removeExtendedNegotiationFor(String cuid) {
        return extNegMap.remove(cuid);
    }

    public Collection<CommonExtended> getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(commonExtNegMap.values());
    }

    public CommonExtended getCommonExtendedNegotiationFor(String cuid) {
        return commonExtNegMap.get(cuid);
    }

    public CommonExtended addCommonExtendedNegotiation(
            CommonExtended extNeg) {
        return commonExtNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public CommonExtended removeCommonExtendedNegotiationFor(
            String cuid) {
        return commonExtNegMap.remove(cuid);
    }

    public int length() {
        int len = 68; // Fix AA-RQ/AC PDU fields
        len += 4 + applicationContext.length();
        for (PresentationContext pc : pcs) {
            len += 4 + pc.length();
        }
        len += 4 + userInfoLength();
        return len;
    }

    public int userInfoLength() {
        int len = 8; // Max Length Sub-Item
        len += 4 + implClassUID.length();
        if (isAsyncOps())
            len += 8; // Asynchronous Operations Window Sub-Item
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
        if (userIdentityRQ != null)
            len += 4 + userIdentityRQ.length();
        if (userIdentityAC != null)
            len += 4 + userIdentityAC.length();
        return len;
    }

    protected StringBuilder promptTo(String header, StringBuilder sb) {
        sb.append(header)
                .append(Property.LINE_SEPARATOR)
                .append("  calledAET: ").append(calledAET)
                .append(Property.LINE_SEPARATOR)
                .append("  callingAET: ").append(callingAET)
                .append(Property.LINE_SEPARATOR)
                .append("  applicationContext: ");
        UID.promptTo(applicationContext, sb)
                .append(Property.LINE_SEPARATOR)
                .append("  implClassUID: ").append(implClassUID)
                .append(Property.LINE_SEPARATOR)
                .append("  implVersionName: ").append(implVersionName)
                .append(Property.LINE_SEPARATOR)
                .append("  maxPDULength: ").append(maxPDULength)
                .append(Property.LINE_SEPARATOR)
                .append("  maxOpsInvoked/maxOpsPerformed: ")
                .append(maxOpsInvoked).append("/").append(maxOpsPerformed)
                .append(Property.LINE_SEPARATOR);
        if (userIdentityRQ != null)
            userIdentityRQ.promptTo(sb).append(Property.LINE_SEPARATOR);
        if (userIdentityAC != null)
            userIdentityAC.promptTo(sb).append(Property.LINE_SEPARATOR);
        for (PresentationContext pc : pcs)
            pc.promptTo(sb).append(Property.LINE_SEPARATOR);
        for (RoleSelection rs : roleSelMap.values())
            rs.promptTo(sb).append(Property.LINE_SEPARATOR);
        for (ExtendedNegotiation extNeg : extNegMap.values())
            extNeg.promptTo(sb).append(Property.LINE_SEPARATOR);
        for (CommonExtended extNeg : commonExtNegMap.values())
            extNeg.promptTo(sb).append(Property.LINE_SEPARATOR);
        return sb.append("]");
    }

}
