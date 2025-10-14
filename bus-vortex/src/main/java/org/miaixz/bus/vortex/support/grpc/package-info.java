/**
 * Provides the concrete implementation for routing requests to downstream gRPC services.
 * <p>
 * This package contains the necessary components to act as a gRPC client. It is responsible for taking the incoming
 * gateway request, translating it into a gRPC message, and invoking the appropriate method on a remote gRPC service.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.grpc;
