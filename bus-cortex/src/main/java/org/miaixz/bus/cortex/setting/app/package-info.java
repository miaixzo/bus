/**
 * Application directory abstractions for the setting domain.
 * <p>
 * App is a namespace-scoped directory resource and inherits the shared setting-domain base fields from
 * {@link org.miaixz.bus.cortex.Setting}. The canonical relationship is {@code namespace -> app -> item}. App can expose
 * zero to many {@code profile_ids}; an empty set means all namespace-scoped profiles are allowed.
 */
package org.miaixz.bus.cortex.setting.app;
