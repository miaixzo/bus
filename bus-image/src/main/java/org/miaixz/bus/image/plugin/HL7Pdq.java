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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.HL7Message;
import org.miaixz.bus.image.metric.hl7.HL7Segment;
import org.miaixz.bus.image.metric.hl7.MLLPConnection;
import org.miaixz.bus.image.metric.hl7.MLLPRelease;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Iterator;

/**
 * The {@code HL7Pdq} class is a client for the IHE Patient Demographics Query (PDQ) profile. It sends an HL7 QBP^Q22
 * message over an MLLP connection and waits for a response.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Pdq extends Device {

    /**
     * The local network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The remote network connection configuration.
     */
    private final Connection remote = new Connection();
    /**
     * The MLLP release protocol version.
     */
    private MLLPRelease mllpRelease;
    /**
     * The sending application and facility, formatted as application^facility.
     */
    private String sendingApplication = "hl7pdq^miaixz";
    /**
     * The receiving application and facility, formatted as application^facility.
     */
    private String receivingApplication = "";
    /**
     * The character set to be specified in the MSH-18 field.
     */
    private String charset;

    /**
     * The underlying socket for the connection.
     */
    private Socket sock;
    /**
     * The MLLP connection handler.
     */
    private MLLPConnection mllp;

    /**
     * 
     * Constructs a new {@code HL7Pdq} device.
     *
     * @throws IOException if an I/O error occurs.
     */
    public HL7Pdq() throws IOException {
        super("hl7pdq");
        addConnection(conn);
    }

    /**
     * 
     * Counts the number of query parameters in an iterator. Query parameters are identified by a leading '@' character.
     *
     * @param args The iterator of string arguments.
     * @return The number of query parameters found.
     */
    private static int countQueryParams(Iterator<String> args) {
        int count = 0;
        while (args.hasNext() && args.next().charAt(0) == '@')
            count++;
        return count;
    }

    /**
     * 
     * Sets the MLLP release protocol to be used.
     *
     * @param mllpRelease The MLLP release version.
     */
    public void setMLLPRelease(MLLPRelease mllpRelease) {
        this.mllpRelease = mllpRelease;
    }

    /**
     * 
     * Gets the configured sending application and facility.
     *
     * @return The sending application string.
     */
    public String getSendingApplication() {
        return sendingApplication;
    }

    /**
     * 
     * Sets the sending application and facility.
     *
     * @param sendingApplication The sending application string (e.g., "APP^FACILITY").
     */
    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    /**
     * 
     * Gets the configured receiving application and facility.
     *
     * @return The receiving application string.
     */
    public String getReceivingApplication() {
        return receivingApplication;
    }

    /**
     * 
     * Sets the receiving application and facility.
     *
     * @param receivingApplication The receiving application string (e.g., "APP^FACILITY").
     */
    public void setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
    }

    /**
     * 
     * Sets the character set to be used for encoding the HL7 message. This value will be placed in the MSH-18 field.
     *
     * @param charset The name of the character set.
     */
    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    /**
     * 
     * Opens the MLLP connection to the remote peer.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void open() throws IOException, InternalException, GeneralSecurityException {
        sock = conn.connect(remote);
        sock.setSoTimeout(conn.getResponseTimeout());
        mllp = new MLLPConnection(sock, mllpRelease);
    }

    /**
     * 
     * Closes the MLLP connection.
     */
    public void close() {
        conn.close(sock);
    }

    /**
     * 
     * Sends a Patient Demographics Query (PDQ) message.
     *
     * @param queryParams The query parameters for the QPD segment.
     * @param domains     The patient identifier domains to query.
     * @throws IOException if an I/O error occurs during message transmission or receipt.
     */
    public void query(String[] queryParams, String[] domains) throws IOException {
        HL7Message qbp = HL7Message.makePdqQuery(queryParams, domains);
        HL7Segment msh = qbp.get(0);
        msh.setSendingApplicationWithFacility(sendingApplication);
        msh.setReceivingApplicationWithFacility(receivingApplication);
        msh.setField(17, charset);
        mllp.writeMessage(qbp.getBytes(charset));
        if (mllp.readMessage() == null)
            throw new IOException("Connection closed by receiver");
    }

}
