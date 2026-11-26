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

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder;
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.Session;
import org.miaixz.bus.extra.ssh.SshjKit;

/**
 * SSHJ-based Session implementation. This class provides a wrapper around the SSHJ library's session handling, offering
 * functionalities for command execution, port forwarding, and SFTP integration. Project homepage:
 * <a href="https://github.com/hierynomus/sshj">https://github.com/hierynomus/sshj</a>
 *
 * @author Kimi Liu
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
     * Retrieves the raw, unwrapped object.
     *
     * @return The original object.
     */
    @Override
    public net.schmizz.sshj.connection.channel.direct.Session getRaw() {
        return raw;
    }

    /**
     * Checks if the SSH session is currently connected and active.
     *
     * @return {@code true} if the session is connected, {@code false} otherwise.
     */
    @Override
    public boolean isConnected() {
        return null != this.raw && (null == this.ssh || this.ssh.isConnected());
    }

    /**
     * Closes the SSH command session and client while suppressing close failures.
     *
     * @throws IOException not thrown by this implementation because close failures are suppressed
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
     * Binds a local address (host and port) to a remote address, creating a local port forwarding tunnel. This is an
     * extended version of {@link #bindLocalPort(int, InetSocketAddress)} that allows specifying the local bind address.
     *
     * @param localAddress  The local {@link InetSocketAddress} on the client machine to bind to.
     * @param remoteAddress The remote {@link InetSocketAddress} to forward traffic to.
     * @throws InternalException if the SSH port-forwarding operation fails
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
     * Removes a local port forwarding binding for a specific local address.
     *
     * @param localAddress The local {@link InetSocketAddress} that was previously bound.
     * @throws InternalException if the SSH port-forwarding operation fails
     */
    @Override
    public void unBindLocalPort(final InetSocketAddress localAddress) throws InternalException {
        if (MapKit.isEmpty(this.localPortForwarderMap)) {
            return;
        }

        IoKit.closeQuietly(this.localPortForwarderMap.remove(localAddress.toString()));
    }

    /**
     * Binds a port on the remote SSH server to a local address, creating a remote port forwarding tunnel. This is
     * useful for allowing the remote server to access a service running on the local client machine. For example, a
     * connection to {@code remoteAddress} on the SSH server will be forwarded through the tunnel to
     * {@code localAddress} on the client machine.
     *
     * @param remoteAddress The {@link InetSocketAddress} on the SSH server to bind.
     * @param localAddress  The local {@link InetSocketAddress} to forward traffic to.
     * @throws InternalException if an error occurs while binding the remote port.
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
     * Removes a remote port forwarding binding.
     *
     * @param remoteAddress The remote {@link InetSocketAddress} that was previously bound.
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
