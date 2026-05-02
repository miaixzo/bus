/**
 * Profile directory abstractions for the setting domain.
 * <p>
 * Profile is a namespace-scoped environment dimension rather than a child resource of app, and it inherits the shared
 * setting-domain base fields from {@link org.miaixz.bus.cortex.Setting}. The canonical relationships are
 * {@code namespace -> profile} and {@code namespace -> app -> item}. Both app and item may bind to multiple
 * {@code profile_ids}.
 */
package org.miaixz.bus.cortex.setting.profile;
