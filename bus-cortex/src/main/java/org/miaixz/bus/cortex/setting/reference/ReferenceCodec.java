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
package org.miaixz.bus.cortex.setting.reference;

import java.util.*;

import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Sole parser for current or frozen relationship options; never parses Item definitions.
 *
 * @author Kimi Liu
 */
public final class ReferenceCodec {

    /**
     * Maximum option depth.
     */
    private final int maximumDepth;
    /**
     * Maximum option work.
     */
    private final long maximumSteps;

    /**
     * Creates bounded option parsing.
     *
     * @param maximumDepth maximum tree depth
     * @param maximumSteps maximum work
     */
    public ReferenceCodec(int maximumDepth, long maximumSteps) {
        if (maximumDepth < 1 || maximumSteps < 1)
            throw new IllegalArgumentException("Invalid option bounds");
        this.maximumDepth = maximumDepth;
        this.maximumSteps = maximumSteps;
    }

    /**
     * Encodes typed frozen options without reparsing.
     *
     * @param options parsed options
     * @return option tree
     */
    public static JsonValue.ObjectValue tree(Options options) {
        Map<String, JsonValue> map = new TreeMap<>();
        map.put("type", new JsonValue.StringValue(options.type().name()));
        map.put("alias", new JsonValue.StringValue(options.alias()));
        map.put("prefix", new JsonValue.StringValue(options.prefix()));
        map.put("override", new JsonValue.StringValue(options.override().name()));
        map.put(
                "keys",
                new JsonValue.ArrayValue(
                        options.keys().stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast).toList()));
        if (options.overlay() != null)
            map.put("overlay", options.overlay());
        return new JsonValue.ObjectValue(map);
    }

    /**
     * Decodes only this protocol's exact members.
     *
     * @param raw frozen options
     * @return parsed options
     */
    public static Options options(JsonValue raw) {
        if (!(raw instanceof JsonValue.ObjectValue object) || !Set
                .of("type", "alias", "prefix", "override", "keys", "overlay").containsAll(object.values().keySet()))
            throw new IllegalArgumentException("Invalid frozen relationship");
        Reference reference = new Reference();
        reference.setType(((JsonValue.StringValue) object.values().get("type")).value());
        reference.setAlias(((JsonValue.StringValue) object.values().get("alias")).value());
        reference.setPrefix(((JsonValue.StringValue) object.values().get("prefix")).value());
        reference.setOverride(((JsonValue.StringValue) object.values().get("override")).value());
        reference.setKeys(ItemCodec.encode(object.values().get("keys")));
        reference.setOverlay(ItemCodec.encode(object.values().get("overlay")));
        return new ReferenceCodec(64, 100000).decode(reference);
    }

    /**
     * Decodes one relationship once.
     *
     * @param reference relationship entity
     * @return typed usage options
     */
    public Options decode(Reference reference) {
        Set<String> keys = new TreeSet<>();
        JsonValue raw = ItemCodec.tree(reference.getKeys());
        if (raw != null) {
            if (!(raw instanceof JsonValue.ArrayValue array))
                throw new IllegalArgumentException("Override keys must be an array");
            for (JsonValue key : array.values())
                if (key instanceof JsonValue.StringValue text)
                    keys.add(text.value());
                else
                    throw new IllegalArgumentException("Override path must be text");
        }
        JsonValue overlay = ItemCodec.tree(reference.getOverlay());
        if (overlay != null && !(overlay instanceof JsonValue.ObjectValue))
            throw new IllegalArgumentException("Overlay must be an object");
        if (overlay != null)
            JsonSchema.validate(new JsonValue.BooleanValue(true), overlay, maximumDepth, maximumSteps);
        Options options = new Options(Reference.Type.valueOf(reference.getType()), reference.getAlias(),
                reference.getPrefix(),
                reference.getOverride() == null ? Override.NONE : Override.valueOf(reference.getOverride()), keys,
                (JsonValue.ObjectValue) overlay);
        if (options.type() == Reference.Type.ITEM_OWNER && reference.getRevision_id() != null)
            throw new IllegalArgumentException("Owner cannot pin a revision");
        return options;
    }

    /**
     * Encodes normalized options without changing source/target identity.
     *
     * @param options     typed options
     * @param coordinates detached relationship
     * @return encoded relationship
     */
    public Reference encode(Options options, Reference coordinates) {
        coordinates.setType(options.type().name());
        coordinates.setAlias(options.alias());
        coordinates.setPrefix(options.prefix());
        coordinates.setOverride(options.override().name());
        coordinates.setKeys(
                ItemCodec.encode(
                        new JsonValue.ArrayValue(options.keys().stream().map(JsonValue.StringValue::new)
                                .map(JsonValue.class::cast).toList())));
        coordinates.setOverlay(ItemCodec.encode(options.overlay()));
        return coordinates;
    }

    /**
     * Allowed overlay behavior.
     *
     * @author Kimi Liu
     */
    public enum Override {
        /**
         * No application replacement.
         */
        NONE,
        /**
         * Exact allowed paths only.
         */
        KEYS,
        /**
         * Whole-source replacement allowed.
         */
        ALL

    }

    /**
     * Typed immutable usage options.
     *
     * @param type     relationship type
     * @param alias    app-local name
     * @param prefix   literal root prefix
     * @param override replacement policy
     * @param keys     exact allowed paths
     * @param overlay  optional protected relationship overlay
     * @author Kimi Liu
     */
    public record Options(Reference.Type type, String alias, String prefix, Override override, Set<String> keys,
            JsonValue.ObjectValue overlay) {

        /**
         * Validates and freezes domain options.
         *
         * @param type     relationship type
         * @param alias    alias
         * @param prefix   literal prefix
         * @param override replacement policy
         * @param keys     exact paths
         * @param overlay  protected object
         */
        public Options {
            Objects.requireNonNull(type);
            Objects.requireNonNull(override);
            alias = alias == null ? "" : alias.trim();
            prefix = prefix == null ? "" : prefix.trim();
            keys = Collections.unmodifiableSet(new TreeSet<>(keys));
            if (alias.length() > 255 || prefix.length() > 255)
                throw new IllegalArgumentException("Relationship name too long");
            for (String key : keys)
                if (key.isEmpty() || key.length() > 4096)
                    throw new IllegalArgumentException("Invalid override path");
                else
                    JsonSchema.tokens(key);
            if ((override == Override.KEYS) == keys.isEmpty())
                throw new IllegalArgumentException("Override paths mismatch");
            if (type == Reference.Type.ITEM_OWNER && (!prefix.isEmpty() || override != Override.NONE || !keys.isEmpty()
                    || overlay != null && !overlay.values().isEmpty()))
                throw new IllegalArgumentException("Owner cannot carry reference options");
        }

    }

}
