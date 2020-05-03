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
package org.aoju.bus.image;

import lombok.Data;
import org.aoju.bus.image.centre.Device;
import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.metric.ApplicationEntity;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.SSLManagerFactory;
import org.aoju.bus.image.metric.internal.pdu.AAssociateRQ;
import org.aoju.bus.image.metric.internal.pdu.IdentityRQ;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@Data
public class Args {

    public static String[] IVR_LE_FIRST = {
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndianRetired
    };
    public static String[] EVR_LE_FIRST = {
            UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndianRetired,
            UID.ImplicitVRLittleEndian
    };
    public static String[] EVR_BE_FIRST = {
            UID.ExplicitVRBigEndianRetired,
            UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian
    };
    public static String[] IVR_LE_ONLY = {
            UID.ImplicitVRLittleEndian
    };
    private boolean bindCallingAet;
    private URL transferCapabilityFile;
    private String[] acceptedCallingAETitles;
    private Object informationModel;
    private EnumSet<Option.Type> types = EnumSet.noneOf(Option.Type.class);
    private String[] tsuidOrder = IVR_LE_FIRST;
    private String proxy;
    private IdentityRQ identity;
    private int priority = 0;
    private Option option;

    private boolean extendNegociation;
    private URL extendSopClassesURL;

    private int tag;
    private String[] values;
    private int[] parentSeqTags;
    private String storagePattern;

    private Editors editors;

    public Args() {

    }

    public Args(boolean bindCallingAet) {
        this(null, bindCallingAet, null, null);
    }

    public Args(int tag, String... values) {
        this(null, tag, values);
    }

    /**
     * @param editors             a editor to modify DICOM attributes
     * @param extendNegociation   extends SOP classes negotiation
     * @param extendSopClassesURL configuration file of the SOP classes negotiation extension
     */
    public Args(Editors editors,
                boolean extendNegociation,
                URL extendSopClassesURL) {
        this.editors = editors;
        this.extendNegociation = extendNegociation;
        this.extendSopClassesURL = extendSopClassesURL;
    }

    public Args(int[] parentSeqTags, int tag, String... values) {
        this.tag = tag;
        this.values = values;
        this.parentSeqTags = parentSeqTags;
    }

    /**
     * @param option                  optional advanced parameters (proxy, authentication, connection and TLS)
     * @param bindCallingAet          when true it will set the AET of the listener DICOM node. Only requests with matching called AETitle
     *                                will be accepted. If false all the called AETs will be accepted.
     * @param storagePattern          the storage pattern
     * @param transferCapabilityFile  an URL for getting a file containing the transfer capabilities (sopClasses, roles, transferSyntaxes)
     * @param acceptedCallingAETitles the list of the accepted calling AETitles. Null will accepted all the AETitles.
     */
    public Args(Option option,
                boolean bindCallingAet,
                String storagePattern,
                URL transferCapabilityFile,
                String... acceptedCallingAETitles) {
        this.bindCallingAet = bindCallingAet;
        this.storagePattern = storagePattern;
        this.transferCapabilityFile = transferCapabilityFile;
        this.acceptedCallingAETitles = acceptedCallingAETitles == null ? new String[0] : acceptedCallingAETitles;
        if (option == null && this.option != null) {
            this.option.setMaxOpsInvoked(15);
            this.option.setMaxOpsPerformed(15);
        }
    }

    public void configureConnect(AAssociateRQ aAssociateRQ, Connection remote, Node calledNode) {
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
     * @param callingNode Node
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
     * Bind the connection and applicationEntity with the callingNode
     *
     * @param applicationEntity ApplicationEntity
     * @param connection        Connection
     * @param callingNode       the Node
     */
    public void configureBind(ApplicationEntity applicationEntity, Connection connection, Node callingNode) {
        applicationEntity.setAETitle(callingNode.getAet());
        if (callingNode.getHostname() != null) {
            connection.setHostname(callingNode.getHostname());
        }
        if (callingNode.getPort() != null) {
            connection.setPort(callingNode.getPort());
        }
    }

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

    public void configureTLS(Connection conn, Connection remote) throws IOException {
        if (option != null) {
            conn.setTlsCipherSuites(option.getCipherSuites());
            conn.setTlsProtocols(option.getTlsProtocols());
            conn.setTlsNeedClientAuth(option.isTlsNeedClientAuth());

            Device device = conn.getDevice();
            try {
                device.setKeyManager(SSLManagerFactory.createKeyManager(option.getKeystoreType(),
                        option.getKeystoreURL(), option.getKeystorePass(), option.getKeyPass()));
                device.setTrustManager(SSLManagerFactory.createTrustManager(option.getTruststoreType(),
                        option.getTruststoreURL(), option.getTruststorePass()));
                if (remote != null) {
                    remote.setTlsProtocols(conn.getTlsProtocols());
                    remote.setTlsCipherSuites(conn.getTlsCipherSuites());
                }
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    }

    public String getTagName() {
        return ElementDictionary.keywordOf(tag, null);
    }

}
