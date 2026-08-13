/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Implements authentication runtime assembly, typed registries, lifecycle management, and provider dispatch.
 *
 * <p>
 * Code here may depend on root authentication contracts and the specific shared Bus abstractions required by its
 * internal role; reverse dependencies from exported contracts are forbidden. It must preserve the protocol-neutral
 * boundary defined by the root package. This package is module-internal and is not exported by the {@code bus.auth}
 * module. Credentials, tokens, callback codes, state values, and other sensitive material must not be retained or
 * exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.runtime;
