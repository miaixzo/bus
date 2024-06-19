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
package org.miaixz.bus.image;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.image.galaxy.Material;
import org.miaixz.bus.image.galaxy.data.Code;
import org.miaixz.bus.image.galaxy.data.Issuer;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.acquire.DeviceExtension;
import org.miaixz.bus.image.metric.internal.pdu.AAssociateRQ;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 设备信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Device implements Serializable {

    /**
     * AE可以发起的最大开放关联数
     */
    private final LinkedHashMap<String, Integer> limitAssociationsInitiatedBy = new LinkedHashMap<>();
    private final LinkedHashMap<String, X509Certificate[]> authorizedNodeCertificates = new LinkedHashMap<>();
    private final LinkedHashMap<String, X509Certificate[]> thisNodeCertificates = new LinkedHashMap<>();
    private final List<Connection> conns = new ArrayList<>();
    private final LinkedHashMap<String, ApplicationEntity> aes = new LinkedHashMap<>();
    private final LinkedHashMap<String, WebApplication> webapps = new LinkedHashMap<>();
    private final LinkedHashMap<String, KeycloakClient> keycloakClients = new LinkedHashMap<>();
    private final Map<Class<? extends DeviceExtension>, DeviceExtension> extensions = new HashMap<>();
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
    private String keyStoreKeyPin;
    private String keyStoreKeyPinProperty;
    private Issuer issuerOfPatientID;
    private Issuer issuerOfAccessionNumber;
    private Issuer orderPlacerIdentifier;
    private Issuer orderFillerIdentifier;
    private Issuer issuerOfAdmissionID;
    private Issuer issuerOfServiceEpisodeID;
    private Issuer issuerOfContainerIdentifier;
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
    private boolean roleSelectionNegotiationLenient;
    /**
     * 设备的时区
     */
    private TimeZone timeZoneOfDevice;
    private Boolean arcDevExt;
    private transient DimseRQHandler dimseRQHandler;
    private transient Monitoring monitoring;
    private transient AssociationMonitor associationMonitor;
    private transient Executor executor;
    private transient ScheduledExecutorService scheduledExecutor;
    private transient volatile SSLContext sslContext;
    private transient volatile KeyManager km;
    private transient volatile TrustManager tm;
    private transient AssociationHandler associationHandler = new AssociationHandler();

    public Device() {

    }

    public Device(String name) {
        setDeviceName(name);
    }

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

    private void checkNotEmpty(String name, String val) {
        if (null != val && val.isEmpty())
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

    public String getDeviceUID() {
        return deviceUID;
    }

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
     * 设置该设备的制造商
     * 这应该与该设备创建的SOP实例中的制造商(0008,0070)的值相同
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
     * 设置此设备的制造商型号名称
     * 这应该与该设备创建的SOP实例中的制造商型号名称(0008,1090)的值相同
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
     * 设置在该设备上运行(或由该设备实现)的软件版本
     * 这应该与该设备创建的SOP实例中的软件版本(0018、1020)的值相同
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
     * 设置属于此设备的工作站名称
     * 这应该与此设备创建的SOP实例中的站名(0008,1010)的值相同
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
     * 设置此设备的序列号
     * 这应该与该设备创建的SOP实例中的设备序列号(0018,1000)的值相同
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
     * 设置与此设备关联的类型编解码器
     * 表示一种设备，最适用于采集方式。如果适用，类型应该从PS3.16中上下文ID 30的内部值(0008,0100)列表中选择
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
     * 设置与此设备关联的机构名称;可能是它所驻留或代表的站点吗
     * 是否应该与该设备创建的SOP实例中的机构名称(0008,0080)相同
     *
     * @param names 包含机构名称值的字符串数组
     */
    public void setInstitutionNames(String... names) {
        institutionNames = names;
    }

    public final Code[] getInstitutionCodes() {
        return institutionCodes;
    }

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
     * 获取操作该设备的机构的地址
     * 是否与该设备创建的SOP实例中的机构地址(0008,0081)属性值相同
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
     * 设置与此设备关联的部门名称
     * 是否应该与该设备创建的SOP实例中的机构部门名称(0008,1040)的值相同
     *
     * @param names 包含部门名称值的字符串数组
     */
    public void setInstitutionalDepartmentNames(String... names) {
        institutionalDepartmentNames = names;
    }

    public final Issuer getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public final void setIssuerOfPatientID(Issuer issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public final Issuer getIssuerOfAccessionNumber() {
        return issuerOfAccessionNumber;
    }

    public final void setIssuerOfAccessionNumber(Issuer issuerOfAccessionNumber) {
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
    }

    public final Issuer getOrderPlacerIdentifier() {
        return orderPlacerIdentifier;
    }

    public final void setOrderPlacerIdentifier(Issuer orderPlacerIdentifier) {
        this.orderPlacerIdentifier = orderPlacerIdentifier;
    }

    public final Issuer getOrderFillerIdentifier() {
        return orderFillerIdentifier;
    }

    public final void setOrderFillerIdentifier(Issuer orderFillerIdentifier) {
        this.orderFillerIdentifier = orderFillerIdentifier;
    }

    public final Issuer getIssuerOfAdmissionID() {
        return issuerOfAdmissionID;
    }

    public final void setIssuerOfAdmissionID(Issuer issuerOfAdmissionID) {
        this.issuerOfAdmissionID = issuerOfAdmissionID;
    }

    public final Issuer getIssuerOfServiceEpisodeID() {
        return issuerOfServiceEpisodeID;
    }

    public final void setIssuerOfServiceEpisodeID(Issuer issuerOfServiceEpisodeID) {
        this.issuerOfServiceEpisodeID = issuerOfServiceEpisodeID;
    }

    public final Issuer getIssuerOfContainerIdentifier() {
        return issuerOfContainerIdentifier;
    }

    public final void setIssuerOfContainerIdentifier(Issuer issuerOfContainerIdentifier) {
        this.issuerOfContainerIdentifier = issuerOfContainerIdentifier;
    }

    public final Issuer getIssuerOfSpecimenIdentifier() {
        return issuerOfSpecimenIdentifier;
    }

    public final void setIssuerOfSpecimenIdentifier(Issuer issuerOfSpecimenIdentifier) {
        this.issuerOfSpecimenIdentifier = issuerOfSpecimenIdentifier;
    }

    public X509Certificate[] getAuthorizedNodeCertificates(String ref) {
        return authorizedNodeCertificates.get(ref);
    }

    public void setAuthorizedNodeCertificates(String ref, X509Certificate... certs) {
        authorizedNodeCertificates.put(ref, certs);
        setTrustManager(null);
    }

    public X509Certificate[] removeAuthorizedNodeCertificates(String ref) {
        X509Certificate[] certs = authorizedNodeCertificates.remove(ref);
        setTrustManager(null);
        return certs;
    }

    public void removeAllAuthorizedNodeCertificates() {
        authorizedNodeCertificates.clear();
        setTrustManager(null);
    }

    public X509Certificate[] getAllAuthorizedNodeCertificates() {
        return toArray(authorizedNodeCertificates.values());
    }

    public String[] getAuthorizedNodeCertificateRefs() {
        return authorizedNodeCertificates.keySet().toArray(Normal.EMPTY_STRING_ARRAY);
    }

    public final String getTrustStoreURL() {
        return trustStoreURL;
    }

    public final void setTrustStoreURL(String trustStoreURL) {
        checkNotEmpty("trustStoreURL", trustStoreURL);
        if (null == trustStoreURL
                ? null == this.trustStoreURL
                : trustStoreURL.equals(this.trustStoreURL))
            return;

        this.trustStoreURL = trustStoreURL;
        setTrustManager(null);
    }

    public final String getTrustStoreType() {
        return trustStoreType;
    }

    public final void setTrustStoreType(String trustStoreType) {
        checkNotEmpty("trustStoreType", trustStoreType);
        this.trustStoreType = trustStoreType;
    }

    public final String getTrustStorePin() {
        return trustStorePin;
    }

    public final void setTrustStorePin(String trustStorePin) {
        checkNotEmpty("trustStorePin", trustStorePin);
        this.trustStorePin = trustStorePin;
    }

    public final String getTrustStorePinProperty() {
        return trustStorePinProperty;
    }

    public final void setTrustStorePinProperty(String trustStorePinProperty) {
        checkNotEmpty("keyPin", keyStoreKeyPin);
        this.trustStorePinProperty = trustStorePinProperty;
    }

    public X509Certificate[] getThisNodeCertificates(String ref) {
        return thisNodeCertificates.get(ref);
    }

    public void setThisNodeCertificates(String ref, X509Certificate... certs) {
        thisNodeCertificates.put(ref, certs);
    }

    public X509Certificate[] removeThisNodeCertificates(String ref) {
        return thisNodeCertificates.remove(ref);
    }

    public final String getKeyStoreURL() {
        return keyStoreURL;
    }

    public final void setKeyStoreURL(String keyStoreURL) {
        checkNotEmpty("keyStoreURL", keyStoreURL);
        if (null == keyStoreURL
                ? null == this.keyStoreURL
                : keyStoreURL.equals(this.keyStoreURL))
            return;

        this.keyStoreURL = keyStoreURL;
        setKeyManager(null);
    }

    public final String getKeyStoreType() {
        return keyStoreType;
    }

    public final void setKeyStoreType(String keyStoreType) {
        checkNotEmpty("keyStoreType", keyStoreURL);
        this.keyStoreType = keyStoreType;
    }

    public final String getKeyStorePin() {
        return keyStorePin;
    }

    public final void setKeyStorePin(String keyStorePin) {
        checkNotEmpty("keyStorePin", keyStorePin);
        this.keyStorePin = keyStorePin;
    }

    public final String getKeyStorePinProperty() {
        return keyStorePinProperty;
    }

    public final void setKeyStorePinProperty(String keyStorePinProperty) {
        checkNotEmpty("keyStorePinProperty", keyStorePinProperty);
        this.keyStorePinProperty = keyStorePinProperty;
    }

    public final String getKeyStoreKeyPin() {
        return keyStoreKeyPin;
    }

    public final void setKeyStoreKeyPin(String keyStorePin) {
        checkNotEmpty("keyStoreKeyPin", keyStorePin);
        this.keyStoreKeyPin = keyStorePin;
    }

    public final String getKeyStoreKeyPinProperty() {
        return keyStoreKeyPinProperty;
    }

    public final void setKeyStoreKeyPinProperty(String keyStoreKeyPinProperty) {
        checkNotEmpty("keyStoreKeyPinProperty", keyStoreKeyPinProperty);
        this.keyStoreKeyPinProperty = keyStoreKeyPinProperty;
    }

    public void removeAllThisNodeCertificates() {
        thisNodeCertificates.clear();
    }

    public X509Certificate[] getAllThisNodeCertificates() {
        return toArray(thisNodeCertificates.values());
    }

    public String[] getThisNodeCertificateRefs() {
        return thisNodeCertificates.keySet().toArray(Normal.EMPTY_STRING_ARRAY);
    }

    public final String[] getRelatedDeviceRefs() {
        return relatedDeviceRefs;
    }

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
     * 获取一个布尔值，指示此设备当前是否安装在网络上(这对于预配置、移动货车和类似情况非常有用)
     *
     * @return 一个布尔值，如果安装了这个设备，它将为真
     */
    public final boolean isInstalled() {
        return installed;
    }

    /**
     * 设置一个布尔值，指示此设备当前是否安装在网络上(这对于预配置、移动货车和类似情况非常有用)
     *
     * @param installed 一个布尔值，如果安装了这个设备，它将为真
     */
    public final void setInstalled(boolean installed) {
        if (this.installed == installed)
            return;

        this.installed = installed;
        needRebindConnections();
    }

    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    public void setRoleSelectionNegotiationLenient(boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    public TimeZone getTimeZoneOfDevice() {
        return timeZoneOfDevice;
    }

    public void setTimeZoneOfDevice(TimeZone timeZoneOfDevice) {
        this.timeZoneOfDevice = timeZoneOfDevice;
    }

    public final DimseRQHandler getDimseRQHandler() {
        return dimseRQHandler;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    public final AssociationHandler getAssociationHandler() {
        return associationHandler;
    }

    public void setAssociationHandler(AssociationHandler associationHandler) {
        if (null == associationHandler)
            throw new NullPointerException();
        this.associationHandler = associationHandler;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    public AssociationMonitor getAssociationMonitor() {
        return associationMonitor;
    }

    public void setAssociationMonitor(AssociationMonitor associationMonitor) {
        this.associationMonitor = associationMonitor;
    }

    public void bindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            con.bind();
    }

    public void rebindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            if (con.isRebindNeeded())
                con.rebind();
    }

    private void needRebindConnections() {
        for (Connection con : conns)
            con.needRebind();
    }

    private void needReconfigureTLS() {
        for (Connection con : conns)
            if (con.isTls())
                con.needRebind();
        sslContext = null;
    }

    public void unbindConnections() {
        for (Connection con : conns)
            con.unbind();
    }

    public final Executor getExecutor() {
        return executor;
    }

    public final void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public final ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public final void setScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = executor;
    }

    public void addConnection(Connection conn) {
        conn.setDevice(this);
        conns.add(conn);
        conn.needRebind();
    }

    public boolean removeConnection(Connection conn) {
        for (ApplicationEntity ae : aes.values())
            if (ae.getConnections().contains(conn))
                throw new IllegalStateException(conn + " used by AE: " +
                        ae.getAETitle());

        for (DeviceExtension ext : extensions.values())
            ext.verifyNotUsed(conn);

        if (!conns.remove(conn))
            return false;

        conn.setDevice(null);
        conn.unbind();
        return true;
    }

    public List<Connection> listConnections() {
        return Collections.unmodifiableList(conns);
    }

    public Connection connectionWithEqualsRDN(Connection other) {
        for (Connection conn : conns)
            if (conn.equalsRDN(other))
                return conn;

        return null;
    }

    public void addApplicationEntity(ApplicationEntity ae) {
        ae.setDevice(this);
        aes.put(ae.getAETitle(), ae);
    }

    public ApplicationEntity removeApplicationEntity(ApplicationEntity ae) {
        return removeApplicationEntity(ae.getAETitle());
    }

    public ApplicationEntity removeApplicationEntity(String aet) {
        ApplicationEntity ae = aes.remove(aet);
        if (null != ae)
            ae.setDevice(null);
        return ae;
    }

    public Collection<String> getWebApplicationNames() {
        return webapps.keySet();
    }

    public Collection<WebApplication> getWebApplications() {
        return webapps.values();
    }

    public Collection<WebApplication> getWebApplicationsWithServiceClass(WebApplication.ServiceClass serviceClass) {
        Collection<WebApplication> result = new ArrayList<>(webapps.size());
        for (WebApplication webapp : webapps.values()) {
            if (webapp.containsServiceClass(serviceClass))
                result.add(webapp);
        }
        return result;
    }

    public WebApplication getWebApplication(String name) {
        return webapps.get(name);
    }

    public void addWebApplication(WebApplication webapp) {
        webapp.setDevice(this);
        webapps.put(webapp.getApplicationName(), webapp);
    }

    public WebApplication removeWebApplication(WebApplication webapp) {
        return removeWebApplication(webapp.getApplicationName());
    }

    public WebApplication removeWebApplication(String name) {
        WebApplication webapp = webapps.remove(name);
        if (null != webapp)
            webapp.setDevice(null);
        return webapp;
    }

    public Collection<String> getKeycloakClientIDs() {
        return keycloakClients.keySet();
    }

    public Collection<KeycloakClient> getKeycloakClients() {
        return keycloakClients.values();
    }

    public KeycloakClient getKeycloakClient(String clientID) {
        return keycloakClients.get(clientID);
    }

    public void addKeycloakClient(KeycloakClient client) {
        client.setDevice(this);
        keycloakClients.put(client.getKeycloakClientID(), client);
    }

    public KeycloakClient removeKeycloakClient(KeycloakClient client) {
        return removeKeycloakClient(client.getKeycloakClientID());
    }

    public KeycloakClient removeKeycloakClient(String name) {
        KeycloakClient client = keycloakClients.remove(name);
        if (null != client)
            client.setDevice(null);
        return client;
    }

    public void addDeviceExtension(DeviceExtension ext) {
        Class<? extends DeviceExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException(
                    "already contains Device Extension:" + clazz);

        ext.setDevice(this);
        extensions.put(clazz, ext);
    }

    public boolean removeDeviceExtension(DeviceExtension ext) {
        if (null == extensions.remove(ext.getClass()))
            return false;

        ext.setDevice(null);
        return true;
    }

    public final int getLimitOpenAssociations() {
        return limitOpenAssociations;
    }

    public final void setLimitOpenAssociations(int limit) {
        if (limit < 0)
            throw new IllegalArgumentException("limit: " + limit);

        this.limitOpenAssociations = limit;
    }

    /**
     * 返回指定的远程AE可以发起的最大开放关联数。如果超过了这个限制，那么来自AE的进一步关联请求将被拒绝
     * Result = 2 - rejected-transient，
     * Source = 1 - DICOM UL service-user，
     * Reason = 2 - local-limit-exceeded
     *
     * @param callingAET 远程AE的AE名称
     * @return 开放关联的最大数目或无限制为0
     * @throws NullPointerException 如果callingAET为空
     * @see #setLimitAssociationsInitiatedBy(String, int)
     */
    public int getLimitAssociationsInitiatedBy(String callingAET) {
        Integer value = limitAssociationsInitiatedBy.get(Objects.requireNonNull(callingAET));
        return null != value ? value.intValue() : 0;
    }

    /**
     * 返回指定的远程AE可以发起的最大开放关联数。如果超过了这个限制，那么来自AE的进一步关联请求将被拒绝
     * Result = 2 - rejected-transient，
     * Source = 1 - DICOM UL service-user，
     * Reason = 2 - local-limit-exceeded
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

    public String[] getLimitAssociationsInitiatedBy() {
        String[] ss = new String[limitAssociationsInitiatedBy.size()];
        int i = 0;
        for (Entry<String, Integer> entry : limitAssociationsInitiatedBy.entrySet()) {
            ss[i++] = entry.getKey() + Symbol.C_EQUAL + entry.getValue();
        }
        return ss;
    }

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

    private void setLimitAssociationsInitiatedBy(Map<String, Integer> tmp) {
        limitAssociationsInitiatedBy.clear();
        limitAssociationsInitiatedBy.putAll(tmp);
    }

    public void addAssociation(Association as) {
        synchronized (associations) {
            associations.add(as);
        }
    }

    public void removeAssociation(Association as) {
        synchronized (associations) {
            associations.remove(as);
            associations.notifyAll();
        }
    }

    public Association[] listOpenAssociations() {
        synchronized (associations) {
            return associations.toArray(new Association[associations.size()]);
        }
    }

    public int getNumberOfOpenAssociations() {
        return associations.size();
    }

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

    public void waitForNoOpenConnections() throws InterruptedException {
        synchronized (associations) {
            while (!associations.isEmpty())
                associations.wait();
        }
    }

    public boolean isLimitOfAssociationsExceeded(AAssociateRQ rq) {
        Integer limit;
        return limitOpenAssociations > 0 && associations.size() > limitOpenAssociations
                || null != (limit = limitAssociationsInitiatedBy.get(rq.getCallingAET()))
                && getNumberOfAssociationsInitiatedBy(rq.getCallingAET()) > limit;
    }

    public ApplicationEntity getApplicationEntity(String aet) {
        return aes.get(aet);
    }

    public ApplicationEntity getApplicationEntity(String aet, boolean matchOtherAETs) {
        ApplicationEntity ae = aes.get(aet);
        if (null == ae)
            ae = aes.get(Symbol.STAR);
        if (null == ae && matchOtherAETs)
            for (ApplicationEntity ae1 : getApplicationEntities())
                if (ae1.isOtherAETitle(aet))
                    return ae1;
        return ae;
    }

    public Collection<String> getApplicationAETitles() {
        return aes.keySet();
    }

    public Collection<ApplicationEntity> getApplicationEntities() {
        return aes.values();
    }

    public final KeyManager getKeyManager() {
        return km;
    }

    public final void setKeyManager(KeyManager km) {
        this.km = km;
        needReconfigureTLS();
    }

    private KeyManager km() throws GeneralSecurityException, IOException {
        KeyManager ret = km;
        if (null != ret || null == keyStoreURL)
            return ret;
        String keyStorePin = keyStorePin();
        km = ret = SSLManagerFactory.createKeyManager(
                Material.replaceSystemProperties(keyStoreType()),
                Material.replaceSystemProperties(keyStoreURL),
                Material.replaceSystemProperties(keyStorePin()),
                Material.replaceSystemProperties(keyPin(keyStorePin)));
        return ret;
    }

    private String keyStoreType() {
        if (null == keyStoreType)
            throw new IllegalStateException("keyStoreURL requires keyStoreType");

        return keyStoreType;
    }

    private String keyStorePin() {
        if (null != keyStorePin)
            return keyStorePin;

        if (null == keyStorePinProperty)
            throw new IllegalStateException(
                    "keyStoreURL requires keyStorePin or keyStorePinProperty");

        String pin = System.getProperty(keyStorePinProperty);
        if (null == pin)
            throw new IllegalStateException(
                    "No such keyStorePinProperty: " + keyStorePinProperty);

        return pin;
    }

    private String keyPin(String keyStorePin) {
        if (null != keyStoreKeyPin)
            return keyStoreKeyPin;

        if (null == keyStoreKeyPinProperty)
            return keyStorePin;

        String pin = System.getProperty(keyStoreKeyPinProperty);
        if (null == pin)
            throw new IllegalStateException(
                    "No such keyPinProperty: " + keyStoreKeyPinProperty);

        return pin;
    }

    public final TrustManager getTrustManager() {
        return tm;
    }

    public final void setTrustManager(TrustManager tm) {
        this.tm = tm;
        needReconfigureTLS();
    }

    private TrustManager tm() throws GeneralSecurityException, IOException {
        TrustManager ret = tm;
        if (null != ret
                || null == trustStoreURL && authorizedNodeCertificates.isEmpty())
            return ret;

        tm = ret = null != trustStoreURL
                ? SSLManagerFactory.createTrustManager(
                Material.replaceSystemProperties(trustStoreType()),
                Material.replaceSystemProperties(trustStoreURL),
                Material.replaceSystemProperties(trustStorePin()))
                : SSLManagerFactory.createTrustManager(
                getAllAuthorizedNodeCertificates());
        return ret;
    }

    private String trustStoreType() {
        if (null == trustStoreType)
            throw new IllegalStateException("trustStoreURL requires trustStoreType");

        return trustStoreType;
    }

    private String trustStorePin() {
        if (null != trustStorePin)
            return trustStorePin;

        if (null == trustStorePinProperty)
            throw new IllegalStateException(
                    "trustStoreURL requires trustStorePin or trustStorePinProperty");

        String pin = System.getProperty(trustStorePinProperty);
        if (null == pin)
            throw new IllegalStateException(
                    "No such trustStorePinProperty: " + trustStorePinProperty);

        return pin;
    }

    public SSLContext sslContext() throws GeneralSecurityException, IOException {
        SSLContext ctx = sslContext;
        if (null != ctx)
            return ctx;

        ctx = SSLContext.getInstance(HTTP.TLS);
        ctx.init(keyManagers(), trustManagers(), null);
        sslContext = ctx;
        return ctx;
    }

    public KeyManager[] keyManagers() throws GeneralSecurityException, IOException {
        KeyManager tmp = km();
        return null != tmp ? new KeyManager[]{tmp} : null;
    }

    public TrustManager[] trustManagers() throws GeneralSecurityException, IOException {
        TrustManager tmp = tm();
        return null != tmp ? new TrustManager[]{tmp} : null;
    }

    public void execute(Runnable command) {
        if (null == executor)
            throw new IllegalStateException("executer not initalized");

        executor.execute(command);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay,
                                       TimeUnit unit) {
        if (null == scheduledExecutor)
            throw new IllegalStateException(
                    "scheduled executor service not initalized");

        return scheduledExecutor.schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay, long period, TimeUnit unit) {
        if (null == scheduledExecutor)
            throw new IllegalStateException(
                    "scheduled executor service not initalized");

        return scheduledExecutor.scheduleAtFixedRate(command,
                initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay, long delay, TimeUnit unit) {
        if (null == scheduledExecutor)
            throw new IllegalStateException(
                    "scheduled executor service not initalized");

        return scheduledExecutor.scheduleWithFixedDelay(command,
                initialDelay, delay, unit);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Material.appendLine(sb, indent, "Device[name: ", deviceName);
        Material.appendLine(sb, indent2, "desc: ", description);
        Material.appendLine(sb, indent2, "installed: ", installed);
        for (Connection conn : conns)
            conn.promptTo(sb, indent2).append(Material.LINE_SEPARATOR);
        for (ApplicationEntity ae : aes.values())
            ae.promptTo(sb, indent2).append(Material.LINE_SEPARATOR);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    public void reconfigure(Device from) {
        setDeviceAttributes(from);
        reconfigureConnections(from);
        reconfigureApplicationEntities(from);
        reconfigureWebApplications(from);
        reconfigureKeycloakClients(from);
        reconfigureDeviceExtensions(from);
    }

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

    private void setAuthorizedNodeCertificates(Map<String, X509Certificate[]> from) {
        if (update(authorizedNodeCertificates, from))
            setTrustManager(null);
    }

    private void setThisNodeCertificates(Map<String, X509Certificate[]> from) {
        update(thisNodeCertificates, from);
    }

    private boolean update(Map<String, X509Certificate[]> target,
                           Map<String, X509Certificate[]> from) {
        boolean updated = target.keySet().retainAll(from.keySet());
        for (Entry<String, X509Certificate[]> e : from.entrySet()) {
            String key = e.getKey();
            X509Certificate[] value = e.getValue();
            X509Certificate[] certs = target.get(key);
            if (null == certs || !Arrays.equals(value, certs)) {
                target.put(key, value);
                updated = true;
            }
        }
        return updated;
    }

    private void reconfigureConnections(Device from) {
        Iterator<Connection> connIter = conns.iterator();
        while (connIter.hasNext()) {
            Connection conn = connIter.next();
            if (null == from.connectionWithEqualsRDN(conn)) {
                connIter.remove();
                conn.setDevice(null);
                conn.unbind();
            }
        }
        for (Connection src : from.conns) {
            Connection conn = connectionWithEqualsRDN(src);
            if (null == conn)
                this.addConnection(conn = new Connection());
            conn.reconfigure(src);
        }
    }

    private void reconfigureApplicationEntities(Device from) {
        aes.keySet().retainAll(from.aes.keySet());
        for (ApplicationEntity src : from.aes.values()) {
            ApplicationEntity ae = aes.get(src.getAETitle());
            if (null == ae)
                addApplicationEntity(ae = new ApplicationEntity(src.getAETitle()));
            ae.reconfigure(src);
        }
    }

    private void reconfigureWebApplications(Device from) {
        webapps.keySet().retainAll(from.webapps.keySet());
        for (WebApplication src : from.webapps.values()) {
            WebApplication webapp = webapps.get(src.getApplicationName());
            if (null == webapp)
                addWebApplication(webapp = new WebApplication(src.getApplicationName()));
            webapp.reconfigure(src);
        }
    }

    private void reconfigureKeycloakClients(Device from) {
        keycloakClients.keySet().retainAll(from.keycloakClients.keySet());
        for (KeycloakClient src : from.keycloakClients.values()) {
            KeycloakClient client = keycloakClients.get(src.getKeycloakClientID());
            if (null == client)
                addKeycloakClient(client = new KeycloakClient(src.getKeycloakClientID()));
            client.reconfigure(src);
        }
    }

    public void reconfigureConnections(List<Connection> conns,
                                       List<Connection> src) {
        conns.clear();
        for (Connection conn : src)
            conns.add(connectionWithEqualsRDN(conn));
    }

    private void reconfigureDeviceExtensions(Device from) {
        for (Iterator<Class<? extends DeviceExtension>> it =
             extensions.keySet().iterator(); it.hasNext(); ) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (DeviceExtension src : from.extensions.values()) {
            Class<? extends DeviceExtension> clazz = src.getClass();
            DeviceExtension ext = extensions.get(clazz);
            if (null == ext)
                try {
                    addDeviceExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    public Collection<DeviceExtension> listDeviceExtensions() {
        return extensions.values();
    }

    public <T extends DeviceExtension> T getDeviceExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    public <T extends DeviceExtension> T getDeviceExtensionNotNull(Class<T> clazz) {
        T devExt = getDeviceExtension(clazz);
        if (null == devExt)
            throw new IllegalStateException("No " + clazz.getName()
                    + " configured for Device: " + deviceName);
        return devExt;
    }

    public Boolean getArcDevExt() {
        return arcDevExt;
    }

    public void setArcDevExt(Boolean arcDevExt) {
        this.arcDevExt = arcDevExt;
    }

}
