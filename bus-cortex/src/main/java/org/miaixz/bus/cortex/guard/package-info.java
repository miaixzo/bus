/**
 * Guard abstractions for Cortex access-control policy, credential dispatch, and security-oriented configuration.
 * <p>
 * This package intentionally stays at the policy/configuration layer. Runtime gateway authorization is still performed
 * by the Vortex strategy chain, while {@code cortex.guard} provides the stable models and extension points used to
 * express policy intent, evaluate request context, and carry token-related settings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.guard;
