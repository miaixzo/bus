/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation.resolve;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.map.multiple.Graph;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.annotation.resolve.elements.MetaAnnotatedElement;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

/**
 * An annotation mapping that wraps and enhances a regular annotation object. The wrapped annotation can be accessed via
 * {@code getResolvedXXX} methods, which support attribute aliasing and attribute overriding mechanisms.
 *
 * <p>
 * <strong>Parent-Child Annotations</strong>
 * <p>
 * When an instance is created, a child annotation can be specified via {@link #source}. Multiple instances can form a
 * unidirectional linked list representing parent-child or meta-annotation relationships. If {@link #source} is
 * {@code null}, the current annotation is considered a root annotation.
 *
 * <p>
 * <strong>Attribute Aliasing</strong>
 * <p>
 * Attributes within an annotation can be linked via {@link Alias}. When resolving, assigning a value to any attribute
 * in a binding will synchronize that value to all directly or indirectly linked attributes. For example, if an
 * annotation has an alias relationship {@code a <=> b <=> c}, assigning a value to {@code a} will also assign it to
 * {@code b} and {@code c}.
 *
 * <p>
 * <strong>Attribute Overriding</strong>
 * <p>
 * If {@link #source} is not {@code null} (i.e., the current annotation has one or more child annotations), and a child
 * annotation has an attribute with the same name and type as an attribute in the current annotation, then when
 * retrieving the value, the child annotation's value will take precedence. This also applies to aliased attributes.
 * Attribute overriding follows these rules:
 * <ul>
 * <li>If an overridden attribute has aliased attributes, the aliased attributes will also be overridden. For example,
 * if an annotation has an alias relationship {@code a <=> b <=> c}, overriding {@code a} will also override attributes
 * {@code b} and {@code c}.</li>
 * <li>If an attribute can be overridden by multiple child annotations, the child annotation closest to the root
 * annotation will take precedence. For example, if there is a dependency relationship {@code a => b => c} from a root
 * annotation {@code a} to meta-annotation {@code b}, and then to {@code c}, and an attribute in {@code c} can be
 * overridden by both {@code a} and {@code b}, then {@code a} will be chosen first.</li>
 * <li>If a child annotation's attribute that overrides another attribute is itself overridden by its own child
 * annotation, it is equivalent to the child's child annotation directly overriding the current annotation's attribute.
 * For example, if there is a dependency relationship {@code a => b => c}, and an attribute in {@code b} is overridden
 * by {@code a}, and the overridden attribute in {@code b} then overrides an attribute in {@code c}, it is equivalent to
 * the attribute in {@code c} being directly overridden by {@code a}.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @see MetaAnnotatedElement
 * @since Java 17+
 */
public class ResolvedAnnotationMapping implements AnnotationMapping<Annotation> {

    /**
     * Default index for attributes that are not found.
     */
    protected static final int NOT_FOUND_INDEX = -1;

    /**
     * The methods representing the attributes of the annotation. The index in this array corresponds to the attribute
     * itself.
     */
    private final Method[] attributes;

    /**
     * An array of {@link AliasSet} objects, where each element corresponds to an attribute in {@link #attributes}. Each
     * {@link AliasSet} groups attributes that are aliases of each other.
     */
    private final AliasSet[] aliasSets;

    /**
     * An array storing the resolved index for each attribute. An index other than {@link #NOT_FOUND_INDEX} indicates
     * that the attribute has been resolved. If the corresponding entry in {@link #resolvedAttributeSources} is null, it
     * means the attribute is an alias. If it's not null, it means the attribute has been overridden.
     */
    private final int[] resolvedAttributes;

    /**
     * An array storing the {@link ResolvedAnnotationMapping} instance that is the source of the resolved attribute. If
     * an attribute is overridden, this array will point to the annotation object that performed the override.
     */
    private final ResolvedAnnotationMapping[] resolvedAttributeSources;

    /**
     * The child annotation mapping. If this is {@code null}, the current annotation is considered a root annotation.
     */
    private final ResolvedAnnotationMapping source;

    /**
     * The original annotation object.
     */
    private final Annotation annotation;
    /**
     * Indicates whether the attributes of this annotation have been resolved (aliasing and overriding applied).
     */
    private final boolean resolved;
    /**
     * Cached proxy object for the resolved annotation, created on demand.
     */
    private volatile Annotation proxied;

    /**
     * Constructs a new {@code ResolvedAnnotationMapping} object.
     *
     * @param source           The child annotation mapping. Can be {@code null} if this is a root annotation.
     * @param annotation       The annotation object to wrap. Must not be {@code null}.
     * @param resolveAttribute Whether to resolve attributes (apply aliasing and overriding).
     * @throws NullPointerException     if {@code annotation} is {@code null}.
     * @throws IllegalArgumentException if {@code annotation} has already been proxied, or is already wrapped by
     *                                  {@code ResolvedAnnotationMapping}, or if {@code source} wraps the same
     *                                  annotation as {@code annotation}.
     */
    public ResolvedAnnotationMapping(final ResolvedAnnotationMapping source, final Annotation annotation,
            final boolean resolveAttribute) {
        Objects.requireNonNull(annotation);
        Assert.isFalse(AnnotationMappingProxy.isProxied(annotation), "annotation has been proxied");
        Assert.isFalse(annotation instanceof ResolvedAnnotationMapping, "annotation has been wrapped");
        Assert.isFalse(
                Objects.nonNull(source) && Objects.equals(source.annotation, annotation),
                "source annotation can not same with target [{}]",
                annotationType());
        this.annotation = annotation;
        this.attributes = AnnoKit.getAnnotationAttributes(annotation.annotationType());
        this.source = source;

        // Initialize alias sets
        this.aliasSets = new AliasSet[this.attributes.length];

        // Initialize resolved attributes and their sources
        this.resolvedAttributeSources = new ResolvedAnnotationMapping[this.attributes.length];
        this.resolvedAttributes = new int[this.attributes.length];
        Arrays.fill(this.resolvedAttributes, NOT_FOUND_INDEX);

        // Resolve attributes if required
        // TODO flag should be an enum to allow choosing: 1. only attribute aliasing, 2. only attribute overriding, 3.
        // both, 4. neither
        this.resolved = resolveAttribute && resolveAttributes();
    }

    /**
     * Creates a new {@code ResolvedAnnotationMapping} object for a root annotation.
     *
     * @param annotation                 The annotation object to wrap.
     * @param resolveAnnotationAttribute Whether to resolve annotation attributes (apply aliasing and overriding).
     * @return A new {@code ResolvedAnnotationMapping} instance.
     */
    public static ResolvedAnnotationMapping create(
            final Annotation annotation,
            final boolean resolveAnnotationAttribute) {
        return create(null, annotation, resolveAnnotationAttribute);
    }

    /**
     * Creates a new {@code ResolvedAnnotationMapping} object, where attributes from child annotations (and their
     * children) will override same-named and same-typed attributes in the current annotation. When multiple child
     * annotations can override an attribute, the one closest to the root annotation takes precedence.
     *
     * @param source                     The child annotation mapping. Can be {@code null} if this is a root annotation.
     * @param annotation                 The annotation object to wrap.
     * @param resolveAnnotationAttribute Whether to resolve annotation attributes (apply aliasing and overriding).
     * @return A new {@code ResolvedAnnotationMapping} instance.
     */
    public static ResolvedAnnotationMapping create(
            final ResolvedAnnotationMapping source,
            final Annotation annotation,
            final boolean resolveAnnotationAttribute) {
        return new ResolvedAnnotationMapping(source, annotation, resolveAnnotationAttribute);
    }

    /**
     * Resolves the attributes of the annotation, applying aliasing and overriding rules.
     *
     * @return {@code true} if any attributes were resolved, {@code false} otherwise.
     */
    private boolean resolveAttributes() {
        // TODO Support @Ignore: attributes marked with @Ignore cannot be overridden or aliased.
        // Resolve aliases within the same annotation.
        resolveAliasAttributes();
        // Use child annotations to override attributes in the current annotation.
        resolveOverwriteAttributes();
        // Check if any attribute resolution occurred.
        return IntStream.of(resolvedAttributes).anyMatch(idx -> NOT_FOUND_INDEX != idx);
    }

    /**
     * Checks whether the current annotation is a root annotation (i.e., has no child annotation).
     *
     * @return {@code true} if this is a root annotation, {@code false} otherwise.
     */
    @Override
    public boolean isRoot() {
        return Objects.isNull(source);
    }

    /**
     * Retrieves the root annotation mapping in the hierarchy.
     *
     * @return The {@code ResolvedAnnotationMapping} instance representing the root annotation.
     */
    public ResolvedAnnotationMapping getRoot() {
        ResolvedAnnotationMapping mapping = this;
        while (Objects.nonNull(mapping.source)) {
            mapping = mapping.source;
        }
        return mapping;
    }

    /**
     * Retrieves the methods representing the attributes of the annotation.
     *
     * @return An array of {@link Method} objects representing the annotation's attributes.
     */
    @Override
    public Method[] getAttributes() {
        return attributes;
    }

    /**
     * Retrieves the original annotation object wrapped by this mapping.
     *
     * @return The original annotation object.
     */
    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Indicates whether the attributes of this annotation have been resolved (i.e., aliasing and overriding applied).
     * If this value is {@code false}, then {@code getResolvedAttributeValue} will return the original attribute values,
     * and {@link #getResolvedAnnotation()} will return the original annotation object.
     *
     * @return {@code true} if annotation attributes have been resolved, {@code false} otherwise.
     */
    @Override
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Generates a synthetic annotation object via dynamic proxy, based on the current mapping object. This synthetic
     * annotation behaves like the original but incorporates enhanced features:
     * <ul>
     * <li>Supports attribute aliasing within the same annotation via {@link Alias}.</li>
     * <li>Supports attribute overriding where a child annotation's attribute with the same name and type overrides that
     * of its meta-annotation.</li>
     * </ul>
     * If {@link #isResolved()} returns {@code false}, this method returns the original wrapped annotation object.
     *
     * @return The resolved annotation object, or the original annotation object if {@link #isResolved()} is
     *         {@code false}.
     */
    @Override
    public Annotation getResolvedAnnotation() {
        if (!isResolved()) {
            return annotation;
        }
        // Double-checked locking to ensure thread-safe creation of the proxy cache.
        if (Objects.isNull(proxied)) {
            synchronized (this) {
                if (Objects.isNull(proxied)) {
                    proxied = AnnotationMappingProxy.create(annotationType(), this);
                }
            }
        }
        return proxied;
    }

    /**
     * Checks if the annotation has a specific attribute by name and type.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The type of the attribute.
     * @return {@code true} if the attribute exists, {@code false} otherwise.
     */
    public boolean hasAttribute(final String attributeName, final Class<?> attributeType) {
        return getAttributeIndex(attributeName, attributeType) != NOT_FOUND_INDEX;
    }

    /**
     * Checks if an attribute exists at the given index.
     *
     * @param index The index of the attribute.
     * @return {@code true} if an attribute exists at the index, {@code false} otherwise.
     */
    public boolean hasAttribute(final int index) {
        return index != NOT_FOUND_INDEX && Objects.nonNull(ArrayKit.get(attributes, index));
    }

    /**
     * Retrieves the index of an annotation attribute by its name and type.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The type of the attribute.
     * @return The index of the attribute, or {@link #NOT_FOUND_INDEX} if not found.
     */
    public int getAttributeIndex(final String attributeName, final Class<?> attributeType) {
        for (int i = 0; i < attributes.length; i++) {
            final Method attribute = attributes[i];
            if (CharsBacker.equals(attribute.getName(), attributeName)
                    && ClassKit.isAssignable(attributeType, attribute.getReturnType())) {
                return i;
            }
        }
        return NOT_FOUND_INDEX;
    }

    /**
     * Retrieves an annotation attribute (method) by its index.
     *
     * @param index The index of the attribute.
     * @return The {@link Method} representing the attribute, or {@code null} if the index is out of bounds.
     */
    public Method getAttribute(final int index) {
        return ArrayKit.get(attributes, index);
    }

    /**
     * Retrieves the value of a specific attribute from the original annotation.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @param <R>           The return type of the attribute value.
     * @return The attribute value, or {@code null} if the attribute is not found or its type is incompatible.
     */
    @Override
    public <R> R getAttributeValue(final String attributeName, final Class<R> attributeType) {
        return getAttributeValue(getAttributeIndex(attributeName, attributeType));
    }

    /**
     * Retrieves the value of an attribute by its index from the original annotation.
     *
     * @param index The index of the attribute.
     * @param <R>   The return type of the attribute value.
     * @return The attribute value, or {@code null} if the attribute does not exist at the given index.
     */
    public <R> R getAttributeValue(final int index) {
        return hasAttribute(index) ? MethodKit.invoke(annotation, attributes[index]) : null;
    }

    /**
     * Retrieves the resolved value of a specific attribute, applying aliasing and overriding rules.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @param <R>           The return type of the attribute value.
     * @return The resolved attribute value, or {@code null} if the attribute is not found or its type is incompatible.
     */
    @Override
    public <R> R getResolvedAttributeValue(final String attributeName, final Class<R> attributeType) {
        return getResolvedAttributeValue(getAttributeIndex(attributeName, attributeType));
    }

    /**
     * Retrieves the resolved value of an attribute by its index, applying aliasing and overriding rules.
     *
     * @param index The index of the attribute.
     * @param <R>   The return type of the attribute value.
     * @return The resolved attribute value, or {@code null} if the attribute does not exist at the given index.
     */
    public <R> R getResolvedAttributeValue(final int index) {
        if (!hasAttribute(index)) {
            return null;
        }
        // If the attribute has not been resolved, return its original value.
        final int resolvedIndex = resolvedAttributes[index];
        if (resolvedIndex == NOT_FOUND_INDEX) {
            return getAttributeValue(index);
        }
        // If the attribute has been resolved and its source is within the current instance (i.e., it's an alias),
        // then get the value from the actual attribute.
        final ResolvedAnnotationMapping attributeSource = resolvedAttributeSources[index];
        if (Objects.isNull(attributeSource)) {
            return getAttributeValue(resolvedIndex);
        }
        // If the attribute has been resolved and its source is a meta-annotation,
        // then get the resolved value from that meta-annotation.
        return attributeSource.getResolvedAttributeValue(resolvedIndex);
    }

    /**
     * Overrides attributes in the current annotation with attributes from a child annotation (specified by
     * {@code annotationAttributes}) that have the same name and type. This step must be performed after
     * {@link #resolveAliasAttributes()}.
     */
    private void resolveOverwriteAttributes() {
        if (Objects.isNull(source)) {
            return;
        }
        // Get all child annotations except itself.
        final Deque<ResolvedAnnotationMapping> sources = new LinkedList<>();
        final Set<Class<? extends Annotation>> accessed = new HashSet<>();
        accessed.add(this.annotationType());
        ResolvedAnnotationMapping sourceMapping = this.source;
        while (Objects.nonNull(sourceMapping)) {
            // Check for circular dependencies.
            Assert.isFalse(
                    accessed.contains(sourceMapping.annotationType()),
                    "circular dependency between [{}] and [{}]",
                    annotationType(),
                    sourceMapping.annotationType());
            sources.addFirst(sourceMapping);
            accessed.add(sourceMapping.annotationType());
            sourceMapping = sourceMapping.source;
        }
        // Starting from the root annotation, child annotations successively override values in the current annotation.
        for (final ResolvedAnnotationMapping mapping : sources) {
            updateResolvedAttributesByOverwrite(mapping);
        }
    }

    /**
     * 令{@code annotationAttributes}中属性覆写当前注解中同名同类型且未被覆写的属性
     *
     * @param overwriteMapping 注解属性聚合
     */
    private void updateResolvedAttributesByOverwrite(final ResolvedAnnotationMapping overwriteMapping) {
        for (int overwriteIndex = 0; overwriteIndex < overwriteMapping.getAttributes().length; overwriteIndex++) {
            final Method overwrite = overwriteMapping.getAttribute(overwriteIndex);
            for (int targetIndex = 0; targetIndex < attributes.length; targetIndex++) {
                final Method attribute = attributes[targetIndex];
                // 覆写的属性与被覆写的属性名称与类型必须一致
                if (!CharsBacker.equals(attribute.getName(), overwrite.getName())
                        || !ClassKit.isAssignable(attribute.getReturnType(), overwrite.getReturnType())) {
                    continue;
                }
                // 若目标属性未被覆写，则覆写其属性
                overwriteAttribute(overwriteMapping, overwriteIndex, targetIndex, true);
            }
        }
    }

    /**
     * Overrides attributes in the current annotation with attributes from the {@code overwriteMapping} that have the
     * same name and type and have not yet been overridden.
     *
     * @param overwriteMapping The annotation mapping containing attributes to override with.
     * @param overwriteIndex   The index of the overriding attribute in {@code overwriteMapping.attributes}.
     * @param targetIndex      The index of the target attribute in {@code this.attributes}.
     * @param overwriteAliases {@code true} if aliases of the target attribute should also be overridden, {@code false}
     *                         otherwise.
     */
    private void overwriteAttribute(
            final ResolvedAnnotationMapping overwriteMapping,
            final int overwriteIndex,
            final int targetIndex,
            final boolean overwriteAliases) {
        // If the target attribute has already been overridden, do not override it again.
        if (isOverwrittenAttribute(targetIndex)) {
            return;
        }
        // Override the attribute.
        resolvedAttributes[targetIndex] = overwriteIndex;
        resolvedAttributeSources[targetIndex] = overwriteMapping;
        // If the overridden attribute itself has aliases, override the aliased attributes as well.
        if (overwriteAliases && Objects.nonNull(aliasSets[targetIndex])) {
            aliasSets[targetIndex]
                    .forEach(aliasIndex -> overwriteAttribute(overwriteMapping, overwriteIndex, aliasIndex, false));
        }
    }

    /**
     * Checks if the attribute at the given index has been overridden.
     *
     * @param index The index of the attribute.
     * @return {@code true} if the attribute has been overridden, {@code false} otherwise.
     */
    private boolean isOverwrittenAttribute(final int index) {
        // If the attribute has not been resolved, it has not been overridden.
        return NOT_FOUND_INDEX != resolvedAttributes[index]
                // If the attribute has been resolved and points to another instance, it has been overridden.
                && Objects.nonNull(resolvedAttributeSources[index]);
    }

    /**
     * Resolves attributes within the current annotation that are aliased via {@link Alias}.
     */
    private void resolveAliasAttributes() {
        final Map<Method, Integer> attributeIndexes = new HashMap<>(attributes.length);

        final Graph<Method> methodGraph = new Graph<>();
        // Parse aliased attributes and build an adjacency list based on their relationships.
        for (int i = 0; i < attributes.length; i++) {
            // Get the @Alias annotation on the attribute.
            final Method attribute = attributes[i];
            attributeIndexes.put(attribute, i);
            final Alias attributeAnnotation = attribute.getAnnotation(Alias.class);
            if (Objects.isNull(attributeAnnotation)) {
                continue;
            }
            // Get the aliased attribute. It must exist in the current annotation.
            final Method aliasAttribute = getAliasAttribute(attribute, attributeAnnotation);
            Objects.requireNonNull(aliasAttribute);
            methodGraph.putEdge(aliasAttribute, attribute);
        }

        // Traverse the adjacency list using breadth-first search to group nodes belonging to the same graph
        // and create an AliasSet for each group.
        final Set<Method> accessed = new HashSet<>(attributes.length);
        final Set<Method> group = new LinkedHashSet<>();
        final Deque<Method> deque = new LinkedList<>();
        for (final Method target : methodGraph.keySet()) {
            group.clear();
            deque.addLast(target);
            while (!deque.isEmpty()) {
                final Method curr = deque.removeFirst();
                // Skip already accessed nodes.
                if (accessed.contains(curr)) {
                    continue;
                }
                accessed.add(curr);
                // Add to the relationship group.
                group.add(curr);
                final Collection<Method> aliases = methodGraph.getAdjacentPoints(curr);
                if (CollKit.isNotEmpty(aliases)) {
                    deque.addAll(aliases);
                }
            }
            // Build alias relationships for nodes in the same group.
            final int[] groupIndexes = group.stream().mapToInt(attributeIndexes::get).toArray();
            updateAliasSetsForAliasGroup(groupIndexes);
        }

        // Update resolved attributes based on AliasSet.
        Stream.of(aliasSets).filter(Objects::nonNull).forEach(set -> {
            final int effectiveAttributeIndex = set.determineEffectiveAttribute();
            set.forEach(index -> resolvedAttributes[index] = effectiveAttributeIndex);
        });
    }

    /**
     * Retrieves and performs basic validation on an aliased attribute.
     *
     * @param attribute           The attribute method that has the {@link Alias} annotation.
     * @param attributeAnnotation The {@link Alias} annotation itself.
     * @return The {@link Method} representing the aliased attribute.
     * @throws IllegalArgumentException if the aliased attribute cannot be found, aliases itself, or has an incompatible
     *                                  return type.
     */
    private Method getAliasAttribute(final Method attribute, final Alias attributeAnnotation) {
        // Get the index of the aliased attribute. It must exist in the current annotation.
        final int aliasAttributeIndex = getAttributeIndex(attributeAnnotation.value(), attribute.getReturnType());
        Assert.isTrue(
                hasAttribute(aliasAttributeIndex),
                "can not find alias attribute [{}] in [{}]",
                attributeAnnotation.value(),
                this.annotation.annotationType());

        // Get the specific aliased attribute. It cannot be the attribute itself.
        final Method aliasAttribute = getAttribute(aliasAttributeIndex);
        Assert.notEquals(aliasAttribute, attribute, "attribute [{}] can not alias for itself", attribute);

        // Aliased attributes must have compatible return types.
        Assert.isAssignable(
                attribute.getReturnType(),
                aliasAttribute.getReturnType(),
                "aliased attributes [{}] and [{}] must have same return type",
                attribute,
                aliasAttribute);
        return aliasAttribute;
    }

    /**
     * Creates and assigns an {@link AliasSet} for a group of aliased attributes.
     *
     * @param groupIndexes An array of indices of attributes that form an alias group.
     */
    private void updateAliasSetsForAliasGroup(final int[] groupIndexes) {
        final AliasSet set = new AliasSet(groupIndexes);
        for (final int index : groupIndexes) {
            aliasSets[index] = set;
        }
    }

    /**
     * Compares this instance with the specified object for equality. Two {@code ResolvedAnnotationMapping} instances
     * are considered equal if their wrapped annotations and resolved status are equal.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResolvedAnnotationMapping that = (ResolvedAnnotationMapping) o;
        return resolved == that.resolved && annotation.equals(that.annotation);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(annotation, resolved);
    }

    /**
     * Represents a set of aliased attributes. All attributes in a group share the same instance of this class.
     */
    private class AliasSet {

        /**
         * The indices of the aliased attributes in the {@link ResolvedAnnotationMapping#attributes} array.
         */
        final int[] indexes;

        /**
         * Constructs a new {@code AliasSet}.
         *
         * @param indexes The indices of the mutually aliased attributes.
         */
        AliasSet(final int[] indexes) {
            this.indexes = indexes;
        }

        /**
         * Determines the single effective attribute from all associated aliased attributes.
         * <ul>
         * <li>If all attributes have only default values, all default values must be equal. If so, the first attribute
         * is returned; otherwise, an error is thrown.</li>
         * <li>If exactly one attribute has a non-default value, that attribute is returned.</li>
         * <li>If multiple attributes have non-default values, all non-default values must be equal. If so, the first
         * attribute with a non-default value is returned; otherwise, an error is thrown.</li>
         * </ul>
         *
         * @return The index of the effective attribute.
         * @throws IllegalArgumentException if aliased attributes have conflicting non-default values or inconsistent
         *                                  default values.
         */
        private int determineEffectiveAttribute() {
            int resolvedIndex = NOT_FOUND_INDEX;
            boolean hasNotDef = false;
            Object lastValue = null;
            for (final int index : indexes) {
                final Method attribute = attributes[index];

                // Get the attribute's value and determine if it's the default value.
                final Object def = attribute.getDefaultValue();
                final Object undef = MethodKit.invoke(annotation, attribute);
                final boolean isDefault = Objects.equals(def, undef);

                // If this is the first attribute being processed in the group.
                if (resolvedIndex == NOT_FOUND_INDEX) {
                    resolvedIndex = index;
                    lastValue = isDefault ? def : undef;
                    hasNotDef = !isDefault;
                    continue;
                }

                // If this is not the first attribute, and a non-default value has already been found.
                if (hasNotDef) {
                    // If the current attribute also has a non-default value, they must be equal.
                    if (!isDefault) {
                        Assert.isTrue(
                                Objects.equals(lastValue, undef),
                                "aliased attribute [{}] and [{}] must have same not default value, but is different: [{}] <==> [{}]",
                                attributes[resolvedIndex],
                                attribute,
                                lastValue,
                                undef);
                    }
                    // Otherwise, skip and continue to use the previous non-default value.
                    continue;
                }

                // If this is not the first attribute, but no non-default value has been found yet,
                // and the current value is non-default, update the effective value and index.
                if (!isDefault) {
                    hasNotDef = true;
                    lastValue = undef;
                    resolvedIndex = index;
                    continue;
                }

                // If this is not the first attribute, no non-default value has been found yet,
                // and the current value is also a default value, then all default values must be equal.
                Assert.isTrue(
                        Objects.equals(lastValue, def),
                        "aliased attribute [{}] and [{}] must have same default value, but is different: [{}] <==> [{}]",
                        attributes[resolvedIndex],
                        attribute,
                        lastValue,
                        def);
            }
            Assert.isFalse(
                    resolvedIndex == NOT_FOUND_INDEX,
                    "can not resolve aliased attributes from [{}]",
                    annotation);
            return resolvedIndex;
        }

        /**
         * Performs the given action for each index in this set.
         *
         * @param consumer The action to be performed for each index.
         */
        void forEach(final IntConsumer consumer) {
            for (final int index : indexes) {
                consumer.accept(index);
            }
        }

    }

}
