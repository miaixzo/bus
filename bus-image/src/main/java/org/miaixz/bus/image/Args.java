/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.miaixz.bus.core.net.tls.AnyTrustManager;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.Priority;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.IdentityAC;
import org.miaixz.bus.image.metric.pdu.IdentityRQ;

import lombok.*;
import lombok.Builder;

/**
 * Represents request argument information for DICOM operations. This class encapsulates various configuration
 * parameters for establishing and managing DICOM associations, including security, network settings, and data handling
 * options.
 *
 * @author Kimi Liu
 * @since Java 17+
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
        this(null, bindCallingAet, null, null);
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
        this.acceptedCallingAETitles = null == acceptedCallingAETitles ? new String[0] : acceptedCallingAETitles;
        if (null == option && null != this.option) {
            this.option.setMaxOpsInvoked(15);
            this.option.setMaxOpsPerformed(15);
        }
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
            conn.setBacklog(option.getBacklog());
            conn.setConnectTimeout(option.getConnectTimeout());
            conn.setRequestTimeout(option.getRequestTimeout());
            conn.setAcceptTimeout(option.getAcceptTimeout());
            conn.setReleaseTimeout(option.getReleaseTimeout());
            conn.setResponseTimeout(option.getResponseTimeout());
            conn.setRetrieveTimeout(option.getRetrieveTimeout());
            conn.setIdleTimeout(option.getIdleTimeout());
            conn.setSocketCloseDelay(option.getSocloseDelay());
            conn.setReceiveBufferSize(option.getSorcvBuffer());
            conn.setSendBufferSize(option.getSosndBuffer());
            conn.setReceivePDULength(option.getMaxPdulenRcv());
            conn.setSendPDULength(option.getMaxPdulenSnd());
            conn.setMaxOpsInvoked(option.getMaxOpsInvoked());
            conn.setMaxOpsPerformed(option.getMaxOpsPerformed());
            conn.setPackPDV(option.isPackPDV());
            conn.setTcpNoDelay(option.isTcpNoDelay());
        }
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
