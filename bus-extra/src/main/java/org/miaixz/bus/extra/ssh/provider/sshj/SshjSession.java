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
package org.miaixz.bus.extra.ssh.provider.sshj;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.Session;
import org.miaixz.bus.extra.ssh.SshjKit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder;
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener;

/**
 * SSHJ-based Session implementation. This class provides a wrapper around the SSHJ library's session handling, offering
 * functionalities for command execution, port forwarding, and SFTP integration. Project homepage:
 * <a href="https://github.com/hierynomus/sshj">https://github.com/hierynomus/sshj</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SshjSession implements Session {

    /**
     * The underlying SSHJ client.
     */
    private final SSHClient ssh;
    /**
     * The underlying raw SSHJ session.
     */
    private final net.schmizz.sshj.connection.channel.direct.Session raw;

    /**
     * A map to keep track of local port forwarding server sockets.
     */
    private Map<String, ServerSocket> localPortForwarderMap;

    /**
     * Constructs a {@code SshjSession} with the given {@link Connector}.
     *
     * @param connector The {@link Connector} holding connection and authentication information.
     */
    public SshjSession(final Connector connector) {
        this(SshjKit.openClient(connector));
    }

    /**
     * Constructs a {@code SshjSession} with a given {@link SSHClient}.
     *
     * @param ssh The {@link SSHClient} instance.
     */
    public SshjSession(final SSHClient ssh) {
        this.ssh = ssh;
        try {
            this.raw = ssh.startSession();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public net.schmizz.sshj.connection.channel.direct.Session getRaw() {
        return raw;
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public boolean isConnected() {
        return null != this.raw && (null == this.ssh || this.ssh.isConnected());
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.raw);
        IoKit.closeQuietly(this.ssh);
    }

    /**
     * Opens an SFTP session.
     *
     * @param charset The character set for file names.
     * @return A {@link SshjSftp} instance.
     */
    public SshjSftp openSftp(final java.nio.charset.Charset charset) {
        return new SshjSftp(this.ssh, charset);
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void bindLocalPort(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress)
            throws InternalException {
        final Parameters params = new Parameters(localAddress.getHostName(), localAddress.getPort(),
                remoteAddress.getHostName(), remoteAddress.getPort());
        final ServerSocket ss;
        try {
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(localAddress);
            ssh.newLocalPortForwarder(params, ss).listen();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == this.localPortForwarderMap) {
            this.localPortForwarderMap = new HashMap<>();
        }

        this.localPortForwarderMap.put(localAddress.toString(), ss);
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void unBindLocalPort(final InetSocketAddress localAddress) throws InternalException {
        if (MapKit.isEmpty(this.localPortForwarderMap)) {
            return;
        }

        IoKit.closeQuietly(this.localPortForwarderMap.remove(localAddress.toString()));
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void bindRemotePort(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress)
            throws InternalException {
        try {
            this.ssh.getRemotePortForwarder().bind(
                    new RemotePortForwarder.Forward(remoteAddress.getHostName(), remoteAddress.getPort()),
                    new SocketForwardingConnectListener(localAddress));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void unBindRemotePort(final InetSocketAddress remoteAddress) {
        final String hostName = remoteAddress.getHostName();
        final int port = remoteAddress.getPort();

        final RemotePortForwarder remotePortForwarder = this.ssh.getRemotePortForwarder();
        final Set<RemotePortForwarder.Forward> activeForwards = remotePortForwarder.getActiveForwards();
        for (final RemotePortForwarder.Forward activeForward : activeForwards) {
            if (port == activeForward.getPort()) {
                final String activeAddress = activeForward.getAddress();
                if (StringKit.isNotBlank(activeAddress) && !StringKit.equalsIgnoreCase(hostName, activeAddress)) {
                    continue;
                }

                try {
                    remotePortForwarder.cancel(activeForward);
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
                return;
            }
        }
    }

    /**
     * Executes a command using the 'exec' channel. This method sends a single command, does not read environment
     * variables, and is non-blocking.
     *
     * @param cmd       The command to execute.
     * @param charset   The character set for sending and reading content.
     * @param errStream The output stream for error messages.
     * @return The execution result.
     */
    public String exec(final String cmd, java.nio.charset.Charset charset, final OutputStream errStream) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }

        final net.schmizz.sshj.connection.channel.direct.Session.Command command;

        try {
            command = this.raw.exec(cmd);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null != errStream) {
            IoKit.copy(command.getErrorStream(), errStream);
        }

        return IoKit.read(command.getInputStream(), charset);
    }

    /**
     * Executes a command within an interactive 'shell' channel. This method loads environment variables and may be
     * blocking.
     *
     * @param cmd       The command to execute.
     * @param charset   The character set for sending and reading content.
     * @param errStream The output stream for error messages.
     * @return The execution result.
     */
    public String execByShell(final String cmd, java.nio.charset.Charset charset, final OutputStream errStream) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }

        final net.schmizz.sshj.connection.channel.direct.Session.Shell shell;
        try {
            shell = this.raw.startShell();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        IoKit.write(shell.getOutputStream(), charset, true, cmd);

        if (null != errStream) {
            IoKit.copy(shell.getErrorStream(), errStream);
        }

        return IoKit.read(shell.getInputStream(), charset);
    }

}
