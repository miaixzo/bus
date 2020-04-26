/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.params;

import lombok.Data;
import org.aoju.bus.image.Device;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.metric.ApplicationEntity;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.QueryOption;
import org.aoju.bus.image.metric.SSLManagerFactory;
import org.aoju.bus.image.metric.internal.pdu.AAssociateRQ;
import org.aoju.bus.image.metric.internal.pdu.IdentityRQ;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@Data
public class AdvancedParams {

    public static String[] IVR_LE_FIRST =
            {UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired};
    public static String[] EVR_LE_FIRST =
            {UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired, UID.ImplicitVRLittleEndian};
    public static String[] EVR_BE_FIRST =
            {UID.ExplicitVRBigEndianRetired, UID.ExplicitVRLittleEndian, UID.ImplicitVRLittleEndian};
    public static String[] IVR_LE_ONLY = {UID.ImplicitVRLittleEndian};

    private Object informationModel;
    private EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
    private String[] tsuidOrder = IVR_LE_FIRST;

    private String proxy;

    private IdentityRQ identity;

    private int priority = 0;

    private ConnectOptions connectOptions;
    private TlsOptions tlsOptions;

    public void configureConnect(AAssociateRQ aAssociateRQ, Connection remote, DicomNode calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        if (identity != null) {
            aAssociateRQ.setIdentityRQ(identity);
        }
        remote.setHostname(calledNode.getHostname());
        remote.setPort(calledNode.getPort());
    }

    /**
     * Bind the connection with the callingNode
     *
     * @param connection  Connection
     * @param callingNode DicomNode
     */
    public void configureBind(Connection connection, DicomNode callingNode) {
        if (callingNode.getHostname() != null) {
            connection.setHostname(callingNode.getHostname());
        }
        if (callingNode.getPort() != null) {
            connection.setPort(callingNode.getPort());
        }
    }

    /**
     * Bind the connection and applicationEntity with the callingNode
     *
     * @param applicationEntity ApplicationEntity
     * @param connection        Connection
     * @param callingNode       DicomNode
     */
    public void configureBind(ApplicationEntity applicationEntity, Connection connection, DicomNode callingNode) {
        applicationEntity.setAETitle(callingNode.getAet());
        if (callingNode.getHostname() != null) {
            connection.setHostname(callingNode.getHostname());
        }
        if (callingNode.getPort() != null) {
            connection.setPort(callingNode.getPort());
        }
    }

    public void configure(Connection conn) {
        if (connectOptions != null) {
            conn.setBacklog(connectOptions.getBacklog());
            conn.setConnectTimeout(connectOptions.getConnectTimeout());
            conn.setRequestTimeout(connectOptions.getRequestTimeout());
            conn.setAcceptTimeout(connectOptions.getAcceptTimeout());
            conn.setReleaseTimeout(connectOptions.getReleaseTimeout());
            conn.setResponseTimeout(connectOptions.getResponseTimeout());
            conn.setRetrieveTimeout(connectOptions.getRetrieveTimeout());
            conn.setIdleTimeout(connectOptions.getIdleTimeout());
            conn.setSocketCloseDelay(connectOptions.getSocloseDelay());
            conn.setReceiveBufferSize(connectOptions.getSorcvBuffer());
            conn.setSendBufferSize(connectOptions.getSosndBuffer());
            conn.setReceivePDULength(connectOptions.getMaxPdulenRcv());
            conn.setSendPDULength(connectOptions.getMaxPdulenSnd());
            conn.setMaxOpsInvoked(connectOptions.getMaxOpsInvoked());
            conn.setMaxOpsPerformed(connectOptions.getMaxOpsPerformed());
            conn.setPackPDV(connectOptions.isPackPDV());
            conn.setTcpNoDelay(connectOptions.isTcpNoDelay());
        }
    }

    public void configureTLS(Connection conn, Connection remote) throws IOException {
        if (tlsOptions != null) {
            conn.setTlsCipherSuites(tlsOptions.getCipherSuites());
            conn.setTlsProtocols(tlsOptions.getTlsProtocols());
            conn.setTlsNeedClientAuth(tlsOptions.isTlsNeedClientAuth());

            Device device = conn.getDevice();
            try {
                device.setKeyManager(SSLManagerFactory.createKeyManager(tlsOptions.getKeystoreType(),
                        tlsOptions.getKeystoreURL(), tlsOptions.getKeystorePass(), tlsOptions.getKeyPass()));
                device.setTrustManager(SSLManagerFactory.createTrustManager(tlsOptions.getTruststoreType(),
                        tlsOptions.getTruststoreURL(), tlsOptions.getTruststorePass()));
                if (remote != null) {
                    remote.setTlsProtocols(conn.getTlsProtocols());
                    remote.setTlsCipherSuites(conn.getTlsCipherSuites());
                }
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    }

}
