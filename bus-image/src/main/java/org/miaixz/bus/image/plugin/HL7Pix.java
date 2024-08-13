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
package org.miaixz.bus.image.plugin;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.HL7Message;
import org.miaixz.bus.image.metric.hl7.HL7Segment;
import org.miaixz.bus.image.metric.hl7.MLLPConnection;
import org.miaixz.bus.image.metric.hl7.MLLPRelease;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Pix extends Device {

    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private MLLPRelease mllpRelease;
    private String sendingApplication = "hl7pix^miaixz";
    private String receivingApplication = "";
    private String charset;

    private Socket sock;
    private MLLPConnection mllp;

    public HL7Pix() throws IOException {
        super("hl7pix");
        addConnection(conn);
    }

    public void setMLLPRelease(MLLPRelease mllpRelease) {
        this.mllpRelease = mllpRelease;
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    public String getReceivingApplication() {
        return receivingApplication;
    }

    public void setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
    }

    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    public void open() throws IOException, InternalException, GeneralSecurityException {
        sock = conn.connect(remote);
        sock.setSoTimeout(conn.getResponseTimeout());
        mllp = new MLLPConnection(sock, mllpRelease);
    }

    public void close() {
        conn.close(sock);
    }

    public void query(String pid, String[] domains) throws IOException {
        HL7Message qbp = HL7Message.makePixQuery(pid, domains);
        HL7Segment msh = qbp.get(0);
        msh.setSendingApplicationWithFacility(sendingApplication);
        msh.setReceivingApplicationWithFacility(receivingApplication);
        msh.setField(17, charset);
        mllp.writeMessage(qbp.getBytes(charset));
        if (mllp.readMessage() == null)
            throw new IOException("Connection closed by receiver");
    }

}
