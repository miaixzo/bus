/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.hl7;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MLLPOutputStream extends FilterOutputStream {

    /**
     * 消息开始
     */
    private static final int SOM = 0x0b;
    /**
     * 消息结束
     */
    private static final byte[] EOM = { 0x1c, 0x0d };

    private boolean somWritten;

    public MLLPOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public synchronized void write(int b) throws IOException {
        writeStartBlock();
        out.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        writeStartBlock();
        out.write(b, off, len);
    }

    public void writeMessage(byte[] b) throws IOException {
        writeMessage(b, 0, b.length);
    }

    public synchronized void writeMessage(byte[] b, int off, int len) throws IOException {
        if (somWritten)
            throw new IllegalStateException();

        byte[] msg = new byte[len + 3];
        msg[0] = SOM;
        System.arraycopy(b, off, msg, 1, len);
        System.arraycopy(EOM, 0, msg, len + 1, 2);
        out.write(msg);
        out.flush();
    }

    private void writeStartBlock() throws IOException {
        if (!somWritten) {
            out.write(SOM);
            somWritten = true;
        }
    }

    public synchronized void finish() throws IOException {
        if (!somWritten)
            throw new IllegalStateException();
        out.write(EOM);
        out.flush();
        somWritten = false;
    }

}
