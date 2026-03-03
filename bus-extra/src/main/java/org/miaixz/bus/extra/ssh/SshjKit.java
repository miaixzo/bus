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
