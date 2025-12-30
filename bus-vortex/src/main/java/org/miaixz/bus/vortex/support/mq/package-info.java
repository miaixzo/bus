/**
 * Provides the concrete implementation for executing message queue (MQ) requests.
 * <p>
 * This package contains the {@link org.miaixz.bus.vortex.support.mq.MqExecutor}, which is responsible for taking the
 * request payload and executing it as a message to the appropriate topic or queue in a message broker.
 * <p>
 * The corresponding {@link org.miaixz.bus.vortex.support.MqRouter} delegates routing logic to this executor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.mq;
