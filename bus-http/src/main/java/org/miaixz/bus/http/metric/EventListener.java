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
package org.miaixz.bus.http.metric;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.Connection;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.socket.Handshake;

/**
 * Listener for metrics events. Extend this class to monitor the quantity, size, and duration of your application's HTTP
 * calls.
 *
 * <p>
 * All event methods must execute quickly, without external locking, without throwing exceptions, without attempting to
 * mutate the event parameters, and without reentrant calls into the client. Any IO writing should be done
 * asynchronously.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class EventListener {

    public static final EventListener NONE = new EventListener() {

    };

    /**
     * Creates a factory that returns the same listener instance for all calls.
     *
     * @param listener The listener to be returned by the factory.
     * @return A factory that returns the given listener.
     */
    public static EventListener.Factory factory(EventListener listener) {
        return call -> listener;
    }

    /**
     * Invoked immediately when a call is enqueued or executed by the client. In case of thread or stream limits, this
     * call may be executed before processing of the request starts.
     *
     * <p>
     * This is invoked only once for a single {@link NewCall}. Retries of different routes or redirects will be handled
     * within the boundaries of a single callStart and {@link #callEnd}/{@link #callFailed} pair.
     *
     * @param call The call information.
     */
    public void callStart(NewCall call) {

    }

    /**
     * Invoked just prior to a DNS lookup. See {@link DnsX#lookup(String)}.
     *
     * @param call       The call information.
     * @param domainName The hostname.
     */
    public void dnsStart(NewCall call, String domainName) {

    }

    /**
     * Invoked immediately after a DNS lookup. This method is invoked after {@link #dnsStart}.
     *
     * @param call            The call information.
     * @param domainName      The hostname.
     * @param inetAddressList The list of IP addresses.
     */
    public void dnsEnd(NewCall call, String domainName, List<InetAddress> inetAddressList) {

    }

    /**
     * Invoked just prior to initiating a socket connection. This method will be invoked if no existing connection in
     * the {@link ConnectionPool} can be reused.
     *
     * @param call              The call information.
     * @param inetSocketAddress The socket address.
     * @param proxy             The proxy.
     */
    public void connectStart(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy) {

    }

    /**
     * Invoked prior to starting a TLS connection.
     *
     * @param call The call information.
     */
    public void secureConnectStart(NewCall call) {

    }

    /**
     * Invoked immediately after a TLS connection was attempted. This method is invoked after
     * {@link #secureConnectStart}.
     *
     * @param call      The call information.
     * @param handshake The handshake information.
     */
    public void secureConnectEnd(NewCall call, Handshake handshake) {

    }

    /**
     * Invoked immediately after a socket connection was attempted.
     *
     * @param call              The call information.
     * @param inetSocketAddress The socket address.
     * @param proxy             The proxy.
     * @param protocol          The protocol.
     */
    public void connectEnd(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {

    }

    /**
     * Invoked when a connection attempt fails. This failure is not terminal if further routes are available and failure
     * recovery is enabled.
     *
     * @param call              The call information.
     * @param inetSocketAddress The socket address.
     * @param proxy             The proxy.
     * @param protocol          The protocol.
     * @param ioe               The exception.
     */
    public void connectFailed(
            NewCall call,
            InetSocketAddress inetSocketAddress,
            Proxy proxy,
            Protocol protocol,
            IOException ioe) {

    }

    /**
     * Invoked after a connection is acquired for a {@code call}.
     *
     * @param call       The call information.
     * @param connection The connection information.
     */
    public void connectionAcquired(NewCall call, Connection connection) {

    }

    /**
     * Invoked after a connection is released for a {@code call}. This method is always invoked after
     * {@link #connectionAcquired(NewCall, Connection)}.
     *
     * @param call       The call information.
     * @param connection The connection information.
     */
    public void connectionReleased(NewCall call, Connection connection) {

    }

    /**
     * Invoked just prior to sending request headers. The connection is implicit and is typically associated with the
     * last {@link #connectionAcquired(NewCall, Connection)} event.
     *
     * @param call The call information.
     */
    public void requestHeadersStart(NewCall call) {

    }

    /**
     * Invoked immediately after sending request headers. This method is always invoked after
     * {@link #requestHeadersStart(NewCall)}.
     *
     * @param call    The call information.
     * @param request The request sent over the network.
     */
    public void requestHeadersEnd(NewCall call, Request request) {

    }

    /**
     * Invoked just prior to sending a request body. Will only be invoked if the request permits and has a request body
     * to send. The connection is implicit and is typically associated with the last
     * {@link #connectionAcquired(NewCall, Connection)} event.
     *
     * @param call The call information.
     */
    public void requestBodyStart(NewCall call) {

    }

    /**
     * Invoked immediately after sending a request body. This method is always invoked after
     * {@link #requestBodyStart(NewCall)}.
     *
     * @param call      The call information.
     * @param byteCount The byte count.
     */
    public void requestBodyEnd(NewCall call, long byteCount) {

    }

    /**
     * Invoked when a request fails to be written. This method is invoked after {@link #requestHeadersStart} or
     * {@link #requestBodyStart}.
     *
     * @param call The call information.
     * @param ioe  The exception.
     */
    public void requestFailed(NewCall call, IOException ioe) {
    }

    /**
     * Invoked just prior to receiving response headers. The connection is implicit and is typically associated with the
     * last {@link #connectionAcquired(NewCall, Connection)} event. This may be invoked multiple times for a single
     * {@link NewCall}. For example, if the response to {@link NewCall#request()} is a redirect to another address.
     *
     * @param call The call information.
     */
    public void responseHeadersStart(NewCall call) {

    }

    /**
     * Invoked immediately after receiving response headers. This method is always invoked after
     * {@link #responseHeadersStart}.
     *
     * @param call     The call information.
     * @param response The response received from the network.
     */
    public void responseHeadersEnd(NewCall call, Response response) {

    }

    /**
     * Invoked just prior to receiving a response body. The connection is implicit and is typically associated with the
     * last {@link #connectionAcquired(NewCall, Connection)} event. This is typically invoked only once for a single
     * {@link NewCall}, except for a limited set of cases including failure recovery.
     *
     * @param call The call information.
     */
    public void responseBodyStart(NewCall call) {

    }

    /**
     * Invoked immediately after receiving a response body and completing reading it. Will only be invoked for requests
     * that have a response body, for example, it will not be invoked for a websocket upgrade. This method is always
     * invoked after {@link #requestBodyStart(NewCall)}.
     *
     * @param call      The call information.
     * @param byteCount The byte count.
     */
    public void responseBodyEnd(NewCall call, long byteCount) {

    }

    /**
     * Invoked when a response fails to be read. This method is invoked after {@link #responseHeadersStart} or
     * {@link #responseBodyStart}.
     *
     * @param call The call information.
     * @param ioe  The exception.
     */
    public void responseFailed(NewCall call, IOException ioe) {
    }

    /**
     * Invoked immediately after a call has completely ended. This includes any delayed consumption of the response body
     * by the caller. This method is always invoked after {@link #callStart(NewCall)}.
     *
     * @param call The call information.
     */
    public void callEnd(NewCall call) {

    }

    /**
     * Invoked when a call fails permanently. This method is always invoked after {@link #callStart(NewCall)}.
     *
     * @param call The call information.
     * @param ioe  The exception.
     */
    public void callFailed(NewCall call, IOException ioe) {

    }

    public interface Factory {

        /**
         * Creates an instance of the {@link EventListener} for a specific {@link NewCall}. The returned
         * {@link EventListener} instance will be used for the lifecycle of the {@code call}.
         *
         * <p>
         * This method is invoked after the {@code call} is created. See {@link Httpd#newCall(Request)}. It is an error
         * for implementations to issue any mutating operations on the {@code call} instance in this method.
         *
         * @param call The call information.
         * @return The listener.
         */
        EventListener create(NewCall call);
    }

}
