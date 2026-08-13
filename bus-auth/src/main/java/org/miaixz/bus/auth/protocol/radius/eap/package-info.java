/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Implements the RADIUS eap layer behind the public RADIUS facade.
 *
 * <p>
 * Code here may depend on its public protocol facade, root contracts, bus-core, bus-crypto, and bus-fabric, but not on
 * vendor clients. Server-facing operations must return the standard response and error types owned by their protocol.
 * This package is module-internal and is not exported by the {@code bus.auth} module. Credentials, tokens, callback
 * codes, state values, and other sensitive material must not be retained or exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.radius.eap;
