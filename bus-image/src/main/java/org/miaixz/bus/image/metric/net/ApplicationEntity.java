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
 * DICOM Part 15 附录H中定义的符合DICOM网络服务规范的应用实体描述。 网络应用实体(AE)是在网络上提供服务的应用实体。无论使用何种特定网络连接， 网络AE都具有相同的功能能力。如果基于选定的网络连接存在功能差异，
 * 则这些是独立的网络AE。如果基于其他内部结构存在功能差异，则这些也是独立的网络AE。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApplicationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852269956092L;

    /**
     * 已接受的调用应用实体标题集合
     */
    private final LinkedHashSet<String> acceptedCallingAETs = new LinkedHashSet<>();

    /**
     * 其他应用实体标题集合
     */
    private final LinkedHashSet<String> otherAETs = new LinkedHashSet<>();

    /** 禁用异步模式的被调用应用实体标题集合 */
    private final LinkedHashSet<String> noAsyncModeCalledAETs = new LinkedHashSet<>();

    /**
     * 伪装调用应用实体标题映射
     */
    private final LinkedHashMap<String, String> masqueradeCallingAETs = new LinkedHashMap<>();

    /**
     * 伪装被调用应用实体标题映射
     */
    private final LinkedHashMap<String, String> masqueradeCalledAETs = new LinkedHashMap<>();

    /**
     * 网络连接列表
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * SCU（服务类用户）传输能力映射
     */
    private final LinkedHashMap<String, TransferCapability> scuTCs = new LinkedHashMap<>();

    /**
     * SCP（服务类提供者）传输能力映射
     */
    private final LinkedHashMap<String, TransferCapability> scpTCs = new LinkedHashMap<>();

    /**
     * 应用实体扩展映射
     */
    private final LinkedHashMap<Class<? extends AEExtension>, AEExtension> extensions = new LinkedHashMap<>();

    /**
     * 关联的设备
     */
    private Device device;

    /**
     * 应用实体标题
     */
    private String aet;

    /**
     * 应用实体描述
     */
    private String description;

    /**
     * 供应商数据
     */
    private byte[][] vendorData = {};

    /**
     * 应用程序集群
     */
    private String[] applicationClusters = {};

    /**
     * 首选被调用应用实体标题
     */
    private String[] prefCalledAETs = {};

    /**
     * 首选调用应用实体标题
     */
    private String[] prefCallingAETs = {};

    /**
     * 首选传输语法
     */
    private String[] prefTransferSyntaxes = {};

    /**
     * 支持的字符集
     */
    private String[] supportedCharacterSets = {};

    /**
     * 是否作为关联接受者
     */
    private boolean acceptor = true;

    /**
     * 是否作为关联发起者
     */
    private boolean initiator = true;

    /**
     * 是否已安装
     */
    private Boolean installed;

    /**
     * 角色选择协商是否宽松
     */
    private Boolean roleSelectionNegotiationLenient;

    /**
     * 共享传输能力的应用实体标题
     */
    private String shareTransferCapabilitiesFromAETitle;

    /**
     * HL7应用程序名称
     */
    private String hl7ApplicationName;

    /**
     * DIMSE请求处理器
     */
    private transient DimseRQHandler dimseRQHandler;

    /**
     * 构造一个空的应用实体
     */
    public ApplicationEntity() {

    }

    /**
     * 使用指定的应用实体标题构造应用实体
     *
     * @param aeTitle 应用实体标题
     */
    public ApplicationEntity(String aeTitle) {
        setAETitle(aeTitle);
    }

    /**
     * 获取此应用实体标识的设备
     *
     * @return 关联的设备
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * 设置此应用实体标识的设备
     *
     * @param device 要关联的设备
     * @throws IllegalStateException 如果应用实体已被其他设备拥有或连接不属于指定设备
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
     * 获取此网络AE的应用实体标题
     *
     * @return 包含应用实体标题的字符串
     */
    public final String getAETitle() {
        return aet;
    }

    /**
     * 设置此网络AE的应用实体标题
     *
     * @param aet 包含应用实体标题的字符串
     * @throws IllegalArgumentException 如果应用实体标题为空
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
     * @return 供应商数据
     */
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * 设置任何特定于此网络AE的供应商信息或配置
     *
     * @param vendorData 供应商数据
     */
    public final void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * 获取相关应用程序子集的本地定义名称（例如神经放射学）
     *
     * @return 包含名称的字符串数组
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    /**
     * 设置相关应用程序子集的本地定义名称
     *
     * @param clusters 应用程序集群名称
     */
    public void setApplicationClusters(String... clusters) {
        applicationClusters = clusters;
    }

    /**
     * 获取从此网络AE发起关联所需的AE标题
     *
     * @return 首选被调用AE标题的字符串数组
     */
    public String[] getPreferredCalledAETitles() {
        return prefCalledAETs;
    }

    /**
     * 设置从此网络AE发起关联所需的AE标题
     *
     * @param aets 首选被调用AE标题数组
     */
    public void setPreferredCalledAETitles(String... aets) {
        prefCalledAETs = aets;
    }

    /**
     * 获取通过此网络AE接受关联的首选AE标题
     *
     * @return 包含首选调用AE标题的字符串数组
     */
    public String[] getPreferredCallingAETitles() {
        return prefCallingAETs;
    }

    /**
     * 设置通过此网络AE接受关联的首选AE标题
     *
     * @param aets 首选调用AE标题数组
     */
    public void setPreferredCallingAETitles(String... aets) {
        prefCallingAETs = aets;
    }

    /**
     * 获取首选传输语法
     *
     * @return 首选传输语法数组
     */
    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    /**
     * 设置首选传输语法
     *
     * @param transferSyntaxes 传输语法数组
     * @throws IllegalArgumentException 如果传输语法为空
     */
    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes = Builder.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    /**
     * 获取接受的调用应用实体标题
     *
     * @return 接受的调用应用实体标题数组
     */
    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETs.toArray(new String[acceptedCallingAETs.size()]);
    }

    /**
     * 设置接受的调用应用实体标题
     *
     * @param aets 接受的调用应用实体标题数组
     */
    public void setAcceptedCallingAETitles(String... aets) {
        acceptedCallingAETs.clear();
        Collections.addAll(acceptedCallingAETs, aets);
    }

    /**
     * 检查指定的应用实体标题是否被接受
     *
     * @param aet 要检查的应用实体标题
     * @return 如果接受则返回true，否则返回false
     */
    public boolean isAcceptedCallingAETitle(String aet) {
        return acceptedCallingAETs.isEmpty() || acceptedCallingAETs.contains(aet);
    }

    /**
     * 获取其他应用实体标题
     *
     * @return 其他应用实体标题数组
     */
    public String[] getOtherAETitles() {
        return otherAETs.toArray(new String[otherAETs.size()]);
    }

    /**
     * 设置其他应用实体标题
     *
     * @param aets 其他应用实体标题数组
     */
    public void setOtherAETitles(String... aets) {
        otherAETs.clear();
        Collections.addAll(otherAETs, aets);
    }

    /**
     * 检查指定的应用实体标题是否在其他应用实体标题列表中
     *
     * @param aet 要检查的应用实体标题
     * @return 如果存在则返回true，否则返回false
     */
    public boolean isOtherAETitle(String aet) {
        return otherAETs.contains(aet);
    }

    /**
     * 获取禁用异步模式的被调用应用实体标题
     *
     * @return 禁用异步模式的被调用应用实体标题数组
     */
    public String[] getNoAsyncModeCalledAETitles() {
        return noAsyncModeCalledAETs.toArray(new String[noAsyncModeCalledAETs.size()]);
    }

    /**
     * 设置禁用异步模式的被调用应用实体标题
     *
     * @param aets 禁用异步模式的被调用应用实体标题数组
     */
    public void setNoAsyncModeCalledAETitles(String... aets) {
        noAsyncModeCalledAETs.clear();
        Collections.addAll(noAsyncModeCalledAETs, aets);
    }

    /**
     * 检查指定的被调用应用实体标题是否禁用异步模式
     *
     * @param calledAET 被调用应用实体标题
     * @return 如果禁用则返回true，否则返回false
     */
    public boolean isNoAsyncModeCalledAETitle(String calledAET) {
        return noAsyncModeCalledAETs.contains(calledAET);
    }

    /**
     * 获取伪装调用应用实体标题
     *
     * @return 伪装调用应用实体标题数组
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
     * 设置伪装调用应用实体标题
     *
     * @param aets 伪装调用应用实体标题数组
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
     * 获取伪装被调用应用实体标题
     *
     * @return 伪装被调用应用实体标题数组
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
     * 设置伪装被调用应用实体标题
     *
     * @param aets 伪装被调用应用实体标题数组
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
     * 获取指定被调用应用实体标题对应的调用应用实体标题
     *
     * @param calledAET 被调用应用实体标题
     * @return 调用应用实体标题
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
     * 检查是否存在指定被调用应用实体标题的伪装调用应用实体标题
     *
     * @param calledAET 被调用应用实体标题
     * @return 如果存在则返回true，否则返回false
     */
    public boolean isMasqueradeCallingAETitle(String calledAET) {
        return masqueradeCallingAETs.containsKey(calledAET) || masqueradeCallingAETs.containsKey("*");
    }

    /**
     * 获取指定被调用应用实体标题的伪装被调用应用实体标题
     *
     * @param calledAET 被调用应用实体标题
     * @return 伪装被调用应用实体标题
     */
    public String masqueradeCalledAETitle(String calledAET) {
        return masqueradeCalledAETs.getOrDefault(calledAET, calledAET);
    }

    /**
     * 获取网络AE支持的字符集（用于接收的数据集） 该值应从PS3.3中的"特定字符集定义"条款(0008,0005)中选择。 如果没有值，表示网络AE仅支持默认字符集(ISO IR 6)
     *
     * @return 支持的字符集数组
     */
    public String[] getSupportedCharacterSets() {
        return supportedCharacterSets;
    }

    /**
     * 设置网络AE支持的字符集（用于接收的数据集） 该值应从PS3.3中的"特定字符集定义"条款(0008,0005)中选择。 如果没有值，表示网络AE仅支持默认字符集(ISO IR 6)
     *
     * @param characterSets 支持的字符集数组
     */
    public void setSupportedCharacterSets(String... characterSets) {
        supportedCharacterSets = characterSets;
    }

    /**
     * 确定此网络AE是否可以接受关联
     *
     * @return 如果网络AE可以接受关联则返回true，否则返回false
     */
    public final boolean isAssociationAcceptor() {
        return acceptor;
    }

    /**
     * 设置此网络AE是否可以接受关联
     *
     * @param acceptor 如果网络AE可以接受关联则设为true，否则设为false
     */
    public final void setAssociationAcceptor(boolean acceptor) {
        this.acceptor = acceptor;
    }

    /**
     * 确定此网络AE是否可以发起关联
     *
     * @return 如果网络AE可以发起关联则返回true，否则返回false
     */
    public final boolean isAssociationInitiator() {
        return initiator;
    }

    /**
     * 设置此网络AE是否可以发起关联
     *
     * @param initiator 如果网络AE可以发起关联则设为true，否则设为false
     */
    public final void setAssociationInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    /**
     * 确定此网络AE是否已安装到网络上
     *
     * @return 布尔值。如果AE已安装到网络上则返回true， 如果不存在，则从设备继承有关AE安装状态的信息
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    /**
     * 获取此网络AE的安装状态
     *
     * @return 安装状态（true表示已安装，false表示未安装，null表示继承设备状态）
     */
    public final Boolean getInstalled() {
        return installed;
    }

    /**
     * 设置此网络AE是否已安装到网络上
     *
     * @param installed 如果AE已安装到网络上则设为true， 如果不存在，则AE的安装状态信息将从设备继承
     */
    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    /**
     * 检查角色选择协商是否宽松
     *
     * @return 如果角色选择协商宽松则返回true，否则返回false
     */
    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient != null ? roleSelectionNegotiationLenient.booleanValue()
                : device != null && device.isRoleSelectionNegotiationLenient();
    }

    /**
     * 获取角色选择协商是否宽松的设置
     *
     * @return 角色选择协商是否宽松的设置（true表示宽松，false表示严格，null表示继承设备设置）
     */
    public final Boolean getRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    /**
     * 设置角色选择协商是否宽松
     *
     * @param roleSelectionNegotiationLenient 角色选择协商是否宽松的设置
     */
    public void setRoleSelectionNegotiationLenient(Boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    /**
     * 获取共享传输能力的应用实体标题
     *
     * @return 共享传输能力的应用实体标题
     */
    public String getShareTransferCapabilitiesFromAETitle() {
        return shareTransferCapabilitiesFromAETitle;
    }

    /**
     * 设置共享传输能力的应用实体标题
     *
     * @param shareTransferCapabilitiesFromAETitle 共享传输能力的应用实体标题
     */
    public void setShareTransferCapabilitiesFromAETitle(String shareTransferCapabilitiesFromAETitle) {
        this.shareTransferCapabilitiesFromAETitle = shareTransferCapabilitiesFromAETitle;
    }

    /**
     * 获取共享传输能力的应用实体
     *
     * @return 共享传输能力的应用实体
     */
    public ApplicationEntity transferCapabilitiesAE() {
        return shareTransferCapabilitiesFromAETitle != null
                ? device.getApplicationEntity(shareTransferCapabilitiesFromAETitle)
                : this;
    }

    /**
     * 获取HL7应用程序名称
     *
     * @return HL7应用程序名称
     */
    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    /**
     * 设置HL7应用程序名称
     *
     * @param hl7ApplicationName HL7应用程序名称
     */
    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    /**
     * 获取DIMSE请求处理器
     *
     * @return DIMSE请求处理器
     */
    public DimseRQHandler getDimseRQHandler() {
        DimseRQHandler handler = dimseRQHandler;
        if (handler != null)
            return handler;
        Device device = this.device;
        return device != null ? device.getDimseRQHandler() : null;
    }

    /**
     * 设置DIMSE请求处理器
     *
     * @param dimseRQHandler DIMSE请求处理器
     */
    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    /**
     * 检查应用实体是否已安装
     *
     * @throws IllegalStateException 如果应用实体未安装
     */
    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    /**
     * 检查应用实体是否已关联到设备
     *
     * @throws IllegalStateException 如果应用实体未关联到设备
     */
    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    /**
     * 处理DIMSE请求
     *
     * @param as       关联
     * @param pc       表示上下文
     * @param cmd      DIMSE命令
     * @param cmdAttrs 命令属性
     * @param data     数据流
     * @throws IOException 如果发生I/O错误
     */
    public void onDimseRQ(Association as, PresentationContext pc, Dimse cmd, Attributes cmdAttrs, PDVInputStream data)
            throws IOException {
        DimseRQHandler tmp = getDimseRQHandler();
        if (tmp == null) {
            Logger.error("DimseRQHandler not initalized");
            throw new AAbort();
        }
        tmp.onDimseRQ(as, pc, cmd, cmdAttrs, data);
    }

    /**
     * 添加网络连接到此应用实体
     *
     * @param conn 要添加的网络连接
     * @throws IllegalArgumentException 如果连接协议不是DICOM
     * @throws IllegalStateException    如果连接不属于指定设备
     */
    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.DICOM)
            throw new IllegalArgumentException("protocol != DICOM - " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by Device: " + device.getDeviceName());
        conns.add(conn);
    }

    /**
     * 从此应用实体移除网络连接
     *
     * @param conn 要移除的网络连接
     * @return 如果连接被移除则返回true，否则返回false
     */
    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    /**
     * 获取此应用实体的所有网络连接
     *
     * @return 网络连接列表
     */
    public List<Connection> getConnections() {
        return conns;
    }

    /**
     * 添加传输能力到此应用实体
     *
     * @param tc 要添加的传输能力
     * @return 之前具有相同SOP类和角色的传输能力，如果没有则返回null
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
     * 从此应用实体移除指定SOP类和角色的传输能力
     *
     * @param sopClass SOP类
     * @param role     角色（SCU或SCP）
     * @return 被移除的传输能力，如果没有则返回null
     */
    public TransferCapability removeTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        TransferCapability tc = (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).remove(sopClass);
        if (tc != null)
            tc.setApplicationEntity(null);
        return tc;
    }

    /**
     * 获取此应用实体的所有传输能力
     *
     * @return 传输能力集合
     */
    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs = new ArrayList<>(scuTCs.size() + scpTCs.size());
        tcs.addAll(scpTCs.values());
        tcs.addAll(scuTCs.values());
        return tcs;
    }

    /**
     * 获取具有指定角色的传输能力
     *
     * @param role（SCU或SCP）
     * @return 具有指定角色的传输能力集合
     */
    public Collection<TransferCapability> getTransferCapabilitiesWithRole(TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).values();
    }

    /**
     * 获取指定SOP类和角色的传输能力
     *
     * @param sopClass SOP类
     * @param role     角色（SCU或SCP）
     * @return 匹配的传输能力，如果没有则返回null
     */
    public TransferCapability getTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).get(sopClass);
    }

    /**
     * 检查是否存在指定SOP类和角色的传输能力
     *
     * @param sopClass SOP类
     * @param role     角色（SCU或SCP）
     * @return 如果存在则返回true，否则返回false
     */
    public boolean hasTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).containsKey(sopClass);
    }

    /**
     * 协商表示上下文
     *
     * @param rq   A-ASSOCIATE-RQ PDU
     * @param ac   A-ASSOCIATE-AC PDU
     * @param rqpc 请求的表示上下文
     * @return 协商后的表示上下文
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
     * 执行角色选择
     *
     * @param rq    A-ASSOCIATE-RQ PDU
     * @param ac    A-ASSOCIATE-AC PDU
     * @param asuid 抽象语法UID
     * @return 选择的传输能力
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
     * 获取传输能力
     *
     * @param tcs   传输能力映射
     * @param asuid 抽象语法UID
     * @param rq    A-ASSOCIATE-RQ PDU
     * @return 匹配的传输能力，如果没有则返回null
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
     * 协商扩展协商信息
     *
     * @param exneg 扩展协商
     * @param tc    传输能力
     * @return 协商后的信息，如果没有则返回null
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
     * 连接到远程应用实体
     *
     * @param local  本地连接
     * @param remote 远程连接
     * @param rq     A-ASSOCIATE-RQ PDU
     * @return 建立的关联
     * @throws IOException              如果发生I/O错误
     * @throws InterruptedException     如果线程被中断
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
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
     * 使用指定的远程连接连接到远程应用实体
     *
     * @param remote 远程连接
     * @param rq     A-ASSOCIATE-RQ PDU
     * @return 建立的关联
     * @throws IOException              如果发生I/O错误
     * @throws InterruptedException     如果线程被中断
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public Association connect(Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote, rq);
    }

    /**
     * 查找与指定远程连接兼容的本地连接
     *
     * @param remoteConn 远程连接
     * @return 兼容的本地连接
     * @throws InternalException 如果没有可用的兼容连接
     */
    public Connection findCompatibleConnection(Connection remoteConn) throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException("No compatible connection to " + remoteConn + " available on " + aet);
    }

    /**
     * 查找与指定远程应用实体兼容的连接
     *
     * @param remote 远程应用实体
     * @return 兼容的连接对（本地连接和远程连接）
     * @throws InternalException 如果没有可用的兼容连接
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
     * 连接到远程应用实体
     *
     * @param remote 远程应用实体
     * @param rq     A-ASSOCIATE-RQ PDU
     * @return 建立的关联
     * @throws IOException              如果发生I/O错误
     * @throws InterruptedException     如果线程被中断
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public Association connect(ApplicationEntity remote, AAssociateRQ rq)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        if (rq.getCalledAET() == null)
            rq.setCalledAET(masqueradeCalledAETitle(remote.getAETitle()));
        return connect(cc.getLocalConnection(), cc.getRemoteConnection(), rq);
    }

    /**
     * 返回此应用实体的字符串表示
     *
     * @return 应用实体的字符串表示
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    /**
     * 将此应用实体的提示信息追加到指定的字符串构建器
     *
     * @param sb     字符串构建器
     * @param indent 缩进字符串
     * @return 追加后的字符串构建器
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
     * 从指定的源应用实体重新配置此应用实体
     *
     * @param src 源应用实体
     */
    public void reconfigure(ApplicationEntity src) {
        setApplicationEntityAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureTransferCapabilities(src);
        reconfigureAEExtensions(src);
    }

    /**
     * 从指定的源应用实体重新配置传输能力
     *
     * @param src 源应用实体
     */
    private void reconfigureTransferCapabilities(ApplicationEntity src) {
        scuTCs.clear();
        scuTCs.putAll(src.scuTCs);
        scpTCs.clear();
        scpTCs.putAll(src.scpTCs);
    }

    /**
     * 从指定的源应用实体重新配置应用实体扩展
     *
     * @param from 源应用实体
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
     * 从指定的源应用实体设置应用实体属性
     *
     * @param from 源应用实体
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
     * 添加应用实体扩展
     *
     * @param ext 要添加的应用实体扩展
     * @throws IllegalStateException 如果已存在相同类型的应用实体扩展
     */
    public void addAEExtension(AEExtension ext) {
        Class<? extends AEExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains AE Extension:" + clazz);
        ext.setApplicationEntity(this);
        extensions.put(clazz, ext);
    }

    /**
     * 移除应用实体扩展
     *
     * @param ext 要移除的应用实体扩展
     * @return 如果扩展被移除则返回true，否则返回false
     */
    public boolean removeAEExtension(AEExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;
        ext.setApplicationEntity(null);
        return true;
    }

    /**
     * 列出所有应用实体扩展
     *
     * @return 应用实体扩展集合
     */
    public Collection<AEExtension> listAEExtensions() {
        return extensions.values();
    }

    /**
     * 获取指定类型的应用实体扩展
     *
     * @param <T>   扩展类型
     * @param clazz 扩展类
     * @return 匹配的应用实体扩展，如果没有则返回null
     */
    public <T extends AEExtension> T getAEExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    /**
     * 获取指定类型的应用实体扩展，如果不存在则抛出异常
     *
     * @param <T>   扩展类型
     * @param clazz 扩展类
     * @return 匹配的应用实体扩展
     * @throws IllegalStateException 如果不存在指定类型的扩展
     */
    public <T extends AEExtension> T getAEExtensionNotNull(Class<T> clazz) {
        T aeExt = getAEExtension(clazz);
        if (aeExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for AE: " + aet);
        return aeExt;
    }

}
