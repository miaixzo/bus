/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~ Licensed under the Apache License, Version 2.0.                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
/**
 * Provides a forward-auth decision engine. The package sanitizes request headers, validates registered HTTPS origins,
 * and emits allow, deny, or redirect decisions; the product owns HTTP routing, reverse proxying, authentication UI,
 * sessions, and transport execution.
 */
package org.miaixz.bus.auth.metric.proxy;
