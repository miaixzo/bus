/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Defines protocol-neutral authentication contracts, immutable operation context values, policies, outcomes, and
 * provider metadata.
 *
 * <p>
 * Code here may depend on bus-core and stable bus-fabric value contracts, but never on concrete protocol or vendor
 * implementations. It must preserve the protocol-neutral boundary defined by the root package. This package is exported
 * by the {@code bus.auth} module as a stable public contract. Credentials, tokens, callback codes, state values, and
 * other sensitive material must not be retained or exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth;
