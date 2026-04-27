/**
 * External-facing setting delivery services.
 * <p>
 * {@code ItemQueryService} resolves one or more logical setting entries for callers that are allowed to read them.
 * {@code ItemExportService} exports resolved values for namespace/group/profile scopes while enforcing exposure
 * policies so that only public entries leave the control plane. {@code RuntimeItemOverlayService} provides a separate
 * non-revisioned runtime overlay path backed by the shared cache.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.setting.delivery;
