/**
 * Provides the concrete implementation for executing gRPC requests to downstream services.
 * <p>
 * This package contains the {@link org.miaixz.bus.vortex.support.grpc.GrpcExecutor}, which is responsible for taking
 * the incoming gateway request, translating it into a gRPC message, and executing the appropriate method on a remote
 * gRPC service via HTTP gateway.
 * <p>
 * The corresponding {@link org.miaixz.bus.vortex.support.GrpcRouter} delegates routing logic to this executor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.grpc;
