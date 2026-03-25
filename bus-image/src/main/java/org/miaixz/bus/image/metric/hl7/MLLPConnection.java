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
 * @author Kimi Liu
 * @since Java 21+
 */
public class MLLPConnection implements Closeable {

    private static final byte ACK = 0x06;
    private static final byte NAK = 0x15;
    private final Socket sock;
    private final MLLPInputStream mllpIn;
    private final MLLPOutputStream mllpOut;
    private final MLLPRelease mllpRelease;

    public MLLPConnection(Socket sock) throws IOException {
        this(sock, MLLPRelease.MLLP1);
    }

    public MLLPConnection(Socket sock, MLLPRelease mllpRelease) throws IOException {
        this.sock = sock;
        mllpIn = new MLLPInputStream(sock.getInputStream());
        mllpOut = new MLLPOutputStream(sock.getOutputStream());
        this.mllpRelease = mllpRelease;
    }

    public MLLPConnection(Socket sock, int bufferSize) throws IOException {
        this(sock, MLLPRelease.MLLP1, bufferSize);
    }

    public MLLPConnection(Socket sock, MLLPRelease mllpRelease, int bufferSize) throws IOException {
        this.sock = sock;
        mllpIn = new MLLPInputStream(sock.getInputStream());
        mllpOut = new MLLPOutputStream(new BufferedOutputStream(sock.getOutputStream(), bufferSize));
        this.mllpRelease = mllpRelease;
    }

    public final Socket getSocket() {
        return sock;
    }

    public void writeMessage(byte[] b) throws IOException {
        writeMessage(b, 0, b.length);
    }

    public void writeMessage(byte[] b, int off, int len) throws IOException {
        log("{} << {}", b, off, len);
        mllpOut.writeMessage(b, off, len);
        if (mllpRelease == MLLPRelease.MLLP2)
            readACK();
    }

    public byte[] readMessage() throws IOException {
        byte[] b = mllpIn.readMessage();
        if (b != null) {
            log("{} >> {}", b, 0, b.length);
            if (mllpRelease == MLLPRelease.MLLP2)
                writeACK();
        }
        return b;
    }

    private void writeACK() throws IOException {
        Logger.debug("{} << <ACK>", sock);
        mllpOut.write(ACK);
        mllpOut.finish();
    }

    private void readACK() throws IOException {
        byte[] b = mllpIn.readMessage();
        if (b == null)
            throw new IOException("Connection closed by receiver");
        if (b.length == 1) {
            switch (b[0]) {
                case ACK:
                    Logger.debug("{} >> <ACK>", sock);
                    return;

                case NAK:
                    Logger.info("{} >> <NAK>", sock);
                    throw new IOException("NAK received");
            }
        }
        Logger.info("{}: <ACK> or <NAK> expected, but received {} bytes", sock, b.length);
        throw new IOException("<ACK> or <NAK> expected, but received " + b.length + " bytes");
    }

    private void log(String format, byte[] b, int off, int len) {
        if (!Logger.isInfoEnabled())
            return;
        int mshlen = 0;
        while (mshlen < len && b[off + mshlen] != '\r')
            mshlen++;
        Logger.info(format, sock, new String(b, off, mshlen));
        if (Logger.isDebugEnabled())
            Logger.debug(format, sock, new String(b, off, len).replace('\r', '\n'));
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }

}
