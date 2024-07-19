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
package org.miaixz.bus.image.builtin.ldap;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.galaxy.io.BasicBulkDataDescriptor;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.api.AttributeCoercion;
import org.miaixz.bus.image.metric.api.AttributeCoercions;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.metric.api.DicomConfiguration;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.ApplicationEntityInfo;
import org.miaixz.bus.image.metric.net.KeycloakClient;
import org.miaixz.bus.logger.Logger;

import javax.naming.*;
import javax.naming.directory.*;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LdapDicomConfiguration implements DicomConfiguration {

    static final String[] AE_ATTRS = {
            "dicomDeviceName",
            "dicomAETitle",
            "dcmOtherAETitle",
            "dicomDescription",
            "dicomAssociationInitiator",
            "dicomAssociationAcceptor",
            "dicomApplicationCluster",
            "dicomInstalled",
            "hl7ApplicationName",
            "dicomNetworkConnectionReference"
    };
    static final String[] WEBAPP_ATTRS = {
            "dicomDeviceName",
            "dcmWebAppName",
            "dcmWebServicePath",
            "dcmWebServiceClass",
            "dcmKeycloakClientID",
            "dicomAETitle",
            "dicomDescription",
            "dicomApplicationCluster",
            "dcmProperty",
            "dicomInstalled",
            "dicomNetworkConnectionReference"
    };
    private static final String CN_UNIQUE_AE_TITLES_REGISTRY = "cn=Unique AE Titles Registry,";
    private static final String CN_UNIQUE_WEB_APP_NAMES_REGISTRY = "cn=Unique Web Application Names Registry,";
    private static final String CN_DEVICES = "cn=Devices,";
    private static final String DICOM_CONFIGURATION = "DICOM Configuration";
    private static final String DICOM_CONFIGURATION_ROOT = "dicomConfigurationRoot";
    private static final String PKI_USER = "pkiUser";
    private static final String USER_CERTIFICATE_BINARY = "userCertificate;binary";
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};
    private final ReconnectDirContext ctx;
    private final String baseDN;
    private final List<LdapDicomConfigurationExtension> extensions = new ArrayList<>();
    /**
     * Needed for avoiding infinite loops when dealing with extensions containing circular references
     * e.g., one device extension references another device which has an extension that references the former device.
     * Devices that have been created but not fully loaded are added to this threadlocal. See loadDevice.
     */
    private final ThreadLocal<Map<String, Device>> currentlyLoadedDevicesLocal = new ThreadLocal<>();
    private String configurationDN;
    private String devicesDN;
    private String aetsRegistryDN;
    private String webAppsRegistryDN;
    private String configurationCN = DICOM_CONFIGURATION;
    private String configurationRoot = DICOM_CONFIGURATION_ROOT;
    private String pkiUser = PKI_USER;
    private String userCertificate = USER_CERTIFICATE_BINARY;
    private boolean extended = true;

    public LdapDicomConfiguration() throws InternalException {
        this(ResourceManager.getInitialEnvironment());
    }


    public LdapDicomConfiguration(Hashtable<?, ?> env)
            throws InternalException {
        Hashtable<String, String> map = new Hashtable();
        for (Map.Entry<?, ?> entry : env.entrySet())
            map.put((String) entry.getKey(),
                    Builder.replaceSystemProperties((String) entry.getValue()));

        try {
            // split baseDN from LDAP URL
            String s = map.get(Context.PROVIDER_URL);
            int end = s.lastIndexOf('/');
            map.put(Context.PROVIDER_URL, s.substring(0, end));
            this.baseDN = s.substring(end + 1);
            this.ctx = new ReconnectDirContext(map);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    private static <T> void appendFilter(String attrid, T value, StringBuilder sb) {
        if (value == null)
            return;

        sb.append('(').append(attrid).append('=').append(LdapBuilder.toString(value)).append(')');
    }

    private static <T> void appendFilter(String attrid, T[] values, StringBuilder sb) {
        if (values.length == 0)
            return;

        if (values.length == 1) {
            appendFilter(attrid, values[0], sb);
            return;
        }

        sb.append("(|");
        for (T value : values)
            appendFilter(attrid, value, sb);
        sb.append(")");
    }

    private static EnumSet<QueryOption> toQueryOptions(Attributes attrs)
            throws NamingException {
        Attribute relational = attrs.get("dcmRelationalQueries");
        Attribute datetime = attrs.get("dcmCombinedDateTimeMatching");
        Attribute fuzzy = attrs.get("dcmFuzzySemanticMatching");
        Attribute timezone = attrs.get("dcmTimezoneQueryAdjustment");
        if (relational == null && datetime == null && fuzzy == null && timezone == null)
            return null;
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (LdapBuilder.booleanValue(relational, false))
            opts.add(QueryOption.RELATIONAL);
        if (LdapBuilder.booleanValue(datetime, false))
            opts.add(QueryOption.DATETIME);
        if (LdapBuilder.booleanValue(fuzzy, false))
            opts.add(QueryOption.FUZZY);
        if (LdapBuilder.booleanValue(timezone, false))
            opts.add(QueryOption.TIMEZONE);
        return opts;
    }

    private static StorageOptions toStorageOptions(Attributes attrs) throws NamingException {
        Attribute levelOfSupport = attrs.get("dcmStorageConformance");
        Attribute signatureSupport = attrs.get("dcmDigitalSignatureSupport");
        Attribute coercion = attrs.get("dcmDataElementCoercion");
        if (levelOfSupport == null && signatureSupport == null && coercion == null)
            return null;
        StorageOptions opts = new StorageOptions();
        opts.setLevelOfSupport(
                StorageOptions.LevelOfSupport.valueOf(LdapBuilder.intValue(levelOfSupport, 3)));
        opts.setDigitalSignatureSupport(
                StorageOptions.DigitalSignatureSupport.valueOf(LdapBuilder.intValue(signatureSupport, 0)));
        opts.setElementCoercion(
                StorageOptions.ElementCoercion.valueOf(LdapBuilder.intValue(coercion, 2)));
        return opts;
    }

    private static byte[][] byteArrays(Attribute attr) throws NamingException {
        if (attr == null)
            return new byte[0][];

        byte[][] bb = new byte[attr.size()][];
        for (int i = 0; i < bb.length; i++)
            bb[i] = (byte[]) attr.get(i);

        return bb;
    }

    private static TransferCapability findByDN(String aeDN,
                                               Collection<TransferCapability> tcs, String dn) {
        for (TransferCapability tc : tcs)
            if (dn.equals(dnOf(tc, aeDN)))
                return tc;
        return null;
    }

    private static void storeDiff(ConfigurationChanges.ModifiedObject ldapObj, List<ModificationItem> mods, String attrId,
                                  byte[][] prevs, byte[][] vals) {
        if (!equals(prevs, vals)) {
            mods.add((vals.length == 0)
                    ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    new BasicAttribute(attrId))
                    : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    attr(attrId, vals)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (byte[] val : vals)
                    attribute.addValue(val.length + " bytes");
                for (byte[] prev : prevs)
                    attribute.removeValue(prev.length + " bytes");
                ldapObj.add(attribute);
            }
        }
    }

    private static boolean equals(byte[][] a, byte[][] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;

        outer:
        for (byte[] o1 : a) {
            for (byte[] o2 : a2)
                if (Arrays.equals(o1, o2))
                    continue outer;
            return false;
        }
        return true;
    }

    private static String aetDN(String aet, String parentDN) {
        return LdapBuilder.dnOf("dicomAETitle", aet, parentDN);
    }

    private static String webAppDN(String webAppName, String parentDN) {
        return LdapBuilder.dnOf("dcmWebAppName", webAppName, parentDN);
    }

    private static String keycloakClientDN(String keycloakClientID, String parentDN) {
        return LdapBuilder.dnOf("dcmKeycloakClientID", keycloakClientID, parentDN);
    }

    private static String dnOf(TransferCapability tc, String aeDN) {
        String cn = tc.getCommonName();
        return (cn != null)
                ? LdapBuilder.dnOf("cn", cn, aeDN)
                : LdapBuilder.dnOf("dicomSOPClass", tc.getSopClass(),
                "dicomTransferRole", tc.getRole().toString(), aeDN);
    }

    private static void storeNotEmpty(ConfigurationChanges.ModifiedObject ldapObj, Attributes attrs, String attrID, byte[]... vals) {
        if (vals != null && vals.length > 0) {
            attrs.put(attr(attrID, vals));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (byte[] val : vals)
                    attribute.addValue(val.length + " bytes");
                ldapObj.add(attribute);
            }
        }
    }

    private static Attribute attr(String attrID, byte[]... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (byte[] val : vals)
            attr.add(val);
        return attr;
    }

    private static Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj,
                                      BasicBulkDataDescriptor descriptor, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmBulkDataDescriptor");
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmBulkDataDescriptorID",
                descriptor.getBulkDataDescriptorID(), null);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmBulkDataExcludeDefaults",
                descriptor.isExcludeDefaults(), false);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmAttributeSelector", descriptor.getAttributeSelectors());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmBulkDataVRLengthThreshold",
                descriptor.getLengthsThresholdsAsStrings());
        return attrs;
    }

    private static Attributes storeTo(AttributeCoercion ac, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmAttributeCoercion");
        attrs.put("cn", ac.getCommonName());
        LdapBuilder.storeNotNullOrDef(attrs, "dcmDIMSE", ac.getDIMSE(), null);
        LdapBuilder.storeNotNullOrDef(attrs, "dicomTransferRole", ac.getRole(), null);
        LdapBuilder.storeNotEmpty(attrs, "dcmAETitle", ac.getAETitles());
        LdapBuilder.storeNotEmpty(attrs, "dcmSOPClass", ac.getSOPClasses());
        LdapBuilder.storeNotNullOrDef(attrs, "dcmURI", ac.getURI(), null);
        return attrs;
    }

    private static String toFilter(ApplicationEntityInfo keys) {
        if (keys == null)
            return "(objectclass=dicomNetworkAE)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=dicomNetworkAE)");
        if (keys.getAETitle() != null) {
            sb.append("(|");
            appendFilter("dicomAETitle", keys.getAETitle(), sb);
            appendFilter("dcmOtherAETitle", keys.getAETitle(), sb);
            sb.append(")");
        }
        appendFilter("dicomDescription", keys.getDescription(), sb);
        appendFilter("dicomAssociationInitiator", keys.getAssociationInitiator(), sb);
        appendFilter("dicomAssociationAcceptor", keys.getAssociationAcceptor(), sb);
        appendFilter("dicomApplicationCluster", keys.getApplicationClusters(), sb);
        sb.append(")");
        return sb.toString();
    }

    private static String toFilter(WebApplication keys) {
        if (keys == null)
            return "(objectclass=dcmWebApp)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=dcmWebApp)");
        appendFilter("dcmWebAppName", keys.getApplicationName(), sb);
        appendFilter("dicomDescription", keys.getDescription(), sb);
        appendFilter("dcmWebServicePath", keys.getServicePath(), sb);
        appendFilter("dcmWebServiceClass", keys.getServiceClasses(), sb);
        appendFilter("dicomAETitle", keys.getAETitle(), sb);
        appendFilter("dicomApplicationCluster", keys.getApplicationClusters(), sb);
        appendFilter("dcmKeycloakClientID", keys.getKeycloakClientID(), sb);
        sb.append(")");
        return sb.toString();
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public String getConfigurationCN() {
        return configurationCN;
    }

    public void setConfigurationCN(String configurationCN) {
        this.configurationCN = configurationCN;
    }

    public String getConfigurationRoot() {
        return configurationRoot;
    }

    public void setConfigurationRoot(String configurationRoot) {
        this.configurationRoot = configurationRoot;
    }

    public String getPkiUser() {
        return pkiUser;
    }

    public void setPkiUser(String pkiUser) {
        this.pkiUser = pkiUser;
    }

    public String getUserCertificate() {
        return userCertificate;
    }

    public void setUserCertificate(String userCertificate) {
        this.userCertificate = userCertificate;
    }

    public void addDicomConfigurationExtension(LdapDicomConfigurationExtension ext) {
        ext.setDicomConfiguration(this);
        extensions.add(ext);
    }

    public boolean removeDicomConfigurationExtension(
            LdapDicomConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        ext.setDicomConfiguration(null);
        return true;
    }


    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {
        for (LdapDicomConfigurationExtension ext : extensions) {
            if (clazz.isInstance(ext))
                return (T) ext;
        }
        return null;
    }

    @Override
    public synchronized void close() {
        ctx.close();
    }

    @Override
    public synchronized boolean configurationExists() throws InternalException {
        return configurationDN != null || findConfiguration();
    }

    public boolean exists(String dn) throws NamingException {
        try {
            ctx.getAttributes(dn);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public synchronized boolean purgeConfiguration() throws InternalException {
        if (!configurationExists())
            return false;

        try {
            destroySubcontextWithChilds(configurationDN);
            Logger.info("Purge DICOM Configuration at {}", configurationDN);
            clearConfigurationDN();
        } catch (NamingException e) {
            throw new InternalException(e);
        }
        return true;
    }

    @Override
    public synchronized boolean registerAETitle(String aet) throws InternalException {
        ensureConfigurationExists();
        try {
            registerAET(aet);
            return true;
        } catch (AlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public synchronized boolean registerWebAppName(String webAppName) throws InternalException {
        ensureConfigurationExists();
        try {
            registerWebApp(webAppName);
            return true;
        } catch (AlreadyExistsException e) {
            return false;
        }
    }

    private String registerAET(String aet) throws InternalException {
        try {
            String dn = aetDN(aet, aetsRegistryDN);
            createSubcontext(dn, LdapBuilder.attrs("dicomUniqueAETitle", "dicomAETitle", aet));
            return dn;
        } catch (NameAlreadyBoundException e) {
            throw new AlreadyExistsException("AE Title '" + aet + "' already exists");
        } catch (NamingException e) {
            throw new InternalException(e);
        }
    }

    private String registerWebApp(String webAppName) throws InternalException {
        try {
            String dn = webAppDN(webAppName, webAppsRegistryDN);
            createSubcontext(dn, LdapBuilder.attrs("dcmUniqueWebAppName", "dcmWebAppName", webAppName));
            return dn;
        } catch (NameAlreadyBoundException e) {
            throw new AlreadyExistsException("Web Application '" + webAppName + "' already exists");
        } catch (NamingException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public synchronized void unregisterAETitle(String aet) throws InternalException {
        if (configurationExists())
            try {
                ctx.destroySubcontext(aetDN(aet, aetsRegistryDN));
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
                throw new InternalException(e);
            }
    }

    @Override
    public synchronized void unregisterWebAppName(String webAppName) throws InternalException {
        if (configurationExists())
            try {
                ctx.destroySubcontext(webAppDN(webAppName, webAppsRegistryDN));
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
                throw new InternalException(e);
            }
    }

    @Override
    public synchronized ApplicationEntity findApplicationEntity(String aet)
            throws InternalException {
        return findDevice(
                "(&(objectclass=dicomNetworkAE)(dicomAETitle=" + aet + "))", aet)
                .getApplicationEntity(aet);
    }

    @Override
    public synchronized WebApplication findWebApplication(String name) throws InternalException {
        return findDevice("(&(objectclass=dcmWebApp)(dcmWebAppName=" + name + "))", name)
                .getWebApplication(name);
    }

    public synchronized Device findDevice(String filter, String childName)
            throws InternalException {
        if (!configurationExists())
            throw new NotFoundException();

        SearchControls ctls = searchControlSubtreeScope(1, Normal.EMPTY_STRING_ARRAY, false);
        NamingEnumeration<SearchResult> ne = null;
        String childDN;
        try {
            ne = ctx.search(devicesDN, filter, ctls);
            if (!ne.hasMore())
                throw new NotFoundException(childName);

            childDN = ne.next().getNameInNamespace();
        } catch (NamingException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
        String deviceDN = childDN.substring(childDN.indexOf(',') + 1);
        return loadDevice(deviceDN);
    }

    public Connection findConnection(String connDN, Map<String, Connection> cache)
            throws NamingException, InternalException {
        Connection conn = cache.get(connDN);
        if (conn == null) {
            try {
                String[] attrIds = {"dicomHostname", "dicomPort", "dicomTLSCipherSuite", "dicomInstalled"};
                Attributes attrs = ctx.getAttributes(connDN, attrIds);
                cache.put(connDN, conn = new Connection());
                loadFrom(conn, attrs, false);
            } catch (NameNotFoundException e) {
                throw new InternalException(e);
            }
        }
        return conn;
    }

    private SearchControls searchControlSubtreeScope(int countLimit, String[] returningAttrs, boolean returningObjFlag) {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(countLimit);
        ctls.setReturningAttributes(returningAttrs);
        ctls.setReturningObjFlag(returningObjFlag);
        return ctls;
    }

    @Override
    public synchronized Device findDevice(String name) throws InternalException {
        if (!configurationExists())
            throw new NotFoundException();

        return loadDevice(deviceRef(name));
    }

    @Override
    public synchronized Device[] listDeviceInfos(Device keys) throws InternalException {
        if (!configurationExists())
            return new Device[0];

        List<Device> results = new ArrayList<>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            ne = search(devicesDN, toFilter(keys), "dicomDeviceName",
                    "dicomDescription",
                    "dicomManufacturer",
                    "dicomManufacturerModelName",
                    "dicomSoftwareVersion",
                    "dicomStationName",
                    "dicomInstitutionName",
                    "dicomInstitutionDepartmentName",
                    "dicomPrimaryDeviceType",
                    "dicomInstalled",
                    "objectClass");
            while (ne.hasMore()) {
                Device deviceInfo = new Device();
                loadFrom(deviceInfo, ne.next().getAttributes());
                results.add(deviceInfo);
            }
        } catch (NamingException | CertificateException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
        return results.toArray(new Device[results.size()]);
    }

    private String toFilter(Device keys) {
        if (keys == null)
            return "(objectclass=dicomDevice)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=dicomDevice)");
        appendFilter("dicomDeviceName", keys.getDeviceName(), sb);
        appendFilter("dicomDescription", keys.getDescription(), sb);
        appendFilter("dicomManufacturer", keys.getManufacturer(), sb);
        appendFilter("dicomManufacturerModelName", keys.getManufacturerModelName(), sb);
        appendFilter("dicomSoftwareVersion", keys.getSoftwareVersions(), sb);
        appendFilter("dicomStationName", keys.getStationName(), sb);
        appendFilter("dicomInstitutionName", keys.getInstitutionNames(), sb);
        appendFilter("dicomInstitutionDepartmentName", keys.getInstitutionalDepartmentNames(), sb);
        appendFilter("dicomPrimaryDeviceType", keys.getPrimaryDeviceTypes(), sb);
        appendFilter("dicomInstalled", keys.isInstalled(), sb);
        sb.append(")");
        return sb.toString();
    }

    private void loadFrom(ApplicationEntityInfo aetInfo, Attributes attrs, String deviceName,
                          Map<String, Connection> connCache)
            throws NamingException, InternalException {
        aetInfo.setDeviceName(deviceName);
        aetInfo.setAETitle(
                LdapBuilder.stringValue(attrs.get("dicomAETitle"), null));
        aetInfo.setOtherAETitle(
                LdapBuilder.stringArray(attrs.get("dcmOtherAETitle")));
        aetInfo.setDescription(
                LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        aetInfo.setAssociationInitiator(
                LdapBuilder.booleanValue(attrs.get("dicomAssociationInitiator"), true));
        aetInfo.setAssociationAcceptor(
                LdapBuilder.booleanValue(attrs.get("dicomAssociationAcceptor"), true));
        aetInfo.setInstalled(
                LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
        aetInfo.setApplicationClusters(
                LdapBuilder.stringArray(attrs.get("dicomApplicationCluster")));
        aetInfo.setHl7ApplicationName(
                LdapBuilder.stringValue(attrs.get("hl7ApplicationName"), null));
        for (String connDN : LdapBuilder.stringArray(attrs.get("dicomNetworkConnectionReference")))
            aetInfo.getConnections().add(findConnection(connDN, connCache));
    }

    private void loadFrom(WebApplication keys, Attributes attrs, String deviceName,
                          Map<String, Connection> connCache, Map<String, KeycloakClient> keycloakClientCache)
            throws NamingException, InternalException {
        keys.setDeviceName(deviceName);
        keys.setApplicationName(LdapBuilder.stringValue(attrs.get("dcmWebAppName"), null));
        keys.setDescription(LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        keys.setServicePath(LdapBuilder.stringValue(attrs.get("dcmWebServicePath"), null));
        keys.setServiceClasses(LdapBuilder.enumArray(WebApplication.ServiceClass.class, attrs.get("dcmWebServiceClass")));
        keys.setAETitle(LdapBuilder.stringValue(attrs.get("dicomAETitle"), null));
        keys.setApplicationClusters(LdapBuilder.stringArray(attrs.get("dicomApplicationCluster")));
        keys.setProperties(LdapBuilder.stringArray(attrs.get("dcmProperty")));
        String keycloakClientID = LdapBuilder.stringValue(attrs.get("dcmKeycloakClientID"), null);
        keys.setKeycloakClientID(keycloakClientID);
        keys.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
        for (String connDN : LdapBuilder.stringArray(attrs.get("dicomNetworkConnectionReference")))
            keys.getConnections().add(findConnection(connDN, connCache));
        if (keycloakClientID != null)
            keys.setKeycloakClient(findKeycloakClient(keycloakClientID, deviceName, keycloakClientCache));
    }

    private KeycloakClient findKeycloakClient(String clientID, String deviceName, Map<String, KeycloakClient> cache)
            throws NamingException, InternalException {
        String keycloakClientDN = keycloakClientDN(clientID, deviceRef(deviceName));
        KeycloakClient keycloakClient = cache.get(keycloakClientDN);
        if (keycloakClient == null) {
            try {
                Attributes attrs = ctx.getAttributes(keycloakClientDN);
                cache.put(keycloakClientDN, keycloakClient = new KeycloakClient(clientID));
                loadFrom(keycloakClient, attrs);
            } catch (NameNotFoundException e) {
                throw new InternalException(e);
            }
        }
        return keycloakClient;
    }

    @Override
    public synchronized String[] listDeviceNames() throws InternalException {
        if (!configurationExists())
            return Normal.EMPTY_STRING_ARRAY;

        return list(devicesDN, "(objectclass=dicomDevice)", "dicomDeviceName");
    }

    @Override
    public synchronized String[] listRegisteredAETitles() throws InternalException {
        if (!configurationExists())
            return Normal.EMPTY_STRING_ARRAY;

        return list(aetsRegistryDN, "(objectclass=dicomUniqueAETitle)", "dicomAETitle");
    }

    @Override
    public synchronized String[] listRegisteredWebAppNames() throws InternalException {
        if (!configurationExists())
            return Normal.EMPTY_STRING_ARRAY;

        return list(webAppsRegistryDN, "(objectclass=dcmUniqueWebAppName)", "dcmWebAppName");
    }

    public synchronized String[] list(String dn, String filter, String attrID)
            throws InternalException {
        List<String> values = new ArrayList<>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            ne = search(dn, filter, attrID);
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                values.add(LdapBuilder.stringValue(attrs.get(attrID), null));
            }
        } catch (NamingException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public synchronized ConfigurationChanges persist(Device device, EnumSet<Option> options)
            throws InternalException {
        ensureConfigurationExists();
        String deviceName = device.getDeviceName();
        String deviceDN = deviceRef(deviceName);
        boolean rollback = false;
        ArrayList<String> destroyDNs = new ArrayList<>();

        try {
            if (options != null && options.contains(Option.REGISTER))
                register(device, destroyDNs);

            ConfigurationChanges diffs = configurationChangesOf(options);
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, deviceDN, ConfigurationChanges.ChangeType.C);
            createSubcontext(deviceDN,
                    storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                            device, new BasicAttributes(true)));
            rollback = true;
            storeChilds(ConfigurationChanges.nullifyIfNotVerbose(diffs, diffs), deviceDN, device);
            if (options == null || !options.contains(Option.PRESERVE_CERTIFICATE))
                updateCertificates(device);
            rollback = false;
            destroyDNs.clear();
            return diffs;
        } catch (NameAlreadyBoundException e) {
            throw new AlreadyExistsException(deviceName);
        } catch (NamingException e) {
            throw new InternalException(e);
        } catch (CertificateException e) {
            throw new InternalException(e);
        } finally {
            if (rollback) {
                try {
                    destroySubcontextWithChilds(deviceDN);
                } catch (NamingException e) {
                    Logger.warn("Rollback failed:", e);
                }
            }
            unregister(destroyDNs);
        }
    }

    private ConfigurationChanges configurationChangesOf(EnumSet<Option> options) {
        return options != null
                && (options.contains(Option.CONFIGURATION_CHANGES)
                || options.contains(Option.CONFIGURATION_CHANGES_VERBOSE))
                ? new ConfigurationChanges(options.contains(Option.CONFIGURATION_CHANGES_VERBOSE))
                : null;
    }

    private void unregister(ArrayList<String> destroyDNs) {
        for (String dn : destroyDNs) {
            try {
                destroySubcontext(dn);
            } catch (NamingException e) {
                Logger.warn("Unregister {} failed:", dn, e);
            }
        }
    }

    private void register(Device device, List<String> dns) throws InternalException {
        for (String aet : device.getApplicationAETitles())
            if (!aet.equals("*"))
                dns.add(registerAET(aet));
        for (String webAppName : device.getWebApplicationNames())
            if (!webAppName.equals("*"))
                dns.add(registerWebApp(webAppName));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.register(device, dns);
    }

    private void updateCertificates(Device device)
            throws CertificateException, NamingException {
        for (String dn : device.getAuthorizedNodeCertificateRefs())
            updateCertificates(dn, loadCertificates(dn),
                    device.getAuthorizedNodeCertificates(dn));
        for (String dn : device.getThisNodeCertificateRefs())
            updateCertificates(dn, loadCertificates(dn),
                    device.getThisNodeCertificates(dn));
    }

    private void updateCertificates(String dn,
                                    X509Certificate[] prev, X509Certificate[] certs)
            throws CertificateEncodingException, NamingException {
        if (!LdapBuilder.equals(prev, certs))
            storeCertificates(dn, certs);
    }

    private void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device)
            throws NamingException, InternalException {
        for (Connection conn : device.listConnections()) {
            String dn = LdapBuilder.dnOf(conn, deviceDN);
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
            createSubcontext(dn, storeTo(ldapObj, conn, new BasicAttributes(true)));
        }
        for (ApplicationEntity ae : device.getApplicationEntities())
            store(diffs, ae, deviceDN);
        if (extended) {
            for (WebApplication webapp : device.getWebApplications())
                store(diffs, webapp, deviceDN);
            for (KeycloakClient client : device.getKeycloakClients())
                store(diffs, client, deviceDN);
            for (LdapDicomConfigurationExtension ext : extensions)
                ext.storeChilds(diffs, deviceDN, device);
        }
    }

    private void store(ConfigurationChanges diffs, ApplicationEntity ae, String deviceDN) throws NamingException {
        String aeDN = aetDN(ae.getAETitle(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, aeDN, ConfigurationChanges.ChangeType.C);
        createSubcontext(aeDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        ae, deviceDN, new BasicAttributes(true)));
        storeChilds(ConfigurationChanges.nullifyIfNotVerbose(diffs, diffs), aeDN, ae);
    }

    private void store(ConfigurationChanges diffs, WebApplication webapp, String deviceDN) throws NamingException {
        String webappDN = webAppDN(webapp.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, webappDN, ConfigurationChanges.ChangeType.C);
        createSubcontext(webappDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        webapp, deviceDN, new BasicAttributes(true)));
    }

    private void store(ConfigurationChanges diffs, KeycloakClient client, String deviceDN) throws NamingException {
        String clientDN = keycloakClientDN(client.getKeycloakClientID(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, clientDN, ConfigurationChanges.ChangeType.C);
        createSubcontext(clientDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        client, new BasicAttributes(true)));
    }

    private void storeChilds(ConfigurationChanges diffs, String aeDN, ApplicationEntity ae)
            throws NamingException {
        for (TransferCapability tc : ae.getTransferCapabilities()) {
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, aeDN, ConfigurationChanges.ChangeType.C);
            createSubcontext(dnOf(tc, aeDN), storeTo(ldapObj, tc, new BasicAttributes(true)));
        }
        if (extended)
            for (LdapDicomConfigurationExtension ext : extensions)
                ext.storeChilds(diffs, aeDN, ae);
    }

    @Override
    public ConfigurationChanges merge(Device device, EnumSet<Option> options) throws InternalException {
        ConfigurationChanges diffs = configurationChangesOf(options);
        merge(device, options, diffs);
        return diffs;
    }

    private synchronized void merge(Device device, EnumSet<Option> options, ConfigurationChanges diffs)
            throws InternalException {
        if (!configurationExists())
            throw new NotFoundException();

        String deviceDN = deviceRef(device.getDeviceName());
        Device prev = loadDevice(deviceDN);
        ArrayList<String> destroyDNs = new ArrayList<>();
        try {
            boolean register = options != null && options.contains(Option.REGISTER);
            boolean preserveVendorData = options != null && options.contains(Option.PRESERVE_VENDOR_DATA);
            if (register) {
                registerDiff(prev, device, destroyDNs);
            }
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, deviceDN, ConfigurationChanges.ChangeType.U);
            modifyAttributes(deviceDN,
                    storeDiffs(ldapObj, prev, device, new ArrayList<>(), preserveVendorData));
            ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            mergeChilds(diffs, prev, device, deviceDN, preserveVendorData);
            destroyDNs.clear();
            if (register) {
                markForUnregister(prev, device, destroyDNs);
            }
            if (options == null || !options.contains(Option.PRESERVE_CERTIFICATE))
                updateCertificates(prev, device);
        } catch (NameNotFoundException e) {
            throw new NotFoundException(e);
        } catch (NamingException e) {
            throw new InternalException(e);
        } catch (CertificateException e) {
            throw new InternalException(e);
        } finally {
            unregister(destroyDNs);
        }
    }

    private void registerDiff(Device prev, Device device, List<String> dns) throws InternalException {
        for (String aet : device.getApplicationAETitles())
            if (!aet.equals("*") && prev.getApplicationEntity(aet) == null)
                dns.add(registerAET(aet));
        for (String webAppName : device.getWebApplicationNames())
            if (!webAppName.equals("*") && prev.getWebApplication(webAppName) == null)
                dns.add(registerWebApp(webAppName));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.registerDiff(prev, device, dns);
    }

    private void markForUnregister(Device prev, Device device, List<String> dns) {
        for (String aet : prev.getApplicationAETitles())
            if (!aet.equals("*") && device.getApplicationEntity(aet) == null)
                dns.add(aetDN(aet, aetsRegistryDN));
        for (String webAppName : prev.getWebApplicationNames())
            if (!webAppName.equals("*") && device.getWebApplication(webAppName) == null)
                dns.add(webAppDN(webAppName, webAppsRegistryDN));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.markForUnregister(prev, device, dns);
    }

    private void updateCertificates(Device prev, Device device)
            throws CertificateException, NamingException {
        for (String dn : device.getAuthorizedNodeCertificateRefs()) {
            X509Certificate[] prevCerts = prev.getAuthorizedNodeCertificates(dn);
            updateCertificates(dn,
                    prevCerts != null ? prevCerts : loadCertificates(dn),
                    device.getAuthorizedNodeCertificates(dn));
        }
        for (String dn : device.getThisNodeCertificateRefs()) {
            X509Certificate[] prevCerts = prev.getThisNodeCertificates(dn);
            updateCertificates(dn,
                    prevCerts != null ? prevCerts : loadCertificates(dn),
                    device.getThisNodeCertificates(dn));
        }
    }

    private void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN, boolean preserveVendorData)
            throws NamingException, InternalException {
        mergeConnections(diffs, prev, device, deviceDN);
        mergeAEs(diffs, prev, device, deviceDN, preserveVendorData);
        mergeWebApps(diffs, prev, device, deviceDN);
        mergeKeycloakClients(diffs, prev, device, deviceDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(diffs, prev, device, deviceDN);
    }

    @Override
    public synchronized ConfigurationChanges removeDevice(String name, EnumSet<Option> options)
            throws InternalException {
        if (!configurationExists())
            throw new NotFoundException();

        String dn = deviceRef(name);
        removeDeviceWithDN(dn, options != null && options.contains(Option.REGISTER));
        ConfigurationChanges diffs = new ConfigurationChanges(false);
        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
        return diffs;
    }

    private void markForUnregister(String deviceDN, List<String> dns)
            throws NamingException, InternalException {
        NamingEnumeration<SearchResult> aets =
                search(deviceDN, "(objectclass=dicomNetworkAE)", Normal.EMPTY_STRING_ARRAY);
        try {
            while (aets.hasMore()) {
                String rdn = aets.next().getName();
                if (!rdn.equals("dicomAETitle=*"))
                    dns.add(rdn + ',' + aetsRegistryDN);
            }
        } finally {
            LdapBuilder.safeClose(aets);
        }
        NamingEnumeration<SearchResult> webApps =
                search(deviceDN, "(objectclass=dcmWebApp)", Normal.EMPTY_STRING_ARRAY);
        try {
            while (webApps.hasMore()) {
                String rdn = webApps.next().getName();
                dns.add(rdn + ',' + webAppsRegistryDN);
            }
        } finally {
            LdapBuilder.safeClose(webApps);
        }
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.markForUnregister(deviceDN, dns);
    }

    private void removeDeviceWithDN(String deviceDN, boolean unregister) throws InternalException {
        try {
            ArrayList<String> destroyDNs = new ArrayList<>();
            if (unregister)
                markForUnregister(deviceDN, destroyDNs);
            destroySubcontextWithChilds(deviceDN);
            unregister(destroyDNs);
        } catch (NameNotFoundException e) {
            throw new NotFoundException(e);
        } catch (NamingException e) {
            throw new InternalException(e);
        }
    }

    public synchronized void createSubcontext(String name, Attributes attrs)
            throws NamingException {
        ctx.createSubcontextAndClose(name, attrs);
    }

    public synchronized void destroySubcontext(String dn) throws NamingException {
        ctx.destroySubcontext(dn);
    }

    public synchronized void destroySubcontextWithChilds(String name)
            throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list(name);
        try {
            while (list.hasMore())
                destroySubcontextWithChilds(list.next().getNameInNamespace());
        } finally {
            LdapBuilder.safeClose(list);
        }
        ctx.destroySubcontext(name);
    }

    public String getConfigurationDN() {
        return configurationDN;
    }

    private void setConfigurationDN(String configurationDN) {
        this.configurationDN = configurationDN;
        this.devicesDN = CN_DEVICES + configurationDN;
        this.aetsRegistryDN = CN_UNIQUE_AE_TITLES_REGISTRY + configurationDN;
        this.webAppsRegistryDN = CN_UNIQUE_WEB_APP_NAMES_REGISTRY + configurationDN;
    }

    private void clearConfigurationDN() {
        this.configurationDN = null;
        this.devicesDN = null;
        this.aetsRegistryDN = null;
        this.webAppsRegistryDN = null;
    }

    public void ensureConfigurationExists() throws InternalException {
        if (!configurationExists())
            initConfiguration();
    }

    private void initConfiguration() throws InternalException {
        setConfigurationDN("cn=" + configurationCN + ',' + baseDN);
        try {
            createSubcontext(configurationDN,
                    LdapBuilder.attrs(configurationRoot, "cn", configurationCN));
            createSubcontext(devicesDN,
                    LdapBuilder.attrs("dicomDevicesRoot", "cn", "Devices"));
            createSubcontext(aetsRegistryDN,
                    LdapBuilder.attrs("dicomUniqueAETitlesRegistryRoot",
                            "cn", "Unique AE Titles Registry"));
            createSubcontext(webAppsRegistryDN,
                    LdapBuilder.attrs("dcmUniqueWebAppNamesRegistryRoot",
                            "cn", "Unique Web Application Names Registry"));
            Logger.info("Create DICOM Configuration at {}", configurationDN);
        } catch (NamingException e) {
            clearConfigurationDN();
            throw new InternalException(e);
        }
    }

    private boolean findConfiguration() throws InternalException {
        NamingEnumeration<SearchResult> ne = null;
        try {
            SearchControls ctls = searchControlSubtreeScope(1, Normal.EMPTY_STRING_ARRAY, false);
            ne = ctx.search(
                    baseDN,
                    "(&(objectclass=" + configurationRoot
                            + ")(cn=" + configurationCN + "))",
                    ctls);
            if (!ne.hasMore())
                return false;

            setConfigurationDN(ne.next().getName() + "," + baseDN);
            return true;
        } catch (NamingException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, Device device, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomDevice");
        attrs.put(objectclass);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDeviceName", device.getDeviceName(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", device.getDescription(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDeviceUID", device.getDeviceUID(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomManufacturer", device.getManufacturer(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomManufacturerModelName",
                device.getManufacturerModelName(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomSoftwareVersion",
                device.getSoftwareVersions());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomStationName", device.getStationName(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDeviceSerialNumber",
                device.getDeviceSerialNumber(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfPatientID",
                device.getIssuerOfPatientID(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfAccessionNumber",
                device.getIssuerOfAccessionNumber(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomOrderPlacerIdentifier",
                device.getOrderPlacerIdentifier(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomOrderFillerIdentifier",
                device.getOrderFillerIdentifier(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfAdmissionID",
                device.getIssuerOfAdmissionID(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfServiceEpisodeID",
                device.getIssuerOfServiceEpisodeID(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfContainerIdentifier",
                device.getIssuerOfContainerIdentifier(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomIssuerOfSpecimenIdentifier",
                device.getIssuerOfSpecimenIdentifier(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomInstitutionName",
                device.getInstitutionNames());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomInstitutionCode",
                device.getInstitutionCodes());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomInstitutionAddress",
                device.getInstitutionAddresses());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomInstitutionDepartmentName",
                device.getInstitutionalDepartmentNames());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomPrimaryDeviceType",
                device.getPrimaryDeviceTypes());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomRelatedDeviceReference",
                device.getRelatedDeviceRefs());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs());
        storeNotEmpty(ldapObj, attrs, "dicomVendorData", device.getVendorData());
        LdapBuilder.storeBoolean(ldapObj, attrs, "dicomInstalled", device.isInstalled());
        if (!extended)
            return attrs;

        objectclass.add("dcmDevice");
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmRoleSelectionNegotiationLenient",
                device.isRoleSelectionNegotiationLenient(), false);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmLimitAssociationsInitiatedBy",
                device.getLimitAssociationsInitiatedBy());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStoreURL", device.getTrustStoreURL(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStoreType", device.getTrustStoreType(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStorePin", device.getTrustStorePin(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmTrustStorePinProperty", device.getTrustStorePinProperty(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreURL", device.getKeyStoreURL(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreType", device.getKeyStoreType(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStorePin", device.getKeyStorePin(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStorePinProperty", device.getKeyStorePinProperty(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreKeyPin", device.getKeyStoreKeyPin(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmTimeZoneOfDevice", device.getTimeZoneOfDevice(), null);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(ldapObj, device, attrs);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, Connection conn, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkConnection");
        attrs.put(objectclass);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "cn", conn.getCommonName(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomHostname", conn.getHostname(), null);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuites());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", conn.getInstalled(), null);
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkConnection");
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmProtocol", conn.getProtocol(), Connection.Protocol.DICOM);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmHTTPProxy", conn.getHttpProxy(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmBlacklistedHostname", conn.getBlacklist());
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmSendTimeout",
                conn.getSendTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmStoreTimeout",
                conn.getStoreTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmResponseTimeout",
                conn.getResponseTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmRetrieveTimeout",
                conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmRetrieveTimeoutTotal",
                conn.isRetrieveTimeoutTotal(), false);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmAATimeout",
                conn.getAbortTimeout(), Connection.DEF_ABORT_TIMEOUT);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmBindAddress", conn.getBindAddress(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmClientBindAddress", conn.getClientBindAddress(), null);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmPackPDV", conn.isPackPDV(), true);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmTLSProtocol", conn.getTlsProtocols(), Connection.DEFAULT_TLS_PROTOCOLS);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmTLSEndpointIdentificationAlgorithm",
                conn.getTlsEndpointIdentificationAlgorithm(), null);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity ae, String deviceDN, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomNetworkAE");
        attrs.put(objectclass);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomAETitle", ae.getAETitle(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", ae.getDescription(), null);
        storeNotEmpty(ldapObj, attrs, "dicomVendorData", ae.getVendorData());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomApplicationCluster", ae.getApplicationClusters());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        LdapBuilder.storeBoolean(ldapObj, attrs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        LdapBuilder.storeBoolean(ldapObj, attrs, "dicomAssociationAcceptor", ae.isAssociationAcceptor());
        LdapBuilder.storeConnRefs(ldapObj, attrs, ae.getConnections(), deviceDN);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", ae.getInstalled(), null);
        if (!extended)
            return attrs;

        objectclass.add("dcmNetworkAE");
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmRoleSelectionNegotiationLenient",
                ae.getRoleSelectionNegotiationLenient(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmPreferredTransferSyntax",
                LdapBuilder.addOrdinalPrefix(ae.getPreferredTransferSyntaxes()));
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmShareTransferCapabilitiesFromAETitle",
                ae.getShareTransferCapabilitiesFromAETitle(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "hl7ApplicationName", ae.getHl7ApplicationName(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmOtherAETitle", ae.getOtherAETitles());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmNoAsyncModeCalledAETitle", ae.getNoAsyncModeCalledAETitles());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmMasqueradeCallingAETitle", ae.getMasqueradeCallingAETitles());
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeTo(ldapObj, ae, attrs);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, WebApplication webapp, String deviceDN,
                               Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dcmWebApp");
        attrs.put(objectclass);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmWebAppName", webapp.getApplicationName(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", webapp.getDescription(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmWebServicePath", webapp.getServicePath(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakClientID", webapp.getKeycloakClientID(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmWebServiceClass", webapp.getServiceClasses());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomAETitle", webapp.getAETitle(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomApplicationCluster", webapp.getApplicationClusters());
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmProperty", webapp.getProperties());
        LdapBuilder.storeConnRefs(ldapObj, attrs, webapp.getConnections(), deviceDN);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", webapp.getInstalled(), null);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, KeycloakClient client, Attributes attrs) {
        attrs.put("objectclass", "dcmKeycloakClient");
        attrs.put("dcmKeycloakClientID", client.getKeycloakClientID());
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmURI", client.getKeycloakServerURL(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakRealm", client.getKeycloakRealm(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakGrantType",
                client.getKeycloakGrantType(), KeycloakClient.GrantType.client_credentials);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dcmKeycloakClientSecret",
                client.getKeycloakClientSecret(), null);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTLSAllowAnyHostname",
                client.isTLSAllowAnyHostname(), false);
        LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTLSDisableTrustManager",
                client.isTLSDisableTrustManager(), false);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "uid", client.getUserID(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "userPassword", client.getPassword(), null);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, TransferCapability tc, Attributes attrs) {
        BasicAttribute objectclass = new BasicAttribute("objectclass", "dicomTransferCapability");
        attrs.put(objectclass);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "cn", tc.getCommonName(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomSOPClass", tc.getSopClass(), null);
        LdapBuilder.storeNotNullOrDef(ldapObj, attrs, "dicomTransferRole", tc.getRole(), null);
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        if (!extended)
            return attrs;

        objectclass.add("dcmTransferCapability");
        LdapBuilder.storeNotEmpty(ldapObj, attrs, "dcmPreferredTransferSyntax",
                LdapBuilder.addOrdinalPrefix(tc.getPreferredTransferSyntaxes()));
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            LdapBuilder.storeNotDef(ldapObj, attrs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            LdapBuilder.storeNotDef(ldapObj, attrs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            LdapBuilder.storeNotDef(ldapObj, attrs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            LdapBuilder.storeNotDef(ldapObj, attrs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE), false);
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            LdapBuilder.storeInt(ldapObj, attrs, "dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            LdapBuilder.storeInt(ldapObj, attrs, "dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            LdapBuilder.storeInt(ldapObj, attrs, "dcmDataElementCoercion",
                    storageOpts.getElementCoercion().ordinal());
        }
        return attrs;
    }

    @Override
    public synchronized void persistCertificates(String dn, X509Certificate... certs)
            throws InternalException {
        try {
            storeCertificates(dn, certs);
        } catch (NameNotFoundException e) {
            throw new NotFoundException(e);
        } catch (NamingException e) {
            throw new InternalException(e);
        } catch (CertificateEncodingException e) {
            throw new InternalException(e);
        }
    }

    private void storeCertificates(String dn, X509Certificate... certs)
            throws CertificateEncodingException, NamingException {
        byte[][] vals = new byte[certs.length][];
        for (int i = 0; i < vals.length; i++)
            vals[i] = certs[i].getEncoded();
        Attributes attrs = ctx.getAttributes(dn,
                new String[]{"objectClass"});
        ModificationItem replaceCert = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr(userCertificate, vals));
        ctx.modifyAttributes(dn,
                LdapBuilder.hasObjectClass(attrs, pkiUser)
                        ? new ModificationItem[]{replaceCert}
                        : new ModificationItem[]{
                        new ModificationItem(
                                DirContext.ADD_ATTRIBUTE,
                                LdapBuilder.attr("objectClass", pkiUser)),
                        replaceCert});
    }

    @Override
    public synchronized void removeCertificates(String dn) throws InternalException {
        try {
            ModificationItem removeCert = new ModificationItem(
                    DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(userCertificate));
            ctx.modifyAttributes(dn, removeCert);
        } catch (NameNotFoundException e) {
            throw new NotFoundException(e);
        } catch (NamingException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public synchronized X509Certificate[] findCertificates(String dn) throws InternalException {
        try {
            return loadCertificates(dn);
        } catch (NameNotFoundException e) {
            throw new NotFoundException(e);
        } catch (NamingException e) {
            throw new InternalException(e);
        } catch (CertificateException e) {
            throw new InternalException(e);
        }
    }

    private X509Certificate[] loadCertificates(String dn)
            throws NamingException, CertificateException {
        Attributes attrs = ctx.getAttributes(dn, new String[]{userCertificate});
        Attribute attr = attrs.get(userCertificate);
        if (attr == null)
            return EMPTY_X509_CERTIFICATES;
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        X509Certificate[] certs = new X509Certificate[attr.size()];
        for (int i = 0; i < certs.length; i++)
            certs[i] = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream((byte[]) attr.get(i)));

        return certs;
    }

    @Override
    public byte[][] loadDeviceVendorData(String deviceName) throws InternalException {
        if (!configurationExists())
            throw new NotFoundException();

        try {
            Attributes attrs = getAttributes(deviceRef(deviceName), new String[]{"dicomVendorData"});
            return byteArrays(attrs.get("dicomVendorData"));
        } catch (NameNotFoundException e) {
            throw new NotFoundException("Device with specified name not found", e);
        } catch (NamingException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public ConfigurationChanges updateDeviceVendorData(String deviceName, byte[]... vendorData)
            throws InternalException {
        String deviceRef = deviceRef(deviceName);
        if (!configurationExists())
            throw new NotFoundException();

        ConfigurationChanges diffs = new ConfigurationChanges(false);
        try {
            Attributes attrs = getAttributes(deviceRef, new String[]{"dicomVendorData"});
            byte[][] prev = byteArrays(attrs.get("dicomVendorData"));
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, deviceRef, ConfigurationChanges.ChangeType.C);
            List<ModificationItem> mods = new ArrayList<>(1);
            storeDiff(ldapObj, mods, "dicomVendorData", prev, vendorData);
            modifyAttributes(deviceRef, mods);
        } catch (NameNotFoundException e) {
            throw new NotFoundException("Device with specified name not found", e);
        } catch (NamingException e) {
            throw new InternalException(e);
        }
        return diffs;
    }

    public Device loadDevice(String deviceDN) throws InternalException {
        // get the device cache for this loading phase
        Map<String, Device> deviceCache = currentlyLoadedDevicesLocal.get();

        // if there is none, create one for the current thread and remember that it should be cleaned up when the device is loaded
        boolean doCleanUpCache = false;
        if (deviceCache == null) {
            doCleanUpCache = true;
            deviceCache = new HashMap<>();
            currentlyLoadedDevicesLocal.set(deviceCache);
        }

        // if a requested device is already being (was) loaded, do not load it again, just return existing Device object
        if (deviceCache.containsKey(deviceDN))
            return deviceCache.get(deviceDN);


        try {
            Attributes attrs = getAttributes(deviceDN);
            Device device = new Device(LdapBuilder.stringValue(attrs.get("dicomDeviceName"), null));

            // remember this device so it won't be loaded again in this run
            deviceCache.put(deviceDN, device);

            loadFrom(device, attrs);
            loadChilds(device, deviceDN);
            return device;
        } catch (NameNotFoundException e) {
            throw new NotFoundException("Device with specified name not found", e);
        } catch (NamingException | CertificateException e) {
            throw new InternalException(e);
        } finally {

            // if this loadDevice call initialized the cache, then clean it up
            if (doCleanUpCache) currentlyLoadedDevicesLocal.remove();
        }

    }

    public Attributes getAttributes(String name) throws NamingException {
        return ctx.getAttributes(name);
    }

    public Attributes getAttributes(String name, String[] attrIDs) throws NamingException {
        return ctx.getAttributes(name, attrIDs);
    }

    private void loadChilds(Device device, String deviceDN)
            throws NamingException, InternalException {
        loadConnections(device, deviceDN);
        loadApplicationEntities(device, deviceDN);
        loadWebApplications(device, deviceDN);
        loadKeycloakClients(device, deviceDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadChilds(device, deviceDN);
    }

    private void loadFrom(TransferCapability tc, Attributes attrs) throws NamingException {
        tc.setCommonName(LdapBuilder.stringValue(attrs.get("cn"), null));
        tc.setSopClass(LdapBuilder.stringValue(attrs.get("dicomSOPClass"), null));
        tc.setRole(TransferCapability.Role.valueOf(
                LdapBuilder.stringValue(attrs.get("dicomTransferRole"), null)));
        tc.setTransferSyntaxes(LdapBuilder.stringArray(attrs.get("dicomTransferSyntax")));
        if (!LdapBuilder.hasObjectClass(attrs, "dcmTransferCapability"))
            return;

        tc.setPreferredTransferSyntaxes(LdapBuilder.removeOrdinalPrefix(
                LdapBuilder.stringArray(attrs.get("dcmPreferredTransferSyntax"))));
        tc.setQueryOptions(toQueryOptions(attrs));
        tc.setStorageOptions(toStorageOptions(attrs));
    }

    private void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException {
        device.setDeviceName(LdapBuilder.stringValue(attrs.get("dicomDeviceName"), null));
        device.setDescription(LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        device.setDeviceUID(LdapBuilder.stringValue(attrs.get("dicomDeviceUID"), null));
        device.setManufacturer(LdapBuilder.stringValue(attrs.get("dicomManufacturer"), null));
        device.setManufacturerModelName(LdapBuilder.stringValue(attrs.get("dicomManufacturerModelName"), null));
        device.setSoftwareVersions(LdapBuilder.stringArray(attrs.get("dicomSoftwareVersion")));
        device.setStationName(LdapBuilder.stringValue(attrs.get("dicomStationName"), null));
        device.setDeviceSerialNumber(LdapBuilder.stringValue(attrs.get("dicomDeviceSerialNumber"), null));
        device.setIssuerOfPatientID(
                LdapBuilder.issuerValue(attrs.get("dicomIssuerOfPatientID")));
        device.setIssuerOfAccessionNumber(
                LdapBuilder.issuerValue(attrs.get("dicomIssuerOfAccessionNumber")));
        device.setOrderPlacerIdentifier(
                LdapBuilder.issuerValue(attrs.get("dicomOrderPlacerIdentifier")));
        device.setOrderFillerIdentifier(
                LdapBuilder.issuerValue(attrs.get("dicomOrderFillerIdentifier")));
        device.setIssuerOfAdmissionID(
                LdapBuilder.issuerValue(attrs.get("dicomIssuerOfAdmissionID")));
        device.setIssuerOfServiceEpisodeID(
                LdapBuilder.issuerValue(attrs.get("dicomIssuerOfServiceEpisodeID")));
        device.setIssuerOfContainerIdentifier(
                LdapBuilder.issuerValue(attrs.get("dicomIssuerOfContainerIdentifier")));
        device.setIssuerOfSpecimenIdentifier(
                LdapBuilder.issuerValue(attrs.get("dicomIssuerOfSpecimenIdentifier")));
        device.setInstitutionNames(LdapBuilder.stringArray(attrs.get("dicomInstitutionName")));
        device.setInstitutionCodes(LdapBuilder.codeArray(attrs.get("dicomInstitutionCode")));
        device.setInstitutionAddresses(LdapBuilder.stringArray(attrs.get("dicomInstitutionAddress")));
        device.setInstitutionalDepartmentNames(
                LdapBuilder.stringArray(attrs.get("dicomInstitutionDepartmentName")));
        device.setPrimaryDeviceTypes(LdapBuilder.stringArray(attrs.get("dicomPrimaryDeviceType")));
        device.setRelatedDeviceRefs(LdapBuilder.stringArray(attrs.get("dicomRelatedDeviceReference")));
        for (String dn : LdapBuilder.stringArray(attrs.get("dicomAuthorizedNodeCertificateReference")))
            device.setAuthorizedNodeCertificates(dn, loadCertificates(dn));
        for (String dn : LdapBuilder.stringArray(attrs.get("dicomThisNodeCertificateReference")))
            device.setThisNodeCertificates(dn, loadCertificates(dn));
        device.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        device.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), true));
        if (!LdapBuilder.hasObjectClass(attrs, "dcmDevice"))
            return;

        device.setRoleSelectionNegotiationLenient(
                LdapBuilder.booleanValue(attrs.get("dcmRoleSelectionNegotiationLenient"), false));
        device.setLimitOpenAssociations(
                LdapBuilder.intValue(attrs.get("dcmLimitOpenAssociations"), 0));
        device.setLimitAssociationsInitiatedBy(
                LdapBuilder.stringArray(attrs.get("dcmLimitAssociationsInitiatedBy")));
        device.setTrustStoreURL(LdapBuilder.stringValue(attrs.get("dcmTrustStoreURL"), null));
        device.setTrustStoreType(LdapBuilder.stringValue(attrs.get("dcmTrustStoreType"), null));
        device.setTrustStorePin(LdapBuilder.stringValue(attrs.get("dcmTrustStorePin"), null));
        device.setTrustStorePinProperty(
                LdapBuilder.stringValue(attrs.get("dcmTrustStorePinProperty"), null));
        device.setKeyStoreURL(LdapBuilder.stringValue(attrs.get("dcmKeyStoreURL"), null));
        device.setKeyStoreType(LdapBuilder.stringValue(attrs.get("dcmKeyStoreType"), null));
        device.setKeyStorePin(LdapBuilder.stringValue(attrs.get("dcmKeyStorePin"), null));
        device.setKeyStorePinProperty(
                LdapBuilder.stringValue(attrs.get("dcmKeyStorePinProperty"), null));
        device.setKeyStoreKeyPin(LdapBuilder.stringValue(attrs.get("dcmKeyStoreKeyPin"), null));
        device.setKeyStoreKeyPinProperty(
                LdapBuilder.stringValue(attrs.get("dcmKeyStoreKeyPinProperty"), null));
        device.setTimeZoneOfDevice(LdapBuilder.timeZoneValue(attrs.get("dcmTimeZoneOfDevice"), null));
        device.setArcDevExt(LdapBuilder.hasObjectClass(attrs, "dcmArchiveDevice"));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadFrom(device, attrs);
    }

    private void loadConnections(Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(deviceDN, "(objectclass=dicomNetworkConnection)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                Connection conn = new Connection();
                loadFrom(conn, attrs, LdapBuilder.hasObjectClass(attrs, "dcmNetworkConnection"));
                device.addConnection(conn);
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    public NamingEnumeration<SearchResult> search(String dn, String filter)
            throws NamingException {
        return search(dn, filter, (String[]) null);
    }

    public NamingEnumeration<SearchResult> search(String dn, String filter,
                                                  String... attrs) throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningObjFlag(false);
        ctls.setReturningAttributes(attrs);
        return ctx.search(dn, filter, ctls);
    }

    private void loadFrom(Connection conn, Attributes attrs, boolean extended)
            throws NamingException {
        conn.setCommonName(LdapBuilder.stringValue(attrs.get("cn"), null));
        conn.setHostname(LdapBuilder.stringValue(attrs.get("dicomHostname"), null));
        conn.setPort(LdapBuilder.intValue(attrs.get("dicomPort"), Connection.NOT_LISTENING));
        conn.setTlsCipherSuites(LdapBuilder.stringArray(attrs.get("dicomTLSCipherSuite")));
        conn.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
        if (!extended)
            return;

        conn.setProtocol(LdapBuilder.enumValue(Connection.Protocol.class, attrs.get("dcmProtocol"), Connection.Protocol.DICOM));
        conn.setHttpProxy(LdapBuilder.stringValue(attrs.get("dcmHTTPProxy"), null));
        conn.setBlacklist(LdapBuilder.stringArray(attrs.get("dcmBlacklistedHostname")));
        conn.setBacklog(LdapBuilder.intValue(attrs.get("dcmTCPBacklog"), Connection.DEF_BACKLOG));
        conn.setConnectTimeout(LdapBuilder.intValue(attrs.get("dcmTCPConnectTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRequestTimeout(LdapBuilder.intValue(attrs.get("dcmAARQTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(LdapBuilder.intValue(attrs.get("dcmAAACTimeout"),
                Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(LdapBuilder.intValue(attrs.get("dcmARRPTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSendTimeout(LdapBuilder.intValue(attrs.get("dcmSendTimeout"),
                Connection.NO_TIMEOUT));
        conn.setStoreTimeout(LdapBuilder.intValue(attrs.get("dcmStoreTimeout"),
                Connection.NO_TIMEOUT));
        conn.setResponseTimeout(LdapBuilder.intValue(attrs.get("dcmResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRetrieveTimeout(LdapBuilder.intValue(attrs.get("dcmRetrieveTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRetrieveTimeoutTotal(LdapBuilder.booleanValue(attrs.get("dcmRetrieveTimeoutTotal"), false));
        conn.setIdleTimeout(LdapBuilder.intValue(attrs.get("dcmIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAbortTimeout(LdapBuilder.intValue(attrs.get("dcmAATimeout"),
                Connection.DEF_ABORT_TIMEOUT));
        conn.setSocketCloseDelay(LdapBuilder.intValue(attrs.get("dcmTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(LdapBuilder.intValue(attrs.get("dcmTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(LdapBuilder.intValue(attrs.get("dcmTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(LdapBuilder.booleanValue(attrs.get("dcmTCPNoDelay"), true));
        conn.setBindAddress(LdapBuilder.stringValue(attrs.get("dcmBindAddress"), null));
        conn.setClientBindAddress(LdapBuilder.stringValue(attrs.get("dcmClientBindAddress"), null));
        conn.setTlsNeedClientAuth(LdapBuilder.booleanValue(attrs.get("dcmTLSNeedClientAuth"), true));
        conn.setTlsProtocols(LdapBuilder.stringArray(attrs.get("dcmTLSProtocol"), Connection.DEFAULT_TLS_PROTOCOLS));
        conn.setTlsEndpointIdentificationAlgorithm(
                LdapBuilder.enumValue(Connection.EndpointIdentificationAlgorithm.class,
                        attrs.get("dcmTLSEndpointIdentificationAlgorithm"), null));
        conn.setSendPDULength(LdapBuilder.intValue(attrs.get("dcmSendPDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setReceivePDULength(LdapBuilder.intValue(attrs.get("dcmReceivePDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setMaxOpsPerformed(LdapBuilder.intValue(attrs.get("dcmMaxOpsPerformed"),
                Connection.SYNCHRONOUS_MODE));
        conn.setMaxOpsInvoked(LdapBuilder.intValue(attrs.get("dcmMaxOpsInvoked"),
                Connection.SYNCHRONOUS_MODE));
        conn.setPackPDV(LdapBuilder.booleanValue(attrs.get("dcmPackPDV"), true));
    }

    private void loadApplicationEntities(Device device, String deviceDN)
            throws NamingException, InternalException {
        NamingEnumeration<SearchResult> ne =
                search(deviceDN, "(objectclass=dicomNetworkAE)");
        try {
            while (ne.hasMore()) {
                device.addApplicationEntity(
                        loadApplicationEntity(ne.next(), deviceDN, device));
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private ApplicationEntity loadApplicationEntity(SearchResult sr,
                                                    String deviceDN, Device device) throws NamingException, InternalException {
        Attributes attrs = sr.getAttributes();
        ApplicationEntity ae = new ApplicationEntity(LdapBuilder.stringValue(attrs.get("dicomAETitle"), null));
        loadFrom(ae, attrs);
        for (String connDN : LdapBuilder.stringArray(attrs.get("dicomNetworkConnectionReference")))
            ae.addConnection(LdapBuilder.findConnection(connDN, deviceDN, device));
        loadChilds(ae, sr.getNameInNamespace());
        return ae;
    }

    private void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        ae.setDescription(LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        ae.setVendorData(byteArrays(attrs.get("dicomVendorData")));
        ae.setApplicationClusters(LdapBuilder.stringArray(attrs.get("dicomApplicationCluster")));
        ae.setPreferredCallingAETitles(LdapBuilder.stringArray(attrs.get("dicomPreferredCallingAETitle")));
        ae.setPreferredCalledAETitles(LdapBuilder.stringArray(attrs.get("dicomPreferredCalledAETitle")));
        ae.setAssociationInitiator(LdapBuilder.booleanValue(attrs.get("dicomAssociationInitiator"), false));
        ae.setAssociationAcceptor(LdapBuilder.booleanValue(attrs.get("dicomAssociationAcceptor"), false));
        ae.setSupportedCharacterSets(LdapBuilder.stringArray(attrs.get("dicomSupportedCharacterSet")));
        ae.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
        if (!LdapBuilder.hasObjectClass(attrs, "dcmNetworkAE"))
            return;

        ae.setRoleSelectionNegotiationLenient(
                LdapBuilder.booleanValue(attrs.get("dcmRoleSelectionNegotiationLenient"), null));
        ae.setAcceptedCallingAETitles(LdapBuilder.stringArray(attrs.get("dcmAcceptedCallingAETitle")));
        ae.setPreferredTransferSyntaxes(LdapBuilder.removeOrdinalPrefix(
                LdapBuilder.stringArray(attrs.get("dcmPreferredTransferSyntax"))));
        ae.setOtherAETitles(LdapBuilder.stringArray(attrs.get("dcmOtherAETitle")));
        ae.setNoAsyncModeCalledAETitles(LdapBuilder.stringArray(attrs.get("dcmNoAsyncModeCalledAETitle")));
        ae.setMasqueradeCallingAETitles(LdapBuilder.stringArray(attrs.get("dcmMasqueradeCallingAETitle")));
        ae.setShareTransferCapabilitiesFromAETitle(LdapBuilder.stringValue(
                attrs.get("dcmShareTransferCapabilitiesFromAETitle"), null));
        ae.setHl7ApplicationName(LdapBuilder.stringValue(attrs.get("hl7ApplicationName"), null));
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadFrom(ae, attrs);
    }

    private void loadChilds(ApplicationEntity ae, String aeDN) throws NamingException, InternalException {
        loadTransferCapabilities(ae, aeDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.loadChilds(ae, aeDN);
    }

    private void loadWebApplications(Device device, String deviceDN)
            throws NamingException, InternalException {
        NamingEnumeration<SearchResult> ne =
                search(deviceDN, "(objectclass=dcmWebApp)");
        try {
            while (ne.hasMore()) {
                device.addWebApplication(
                        loadWebApplication(ne.next(), deviceDN, device));
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private WebApplication loadWebApplication(SearchResult sr, String deviceDN, Device device) throws NamingException {
        Attributes attrs = sr.getAttributes();
        WebApplication webapp = new WebApplication(LdapBuilder.stringValue(attrs.get("dcmWebAppName"), null));
        loadFrom(webapp, attrs);
        for (String connDN : LdapBuilder.stringArray(attrs.get("dicomNetworkConnectionReference")))
            webapp.addConnection(LdapBuilder.findConnection(connDN, deviceDN, device));
        return webapp;
    }

    private void loadFrom(WebApplication webapp, Attributes attrs) throws NamingException {
        webapp.setDescription(LdapBuilder.stringValue(attrs.get("dicomDescription"), null));
        webapp.setServicePath(LdapBuilder.stringValue(attrs.get("dcmWebServicePath"), null));
        webapp.setKeycloakClientID(LdapBuilder.stringValue(attrs.get("dcmKeycloakClientID"), null));
        webapp.setServiceClasses(LdapBuilder.enumArray(WebApplication.ServiceClass.class, attrs.get("dcmWebServiceClass")));
        webapp.setAETitle(LdapBuilder.stringValue(attrs.get("dicomAETitle"), null));
        webapp.setApplicationClusters(LdapBuilder.stringArray(attrs.get("dicomApplicationCluster")));
        webapp.setProperties(LdapBuilder.stringArray(attrs.get("dcmProperty")));
        webapp.setInstalled(LdapBuilder.booleanValue(attrs.get("dicomInstalled"), null));
    }

    private void loadKeycloakClients(Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne = search(deviceDN, "(objectclass=dcmKeycloakClient)");
        try {
            while (ne.hasMore()) {
                device.addKeycloakClient(loadKeycloakClient(ne.next()));
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private KeycloakClient loadKeycloakClient(SearchResult sr) throws NamingException {
        Attributes attrs = sr.getAttributes();
        KeycloakClient client = new KeycloakClient(LdapBuilder.stringValue(attrs.get("dcmKeycloakClientID"), null));
        loadFrom(client, attrs);
        return client;
    }

    private void loadFrom(KeycloakClient client, Attributes attrs) throws NamingException {
        client.setKeycloakServerURL(LdapBuilder.stringValue(attrs.get("dcmURI"), null));
        client.setKeycloakRealm(LdapBuilder.stringValue(attrs.get("dcmKeycloakRealm"), null));
        client.setKeycloakGrantType(LdapBuilder.enumValue(KeycloakClient.GrantType.class, attrs.get("dcmKeycloakGrantType"),
                KeycloakClient.GrantType.client_credentials));
        client.setKeycloakClientSecret(LdapBuilder.stringValue(attrs.get("dcmKeycloakClientSecret"), null));
        client.setTLSAllowAnyHostname(LdapBuilder.booleanValue(attrs.get("dcmTLSAllowAnyHostname"), false));
        client.setTLSDisableTrustManager(LdapBuilder.booleanValue(attrs.get("dcmTLSDisableTrustManager"), false));
        client.setUserID(LdapBuilder.stringValue(attrs.get("uid"), null));
        client.setPassword(LdapBuilder.stringValue(attrs.get("userPassword"), null));
    }

    private void loadTransferCapabilities(ApplicationEntity ae, String aeDN)
            throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(aeDN, "(objectclass=dicomTransferCapability)");
        try {
            while (ne.hasMore())
                ae.addTransferCapability(loadTransferCapability(ne.next()));
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private TransferCapability loadTransferCapability(SearchResult sr)
            throws NamingException {
        Attributes attrs = sr.getAttributes();
        TransferCapability tc = new TransferCapability();
        loadFrom(tc, attrs);
        return tc;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, Device a, Device b,
                                              List<ModificationItem> mods, boolean preserveVendorData) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomDeviceUID",
                a.getDeviceUID(),
                b.getDeviceUID(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomManufacturer",
                a.getManufacturer(),
                b.getManufacturer(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomManufacturerModelName",
                a.getManufacturerModelName(),
                b.getManufacturerModelName(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomSoftwareVersion",
                a.getSoftwareVersions(),
                b.getSoftwareVersions());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomStationName",
                a.getStationName(),
                b.getStationName(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomDeviceSerialNumber",
                a.getDeviceSerialNumber(),
                b.getDeviceSerialNumber(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomIssuerOfPatientID",
                a.getIssuerOfPatientID(),
                b.getIssuerOfPatientID(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomIssuerOfAccessionNumber",
                a.getIssuerOfAccessionNumber(),
                b.getIssuerOfAccessionNumber(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomOrderPlacerIdentifier",
                a.getOrderPlacerIdentifier(),
                b.getOrderPlacerIdentifier(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomOrderFillerIdentifier",
                a.getOrderFillerIdentifier(),
                b.getOrderFillerIdentifier(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomIssuerOfAdmissionID",
                a.getIssuerOfAdmissionID(),
                b.getIssuerOfAdmissionID(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomIssuerOfServiceEpisodeID",
                a.getIssuerOfServiceEpisodeID(),
                b.getIssuerOfServiceEpisodeID(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomIssuerOfContainerIdentifier",
                a.getIssuerOfContainerIdentifier(),
                b.getIssuerOfContainerIdentifier(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomIssuerOfSpecimenIdentifier",
                a.getIssuerOfSpecimenIdentifier(),
                b.getIssuerOfSpecimenIdentifier(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomInstitutionName",
                a.getInstitutionNames(),
                b.getInstitutionNames());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomInstitutionCode",
                a.getInstitutionCodes(),
                b.getInstitutionCodes());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomInstitutionAddress",
                a.getInstitutionAddresses(),
                b.getInstitutionAddresses());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomInstitutionDepartmentName",
                a.getInstitutionalDepartmentNames(),
                b.getInstitutionalDepartmentNames());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomPrimaryDeviceType",
                a.getPrimaryDeviceTypes(),
                b.getPrimaryDeviceTypes());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomRelatedDeviceReference",
                a.getRelatedDeviceRefs(),
                b.getRelatedDeviceRefs());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomAuthorizedNodeCertificateReference",
                a.getAuthorizedNodeCertificateRefs(),
                b.getAuthorizedNodeCertificateRefs());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomThisNodeCertificateReference",
                a.getThisNodeCertificateRefs(),
                b.getThisNodeCertificateRefs());
        if (!preserveVendorData)
            storeDiff(ldapObj, mods, "dicomVendorData",
                    a.getVendorData(),
                    b.getVendorData());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.isInstalled(),
                b.isInstalled(), null);
        if (!extended)
            return mods;

        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmRoleSelectionNegotiationLenient",
                a.isRoleSelectionNegotiationLenient(),
                b.isRoleSelectionNegotiationLenient(), false);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmLimitOpenAssociations",
                a.getLimitOpenAssociations(),
                b.getLimitOpenAssociations(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmLimitAssociationsInitiatedBy",
                a.getLimitAssociationsInitiatedBy(),
                b.getLimitAssociationsInitiatedBy());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmTrustStoreURL",
                a.getTrustStoreURL(),
                b.getTrustStoreURL(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmTrustStoreType",
                a.getTrustStoreType(),
                b.getTrustStoreType(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmTrustStorePin",
                a.getTrustStorePin(),
                b.getTrustStorePin(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmTrustStorePinProperty",
                a.getTrustStorePinProperty(),
                b.getTrustStorePinProperty(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeyStoreURL",
                a.getKeyStoreURL(),
                b.getKeyStoreURL(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeyStoreType",
                a.getKeyStoreType(),
                b.getKeyStoreType(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeyStorePin",
                a.getKeyStorePin(),
                b.getKeyStorePin(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeyStorePinProperty",
                a.getKeyStorePinProperty(),
                b.getKeyStorePinProperty(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeyStoreKeyPin",
                a.getKeyStoreKeyPin(),
                b.getKeyStoreKeyPin(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeyStoreKeyPinProperty",
                a.getKeyStoreKeyPinProperty(),
                b.getKeyStoreKeyPinProperty(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmTimeZoneOfDevice",
                a.getTimeZoneOfDevice(),
                b.getTimeZoneOfDevice(), null);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(ldapObj, a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, Connection a, Connection b,
                                              List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomHostname",
                a.getHostname(),
                b.getHostname(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomPort",
                a.getPort(),
                b.getPort(),
                Connection.NOT_LISTENING);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomTLSCipherSuite",
                a.getTlsCipherSuites(),
                b.getTlsCipherSuites());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        if (!extended)
            return mods;

        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmProtocol", a.getProtocol(), b.getProtocol(), Connection.Protocol.DICOM);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmHTTPProxy",
                a.getHttpProxy(),
                b.getHttpProxy(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmSendTimeout",
                a.getSendTimeout(),
                b.getSendTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmStoreTimeout",
                a.getStoreTimeout(),
                b.getStoreTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmResponseTimeout",
                a.getResponseTimeout(),
                b.getResponseTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmRetrieveTimeout",
                a.getRetrieveTimeout(),
                b.getRetrieveTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmRetrieveTimeoutTotal",
                a.isRetrieveTimeoutTotal(),
                b.isRetrieveTimeoutTotal(),
                false);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAATimeout",
                a.getAbortTimeout(),
                b.getAbortTimeout(),
                Connection.DEF_ABORT_TIMEOUT);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmBindAddress",
                a.getBindAddress(),
                b.getBindAddress(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmClientBindAddress",
                a.getClientBindAddress(),
                b.getClientBindAddress(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTLSProtocol",
                a.getTlsProtocols(),
                b.getTlsProtocols(),
                Connection.DEFAULT_TLS_PROTOCOLS);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTLSNeedClientAuth",
                a.isTlsNeedClientAuth(),
                b.isTlsNeedClientAuth(),
                true);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmTLSEndpointIdentificationAlgorithm",
                a.getTlsEndpointIdentificationAlgorithm(),
                b.getTlsEndpointIdentificationAlgorithm(),
                null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV(),
                true);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, ApplicationEntity a,
                                              ApplicationEntity b, String deviceDN, List<ModificationItem> mods, boolean preserveVendorData) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        if (!preserveVendorData)
            storeDiff(ldapObj, mods, "dicomVendorData",
                    a.getVendorData(),
                    b.getVendorData());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomPreferredCallingAETitle",
                a.getPreferredCallingAETitles(),
                b.getPreferredCallingAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomPreferredCalledAETitle",
                a.getPreferredCalledAETitles(),
                b.getPreferredCalledAETitles());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomAssociationInitiator",
                a.isAssociationInitiator(),
                b.isAssociationInitiator(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomAssociationAcceptor",
                a.isAssociationAcceptor(),
                b.isAssociationAcceptor(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomSupportedCharacterSet",
                a.getSupportedCharacterSets(),
                b.getSupportedCharacterSets());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        if (!extended)
            return mods;

        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmRoleSelectionNegotiationLenient",
                a.isRoleSelectionNegotiationLenient(),
                b.isRoleSelectionNegotiationLenient(), null);
        LdapBuilder.storeDiffWithOrdinalPrefix(ldapObj, mods, "dcmPreferredTransferSyntax",
                a.getPreferredTransferSyntaxes(),
                b.getPreferredTransferSyntaxes());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAcceptedCallingAETitle",
                a.getAcceptedCallingAETitles(),
                b.getAcceptedCallingAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmOtherAETitle",
                a.getOtherAETitles(),
                b.getOtherAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmNoAsyncModeCalledAETitle",
                a.getNoAsyncModeCalledAETitles(),
                b.getNoAsyncModeCalledAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmMasqueradeCallingAETitle",
                a.getMasqueradeCallingAETitles(),
                b.getMasqueradeCallingAETitles());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmShareTransferCapabilitiesFromAETitle",
                a.getShareTransferCapabilitiesFromAETitle(),
                b.getShareTransferCapabilitiesFromAETitle(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "hl7ApplicationName",
                a.getHl7ApplicationName(),
                b.getHl7ApplicationName(), null);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.storeDiffs(ldapObj, a, b, mods);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, TransferCapability a,
                                              TransferCapability b, List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomSOPClass",
                a.getSopClass(),
                b.getSopClass(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomTransferRole",
                a.getRole(),
                b.getRole(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomTransferSyntax",
                a.getTransferSyntaxes(),
                b.getTransferSyntaxes());
        if (!extended)
            return mods;

        LdapBuilder.storeDiffWithOrdinalPrefix(ldapObj, mods, "dcmPreferredTransferSyntax",
                a.getPreferredTransferSyntaxes(),
                b.getPreferredTransferSyntaxes());
        storeDiffs(ldapObj, a.getQueryOptions(), b.getQueryOptions(), mods);
        storeDiffs(ldapObj, a.getStorageOptions(), b.getStorageOptions(), mods);
        return mods;
    }

    private void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, EnumSet<QueryOption> prev,
                            EnumSet<QueryOption> val, List<ModificationItem> mods) {
        if (Objects.equals(prev, val))
            return;

        LdapBuilder.storeDiff(ldapObj, mods, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private void storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, StorageOptions prev,
                            StorageOptions val, List<ModificationItem> mods) {
        if (Objects.equals(prev, val))
            return;

        LdapBuilder.storeDiff(ldapObj, mods, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, WebApplication a,
                                              WebApplication b, String deviceDN, List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmWebServicePath",
                a.getServicePath(),
                b.getServicePath(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmWebServiceClass",
                a.getServiceClasses(),
                b.getServiceClasses());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomAETitle",
                a.getAETitle(),
                b.getAETitle(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeycloakClientID",
                a.getKeycloakClientID(),
                b.getKeycloakClientID(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapBuilder.storeDiffProperties(ldapObj, mods, "dcmProperty", a.getProperties(), b.getProperties());
        LdapBuilder.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, KeycloakClient a,
                                              KeycloakClient b, List<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmURI",
                a.getKeycloakServerURL(), b.getKeycloakServerURL(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeycloakRealm",
                a.getKeycloakRealm(), b.getKeycloakRealm(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeycloakGrantType",
                a.getKeycloakGrantType(), b.getKeycloakGrantType(), KeycloakClient.GrantType.client_credentials);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmKeycloakClientSecret",
                a.getKeycloakClientSecret(), b.getKeycloakClientSecret(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTLSAllowAnyHostname",
                a.isTLSAllowAnyHostname(), b.isTLSAllowAnyHostname(), false);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmTLSDisableTrustManager",
                a.isTLSDisableTrustManager(), b.isTLSDisableTrustManager(), false);
        LdapBuilder.storeDiffObject(ldapObj, mods, "uid", a.getUserID(), b.getUserID(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "userPassword", a.getPassword(), b.getPassword(), null);
        return mods;
    }

    private void mergeAEs(ConfigurationChanges diffs, Device prevDev, Device dev, String deviceDN, boolean preserveVendorData)
            throws NamingException {
        Collection<String> aets = dev.getApplicationAETitles();
        for (String aet : prevDev.getApplicationAETitles()) {
            if (!aets.contains(aet)) {
                String aetDN = aetDN(aet, deviceDN);
                destroySubcontextWithChilds(aetDN);
                ConfigurationChanges.addModifiedObject(diffs, aetDN, ConfigurationChanges.ChangeType.D);
            }
        }
        Collection<String> prevAETs = prevDev.getApplicationAETitles();
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            String aet = ae.getAETitle();
            if (!prevAETs.contains(aet)) {
                store(diffs, ae, deviceDN);
            } else
                merge(diffs, prevDev.getApplicationEntity(aet), ae, deviceDN, preserveVendorData);
        }
    }

    private void merge(ConfigurationChanges diffs, ApplicationEntity prev, ApplicationEntity ae,
                       String deviceDN, boolean preserveVendorData) throws NamingException {
        String aeDN = aetDN(ae.getAETitle(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, aeDN, ConfigurationChanges.ChangeType.U);
        modifyAttributes(aeDN, storeDiffs(ldapObj, prev, ae, deviceDN, new ArrayList<ModificationItem>(), preserveVendorData));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
        mergeChilds(diffs, prev, ae, aeDN);
    }

    private void mergeChilds(ConfigurationChanges diffs, ApplicationEntity prev, ApplicationEntity ae,
                             String aeDN) throws NamingException {
        merge(diffs, prev.getTransferCapabilities(), ae.getTransferCapabilities(), aeDN);
        for (LdapDicomConfigurationExtension ext : extensions)
            ext.mergeChilds(diffs, prev, ae, aeDN);
    }

    private void mergeWebApps(ConfigurationChanges diffs, Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        Collection<String> names = dev.getWebApplicationNames();
        for (String webAppName : prevDev.getWebApplicationNames()) {
            if (!names.contains(webAppName)) {
                String webAppDN = webAppDN(webAppName, deviceDN);
                destroySubcontextWithChilds(webAppDN);
                ConfigurationChanges.addModifiedObject(diffs, webAppDN, ConfigurationChanges.ChangeType.D);
            }
        }
        Collection<String> prevNames = prevDev.getWebApplicationNames();
        for (WebApplication webapp : dev.getWebApplications()) {
            String name = webapp.getApplicationName();
            if (!prevNames.contains(name)) {
                store(diffs, webapp, deviceDN);
            } else
                merge(diffs, prevDev.getWebApplication(name), webapp, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, WebApplication prev, WebApplication webapp, String deviceDN)
            throws NamingException {
        String webappDN = webAppDN(webapp.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, webappDN, ConfigurationChanges.ChangeType.U);
        modifyAttributes(webappDN, storeDiffs(ldapObj, prev, webapp, deviceDN, new ArrayList<ModificationItem>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
    }

    private void mergeKeycloakClients(ConfigurationChanges diffs, Device prevDev, Device dev, String deviceDN)
            throws NamingException {
        Collection<String> clientIDs = dev.getKeycloakClientIDs();
        for (String clientID : prevDev.getKeycloakClientIDs()) {
            if (!clientIDs.contains(clientID)) {
                String keycloakClientDN = keycloakClientDN(clientID, deviceDN);
                destroySubcontextWithChilds(keycloakClientDN);
                ConfigurationChanges.addModifiedObject(diffs, keycloakClientDN, ConfigurationChanges.ChangeType.D);
            }
        }
        Collection<String> prevClientIDs = prevDev.getKeycloakClientIDs();
        for (KeycloakClient client : dev.getKeycloakClients()) {
            String clientID = client.getKeycloakClientID();
            if (!prevClientIDs.contains(clientID)) {
                store(diffs, client, deviceDN);
            } else
                merge(diffs, prevDev.getKeycloakClient(clientID), client, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, KeycloakClient prev, KeycloakClient client, String deviceDN)
            throws NamingException {
        String clientDN = keycloakClientDN(client.getKeycloakClientID(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, clientDN, ConfigurationChanges.ChangeType.U);
        modifyAttributes(clientDN, storeDiffs(ldapObj, prev, client, new ArrayList<ModificationItem>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
    }

    public void modifyAttributes(String dn, List<ModificationItem> mods)
            throws NamingException {
        if (!mods.isEmpty())
            ctx.modifyAttributes(dn, mods.toArray(new ModificationItem[mods.size()]));
    }

    public void replaceAttributes(String dn, Attributes attrs)
            throws NamingException {
        ctx.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, attrs);
    }

    private void merge(ConfigurationChanges diffs, Collection<TransferCapability> prevs,
                       Collection<TransferCapability> tcs, String aeDN) throws NamingException {
        for (TransferCapability tc : prevs) {
            String dn = dnOf(tc, aeDN);
            if (findByDN(aeDN, tcs, dn) == null) {
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (TransferCapability tc : tcs) {
            String dn = dnOf(tc, aeDN);
            TransferCapability prev = findByDN(aeDN, prevs, dn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn,
                        storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                tc, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, tc, new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private void mergeConnections(ConfigurationChanges diffs, Device prevDev, Device device, String deviceDN)
            throws NamingException {
        List<Connection> prevs = prevDev.listConnections();
        List<Connection> conns = device.listConnections();
        for (Connection prev : prevs) {
            String dn = LdapBuilder.dnOf(prev, deviceDN);
            if (LdapBuilder.findByDN(deviceDN, conns, dn) == null) {
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Connection conn : conns) {
            String dn = LdapBuilder.dnOf(conn, deviceDN);
            Connection prev = LdapBuilder.findByDN(deviceDN, prevs, dn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn,
                        storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                conn, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, conn, new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    @Override
    public String deviceRef(String name) {
        return LdapBuilder.dnOf("dicomDeviceName", name, devicesDN);
    }

    public void store(ConfigurationChanges diffs, Map<String, BasicBulkDataDescriptor> descriptors, String parentDN)
            throws NamingException {
        for (BasicBulkDataDescriptor descriptor : descriptors.values()) {
            String dn = LdapBuilder.dnOf("dcmBulkDataDescriptorID",
                    descriptor.getBulkDataDescriptorID(), parentDN);
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
            createSubcontext(dn, storeTo(ldapObj, descriptor, new BasicAttributes(true)));
        }
    }

    public void load(Map<String, BasicBulkDataDescriptor> descriptors, String parentDN) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(parentDN, "(objectclass=dcmBulkDataDescriptor)");
        try {
            while (ne.hasMore()) {
                BasicBulkDataDescriptor descriptor = loadBulkDataDescriptor(ne.next());
                descriptors.put(descriptor.getBulkDataDescriptorID(), descriptor);
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private BasicBulkDataDescriptor loadBulkDataDescriptor(SearchResult sr) throws NamingException {
        Attributes attrs = sr.getAttributes();
        BasicBulkDataDescriptor descriptor = new BasicBulkDataDescriptor(
                LdapBuilder.stringValue(attrs.get("dcmBulkDataDescriptorID"), null));
        descriptor.excludeDefaults(LdapBuilder.booleanValue(attrs.get("dcmBulkDataExcludeDefaults"), false));
        descriptor.setAttributeSelectorsFromStrings(LdapBuilder.stringArray(attrs.get("dcmAttributeSelector")));
        descriptor.setLengthsThresholdsFromStrings(LdapBuilder.stringArray(attrs.get("dcmBulkDataVRLengthThreshold")));
        return descriptor;
    }

    public void merge(ConfigurationChanges diffs,
                      Map<String, BasicBulkDataDescriptor> prevs,
                      Map<String, BasicBulkDataDescriptor> descriptors,
                      String parentDN)
            throws NamingException {
        for (String prevBulkDataDescriptorID : prevs.keySet()) {
            if (!descriptors.containsKey(prevBulkDataDescriptorID)) {
                String dn = LdapBuilder.dnOf("dcmBulkDataDescriptorID", prevBulkDataDescriptorID, parentDN);
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Map.Entry<String, BasicBulkDataDescriptor> entry : descriptors.entrySet()) {
            String dn = LdapBuilder.dnOf("dcmBulkDataDescriptorID", entry.getKey(), parentDN);
            BasicBulkDataDescriptor prev = prevs.get(entry.getKey());
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn, storeTo(ldapObj, entry.getValue(), new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, entry.getValue(), new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj,
                                              BasicBulkDataDescriptor prev,
                                              BasicBulkDataDescriptor descriptor,
                                              ArrayList<ModificationItem> mods) {
        LdapBuilder.storeDiff(ldapObj, mods, "dcmBulkDataExcludeDefaults",
                prev.isExcludeDefaults(), descriptor.isExcludeDefaults(), false);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAttributeSelector",
                prev.getAttributeSelectors(), descriptor.getAttributeSelectors());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmBulkDataVRLengthThreshold",
                prev.getLengthsThresholdsAsStrings(), descriptor.getLengthsThresholdsAsStrings());
        return mods;
    }

    public void store(AttributeCoercions coercions, String parentDN)
            throws NamingException {
        for (AttributeCoercion ac : coercions)
            createSubcontext(
                    LdapBuilder.dnOf("cn", ac.getCommonName(), parentDN),
                    storeTo(ac, new BasicAttributes(true)));
    }

    public void load(AttributeCoercions acs, String dn) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                search(dn, "(objectclass=dcmAttributeCoercion)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                acs.add(new AttributeCoercion(
                        LdapBuilder.stringValue(attrs.get("cn"), null),
                        LdapBuilder.stringArray(attrs.get("dcmSOPClass")),
                        Dimse.valueOf(LdapBuilder.stringValue(attrs.get("dcmDIMSE"), null)),
                        TransferCapability.Role.valueOf(
                                LdapBuilder.stringValue(attrs.get("dicomTransferRole"), null)),
                        LdapBuilder.stringArray(attrs.get("dcmAETitle")),
                        LdapBuilder.stringValue(attrs.get("dcmURI"), null)));
            }
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    public void merge(ConfigurationChanges diffs, AttributeCoercions prevs, AttributeCoercions acs,
                      String parentDN) throws NamingException {
        for (AttributeCoercion prev : prevs) {
            String cn = prev.getCommonName();
            if (acs.findByCommonName(cn) == null) {
                String dn = LdapBuilder.dnOf("cn", cn, parentDN);
                destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (AttributeCoercion ac : acs) {
            String cn = ac.getCommonName();
            String dn = LdapBuilder.dnOf("cn", cn, parentDN);
            AttributeCoercion prev = prevs.findByCommonName(cn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                createSubcontext(dn, storeTo(ac, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                modifyAttributes(dn, storeDiffs(ldapObj, prev, ac, new ArrayList<>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, AttributeCoercion prev,
                                              AttributeCoercion ac, ArrayList<ModificationItem> mods) {
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmDIMSE", prev.getDIMSE(), ac.getDIMSE(), null);
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomTransferRole",
                prev.getRole(),
                ac.getRole(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAETitle",
                prev.getAETitles(),
                ac.getAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmSOPClass",
                prev.getSOPClasses(),
                ac.getSOPClasses());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmURI", prev.getURI(), ac.getURI(), null);
        return mods;
    }

    @Override
    public void sync() throws InternalException {
        // NOOP
    }

    @Override
    public synchronized ApplicationEntityInfo[] listAETInfos(ApplicationEntityInfo keys)
            throws InternalException {
        if (!configurationExists())
            return new ApplicationEntityInfo[0];

        ArrayList<ApplicationEntityInfo> results = new ArrayList<>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            String deviceName = keys.getDeviceName();
            ne = search(deviceName, AE_ATTRS, toFilter(keys));
            Map<String, Connection> connCache = new HashMap<>();
            while (ne.hasMore()) {
                ApplicationEntityInfo aetInfo = new ApplicationEntityInfo();
                SearchResult ne1 = ne.next();
                loadFrom(aetInfo, ne1.getAttributes(),
                        deviceName != null ? deviceName : LdapBuilder.cutDeviceName(ne1.getName()), connCache);
                results.add(aetInfo);
            }
        } catch (NameNotFoundException e) {
            return new ApplicationEntityInfo[0];
        } catch (NamingException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
        return results.toArray(new ApplicationEntityInfo[results.size()]);
    }

    @Override
    public synchronized WebApplication[] listWebApplicationInfos(WebApplication keys)
            throws InternalException {
        if (!configurationExists())
            return new WebApplication[0];

        ArrayList<WebApplication> results = new ArrayList<>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            String deviceName = keys.getDeviceName();
            ne = search(deviceName, WEBAPP_ATTRS, toFilter(keys));
            Map<String, Connection> connCache = new HashMap<>();
            Map<String, KeycloakClient> keycloakClientCache = new HashMap<>();
            while (ne.hasMore()) {
                WebApplication webappInfo = new WebApplication();
                SearchResult ne1 = ne.next();
                loadFrom(webappInfo, ne1.getAttributes(),
                        deviceName != null ? deviceName : LdapBuilder.cutDeviceName(ne1.getName()),
                        connCache, keycloakClientCache);
                results.add(webappInfo);
            }
        } catch (NameNotFoundException e) {
            return new WebApplication[0];
        } catch (NamingException e) {
            throw new InternalException(e);
        } finally {
            LdapBuilder.safeClose(ne);
        }
        return results.toArray(new WebApplication[results.size()]);
    }

    public NamingEnumeration<SearchResult> search(String deviceName, String[] attrsArray, String filter)
            throws NamingException {
        return deviceName != null
                ? search(deviceRef(deviceName), filter, attrsArray)
                : ctx.search(devicesDN, filter, searchControlSubtreeScope(0, attrsArray, true));
    }

}
