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
package org.miaixz.bus.image.metric.hl7;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import org.miaixz.bus.logger.Logger;

/**
 * Represents the MLLPConnection type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MLLPConnection implements Closeable {

    /**
     * The ack value.
     */
    private static final byte ACK = 0x06;

    /**
     * The nak value.
     */
    private static final byte NAK = 0x15;

    /**
     * The sock value.
     */
    private final Socket sock;

    /**
     * The mllp in value.
     */
    private final MLLPInputStream mllpIn;

    /**
     * The mllp out value.
     */
    private final MLLPOutputStream mllpOut;

    /**
     * The mllp release value.
     */
    private final MLLPRelease mllpRelease;

    /**
     * Creates a new instance.
     *
     * @param sock the sock.
     * @throws IOException if the operation cannot be completed.
     */
    public MLLPConnection(Socket sock) throws IOException {
        this(sock, MLLPRelease.MLLP1);
    }

    /**
     * Creates a new instance.
     *
     * @param sock        the sock.
     * @param mllpRelease the mllp release.
     * @throws IOException if the operation cannot be completed.
     */
    public MLLPConnection(Socket sock, MLLPRelease mllpRelease) throws IOException {
        this.sock = sock;
        mllpIn = new MLLPInputStream(sock.getInputStream());
        mllpOut = new MLLPOutputStream(sock.getOutputStream());
        this.mllpRelease = mllpRelease;
    }

    /**
     * Creates a new instance.
     *
     * @param sock       the sock.
     * @param bufferSize the buffer size.
     * @throws IOException if the operation cannot be completed.
     */
    public MLLPConnection(Socket sock, int bufferSize) throws IOException {
        this(sock, MLLPRelease.MLLP1, bufferSize);
    }

    /**
     * Creates a new instance.
     *
     * @param sock        the sock.
     * @param mllpRelease the mllp release.
     * @param bufferSize  the buffer size.
     * @throws IOException if the operation cannot be completed.
     */
    public MLLPConnection(Socket sock, MLLPRelease mllpRelease, int bufferSize) throws IOException {
        this.sock = sock;
        mllpIn = new MLLPInputStream(sock.getInputStream());
        mllpOut = new MLLPOutputStream(new BufferedOutputStream(sock.getOutputStream(), bufferSize));
        this.mllpRelease = mllpRelease;
    }

    /**
     * Gets the socket.
     *
     * @return the socket.
     */
    public final Socket getSocket() {
        return sock;
    }

    /**
     * Writes the message.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeMessage(byte[] b) throws IOException {
        writeMessage(b, 0, b.length);
    }

    /**
     * Writes the message.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeMessage(byte[] b, int off, int len) throws IOException {
        log("{} << {}", b, off, len);
        mllpOut.writeMessage(b, off, len);
        if (mllpRelease == MLLPRelease.MLLP2)
            readACK();
    }

    /**
     * Reads the message.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public byte[] readMessage() throws IOException {
        byte[] b = mllpIn.readMessage();
        if (b != null) {
            log("{} >> {}", b, 0, b.length);
            if (mllpRelease == MLLPRelease.MLLP2)
                writeACK();
        }
        return b;
    }

    /**
     * Writes the ack.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void writeACK() throws IOException {
        Logger.debug(false, "Image", "HL7 ACK sent: protocol=hl7, socket={}", sock);
        mllpOut.write(ACK);
        mllpOut.finish();
    }

    /**
     * Reads the ack.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void readACK() throws IOException {
        byte[] b = mllpIn.readMessage();
        if (b == null)
            throw new IOException("Connection closed by receiver");
        if (b.length == 1) {
            switch (b[0]) {
                case ACK:
                    Logger.debug(false, "Image", "HL7 ACK received: protocol=hl7, socket={}", sock);
                    return;

                case NAK:
                    Logger.info(false, "Image", "HL7 NAK received: protocol=hl7, socket={}", sock);
                    throw new IOException("NAK received");
            }
        }
        Logger.info(false, "Image", "HL7 acknowledgment invalid: protocol=hl7, socket={}, bytes={}", sock, b.length);
        throw new IOException("<ACK> or <NAK> expected, but received " + b.length + " bytes");
    }

    /**
     * Executes the log operation.
     *
     * @param format the format.
     * @param b      the b.
     * @param off    the off.
     * @param len    the len.
     */
    private void log(String format, byte[] b, int off, int len) {
        if (!Logger.isInfoEnabled())
            return;
        int mshlen = 0;
        while (mshlen < len && b[off + mshlen] != '\r')
            mshlen++;
        Logger.info(
                false,
                "Image",
                "HL7 message observed: protocol=hl7, socket={}, direction={}, headerBytes={}, messageBytes={}",
                sock,
                format.contains("<<") ? "out" : "in",
                mshlen,
                len);
        if (Logger.isDebugEnabled())
            Logger.debug(
                    false,
                    "Image",
                    "HL7 message payload suppressed: protocol=hl7, socket={}, direction={}, messageBytes={}",
                    sock,
                    format.contains("<<") ? "out" : "in",
                    len);
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        sock.close();
    }

}
