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
package org.miaixz.bus.image.metric.net;

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

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * DICOM Part 15, Annex H compliant description of a DICOM network service. A Network AE is an application entity that
 * provides services on a network. A Network AE will have the 16 same functional capability regardless of the particular
 * network connection used. If there are functional differences based on selected network connection, then these are
 * separate Network AEs. If there are 18 functional differences based on other internal structures, then these are
 * separate Network AEs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApplicationEntity implements Serializable {

    private static final long serialVersionUID = -1L;
    private final LinkedHashSet<String> acceptedCallingAETs = new LinkedHashSet<>();
    private final LinkedHashSet<String> otherAETs = new LinkedHashSet<>();
    private final LinkedHashSet<String> noAsyncModeCalledAETs = new LinkedHashSet<>();
    private final LinkedHashMap<String, String> masqueradeCallingAETs = new LinkedHashMap<>();
    private final List<Connection> conns = new ArrayList<>(1);
    private final LinkedHashMap<String, TransferCapability> scuTCs = new LinkedHashMap<>();
    private final LinkedHashMap<String, TransferCapability> scpTCs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<? extends AEExtension>, AEExtension> extensions = new LinkedHashMap<>();
    private Device device;
    private String aet;
    private String description;
    private byte[][] vendorData = {};
    private String[] applicationClusters = {};
    private String[] prefCalledAETs = {};
    private String[] prefCallingAETs = {};
    private String[] prefTransferSyntaxes = {};
    private String[] supportedCharacterSets = {};
    private boolean acceptor = true;
    private boolean initiator = true;
    private Boolean installed;
    private Boolean roleSelectionNegotiationLenient;
    private String shareTransferCapabilitiesFromAETitle;
    private String hl7ApplicationName;
    private transient DimseRQHandler dimseRQHandler;

    public ApplicationEntity() {
    }

    public ApplicationEntity(String aeTitle) {
        setAETitle(aeTitle);
    }

    /**
     * 获取此应用程序实体标识的设备
     *
     * @return 主要设备
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * 设置此应用程序实体标识的设备.
     *
     * @param device 主要设备.
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
     * 获取此网络AE的AET
     *
     * @return 包含AE标题的字符串.
     */
    public final String getAETitle() {
        return aet;
    }

    /**
     * 设置此网络AE的AE标题
     *
     * @param aet 包含AE标题的字符串
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
     * 获取此网络AE的描述
     *
     * @return 包含描述的字符串
     */
    public final String getDescription() {
        return description;
    }

    /**
     * 设置此网络AE的描述
     *
     * @param description 包含描述的字符串
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取任何特定于此网络AE的供应商信息或配置
     *
     * @return 供应商数据的对象
     */
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * 设置任何特定于此网络AE的供应商信息或配置
     *
     * @param vendorData 供应商数据的对象
     */
    public final void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * 获取相关应用程序子集的本地定义名称。例如神经放射学
     *
     * @return 包含名称的String []
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String... clusters) {
        applicationClusters = clusters;
    }

    /**
     * 从此网络AE获取启动关联,所需的AE标题
     *
     * @return 首选AE标题的String []
     */
    public String[] getPreferredCalledAETitles() {
        return prefCalledAETs;
    }

    public void setPreferredCalledAETitles(String... aets) {
        prefCalledAETs = aets;
    }

    /**
     * 通过此网络AE获取首选的接受关联的AE标题
     *
     * @return 一个String []包含首选的调用AE标题
     */
    public String[] getPreferredCallingAETitles() {
        return prefCallingAETs;
    }

    public void setPreferredCallingAETitles(String... aets) {
        prefCallingAETs = aets;
    }

    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes = Builder.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETs.toArray(new String[acceptedCallingAETs.size()]);
    }

    public void setAcceptedCallingAETitles(String... aets) {
        acceptedCallingAETs.clear();
        Collections.addAll(acceptedCallingAETs, aets);
    }

    public boolean isAcceptedCallingAETitle(String aet) {
        return acceptedCallingAETs.isEmpty() || acceptedCallingAETs.contains(aet);
    }

    public String[] getOtherAETitles() {
        return otherAETs.toArray(new String[otherAETs.size()]);
    }

    public void setOtherAETitles(String... aets) {
        otherAETs.clear();
        Collections.addAll(otherAETs, aets);
    }

    public boolean isOtherAETitle(String aet) {
        return otherAETs.contains(aet);
    }

    public String[] getNoAsyncModeCalledAETitles() {
        return noAsyncModeCalledAETs.toArray(new String[noAsyncModeCalledAETs.size()]);
    }

    public void setNoAsyncModeCalledAETitles(String... aets) {
        noAsyncModeCalledAETs.clear();
        Collections.addAll(noAsyncModeCalledAETs, aets);
    }

    public boolean isNoAsyncModeCalledAETitle(String calledAET) {
        return noAsyncModeCalledAETs.contains(calledAET);
    }

    public String[] getMasqueradeCallingAETitles() {
        String[] aets = new String[masqueradeCallingAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : masqueradeCallingAETs.entrySet()) {
            aets[i] = entry.getKey().equals(Symbol.STAR) ? entry.getValue()
                    : Symbol.C_BRACKET_LEFT + entry.getKey() + Symbol.C_BRACKET_RIGHT + entry.getValue();
            i++;
        }
        return aets;
    }

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

    public String getCallingAETitle(String calledAET) {
        String callingAET = masqueradeCallingAETs.get(calledAET);
        if (callingAET == null) {
            callingAET = masqueradeCallingAETs.get(Symbol.STAR);
            if (callingAET == null)
                callingAET = aet;
        }
        return callingAET;
    }

    public boolean isMasqueradeCallingAETitle(String calledAET) {
        return masqueradeCallingAETs.containsKey(calledAET) || masqueradeCallingAETs.containsKey("*");
    }

    /**
     * 获取网络AE支持的字符集 接收的数据集,该值应从PS3.3中的“特定 字符集定义的条款(0008,0005)”中选择。如果没有值 则表示网络AE仅支持默认字符*曲目(ISO IR 6)
     *
     * @return 支持的字符集的String数组
     */
    public String[] getSupportedCharacterSets() {
        return supportedCharacterSets;
    }

    /**
     * 设置网络AE支持的字符集接收的数据集 该值应从PS3.3中的特定字符集定义的条款(0008,0005)中选择，如果没有值 则表示网络AE仅支持默认字符*曲目(ISO IR 6)
     *
     * @param characterSets 支持的字符集的String数组
     */
    public void setSupportedCharacterSets(String... characterSets) {
        supportedCharacterSets = characterSets;
    }

    /**
     * 确定此网络AE是否可以接受关联
     *
     * @return 如果网络AE可以接受关联，则为true，否则为false
     */
    public final boolean isAssociationAcceptor() {
        return acceptor;
    }

    /**
     * 设置此网络AE是否可以接受关联
     *
     * @param acceptor 如果网络AE可以接受*关联，则为true，否则为false
     */
    public final void setAssociationAcceptor(boolean acceptor) {
        this.acceptor = acceptor;
    }

    /**
     * 确定此网络AE是否可以发起关联
     *
     * @return 如果网络AE可以接受关联，则为true，否则为false
     */
    public final boolean isAssociationInitiator() {
        return initiator;
    }

    /**
     * 设置此网络AE是否可以发起关联
     *
     * @param initiator 如果网络AE可以接受关联，则为true，否则为false
     */
    public final void setAssociationInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    /**
     * 确定此网络AE是否安装在网络上
     *
     * @return 布尔值。如果AE安装在网络上，则为True,如果不存在*，则从设备继承有关AE安装状态的信息
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    public final Boolean getInstalled() {
        return installed;
    }

    /**
     * 设置此网络AE是否安装在网络上
     *
     * @param installed 如果AE安装在网络上，则为True,如果不存在，则AE的安装状态信息将从设备继承
     */
    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient != null ? roleSelectionNegotiationLenient.booleanValue()
                : device != null && device.isRoleSelectionNegotiationLenient();
    }

    public final Boolean getRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    public void setRoleSelectionNegotiationLenient(Boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    public String getShareTransferCapabilitiesFromAETitle() {
        return shareTransferCapabilitiesFromAETitle;
    }

    public void setShareTransferCapabilitiesFromAETitle(String shareTransferCapabilitiesFromAETitle) {
        this.shareTransferCapabilitiesFromAETitle = shareTransferCapabilitiesFromAETitle;
    }

    public ApplicationEntity transferCapabilitiesAE() {
        return shareTransferCapabilitiesFromAETitle != null
                ? device.getApplicationEntity(shareTransferCapabilitiesFromAETitle)
                : this;
    }

    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    public DimseRQHandler getDimseRQHandler() {
        DimseRQHandler handler = dimseRQHandler;
        if (handler != null)
            return handler;

        Device device = this.device;
        return device != null ? device.getDimseRQHandler() : null;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    public void onDimseRQ(Association as, PresentationContext pc, Dimse cmd, Attributes cmdAttrs, PDVInputStream data)
            throws IOException {
        DimseRQHandler tmp = getDimseRQHandler();
        if (tmp == null) {
            Logger.error("DimseRQHandler not initalized");
            throw new AAbort();
        }
        tmp.onDimseRQ(as, pc, cmd, cmdAttrs, data);
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.DICOM)
            throw new IllegalArgumentException("protocol != DICOM - " + conn.getProtocol());

        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by Device: " + device.getDeviceName());
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public TransferCapability addTransferCapability(TransferCapability tc) {
        tc.setApplicationEntity(this);
        TransferCapability prev = (tc.getRole() == TransferCapability.Role.SCU ? scuTCs : scpTCs).put(tc.getSopClass(),
                tc);
        if (prev != null && prev != tc)
            prev.setApplicationEntity(null);
        return prev;
    }

    public TransferCapability removeTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        TransferCapability tc = (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).remove(sopClass);
        if (tc != null)
            tc.setApplicationEntity(null);
        return tc;
    }

    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs = new ArrayList<>(scuTCs.size() + scpTCs.size());
        tcs.addAll(scpTCs.values());
        tcs.addAll(scuTCs.values());
        return tcs;
    }

    public Collection<TransferCapability> getTransferCapabilitiesWithRole(TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).values();
    }

    public TransferCapability getTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).get(sopClass);
    }

    public boolean hasTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).containsKey(sopClass);
    }

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

    public Association connect(Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote, rq);
    }

    public Connection findCompatibleConnection(Connection remoteConn) throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException("No compatible connection to " + remoteConn + " available on " + aet);
    }

    public Compatible findCompatibleConnection(ApplicationEntity remote) throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException("No compatible connection to " + remote.getAETitle() + " available on " + aet);
    }

    public Association connect(ApplicationEntity remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        if (rq.getCalledAET() == null)
            rq.setCalledAET(remote.getAETitle());
        return connect(cc.getLocalConnection(), cc.getRemoteConnection(), rq);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

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

    public void reconfigure(ApplicationEntity src) {
        setApplicationEntityAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureTransferCapabilities(src);
        reconfigureAEExtensions(src);
    }

    private void reconfigureTransferCapabilities(ApplicationEntity src) {
        scuTCs.clear();
        scuTCs.putAll(src.scuTCs);
        scpTCs.clear();
        scpTCs.putAll(src.scpTCs);
    }

    private void reconfigureAEExtensions(ApplicationEntity from) {
        for (Iterator<Class<? extends AEExtension>> it = extensions.keySet().iterator(); it.hasNext();) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
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

    public void addAEExtension(AEExtension ext) {
        Class<? extends AEExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains AE Extension:" + clazz);

        ext.setApplicationEntity(this);
        extensions.put(clazz, ext);
    }

    public boolean removeAEExtension(AEExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;

        ext.setApplicationEntity(null);
        return true;
    }

    public Collection<AEExtension> listAEExtensions() {
        return extensions.values();
    }

    public <T extends AEExtension> T getAEExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    public <T extends AEExtension> T getAEExtensionNotNull(Class<T> clazz) {
        T aeExt = getAEExtension(clazz);
        if (aeExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for AE: " + aet);
        return aeExt;
    }

}
