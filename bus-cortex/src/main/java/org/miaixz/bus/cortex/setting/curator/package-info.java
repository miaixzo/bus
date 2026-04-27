/**
 * Curator internals for current-state storage, revision history, source adapters, and effective-value resolution.
 * <p>
 * When present, {@code ItemStore} acts as the durable current-state source of truth, while {@code StoreBackedItemStore}
 * coordinates that store with the CacheX projection and also supports cache-only fallback mode.
 * {@code ItemRevisionStore} defines revision history storage. {@code ItemValueResolver} selects a
 * {@code ItemSourceAdapter} to resolve the effective value of one logical setting item, applies gray-release rules, and
 * delegates protected-value decryption to the secret codec.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.setting.curator;
