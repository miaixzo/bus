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
package org.miaixz.bus.image.galaxy.data;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base wrapper for DICOM module-like structures backed by {@link Attributes}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DicomModule {

    /**
     * The dcm items value.
     */
    protected final Attributes dcmItems;

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public DicomModule(Attributes dcmItems) {
        this.dcmItems = Objects.requireNonNull(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param attributes the attributes.
     * @return the operation result.
     */
    public static DicomModule of(Attributes attributes) {
        return new DicomModule(attributes);
    }

    /**
     * Executes the key object document operation.
     *
     * @param attributes the attributes.
     * @return the operation result.
     */
    public static KeyObjectDocumentModule keyObjectDocument(Attributes attributes) {
        return new KeyObjectDocumentModule(attributes);
    }

    /**
     * Gets the attributes.
     *
     * @return the attributes.
     */
    public final Attributes getAttributes() {
        return dcmItems;
    }

    /**
     * Removes the all sequence items.
     *
     * @param seqTag the seq tag.
     */
    public final void removeAllSequenceItems(int seqTag) {
        Sequence sequence = dcmItems.getSequence(seqTag);
        if (sequence != null) {
            sequence.clear();
        }
    }

    /**
     * Removes the sequence item.
     *
     * @param seqTag the seq tag.
     * @param index  the index.
     */
    public final void removeSequenceItem(int seqTag, int index) {
        Sequence sequence = dcmItems.getSequence(seqTag);
        if (sequence != null && index >= 0 && index < sequence.size()) {
            sequence.remove(index);
        }
    }

    /**
     * Removes the sequence item.
     *
     * @param seqTag the seq tag.
     * @param item   the item.
     */
    public final void removeSequenceItem(int seqTag, Attributes item) {
        Sequence sequence = dcmItems.getSequence(seqTag);
        if (sequence != null) {
            sequence.remove(item);
        }
    }

    /**
     * Updates the sequence.
     *
     * @param tag    the tag.
     * @param module the module.
     */
    protected final void updateSequence(int tag, DicomModule module) {
        clearSequence(tag);
        if (module != null) {
            dcmItems.newSequence(tag, 1).add(detached(module.getAttributes()));
        }
    }

    /**
     * Updates the sequence.
     *
     * @param tag     the tag.
     * @param modules the modules.
     */
    protected final void updateSequence(int tag, Collection<? extends DicomModule> modules) {
        clearSequence(tag);
        if (modules != null && !modules.isEmpty()) {
            Sequence sequence = dcmItems.newSequence(tag, modules.size());
            for (DicomModule module : modules) {
                if (module != null) {
                    sequence.add(detached(module.getAttributes()));
                }
            }
        }
    }

    /**
     * Updates the code sequence.
     *
     * @param tag  the tag.
     * @param code the code.
     */
    protected final void updateCodeSequence(int tag, Code code) {
        clearSequence(tag);
        if (code != null) {
            dcmItems.newSequence(tag, 1).add(code.toItem());
        }
    }

    /**
     * Executes the nested code operation.
     *
     * @param attributes the attributes.
     * @param tag        the tag.
     * @return the operation result.
     */
    protected static Code nestedCode(Attributes attributes, int tag) {
        Attributes item = attributes.getNestedDataset(tag);
        return item == null ? null : new Code(item);
    }

    /**
     * Executes the map sequence operation.
     *
     * @param sequence the sequence.
     * @param mapper   the mapper.
     * @param <T>      the mapped value type.
     * @return the operation result.
     */
    protected static <T> List<T> mapSequence(Sequence sequence, Function<Attributes, T> mapper) {
        if (sequence == null || sequence.isEmpty()) {
            return List.of();
        }
        return sequence.stream().map(mapper).toList();
    }

    /**
     * Executes the clear sequence operation.
     *
     * @param tag the tag.
     */
    private void clearSequence(int tag) {
        Sequence sequence = dcmItems.getSequence(tag);
        if (sequence != null) {
            sequence.clear();
        }
    }

    /**
     * Executes the detached operation.
     *
     * @param attributes the attributes.
     * @return the operation result.
     */
    private static Attributes detached(Attributes attributes) {
        return attributes.getParent() == null ? attributes : new Attributes(attributes);
    }

}
