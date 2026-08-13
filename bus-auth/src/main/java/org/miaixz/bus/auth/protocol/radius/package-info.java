/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Defines the public RADIUS protocol facade and standard packet-processing contracts.
 *
 * <p>
 * Code here may depend on root authentication contracts, the common protocol boundary, bus-core, and bus-fabric; it
 * must not depend on vendor clients. Server-facing operations must return the standard response and error types owned
 * by their protocol. This package is exported by the {@code bus.auth} module as a stable public contract. Credentials,
 * tokens, callback codes, state values, and other sensitive material must not be retained or exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.radius;
