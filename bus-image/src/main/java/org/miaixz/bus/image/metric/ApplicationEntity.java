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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Option;
import org.miaixz.bus.image.galaxy.Property;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.acquire.AEExtension;
import org.miaixz.bus.image.metric.internal.pdu.*;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApplicationEntity implements Serializable {

    private final LinkedHashSet<String> acceptedCallingAETs = new LinkedHashSet<>();
    private final LinkedHashSet<String> otherAETs = new LinkedHashSet<>();
    private final LinkedHashMap<String, String> masqueradeCallingAETs = new LinkedHashMap<>();
    private final List<Connection> conns = new ArrayList<>(1);
    private final LinkedHashMap<String, TransferCapability> scuTCs = new LinkedHashMap<>();
    private final LinkedHashMap<String, TransferCapability> scpTCs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<? extends AEExtension>, AEExtension> extensions = new LinkedHashMap<>();
    /**
     * 主要设备
     */
    private Device device;
    /**
     * AET信息
     */
    private String aet;
    /**
     * 描述信息
     */
    private String description;
    /**
     * AE供应商
     */
    private byte[][] vendorData = {};
    /**
     * 应用集群
     */
    private String[] applicationClusters = {};
    /**
     * 调用者为AET
     */
    private String[] prefCalledAETs = {};
    /**
     * 被调用者为AET
     */
    private String[] prefCallingAETs = {};
    /**
     * 转换语法信息
     */
    private String[] prefTransferSyntaxes = {};
    /**
     * 网络AE支持的字符集
     */
    private String[] supportedCharacterSets = {};
    /**
     * 是否可以接受关联
     */
    private boolean acceptor = true;
    /**
     * AE是否可以发起关联
     */
    private boolean initiator = true;
    /**
     * AE是否安装在网络上
     */
    private Boolean installed;
    private Boolean roleSelectionNegotiationLenient;
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
        if (null != device) {
            if (null != this.device)
                throw new IllegalStateException("already owned by " +
                        this.device.getDeviceName());
            for (Connection conn : conns)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " +
                            device.getDeviceName());
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
        if (null != device)
            device.removeApplicationEntity(this.aet);
        this.aet = aet;
        if (null != device)
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
        this.prefTransferSyntaxes =
                Property.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETs.toArray(
                new String[acceptedCallingAETs.size()]);
    }

    public void setAcceptedCallingAETitles(String... aets) {
        acceptedCallingAETs.clear();
        for (String name : aets)
            acceptedCallingAETs.add(name);
    }

    public boolean isAcceptedCallingAETitle(String aet) {
        return acceptedCallingAETs.isEmpty()
                || acceptedCallingAETs.contains(aet);
    }

    public String[] getOtherAETitles() {
        return otherAETs.toArray(new String[otherAETs.size()]);
    }

    public void setOtherAETitles(String... aets) {
        otherAETs.clear();
        for (String name : aets)
            otherAETs.add(name);
    }

    public boolean isOtherAETitle(String aet) {
        return otherAETs.contains(aet);
    }

    public String[] getMasqueradeCallingAETitles() {
        String[] aets = new String[masqueradeCallingAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : masqueradeCallingAETs.entrySet()) {
            aets[i] = entry.getKey().equals(Symbol.STAR)
                    ? entry.getValue()
                    : Symbol.C_BRACKET_LEFT + entry.getKey() + Symbol.C_BRACKET_RIGHT + entry.getValue();
            i++;
        }
        return aets;
    }

    public void setMasqueradeCallingAETitles(String... aets) {
        masqueradeCallingAETs.clear();
        for (String aet : aets) {
            if (aet.charAt(0) == Symbol.C_BRACKET_LEFT) {
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
        if (null == callingAET) {
            callingAET = masqueradeCallingAETs.get(Symbol.STAR);
            if (null == callingAET)
                callingAET = aet;
        }
        return callingAET;
    }

    public boolean isMasqueradeCallingAETitle(String calledAET) {
        return masqueradeCallingAETs.containsKey(calledAET) || masqueradeCallingAETs.containsKey(Symbol.STAR);
    }

    /**
     * 获取网络AE支持的字符集
     * 接收的数据集,该值应从PS3.3中的“特定
     * 字符集定义的条款(0008,0005)”中选择。如果没有值
     * 则表示网络AE仅支持默认字符*曲目(ISO IR 6)
     *
     * @return 支持的字符集的String数组
     */
    public String[] getSupportedCharacterSets() {
        return supportedCharacterSets;
    }

    /**
     * 设置网络AE支持的字符集接收的数据集
     * 该值应从PS3.3中的特定字符集定义的条款(0008,0005)中选择，如果没有值
     * 则表示网络AE仅支持默认字符*曲目(ISO IR 6)
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
        return null != device && device.isInstalled()
                && (null == installed || installed.booleanValue());
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
        return null != roleSelectionNegotiationLenient
                ? roleSelectionNegotiationLenient.booleanValue()
                : null != device && device.isRoleSelectionNegotiationLenient();
    }

    public final Boolean getRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    public void setRoleSelectionNegotiationLenient(Boolean installed) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    public DimseRQHandler getDimseRQHandler() {
        DimseRQHandler handler = dimseRQHandler;
        if (null != handler)
            return handler;

        Device device = this.device;
        return null != device
                ? device.getDimseRQHandler()
                : null;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (null == device)
            throw new IllegalStateException("Not attached to Device");
    }

    void onDimseRQ(Association as, Presentation pc, Dimse cmd,
                   Attributes cmdAttrs, PDVInputStream data) throws IOException {
        DimseRQHandler tmp = getDimseRQHandler();
        if (null == tmp) {
            Logger.error("DimseRQHandler not initalized");
            throw new AAbort();
        }
        tmp.onDimse(as, pc, cmd, cmdAttrs, data);
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.DICOM)
            throw new IllegalArgumentException(
                    "protocol != DICOM - " + conn.getProtocol());


        if (null != device && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by Device: " +
                    device.getDeviceName());
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
        TransferCapability prev = (tc.getRole() == TransferCapability.Role.SCU
                ? scuTCs : scpTCs).put(tc.getSopClass(), tc);
        if (null != prev && prev != tc)
            prev.setApplicationEntity(null);
        return prev;
    }

    public TransferCapability removeTransferCapabilityFor(String sopClass,
                                                          TransferCapability.Role role) {
        TransferCapability tc = (role == TransferCapability.Role.SCU ? scuTCs : scpTCs)
                .remove(sopClass);
        if (null != tc) {
            tc.setApplicationEntity(null);
        }
        return tc;
    }

    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs =
                new ArrayList<>(scuTCs.size() + scpTCs.size());
        tcs.addAll(scpTCs.values());
        tcs.addAll(scuTCs.values());
        return tcs;
    }

    public Collection<TransferCapability> getTransferCapabilitiesWithRole(
            TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).values();
    }

    public TransferCapability getTransferCapabilityFor(
            String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).get(sopClass);
    }

    protected Presentation negotiate(AAssociateRQ rq, AAssociateAC ac,
                                     Presentation rqpc) {
        String as = rqpc.getAbstractSyntax();
        TransferCapability tc = roleSelection(rq, ac, as);
        int pcid = rqpc.getPCID();
        if (null == tc)
            return new Presentation(pcid,
                    Presentation.ABSTRACT_SYNTAX_NOT_SUPPORTED,
                    rqpc.getTransferSyntax());

        String ts = tc.selectTransferSyntax(rqpc.getTransferSyntaxes());
        if (null == ts)
            return new Presentation(pcid,
                    Presentation.TRANSFER_SYNTAX_NOT_SUPPORTED,
                    rqpc.getTransferSyntax());

        byte[] info = negotiate(rq.getExtNegotiationFor(as), tc);
        if (null != info) {
            ac.addExtendedNegotiate(new ExtendedNegotiate(as, info));
        }
        return new Presentation(pcid,
                Presentation.ACCEPTANCE, ts);
    }

    private TransferCapability roleSelection(AAssociateRQ rq,
                                             AAssociateAC ac, String asuid) {
        RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
        if (null == rqrs)
            return getTC(scpTCs, asuid, rq);

        RoleSelection acrs = ac.getRoleSelectionFor(asuid);
        if (null != acrs) {
            return getTC(acrs.isSCU() ? scpTCs : scuTCs, asuid, rq);
        }

        TransferCapability tcscu = null;
        TransferCapability tcscp = null;
        boolean scu = rqrs.isSCU()
                && null != (tcscp = getTC(scpTCs, asuid, rq));
        boolean scp = rqrs.isSCP()
                && null != (tcscu = getTC(scuTCs, asuid, rq));
        ac.addRoleSelection(new RoleSelection(asuid, scu, scp));
        return scu ? tcscp : tcscu;
    }

    private TransferCapability getTC(Map<String, TransferCapability> tcs,
                                     String asuid, AAssociateRQ rq) {
        TransferCapability tc = tcs.get(asuid);
        if (null != tc) {
            return tc;
        }

        CommonExtended commonExtNeg =
                rq.getCommonExtendedNegotiationFor(asuid);
        if (null != commonExtNeg) {
            for (String cuid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                tc = tcs.get(cuid);
                if (null != tc)
                    return tc;
            }
            tc = tcs.get(commonExtNeg.getServiceClassUID());
            if (null != tc)
                return tc;
        }

        return tcs.get(Symbol.STAR);
    }

    private byte[] negotiate(ExtendedNegotiate exneg, TransferCapability tc) {
        if (null == exneg)
            return null;

        StorageOptions storageOptions = tc.getStorageOptions();
        if (null != storageOptions)
            return storageOptions.toExtendedNegotiationInformation();

        EnumSet<Option.Type> types = tc.getTypes();
        if (null != types) {
            EnumSet<Option.Type> commonOpts = Option.Type.toOptions(exneg);
            commonOpts.retainAll(types);
            return Option.Type.toExtendedNegotiationInformation(commonOpts);
        }
        return null;
    }

    public Association connect(Connection local, Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        if (null == rq.getCallingAET())
            rq.setCallingAET(getCallingAETitle(rq.getCalledAET()));
        rq.setMaxOpsInvoked(local.getMaxOpsInvoked());
        rq.setMaxOpsPerformed(local.getMaxOpsPerformed());
        rq.setMaxPDULength(local.getReceivePDULength());
        Socket sock = local.connect(remote);
        AssociationMonitor monitor = device.getAssociationMonitor();
        Association as = null;
        try {
            as = new Association(this, local, sock);
            as.write(rq);
            as.waitForLeaving(Association.State.Sta5);
            if (null != monitor)
                monitor.onAssociationEstablished(as);
            return as;
        } catch (InterruptedException | IOException e) {
            IoKit.close(sock);
            if (null != as && null != monitor)
                monitor.onAssociationFailed(as, e);
            throw e;
        }
    }

    public Association connect(Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote, rq);
    }

    public Connection findCompatibleConnection(Connection remoteConn)
            throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException(
                "No compatible connection to " + remoteConn + " available on " + aet);
    }

    public Compatible findCompatibleConnection(ApplicationEntity remote)
            throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException(
                "No compatible connection to " + remote.getAETitle() + " available on " + aet);
    }

    public Association connect(ApplicationEntity remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        if (null == rq.getCalledAET())
            rq.setCalledAET(remote.getAETitle());
        return connect(cc.getLocalConnection(), cc.getRemoteConnection(), rq);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Property.appendLine(sb, indent, "ApplicationEntity[title: ", aet);
        Property.appendLine(sb, indent2, "desc: ", description);
        Property.appendLine(sb, indent2, "acceptor: ", acceptor);
        Property.appendLine(sb, indent2, "initiator: ", initiator);
        Property.appendLine(sb, indent2, "installed: ", getInstalled());
        for (Connection conn : conns)
            conn.promptTo(sb, indent2).append(Property.LINE_SEPARATOR);
        for (TransferCapability tc : getTransferCapabilities())
            tc.promptTo(sb, indent2).append(Property.LINE_SEPARATOR);
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
        for (Iterator<Class<? extends AEExtension>> it =
             extensions.keySet().iterator(); it.hasNext(); ) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (AEExtension src : from.extensions.values()) {
            Class<? extends AEExtension> clazz = src.getClass();
            AEExtension ext = extensions.get(clazz);
            if (null == ext)
                try {
                    addAEExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to instantiate " + clazz.getName(), e);
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
        masqueradeCallingAETs.clear();
        masqueradeCallingAETs.putAll(from.masqueradeCallingAETs);
        supportedCharacterSets = from.supportedCharacterSets;
        prefTransferSyntaxes = from.prefTransferSyntaxes;
        hl7ApplicationName = from.hl7ApplicationName;
        acceptor = from.acceptor;
        initiator = from.initiator;
        installed = from.installed;
        roleSelectionNegotiationLenient = from.roleSelectionNegotiationLenient;
    }

    public void addAEExtension(AEExtension ext) {
        Class<? extends AEExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException(
                    "already contains AE Extension:" + clazz);

        ext.setApplicationEntity(this);
        extensions.put(clazz, ext);
    }

    public boolean removeAEExtension(AEExtension ext) {
        if (null == extensions.remove(ext.getClass()))
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
        if (null == aeExt)
            throw new IllegalStateException("No " + clazz.getName()
                    + " configured for AE: " + aet);
        return aeExt;
    }

}
