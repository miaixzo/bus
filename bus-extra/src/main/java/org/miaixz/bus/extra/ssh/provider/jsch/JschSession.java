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
package org.miaixz.bus.extra.ssh.provider.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.JschKit;
import org.miaixz.bus.extra.ssh.Session;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

/**
 * JSch Session encapsulation. This class implements the {@link Session} interface and provides a wrapper around the
 * JSch {@link com.jcraft.jsch.Session}, offering functionalities for command execution, port forwarding, and channel
 * management.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JschSession implements Session {

    /**
     * The underlying raw JSch session object.
     */
    private final com.jcraft.jsch.Session raw;
    /**
     * The connection timeout duration in milliseconds.
     */
    private final long timeout;

    /**
     * Constructs a {@code JschSession} with the given {@link Connector}. This creates a new JSch session based on the
     * provided connection and authentication information.
     *
     * @param connector The {@link Connector} holding connection and authentication details.
     */
    public JschSession(final Connector connector) {
        this(JschKit.openSession(connector), connector.getTimeout());
    }

    /**
     * Constructs a {@code JschSession} by wrapping a raw JSch {@link com.jcraft.jsch.Session} and a timeout.
     *
     * @param raw     The raw JSch {@link com.jcraft.jsch.Session} to wrap.
     * @param timeout The connection timeout duration in milliseconds; 0 indicates no limit.
     */
    public JschSession(final com.jcraft.jsch.Session raw, final long timeout) {
        this.raw = raw;
        this.timeout = timeout;
    }

    /**
     * Gets the underlying raw JSch session. This method is designed to be overridden by subclasses for custom session
     * access.
     *
     * Subclasses may override to add validation or wrapping.
     *
     * @return The raw JSch {@link com.jcraft.jsch.Session}.
     */
    @Override
    public com.jcraft.jsch.Session getRaw() {
        return this.raw;
    }

    /**
     * Checks if the session is connected. This method is designed to be overridden by subclasses for custom connection
     * checks.
     *
     * Subclasses may override to add additional connection validation.
     *
     * @return {@code true} if the session is connected, {@code false} otherwise.
     */
    @Override
    public boolean isConnected() {
        return null != this.raw && this.raw.isConnected();
    }

    /**
     * Closes the session and releases all resources. This method is designed to be overridden by subclasses for custom
     * cleanup logic.
     *
     * Subclasses should call {@code super.close()} to ensure proper cleanup.
     *
     * @throws IOException if an error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        JschKit.close(this.raw);
    }

    /**
     * Binds a local port to a remote address (local port forwarding). This method is designed to be overridden by
     * subclasses for custom port forwarding logic.
     *
     * Subclasses may override to add logging or validation.
     *
     * @param localAddress  The local address to bind.
     * @param remoteAddress The remote address to forward to.
     * @throws InternalException if port forwarding setup fails.
     */
    @Override
    public void bindLocalPort(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress)
            throws InternalException {
        try {
            this.raw.setPortForwardingL(
                    localAddress.getHostName(),
                    localAddress.getPort(),
                    remoteAddress.getHostName(),
                    remoteAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e, "From [{}] mapping to [{}] error！", localAddress, remoteAddress);
        }
    }

    /**
     * Unbinds a local port forwarding. This method is designed to be overridden by subclasses for custom port unbinding
     * logic.
     *
     * Subclasses may override to add logging or validation.
     *
     * @param localAddress The local address to unbind.
     */
    @Override
    public void unBindLocalPort(final InetSocketAddress localAddress) {
        try {
            this.raw.delPortForwardingL(localAddress.getHostName(), localAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Binds a remote port to a local address (remote port forwarding). This method is designed to be overridden by
     * subclasses for custom port forwarding logic.
     *
     * Subclasses may override to add logging or validation.
     *
     * @param remoteAddress The remote address to bind.
     * @param localAddress  The local address to forward to.
     * @throws InternalException if port forwarding setup fails.
     */
    @Override
    public void bindRemotePort(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress)
            throws InternalException {
        try {
            this.raw.setPortForwardingR(
                    remoteAddress.getHostName(),
                    remoteAddress.getPort(),
                    localAddress.getHostName(),
                    localAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e, "From [{}] mapping to [{}] error！", remoteAddress, localAddress);
        }
    }

    /**
     * Unbinds a remote port forwarding. This method is designed to be overridden by subclasses for custom port
     * unbinding logic.
     *
     * Subclasses may override to add logging or validation.
     *
     * @param remoteAddress The remote address to unbind.
     */
    @Override
    public void unBindRemotePort(final InetSocketAddress remoteAddress) {
        try {
            this.raw.delPortForwardingR(remoteAddress.getHostName(), remoteAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates an SSH channel but does not connect it.
     *
     * @param channelType The type of channel to create (e.g., shell, sftp), see {@link ChannelType}.
     * @return An unconnected JSch {@link Channel} object.
     */
    public Channel createChannel(final ChannelType channelType) {
        return JschKit.createChannel(this.raw, channelType, this.timeout);
    }

    /**
     * Opens and connects an interactive Shell channel.
     *
     * @return A connected {@link ChannelShell} object.
     */
    public ChannelShell openShell() {
        return (ChannelShell) openChannel(ChannelType.SHELL);
    }

    /**
     * Opens and connects an SSH channel of a specified type.
     *
     * @param channelType The type of channel to open (e.g., shell, sftp), see {@link ChannelType}.
     * @return A connected JSch {@link Channel} object.
     */
    public Channel openChannel(final ChannelType channelType) {
        return JschKit.openChannel(this.raw, channelType, this.timeout);
    }

    /**
     * Opens an SFTP session, returning a wrapper for SFTP operations.
     *
     * @param charset The character set to use for file names.
     * @return A {@link JschSftp} instance for performing SFTP operations.
     */
    public JschSftp openSftp(final java.nio.charset.Charset charset) {
        return new JschSftp(this.raw, charset, this.timeout);
    }

    /**
     * Executes a command using the 'exec' channel and returns the output.
     *
     * @param cmd     The command to execute.
     * @param charset The character set for encoding the command and decoding the output.
     * @return The execution result as a string.
     */
    public String exec(final String cmd, final java.nio.charset.Charset charset) {
        return exec(cmd, charset, System.err);
    }

    /**
     * Executes a command using the 'exec' channel and returns the output. This method is non-interactive, sends a
     * single command, and does not load the user's shell profile. The channel is automatically closed after execution.
     *
     * @param cmd       The command to execute.
     * @param charset   The character set for encoding the command and decoding the output.
     * @param errStream The {@link OutputStream} to which error messages will be written.
     * @return The execution result as a string.
     * @throws InternalException if an I/O error or JSch exception occurs, or if the command returns a non-zero exit
     *                           status.
     */
    public String exec(final String cmd, java.nio.charset.Charset charset, final OutputStream errStream) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }
        final ChannelExec channel = (ChannelExec) createChannel(ChannelType.EXEC);
        channel.setCommand(ByteKit.toBytes(cmd, charset));
        channel.setInputStream(null);
        channel.setErrStream(errStream);

        String result;
        InputStream in = null;
        try {
            channel.connect();
            in = channel.getInputStream();
            result = IoKit.read(in, charset);

            if (channel.getExitStatus() != 0) {
                throw new InternalException("Execute command [{}] error, exit status is [{}]", cmd,
                        channel.getExitStatus());
            }
        } catch (final IOException | JSchException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }

        return result;
    }

    /**
     * Executes a command within an interactive 'shell' channel. This method simulates typing a command into a shell,
     * which means the user's profile and environment variables are loaded. The channel is automatically closed after
     * execution.
     *
     * @param cmd     The command to execute.
     * @param charset The character set for sending and reading content.
     * @return The execution result content.
     * @throws InternalException if an I/O error occurs.
     */
    public String execByShell(final String cmd, final java.nio.charset.Charset charset) {
        final ChannelShell shell = openShell();
        shell.setPty(true);
        OutputStream out = null;
        InputStream in = null;
        try {
            out = shell.getOutputStream();
            in = shell.getInputStream();

            out.write(ByteKit.toBytes(cmd, charset));
            out.flush();

            return IoKit.read(in, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(out);
            IoKit.closeQuietly(in);
            if (shell.isConnected()) {
                shell.disconnect();
            }
        }
    }

}
