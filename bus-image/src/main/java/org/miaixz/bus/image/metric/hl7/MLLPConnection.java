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
package org.miaixz.bus.image.metric.hl7;

import org.miaixz.bus.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Kimi Liu
 * @since Java 17+
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
