/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.Closeable;
import java.net.InetSocketAddress;

import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Abstract interface for an SSH (Secure Shell) session. This interface defines common operations for managing an SSH
 * connection, including checking the connection status and handling port forwarding (tunnelling). It extends
 * {@link Wrapper} to provide access to the underlying session object and {@link Closeable} for resource management.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Session extends Wrapper<Object>, Closeable {

    /**
     * Checks if the SSH session is currently connected and active.
     *
     * @return {@code true} if the session is connected, {@code false} otherwise.
     */
    boolean isConnected();

    /**
     * Binds a local port to a remote address, creating a local port forwarding tunnel. This is useful for accessing a
     * service on a remote network that is only accessible from the SSH server. For example, a request to
     * {@code localhost:localPort} on the client machine will be forwarded through the SSH tunnel to
     * {@code remoteHost:remotePort} on the server's network.
     *
     * @param localPort     The local port on the client machine to listen on.
     * @param remoteAddress The {@link InetSocketAddress} of the remote host and port to forward traffic to.
     */
    default void bindLocalPort(final int localPort, final InetSocketAddress remoteAddress) {
        bindLocalPort(new InetSocketAddress(localPort), remoteAddress);
    }

    /**
     * Binds a local address (host and port) to a remote address, creating a local port forwarding tunnel. This is an
     * extended version of {@link #bindLocalPort(int, InetSocketAddress)} that allows specifying the local bind address.
     *
     * @param localAddress  The local {@link InetSocketAddress} on the client machine to bind to.
     * @param remoteAddress The remote {@link InetSocketAddress} to forward traffic to.
     */
    void bindLocalPort(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress);

    /**
     * Removes a local port forwarding binding.
     *
     * @param localPort The local port that was previously bound.
     * @throws InternalException if an error occurs while unbinding the port.
     */
    default void unBindLocalPort(final int localPort) {
        unBindLocalPort(new InetSocketAddress(localPort));
    }

    /**
     * Removes a local port forwarding binding for a specific local address.
     *
     * @param localAddress The local {@link InetSocketAddress} that was previously bound.
     */
    void unBindLocalPort(final InetSocketAddress localAddress);

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
    void bindRemotePort(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress);

    /**
     * Removes a remote port forwarding binding.
     *
     * @param remoteAddress The remote {@link InetSocketAddress} that was previously bound.
     */
    void unBindRemotePort(final InetSocketAddress remoteAddress);

}
