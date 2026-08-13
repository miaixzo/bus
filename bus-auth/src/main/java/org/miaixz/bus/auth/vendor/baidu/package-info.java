/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Implements the internal baidu third-party authentication client and its authorization-scope metadata.
 *
 * <p>
 * Code here may depend on root and vendor contracts plus shared Bus transport, cache, JSON, and cryptography
 * components; it must not depend on protocol server implementations. It is a client-side implementation and must not
 * produce server protocol wire responses. This package is module-internal and is not exported by the {@code bus.auth}
 * module. Credentials, tokens, callback codes, state values, and other sensitive material must not be retained or
 * exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.baidu;
