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
package org.miaixz.bus.image.metric.json;

import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.galaxy.io.BasicBulkDataDescriptor;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.hl7.net.HL7ApplicationInfo;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.ApplicationEntityInfo;
import org.miaixz.bus.image.metric.net.KeycloakClient;

import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonConfiguration {

    private final List<JsonConfigurationExtension> extensions = new ArrayList<>();

    public void addJsonConfigurationExtension(JsonConfigurationExtension ext) {
        extensions.add(ext);
        ext.setJsonConfiguration(this);
    }

    public boolean removeJsonConfigurationExtension(JsonConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        ext.setJsonConfiguration(null);
        return true;
    }

    public <T extends JsonConfigurationExtension> T getJsonConfigurationExtension(Class<T> clazz) {
        for (JsonConfigurationExtension extension : extensions) {
            if (clazz.isAssignableFrom(extension.getClass()))
                return (T) extension;
        }
        return null;
    }

    public void writeTo(Device deviceInfo, JsonGenerator gen) {
        JSONWriter writer = new JSONWriter(gen);
        gen.writeStartObject();
        gen.write("dicomDeviceName", deviceInfo.getDeviceName());
        writer.writeNotNullOrDef("dicomDescription", deviceInfo.getDescription(), null);
        writer.writeNotNullOrDef("dicomManufacturer", deviceInfo.getManufacturer(), null);
        writer.writeNotNullOrDef("dicomManufacturerModelName", deviceInfo.getManufacturerModelName(), null);
        writer.writeNotEmpty("dicomSoftwareVersion", deviceInfo.getSoftwareVersions());
        writer.writeNotNullOrDef("dicomStationName", deviceInfo.getStationName(), null);
        writer.writeNotEmpty("dicomInstitutionName", deviceInfo.getInstitutionNames());
        writer.writeNotEmpty("dicomInstitutionDepartmentName", deviceInfo.getInstitutionalDepartmentNames());
        writer.writeNotEmpty("dicomPrimaryDeviceType", deviceInfo.getPrimaryDeviceTypes());
        gen.write("dicomInstalled", deviceInfo.isInstalled());
        gen.write("hasArcDevExt", deviceInfo.getArcDevExt());
        gen.writeEnd();
    }

    public void writeTo(ApplicationEntityInfo aetInfo, JsonGenerator gen) {
        JSONWriter writer = new JSONWriter(gen);
        gen.writeStartObject();
        writer.writeNotNullOrDef("dicomDeviceName", aetInfo.getDeviceName(), null);
        writer.writeNotNullOrDef("dicomAETitle", aetInfo.getAETitle(), null);
        writer.writeNotEmpty("dcmOtherAETitle", aetInfo.getOtherAETitle());
        writer.writeNotNullOrDef("dicomDescription", aetInfo.getDescription(), null);
        gen.write("dicomAssociationInitiator", aetInfo.getAssociationInitiator());
        gen.write("dicomAssociationAcceptor", aetInfo.getAssociationAcceptor());
        writer.writeNotEmpty("dicomApplicationCluster", aetInfo.getApplicationClusters());
        writer.writeNotNull("dicomInstalled", aetInfo.getInstalled());
        writer.writeNotNullOrDef("hl7ApplicationName", aetInfo.getHl7ApplicationName(), null);
        writeNotExtendedConns(aetInfo.getConnections(), writer);
        gen.writeEnd();
    }

    public void writeTo(WebApplication webapp, JsonGenerator gen) {
        writeTo(webapp, gen, webapp.getKeycloakClientID());
    }

    public void writeTo(WebApplication webapp, JsonGenerator gen, String keycloakClientID) {
        JSONWriter writer = new JSONWriter(gen);
        gen.writeStartObject();
        writer.writeNotNullOrDef("dicomDeviceName", webapp.getDeviceName(), null);
        writer.writeNotNullOrDef("dcmWebAppName", webapp.getApplicationName(), null);
        writer.writeNotNullOrDef("dicomDescription", webapp.getDescription(), null);
        writer.writeNotNullOrDef("dcmWebServicePath", webapp.getServicePath(), null);
        writer.writeNotNullOrDef("dcmKeycloakClientID", keycloakClientID, null);
        writer.writeNotEmpty("dcmWebServiceClass", webapp.getServiceClasses());
        writer.writeNotNullOrDef("dicomAETitle", webapp.getAETitle(), null);
        writer.writeNotEmpty("dicomApplicationCluster", webapp.getApplicationClusters());
        writer.writeNotEmpty("dcmProperty", webapp.getProperties());
        writer.writeNotNull("dicomInstalled", webapp.getInstalled());
        writeNotExtendedConns(webapp.getConnections(), writer);
        gen.writeEnd();
    }

    private void writeNotExtendedConns(List<Connection> connections, JSONWriter writer) {
        if (!connections.isEmpty()) {
            writer.writeStartArray("dicomNetworkConnection");
            for (Connection conn : connections)
                writeTo(conn, writer, false);
            writer.writeEnd();
        }
    }

    public void writeTo(HL7ApplicationInfo hl7AppInfo, JsonGenerator gen) {
        JSONWriter writer = new JSONWriter(gen);
        gen.writeStartObject();
        writer.writeNotNullOrDef("dicomDeviceName", hl7AppInfo.getDeviceName(), null);
        writer.writeNotNullOrDef("hl7ApplicationName", hl7AppInfo.getHl7ApplicationName(), null);
        writer.writeNotEmpty("hl7OtherApplicationName", hl7AppInfo.getHl7OtherApplicationName());
        writer.writeNotNullOrDef("dicomDescription", hl7AppInfo.getDescription(), null);
        writer.writeNotEmpty("dicomApplicationCluster", hl7AppInfo.getApplicationClusters());
        writer.writeNotNull("dicomInstalled", hl7AppInfo.getInstalled());
        writeNotExtendedConns(hl7AppInfo.getConnections(), writer);
        gen.writeEnd();
    }

    public void writeTo(Device device, JsonGenerator gen, boolean extended) {
        JSONWriter writer = new JSONWriter(gen);
        writer.writeStartObject();
        writer.writeNotNullOrDef("dicomDeviceName", device.getDeviceName(), null);
        writer.writeNotNullOrDef("dicomDescription", device.getDescription(), null);
        writer.writeNotNullOrDef("dicomDeviceUID", device.getDeviceUID(), null);
        writer.writeNotNullOrDef("dicomManufacturer", device.getManufacturer(), null);
        writer.writeNotNullOrDef("dicomManufacturerModelName", device.getManufacturerModelName(), null);
        writer.writeNotEmpty("dicomSoftwareVersion", device.getSoftwareVersions());
        writer.writeNotNullOrDef("dicomStationName", device.getStationName(), null);
        writer.writeNotNullOrDef("dicomDeviceSerialNumber", device.getDeviceSerialNumber(), null);
        writer.writeNotNullOrDef("dicomIssuerOfPatientID", device.getIssuerOfPatientID(), null);
        writer.writeNotNullOrDef("dicomIssuerOfAccessionNumber", device.getIssuerOfAccessionNumber(), null);
        writer.writeNotNullOrDef("dicomOrderPlacerIdentifier", device.getOrderPlacerIdentifier(), null);
        writer.writeNotNullOrDef("dicomOrderFillerIdentifier", device.getOrderFillerIdentifier(), null);
        writer.writeNotNullOrDef("dicomIssuerOfAdmissionID", device.getIssuerOfAdmissionID(), null);
        writer.writeNotNullOrDef("dicomIssuerOfServiceEpisodeID", device.getIssuerOfServiceEpisodeID(), null);
        writer.writeNotNullOrDef("dicomIssuerOfContainerIdentifier", device.getIssuerOfContainerIdentifier(), null);
        writer.writeNotNullOrDef("dicomIssuerOfSpecimenIdentifier", device.getIssuerOfSpecimenIdentifier(), null);
        writer.writeNotEmpty("dicomInstitutionName", device.getInstitutionNames());
        writer.writeNotEmpty("dicomInstitutionCode", device.getInstitutionCodes());
        writer.writeNotEmpty("dicomInstitutionAddress", device.getInstitutionAddresses());
        writer.writeNotEmpty("dicomInstitutionDepartmentName", device.getInstitutionalDepartmentNames());
        writer.writeNotEmpty("dicomPrimaryDeviceType", device.getPrimaryDeviceTypes());
        writer.writeNotEmpty("dicomRelatedDeviceReference", device.getRelatedDeviceRefs());
        writer.writeNotEmpty("dicomAuthorizedNodeCertificateReference", device.getAuthorizedNodeCertificateRefs());
        writer.writeNotEmpty("dicomThisNodeCertificateReference", device.getThisNodeCertificateRefs());
        writer.write("dicomVendorData", device.getVendorData().length > 0);
        writer.write("dicomInstalled", device.isInstalled());
        writeConnectionsTo(device, writer, extended);
        writeApplicationAEsTo(device, writer, extended);

        if (extended) {
            gen.writeStartObject("dcmDevice");
            writer.writeNotDef("dcmRoleSelectionNegotiationLenient", device.isRoleSelectionNegotiationLenient(), false);
            writer.writeNotDef("dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
            writer.writeNotEmpty("dcmLimitAssociationsInitiatedBy", device.getLimitAssociationsInitiatedBy());
            writer.writeNotNullOrDef("dcmTrustStoreURL", device.getTrustStoreURL(), null);
            writer.writeNotNullOrDef("dcmTrustStoreType", device.getTrustStoreType(), null);
            writer.writeNotNullOrDef("dcmTrustStorePin", device.getTrustStorePin(), null);
            writer.writeNotNullOrDef("dcmTrustStorePinProperty", device.getTrustStorePinProperty(), null);
            writer.writeNotNullOrDef("dcmKeyStoreURL", device.getKeyStoreURL(), null);
            writer.writeNotNullOrDef("dcmKeyStoreType", device.getKeyStoreType(), null);
            writer.writeNotNullOrDef("dcmKeyStorePin", device.getKeyStorePin(), null);
            writer.writeNotNullOrDef("dcmKeyStorePinProperty", device.getKeyStorePinProperty(), null);
            writer.writeNotNullOrDef("dcmKeyStoreKeyPin", device.getKeyStoreKeyPin(), null);
            writer.writeNotNullOrDef("dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty(), null);
            writer.writeNotNullOrDef("dcmTimeZoneOfDevice", device.getTimeZoneOfDevice(), null);
            writeWebApplicationsTo(device, writer);
            writeKeycloackClientsTo(device, writer);
            for (JsonConfigurationExtension ext : extensions)
                ext.storeTo(device, writer);
            gen.writeEnd();
        }
        gen.writeEnd();
    }

    public Device loadDeviceFrom(JsonParser parser, ConfigurationDelegate config)
            throws InternalException {
        Device device = new Device();
        JSONReader reader = new JSONReader(parser);
        reader.next();
        reader.expect(JsonParser.Event.START_OBJECT);
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dicomDeviceName":
                    device.setDeviceName(reader.stringValue());
                    break;
                case "dicomDescription":
                    device.setDescription(reader.stringValue());
                    break;
                case "dicomDeviceUID":
                    device.setDeviceUID(reader.stringValue());
                    break;
                case "dicomManufacturer":
                    device.setManufacturer(reader.stringValue());
                    break;
                case "dicomManufacturerModelName":
                    device.setManufacturerModelName(reader.stringValue());
                    break;
                case "dicomSoftwareVersion":
                    device.setSoftwareVersions(reader.stringArray());
                    break;
                case "dicomStationName":
                    device.setStationName(reader.stringValue());
                    break;
                case "dicomDeviceSerialNumber":
                    device.setDeviceSerialNumber(reader.stringValue());
                    break;
                case "dicomIssuerOfPatientID":
                    device.setIssuerOfPatientID(reader.issuerValue());
                    break;
                case "dicomIssuerOfAccessionNumber":
                    device.setIssuerOfAccessionNumber(reader.issuerValue());
                    break;
                case "dicomOrderPlacerIdentifier":
                    device.setOrderPlacerIdentifier(reader.issuerValue());
                    break;
                case "dicomOrderFillerIdentifier":
                    device.setOrderFillerIdentifier(reader.issuerValue());
                    break;
                case "dicomIssuerOfAdmissionID":
                    device.setIssuerOfAdmissionID(reader.issuerValue());
                    break;
                case "dicomIssuerOfServiceEpisodeID":
                    device.setIssuerOfServiceEpisodeID(reader.issuerValue());
                    break;
                case "dicomIssuerOfContainerIdentifier":
                    device.setIssuerOfContainerIdentifier(reader.issuerValue());
                    break;
                case "dicomIssuerOfSpecimenIdentifier":
                    device.setIssuerOfSpecimenIdentifier(reader.issuerValue());
                    break;
                case "dicomInstitutionName":
                    device.setInstitutionNames(reader.stringArray());
                    break;
                case "dicomInstitutionCode":
                    device.setInstitutionCodes(reader.codeArray());
                    break;
                case "dicomInstitutionAddress":
                    device.setInstitutionAddresses(reader.stringArray());
                    break;
                case "dicomInstitutionDepartmentName":
                    device.setInstitutionalDepartmentNames(reader.stringArray());
                    break;
                case "dicomPrimaryDeviceType":
                    device.setPrimaryDeviceTypes(reader.stringArray());
                    break;
                case "dicomRelatedDeviceReference":
                    device.setRelatedDeviceRefs(reader.stringArray());
                    break;
                case "dicomAuthorizedNodeCertificateReference":
                    for (String ref : reader.stringArray())
                        device.setAuthorizedNodeCertificates(ref);
                    break;
                case "dicomThisNodeCertificateReference":
                    for (String ref : reader.stringArray())
                        device.setThisNodeCertificates(ref);
                    break;
                case "dicomVendorData":
                    reader.booleanValue();
                    break;
                case "dicomInstalled":
                    device.setInstalled(reader.booleanValue());
                    break;
                case "dcmDevice":
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmRoleSelectionNegotiationLenient":
                                device.setRoleSelectionNegotiationLenient(reader.booleanValue());
                                break;
                            case "dcmLimitOpenAssociations":
                                device.setLimitOpenAssociations(reader.intValue());
                                break;
                            case "dcmLimitAssociationsInitiatedBy":
                                device.setLimitAssociationsInitiatedBy(reader.stringArray());
                                break;
                            case "dcmTrustStoreURL":
                                device.setTrustStoreURL(reader.stringValue());
                                break;
                            case "dcmTrustStoreType":
                                device.setTrustStoreType(reader.stringValue());
                                break;
                            case "dcmTrustStorePin":
                                device.setTrustStorePin(reader.stringValue());
                                break;
                            case "dcmTrustStorePinProperty":
                                device.setTrustStorePinProperty(reader.stringValue());
                                break;
                            case "dcmKeyStoreURL":
                                device.setKeyStoreURL(reader.stringValue());
                                break;
                            case "dcmKeyStoreType":
                                device.setKeyStoreType(reader.stringValue());
                                break;
                            case "dcmKeyStorePin":
                                device.setKeyStorePin(reader.stringValue());
                                break;
                            case "dcmKeyStorePinProperty":
                                device.setKeyStorePinProperty(reader.stringValue());
                                break;
                            case "dcmKeyStoreKeyPin":
                                device.setKeyStoreKeyPin(reader.stringValue());
                                break;
                            case "dcmKeyStoreKeyPinProperty":
                                device.setKeyStoreKeyPinProperty(reader.stringValue());
                                break;
                            case "dcmTimeZoneOfDevice":
                                device.setTimeZoneOfDevice(reader.timeZoneValue());
                                break;
                            case "dcmWebApp":
                                loadWebApplications(device, reader);
                                break;
                            case "dcmKeycloakClient":
                                loadKeycloakClients(device, reader);
                                break;
                            default:
                                if (!loadDeviceExtension(device, reader, config))
                                    reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    break;
                case "dicomNetworkConnection":
                    loadConnections(device, reader);
                    break;
                case "dicomNetworkAE":
                    loadApplicationEntities(device, reader, config);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (device.getDeviceName() == null)
            throw new JsonParsingException("Missing property: dicomDeviceName", reader.getLocation());
        return device;
    }


    private boolean loadDeviceExtension(Device device, JSONReader reader, ConfigurationDelegate config)
            throws InternalException {
        for (JsonConfigurationExtension ext : extensions)
            if (ext.loadDeviceExtension(device, reader, config))
                return true;
        return false;
    }

    private void writeConnectionsTo(Device device, JSONWriter writer, boolean extended) {
        List<Connection> conns = device.listConnections();
        if (conns.isEmpty())
            return;

        writer.writeStartArray("dicomNetworkConnection");
        for (Connection conn : conns)
            writeTo(conn, writer, extended);
        writer.writeEnd();
    }

    private void writeTo(Connection conn, JSONWriter writer, boolean extended) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("cn", conn.getCommonName(), null);
        writer.writeNotNullOrDef("dicomHostname", conn.getHostname(), null);
        writer.writeNotDef("dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        writer.writeNotEmpty("dicomTLSCipherSuite", conn.getTlsCipherSuites());
        writer.writeNotNull("dicomInstalled", conn.getInstalled());
        if (extended) {
            writer.writeStartObject("dcmNetworkConnection");
            writer.writeNotNullOrDef("dcmProtocol", conn.getProtocol(), Connection.Protocol.DICOM);
            writer.writeNotNullOrDef("dcmHTTPProxy", conn.getHttpProxy(), null);
            writer.writeNotEmpty("dcmBlacklistedHostname", conn.getBlacklist());
            writer.writeNotDef("dcmTCPBacklog", conn.getBacklog(), Connection.DEF_BACKLOG);
            writer.writeNotDef("dcmTCPConnectTimeout",
                    conn.getConnectTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmAARQTimeout",
                    conn.getRequestTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmAAACTimeout",
                    conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmARRPTimeout",
                    conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmSendTimeout",
                    conn.getSendTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmStoreTimeout",
                    conn.getStoreTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmResponseTimeout",
                    conn.getResponseTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmRetrieveTimeout",
                    conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmRetrieveTimeoutTotal",
                    conn.isRetrieveTimeoutTotal(), false);
            writer.writeNotDef("dcmIdleTimeout", conn.getIdleTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmAATimeout",
                    conn.getAbortTimeout(), Connection.DEF_ABORT_TIMEOUT);
            writer.writeNotDef("dcmTCPCloseDelay",
                    conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
            writer.writeNotDef("dcmTCPSendBufferSize",
                    conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
            writer.writeNotDef("dcmTCPReceiveBufferSize",
                    conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
            writer.writeNotDef("dcmTCPNoDelay", conn.isTcpNoDelay(), true);
            writer.writeNotNullOrDef("dcmBindAddress", conn.getBindAddress(), null);
            writer.writeNotNullOrDef("dcmClientBindAddress", conn.getClientBindAddress(), null);
            writer.writeNotDef("dcmSendPDULength",
                    conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
            writer.writeNotDef("dcmReceivePDULength",
                    conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
            writer.writeNotDef("dcmMaxOpsPerformed",
                    conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
            writer.writeNotDef("dcmMaxOpsInvoked",
                    conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
            writer.writeNotDef("dcmPackPDV", conn.isPackPDV(), true);
            writer.writeNotEmpty("dcmTLSProtocol", conn.getTlsProtocols(), Connection.DEFAULT_TLS_PROTOCOLS);
            writer.writeNotDef("dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
            writer.writeNotNullOrDef("dcmTLSEndpointIdentificationAlgorithm", conn.getTlsEndpointIdentificationAlgorithm(), null);
            writer.writeEnd();
        }
        writer.writeEnd();
    }

    private void loadConnections(Device device, JSONReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            Connection conn = new Connection();
            loadFrom(conn, reader);
            device.addConnection(conn);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(Connection conn, JSONReader reader) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "cn":
                    conn.setCommonName(reader.stringValue());
                    break;
                case "dicomHostname":
                    conn.setHostname(reader.stringValue());
                    break;
                case "dicomPort":
                    conn.setPort(reader.intValue());
                    break;
                case "dicomTLSCipherSuite":
                    conn.setTlsCipherSuites(reader.stringArray());
                    break;
                case "dicomInstalled":
                    conn.setInstalled(reader.booleanValue());
                    break;
                case "dcmNetworkConnection":
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmProtocol":
                                conn.setProtocol(Connection.Protocol.valueOf(reader.stringValue()));
                                break;
                            case "dcmHTTPProxy":
                                conn.setHttpProxy(reader.stringValue());
                                break;
                            case "dcmBlacklistedHostname":
                                conn.setBlacklist(reader.stringArray());
                                break;
                            case "dcmTCPBacklog":
                                conn.setBacklog(reader.intValue());
                                break;
                            case "dcmTCPConnectTimeout":
                                conn.setConnectTimeout(reader.intValue());
                                break;
                            case "dcmAARQTimeout":
                                conn.setRequestTimeout(reader.intValue());
                                break;
                            case "dcmAAACTimeout":
                                conn.setAcceptTimeout(reader.intValue());
                                break;
                            case "dcmARRPTimeout":
                                conn.setReleaseTimeout(reader.intValue());
                                break;
                            case "dcmSendTimeout":
                                conn.setSendTimeout(reader.intValue());
                                break;
                            case "dcmStoreTimeout":
                                conn.setStoreTimeout(reader.intValue());
                                break;
                            case "dcmResponseTimeout":
                                conn.setResponseTimeout(reader.intValue());
                                break;
                            case "dcmRetrieveTimeout":
                                conn.setRetrieveTimeout(reader.intValue());
                                break;
                            case "dcmRetrieveTimeoutTotal":
                                conn.setRetrieveTimeoutTotal(reader.booleanValue());
                                break;
                            case "dcmIdleTimeout":
                                conn.setIdleTimeout(reader.intValue());
                                break;
                            case "dcmAATimeout":
                                conn.setAbortTimeout(reader.intValue());
                                break;
                            case "dcmTCPCloseDelay":
                                conn.setSocketCloseDelay(reader.intValue());
                                break;
                            case "dcmTCPSendBufferSize":
                                conn.setSendBufferSize(reader.intValue());
                                break;
                            case "dcmTCPReceiveBufferSize":
                                conn.setReceiveBufferSize(reader.intValue());
                                break;
                            case "dcmTCPNoDelay":
                                conn.setTcpNoDelay(reader.booleanValue());
                                break;
                            case "dcmBindAddress":
                                conn.setBindAddress(reader.stringValue());
                                break;
                            case "dcmClientBindAddress":
                                conn.setClientBindAddress(reader.stringValue());
                                break;
                            case "dcmTLSNeedClientAuth":
                                conn.setTlsNeedClientAuth(reader.booleanValue());
                                break;
                            case "dcmTLSProtocol":
                                conn.setTlsProtocols(reader.stringArray());
                                break;
                            case "dcmTLSEndpointIdentificationAlgorithm":
                                conn.setTlsEndpointIdentificationAlgorithm(
                                        Connection.EndpointIdentificationAlgorithm.valueOf(reader.stringValue()));
                                break;
                            case "dcmSendPDULength":
                                conn.setSendPDULength(reader.intValue());
                                break;
                            case "dcmReceivePDULength":
                                conn.setReceivePDULength(reader.intValue());
                                break;
                            case "dcmMaxOpsPerformed":
                                conn.setMaxOpsPerformed(reader.intValue());
                                break;
                            case "dcmMaxOpsInvoked":
                                conn.setMaxOpsInvoked(reader.intValue());
                                break;
                            case "dcmPackPDV":
                                conn.setPackPDV(reader.booleanValue());
                                break;
                            default:
                                reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
    }

    private void writeApplicationAEsTo(Device device, JSONWriter writer, boolean extended) {
        Collection<ApplicationEntity> aes = device.getApplicationEntities();
        if (aes.isEmpty())
            return;

        List<Connection> conns = device.listConnections();
        writer.writeStartArray("dicomNetworkAE");
        for (ApplicationEntity ae : aes)
            writeTo(ae, conns, writer, extended);
        writer.writeEnd();
    }

    private void writeTo(ApplicationEntity ae, List<Connection> conns, JSONWriter writer, boolean extended) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("dicomAETitle", ae.getAETitle(), null);
        writer.writeNotNullOrDef("dicomDescription", ae.getDescription(), null);
        writer.writeNotEmpty("dicomApplicationCluster", ae.getApplicationClusters());
        writer.writeNotEmpty("dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        writer.writeNotEmpty("dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        writer.write("dicomAssociationInitiator", ae.isAssociationInitiator());
        writer.write("dicomAssociationAcceptor", ae.isAssociationAcceptor());
        writer.writeConnRefs(conns, ae.getConnections());
        writer.writeNotEmpty("dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        writer.writeNotNull("dicomInstalled", ae.getInstalled());
        writeTransferCapabilitiesTo(ae, writer, extended);
        if (extended) {
            writer.writeStartObject("dcmNetworkAE");
            writer.writeNotNull("dcmRoleSelectionNegotiationLenient", ae.getRoleSelectionNegotiationLenient());
            writer.writeNotEmpty("dcmPreferredTransferSyntax", ae.getPreferredTransferSyntaxes());
            writer.writeNotEmpty("dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
            writer.writeNotEmpty("dcmOtherAETitle", ae.getOtherAETitles());
            writer.writeNotEmpty("dcmNoAsyncModeCalledAETitle", ae.getNoAsyncModeCalledAETitles());
            writer.writeNotEmpty("dcmMasqueradeCallingAETitle", ae.getMasqueradeCallingAETitles());
            writer.writeNotNullOrDef("dcmShareTransferCapabilitiesFromAETitle",
                    ae.getShareTransferCapabilitiesFromAETitle(), null);
            writer.writeNotNullOrDef("hl7ApplicationName", ae.getHl7ApplicationName(), null);
            for (JsonConfigurationExtension ext : extensions)
                ext.storeTo(ae, writer);
            writer.writeEnd();
        }
        writer.writeEnd();
    }

    private void loadApplicationEntities(Device device, JSONReader reader, ConfigurationDelegate config)
            throws InternalException {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            ApplicationEntity ae = new ApplicationEntity();
            loadFrom(ae, reader, device, config);
            device.addApplicationEntity(ae);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(ApplicationEntity ae, JSONReader reader, Device device, ConfigurationDelegate config)
            throws InternalException {
        List<Connection> conns = device.listConnections();
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dicomAETitle":
                    ae.setAETitle(reader.stringValue());
                    break;
                case "dicomDescription":
                    ae.setDescription(reader.stringValue());
                    break;
                case "dicomApplicationCluster":
                    ae.setApplicationClusters(reader.stringArray());
                    break;
                case "dicomPreferredCallingAETitle":
                    ae.setPreferredCallingAETitles(reader.stringArray());
                    break;
                case "dicomPreferredCalledAETitle":
                    ae.setPreferredCalledAETitles(reader.stringArray());
                    break;
                case "dicomAssociationInitiator":
                    ae.setAssociationInitiator(reader.booleanValue());
                    break;
                case "dicomAssociationAcceptor":
                    ae.setAssociationAcceptor(reader.booleanValue());
                    break;
                case "dicomSupportedCharacterSet":
                    ae.setSupportedCharacterSets(reader.stringArray());
                    break;
                case "dicomInstalled":
                    ae.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        ae.addConnection(conns.get(JSONReader.toConnectionIndex(connRef)));
                    break;
                case "dcmNetworkAE":
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmRoleSelectionNegotiationLenient":
                                ae.setRoleSelectionNegotiationLenient(reader.booleanValue());
                                break;
                            case "dcmPreferredTransferSyntax":
                                ae.setPreferredTransferSyntaxes(reader.stringArray());
                                break;
                            case "dcmAcceptedCallingAETitle":
                                ae.setAcceptedCallingAETitles(reader.stringArray());
                                break;
                            case "dcmOtherAETitle":
                                ae.setOtherAETitles(reader.stringArray());
                                break;
                            case "dcmNoAsyncModeCalledAETitle":
                                ae.setNoAsyncModeCalledAETitles(reader.stringArray());
                                break;
                            case "dcmMasqueradeCallingAETitle":
                                ae.setMasqueradeCallingAETitles(reader.stringArray());
                                break;
                            case "dcmShareTransferCapabilitiesFromAETitle":
                                ae.setShareTransferCapabilitiesFromAETitle(reader.stringValue());
                                break;
                            case "hl7ApplicationName":
                                ae.setHl7ApplicationName(reader.stringValue());
                                break;
                            default:
                                if (!loadApplicationEntityExtension(device, ae, reader, config))
                                    reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    break;
                case "dicomTransferCapability":
                    loadTransferCapabilities(ae, reader);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (ae.getAETitle() == null)
            throw new JsonParsingException("Missing property: dicomAETitle", reader.getLocation());
    }

    private boolean loadApplicationEntityExtension(Device device, ApplicationEntity ae, JSONReader reader,
                                                   ConfigurationDelegate config) throws InternalException {
        for (JsonConfigurationExtension ext : extensions)
            if (ext.loadApplicationEntityExtension(device, ae, reader, config))
                return true;
        return false;
    }

    private void writeTransferCapabilitiesTo(ApplicationEntity ae, JSONWriter writer, boolean extended) {
        writer.writeStartArray("dicomTransferCapability");
        for (TransferCapability tc : ae.getTransferCapabilities())
            writeTo(tc, writer, extended);
        writer.writeEnd();

    }

    private void writeTo(TransferCapability tc, JSONWriter writer, boolean extended) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("cn", tc.getCommonName(), null);
        writer.writeNotNullOrDef("dicomSOPClass", tc.getSopClass(), null);
        writer.writeNotNullOrDef("dicomTransferRole", tc.getRole().toString(), null);
        writer.writeNotEmpty("dicomTransferSyntax", tc.getTransferSyntaxes());
        if (extended) {
            EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
            StorageOptions storageOpts = tc.getStorageOptions();
            String[] preferredTransferSyntaxes = tc.getPreferredTransferSyntaxes();
            if (queryOpts != null || storageOpts != null || preferredTransferSyntaxes.length > 0) {
                writer.writeStartObject("dcmTransferCapability");
                writer.writeNotEmpty("dcmPreferredTransferSyntax", preferredTransferSyntaxes);
                if (queryOpts != null) {
                    writer.writeNotDef("dcmRelationalQueries",
                            queryOpts.contains(QueryOption.RELATIONAL), false);
                    writer.writeNotDef("dcmCombinedDateTimeMatching",
                            queryOpts.contains(QueryOption.DATETIME), false);
                    writer.writeNotDef("dcmFuzzySemanticMatching",
                            queryOpts.contains(QueryOption.FUZZY), false);
                    writer.writeNotDef("dcmTimezoneQueryAdjustment",
                            queryOpts.contains(QueryOption.TIMEZONE), false);
                }
                if (storageOpts != null) {
                    writer.write("dcmStorageConformance", storageOpts.getLevelOfSupport().ordinal());
                    writer.write("dcmDigitalSignatureSupport", storageOpts.getDigitalSignatureSupport().ordinal());
                    writer.write("dcmDataElementCoercion", storageOpts.getElementCoercion().ordinal());
                }
                writer.writeEnd();
            }
        }
        writer.writeEnd();
    }

    private void loadTransferCapabilities(ApplicationEntity ae, JSONReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            TransferCapability tc = new TransferCapability();
            loadFrom(tc, reader);
            ae.addTransferCapability(tc);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(TransferCapability tc, JSONReader reader) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "cn":
                    tc.setCommonName(reader.stringValue());
                    break;
                case "dicomSOPClass":
                    tc.setSopClass(reader.stringValue());
                    break;
                case "dicomTransferRole":
                    tc.setRole(TransferCapability.Role.valueOf(reader.stringValue()));
                    break;
                case "dicomTransferSyntax":
                    tc.setTransferSyntaxes(reader.stringArray());
                    break;
                case "dcmTransferCapability":
                    EnumSet<QueryOption> queryOpts = null;
                    StorageOptions storageOpts = null;
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmPreferredTransferSyntax":
                                tc.setPreferredTransferSyntaxes(reader.stringArray());
                                break;
                            case "dcmRelationalQueries":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.RELATIONAL);
                                }
                                break;
                            case "dcmCombinedDateTimeMatching":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.DATETIME);
                                }
                                break;
                            case "dcmFuzzySemanticMatching":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.FUZZY);
                                }
                                break;
                            case "dcmTimezoneQueryAdjustment":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.TIMEZONE);
                                }
                                break;
                            case "dcmStorageConformance":
                                if (storageOpts == null)
                                    storageOpts = new StorageOptions();
                                storageOpts.setLevelOfSupport(
                                        StorageOptions.LevelOfSupport.valueOf(reader.intValue()));
                                break;
                            case "dcmDigitalSignatureSupport":
                                if (storageOpts == null)
                                    storageOpts = new StorageOptions();
                                storageOpts.setDigitalSignatureSupport(
                                        StorageOptions.DigitalSignatureSupport.valueOf(reader.intValue()));
                                break;
                            case "dcmDataElementCoercion":
                                if (storageOpts == null)
                                    storageOpts = new StorageOptions();
                                storageOpts.setElementCoercion(
                                        StorageOptions.ElementCoercion.valueOf(reader.intValue()));
                                break;
                            default:
                                reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    tc.setQueryOptions(queryOpts);
                    tc.setStorageOptions(storageOpts);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
    }

    private void writeWebApplicationsTo(Device device, JSONWriter writer) {
        Collection<WebApplication> webapps = device.getWebApplications();
        if (webapps.isEmpty())
            return;

        List<Connection> conns = device.listConnections();
        writer.writeStartArray("dcmWebApp");
        for (WebApplication webapp : webapps)
            writeTo(webapp, conns, writer);
        writer.writeEnd();
    }

    private void writeTo(WebApplication webapp, List<Connection> conns, JSONWriter writer) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("dcmWebAppName", webapp.getApplicationName(), null);
        writer.writeNotNullOrDef("dicomDescription", webapp.getDescription(), null);
        writer.writeNotNullOrDef("dcmWebServicePath", webapp.getServicePath(), null);
        writer.writeNotNullOrDef("dcmKeycloakClientID", webapp.getKeycloakClientID(), null);
        writer.writeNotEmpty("dcmWebServiceClass", webapp.getServiceClasses());
        writer.writeNotNullOrDef("dicomAETitle", webapp.getAETitle(), null);
        writer.writeNotEmpty("dicomApplicationCluster", webapp.getApplicationClusters());
        writer.writeNotEmpty("dcmProperty", webapp.getProperties());
        writer.writeConnRefs(conns, webapp.getConnections());
        writer.writeNotNull("dicomInstalled", webapp.getInstalled());
        writer.writeEnd();
    }

    private void loadWebApplications(Device device, JSONReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            WebApplication webapp = new WebApplication();
            loadFrom(webapp, reader, device);
            device.addWebApplication(webapp);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(WebApplication webapp, JSONReader reader, Device device) {
        List<Connection> conns = device.listConnections();
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dcmWebAppName":
                    webapp.setApplicationName(reader.stringValue());
                    break;
                case "dicomDescription":
                    webapp.setDescription(reader.stringValue());
                    break;
                case "dcmWebServicePath":
                    webapp.setServicePath(reader.stringValue());
                    break;
                case "dcmKeycloakClientID":
                    webapp.setKeycloakClientID(reader.stringValue());
                    break;
                case "dcmWebServiceClass":
                    webapp.setServiceClasses(reader.enumArray(WebApplication.ServiceClass.class));
                    break;
                case "dicomAETitle":
                    webapp.setAETitle(reader.stringValue());
                    break;
                case "dicomApplicationCluster":
                    webapp.setApplicationClusters(reader.stringArray());
                    break;
                case "dcmProperty":
                    webapp.setProperties(reader.stringArray());
                    break;
                case "dicomInstalled":
                    webapp.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        webapp.addConnection(conns.get(JSONReader.toConnectionIndex(connRef)));
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (webapp.getApplicationName() == null)
            throw new JsonParsingException("Missing property: dcmWebAppName", reader.getLocation());
    }

    private void writeKeycloackClientsTo(Device device, JSONWriter writer) {
        Collection<KeycloakClient> clients = device.getKeycloakClients();
        if (clients.isEmpty())
            return;

        writer.writeStartArray("dcmKeycloakClient");
        for (KeycloakClient client : clients) {
            writer.writeStartObject();
            writer.writeNotNullOrDef("dcmKeycloakClientID", client.getKeycloakClientID(), null);
            writer.writeNotNullOrDef("dcmURI", client.getKeycloakServerURL(), null);
            writer.writeNotNullOrDef("dcmKeycloakRealm", client.getKeycloakRealm(), null);
            writer.writeNotNullOrDef("dcmKeycloakGrantType", client.getKeycloakGrantType(), null);
            writer.writeNotNullOrDef("dcmKeycloakClientSecret", client.getKeycloakClientSecret(), null);
            writer.writeNotDef("dcmTLSAllowAnyHostname", client.isTLSAllowAnyHostname(), false);
            writer.writeNotDef("dcmTLSDisableTrustManager", client.isTLSDisableTrustManager(), false);
            writer.writeNotNullOrDef("uid", client.getUserID(), null);
            writer.writeNotNullOrDef("userPassword", client.getPassword(), null);
            writer.writeEnd();
        }
        writer.writeEnd();
    }

    private void loadKeycloakClients(Device device, JSONReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            KeycloakClient client = new KeycloakClient();
            loadFrom(client, reader);
            device.addKeycloakClient(client);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(KeycloakClient client, JSONReader reader) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dcmKeycloakClientID":
                    client.setKeycloakClientID(reader.stringValue());
                    break;
                case "dcmURI":
                    client.setKeycloakServerURL(reader.stringValue());
                    break;
                case "dcmKeycloakRealm":
                    client.setKeycloakRealm(reader.stringValue());
                    break;
                case "dcmKeycloakGrantType":
                    client.setKeycloakGrantType(KeycloakClient.GrantType.valueOf(reader.stringValue()));
                    break;
                case "dcmKeycloakClientSecret":
                    client.setKeycloakClientSecret(reader.stringValue());
                    break;
                case "dcmTLSAllowAnyHostname":
                    client.setTLSAllowAnyHostname(reader.booleanValue());
                    break;
                case "dcmTLSDisableTrustManager":
                    client.setTLSDisableTrustManager(reader.booleanValue());
                    break;
                case "uid":
                    client.setUserID(reader.stringValue());
                    break;
                case "userPassword":
                    client.setPassword(reader.stringValue());
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (client.getKeycloakClientID() == null)
            throw new JsonParsingException("Missing property: dcmKeycloakClientID", reader.getLocation());
    }

    public void writeBulkdataDescriptors(Map<String, BasicBulkDataDescriptor> descriptors, JSONWriter writer) {
        if (descriptors.isEmpty())
            return;

        writer.writeStartArray("dcmBulkDataDescriptor");
        for (BasicBulkDataDescriptor descriptor : descriptors.values())
            writeTo(descriptor, writer);
        writer.writeEnd();
    }

    private void writeTo(BasicBulkDataDescriptor descriptor, JSONWriter writer) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("dcmBulkDataDescriptorID", descriptor.getBulkDataDescriptorID(), null);
        writer.writeNotDef("dcmBulkDataExcludeDefaults", descriptor.isExcludeDefaults(), false);
        writer.writeNotEmpty("dcmAttributeSelector", descriptor.getAttributeSelectors());
        writer.writeNotEmpty("dcmBulkDataVRLengthThreshold", descriptor.getLengthsThresholdsAsStrings());
        writer.writeEnd();
    }

    public void loadBulkdataDescriptors(Map<String, BasicBulkDataDescriptor> descriptors, JSONReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            BasicBulkDataDescriptor descriptor = new BasicBulkDataDescriptor();
            loadFrom(descriptor, reader);
            descriptors.put(descriptor.getBulkDataDescriptorID(), descriptor);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(BasicBulkDataDescriptor descriptor, JSONReader reader) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dcmBulkDataDescriptorID":
                    descriptor.setBulkDataDescriptorID(reader.stringValue());
                    break;
                case "dcmBulkDataExcludeDefaults":
                    descriptor.excludeDefaults(reader.booleanValue());
                    break;
                case "dcmAttributeSelector":
                    descriptor.setAttributeSelectorsFromStrings(reader.stringArray());
                    break;
                case "dcmBulkDataVRLengthThreshold":
                    descriptor.setLengthsThresholdsFromStrings(reader.stringArray());
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (descriptor.getBulkDataDescriptorID() == null)
            throw new JsonParsingException("Missing property: dcmBulkDataDescriptorID", reader.getLocation());
    }

}
