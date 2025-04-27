/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.stream;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageInputStreamAdapter extends InputStream {

    private final ImageInputStream iis;

    private long markedPos;

    private IOException markException;

    public ImageInputStreamAdapter(ImageInputStream iis) {
        this.iis = iis;
    }

    @Override
    public int read() throws IOException {
        return iis.read();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPos = iis.getStreamPosition();
            this.markException = null;
        } catch (IOException e) {
            this.markException = e;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return iis.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (markException != null)
            throw markException;
        iis.seek(markedPos);
    }

    @Override
    public long skip(long n) throws IOException {
        return iis.skipBytes((int) n);
    }

}
