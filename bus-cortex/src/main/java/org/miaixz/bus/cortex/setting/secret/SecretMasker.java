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
package org.miaixz.bus.cortex.setting.secret;

import java.util.Map;
import java.util.TreeMap;

import org.miaixz.bus.cortex.setting.item.Item.Property;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Safe projection over already parsed definitions; never reparses an entity.
 *
 * @author Kimi Liu
 */
public final class SecretMasker {

    /**
     * Fixed safe-view placeholder, never an editable secret value.
     */
    public static final String MASK = "****";

    /**
     * Creates a stateless projector.
     */
    public SecretMasker() { // No initialization required.
    }

    /**
     * Masks a present string.
     *
     * @param value original value
     * @return fixed mask or absent
     */
    public String mask(String value) {
        return value == null ? null : MASK;
    }

    /**
     * Masks sensitive business values and protected dynamic attributes.
     *
     * @param document parsed definition
     * @return safe immutable projection
     */
    public ItemCodec.Document mask(ItemCodec.Document document) {
        Map<String, Property> properties = new TreeMap<>();
        document.properties().forEach((name, p) -> {
            String path = name.isEmpty() || name.startsWith("/") ? name : JsonSchema.pointer("", name);
            Property changed = p.with("value", member(p.value(), path, document))
                    .with("default", member(p.defaultValue(), path, document));
            if (document.protection() != ItemCodec.Protection.NONE)
                for (String attribute : p.attributes().keySet())
                    if (ItemCodec.isAttribute(attribute))
                        changed = changed.with(attribute, new JsonValue.StringValue(MASK));
            properties.put(name, changed);
        });
        return new ItemCodec.Document(properties, document.rules(), document.schema(), document.management(),
                document.protection());
    }

    /**
     * Masks one member using predecoded sensitivity.
     *
     * @param value    node
     * @param path     definition path
     * @param document rules
     * @return safe node
     */
    private JsonValue member(JsonValue value, String path, ItemCodec.Document document) {
        if (value == null)
            return null;
        if (document.protection() == ItemCodec.Protection.WHOLE)
            return new JsonValue.StringValue(MASK);
        JsonValue result = value;
        for (var rule : document.rules().entrySet())
            if (rule.getValue().sensitive()) {
                String key = rule.getKey();
                if (key.isEmpty() || key.equals(path) || path.startsWith(key + "/"))
                    return new JsonValue.StringValue(MASK);
                if (path.isEmpty() || key.startsWith(path + "/")) {
                    String relative = key.substring(path.length());
                    if (ItemCodec.at(result, relative) != null)
                        result = ItemCodec.write(result, relative, new JsonValue.StringValue(MASK), false);
                }
            }
        return result;
    }

}
