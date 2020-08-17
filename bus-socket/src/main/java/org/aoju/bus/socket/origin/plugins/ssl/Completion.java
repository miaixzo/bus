/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.socket.origin.plugins.ssl;


import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
class Completion implements CompletionHandler<Integer, Handshake> {

    private SSLService sslService;

    public Completion(SSLService sslService) {
        this.sslService = sslService;
    }

    @Override
    public void completed(Integer result, Handshake attachment) {
        if (result == -1) {
            attachment.setEof(true);
        }
        synchronized (attachment) {
            sslService.doHandshake(attachment);
        }
    }

    @Override
    public void failed(Throwable exc, Handshake attachment) {
        try {
            attachment.getSocketChannel().close();
            attachment.getSslEngine().closeOutbound();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.warn("handshake exception", exc);
    }
}
