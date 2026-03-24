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
package org.miaixz.bus.core.lang.annotation.resolve.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Base implementation of {@link WrappedAnnotationAttribute}.
 *
 * @author Kimi Liu
 * @since Java 21+
 * @see ForceAliasedAnnotationAttribute
 * @see AliasedAnnotationAttribute
 * @see MirroredAnnotationAttribute
 */
public abstract class AbstractWrappedAnnotationAttribute implements WrappedAnnotationAttribute {

    /**
     * The original annotation attribute being wrapped.
     */
    protected final AnnotationAttribute original;

    /**
     * The linked annotation attribute that may override or mirror the original.
     */
    protected final AnnotationAttribute linked;

    /**
     * Constructs a new {@code AbstractWrappedAnnotationAttribute}.
     *
     * @param original the original annotation attribute; must not be {@code null}
     * @param linked   the linked annotation attribute; must not be {@code null}
     */
    protected AbstractWrappedAnnotationAttribute(final AnnotationAttribute original, final AnnotationAttribute linked) {
        Assert.notNull(original, "target must not null");
        Assert.notNull(linked, "linked must not null");
        this.original = original;
        this.linked = linked;
    }

    /**
     * Returns the wrapped original {@link AnnotationAttribute} object.
     *
     * @return the original annotation attribute
     */
    @Override
    public AnnotationAttribute getOriginal() {
        return original;
    }

    /**
     * Returns the linked {@link AnnotationAttribute} object.
     *
     * @return the linked annotation attribute
     */
    @Override
    public AnnotationAttribute getLinked() {
        return linked;
    }

    /**
     * Returns the innermost (non-wrapped) original {@link AnnotationAttribute} by recursively unwrapping the original
     * chain.
     *
     * @return the innermost original annotation attribute
     */
    @Override
    public AnnotationAttribute getNonWrappedOriginal() {
        AnnotationAttribute curr = null;
        AnnotationAttribute next = original;
        while (next != null) {
            curr = next;
            next = next.isWrapped() ? ((WrappedAnnotationAttribute) curr).getOriginal() : null;
        }
        return curr;
    }

    /**
     * Traverses the tree structure rooted at this instance and collects all non-wrapped leaf attributes.
     *
     * @return all non-wrapped leaf attributes in the tree
     */
    @Override
    public Collection<AnnotationAttribute> getAllLinkedNonWrappedAttributes() {
        final List<AnnotationAttribute> leafAttributes = new ArrayList<>();
        collectLeafAttribute(this, leafAttributes);
        return leafAttributes;
    }

    /**
     * Recursively collects non-wrapped (leaf) annotation attributes from the tree rooted at {@code curr}.
     *
     * @param curr           the current annotation attribute node
     * @param leafAttributes the list to collect leaf attributes into
     */
    private void collectLeafAttribute(final AnnotationAttribute curr, final List<AnnotationAttribute> leafAttributes) {
        if (ObjectKit.isNull(curr)) {
            return;
        }
        if (!curr.isWrapped()) {
            leafAttributes.add(curr);
            return;
        }
        final WrappedAnnotationAttribute wrappedAttribute = (WrappedAnnotationAttribute) curr;
        collectLeafAttribute(wrappedAttribute.getOriginal(), leafAttributes);
        collectLeafAttribute(wrappedAttribute.getLinked(), leafAttributes);
    }

}
