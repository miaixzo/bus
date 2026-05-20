/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import lombok.*;
import lombok.Builder;

import org.miaixz.bus.core.net.tls.AnyTrustManager;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.Priority;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.IdentityAC;
import org.miaixz.bus.image.metric.pdu.IdentityRQ;

/**
 * Represents request argument information for DICOM operations. This class encapsulates various configuration
 * parameters for establishing and managing DICOM associations, including security, network settings, and data handling
 * options.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Args {

    /**
     * Transfer Syntax order: Implicit VR Little Endian first.
     */
    public static final String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian.uid, UID.ExplicitVRLittleEndian.uid,
            UID.ExplicitVRBigEndian.uid };

    /**
     * Transfer Syntax order: Explicit VR Little Endian first.
     */
    public static final String[] EVR_LE_FIRST = { UID.ExplicitVRLittleEndian.uid, UID.ExplicitVRBigEndian.uid,
            UID.ImplicitVRLittleEndian.uid };

    /**
     * Transfer Syntax order: Explicit VR Big Endian first.
     */
    public static final String[] EVR_BE_FIRST = { UID.ExplicitVRBigEndian.uid, UID.ExplicitVRLittleEndian.uid,
            UID.ImplicitVRLittleEndian.uid };

    /**
     * Transfer Syntax order: Implicit VR Little Endian only.
     */
    public static final String[] IVR_LE_ONLY = { UID.ImplicitVRLittleEndian.uid };

    /**
     * If {@code true}, binds the listener to the specified calling AET. Only requests with a matching called AET will
     * be accepted. If {@code false}, all called AETs will be accepted.
     */
    private boolean bindCallingAet;

    /**
     * A list of accepted calling Application Entity Titles (AETs). If empty, all calling AETs are accepted.
     */
    private String[] acceptedCallingAETitles;

    /**
     * The information model for the operation (e.g., Study Root, Patient Root).
     */
    private Object informationModel;

    /**
     * A set of {@link QueryOption}s to configure query behavior.
     */
    @Builder.Default
    private EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);

    /**
     * The preferred order of Transfer Syntax UIDs for negotiation.
     */
    @Builder.Default
    private String[] tsuidOrder = Arrays.copyOf(IVR_LE_FIRST, IVR_LE_FIRST.length);

    /**
     * Specifies an HTTP proxy for tunneling the DICOM connection. The format is {@code <[user:password@]host:port>}.
     */
    private String proxy;

    /**
     * The user identity request for authentication.
     */
    private IdentityRQ identityRQ;

    /**
     * The user identity acceptance response.
     */
    private IdentityAC identityAC;

    /**
     * The priority of the DICOM operation.
     *
     * @see Priority
     */
    @Builder.Default
    private int priority = Priority.NORMAL;

    /**
     * Advanced options for proxy, authentication, connection, and TLS settings.
     */
    private Option option;

    /**
     * A list of {@link Editors} to modify DICOM attributes during processing.
     */
    private List<Editors> editors;

    /**
     * If {@code true}, enables extended negotiation of SOP Classes.
     */
    private boolean negociation;

    /**
     * URL to a properties file defining SOP Classes and their UIDs (e.g., {@code sop-classes.properties}).
     */
    private URL sopClasses;

    /**
     * URL to a properties file defining related general-purpose SOP classes as per DICOM Part 4, B.3.1.4 (e.g.,
     * {@code sop-classes-uid.properties}).
     */
    private URL sopClassesUID;

    /**
     * URL to a properties file defining extended storage transfer capabilities (e.g.,
     * {@code sop-classes-tcs.properties}).
     */
    private URL sopClassesTCS;

    /**
     * Constructs an {@code Args} object with a specific setting for binding the calling AET.
     *
     * @param bindCallingAet If {@code true}, binds the listener to the specified calling AET.
     */
    public Args(boolean bindCallingAet) {
        this(null, bindCallingAet, null, (String[]) null);
    }

    /**
     * Constructs an {@code Args} object with editors and extended negotiation settings.
     *
     * @param editors     A list of editors to modify DICOM attributes.
     * @param negociation If {@code true}, enables extended negotiation of SOP Classes.
     * @param sopClasses  URL to a properties file for SOP Class extension.
     */
    public Args(List<Editors> editors, boolean negociation, URL sopClasses) {
        this.editors = editors;
        this.negociation = negociation;
        this.sopClasses = sopClasses;
    }

    /**
     * Constructs an {@code Args} object with advanced options and transfer capabilities.
     *
     * @param option                  Advanced options (proxy, authentication, connection, TLS).
     * @param bindCallingAet          If {@code true}, binds the listener to the specified calling AET.
     * @param sopClassesTCS           URL to a file containing transfer capabilities (SOP class, role, transfer
     *                                syntaxes).
     * @param acceptedCallingAETitles A list of acceptable calling AETs. An empty list accepts all.
     */
    public Args(Option option, boolean bindCallingAet, URL sopClassesTCS, String... acceptedCallingAETitles) {
        this.option = option;
        this.bindCallingAet = bindCallingAet;
        this.sopClassesTCS = sopClassesTCS;
        this.acceptedCallingAETitles = null == acceptedCallingAETitles ? new String[0]
                : Arrays.copyOf(acceptedCallingAETitles, acceptedCallingAETitles.length);
        if (null != this.option) {
            this.option.setMaxOpsInvoked(15);
            this.option.setMaxOpsPerformed(15);
        }
    }

    /**
     * Creates argument parameters bound to an existing option carrier.
     *
     * @param option Advanced connection/TLS options.
     * @return A new argument instance.
     */
    public static Args of(Option option) {
        return new Args().withOption(option);
    }

    /**
     * Creates a copy configured with the option.
     *
     * @param option the option.
     * @return the operation result.
     */
    public Args withOption(Option option) {
        this.option = option;
        return this;
    }

    /**
     * Creates a copy configured with the information model.
     *
     * @param informationModel the information model.
     * @return the operation result.
     */
    public Args withInformationModel(Object informationModel) {
        this.informationModel = informationModel;
        return this;
    }

    /**
     * Creates a copy configured with the query options.
     *
     * @param queryOptions the query options.
     * @return the operation result.
     */
    public Args withQueryOptions(EnumSet<QueryOption> queryOptions) {
        setQueryOptions(queryOptions);
        return this;
    }

    /**
     * Creates a copy configured with the transfer syntax order.
     *
     * @param tsuidOrder the tsuid order.
     * @return the operation result.
     */
    public Args withTransferSyntaxOrder(String... tsuidOrder) {
        setTsuidOrder(tsuidOrder);
        return this;
    }

    /**
     * Creates a copy configured with the proxy.
     *
     * @param proxy the proxy.
     * @return the operation result.
     */
    public Args withProxy(String proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Creates a copy configured with the priority.
     *
     * @param priority the priority.
     * @return the operation result.
     */
    public Args withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Creates a copy configured with the editors.
     *
     * @param editors the editors.
     * @return the operation result.
     */
    public Args withEditors(List<Editors> editors) {
        this.editors = editors;
        return this;
    }

    /**
     * Creates a copy configured with the default editor.
     *
     * @param tagToOverride the tag to override.
     * @return the operation result.
     */
    public Args withDefaultEditor(Attributes tagToOverride) {
        return withEditors(List.of(Editors.defaults(tagToOverride)));
    }

    /**
     * Creates a copy configured with the default editor.
     *
     * @param generateUIDs  the generate ui ds.
     * @param tagToOverride the tag to override.
     * @return the operation result.
     */
    public Args withDefaultEditor(boolean generateUIDs, Attributes tagToOverride) {
        return withEditors(List.of(Editors.defaults(generateUIDs, tagToOverride)));
    }

    /**
     * Creates a copy configured with the default editor.
     *
     * @param generateUIDs  the generate ui ds.
     * @param globalKey     the global key.
     * @param tagToOverride the tag to override.
     * @return the operation result.
     */
    public Args withDefaultEditor(boolean generateUIDs, String globalKey, Attributes tagToOverride) {
        return withEditors(List.of(Editors.defaults(generateUIDs, globalKey, tagToOverride)));
    }

    /**
     * Creates a copy configured with the accepted calling ae titles.
     *
     * @param acceptedCallingAETitles the accepted calling ae titles.
     * @return the operation result.
     */
    public Args withAcceptedCallingAETitles(String... acceptedCallingAETitles) {
        setAcceptedCallingAETitles(acceptedCallingAETitles);
        return this;
    }

    /**
     * Gets the query options.
     *
     * @return the query options.
     */
    public EnumSet<QueryOption> getQueryOptions() {
        return queryOptions == null ? EnumSet.noneOf(QueryOption.class) : EnumSet.copyOf(queryOptions);
    }

    /**
     * Sets the query options.
     *
     * @param queryOptions the query options.
     */
    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions = queryOptions == null ? EnumSet.noneOf(QueryOption.class) : EnumSet.copyOf(queryOptions);
    }

    /**
     * Gets the tsuid order.
     *
     * @return the tsuid order.
     */
    public String[] getTsuidOrder() {
        return tsuidOrder == null ? new String[0] : Arrays.copyOf(tsuidOrder, tsuidOrder.length);
    }

    /**
     * Sets the tsuid order.
     *
     * @param tsuidOrder the tsuid order.
     */
    public void setTsuidOrder(String[] tsuidOrder) {
        this.tsuidOrder = Arrays
                .copyOf(Objects.requireNonNull(tsuidOrder, "tsuidOrder cannot be null"), tsuidOrder.length);
    }

    /**
     * Gets the accepted calling ae titles.
     *
     * @return the accepted calling ae titles.
     */
    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETitles == null ? new String[0]
                : Arrays.copyOf(acceptedCallingAETitles, acceptedCallingAETitles.length);
    }

    /**
     * Sets the accepted calling ae titles.
     *
     * @param acceptedCallingAETitles the accepted calling ae titles.
     */
    public void setAcceptedCallingAETitles(String[] acceptedCallingAETitles) {
        this.acceptedCallingAETitles = acceptedCallingAETitles == null ? new String[0]
                : Arrays.copyOf(acceptedCallingAETitles, acceptedCallingAETitles.length);
    }

    /**
     * Configures the connection parameters for an association request.
     *
     * @param aAssociateRQ The association request PDU to configure.
     * @param remote       The remote connection details.
     * @param calledNode   The node being called.
     */
    public void configureConnect(AAssociateRQ aAssociateRQ, Connection remote, Node calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        if (identityRQ != null) {
            aAssociateRQ.setIdentityRQ(identityRQ);
        }
        remote.setHostname(calledNode.getHostname());
        remote.setPort(calledNode.getPort());
    }

    /**
     * Binds the connection using the calling node's information.
     *
     * @param connection  The connection to configure.
     * @param callingNode The node initiating the connection.
     */
    public void configureBind(Connection connection, Node callingNode) {
        if (callingNode.getHostname() != null) {
            connection.setHostname(callingNode.getHostname());
        }
        if (callingNode.getPort() != null) {
            connection.setPort(callingNode.getPort());
        }
    }

    /**
     * Configures the binding for an association request, including identity negotiation.
     *
     * @param aAssociateRQ The association request PDU to configure.
     * @param remote       The remote connection details.
     * @param calledNode   The node being called.
     */
    public void configureBind(AAssociateRQ aAssociateRQ, Connection remote, Node calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        if (null != identityRQ) {
            aAssociateRQ.setIdentityRQ(identityRQ);
        }
        if (null != identityAC) {
            aAssociateRQ.setIdentityAC(identityAC);
        }
        remote.setHostname(calledNode.getHostname());
        remote.setPort(calledNode.getPort());
    }

    /**
     * Binds the connection and application entity with the calling node's information.
     *
     * @param applicationEntity The application entity to configure.
     * @param connection        The connection to configure.
     * @param callingNode       The node initiating the connection.
     */
    public void configureBind(ApplicationEntity applicationEntity, Connection connection, Node callingNode) {
        applicationEntity.setAETitle(callingNode.getAet());
        if (null != callingNode.getHostname()) {
            connection.setHostname(callingNode.getHostname());
        }
        if (null != callingNode.getPort()) {
            connection.setPort(callingNode.getPort());
        }
    }

    /**
     * Configures connection-related parameters from the advanced options.
     *
     * @param conn The connection to configure.
     */
    public void configure(Connection conn) {
        if (option != null) {
            configureBasicOptions(conn);
            configureBufferOptions(conn);
            configurePduOptions(conn);
            configureOperationOptions(conn);
        }
    }

    /**
     * Configures the basic options.
     *
     * @param conn the conn.
     */
    private void configureBasicOptions(Connection conn) {
        conn.setBacklog(option.getBacklog());
        conn.setConnectTimeout(option.getConnectTimeout());
        conn.setRequestTimeout(option.getRequestTimeout());
        conn.setAcceptTimeout(option.getAcceptTimeout());
        conn.setReleaseTimeout(option.getReleaseTimeout());
        conn.setResponseTimeout(option.getResponseTimeout());
        conn.setRetrieveTimeout(option.getRetrieveTimeout());
        conn.setIdleTimeout(option.getIdleTimeout());
        conn.setSocketCloseDelay(option.getSocloseDelay());
    }

    /**
     * Configures the buffer options.
     *
     * @param conn the conn.
     */
    private void configureBufferOptions(Connection conn) {
        conn.setReceiveBufferSize(option.getSorcvBuffer());
        conn.setSendBufferSize(option.getSosndBuffer());
        conn.setPackPDV(option.isPackPDV());
        conn.setTcpNoDelay(option.isTcpNoDelay());
    }

    /**
     * Configures the pdu options.
     *
     * @param conn the conn.
     */
    private void configurePduOptions(Connection conn) {
        conn.setReceivePDULength(option.getMaxPdulenRcv());
        conn.setSendPDULength(option.getMaxPdulenSnd());
    }

    /**
     * Configures the operation options.
     *
     * @param conn the conn.
     */
    private void configureOperationOptions(Connection conn) {
        conn.setMaxOpsInvoked(option.getMaxOpsInvoked());
        conn.setMaxOpsPerformed(option.getMaxOpsPerformed());
    }

    /**
     * Configures TLS-related parameters for the connection.
     *
     * @param conn   The connection to configure.
     * @param remote The remote connection details, can be {@code null}.
     * @throws IOException if a security error occurs while configuring TLS.
     */
    public void configureTLS(Connection conn, Connection remote) throws IOException {
        if (option != null) {
            if (option.getCipherSuites() != null) {
                conn.setTlsCipherSuites(option.getCipherSuites().toArray(new String[0]));
            }
            if (option.getTlsProtocols() != null) {
                conn.setTlsProtocols(option.getTlsProtocols().toArray(new String[0]));
            }
            conn.setTlsNeedClientAuth(option.isTlsNeedClientAuth());

            Device device = conn.getDevice();
            try {
                device.setKeyManager(
                        AnyTrustManager.createKeyManager(
                                option.getKeystoreType(),
                                option.getKeystoreURL(),
                                option.getKeystorePass(),
                                option.getKeyPass()));
                device.setTrustManager(
                        AnyTrustManager.createTrustManager(
                                option.getTruststoreType(),
                                option.getTruststoreURL(),
                                option.getTruststorePass()));
                if (remote != null) {
                    remote.setTlsProtocols(conn.getTlsProtocols());
                    remote.setTlsCipherSuites(conn.getTlsCipherSuites());
                }
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Checks if there are any DICOM attribute editors configured.
     *
     * @return {@code true} if editors are configured, {@code false} otherwise.
     */
    public boolean hasEditors() {
        return editors != null && !editors.isEmpty();
    }

}
