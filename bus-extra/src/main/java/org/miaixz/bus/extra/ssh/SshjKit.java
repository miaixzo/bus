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

import org.miaixz.bus.core.lang.exception.InternalException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * Utility class for SSHJ, a Java library for SSH. This class provides a simplified way to create and configure an
 * {@link SSHClient}. Project homepage:
 * <a href="https://github.com/hierynomus/sshj">https://github.com/hierynomus/sshj</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SshjKit {

    /**
     * Opens and authenticates an SSH client connection using the provided connection details. This method configures
     * the client with a promiscuous host key verifier, sets timeouts, connects to the server, and authenticates using a
     * password.
     *
     * @param connector The {@link Connector} object containing connection information (host, port, user, password,
     *                  timeout).
     * @return An initialized and authenticated {@link SSHClient} instance.
     * @throws InternalException if an {@link IOException} occurs during connection or authentication.
     */
    public static SSHClient openClient(final Connector connector) {
        final SSHClient ssh = new SSHClient();
        // Use a promiscuous verifier to accept all host keys, simplifying connection setup.
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.setConnectTimeout((int) connector.getTimeout());
        ssh.setTimeout((int) connector.getTimeout());

        try {
            ssh.connect(connector.getHost(), connector.getPort());
            ssh.authPassword(connector.getUser(), connector.getPassword());
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return ssh;
    }

}
