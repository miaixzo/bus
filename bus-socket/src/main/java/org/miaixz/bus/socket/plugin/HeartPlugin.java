/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.metric.HashedWheelTimer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A plugin for managing heartbeats in a socket communication session.
 * <p>
 * This plugin sends heartbeat requests and monitors for message timeouts to ensure session liveness. It can
 * automatically close sessions that fail to respond within a configured timeout period.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class HeartPlugin<T> extends AbstractPlugin<T> {

    /**
     * Default callback for handling session timeouts, which closes the session immediately.
     */
    private static final TimeoutCallback DEFAULT_TIMEOUT_CALLBACK = (session, lastTime) -> session.close(true);
    /**
     * A map to store the last activity timestamp for each session.
     */
    private Map<Session, Long> sessionMap = new HashMap<>();
    /**
     * The frequency at which heartbeats are sent, in milliseconds.
     */
    private long heartRate;
    /**
     * The maximum time (in milliseconds) a session can be inactive before being considered timed out.
     */
    private long timeout;
    /**
     * The callback to execute when a session times out.
     */
    private TimeoutCallback timeoutCallback;

    /**
     * Constructs a {@code HeartPlugin} with a specified heartbeat rate.
     *
     * @param heartRate the frequency of heartbeats
     * @param timeUnit  the time unit for {@code heartRate}
     */
    public HeartPlugin(int heartRate, TimeUnit timeUnit) {
        this(heartRate, 0, timeUnit);
    }

    /**
     * Constructs a {@code HeartPlugin} with a specified heartbeat rate and timeout.
     * <p>
     * In scenarios with network disconnections, TCP Retransmission might occur, making it difficult to perceive the
     * actual network status. Setting a timeout can help close connections in such cases.
     * </p>
     *
     * @param heartRate the frequency of heartbeats
     * @param timeout   the message timeout duration
     * @param unit      the time unit for {@code heartRate} and {@code timeout}
     */
    public HeartPlugin(int heartRate, int timeout, TimeUnit unit) {
        this(heartRate, timeout, unit, DEFAULT_TIMEOUT_CALLBACK);
    }

    /**
     * Constructs a {@code HeartPlugin} with a specified heartbeat rate, timeout, and custom timeout callback.
     * <p>
     * In scenarios with network disconnections, TCP Retransmission might occur, making it difficult to perceive the
     * actual network status. Setting a timeout can help close connections in such cases.
     * </p>
     *
     * @param heartRate       the frequency of heartbeats
     * @param timeout         the message timeout duration
     * @param timeUnit        the time unit for {@code heartRate} and {@code timeout}
     * @param timeoutCallback the callback to execute when a session times out
     */
    public HeartPlugin(int heartRate, int timeout, TimeUnit timeUnit, TimeoutCallback timeoutCallback) {
        if (timeout > 0 && heartRate >= timeout) {
            throw new IllegalArgumentException("heartRate must be less than timeout");
        }
        this.heartRate = timeUnit.toMillis(heartRate);
        this.timeout = timeUnit.toMillis(timeout);
        this.timeoutCallback = timeoutCallback;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public final boolean process(Session session, T data) {
        sessionMap.put(session, System.currentTimeMillis());
        // Return true if it's not a heartbeat response message, allowing further processing
        return !isHeartMessage(session, data);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public final void stateEvent(Status status, Session session, Throwable throwable) {
        switch (status) {
            case NEW_SESSION:
                sessionMap.put(session, System.currentTimeMillis());
                registerHeart(session, heartRate);
                // Register heartbeat monitoring
                break;

            case SESSION_CLOSED:
                // Remove heartbeat monitoring
                sessionMap.remove(session);
                break;

            default:
                break;
        }
    }

    /**
     * Defines how to send a custom heartbeat request message.
     *
     * @param session the session to send the heartbeat request to
     * @throws IOException if an I/O error occurs during sending
     */
    public abstract void sendHeartRequest(Session session) throws IOException;

    /**
     * Determines if the received message is a heartbeat message.
     * <p>
     * Heartbeat request and response messages may be the same or different, depending on the actual scenario.
     * Therefore, the interface definition does not distinguish between them.
     * </p>
     *
     * @param session the session that received the message
     * @param msg     the received message
     * @return {@code true} if the message is a heartbeat message, {@code false} otherwise
     */
    public abstract boolean isHeartMessage(Session session, T msg);

    /**
     * Registers a heartbeat task for the given session.
     *
     * @param session   the session for which to register the heartbeat
     * @param heartRate the heartbeat frequency in milliseconds
     */
    private void registerHeart(final Session session, final long heartRate) {
        if (heartRate <= 0) {
            Logger.info(
                    "Session: {} heartbeat interval is {}, terminating heartbeat monitoring task.",
                    session,
                    heartRate);
            return;
        }
        Logger.debug("Session: {} registering heartbeat task, heartbeat interval: {}", session, heartRate);
        HashedWheelTimer.DEFAULT_TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                if (session.isInvalid()) {
                    sessionMap.remove(session);
                    Logger.info("Session: {} is invalid, removing heartbeat task.", session);
                    return;
                }
                Long lastTime = sessionMap.get(session);
                if (lastTime == null) {
                    Logger.warn("Session: {} last activity time is null, initializing.", session);
                    lastTime = System.currentTimeMillis();
                    sessionMap.put(session, lastTime);
                }
                long current = System.currentTimeMillis();
                // Close connection if no message received within timeout
                if (timeout > 0 && (current - lastTime) > timeout) {
                    timeoutCallback.callback(session, lastTime);
                }
                // If no message received within heartRate, try sending a heartbeat message
                else if (current - lastTime > heartRate) {
                    try {
                        sendHeartRequest(session);
                        session.writeBuffer().flush();
                    } catch (IOException e) {
                        Logger.error("Heartbeat exception, will close session: {}", session, e);
                        session.close(true);
                    }
                }
                registerHeart(session, heartRate);
            }
        }, heartRate, TimeUnit.MILLISECONDS);
    }

    /**
     * Callback interface for handling session timeouts.
     */
    public interface TimeoutCallback {

        /**
         * Called when a session times out.
         *
         * @param session  the timed-out session
         * @param lastTime the last activity timestamp of the session
         */
        void callback(Session session, long lastTime);
    }

}
