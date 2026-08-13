/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Defines context-aware asynchronous subject and secret resolution ports used by runtimes and protocols.
 *
 * <p>
 * Code here may depend on root context values, bus-core, and asynchronous bus-fabric contracts only. It must preserve
 * the protocol-neutral boundary defined by the root package. This package is exported by the {@code bus.auth} module as
 * a stable public contract. Credentials, tokens, callback codes, state values, and other sensitive material must not be
 * retained or exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.resolver;
