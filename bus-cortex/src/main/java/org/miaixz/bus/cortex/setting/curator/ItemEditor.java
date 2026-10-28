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

import java.util.*;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.EnumValue.Format;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.Item.Property;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.secret.NoOpSecretCodec;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Prepares current definitions and protects business members and dynamic attributes; never writes or versions them.
 *
 * @author Kimi Liu
 */
public final class ItemEditor {

    /**
     * Unique definition parser.
     */
    private final ItemCodec codec;
    /**
     * Required storage protection implementation.
     */
    private final SecretCodec secrets;
    /**
     * Safe view projection.
     */
    private final SecretMasker masker;

    /**
     * Creates the editor.
     *
     * @param codec   definition parser
     * @param secrets protecting codec
     * @param masker  safe view projector
     */
    public ItemEditor(ItemCodec codec, SecretCodec secrets, SecretMasker masker) {
        this.codec = Objects.requireNonNull(codec);
        this.secrets = Objects.requireNonNull(secrets);
        this.masker = Objects.requireNonNull(masker);
    }

    /**
     * Tests strict path ancestry.
     *
     * @param parent canonical ancestor
     * @param child  canonical descendant
     * @return ancestry result
     */
    private static boolean ancestor(String parent, String child) {
        return parent.isEmpty() || parent.equals(child) || child.startsWith(parent + "/");
    }

    /**
     * Rejects view placeholders in replacement data.
     *
     * @param value replacement
     * @return whether a placeholder occurs
     */
    private static boolean containsMask(JsonValue value) {
        if (value instanceof JsonValue.StringValue text)
            return SecretMasker.MASK.equals(text.value());
        if (value instanceof JsonValue.ObjectValue object)
            return object.values().values().stream().anyMatch(ItemEditor::containsMask);
        if (value instanceof JsonValue.ArrayValue array)
            return array.values().stream().anyMatch(ItemEditor::containsMask);
        return false;
    }

    /**
     * Detaches an entity without ORM state.
     *
     * @param item source entity
     * @return defensive copy
     */
    private static Item copy(Item item) {
        return JsonKit.toPojo(JsonKit.toJsonString(item), Item.class);
    }

    /**
     * Prepares an explicit edit without assigning edition or persisting.
     *
     * @param expected  current protected item
     * @param candidate explicit edited definition
     * @param actions   exact business-member and extension-attribute intents
     * @param level     validation level
     * @return protected candidate
     */
    public Item prepare(
            Item expected,
            Item candidate,
            Map<String, SecretAction> actions,
            ItemCodec.ValidationLevel level) {
        Item next = copy(Objects.requireNonNull(candidate));
        if (next.getTenant_id() == null || next.getTenant_id().isBlank())
            throw new IllegalArgumentException("Tenant is required");
        if (expected != null && (!Objects.equals(next.getTenant_id(), expected.getTenant_id())
                || !Objects.equals(next.getId(), expected.getId())))
            throw new IllegalArgumentException("Definition identity changed");
        if (next.getId() == null)
            next.setId(ID.objectId());
        if (!PatternKit.isMatch(Pattern.get("[A-Za-z0-9._-]{1,255}", 0), next.getCode()))
            throw new IllegalArgumentException("Invalid configuration code");
        if (next.getType() == null)
            next.setType(Item.Type.CONFIG_MAP.key());
        Item.Type.of(next.getType());
        if (next.getStatus() == null)
            next.setStatus(Item.Status.ACTIVE.key());
        Item.Status.of(next.getStatus());
        if (next.getFormat() == null)
            next.setFormat(Format.JSON.name());
        if (next.getEncrypted() == null)
            next.setEncrypted(0);
        if (next.getEncrypted() != 0 && next.getEncrypted() != 1)
            throw new IllegalArgumentException("Invalid protection flag");
        if (next.getType() == Item.Type.SECRET.key())
            next.setEncrypted(1);
        boolean whole = next.getType() == Item.Type.SECRET.key()
                || next.getEncrypted() == 1 && (expected == null || !keyMode(expected));
        ItemCodec.Document incoming = codec.decode(
                Objects.requireNonNull(next.getContent()),
                next.getFormat(),
                next.getSchema(),
                next.getExtension());
        if (incoming.protection() != ItemCodec.Protection.NONE)
            throw new IllegalArgumentException("Protection metadata is server-owned");
        ItemCodec.Document before = expected == null ? null : open(expected);
        if (expected != null && (expected.getType() == Item.Type.SECRET.key() || expected.getEncrypted() == 1) && !whole
                && incoming.rules().values().stream().noneMatch(ItemCodec.Rule::sensitive))
            throw new IllegalArgumentException("Cannot implicitly remove protection");
        Map<String, SecretAction> commands = actions == null ? Map.of() : Map.copyOf(actions);
        Map<String, Map<String, SecretAction>> attributeActions = new HashMap<>();
        commands.forEach((coordinate, action) -> {
            if (coordinate.startsWith("#")) {
                List<String> tokens = ItemCodec.attributePath(coordinate);
                attributeActions.computeIfAbsent(tokens.getFirst(), ignored -> new TreeMap<>()).put(tokens.get(1), action);
            }
        });
        boolean attributeProtection = whole || expected != null && Integer.valueOf(1).equals(expected.getEncrypted())
                || incoming.rules().values().stream().anyMatch(ItemCodec.Rule::sensitive);
        Set<String> used = new HashSet<>();
        Map<String, Property> properties = new TreeMap<>();
        Set<String> names = new TreeSet<>(incoming.properties().keySet());
        if (before != null)
            names.addAll(before.properties().keySet());
        for (String name : names) {
            Property old = before == null ? null : before.properties().get(name),
                    fresh = incoming.properties().get(name);
            String path = name.isEmpty() || name.startsWith("/") ? name : JsonSchema.pointer("", name);
            JsonValue value = edit(
                    path,
                    "value",
                    old == null ? null : old.value(),
                    fresh == null ? null : fresh.value(),
                    before,
                    incoming,
                    whole,
                    commands,
                    used);
            JsonValue fallback = edit(
                    path,
                    "default",
                    old == null ? null : old.defaultValue(),
                    fresh == null ? null : fresh.defaultValue(),
                    before,
                    incoming,
                    whole,
                    commands,
                    used);
            Property seed = fresh != null ? fresh : old != null ? old : new Property(Map.of());
            Property changed = editAttributes(name, old, fresh, seed.with("value", value).with("default", fallback),
                    attributeProtection, attributeActions.getOrDefault(path, Map.of()), used);
            if (fresh != null || value != null || fallback != null || changed.attributes().keySet().stream().anyMatch(ItemCodec::isAttribute))
                properties.put(name, changed);
        }
        if (!used.containsAll(commands.keySet()))
            throw new IllegalArgumentException("Unknown protected-member action");
        Map<String, ItemCodec.Rule> rules = new TreeMap<>(incoming.rules());
        if (before != null)
            before.rules().forEach((path, rule) -> {
                if (rule.sensitive() && !rules.containsKey(path) && !removed(path, before,
                        new ItemCodec.Document(properties, rules, incoming.schema(), incoming.management(),
                                ItemCodec.Protection.NONE), commands, used))
                    throw new IllegalArgumentException("Sensitive rule cannot be implicitly removed");
            });
        if (whole)
            properties.keySet().forEach(name -> {
                String path = name.isEmpty() || name.startsWith("/") ? name : JsonSchema.pointer("", name);
                var rule = rules.get(path);
                rules.put(path, new ItemCodec.Rule(JsonSchema.tokens(path), true, rule == null ? null : rule.schema()));
            });
        ItemCodec.Document plain = new ItemCodec.Document(properties, rules, incoming.schema(), incoming.management(),
                ItemCodec.Protection.NONE);
        codec.validate(plain, Objects.requireNonNull(level));
        if (expected != null && before != null && plain.properties().equals(before.properties())
                && plain.rules().equals(before.rules()) && Objects.equals(plain.schema(), before.schema())
                && plain.management().equals(before.management()) && Objects.equals(next.getType(), expected.getType())
                && Objects.equals(next.getFormat(), expected.getFormat())
                && Objects.equals(next.getCode(), expected.getCode())
                && Objects.equals(next.getDescription(), expected.getDescription())
                && Objects.equals(next.getEncrypted(), expected.getEncrypted())
                && (expected.getEncrypted() != 1 || protection(expected) == ItemCodec.Protection.VALUES))
            return copy(expected);
        boolean partial = whole || rules.values().stream().anyMatch(ItemCodec.Rule::sensitive);
        next.setEncrypted(partial ? 1 : 0);
        ItemCodec.Document marked = new ItemCodec.Document(properties, rules, plain.schema(), plain.management(),
                partial ? ItemCodec.Protection.VALUES : ItemCodec.Protection.NONE);
        next.setContent(
                codec.content(
                                partial ? crypt(
                                        marked,
                                        true,
                                        before,
                                        expected != null && keyMode(expected) && before.rules().equals(marked.rules())
                                                ? codec.decode(
                                                        expected.getContent(),
                                                        expected.getFormat(),
                                                        null,
                                                        expected.getExtension())
                                                : null)
                                        : plain));
        next.setSchema(ItemCodec.encode(plain.schema()));
        next.setExtension(codec.extension(marked));
        next.setChecksum(codec.checksum(next));
        return next;
    }

    /**
     * Verifies that every affected old member was explicitly deleted before retiring its sensitive rule.
     * Present replacement data, including JSON null, still requires the protection rule.
     *
     * @param path previous sensitive rule coordinate
     * @param before previous plaintext definition
     * @param after processed current definition
     * @param commands explicit member intents
     * @param used verified consumed intent coordinates
     * @return whether the rule protects no remaining data after explicit deletion
     */
    private boolean removed(String path, ItemCodec.Document before, ItemCodec.Document after,
            Map<String, SecretAction> commands, Set<String> used) {
        if (ItemCodec.at(after.values(), path) != null || ItemCodec.at(after.defaults(), path) != null)
            return false;
        Set<String> selected = new TreeSet<>();
        before.properties().keySet().forEach(name -> {
            String pointer = ItemCodec.coordinate(name);
            if (ancestor(path, pointer))
                selected.add(pointer);
            else if (ancestor(pointer, path))
                selected.add(path);
        });
        return !selected.isEmpty() && selected.stream().allMatch(pointer ->
                commands.get(pointer + "/value") == SecretAction.DELETE
                        && commands.get(pointer + "/default") == SecretAction.DELETE
                        && used.contains(pointer + "/value") && used.contains(pointer + "/default"));
    }

    /**
     * Applies atomic dynamic-attribute edits without interpreting metadata as business paths.
     *
     * @param name exact definition key
     * @param before previous plaintext property
     * @param incoming submitted property
     * @param changed already processed business members
     * @param protectedAttributes whether metadata requires protection
     * @param commands exact edit intents
     * @param used consumed command coordinates
     * @return complete property with untouched attributes retained
     */
    private Property editAttributes(
            String name,
            Property before,
            Property incoming,
            Property changed,
            boolean protectedAttributes,
            Map<String, SecretAction> commands,
            Set<String> used) {
        Set<String> attributes = new TreeSet<>(changed.attributes().keySet());
        if (before != null)
            attributes.addAll(before.attributes().keySet());
        attributes.addAll(commands.keySet());
        for (String attribute : attributes) {
            if (!ItemCodec.isAttribute(attribute))
                continue;
            String coordinate = ItemCodec.attributePath(name, attribute);
            SecretAction action = commands.get(attribute);
            JsonValue previous = before == null ? null : before.get(attribute),
                    candidate = incoming == null ? null : incoming.get(attribute);
            if (action != null)
                used.add(coordinate);
            if (action == SecretAction.DELETE)
                candidate = null;
            else if (action == SecretAction.REPLACE) {
                if (candidate == null || protectedAttributes && containsMask(candidate))
                    throw new IllegalArgumentException("Explicit attribute replacement required");
            } else if (protectedAttributes || action == SecretAction.KEEP) {
                if (previous == null && candidate != null)
                    throw new IllegalArgumentException("New sensitive attribute requires REPLACE");
                candidate = previous;
            }
            changed = changed.with(attribute, candidate);
        }
        return changed;
    }

    /**
     * Applies one member's protected edit intent.
     *
     * @param path     property path
     * @param member   value/default
     * @param old      old plaintext member
     * @param fresh    incoming member
     * @param before   previous rules
     * @param incoming next rules
     * @param whole    whole protection
     * @param commands explicit intents
     * @param used     consumed commands
     * @return edited member
     */
    private JsonValue edit(
            String path,
            String member,
            JsonValue old,
            JsonValue fresh,
            ItemCodec.Document before,
            ItemCodec.Document incoming,
            boolean whole,
            Map<String, SecretAction> commands,
            Set<String> used) {
        Set<String> sensitive = new TreeSet<>();
        if (whole || before != null && before.protection() == ItemCodec.Protection.WHOLE)
            sensitive.add(path);
        if (before != null)
            before.rules().forEach((p, r) -> {
                if (r.sensitive())
                    sensitive.add(p);
            });
        incoming.rules().forEach((p, r) -> {
            if (r.sensitive())
                sensitive.add(p);
        });
        JsonValue result = fresh;
        for (String pointer : sensitive) {
            if (!(ancestor(pointer, path) || ancestor(path, pointer)))
                continue;
            if (sensitive.stream().anyMatch(p -> !p.equals(pointer) && ancestor(p, pointer) && ancestor(path, p)))
                continue;
            String selected = ancestor(pointer, path) ? path : pointer, relative = selected.substring(path.length());
            String command = selected + "/" + member;
            SecretAction action = commands.get(command);
            JsonValue previous = relative.isEmpty() ? old : ItemCodec.at(old, relative),
                    candidate = relative.isEmpty() ? result : ItemCodec.at(result, relative);
            if (action != null)
                used.add(command);
            if (action == SecretAction.REPLACE) {
                if (candidate == null || containsMask(candidate))
                    throw new IllegalArgumentException("Explicit secret replacement required");
            } else if (action == SecretAction.DELETE) {
                if (relative.isEmpty())
                    result = null;
                else if (result != null)
                    result = ItemCodec.write(result, relative, null, true);
            } else {
                if (previous == null && candidate != null)
                    throw new IllegalArgumentException("New sensitive member requires REPLACE");
                if (relative.isEmpty())
                    result = previous;
                else if (previous != null)
                    result = ItemCodec.write(
                            result == null ? new JsonValue.ObjectValue(Map.of()) : result,
                            relative,
                            previous,
                            false);
                else if (result != null)
                    result = ItemCodec.write(result, relative, null, true);
            }
        }
        return result;
    }

    /**
     * Reveals a verified protected definition for the requested operation.
     *
     * @param stored protected item
     * @return invocation-local plaintext definition
     */
    public ItemCodec.Document open(Item stored) {
        if (stored.getChecksum() == null || !stored.getChecksum().equals(codec.checksum(stored)))
            throw new IllegalStateException("Definition checksum mismatch");
        if (!Integer.valueOf(1).equals(stored.getEncrypted()))
            return codec.decode(stored.getContent(), stored.getFormat(), stored.getSchema(), stored.getExtension());
        if (secrets instanceof NoOpSecretCodec)
            throw new IllegalStateException("Protecting codec required");
        boolean keys = keyMode(stored);
        ItemCodec.Document parsed = codec.decode(
                keys ? stored.getContent() : secrets.decrypt(stored.getContent()),
                stored.getFormat(),
                stored.getSchema() == null ? null
                        : protection(stored) == ItemCodec.Protection.VALUES ? stored.getSchema() : secrets.decrypt(stored.getSchema()),
                keys ? stored.getExtension() : secrets.decrypt(stored.getExtension()));
        return keys ? crypt(parsed, false, null, null) : parsed;
    }

    /**
     * Returns a detached safe view after storage verification.
     *
     * @param stored protected item
     * @return masked definition
     */
    public ItemCodec.Document view(Item stored) {
        if (Integer.valueOf(1).equals(stored.getEncrypted()) && keyMode(stored)) {
            if (stored.getChecksum() == null || !stored.getChecksum().equals(codec.checksum(stored)))
                throw new IllegalStateException("Definition checksum mismatch");
            return masker.mask(codec.decode(stored.getContent(), stored.getFormat(),
                    protection(stored) == ItemCodec.Protection.VALUES ? stored.getSchema() : null, stored.getExtension()));
        }
        return masker.mask(open(stored));
    }

    /**
     * Reveals only the requested value cell, leaving every unrelated protected member unopened.
     *
     * @param stored protected definition
     * @param pointer exact business-value pointer
     * @return selected plaintext value
     */
    public JsonValue reveal(Item stored, String pointer) {
        if (stored.getChecksum() == null || !stored.getChecksum().equals(codec.checksum(stored)))
            throw new IllegalStateException("Definition checksum mismatch");
        if (Integer.valueOf(1).equals(stored.getEncrypted()) && !keyMode(stored))
            throw new IllegalStateException("Legacy whole protection requires conversion before per-value revelation");
        if (Integer.valueOf(1).equals(stored.getEncrypted()) && secrets instanceof NoOpSecretCodec)
            throw new IllegalStateException("Protecting codec required");
        var document = codec.decode(stored.getContent(), stored.getFormat(),
                Integer.valueOf(1).equals(stored.getEncrypted()) && protection(stored) != ItemCodec.Protection.VALUES ? null : stored.getSchema(),
                stored.getExtension());
        if (pointer == null || pointer.isEmpty() && (document.properties().size() != 1 || !document.properties().containsKey(""))
                || !pointer.isEmpty() && (!pointer.startsWith("/") || JsonSchema.tokens(pointer).isEmpty()))
            throw new IllegalArgumentException("Exact value cell required");
        var values = document.values();
        var ancestors = document.rules().entrySet().stream().filter(e -> e.getValue().sensitive() && ancestor(e.getKey(), pointer))
                .map(Map.Entry::getKey).sorted(Comparator.comparingInt(String::length)).toList();
        if (Integer.valueOf(1).equals(stored.getEncrypted()) && !ancestors.isEmpty()) {
            String path = ancestors.getFirst();
            var value = ItemCodec.at(values, path);
            if (!(value instanceof JsonValue.StringValue text))
                throw new IllegalStateException("Invalid protected member");
            return ItemCodec.at(ItemCodec.tree(secrets.decrypt(text.value())), pointer.substring(path.length()));
        }
        JsonValue selected = ItemCodec.at(values, pointer);
        if (selected == null)
            throw new IllegalArgumentException("Unknown stored property");
        Set<String> opened = new HashSet<>();
        for (String path : document.rules().entrySet().stream().filter(e -> e.getValue().sensitive() && ancestor(pointer, e.getKey()))
                .map(Map.Entry::getKey).sorted(Comparator.comparingInt(String::length)).toList()) {
            if (!Integer.valueOf(1).equals(stored.getEncrypted()) || opened.stream().anyMatch(parent -> ancestor(parent, path)))
                continue;
            String relative = path.substring(pointer.length());
            var value = ItemCodec.at(selected, relative);
            if (value == null)
                continue;
            if (!(value instanceof JsonValue.StringValue text))
                throw new IllegalStateException("Invalid protected member");
            selected = ItemCodec.write(selected, relative, ItemCodec.tree(secrets.decrypt(text.value())), false);
            opened.add(path);
        }
        return selected;
    }

    /**
     * Transforms sensitive subtrees in both explicit and default members.
     *
     * @param document parsed definition
     * @param encrypt  true to protect, false to reveal sensitive members
     * @param previous previous plaintext definition, or null when unavailable
     * @param stored   previous protected definition used to reuse unchanged ciphertext, or null
     * @return transformed definition
     */
    private ItemCodec.Document crypt(
            ItemCodec.Document document,
            boolean encrypt,
            ItemCodec.Document previous,
            ItemCodec.Document stored) {
        Map<String, Property> properties = new TreeMap<>();
        document.properties().forEach((name, property) -> {
            String path = name.isEmpty() || name.startsWith("/") ? name : JsonSchema.pointer("", name);
            Property old = previous == null ? null : previous.properties().get(name),
                    protectedOld = stored == null ? null : stored.properties().get(name);
            Property changed = property.with("value", cryptMember(property.value(), path, document, encrypt,
                    old == null ? null : old.value(), protectedOld == null ? null : protectedOld.value()))
                    .with("default", cryptMember(property.defaultValue(), path, document, encrypt,
                            old == null ? null : old.defaultValue(), protectedOld == null ? null : protectedOld.defaultValue()));
            if (document.protection() != ItemCodec.Protection.NONE)
                for (var attribute : property.attributes().entrySet())
                    if (ItemCodec.isAttribute(attribute.getKey()))
                        changed = changed.with(attribute.getKey(), protectedValue(attribute.getValue(), encrypt,
                                old == null ? null : old.get(attribute.getKey()),
                                protectedOld == null ? null : protectedOld.get(attribute.getKey())));
            properties.put(name, changed);
        });
        return new ItemCodec.Document(properties, document.rules(), document.schema(), document.management(),
                document.protection());
    }

    /**
     * Transforms a present exact member once per protected root.
     *
     * @param value    present member
     * @param path     source coordinate
     * @param document sensitivity rules
     * @param encrypt  true to protect, false to reveal the member
     * @param previous previous plaintext member, or null when unavailable
     * @param stored   previous protected member used to reuse unchanged ciphertext, or null
     * @return protected or revealed member
     */
    private JsonValue cryptMember(
            JsonValue value,
            String path,
            ItemCodec.Document document,
            boolean encrypt,
            JsonValue previous,
            JsonValue stored) {
        if (value == null)
            return null;
        JsonValue result = value;
        for (var rule : document.rules().entrySet()) {
            if (!rule.getValue().sensitive())
                continue;
            String pointer = rule.getKey();
            if (document.rules().entrySet().stream().anyMatch(
                    e -> e.getValue().sensitive() && !e.getKey().equals(pointer) && ancestor(e.getKey(), pointer)))
                continue;
            if (ancestor(pointer, path))
                return protectedValue(result, encrypt, previous, stored);
            if (ancestor(path, pointer)) {
                String relative = pointer.substring(path.length());
                JsonValue child = ItemCodec.at(result, relative);
                if (child != null)
                    result = ItemCodec.write(
                            result,
                            relative,
                            protectedValue(
                                    child,
                                    encrypt,
                                    ItemCodec.at(previous, relative),
                                    ItemCodec.at(stored, relative)),
                            false);
            }
        }
        return result;
    }

    /**
     * Reuses unchanged protected members instead of assigning a new random ciphertext.
     *
     * @param value    plaintext to protect or ciphertext to reveal
     * @param encrypt  true to protect, false to reveal the node
     * @param previous old plaintext
     * @param stored   old ciphertext
     * @return protected or revealed node
     */
    private JsonValue protectedValue(JsonValue value, boolean encrypt, JsonValue previous, JsonValue stored) {
        if (encrypt && stored != null && Objects.equals(ItemCodec.encode(value), ItemCodec.encode(previous)))
            return stored;
        return cryptValue(value, encrypt);
    }

    /**
     * Protects one typed node.
     *
     * @param value   node
     * @param encrypt direction
     * @return transformed node
     */
    private JsonValue cryptValue(JsonValue value, boolean encrypt) {
        if (encrypt)
            return new JsonValue.StringValue(secrets.protect(ItemCodec.encode(value)));
        if (!(value instanceof JsonValue.StringValue text))
            throw new IllegalStateException("Invalid protected member");
        return ItemCodec.tree(secrets.decrypt(text.value()));
    }

    /**
     * Detects explicit per-key storage metadata.
     *
     * @param item stored definition
     * @return whether keys are individually protected
     */
    private boolean keyMode(Item item) {
        return protection(item) == ItemCodec.Protection.KEYS || protection(item) == ItemCodec.Protection.VALUES;
    }

    /**
     * Reads the explicit storage marker without opening any protected value.
     *
     * @param item stored definition
     * @return storage layout; legacy opaque definitions retain WHOLE
     */
    public ItemCodec.Protection protection(Item item) {
        if (!Integer.valueOf(1).equals(item.getEncrypted()))
            return ItemCodec.Protection.NONE;
        if (item.getExtension() == null || !item.getExtension().stripLeading().startsWith("{"))
            return ItemCodec.Protection.WHOLE;
        JsonValue raw = ItemCodec.tree(item.getExtension());
        if (raw instanceof JsonValue.ObjectValue root
                && root.values().get("services") instanceof JsonValue.ObjectValue services
                && services.values().get("protection") instanceof JsonValue.StringValue mode
                && Set.of("KEYS", "VALUES").contains(mode.value()))
            return ItemCodec.Protection.valueOf(mode.value());
        return ItemCodec.Protection.WHOLE;
    }

    /**
     * Restores authorized immutable content as current input, without version behavior.
     *
     * @param current   current definition
     * @param published exact protected content
     * @return explicit restore candidate and sensitive edit intent
     */
    public Restoration restore(Item current, Item published) {
        ItemCodec.Document doc = open(published);
        Item candidate = copy(published);
        candidate.setId(current.getId());
        candidate.setEdition(current.getEdition());
        ItemCodec.Document plain = new ItemCodec.Document(doc.properties(), doc.rules(), doc.schema(), doc.management(),
                ItemCodec.Protection.NONE);
        candidate.setContent(codec.content(plain));
        candidate.setSchema(ItemCodec.encode(plain.schema()));
        candidate.setExtension(codec.extension(plain));
        Map<String, SecretAction> actions = new TreeMap<>();
        doc.properties().forEach((key, p) -> {
            String path = key.isEmpty() || key.startsWith("/") ? key : JsonSchema.pointer("", key);
            if (doc.protection() == ItemCodec.Protection.WHOLE) {
                if (p.value() != null)
                    actions.put(path + "/value", SecretAction.REPLACE);
                if (p.defaultValue() != null)
                    actions.put(path + "/default", SecretAction.REPLACE);
            } else
                for (var rule : doc.rules().entrySet())
                    if (rule.getValue().sensitive()) {
                        String selected = ancestor(rule.getKey(), path) ? path
                                : ancestor(path, rule.getKey()) ? rule.getKey() : null;
                        if (selected == null)
                            continue;
                        String relative = selected.substring(path.length());
                        if (ItemCodec.at(p.value(), relative) != null)
                            actions.put(selected + "/value", SecretAction.REPLACE);
                        if (ItemCodec.at(p.defaultValue(), relative) != null)
                            actions.put(selected + "/default", SecretAction.REPLACE);
                    }
        });
        ItemCodec.Document currentDocument = open(current);
        Set<String> oldPaths = new TreeSet<>();
        currentDocument.rules().forEach((path, rule) -> {
            if (rule.sensitive())
                oldPaths.add(path);
        });
        Set<String> names = new TreeSet<>(currentDocument.properties().keySet());
        names.addAll(doc.properties().keySet());
        for (String name : names) {
            String path = name.isEmpty() || name.startsWith("/") ? name : JsonSchema.pointer("", name);
            Set<String> selected = new TreeSet<>();
            if (currentDocument.protection() == ItemCodec.Protection.WHOLE)
                selected.add(path);
            for (String protectedPath : oldPaths)
                if (ancestor(protectedPath, path))
                    selected.add(path);
                else if (ancestor(path, protectedPath))
                    selected.add(protectedPath);
            Property target = doc.properties().get(name);
            Set<String> attributes = new TreeSet<>();
            if (target != null)
                attributes.addAll(target.attributes().keySet());
            if (currentDocument.properties().get(name) != null)
                attributes.addAll(currentDocument.properties().get(name).attributes().keySet());
            for (String attribute : attributes)
                if (ItemCodec.isAttribute(attribute))
                    actions.put(ItemCodec.attributePath(name, attribute), target != null && target.contains(attribute)
                            ? SecretAction.REPLACE : SecretAction.DELETE);
            for (String protectedPath : selected)
                for (String member : List.of("value", "default")) {
                    JsonValue node = target == null ? null
                            : member.equals("value") ? target.value() : target.defaultValue();
                    node = ItemCodec.at(node, protectedPath.substring(path.length()));
                    actions.put(
                            protectedPath + "/" + member,
                            node == null ? SecretAction.DELETE : SecretAction.REPLACE);
                }
        }
        return new Restoration(candidate, actions);
    }

    /**
     * Protects sensitive relationship-local values as one opaque object.
     *
     * @param source  parsed source sensitivity
     * @param overlay explicit application values
     * @return protected overlay
     */
    public JsonValue.ObjectValue protectOverlay(ItemCodec.Document source, JsonValue.ObjectValue overlay) {
        if (overlay == null || overlay.values().isEmpty())
            return overlay;
        if (overlay.values().containsKey("$protected"))
            throw new IllegalArgumentException("Overlay protection is server-owned");
        if (needsOverlayProtection(source, overlay))
            return new JsonValue.ObjectValue(
                    Map.of("$protected", new JsonValue.StringValue(secrets.protect(ItemCodec.encode(overlay)))));
        return overlay;
    }

    /**
     * Recognizes the complete server-owned overlay protection envelope without decrypting it.
     *
     * @param overlay relationship-local storage value
     * @return whether exactly one nonblank protected value is present and a protecting codec is available
     */
    public boolean isProtectedOverlay(JsonValue.ObjectValue overlay) {
        return overlay != null && overlay.values().size() == 1
                && overlay.values().get("$protected") instanceof JsonValue.StringValue text && !text.value().isBlank()
                && !(secrets instanceof NoOpSecretCodec);
    }

    /**
     * Evaluates overlay storage against the selected definition's current or frozen sensitivity rules.
     *
     * @param source  already verified selected definition
     * @param overlay relationship-local storage value
     * @return whether a nonempty sensitive overlay is missing a valid protection envelope
     */
    public boolean needsOverlayProtection(ItemCodec.Document source, JsonValue.ObjectValue overlay) {
        return overlay != null && !overlay.values().isEmpty()
                && (source.protection() != ItemCodec.Protection.NONE
                        || source.rules().values().stream().anyMatch(ItemCodec.Rule::sensitive))
                && !isProtectedOverlay(overlay);
    }

    /**
     * Opens an authorized frozen relationship overlay.
     *
     * @param overlay frozen protected object
     * @return exact plaintext overlay
     */
    public JsonValue.ObjectValue openOverlay(JsonValue.ObjectValue overlay) {
        if (overlay == null || !overlay.values().containsKey("$protected"))
            return overlay;
        if (overlay.values().size() != 1 || !(overlay.values().get("$protected") instanceof JsonValue.StringValue text)
                || secrets instanceof NoOpSecretCodec)
            throw new IllegalStateException("Invalid protected overlay");
        JsonValue value = ItemCodec.tree(secrets.decrypt(text.value()));
        if (!(value instanceof JsonValue.ObjectValue object))
            throw new IllegalStateException("Overlay object required");
        return object;
    }

    /**
     * Explicit protected-member intent.
     *
     * @author Kimi Liu
     */
    public enum SecretAction {
        /**
         * Retain existing protected member.
         */
        KEEP,
        /**
         * Replace the explicitly supplied member.
         */
        REPLACE,
        /**
         * Remove the explicitly selected member.
         */
        DELETE

    }

    /**
     * Explicit recoverable edit intent; no persistence or new snapshot.
     *
     * @param candidate plaintext current edit
     * @param actions   exact protected-member commands
     * @author Kimi Liu
     */
    public record Restoration(Item candidate, Map<String, SecretAction> actions) {

        /**
         * Freezes restoration intent.
         *
         * @param candidate explicit edit
         * @param actions   sensitive commands
         */
        public Restoration {
            Objects.requireNonNull(candidate);
            actions = Map.copyOf(actions);
        }

    }

}
