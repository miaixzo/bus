/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cortex.setting.curator;

import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.setting.item.GrayRule;
import org.miaixz.bus.cortex.setting.item.GrayRuleMatcher;
import org.miaixz.bus.cortex.setting.item.GrayRequestContext;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Resolves effective setting values using source adapters, rules and secret handling.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ItemValueResolver {

    /**
     * Source adapters keyed by their supported source.
     */
    private final Map<String, ItemSourceAdapter> adapters;
    /**
     * Gray-rule matcher used for conditional delivery.
     */
    private final GrayRuleMatcher grayRuleMatcher;
    /**
     * Secret codec used to decrypt protected resolved values.
     */
    private final SecretCodec secretCodec;

    /**
     * Creates an ItemValueResolver.
     *
     * @param adapters        configured source adapters
     * @param grayRuleMatcher rule matcher
     * @param secretCodec     secret codec used for protected values
     */
    public ItemValueResolver(List<ItemSourceAdapter> adapters, GrayRuleMatcher grayRuleMatcher,
            SecretCodec secretCodec) {
        this.adapters = new LinkedHashMap<>();
        if (adapters != null) {
            for (ItemSourceAdapter adapter : adapters) {
                if (adapter != null) {
                    String source = sourceKey(adapter.source());
                    if (source != null) {
                        this.adapters.put(source, adapter);
                    }
                }
            }
        }
        this.grayRuleMatcher = grayRuleMatcher;
        this.secretCodec = secretCodec;
    }

    /**
     * Resolves the effective value for the entry.
     *
     * @param entry          setting entry
     * @param requestContext optional request context used for gray routing
     * @return resolved value
     */
    public String resolve(Item entry, GrayRequestContext requestContext) {
        if (entry == null) {
            return null;
        }
        ItemSourceAdapter adapter = adapter(entry);
        if (!adapter.validate(entry)) {
            throw new IllegalArgumentException("Invalid setting source metadata for " + entry.getId());
        }
        String resolved = adapter.resolve(entry);
        GrayRule rule = rule(entry);
        if (rule != null && requestContext != null && grayRuleMatcher.matches(rule, requestContext)
                && rule.getGrayContent() != null) {
            resolved = rule.getGrayContent();
        }
        if (ItemNormalizer.isEncryptedFlagEnabled(entry.getEncrypted()) && resolved != null) {
            return secretCodec.decrypt(resolved);
        }
        return resolved;
    }

    /**
     * Selects the adapter that should resolve the supplied setting entry.
     *
     * @param entry setting entry requesting resolution
     * @return matching source adapter
     */
    private ItemSourceAdapter adapter(Item entry) {
        String type = valueOrInline(entry.getSource());
        ItemSourceAdapter direct = adapters.get(sourceKey(type));
        if (direct == null) {
            throw new IllegalStateException("No setting source adapter registered for " + type);
        }
        if (!direct.supports(entry)) {
            throw new IllegalArgumentException("Setting source adapter " + type + " does not support " + entry.getId());
        }
        return direct;
    }

    /**
     * Returns a preview value using the same adapter resolution path as live reads.
     *
     * @param entry setting entry
     * @return preview value
     */
    public String preview(Item entry) {
        if (entry == null) {
            return null;
        }
        ItemSourceAdapter adapter = adapter(entry);
        return adapter.preview(entry);
    }

    /**
     * Returns the effective source, defaulting blank values to inline content.
     *
     * @param source raw source
     * @return effective source
     */
    private String valueOrInline(String source) {
        return StringKit.isEmpty(source) ? ItemNormalizer.INLINE_SOURCE : source;
    }

    /**
     * Normalizes one source into the uppercase adapter-map lookup key.
     *
     * @param source raw source
     * @return uppercase adapter-map key or {@code null}
     */
    private String sourceKey(String source) {
        if (StringKit.isEmpty(source)) {
            return null;
        }
        return source.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Parses one serialized rule string into the structured rule model.
     *
     * @param entry setting entry
     * @return parsed rule, or {@code null} when absent
     */
    private GrayRule rule(Item entry) {
        if (entry == null || StringKit.isEmpty(entry.getRule())) {
            return null;
        }
        return JsonKit.toPojo(entry.getRule(), GrayRule.class);
    }

}
