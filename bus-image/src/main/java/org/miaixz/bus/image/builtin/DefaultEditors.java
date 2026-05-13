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
package org.miaixz.bus.image.builtin;

import java.math.BigInteger;
import java.util.Objects;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.image.Editors;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.EditorContext;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.UpdatePolicy;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.data.Value;

/**
 * Default DICOM attribute editor used by bus-image.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultEditors implements Editors {

    /**
     * The generate ui ds value.
     */
    private final boolean generateUIDs;

    /**
     * The tag to override value.
     */
    private final Attributes tagToOverride;

    /**
     * The hmac value.
     */
    private final HMac hmac;

    /**
     * Creates a default editor that only applies configured attribute overrides.
     *
     * @param tagToOverride DICOM attributes that should override matching target attributes.
     */
    public DefaultEditors(Attributes tagToOverride) {
        this(false, null, tagToOverride);
    }

    /**
     * Creates a default editor for optional UID regeneration and attribute overriding.
     *
     * @param generateUIDs  whether UI values should be rewritten.
     * @param tagToOverride DICOM attributes that should override matching target attributes.
     */
    public DefaultEditors(boolean generateUIDs, Attributes tagToOverride) {
        this(generateUIDs, null, tagToOverride);
    }

    /**
     * Creates a default editor with a deterministic UID rewrite key.
     *
     * @param generateUIDs  whether UI values should be rewritten.
     * @param globalKey     hex encoded HMAC key for deterministic UID rewriting.
     * @param tagToOverride DICOM attributes that should override matching target attributes.
     */
    public DefaultEditors(boolean generateUIDs, String globalKey, Attributes tagToOverride) {
        this.generateUIDs = generateUIDs;
        this.tagToOverride = tagToOverride;
        this.hmac = generateUIDs ? createHmac(globalKey) : null;
    }

    /**
     * Executes the apply operation.
     *
     * @param attributes the attributes.
     * @param context    the context.
     */
    @Override
    public void apply(Attributes attributes, EditorContext context) {
        if (attributes == null) {
            return;
        }
        if (generateUIDs) {
            generateNewUIDs(attributes);
        }
        if (tagToOverride != null && !tagToOverride.isEmpty()) {
            attributes.update(UpdatePolicy.OVERWRITE, tagToOverride, null);
        }
    }

    /**
     * Creates the hmac.
     *
     * @param globalKey the global key.
     * @return the operation result.
     */
    private static HMac createHmac(String globalKey) {
        byte[] key = StringKit.hasText(globalKey) ? decodeHmacKey(globalKey)
                : RandomKit.randomBytes(16, RandomKit.getSecureRandom());
        return new HMac(Algorithm.HMACSHA256, key);
    }

    /**
     * Executes the decode hmac key operation.
     *
     * @param globalKey the global key.
     * @return the operation result.
     */
    private static byte[] decodeHmacKey(String globalKey) {
        String cleanHex = globalKey.startsWith("0x") ? globalKey.substring(2) : globalKey;
        cleanHex = cleanHex.replace("-", "");
        if (cleanHex.length() % 2 != 0) {
            cleanHex = "0" + cleanHex;
        }
        try {
            return HexKit.decode(cleanHex);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid HMAC key", e);
        }
    }

    /**
     * Executes the generate new ui ds operation.
     *
     * @param attributes the attributes.
     */
    private void generateNewUIDs(Attributes attributes) {
        try {
            attributes.accept((attrs, tag, vr, value) -> {
                if (vr == VR.UI && value != Value.NULL && isSupportedUidTag(tag)) {
                    processUidValue(attrs, tag, value);
                }
                return true;
            }, true);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate UIDs", e);
        }
    }

    /**
     * Determines whether supported uid tag.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isSupportedUidTag(int tag) {
        return switch (tag) {
            case Tag.StudyInstanceUID, Tag.SeriesInstanceUID, Tag.SOPInstanceUID, Tag.AffectedSOPInstanceUID, Tag.FailedSOPInstanceUIDList, Tag.MediaStorageSOPInstanceUID, Tag.ReferencedSOPInstanceUID, Tag.ReferencedSOPInstanceUIDInFile, Tag.RequestedSOPInstanceUID, Tag.MultiFrameSourceSOPInstanceUID -> true;
            default -> false;
        };
    }

    /**
     * Processes the uid value.
     *
     * @param attrs the attrs.
     * @param tag   the tag.
     * @param value the value.
     */
    private void processUidValue(Attributes attrs, int tag, Object value) {
        if (value instanceof byte[]) {
            processStringValues(attrs, tag, attrs.getStrings(tag));
        } else if (value instanceof String[] strings) {
            processStringValues(attrs, tag, strings);
        } else {
            processSingleValue(attrs, tag, value.toString());
        }
    }

    /**
     * Processes the string values.
     *
     * @param attrs  the attrs.
     * @param tag    the tag.
     * @param values the values.
     */
    private void processStringValues(Attributes attrs, int tag, String[] values) {
        if (values == null || values.length == 0) {
            return;
        }
        String[] updated = values.clone();
        for (int i = 0; i < updated.length; i++) {
            String hashedUid = uidHash(updated[i]);
            if (hashedUid != null) {
                updated[i] = hashedUid;
            }
        }
        attrs.setString(tag, VR.UI, updated);
    }

    /**
     * Processes the single value.
     *
     * @param attrs the attrs.
     * @param tag   the tag.
     * @param value the value.
     */
    private void processSingleValue(Attributes attrs, int tag, String value) {
        String hashedUid = uidHash(value);
        if (hashedUid != null) {
            attrs.setString(tag, VR.UI, hashedUid);
        }
    }

    /**
     * Executes the uid hash operation.
     *
     * @param inputUID the input uid.
     * @return the operation result.
     */
    private String uidHash(String inputUID) {
        if (!StringKit.hasText(inputUID)) {
            return null;
        }
        byte[] hash = byteHash(inputUID);
        byte[] uuid = new byte[16];
        System.arraycopy(hash, 0, uuid, 0, Math.min(uuid.length, hash.length));
        uuid[6] = (byte) ((uuid[6] & 0x0F) | 0x40);
        uuid[8] = (byte) ((uuid[8] & 0x3F) | 0x80);
        return "2.25." + new BigInteger(1, uuid);
    }

    /**
     * Executes the byte hash operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private byte[] byteHash(String value) {
        Objects.requireNonNull(value, "Value cannot be null");
        synchronized (hmac) {
            return hmac.digest(value);
        }
    }

}
