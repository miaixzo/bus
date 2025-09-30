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
package org.miaixz.bus.image;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.tls.AnyTrustManager;
import org.miaixz.bus.image.galaxy.data.Code;
import org.miaixz.bus.image.galaxy.data.Issuer;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.ConnectionMonitor;
import org.miaixz.bus.image.metric.net.DeviceExtension;
import org.miaixz.bus.image.metric.net.KeycloakClient;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;

/**
 * 设备信息类
 * <p>
 * 该类表示DICOM网络中的一个设备实体，包含设备的基本信息、连接配置、应用实体、 证书配置等。设备可以包含多个应用实体(AE)、连接、Web应用和Keycloak客户端。
 * </p>
 * <p>
 * 设备支持TLS/SSL安全连接，可以配置信任存储和密钥存储，以及证书管理。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Device implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852253317829L;

    /**
     * AE可以发起的最大开放关联数映射
     */
    private final LinkedHashMap<String, Integer> limitAssociationsInitiatedBy = new LinkedHashMap<>();

    /**
     * 授权节点证书映射
     */
    private final LinkedHashMap<String, X509Certificate[]> authorizedNodeCertificates = new LinkedHashMap<>();

    /**
     * 本节点证书映射
     */
    private final LinkedHashMap<String, X509Certificate[]> thisNodeCertificates = new LinkedHashMap<>();

    /**
     * 连接列表
     */
    private final List<Connection> conns = new ArrayList<>();

    /**
     * 应用实体映射
     */
    private final LinkedHashMap<String, ApplicationEntity> aes = new LinkedHashMap<>();

    /**
     * Web应用映射
     */
    private final LinkedHashMap<String, WebApplication> webapps = new LinkedHashMap<>();

    /**
     * Keycloak客户端映射
     */
    private final LinkedHashMap<String, KeycloakClient> keycloakClients = new LinkedHashMap<>();

    /**
     * 设备扩展映射
     */
    private final Map<Class<? extends DeviceExtension>, DeviceExtension> extensions = new HashMap<>();

    /**
     * 关联列表
     */
    private transient final List<Association> associations = new ArrayList<>();

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备标识
     */
    private String deviceUID;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 设备制造商
     */
    private String manufacturer;

    /**
     * 设备商型号名称
     */
    private String manufacturerModelName;

    /**
     * 工作站名称
     */
    private String stationName;

    /**
     * 设备序列号
     */
    private String deviceSerialNumber;

    /**
     * 信任证书URL
     */
    private String trustStoreURL;

    /**
     * 信任证书类型
     */
    private String trustStoreType;

    /**
     * 信任证书PIN
     */
    private String trustStorePin;

    /**
     * 信任证书PIN属性
     */
    private String trustStorePinProperty;

    /**
     * 密钥库URL
     */
    private String keyStoreURL;

    /**
     * 密钥库类型
     */
    private String keyStoreType;

    /**
     * 密钥库PIN
     */
    private String keyStorePin;

    /**
     * 密钥库Pin属性
     */
    private String keyStorePinProperty;

    /**
     * 密钥库密钥PIN
     */
    private String keyStoreKeyPin;

    /**
     * 密钥库密钥Pin属性
     */
    private String keyStoreKeyPinProperty;

    /**
     * 患者ID签发者
     */
    private Issuer issuerOfPatientID;

    /**
     * 检查号签发者
     */
    private Issuer issuerOfAccessionNumber;

    /**
     * 检查申请者标识
     */
    private Issuer orderPlacerIdentifier;

    /**
     * 检查完成者标识
     */
    private Issuer orderFillerIdentifier;

    /**
     * 入院ID签发者
     */
    private Issuer issuerOfAdmissionID;

    /**
     * 服务事件ID签发者
     */
    private Issuer issuerOfServiceEpisodeID;

    /**
     * 容器标识签发者
     */
    private Issuer issuerOfContainerIdentifier;

    /**
     * 样本标识签发者
     */
    private Issuer issuerOfSpecimenIdentifier;

    /**
     * 软件版本
     */
    private String[] softwareVersions = {};

    /**
     * 主要设备类型
     */
    private String[] primaryDeviceTypes = {};

    /**
     * 设备关联的机构名称
     */
    private String[] institutionNames = {};

    /**
     * 设备关联的机构代码
     */
    private Code[] institutionCodes = {};

    /**
     * 设备的机构的地址
     */
    private String[] institutionAddresses = {};

    /**
     * 设备关联的部门名称
     */
    private String[] institutionalDepartmentNames = {};

    /**
     * 相关设备参考
     */
    private String[] relatedDeviceRefs = {};

    /**
     * 设备数据对象
     */
    private byte[][] vendorData = {};

    /**
     * 限制开放
     */
    private int limitOpenAssociations;

    /**
     * 当前是否安装在网络
     */
    private boolean installed = true;

    /**
     * 角色选择协商是否宽松
     */
    private boolean roleSelectionNegotiationLenient;

    /**
     * 设备的时区
     */
    private TimeZone timeZoneOfDevice;

    /**
     * ARC设备扩展
     */
    private Boolean arcDevExt;

    /**
     * 关联处理器
     */
    private transient AssociationHandler associationHandler = new AssociationHandler();

    /**
     * DIMSE请求处理器
     */
    private transient DimseRQHandler dimseRQHandler;

    /**
     * 连接监视器
     */
    private transient ConnectionMonitor connectionMonitor;

    /**
     * 关联监视器
     */
    private transient AssociationMonitor associationMonitor;

    /**
     * 执行器
     */
    private transient Executor executor;

    /**
     * 定时执行器
     */
    private transient ScheduledExecutorService scheduledExecutor;

    /**
     * SSL上下文
     */
    private transient volatile SSLContext sslContext;

    /**
     * 密钥管理器
     */
    private transient volatile KeyManager km;

    /**
     * 信任管理器
     */
    private transient volatile TrustManager tm;

    /**
     * 默认构造方法
     */
    public Device() {
    }

    /**
     * 构造方法
     *
     * @param name 设备名称
     */
    public Device(String name) {
        setDeviceName(name);
    }

    /**
     * 将证书集合转换为数组
     *
     * @param c 证书集合
     * @return 证书数组
     */
    private static X509Certificate[] toArray(Collection<X509Certificate[]> c) {
        int size = 0;
        for (X509Certificate[] certs : c)
            size += certs.length;
        X509Certificate[] dest = new X509Certificate[size];
        int destPos = 0;
        for (X509Certificate[] certs : c) {
            System.arraycopy(certs, 0, dest, destPos, certs.length);
            destPos += certs.length;
        }
        return dest;
    }

    /**
     * 检查字符串是否为空
     *
     * @param name 参数名称
     * @param val  参数值
     */
    private void checkNotEmpty(String name, String val) {
        if (val != null && val.isEmpty())
            throw new IllegalArgumentException(name + " cannot be empty");
    }

    /**
     * 获取该设备的名称
     *
     * @return 包含设备名的字符串
     */
    public final String getDeviceName() {
        return deviceName;
    }

    /**
     * 设置此设备的名称
     *
     * @param name 包含设备名的字符串
     */
    public final void setDeviceName(String name) {
        checkNotEmpty("Device Name", name);
        this.deviceName = name;
    }

    /**
     * 获取该设备的描述
     *
     * @return 包含设备描述的字符串
     */
    public final String getDescription() {
        return description;
    }

    /**
     * 设置该设备的描述
     *
     * @param description 包含设备描述的字符串
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取设备UID
     *
     * @return 设备UID
     */
    public String getDeviceUID() {
        return deviceUID;
    }

    /**
     * 设置设备UID
     *
     * @param deviceUID 设备UID
     */
    public void setDeviceUID(String deviceUID) {
        this.deviceUID = deviceUID;
    }

    /**
     * 获取这个设备的制造商
     *
     * @return 包含设备制造商的字符串
     */
    public final String getManufacturer() {
        return manufacturer;
    }

    /**
     * 设置该设备的制造商 这应该与该设备创建的SOP实例中的制造商(0008,0070)的值相同
     *
     * @param manufacturer 包含设备制造商的字符串
     */
    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * 获取该设备的制造商型号名称
     *
     * @return 包含设备制造商模型名称的字符串
     */
    public final String getManufacturerModelName() {
        return manufacturerModelName;
    }

    /**
     * 设置此设备的制造商型号名称 这应该与该设备创建的SOP实例中的制造商型号名称(0008,1090)的值相同
     *
     * @param manufacturerModelName 包含设备制造商模型名称的字符串
     */
    public final void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    /**
     * 获取在该设备上运行(或由该设备实现)的软件版本
     *
     * @return 包含软件版本的字符串数组
     */
    public final String[] getSoftwareVersions() {
        return softwareVersions;
    }

    /**
     * 设置在该设备上运行(或由该设备实现)的软件版本 这应该与该设备创建的SOP实例中的软件版本(0018、1020)的值相同
     *
     * @param softwareVersions 包含软件版本的字符串数组
     */
    public final void setSoftwareVersions(String... softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    /**
     * 获取属于此设备的工作站名称
     *
     * @return 包含电台名称的字符串
     */
    public final String getStationName() {
        return stationName;
    }

    /**
     * 设置属于此设备的工作站名称 这应该与此设备创建的SOP实例中的站名(0008,1010)的值相同
     *
     * @param stationName 包含电台名称的字符串
     */
    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    /**
     * 获取属于该设备的序列号
     *
     * @return 包含序列号的字符串
     */
    public final String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    /**
     * 设置此设备的序列号 这应该与该设备创建的SOP实例中的设备序列号(0018,1000)的值相同
     *
     * @param deviceSerialNumber 包含此设备的类型编解码器的字符串数组
     */
    public final void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    /**
     * 获取与此设备关联的类型编解码器
     *
     * @return 包含此设备的类型编解码器的字符串数组
     */
    public final String[] getPrimaryDeviceTypes() {
        return primaryDeviceTypes;
    }

    /**
     * 设置与此设备关联的类型编解码器 表示一种设备，最适用于采集方式。如果适用，类型应该从PS3.16中上下文ID 30的内部值(0008,0100)列表中选择
     *
     * @param primaryDeviceTypes 主要设备类型
     */
    public void setPrimaryDeviceTypes(String... primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    /**
     * 获取与此设备关联的机构名称;可能是它所驻留或代表的站点吗
     *
     * @return 包含机构名称值的字符串数组
     */
    public final String[] getInstitutionNames() {
        return institutionNames;
    }

    /**
     * 设置与此设备关联的机构名称;可能是它所驻留或代表的站点吗 是否应该与该设备创建的SOP实例中的机构名称(0008,0080)相同
     *
     * @param names 包含机构名称值的字符串数组
     */
    public void setInstitutionNames(String... names) {
        institutionNames = names;
    }

    /**
     * 获取机构代码
     *
     * @return 机构代码数组
     */
    public final Code[] getInstitutionCodes() {
        return institutionCodes;
    }

    /**
     * 设置机构代码
     *
     * @param codes 机构代码数组
     */
    public void setInstitutionCodes(Code... codes) {
        institutionCodes = codes;
    }

    /**
     * 设置操作该设备的机构的地址
     *
     * @return 包含机构地址值的字符串数组
     */
    public final String[] getInstitutionAddresses() {
        return institutionAddresses;
    }

    /**
     * 获取操作该设备的机构的地址 是否与该设备创建的SOP实例中的机构地址(0008,0081)属性值相同
     *
     * @param addresses 包含机构地址值的字符串数组
     */
    public void setInstitutionAddresses(String... addresses) {
        institutionAddresses = addresses;
    }

    /**
     * 获取与此设备关联的部门名称
     *
     * @return 包含部门名称值的字符串数组
     */
    public final String[] getInstitutionalDepartmentNames() {
        return institutionalDepartmentNames;
    }

    /**
     * 设置与此设备关联的部门名称 是否应该与该设备创建的SOP实例中的机构部门名称(0008,1040)的值相同
     *
     * @param names 包含部门名称值的字符串数组
     */
    public void setInstitutionalDepartmentNames(String... names) {
        institutionalDepartmentNames = names;
    }

    /**
     * 获取患者ID签发者
     *
     * @return 患者ID签发者
     */
    public final Issuer getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    /**
     * 设置患者ID签发者
     *
     * @param issuerOfPatientID 患者ID签发者
     */
    public final void setIssuerOfPatientID(Issuer issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    /**
     * 获取检查号签发者
     *
     * @return 检查号签发者
     */
    public final Issuer getIssuerOfAccessionNumber() {
        return issuerOfAccessionNumber;
    }

    /**
     * 设置检查号签发者
     *
     * @param issuerOfAccessionNumber 检查号签发者
     */
    public final void setIssuerOfAccessionNumber(Issuer issuerOfAccessionNumber) {
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
    }

    /**
     * 获取检查申请者标识
     *
     * @return 检查申请者标识
     */
    public final Issuer getOrderPlacerIdentifier() {
        return orderPlacerIdentifier;
    }

    /**
     * 设置检查申请者标识
     *
     * @param orderPlacerIdentifier 检查申请者标识
     */
    public final void setOrderPlacerIdentifier(Issuer orderPlacerIdentifier) {
        this.orderPlacerIdentifier = orderPlacerIdentifier;
    }

    /**
     * 获取检查完成者标识
     *
     * @return 检查完成者标识
     */
    public final Issuer getOrderFillerIdentifier() {
        return orderFillerIdentifier;
    }

    /**
     * 设置检查完成者标识
     *
     * @param orderFillerIdentifier 检查完成者标识
     */
    public final void setOrderFillerIdentifier(Issuer orderFillerIdentifier) {
        this.orderFillerIdentifier = orderFillerIdentifier;
    }

    /**
     * 获取入院ID签发者
     *
     * @return 入院ID签发者
     */
    public final Issuer getIssuerOfAdmissionID() {
        return issuerOfAdmissionID;
    }

    /**
     * 设置入院ID签发者
     *
     * @param issuerOfAdmissionID 入院ID签发者
     */
    public final void setIssuerOfAdmissionID(Issuer issuerOfAdmissionID) {
        this.issuerOfAdmissionID = issuerOfAdmissionID;
    }

    /**
     * 获取服务事件ID签发者
     *
     * @return 服务事件ID签发者
     */
    public final Issuer getIssuerOfServiceEpisodeID() {
        return issuerOfServiceEpisodeID;
    }

    /**
     * 设置服务事件ID签发者
     *
     * @param issuerOfServiceEpisodeID 服务事件ID签发者
     */
    public final void setIssuerOfServiceEpisodeID(Issuer issuerOfServiceEpisodeID) {
        this.issuerOfServiceEpisodeID = issuerOfServiceEpisodeID;
    }

    /**
     * 获取容器标识签发者
     *
     * @return 容器标识签发者
     */
    public final Issuer getIssuerOfContainerIdentifier() {
        return issuerOfContainerIdentifier;
    }

    /**
     * 设置容器标识签发者
     *
     * @param issuerOfContainerIdentifier 容器标识签发者
     */
    public final void setIssuerOfContainerIdentifier(Issuer issuerOfContainerIdentifier) {
        this.issuerOfContainerIdentifier = issuerOfContainerIdentifier;
    }

    /**
     * 获取样本标识签发者
     *
     * @return 样本标识签发者
     */
    public final Issuer getIssuerOfSpecimenIdentifier() {
        return issuerOfSpecimenIdentifier;
    }

    /**
     * 设置样本标识签发者
     *
     * @param issuerOfSpecimenIdentifier 样本标识签发者
     */
    public final void setIssuerOfSpecimenIdentifier(Issuer issuerOfSpecimenIdentifier) {
        this.issuerOfSpecimenIdentifier = issuerOfSpecimenIdentifier;
    }

    /**
     * 获取授权节点证书
     *
     * @param ref 引用
     * @return 授权节点证书
     */
    public X509Certificate[] getAuthorizedNodeCertificates(String ref) {
        return authorizedNodeCertificates.get(ref);
    }

    /**
     * 设置授权节点证书
     *
     * @param ref   引用
     * @param certs 证书
     */
    public void setAuthorizedNodeCertificates(String ref, X509Certificate... certs) {
        authorizedNodeCertificates.put(ref, certs);
        setTrustManager(null);
    }

    /**
     * 移除授权节点证书
     *
     * @param ref 引用
     * @return 证书
     */
    public X509Certificate[] removeAuthorizedNodeCertificates(String ref) {
        X509Certificate[] certs = authorizedNodeCertificates.remove(ref);
        setTrustManager(null);
        return certs;
    }

    /**
     * 移除所有授权节点证书
     */
    public void removeAllAuthorizedNodeCertificates() {
        authorizedNodeCertificates.clear();
        setTrustManager(null);
    }

    /**
     * 获取所有授权节点证书
     *
     * @return 证书数组
     */
    public X509Certificate[] getAllAuthorizedNodeCertificates() {
        return toArray(authorizedNodeCertificates.values());
    }

    /**
     * 获取授权节点证书引用
     *
     * @return 引用数组
     */
    public String[] getAuthorizedNodeCertificateRefs() {
        return authorizedNodeCertificates.keySet().toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * 获取信任存储URL
     *
     * @return 信任存储URL
     */
    public final String getTrustStoreURL() {
        return trustStoreURL;
    }

    /**
     * 设置信任存储URL
     *
     * @param trustStoreURL 信任存储URL
     */
    public final void setTrustStoreURL(String trustStoreURL) {
        checkNotEmpty("trustStoreURL", trustStoreURL);
        if (Objects.equals(trustStoreURL, this.trustStoreURL))
            return;
        this.trustStoreURL = trustStoreURL;
        setTrustManager(null);
    }

    /**
     * 获取信任存储类型
     *
     * @return 信任存储类型
     */
    public final String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * 设置信任存储类型
     *
     * @param trustStoreType 信任存储类型
     */
    public final void setTrustStoreType(String trustStoreType) {
        checkNotEmpty("trustStoreType", trustStoreType);
        this.trustStoreType = trustStoreType;
    }

    /**
     * 获取信任存储PIN
     *
     * @return 信任存储PIN
     */
    public final String getTrustStorePin() {
        return trustStorePin;
    }

    /**
     * 设置信任存储PIN
     *
     * @param trustStorePin 信任存储PIN
     */
    public final void setTrustStorePin(String trustStorePin) {
        checkNotEmpty("trustStorePin", trustStorePin);
        this.trustStorePin = trustStorePin;
    }

    /**
     * 获取信任存储PIN属性
     *
     * @return 信任存储PIN属性
     */
    public final String getTrustStorePinProperty() {
        return trustStorePinProperty;
    }

    /**
     * 设置信任存储PIN属性
     *
     * @param trustStorePinProperty 信任存储PIN属性
     */
    public final void setTrustStorePinProperty(String trustStorePinProperty) {
        checkNotEmpty("trustStorePinProperty", trustStorePinProperty);
        this.trustStorePinProperty = trustStorePinProperty;
    }

    /**
     * 获取本节点证书
     *
     * @param ref 引用
     * @return 证书
     */
    public X509Certificate[] getThisNodeCertificates(String ref) {
        return thisNodeCertificates.get(ref);
    }

    /**
     * 设置本节点证书
     *
     * @param ref   引用
     * @param certs 证书
     */
    public void setThisNodeCertificates(String ref, X509Certificate... certs) {
        thisNodeCertificates.put(ref, certs);
    }

    /**
     * 移除本节点证书
     *
     * @param ref 引用
     * @return 证书
     */
    public X509Certificate[] removeThisNodeCertificates(String ref) {
        return thisNodeCertificates.remove(ref);
    }

    /**
     * 获取密钥存储URL
     *
     * @return 密钥存储URL
     */
    public final String getKeyStoreURL() {
        return keyStoreURL;
    }

    /**
     * 设置密钥存储URL
     *
     * @param keyStoreURL 密钥存储URL
     */
    public final void setKeyStoreURL(String keyStoreURL) {
        checkNotEmpty("keyStoreURL", keyStoreURL);
        if (Objects.equals(keyStoreURL, this.keyStoreURL))
            return;
        this.keyStoreURL = keyStoreURL;
        setKeyManager(null);
    }

    /**
     * 获取密钥存储类型
     *
     * @return 密钥存储类型
     */
    public final String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * 设置密钥存储类型
     *
     * @param keyStoreType 密钥存储类型
     */
    public final void setKeyStoreType(String keyStoreType) {
        checkNotEmpty("keyStoreType", keyStoreType);
        this.keyStoreType = keyStoreType;
    }

    /**
     * 获取密钥存储PIN
     *
     * @return 密钥存储PIN
     */
    public final String getKeyStorePin() {
        return keyStorePin;
    }

    /**
     * 设置密钥存储PIN
     *
     * @param keyStorePin 密钥存储PIN
     */
    public final void setKeyStorePin(String keyStorePin) {
        checkNotEmpty("keyStorePin", keyStorePin);
        this.keyStorePin = keyStorePin;
    }

    /**
     * 获取密钥存储PIN属性
     *
     * @return 密钥存储PIN属性
     */
    public final String getKeyStorePinProperty() {
        return keyStorePinProperty;
    }

    /**
     * 设置密钥存储PIN属性
     *
     * @param keyStorePinProperty 密钥存储PIN属性
     */
    public final void setKeyStorePinProperty(String keyStorePinProperty) {
        checkNotEmpty("keyStorePinProperty", keyStorePinProperty);
        this.keyStorePinProperty = keyStorePinProperty;
    }

    /**
     * 获取密钥存储密钥PIN
     *
     * @return 密钥存储密钥PIN
     */
    public final String getKeyStoreKeyPin() {
        return keyStoreKeyPin;
    }

    /**
     * 设置密钥存储密钥PIN
     *
     * @param keyStoreKeyPin 密钥存储密钥PIN
     */
    public final void setKeyStoreKeyPin(String keyStoreKeyPin) {
        checkNotEmpty("keyStoreKeyPin", keyStoreKeyPin);
        this.keyStoreKeyPin = keyStoreKeyPin;
    }

    /**
     * 获取密钥存储密钥PIN属性
     *
     * @return 密钥存储密钥PIN属性
     */
    public final String getKeyStoreKeyPinProperty() {
        return keyStoreKeyPinProperty;
    }

    /**
     * 设置密钥存储密钥PIN属性
     *
     * @param keyStoreKeyPinProperty 密钥存储密钥PIN属性
     */
    public final void setKeyStoreKeyPinProperty(String keyStoreKeyPinProperty) {
        checkNotEmpty("keyStoreKeyPinProperty", keyStoreKeyPinProperty);
        this.keyStoreKeyPinProperty = keyStoreKeyPinProperty;
    }

    /**
     * 移除所有本节点证书
     */
    public void removeAllThisNodeCertificates() {
        thisNodeCertificates.clear();
    }

    /**
     * 获取所有本节点证书
     *
     * @return 证书数组
     */
    public X509Certificate[] getAllThisNodeCertificates() {
        return toArray(thisNodeCertificates.values());
    }

    /**
     * 获取本节点证书引用
     *
     * @return 引用数组
     */
    public String[] getThisNodeCertificateRefs() {
        return thisNodeCertificates.keySet().toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * 获取相关设备引用
     *
     * @return 相关设备引用数组
     */
    public final String[] getRelatedDeviceRefs() {
        return relatedDeviceRefs;
    }

    /**
     * 设置相关设备引用
     *
     * @param refs 相关设备引用数组
     */
    public void setRelatedDeviceRefs(String... refs) {
        relatedDeviceRefs = refs;
    }

    /**
     * 获取设备特定的供应商配置信息
     *
     * @return 设备数据的一个对象
     */
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * 设置设备特定的供应商配置信息
     *
     * @param vendorData 设备数据的一个对象
     */
    public void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * 获取一个布尔值，指示此设备当前是否安装在网络上 (这对于预配置、移动货车和类似情况非常有用)
     *
     * @return 一个布尔值，如果安装了这个设备，它将为真
     */
    public final boolean isInstalled() {
        return installed;
    }

    /**
     * 设置一个布尔值，指示此设备当前是否安装在网络上 (这对于预配置、移动货车和类似情况非常有用)
     *
     * @param installed 一个布尔值，如果安装了这个设备，它将为真
     */
    public final void setInstalled(boolean installed) {
        if (this.installed == installed)
            return;
        this.installed = installed;
        needRebindConnections();
    }

    /**
     * 获取角色选择协商是否宽松
     *
     * @return 角色选择协商是否宽松
     */
    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    /**
     * 设置角色选择协商是否宽松
     *
     * @param roleSelectionNegotiationLenient 角色选择协商是否宽松
     */
    public void setRoleSelectionNegotiationLenient(boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    /**
     * 获取设备的时区
     *
     * @return 设备的时区
     */
    public TimeZone getTimeZoneOfDevice() {
        return timeZoneOfDevice;
    }

    /**
     * 设置设备的时区
     *
     * @param timeZoneOfDevice 设备的时区
     */
    public void setTimeZoneOfDevice(TimeZone timeZoneOfDevice) {
        this.timeZoneOfDevice = timeZoneOfDevice;
    }

    /**
     * 获取DIMSE请求处理器
     *
     * @return DIMSE请求处理器
     */
    public final DimseRQHandler getDimseRQHandler() {
        return dimseRQHandler;
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
     * 获取ARC设备扩展
     *
     * @return ARC设备扩展
     */
    public Boolean getArcDevExt() {
        return arcDevExt;
    }

    /**
     * 设置ARC设备扩展
     *
     * @param arcDevExt ARC设备扩展
     */
    public void setArcDevExt(Boolean arcDevExt) {
        this.arcDevExt = arcDevExt;
    }

    /**
     * 获取关联处理器
     *
     * @return 关联处理器
     */
    public final AssociationHandler getAssociationHandler() {
        return associationHandler;
    }

    /**
     * 设置关联处理器
     *
     * @param associationHandler 关联处理器
     */
    public void setAssociationHandler(AssociationHandler associationHandler) {
        if (associationHandler == null)
            throw new NullPointerException();
        this.associationHandler = associationHandler;
    }

    /**
     * 获取连接监视器
     *
     * @return 连接监视器
     */
    public ConnectionMonitor getConnectionMonitor() {
        return connectionMonitor;
    }

    /**
     * 设置连接监视器
     *
     * @param connectionMonitor 连接监视器
     */
    public void setConnectionMonitor(ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
    }

    /**
     * 获取关联监视器
     *
     * @return 关联监视器
     */
    public AssociationMonitor getAssociationMonitor() {
        return associationMonitor;
    }

    /**
     * 设置关联监视器
     *
     * @param associationMonitor 关联监视器
     */
    public void setAssociationMonitor(AssociationMonitor associationMonitor) {
        this.associationMonitor = associationMonitor;
    }

    /**
     * 绑定连接
     *
     * @throws IOException              IO异常
     * @throws GeneralSecurityException 安全异常
     */
    public void bindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            con.bind();
    }

    /**
     * 重新绑定连接
     *
     * @throws IOException              IO异常
     * @throws GeneralSecurityException 安全异常
     */
    public void rebindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            if (con.isRebindNeeded())
                con.rebind();
    }

    /**
     * 需要重新绑定连接
     */
    private void needRebindConnections() {
        for (Connection con : conns)
            con.needRebind();
    }

    /**
     * 需要重新配置TLS
     */
    private void needReconfigureTLS() {
        for (Connection con : conns)
            if (con.isTls())
                con.needRebind();
        sslContext = null;
    }

    /**
     * 解绑连接
     */
    public void unbindConnections() {
        for (Connection con : conns)
            con.unbind();
    }

    /**
     * 获取执行器
     *
     * @return 执行器
     */
    public final Executor getExecutor() {
        return executor;
    }

    /**
     * 设置执行器
     *
     * @param executor 执行器
     */
    public final void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * 获取定时执行器
     *
     * @return 定时执行器
     */
    public final ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * 设置定时执行器
     *
     * @param executor 定时执行器
     */
    public final void setScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = executor;
    }

    /**
     * 添加连接
     *
     * @param conn 连接
     */
    public void addConnection(Connection conn) {
        conn.setDevice(this);
        conns.add(conn);
        conn.needRebind();
    }

    /**
     * 移除连接
     *
     * @param conn 连接
     * @return 是否移除成功
     */
    public boolean removeConnection(Connection conn) {
        for (ApplicationEntity ae : aes.values())
            if (ae.getConnections().contains(conn))
                throw new IllegalStateException(conn + " used by AE: " + ae.getAETitle());
        for (DeviceExtension ext : extensions.values())
            ext.verifyNotUsed(conn);
        if (!conns.remove(conn))
            return false;
        conn.setDevice(null);
        conn.unbind();
        return true;
    }

    /**
     * 列出连接
     *
     * @return 连接列表
     */
    public List<Connection> listConnections() {
        return Collections.unmodifiableList(conns);
    }

    /**
     * 查找具有相等RDN的连接
     *
     * @param other 其他连接
     * @return 连接
     */
    public Connection connectionWithEqualsRDN(Connection other) {
        for (Connection conn : conns)
            if (conn.equalsRDN(other))
                return conn;
        return null;
    }

    /**
     * 添加应用实体
     *
     * @param ae 应用实体
     */
    public void addApplicationEntity(ApplicationEntity ae) {
        ae.setDevice(this);
        aes.put(ae.getAETitle(), ae);
    }

    /**
     * 移除应用实体
     *
     * @param ae 应用实体
     * @return 移除的应用实体
     */
    public ApplicationEntity removeApplicationEntity(ApplicationEntity ae) {
        return removeApplicationEntity(ae.getAETitle());
    }

    /**
     * 移除应用实体
     *
     * @param aet 应用实体标题
     * @return 移除的应用实体
     */
    public ApplicationEntity removeApplicationEntity(String aet) {
        ApplicationEntity ae = aes.remove(aet);
        if (ae != null)
            ae.setDevice(null);
        return ae;
    }

    /**
     * 获取Web应用名称集合
     *
     * @return Web应用名称集合
     */
    public Collection<String> getWebApplicationNames() {
        return webapps.keySet();
    }

    /**
     * 获取Web应用集合
     *
     * @return Web应用集合
     */
    public Collection<WebApplication> getWebApplications() {
        return webapps.values();
    }

    /**
     * 获取具有指定服务类的Web应用
     *
     * @param serviceClass 服务类
     * @return Web应用集合
     */
    public Collection<WebApplication> getWebApplicationsWithServiceClass(WebApplication.ServiceClass serviceClass) {
        Collection<WebApplication> result = new ArrayList<>(webapps.size());
        for (WebApplication webapp : webapps.values()) {
            if (webapp.containsServiceClass(serviceClass))
                result.add(webapp);
        }
        return result;
    }

    /**
     * 获取Web应用
     *
     * @param name Web应用名称
     * @return Web应用
     */
    public WebApplication getWebApplication(String name) {
        return webapps.get(name);
    }

    /**
     * 添加Web应用
     *
     * @param webapp Web应用
     */
    public void addWebApplication(WebApplication webapp) {
        webapp.setDevice(this);
        webapps.put(webapp.getApplicationName(), webapp);
    }

    /**
     * 移除Web应用
     *
     * @param webapp Web应用
     * @return 移除的Web应用
     */
    public WebApplication removeWebApplication(WebApplication webapp) {
        return removeWebApplication(webapp.getApplicationName());
    }

    /**
     * 移除Web应用
     *
     * @param name Web应用名称
     * @return 移除的Web应用
     */
    public WebApplication removeWebApplication(String name) {
        WebApplication webapp = webapps.remove(name);
        if (webapp != null)
            webapp.setDevice(null);
        return webapp;
    }

    /**
     * 获取Keycloak客户端ID集合
     *
     * @return Keycloak客户端ID集合
     */
    public Collection<String> getKeycloakClientIDs() {
        return keycloakClients.keySet();
    }

    /**
     * 获取Keycloak客户端集合
     *
     * @return Keycloak客户端集合
     */
    public Collection<KeycloakClient> getKeycloakClients() {
        return keycloakClients.values();
    }

    /**
     * 获取Keycloak客户端
     *
     * @param clientID 客户端ID
     * @return Keycloak客户端
     */
    public KeycloakClient getKeycloakClient(String clientID) {
        return keycloakClients.get(clientID);
    }

    /**
     * 添加Keycloak客户端
     *
     * @param client Keycloak客户端
     */
    public void addKeycloakClient(KeycloakClient client) {
        client.setDevice(this);
        keycloakClients.put(client.getKeycloakClientID(), client);
    }

    /**
     * 移除Keycloak客户端
     *
     * @param client Keycloak客户端
     * @return 移除的Keycloak客户端
     */
    public KeycloakClient removeKeycloakClient(KeycloakClient client) {
        return removeKeycloakClient(client.getKeycloakClientID());
    }

    /**
     * 移除Keycloak客户端
     *
     * @param name 客户端ID
     * @return 移除的Keycloak客户端
     */
    public KeycloakClient removeKeycloakClient(String name) {
        KeycloakClient client = keycloakClients.remove(name);
        if (client != null)
            client.setDevice(null);
        return client;
    }

    /**
     * 添加设备扩展
     *
     * @param ext 设备扩展
     */
    public void addDeviceExtension(DeviceExtension ext) {
        Class<? extends DeviceExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains Device Extension:" + clazz);
        ext.setDevice(this);
        extensions.put(clazz, ext);
    }

    /**
     * 移除设备扩展
     *
     * @param ext 设备扩展
     * @return 是否移除成功
     */
    public boolean removeDeviceExtension(DeviceExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;
        ext.setDevice(null);
        return true;
    }

    /**
     * 获取开放关联限制
     *
     * @return 开放关联限制
     */
    public final int getLimitOpenAssociations() {
        return limitOpenAssociations;
    }

    /**
     * 设置开放关联限制
     *
     * @param limit 开放关联限制
     */
    public final void setLimitOpenAssociations(int limit) {
        if (limit < 0)
            throw new IllegalArgumentException("limit: " + limit);
        this.limitOpenAssociations = limit;
    }

    /**
     * 返回指定的远程AE可以发起的最大开放关联数。 如果超过了这个限制，那么来自AE的进一步关联请求将被拒绝 Result = 2 - rejected-transient，Source = 1 - DICOM UL
     * service-user，Reason = 2 - local-limit-exceeded
     *
     * @param callingAET 远程AE的AE名称
     * @return 开放关联的最大数目或无限制为0
     * @throws NullPointerException 如果callingAET为空
     * @see #setLimitAssociationsInitiatedBy(String, int)
     */
    public int getLimitAssociationsInitiatedBy(String callingAET) {
        Integer value = limitAssociationsInitiatedBy.get(Objects.requireNonNull(callingAET));
        return value != null ? value.intValue() : 0;
    }

    /**
     * 返回指定的远程AE可以发起的最大开放关联数。如果超过了这个限制，那么来自AE的进一步关联请求将被拒绝 Result = 2 - rejected-transient， Source = 1 - DICOM UL
     * service-user， Reason = 2 - local-limit-exceeded
     *
     * @param callingAET 远程AE的AE名称
     * @param limit      开放关联的最大数目或无限制为0
     * @throws NullPointerException     如果callingAET为空
     * @throws IllegalArgumentException 如果限制小于零
     * @see #getLimitAssociationsInitiatedBy(String)
     */
    public void setLimitAssociationsInitiatedBy(String callingAET, int limit) {
        Objects.requireNonNull(callingAET);
        if (limit < 0)
            throw new IllegalArgumentException("limit: " + limit);
        if (limit > 0)
            limitAssociationsInitiatedBy.put(callingAET, limit);
        else
            limitAssociationsInitiatedBy.remove(callingAET);
    }

    /**
     * 获取所有远程AE及其关联限制的数组。 每个数组元素格式为"AE名称=限制值"。
     *
     * @return 包含AE名称和限制值的字符串数组
     */
    public String[] getLimitAssociationsInitiatedBy() {
        String[] ss = new String[limitAssociationsInitiatedBy.size()];
        int i = 0;
        for (Entry<String, Integer> entry : limitAssociationsInitiatedBy.entrySet()) {
            ss[i++] = entry.getKey() + Symbol.C_EQUAL + entry.getValue();
        }
        return ss;
    }

    /**
     * 通过字符串数组设置远程AE的关联限制。 每个数组元素格式应为"AE名称=限制值"。
     *
     * @param values 包含AE名称和限制值的字符串数组
     * @throws IllegalArgumentException 如果数组中的任何元素格式无效
     */
    public void setLimitAssociationsInitiatedBy(String[] values) {
        Map<String, Integer> tmp = new HashMap<>();
        for (String value : values) {
            int endIndex = value.lastIndexOf(Symbol.C_EQUAL);
            try {
                tmp.put(value.substring(0, endIndex), Integer.valueOf(value.substring(endIndex + 1)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(value);
            }
        }
        setLimitAssociationsInitiatedBy(tmp);
    }

    /**
     * 批量设置远程AE的关联限制。 此方法会先清除所有现有限制，然后设置新的限制。
     *
     * @param tmp 包含AE名称和限制值的映射
     */
    private void setLimitAssociationsInitiatedBy(Map<String, Integer> tmp) {
        limitAssociationsInitiatedBy.clear();
        limitAssociationsInitiatedBy.putAll(tmp);
    }

    /**
     * 添加一个新的关联到关联列表中。
     *
     * @param as 要添加的关联对象
     */
    public void addAssociation(Association as) {
        synchronized (associations) {
            associations.add(as);
        }
    }

    /**
     * 从关联列表中移除指定的关联，并通知所有等待的线程。
     *
     * @param as 要移除的关联对象
     */
    public void removeAssociation(Association as) {
        synchronized (associations) {
            associations.remove(as);
            associations.notifyAll();
        }
    }

    /**
     * 获取所有开放关联的数组。
     *
     * @return 包含所有开放关联的数组
     */
    public Association[] listOpenAssociations() {
        synchronized (associations) {
            return associations.toArray(new Association[associations.size()]);
        }
    }

    /**
     * 获取当前开放关联的数量。
     *
     * @return 开放关联的数量
     */
    public int getNumberOfOpenAssociations() {
        return associations.size();
    }

    /**
     * 获取由指定AE发起的关联数量。
     *
     * @param callingAET 远程AE的名称
     * @return 由指定AE发起的关联数量
     */
    public int getNumberOfAssociationsInitiatedBy(String callingAET) {
        synchronized (associations) {
            int count = 0;
            for (Association association : associations) {
                if (callingAET.equals(association.getCallingAET()))
                    count++;
            }
            return count;
        }
    }

    /**
     * 获取发往指定AE的关联数量。
     *
     * @param calledAET 目标AE的名称
     * @return 发往指定AE的关联数量
     */
    public int getNumberOfAssociationsInitiatedTo(String calledAET) {
        synchronized (associations) {
            int count = 0;
            for (Association association : associations) {
                if (calledAET.equals(association.getCalledAET()))
                    count++;
            }
            return count;
        }
    }

    /**
     * 等待直到没有开放的连接。 如果当前有开放的连接，此方法将阻塞，直到所有连接都关闭。
     *
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public void waitForNoOpenConnections() throws InterruptedException {
        synchronized (associations) {
            while (!associations.isEmpty())
                associations.wait();
        }
    }

    /**
     * 检查关联请求是否超过了限制。 如果全局开放关联数限制大于0且当前开放关联数超过了该限制，或者 如果特定AE的关联限制存在且该AE当前关联数超过了该限制，则返回true。
     *
     * @param rq 关联请求
     * @return 如果超过了限制返回true，否则返回false
     */
    public boolean isLimitOfAssociationsExceeded(AAssociateRQ rq) {
        Integer limit;
        return limitOpenAssociations > 0 && associations.size() > limitOpenAssociations
                || (limit = limitAssociationsInitiatedBy.get(rq.getCallingAET())) != null
                        && getNumberOfAssociationsInitiatedBy(rq.getCallingAET()) > limit;
    }

    /**
     * 根据AE标题获取应用程序实体。
     *
     * @param aet AE标题
     * @return 具有指定AE标题的应用程序实体，如果不存在则返回null
     */
    public ApplicationEntity getApplicationEntity(String aet) {
        return aes.get(aet);
    }

    /**
     * 根据AE标题获取应用程序实体，支持通配符和其他AE标题匹配。 首先尝试精确匹配，如果没有找到则尝试通配符(*)匹配， 如果仍然没有找到且允许匹配其他AE标题，则尝试匹配其他AE标题。
     *
     * @param aet            AE标题
     * @param matchOtherAETs 是否允许匹配其他AE标题
     * @return 匹配的应用程序实体，如果找不到则返回null
     */
    public ApplicationEntity getApplicationEntity(String aet, boolean matchOtherAETs) {
        ApplicationEntity ae = aes.get(aet);
        if (ae == null)
            ae = aes.get(Symbol.STAR);
        if (ae == null && matchOtherAETs)
            for (ApplicationEntity ae1 : getApplicationEntities())
                if (ae1.isOtherAETitle(aet))
                    return ae1;
        return ae;
    }

    /**
     * 获取所有应用程序AE标题的集合。
     *
     * @return 包含所有应用程序AE标题的集合
     */
    public Collection<String> getApplicationAETitles() {
        return aes.keySet();
    }

    /**
     * 获取所有应用程序实体的集合。
     *
     * @return 包含所有应用程序实体的集合
     */
    public Collection<ApplicationEntity> getApplicationEntities() {
        return aes.values();
    }

    /**
     * 获取密钥管理器。
     *
     * @return 密钥管理器
     */
    public final KeyManager getKeyManager() {
        return km;
    }

    /**
     * 设置密钥管理器。
     *
     * @param km 要设置的密钥管理器
     */
    public final void setKeyManager(KeyManager km) {
        this.km = km;
        needReconfigureTLS();
    }

    /**
     * 获取或创建密钥管理器。 如果密钥管理器已存在或密钥存储URL为null，则返回现有的密钥管理器。 否则，使用密钥存储信息创建新的密钥管理器。
     *
     * @return 密钥管理器
     * @throws GeneralSecurityException 如果创建密钥管理器时发生安全异常
     * @throws IOException              如果读取密钥存储时发生I/O异常
     */
    private KeyManager km() throws GeneralSecurityException, IOException {
        KeyManager ret = km;
        if (ret != null || keyStoreURL == null)
            return ret;
        String keyStorePin = keyStorePin();
        km = ret = AnyTrustManager.createKeyManager(
                Builder.replaceSystemProperties(keyStoreType()),
                Builder.replaceSystemProperties(keyStoreURL),
                Builder.replaceSystemProperties(keyStorePin()),
                Builder.replaceSystemProperties(keyPin(keyStorePin)));
        return ret;
    }

    /**
     * 获取密钥存储类型。 如果密钥存储类型为null，则抛出异常。
     *
     * @return 密钥存储类型
     * @throws IllegalStateException 如果密钥存储类型为null
     */
    private String keyStoreType() {
        if (keyStoreType == null)
            throw new IllegalStateException("keyStoreURL requires keyStoreType");
        return keyStoreType;
    }

    /**
     * 获取密钥存储PIN。 如果直接设置了密钥存储PIN，则返回该PIN。 否则，尝试从系统属性中获取PIN。
     *
     * @return 密钥存储PIN
     * @throws IllegalStateException 如果既没有直接设置PIN，也没有设置PIN属性，或者属性不存在
     */
    private String keyStorePin() {
        if (keyStorePin != null)
            return keyStorePin;
        if (keyStorePinProperty == null)
            throw new IllegalStateException("keyStoreURL requires keyStorePin or keyStorePinProperty");
        String pin = System.getProperty(keyStorePinProperty);
        if (pin == null)
            throw new IllegalStateException("No such keyStorePinProperty: " + keyStorePinProperty);
        return pin;
    }

    /**
     * 获取密钥PIN。 如果直接设置了密钥PIN，则返回该PIN。 否则，如果设置了密钥PIN属性，则从系统属性中获取PIN。 如果都没有，则返回密钥存储PIN。
     *
     * @param keyStorePin 密钥存储PIN
     * @return 密钥PIN
     * @throws IllegalStateException 如果设置了密钥PIN属性但属性不存在
     */
    private String keyPin(String keyStorePin) {
        if (keyStoreKeyPin != null)
            return keyStoreKeyPin;
        if (keyStoreKeyPinProperty == null)
            return keyStorePin;
        String pin = System.getProperty(keyStoreKeyPinProperty);
        if (pin == null)
            throw new IllegalStateException("No such keyPinProperty: " + keyStoreKeyPinProperty);
        return pin;
    }

    /**
     * 获取信任管理器。
     *
     * @return 信任管理器
     */
    public final TrustManager getTrustManager() {
        return tm;
    }

    /**
     * 设置信任管理器。
     *
     * @param tm 要设置的信任管理器
     */
    public final void setTrustManager(TrustManager tm) {
        this.tm = tm;
        needReconfigureTLS();
    }

    /**
     * 获取或创建信任管理器。 如果信任管理器已存在或没有设置信任存储URL且没有授权节点证书，则返回现有的信任管理器。 否则，使用信任存储信息或授权节点证书创建新的信任管理器。
     *
     * @return 信任管理器
     * @throws GeneralSecurityException 如果创建信任管理器时发生安全异常
     * @throws IOException              如果读取信任存储时发生I/O异常
     */
    private TrustManager tm() throws GeneralSecurityException, IOException {
        TrustManager ret = tm;
        if (ret != null || trustStoreURL == null && authorizedNodeCertificates.isEmpty())
            return ret;
        tm = ret = trustStoreURL != null
                ? AnyTrustManager.createTrustManager(
                        Builder.replaceSystemProperties(trustStoreType()),
                        Builder.replaceSystemProperties(trustStoreURL),
                        Builder.replaceSystemProperties(trustStorePin()))
                : AnyTrustManager.createTrustManager(getAllAuthorizedNodeCertificates());
        return ret;
    }

    /**
     * 获取信任存储类型。 如果信任存储类型为null，则抛出异常。
     *
     * @return 信任存储类型
     * @throws IllegalStateException 如果信任存储类型为null
     */
    private String trustStoreType() {
        if (trustStoreType == null)
            throw new IllegalStateException("trustStoreURL requires trustStoreType");
        return trustStoreType;
    }

    /**
     * 获取信任存储PIN。 如果直接设置了信任存储PIN，则返回该PIN。 否则，尝试从系统属性中获取PIN。
     *
     * @return 信任存储PIN
     * @throws IllegalStateException 如果既没有直接设置PIN，也没有设置PIN属性，或者属性不存在
     */
    private String trustStorePin() {
        if (trustStorePin != null)
            return trustStorePin;
        if (trustStorePinProperty == null)
            throw new IllegalStateException("trustStoreURL requires trustStorePin or trustStorePinProperty");
        String pin = System.getProperty(trustStorePinProperty);
        if (pin == null)
            throw new IllegalStateException("No such trustStorePinProperty: " + trustStorePinProperty);
        return pin;
    }

    /**
     * 获取或创建SSL上下文。 如果SSL上下文已存在，则返回现有的SSL上下文。 否则，创建新的SSL上下文并初始化。
     *
     * @return SSL上下文
     * @throws GeneralSecurityException 如果创建SSL上下文时发生安全异常
     * @throws IOException              如果初始化SSL上下文时发生I/O异常
     */
    public SSLContext sslContext() throws GeneralSecurityException, IOException {
        SSLContext ctx = sslContext;
        if (ctx != null)
            return ctx;
        ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagers(), trustManagers(), null);
        sslContext = ctx;
        return ctx;
    }

    /**
     * 获取密钥管理器数组。
     *
     * @return 包含密钥管理器的数组，如果没有密钥管理器则返回null
     * @throws GeneralSecurityException 如果获取密钥管理器时发生安全异常
     * @throws IOException              如果获取密钥管理器时发生I/O异常
     */
    public KeyManager[] keyManagers() throws GeneralSecurityException, IOException {
        KeyManager tmp = km();
        return tmp != null ? new KeyManager[] { tmp } : null;
    }

    /**
     * 获取信任管理器数组。
     *
     * @return 包含信任管理器的数组，如果没有信任管理器则返回null
     * @throws GeneralSecurityException 如果获取信任管理器时发生安全异常
     * @throws IOException              如果获取信任管理器时发生I/O异常
     */
    public TrustManager[] trustManagers() throws GeneralSecurityException, IOException {
        TrustManager tmp = tm();
        return tmp != null ? new TrustManager[] { tmp } : null;
    }

    /**
     * 执行一个任务。
     *
     * @param command 要执行的任务
     * @throws IllegalStateException 如果执行器未初始化
     */
    public void execute(Runnable command) {
        if (executor == null)
            throw new IllegalStateException("executor not initialized");
        executor.execute(command);
    }

    /**
     * 调度一个延迟执行的任务。
     *
     * @param command 要执行的任务
     * @param delay   延迟时间
     * @param unit    延迟时间的单位
     * @return 表示任务未完成结果的ScheduledFuture
     * @throws IllegalStateException 如果调度执行器服务未初始化
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException("scheduled executor service not initialized");
        return scheduledExecutor.schedule(command, delay, unit);
    }

    /**
     * 调度一个固定速率执行的任务。 任务将在初始延迟后首次执行，然后在固定的时间间隔内重复执行。
     *
     * @param command      要执行的任务
     * @param initialDelay 初始延迟时间
     * @param period       连续执行之间的时间间隔
     * @param unit         时间单位
     * @return 表示任务未完成结果的ScheduledFuture
     * @throws IllegalStateException 如果调度执行器服务未初始化
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException("scheduled executor service not initialized");
        return scheduledExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * 调度一个固定延迟执行的任务。 任务将在初始延迟后首次执行，然后在每次执行完成后等待指定的延迟时间再次执行。
     *
     * @param command      要执行的任务
     * @param initialDelay 初始延迟时间
     * @param delay        一次执行终止和下一次执行开始之间的延迟
     * @param unit         时间单位
     * @return 表示任务未完成结果的ScheduledFuture
     * @throws IllegalStateException 如果调度执行器服务未初始化
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException("scheduled executor service not initialized");
        return scheduledExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    /**
     * 返回对象的字符串表示。
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    /**
     * 将设备信息追加到字符串构建器中。
     *
     * @param sb     要追加信息的字符串构建器
     * @param indent 缩进字符串
     * @return 追加信息后的字符串构建器
     */
    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "Device[name: ", deviceName);
        Builder.appendLine(sb, indent2, "desc: ", description);
        Builder.appendLine(sb, indent2, "installed: ", installed);
        for (Connection conn : conns)
            conn.promptTo(sb, indent2).append(Builder.LINE_SEPARATOR);
        for (ApplicationEntity ae : aes.values())
            ae.promptTo(sb, indent2).append(Builder.LINE_SEPARATOR);
        return sb.append(indent).append(']');
    }

    /**
     * 重新配置设备。 从另一个设备复制配置信息，包括设备属性、连接、应用程序实体、Web应用程序、Keycloak客户端和设备扩展。
     *
     * @param from 源设备，从中复制配置信息
     * @throws IOException              如果重新配置过程中发生I/O异常
     * @throws GeneralSecurityException 如果重新配置过程中发生安全异常
     */
    public void reconfigure(Device from) throws IOException, GeneralSecurityException {
        setDeviceAttributes(from);
        reconfigureConnections(from);
        reconfigureApplicationEntities(from);
        reconfigureWebApplications(from);
        reconfigureKeycloakClients(from);
        reconfigureDeviceExtensions(from);
    }

    /**
     * 从另一个设备复制设备属性。 包括描述、设备UID、制造商、软件版本、安全设置等所有基本属性。
     *
     * @param from 源设备，从中复制属性
     */
    protected void setDeviceAttributes(Device from) {
        setDescription(from.description);
        setDeviceUID(from.deviceUID);
        setManufacturer(from.manufacturer);
        setManufacturerModelName(from.manufacturerModelName);
        setSoftwareVersions(from.softwareVersions);
        setStationName(from.stationName);
        setDeviceSerialNumber(from.deviceSerialNumber);
        setTrustStoreURL(from.trustStoreURL);
        setTrustStoreType(from.trustStoreType);
        setTrustStorePin(from.trustStorePin);
        setKeyStoreURL(from.keyStoreURL);
        setKeyStoreType(from.keyStoreType);
        setKeyStorePin(from.keyStorePin);
        setKeyStoreKeyPin(from.keyStoreKeyPin);
        setTimeZoneOfDevice(from.timeZoneOfDevice);
        setIssuerOfPatientID(from.issuerOfPatientID);
        setIssuerOfAccessionNumber(from.issuerOfAccessionNumber);
        setOrderPlacerIdentifier(from.orderPlacerIdentifier);
        setOrderFillerIdentifier(from.orderFillerIdentifier);
        setIssuerOfAdmissionID(from.issuerOfAdmissionID);
        setIssuerOfServiceEpisodeID(from.issuerOfServiceEpisodeID);
        setIssuerOfContainerIdentifier(from.issuerOfContainerIdentifier);
        setIssuerOfSpecimenIdentifier(from.issuerOfSpecimenIdentifier);
        setInstitutionNames(from.institutionNames);
        setInstitutionCodes(from.institutionCodes);
        setInstitutionAddresses(from.institutionAddresses);
        setInstitutionalDepartmentNames(from.institutionalDepartmentNames);
        setPrimaryDeviceTypes(from.primaryDeviceTypes);
        setRelatedDeviceRefs(from.relatedDeviceRefs);
        setAuthorizedNodeCertificates(from.authorizedNodeCertificates);
        setThisNodeCertificates(from.thisNodeCertificates);
        setVendorData(from.vendorData);
        setLimitOpenAssociations(from.limitOpenAssociations);
        setInstalled(from.installed);
        setLimitAssociationsInitiatedBy(from.limitAssociationsInitiatedBy);
        setRoleSelectionNegotiationLenient(from.roleSelectionNegotiationLenient);
    }

    /**
     * 设置授权节点证书。 如果证书有更新，则重置信任管理器。
     *
     * @param from 包含授权节点证书的映射
     */
    private void setAuthorizedNodeCertificates(Map<String, X509Certificate[]> from) {
        if (update(authorizedNodeCertificates, from))
            setTrustManager(null);
    }

    /**
     * 设置此节点证书。
     *
     * @param from 包含此节点证书的映射
     */
    private void setThisNodeCertificates(Map<String, X509Certificate[]> from) {
        update(thisNodeCertificates, from);
    }

    /**
     * 更新目标证书映射。 首先保留目标映射中与源映射相同的键，然后更新或添加源映射中的所有证书。
     *
     * @param target 要更新的目标证书映射
     * @param from   源证书映射
     * @return 如果有任何更新则返回true，否则返回false
     */
    private boolean update(Map<String, X509Certificate[]> target, Map<String, X509Certificate[]> from) {
        boolean updated = target.keySet().retainAll(from.keySet());
        for (Entry<String, X509Certificate[]> e : from.entrySet()) {
            String key = e.getKey();
            X509Certificate[] value = e.getValue();
            X509Certificate[] certs = target.get(key);
            if (certs == null || !Arrays.equals(value, certs)) {
                target.put(key, value);
                updated = true;
            }
        }
        return updated;
    }

    /**
     * 重新配置连接。 移除源设备中不存在的连接，添加源设备中存在的新连接，并重新配置所有连接。
     *
     * @param from 源设备，从中复制连接配置
     */
    private void reconfigureConnections(Device from) {
        Iterator<Connection> connIter = conns.iterator();
        while (connIter.hasNext()) {
            Connection conn = connIter.next();
            if (from.connectionWithEqualsRDN(conn) == null) {
                connIter.remove();
                conn.setDevice(null);
                conn.unbind();
            }
        }
        for (Connection src : from.conns) {
            Connection conn = connectionWithEqualsRDN(src);
            if (conn == null)
                this.addConnection(conn = new Connection());
            conn.reconfigure(src);
        }
    }

    /**
     * 重新配置应用程序实体。 移除源设备中不存在的应用程序实体，添加源设备中存在的新应用程序实体，并重新配置所有应用程序实体。
     *
     * @param from 源设备，从中复制应用程序实体配置
     */
    private void reconfigureApplicationEntities(Device from) {
        aes.keySet().retainAll(from.aes.keySet());
        for (ApplicationEntity src : from.aes.values()) {
            ApplicationEntity ae = aes.get(src.getAETitle());
            if (ae == null)
                addApplicationEntity(ae = new ApplicationEntity(src.getAETitle()));
            ae.reconfigure(src);
        }
    }

    /**
     * 重新配置Web应用程序。 移除源设备中不存在的Web应用程序，添加源设备中存在的新Web应用程序，并重新配置所有Web应用程序。
     *
     * @param from 源设备，从中复制Web应用程序配置
     */
    private void reconfigureWebApplications(Device from) {
        webapps.keySet().retainAll(from.webapps.keySet());
        for (WebApplication src : from.webapps.values()) {
            WebApplication webapp = webapps.get(src.getApplicationName());
            if (webapp == null)
                addWebApplication(webapp = new WebApplication(src.getApplicationName()));
            webapp.reconfigure(src);
        }
    }

    /**
     * 重新配置Keycloak客户端。 移除源设备中不存在的Keycloak客户端，添加源设备中存在的新Keycloak客户端，并重新配置所有Keycloak客户端。
     *
     * @param from 源设备，从中复制Keycloak客户端配置
     */
    private void reconfigureKeycloakClients(Device from) {
        keycloakClients.keySet().retainAll(from.keycloakClients.keySet());
        for (KeycloakClient src : from.keycloakClients.values()) {
            KeycloakClient client = keycloakClients.get(src.getKeycloakClientID());
            if (client == null)
                addKeycloakClient(client = new KeycloakClient(src.getKeycloakClientID()));
            client.reconfigure(src);
        }
    }

    /**
     * 重新配置连接列表。 清空目标连接列表，然后添加源连接列表中具有相同RDN的连接。
     *
     * @param conns 要重新配置的目标连接列表
     * @param src   源连接列表
     */
    public void reconfigureConnections(List<Connection> conns, List<Connection> src) {
        conns.clear();
        for (Connection conn : src)
            conns.add(connectionWithEqualsRDN(conn));
    }

    /**
     * 重新配置设备扩展。 移除源设备中不存在的设备扩展，添加源设备中存在的新设备扩展，并重新配置所有设备扩展。
     *
     * @param from 源设备，从中复制设备扩展配置
     */
    private void reconfigureDeviceExtensions(Device from) {
        extensions.keySet().removeIf(aClass -> !from.extensions.containsKey(aClass));
        for (DeviceExtension src : from.extensions.values()) {
            Class<? extends DeviceExtension> clazz = src.getClass();
            DeviceExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addDeviceExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    /**
     * 列出所有设备扩展。
     *
     * @return 包含所有设备扩展的集合
     */
    public Collection<DeviceExtension> listDeviceExtensions() {
        return extensions.values();
    }

    /**
     * 获取指定类型的设备扩展。
     *
     * @param <T>   设备扩展的类型
     * @param clazz 设备扩展的类
     * @return 指定类型的设备扩展，如果不存在则返回null
     */
    public <T extends DeviceExtension> T getDeviceExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    /**
     * 获取指定类型的设备扩展，如果不存在则抛出异常。
     *
     * @param <T>   设备扩展的类型
     * @param clazz 设备扩展的类
     * @return 指定类型的设备扩展
     * @throws IllegalStateException 如果指定类型的设备扩展不存在
     */
    public <T extends DeviceExtension> T getDeviceExtensionNotNull(Class<T> clazz) {
        T devExt = getDeviceExtension(clazz);
        if (devExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for Device: " + deviceName);
        return devExt;
    }

}
