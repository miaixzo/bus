/* Copyright (c) 2015-2026 miaixz.org; licensed under the Apache License, Version 2.0. */
/**
 * Publishes the closed immutable catalog of built-in third-party client definitions and factories.
 *
 * <p>
 * Code here may depend on root and vendor contracts plus immutable endpoint metadata; it must not execute network
 * operations. It must preserve the protocol-neutral boundary defined by the root package. This package is exported by
 * the {@code bus.auth} module as a stable public contract. Credentials, tokens, callback codes, state values, and other
 * sensitive material must not be retained or exposed by diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.catalog;
