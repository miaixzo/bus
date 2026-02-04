/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.socket.secure;

import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;

import org.miaixz.bus.socket.buffer.VirtualBuffer;

/**
 * A model class to hold state and buffers during an SSL/TLS handshake process. This class encapsulates the necessary
 * components for managing the handshake between two parties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HandshakeModel {

    /**
     * The underlying {@link AsynchronousSocketChannel} for communication.
     */
    private AsynchronousSocketChannel socketChannel;
    /**
     * The {@link SSLEngine} used for SSL/TLS operations.
     */
    private SSLEngine sslEngine;
    /**
     * Application data buffer for writing.
     */
    private VirtualBuffer appWriteBuffer;
    /**
     * Network data buffer for writing.
     */
    private VirtualBuffer netWriteBuffer;
    /**
     * Application data buffer for reading.
     */
    private VirtualBuffer appReadBuffer;

    /**
     * Network data buffer for reading.
     */
    private VirtualBuffer netReadBuffer;
    /**
     * Callback to be invoked upon handshake completion.
     */
    private HandshakeCallback handshakeCallback;
    /**
     * Any exception that occurred during the handshake.
     */
    private Throwable exception;
    /**
     * Flag indicating if the handshake is finished.
     */
    private boolean finished;

    /**
     * Gets the socket channel.
     *
     * @return the {@link AsynchronousSocketChannel}
     */
    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * Sets the socket channel.
     *
     * @param socketChannel the {@link AsynchronousSocketChannel} to set
     */
    public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    /**
     * Gets the application write buffer.
     *
     * @return the {@link VirtualBuffer} for application data to write
     */
    public VirtualBuffer getAppWriteBuffer() {
        return appWriteBuffer;
    }

    /**
     * Sets the application write buffer.
     *
     * @param appWriteBuffer the {@link VirtualBuffer} to set
     */
    public void setAppWriteBuffer(VirtualBuffer appWriteBuffer) {
        this.appWriteBuffer = appWriteBuffer;
    }

    /**
     * Gets the network write buffer.
     *
     * @return the {@link VirtualBuffer} for network data to write
     */
    public VirtualBuffer getNetWriteBuffer() {
        return netWriteBuffer;
    }

    /**
     * Sets the network write buffer.
     *
     * @param netWriteBuffer the {@link VirtualBuffer} to set
     */
    public void setNetWriteBuffer(VirtualBuffer netWriteBuffer) {
        this.netWriteBuffer = netWriteBuffer;
    }

    /**
     * Gets the application read buffer.
     *
     * @return the {@link VirtualBuffer} for application data to read
     */
    public VirtualBuffer getAppReadBuffer() {
        return appReadBuffer;
    }

    /**
     * Sets the application read buffer.
     *
     * @param appReadBuffer the {@link VirtualBuffer} to set
     */
    public void setAppReadBuffer(VirtualBuffer appReadBuffer) {
        this.appReadBuffer = appReadBuffer;
    }

    /**
     * Gets the network read buffer.
     *
     * @return the {@link VirtualBuffer} for network data to read
     */
    public VirtualBuffer getNetReadBuffer() {
        return netReadBuffer;
    }

    /**
     * Sets the network read buffer.
     *
     * @param netReadBuffer the {@link VirtualBuffer} to set
     */
    public void setNetReadBuffer(VirtualBuffer netReadBuffer) {
        this.netReadBuffer = netReadBuffer;
    }

    /**
     * Gets the SSL engine.
     *
     * @return the {@link SSLEngine}
     */
    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    /**
     * Sets the SSL engine.
     *
     * @param sslEngine the {@link SSLEngine} to set
     */
    public void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    /**
     * Checks if the handshake is finished.
     *
     * @return {@code true} if the handshake is finished, {@code false} otherwise
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Sets the finished status of the handshake.
     *
     * @param finished {@code true} if the handshake is finished, {@code false} otherwise
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * Gets the handshake callback.
     *
     * @return the {@link HandshakeCallback}
     */
    public HandshakeCallback getHandshakeCallback() {
        return handshakeCallback;
    }

    /**
     * Sets the handshake callback.
     *
     * @param handshakeCallback the {@link HandshakeCallback} to set
     */
    public void setHandshakeCallback(HandshakeCallback handshakeCallback) {
        this.handshakeCallback = handshakeCallback;
    }

    /**
     * Gets the exception that occurred during the handshake.
     *
     * @return the {@link Throwable} exception, or {@code null} if no exception occurred
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Sets the exception that occurred during the handshake.
     *
     * @param exception the {@link Throwable} exception to set
     */
    public void setException(Throwable exception) {
        this.exception = exception;
    }

}
