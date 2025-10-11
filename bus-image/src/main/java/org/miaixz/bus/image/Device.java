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
 * Represents a DICOM Device entity in a network.
 * <p>
 * This class encapsulates the configuration of a device, including its basic information (name, manufacturer), network
 * connections, Application Entities (AEs), security settings (TLS/SSL), and other operational parameters. A device can
 * contain multiple AEs, connections, web applications, and Keycloak clients.
 * <p>
 * The device supports TLS/SSL for secure communication and can be configured with trust stores, key stores, and
 * certificate management.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Device implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852253317829L;

    /**
     * Maps calling AE titles to the maximum number of associations they can initiate.
     */
    private final LinkedHashMap<String, Integer> limitAssociationsInitiatedBy = new LinkedHashMap<>();

    /**
     * Maps references to authorized node certificates for TLS peer authentication.
     */
    private final LinkedHashMap<String, X509Certificate[]> authorizedNodeCertificates = new LinkedHashMap<>();

    /**
     * Maps references to certificates for this node, used for TLS key management.
     */
    private final LinkedHashMap<String, X509Certificate[]> thisNodeCertificates = new LinkedHashMap<>();

    /**
     * The list of network connections configured for this device.
     */
    private final List<Connection> conns = new ArrayList<>();

    /**
     * Maps AE titles to their ApplicationEntity configurations.
     */
    private final LinkedHashMap<String, ApplicationEntity> aes = new LinkedHashMap<>();

    /**
     * Maps application names to their WebApplication configurations.
     */
    private final LinkedHashMap<String, WebApplication> webapps = new LinkedHashMap<>();

    /**
     * Maps client IDs to their KeycloakClient configurations.
     */
    private final LinkedHashMap<String, KeycloakClient> keycloakClients = new LinkedHashMap<>();

    /**
     * Maps extension classes to their respective DeviceExtension instances.
     */
    private final Map<Class<? extends DeviceExtension>, DeviceExtension> extensions = new HashMap<>();

    /**
     * A transient list of currently active associations with this device.
     */
    private transient final List<Association> associations = new ArrayList<>();

    /**
     * The unique name of the device.
     */
    private String deviceName;

    /**
     * The unique identifier of the device (Device UID).
     */
    private String deviceUID;

    /**
     * A textual description of the device.
     */
    private String description;

    /**
     * The manufacturer of the device.
     */
    private String manufacturer;

    /**
     * The manufacturer's model name for the device.
     */
    private String manufacturerModelName;

    /**
     * The name of the station where the device is located.
     */
    private String stationName;

    /**
     * The serial number of the device.
     */
    private String deviceSerialNumber;

    /**
     * The URL of the trust store for TLS connections.
     */
    private String trustStoreURL;

    /**
     * The type of the trust store (e.g., "JKS", "PKCS12").
     */
    private String trustStoreType;

    /**
     * The PIN (password) for the trust store.
     */
    private String trustStorePin;

    /**
     * A system property from which to read the trust store PIN.
     */
    private String trustStorePinProperty;

    /**
     * The URL of the key store for TLS connections.
     */
    private String keyStoreURL;

    /**
     * The type of the key store (e.g., "JKS", "PKCS12").
     */
    private String keyStoreType;

    /**
     * The PIN (password) for the key store.
     */
    private String keyStorePin;

    /**
     * A system property from which to read the key store PIN.
     */
    private String keyStorePinProperty;

    /**
     * The PIN (password) for the private key within the key store.
     */
    private String keyStoreKeyPin;

    /**
     * A system property from which to read the key store's key PIN.
     */
    private String keyStoreKeyPinProperty;

    /**
     * The issuer responsible for generating Patient IDs.
     */
    private Issuer issuerOfPatientID;

    /**
     * The issuer responsible for generating Accession Numbers.
     */
    private Issuer issuerOfAccessionNumber;

    /**
     * The identifier for the entity that places orders.
     */
    private Issuer orderPlacerIdentifier;

    /**
     * The identifier for the entity that fills orders.
     */
    private Issuer orderFillerIdentifier;

    /**
     * The issuer responsible for generating Admission IDs.
     */
    private Issuer issuerOfAdmissionID;

    /**
     * The issuer responsible for generating Service Episode IDs.
     */
    private Issuer issuerOfServiceEpisodeID;

    /**
     * The issuer responsible for generating Container Identifiers.
     */
    private Issuer issuerOfContainerIdentifier;

    /**
     * The issuer responsible for generating Specimen Identifiers.
     */
    private Issuer issuerOfSpecimenIdentifier;

    /**
     * The software versions running on or implemented by the device.
     */
    private String[] softwareVersions = {};

    /**
     * The primary types of the device (e.g., "CT", "MR").
     */
    private String[] primaryDeviceTypes = {};

    /**
     * The names of the institutions associated with this device.
     */
    private String[] institutionNames = {};

    /**
     * The codes of the institutions associated with this device.
     */
    private Code[] institutionCodes = {};

    /**
     * The addresses of the institutions associated with this device.
     */
    private String[] institutionAddresses = {};

    /**
     * The department names within the institutions associated with this device.
     */
    private String[] institutionalDepartmentNames = {};

    /**
     * References to other related devices.
     */
    private String[] relatedDeviceRefs = {};

    /**
     * Vendor-specific configuration data.
     */
    private byte[][] vendorData = {};

    /**
     * The maximum number of simultaneous open associations this device will accept. 0 for unlimited.
     */
    private int limitOpenAssociations;

    /**
     * A flag indicating if the device is currently installed and active on the network.
     */
    private boolean installed = true;

    /**
     * A flag to allow lenient handling of role selection negotiation in associations.
     */
    private boolean roleSelectionNegotiationLenient;

    /**
     * The timezone of the device.
     */
    private TimeZone timeZoneOfDevice;

    /**
     * A flag for enabling an archive-specific device extension.
     */
    private Boolean arcDevExt;

    /**
     * A transient handler for managing association lifecycle events.
     */
    private transient AssociationHandler associationHandler = new AssociationHandler();

    /**
     * A transient handler for processing incoming DIMSE-RQ messages.
     */
    private transient DimseRQHandler dimseRQHandler;

    /**
     * A transient monitor for tracking connection status.
     */
    private transient ConnectionMonitor connectionMonitor;

    /**
     * A transient monitor for tracking association status.
     */
    private transient AssociationMonitor associationMonitor;

    /**
     * A transient executor for running asynchronous tasks.
     */
    private transient Executor executor;

    /**
     * A transient scheduled executor for running delayed or periodic tasks.
     */
    private transient ScheduledExecutorService scheduledExecutor;

    /**
     * A transient, volatile SSLContext for TLS connections.
     */
    private transient volatile SSLContext sslContext;

    /**
     * A transient, volatile KeyManager for TLS.
     */
    private transient volatile KeyManager km;

    /**
     * A transient, volatile TrustManager for TLS.
     */
    private transient volatile TrustManager tm;

    /**
     * Default constructor for creating a Device.
     */
    public Device() {
    }

    /**
     * Constructs a Device with a specified name.
     *
     * @param name The name of the device.
     */
    public Device(String name) {
        setDeviceName(name);
    }

    /**
     * Converts a collection of certificate arrays into a single flat array of certificates.
     *
     * @param c The collection of certificate arrays.
     * @return A single array containing all certificates.
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
     * Checks if a given string value is not null and not empty.
     *
     * @param name The name of the parameter being checked.
     * @param val  The string value.
     * @throws IllegalArgumentException if the value is an empty string.
     */
    private void checkNotEmpty(String name, String val) {
        if (val != null && val.isEmpty())
            throw new IllegalArgumentException(name + " cannot be empty");
    }

    /**
     * Gets the name of this device.
     *
     * @return A string containing the device name.
     */
    public final String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets the name of this device.
     *
     * @param name A string containing the device name.
     */
    public final void setDeviceName(String name) {
        checkNotEmpty("Device Name", name);
        this.deviceName = name;
    }

    /**
     * Gets the description of this device.
     *
     * @return A string containing the device description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Sets the description of this device.
     *
     * @param description A string containing the device description.
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the Device UID.
     *
     * @return The Device UID.
     */
    public String getDeviceUID() {
        return deviceUID;
    }

    /**
     * Sets the Device UID.
     *
     * @param deviceUID The Device UID.
     */
    public void setDeviceUID(String deviceUID) {
        this.deviceUID = deviceUID;
    }

    /**
     * Gets the manufacturer of this device.
     *
     * @return A string containing the device manufacturer.
     */
    public final String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the manufacturer of this device. This should be the same as the value of Manufacturer (0008,0070) in SOP
     * Instances created by this device.
     *
     * @param manufacturer A string containing the device manufacturer.
     */
    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Gets the manufacturer's model name of this device.
     *
     * @return A string containing the manufacturer's model name.
     */
    public final String getManufacturerModelName() {
        return manufacturerModelName;
    }

    /**
     * Sets the manufacturer's model name of this device. This should be the same as the value of Manufacturer's Model
     * Name (0008,1090) in SOP Instances created by this device.
     *
     * @param manufacturerModelName A string containing the manufacturer's model name.
     */
    public final void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    /**
     * Gets the software versions running on (or implemented by) this device.
     *
     * @return An array of strings containing the software versions.
     */
    public final String[] getSoftwareVersions() {
        return softwareVersions;
    }

    /**
     * Sets the software versions running on (or implemented by) this device. This should be the same as the value of
     * Software Versions (0018,1020) in SOP Instances created by this device.
     *
     * @param softwareVersions An array of strings containing the software versions.
     */
    public final void setSoftwareVersions(String... softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    /**
     * Gets the station name associated with this device.
     *
     * @return A string containing the station name.
     */
    public final String getStationName() {
        return stationName;
    }

    /**
     * Sets the station name associated with this device. This should be the same as the value of Station Name
     * (0008,1010) in SOP Instances created by this device.
     *
     * @param stationName A string containing the station name.
     */
    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    /**
     * Gets the serial number of this device.
     *
     * @return A string containing the serial number.
     */
    public final String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    /**
     * Sets the serial number of this device. This should be the same as the value of Device Serial Number (0018,1000)
     * in SOP Instances created by this device.
     *
     * @param deviceSerialNumber A string containing the serial number.
     */
    public final void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    /**
     * Gets the primary device types associated with this device.
     *
     * @return An array of strings containing the primary device types.
     */
    public final String[] getPrimaryDeviceTypes() {
        return primaryDeviceTypes;
    }

    /**
     * Sets the primary device types for this device. These signify a kind of device, most applicable for an acquisition
     * modality. If applicable, types should be chosen from the list of UIDs for SOP Classes (0008,0100) in Context ID
     * 30 of PS3.16.
     *
     * @param primaryDeviceTypes The primary device types.
     */
    public void setPrimaryDeviceTypes(String... primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    /**
     * Gets the institution names associated with this device.
     *
     * @return An array of strings containing institution names.
     */
    public final String[] getInstitutionNames() {
        return institutionNames;
    }

    /**
     * Sets the institution names associated with this device. This should be the same as Institution Name (0008,0080)
     * in SOP Instances created by this device.
     *
     * @param names An array of strings containing institution names.
     */
    public void setInstitutionNames(String... names) {
        institutionNames = names;
    }

    /**
     * Gets the institution codes.
     *
     * @return An array of institution codes.
     */
    public final Code[] getInstitutionCodes() {
        return institutionCodes;
    }

    /**
     * Sets the institution codes.
     *
     * @param codes An array of institution codes.
     */
    public void setInstitutionCodes(Code... codes) {
        institutionCodes = codes;
    }

    /**
     * Gets the addresses of the institution operating this device.
     *
     * @return An array of strings containing institution addresses.
     */
    public final String[] getInstitutionAddresses() {
        return institutionAddresses;
    }

    /**
     * Sets the addresses of the institution operating this device. This should be the same as the value of Institution
     * Address (0008,0081) in SOP Instances created by this device.
     *
     * @param addresses An array of strings containing institution addresses.
     */
    public void setInstitutionAddresses(String... addresses) {
        institutionAddresses = addresses;
    }

    /**
     * Gets the department names associated with this device.
     *
     * @return An array of strings containing department names.
     */
    public final String[] getInstitutionalDepartmentNames() {
        return institutionalDepartmentNames;
    }

    /**
     * Sets the department names associated with this device. This should be the same as Institutional Department Name
     * (0008,1040) in SOP Instances created by this device.
     *
     * @param names An array of strings containing department names.
     */
    public void setInstitutionalDepartmentNames(String... names) {
        institutionalDepartmentNames = names;
    }

    /**
     * Gets the issuer of Patient IDs.
     *
     * @return The issuer of Patient IDs.
     */
    public final Issuer getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    /**
     * Sets the issuer of Patient IDs.
     *
     * @param issuerOfPatientID The issuer of Patient IDs.
     */
    public final void setIssuerOfPatientID(Issuer issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    /**
     * Gets the issuer of Accession Numbers.
     *
     * @return The issuer of Accession Numbers.
     */
    public final Issuer getIssuerOfAccessionNumber() {
        return issuerOfAccessionNumber;
    }

    /**
     * Sets the issuer of Accession Numbers.
     *
     * @param issuerOfAccessionNumber The issuer of Accession Numbers.
     */
    public final void setIssuerOfAccessionNumber(Issuer issuerOfAccessionNumber) {
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
    }

    /**
     * Gets the Order Placer Identifier.
     *
     * @return The Order Placer Identifier.
     */
    public final Issuer getOrderPlacerIdentifier() {
        return orderPlacerIdentifier;
    }

    /**
     * Sets the Order Placer Identifier.
     *
     * @param orderPlacerIdentifier The Order Placer Identifier.
     */
    public final void setOrderPlacerIdentifier(Issuer orderPlacerIdentifier) {
        this.orderPlacerIdentifier = orderPlacerIdentifier;
    }

    /**
     * Gets the Order Filler Identifier.
     *
     * @return The Order Filler Identifier.
     */
    public final Issuer getOrderFillerIdentifier() {
        return orderFillerIdentifier;
    }

    /**
     * Sets the Order Filler Identifier.
     *
     * @param orderFillerIdentifier The Order Filler Identifier.
     */
    public final void setOrderFillerIdentifier(Issuer orderFillerIdentifier) {
        this.orderFillerIdentifier = orderFillerIdentifier;
    }

    /**
     * Gets the issuer of Admission IDs.
     *
     * @return The issuer of Admission IDs.
     */
    public final Issuer getIssuerOfAdmissionID() {
        return issuerOfAdmissionID;
    }

    /**
     * Sets the issuer of Admission IDs.
     *
     * @param issuerOfAdmissionID The issuer of Admission IDs.
     */
    public final void setIssuerOfAdmissionID(Issuer issuerOfAdmissionID) {
        this.issuerOfAdmissionID = issuerOfAdmissionID;
    }

    /**
     * Gets the issuer of Service Episode IDs.
     *
     * @return The issuer of Service Episode IDs.
     */
    public final Issuer getIssuerOfServiceEpisodeID() {
        return issuerOfServiceEpisodeID;
    }

    /**
     * Sets the issuer of Service Episode IDs.
     *
     * @param issuerOfServiceEpisodeID The issuer of Service Episode IDs.
     */
    public final void setIssuerOfServiceEpisodeID(Issuer issuerOfServiceEpisodeID) {
        this.issuerOfServiceEpisodeID = issuerOfServiceEpisodeID;
    }

    /**
     * Gets the issuer of Container Identifiers.
     *
     * @return The issuer of Container Identifiers.
     */
    public final Issuer getIssuerOfContainerIdentifier() {
        return issuerOfContainerIdentifier;
    }

    /**
     * Sets the issuer of Container Identifiers.
     *
     * @param issuerOfContainerIdentifier The issuer of Container Identifiers.
     */
    public final void setIssuerOfContainerIdentifier(Issuer issuerOfContainerIdentifier) {
        this.issuerOfContainerIdentifier = issuerOfContainerIdentifier;
    }

    /**
     * Gets the issuer of Specimen Identifiers.
     *
     * @return The issuer of Specimen Identifiers.
     */
    public final Issuer getIssuerOfSpecimenIdentifier() {
        return issuerOfSpecimenIdentifier;
    }

    /**
     * Sets the issuer of Specimen Identifiers.
     *
     * @param issuerOfSpecimenIdentifier The issuer of Specimen Identifiers.
     */
    public final void setIssuerOfSpecimenIdentifier(Issuer issuerOfSpecimenIdentifier) {
        this.issuerOfSpecimenIdentifier = issuerOfSpecimenIdentifier;
    }

    /**
     * Gets the authorized node certificates for a given reference.
     *
     * @param ref The reference key.
     * @return An array of authorized certificates.
     */
    public X509Certificate[] getAuthorizedNodeCertificates(String ref) {
        return authorizedNodeCertificates.get(ref);
    }

    /**
     * Sets the authorized node certificates for a given reference.
     *
     * @param ref   The reference key.
     * @param certs The certificates to set.
     */
    public void setAuthorizedNodeCertificates(String ref, X509Certificate... certs) {
        authorizedNodeCertificates.put(ref, certs);
        setTrustManager(null);
    }

    /**
     * Removes the authorized node certificates for a given reference.
     *
     * @param ref The reference key.
     * @return The removed certificates.
     */
    public X509Certificate[] removeAuthorizedNodeCertificates(String ref) {
        X509Certificate[] certs = authorizedNodeCertificates.remove(ref);
        setTrustManager(null);
        return certs;
    }

    /**
     * Removes all authorized node certificates.
     */
    public void removeAllAuthorizedNodeCertificates() {
        authorizedNodeCertificates.clear();
        setTrustManager(null);
    }

    /**
     * Gets all authorized node certificates from all references.
     *
     * @return A flat array of all authorized certificates.
     */
    public X509Certificate[] getAllAuthorizedNodeCertificates() {
        return toArray(authorizedNodeCertificates.values());
    }

    /**
     * Gets all reference keys for authorized node certificates.
     *
     * @return An array of reference strings.
     */
    public String[] getAuthorizedNodeCertificateRefs() {
        return authorizedNodeCertificates.keySet().toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the Trust Store URL.
     *
     * @return The Trust Store URL.
     */
    public final String getTrustStoreURL() {
        return trustStoreURL;
    }

    /**
     * Sets the Trust Store URL.
     *
     * @param trustStoreURL The Trust Store URL.
     */
    public final void setTrustStoreURL(String trustStoreURL) {
        checkNotEmpty("trustStoreURL", trustStoreURL);
        if (Objects.equals(trustStoreURL, this.trustStoreURL))
            return;
        this.trustStoreURL = trustStoreURL;
        setTrustManager(null);
    }

    /**
     * Gets the Trust Store type.
     *
     * @return The Trust Store type.
     */
    public final String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * Sets the Trust Store type.
     *
     * @param trustStoreType The Trust Store type.
     */
    public final void setTrustStoreType(String trustStoreType) {
        checkNotEmpty("trustStoreType", trustStoreType);
        this.trustStoreType = trustStoreType;
    }

    /**
     * Gets the Trust Store PIN.
     *
     * @return The Trust Store PIN.
     */
    public final String getTrustStorePin() {
        return trustStorePin;
    }

    /**
     * Sets the Trust Store PIN.
     *
     * @param trustStorePin The Trust Store PIN.
     */
    public final void setTrustStorePin(String trustStorePin) {
        checkNotEmpty("trustStorePin", trustStorePin);
        this.trustStorePin = trustStorePin;
    }

    /**
     * Gets the system property name for the Trust Store PIN.
     *
     * @return The property name.
     */
    public final String getTrustStorePinProperty() {
        return trustStorePinProperty;
    }

    /**
     * Sets the system property name for the Trust Store PIN.
     *
     * @param trustStorePinProperty The property name.
     */
    public final void setTrustStorePinProperty(String trustStorePinProperty) {
        checkNotEmpty("trustStorePinProperty", trustStorePinProperty);
        this.trustStorePinProperty = trustStorePinProperty;
    }

    /**
     * Gets this node's certificates for a given reference.
     *
     * @param ref The reference key.
     * @return An array of certificates.
     */
    public X509Certificate[] getThisNodeCertificates(String ref) {
        return thisNodeCertificates.get(ref);
    }

    /**
     * Sets this node's certificates for a given reference.
     *
     * @param ref   The reference key.
     * @param certs The certificates to set.
     */
    public void setThisNodeCertificates(String ref, X509Certificate... certs) {
        thisNodeCertificates.put(ref, certs);
    }

    /**
     * Removes this node's certificates for a given reference.
     *
     * @param ref The reference key.
     * @return The removed certificates.
     */
    public X509Certificate[] removeThisNodeCertificates(String ref) {
        return thisNodeCertificates.remove(ref);
    }

    /**
     * Gets the Key Store URL.
     *
     * @return The Key Store URL.
     */
    public final String getKeyStoreURL() {
        return keyStoreURL;
    }

    /**
     * Sets the Key Store URL.
     *
     * @param keyStoreURL The Key Store URL.
     */
    public final void setKeyStoreURL(String keyStoreURL) {
        checkNotEmpty("keyStoreURL", keyStoreURL);
        if (Objects.equals(keyStoreURL, this.keyStoreURL))
            return;
        this.keyStoreURL = keyStoreURL;
        setKeyManager(null);
    }

    /**
     * Gets the Key Store type.
     *
     * @return The Key Store type.
     */
    public final String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Sets the Key Store type.
     *
     * @param keyStoreType The Key Store type.
     */
    public final void setKeyStoreType(String keyStoreType) {
        checkNotEmpty("keyStoreType", keyStoreType);
        this.keyStoreType = keyStoreType;
    }

    /**
     * Gets the Key Store PIN.
     *
     * @return The Key Store PIN.
     */
    public final String getKeyStorePin() {
        return keyStorePin;
    }

    /**
     * Sets the Key Store PIN.
     *
     * @param keyStorePin The Key Store PIN.
     */
    public final void setKeyStorePin(String keyStorePin) {
        checkNotEmpty("keyStorePin", keyStorePin);
        this.keyStorePin = keyStorePin;
    }

    /**
     * Gets the system property name for the Key Store PIN.
     *
     * @return The property name.
     */
    public final String getKeyStorePinProperty() {
        return keyStorePinProperty;
    }

    /**
     * Sets the system property name for the Key Store PIN.
     *
     * @param keyStorePinProperty The property name.
     */
    public final void setKeyStorePinProperty(String keyStorePinProperty) {
        checkNotEmpty("keyStorePinProperty", keyStorePinProperty);
        this.keyStorePinProperty = keyStorePinProperty;
    }

    /**
     * Gets the PIN for the private key within the Key Store.
     *
     * @return The key PIN.
     */
    public final String getKeyStoreKeyPin() {
        return keyStoreKeyPin;
    }

    /**
     * Sets the PIN for the private key within the Key Store.
     *
     * @param keyStoreKeyPin The key PIN.
     */
    public final void setKeyStoreKeyPin(String keyStoreKeyPin) {
        checkNotEmpty("keyStoreKeyPin", keyStoreKeyPin);
        this.keyStoreKeyPin = keyStoreKeyPin;
    }

    /**
     * Gets the system property name for the Key Store's key PIN.
     *
     * @return The property name.
     */
    public final String getKeyStoreKeyPinProperty() {
        return keyStoreKeyPinProperty;
    }

    /**
     * Sets the system property name for the Key Store's key PIN.
     *
     * @param keyStoreKeyPinProperty The property name.
     */
    public final void setKeyStoreKeyPinProperty(String keyStoreKeyPinProperty) {
        checkNotEmpty("keyStoreKeyPinProperty", keyStoreKeyPinProperty);
        this.keyStoreKeyPinProperty = keyStoreKeyPinProperty;
    }

    /**
     * Removes all of this node's certificates.
     */
    public void removeAllThisNodeCertificates() {
        thisNodeCertificates.clear();
    }

    /**
     * Gets all of this node's certificates from all references.
     *
     * @return A flat array of all certificates for this node.
     */
    public X509Certificate[] getAllThisNodeCertificates() {
        return toArray(thisNodeCertificates.values());
    }

    /**
     * Gets all reference keys for this node's certificates.
     *
     * @return An array of reference strings.
     */
    public String[] getThisNodeCertificateRefs() {
        return thisNodeCertificates.keySet().toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the references to related devices.
     *
     * @return An array of related device references.
     */
    public final String[] getRelatedDeviceRefs() {
        return relatedDeviceRefs;
    }

    /**
     * Sets the references to related devices.
     *
     * @param refs An array of related device references.
     */
    public void setRelatedDeviceRefs(String... refs) {
        relatedDeviceRefs = refs;
    }

    /**
     * Gets the device-specific vendor configuration data.
     *
     * @return An array of byte arrays containing vendor data.
     */
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * Sets the device-specific vendor configuration data.
     *
     * @param vendorData An array of byte arrays containing vendor data.
     */
    public void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * Gets a boolean indicating if this device is currently installed on the network. This is useful for
     * pre-configurations, mobile vans, and similar scenarios.
     *
     * @return {@code true} if this device is installed.
     */
    public final boolean isInstalled() {
        return installed;
    }

    /**
     * Sets a boolean indicating if this device is currently installed on the network.
     *
     * @param installed {@code true} if this device is installed.
     */
    public final void setInstalled(boolean installed) {
        if (this.installed == installed)
            return;
        this.installed = installed;
        needRebindConnections();
    }

    /**
     * Checks if role selection negotiation is lenient.
     *
     * @return {@code true} if role selection negotiation is lenient.
     */
    public boolean isRoleSelectionNegotiationLenient() {
        return roleSelectionNegotiationLenient;
    }

    /**
     * Sets whether role selection negotiation should be lenient.
     *
     * @param roleSelectionNegotiationLenient {@code true} for lenient negotiation.
     */
    public void setRoleSelectionNegotiationLenient(boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    /**
     * Gets the timezone of the device.
     *
     * @return The device's timezone.
     */
    public TimeZone getTimeZoneOfDevice() {
        return timeZoneOfDevice;
    }

    /**
     * Sets the timezone of the device.
     *
     * @param timeZoneOfDevice The device's timezone.
     */
    public void setTimeZoneOfDevice(TimeZone timeZoneOfDevice) {
        this.timeZoneOfDevice = timeZoneOfDevice;
    }

    /**
     * Gets the DIMSE-RQ handler for this device.
     *
     * @return The DIMSE-RQ handler.
     */
    public final DimseRQHandler getDimseRQHandler() {
        return dimseRQHandler;
    }

    /**
     * Sets the DIMSE-RQ handler for this device.
     *
     * @param dimseRQHandler The DIMSE-RQ handler.
     */
    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    /**
     * Gets the archive device extension flag.
     *
     * @return The archive device extension flag.
     */
    public Boolean getArcDevExt() {
        return arcDevExt;
    }

    /**
     * Sets the archive device extension flag.
     *
     * @param arcDevExt The archive device extension flag.
     */
    public void setArcDevExt(Boolean arcDevExt) {
        this.arcDevExt = arcDevExt;
    }

    /**
     * Gets the association handler for this device.
     *
     * @return The association handler.
     */
    public final AssociationHandler getAssociationHandler() {
        return associationHandler;
    }

    /**
     * Sets the association handler for this device.
     *
     * @param associationHandler The association handler.
     * @throws NullPointerException if the handler is null.
     */
    public void setAssociationHandler(AssociationHandler associationHandler) {
        if (associationHandler == null)
            throw new NullPointerException();
        this.associationHandler = associationHandler;
    }

    /**
     * Gets the connection monitor.
     *
     * @return The connection monitor.
     */
    public ConnectionMonitor getConnectionMonitor() {
        return connectionMonitor;
    }

    /**
     * Sets the connection monitor.
     *
     * @param connectionMonitor The connection monitor.
     */
    public void setConnectionMonitor(ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
    }

    /**
     * Gets the association monitor.
     *
     * @return The association monitor.
     */
    public AssociationMonitor getAssociationMonitor() {
        return associationMonitor;
    }

    /**
     * Sets the association monitor.
     *
     * @param associationMonitor The association monitor.
     */
    public void setAssociationMonitor(AssociationMonitor associationMonitor) {
        this.associationMonitor = associationMonitor;
    }

    /**
     * Binds all configured connections, making them active and ready to listen for incoming associations.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws GeneralSecurityException if a security error occurs (e.g., with TLS configuration).
     */
    public void bindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            con.bind();
    }

    /**
     * Rebinds all connections that have been marked as needing a rebind.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void rebindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : conns)
            if (con.isRebindNeeded())
                con.rebind();
    }

    /**
     * Marks all connections as needing to be rebound.
     */
    private void needRebindConnections() {
        for (Connection con : conns)
            con.needRebind();
    }

    /**
     * Triggers a TLS reconfiguration by marking TLS connections for rebinding and clearing the cached SSL context.
     */
    private void needReconfigureTLS() {
        for (Connection con : conns)
            if (con.isTls())
                con.needRebind();
        sslContext = null;
    }

    /**
     * Unbinds all configured connections, closing any open server sockets.
     */
    public void unbindConnections() {
        for (Connection con : conns)
            con.unbind();
    }

    /**
     * Gets the executor for asynchronous tasks.
     *
     * @return The executor.
     */
    public final Executor getExecutor() {
        return executor;
    }

    /**
     * Sets the executor for asynchronous tasks.
     *
     * @param executor The executor.
     */
    public final void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * Gets the scheduled executor for delayed or periodic tasks.
     *
     * @return The scheduled executor service.
     */
    public final ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * Sets the scheduled executor for delayed or periodic tasks.
     *
     * @param executor The scheduled executor service.
     */
    public final void setScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = executor;
    }

    /**
     * Adds a network connection to this device.
     *
     * @param conn The connection to add.
     */
    public void addConnection(Connection conn) {
        conn.setDevice(this);
        conns.add(conn);
        conn.needRebind();
    }

    /**
     * Removes a network connection from this device.
     *
     * @param conn The connection to remove.
     * @return {@code true} if the connection was successfully removed.
     * @throws IllegalStateException if the connection is still in use by an Application Entity.
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
     * Returns an unmodifiable list of the connections configured for this device.
     *
     * @return A list of connections.
     */
    public List<Connection> listConnections() {
        return Collections.unmodifiableList(conns);
    }

    /**
     * Finds a configured connection that has an equal distinguished name (RDN) to the given connection.
     *
     * @param other The connection to compare against.
     * @return The matching connection, or null if not found.
     */
    public Connection connectionWithEqualsRDN(Connection other) {
        for (Connection conn : conns)
            if (conn.equalsRDN(other))
                return conn;
        return null;
    }

    /**
     * Adds an Application Entity to this device.
     *
     * @param ae The Application Entity to add.
     */
    public void addApplicationEntity(ApplicationEntity ae) {
        ae.setDevice(this);
        aes.put(ae.getAETitle(), ae);
    }

    /**
     * Removes an Application Entity from this device.
     *
     * @param ae The Application Entity to remove.
     * @return The removed Application Entity, or null if it was not found.
     */
    public ApplicationEntity removeApplicationEntity(ApplicationEntity ae) {
        return removeApplicationEntity(ae.getAETitle());
    }

    /**
     * Removes an Application Entity from this device by its AE title.
     *
     * @param aet The AE title of the Application Entity to remove.
     * @return The removed Application Entity, or null if it was not found.
     */
    public ApplicationEntity removeApplicationEntity(String aet) {
        ApplicationEntity ae = aes.remove(aet);
        if (ae != null)
            ae.setDevice(null);
        return ae;
    }

    /**
     * Gets the names of all configured web applications.
     *
     * @return A collection of web application names.
     */
    public Collection<String> getWebApplicationNames() {
        return webapps.keySet();
    }

    /**
     * Gets all configured web applications.
     *
     * @return A collection of web applications.
     */
    public Collection<WebApplication> getWebApplications() {
        return webapps.values();
    }

    /**
     * Gets all web applications that provide a specific service class.
     *
     * @param serviceClass The service class to look for.
     * @return A collection of matching web applications.
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
     * Gets a web application by its name.
     *
     * @param name The name of the web application.
     * @return The web application, or null if not found.
     */
    public WebApplication getWebApplication(String name) {
        return webapps.get(name);
    }

    /**
     * Adds a web application to this device.
     *
     * @param webapp The web application to add.
     */
    public void addWebApplication(WebApplication webapp) {
        webapp.setDevice(this);
        webapps.put(webapp.getApplicationName(), webapp);
    }

    /**
     * Removes a web application from this device.
     *
     * @param webapp The web application to remove.
     * @return The removed web application, or null if not found.
     */
    public WebApplication removeWebApplication(WebApplication webapp) {
        return removeWebApplication(webapp.getApplicationName());
    }

    /**
     * Removes a web application by its name.
     *
     * @param name The name of the web application to remove.
     * @return The removed web application, or null if not found.
     */
    public WebApplication removeWebApplication(String name) {
        WebApplication webapp = webapps.remove(name);
        if (webapp != null)
            webapp.setDevice(null);
        return webapp;
    }

    /**
     * Gets the client IDs of all configured Keycloak clients.
     *
     * @return A collection of client IDs.
     */
    public Collection<String> getKeycloakClientIDs() {
        return keycloakClients.keySet();
    }

    /**
     * Gets all configured Keycloak clients.
     *
     * @return A collection of Keycloak clients.
     */
    public Collection<KeycloakClient> getKeycloakClients() {
        return keycloakClients.values();
    }

    /**
     * Gets a Keycloak client by its client ID.
     *
     * @param clientID The client ID.
     * @return The Keycloak client, or null if not found.
     */
    public KeycloakClient getKeycloakClient(String clientID) {
        return keycloakClients.get(clientID);
    }

    /**
     * Adds a Keycloak client to this device.
     *
     * @param client The Keycloak client to add.
     */
    public void addKeycloakClient(KeycloakClient client) {
        client.setDevice(this);
        keycloakClients.put(client.getKeycloakClientID(), client);
    }

    /**
     * Removes a Keycloak client from this device.
     *
     * @param client The Keycloak client to remove.
     * @return The removed client, or null if not found.
     */
    public KeycloakClient removeKeycloakClient(KeycloakClient client) {
        return removeKeycloakClient(client.getKeycloakClientID());
    }

    /**
     * Removes a Keycloak client by its client ID.
     *
     * @param name The client ID.
     * @return The removed client, or null if not found.
     */
    public KeycloakClient removeKeycloakClient(String name) {
        KeycloakClient client = keycloakClients.remove(name);
        if (client != null)
            client.setDevice(null);
        return client;
    }

    /**
     * Adds a device extension to this device.
     *
     * @param ext The device extension to add.
     * @throws IllegalStateException if an extension of the same class already exists.
     */
    public void addDeviceExtension(DeviceExtension ext) {
        Class<? extends DeviceExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains Device Extension:" + clazz);
        ext.setDevice(this);
        extensions.put(clazz, ext);
    }

    /**
     * Removes a device extension from this device.
     *
     * @param ext The device extension to remove.
     * @return {@code true} if the extension was successfully removed.
     */
    public boolean removeDeviceExtension(DeviceExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;
        ext.setDevice(null);
        return true;
    }

    /**
     * Gets the limit on the total number of open associations this device will accept.
     *
     * @return The maximum number of open associations, or 0 for unlimited.
     */
    public final int getLimitOpenAssociations() {
        return limitOpenAssociations;
    }

    /**
     * Sets the limit on the total number of open associations this device will accept.
     *
     * @param limit The maximum number of open associations, or 0 for unlimited.
     */
    public final void setLimitOpenAssociations(int limit) {
        if (limit < 0)
            throw new IllegalArgumentException("limit: " + limit);
        this.limitOpenAssociations = limit;
    }

    /**
     * Returns the maximum number of open associations that can be initiated by the specified remote AE. If this limit
     * is exceeded, further association requests from that AE will be rejected.
     *
     * @param callingAET The AE title of the remote AE.
     * @return The maximum number of open associations, or 0 for unlimited.
     */
    public int getLimitAssociationsInitiatedBy(String callingAET) {
        Integer value = limitAssociationsInitiatedBy.get(Objects.requireNonNull(callingAET));
        return value != null ? value : 0;
    }

    /**
     * Sets the maximum number of open associations that can be initiated by the specified remote AE.
     *
     * @param callingAET The AE title of the remote AE.
     * @param limit      The maximum number of open associations, or 0 for unlimited.
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
     * Gets an array of all remote AEs and their association limits. Each array element is formatted as "AETitle=limit".
     *
     * @return An array of strings with AE titles and their limits.
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
     * Sets the association limits for remote AEs from an array of strings. Each array element must be formatted as
     * "AETitle=limit".
     *
     * @param values An array of strings with AE titles and their limits.
     * @throws IllegalArgumentException if any string in the array is incorrectly formatted.
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
     * Sets the association limits from a map, clearing any previous limits.
     *
     * @param tmp A map containing AE titles and their association limits.
     */
    private void setLimitAssociationsInitiatedBy(Map<String, Integer> tmp) {
        limitAssociationsInitiatedBy.clear();
        limitAssociationsInitiatedBy.putAll(tmp);
    }

    /**
     * Adds a newly established association to the list of active associations.
     *
     * @param as The association to add.
     */
    public void addAssociation(Association as) {
        synchronized (associations) {
            associations.add(as);
        }
    }

    /**
     * Removes an association from the list of active associations.
     *
     * @param as The association to remove.
     */
    public void removeAssociation(Association as) {
        synchronized (associations) {
            associations.remove(as);
            associations.notifyAll();
        }
    }

    /**
     * Returns an array of all currently open associations.
     *
     * @return An array of active associations.
     */
    public Association[] listOpenAssociations() {
        synchronized (associations) {
            return associations.toArray(new Association[0]);
        }
    }

    /**
     * Gets the current number of open associations.
     *
     * @return The number of open associations.
     */
    public int getNumberOfOpenAssociations() {
        return associations.size();
    }

    /**
     * Gets the number of associations initiated by a specific remote AE.
     *
     * @param callingAET The AE title of the remote AE.
     * @return The number of associations initiated by that AE.
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
     * Gets the number of associations initiated to a specific local AE.
     *
     * @param calledAET The AE title of the local AE.
     * @return The number of associations initiated to that AE.
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
     * Waits until there are no open associations. This method blocks until all active connections are closed.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void waitForNoOpenConnections() throws InterruptedException {
        synchronized (associations) {
            while (!associations.isEmpty())
                associations.wait();
        }
    }

    /**
     * Checks if an incoming association request would exceed configured limits.
     *
     * @param rq The A-ASSOCIATE-RQ PDU.
     * @return {@code true} if the association limit is exceeded, {@code false} otherwise.
     */
    public boolean isLimitOfAssociationsExceeded(AAssociateRQ rq) {
        Integer limit;
        return limitOpenAssociations > 0 && associations.size() >= limitOpenAssociations
                || (limit = limitAssociationsInitiatedBy.get(rq.getCallingAET())) != null
                        && getNumberOfAssociationsInitiatedBy(rq.getCallingAET()) >= limit;
    }

    /**
     * Gets an Application Entity by its AE title.
     *
     * @param aet The AE title.
     * @return The Application Entity, or null if not found.
     */
    public ApplicationEntity getApplicationEntity(String aet) {
        return aes.get(aet);
    }

    /**
     * Gets an Application Entity by its AE title, with support for wildcards and alternative AE titles.
     *
     * @param aet            The AE title to match.
     * @param matchOtherAETs If true, also checks alternative AE titles defined in the AE configurations.
     * @return The matching Application Entity, or null if not found.
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
     * Gets the AE titles of all configured Application Entities.
     *
     * @return A collection of AE titles.
     */
    public Collection<String> getApplicationAETitles() {
        return aes.keySet();
    }

    /**
     * Gets all configured Application Entities.
     *
     * @return A collection of Application Entities.
     */
    public Collection<ApplicationEntity> getApplicationEntities() {
        return aes.values();
    }

    /**
     * Gets the configured KeyManager for TLS.
     *
     * @return The KeyManager.
     */
    public final KeyManager getKeyManager() {
        return km;
    }

    /**
     * Sets a custom KeyManager for TLS.
     *
     * @param km The KeyManager to set.
     */
    public final void setKeyManager(KeyManager km) {
        this.km = km;
        needReconfigureTLS();
    }

    /**
     * Lazily initializes and returns the KeyManager based on the device's key store configuration.
     *
     * @return The initialized KeyManager.
     * @throws GeneralSecurityException if there is a security-related error.
     * @throws IOException              if the key store cannot be read.
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
     * Gets the Key Store type, throwing an exception if not configured.
     *
     * @return The Key Store type.
     */
    private String keyStoreType() {
        if (keyStoreType == null)
            throw new IllegalStateException("keyStoreURL requires keyStoreType");
        return keyStoreType;
    }

    /**
     * Gets the Key Store PIN, resolving from system properties if necessary.
     *
     * @return The Key Store PIN.
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
     * Gets the key PIN, falling back to the key store PIN if not specified.
     *
     * @param keyStorePin The key store PIN to use as a fallback.
     * @return The key PIN.
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
     * Gets the configured TrustManager for TLS.
     *
     * @return The TrustManager.
     */
    public final TrustManager getTrustManager() {
        return tm;
    }

    /**
     * Sets a custom TrustManager for TLS.
     *
     * @param tm The TrustManager to set.
     */
    public final void setTrustManager(TrustManager tm) {
        this.tm = tm;
        needReconfigureTLS();
    }

    /**
     * Lazily initializes and returns the TrustManager based on the device's trust store configuration.
     *
     * @return The initialized TrustManager.
     * @throws GeneralSecurityException if there is a security-related error.
     * @throws IOException              if the trust store cannot be read.
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
     * Gets the Trust Store type, throwing an exception if not configured.
     *
     * @return The Trust Store type.
     */
    private String trustStoreType() {
        if (trustStoreType == null)
            throw new IllegalStateException("trustStoreURL requires trustStoreType");
        return trustStoreType;
    }

    /**
     * Gets the Trust Store PIN, resolving from system properties if necessary.
     *
     * @return The Trust Store PIN.
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
     * Lazily initializes and returns the SSLContext for TLS connections.
     *
     * @return The initialized SSLContext.
     * @throws GeneralSecurityException if there is a security-related error.
     * @throws IOException              if the key/trust stores cannot be read.
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
     * Returns the configured KeyManagers as an array.
     *
     * @return An array containing the KeyManager, or null.
     * @throws GeneralSecurityException if there is a security-related error.
     * @throws IOException              if the key store cannot be read.
     */
    public KeyManager[] keyManagers() throws GeneralSecurityException, IOException {
        KeyManager tmp = km();
        return tmp != null ? new KeyManager[] { tmp } : null;
    }

    /**
     * Returns the configured TrustManagers as an array.
     *
     * @return An array containing the TrustManager, or null.
     * @throws GeneralSecurityException if there is a security-related error.
     * @throws IOException              if the trust store cannot be read.
     */
    public TrustManager[] trustManagers() throws GeneralSecurityException, IOException {
        TrustManager tmp = tm();
        return tmp != null ? new TrustManager[] { tmp } : null;
    }

    /**
     * Executes a command using the device's configured executor.
     *
     * @param command The command to execute.
     * @throws IllegalStateException if the executor has not been initialized.
     */
    public void execute(Runnable command) {
        if (executor == null)
            throw new IllegalStateException("executor not initialized");
        executor.execute(command);
    }

    /**
     * Schedules a command for one-shot execution after a given delay.
     *
     * @param command The command to schedule.
     * @param delay   The delay before execution.
     * @param unit    The time unit of the delay.
     * @return A ScheduledFuture representing the pending task.
     * @throws IllegalStateException if the scheduled executor has not been initialized.
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException("scheduled executor service not initialized");
        return scheduledExecutor.schedule(command, delay, unit);
    }

    /**
     * Schedules a command for periodic execution at a fixed rate.
     *
     * @param command      The command to schedule.
     * @param initialDelay The initial delay.
     * @param period       The period between executions.
     * @param unit         The time unit.
     * @return A ScheduledFuture representing the pending task.
     * @throws IllegalStateException if the scheduled executor has not been initialized.
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException("scheduled executor service not initialized");
        return scheduledExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * Schedules a command for periodic execution with a fixed delay between the end of one execution and the start of
     * the next.
     *
     * @param command      The command to schedule.
     * @param initialDelay The initial delay.
     * @param delay        The delay between executions.
     * @param unit         The time unit.
     * @return A ScheduledFuture representing the pending task.
     * @throws IllegalStateException if the scheduled executor has not been initialized.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (scheduledExecutor == null)
            throw new IllegalStateException("scheduled executor service not initialized");
        return scheduledExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    /**
     * Returns a string representation of the device and its main components.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    /**
     * Appends a formatted string representation of the device to a StringBuilder.
     *
     * @param sb     The StringBuilder to append to.
     * @param indent The indentation string to use for formatting.
     * @return The StringBuilder with the appended information.
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
     * Reconfigures this device with the settings from another device instance.
     *
     * @param from The source device from which to copy the configuration.
     * @throws IOException              if a connection rebinding error occurs.
     * @throws GeneralSecurityException if a TLS configuration error occurs.
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
     * Copies all basic attributes from another device to this one.
     *
     * @param from The source device.
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
     * Sets the authorized node certificates from a source map.
     *
     * @param from The source map of certificates.
     */
    private void setAuthorizedNodeCertificates(Map<String, X509Certificate[]> from) {
        if (update(authorizedNodeCertificates, from))
            setTrustManager(null);
    }

    /**
     * Sets this node's certificates from a source map.
     *
     * @param from The source map of certificates.
     */
    private void setThisNodeCertificates(Map<String, X509Certificate[]> from) {
        update(thisNodeCertificates, from);
    }

    /**
     * Updates a target map of certificates from a source map.
     *
     * @param target The target map to update.
     * @param from   The source map.
     * @return {@code true} if the target map was modified.
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
     * Reconfigures the connections of this device based on a source device's configuration.
     *
     * @param from The source device.
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
     * Reconfigures the Application Entities of this device based on a source device's configuration.
     *
     * @param from The source device.
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
     * Reconfigures the Web Applications of this device based on a source device's configuration.
     *
     * @param from The source device.
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
     * Reconfigures the Keycloak clients of this device based on a source device's configuration.
     *
     * @param from The source device.
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
     * Reconfigures a list of connections to match a source list, based on RDN equality.
     *
     * @param conns The target list of connections to reconfigure.
     * @param src   The source list of connections.
     */
    public void reconfigureConnections(List<Connection> conns, List<Connection> src) {
        conns.clear();
        for (Connection conn : src)
            conns.add(connectionWithEqualsRDN(conn));
    }

    /**
     * Reconfigures the device extensions of this device based on a source device's configuration.
     *
     * @param from The source device.
     */
    private void reconfigureDeviceExtensions(Device from) {
        extensions.keySet().removeIf(aClass -> !from.extensions.containsKey(aClass));
        for (DeviceExtension src : from.extensions.values()) {
            Class<? extends DeviceExtension> clazz = src.getClass();
            DeviceExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addDeviceExtension(ext = clazz.getConstructor().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    /**
     * Lists all device extensions installed on this device.
     *
     * @return A collection of device extensions.
     */
    public Collection<DeviceExtension> listDeviceExtensions() {
        return extensions.values();
    }

    /**
     * Gets the device extension of a specific class.
     *
     * @param <T>   The type of the device extension.
     * @param clazz The class of the device extension.
     * @return The device extension instance, or null if not found.
     */
    public <T extends DeviceExtension> T getDeviceExtension(Class<T> clazz) {
        return clazz.cast(extensions.get(clazz));
    }

    /**
     * Gets the device extension of a specific class, throwing an exception if it is not found.
     *
     * @param <T>   The type of the device extension.
     * @param clazz The class of the device extension.
     * @return The device extension instance.
     * @throws IllegalStateException if the specified extension is not configured for this device.
     */
    public <T extends DeviceExtension> T getDeviceExtensionNotNull(Class<T> clazz) {
        T devExt = getDeviceExtension(clazz);
        if (devExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for Device: " + deviceName);
        return devExt;
    }

}
