package org.aoju.bus.core.builder;

import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * <p>
 * A {@code Diff} contains the differences between two {@link Diffable} class
 * fields.
 * </p>
 *
 * <p>
 * Typically, {@code Diff}s are retrieved by using a {@link DiffBuilder} to
 * produce a {@link DiffResult}, containing the differences between two objects.
 * </p>
 *
 * @param <T> The type of object contained within this {@code Diff}. Differences
 *            between primitive objects are stored as their Object wrapper
 *            equivalent.
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class Diff<T> extends Pair<T, T> {

    private final Type type;
    private final String fieldName;

    /**
     * <p>
     * Constructs a new {@code Diff} for the given field name.
     * </p>
     *
     * @param fieldName the name of the field
     */
    protected Diff(final String fieldName) {
        this.type = ObjectUtils.defaultIfNull(
                TypeUtils.getTypeArguments(getClass(), Diff.class).get(
                        Diff.class.getTypeParameters()[0]), Object.class);
        this.fieldName = fieldName;
    }

    /**
     * <p>
     * Returns the type of the field.
     * </p>
     *
     * @return the field type
     */
    public final Type getType() {
        return type;
    }

    /**
     * <p>
     * Returns the name of the field.
     * </p>
     *
     * @return the field name
     */
    public final String getFieldName() {
        return fieldName;
    }

    /**
     * <p>
     * Returns a {@code String} representation of the {@code Diff}, with the
     * following format:</p>
     *
     * <pre>
     * [fieldname: left-value, right-value]
     * </pre>
     *
     * @return the string representation
     */
    @Override
    public final String toString() {
        return String.format("[%s: %s, %s]", fieldName, getLeft(), getRight());
    }

    /**
     * <p>
     * Throws {@code UnsupportedOperationException}.
     * </p>
     *
     * @param value ignored
     * @return nothing
     */
    @Override
    public final T setValue(final T value) {
        throw new UnsupportedOperationException("Cannot alter Diff object.");
    }

}
