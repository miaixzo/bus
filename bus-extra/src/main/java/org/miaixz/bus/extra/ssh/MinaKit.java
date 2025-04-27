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
package org.miaixz.bus.extra.ssh;

import java.io.IOException;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Apache MINA SSHD（https://mina.apache.org/sshd-project/）相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MinaKit {

    /**
     * 打开一个客户端对象
     *
     * @return 客户端对象
     */
    public static SshClient openClient() {
        final SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();

        return sshClient;
    }

    /**
     * 打开一个新的Session
     *
     * @param sshClient 客户端
     * @param connector 连接信息
     * @return {@link ClientSession}
     */
    public static ClientSession openSession(final SshClient sshClient, final Connector connector) {
        final ClientSession session;
        final boolean success;
        try {
            session = sshClient.connect(connector.getUser(), connector.getHost(), connector.getPort()).verify()
                    .getSession();

            session.addPasswordIdentity(connector.getPassword());
            success = session.auth().verify().isSuccess();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (!success) {
            throw new InternalException("Authentication failed.");
        }

        return session;
    }
}
