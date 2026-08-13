/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Defines third-party authentication client contracts, immutable registration metadata, token values, identities, and
 * shared client behavior.
 *
 * <p>
 * Code here may depend on root contracts and shared bus-core, bus-cache, bus-crypto, bus-extra, and bus-fabric
 * abstractions, but not on protocol server implementations. It must preserve the protocol-neutral boundary defined by
 * the root package. This package is exported by the {@code bus.auth} module as a stable public contract. Credentials,
 * tokens, callback codes, state values, and other sensitive material must not be retained or exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor;
