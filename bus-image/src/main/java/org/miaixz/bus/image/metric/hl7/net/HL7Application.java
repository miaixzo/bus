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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Compatible;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.ERRSegment;
import org.miaixz.bus.image.metric.hl7.HL7Exception;
import org.miaixz.bus.image.metric.hl7.HL7Segment;
import org.miaixz.bus.image.metric.hl7.MLLPConnection;

/**
 * HL7应用程序类，用于处理HL7消息通信。 该类提供了HL7消息的发送、接收和处理功能，包括连接管理、消息验证、字符集设置等。 它实现了Serializable接口，支持序列化操作。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Application implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852267135868L;

    /**
     * 已接受的发送应用程序集合
     */
    private final LinkedHashSet<String> acceptedSendingApplications = new LinkedHashSet<>();

    /**
     * 其他应用程序名称集合
     */
    private final LinkedHashSet<String> otherApplicationNames = new LinkedHashSet<>();

    /**
     * 已接受的消息类型集合
     */
    private final LinkedHashSet<String> acceptedMessageTypes = new LinkedHashSet<>();

    /**
     * 网络连接列表
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * HL7应用程序扩展映射
     */
    private final Map<Class<? extends HL7ApplicationExtension>, HL7ApplicationExtension> extensions = new HashMap<>();

    /**
     * 关联的设备
     */
    private Device device;

    /**
     * 应用程序名称
     */
    private String name;

    /**
     * HL7默认字符集
     */
    private String hl7DefaultCharacterSet = "ASCII";

    /**
     * HL7发送字符集
     */
    private String hl7SendingCharacterSet = "ASCII";

    /**
     * 是否已安装标志
     */
    private Boolean installed;

    /**
     * 应用程序描述
     */
    private String description;

    /**
     * 可选MSH字段数组
     */
    private int[] optionalMSHFields = {};

    /**
     * 应用程序集群数组
     */
    private String[] applicationClusters = {};

    /**
     * HL7消息监听器
     */
    private transient HL7MessageListener hl7MessageListener;

    /**
     * 构造一个空的HL7应用程序
     */
    public HL7Application() {

    }

    /**
     * 使用指定的名称构造HL7应用程序
     *
     * @param name 应用程序名称
     */
    public HL7Application(String name) {
        setApplicationName(name);
    }

    /**
     * 获取此HL7应用程序关联的设备
     *
     * @return 关联的设备
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * 设置此HL7应用程序关联的设备
     *
     * @param device 要关联的设备
     * @throws IllegalStateException 如果应用程序已被其他设备拥有或连接不属于指定设备
     */
    void setDevice(Device device) {
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
     * 获取应用程序名称
     *
     * @return 应用程序名称
     */
    public String getApplicationName() {
        return name;
    }

    /**
     * 设置应用程序名称
     *
     * @param name 应用程序名称
     * @throws IllegalArgumentException 如果名称为空
     */
    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        HL7DeviceExtension ext = device != null ? device.getDeviceExtension(HL7DeviceExtension.class) : null;
        if (ext != null)
            ext.removeHL7Application(this.name);
        this.name = name;
        if (ext != null)
            ext.addHL7Application(this);
    }

    /**
     * 获取HL7默认字符集
     *
     * @return HL7默认字符集
     */
    public final String getHL7DefaultCharacterSet() {
        return hl7DefaultCharacterSet;
    }

    /**
     * 设置HL7默认字符集
     *
     * @param hl7DefaultCharacterSet HL7默认字符集
     */
    public final void setHL7DefaultCharacterSet(String hl7DefaultCharacterSet) {
        this.hl7DefaultCharacterSet = hl7DefaultCharacterSet;
    }

    /**
     * 获取HL7发送字符集
     *
     * @return HL7发送字符集
     */
    public String getHL7SendingCharacterSet() {
        return hl7SendingCharacterSet;
    }

    /**
     * 设置HL7发送字符集
     *
     * @param hl7SendingCharacterSet HL7发送字符集
     */
    public void setHL7SendingCharacterSet(String hl7SendingCharacterSet) {
        this.hl7SendingCharacterSet = hl7SendingCharacterSet;
    }

    /**
     * 获取已接受的发送应用程序数组
     *
     * @return 已接受的发送应用程序数组
     */
    public String[] getAcceptedSendingApplications() {
        return acceptedSendingApplications.toArray(new String[acceptedSendingApplications.size()]);
    }

    /**
     * 设置已接受的发送应用程序
     *
     * @param names 发送应用程序名称数组
     */
    public void setAcceptedSendingApplications(String... names) {
        acceptedSendingApplications.clear();
        Collections.addAll(acceptedSendingApplications, names);
    }

    /**
     * 获取其他应用程序名称数组
     *
     * @return 其他应用程序名称数组
     */
    public String[] getOtherApplicationNames() {
        return otherApplicationNames.toArray(new String[otherApplicationNames.size()]);
    }

    /**
     * 设置其他应用程序名称
     *
     * @param names 其他应用程序名称数组
     */
    public void setOtherApplicationNames(String... names) {
        otherApplicationNames.clear();
        Collections.addAll(otherApplicationNames, names);
    }

    /**
     * 检查指定的名称是否在其他应用程序名称列表中
     *
     * @param name 要检查的名称
     * @return 如果存在则返回true，否则返回false
     */
    public boolean isOtherApplicationName(String name) {
        return otherApplicationNames.contains(name);
    }

    /**
     * 获取已接受的消息类型数组
     *
     * @return 已接受的消息类型数组
     */
    public String[] getAcceptedMessageTypes() {
        return acceptedMessageTypes.toArray(new String[acceptedMessageTypes.size()]);
    }

    /**
     * 设置已接受的消息类型
     *
     * @param types 消息类型数组
     */
    public void setAcceptedMessageTypes(String... types) {
        acceptedMessageTypes.clear();
        Collections.addAll(acceptedMessageTypes, types);
    }

    /**
     * 获取应用程序描述
     *
     * @return 应用程序描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置应用程序描述
     *
     * @param description 应用程序描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取可选MSH字段数组
     *
     * @return 可选MSH字段数组
     */
    public int[] getOptionalMSHFields() {
        return optionalMSHFields;
    }

    /**
     * 设置可选MSH字段
     *
     * @param optionalMSHFields 可选MSH字段数组
     */
    public void setOptionalMSHFields(int... optionalMSHFields) {
        this.optionalMSHFields = optionalMSHFields;
    }

    /**
     * 获取应用程序集群数组
     *
     * @return 应用程序集群数组
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    /**
     * 设置应用程序集群
     *
     * @param applicationClusters 应用程序集群数组
     */
    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    /**
     * 检查应用程序是否已安装
     *
     * @return 如果应用程序已安装则返回true，否则返回false
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    /**
     * 获取安装状态
     *
     * @return 安装状态（true表示已安装，false表示未安装，null表示继承设备状态）
     */
    public final Boolean getInstalled() {
        return installed;
    }

    /**
     * 设置安装状态
     *
     * @param installed 安装状态
     * @throws IllegalStateException 如果设置为已安装但所属设备未安装
     */
    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue() && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    /**
     * 获取HL7消息监听器
     *
     * @return HL7消息监听器
     */
    public HL7MessageListener getHL7MessageListener() {
        HL7MessageListener listener = hl7MessageListener;
        if (listener != null)
            return listener;
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext != null ? hl7Ext.getHL7MessageListener() : null;
    }

    /**
     * 设置HL7消息监听器
     *
     * @param listener HL7消息监听器
     */
    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    /**
     * 添加网络连接到此HL7应用程序
     *
     * @param conn 要添加的网络连接
     * @throws IllegalArgumentException 如果连接协议不是HL7
     * @throws IllegalStateException    如果连接不属于指定设备
     */
    public void addConnection(Connection conn) {
        if (!conn.getProtocol().isHL7())
            throw new IllegalArgumentException("protocol != HL7 - " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + device.getDeviceName());
        conns.add(conn);
    }

    /**
     * 从此HL7应用程序移除网络连接
     *
     * @param conn 要移除的网络连接
     * @return 如果连接被移除则返回true，否则返回false
     */
    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    /**
     * 获取此HL7应用程序的所有网络连接
     *
     * @return 网络连接列表
     */
    public List<Connection> getConnections() {
        return conns;
    }

    /**
     * 处理接收到的HL7消息
     *
     * @param conn 网络连接
     * @param s    套接字
     * @param msg  未解析的HL7消息
     * @return 处理后的未解析HL7消息
     * @throws HL7Exception 如果消息处理失败
     */
    UnparsedHL7Message onMessage(Connection conn, Socket s, UnparsedHL7Message msg) throws HL7Exception {
        HL7Segment msh = msg.msh();
        validateMSH(msh);
        HL7MessageListener listener = getHL7MessageListener();
        if (listener == null)
            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.APPLICATION_INTERNAL_ERROR)
                    .setUserMessage("No HL7 Message Listener configured"));
        return listener.onMessage(this, conn, s, msg);
    }

    /**
     * 验证MSH段
     *
     * @param msh MSH段
     * @throws HL7Exception 如果验证失败
     */
    private void validateMSH(HL7Segment msh) throws HL7Exception {
        String[] errorLocations = { ERRSegment.SENDING_APPLICATION, // MSH-3
                ERRSegment.SENDING_FACILITY, // MSH-4
                ERRSegment.RECEIVING_APPLICATION, // MSH-5
                ERRSegment.RECEIVING_FACILITY, // MSH-6
                ERRSegment.MESSAGE_DATETIME, // MSH-7
                null, // MSH-8
                ERRSegment.MESSAGE_CODE, // MSH-9
                ERRSegment.MESSAGE_CONTROL_ID, // MSH-10
                ERRSegment.MESSAGE_PROCESSING_ID, // MSH-11
                ERRSegment.MESSAGE_VERSION_ID, // MSH-12
        };
        String[] userMsg = { "Missing Sending Application", "Missing Sending Facility", "Missing Receiving Application",
                "Missing Receiving Facility", "Missing Date/Time of Message", null, "Missing Message Type",
                "Missing Message Control ID", "Missing Processing ID", "Missing Version ID" };
        for (int hl7OptionalMSHField : optionalMSHFields) {
            try {
                errorLocations[hl7OptionalMSHField - 3] = null;
            } catch (IndexOutOfBoundsException ignore) {
            }
        }
        errorLocations[6] = ERRSegment.MESSAGE_CODE; // never optional
        for (int i = 0; i < errorLocations.length; i++) {
            if (errorLocations[i] != null)
                if (msh.getField(i + 2, null) == null)
                    throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.REQUIRED_FIELD_MISSING)
                            .setErrorLocation(errorLocations[i]).setUserMessage(userMsg[i]));
        }
        if (!(acceptedSendingApplications.isEmpty()
                || acceptedSendingApplications.contains(msh.getSendingApplicationWithFacility())))
            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.TABLE_VALUE_NOT_FOUND)
                    .setErrorLocation(ERRSegment.SENDING_APPLICATION)
                    .setUserMessage("Sending Application and/or Facility not recognized"));
        String messageType = msh.getMessageType();
        if (!(acceptedMessageTypes.contains("*") || acceptedMessageTypes.contains(messageType))) {
            if (unsupportedMessageCode(messageType.substring(0, 3)))
                throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.UNSUPPORTED_MESSAGE_TYPE)
                        .setErrorLocation(ERRSegment.MESSAGE_CODE)
                        .setUserMessage("Message Type - Message Code not supported"));
            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.UNSUPPORTED_EVENT_CODE)
                    .setErrorLocation(ERRSegment.TRIGGER_EVENT)
                    .setUserMessage("Message Type - Trigger Event not supported"));
        }
    }

    /**
     * 检查消息代码是否不被支持
     *
     * @param messageType 消息类型
     * @return 如果消息代码不被支持则返回true，否则返回false
     */
    private boolean unsupportedMessageCode(String messageType) {
        for (String acceptedMessageType : acceptedMessageTypes) {
            if (acceptedMessageType.startsWith(messageType))
                return false;
        }
        return true;
    }

    /**
     * 连接到远程连接
     *
     * @param remote 远程连接
     * @return MLLP连接
     * @throws IOException              如果发生I/O错误
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public MLLPConnection connect(Connection remote) throws IOException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote);
    }

    /**
     * 连接到远程HL7应用程序
     *
     * @param remote 远程HL7应用程序
     * @return MLLP连接
     * @throws IOException              如果发生I/O错误
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public MLLPConnection connect(HL7Application remote)
            throws IOException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        return connect(cc.getLocalConnection(), cc.getRemoteConnection());
    }

    /**
     * 使用本地和远程连接建立连接
     *
     * @param local  本地连接
     * @param remote 远程连接
     * @return MLLP连接
     * @throws IOException              如果发生I/O错误
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public MLLPConnection connect(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        Socket sock = local.connect(remote);
        sock.setSoTimeout(local.getResponseTimeout());
        return new MLLPConnection(sock);
    }

    /**
     * 打开到远程连接的HL7连接
     *
     * @param remote 远程连接
     * @return HL7连接
     * @throws IOException              如果发生I/O错误
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public HL7Connection open(Connection remote) throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    /**
     * 打开到远程HL7应用程序的HL7连接
     *
     * @param remote 远程HL7应用程序
     * @return HL7连接
     * @throws IOException              如果发生I/O错误
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public HL7Connection open(HL7Application remote) throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    /**
     * 使用本地和远程连接打开HL7连接
     *
     * @param local  本地连接
     * @param remote 远程连接
     * @return HL7连接
     * @throws IOException              如果发生I/O错误
     * @throws InternalException        如果发生内部错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public HL7Connection open(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(local, remote));
    }

    /**
     * 查找与远程HL7应用程序兼容的连接
     *
     * @param remote 远程HL7应用程序
     * @return 兼容的连接对（本地连接和远程连接）
     * @throws InternalException 如果没有可用的兼容连接
     */
    public Compatible findCompatibleConnection(HL7Application remote) throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException(
                "No compatible connection to " + remote.getApplicationName() + " available on " + name);
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
        throw new InternalException("No compatible connection to " + remoteConn + " available on " + name);
    }

    /**
     * 检查应用程序是否已安装
     *
     * @throws IllegalStateException 如果应用程序未安装
     */
    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    /**
     * 检查应用程序是否已关联到设备
     *
     * @throws IllegalStateException 如果应用程序未关联到设备
     */
    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    /**
     * 从源HL7应用程序重新配置此应用程序
     *
     * @param src 源HL7应用程序
     */
    void reconfigure(HL7Application src) {
        setHL7ApplicationAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureHL7ApplicationExtensions(src);
    }

    /**
     * 从源HL7应用程序重新配置HL7应用程序扩展
     *
     * @param from 源HL7应用程序
     */
    private void reconfigureHL7ApplicationExtensions(HL7Application from) {
        extensions.keySet().removeIf(aClass -> !from.extensions.containsKey(aClass));
        for (HL7ApplicationExtension src : from.extensions.values()) {
            Class<? extends HL7ApplicationExtension> clazz = src.getClass();
            HL7ApplicationExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addHL7ApplicationExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    /**
     * 从源HL7应用程序设置HL7应用程序属性
     *
     * @param src 源HL7应用程序
     */
    protected void setHL7ApplicationAttributes(HL7Application src) {
        description = src.description;
        applicationClusters = src.applicationClusters;
        hl7DefaultCharacterSet = src.hl7DefaultCharacterSet;
        hl7SendingCharacterSet = src.hl7SendingCharacterSet;
        optionalMSHFields = src.optionalMSHFields;
        acceptedSendingApplications.clear();
        acceptedSendingApplications.addAll(src.acceptedSendingApplications);
        otherApplicationNames.clear();
        otherApplicationNames.addAll(src.otherApplicationNames);
        acceptedMessageTypes.clear();
        acceptedMessageTypes.addAll(src.acceptedMessageTypes);
        installed = src.installed;
    }

    /**
     * 添加HL7应用程序扩展
     *
     * @param ext 要添加的HL7应用程序扩展
     * @throws IllegalStateException 如果已存在相同类型的扩展
     */
    public void addHL7ApplicationExtension(HL7ApplicationExtension ext) {
        Class<? extends HL7ApplicationExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains AE Extension:" + clazz);
        ext.setHL7Application(this);
        extensions.put(clazz, ext);
    }

    /**
     * 移除HL7应用程序扩展
     *
     * @param ext 要移除的HL7应用程序扩展
     * @return 如果扩展被移除则返回true，否则返回false
     */
    public boolean removeHL7ApplicationExtension(HL7ApplicationExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;
        ext.setHL7Application(null);
        return true;
    }

    /**
     * 列出所有HL7应用程序扩展
     *
     * @return HL7应用程序扩展集合
     */
    public Collection<HL7ApplicationExtension> listHL7ApplicationExtensions() {
        return extensions.values();
    }

    /**
     * 获取指定类型的HL7应用程序扩展
     *
     * @param <T>   扩展类型
     * @param clazz 扩展类
     * @return 匹配的HL7应用程序扩展，如果没有则返回null
     */
    public <T extends HL7ApplicationExtension> T getHL7ApplicationExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    /**
     * 获取指定类型的HL7应用程序扩展，如果不存在则抛出异常
     *
     * @param <T>   扩展类型
     * @param clazz 扩展类
     * @return 匹配的HL7应用程序扩展
     * @throws IllegalStateException 如果不存在指定类型的扩展
     */
    public <T extends HL7ApplicationExtension> T getHL7AppExtensionNotNull(Class<T> clazz) {
        T hl7AppExt = getHL7ApplicationExtension(clazz);
        if (hl7AppExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for HL7 Application: " + name);
        return hl7AppExt;
    }

}
